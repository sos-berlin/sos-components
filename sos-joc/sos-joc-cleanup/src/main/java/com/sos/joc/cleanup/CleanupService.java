package com.sos.joc.cleanup;

import java.nio.file.Path;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.joc.Globals;
import com.sos.joc.cluster.AJocClusterService;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.js7.event.controller.configuration.controller.ControllerConfiguration;

public class CleanupService extends AJocClusterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupService.class);

    private static final String IDENTIFIER = ClusterServices.cleanup.name();
    private static final int MAX_POOL_SIZE = 3;

    private JocClusterHibernateFactory factory;
    private ExecutorService threadPool = null;
    private CleanupServiceSchedule schedule = null;
    private CleanupServiceConfiguration config = null;
    private AtomicBoolean closed = new AtomicBoolean(false);

    public CleanupService(JocConfiguration jocConf, ThreadGroup clusterThreadGroup) {
        super(jocConf, clusterThreadGroup, IDENTIFIER);
        setConfig();
    }

    @Override
    public JocClusterAnswer start(List<ControllerConfiguration> controllers, StartupMode mode) {
        try {
            closed.set(false);
            AJocClusterService.setLogger(IDENTIFIER);

            LOGGER.info(String.format("[%s][%s]start...", getIdentifier(), mode));
            LOGGER.info(String.format("[%s][%s]%s", getIdentifier(), mode, config.toString()));

            if (config.getPeriod() == null) {
                LOGGER.error(String.format("[%s][%s][skip start]missing \"cleanup_period\" parameter", getIdentifier(), mode));
                return JocCluster.getOKAnswer(JocClusterAnswerState.MISSING_CONFIGURATION);
            } else {
                createFactory(getJocConfig().getHibernateConfiguration());

                threadPool = Executors.newFixedThreadPool(1, new JocClusterThreadFactory(getThreadGroup(), IDENTIFIER + "-start"));
                schedule = new CleanupServiceSchedule(this);
                AtomicLong errors = new AtomicLong();
                Runnable thread = new Runnable() {

                    @Override
                    public void run() {
                        while (!closed.get()) {
                            AJocClusterService.setLogger(IDENTIFIER);
                            try {
                                schedule.start(mode);
                            } catch (Throwable e) {
                                LOGGER.error(e.toString(), e);
                                long current = errors.get();
                                if (current > 100) {
                                    closed.set(true);
                                    LOGGER.error("[stopped]max errors(" + current + ") reached");
                                } else {
                                    errors.set(current + 1);
                                }
                            }
                            AJocClusterService.clearLogger();
                        }
                    }
                };
                threadPool.submit(thread);
                return JocCluster.getOKAnswer(JocClusterAnswerState.STARTED);
            }
        } catch (Exception e) {
            return JocCluster.getErrorAnswer(e);
        } finally {
            AJocClusterService.clearLogger();
        }
    }

    @Override
    public JocClusterAnswer stop(StartupMode mode) {
        AJocClusterService.setLogger(IDENTIFIER);
        LOGGER.info(String.format("[%s][%s]stop...", getIdentifier(), mode));

        closed.set(true);
        close(mode);
        closeFactory();
        LOGGER.info(String.format("[%s][%s]stopped", getIdentifier(), mode));

        AJocClusterService.clearLogger();
        return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
    }

    @Override
    public JocServiceAnswer getInfo() {
        return new JocServiceAnswer();
    }

    private void setConfig() {
        this.config = new CleanupServiceConfiguration(Globals.sosCockpitProperties.getProperties());
    }

    public CleanupServiceConfiguration getConfig() {
        return config;
    }

    private void close(StartupMode mode) {
        if (schedule != null) {
            schedule.close(mode);
        }
        if (threadPool != null) {
            JocCluster.shutdownThreadPool(mode, threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);
            threadPool = null;
        }
    }

    public JocClusterHibernateFactory getFactory() {
        return factory;
    }

    public static Date getCurrentDateTimeMinusMinutes(Long minutes) {
        return Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).minusMinutes(minutes).toInstant());
    }

    public static String toString(Date date) {
        try {
            return SOSDate.getDateTimeAsString(date, SOSDate.dateTimeFormat);
        } catch (Exception e) {
            return date == null ? "null" : date.toString();
        }
    }

    private void createFactory(Path configFile) throws Exception {
        factory = new JocClusterHibernateFactory(configFile, 1, MAX_POOL_SIZE);
        factory.setIdentifier(IDENTIFIER);
        factory.setAutoCommit(false);
        factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        factory.addClassMapping(DBLayer.getJocClassMapping());
        factory.build();
    }

    private void closeFactory() {
        if (factory != null) {
            factory.close();
            factory = null;
        }
        LOGGER.info(String.format("[%s]database factory closed", getIdentifier()));
    }
}
