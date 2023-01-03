package my.test.proxy.server;

import my.test.proxy.client.SingleSftpClient;
import org.apache.sshd.common.util.GenericUtils;
import org.apache.sshd.common.util.ObjectBuilder;
import org.apache.sshd.common.util.threads.CloseableExecutorService;
import org.apache.sshd.server.channel.ChannelDataReceiver;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.sftp.server.*;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

public class ProxySftpSubsystemFactoryExtend extends SftpSubsystemFactory {

    @Override
    public Command createSubsystem(ChannelSession channel) throws IOException {

        // Client daemon start
        SingleSftpClient singleSftpClient = new SingleSftpClient();

        SftpSubsystemExtend sftpSubsystemExtend = new SftpSubsystemExtend(channel, this);
        GenericUtils.forEach(getRegisteredListeners(), sftpSubsystemExtend::addSftpEventListener);
        sftpSubsystemExtend.setSingleSftpClient(singleSftpClient);

        // Client daemon add
        singleSftpClient.setServerAuthenticated(true);
        singleSftpClient.setSftpSubsystemExtend(sftpSubsystemExtend);
        singleSftpClient.createSftpClient();
        
        System.out.println("쓰레드 잠자기 시작");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("쓰레드 잠자기 끝");

        return sftpSubsystemExtend;
    }

    public static class Builder extends AbstractSftpEventListenerManager implements ObjectBuilder<ProxySftpSubsystemFactoryExtend> {
        private Supplier<? extends CloseableExecutorService> executorsProvider;
        private UnsupportedAttributePolicy policy = DEFAULT_POLICY;
        private SftpFileSystemAccessor fileSystemAccessor = SftpFileSystemAccessor.DEFAULT;
        private SftpErrorStatusDataHandler errorStatusDataHandler = SftpErrorStatusDataHandler.DEFAULT;
        private ChannelDataReceiver errorChannelDataReceiver;

        public Builder() {
            super();
        }

        public Builder withExecutorServiceProvider(Supplier<? extends CloseableExecutorService> provider) {
            executorsProvider = provider;
            return this;
        }

        public Builder withUnsupportedAttributePolicy(UnsupportedAttributePolicy p) {
            policy = Objects.requireNonNull(p, "No policy");
            return this;
        }

        public Builder withFileSystemAccessor(SftpFileSystemAccessor accessor) {
            fileSystemAccessor = Objects.requireNonNull(accessor, "No accessor");
            return this;
        }

        public Builder withSftpErrorStatusDataHandler(SftpErrorStatusDataHandler handler) {
            errorStatusDataHandler = Objects.requireNonNull(handler, "No error status handler");
            return this;
        }

        public Builder withErrorChannelDataReceiver(ChannelDataReceiver receiver) {
            errorChannelDataReceiver = receiver;
            return this;
        }

        @Override
        public ProxySftpSubsystemFactoryExtend build() {
            ProxySftpSubsystemFactoryExtend factory = new ProxySftpSubsystemFactoryExtend();
            factory.setExecutorServiceProvider(executorsProvider);
            factory.setUnsupportedAttributePolicy(policy);
//            factory.setFileSystemAccessor(fileSystemAccessor);
            factory.setErrorStatusDataHandler(errorStatusDataHandler);
            factory.setErrorChannelDataReceiver(errorChannelDataReceiver);
            GenericUtils.forEach(getRegisteredListeners(), factory::addSftpEventListener);
            return factory;
        }
    }
}
