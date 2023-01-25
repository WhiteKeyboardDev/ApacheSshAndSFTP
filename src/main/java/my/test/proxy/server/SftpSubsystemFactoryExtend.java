package my.test.proxy.server;

import org.apache.sshd.common.util.GenericUtils;
import org.apache.sshd.common.util.ObjectBuilder;
import org.apache.sshd.common.util.threads.CloseableExecutorService;
import org.apache.sshd.server.channel.ChannelDataReceiver;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.sftp.server.*;

import java.util.Objects;
import java.util.function.Supplier;

import static my.test.proxy.client.SingleSftpClient.singleSftpClient;

public class SftpSubsystemFactoryExtend extends SftpSubsystemFactory {



    @Override
    public Command createSubsystem(ChannelSession channel) {
        SftpSubsystemExtend sftpSubsystemExtend = new SftpSubsystemExtend(channel, this);

        GenericUtils.forEach(getRegisteredListeners(), sftpSubsystemExtend::addSftpEventListener);
        sftpSubsystemExtend.setSingleSftpClient(singleSftpClient);
        singleSftpClient.setSftpSubsystemExtend(sftpSubsystemExtend);
        singleSftpClient.setServerAuthenticated(true);
        return sftpSubsystemExtend;
    }

    public static class Builder extends AbstractSftpEventListenerManager implements ObjectBuilder<SftpSubsystemFactoryExtend> {
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
        public SftpSubsystemFactoryExtend build() {
            SftpSubsystemFactoryExtend factory = new SftpSubsystemFactoryExtend();
            factory.setExecutorServiceProvider(executorsProvider);
            factory.setUnsupportedAttributePolicy(policy);
            factory.setFileSystemAccessor(fileSystemAccessor);
            factory.setErrorStatusDataHandler(errorStatusDataHandler);
            factory.setErrorChannelDataReceiver(errorChannelDataReceiver);
            GenericUtils.forEach(getRegisteredListeners(), factory::addSftpEventListener);
            return factory;
        }
    }
}
