package com.sos.cluster;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadTest.class);

    private Object lock = new Object();
    private String name;

    public void A() {
        synchronized (lock) {
            name = "test A";
            LOGGER.info("A name=" + name);
        }
    }

    public void B() {
        synchronized (lock) {
            name = "test B";
            LOGGER.info("B name=" + name);
        }
    }

    public static void main(String[] args) {
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);

        ThreadTest t1 = new ThreadTest();

        threadPool.submit(new Runnable() {

            @Override
            public void run() {
                t1.A();
            }
        });
        threadPool.submit(new Runnable() {

            @Override
            public void run() {
                t1.B();
            }
        });
        threadPool.submit(new Runnable() {

            @Override
            public void run() {
                t1.A();
            }
        });
        threadPool.shutdown();
    }
}
