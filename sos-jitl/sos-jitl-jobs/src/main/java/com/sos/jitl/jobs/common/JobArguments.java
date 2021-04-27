package com.sos.jitl.jobs.common;

import com.sos.jitl.jobs.common.JobLogger.LogLevel;

public class JobArguments {

    private JobArgument<LogLevel> logLevel = new JobArgument<LogLevel>("log_level", LogLevel.INFO);

    public JobArgument<LogLevel> getLogLevel() {
        return logLevel;
    }

}
