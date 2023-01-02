package my.test.proxy.aa.channel;

import org.apache.sshd.common.channel.PtyChannelConfigurationHolder;
import org.apache.sshd.common.channel.PtyMode;

import java.util.Map;

/**
 * It is best to import the client's channel settings from the beginning,
 * but It doesn't matter much because there is a channel synchronization daemon.
 */
public class PtyChannelConfigurationHolderImpl implements PtyChannelConfigurationHolder {
    @Override
    public String getPtyType() {
        return "xterm";
    }

    @Override
    public int getPtyColumns() {
        return DEFAULT_COLUMNS_COUNT;
    }

    @Override
    public int getPtyLines() {
        return DEFAULT_ROWS_COUNT;
    }

    @Override
    public int getPtyWidth() {
        return DEFAULT_WIDTH;
    }

    @Override
    public int getPtyHeight() {
        return DEFAULT_HEIGHT;
    }

    @Override
    public Map<PtyMode, Integer> getPtyModes() {
        return DEFAULT_PTY_MODES;
    }
}
