package com.sos.jobscheduler.history.master;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HistoryFutureExecutorServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryEventHandler.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private final int maxCounter;
    private List<Future<MyObject>> futureList = new ArrayList<Future<MyObject>>();
    private final List<MyObject> activeHandlers = Collections.synchronizedList(new ArrayList<MyObject>());
    private boolean closed = false;

    private final ExecutorService threadPool;

    public HistoryFutureExecutorServiceTest(int max) {
        threadPool = Executors.newFixedThreadPool(2);
        maxCounter = max;
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public void start() throws Exception {
        Runnable task = new Runnable() {

            @Override
            public void run() {
                MyObject obj = new MyObject(Thread.currentThread().getName());
                LOGGER.info("[" + obj.getName() + "]start");
                activeHandlers.add(obj);
                int i = 0;
                while (!obj.isClosed()) {
                    i++;
                    obj.setCounter(i);

                    LOGGER.info("[" + obj.getName() + "] " + obj.getCounter() + ", closed=" + obj.isClosed());
                    try {
                        Thread.sleep(1_000);
                    } catch (Throwable e) {
                        LOGGER.info("[" + obj.getName() + "]sleep interrupted");
                    }
                    if (i == maxCounter) {
                        obj.setIsClosed(true);
                        closed = true;
                    }
                }
                LOGGER.info("[" + obj.getName() + "]end");
                // return obj;
            }

        };
        // futureList.add(threadPool.submit(task));
        threadPool.submit(task);
    }

    public ExecutorService getTreadPool() {
        return threadPool;
    }

    public List<Future<MyObject>> getTasks() {
        return futureList;
    }

    public boolean getClosed() {
        return closed;
    }

    public List<MyObject> getActiveHandler() {
        return activeHandlers;
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

    public class MyObject {

        private final String name;
        private int counter;
        private boolean closed;

        public MyObject(String n) {
            name = n;
        }

        public String getName() {
            return name;
        }

        public void setCounter(int c) {
            counter = c;
        }

        public int getCounter() {
            return counter;
        }

        public void setIsClosed(boolean c) {
            closed = c;
        }

        public boolean isClosed() {
            return closed;
        }
    }

    public static void main(String[] args) throws Exception {
        LOGGER.info("programm start ");

        int maxCounter = args.length == 0 ? 10 : Integer.parseInt(args[0]);
        HistoryFutureExecutorServiceTest t = new HistoryFutureExecutorServiceTest(maxCounter);

        t.start();

        Thread.sleep(1_000);
        MyObject o = t.getActiveHandler().get(0);
        o.setIsClosed(true);
        System.out.println("handler size : " + t.getActiveHandler().size());
        
        t.start();
        Thread.sleep(1_000);
        o = t.getActiveHandler().get(1);

        ThreadPoolExecutor tp = (ThreadPoolExecutor)t.getTreadPool();
        System.out.println("ac : " + tp.getActiveCount());
        
        
        t.start();
        Thread.sleep(1_000);
        o = t.getActiveHandler().get(2);

        // System.out.println("future : " + t.getTasks().get(0).get().getName());

        // for(Future<MyObject> f : t.getTasks()) {
        // System.out.println("future : "+f.get().getName()+", " +f.get().getCounter());
        // }

        // t.start();

        while (!o.isClosed()) {
            try {
                Thread.sleep(1_000);
            } catch (Throwable e) {

            }
        }

        t.stop();
    }

}
