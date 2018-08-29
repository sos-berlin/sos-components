package com.sos.jobscheduler.history.master;

import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HistoryExecutorServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryExecutorServiceTest.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private final int maxCounter;
    private boolean closed = false;

    private final ExecutorService threadPool;

    public HistoryExecutorServiceTest(int max) {
        threadPool = Executors.newFixedThreadPool(2);
        maxCounter = max;
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public void start() throws Exception {
        Runnable task = new Runnable() {

            @Override
            public void run() {
                String name = Thread.currentThread().getName();
                LOGGER.info("[" + name + "]start");
                try {
                    int i = 0;
                    while (!closed) {
                        i++;
                        LOGGER.info("[" + name + "] --- " + i);

                        if (i % 100 == 0) {
                            LOGGER.info("[" + name + "] --- Free memory after " + i + " " + getFreeMemory());
                        }

                        Thread.sleep(1_000);
                        if (i == maxCounter) {
                            closed = true;
                        }
                    }
                    LOGGER.info("[" + name + "]end");
                } catch (InterruptedException e) {
                    LOGGER.error(e.toString(), e);
                }
            }

        };
        threadPool.submit(task);
    }

    public boolean getClosed() {
        return closed;
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public void stop() {
        try {
            threadPool.shutdownNow();
            boolean shutdown = threadPool.awaitTermination(1L, TimeUnit.SECONDS);
            if (isDebugEnabled) {
                if (shutdown) {
                    LOGGER.debug(String.format("thread has been shut down correctly"));
                } else {
                    LOGGER.debug(String.format("thread has ended due to timeout on shutdown. doesn�t wait for answer from thread"));
                }
            }
        } catch (InterruptedException e) {
            LOGGER.error(e.toString(), e);
        }
    }

    public static String getFreeMemory() {
        long kb = Runtime.getRuntime().freeMemory() / 1024;
        return new StringBuilder().append(kb).append(" KB (").append(kb / 1024).append(" MB)").toString();
    }

    public static void main(String[] args) throws Exception {
        LOGGER.info("start: Free memory " + getFreeMemory());

        int maxCounter = args.length == 0 ? 10 : Integer.parseInt(args[0]);
        HistoryExecutorServiceTest t = new HistoryExecutorServiceTest(maxCounter);

        t.start();
        // t.start();

        while (!t.getClosed()) {
            Thread.sleep(1_000);
        }

        t.stop();
    }

}
