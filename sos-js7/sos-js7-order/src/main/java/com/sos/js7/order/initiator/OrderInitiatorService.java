package com.sos.js7.order.initiator;

import java.util.Date;
import java.util.List;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.cluster.AJocClusterService;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.js7.event.controller.configuration.controller.ControllerConfiguration;

public class OrderInitiatorService extends AJocClusterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderInitiatorService.class);

    private static final String IDENTIFIER = ClusterServices.dailyplan.name();

    private OrderInitiatorSettings settings;
    private Timer timer;
    private long lastActivityStart;
    private long lastActivityEnd;

    public OrderInitiatorService(JocConfiguration jocConfiguration, ThreadGroup parentThreadGroup) {
        super(jocConfiguration, parentThreadGroup, IDENTIFIER);
        AJocClusterService.setLogger(IDENTIFIER);
        LOGGER.info("Ressource: " + jocConfiguration.getResourceDirectory());
        AJocClusterService.clearLogger();
    }

    @Override
    public JocClusterAnswer start(List<ControllerConfiguration> controllers, StartupMode mode) {
        try {
            lastActivityStart = new Date().getTime();

            AJocClusterService.setLogger(IDENTIFIER);
            LOGGER.info(String.format("[%s][%s] start", getIdentifier(), mode));

            setSettings();
            settings.setStartMode(mode);

            LOGGER.info("[daily_plan_days_ahead_plan] onPeriodChange will create daily plan for " + settings.getDayAheadPlan() + " days ahead");
            LOGGER.info("[daily_plan_days_ahead_submit] onPeriodChange will submit daily plan for " + settings.getDayAheadSubmit() + " days ahead");
            if (settings.getDayAheadPlan() > 0) {
                resetStartPlannedOrderTimer(controllers);
            }
            lastActivityEnd = new Date().getTime();

            return JocCluster.getOKAnswer(JocClusterAnswerState.STARTED);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            return JocCluster.getErrorAnswer(e);
        } finally {
            AJocClusterService.clearLogger();
        }
    }

    @Override
    public JocClusterAnswer stop(StartupMode mode) {
        AJocClusterService.setLogger(IDENTIFIER);
        LOGGER.info(String.format("[%s][%s]stop", getIdentifier(), mode));
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        AJocClusterService.clearLogger();
        return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
    }

    @Override
    public JocServiceAnswer getInfo() {
        // TODO
        return new JocServiceAnswer(lastActivityStart, lastActivityEnd);
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

        settings.setDayAheadPlan(getProperty(Globals.sosCockpitProperties, "daily_plan_days_ahead_plan", "0"));
        settings.setDayAheadSubmit(getProperty(Globals.sosCockpitProperties, "daily_plan_days_ahead_submit", "0"));
        settings.setTimeZone(getProperty(Globals.sosCockpitProperties, "daily_plan_time_zone", "UTC"));
        settings.setPeriodBegin(getProperty(Globals.sosCockpitProperties, "daily_plan_period_begin", "00:00"));
        settings.setDailyPlanDaysCreateOnStart("1".equals(getProperty(Globals.sosCockpitProperties, "daily_plan_days_create_on_start", "0")));

        settings.setHibernateConfigurationFile(getJocConfig().getHibernateConfiguration());

        settings.setHibernateConfigurationFile(getJocConfig().getHibernateConfiguration());

    }

}
