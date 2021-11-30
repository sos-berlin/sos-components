package com.sos.js7.order.initiator;

import java.time.Instant;
import java.util.List;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.cluster.AJocClusterService;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration.Action;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.js7.order.initiator.common.DailyPlanHelper;
import com.sos.js7.order.initiator.common.GlobalSettingsReader;

public class DailyPlanService extends AJocClusterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanService.class);

    private static final String IDENTIFIER = ClusterServices.dailyplan.name();

    private DailyPlanRunner runner;
    private Timer timer;
    private Instant lastActivityStart = null;
    private Instant lastActivityEnd = null;

    public DailyPlanService(JocConfiguration jocConfiguration, ThreadGroup parentThreadGroup) {
        super(jocConfiguration, parentThreadGroup, IDENTIFIER);
    }

    @Override
    public JocClusterAnswer start(List<ControllerConfiguration> controllers, AConfigurationSection globalSettings, StartupMode mode) {
        try {
            lastActivityStart = Instant.now();

            AJocClusterService.setLogger(IDENTIFIER);
            LOGGER.info(String.format("[%s][%s] start", getIdentifier(), mode));

            DailyPlanSettings settings = getSettings(mode, globalSettings);

            String startTime = DailyPlanHelper.getStartTimeAsString(settings.getTimeZone(), settings.getDailyPlanStartTime(), settings
                    .getPeriodBegin());

            if (settings.getDayAheadPlan() > 0) {
                if (!StartupMode.manual_restart.equals(mode)) {
                    LOGGER.info(String.format("[planned][%s %s]creating daily plan for %s days ahead, submitting for %s days ahead", startTime,
                            settings.getTimeZone(), settings.getDayAheadPlan(), settings.getDayAheadSubmit()));
                }
                resetStartPlannedOrderTimer(controllers, settings);
            } else {
                LOGGER.info(String.format("[planned][%s %s][skip]because creating daily plan for %s days ahead", startTime, settings.getTimeZone(),
                        settings.getDayAheadPlan()));
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
        LOGGER.info(String.format("[%s][%s] stop", getIdentifier(), mode));
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        AJocClusterService.clearLogger();
        return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
    }

    @Override
    public JocServiceAnswer getInfo() {
        if (runner != null) {
            Instant rla = Instant.ofEpochMilli(runner.getLastActivityStart().get());
            if (rla.isAfter(this.lastActivityStart)) {
                this.lastActivityStart = rla;
            }
            rla = Instant.ofEpochMilli(runner.getLastActivityEnd().get());
            if (rla.isAfter(this.lastActivityEnd)) {
                this.lastActivityEnd = rla;
            }
        }
        return new JocServiceAnswer(lastActivityStart, lastActivityEnd);
    }

    @Override
    public void update(List<ControllerConfiguration> controllers, String controllerId, Action action) {

    }

    private void resetStartPlannedOrderTimer(List<ControllerConfiguration> controllers, DailyPlanSettings settings) {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        timer = new Timer();
        runner = new DailyPlanRunner(controllers, settings, true);
        timer.schedule(runner, 0, 60 * 1000);
    }

    private DailyPlanSettings getSettings(StartupMode mode, AConfigurationSection globalSettings) throws Exception {
        DailyPlanSettings dailyPlanGlobalSettings = new GlobalSettingsReader().getSettings(globalSettings);

        // TODO why create new DailyPlanSettings?
        DailyPlanSettings settings = new DailyPlanSettings();
        settings.setTimeZone(dailyPlanGlobalSettings.getTimeZone());
        settings.setPeriodBegin(dailyPlanGlobalSettings.getPeriodBegin());
        settings.setDailyPlanStartTime(dailyPlanGlobalSettings.getDailyPlanStartTime());
        settings.setDayAheadPlan(dailyPlanGlobalSettings.getDayAheadPlan());
        settings.setDayAheadSubmit(dailyPlanGlobalSettings.getDayAheadSubmit());

        settings.setHibernateConfigurationFile(getJocConfig().getHibernateConfiguration());
        settings.setStartMode(mode);

        return settings;
    }

}
