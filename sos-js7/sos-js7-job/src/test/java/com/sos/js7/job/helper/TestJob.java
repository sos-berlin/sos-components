package com.sos.js7.job.helper;

import com.sos.js7.job.OrderProcessStep;

public class TestJob extends TestJobSuperClass {

    @Override
    public void processOrder(OrderProcessStep<TestJobArguments> step) throws Exception {
        step.getLogger().info("Info from job onOrderProcess");

        if (step.getDeclaredArguments().getLogAllArguments().getValue()) {
            step.getLogger().info("step.getAllArgumentsAsNameValueMap():");
            step.getAllArgumentsAsNameValueMap().entrySet().forEach(e -> {
                step.getLogger().info("  " + e.getKey() + "=" + e.getValue());
            });
        }

    }
}
