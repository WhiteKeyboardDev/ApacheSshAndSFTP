package my.test.proxy.aa.importProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

public class ClientDaemon implements Runnable {
    private static final Logger logger = LogManager.getLogger(ClientDaemon.class);
    public SshClient client;
    public ClientSession session;
    public AuthFuture auth;
    public ClientChannel channel;

    public byte[] buf = new byte[8192];
    public ByteArrayInputStream in = new ByteArrayInputStream(buf);
    public ByteArrayOutputStream out = new ByteArrayOutputStream();
    public ByteArrayOutputStream err = new ByteArrayOutputStream();
    public CustomShell shell;

    public void setShell(CustomShell shell) {
        this.shell = shell;
    }

    public void create() throws IOException {
        client = SshClient.setUpDefaultClient();
        client.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE);
        client.start();

        ConnectFuture conSync = client
            .connect("192.168.5.102:22")
            .verify(5000);

        session = conSync.getSession();
        session.setUsername("root");
        session.addPasswordIdentity("1234");

        auth = session
            .auth()
            .verify(10000);

        if (!auth.isDone() || auth.isFailure()) {
            throw new IOException("Authentication failed.");
        }

        channel = session.createShellChannel();

        channel.setOut(out);
        channel.setErr(err);

        channel.open().verify(5000);
        channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 5000);

        new Thread(this).start();
    }

    @Override
    public void run() {
        while (this.client != null && this.client.isOpen()) {
            try {
                if (0 < out.size()) {
                    logger.info(" >>>>>>>> OUTPUT: " + new String(out.toByteArray(), StandardCharsets.UTF_8));

                    if (this.shell != null) {
                        shell.getInputStream().write(out.toByteArray());
                    }

                    out.flush();
                    out.reset();
                }

                Thread.sleep(10);
            }
            catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public boolean isAlive() {
        return this.client != null && this.client.isOpen();
    }

    public void stop() {
        if (this.client != null && this.client.isOpen()) {
            this.client.stop();
        }
    }
}
