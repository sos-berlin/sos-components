package com.sos.commons.util.concurrency;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicInteger;

public class SOSNamedForkJoinPoolThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {

    private final String prefix;
    private final AtomicInteger counter = new AtomicInteger(1);

    public SOSNamedForkJoinPoolThreadFactory(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
        return new SOSNamedForkJoinWorkerThread(pool, prefix + "-worker-" + counter.getAndIncrement());
    }

    static class SOSNamedForkJoinWorkerThread extends ForkJoinWorkerThread {

        private SOSNamedForkJoinWorkerThread(ForkJoinPool pool, String name) {
            super(pool);
            setName(name);
        }
    }
}
