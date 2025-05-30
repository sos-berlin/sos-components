package com.sos.joc.cleanup.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSPath;
import com.sos.joc.cleanup.CleanupService;
import com.sos.joc.cleanup.CleanupServiceConfiguration;
import com.sos.joc.cleanup.CleanupServiceConfiguration.Age;
import com.sos.joc.cleanup.CleanupServiceSchedule;
import com.sos.joc.cleanup.CleanupServiceTask;
import com.sos.joc.cleanup.CleanupServiceTask.TaskDateTime;
import com.sos.joc.cleanup.model.CleanupTaskHistory.Range;
import com.sos.joc.cleanup.model.CleanupTaskHistory.Scope;
import com.sos.joc.cleanup.model.CleanupTaskMonitoring.MontitoringRange;
import com.sos.joc.cleanup.model.CleanupTaskMonitoring.MontitoringScope;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsCleanup;
import com.sos.joc.cluster.configuration.globals.common.ConfigurationEntry;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.cluster.common.state.JocClusterServiceTaskState;

public class CleanupTaskTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskTest.class);

    private AtomicBoolean isPaused;

    @Ignore
    @Test
    public void testCleanupDeploymentSearch() throws Exception {
        JocClusterHibernateFactory factory = null;
        CleanupTaskDeployment t = null;
        try {
            factory = createFactory();
            t = new CleanupTaskDeployment(factory, 1, "deployment", null);

            t.cleanupSearch();
        } catch (Throwable e) {
            throw e;
        } finally {
            close(t, factory);
        }
    }

    @Ignore
    @Test
    public void testCleanupHistoryOrders() throws Exception {
        JocClusterHibernateFactory factory = null;
        CleanupTaskHistory t = null;
        try {
            factory = createFactory();
            t = new CleanupTaskHistory(factory, null, 1000, null);

            Date d = SOSDate.add(new Date(), -9, ChronoUnit.DAYS);

            JocClusterServiceTaskState state = t.cleanupOrders(Scope.MAIN, Range.ALL, d, "", true);
            LOGGER.info("[STATE]" + state);
        } catch (Throwable e) {
            rollback(t);
            throw e;
        } finally {
            close(t, factory);
        }
    }

    @Ignore
    @Test
    public void testCleanupHistoryControllers() throws Exception {
        JocClusterHibernateFactory factory = null;
        CleanupTaskHistory t = null;
        try {
            factory = createFactory();
            t = new CleanupTaskHistory(factory, null, 1000, null);

            Date readyTime = SOSDate.add(new Date(), -365, ChronoUnit.DAYS);

            StringBuilder log = new StringBuilder();

            t.tryOpenSession();
            t.getDbLayer().beginTransaction();
            t.deleteControllers(readyTime, log);
            t.getDbLayer().commit();
            LOGGER.info("[LOG]" + log);
        } catch (Throwable e) {
            rollback(t);
            throw e;
        } finally {
            close(t, factory);
        }
    }

    @Ignore
    @Test
    public void testCleanupHistoryAgents() throws Exception {
        JocClusterHibernateFactory factory = null;
        CleanupTaskHistory t = null;
        try {
            factory = createFactory();
            t = new CleanupTaskHistory(factory, null, 1000, null);

            Date readyTime = SOSDate.add(new Date(), -365, ChronoUnit.DAYS);

            StringBuilder log = new StringBuilder();

            t.tryOpenSession();
            t.getDbLayer().beginTransaction();
            t.deleteAgents(readyTime, log);
            t.getDbLayer().commit();
            LOGGER.info("[LOG]" + log);
        } catch (Throwable e) {
            rollback(t);
            throw e;
        } finally {
            close(t, factory);
        }
    }

    @Ignore
    @Test
    public void testCleanupMonitoringOrders() throws Exception {
        JocClusterHibernateFactory factory = null;
        CleanupTaskMonitoring t = null;
        try {
            factory = createFactory();
            t = new CleanupTaskMonitoring(factory, null, 1000, null);

            Date d = SOSDate.add(new Date(), -2, ChronoUnit.DAYS);

            JocClusterServiceTaskState state = t.cleanupOrders(MontitoringScope.MAIN, MontitoringRange.ALL, d, "");
            LOGGER.info("[STATE]" + state);
        } catch (Throwable e) {
            rollback(t);
            throw e;
        } finally {
            close(t, factory);
        }
    }

    @Ignore
    @Test
    public void testCleanupUserProfiles() throws Exception {
        JocClusterHibernateFactory factory = null;
        CleanupTaskUserProfiles t = null;

        ConfigurationGlobalsCleanup cleanup = new ConfigurationGlobalsCleanup();
        cleanup.getTimeZone().setValue("Europe/Berlin");
        cleanup.getPeriodBegin().setValue("12:00");
        cleanup.getMaxPoolSize().setValue("1");
        cleanup.getBatchSize().setValue("1000");
        cleanup.getDeploymentHistoryVersions().setValue("5");

        cleanup.getProfileAge().setValue("365d");
        cleanup.getFailedLoginHistoryAge().setValue("5d");

        CleanupServiceTask cst = getCleanupServiceTask();
        try {
            factory = createFactory();
            t = new CleanupTaskUserProfiles(factory, 1000, "profiles", null);

            List<TaskDateTime> datetimes = new ArrayList<>();
            datetimes.add(getTaskDateTime(cst, cleanup, cleanup.getProfileAge()));
            datetimes.add(getTaskDateTime(cst, cleanup, cleanup.getFailedLoginHistoryAge()));

            JocClusterServiceTaskState state = t.cleanup(datetimes);
            LOGGER.info("[STATE]" + state);
        } catch (Throwable e) {
            rollback(t);
            throw e;
        } finally {
            close(t, factory);
        }
    }

    private static CleanupServiceTask getCleanupServiceTask() {
        return new CleanupServiceTask(new CleanupServiceSchedule(new CleanupService(null, Thread.currentThread().getThreadGroup())));
    }

    private static TaskDateTime getTaskDateTime(CleanupServiceTask t, ConfigurationGlobalsCleanup cleanup, ConfigurationEntry entry) {
        Age age = new CleanupServiceConfiguration(cleanup).new Age(entry);
        return t.new TaskDateTime(age, Instant.now().atZone(ZoneId.of(cleanup.getTimeZone().getValue())));
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

    private void rollback(CleanupTaskModel t) {
        if (t != null && t.getDbLayer() != null) {
            t.getDbLayer().rollback();
        }
    }

    private void close(CleanupTaskModel t, SOSHibernateFactory factory) {
        if (t != null && t.getDbLayer() != null) {
            t.getDbLayer().close();
        }
        if (factory != null) {
            factory.close();
        }
    }

    private JocClusterHibernateFactory createFactory() throws Exception {
        JocClusterHibernateFactory factory = new JocClusterHibernateFactory(Paths.get("src/test/resources/hibernate/hibernate.cfg.mysql.xml"), 1, 1);
        factory.addClassMapping(DBLayer.getJocClassMapping());
        factory.setAutoCommit(false);
        factory.build();
        return factory;
    }
}
