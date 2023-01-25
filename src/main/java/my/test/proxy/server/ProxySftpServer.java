package my.test.proxy.server;

import my.test.proxy.aa.key.GenerateServerKeyPair;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;

import java.io.IOException;
import java.util.Collections;

import static my.test.proxy.client.SingleSftpClient.singleSftpClient;

public class ProxySftpServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        SshServer sshd = SshServer.setUpDefaultServer();

        // Server Key
        sshd.setKeyPairProvider(new GenerateServerKeyPair());

        // Server Host
        sshd.setHost("192.168.5.171");

        // SshServer Port
        sshd.setPort(2022);

        // SFTP server setting
        SftpSubsystemFactoryExtend sftpSubsystemFactory = new SftpSubsystemFactoryExtend.Builder()
//                .withFileSystemAccessor(new NativeFileSystemFactory())
                .withFileSystemAccessor(new SftpFileSystemAccessorExtend())
                .build();
        sshd.setSubsystemFactories(Collections.singletonList(sftpSubsystemFactory));

        // Add authentication implementation.
        sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
          @Override
          public boolean authenticate(String s, String s1, ServerSession serverSession) throws PasswordChangeRequiredException, AsyncAuthException {
              try {
                  singleSftpClient.createSftpClient();
                  System.out.println();
                  System.out.println("Thread Sleep 1 seconds start");
                  try {
                      Thread.sleep(1000);
                  } catch (InterruptedException e) {
                      throw new RuntimeException(e);
                  }
              } catch (IOException e) {
                  throw new RuntimeException(e);
              }
              System.out.println("Thread Sleep 1 seconds end");
              System.out.println();

              singleSftpClient.setServerAuthenticated(true);

              return true;
          }
        }
        );

        // Server Start
        sshd.start();

        while (sshd.isStarted())
            Thread.sleep(1000*60*60);
    }
}
