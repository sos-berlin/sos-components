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
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration.Action;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsCleanup;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.service.JocClusterServiceLogger;
import com.sos.joc.cluster.service.active.AJocActiveMemberService;
import com.sos.joc.model.cluster.common.ClusterServices;

public class CleanupService extends AJocActiveMemberService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupService.class);

    private static final String IDENTIFIER = ClusterServices.cleanup.name();

    private ExecutorService threadPool = null;
    private CleanupServiceSchedule schedule = null;
    private CleanupServiceConfiguration config = null;
    private AtomicBoolean closed = new AtomicBoolean(false);
    private AtomicLong lastActivityStart = new AtomicLong();
    private AtomicLong lastActivityEnd = new AtomicLong();
    private final Object lock = new Object();

    public CleanupService(JocConfiguration jocConf, ThreadGroup clusterThreadGroup) {
        super(jocConf, clusterThreadGroup, IDENTIFIER);
        setServiceLogger();
    }

    @Override
    public JocClusterAnswer start(StartupMode mode, List<ControllerConfiguration> controllers, AConfigurationSection configuration) {
        try {
            closed.set(false);
            lastActivityStart.set(new Date().getTime());
            setServiceLogger();

            setConfig((ConfigurationGlobalsCleanup) configuration);

            LOGGER.info(String.format("[%s][%s]start...", getIdentifier(), mode));
            LOGGER.info(String.format("[%s][%s]%s", getIdentifier(), mode, config.toString()));
            lastActivityEnd.set(new Date().getTime());
            if (config.getPeriod() == null || config.getPeriod().getWeekDays().size() == 0) {
                LOGGER.info(String.format("[%s][%s][stop]missing \"%s\" parameter", getIdentifier(), mode,
                        CleanupServiceConfiguration.PROPERTY_NAME_PERIOD));
                return JocCluster.getOKAnswer(JocClusterAnswerState.MISSING_CONFIGURATION);
            } else {
                threadPool = Executors.newFixedThreadPool(1, new JocClusterThreadFactory(getThreadGroup(), IDENTIFIER + "-start"));
                schedule = new CleanupServiceSchedule(this);
                AtomicLong errors = new AtomicLong();
                Runnable thread = new Runnable() {

                    @Override
                    public void run() {
                        StartupMode startupMode = mode;
                        while (!closed.get()) {
                            setServiceLogger();
                            try {
                                schedule.start(startupMode);
                                startupMode = StartupMode.automatic;
                                waitFor(30);
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
                return JocCluster.getOKAnswer(JocClusterAnswerState.STARTED);
            }
        } catch (Exception e) {
            return JocCluster.getErrorAnswer(e);
        }
    }

    @Override
    public JocClusterAnswer stop(StartupMode mode) {
        setServiceLogger();
        LOGGER.info(String.format("[%s][%s]stop...", getIdentifier(), mode));

        closed.set(true);
        close(mode);
        LOGGER.info(String.format("[%s][%s]stopped", getIdentifier(), mode));
        removeServiceLogger();
        return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
    }

    @Override
    public JocServiceAnswer getInfo() {
        return new JocServiceAnswer(Instant.ofEpochMilli(lastActivityStart.get()), Instant.ofEpochMilli(lastActivityEnd.get()));
    }

    @Override
    public void update(StartupMode mode, List<ControllerConfiguration> controllers, String controllerId, Action action) {

    }

    @Override
    public void update(StartupMode mode, AConfigurationSection configuration) {

    }

    private void setConfig(ConfigurationGlobalsCleanup configuration) {
        config = new CleanupServiceConfiguration(configuration);
        config.setHibernateConfiguration(getJocConfig().getHibernateConfiguration());
    }

    public CleanupServiceConfiguration getConfig() {
        return config;
    }

    private void close(StartupMode mode) {
        synchronized (lock) {
            lock.notifyAll();
        }
        setServiceLogger();
        if (schedule != null) {
            schedule.stop(mode);
        }
        if (threadPool != null) {
            JocCluster.shutdownThreadPool(mode, threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);
            threadPool = null;
        }
        removeServiceLogger();
    }

    public static Date toDate(ZonedDateTime dateTime) {
        return dateTime == null ? null : Date.from(dateTime.toInstant());
    }

    public static ZonedDateTime getZonedDateTimeUTCMinusMinutes(ZonedDateTime datetime, long minutes) {
        return datetime.withZoneSameInstant(ZoneId.of("UTC")).minusMinutes(minutes);
    }

    public static String toString(Date date) {
        try {
            return SOSDate.getDateTimeAsString(date);
        } catch (Exception e) {
            return date == null ? "null" : date.toString();
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

    protected void setLastActivityStart(Long val) {
        lastActivityStart.set(val);
    }

    protected void setLastActivityEnd(Long val) {
        lastActivityEnd.set(val);
    }

    protected static void setServiceLogger() {
        JocClusterServiceLogger.setLogger(IDENTIFIER);
    }

    private static void removeServiceLogger() {
        JocClusterServiceLogger.removeLogger(IDENTIFIER);
    }
}
