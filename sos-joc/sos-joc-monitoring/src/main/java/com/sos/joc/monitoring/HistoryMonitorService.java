package com.sos.joc.monitoring;

import java.nio.file.Path;
import java.sql.Connection;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.common.JocClusterServiceActivity;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration.Action;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.service.active.AJocActiveMemberService;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.cluster.common.state.JocClusterState;
import com.sos.joc.monitoring.model.HistoryMonitoringModel;

public class HistoryMonitorService extends AJocActiveMemberService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryMonitorService.class);

    private JocClusterHibernateFactory factory;
    private HistoryMonitoringModel history = null;
    private AtomicBoolean closed = new AtomicBoolean(false);

    public HistoryMonitorService(JocConfiguration jocConf, ThreadGroup clusterThreadGroup) {
        super(jocConf, clusterThreadGroup, MonitorService.MAIN_SERVICE_IDENTIFIER);
    }

    @Override
    public synchronized JocClusterAnswer start(StartupMode mode, List<ControllerConfiguration> controllers,
            AConfigurationSection serviceSettingsSection) {
        try {
            MonitorService.setLogger();
            stopOnStart(mode);

            closed.set(false);
            LOGGER.info(String.format("[%s][%s]start...", MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, mode));

            createFactory(getJocConfig().getHibernateConfiguration());
            history = new HistoryMonitoringModel(getThreadGroup(), factory, getJocConfig());
            history.start(getThreadGroup());

            return JocCluster.getOKAnswer(JocClusterState.STARTED);
        } catch (Throwable e) {
            return JocCluster.getErrorAnswer(e);
        } finally {
            MonitorService.removeLogger();
        }
    }

    @Override
    public synchronized JocClusterAnswer stop(StartupMode mode) {
        MonitorService.setLogger();
        LOGGER.info(String.format("[%s][%s]stop...", MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, mode));

        closed.set(true);
        close(mode);
        LOGGER.info(String.format("[%s][%s]stopped", MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, mode));

        MonitorService.removeLogger();
        return JocCluster.getOKAnswer(JocClusterState.STOPPED);
    }

    @Override
    public JocClusterServiceActivity getActivity() {
        if (history == null) {
            return JocClusterServiceActivity.Relax();
        } else {
            return new JocClusterServiceActivity(Instant.ofEpochMilli(history.getLastActivityStart().get()), Instant.ofEpochMilli(history
                    .getLastActivityEnd().get()));
        }
    }

    @Override
    public void startPause(String caller, int pauseDurationInSeconds) {
        if (history != null) {
            history.startPause(caller, pauseDurationInSeconds);
        }
    }

    @Override
    public void stopPause(String caller) {
        if (history != null) {
            history.stopPause(caller);
        }
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

    private void close(StartupMode mode) {
        if (history != null) {
            history.close(mode);
            history = null;
        }
        closeFactory();
    }

    private void stopOnStart(StartupMode mode) {
        close(mode);
    }

    private void createFactory(Path configFile) throws Exception {
        // 1-history monitoring, 2 - configuration thread
        factory = new JocClusterHibernateFactory(configFile, 1, 2);
        factory.setIdentifier(MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY);
        factory.setAutoCommit(false);
        factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        factory.addClassMapping(DBLayer.getMonitoringClassMapping());
        factory.build();
    }

    private void closeFactory() {
        if (factory != null) {
            MonitorService.setLogger();

            factory.close();
            factory = null;
            LOGGER.info(String.format("[%s]database factory closed", MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY));
        }
    }

}
