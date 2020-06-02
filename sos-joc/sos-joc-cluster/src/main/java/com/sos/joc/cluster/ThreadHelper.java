package com.sos.joc.cluster;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.sos.commons.util.SOSString;

public class ThreadHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadHelper.class);

    public static ThreadGroup getThreadGroup() {
        SecurityManager s = System.getSecurityManager();
        return (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
    }

    public static void stopThreads(List<String> l) {
        stopThreads(getThreadGroup(), "main", Joiner.on("|").join(l));
    }

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
                        t.stop();// TODO
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

    public static void showGroupInfo(String indent, ThreadGroup group) {

        Thread[] threads = new Thread[group.activeCount()];
        group.enumerate(threads, false);

        LOGGER.info(group.toString().trim());

        for (Thread t : threads) {
            if (t != null) {
                LOGGER.info(String.format("%s[group=%s]%s daemon=%s", indent, group.getName(), t, t.isDaemon()));
            }
        }

        ThreadGroup[] activeGroup = new ThreadGroup[group.activeGroupCount()];
        group.enumerate(activeGroup, false);

        for (ThreadGroup g : activeGroup) {
            showGroupInfo(indent + indent, g);
        }
    }
}
