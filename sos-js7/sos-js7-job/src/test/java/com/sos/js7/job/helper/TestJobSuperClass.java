package com.sos.js7.job.helper;

import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;

public class TestJobSuperClass extends Job<TestJobArguments> {

    @Override
    public void processOrder(OrderProcessStep<TestJobArguments> step) throws Exception {
    }
}
