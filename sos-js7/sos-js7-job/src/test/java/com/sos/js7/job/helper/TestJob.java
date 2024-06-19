package com.sos.js7.job.helper;

import java.util.List;
import java.util.Map;

import com.sos.commons.vfs.ssh.common.SSHProviderArguments.AuthMethod;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.OrderProcessStep;

public class TestJob extends TestJobSuperClass {

    public static final String ARG_NAME_LIST_SINGLETONMAP_DECLARED = "list_singleton_map";
    public static final String ARG_NAME_LIST_SINGLETONMAP_UNDECLARED = "list_singleton_map_undeclared";

    @Override
    public void processOrder(OrderProcessStep<TestJobArguments> step) throws Exception {
        step.getLogger().info("Info from job onOrderProcess");

        if (step.getDeclaredArguments().getLogAllArguments().getValue()) {

            if (step.getDeclaredArguments().getAuthMethods().getValue() != null) {
                step.getLogger().info("getAuthMethods:");
                for (AuthMethod m : step.getDeclaredArguments().getAuthMethods().getValue()) {
                    step.getLogger().info("    " + m + " (" + m.getClass() + ")");
                }
            }

            if (step.getDeclaredArguments().getListSingletonMap().getValue() != null) {
                step.getLogger().info("getListSingletonMap:");
                for (Map<String, Object> m : step.getDeclaredArguments().getListSingletonMap().getValue()) {
                    step.getLogger().info("    " + m + " (" + m.getClass() + ")");
                }
            }

            step.getLogger().info("step.getAllArgumentsAsNameValueMap():");
            step.getAllArgumentsAsNameValueMap().entrySet().forEach(e -> {
                step.getLogger().info("    " + e.getKey() + "=" + e.getValue());
                if (e.getKey().equals(ARG_NAME_LIST_SINGLETONMAP_UNDECLARED)) {
                    JobArgument<?> arg = step.getAllArguments().get(ARG_NAME_LIST_SINGLETONMAP_UNDECLARED);
                    step.getLogger().info("        " + arg);
                    for (Object o : ((List<?>) arg.getValue())) {
                        step.getLogger().info("        " + o.getClass());
                    }
                }
            });

        }

    }
}
