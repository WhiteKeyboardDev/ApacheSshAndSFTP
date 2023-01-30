package my.test.proxy.server;

import my.test.proxy.client.SingleSftpClient;
import org.apache.sshd.common.channel.LocalWindow;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.common.util.buffer.BufferUtils;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.sftp.server.SftpSubsystem;
import org.apache.sshd.sftp.server.SftpSubsystemConfigurator;

import java.io.IOException;

public class SftpSubsystemExtend extends SftpSubsystem {

    SingleSftpClient singleSftpClient;

    public void setSingleSftpClient(SingleSftpClient singleSftpClient) {
        this.singleSftpClient = singleSftpClient;
    }

    public SftpSubsystemExtend(ChannelSession channel, SftpSubsystemConfigurator configurator) {
        super(channel, configurator);
    }

    static int tmptmp = 0;

    public void publicSend(byte[] buf, int start, int len) throws IOException {
        System.out.println("■■■■■■■■■■■Server■■ publicSend ■■■■■■■■■■■■■■■");

        Buffer tmpBuffer = new ByteArrayBuffer(buf, start, len);
        BufferUtils.updateLengthPlaceholder(tmpBuffer, 0);
        out.writeBuffer(tmpBuffer);
    }

    static int count = 0;

    @Override
    public void run() {
        int exitCode = 0;
        long buffersCount = 0L;
        try {
            ChannelSession channel = getServerChannelSession();
            LocalWindow localWindow = channel.getLocalWindow();
            while (true) {
                Buffer buffer = requests.take();
                System.out.println("■■■■■■■■■■■Server■■ requests.take() ■■■■■■■■■■■■■■■");
                if (buffer == CLOSE) {
                    break;
                }
                buffersCount++;
                if (count < 1) {
                    count++;
                    process(buffer);
                } else {
                    singleSftpClient.defaultSftpClientExtend.publicSend(buffer);
                }

                localWindow.check();
            }
        } catch (Throwable t) {
            if (!closed.get()) { // Ignore
                Session session = getServerSession();
                error("run({}) {} caught in SFTP subsystem after {} buffers: {}",
                        session, t.getClass().getSimpleName(), buffersCount, t.getMessage(), t);
                exitCode = -1;
            }
        } finally {
            closeAllHandles();
            callback.onExit(exitCode, exitCode != 0);
        }
    }

    // 처음 데이터 오는 지점. (클라이언트로 보내고)
    @Override
    public int data(ChannelSession channel, byte[] buf, int start, int len) throws IOException {
        super.data(channel, buf, start, len);
//        else {
//            byte[] replyPrepare = {};
//            replyPrepare = Arrays.copyOf(buf, buf.length);
//            Buffer replyBuffer = new ByteArrayBuffer(replyPrepare, start, len);
//
//            int length = replyBuffer.getInt();
//            int type = replyBuffer.getUByte();
//            int id = replyBuffer.getInt();
//            String str = replyBuffer.getString();
//
//            byte[] reply = {};
//            reply = Arrays.copyOfRange(buf, start, len);
//            Buffer replyBufferSend = new ByteArrayBuffer(reply, start, len);
//
//            System.out.println("■■■■■■■■■■■Server■■ data ■■■■■■■■■■■■■■■");
//            System.out.println(new String(buf, start, len));
//            log.debug("process({})[length={}, type={}, id={}] processing",
//                    getSession(), length, SftpConstants.getCommandMessageName(type), id);
//            System.out.println();
//        }
        return 0;
    }
}
