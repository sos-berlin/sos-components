package com.sos.joc.cluster;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class JocClusterThreadFactory implements ThreadFactory {

    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public JocClusterThreadFactory(ThreadGroup tg, String prefix) {
        this.group = (tg != null) ? tg : Thread.currentThread().getThreadGroup();
        this.namePrefix = prefix + "-" + poolNumber.getAndIncrement() + "-";
    }

    /** Ensures the created thread is non-daemon, so it prevents the JVM from exiting<br/>
     * before the thread completes, matching Executors.defaultThreadFactory() behavior. */
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}
