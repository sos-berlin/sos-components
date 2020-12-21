package com.sos.js7.order.initiator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterService;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.JocClusterServices;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.js7.event.controller.configuration.controller.ControllerConfiguration;

public class OrderInitiatorMain extends JocClusterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderInitiatorMain.class);

    private static final String IDENTIFIER = JocClusterServices.dailyplan.name();
    private static final String PROPERTIES_FILE = "dailyplan.properties";

    private OrderInitiatorSettings settings;
    private Timer timer;

    public OrderInitiatorMain(JocConfiguration jocConfiguration, ThreadGroup parentThreadGroup) {
        super(jocConfiguration, parentThreadGroup, IDENTIFIER);
    }

    @Override
    public JocClusterAnswer start(List<ControllerConfiguration> controllers) {
        try {
            LOGGER.info(String.format("[%s]start", getIdentifier()));

            setSettings();
            if (settings.getDayAhead() > 0) {
                resetStartPlannedOrderTimer(controllers);
            }

            return JocCluster.getOKAnswer(JocClusterAnswerState.STARTED);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            return JocCluster.getErrorAnswer(e);
        }
    }

    @Override
    public JocClusterAnswer stop() {
        LOGGER.info(String.format("[%s]stop", getIdentifier()));
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }

        return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
    }

    private void resetStartPlannedOrderTimer(List<ControllerConfiguration> controllers) {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        timer = new Timer();
        timer.schedule(new OrderInitiatorRunner(controllers, settings, true), 0, 60 * 1000);
    }

    private void setSettings() throws Exception {
        settings = new OrderInitiatorSettings();

        Path file = getJocConfig().getResourceDirectory().resolve(PROPERTIES_FILE).normalize();
        if (Files.exists(file)) {
            Properties conf = JocConfiguration.readConfiguration(file);
            LOGGER.info(conf.toString());

            settings.setDayAhead(conf.getProperty("day_ahead"));
            settings.setTimeZone(conf.getProperty("time_zone"));
            settings.setPeriodBegin(conf.getProperty("period_begin"));
            settings.setOrderTemplatesDirectory(conf.getProperty("order_templates_directory"));
            settings.setHibernateConfigurationFile(getJocConfig().getHibernateConfiguration());
        } else {
            LOGGER.info(String.format("[%s]not found. use defaults", file));
        }
    }

}
