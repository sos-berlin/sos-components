package com.sos.yade.engine.commons.helpers;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;

import com.sos.yade.engine.commons.arguments.YADEArguments;

public class YADEParallelExecutorFactory {

    /** Custom ForkJoinPool & parallelStream because this combination:<br/>
     * - allows control over the number of threads created<br/>
     * - blocks the main thread until all tasks are completed<br/>
     * - does not increase memory usage (compared to using a Future list callback in case of a large number of files)<br/>
     */
    public static ForkJoinPool create(int configuredMaxThreads, int numberOfTasks) {
        int maxThreads = numberOfTasks < configuredMaxThreads ? numberOfTasks : configuredMaxThreads;

        return new ForkJoinPool(maxThreads, new ForkJoinPool.ForkJoinWorkerThreadFactory() {

            private int count = 1;

            @Override
            public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
                ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                thread.setName("yade-thread-" + (count++));
                return thread;
            }

        }, null, false);
    }

    public static void shutdown(ForkJoinPool pool) {
        if (pool == null) {
            return;
        }
        pool.shutdown();
        try {
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            // preserve interrupt status and force shutdown
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static int getParallelism(YADEArguments args, int sourceFilesSize) {
        if (sourceFilesSize <= 1) {
            return 1;
        }
        return args.isParallelismEnabled() ? args.getParallelism().getValue().intValue() : 1;
    }
}
