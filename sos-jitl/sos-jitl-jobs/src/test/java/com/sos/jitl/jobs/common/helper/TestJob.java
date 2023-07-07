package com.sos.jitl.jobs.common.helper;

import com.sos.jitl.jobs.common.OrderProcessStep;

public class TestJob extends TestJobSuperClass {

    @Override
    public void onOrderProcess(OrderProcessStep<TestJobArguments> step) throws Exception {
        step.getLogger().info("info from job onOrderProcess");
    }
}
