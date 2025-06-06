package com.sos.joc.cleanup;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSDate;
import com.sos.joc.cleanup.exception.CleanupComputeException;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.common.JocClusterServiceActivity;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration.Action;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsCleanup;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.service.JocClusterServiceLogger;
import com.sos.joc.cluster.service.active.AJocActiveMemberService;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.joc.model.cluster.common.state.JocClusterState;

public class CleanupService extends AJocActiveMemberService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupService.class);

    public static final String IDENTIFIER = ClusterServices.cleanup.name();

    private ExecutorService threadPool = null;
    private CleanupServiceSchedule schedule = null;
    private CleanupServiceConfiguration config = null;
    private AtomicBoolean closed = new AtomicBoolean(false);
    private AtomicBoolean runServiceNow = new AtomicBoolean(false);
    private final Object lock = new Object();

    public CleanupService(JocConfiguration jocConfiguration, ThreadGroup clusterThreadGroup) {
        super(jocConfiguration, clusterThreadGroup, IDENTIFIER);
        setServiceLogger();
    }

    @Override
    public synchronized JocClusterAnswer start(StartupMode mode, List<ControllerConfiguration> controllers,
            AConfigurationSection serviceSettingsSection) {
        try {
            setServiceLogger();
            stopOnStart(mode);

            setRunServiceNow("start", false);
            closed.set(false);

            setConfig((ConfigurationGlobalsCleanup) serviceSettingsSection);

            LOGGER.info(String.format("[%s][%s]start...", getIdentifier(), mode));
            // LOGGER.info(String.format("[%s][%s]%s", getIdentifier(), mode, config.toString()));
            if (config.getPeriod() == null || config.getPeriod().getWeekDays().size() == 0) {
                LOGGER.info(String.format("[%s][%s][stop]missing \"%s\" parameter", getIdentifier(), mode,
                        ConfigurationGlobalsCleanup.ENTRY_NAME_PERIOD));
                return JocCluster.getOKAnswer(JocClusterState.MISSING_CONFIGURATION);
            } else {
                threadPool = Executors.newFixedThreadPool(1, new JocClusterThreadFactory(getThreadGroup(), IDENTIFIER + "-start"));
                schedule = new CleanupServiceSchedule(this);
                AtomicLong errors = new AtomicLong();
                Runnable thread = new Runnable() {

                    @Override
                    public void run() {
                        StartupMode startupMode = mode;
                        boolean runNow = false;
                        while (!closed.get()) {
                            setServiceLogger();
                            try {
                                schedule.start(startupMode, runNow);
                                if (runNow) { // 2) - after next iteration
                                    runNow = false;
                                }
                                if (runServiceNow.get()) { // runServiceNow was set by another thread
                                    runNow = true; // 1) set runNow for the next while iteration
                                    startupMode = com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode.run_now;
                                }
                                if (!runNow) {
                                    startupMode = com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode.automatic;
                                    // waitFor(30);
                                }
                            } catch (CleanupComputeException e) {
                                closed.set(true);
                                setServiceLogger();
                                LOGGER.error(String.format("[%s][%s][start][stopped]%s", getIdentifier(), mode, e.toString()));
                            } catch (SOSHibernateException e) {
                                setServiceLogger();
                                LOGGER.error(e.toString(), e);
                                waitFor(60);
                            } catch (Throwable e) {
                                setServiceLogger();
                                LOGGER.error(e.toString(), e);
                                long current = errors.get();
                                if (current > 100) {
                                    closed.set(true);
                                    setServiceLogger();
                                    LOGGER.error(String.format("[%s][%s][start][stopped]max errors(%s) reached", getIdentifier(), mode, current));
                                } else {
                                    errors.set(current + 1);
                                    waitFor(60);
                                }
                            }
                        }
                    }
                };
                threadPool.submit(thread);
                return JocCluster.getOKAnswer(JocClusterState.STARTED);
            }
        } catch (Throwable e) {
            return JocCluster.getErrorAnswer(e);
        }
    }

    @Override
    public synchronized JocClusterAnswer stop(StartupMode mode) {
        setServiceLogger();
        LOGGER.info(String.format("[%s][%s]stop...", getIdentifier(), mode));

        closed.set(true);
        close(mode);
        LOGGER.info(String.format("[%s][%s]stopped", getIdentifier(), mode));
        removeServiceLogger();
        return JocCluster.getOKAnswer(JocClusterState.STOPPED);
    }

    @Override
    public synchronized JocClusterAnswer runNow(StartupMode mode, List<ControllerConfiguration> controllers,
            AConfigurationSection serviceSettingsSection) {
        setServiceLogger();

        if (schedule == null) {
            LOGGER.info(String.format("[%s][%s][runNow][skip]schedule=null", getIdentifier(), mode));
            return new JocClusterAnswer(JocClusterState.MISSING_CONFIGURATION, "schedule=null");
        }
        if (schedule.isBusy()) {
            LOGGER.info(String.format("[%s][%s][runNow][skip]isBusy=true", getIdentifier(), mode));
            return new JocClusterAnswer(JocClusterState.ALREADY_RUNNING);
        }

        if (serviceSettingsSection instanceof ConfigurationGlobalsCleanup) {
            setConfig((ConfigurationGlobalsCleanup) serviceSettingsSection);
        }
        try {
            schedule.runNow(mode);
        } catch (Throwable e) {
            LOGGER.info(String.format("[%s][%s][runNow]%s", getIdentifier(), mode, e.toString()), e);
            return JocCluster.getErrorAnswer(e);
        }
        return new JocClusterAnswer(JocClusterState.RUNNING);
    }

    @Override
    public void startPause(String caller, int pauseDurationInSeconds) {
    }

    @Override
    public void stopPause(String caller) {
    }

    @Override
    public JocClusterServiceActivity getActivity() {
        if (schedule == null) {
            return JocClusterServiceActivity.Relax();
        }
        return schedule.isBusy() ? JocClusterServiceActivity.Busy() : JocClusterServiceActivity.Relax();
    }

    @Override
    public void update(StartupMode mode, List<ControllerConfiguration> controllers, String controllerId, Action action) {

    }

    @Override
    public void update(StartupMode mode, AConfigurationSection settingsSection) {

    }

    @Override
    public void update(StartupMode mode, JocConfiguration jocConfiguration) {

    }

    public ZonedDateTime getNow() {
        return Instant.now().atZone(config.getZoneId());
    }

    private void setConfig(ConfigurationGlobalsCleanup configuration) {
        config = new CleanupServiceConfiguration(configuration);
        config.setHibernateConfiguration(getJocConfig().getHibernateConfiguration());
    }

    public CleanupServiceConfiguration getConfig() {
        return config;
    }

    private void close(StartupMode mode) {
        cancelWaitFor();
        setServiceLogger();
        if (schedule != null) {
            schedule.stop(mode);
            schedule = null;
        }
        if (threadPool != null) {
            JocCluster.shutdownThreadPool("[" + getIdentifier() + "][" + mode + "]", threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);
            threadPool = null;
        }
        removeServiceLogger();
    }

    private void stopOnStart(StartupMode mode) {
        if (schedule != null || threadPool != null) {
            LOGGER.info(String.format("[%s][%s][stopOnStart]stop...", getIdentifier(), mode));

            closed.set(true);
            close(mode);
        }
    }

    public static Date toDate(ZonedDateTime dateTime) {
        return dateTime == null ? null : Date.from(dateTime.toInstant());
    }

    public static ZonedDateTime getZonedDateTimeUTCMinusMinutes(ZonedDateTime datetime, long minutes) {
        return datetime.withZoneSameInstant(ZoneId.of(SOSDate.TIMEZONE_UTC)).minusMinutes(minutes);
    }

    public static String toString(Date date) {
        try {
            return SOSDate.getDateTimeAsString(date);
        } catch (Exception e) {
            return date == null ? "null" : date.toString();
        }
    }

    private void cancelWaitFor() {
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    private void waitFor(int interval) {
        if (!closed.get() && interval > 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[wait]%ss ...", interval));
            }
            try {
                synchronized (lock) {
                    lock.wait(interval * 1_000);
                }
            } catch (InterruptedException e) {
                if (closed.get()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("[wait]sleep interrupted due to task stop");
                    }
                } else {
                    LOGGER.warn(String.format("[wait]%s", e.toString()), e);
                }
            }
        }
    }

    protected void setRunServiceNow(String caller, boolean val) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[setRunServiceNow][" + caller + "]" + val);
        }
        runServiceNow.set(val);
        if (val) {
            cancelWaitFor();
        }
    }

    public static void setServiceLogger() {
        JocClusterServiceLogger.setLogger(IDENTIFIER);
    }

    public static void removeServiceLogger() {
        JocClusterServiceLogger.removeLogger(IDENTIFIER);
    }
}
