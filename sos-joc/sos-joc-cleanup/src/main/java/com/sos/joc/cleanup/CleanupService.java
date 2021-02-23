package com.sos.joc.cleanup;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.cluster.AJocClusterService;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer.JocServiceAnswerState;
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

    public CleanupService(JocConfiguration jocConf, ThreadGroup clusterThreadGroup) {
        super(jocConf, clusterThreadGroup, IDENTIFIER);
        setConfig();
    }

    @Override
    public JocClusterAnswer start(List<ControllerConfiguration> controllers, StartupMode mode) {
        try {
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
                Runnable thread = new Runnable() {

                    @Override
                    public void run() {
                        AJocClusterService.setLogger(IDENTIFIER);
                        schedule.start(mode);
                        AJocClusterService.clearLogger();
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

        close(mode);
        closeFactory();
        LOGGER.info(String.format("[%s][%s]stopped", getIdentifier(), mode));

        AJocClusterService.clearLogger();
        return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
    }

    @Override
    public JocServiceAnswer getInfo() {
        return new JocServiceAnswer(JocServiceAnswerState.RELAX);
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
