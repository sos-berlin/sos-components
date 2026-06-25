package com.sos.joc.monitoring.model;

import java.nio.file.Path;
import java.sql.Connection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.concurrency.SOSNamedForkJoinPoolThreadFactory;
import com.sos.history.JobWarning;
import com.sos.joc.Globals;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.InventoryJobTagDBLayer;
import com.sos.joc.db.monitoring.DBItemMonitoringOrder;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;
import com.sos.joc.db.monitoring.DBItemNotification;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.monitoring.NotificationCreated;
import com.sos.joc.model.cluster.common.state.JocClusterState;
import com.sos.joc.monitoring.MonitorService;
import com.sos.joc.monitoring.configuration.Configuration;
import com.sos.joc.monitoring.configuration.Notification;
import com.sos.joc.monitoring.configuration.monitor.AMonitor;
import com.sos.joc.monitoring.db.DBLayerMonitoring;
import com.sos.joc.monitoring.db.LastWorkflowNotificationDBItemEntity;
import com.sos.joc.monitoring.model.bean.AMonitorResult;
import com.sos.joc.monitoring.model.bean.MonitorEmptyResult;
import com.sos.joc.monitoring.model.bean.MonitorOrderResult;
import com.sos.joc.monitoring.model.bean.MonitorOrderStepResult;
import com.sos.joc.monitoring.model.bean.MonitorOrderStepResultWarn;
import com.sos.joc.monitoring.model.bean.NotifierTask;
import com.sos.joc.monitoring.model.bean.ToNotify;
import com.sos.joc.monitoring.notification.notifier.ANotifier;
import com.sos.joc.monitoring.notification.notifier.NotifyResult;
import com.sos.monitoring.notification.NotificationType;
import com.sos.monitoring.notification.OrderNotificationRange;

