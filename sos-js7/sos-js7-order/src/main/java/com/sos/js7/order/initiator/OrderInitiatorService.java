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
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration.Action;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.joc.model.configuration.globals.GlobalSettingsSection;
import com.sos.js7.order.initiator.classes.DailyPlanHelper;
import com.sos.js7.order.initiator.classes.GlobalSettingsReader;
 
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
            
            LOGGER.info("will start creating daily plan at " + DailyPlanHelper.getStartTimeAsString(settings.getTimeZone(),settings.getDailyPlanStartTime(),settings.getPeriodBegin()) + " " + settings.getTimeZone() + " for "
                    + settings.getDayAheadPlan() + " days ahead");
            LOGGER.info("will start submitting daily plan at " + DailyPlanHelper.getStartTimeAsString(settings.getTimeZone(),settings.getDailyPlanStartTime(),settings.getPeriodBegin()) + " " + settings.getTimeZone() + " for "
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
    
    @Override
    public void update(List<ControllerConfiguration> controllers, String controllerId, Action action) {
        
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

   
}
