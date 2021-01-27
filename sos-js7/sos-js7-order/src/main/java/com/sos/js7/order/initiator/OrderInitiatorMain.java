package com.sos.js7.order.initiator;

import java.util.List;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
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

    private OrderInitiatorSettings settings;
    private Timer timer;

    public OrderInitiatorMain(JocConfiguration jocConfiguration, ThreadGroup parentThreadGroup) {
        super(jocConfiguration, parentThreadGroup, IDENTIFIER);
        LOGGER.info("Ressource: " + jocConfiguration.getResourceDirectory());

    }

    @Override
    public JocClusterAnswer start(List<ControllerConfiguration> controllers) {
        try {
            LOGGER.info(String.format("[%s] start", getIdentifier()));

            setSettings();
            LOGGER.info("Creating plan for " + settings.getDayAhead() + " days ahead");
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

    private String getProperty(JocCockpitProperties sosCockpitProperties, String prop, String defaults) {
        String val = defaults;
        if (sosCockpitProperties != null) {
            val = sosCockpitProperties.getProperty(prop);
            if (val == null) {
                val = defaults;
            }

        }
        LOGGER.debug("Setting " + prop + "=" + val);
        return val;
    } 
      
    private void setSettings() throws Exception {
        settings = new OrderInitiatorSettings();
        if (Globals.sosCockpitProperties == null) {
            Globals.sosCockpitProperties = new JocCockpitProperties(getJocConfig().getResourceDirectory().resolve("joc.properties"));
        }
 
        LOGGER.debug("...Settings from " + getJocConfig().getResourceDirectory().resolve("joc.properties").normalize());
         
        settings.setDayAhead(getProperty(Globals.sosCockpitProperties, "daily_plan_day_ahead", "0"));
        settings.setTimeZone(getProperty(Globals.sosCockpitProperties, "daily_plan_timezone", "UTC"));
        settings.setPeriodBegin(getProperty(Globals.sosCockpitProperties, "daily_plan_period_begin", "00:00"));
         

        settings.setHibernateConfigurationFile(getJocConfig().getHibernateConfiguration());

        
        settings.setHibernateConfigurationFile(getJocConfig().getHibernateConfiguration());

    }

}
