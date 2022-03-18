package com.sos.joc.monitoring.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateObjectOperationException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSSerializer;
import com.sos.commons.util.SOSString;
import com.sos.controller.model.event.EventType;
import com.sos.history.JobWarning;
import com.sos.joc.cluster.AJocClusterService;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.bean.history.AHistoryBean;
import com.sos.joc.cluster.bean.history.HistoryOrderBean;
import com.sos.joc.cluster.bean.history.HistoryOrderStepBean;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.db.joc.DBItemJocVariable;
import com.sos.joc.db.monitoring.DBItemMonitoringOrder;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.deploy.DeployHistoryJobResourceEvent;
import com.sos.joc.event.bean.history.HistoryEvent;
import com.sos.joc.event.bean.history.HistoryOrderEvent;
import com.sos.joc.event.bean.history.HistoryTaskEvent;
import com.sos.joc.event.bean.monitoring.MonitoringEvent;
import com.sos.joc.event.bean.monitoring.NotificationConfigurationReleased;
import com.sos.joc.event.bean.monitoring.NotificationConfigurationRemoved;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.joc.monitoring.configuration.Configuration;
import com.sos.joc.monitoring.configuration.monitor.mail.MailResource;
import com.sos.joc.monitoring.db.DBLayerMonitoring;
import com.sos.monitoring.notification.NotificationType;

