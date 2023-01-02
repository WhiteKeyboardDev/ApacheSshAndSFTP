package my.test.proxy.client;

import my.test.proxy.server.SftpSubsystemExtend;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.sftp.client.SftpErrorDataHandler;
import org.apache.sshd.sftp.client.SftpVersionSelector;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SingleSftpClient extends Thread {

    public static boolean isAuthenticationSuccessClientSession = false;
    public static boolean serverAuthenticated = false;

    public static boolean isAuthenticationSuccessClientSession() {
        return isAuthenticationSuccessClientSession;
    }

    public static void setIsAuthenticationSuccessClientSession(boolean isAuthenticationSuccessClientSession) {
        SingleSftpClient.isAuthenticationSuccessClientSession = isAuthenticationSuccessClientSession;
    }

    public static boolean isServerAuthenticated() {
        return serverAuthenticated;
    }

    public static void setServerAuthenticated(boolean serverAuthenticated) {
        SingleSftpClient.serverAuthenticated = serverAuthenticated;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new SingleSftpClient().createSftpClient();
    }

    public ProxyDefaultSftpClientExtend proxyDefaultSftpClientExtend;
    public SftpSubsystemExtend sftpSubsystemExtend;

    public ProxyDefaultSftpClientExtend getProxyDefaultSftpClientExtend() {
        return proxyDefaultSftpClientExtend;
    }

    public void setSftpSubsystemExtend(SftpSubsystemExtend sftpSubsystemExtend) {
        this.sftpSubsystemExtend = sftpSubsystemExtend;
    }

    public void createSftpClient() throws IOException {
        // session create
        SshClient client = SshClient.setUpDefaultClient();
        client.start();
        // win server
        ClientSession clientSession = null;
        try {
//            clientSession = client.connect("root", "192.168.5.171", 2024).verify().getSession();
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
        System.out.println("session : " + tt.isSuccess());


        // sftp client - default
        ProxyDefaultSftpClientFactoryExtend defaultSftpClientFactory = new ProxyDefaultSftpClientFactoryExtend();
        try {
            proxyDefaultSftpClientExtend =
                    defaultSftpClientFactory.createDefaultSftpClient(
                            clientSession,
                            SftpVersionSelector.CURRENT,
                            SftpErrorDataHandler.EMPTY
                    );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        proxyDefaultSftpClientExtend.setProxySingleSftpClient(this);
        proxyDefaultSftpClientExtend.setProxySftpSubsystemExtend(sftpSubsystemExtend);
        isAuthenticationSuccessClientSession = true;

        proxyDefaultSftpClientExtend.open("/opt");
        proxyDefaultSftpClientExtend.read("/opt/tet.txt");

        start();
    }

    @Override
    public void run() {
        for (; ; ) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
