package com.sos.jitl.jobs.examples;

import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;

public class InfoJobArguments extends JobArguments {

    private JobArgument<Boolean> showEnv = new JobArgument<Boolean>("show_env", false);
    private JobArgument<Boolean> redefineShowEnv = new JobArgument<Boolean>("redefine_show_env", false);

    public JobArgument<Boolean> getShowEnv() {
        return showEnv;
    }

    public JobArgument<Boolean> getRedefineShowEnv() {
        return redefineShowEnv;
    }
}