public class HistoryMonitoringModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryMonitoringModel.class);

    private static final String IDENTIFIER = ClusterServices.history.name();
    private static final String NOTIFICATION_IDENTIFIER = "notification";
    /** seconds */
    private static final long SCHEDULE_DELAY = 2;
    private static final int THREAD_POOL_CORE_POOL_SIZE = 1;
    /** 1day */
    private static final int MAX_LONGER_THAN_SECONDS = 24 * 60 * 60;

    private final SOSHibernateFactory factory;
    private final JocConfiguration jocConfiguration;
    private final NotifierModel notifier;
    private final String serviceIdentifier;

    private ScheduledExecutorService threadPool;
    private CopyOnWriteArraySet<AHistoryBean> payloads = new CopyOnWriteArraySet<>();
    // HashMap type on the left side for serializing
    private HashMap<Long, HistoryOrderStepBean> longerThan = new HashMap<>();// new ConcurrentHashMap<>();

    private AtomicLong lastActivityStart = new AtomicLong();
    private AtomicLong lastActivityEnd = new AtomicLong();

    private AtomicBoolean closed = new AtomicBoolean();
    private Configuration configuration;

    private boolean tmpLogging = false;
    // TODO ? commit after n db operations
    // private int maxTransactions = 100;

    public HistoryMonitoringModel(ThreadGroup threadGroup, SOSHibernateFactory factory, JocConfiguration jocConfiguration, String serviceIdentifier) {
        this.factory = factory;
        this.jocConfiguration = jocConfiguration;
        this.serviceIdentifier = serviceIdentifier;
        this.notifier = new NotifierModel(threadGroup, factory.getConfigFile().get(), this.serviceIdentifier);
        EventBus.getInstance().register(this);
    }

    @Subscribe({ HistoryOrderEvent.class, HistoryTaskEvent.class })
    public void handleHistoryEvents(HistoryEvent evt) {
        // AJocClusterService.setLogger(serviceIdentifier);
        // LOGGER.info("[EV]" + SOSString.toString(evt));

        AJocClusterService.setLogger(serviceIdentifier);
        if (evt.getPayload() != null) {
            payloads.add((AHistoryBean) evt.getPayload());
        }
    }

    @Subscribe({ NotificationConfigurationReleased.class, NotificationConfigurationRemoved.class })
    public void handleMonitoringEvents(MonitoringEvent evt) {
        if (configuration != null) {
            AJocClusterService.setLogger(serviceIdentifier);
            LOGGER.info(String.format("[%s][%s][configuration]%s", serviceIdentifier, NOTIFICATION_IDENTIFIER, evt.getClass().getSimpleName()));
            setConfiguration();
        }
    }

    @Subscribe({ DeployHistoryJobResourceEvent.class })
    public void handleMonitoringEvents(DeployHistoryJobResourceEvent evt) {
        if (configuration != null && configuration.exists() && evt.getName() != null) {
            AJocClusterService.setLogger(serviceIdentifier);
            List<String> names = configuration.getMailResources().entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
            if (names.contains(evt.getName())) {
                LOGGER.info(String.format("[%s][%s][configuration]%s jr=%s", serviceIdentifier, NOTIFICATION_IDENTIFIER, evt.getClass()
                        .getSimpleName(), evt.getName()));
                setConfiguration();
            }
        }
    }

    public void start(ThreadGroup threadGroup) {
        closed.set(false);

        deserialize();
        setConfiguration();
        schedule(threadGroup);

        AJocClusterService.setLogger(serviceIdentifier);
        LOGGER.info(String.format("[%s][%s]start..", serviceIdentifier, IDENTIFIER));
    }

    public void close(StartupMode mode) {
        closed.set(true);

        if (notifier != null) {
            notifier.close(mode);
        }

        if (threadPool != null) {
            AJocClusterService.setLogger(serviceIdentifier);
            JocCluster.shutdownThreadPool(mode, threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);
            threadPool = null;
            serialize();
        }
    }

    private void schedule(ThreadGroup threadGroup) {
        this.threadPool = Executors.newScheduledThreadPool(THREAD_POOL_CORE_POOL_SIZE, new JocClusterThreadFactory(threadGroup, serviceIdentifier
                + "-h"));
        this.threadPool.scheduleWithFixedDelay(new Runnable() {

            private AtomicLong currentEventId = new AtomicLong();
            private AtomicLong lastStart = new AtomicLong();

            private Long calculateEventId(Long eventId, long lastDuration) {
                if (eventId == null) {// no new events
                    long id = currentEventId.get();
                    if (id != 0 && lastDuration != 0) {
                        eventId = id + (lastDuration * 1_000);
                    }
                }
                if (eventId != null) {
                    currentEventId.set(eventId);
                }
                return eventId == null ? 0L : eventId;
            }

            @Override
            public void run() {
                long currentStart = new Date().getTime();
                long previousStart = lastStart.get();
                long lastDuration = 0;
                if (previousStart != 0) {
                    lastDuration = currentStart - previousStart;
                }
                lastStart.set(currentStart);
                try {
                    AJocClusterService.setLogger(serviceIdentifier);
                    boolean isDebugEnabled = LOGGER.isDebugEnabled();

                    ToNotify toNotifyPayloads = handlePayloads(isDebugEnabled);
                    ToNotify toNotifyLongerThan = handleLongerThan(calculateEventId(toNotifyPayloads.getLastEventId(), lastDuration));

                    notifier.notify(configuration, toNotifyPayloads, toNotifyLongerThan);
                } catch (Throwable e) {
                    AJocClusterService.setLogger(serviceIdentifier);
                    LOGGER.error(e.toString(), e);
                }
            }
        }, 0 /* start delay */, SCHEDULE_DELAY /* delay */, TimeUnit.SECONDS);

    }

    private ToNotify handlePayloads(boolean isDebugEnabled) {
        ToNotify toNotify = new ToNotify();
        if (payloads.size() == 0) {
            return toNotify;
        }

        setLastActivityStart();
        List<AHistoryBean> toRemove = new ArrayList<>();
        DBLayerMonitoring dbLayer = new DBLayerMonitoring(serviceIdentifier + "_" + IDENTIFIER, serviceIdentifier);
        try {
            Instant start = Instant.now();
            List<AHistoryBean> copy = new ArrayList<>(payloads);
            copy.sort(Comparator.comparing(AHistoryBean::getEventId));

            dbLayer.setSession(factory.openStatelessSession(dbLayer.getIdentifier()));
            dbLayer.getSession().beginTransaction();

            HistoryOrderBean hob;
            for (AHistoryBean b : copy) {
                if (isDebugEnabled) {
                    LOGGER.debug("[PAYLOADS]" + b.getEventType() + "=" + SOSString.toString(b));
                }
                if (closed.get()) {
                    break;
                }

                switch (b.getEventType()) {
                // Order
                case OrderStarted:
                    orderStarted(dbLayer, (HistoryOrderBean) b);
                    break;
                case OrderResumed:
                    orderResumed(dbLayer, (HistoryOrderBean) b);
                    break;
                case OrderForked:
                    orderForked(dbLayer, (HistoryOrderBean) b);
                    break;
                case OrderJoined:
                    orderJoined(dbLayer, (HistoryOrderBean) b);
                    break;
                case OrderFailed:
                    hob = (HistoryOrderBean) b;
                    orderFailed(dbLayer, hob);

                    toNotify.getErrorOrders().add(hob);
                    break;
                case OrderSuspended:
                    orderSuspended(dbLayer, (HistoryOrderBean) b);
                    break;
                case OrderCancelled:
                    orderCancelled(dbLayer, (HistoryOrderBean) b);
                    break;
                case OrderBroken:
                    hob = (HistoryOrderBean) b;
                    orderBroken(dbLayer, hob);

                    toNotify.getErrorOrders().add(hob);
                    break;
                case OrderFinished:
                    hob = (HistoryOrderBean) b;
                    orderFinished(dbLayer, hob);

                    toNotify.getSuccessOrders().add(hob);
                    break;
                // OrderStep
                case OrderProcessingStarted:
                    orderStepStarted(dbLayer, (HistoryOrderStepBean) b);
                    break;
                case OrderProcessed:
                    toNotify.getSteps().add(orderStepProcessed(dbLayer, (HistoryOrderStepBean) b));
                    break;
                default:
                    break;
                }
                toRemove.add(b);
            }
            dbLayer.getSession().commit();

            Long firstEventId = copy.get(0).getEventId();
            toNotify.setLastEventId(copy.get(copy.size() - 1).getEventId());

            LOGGER.info(String.format("[%s][%s][%s-%s][UTC][%s-%s][%s]%s", serviceIdentifier, IDENTIFIER, firstEventId, toNotify.getLastEventId(),
                    eventIdAsTime(firstEventId), eventIdAsTime(toNotify.getLastEventId()), SOSDate.getDuration(Duration.between(start, Instant
                            .now())), toRemove.size()));
        } catch (Throwable e) {
            dbLayer.rollback();
            LOGGER.error(e.toString(), e);
        } finally {
            dbLayer.close();
            payloads.removeAll(toRemove);
            setLastActivityEnd();
        }
        return toNotify;
    }

    private ToNotify handleLongerThan(Long calculatedEventId) {
        // LOGGER.info("calculatedEventId=" + calculatedEventId + "(" + eventIdAsTime(calculatedEventId) + "), size=" + longerThan.size());

        ToNotify toNotify = new ToNotify();
        if (longerThan.size() == 0) {
            return toNotify;
        }
        if (calculatedEventId == null || calculatedEventId.equals(0L)) {
            return toNotify;
        }

        AJocClusterService.setLogger(serviceIdentifier);
        DBLayerMonitoring dbLayer = new DBLayerMonitoring(serviceIdentifier + "_" + IDENTIFIER, serviceIdentifier);
        try {
            setLastActivityStart();
            dbLayer.setSession(factory.openStatelessSession(dbLayer.getIdentifier()));

            Date endTime = getEventIdAsDate(calculatedEventId);
            Map<Long, HistoryOrderStepResult> w = new HashMap<>();
            longerThan.entrySet().stream().forEach(entry -> {
                HistoryOrderStepBean hosb = entry.getValue();
                Date stepEndTime = hosb.getEndTime() == null ? endTime : hosb.getEndTime();
                HistoryOrderStepResultWarn warn = analyzeLongerThan(dbLayer, hosb, hosb.getWarnIfLonger(), hosb.getStartTime(), stepEndTime, entry
                        .getKey(), false);
                if (warn != null) {
                    HistoryOrderStepResult r = new HistoryOrderStepResult(hosb, warn);
                    w.put(entry.getKey(), r);

                    try {
                        LOGGER.info(String.format(
                                "[%s][%s][handleLongerThan][UTC][start=%s,calculated current=%s][%s][step historyId=%s]orderId=%s,workflow=%s,job=%s",
                                serviceIdentifier, IDENTIFIER, SOSDate.getTimeAsString(hosb.getStartTime()), SOSDate.getTimeAsString(stepEndTime), r
                                        .getWarn().getText(), hosb.getHistoryId(), hosb.getOrderId(), hosb.getWorkflowPath(), hosb.getJobName()));
                    } catch (Throwable e) {
                        LOGGER.warn(e.toString(), e);
                    }
                }
            });
            if (w.size() == 0) {
                return toNotify;
            }

            dbLayer.getSession().beginTransaction();
            for (Map.Entry<Long, HistoryOrderStepResult> entry : w.entrySet()) {
                HistoryOrderStepResult sr = entry.getValue();

                if (tmpLogging) {
                    DBItemMonitoringOrderStep os = dbLayer.getMonitoringOrderStep(entry.getKey(), false);
                    LOGGER.info(String.format("    [tmp][%s][%s][handleLongerThan][tmp][1][entryKey=%s]%s", serviceIdentifier, IDENTIFIER, entry
                            .getKey(), SOSString.toString(os)));
                }

                int r = dbLayer.updateOrderStepOnLongerThan(entry.getKey(), sr.getWarn());

                if (tmpLogging) {
                    DBItemMonitoringOrderStep os = dbLayer.getMonitoringOrderStep(entry.getKey(), false);
                    LOGGER.info(String.format("    [tmp][%s][%s][handleLongerThan][tmp][2][entryKey=%s]%s", serviceIdentifier, IDENTIFIER, entry
                            .getKey(), SOSString.toString(os)));
                    LOGGER.info(String.format("    [tmp][%s][%s][handleLongerThan][tmp][3][r=%s][entryKey=%s]HistoryOrderStepResult step=%s,warn=%s",
                            serviceIdentifier, IDENTIFIER, r, entry.getKey(), SOSString.toString(sr.getStep()), SOSString.toString(sr.getWarn())));
                }

                if (longerThan.containsKey(entry.getKey())) {
                    longerThan.remove(entry.getKey());
                }
                if (r != 0) {
                    toNotify.getSteps().add(sr);
                }
            }
            dbLayer.getSession().commit();

            LOGGER.info(String.format("[%s][%s][handleLongerThan][processed=%s]toNotify steps=%s", serviceIdentifier, IDENTIFIER, w.size(), toNotify
                    .getSteps().size()));

        } catch (Throwable ex) {
            dbLayer.rollback();
            LOGGER.error(ex.toString(), ex);
        } finally {
            dbLayer.close();
            setLastActivityEnd();
        }
        return toNotify;
    }

    private void orderStarted(DBLayerMonitoring dbLayer, HistoryOrderBean hob) throws SOSHibernateException {

        DBItemMonitoringOrder item = dbLayer.getMonitoringOrder(hob.getHistoryId(), false);
        if (item != null) {
            return;
        }

        item = new DBItemMonitoringOrder();
        item.setHistoryId(hob.getHistoryId());
        item.setControllerId(hob.getControllerId());
        item.setOrderId(hob.getOrderId());

        item.setWorkflowPath(hob.getWorkflowPath());
        item.setWorkflowVersionId(hob.getWorkflowVersionId());
        item.setWorkflowPosition(hob.getWorkflowPosition());
        item.setWorkflowFolder(hob.getWorkflowFolder());
        item.setWorkflowName(hob.getWorkflowName());
        item.setWorkflowTitle(hob.getWorkflowTitle());

        item.setMainParentId(hob.getMainParentId());
        item.setParentId(hob.getParentId());
        item.setParentOrderId(hob.getParentOrderId());
        item.setHasChildren(false);
        item.setName(hob.getName());
        item.setCurrentHistoryOrderStepId(hob.getCurrentHistoryOrderStepId());

        item.setStartCause(hob.getStartCause());
        item.setStartTimeScheduled(hob.getStartTimeScheduled());
        item.setStartTime(hob.getStartTime());
        item.setStartWorkflowPosition(hob.getStartWorkflowPosition());
        item.setStartVariables(hob.getStartVariables());

        // item.setEndTime(hob.getEndTime());
        // item.setEndWorkflowPosition(hob.getEndWorkflowPosition());
        item.setEndHistoryOrderStepId(hob.getEndHistoryOrderStepId());

        item.setSeverity(hob.getSeverity());
        item.setState(hob.getState());
        item.setStateTime(hob.getStateTime());

        item.setError(false);

        item.setCreated(new Date());
        item.setModified(item.getCreated());

        try {
            dbLayer.getSession().save(item);
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                throw e;
            }
        }
    }

    private void orderResumed(DBLayerMonitoring dbLayer, HistoryOrderBean hob) throws SOSHibernateException {
        if (!dbLayer.updateOrderOnResumed(hob)) {
            insert(dbLayer, hob.getEventType(), hob.getOrderId(), hob.getHistoryId());
        }
    }

    private void orderForked(DBLayerMonitoring dbLayer, HistoryOrderBean hob) throws SOSHibernateException {
        if (!dbLayer.updateOrderOnForked(hob)) {
            insert(dbLayer, hob.getEventType(), hob.getOrderId(), hob.getHistoryId());
        }

        List<HistoryOrderBean> children = hob.getChildren();
        if (children == null) {
            return;
        }
        for (HistoryOrderBean child : children) {
            if (child == null) {
                continue;
            }
            orderStarted(dbLayer, child);
        }
    }

    private void orderJoined(DBLayerMonitoring dbLayer, HistoryOrderBean hob) throws SOSHibernateException {
        List<HistoryOrderBean> children = hob.getChildren();
        if (children == null) {
            return;
        }
        for (HistoryOrderBean child : children) {
            if (child == null) {
                continue;
            }
            if (!dbLayer.updateOrder(child)) {
                insert(dbLayer, hob.getEventType(), child.getOrderId(), child.getHistoryId());
            }
        }
    }

    private void orderFailed(DBLayerMonitoring dbLayer, HistoryOrderBean hob) throws SOSHibernateException {
        if (!dbLayer.updateOrder(hob)) {
            insert(dbLayer, hob.getEventType(), hob.getOrderId(), hob.getHistoryId());
        }
    }

    private void orderSuspended(DBLayerMonitoring dbLayer, HistoryOrderBean hob) throws SOSHibernateException {
        if (!dbLayer.updateOrder(hob)) {
            insert(dbLayer, hob.getEventType(), hob.getOrderId(), hob.getHistoryId());
        }
    }

    private void orderCancelled(DBLayerMonitoring dbLayer, HistoryOrderBean hob) throws SOSHibernateException {
        if (!dbLayer.updateOrder(hob)) {
            insert(dbLayer, hob.getEventType(), hob.getOrderId(), hob.getHistoryId());
        }
    }

    private void orderBroken(DBLayerMonitoring dbLayer, HistoryOrderBean hob) throws SOSHibernateException {
        if (!dbLayer.updateOrder(hob)) {
            insert(dbLayer, hob.getEventType(), hob.getOrderId(), hob.getHistoryId());
        }
    }

    private void orderFinished(DBLayerMonitoring dbLayer, HistoryOrderBean hob) throws SOSHibernateException {
        if (!dbLayer.updateOrder(hob)) {
            insert(dbLayer, hob.getEventType(), hob.getOrderId(), hob.getHistoryId());
        }
    }

    private void orderStepStarted(DBLayerMonitoring dbLayer, HistoryOrderStepBean hosb) throws SOSHibernateException {
        DBItemMonitoringOrderStep item = new DBItemMonitoringOrderStep();
        item.setHistoryId(hosb.getHistoryId());
        item.setWorkflowPosition(hosb.getWorkflowPosition());
        item.setHistoryOrderMainParentId(hosb.getHistoryOrderMainParentId());
        item.setHistoryOrderId(hosb.getHistoryOrderId());
        item.setPosition(hosb.getPosition());

        item.setJobName(hosb.getJobName());
        item.setJobLabel(hosb.getJobLabel());
        item.setJobTitle(hosb.getJobTitle());

        item.setJobCriticality(hosb.getCriticality());
        item.setJobNotification(hosb.getNotification());
        item.setSeverity(hosb.getSeverity());

        item.setAgentId(hosb.getAgentId());
        item.setAgentUri(hosb.getAgentUri());

        item.setStartCause(hosb.getStartCause());
        item.setStartTime(hosb.getStartTime());
        item.setStartVariables(hosb.getStartVariables());

        item.setError(false);
        item.setWarn(JobWarning.NONE);

        item.setCreated(new Date());
        item.setModified(item.getCreated());

        try {
            dbLayer.getSession().save(item);
            if (!dbLayer.updateOrderOnOrderStep(item.getHistoryOrderId(), item.getHistoryId())) {
                insert(dbLayer, hosb.getEventType(), hosb.getOrderId(), item.getHistoryOrderId());
            }
            if (!SOSString.isEmpty(hosb.getWarnIfLonger())) {
                longerThan.put(hosb.getHistoryId(), hosb);
            }
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                throw e;
            }
        }
    }

    private HistoryOrderStepResult orderStepProcessed(DBLayerMonitoring dbLayer, HistoryOrderStepBean hosb) throws SOSHibernateException {
        HistoryOrderStepResult r = analyzeExecutionTimeOnProcessed(dbLayer, hosb);
        dbLayer.setOrderStepEnd(r);

        if (tmpLogging) {
            if (r.getWarn() != null) {
                LOGGER.info(String.format("    [tmp][%s][%s][orderStepProcessed][on WARN]step=%s", serviceIdentifier, IDENTIFIER, SOSString.toString(r
                        .getStep())));
                LOGGER.info(String.format("            [on WARN]warn=%s", SOSString.toString(r.getWarn())));
            }
        }

        return r;
    }

    private HistoryOrderStepResult analyzeExecutionTimeOnProcessed(DBLayerMonitoring dbLayer, HistoryOrderStepBean hosb) {
        if (hosb.getStartTime() == null) {
            return new HistoryOrderStepResult(hosb, null);
        }

        HistoryOrderStepResultWarn warn = analyzeLongerThan(dbLayer, hosb, hosb.getWarnIfLonger(), hosb.getStartTime(), hosb.getEndTime(), hosb
                .getHistoryId(), true);
        if (warn == null) {
            warn = analyzeShorterThan(dbLayer, hosb, hosb.getWarnIfShorter(), hosb.getStartTime(), hosb.getEndTime());
        }
        return new HistoryOrderStepResult(hosb, warn);
    }

    private HistoryOrderStepResultWarn analyzeLongerThan(DBLayerMonitoring dbLayer, HistoryOrderStepBean hosb, String definition, Date startTime,
            Date endTime, Long historyId, boolean remove) {
        ExpectedSeconds expected = getExpectedSeconds(dbLayer, JobWarning.LONGER_THAN, hosb, definition);
        if (expected == null || expected.getSeconds() == null) {
            return null;
        }

        if (remove) {
            longerThan.remove(historyId);
        }

        Long diff = SOSDate.getSeconds(endTime) - SOSDate.getSeconds(startTime);
        if (diff < 0) {
            if (LOGGER.isDebugEnabled()) {
                try {
                    LOGGER.debug(String.format("[%s][%s][analyzeLongerThan][diff=%s < 0][startTime=%s, endTime=%s]%s", serviceIdentifier, IDENTIFIER,
                            diff, SOSDate.getDateTimeAsString(startTime), SOSDate.getDateTimeAsString(endTime), SOSString.toString(hosb)));
                } catch (Throwable e) {
                    LOGGER.warn(e.toString(), e);
                }
            }
            return null;
        }

        if (diff > expected.getSeconds()) {
            return new HistoryOrderStepResultWarn(JobWarning.LONGER_THAN, String.format("Job runs longer than the expected %s",
                    getExpectedDurationMessage(definition, expected)));
        } else {
            if (!remove) {// remove old entries
                if (diff > MAX_LONGER_THAN_SECONDS) {
                    longerThan.remove(historyId);
                }
            }
        }
        return null;
    }

    private HistoryOrderStepResultWarn analyzeShorterThan(DBLayerMonitoring dbLayer, HistoryOrderStepBean hosb, String definition, Date startTime,
            Date endTime) {
        ExpectedSeconds expected = getExpectedSeconds(dbLayer, JobWarning.SHORTER_THAN, hosb, definition);
        if (expected == null || expected.getSeconds() == null) {
            return null;
        }
        Long diff = SOSDate.getSeconds(endTime) - SOSDate.getSeconds(startTime);
        if (diff < 0) {
            if (LOGGER.isDebugEnabled()) {
                try {
                    LOGGER.debug(String.format("[%s][%s][analyzeShorterThan][diff=%s < 0][startTime=%s, endTime=%s]%s", serviceIdentifier, IDENTIFIER,
                            diff, SOSDate.getDateTimeAsString(startTime), SOSDate.getDateTimeAsString(endTime), SOSString.toString(hosb)));
                } catch (Throwable e) {
                    LOGGER.warn(e.toString(), e);
                }
            }
            return null;
        }

        if (diff < expected.getSeconds()) {
            return new HistoryOrderStepResultWarn(JobWarning.SHORTER_THAN, String.format("Job runs shorter than the expected %s",
                    getExpectedDurationMessage(definition, expected)));
        }
        return null;
    }

    private String getExpectedDurationMessage(String definition, ExpectedSeconds expected) {
        if (isPercentage(definition)) {
            String avg = expected.getAvg() == null ? "" : SOSDate.getDurationOfSeconds(expected.getAvg());
            return String.format("duration of %s (avg=%s, configured=%s)", SOSDate.getDurationOfSeconds(expected.getSeconds()), avg, definition);
        } else if (isTime(definition)) {
            return String.format("duration of %s (configured=%s)", SOSDate.getDurationOfSeconds(expected.getSeconds()), definition);
        }
        return String.format("duration of %s", SOSDate.getDurationOfSeconds(expected.getSeconds()));
    }

    private ExpectedSeconds getExpectedSeconds(DBLayerMonitoring dbLayer, JobWarning type, HistoryOrderStepBean hosb, String definition) {
        if (SOSString.isEmpty(definition)) {
            return new ExpectedSeconds(null, null);
        }
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        Long seconds = null;
        Long avg = null;
        if (isPercentage(definition)) {
            try {
                int percentage = Integer.parseInt(definition.substring(0, definition.length() - 1));
                if (percentage != 0) {
                    avg = dbLayer.getJobAvg(hosb.getControllerId(), hosb.getWorkflowPath(), hosb.getJobName());
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][%s][workflowPath=%s,job=%s][%s definition=%s]avg=%s", serviceIdentifier, IDENTIFIER, hosb
                                .getControllerId(), hosb.getWorkflowPath(), hosb.getJobName(), type, definition, avg));
                    }
                    if (avg != null) {// job found
                        seconds = new BigDecimal(percentage / 100 * avg).setScale(0, RoundingMode.HALF_UP).longValue();
                    }
                }
            } catch (SOSHibernateException e) {
                LOGGER.error(String.format("[%s][%s][%s][workflowPath=%s,job=%s][%s definition=%s][error on get jobAvg]%s", serviceIdentifier,
                        IDENTIFIER, hosb.getControllerId(), hosb.getWorkflowPath(), hosb.getJobName(), type, definition, e.toString()), e);
            }
        } else if (isSeconds(definition)) {
            seconds = Long.parseLong(definition.substring(0, definition.length() - 1));
        } else if (isTime(definition)) {
            seconds = SOSDate.getTimeAsSeconds(definition);
        } else {
            seconds = Long.parseLong(definition);
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s][%s][workflowPath=%s,job=%s][%s definition=%s]seconds=%s", serviceIdentifier, IDENTIFIER, hosb
                    .getControllerId(), hosb.getWorkflowPath(), hosb.getJobName(), type, definition, seconds));
        }
        return new ExpectedSeconds(seconds, avg);
    }

    private boolean isPercentage(String definition) {
        return definition.endsWith("%");
    }

    private boolean isSeconds(String definition) {
        return definition.toLowerCase().endsWith("s");
    }

    private boolean isTime(String definition) {
        return definition.contains(":");
    }

    private boolean insert(DBLayerMonitoring dbLayer, EventType eventType, String orderId, Long historyId) {
        try {
            LOGGER.info(String.format("[%s][%s][%s][order not found=%s, id=%s]read from history orders...", serviceIdentifier, IDENTIFIER, eventType
                    .name(), orderId, historyId));

            DBItemMonitoringOrder item = dbLayer.convert(dbLayer.getHistoryOrder(historyId));
            if (item != null) {
                try {
                    dbLayer.getSession().save(item);
                    return true;
                } catch (Throwable e) {
                    LOGGER.error(String.format("[%s][save]%s", historyId, e.toString()), e);
                }
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][get]%s", historyId, e.toString()), e);
        }
        return false;
    }

    private void serialize() {
        int payloadsSize = payloads.size();
        int longerThanSize = longerThan.size();
        if (payloadsSize > 0 || longerThanSize > 0) {
            try {
                saveJocVariable(new SOSSerializer<SerializedHistoryResult>().serializeCompressed2bytes(new SerializedHistoryResult(payloads,
                        longerThan)));
                LOGGER.info(String.format("[%s][%s][serialized]payloads=%s,longerThan=%s", serviceIdentifier, IDENTIFIER, payloadsSize,
                        longerThanSize));
            } catch (Throwable e) {
                LOGGER.error(String.format("[%s][%s][serialize]%s", serviceIdentifier, IDENTIFIER, e.toString()), e);
            }
            payloads.clear();
            longerThan.clear();
        } else {
            deleteJocVariable();
        }
    }

    private void deserialize() {
        DBItemJocVariable var = null;
        try {
            var = getJocVariable();
            if (var == null) {
                return;
            }
            deserialize(var);
            deleteJocVariable();
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][%s][deserialize]%s", serviceIdentifier, IDENTIFIER, e.toString()), e);
        }
    }

    private void deserialize(DBItemJocVariable var) throws Exception {
        int payloadsSize = 0;
        int longerThanSize = 0;

        SerializedHistoryResult sr = new SOSSerializer<SerializedHistoryResult>().deserializeCompressed(var.getBinaryValue());
        if (sr.getPayloads() != null) {
            payloadsSize = sr.getPayloads().size();
            // payloads on start is maybe not empty (because event subscription)
            payloads.addAll(sr.getPayloads());
        }
        if (sr.getLongerThan() != null) {
            longerThanSize = sr.getLongerThan().size();
            // longerThan on start is empty ... ?
            longerThan.putAll(sr.getLongerThan());
        }
        LOGGER.info(String.format("[%s][%s][deserialized]payloads=%s,longerThan=%s", serviceIdentifier, IDENTIFIER, payloadsSize, longerThanSize));
    }

    private DBItemJocVariable getJocVariable() throws Exception {
        DBLayerMonitoring dbLayer = new DBLayerMonitoring(serviceIdentifier + "_" + IDENTIFIER, serviceIdentifier);
        try {
            dbLayer.setSession(factory.openStatelessSession(dbLayer.getIdentifier()));
            return dbLayer.getVariable();
        } catch (Exception e) {
            throw e;
        } finally {
            dbLayer.close();
        }
    }

    private void saveJocVariable(byte[] val) throws Exception {
        DBLayerMonitoring dbLayer = new DBLayerMonitoring(serviceIdentifier + "_" + IDENTIFIER, serviceIdentifier);
        try {
            dbLayer.setSession(factory.openStatelessSession(dbLayer.getIdentifier()));
            dbLayer.getSession().beginTransaction();
            dbLayer.saveVariable(val);
            dbLayer.getSession().commit();
        } catch (Exception e) {
            dbLayer.rollback();
            throw e;
        } finally {
            dbLayer.close();
        }
    }

    private void deleteJocVariable() {
        DBLayerMonitoring dbLayer = new DBLayerMonitoring(serviceIdentifier + "_" + IDENTIFIER, serviceIdentifier);
        try {
            dbLayer.setSession(factory.openStatelessSession(dbLayer.getIdentifier()));
            dbLayer.getSession().beginTransaction();
            dbLayer.deleteVariable();
            dbLayer.getSession().commit();
        } catch (Exception e) {
            dbLayer.rollback();
            LOGGER.error(e.toString(), e);
        } finally {
            dbLayer.close();
        }
    }

    private synchronized void setConfiguration() {
        DBLayerMonitoring dbLayer = new DBLayerMonitoring(serviceIdentifier + "_" + IDENTIFIER, serviceIdentifier);
        try {
            AJocClusterService.setLogger(serviceIdentifier);

            dbLayer.setSession(factory.openStatelessSession(dbLayer.getIdentifier()));
            String configXml = dbLayer.getReleasedConfiguration();

            if (configuration == null) {
                configuration = new Configuration(jocConfiguration.getUri());
            }
            configuration.process(configXml);
            if (configuration.exists()) {
                List<String> names = handleMailResources(dbLayer, configuration);

                LOGGER.info(String.format("[%s][%s][configuration][type %s=%s, %s=%s, %s=%s][job_resources %s]", serviceIdentifier,
                        NOTIFICATION_IDENTIFIER, NotificationType.ERROR.name(), configuration.getOnError().size(), NotificationType.WARNING.name(),
                        configuration.getOnWarning().size(), NotificationType.SUCCESS.name(), configuration.getOnSuccess().size(), String.join(", ",
                                names)));
            } else {
                LOGGER.info(String.format("[%s][%s][configuration]exists=false", serviceIdentifier, NOTIFICATION_IDENTIFIER));
            }
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        } finally {
            dbLayer.close();
        }
    }

    private List<String> handleMailResources(DBLayerMonitoring dbLayer, Configuration conf) throws Exception {
        if (conf.getMailResources() == null || conf.getMailResources().size() == 0) {
            return new ArrayList<>();
        }

        List<String> names = conf.getMailResources().entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
        // name,content
        List<Object[]> resources = dbLayer.getDeployedJobResources(names);
        if (resources != null) {
            for (Object[] r : resources) {
                String name = r[0].toString();

                MailResource mr = conf.getMailResources().get(name);
                mr.parse(name, r[1].toString());
                conf.getMailResources().put(name, mr);
            }
            if (resources.size() != conf.getMailResources().size()) {// some configured resources were not found in the database
                List<String> toRemove = conf.getMailResources().entrySet().stream().filter(e -> {
                    return e.getValue().getMailProperties() == null;
                }).map(Map.Entry::getKey).collect(Collectors.toList());

                if (toRemove.size() > 0) {
                    LOGGER.warn(String.format("[Job Resource=%s]configured Job Resource not found in the deployment history", String.join(",",
                            toRemove)));

                    for (String name : toRemove) {
                        conf.getMailResources().remove(name);
                    }
                }
            }
        }
        return names;
    }

    // TODO duplicate HistoryUtil.eventIdAsTime
    private String eventIdAsTime(Long eventId) {
        return eventId.equals(Long.valueOf(0)) ? "0" : SOSDate.getTimeAsString(eventId2Instant(eventId));
    }

    // TODO duplicate HistoryUtil.eventId2Instant
    private static Instant eventId2Instant(Long eventId) {
        return Instant.ofEpochMilli(eventId / 1000);
    }

    // TODO duplicate HistoryUtil.getEventIdAsDate
    private static Date getEventIdAsDate(Long eventId) {
        return eventId == null ? null : Date.from(eventId2Instant(eventId));
    }

    private void setLastActivityStart() {
        lastActivityStart.set(new Date().getTime());
    }

    private void setLastActivityEnd() {
        lastActivityEnd.set(new Date().getTime());
    }

    public AtomicLong getLastActivityStart() {
        return lastActivityStart;
    }

    public AtomicLong getLastActivityEnd() {
        return lastActivityEnd;
    }

    protected class ToNotify {

        private final List<HistoryOrderStepResult> steps;
        private final List<HistoryOrderBean> errorOrders;
        private final List<HistoryOrderBean> successOrders;
        private Long lastEventId;

        protected ToNotify() {
            steps = new ArrayList<>();
            errorOrders = new ArrayList<>();
            successOrders = new ArrayList<>();
        }

        protected List<HistoryOrderStepResult> getSteps() {
            return steps;
        }

        protected List<HistoryOrderBean> getErrorOrders() {
            return errorOrders;
        }

        protected List<HistoryOrderBean> getSuccessOrders() {
            return successOrders;
        }

        protected void setLastEventId(Long val) {
            lastEventId = val;
        }

        protected Long getLastEventId() {
            return lastEventId;
        }
    }

    public class HistoryOrderStepResult {

        private final HistoryOrderStepBean step;
        private final HistoryOrderStepResultWarn warn;

        public HistoryOrderStepResult(HistoryOrderStepBean step, HistoryOrderStepResultWarn warn) {
            this.step = step;
            this.warn = warn;
        }

        public HistoryOrderStepBean getStep() {
            return step;
        }

        public HistoryOrderStepResultWarn getWarn() {
            return warn;
        }
    }

    public class HistoryOrderStepResultWarn {

        private final JobWarning reason;
        private final String text;

        public HistoryOrderStepResultWarn(JobWarning reason, String text) {
            this.reason = reason;
            this.text = text;
        }

        public JobWarning getReason() {
            return reason;
        }

        public String getText() {
            return text;
        }
    }

    private class ExpectedSeconds {

        final Long seconds;
        final Long avg;

        private ExpectedSeconds(Long seconds, Long avg) {
            this.seconds = seconds;
            this.avg = avg;
        }

        private Long getSeconds() {
            return seconds;
        }

        private Long getAvg() {
            return avg;
        }
    }
}
