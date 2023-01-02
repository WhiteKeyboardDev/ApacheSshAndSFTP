
package my.test.proxy.aa.importProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.sshd.common.channel.PtyMode;
import org.apache.sshd.common.util.logging.AbstractLoggingBean;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.channel.ChannelDataReceiver;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.channel.PuttyRequestHandler;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.InvertedShell;
import org.apache.sshd.server.shell.TtyFilterInputStream;
import org.apache.sshd.server.shell.TtyFilterOutputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class CustomShell extends AbstractLoggingBean implements InvertedShell {
    private static final Logger logger = LogManager.getLogger(CustomShell.class);

    private ServerSession session;
    private ChannelSession channelSession;
    private TtyFilterOutputStream in;
    private TtyFilterInputStream out;
    private TtyFilterInputStream err;
    private ClientDaemon client;
    private OutputStream cin;
    private ByteArrayOutputStream cout;

    private byte[] outbuf = new byte[8192];
    private byte[] errbuf = new byte[8192];
    private ByteArrayInputStream rawout = new ByteArrayInputStream(outbuf);
    private ByteArrayOutputStream rawin = new ByteArrayOutputStream();
    private ByteArrayInputStream rawerr = new ByteArrayInputStream(errbuf);

    public CustomShell(ClientDaemon client) {
        this.client = client;
        this.cin = client.channel.getInvertedIn();
        this.cout = client.out;
        this.client.setShell(this);
    }

    @Override
    public ServerSession getServerSession() {
        return session;
    }

    @Override
    public void setSession(ServerSession session) {
        this.session = Objects.requireNonNull(session, "No server session");
    }

    @Override
    public ChannelSession getServerChannelSession() {
        return channelSession;
    }

    @Override
    public void start(ChannelSession channel, Environment env) throws IOException {
        this.channelSession = channel;

        Map<PtyMode, ?> modes = resolveShellTtyOptions(env.getPtyModes());
        out = new TtyFilterInputStream(rawout, modes);
        err = new TtyFilterInputStream(rawerr, modes);
        in = new TtyFilterOutputStream(rawin, err, modes);

        channel.setDataReceiver(new ChannelDataReceiver() {
            @Override
            public int data(ChannelSession channel, byte[] buf, int start, int len) throws IOException {
                String stringified = new String(Arrays.copyOfRange(buf, start, start + len));
                cin.write(stringified.getBytes(StandardCharsets.UTF_8));
                cin.flush();
                return 0;
            }

            @Override
            public void close() throws IOException {
            }
        });
    }

    protected Map<String, String> resolveShellEnvironment(Map<String, String> env) {
        return env;
    }

    // for some reason these modes provide best results BOTH with Linux SSH client and PUTTY
    protected Map<PtyMode, Integer> resolveShellTtyOptions(Map<PtyMode, Integer> modes) {
        if (PuttyRequestHandler.isPuttyClient(getServerSession())) {
            return PuttyRequestHandler.resolveShellTtyOptions(modes);
        } else {
            return modes;
        }
    }

    @Override
    public OutputStream getInputStream() {
        return in;
    }

    @Override
    public InputStream getOutputStream() {
        return out;
    }

    @Override
    public InputStream getErrorStream() {
        return err;
    }

    @Override
    public boolean isAlive() {
        return client.isAlive();
    }

    @Override
    public int exitValue() {
        return 0;
    }

    @Override
    public void destroy(ChannelSession channel) {
        this.client.stop();
    }
}
