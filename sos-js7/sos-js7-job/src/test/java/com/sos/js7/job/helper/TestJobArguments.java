package com.sos.js7.job.helper;

import com.sos.js7.job.JobArgument;

public class TestJobArguments extends TestJobArgumentsSuperClass {

    JobArgument<String> test = new JobArgument<>("test", false);

    JobArgument<Boolean> logAllArguments = new JobArgument<>("log_all_arguments", false, false);

    public JobArgument<String> getTest() {
        return test;
    }

    public JobArgument<Boolean> getLogAllArguments() {
        return logAllArguments;
    }
}