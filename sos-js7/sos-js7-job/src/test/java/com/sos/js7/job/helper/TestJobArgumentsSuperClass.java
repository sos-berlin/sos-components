package com.sos.js7.job.helper;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sos.commons.util.arguments.base.SOSArgument.DisplayMode;
import com.sos.commons.vfs.ssh.commons.SSHAuthMethod;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArguments;

public class TestJobArgumentsSuperClass extends JobArguments {

    private JobArgument<String> testSuperClass = new JobArgument<String>("test_super_class", false);

    private JobArgument<List<String>> list = new JobArgument<>("list", false);
    // corresponds to the JOC/AGENT List type with the Singleton Map as list value
    private JobArgument<List<Map<String, Object>>> listSingletonMap = new JobArgument<>("list_singleton_map", false);
    private JobArgument<LinkedList<String>> linkedList = new JobArgument<>("linked_list", false);
    private JobArgument<List<SSHAuthMethod>> authMethods = new JobArgument<>("auth_methods", false);
    private JobArgument<String> test = new JobArgument<>("test", false);
    private JobArgument<Path> path = new JobArgument<>("path", false);
    private JobArgument<String> password = new JobArgument<>("password", false, DisplayMode.MASKED);

    public JobArgument<String> getTestSuperClass() {
        return testSuperClass;
    }

    public JobArgument<List<String>> getList() {
        return list;
    }

    public JobArgument<List<Map<String, Object>>> getListSingletonMap() {
        return listSingletonMap;
    }

    public JobArgument<LinkedList<String>> getLinkedList() {
        return linkedList;
    }

    public JobArgument<List<SSHAuthMethod>> getAuthMethods() {
        return authMethods;
    }

    public JobArgument<String> getTest() {
        return test;
    }

    public JobArgument<Path> getPath() {
        return path;
    }

    public JobArgument<String> getPassword() {
        return password;
    }
}