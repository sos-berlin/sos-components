package com.sos.js7.job.helper;

import com.sos.js7.job.JobArgument;

public class TestJobArguments extends TestJobArgumentsSuperClass {

    JobArgument<String> test = new JobArgument<String>("test", false);

    public JobArgument<String> getTest() {
        return test;
    }
}