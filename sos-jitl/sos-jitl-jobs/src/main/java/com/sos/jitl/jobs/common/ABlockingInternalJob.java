package com.sos.jitl.jobs.common;

import js7.executor.forjava.internal.BlockingInternalJob;
import js7.executor.forjava.internal.JOrderResult;

public abstract class ABlockingInternalJob implements BlockingInternalJob {

    public ABlockingInternalJob() {

    }

    public ABlockingInternalJob(JJobContext jobContext) {

    }

    public JOrderResult processOrder(JOrderContext context) throws Exception {

        return null;
    }
}
