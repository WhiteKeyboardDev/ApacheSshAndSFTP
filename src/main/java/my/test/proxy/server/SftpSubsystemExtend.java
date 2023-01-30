package my.test.proxy.server;

import my.test.proxy.client.SingleSftpClient;
import org.apache.sshd.common.channel.LocalWindow;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.util.buffer.Buffer;
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

    public void publicSend(Buffer buf) throws IOException {
        System.out.println("■■■■■■■■■■■Server■■ publicSend ■■■■■■■■■■■■■■■");

//        BufferUtils.updateLengthPlaceholder(buf, 0);
        out.writeBuffer(buf);
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
        return 0;
    }
}
