package my.test.proxy.server;

import my.test.proxy.client.SingleSftpClient;
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

    public void publicSend(byte[] buf, int start, int len) throws IOException {
        System.out.println("■■■■■■■■■■■Server■■ publicSend ■■■■■■■■■■■■■■■");
        Buffer tmpBuffer = new ByteArrayBuffer(buf, start, len);
        BufferUtils.updateLengthPlaceholder(buffer, 0);
        out.writeBuffer(tmpBuffer);
    }

    static int count = 0;

    // 처음 데이터 오는 지점. (클라이언트로 보내고)
    @Override
    public int data(ChannelSession channel, byte[] buf, int start, int len) throws IOException {
        System.out.println("■■■■■■■■■■■Server■■ data ■■■■■■■■■■■■■■■");
        System.out.println(new String(buf, start, len));
        System.out.println();

        if(count < 1){
            count++;
            super.data(channel, buf, start, len);
        }else{
//            Buffer tmpBuffer = new ByteArrayBuffer(buf, start, len);
            Buffer sendBuffer = new ByteArrayBuffer(buf, start, len);

//            int length = tmpBuffer.getInt();
//            int type = tmpBuffer.getUByte();
//            int id = tmpBuffer.getInt();
//            String str = tmpBuffer.getString();
//            System.out.println("■■■■■■■■■■■Server■■ data type : "+ type +" ■■■■■■■■■■■■■■■");
//            System.out.println("■■■■■■■■■■■Server■■ data length : "+ length +" ■■■■■■■■■■■■■■■");
//            System.out.println("■■■■■■■■■■■Server■■ data id : "+ id +" ■■■■■■■■■■■■■■■");
//            System.out.println("■■■■■■■■■■■Server■■ data str : "+ str +" ■■■■■■■■■■■■■■■");
            singleSftpClient.defaultSftpClientExtend.publicSend( sendBuffer);
        }
        return 0;
    }
}
