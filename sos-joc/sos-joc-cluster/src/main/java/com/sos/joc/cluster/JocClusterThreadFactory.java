package com.sos.joc.cluster;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class JocClusterThreadFactory implements ThreadFactory {

    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public JocClusterThreadFactory(ThreadGroup tg, String prefix) {
        SecurityManager s = System.getSecurityManager();
        if (tg == null) {
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        } else {
            group = tg;
        }
        namePrefix = prefix + "-" + poolNumber.getAndIncrement() + "-";
    }

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
