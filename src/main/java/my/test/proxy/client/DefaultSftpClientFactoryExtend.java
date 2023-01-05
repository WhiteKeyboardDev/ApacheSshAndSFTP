package my.test.proxy.client;

import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.sftp.client.SftpErrorDataHandler;
import org.apache.sshd.sftp.client.SftpVersionSelector;
import org.apache.sshd.sftp.client.impl.DefaultSftpClientFactory;

import java.io.IOException;

public class DefaultSftpClientFactoryExtend extends DefaultSftpClientFactory {
    @Override
    public DefaultSftpClientExtend createDefaultSftpClient(
            ClientSession session, SftpVersionSelector selector, SftpErrorDataHandler errorDataHandler)
            throws IOException {
        return new DefaultSftpClientExtend(session, selector, errorDataHandler);
    }
}
