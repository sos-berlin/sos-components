package com.sos.commons.job.helper;

import java.util.LinkedList;
import java.util.List;

import com.sos.commons.job.JobArgument;
import com.sos.commons.job.JobArguments;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments.AuthMethod;

public class TestJobArgumentsSuperClass extends JobArguments {

    private JobArgument<String> testSuperClass = new JobArgument<String>("test_super_class", false);

    private JobArgument<List<String>> list = new JobArgument<>("list", false);
    private JobArgument<LinkedList<String>> linkedList = new JobArgument<>("linked_list", false);
    private JobArgument<List<AuthMethod>> authMethods = new JobArgument<>("auth_methods", false);
    private JobArgument<String> test = new JobArgument<>("test", false);

    public JobArgument<String> getTestSuperClass() {
        return testSuperClass;
    }

    public JobArgument<List<String>> getList() {
        return list;
    }

    public JobArgument<LinkedList<String>> getLinkedList() {
        return linkedList;
    }

    public JobArgument<List<AuthMethod>> getAuthMethods() {
        return authMethods;
    }

    public JobArgument<String> getTest() {
        return test;
    }
}