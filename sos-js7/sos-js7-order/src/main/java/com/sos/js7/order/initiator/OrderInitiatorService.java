package com.sos.js7.order.initiator;

import java.time.Instant;
import java.util.List;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.cluster.AJocClusterService;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocClusterGlobalSettings;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.joc.model.configuration.globals.GlobalSettingsSection;
import com.sos.joc.model.configuration.globals.GlobalSettingsSectionEntry;
import com.sos.js7.order.initiator.classes.DailyPlanHelper;
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
    public JocClusterAnswer start(List<ControllerConfiguration> controllers, GlobalSettingsSection globalSettings, StartupMode mode) {
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

    private void setDefaults() {
        GlobalSettingsSection defaultSettings = JocClusterGlobalSettings.getDefaultSettings(ClusterServices.dailyplan);
        GlobalSettingsSectionEntry timezone = JocClusterGlobalSettings.getSectionEntry(defaultSettings, "time_zone");
        settings.setTimeZone(timezone.getDefault());

        GlobalSettingsSectionEntry periodBegin = JocClusterGlobalSettings.getSectionEntry(defaultSettings, "period_begin");
        settings.setPeriodBegin(periodBegin.getDefault());

        GlobalSettingsSectionEntry startTime = JocClusterGlobalSettings.getSectionEntry(defaultSettings, "start_time");
        settings.setDailyPlanStartTime(startTime.getDefault());

        GlobalSettingsSectionEntry daysAheadPlan = JocClusterGlobalSettings.getSectionEntry(defaultSettings, "days_ahead_plan");
        settings.setDayAheadPlan(daysAheadPlan.getDefault());

        GlobalSettingsSectionEntry daysAheadSubmit = JocClusterGlobalSettings.getSectionEntry(defaultSettings, "days_ahead_submit");
        settings.setDayAheadSubmit(daysAheadSubmit.getDefault());
    }

    private void setSettings(StartupMode mode, GlobalSettingsSection globalSettings) throws Exception {
        settings = new OrderInitiatorSettings();
        settings.setHibernateConfigurationFile(getJocConfig().getHibernateConfiguration());
        settings.setStartMode(mode);

        setDefaults();
        if (globalSettings == null) {
            return;
        }

        String timeZone = JocClusterGlobalSettings.getValue(globalSettings, "time_zone");
        if (!SOSString.isEmpty(timeZone)) {
            this.settings.setTimeZone(timeZone);
        }

        String periodBegin = JocClusterGlobalSettings.getValue(globalSettings, "period_begin");
        if (!SOSString.isEmpty(periodBegin)) {
            this.settings.setPeriodBegin(periodBegin);
        }

        String startTime = JocClusterGlobalSettings.getValue(globalSettings, "start_time");
        if (!SOSString.isEmpty(startTime)) {
            this.settings.setDailyPlanStartTime(startTime);
        }

        String daysAheadPlan = JocClusterGlobalSettings.getValue(globalSettings, "days_ahead_plan");
        if (!SOSString.isEmpty(daysAheadPlan)) {
            this.settings.setDayAheadPlan(daysAheadPlan);
        }

        String daysAheadSubmit = JocClusterGlobalSettings.getValue(globalSettings, "days_ahead_submit");
        if (!SOSString.isEmpty(daysAheadSubmit)) {
            this.settings.setDayAheadSubmit(daysAheadSubmit);
        }
        LOGGER.info(SOSString.toString(settings));
    }

    @SuppressWarnings("unused")
    private void setSettingsOld(GlobalSettingsSection globalSettings) throws Exception {
        settings = new OrderInitiatorSettings();
        if (Globals.sosCockpitProperties == null) {
            Globals.sosCockpitProperties = new JocCockpitProperties(getJocConfig().getResourceDirectory().resolve("joc.properties"));
        }

        // if(globalSettings!=null){
        // each setting can be null (not stored in the database) - defaults should be used
        // JocClusterConfiguration.getValue(globalSettings, "time_zone");
        // JocClusterConfiguration.getValue(globalSettings, "period_begin");
        // JocClusterConfiguration.getValue(globalSettings, "start_time");
        // JocClusterConfiguration.getValue(globalSettings, "days_ahead_plan");
        // JocClusterConfiguration.getValue(globalSettings, "days_ahead_submit");
        // }

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
