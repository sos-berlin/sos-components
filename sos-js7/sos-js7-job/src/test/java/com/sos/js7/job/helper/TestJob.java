package com.sos.js7.job.helper;

import com.sos.js7.job.OrderProcessStep;

public class TestJob extends TestJobSuperClass {

    @Override
    public void processOrder(OrderProcessStep<TestJobArguments> step) throws Exception {
        step.getLogger().info("info from job onOrderProcess");
    }
}
