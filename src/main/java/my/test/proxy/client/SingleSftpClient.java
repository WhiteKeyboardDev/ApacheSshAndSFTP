package my.test.proxy.client;

import my.test.proxy.server.SftpSubsystemExtend;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.channel.ChannelOutputStream;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;
import org.apache.sshd.sftp.client.SftpErrorDataHandler;
import org.apache.sshd.sftp.client.SftpVersionSelector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

public class SingleSftpClient extends Thread {

    static public SingleSftpClient singleSftpClient = new SingleSftpClient();

    public static boolean isAuthenticationSuccessClientSession = false;
    public static boolean serverAuthenticated = false;


    /*<server>*/

    public static void setServerAuthenticated(boolean serverAuthenticated) {
        SingleSftpClient.serverAuthenticated = serverAuthenticated;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new SingleSftpClient().createSftpClient();
    }

    public DefaultSftpClientExtend defaultSftpClientExtend;
    public SftpSubsystemExtend sftpSubsystemExtend;

    public ChannelShell shell;

    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
    ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

    public void setSftpSubsystemExtend(SftpSubsystemExtend sftpSubsystemExtend) {
        this.sftpSubsystemExtend = sftpSubsystemExtend;
    }

    public SftpSubsystemExtend getSftpSubsystemExtend() {
        return sftpSubsystemExtend;
    }

    public void createSftpClient() throws IOException {
        boolean proxy = true;
        if (!proxy) {

        } else {
            // session create
            SshClient client = SshClient.setUpDefaultClient();
            client.start();
            ClientSession clientSession = null;
            try {
                clientSession = client.connect("root", "192.168.5.102", 22).verify().getSession();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            clientSession.addPasswordIdentity("1234");
            AuthFuture tt = null;
            try {
                tt = clientSession.auth().verify(60, TimeUnit.SECONDS);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // sftp client - default
            DefaultSftpClientFactoryExtend defaultSftpClientFactory = new DefaultSftpClientFactoryExtend();
            defaultSftpClientExtend = defaultSftpClientFactory.createDefaultSftpClient(clientSession,
                    SftpVersionSelector.CURRENT,
                    SftpErrorDataHandler.EMPTY
            );

            isAuthenticationSuccessClientSession = true;

//            System.out.println("■■■■■■■■■■■Client■■ Command! ■■■■■■■■■■■■■■■");


            start();
        }
    }
        @Override
        public void run () {
            for (; ; ) {
                if (responseStream.size() > 0)

                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
            }
        }
    }
