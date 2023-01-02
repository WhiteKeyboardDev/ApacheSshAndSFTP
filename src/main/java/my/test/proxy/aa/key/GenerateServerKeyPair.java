package my.test.proxy.aa.key;

import org.apache.sshd.common.NamedResource;
import org.apache.sshd.common.session.SessionContext;

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;

public class GenerateServerKeyPair extends AbstractDefaultKeyPairProvider {

    protected Iterable<KeyPair> doReadKeyPairs(SessionContext session, NamedResource resourceKey, InputStream inputStream) throws IOException, GeneralSecurityException {
        ObjectInputStream r = new ObjectInputStream(inputStream);

        KeyPair kp;
        try {
            try {
                kp = (KeyPair)r.readObject();
            } catch (ClassNotFoundException var9) {
                throw new InvalidKeySpecException("Missing classes: " + var9.getMessage(), var9);
            }
        } catch (Throwable var10) {
            try {
                r.close();
            } catch (Throwable var8) {
                var10.addSuppressed(var8);
            }

            throw var10;
        }

        r.close();
        return Collections.singletonList(kp);
    }

    protected void doWriteKeyPair(NamedResource resourceKey, KeyPair kp, OutputStream outputStream) throws IOException, GeneralSecurityException {
        ObjectOutputStream w = new ObjectOutputStream(outputStream);

        try {
            w.writeObject(kp);
        } catch (Throwable var8) {
            try {
                w.close();
            } catch (Throwable var7) {
                var8.addSuppressed(var7);
            }

            throw var8;
        }

        w.close();
    }
}
