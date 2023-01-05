package my.test.proxy.client;

import my.test.proxy.server.SftpSubsystemExtend;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.SshException;
import org.apache.sshd.common.io.IoOutputStream;
import org.apache.sshd.common.io.IoWriteFuture;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.util.GenericUtils;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;
import org.apache.sshd.core.CoreModuleProperties;
import org.apache.sshd.sftp.client.SftpErrorDataHandler;
import org.apache.sshd.sftp.client.SftpVersionSelector;
import org.apache.sshd.sftp.client.impl.DefaultSftpClient;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.Duration;
import java.time.Instant;

public class DefaultSftpClientExtend extends DefaultSftpClient {

    private SingleSftpClient singleSftpClient;
    private SftpSubsystemExtend sftpSubsystemExtend;

    public void setProxySingleSftpClient(SingleSftpClient singleSftpClient) {
        this.singleSftpClient = singleSftpClient;
    }

    public DefaultSftpClientExtend(ClientSession clientSession, SftpVersionSelector initialVersionSelector, SftpErrorDataHandler errorDataHandler) throws IOException {
        super(clientSession, initialVersionSelector, errorDataHandler);
    }

    public void setProxySftpSubsystemExtend(SftpSubsystemExtend sftpSubsystemExtend) {
        this.sftpSubsystemExtend = sftpSubsystemExtend;
    }


    @Override
    protected int data(byte[] buf, int start, int len) throws IOException {
        System.out.println("■■■■■■■■■■■Client■■ data ■■■■■■■■■■■■■■■");
        System.out.println(new String(buf, start, len));
        System.out.println();
        if (sftpSubsystemExtend != null && singleSftpClient.isAuthenticationSuccessClientSession && singleSftpClient.serverAuthenticated) {
            Buffer send = new ByteArrayBuffer(buf, start, len);
            sftpSubsystemExtend.publicSend(send);
            return 0;
        } else {
            return super.data(buf, start, len);
        }
    }

    // Target Server 로 보내기
    @Override
    public int send(int cmd, Buffer buffer) throws IOException {
        System.out.println("■■■■■■■■■■■Client■■ send ■■■■■■■■■■■■■■■");
        return super.send(cmd, buffer);
    }

    public void publicSend(byte[] buf, int start, int len) {

    }

    @Override
    protected void process(Buffer incoming) throws IOException {
        System.out.println("■■■■■■■■■■■Client■■ process ■■■■■■■■■■■■■■■");
        super.process(incoming);
    }

    /* receive */
    @Override
    protected boolean receive(Buffer incoming) throws IOException {
        System.out.println("■■■■■■■■■■■Client■■ receive - protected ■■■■■■■■■■■■■■■");
        System.out.println(new String(incoming.array()));
        System.out.println("");
        return super.receive(incoming);
    }

    @Override
    public Buffer receive(int id) throws IOException {
        System.out.println("■■■■■■■■■■■Client■■ receive1 - override■■■■■■■■■■■■■■■");

        Session session = getClientSession();
        Duration idleTimeout = CoreModuleProperties.IDLE_TIMEOUT.getRequired(session);
        if (GenericUtils.isNegativeOrNull(idleTimeout)) {
            idleTimeout = CoreModuleProperties.IDLE_TIMEOUT.getRequiredDefault();
        }

        Instant now = Instant.now();
        Instant waitEnd = now.plus(idleTimeout);
        boolean traceEnabled = log.isTraceEnabled();
        for (int count = 1; ; count++) {
            if (isClosing() || (!isOpen())) {
                throw new SshException("Channel is being closed");
            }
            if (now.compareTo(waitEnd) > 0) {
                throw new SshException("Timeout expired while waiting for id=" + id);
            }

            Buffer buffer = receive(id, Duration.between(now, waitEnd));
            if (buffer != null) {
                System.out.println(new String(buffer.array()));
                return buffer;
            }

            now = Instant.now();
            if (traceEnabled) {
                log.trace("receive({}) check iteration #{} for id={} remain time={}", this, count, id, idleTimeout);
            }
        }
    }

    @Override
    public Buffer receive(int id, long idleTimeout) throws IOException {
        System.out.println("■■■■■■■■■■■Client■■ receive2 - long - Override ■■■■■■■■■■■■■■■");
        return super.receive(id, idleTimeout);
    }

    @Override
    public Buffer receive(int id, Duration idleTimeout) throws IOException {
        System.out.println("■■■■■■■■■■■Client■■ receive2 - Duration - Override ■■■■■■■■■■■■■■■");

        synchronized (messages) {
            System.out.println("■■■■■■■■■■■Client■■ messages ExtendClient ■■■■■■■■■■■");
            Buffer buffer = messages.remove(id);
            if (buffer != null) {
                System.out.println(new String(buffer.array()));
                return buffer;
            }
            if (GenericUtils.isPositive(idleTimeout)) {
                try {
                    messages.wait(idleTimeout.toMillis(), idleTimeout.getNano() % 1_000_000);
                } catch (InterruptedException e) {
                    throw (IOException) new InterruptedIOException("Interrupted while waiting for messages").initCause(e);
                }
            }
        }
        return null;
    }


}
