package com.sos.jitl.jobs.rest;

import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArguments;

public class JQJobArguments extends JobArguments  {

	private final JobArgument<String> input_variable = new JobArgument<>("in", false);
    private final JobArgument<String> output = new JobArgument<>("out", false);
    
    public JobArgument<String> getInputVariable() {
        return input_variable;
    }

    public JobArgument<String> getOutputVariable() {
        return output;
    }
}
