package com.sos.joc.monitoring;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.common.JocClusterServiceActivity;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.service.embedded.AJocEmbeddedService;
import com.sos.joc.model.cluster.common.state.JocClusterState;
import com.sos.joc.monitoring.model.SystemMonitoringModel;

public class SystemMonitorService extends AJocEmbeddedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemMonitorService.class);

    private SystemMonitoringModel model;
    private AtomicBoolean closed = new AtomicBoolean(false);

    public SystemMonitorService(JocConfiguration jocConf, ThreadGroup clusterThreadGroup) {
        super(jocConf, clusterThreadGroup, MonitorService.SUB_SERVICE_IDENTIFIER_SYSTEM);
    }

    @Override
    public JocClusterAnswer start(StartupMode mode) {
        try {
            closed.set(false);
            MonitorService.setLogger();
            LOGGER.info(String.format("[%s][%s]start...", MonitorService.SUB_SERVICE_IDENTIFIER_SYSTEM, mode));

            model = new SystemMonitoringModel(this);
            model.start(mode);
            return JocCluster.getOKAnswer(JocClusterState.STARTED);
        } catch (Exception e) {
            return JocCluster.getErrorAnswer(e);
        }
    }

    @Override
    public JocClusterAnswer stop(StartupMode mode) {
        MonitorService.setLogger();
        LOGGER.info(String.format("[%s][%s]stop...", MonitorService.SUB_SERVICE_IDENTIFIER_SYSTEM, mode));

        closed.set(true);
        if (model != null) {
            model.close(mode);
        }
        LOGGER.info(String.format("[%s][%s]stopped", MonitorService.SUB_SERVICE_IDENTIFIER_SYSTEM, mode));
        return JocCluster.getOKAnswer(JocClusterState.STOPPED);
    }

    @Override
    public JocClusterServiceActivity getActivity() {
        return JocClusterServiceActivity.Relax();
    }

    @Override
    public void update(StartupMode mode, AConfigurationSection configuration) {
        if (!closed.get()) {

        }
    }

    public boolean closed() {
        return closed.get();
    }

}
