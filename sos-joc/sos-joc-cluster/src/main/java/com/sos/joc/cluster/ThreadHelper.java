package com.sos.joc.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSClassUtil;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;

public class ThreadHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadHelper.class);

    public static ThreadGroup getThreadGroup() {
        return Thread.currentThread().getThreadGroup();
    }

    public static Collection<Thread> getThreads(final ThreadGroup group, final boolean recurse, final String threadPrefix) {
        try {
            Thread[] threads = getThreads(group);
            if (threads != null) {
                final List<Thread> result = new ArrayList<>();
                for (int i = 0; i < threads.length; ++i) {
                    Thread t = threads[i];
                    if (t != null) {
                        if (t.getName().matches("^(" + threadPrefix + ").*")) {
                            result.add(threads[i]);
                        }
                    }
                }
                return Collections.unmodifiableCollection(result);
            }
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
        return null;
    }

    public static Thread[] getThreads(final ThreadGroup group) {
        try {
            int count = group.activeCount();
            Thread[] result;
            do {
                result = new Thread[count + (count / 2) + 1]; // slightly grow the array size
                count = group.enumerate(result, true);
                // return value of enumerate() must be strictly less than the array size according to javadoc
            } while (count >= result.length);
            return result;
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
        return null;
    }

    public static ThreadGroup[] getThreadGroups(final ThreadGroup group) {
        try {
            int count = group.activeGroupCount();
            ThreadGroup[] result;
            do {
                result = new ThreadGroup[count + (count / 2) + 1]; // slightly grow the array size
                count = group.enumerate(result, true);
                // return value of enumerate() must be strictly less than the array size according to javadoc
            } while (count >= result.length);
            return result;
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
        return null;
    }

    public static void tryStop(final StartupMode mode, final ThreadGroup group) {
        if (group == null) {
            return;
        }

        try {
            final int maxAttempts = 5;
            int count = 0;

            Thread[] threads = null;
            for (int attempt = 0; attempt < maxAttempts; attempt++) {
                // reserve - activeCount is only an estimate - 2x to handle newly started threads...
                int estimated = group.activeCount() * 2;
                threads = new Thread[estimated];
                count = group.enumerate(threads, false);
                // break if array is large enough to hold all threads
                if (count < threads.length) {
                    break;
                }
            }

            List<Thread> activeThreads = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                Thread t = threads[i];
                if (t != null && t.isAlive()) {
                    activeThreads.add(t);
                }
            }

            int activeCount = activeThreads.size();
            LOGGER.info(String.format("[%s][tryStop][group=%s]activeCount=%s", mode, group.getName(), activeCount));
            if (activeCount == 0) {
                return;
            }

            for (Thread t : activeThreads) {
                if (t != null && t.isAlive()) {// double check
                    t.interrupt();
                    LOGGER.info(String.format("[%s][tryStop][group=%s][interrupt]%s", mode, group.getName(), t.getName()));
                }
            }
            Thread.sleep(500);

            for (Thread t : activeThreads) {
                if (t != null && t.isAlive()) {// double check
                    t.interrupt();
                    LOGGER.info(String.format("[%s][tryStop][group=%s][still alive]%s", mode, group.getName(), t.getName()));
                }
            }

        } catch (Exception e) {
            LOGGER.warn(String.format("[%s][tryStop][group=%s]%s", mode, group.getName(), e.toString()), e);
        }
    }

    public static void tryStopChilds(final StartupMode mode, final ThreadGroup group, Set<String> excludedGroups) {
        try {
            if (group != null) {
                ThreadGroup[] result = getThreadGroups(group);
                if (result != null) {
                    for (int i = 0; i < result.length; i++) {
                        ThreadGroup tg = result[i];
                        if (tg != null) {
                            ThreadGroup ptg = tg.getParent();
                            if (excludedGroups.contains(tg.getName()) || (ptg != null && excludedGroups.contains(ptg.getName()))) {
                                continue;
                            }
                            tryStop(mode, result[i]);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            LOGGER.warn(String.format("[%s][tryStopChildGroups][group=%s]%s", mode, group.getName(), e.toString()), e);
        }
    }

    @SuppressWarnings("deprecation")
    public static void tryStop(final StartupMode mode, final String threadPrefix) {
        try {
            Collection<Thread> threads = ThreadHelper.getThreads(ThreadHelper.getThreadGroup(), true, threadPrefix);
            if (threads != null) {
                for (Thread t : threads) {
                    if (t != null) {
                        LOGGER.info(String.format("[%s][stop][%s]%s", mode, t.getState(), t));
                        if (!t.isInterrupted()) {
                            try {
                                t.interrupt();
                                Thread.sleep(500);
                            } catch (Throwable e) {
                                LOGGER.warn(e.toString(), e);
                            }
                        }
                        try {
                            if (t.isAlive()) {
                                t.stop();// TODO
                            }
                        } catch (Throwable e) {
                            LOGGER.warn(e.toString(), e);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            LOGGER.warn(e.toString(), e);
        }
    }

    public static void print(final StartupMode mode, String header) {
        if (!LOGGER.isDebugEnabled()) {
            return;
        }
        try {
            LOGGER.debug(String.format("[%s][threads][%s]%s", mode, SOSClassUtil.getMethodName(2), header));
            Thread[] threads = ThreadHelper.getThreads(ThreadHelper.getThreadGroup());
            if (threads != null) {
                for (Thread t : threads) {
                    if (t != null) {
                        String tg = t.getThreadGroup() == null ? "unknown" : t.getThreadGroup().getName();
                        LOGGER.debug(String.format("  [group=%s]%s daemon=%s", tg, t, t.isDaemon()));
                    }
                }
            }
        } catch (Throwable e) {
            LOGGER.warn(e.toString(), e);
        }
    }

}
