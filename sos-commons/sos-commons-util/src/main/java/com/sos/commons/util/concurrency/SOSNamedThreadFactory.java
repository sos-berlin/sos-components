package com.sos.commons.util.concurrency;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/** A {@link ThreadFactory} implementation that creates threads with a custom name prefix and optional daemon flag.<br/>
 * Each thread receives a unique sequential number appended to the prefix.
 * <p>
 * Useful for debugging, logging, and monitoring worker threads in an executor. */
public final class SOSNamedThreadFactory implements ThreadFactory {

    private final String namePrefix;
    private final boolean daemon;

    private final AtomicInteger counter = new AtomicInteger(1);

    /** Creates a new {@code SOSNamedThreadFactory} with the given name prefix.<br/>
     * Threads will be non-daemon by default.
     *
     * @param namePrefix the prefix to use for thread names */
    public SOSNamedThreadFactory(String namePrefix) {
        this(namePrefix, false);
    }

    /** Creates a new {@code SOSNamedThreadFactory} with the given name prefix and daemon setting.
     *
     * @param namePrefix the prefix to use for thread names
     * @param daemon {@code true} if threads should be daemon threads, {@code false} otherwise */
    public SOSNamedThreadFactory(String namePrefix, boolean daemon) {
        this.namePrefix = namePrefix;
        this.daemon = daemon;
    }

    /** Constructs a new {@link Thread} to run the given {@link Runnable} task.<br/>
     * The thread name will be {@code namePrefix}-{sequenceNumber}.
     *
     * @param r the {@link Runnable} to execute
     * @return a newly created {@link Thread} with the configured name and daemon flag */
    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName(namePrefix + "-" + counter.getAndIncrement());
        t.setDaemon(daemon);
        return t;
    }
}
