package com.sos.yade.engine.commons.arguments;

import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.vfs.commons.AProviderArguments;

public class YADESourceTargetArguments extends ASOSArguments {

    private AProviderArguments provider;
    private YADEProviderCommandArguments commands;

    // TODO source_dir/target_dir
    private SOSArgument<String> directory = new SOSArgument<>("dir", false);

    private SOSArgument<Integer> connectionErrorRetryCountMax = new SOSArgument<>("connection_error_retry_count_max", false);
    private SOSArgument<String> connectionErrorRetryInterval = new SOSArgument<>("connection_error_retry_interval", false, "0s");

    /** - Replacing ------- */
    private SOSArgument<String> replacement = new SOSArgument<>("replacement", false);
    private SOSArgument<String> replacing = new SOSArgument<>("replacing", false);

    /** - Integrity Hash ------- */
    /** COPY/MOVE operations<br/>
     * Same algorithm for Source and Target - currently only md5 is supported<br/>
     * Source -> CheckIntegrityHash, Target -> CreateIntegrityHashFile<br/>
     * Argument name is based on XML schema definition */
    private SOSArgument<String> integrityHashAlgorithm = new SOSArgument<>("security_hash_type", false, "md5");

    /** Internal Argument - Source/Target/Jump */
    private SOSArgument<String> label = new SOSArgument<>(null, false);

    public boolean isRetryOnConnectionErrorEnabled() {
        return connectionErrorRetryCountMax.getValue() != null && connectionErrorRetryCountMax.getValue().intValue() > 0;
    }

    public boolean isReplacementEnabled() {
        return !replacing.isEmpty() && !replacement.isEmpty();
    }

    public AProviderArguments getProvider() {
        return provider;
    }

    public void setProvider(AProviderArguments val) {
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

    public SOSArgument<Integer> getConnectionErrorRetryCountMax() {
        return connectionErrorRetryCountMax;
    }

    public SOSArgument<String> getConnectionErrorRetryInterval() {
        return connectionErrorRetryInterval;
    }

    public SOSArgument<String> getReplacement() {
        return replacement;
    }

    public SOSArgument<String> getReplacing() {
        return replacing;
    }

    public SOSArgument<String> getIntegrityHashAlgorithm() {
        return integrityHashAlgorithm;
    }

    public SOSArgument<String> getLabel() {
        return label;
    }

}
