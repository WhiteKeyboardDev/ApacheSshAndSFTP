package my.test.proxy.aa.importProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.sshd.common.util.GenericUtils;
import org.apache.sshd.common.util.ValidateUtils;
import org.apache.sshd.common.util.logging.AbstractLoggingBean;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.shell.InvertedShell;
import org.apache.sshd.server.shell.InvertedShellWrapper;
import org.apache.sshd.server.shell.ShellFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class CustomShellFactory extends AbstractLoggingBean implements ShellFactory {
    private static final Logger logger = LogManager.getLogger(CustomShellFactory.class);

    private String command;
    private List<String> elements;
    public InvertedShell _shell;
    public ClientDaemon _client;

    public CustomShellFactory(ClientDaemon client) {
        this._client = client;
    }


    public String getCommand() {
        return command;
    }

    public List<String> getElements() {
        return elements;
    }

    public void setCommand(String command, String... elements) {
        setCommand(command, GenericUtils.isEmpty(elements) ? Collections.emptyList() : Arrays.asList(elements));
    }

    public void setCommand(String command, List<String> elements) {
        this.command = ValidateUtils.checkNotNullAndNotEmpty(command, "No command");
        this.elements = ValidateUtils.checkNotNullAndNotEmpty(elements, "No parsed elements");
    }

    @Override
    public Command createShell(ChannelSession channel) {
        InvertedShell shell = createInvertedShell(channel);
        this._shell = shell;
        return new InvertedShellWrapper(this._shell);
    }

    protected InvertedShell createInvertedShell(ChannelSession channel) {
        return new CustomShell(this._client);
    }
}
