package com.sos.yade.engine.common.arguments;

import com.sos.commons.util.common.SOSArgument;

public class YADETargetArguments extends YADESourceTargetArguments {

    private SOSArgument<String> directory = new SOSArgument<>("target_dir", false);
    // Create missing Directory on Target
    private SOSArgument<Boolean> makeDirs = new SOSArgument<>("make_dirs", false, Boolean.valueOf(true));

    private SOSArgument<Boolean> keepModificationDate = new SOSArgument<>("keep_modification_date", false, Boolean.valueOf(false));

    public SOSArgument<String> getDirectory() {
        return directory;
    }

    public SOSArgument<Boolean> getMakeDirs() {
        return makeDirs;
    }

    public SOSArgument<Boolean> getKeepModificationDate() {
        return keepModificationDate;
    }

}
