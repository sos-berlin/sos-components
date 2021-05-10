package com.sos.jitl.jobs.common.helper;

import java.util.LinkedList;
import java.util.List;

import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;

public class TestJobArgumentsSuperClass extends JobArguments {

    private JobArgument<String> testSuperClass = new JobArgument<String>("test_super_class", false);

    private JobArgument<List<String>> list = new JobArgument<>("list", false);
    private JobArgument<LinkedList<String>> linkedList = new JobArgument<>("linked_list", false);

    public JobArgument<String> getTestSuperClass() {
        return testSuperClass;
    }

    public JobArgument<List<String>> getList() {
        return list;
    }

    public JobArgument<LinkedList<String>> getLinkedList() {
        return linkedList;
    }
}