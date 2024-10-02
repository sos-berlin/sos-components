package com.sos.joc.dailyplan;

import java.time.Instant;
import java.util.List;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.common.JocClusterServiceActivity;
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
import com.sos.joc.model.cluster.common.state.JocClusterState;

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
    public synchronized JocClusterAnswer start(StartupMode mode, List<ControllerConfiguration> controllers,
            AConfigurationSection serviceSettingsSection) {
        try {
            lastActivityStart = Instant.now();

            JocClusterServiceLogger.setLogger(IDENTIFIER);
            LOGGER.info(String.format("[%s][%s] start", getIdentifier(), mode));

            DailyPlanSettings settings = getSettings(mode, controllers, serviceSettingsSection);

            String startTime = DailyPlanHelper.getStartTimeAsString(settings.getTimeZone(), settings.getDailyPlanStartTime(), settings
                    .getPeriodBegin());

            if (settings.getDayAheadPlan() > 0) {
                LOGGER.info(String.format("[%s][%s][planned][%s %s]creating daily plan for %s days ahead, submitting for %s days ahead",
                        getIdentifier(), mode, startTime, settings.getTimeZone(), settings.getDayAheadPlan(), settings.getDayAheadSubmit()));
                schedule(settings);
            } else {
                LOGGER.info(String.format("[%s][%s][planned][%s %s][skip]because creating daily plan for %s days ahead", getIdentifier(), mode,
                        startTime, settings.getTimeZone(), settings.getDayAheadPlan()));
            }

            lastActivityEnd = Instant.now();
            return JocCluster.getOKAnswer(JocClusterState.STARTED);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
            return JocCluster.getErrorAnswer(e);
        } finally {
            JocClusterServiceLogger.removeLogger(IDENTIFIER);
        }
    }

    @Override
    public synchronized JocClusterAnswer stop(StartupMode mode) {
        JocClusterServiceLogger.setLogger(IDENTIFIER);
        LOGGER.info(String.format("[%s][%s] stop", getIdentifier(), mode));
        resetTimer();
        JocClusterServiceLogger.removeLogger(IDENTIFIER);
        return JocCluster.getOKAnswer(JocClusterState.STOPPED);
    }

    @Override
    public synchronized JocClusterAnswer runNow(StartupMode mode, List<ControllerConfiguration> controllers,
            AConfigurationSection serviceSettingsSection) {
        JocClusterServiceLogger.setLogger(IDENTIFIER);

        if (getActivity().isBusy()) {
            LOGGER.info(String.format("[%s][%s][runNow][skip]isBusy=true", getIdentifier(), mode));
            return new JocClusterAnswer(JocClusterState.ALREADY_RUNNING);
        }
        lastActivityStart = Instant.now();

        try {
            LOGGER.info(String.format("[%s][%s][runNow]...", getIdentifier(), mode));
            DailyPlanSettings settings = getSettings(mode, controllers, serviceSettingsSection);
            settings.setRunNow(true);
            schedule(settings);
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][%s][runNow]%s", getIdentifier(), mode, e.toString()), e);
        }
        return new JocClusterAnswer(JocClusterState.RUNNING);
    }

    @Override
    public JocClusterServiceActivity getActivity() {
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
        return new JocClusterServiceActivity(lastActivityStart, lastActivityEnd);
    }

    @Override
    public void startPause(String caller, int pauseDurationInSeconds) {
    }

    @Override
    public void stopPause(String caller) {
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

    private void schedule(DailyPlanSettings settings) {
        resetTimer();
        timer = new Timer();
        runner = new DailyPlanRunner(settings);
        timer.schedule(runner, 0, 60 * 1000);
    }

    private void resetTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    private DailyPlanSettings getSettings(StartupMode mode, List<ControllerConfiguration> controllers, AConfigurationSection serviceSettingsSection)
            throws Exception {
        DailyPlanSettings dailyPlanGlobalSettings = new GlobalSettingsReader().getSettings(serviceSettingsSection);

        DailyPlanSettings settings = new DailyPlanSettings();
        settings.setStartMode(mode);
        settings.setControllers(controllers);
        settings.setTimeZone(dailyPlanGlobalSettings.getTimeZone());
        settings.setPeriodBegin(dailyPlanGlobalSettings.getPeriodBegin());
        settings.setDailyPlanStartTime(dailyPlanGlobalSettings.getDailyPlanStartTime());
        settings.setDayAheadPlan(dailyPlanGlobalSettings.getDayAheadPlan());
        settings.setDayAheadSubmit(dailyPlanGlobalSettings.getDayAheadSubmit());
        settings.setProjectionsMonthsAhead(dailyPlanGlobalSettings.getProjectionsMonthsAhead());

        return settings;
    }

}
