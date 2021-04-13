package com.sos.jitl.jobs.common;

import js7.executor.forjava.internal.BlockingInternalJob;
import js7.executor.forjava.internal.JOrderProcess;
import js7.executor.forjava.internal.JavaJobContext;

public abstract class ABlockingInternalJob implements BlockingInternalJob {

    public ABlockingInternalJob() {

    }

    public ABlockingInternalJob(JavaJobContext jobContext) {

    }

    public JOrderProcess processOrder(JOrderProcess context) throws Exception {

        return null;
    }
}
