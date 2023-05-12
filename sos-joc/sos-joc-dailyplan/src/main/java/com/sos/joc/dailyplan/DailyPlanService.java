package com.sos.joc.dailyplan;

import java.time.Instant;
import java.util.List;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration.Action;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.service.JocClusterServiceLogger;
import com.sos.joc.cluster.service.active.AJocActiveMemberService;
import com.sos.joc.dailyplan.common.DailyPlanHelper;
import com.sos.joc.dailyplan.common.DailyPlanSettings;
import com.sos.joc.dailyplan.common.GlobalSettingsReader;
import com.sos.joc.model.cluster.common.ClusterServices;

public class DailyPlanService extends AJocActiveMemberService {

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
    public JocClusterAnswer start(StartupMode mode, List<ControllerConfiguration> controllers, AConfigurationSection globalSettings) {
        try {
            lastActivityStart = Instant.now();

            JocClusterServiceLogger.setLogger(IDENTIFIER);
            LOGGER.info(String.format("[%s][%s] start", getIdentifier(), mode));

            DailyPlanSettings settings = getSettings(mode, controllers, globalSettings);

            String startTime = DailyPlanHelper.getStartTimeAsString(settings.getTimeZone(), settings.getDailyPlanStartTime(), settings
                    .getPeriodBegin());

            if (settings.getDayAheadPlan() > 0) {
                if (!StartupMode.manual_restart.equals(mode)) {
                    LOGGER.info(String.format("[%s][planned][%s %s]creating daily plan for %s days ahead, submitting for %s days ahead", mode,
                            startTime, settings.getTimeZone(), settings.getDayAheadPlan(), settings.getDayAheadSubmit()));
                }
                schedule(settings);
            } else {
                LOGGER.info(String.format("[%s][planned][%s %s][skip]because creating daily plan for %s days ahead", mode, startTime, settings
                        .getTimeZone(), settings.getDayAheadPlan()));
            }

            lastActivityEnd = Instant.now();
            return JocCluster.getOKAnswer(JocClusterAnswerState.STARTED);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            return JocCluster.getErrorAnswer(e);
        } finally {
            JocClusterServiceLogger.removeLogger(IDENTIFIER);
        }
    }

    @Override
    public JocClusterAnswer stop(StartupMode mode) {
        JocClusterServiceLogger.setLogger(IDENTIFIER);
        LOGGER.info(String.format("[%s][%s] stop", getIdentifier(), mode));
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        JocClusterServiceLogger.removeLogger(IDENTIFIER);
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
    public void update(StartupMode mode, List<ControllerConfiguration> controllers, String controllerId, Action action) {

    }

    @Override
    public void update(StartupMode mode, AConfigurationSection configuration) {

    }

    private void schedule(DailyPlanSettings settings) {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        timer = new Timer();
        runner = new DailyPlanRunner(settings);
        timer.schedule(runner, 0, 60 * 1000);
    }

    private DailyPlanSettings getSettings(StartupMode mode, List<ControllerConfiguration> controllers, AConfigurationSection globalSettings)
            throws Exception {
        DailyPlanSettings dailyPlanGlobalSettings = new GlobalSettingsReader().getSettings(globalSettings);

        DailyPlanSettings settings = new DailyPlanSettings();
        settings.setStartMode(mode);
        settings.setControllers(controllers);
        settings.setTimeZone(dailyPlanGlobalSettings.getTimeZone());
        settings.setPeriodBegin(dailyPlanGlobalSettings.getPeriodBegin());
        settings.setDailyPlanStartTime(dailyPlanGlobalSettings.getDailyPlanStartTime());
        settings.setDayAheadPlan(dailyPlanGlobalSettings.getDayAheadPlan());
        settings.setDayAheadSubmit(dailyPlanGlobalSettings.getDayAheadSubmit());

        return settings;
    }

}