public class OrderNotifierModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderNotifierModel.class);

    private static final String LOG_IDENTIFIER = String.format("[%s][%s]", MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY,
            MonitorService.NOTIFICATION_IDENTIFIER);

    private static final String IDENTIFIER = String.format("%s_n", MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY);
    private static final String IDENTIFIER_NOTIFICATION_MONITOR_EXECUTOR = IDENTIFIER + "_ex";
    private static final String IDENTIFIER_NOTIFICATION_DB = IDENTIFIER + "_db";

    private static final AMonitorResult CANDIDATES_WAKEUP_ENTRY = new MonitorEmptyResult();
    private final Path hibernateConfigFile;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private SOSHibernateFactory factory;
    // Lazy initialization: initialized only if Notification is used in the current JS7 environment
    // Queue of entries to be processed. BlockingQueue.take() waits efficiently until an entry becomes available.
    private BlockingQueue<AMonitorResult> candidates;
    // Active notifiers
    private Set<NotifierTask> active;
    // Dedicated dispatcher thread that consumes queue entries and starts notification processing in the background.
    private Thread dispatcherThread;
    // Monitor service thread group
    private ThreadGroup threadGroup;
    // Executor for parallel notification delivery (email, script, etc.).
    private ExecutorService notificationMonitorExecutor;
    // Single-threaded executor for persisting notification results from notificationExecutor.
    private ExecutorService dbUpdateExecutor;

    private AtomicBoolean closed = new AtomicBoolean();

    protected OrderNotifierModel(ThreadGroup threadGroup, Path hibernateConfigFile) {
        this.threadGroup = threadGroup;
        this.hibernateConfigFile = hibernateConfigFile;
    }

    private void initIfNeeded() {
        if (initialized.compareAndSet(false, true)) {
            MonitorService.setLogger();

            factory = createFactory();
            candidates = new LinkedBlockingQueue<>();
            active = ConcurrentHashMap.newKeySet();

            if (factory == null) {
                LOGGER.info(String.format("%s[skip]due to the database factory errors", LOG_IDENTIFIER));
            } else {
                notificationMonitorExecutor = new ForkJoinPool(Configuration.INSTANCE.getNotificationParallelism(),
                        new SOSNamedForkJoinPoolThreadFactory(IDENTIFIER_NOTIFICATION_MONITOR_EXECUTOR), null, true);

                dbUpdateExecutor = Executors.newSingleThreadExecutor(new JocClusterThreadFactory(threadGroup, IDENTIFIER_NOTIFICATION_DB));

                dispatcherThread = new Thread(this.threadGroup, this::dispatchLoop, IDENTIFIER);
                dispatcherThread.setDaemon(true); // does not block JVM shutdown
                dispatcherThread.start();
            }
        }
    }

    /** Dispatcher loop: processes entries continuously.<br/>
     * Uses BlockingQueue.take() to block efficiently when the queue is empty.<br/>
     * - This avoids any CPU busy-wait and minimizes memory overhead.<br/>
     * - Note: take() blocks until an entry is available - element is removed from the queue. */
    private void dispatchLoop() {
        MonitorService.setLogger();

        if (!initializeFactory()) {
            return;
        }

        while (!closed.get()) {
            try {
                AMonitorResult r = candidates.take();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("%s[dispatchLoop]%s", LOG_IDENTIFIER, SOSString.toString(r)));
                }
                if (r == CANDIDATES_WAKEUP_ENTRY) { // == instead of equals is ok
                    continue; // go to check closed.get()
                }
                if (r != null) {
                    if (r.isStep()) { // STEP
                        MonitorOrderStepResult step = (MonitorOrderStepResult) r;
                        if (r.isWarnStep()) {
                            notifyStepWarning(step);
                        } else {
                            notifyStep(step);
                        }
                    } else { // ORDER
                        MonitorOrderResult order = (MonitorOrderResult) r;
                        if (order.isErrorOrder()) {
                            notifyOrder(order, NotificationType.ERROR);
                        } else {
                            notifyOrder(order, NotificationType.SUCCESS);
                        }
                    }
                    cleanupActive();
                }
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    protected void notify(ToNotify toNotifyPayloads, ToNotify toNotifyLongerThanNotPayloadWarnings) {
        if (!Configuration.INSTANCE.hasNotifications()) {
            return;
        }

        initIfNeeded();

        // PAYLOAD: steps
        for (MonitorOrderStepResult step : toNotifyPayloads.getSteps()) {
            step.setIsStep();
            candidates.add(step);
        }
        // PAYLOAD: orders (ERROR, SUCCESS)
        for (MonitorOrderResult order : toNotifyPayloads.getErrorOrders()) {
            order.isErrorOrder();
            candidates.add(order);
        }
        for (MonitorOrderResult order : toNotifyPayloads.getSuccessOrders()) {
            candidates.add(order);
        }
        // NOT PAYLOAD: longerThan warning steps
        for (MonitorOrderStepResult step : toNotifyLongerThanNotPayloadWarnings.getSteps()) {
            step.setIsWarnStep();
            candidates.add(step);
        }
    }

    private void notifyStep(MonitorOrderStepResult r) {
        OrderNotificationRange range = OrderNotificationRange.WORKFLOW_JOB;
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        if (r.isCompleted()) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[notifyStep][skip][completed=true][%s]%s", range, r.toString()));
            }
            return;
        }

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[notifyStep][start][%s]%s", range, r.toString()));
        }

        notifyStepWarning(r);

        List<Notification> configuredNotifications;
        if (r.getStep().getError()) {
            // ERROR
            if (!r.isErrorCompleted()) {
                configuredNotifications = Configuration.INSTANCE.findWorkflowMatches(range, Configuration.INSTANCE.getOnError(), r.getStep()
                        .getControllerId(), r.getStep().getWorkflowPath(), r.getStep().getJobName(), r.getStep().getJobLabel(), r.getStep()
                                .getCriticality(), r.getStep().getReturnCode());
                notify(range, configuredNotifications, null, r, NotificationType.ERROR, null);
                r.setErrorCompleted();
            }
        } else {
            // RECOVERY
            if (!r.isRecoveryCompleted()) {
                configuredNotifications = Configuration.INSTANCE.findWorkflowMatches(range, Configuration.INSTANCE.getOnError(), r.getStep()
                        .getControllerId(), r.getStep().getWorkflowPath(), r.getStep().getJobName(), r.getStep().getJobLabel(), r.getStep()
                                .getCriticality(), r.getStep().getReturnCode());
                notify(range, configuredNotifications, null, r, NotificationType.RECOVERED, null);
                r.setRecoveryCompleted();
            }
            // SUCCESS
            if (!r.isSuccessCompleted()) {
                configuredNotifications = Configuration.INSTANCE.findWorkflowMatches(range, Configuration.INSTANCE.getOnSuccess(), r.getStep()
                        .getControllerId(), r.getStep().getWorkflowPath(), r.getStep().getJobName(), r.getStep().getJobLabel(), r.getStep()
                                .getCriticality(), r.getStep().getReturnCode());
                notify(range, configuredNotifications, null, r, NotificationType.SUCCESS, null);
                r.setSuccessCompleted();
            }
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[notifyStep][end][%s]%s", range, r.toString()));
        }
    }

    private void notifyStepWarning(MonitorOrderStepResult r) {
        if (Configuration.INSTANCE.getOnWarning().size() > 0 && r.getWarnings().size() > 0) {
            if (r.isWarnCompleted()) {
                return;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[notifyStepWarning][start]warnings=%s", r.getWarnings().size()));
            }
            OrderNotificationRange range = OrderNotificationRange.WORKFLOW_JOB;
            List<Notification> configuredNotifications = Configuration.INSTANCE.findWorkflowMatches(range, Configuration.INSTANCE.getOnWarning(), r
                    .getStep().getControllerId(), r.getStep().getWorkflowPath(), r.getStep().getJobName(), r.getStep().getJobLabel(), r.getStep()
                            .getCriticality(), r.getStep().getReturnCode());
            notify(range, configuredNotifications, null, r, NotificationType.WARNING, r.getWarnings());
            r.setWarnCompleted();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[notifyStepWarning][end]warnings=%s", r.getWarnings().size()));
            }
        }
    }

    private void notifyOrder(MonitorOrderResult r, NotificationType type) {
        OrderNotificationRange range = OrderNotificationRange.WORKFLOW;
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        if (r.isCompleted()) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[notifyOrder][skip][completed=true][%s]%s", range, r.toString()));
            }
            return;
        }

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[notifyOrder][start][%s][%s]%s", range, type, SOSString.toString(r)));
        }

        List<Notification> configuredNotifications;
        switch (type) {
        case ERROR:
            if (!r.isErrorCompleted()) {
                configuredNotifications = Configuration.INSTANCE.findWorkflowMatches(OrderNotificationRange.WORKFLOW, Configuration.INSTANCE
                        .getOnError(), r.getOrder().getControllerId(), r.getOrder().getWorkflowPath());
                notify(range, configuredNotifications, r, null, NotificationType.ERROR, null);
                r.setErrorCompleted();
            }
            break;
        case SUCCESS:
            // RECOVERY
            if (!r.isRecoveryCompleted()) {
                configuredNotifications = Configuration.INSTANCE.findWorkflowMatches(OrderNotificationRange.WORKFLOW, Configuration.INSTANCE
                        .getOnError(), r.getOrder().getControllerId(), r.getOrder().getWorkflowPath());
                notify(range, configuredNotifications, r, null, NotificationType.RECOVERED, null);
                r.setRecoveryCompleted();
            }
            // SUCCESS
            if (!r.isSuccessCompleted()) {
                configuredNotifications = Configuration.INSTANCE.findWorkflowMatches(OrderNotificationRange.WORKFLOW, Configuration.INSTANCE
                        .getOnSuccess(), r.getOrder().getControllerId(), r.getOrder().getWorkflowPath());
                notify(range, configuredNotifications, r, null, NotificationType.SUCCESS, null);
                r.setSuccessCompleted();
            }
            break;
        default:
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[notifyOrder][skip][%s][%s]because NotificationType=%s", range, type, type));
            }
            break;
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[notifyOrder][end][%s][%s]%s", range, type, SOSString.toString(r)));
        }
    }

    private boolean notify(OrderNotificationRange range, List<Notification> configuredNotifications, MonitorOrderResult mor,
            MonitorOrderStepResult mosr, NotificationType type, List<MonitorOrderStepResultWarn> warnings) {
        if (configuredNotifications.size() == 0) {
            return false;
        }

        OrderNotifyAnalyzer analyzer = new OrderNotifyAnalyzer();

        DBLayerMonitoring dbLayer = new DBLayerMonitoring(IDENTIFIER);
        try {
            dbLayer.setSession(factory.openStatelessSession());
            dbLayer.getSession().beginTransaction();

            if (!analyzer.analyze(range, dbLayer, configuredNotifications, mor, mosr, type, warnings)) {
                return false;
            }

            Map<Long, DBItemMonitoringOrderStep> steps = new HashMap<>();
            boolean notified = false;
            boolean isWarning = NotificationType.WARNING.equals(type) && warnings != null;

            List<CompletableFuture<NotifierTask>> allFutures = new ArrayList<>();
            for (Notification notification : configuredNotifications) {
                if (isWarning) {
                    for (MonitorOrderStepResultWarn warning : warnings) {
                        List<CompletableFuture<NotifierTask>> futures = notify(dbLayer, range, analyzer, notification, type, steps, warning);
                        if (futures != null) {
                            notified = true;
                            allFutures.addAll(futures);
                        }
                    }
                } else {
                    List<CompletableFuture<NotifierTask>> futures = notify(dbLayer, range, analyzer, notification, type, steps, null);
                    if (futures != null) {
                        notified = true;
                        allFutures.addAll(futures);
                    }
                }
            }
            dbLayer.getSession().commit();
            dbLayer.close();
            dbLayer = null;

            // dbUpdateExecutor.execute(() -> {
            updateMonitors(allFutures);
            // });

            return notified;
        } catch (Exception e) {
            LOGGER.error(LOG_IDENTIFIER + e.toString(), e);
            dbLayer.rollback();
            return false;
        } finally {
            if (dbLayer != null) {
                dbLayer.close();
            }
        }
    }

    private List<CompletableFuture<NotifierTask>> notify(DBLayerMonitoring dbLayer, OrderNotificationRange range, OrderNotifyAnalyzer analyzer,
            Notification notification, NotificationType type, Map<Long, DBItemMonitoringOrderStep> steps, MonitorOrderStepResultWarn warning)
            throws Exception {

        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        DBItemMonitoringOrderStep os = analyzer.getOrderStep();
        Long recoveredId = null;
        JobWarning warnReason = JobWarning.NONE;
        String warnText = null;
        switch (type) {
        case ERROR:
        case SUCCESS:
            break;
        case RECOVERED:
            if (analyzer.getToRecovery() != null) {
                LastWorkflowNotificationDBItemEntity r = analyzer.getToRecovery().get(notification.getNotificationId());
                if (r == null) {
                    return null;
                }
                recoveredId = r.getId();
                if (steps.containsKey(r.getStepId())) {
                    os = steps.get(r.getStepId());
                } else {
                    os = dbLayer.getMonitoringOrderStep(r.getStepId(), true);
                    if (os == null) {
                        if (isDebugEnabled) {
                            JobWarning wr = warning == null ? null : warning.getReason();
                            LOGGER.debug(String.format("%s[notification id=%s][%s][%s][skip][monitoringOrderStep not found]%s",
                                    Configuration.LOG_INTENT, notification.getNotificationId(), range, ANotifier.getTypeAsString(type, wr), r
                                            .getStepId()));
                        }
                        return null;
                    }
                    steps.put((r.getStepId()), os);
                }
            }
            break;
        case WARNING:
            if (warning == null || warning.getReason() == null) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[notification id=%s][%s][%s][skip]warning or warning reason is null", Configuration.LOG_INTENT,
                            notification.getNotificationId(), range, ANotifier.getTypeAsString(type, null)));
                }
                return null;
            }

            if (analyzer.getSentWarnings() != null && analyzer.getSentWarnings().containsKey(notification.getNotificationId())) {
                if (analyzer.getSentWarnings().get(notification.getNotificationId()).contains(warning.getReason())) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[notification id=%s][%s][%s %s][skip][already sent]%s", Configuration.LOG_INTENT, notification
                                .getNotificationId(), range, ANotifier.getTypeAsString(type, warning.getReason()), warning.getReason(), analyzer
                                        .getSentWarnings()));
                    }
                    return null;
                }
            }

            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[notification id=%s][%s][%s][warning=%s]sentWarnings=%s", Configuration.LOG_INTENT, notification
                        .getNotificationId(), range, ANotifier.getTypeAsString(type, warning.getReason()), SOSString.toString(warning), analyzer
                                .getSentWarnings()));
            }

            warnReason = warning.getReason();
            warnText = warning.getText();
            break;
        case ACKNOWLEDGED:
            return null;
        }

        DBItemNotification mn = null;
        try {
            mn = dbLayer.saveNotification(notification, analyzer, range, type, recoveredId, warnReason, warnText);
            if (notification.getMonitors().size() == 0) {
                LOGGER.info(String.format("[%s][notification id=%s][%s][%s][store to database only]%s%s", mn.getId(), notification
                        .getNotificationId(), range, ANotifier.getTypeAsString(type, warnReason), ANotifier.getInfo(analyzer), (warnText == null ? ""
                                : warnText)));
            } else {
                LOGGER.info(String.format("[%s][notification id=%s][%s][%s][send to %s monitors %s]%s%s", mn.getId(), notification
                        .getNotificationId(), range, ANotifier.getTypeAsString(type, warnReason), notification.getMonitors().size(), notification
                                .getMonitorsAsString(), ANotifier.getInfo(analyzer), (warnText == null ? "" : warnText)));
            }
        } catch (Exception e) {
            LOGGER.error(String.format("%s[notification id=%s][%s][%s]%s[failed]%s", LOG_IDENTIFIER, notification.getNotificationId(), range,
                    ANotifier.getTypeAsString(type, warnReason), ANotifier.getInfo(analyzer), e.toString()), e);
        }

        if (os != null && analyzer.getOrder() != null) {
            try {
                if (os.getTags() == null) {
                    os.setTags(new InventoryJobTagDBLayer(dbLayer.getSession()).getGroupedTagsOfJob(analyzer.getOrder().getWorkflowName(), os
                            .getJobName()));
                }
            } catch (Exception e) {
                LOGGER.error(String.format("%s[notification id=%s][%s][%s]%s[step][setTags][failed]%s", LOG_IDENTIFIER, notification
                        .getNotificationId(), range, ANotifier.getTypeAsString(type, warnReason), ANotifier.getInfo(analyzer), e.toString()), e);
            }
        }
        postEvent(analyzer.getControllerId(), mn, analyzer.getOrder(), os);

        return notifyMonitors(range, analyzer, notification, type, os, warnReason, mn);
    }

    private List<CompletableFuture<NotifierTask>> notifyMonitors(OrderNotificationRange range, OrderNotifyAnalyzer analyzer,
            Notification notification, NotificationType type, DBItemMonitoringOrderStep os, JobWarning warnReason, DBItemNotification mn) {
        List<CompletableFuture<NotifierTask>> futures = new ArrayList<>();
        int i = 0;
        Instant now = Instant.now();
        for (final AMonitor m : notification.getMonitors()) {
            final int index = ++i;
            String identifier = mn.getId() + "][" + index + "][" + range + "][" + ANotifier.getTypeAsString(type, warnReason);

            final NotifierTask task = new NotifierTask(range, analyzer, notification, type, m, identifier, mn, os, warnReason);
            task.setSubmitted(now);
            active.add(task);

            if (closed.get()) {
                continue;
            }

            futures.add(CompletableFuture.supplyAsync(() -> {
                // return executeMonitorNotification(range, analyzer, notification, type, m, identifier, mn, os, warnReason);
                return executeMonitorNotification(task);
            }, notificationMonitorExecutor));
        }
        return futures;
    }

    private void updateMonitors(List<CompletableFuture<NotifierTask>> futures) {
        for (CompletableFuture<NotifierTask> task : futures) {
            task.thenAcceptAsync(t -> {
                MonitorService.setLogger();
                DBLayerMonitoring dbLayer = null;
                try {
                    if (t.isSaveNotificationMonitor()) {
                        dbLayer = new DBLayerMonitoring(IDENTIFIER_NOTIFICATION_DB);
                        // dbLayer.setSession(factory.openStatelessSession());
                        dbLayer.setSession(Globals.createSosHibernateStatelessConnection(IDENTIFIER_NOTIFICATION_DB));
                        // dbLayer.getSession().beginTransaction();
                        if (t.getException() != null) {
                            dbLayer.saveNotificationMonitor(t.getDbNotification(), t.getMonitor(), t.getException());
                        } else if (t.getNotifyResult() != null) {
                            dbLayer.saveNotificationMonitor(t.getDbNotification(), t.getMonitor(), t.getNotifyResult());
                        }
                        // dbLayer.commit();
                    }
                } catch (Exception e) {
                    // if (dbLayer != null) {
                    // dbLayer.rollback();
                    // }
                    LOGGER.warn(LOG_IDENTIFIER + e.toString(), e);
                } finally {
                    if (dbLayer != null) {
                        dbLayer.close();
                    }
                }

            }, dbUpdateExecutor);
        }
    }

    private NotifierTask executeMonitorNotification(NotifierTask task) {
        active.remove(task);

        MonitorService.setLogger();

        // NotifierResult r = new NotifierResult(task.getMonitor());
        ANotifier n = null;
        try {
            n = task.getMonitor().createNotifier(task.getIdentifier());
        } catch (Exception e) {
            LOGGER.error(LOG_IDENTIFIER + e.toString(), e);// contains all informations about the type etc
            if (task.getDbNotification() == null) {
                LOGGER.info(String.format("%s[%s][notification id=%s][%s][%s]%s[skip save notification monitor]due to save notification failed",
                        Configuration.LOG_INTENT, task.getIdentifier(), task.getNotification().getNotificationId(), task.getRange(), ANotifier
                                .getTypeAsString(task.getType(), task.getWarnReason()), ANotifier.getInfo(task.getAnalyzer(), task.getMonitor())));
            } else {
                task.setExecuted();
                task.saveNotificationMonitor();

                task.setException(e);
            }
            n = null;
        }

        if (n != null) {
            try {
                NotifyResult nr = n.notifyOrderNotification(task.getType(), task.getMonitor().getTimeZone(), task.getAnalyzer().getOrder(), task
                        .getDbOrderStep(), task.getDbNotification());
                if (nr != null && nr.getError() != null) {
                    if (Configuration.INSTANCE.retryIncompleteNotificationsOnStartup() && closed.get() && nr.getError()
                            .getException() instanceof InterruptedException) {
                        LOGGER.info(String.format("%s[on close][%s]notification monitor execution interrupted - marked for retry on next startup",
                                LOG_IDENTIFIER, task.getIdentifier()));

                        // create retry candidate without execution result data
                        NotifierTask taskCopy = task.copyWithoutResult();
                        taskCopy.setSubmitted(task.getSubmitted());
                        active.add(taskCopy);
                    }
                    // LOGGER.error(LOG_IDENTIFIER + nr.getError().getMessage(), nr.getError().getException());
                    LOGGER.error(LOG_IDENTIFIER + nr.getError().getMessage());
                }
                if (task.getDbNotification() == null) {
                    LOGGER.info(String.format("%s[%s][notification id=%s][%s][%s]%s[skip save notification result]due to save notification failed",
                            Configuration.LOG_INTENT, task.getIdentifier(), task.getNotification().getNotificationId(), task.getRange(), ANotifier
                                    .getTypeAsString(task.getType(), task.getWarnReason()), ANotifier.getInfo(task.getAnalyzer(), task
                                            .getMonitor())));
                } else {
                    if (nr.getSkipCause() == null) {
                        task.saveNotificationMonitor();

                        task.setNotifyResult(nr);
                    } else {
                        LOGGER.info(String.format("%s[%s][notification id=%s][%s][%s][skip]%s%s%s", Configuration.LOG_INTENT, task.getIdentifier(),
                                task.getNotification().getNotificationId(), task.getRange(), ANotifier.getTypeAsString(task.getType(), task
                                        .getWarnReason()), ANotifier.getMonitorInfo(task.getMonitor()), nr.getSkipCause(), ANotifier.getInfo(task
                                                .getAnalyzer())));
                    }
                }
            } catch (Exception e) {
                LOGGER.error(LOG_IDENTIFIER + e.toString(), e);
            } finally {
                task.setExecuted();
                n.close();
            }
        }
        return task;
    }

    private void postEvent(String controllerId, DBItemNotification mn, DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos) {
        if (mn == null || mo == null) {
            return;
        }
        String jobName = mos == null ? "" : mos.getJobName();
        EventBus.getInstance().post(new NotificationCreated(controllerId, mn.getId(), mn.getType(), mo.getWorkflowName(), mo.getOrderId(), jobName, mn
                .getCreated(), getPostEventMessage(mn, mo, mos)));
    }

    // see com.sos.joc.monitoring.impl.OrderNotificationsImpl.getMessage
    private String getPostEventMessage(DBItemNotification mn, DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos) {
        if (mn.getType().equals(NotificationType.WARNING.intValue())) {
            return mn.getWarnText();
        }
        if (mos != null && !SOSString.isEmpty(mos.getErrorText())) {
            return mos.getErrorText();
        }
        return mo.getErrorText();
    }

    protected JocClusterAnswer close(StartupMode mode) {
        MonitorService.setLogger();
        closed.set(true);

        // wake up candidates.take() if it is currently waiting
        candidates.offer(CANDIDATES_WAKEUP_ENTRY);

        if (dispatcherThread != null) {
            try {
                dispatcherThread.join(); // wait for clean termination
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (notificationMonitorExecutor != null) {
            JocCluster.shutdownThreadPool("[" + IDENTIFIER_NOTIFICATION_MONITOR_EXECUTOR + "][" + mode + "]", notificationMonitorExecutor, 60);
        }
        if (dbUpdateExecutor != null) {
            JocCluster.shutdownThreadPool("[" + IDENTIFIER_NOTIFICATION_DB + "][" + mode + "]", dbUpdateExecutor, 60);
        }

        closeFactory();
        return JocCluster.getOKAnswer(JocClusterState.STOPPED);
    }

    private boolean initializeFactory() {
        int connErrors = 0;

        while (factory == null && !closed.get()) {
            connErrors++;
            LOGGER.info(String.format("%s[skip %s of 10]due to the database factory errors", LOG_IDENTIFIER, connErrors));

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                return false;
            }
            factory = createFactory();
            if (connErrors >= 10 && factory == null) {
                return false;
            }
        }
        return factory != null;
    }

    private JocClusterHibernateFactory createFactory() {
        JocClusterHibernateFactory factory = null;
        try {
            factory = new JocClusterHibernateFactory(this.hibernateConfigFile, 1, 1);
            factory.setIdentifier(IDENTIFIER);
            factory.setAutoCommit(true); // DBLayerMonitoring - executes select/save - no update/delete statements
            factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            factory.addClassMapping(DBLayer.getMonitoringClassMapping());
            factory.build();
        } catch (SOSHibernateException e) {
            LOGGER.error(LOG_IDENTIFIER + "[createFactory][" + hibernateConfigFile + "]" + e.toString(), e);
            factory = null;
        }
        return factory;
    }

    private void closeFactory() {
        if (factory != null) {
            factory.close();
            LOGGER.info(String.format("%sdatabase factory closed", LOG_IDENTIFIER));
        }
    }

    public void setCandidates(Collection<AMonitorResult> queue) {
        initIfNeeded();
        this.candidates.addAll(queue);
    }

    public void runRestoredActiveNotifiers(Set<NotifierTask> tasks) {
        initIfNeeded();

        List<CompletableFuture<NotifierTask>> futures = new ArrayList<>();
        Instant now = Instant.now();
        for (NotifierTask task : tasks) {
            task.setSubmitted(now);
            active.add(task);
            futures.add(CompletableFuture.supplyAsync(() -> {
                // return executeMonitorNotification(range, analyzer, notification, type, m, identifier, mn, os, warnReason);
                return executeMonitorNotification(task);
            }, notificationMonitorExecutor));
        }
        updateMonitors(futures);
    }

    public List<AMonitorResult> getCandidatesSnapshot() {
        return candidates == null ? new ArrayList<>() : candidates.stream().filter(r -> r != CANDIDATES_WAKEUP_ENTRY).collect(Collectors.toList());
    }

    public Set<NotifierTask> getActiveSnapshot() {
        return active == null ? new HashSet<>() : new HashSet<>(active);
    }

    public void clear() {
        clearCandidates();
        clearActive();
    }

    public int getCandidatesSize() {
        return candidates == null ? 0 : candidates.size();
    }

    public int getActiveSize() {
        return active == null ? 0 : active.size();
    }

    private void clearCandidates() {
        if (candidates == null) {
            return;
        }
        candidates.clear();
    }

    private void clearActive() {
        if (active == null) {
            return;
        }
        active.clear();
    }

    private void cleanupActive() {
        int size = getActiveSize();
        if (size > 100) {
            Instant now = Instant.now();

            int before = size;
            active.removeIf(task -> task.isExceeded(now));
            int removed = before - active.size();
            if (removed > 0) {
                LOGGER.info(String.format("%s[cleanupActive]size=%s, removed=%s", LOG_IDENTIFIER, before, removed));
            }
        }
    }

}
