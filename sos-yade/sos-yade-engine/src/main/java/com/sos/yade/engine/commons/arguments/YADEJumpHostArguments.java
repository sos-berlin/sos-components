package com.sos.yade.engine.commons.arguments;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;
import com.sos.commons.vfs.ssh.commons.SSHProviderArguments;

public class YADEJumpHostArguments extends ASOSArguments {

    public static final String LABEL = "Jump";

    public enum JumpPlatform {
        UNIX, WINDOWS
    }

    private SSHProviderArguments provider;
    private YADEProviderCommandArguments commands;

    /** COPYFROMINTERNET/COPYTOINTERNET/... */
    private SOSArgument<String> directory = new SOSArgument<>("dir", false);
    private SOSArgument<JumpPlatform> platform = new SOSArgument<>("platform", false);

    private SOSArgument<String> yadeClientCommand = new SOSArgument<>("yade_client_command", false);

    /** internal usage */
    private SOSArgument<Boolean> configuredOnSource = new SOSArgument<>(null, false);

    public SSHProviderArguments getProvider() {
        if (provider == null) {
            provider = new SSHProviderArguments();
            provider.applyDefaultIfNullQuietly();
        }
        return provider;
    }

    public void setProvider(SSHProviderArguments val) {
        provider = val;
    }

    public YADEProviderCommandArguments getCommands() {
        if (commands == null) {
            commands = new YADEProviderCommandArguments();
            commands.applyDefaultIfNullQuietly();
        }
        return commands;
    }

    public void setCommands(YADEProviderCommandArguments val) {
        commands = val;
    }

    public SOSArgument<String> getDirectory() {
        return directory;
    }

    public SOSArgument<JumpPlatform> getPlatform() {
        return platform;
    }

    public boolean isPlatformEnabled() {
        return !platform.isEmpty();
    }

    public boolean isWindowsPlatform() {
        return isPlatformEnabled() && JumpPlatform.WINDOWS.equals(platform.getValue());
    }

    public void setPlatform(String val) {
        if (SOSString.isEmpty(val)) {
            return;
        }
        platform.setValue(JumpPlatform.valueOf(val.trim().toUpperCase()));
    }

    public SOSArgument<String> getYADEClientCommand() {
        return yadeClientCommand;
    }

    public SOSArgument<Boolean> getConfiguredOnSource() {
        return configuredOnSource;
    }

    public boolean isConfiguredOnSource() {
        return configuredOnSource.isTrue();
    }

    public String getAccessInfo() throws ProviderInitializationException {
        return provider == null ? null : provider.getAccessInfo();
    }
}
