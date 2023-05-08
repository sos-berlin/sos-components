package com.sos.joc.monitoring.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSClassUtil;
import com.sos.joc.Globals;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.db.monitoring.DBItemNotificationMonitor;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.monitoring.MonitoringGuiEvent;
import com.sos.joc.monitoring.MonitorService;
import com.sos.joc.monitoring.bean.SystemMonitoringEvent;
import com.sos.joc.monitoring.bean.SystemNotifierResult;
import com.sos.joc.monitoring.configuration.Configuration;
import com.sos.joc.monitoring.configuration.SystemNotification;
import com.sos.joc.monitoring.configuration.monitor.AMonitor;
import com.sos.joc.monitoring.db.DBLayerMonitoring;
import com.sos.joc.monitoring.notification.notifier.ANotifier;
import com.sos.joc.monitoring.notification.notifier.NotifyResult;

public class SystemNotifierModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemNotifierModel.class);

    private static final String IDENTIFIER = String.format("%s_%s", MonitorService.SUB_SERVICE_IDENTIFIER_SYSTEM,
            MonitorService.NOTIFICATION_IDENTIFIER);

    private AtomicBoolean closed = new AtomicBoolean();

    private ExecutorService threadPool;
    private ExecutorService threadPoolDB;

    protected SystemNotifierModel(ThreadGroup threadGroup) {
        MonitorService.setLogger();

        threadPool = Executors.newFixedThreadPool(1, new JocClusterThreadFactory(threadGroup, IDENTIFIER));
        // newFixedThreadPool problem: The threads in the pool will exist until it is explicitly shutdown.
        // threadPoolDB = Executors.newFixedThreadPool(10, new JocClusterThreadFactory(threadGroup, IDENTIFIER + "_db"));
    }

    protected void notify(SystemNotification notification, List<SystemMonitoringEvent> toNotify) {
        if (notification == null || toNotify.size() == 0 || closed.get()) {
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
        List<SystemNotifierResult> toStore = new ArrayList<>();
        for (SystemMonitoringEvent event : toNotify) {
            toStore.add(notifyEvent(notification, event));
        }
        // TODO timeout, max threads ....
        // CompletableFuture.runAsync(() -> storeAndPostEvents(toStore, toNotify), threadPoolDB);
        CompletableFuture.runAsync(() -> storeAndPostEvents(toStore, toNotify));
    }

    private SystemNotifierResult notifyEvent(SystemNotification notification, SystemMonitoringEvent event) {
        int i = 1;

        String jocId = Globals.getJocId();
        Date dateTime = truncateMillis(event.getEpochMillis());
        String exception = SOSClassUtil.getStackTrace(event.getThrown());
        SystemNotifierResult result = new SystemNotifierResult(DBLayerMonitoring.createSystemNotification(notification, jocId, event, dateTime,
                exception));

        for (AMonitor m : notification.getMonitors()) {
            ANotifier n = null;
            try {
                n = m.createNotifier(i);
            } catch (Throwable e) {
                LOGGER.error(e.toString(), e);// contains all informations about the type etc
                result.addMonitor(DBLayerMonitoring.createSystemNotificationMonitor(m, new NotifyResult(m.getMessage(), e)));
                n = null;
            }
            if (n != null) {
                try {
                    NotifyResult nr = n.notify(event.getType(), m.getTimeZone(), jocId, event, dateTime, exception);
                    if (nr != null && nr.getError() != null) {
                        LOGGER.error(nr.getError().getMessage(), nr.getError().getException());
                    }

                    if (nr.getSkipCause() == null) {
                        result.addMonitor(DBLayerMonitoring.createSystemNotificationMonitor(m, nr));
                    } else {
                        LOGGER.info(String.format("%s[%s][systemNotification][skip]%s%s", Configuration.LOG_INTENT, i, ANotifier.getMainInfo(m), nr
                                .getSkipCause()));
                    }
                } catch (Throwable e) {
                    LOGGER.error(e.toString(), e);
                } finally {
                    n.close();
                }
            }
            i++;
        }
        return result;
    }

    private void storeAndPostEvents(List<SystemNotifierResult> toStore, List<SystemMonitoringEvent> events) {
        MonitorService.setLogger();

        DBLayerMonitoring dbLayer = new DBLayerMonitoring(IDENTIFIER);
        try {
            dbLayer.setSession(Globals.createSosHibernateStatelessConnection(IDENTIFIER));
            dbLayer.beginTransaction();

            for (SystemNotifierResult r : toStore) {
                r.getNotification().setHasMonitors(r.getMonitors().size() > 0);
                dbLayer.getSession().save(r.getNotification());
                Long id = r.getNotification().getId();

                for (DBItemNotificationMonitor m : r.getMonitors()) {
                    m.setNotificationId(id);
                    dbLayer.getSession().save(m);
                }
            }

            dbLayer.commit();
        } catch (Throwable e) {
            LOGGER.info(String.format("[%s][DB][error]%s", IDENTIFIER, e.toString()));
            dbLayer.rollback();
        } finally {
            dbLayer.close();
            // db failed - events with time delay - but JOC is not reachable without db anyway...
            // db success - events are synchronized with the database entries ...
            for (SystemMonitoringEvent event : events) {
                postEvent(event);
            }
        }
    }

    private void postEvent(SystemMonitoringEvent event) {
        if (event == null) {
            return;
        }
        EventBus.getInstance().post(new MonitoringGuiEvent(event.getType().intValue(), event.getCategory().name(), event.getSource(), event
                .getCaller(), event.getEpochMillis(), event.getMessage()));
    }

    protected JocClusterAnswer close(StartupMode mode) {
        MonitorService.setLogger();
        closed.set(true);

        if (threadPool != null) {
            JocCluster.shutdownThreadPool(mode, threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);
        }
        if (threadPoolDB != null) {
            JocCluster.shutdownThreadPool(mode, threadPoolDB, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);
        }
        return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
    }

    /** Truncate ms, because the database can round seconds from ms, but the time sent by the notifiers is not rounded<br/>
     * e.g.: 2023-01-30T12:00:00.892Z<br/>
     * - TIME notifier: 2023-01-30T13:00:00+0100<br/>
     * - TIME db: 2023-01-30 12:00:01 */
    private Date truncateMillis(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(millis));
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

}
