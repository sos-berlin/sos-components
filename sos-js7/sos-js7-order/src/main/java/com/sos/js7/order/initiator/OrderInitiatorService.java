package com.sos.js7.order.initiator;

import java.time.Instant;
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
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.joc.model.configuration.globals.GlobalSettingsSection;
import com.sos.js7.order.initiator.classes.DailyPlanHelper;
import com.sos.js7.order.initiator.classes.GlobalSettingsReader;
import com.sos.js7.order.initiator.classes.OrderInitiatorGlobals;

public class OrderInitiatorService extends AJocClusterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderInitiatorService.class);

    private static final String IDENTIFIER = ClusterServices.dailyplan.name();

    private OrderInitiatorSettings settings;
    private Timer timer;
    private Instant lastActivityStart = null;
    private Instant lastActivityEnd = null;
    private OrderInitiatorRunner orderInitiatorRunner;

    public OrderInitiatorService(JocConfiguration jocConfiguration, ThreadGroup parentThreadGroup) {
        super(jocConfiguration, parentThreadGroup, IDENTIFIER);
        AJocClusterService.setLogger(IDENTIFIER);
        LOGGER.info("Ressource: " + jocConfiguration.getResourceDirectory());
        AJocClusterService.clearLogger();
    }

    @Override
    public JocClusterAnswer start(List<ControllerConfiguration> controllers, AConfigurationSection globalSettings, StartupMode mode) {
        try {
            lastActivityStart = Instant.now();

            AJocClusterService.setLogger(IDENTIFIER);
            LOGGER.info(String.format("[%s][%s] start", getIdentifier(), mode));

            setSettings(mode, globalSettings);

            OrderInitiatorGlobals.orderInitiatorSettings = settings;

            LOGGER.info("will start at " + DailyPlanHelper.getStartTimeAsString() + " " + settings.getTimeZone() + " creating daily plan for "
                    + settings.getDayAheadPlan() + " days ahead");
            LOGGER.info("will start at " + DailyPlanHelper.getStartTimeAsString() + " " + settings.getTimeZone() + " submitting daily plan for "
                    + settings.getDayAheadSubmit() + " days ahead");

            if (settings.getDayAheadPlan() > 0) {
                resetStartPlannedOrderTimer(controllers);
            }

            lastActivityEnd = Instant.now();
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
        if (orderInitiatorRunner != null) {
            Instant rla = Instant.ofEpochMilli(orderInitiatorRunner.getLastActivityStart().get());
            if (rla.isAfter(this.lastActivityStart)) {
                this.lastActivityStart = rla;
            }
            rla = Instant.ofEpochMilli(orderInitiatorRunner.getLastActivityEnd().get());
            if (rla.isAfter(this.lastActivityEnd)) {
                this.lastActivityEnd = rla;
            }
        }
        return new JocServiceAnswer(lastActivityStart, lastActivityEnd);
    }

    private void resetStartPlannedOrderTimer(List<ControllerConfiguration> controllers) {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        timer = new Timer();
        orderInitiatorRunner = new OrderInitiatorRunner(controllers, settings, true);
        timer.schedule(orderInitiatorRunner, 0, 60 * 1000);
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

    private void setSettings(StartupMode mode, AConfigurationSection globalSettings) throws Exception {
        settings = new OrderInitiatorSettings();
        settings.setHibernateConfigurationFile(getJocConfig().getHibernateConfiguration());
        settings.setStartMode(mode);

        GlobalSettingsReader reader = new GlobalSettingsReader();
        OrderInitiatorSettings readerSettings = reader.getSettings(globalSettings);

        settings.setTimeZone(readerSettings.getTimeZone());
        settings.setPeriodBegin(readerSettings.getPeriodBegin());
        settings.setDailyPlanStartTime(readerSettings.getDailyPlanStartTime());
        settings.setDayAheadPlan(readerSettings.getDayAheadPlan());
        settings.setDayAheadSubmit(readerSettings.getDayAheadSubmit());
    }

    @SuppressWarnings("unused")
    private void setSettingsOld(GlobalSettingsSection globalSettings) throws Exception {
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
        settings.setDailyPlanStartTime(getProperty(Globals.sosCockpitProperties, "daily_plan_start_time", "00:00"));

        settings.setHibernateConfigurationFile(getJocConfig().getHibernateConfiguration());

        settings.setHibernateConfigurationFile(getJocConfig().getHibernateConfiguration());

    }

}
