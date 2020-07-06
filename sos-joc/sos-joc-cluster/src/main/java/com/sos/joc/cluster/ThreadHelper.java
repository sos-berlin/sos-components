package com.sos.joc.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSClassUtil;

public class ThreadHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadHelper.class);

    public static ThreadGroup getThreadGroup() {
        SecurityManager s = System.getSecurityManager();
        return (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
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

    @SuppressWarnings("deprecation")
    public static void tryStop(final ThreadGroup group) {
        try {
            if (group != null && group.activeCount() > 0) {
                group.interrupt();
                Thread.sleep(500);
                if (group.activeCount() > 0) {
                    LOGGER.info(String.format("[stop][group=%s]activeCount=%s", group.getName(), group.activeCount()));
                    group.stop();
                    Thread.sleep(500);
                }
                LOGGER.info(String.format("[stopped][group=%s]activeCount=%s", group.getName(), group.activeCount()));
            }
        } catch (Throwable e) {
            LOGGER.warn(String.format("[stop][group=%s]%s", group.getName(), e.toString()), e);
        }
    }

    public static void tryStopChilds(final ThreadGroup group) {
        try {
            if (group != null) {
                ThreadGroup[] result = getThreadGroups(group);
                if (result != null) {
                    for (int i = 0; i < result.length; i++) {
                        tryStop(result[i]);
                    }
                }
            }
        } catch (Throwable e) {
            LOGGER.warn(String.format("[tryStopChildGroups][group=%s]%s", group.getName(), e.toString()), e);
        }
    }

    public static void tryStop(List<String> l) {
        tryStop(String.join("|", l));
    }

    @SuppressWarnings("deprecation")
    public static void tryStop(final String threadPrefix) {
        try {
            Collection<Thread> threads = ThreadHelper.getThreads(ThreadHelper.getThreadGroup(), true, threadPrefix);
            if (threads != null) {
                for (Thread t : threads) {
                    if (t != null) {
                        LOGGER.info(String.format("[stop][%s]%s", t.getState(), t));
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

    public static void print(String header) {
        try {
            LOGGER.info(String.format("[threads][print][%s]%s", SOSClassUtil.getMethodName(2), header));
            Thread[] threads = ThreadHelper.getThreads(ThreadHelper.getThreadGroup());
            if (threads != null) {
                for (Thread t : threads) {
                    if (t != null) {
                        String tg = t.getThreadGroup() == null ? "unknown" : t.getThreadGroup().getName();
                        LOGGER.info(String.format("  [group=%s]%s daemon=%s", tg, t, t.isDaemon()));
                    }
                }
            }
        } catch (Throwable e) {
            LOGGER.warn(e.toString(), e);
        }
    }

}
