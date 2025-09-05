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

    /** Creates a new worker thread in this factory.
     * <ul>
     * <li>Ensures the thread is non-daemon by default, so it prevents the JVM from exiting<br />
     * before the thread completes. This matches the behavior of Executors.defaultThreadFactory().<br />
     * (Daemon mode can still be set manually by the caller if desired.)</li>
     * <li>Ensures the thread has normal priority.<br />
     * </li>
     * <li>Uses stackSize = 0, meaning the JVM's default thread stack size is applied.</li>
     * </ul>
     */
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
