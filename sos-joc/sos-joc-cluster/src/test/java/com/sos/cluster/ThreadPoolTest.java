package com.sos.cluster;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadPoolTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolTest.class);

    private static final int MAX_EXECUTION_TIME = 20;

    @Ignore
    @Test
    public void testFixedTreadPool() throws Exception {
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);

        submitTask(threadPool, "1");
        submitTask(threadPool, "2");
        submitTask(threadPool, "3");

        setPoolSize(threadPool, 5);

        submitTask(threadPool, "4");
        submitTask(threadPool, "5");
        submitTask(threadPool, "6");

        setPoolSize(threadPool, 1);

        submitTask(threadPool, "7");
        submitTask(threadPool, "8");

        setStopper(threadPool);
    }

    private void submitTask(ThreadPoolExecutor threadPool, String identifier) {
        Runnable task = new Runnable() {

            @Override
            public void run() {

                LOGGER.info(String.format("[run][%s]start ...", identifier));
                LOGGER.info(String.format("[run][%s]poolSize=%s", identifier, threadPool.getPoolSize()));
                waitFor(5);
                LOGGER.info(String.format("[run][%s]end", identifier));
            }

        };
        threadPool.submit(task);
    }

    private void setStopper(ThreadPoolExecutor threadPool) {
        LOGGER.info(String.format("[start][setStopper][%ss]...", MAX_EXECUTION_TIME));
        waitFor(MAX_EXECUTION_TIME);
        shutdown(threadPool);
        LOGGER.info(String.format("[end][setStopper][%ss]", MAX_EXECUTION_TIME));
    }

    private void setPoolSize(ThreadPoolExecutor threadPool, int size) {
        waitFor(5);
        LOGGER.info("[set pool size]" + size);
        threadPool.setCorePoolSize(size);
        threadPool.setMaximumPoolSize(size);
    }

    private void shutdown(ThreadPoolExecutor threadPool) {
        try {
            if (threadPool == null) {
                return;
            }
            threadPool.shutdown();
            if (threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                LOGGER.info(String.format("[shutdown]thread pool has been shut down correctly"));
            } else {
                threadPool.shutdownNow();
                if (threadPool.awaitTermination(3, TimeUnit.SECONDS)) {
                    LOGGER.info(String.format("[shutdown]thread pool has ended due to timeout of 3s on shutdown"));
                } else {
                    LOGGER.info(String.format("[shutdown]thread pool did not terminate due to timeout of 3s on shutdown"));
                }
            }
        } catch (InterruptedException e) {
            LOGGER.error(String.format("[shutdown][exception]%s", e.toString()), e);
            threadPool.shutdownNow();
        }
    }

    private void waitFor(int seconds) {
        try {
            // LOGGER.info(String.format("[wait]%s...", seconds));
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            LOGGER.error(e.toString(), e);
        }
    }

}
