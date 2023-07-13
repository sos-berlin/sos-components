package com.sos.commons.job.helper;

import com.sos.commons.job.OrderProcessStep;

public class TestJob extends TestJobSuperClass {

    @Override
    public void onOrderProcess(OrderProcessStep<TestJobArguments> step) throws Exception {
        step.getLogger().info("info from job onOrderProcess");
    }
}
