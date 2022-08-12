package com.sos.joc.monitoring;

import java.nio.file.Path;
import java.sql.Connection;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.cluster.AJocClusterService;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer.JocServiceAnswerState;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration.Action;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.joc.monitoring.model.HistoryMonitoringModel;

public class MonitorService extends AJocClusterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorService.class);

    private static final String IDENTIFIER = ClusterServices.monitor.name();

    private JocClusterHibernateFactory factory;
    private HistoryMonitoringModel history = null;
    private AtomicBoolean closed = new AtomicBoolean(false);

    public MonitorService(JocConfiguration jocConf, ThreadGroup clusterThreadGroup) {
        super(jocConf, clusterThreadGroup, IDENTIFIER);
        AJocClusterService.setLogger(IDENTIFIER);
    }

    @Override
    public JocClusterAnswer start(List<ControllerConfiguration> controllers, AConfigurationSection configuration, StartupMode mode) {
        try {
            closed.set(false);
            AJocClusterService.setLogger(IDENTIFIER);
            LOGGER.info(String.format("[%s][%s]start...", getIdentifier(), mode));

            createFactory(getJocConfig().getHibernateConfiguration());
            history = new HistoryMonitoringModel(getThreadGroup(), factory, getJocConfig(), IDENTIFIER);
            history.start(getThreadGroup());

            return JocCluster.getOKAnswer(JocClusterAnswerState.STARTED);
        } catch (Exception e) {
            return JocCluster.getErrorAnswer(e);
        } finally {
            AJocClusterService.removeLogger(IDENTIFIER);
        }
    }

    @Override
    public JocClusterAnswer stop(StartupMode mode) {
        AJocClusterService.setLogger(IDENTIFIER);
        LOGGER.info(String.format("[%s][%s]stop...", getIdentifier(), mode));

        closed.set(true);
        close(mode);
        LOGGER.info(String.format("[%s][%s]stopped", getIdentifier(), mode));

        AJocClusterService.removeLogger(IDENTIFIER);
        return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
    }

    @Override
    public JocServiceAnswer getInfo() {
        if (history == null) {
            return new JocServiceAnswer(JocServiceAnswerState.RELAX);
        } else {
            return new JocServiceAnswer(Instant.ofEpochMilli(history.getLastActivityStart().get()), Instant.ofEpochMilli(history.getLastActivityEnd()
                    .get()));
        }
    }

    @Override
    public void update(List<ControllerConfiguration> controllers, String controllerId, Action action) {

    }

    @Override
    public void update(StartupMode mode, AConfigurationSection configuration) {

    }

    private void close(StartupMode mode) {
        if (history != null) {
            history.close(mode);
        }
        closeFactory();
    }

    private void createFactory(Path configFile) throws Exception {
        // 1-history monitoring, 2 - configuration thread
        factory = new JocClusterHibernateFactory(configFile, 1, 2);
        factory.setIdentifier(IDENTIFIER);
        factory.setAutoCommit(false);
        factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        factory.addClassMapping(DBLayer.getMonitoringClassMapping());
        factory.build();
    }

    private void closeFactory() {
        if (factory != null) {
            AJocClusterService.setLogger(IDENTIFIER);

            factory.close();
            factory = null;
            LOGGER.info(String.format("[%s]database factory closed", IDENTIFIER));
        }
    }

}
