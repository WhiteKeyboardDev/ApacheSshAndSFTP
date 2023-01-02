package my.test.proxy.aa.key;

import org.apache.sshd.common.AlgorithmNameProvider;
import org.apache.sshd.common.NamedResource;
import org.apache.sshd.common.cipher.ECCurves;
import org.apache.sshd.common.config.keys.BuiltinIdentities;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.keyprovider.AbstractKeyPairProvider;
import org.apache.sshd.common.keyprovider.KeySizeIndicator;
import org.apache.sshd.common.session.SessionContext;
import org.apache.sshd.common.util.GenericUtils;
import org.apache.sshd.common.util.OsUtils;
import org.apache.sshd.common.util.io.IoUtils;
import org.apache.sshd.common.util.io.resource.PathResource;
import org.apache.sshd.common.util.security.SecurityUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractDefaultKeyPairProvider extends AbstractKeyPairProvider implements AlgorithmNameProvider, KeySizeIndicator {
    public static final String DEFAULT_ALGORITHM = KeyUtils.RSA_ALGORITHM;
    public static final boolean DEFAULT_ALLOWED_TO_OVERWRITE = true;

    private final AtomicReference<Iterable<KeyPair>> keyPairHolder = new AtomicReference<>();

    private Path path;
    private String algorithm = DEFAULT_ALGORITHM;
    private int keySize;
    private AlgorithmParameterSpec keySpec;
    private boolean overwriteAllowed = DEFAULT_ALLOWED_TO_OVERWRITE;

    protected AbstractDefaultKeyPairProvider() {
        super();
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = (path == null) ? null : path.toAbsolutePath();
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public int getKeySize() {
        return keySize;
    }

    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    public AlgorithmParameterSpec getKeySpec() {
        return keySpec;
    }

    public void setKeySpec(AlgorithmParameterSpec keySpec) {
        this.keySpec = keySpec;
    }

    public boolean isOverwriteAllowed() {
        return overwriteAllowed;
    }

    public void setOverwriteAllowed(boolean overwriteAllowed) {
        this.overwriteAllowed = overwriteAllowed;
    }

    public void clearLoadedKeys() {
        Iterable<KeyPair> ids;
        synchronized (keyPairHolder) {
            ids = keyPairHolder.getAndSet(null);
        }

        if ((ids != null) && log.isDebugEnabled()) {
            log.debug("clearLoadedKeys({}) removed keys", getPath());
        }
    }

    @Override
    public synchronized List<KeyPair> loadKeys(SessionContext session) {
        Path keyPath = getPath();
        Iterable<KeyPair> ids;
        synchronized (keyPairHolder) {
            ids = keyPairHolder.get();
            if (ids == null) {
                try {
                    ids = resolveKeyPairs(session, keyPath);
                    if (ids != null) {
                        keyPairHolder.set(ids);
                    }
                } catch (Exception t) {
                    warn("loadKeys({}) Failed ({}) to resolve: {}",
                            keyPath, t.getClass().getSimpleName(), t.getMessage(), t);
                }
            }
        }

        List<KeyPair> pairs = Collections.emptyList();
        if (ids instanceof List<?>) {
            pairs = (List<KeyPair>) ids;
        } else if (ids != null) {
            pairs = new ArrayList<>();
            for (KeyPair kp : ids) {
                if (kp == null) {
                    continue;
                }

                pairs.add(kp);
            }
        }

        return pairs;
    }

    protected Iterable<KeyPair> resolveKeyPairs(SessionContext session, Path keyPath)
            throws IOException, GeneralSecurityException {
        String alg = getAlgorithm();
        if (keyPath != null) {
            try {
                Iterable<KeyPair> ids = loadFromFile(session, alg, keyPath);
                KeyPair kp = GenericUtils.head(ids);
                if (kp != null) {
                    return ids;
                }
            } catch (Exception e) {
                warn("resolveKeyPair({}) Failed ({}) to load: {}",
                        keyPath, e.getClass().getSimpleName(), e.getMessage(), e);
            }
        }

        // either no file specified or no key in file
        KeyPair kp = null;
        try {
            kp = generateKeyPair(alg);
            if (kp == null) {
                return null;
            }

            if (log.isDebugEnabled()) {
                PublicKey key = kp.getPublic();
                log.debug("resolveKeyPair({}) generated {} key={}-{}",
                        keyPath, alg, KeyUtils.getKeyType(key), KeyUtils.getFingerPrint(key));
            }
        } catch (Exception e) {
            warn("resolveKeyPair({})[{}] Failed ({}) to generate {} key-pair: {}",
                    keyPath, alg, e.getClass().getSimpleName(), alg, e.getMessage(), e);
            return null;
        }

        if (keyPath != null) {
            try {
                writeKeyPair(kp, keyPath);
            } catch (Exception e) {
                warn("resolveKeyPair({})[{}] Failed ({}) to write {} key: {}",
                        alg, keyPath, e.getClass().getSimpleName(), alg, e.getMessage(), e);
            }
        }

        return Collections.singletonList(kp);
    }

    protected Iterable<KeyPair> loadFromFile(SessionContext session, String alg, Path keyPath)
            throws IOException, GeneralSecurityException {
        LinkOption[] options = IoUtils.getLinkOptions(true);
        if ((!Files.exists(keyPath, options)) || (!Files.isRegularFile(keyPath, options))) {
            return null;
        }

        Iterable<KeyPair> ids = readKeyPairs(session, keyPath, IoUtils.EMPTY_OPEN_OPTIONS);
        KeyPair kp = GenericUtils.head(ids);
        if (kp == null) {
            return null;
        }

        // Assume all keys are of same type
        PublicKey key = kp.getPublic();
        String keyAlgorithm = key.getAlgorithm();
        if (BuiltinIdentities.Constants.ECDSA.equalsIgnoreCase(keyAlgorithm)) {
            keyAlgorithm = KeyUtils.EC_ALGORITHM;
        } else if (BuiltinIdentities.Constants.ED25519.equalsIgnoreCase(keyAlgorithm)) {
            keyAlgorithm = SecurityUtils.EDDSA;
        }

        if (Objects.equals(alg, keyAlgorithm)) {
            if (log.isDebugEnabled()) {
                log.debug("resolveKeyPair({}) loaded key={}-{}",
                        keyPath, KeyUtils.getKeyType(key), KeyUtils.getFingerPrint(key));
            }
            return ids;
        }

        // Not same algorithm - start again
        if (log.isDebugEnabled()) {
            log.debug("resolveKeyPair({}) mismatched loaded key algorithm: expected={}, loaded={}",
                    keyPath, alg, keyAlgorithm);
        }
        Files.deleteIfExists(keyPath);
        return null;
    }

    protected Iterable<KeyPair> readKeyPairs(SessionContext session, Path keyPath, OpenOption... options)
            throws IOException, GeneralSecurityException {
        PathResource location = new PathResource(keyPath, options);
        try (InputStream inputStream = location.openInputStream()) {
            return doReadKeyPairs(session, location, inputStream);
        }
    }

    protected Iterable<KeyPair> doReadKeyPairs(SessionContext session, NamedResource resourceKey, InputStream inputStream)
            throws IOException, GeneralSecurityException {
        return SecurityUtils.loadKeyPairIdentities(session, resourceKey, inputStream, null);
    }

    protected void writeKeyPair(KeyPair kp, Path keyPath)
            throws IOException, GeneralSecurityException {
        Objects.requireNonNull(kp, "No host key");
        if (!Files.exists(keyPath) || isOverwriteAllowed()) {
            // Create an empty file or truncate an existing file
            Files.newOutputStream(keyPath).close();
            setFilePermissions(keyPath);
            try (OutputStream os = Files.newOutputStream(keyPath, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                doWriteKeyPair(new PathResource(keyPath), kp, os);
            } catch (Exception e) {
                error("writeKeyPair({}) failed ({}) to write {} host key : {}",
                        keyPath, e.getClass().getSimpleName(),
                        KeyUtils.getKeyType(kp), e.getMessage(), e);
            }
        } else {
            log.warn("Overwriting host key ({}) is disabled: using throwaway {} key: {}",
                    keyPath, KeyUtils.getKeyType(kp), KeyUtils.getFingerPrint(kp.getPublic()));
        }
    }

    private void setFilePermissions(Path path) throws IOException {
        Throwable t = null;
        if (OsUtils.isWin32()) {
            AclFileAttributeView view = Files.getFileAttributeView(path, AclFileAttributeView.class);
            UserPrincipal owner = Files.getOwner(path);
            if (view != null && owner != null) {
                try {
                    // Remove all access rights from non-owners.
                    List<AclEntry> restricted = new ArrayList<>();
                    for (AclEntry acl : view.getAcl()) {
                        if (owner.equals(acl.principal()) || AclEntryType.DENY.equals(acl.type())) {
                            restricted.add(acl);
                        } else {
                            // We can't use DENY access: if the owner is member of a group and we deny the group
                            // access, the owner won't be able to perform the access. Instead of denying permissions
                            // simply allow nothing.
                            restricted.add(AclEntry.newBuilder()
                                    .setType(AclEntryType.ALLOW)
                                    .setPrincipal(acl.principal())
                                    .setPermissions(Collections.emptySet())
                                    .build());
                        }
                    }
                    view.setAcl(restricted);
                    return;
                } catch (IOException | SecurityException e) {
                    t = e;
                }
            }
        } else {
            File file = path.toFile();
            if (!file.setExecutable(false)) {
                log.debug("Host key file {}: cannot set non-executable", path);
            }

            boolean success = file.setWritable(false, false) && file.setWritable(true, true);
            success = file.setReadable(false, false) && file.setReadable(true, true) && success;
            if (success) {
                return;
            }
        }
        log.warn("Host key file {}: cannot set file permissions correctly (readable and writeable only by owner)", path, t);
    }

    protected abstract void doWriteKeyPair(
            NamedResource resourceKey, KeyPair kp, OutputStream outputStream)
            throws IOException, GeneralSecurityException;

    protected KeyPair generateKeyPair(String algorithm) throws GeneralSecurityException {
        KeyPairGenerator generator = SecurityUtils.getKeyPairGenerator(algorithm);
        if (keySpec != null) {
            generator.initialize(keySpec);
            log.info("generateKeyPair({}) generating host key - spec={}", algorithm, keySpec.getClass().getSimpleName());
        } else if (KeyUtils.EC_ALGORITHM.equals(algorithm)) {
            ECCurves curve;
            // If left to our own devices choose the biggest key size possible
            if (keySize == 0) {
                int numCurves = ECCurves.SORTED_KEY_SIZE.size();
                curve = ECCurves.SORTED_KEY_SIZE.get(numCurves - 1);
            } else {
                curve = ECCurves.fromCurveSize(keySize);
                if (curve == null) {
                    throw new InvalidKeyException("No match found for curve with key size=" + keySize);
                }
            }
            generator.initialize(curve.getParameters());
            log.info("generateKeyPair({}) generating host key={}", algorithm, curve);
        } else if (keySize != 0) {
            generator.initialize(keySize);
            log.info("generateKeyPair({}) generating host key - size={}", algorithm, keySize);
        } else {
            log.info("generateKeyPair({}) generating host key", algorithm);
        }

        return generator.generateKeyPair();
    }
}
