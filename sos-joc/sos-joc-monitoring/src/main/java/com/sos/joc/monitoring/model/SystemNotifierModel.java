package com.sos.joc.monitoring.model;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.monitoring.MonitoringGuiEvent;
import com.sos.joc.monitoring.MonitorService;
import com.sos.joc.monitoring.bean.SystemMonitoringEvent;
import com.sos.joc.monitoring.configuration.SystemNotification;
import com.sos.joc.monitoring.configuration.monitor.AMonitor;
import com.sos.joc.monitoring.notification.notifier.ANotifier;
import com.sos.joc.monitoring.notification.notifier.NotifyResult;

public class SystemNotifierModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemNotifierModel.class);

    private static final String IDENTIFIER = String.format("%s_%s", MonitorService.SUB_SERVICE_IDENTIFIER_SYSTEM,
            MonitorService.NOTIFICATION_IDENTIFIER);

    private AtomicBoolean closed = new AtomicBoolean();

    private ExecutorService threadPool;

    protected SystemNotifierModel(ThreadGroup threadGroup) {
        MonitorService.setLogger();
        threadPool = Executors.newFixedThreadPool(1, new JocClusterThreadFactory(threadGroup, IDENTIFIER));
    }

    protected void notify(SystemNotification notification, List<SystemMonitoringEvent> toNotify) {
        if (notification == null || toNotify.size() == 0 || notification.getMonitors().size() == 0) {
            return;
        }

        Runnable task = new Runnable() {

            @Override
            public void run() {
                MonitorService.setLogger();
                if (!closed.get()) {
                    notifyEvents(notification, toNotify);
                }
            }
        };
        if (!closed.get()) {
            threadPool.submit(task);
        }
    }

    private void notifyEvents(SystemNotification notification, List<SystemMonitoringEvent> toNotify) {
        for (SystemMonitoringEvent event : toNotify) {
            notifyEvent(notification, event);
        }
    }

    private void notifyEvent(SystemNotification notification, SystemMonitoringEvent event) {
        int i = 1;
        for (AMonitor m : notification.getMonitors()) {
            ANotifier n = null;
            try {
                n = m.createNotifier(i);
            } catch (Throwable e) {
                LOGGER.error(e.toString(), e);// contains all informations about the type etc
                n = null;
            }
            if (n != null) {
                try {
                    NotifyResult nr = n.notify(event.getType(), m.getTimeZone(), event);
                    if (nr != null && nr.getError() != null) {
                        LOGGER.error(nr.getError().getMessage(), nr.getError().getException());
                    }
                    // LOGGER.info(String.format("%s[%s][notification id=%s][%s]%s[skip save notification result]due to save notification failed",
                    // Configuration.LOG_INTENT, i, notification.getNotificationId(), range, ANotifier.getInfo(analyzer, m, type)));
                } catch (Throwable e) {
                    LOGGER.error(e.toString(), e);
                } finally {
                    n.close();
                }
            }
            i++;
        }
        postEvent(event);
    }

    private void postEvent(SystemMonitoringEvent event) {
        if (event == null) {
            return;
        }
        EventBus.getInstance().post(new MonitoringGuiEvent(event.getType().intValue(), event.getCategory().name(), event.getSection(), event
                .getCaller(), event.getEpochMillis(), event.getMessage()));
    }

    protected JocClusterAnswer close(StartupMode mode) {
        MonitorService.setLogger();
        closed.set(true);

        if (threadPool != null) {
            JocCluster.shutdownThreadPool(mode, threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);
        }
        return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
    }

}
