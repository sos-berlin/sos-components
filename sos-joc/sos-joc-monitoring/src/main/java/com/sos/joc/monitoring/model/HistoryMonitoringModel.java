package com.sos.joc.monitoring.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
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
    private static final int THREAD_POOL_CORE_POOL_SIZE = 1;
    /** 1day */
    private static final int MAX_LONGER_THAN_SECONDS = 24 * 60 * 60;

    private final SOSHibernateFactory factory;
    private final JocConfiguration jocConfiguration;
    private final DBLayerMonitoring dbLayer;
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

    // TODO ? commit after n db operations
    // private int maxTransactions = 100;

    protected HistoryMonitoringModel() {// only for Test
        this.factory = null;
        this.jocConfiguration = null;
        this.dbLayer = null;
        this.notifier = null;
        this.serviceIdentifier = "";
    }

    public HistoryMonitoringModel(ThreadGroup threadGroup, SOSHibernateFactory factory, JocConfiguration jocConfiguration, String serviceIdentifier) {
        this.factory = factory;
        this.jocConfiguration = jocConfiguration;
        this.dbLayer = new DBLayerMonitoring(serviceIdentifier + "_" + IDENTIFIER, serviceIdentifier);
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

            @Override
            public void run() {
                AJocClusterService.setLogger(serviceIdentifier);

                boolean isDebugEnabled = LOGGER.isDebugEnabled();
                ToNotify toNotifyPayloads = handlePayloads(isDebugEnabled);
                ToNotify toNotifyLongerThan = handleLongerThan();

                notifier.notify(configuration, toNotifyPayloads, toNotifyLongerThan);
            }
        }, 0 /* start delay */, 2 /* duration */, TimeUnit.SECONDS);

    }

    private ToNotify handlePayloads(boolean isDebugEnabled) {
        ToNotify toNotify = new ToNotify();
        if (payloads.size() == 0) {
            return toNotify;
        }

        setLastActivityStart();
        List<AHistoryBean> toRemove = new ArrayList<>();
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
                    orderStarted((HistoryOrderBean) b);
                    break;
                case OrderResumed:
                    orderResumed((HistoryOrderBean) b);
                    break;
                case OrderForked:
                    orderForked((HistoryOrderBean) b);
                    break;
                case OrderJoined:
                    orderJoined((HistoryOrderBean) b);
                    break;
                case OrderFailed:
                    hob = (HistoryOrderBean) b;
                    orderFailed(hob);
                    toNotify.getErrorOrders().add(hob);
                    break;
                case OrderSuspended:
                    orderSuspended((HistoryOrderBean) b);
                    break;
                case OrderCancelled:
                    orderCancelled((HistoryOrderBean) b);
                    break;
                case OrderBroken:
                    hob = (HistoryOrderBean) b;
                    orderBroken(hob);
                    toNotify.getErrorOrders().add(hob);
                    break;
                case OrderFinished:
                    hob = (HistoryOrderBean) b;
                    orderFinished(hob);
                    toNotify.getSuccessOrders().add(hob);
                    break;
                // OrderStep
                case OrderProcessingStarted:
                    orderStepStarted((HistoryOrderStepBean) b);
                    break;
                case OrderProcessed:
                    toNotify.getSteps().add(orderStepProcessed((HistoryOrderStepBean) b));
                    break;
                default:
                    break;
                }
                toRemove.add(b);
            }
            dbLayer.getSession().commit();

            LOGGER.info(String.format("[%s][%s][processed][%s]%s", serviceIdentifier, IDENTIFIER, SOSDate.getDuration(Duration.between(start, Instant
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

    private ToNotify handleLongerThan() {
        ToNotify toNotify = new ToNotify();
        if (longerThan.size() == 0) {
            return toNotify;
        }

        AJocClusterService.setLogger(serviceIdentifier);
        Map<Long, HistoryOrderStepResult> w = new HashMap<>();
        longerThan.entrySet().stream().forEach(entry -> {
            HistoryOrderStepBean hosb = entry.getValue();
            HistoryOrderStepResultWarn warn = analyzeLongerThan(hosb, hosb.getWarnIfLonger(), hosb.getStartTime(), new Date(), entry.getKey(), false);
            if (warn != null) {
                HistoryOrderStepResult r = new HistoryOrderStepResult(hosb, warn);
                w.put(entry.getKey(), r);
            }
        });
        if (w.size() == 0) {
            return toNotify;
        }

        try {
            setLastActivityStart();
            dbLayer.setSession(factory.openStatelessSession(dbLayer.getIdentifier()));
            dbLayer.getSession().beginTransaction();

            for (Map.Entry<Long, HistoryOrderStepResult> entry : w.entrySet()) {
                HistoryOrderStepResult sr = entry.getValue();
                int r = dbLayer.updateOrderStepOnLongerThan(entry.getKey(), sr.getWarn());
                if (longerThan.containsKey(entry.getKey())) {
                    longerThan.remove(entry.getKey());
                }
                if (r != 0) {
                    toNotify.getSteps().add(sr);
                }
            }
            dbLayer.getSession().commit();
            LOGGER.info(String.format("[%s][%s][longerThan][processed]%s", serviceIdentifier, IDENTIFIER, w.size()));
        } catch (Throwable ex) {
            dbLayer.rollback();
            LOGGER.error(ex.toString(), ex);
        } finally {
            dbLayer.close();
            setLastActivityEnd();
        }
        return toNotify;
    }

    private void orderStarted(HistoryOrderBean hob) throws SOSHibernateException {

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

    private void orderResumed(HistoryOrderBean hob) throws SOSHibernateException {
        if (!dbLayer.updateOrderOnResumed(hob)) {
            insert(hob.getEventType(), hob.getOrderId(), hob.getHistoryId());
        }
    }

    private void orderForked(HistoryOrderBean hob) throws SOSHibernateException {
        if (!dbLayer.updateOrderOnForked(hob)) {
            insert(hob.getEventType(), hob.getOrderId(), hob.getHistoryId());
        }

        List<HistoryOrderBean> children = hob.getChildren();
        if (children == null) {
            return;
        }
        for (HistoryOrderBean child : children) {
            if (child == null) {
                continue;
            }
            orderStarted(child);
        }
    }

    private void orderJoined(HistoryOrderBean hob) throws SOSHibernateException {
        List<HistoryOrderBean> children = hob.getChildren();
        if (children == null) {
            return;
        }
        for (HistoryOrderBean child : children) {
            if (child == null) {
                continue;
            }
            if (!dbLayer.updateOrder(child)) {
                insert(hob.getEventType(), child.getOrderId(), child.getHistoryId());
            }
        }
    }

    private void orderFailed(HistoryOrderBean hob) throws SOSHibernateException {
        if (!dbLayer.updateOrder(hob)) {
            insert(hob.getEventType(), hob.getOrderId(), hob.getHistoryId());
        }
    }

    private void orderSuspended(HistoryOrderBean hob) throws SOSHibernateException {
        if (!dbLayer.updateOrder(hob)) {
            insert(hob.getEventType(), hob.getOrderId(), hob.getHistoryId());
        }
    }

    private void orderCancelled(HistoryOrderBean hob) throws SOSHibernateException {
        if (!dbLayer.updateOrder(hob)) {
            insert(hob.getEventType(), hob.getOrderId(), hob.getHistoryId());
        }
    }

    private void orderBroken(HistoryOrderBean hob) throws SOSHibernateException {
        if (!dbLayer.updateOrder(hob)) {
            insert(hob.getEventType(), hob.getOrderId(), hob.getHistoryId());
        }
    }

    private void orderFinished(HistoryOrderBean hob) throws SOSHibernateException {
        if (!dbLayer.updateOrder(hob)) {
            insert(hob.getEventType(), hob.getOrderId(), hob.getHistoryId());
        }
    }

    private void orderStepStarted(HistoryOrderStepBean hosb) throws SOSHibernateException {
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
                insert(hosb.getEventType(), hosb.getOrderId(), item.getHistoryOrderId());
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

    private HistoryOrderStepResult orderStepProcessed(HistoryOrderStepBean hosb) throws SOSHibernateException {
        HistoryOrderStepResult r = analyzeExecutionTimeOnProcessed(hosb);
        dbLayer.setOrderStepEnd(r);
        return r;
    }

    private HistoryOrderStepResult analyzeExecutionTimeOnProcessed(HistoryOrderStepBean hosb) {
        if (hosb.getStartTime() == null) {
            return new HistoryOrderStepResult(hosb, null);
        }

        HistoryOrderStepResultWarn warn = analyzeLongerThan(hosb, hosb.getWarnIfLonger(), hosb.getStartTime(), hosb.getEndTime(), hosb.getHistoryId(),
                true);
        if (warn == null) {
            warn = analyzeShorterThan(hosb, hosb.getWarnIfShorter(), hosb.getStartTime(), hosb.getEndTime());
        }
        return new HistoryOrderStepResult(hosb, warn);
    }

    private HistoryOrderStepResultWarn analyzeLongerThan(HistoryOrderStepBean hosb, String definition, Date startTime, Date endDate, Long historyId,
            boolean remove) {
        ExpectedSeconds expected = getExpectedSeconds(JobWarning.LONGER_THAN, hosb, definition);
        if (expected == null || expected.getSeconds() == null) {
            return null;
        }

        if (remove) {
            longerThan.remove(historyId);
        }

        Long diff = SOSDate.getSeconds(endDate) - SOSDate.getSeconds(startTime);
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

    private HistoryOrderStepResultWarn analyzeShorterThan(HistoryOrderStepBean hosb, String definition, Date startTime, Date endDate) {
        ExpectedSeconds expected = getExpectedSeconds(JobWarning.SHORTER_THAN, hosb, definition);
        if (expected == null || expected.getSeconds() == null) {
            return null;
        }
        Long diff = SOSDate.getSeconds(endDate) - SOSDate.getSeconds(startTime);
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

    private ExpectedSeconds getExpectedSeconds(JobWarning type, HistoryOrderStepBean hosb, String definition) {
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

    private boolean insert(EventType eventType, String orderId, Long historyId) {
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
                saveJocVariable(new SOSSerializer<SerializedResult>().serializeCompressed2bytes(new SerializedResult(payloads, longerThan)));
                LOGGER.info(String.format("[%s][%s][serialized]payloads=%s,longerThan=%s", serviceIdentifier, IDENTIFIER, payloadsSize,
                        longerThanSize));
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
            payloads.clear();
            longerThan.clear();
        } else {
            deleteJocVariable();
        }
    }

    private void deserialize() {
        int payloadsSize = 0;
        int longerThanSize = 0;
        DBItemJocVariable var = null;
        try {
            var = getJocVariable();
            if (var == null) {
                return;
            }
            deserialize(var, payloadsSize, longerThanSize);
        } catch (Throwable e) {
            if (var == null) {
                LOGGER.error(e.toString(), e);
                return;
            }
            LOGGER.error(e.toString(), e);
            try {
                tryDeserializeVersion1Result(var, payloadsSize, longerThanSize);
            } catch (Exception e1) {
                LOGGER.error(e1.toString(), e1);
            }
        }
        LOGGER.info(String.format("[%s][%s][deserialized]payloads=%s,longerThan=%s", serviceIdentifier, IDENTIFIER, payloadsSize, longerThanSize));
    }

    private void deserialize(DBItemJocVariable var, int payloadsSize, int longerThanSize) throws Exception {
        SerializedResult sr = new SOSSerializer<SerializedResult>().deserializeCompressed(var.getBinaryValue());
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
    }

    // TODO deserialize problem because the SerializedResult(HistoryOrderStepBean) object was changed between JS7 versions ...
    private void tryDeserializeVersion1Result(DBItemJocVariable var, int payloadsSize, int longerThanSize) throws Exception {

    }

    private DBItemJocVariable getJocVariable() throws Exception {
        try {
            dbLayer.setSession(factory.openStatelessSession(dbLayer.getIdentifier()));
            dbLayer.getSession().beginTransaction();
            DBItemJocVariable item = dbLayer.getVariable();
            dbLayer.getSession().commit();
            return item;
        } catch (Exception e) {
            dbLayer.rollback();
            throw e;
        } finally {
            dbLayer.close();
        }
    }

    private void saveJocVariable(byte[] val) throws Exception {
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
        DBLayerMonitoring dbLayer = new DBLayerMonitoring(this.dbLayer.getIdentifier(), this.dbLayer.getJocVariableName());
        try {
            AJocClusterService.setLogger(serviceIdentifier);

            dbLayer.setSession(factory.openStatelessSession(dbLayer.getIdentifier()));
            dbLayer.getSession().beginTransaction();
            String configXml = dbLayer.getReleasedConfiguration();
            dbLayer.getSession().commit();

            if (configuration == null) {
                configuration = new Configuration(jocConfiguration.getUri());
            }
            configuration.process(configXml);
            if (configuration.exists()) {
                List<String> names = handleMailResources(configuration);

                LOGGER.info(String.format("[%s][%s][configuration][type %s=%s, %s=%s, %s=%s][job_resources %s]", serviceIdentifier,
                        NOTIFICATION_IDENTIFIER, NotificationType.ERROR.name(), configuration.getOnError().size(), NotificationType.WARNING.name(),
                        configuration.getOnWarning().size(), NotificationType.SUCCESS.name(), configuration.getOnSuccess().size(), String.join(", ",
                                names)));

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("MailResources=" + SOSString.mapToString(configuration.getMailResources(), true));
                }

            } else {
                LOGGER.info(String.format("[%s][%s][configuration]exists=false", serviceIdentifier, NOTIFICATION_IDENTIFIER));
            }
        } catch (Exception e) {
            dbLayer.rollback();
            LOGGER.error(e.toString(), e);
        } finally {
            dbLayer.close();
        }
    }

    private List<String> handleMailResources(Configuration conf) throws Exception {
        if (conf.getMailResources() == null || conf.getMailResources().size() == 0) {
            return new ArrayList<>();
        }

        dbLayer.setSession(factory.openStatelessSession(dbLayer.getIdentifier()));
        dbLayer.getSession().beginTransaction();
        List<String> names = conf.getMailResources().entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
        // name,content
        List<Object[]> resources = dbLayer.getDeployedJobResources(names);
        dbLayer.getSession().commit();

        if (resources != null) {
            for (Object[] r : resources) {
                String name = r[0].toString();

                MailResource mr = conf.getMailResources().get(name);
                mr.parse(r[1].toString());
                conf.getMailResources().put(name, mr);
            }
        }
        return names;
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

    protected class SerializedResult implements Serializable {

        private static final long serialVersionUID = 1L;

        private final Collection<AHistoryBean> payloads;
        private final Map<Long, HistoryOrderStepBean> longerThan;

        protected SerializedResult(Collection<AHistoryBean> payloads, Map<Long, HistoryOrderStepBean> longerThan) {
            this.payloads = payloads;
            this.longerThan = longerThan;
        }

        protected Collection<AHistoryBean> getPayloads() {
            return payloads;
        }

        protected Map<Long, HistoryOrderStepBean> getLongerThan() {
            return longerThan;
        }
    }

    protected class ToNotify {

        private final List<HistoryOrderStepResult> steps;
        private final List<HistoryOrderBean> errorOrders;
        private final List<HistoryOrderBean> successOrders;

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
