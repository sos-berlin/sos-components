package com.sos.yade.engine.commons.arguments;

import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.vfs.commons.IProvider;

public class YADESourceTargetArguments extends ASOSArguments {

    private AProviderArguments provider;
    private YADEProviderCommandArguments commands;

    // TODO source_dir/target_dir
    private SOSArgument<String> directory = new SOSArgument<>("Directory", false);

    /** - Replacing ------- */
    private SOSArgument<String> replacing = new SOSArgument<>("ReplaceWhat", false);
    private SOSArgument<String> replacement = new SOSArgument<>("ReplaceWith", false);

    /** - Integrity Hash ------- */
    /** COPY/MOVE operations<br/>
     * Same algorithm for Source and Target - currently only md5 is supported<br/>
     * Source -> CheckIntegrityHash, Target -> CreateIntegrityHashFile<br/>
     * Argument name is based on XML schema definition */
    private SOSArgument<String> integrityHashAlgorithm = new SOSArgument<>("HashAlgorithm", false, "md5");

    /** Simulation Argument
     * 
     * @see {@link IProvider#injectConnectivityFault()} */
    private SOSArgument<String> simConnFaults = new SOSArgument<>(YADEArguments.STARTUP_ARG_SIM_CONN_FAULTS, false);

    /** Internal Argument - Source/Target/Jump */
    private SOSArgument<String> label = new SOSArgument<>(null, false);

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

    public SOSArgument<String> getReplacement() {
        return replacement;
    }

    public SOSArgument<String> getReplacing() {
        return replacing;
    }

    public SOSArgument<String> getIntegrityHashAlgorithm() {
        return integrityHashAlgorithm;
    }

    public SOSArgument<String> getSimConnFaults() {
        return simConnFaults;
    }

    public SOSArgument<String> getLabel() {
        return label;
    }

}
