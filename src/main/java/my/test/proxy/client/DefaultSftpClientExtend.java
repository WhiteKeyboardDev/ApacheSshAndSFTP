package my.test.proxy.client;

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
import org.apache.sshd.sftp.common.SftpConstants;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.Duration;
import java.time.Instant;

import static my.test.proxy.client.SingleSftpClient.singleSftpClient;

public class DefaultSftpClientExtend extends DefaultSftpClient {

    public DefaultSftpClientExtend(ClientSession clientSession, SftpVersionSelector initialVersionSelector, SftpErrorDataHandler errorDataHandler) throws IOException {
        super(clientSession, initialVersionSelector, errorDataHandler);
    }

    @Override
    protected int data(byte[] buf, int start, int len) throws IOException {
        System.out.println("■■■■■■■■■■■Client■■ data ■■■■■■■■■■■■■■■");
        System.out.println(new String(buf, start, len));
        System.out.println();

        if (singleSftpClient.getSftpSubsystemExtend() != null && singleSftpClient.isAuthenticationSuccessClientSession && singleSftpClient.serverAuthenticated) {
            singleSftpClient.getSftpSubsystemExtend().publicSend(buf, start, len);
            return 0;
        } else {
            return super.data(buf, start, len);
        }
    }

    public void publicSend(Buffer serverBuffer) throws IOException {
        System.out.println("■■■■■■■■■■■Client■■ publicSend ■■■■■■■■■■■■■■■");
        Buffer buffer = new ByteArrayBuffer(serverBuffer.available() + Long.SIZE, false);
        buffer.putBuffer(serverBuffer);

        int rpos = buffer.rpos();
        int length = buffer.getInt();
        int type = buffer.getUByte();
        Integer server_id = buffer.getInt();
        buffer.rpos(rpos);

        log.debug("process({}), server_id={}, type={}, len={}",
            getClientChannel(), server_id, SftpConstants.getCommandMessageName(type), length);

        ClientChannel clientChannel = getClientChannel();
        IoOutputStream asyncIn = clientChannel.getAsyncIn();
        IoWriteFuture writeFuture = asyncIn.writeBuffer(buffer);
        var t = writeFuture.verify();
        t.isWritten();
    }

    /* receive */
    @Override
    protected boolean receive(Buffer incoming) throws IOException {
        System.out.println("■■■■■■■■■■■Client■■ receive - protected ■■■■■■■■■■■■■■■");
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
