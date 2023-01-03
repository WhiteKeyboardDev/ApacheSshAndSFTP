package my.test.proxy.server;

import my.test.proxy.client.SingleSftpClient;
import org.apache.sshd.common.SshConstants;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.common.util.buffer.BufferUtils;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.common.SftpConstants;
import org.apache.sshd.sftp.server.SftpEventListener;
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

    // 데이터 받아서 처리하는 지점. (클라이언트에서 여기를 호출, 결과값)
    @Override
    protected void process(Buffer buffer) throws IOException {
        System.out.println("■■■■■■■■■■■Server■■ process ■■■■■■■■■■■■■■■");
        ServerSession session = getServerSession();
        int length = buffer.getInt();
        int type = buffer.getUByte();
        int id = buffer.getInt();
        if (log.isDebugEnabled()) {
            log.debug("process({})[length={}, type={}, id={}] processing",
                    session, length, SftpConstants.getCommandMessageName(type), id);
        }
        try {
            SftpEventListener listener = getSftpEventListenerProxy();
            listener.received(session, type, id);
        } catch (IOException | RuntimeException e) {
            if (type == SftpConstants.SSH_FXP_INIT) {
                throw e;
            }
            sendStatus(prepareReply(buffer), id, e, type);
            return;
        }
        doProcess(buffer, length, type, id);
    }

    @Override
    protected void doProcess(Buffer buffer, int length, int type, int id) throws IOException {
            switch (type) {
                case SftpConstants.SSH_FXP_INIT:
                    doInit(buffer, id);
                    break;
                case SftpConstants.SSH_FXP_OPEN:
                    doOpen(buffer, id);
                    break;
                case SftpConstants.SSH_FXP_CLOSE:
                    doClose(buffer, id);
                    break;
                case SftpConstants.SSH_FXP_READ:
                    doRead(buffer, id);
                    break;
                case SftpConstants.SSH_FXP_WRITE:
                    doWrite(buffer, id);
                    break;
                case SftpConstants.SSH_FXP_LSTAT:
                    doLStat(buffer, id);
                    break;
                case SftpConstants.SSH_FXP_FSTAT:
                    doFStat(buffer, id);
                    break;
                case SftpConstants.SSH_FXP_SETSTAT:
                    doSetStat(buffer, id, "", type, null);
                    break;
                case SftpConstants.SSH_FXP_FSETSTAT:
                    doFSetStat(buffer, id);
                    break;
                case SftpConstants.SSH_FXP_OPENDIR:
                    doOpenDir(buffer, id);
                    break;
                case SftpConstants.SSH_FXP_READDIR:
                    doReadDir(buffer, id);
                    break;
                case SftpConstants.SSH_FXP_REMOVE:
                    doRemove(buffer, id);
                    break;
                case SftpConstants.SSH_FXP_MKDIR:
                    doMakeDirectory(buffer, id);
                    break;
                case SftpConstants.SSH_FXP_RMDIR:
                    doRemoveDirectory(buffer, id);
                    break;
                case SftpConstants.SSH_FXP_REALPATH:
                    doRealPath(buffer, id);
                    break;
                case SftpConstants.SSH_FXP_STAT:
                    doStat(buffer, id);
                    break;
                case SftpConstants.SSH_FXP_RENAME:
                    doRename(buffer, id);
                    break;
                case SftpConstants.SSH_FXP_READLINK:
                    doReadLink(buffer, id);
                    break;
                case SftpConstants.SSH_FXP_SYMLINK:
                    doSymLink(buffer, id);
                    break;
                case SftpConstants.SSH_FXP_LINK:
                    doLink(buffer, id);
                    break;
                case SftpConstants.SSH_FXP_BLOCK:
                    doBlock(buffer, id);
                    break;
                case SftpConstants.SSH_FXP_UNBLOCK:
                    doUnblock(buffer, id);
                    break;
                case SftpConstants.SSH_FXP_EXTENDED:
                    doExtended(buffer, id);
                    break;
                default:
                    doUnsupported(buffer, length, type, id);
                    break;
            }
//        }
    }

    // 호스트로 보내기
    @Override
    protected void send(Buffer buffer) throws IOException {
        System.out.println("■■■■■■■■■■■Server■■ send ■■■■■■■■■■■■■■■");
        System.out.println(new String(buffer.array()));
        super.send(buffer);
    }

    public void publicSend(Buffer buffer) throws IOException {
        send(buffer);
    }

    // 처음 데이터 오는 지점. (클라이언트로 보내고)
    @Override
    public int data(ChannelSession channel, byte[] buf, int start, int len) throws IOException {
        System.out.println("■■■■■■■■■■■Server■■ data ■■■■■■■■■■■■■■■");
        String str = new String(buf, start, len);
        System.out.println(str);

        Buffer buff = new ByteArrayBuffer(buf, start, len);
        singleSftpClient.getProxyDefaultSftpClientExtend()
                .publicSend(SshConstants.SSH_MSG_CHANNEL_DATA, buff);

//        super.data(channel, buf, start, len);
        return 0;
    }
}
