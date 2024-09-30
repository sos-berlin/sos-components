package com.sos.joc.monitoring;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.common.JocClusterServiceActivity;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.service.embedded.AJocEmbeddedService;
import com.sos.joc.model.cluster.common.state.JocClusterState;
import com.sos.joc.monitoring.configuration.Configuration;
import com.sos.joc.monitoring.model.SystemMonitoringModel;

public class SystemMonitorService extends AJocEmbeddedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemMonitorService.class);

    private SystemMonitoringModel model;
    private AtomicBoolean closed = new AtomicBoolean(false);

    public SystemMonitorService(JocConfiguration jocConfiguration, ThreadGroup clusterThreadGroup) {
        super(jocConfiguration, clusterThreadGroup, MonitorService.SUB_SERVICE_IDENTIFIER_SYSTEM);
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
    public void update(StartupMode mode, AConfigurationSection settingsSection) {
        if (!closed.get()) {
            if (settingsSection != null && settingsSection instanceof ConfigurationGlobalsJoc) {
                MonitorService.setLogger();

                ConfigurationGlobalsJoc joc = (ConfigurationGlobalsJoc) settingsSection;
                String oldValue = Configuration.INSTANCE.getJocReverseProxyUri();
                String newValue = joc.getJocReverseProxyUrl().getValue();
                if (!SOSString.equals(oldValue, newValue)) {
                    Configuration.INSTANCE.setJocReverseProxyUri(newValue);
                    LOGGER.info(String.format("[%s][%s][%s][old=%s][new=%s]", MonitorService.MAIN_SERVICE_IDENTIFIER, StartupMode.settings_changed
                            .name(), joc.getJocReverseProxyUrl().getName(), oldValue, newValue));
                }
            }
        }
    }

    @Override
    public void update(StartupMode mode, JocConfiguration jocConfiguration) {
        if (!closed.get()) {
            MonitorService.setLogger();

            String oldValue = Configuration.INSTANCE.getJocUri();
            String newValue = jocConfiguration.getUri();
            if (!SOSString.equals(oldValue, newValue)) {
                Configuration.INSTANCE.setJocUri(newValue);
                LOGGER.info(String.format("[%s][%s][JOC Cockpit URL][old=%s][new=%s]", MonitorService.MAIN_SERVICE_IDENTIFIER, mode.name(), oldValue,
                        Configuration.INSTANCE.getJocUri()));
            }
        }
    }

    public boolean closed() {
        return closed.get();
    }

}
