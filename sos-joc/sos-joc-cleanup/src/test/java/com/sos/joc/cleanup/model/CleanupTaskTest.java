package com.sos.joc.cleanup.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSPath;
import com.sos.joc.cleanup.model.CleanupTaskHistory.Range;
import com.sos.joc.cleanup.model.CleanupTaskHistory.Scope;
import com.sos.joc.cleanup.model.CleanupTaskMonitoring.MontitoringRange;
import com.sos.joc.cleanup.model.CleanupTaskMonitoring.MontitoringScope;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.cluster.common.state.JocClusterServiceTaskState;

public class CleanupTaskTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskTest.class);

    private AtomicBoolean isPaused;

    @Ignore
    @Test
    public void testCleanupDeployment() throws Exception {
        JocClusterHibernateFactory factory = null;
        try {
            factory = createFactory();
            CleanupTaskDeployment t = new CleanupTaskDeployment(factory, 1, "deployment", null);

            t.cleanupSearch();

            t.getDbLayer().close();

        } catch (Exception e) {
            throw e;
        } finally {
            if (factory != null) {
                factory.close();
            }
        }
    }

    @Ignore
    @Test
    public void testCleanupHistory() throws Exception {
        JocClusterHibernateFactory factory = null;
        CleanupTaskHistory t = null;
        try {
            factory = createFactory();
            t = new CleanupTaskHistory(factory, null, 1000, null);

            Date d = SOSDate.add(new Date(), -9, ChronoUnit.DAYS);

            JocClusterServiceTaskState state = t.cleanupOrders(Scope.MAIN, Range.ALL, d, "", true);
            LOGGER.info("[STATE]" + state);
        } catch (Exception e) {
            if (t != null && t.getDbLayer() != null) {
                t.getDbLayer().rollback();
            }
            throw e;
        } finally {
            if (t != null && t.getDbLayer() != null) {
                t.getDbLayer().close();
            }
            if (factory != null) {
                factory.close();
            }
        }
    }

    @Ignore
    @Test
    public void testCleanupMonitoring() throws Exception {
        JocClusterHibernateFactory factory = null;
        CleanupTaskMonitoring t = null;
        try {
            factory = createFactory();
            t = new CleanupTaskMonitoring(factory, null, 1000, null);

            Date d = SOSDate.add(new Date(), -2, ChronoUnit.DAYS);

            JocClusterServiceTaskState state = t.cleanupOrders(MontitoringScope.MAIN, MontitoringRange.ALL, d, "");
            LOGGER.info("[STATE]" + state);
        } catch (Exception e) {
            if (t != null && t.getDbLayer() != null) {
                t.getDbLayer().rollback();
            }
            throw e;
        } finally {
            if (t != null && t.getDbLayer() != null) {
                t.getDbLayer().close();
            }
            if (factory != null) {
                factory.close();
            }
        }
    }

    @Ignore
    @Test
    public void testLastModified() throws SOSInvalidDataException {
        Path p = Paths.get("src/test/resources/hibernate.cfg.xml");
        LOGGER.info(String.format("[%s][ms=%s]%s", TimeZone.getDefault().getID(), SOSPath.getLastModified(p).getTime(), SOSDate.getDateTimeAsString(
                SOSPath.getLastModified(p))));

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        LOGGER.info(String.format("[%s][ms=%s]%s", TimeZone.getDefault().getID(), SOSPath.getLastModified(p).getTime(), SOSDate.getDateTimeAsString(
                SOSPath.getLastModified(p))));

    }

    @Ignore
    @Test
    public void testPauseScheduleAtFixedRate() {
        LOGGER.info("[START]");
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        long duration = 5;
        long delay = 5;
        Runnable pauseTask = () -> {
            LOGGER.info("[pauseHandler]start...");
            // task.getService().stopPause();
            try {
                Thread.sleep(duration + 1 * 1_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Delay interrupted: " + e.getMessage());
            }
            LOGGER.info("    end");
        };
        // scheduler.scheduleAtFixedRate(pauseTask, pauseConfig.getDuration(), pauseConfig.getDuration() + pauseConfig.getDelay(), TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(pauseTask, delay, duration + delay, TimeUnit.SECONDS);

        try {
            TimeUnit.SECONDS.sleep(duration * 6);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        scheduler.shutdownNow();
        LOGGER.info("[END]");
    }

    @Ignore
    @Test
    public void testPauseSubmit() {
        isPaused = new AtomicBoolean(false);
        LOGGER.info("[START]");
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        long duration = 5;
        long delay = 5;
        Runnable pauseTask = () -> {
            boolean run = true;
            x: while (run) {
                boolean stopService = true;
                isPaused.set(false);
                LOGGER.info("[pauseHandler][start]stopService=" + stopService + ",pause=" + isPaused.get());
                try {
                    TimeUnit.SECONDS.sleep(duration);
                } catch (InterruptedException e) {
                    run = false;
                    Thread.currentThread().interrupt();
                    System.err.println("[duration]Delay interrupted: " + e.getMessage());
                    break x;
                }
                stopService = false;
                isPaused.set(true);
                LOGGER.info("    stopService=" + stopService + ",pause=" + isPaused.get());

                scheduler.submit(() -> {
                    while (isPaused.get()) {
                        try {
                            LOGGER.info("    wait because isPaused...");
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                });

                try {
                    TimeUnit.SECONDS.sleep(delay);
                } catch (InterruptedException e) {
                    run = false;
                    isPaused.set(false);
                    Thread.currentThread().interrupt();
                    System.err.println("[delay]Delay interrupted: " + e.getMessage());
                }
                LOGGER.info("    end");
            }
        };
        scheduler.submit(pauseTask);
        try {
            TimeUnit.SECONDS.sleep(duration * 6);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        scheduler.shutdownNow();
        LOGGER.info("[END]");
    }

    private JocClusterHibernateFactory createFactory() throws Exception {
        JocClusterHibernateFactory factory = new JocClusterHibernateFactory(Paths.get("src/test/resources/hibernate/hibernate.cfg.mysql.xml"), 1, 1);
        factory.addClassMapping(DBLayer.getJocClassMapping());
        factory.setAutoCommit(false);
        factory.build();
        return factory;
    }
}
