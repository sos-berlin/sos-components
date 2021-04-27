package com.sos.jitl.jobs.common.helper;

import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;

public class TestJobArgumentsSuperClass extends JobArguments {

    JobArgument<String> testSuperClass = new JobArgument<String>("test_super_class");

    public JobArgument<String> getTestSuperClass() {
        return testSuperClass;
    }
}