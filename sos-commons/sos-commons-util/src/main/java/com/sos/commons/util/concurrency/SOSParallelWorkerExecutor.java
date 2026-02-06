package com.sos.commons.util.concurrency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

/** A simple thread pool executor for processing tasks in parallel with a fixed number of worker threads.
 * <p>
 * Tasks are queued internally and processed by a configurable number of worker threads.<br/>
 * Supports optional early termination on exception and an external cancel flag.<br/>
 * Optionally, a custom {@link ThreadFactory} can be provided to control thread names, daemon status, etc.
 *
 * @param <T> the type of task item to process */
public final class SOSParallelWorkerExecutor<T> implements AutoCloseable {

    /** Number of worker threads */
    private final int workers;
    /** Whether to stop processing immediately when a task throws an exception */
    private final boolean stopOnException;
    /** Optional external cancel flag */
    private final AtomicBoolean cancel;

    /** Executor service for worker threads */
    private final ExecutorService executor;
    /** Futures of submitted worker threads */
    private final List<Future<?>> workerFutures = new ArrayList<>();
    /** Exceptions thrown by tasks, collected in a thread-safe list */
    private final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
    /** Flag to ensure execute() is only called once */
    private final AtomicBoolean started = new AtomicBoolean(false);

    /** Internal queue of tasks to process */
    private BlockingQueue<T> queue;

    /** Creates an executor with the given number of worker threads.
     * <p>
     * By default, {@code stopOnException} is {@code true} and a new internal cancel flag is used.
     *
     * @param workers number of parallel worker threads (>0) */
    public SOSParallelWorkerExecutor(int workers) {
        this(workers, null);
    }

    /** Creates an executor with the given number of worker threads and optional custom thread factory.
     * <p>
     * By default, {@code stopOnException} is {@code true}.
     *
     * @param workers number of parallel worker threads (>0)
     * @param threadFactory optional {@link ThreadFactory} for customizing thread names, daemon status, etc. */
    public SOSParallelWorkerExecutor(int workers, ThreadFactory threadFactory) {
        this(workers, threadFactory, true);
    }

    /** Creates an executor with the given number of worker threads and stop-on-exception behavior.
     *
     * @param workers number of parallel worker threads (>0)
     * @param stopOnException whether to stop all workers immediately if a task throws an exception */
    public SOSParallelWorkerExecutor(int workers, boolean stopOnException) {
        this(workers, null, stopOnException);
    }

    /** Creates an executor with the given number of worker threads, optional custom thread factory, and stop-on-exception behavior.
     *
     * @param workers number of parallel worker threads (>0)
     * @param threadFactory optional {@link ThreadFactory} for customizing thread names, daemon status, etc.
     * @param stopOnException whether to stop all workers immediately if a task throws an exception */
    public SOSParallelWorkerExecutor(int workers, ThreadFactory threadFactory, boolean stopOnException) {
        this(workers, threadFactory, stopOnException, new AtomicBoolean(false));
    }

    /** Creates an executor with the given number of worker threads, optional custom thread factory, stop-on-exception behavior, and an external cancel flag.
     *
     * @param workers number of parallel worker threads (>0)
     * @param threadFactory optional {@link ThreadFactory} for customizing thread names, daemon status, etc.
     * @param stopOnException whether to stop all workers immediately if a task throws an exception
     * @param cancel external {@link AtomicBoolean} flag to signal cancellation from another thread */
    public SOSParallelWorkerExecutor(int workers, ThreadFactory threadFactory, boolean stopOnException, AtomicBoolean cancel) {
        if (workers <= 0) {
            throw new IllegalArgumentException("workers must be > 0");
        }
        this.workers = workers;
        this.stopOnException = stopOnException;
        this.cancel = cancel;
        this.executor = threadFactory == null ? Executors.newFixedThreadPool(workers) : Executors.newFixedThreadPool(workers, threadFactory);
    }

    /** Adds all tasks to the internal work queue.
     *
     * @param items collection of items to process */
    public void submitAll(Collection<? extends T> items) {
        this.queue = new ArrayBlockingQueue<>(items.size());// ArrayBlockingQueue is faster as LinkedBlockedQueue etc
        queue.addAll(items);
    }

    /** Starts the worker threads which process queued items using the given task handler.
     * <p>
     * Must be called exactly once per executor instance.
     *
     * @param task the handler to process each task item
     * @throws IllegalStateException if called more than once */
    public void execute(WorkerTask<T> task) {
        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException("Executor already started");
        }

        for (int i = 0; i < workers; i++) {
            workerFutures.add(executor.submit(() -> {
                while (true) {
                    T item = queue.poll();
                    if (item == null || cancel.get()) {
                        return;
                    }
                    try {
                        task.process(item);
                    } catch (Exception e) {
                        exceptions.add(e);
                        if (stopOnException) {
                            throw new RuntimeException(e);
                        }
                        if (cancel.get()) {
                            return;
                        }
                    }
                }
            }));
        }
    }

    /** Waits for all worker threads to finish processing and shuts down the executor.
     *
     * @throws Exception if any worker threw an exception */
    public void awaitAndShutdown() throws Exception {
        try {
            for (Future<?> f : workerFutures) {
                f.get();
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Override
    public void close() throws Exception {
        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    /** Returns a thread-safe list of exceptions thrown by tasks.
     *
     * @return list of exceptions collected during execution */
    public List<Throwable> getExceptions() {
        return exceptions;
    }

    /** Functional interface for processing a task item.
     *
     * @param <T> the type of task item */
    @FunctionalInterface
    public interface WorkerTask<T> {

        /** Processes a single task item.
         *
         * @param item the task item to process
         * @throws Exception any exception that occurs during processing */
        void process(T item) throws Exception;
    }
}
