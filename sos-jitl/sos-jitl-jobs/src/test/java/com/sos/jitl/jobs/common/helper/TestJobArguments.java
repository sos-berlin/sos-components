package com.sos.jitl.jobs.common.helper;

import com.sos.jitl.jobs.common.JobArgument;

public class TestJobArguments extends TestJobArgumentsSuperClass {

    JobArgument<String> test = new JobArgument<String>("test", false);

    public JobArgument<String> getTest() {
        return test;
    }
}