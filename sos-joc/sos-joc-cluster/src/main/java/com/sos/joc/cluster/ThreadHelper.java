package com.sos.joc.cluster;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;

public class ThreadHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadHelper.class);

    private static final String DEFAULT_GROUP_NAME = "main";

    public static ThreadGroup getThreadGroup() {
        SecurityManager s = System.getSecurityManager();
        return (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
    }

    public static void stopThreads(List<String> l) {
        stopThreads(getThreadGroup(), DEFAULT_GROUP_NAME, String.join("|", l));
    }

    public static void stopThreads(String threadNamePrefix) {
        stopThreads(getThreadGroup(), DEFAULT_GROUP_NAME, threadNamePrefix);
    }

    @SuppressWarnings("deprecation")
    // create and stop the ThreadGroups
    public static void stopThreads(ThreadGroup group, String groupName, String threadNames) {
        if (SOSString.isEmpty(threadNames)) {
            LOGGER.warn("missing thread names");
            return;
        }
        Thread[] threads = new Thread[group.activeCount()];
        group.enumerate(threads, false);

        if (group.getName().equals(groupName)) {
            for (Thread t : threads) {
                if (t != null) {
                    if (t.getName().matches("^(" + threadNames + ").*")) {
                        LOGGER.info(String.format("[STOP]%s", t));
                        try {
                            t.stop();// TODO
                        } catch (Throwable e) {
                        }

                    }
                }
            }
        }
        ThreadGroup[] activeGroup = new ThreadGroup[group.activeGroupCount()];
        group.enumerate(activeGroup, false);

        for (ThreadGroup g : activeGroup) {
            stopThreads(g, groupName, threadNames);
        }
    }

    public static void showGroupInfo(ThreadGroup group, String logTitle) {
        showGroupInfo(group, logTitle, " ");
    }

    private static void showGroupInfo(ThreadGroup group, String logTitle, String logIndent) {

        Thread[] threads = new Thread[group.activeCount()];
        group.enumerate(threads, false);

        LOGGER.info(String.format("[%s]%s", logTitle, group.toString().trim()));

        for (Thread t : threads) {
            if (t != null) {
                LOGGER.info(String.format("%s[group=%s]%s daemon=%s", logIndent, group.getName(), t, t.isDaemon()));
            }
        }

        ThreadGroup[] activeGroup = new ThreadGroup[group.activeGroupCount()];
        group.enumerate(activeGroup, false);

        for (ThreadGroup g : activeGroup) {
            showGroupInfo(g, logTitle, logIndent + logIndent);
        }
    }
}
