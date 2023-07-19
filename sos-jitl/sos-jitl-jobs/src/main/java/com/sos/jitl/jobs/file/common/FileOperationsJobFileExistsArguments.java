package com.sos.jitl.jobs.file.common;

import com.sos.js7.job.JobArgument;

public class FileOperationsJobFileExistsArguments extends FileOperationsJobArguments {

    // steady state
    private JobArgument<Integer> steadyStateCount = new JobArgument<Integer>("steady_state_count", false, 0);
    // seconds
    private JobArgument<Integer> steadyStateInterval = new JobArgument<Integer>("steady_state_interval", false, 1);

    public JobArgument<Integer> getSteadyStateCount() {
        return steadyStateCount;
    }

    //
    public JobArgument<Integer> getSteadyStateInterval() {
        return steadyStateInterval;
    }

}
