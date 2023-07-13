package com.sos.commons.job.helper;

import com.sos.commons.job.JobArgument;

public class TestJobArguments extends TestJobArgumentsSuperClass {

    JobArgument<String> test = new JobArgument<String>("test", false);

    public JobArgument<String> getTest() {
        return test;
    }
}