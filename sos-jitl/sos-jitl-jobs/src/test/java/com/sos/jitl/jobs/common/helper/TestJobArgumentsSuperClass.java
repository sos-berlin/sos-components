package com.sos.jitl.jobs.common.helper;

import com.sos.jitl.jobs.common.JobArgument;

public class TestJobArgumentsSuperClass {

    JobArgument<String> testSuperClass = new JobArgument<String>("test_super_class");

    public JobArgument<String> getTestSuperClass() {
        return testSuperClass;
    }
}