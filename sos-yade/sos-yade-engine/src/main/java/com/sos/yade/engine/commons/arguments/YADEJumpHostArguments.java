package com.sos.yade.engine.commons.arguments;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;
import com.sos.commons.vfs.ssh.commons.SSHProviderArguments;

public class YADEJumpHostArguments extends ASOSArguments {

    public static final String LABEL = "Jump";

    public enum JumpPlatform {
        UNIX, WINDOWS
    }

    private SSHProviderArguments provider;
    private YADEProviderCommandArguments commands;

    private SOSArgument<String> tempDirectoryParent = new SOSArgument<>("TempDirectoryParent", false, "/tmp");
    private SOSArgument<JumpPlatform> platform = new SOSArgument<>("Platform", false);

    private SOSArgument<String> yadeClientCommand = new SOSArgument<>("YADEClientCommand", false);

    /** internal usage */
    private SOSArgument<Boolean> configuredOnSource = new SOSArgument<>(null, false);

    private Map<String, Node> configuredProtocolFragments;
    private Map<String, Node> configuredCsFragments;
    private Map<String, Node> configuredDecryptionFragments;

    public SSHProviderArguments getProvider() {
        if (provider == null) {
            provider = new SSHProviderArguments();
            provider.applyDefaultIfNullQuietly();
        }
        return provider;
    }

    public YADEProviderCommandArguments getCommands() {
        if (commands == null) {
            commands = new YADEProviderCommandArguments();
            commands.applyDefaultIfNullQuietly();
        }
        return commands;
    }

    public SOSArgument<String> getTempDirectoryParent() {
        return tempDirectoryParent;
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

    public String getAdvancedAccessInfo() {
        return provider == null ? null : provider.getAdvancedAccessInfo();
    }

    public void addConfiguredProtocolFragment(Node fragment, AProviderArguments args) {
        if (args.getKey().isEmpty()) {
            return;
        }
        if (configuredProtocolFragments == null) {
            configuredProtocolFragments = new HashMap<>();
        }
        configuredProtocolFragments.put(args.getKey().getValue(), fragment);
    }

    public Node getConfiguredProtocolFragment(String key) {
        if (configuredProtocolFragments == null) {
            return null;
        }
        return configuredProtocolFragments.get(key);
    }

    public void addConfiguredCsFragment(Node fragment, String name) {
        if (fragment == null || name == null) {
            return;
        }
        if (configuredCsFragments == null) {
            configuredCsFragments = new HashMap<>();
        }
        configuredCsFragments.put(name, fragment);
    }

    public Map<String, Node> getConfiguredCsFragments() {
        return configuredCsFragments;
    }

    public void addConfiguredDecryptionFragment(Node fragment, String name) {
        if (fragment == null || name == null) {
            return;
        }
        if (configuredDecryptionFragments == null) {
            configuredDecryptionFragments = new HashMap<>();
        }
        configuredDecryptionFragments.put(name, fragment);
    }

    public Map<String, Node> getConfiguredDecryptionFragments() {
        return configuredDecryptionFragments;
    }

    public void clear() {
        configuredProtocolFragments = null;
        configuredCsFragments = null;
        configuredDecryptionFragments = null;
    }

}
