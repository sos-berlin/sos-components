package com.sos.commons.job.helper;

import com.sos.commons.job.ABlockingInternalJob;
import com.sos.commons.job.OrderProcessStep;

public class TestJobSuperClass extends ABlockingInternalJob<TestJobArguments> {

    @Override
    public void onOrderProcess(OrderProcessStep<TestJobArguments> step) throws Exception {
    }
}
