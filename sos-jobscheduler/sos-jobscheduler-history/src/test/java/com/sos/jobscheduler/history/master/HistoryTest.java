package com.sos.jobscheduler.history.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HistoryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryTest.class);

    public static String getFreeMemory() {
        long kb = Runtime.getRuntime().freeMemory() / 1024;
        return new StringBuilder().append(kb).append(" KB (").append(kb / 1024).append(" MB)").toString();
    }

    public static void main(String[] args) {
        int maxCounter = args.length == 0 ? 10 : Integer.parseInt(args[0]);

        String name = Thread.currentThread().getName();
        LOGGER.info("[" + name + "]start: Free memory " + getFreeMemory());
        boolean closed = false;
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

}
