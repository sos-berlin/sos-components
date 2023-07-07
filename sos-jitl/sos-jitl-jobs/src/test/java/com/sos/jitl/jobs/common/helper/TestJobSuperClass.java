package com.sos.jitl.jobs.common.helper;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.OrderProcessStep;

public class TestJobSuperClass extends ABlockingInternalJob<TestJobArguments> {

    @Override
    public void onOrderProcess(OrderProcessStep<TestJobArguments> step) throws Exception {
    }
}
