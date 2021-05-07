package com.sos.jitl.jobs.examples;

import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArgument.DisplayMode;
import com.sos.jitl.jobs.common.JobArguments;

public class InfoJobArguments extends JobArguments {

    private JobArgument<Boolean> showEnv = new JobArgument<Boolean>("show_env", false);
    private JobArgument<Boolean> redefineShowEnv = new JobArgument<Boolean>("redefine_show_env", false);
    private JobArgument<String> stringArgument = new JobArgument<String>("string_argument", false);
    private JobArgument<String> returnVariables = new JobArgument<String>("return_variables", false);
    private JobArgument<String> password = new JobArgument<String>("password", false, DisplayMode.MASKED);

    public JobArgument<Boolean> getShowEnv() {
        return showEnv;
    }

    public JobArgument<Boolean> getRedefineShowEnv() {
        return redefineShowEnv;
    }

    public JobArgument<String> getStringArgument() {
        return stringArgument;
    }

    public JobArgument<String> getReturnVariables() {
        return returnVariables;
    }

    public JobArgument<String> getPassword() {
        return password;
    }
}
