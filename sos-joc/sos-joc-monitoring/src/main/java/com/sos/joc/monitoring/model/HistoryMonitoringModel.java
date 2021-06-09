package com.sos.joc.monitoring.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateObjectOperationException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSSerializer;
import com.sos.commons.util.SOSString;
import com.sos.history.JobWarning;
import com.sos.joc.cluster.AJocClusterService;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.bean.history.AHistoryBean;
import com.sos.joc.cluster.bean.history.HistoryOrderBean;
import com.sos.joc.cluster.bean.history.HistoryOrderStepBean;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.db.joc.DBItemJocVariable;
import com.sos.joc.db.monitoring.DBItemMonitoringOrder;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.history.HistoryEvent;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.joc.monitoring.db.DBLayerMonitoring;

public class HistoryMonitoringModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryMonitoringModel.class);

    private static final String IDENTIFIER = ClusterServices.history.name();

    private final SOSHibernateFactory factory;
    private final DBLayerMonitoring dbLayer;
    private final String serviceIdentifier;

    private ScheduledExecutorService threadPool;
    private CopyOnWriteArraySet<AHistoryBean> payloads = new CopyOnWriteArraySet<>();
    private ConcurrentHashMap<Long, Date> longerThan = new ConcurrentHashMap<>();
    private AtomicLong lastActivityStart = new AtomicLong();
    private AtomicLong lastActivityEnd = new AtomicLong();
    private AtomicBoolean closed = new AtomicBoolean();

    // TODO ? commit after n db operations
    // private int maxTransactions = 100;

    public HistoryMonitoringModel(SOSHibernateFactory factory, String serviceIdentifier) {
        this.factory = factory;
        this.dbLayer = new DBLayerMonitoring(serviceIdentifier + "_" + IDENTIFIER, serviceIdentifier);
        this.serviceIdentifier = serviceIdentifier;
        EventBus.getInstance().register(this);
    }

    @Subscribe({ HistoryEvent.class })
    public void handleHistoryEvents(HistoryEvent evt) {
        if (evt.getPayload() != null) {
            payloads.add((AHistoryBean) evt.getPayload());
        }
    }

    public void start(ThreadGroup threadGroup) {
        closed.set(false);

        deserialize();
        schedule(threadGroup);

        AJocClusterService.setLogger(serviceIdentifier);
        LOGGER.info(String.format("[%s][%s]start..", serviceIdentifier, IDENTIFIER));
    }

    public void close() {
        closed.set(true);

        if (threadPool != null) {
            AJocClusterService.setLogger(serviceIdentifier);
            JocCluster.shutdownThreadPool(StartupMode.automatic, threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);
            threadPool = null;
            serialize();
        }
    }

    private void schedule(ThreadGroup threadGroup) {
        this.threadPool = Executors.newScheduledThreadPool(1, new JocClusterThreadFactory(threadGroup, serviceIdentifier + "-h"));
        this.threadPool.scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                if (payloads.size() > 0) {
                    AJocClusterService.setLogger(serviceIdentifier);

                    setLastActivityStart();
                    try {
                        List<AHistoryBean> l = new ArrayList<>();
                        boolean isDebugEnabled = LOGGER.isDebugEnabled();

                        dbLayer.setSession(factory.openStatelessSession(dbLayer.getIdentifier()));
                        dbLayer.getSession().beginTransaction();
                        for (AHistoryBean b : payloads) {
                            if (isDebugEnabled) {
                                LOGGER.debug(b.getEventType() + "=" + SOSString.toString(b));
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
                                orderFailed(dbLayer, (HistoryOrderBean) b);
                                break;
                            case OrderSuspended:
                                orderSuspended(dbLayer, (HistoryOrderBean) b);
                                break;
                            case OrderCancelled:
                                orderCancelled(dbLayer, (HistoryOrderBean) b);
                                break;
                            case OrderBroken:
                                orderBroken(dbLayer, (HistoryOrderBean) b);
                                break;
                            case OrderFinished:
                                orderFinished(dbLayer, (HistoryOrderBean) b);
                                break;
                            // OrderStep
                            case OrderProcessingStarted:
                                orderStepStarted(dbLayer, (HistoryOrderStepBean) b);
                                break;
                            case OrderProcessed:
                                orderStepProcessed(dbLayer, (HistoryOrderStepBean) b);
                                break;
                            default:
                                break;
                            }
                            l.add(b);
                        }
                        dbLayer.getSession().commit();

                        LOGGER.info(String.format("[%s][%s][processed]%s", serviceIdentifier, IDENTIFIER, l.size()));
                        payloads.removeAll(l);
                    } catch (Throwable e) {
                        dbLayer.rollback();
                        LOGGER.error(e.toString(), e);
                    } finally {
                        dbLayer.close();
                        setLastActivityEnd();
                    }
                }
            }
        }, 0 /* start delay */, 2 /* duration */, TimeUnit.SECONDS);
    }

    private void orderStarted(DBLayerMonitoring dbLayer, HistoryOrderBean hob) throws SOSHibernateException {
        DBItemMonitoringOrder item = new DBItemMonitoringOrder();
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
        item.setTitle(hob.getTitle());
        item.setCurrentHistoryOrderStepId(hob.getCurrentHistoryOrderStepId());

        item.setStartCause(hob.getStartCause());
        item.setStartTimePlanned(hob.getStartTimePlanned());
        item.setStartTime(hob.getStartTime());
        item.setStartWorkflowPosition(hob.getStartWorkflowPosition());
        item.setStartParameters(hob.getStartParameters());

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
            insert(dbLayer, hob.getOrderId(), hob.getHistoryId());
        }
    }

    private void orderForked(DBLayerMonitoring dbLayer, HistoryOrderBean hob) throws SOSHibernateException {
        if (!dbLayer.updateOrderOnForked(hob)) {
            insert(dbLayer, hob.getOrderId(), hob.getHistoryId());
        }

        for (HistoryOrderBean child : hob.getChildren()) {
            orderStarted(dbLayer, child);
        }
    }

    private void orderJoined(DBLayerMonitoring dbLayer, HistoryOrderBean hob) throws SOSHibernateException {
        for (HistoryOrderBean child : hob.getChildren()) {
            if (!dbLayer.updateOrder(child)) {
                insert(dbLayer, child.getOrderId(), child.getHistoryId());
            }
        }
    }

    private void orderFailed(DBLayerMonitoring dbLayer, HistoryOrderBean hob) throws SOSHibernateException {
        if (!dbLayer.updateOrder(hob)) {
            insert(dbLayer, hob.getOrderId(), hob.getHistoryId());
        }
    }

    private void orderSuspended(DBLayerMonitoring dbLayer, HistoryOrderBean hob) throws SOSHibernateException {
        if (!dbLayer.updateOrder(hob)) {
            insert(dbLayer, hob.getOrderId(), hob.getHistoryId());
        }
    }

    private void orderCancelled(DBLayerMonitoring dbLayer, HistoryOrderBean hob) throws SOSHibernateException {
        if (!dbLayer.updateOrder(hob)) {
            insert(dbLayer, hob.getOrderId(), hob.getHistoryId());
        }
    }

    private void orderBroken(DBLayerMonitoring dbLayer, HistoryOrderBean hob) throws SOSHibernateException {
        if (!dbLayer.updateOrder(hob)) {
            insert(dbLayer, hob.getOrderId(), hob.getHistoryId());
        }
    }

    private void orderFinished(DBLayerMonitoring dbLayer, HistoryOrderBean hob) throws SOSHibernateException {
        if (!dbLayer.updateOrder(hob)) {
            insert(dbLayer, hob.getOrderId(), hob.getHistoryId());
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

        item.setCriticality(hosb.getCriticality());
        item.setSeverity(hosb.getSeverity());

        item.setAgentId(hosb.getAgentId());
        item.setAgentUri(hosb.getAgentUri());

        item.setStartCause(hosb.getStartCause());
        item.setStartTime(hosb.getStartTime());
        item.setStartParameters(hosb.getStartParameters());

        item.setError(false);
        item.setWarn(JobWarning.NONE);

        item.setCreated(new Date());
        item.setModified(item.getCreated());

        try {
            dbLayer.getSession().save(item);
            if (!dbLayer.updateOrderOnOrderStep(item.getHistoryOrderId(), item.getHistoryId())) {
                insert(dbLayer, hosb.getOrderId(), item.getHistoryOrderId());
            }
            handleLongerThan(hosb);
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                throw e;
            }
        }
    }

    private void handleLongerThan(HistoryOrderStepBean hosb) {
        if (!SOSString.isEmpty(hosb.getTaskIfLongerThan())) {
            longerThan.put(hosb.getHistoryId(), hosb.getStartTime());
        }
    }

    private void orderStepProcessed(DBLayerMonitoring dbLayer, HistoryOrderStepBean hosb) throws SOSHibernateException {
        dbLayer.setOrderStepEnd(analyzeExecutionTime(hosb));
    }

    private HistoryOrderStepResult analyzeExecutionTime(HistoryOrderStepBean hosb) {
        if (hosb.getStartTime() == null) {
            return new HistoryOrderStepResult(hosb, null);
        }

        HistoryOrderStepResultWarn warn = longerThan(hosb.getHistoryId(), hosb.getStartTime(), hosb.getEndTime(), hosb.getTaskIfLongerThan());
        if (warn == null) {
            warn = shorterThan(hosb.getStartTime(), hosb.getEndTime(), hosb.getTaskIfShorterThan());
        }
        return new HistoryOrderStepResult(hosb, warn);
    }

    private HistoryOrderStepResultWarn longerThan(Long historyId, Date startTime, Date endDate, String definition) {
        if (SOSString.isEmpty(definition)) {
            return null;
        }

        if (longerThan.containsKey(historyId)) {
            longerThan.remove(historyId);
        }

        long diff = SOSDate.getSeconds(endDate) - SOSDate.getSeconds(startTime);
        if (SOSDate.getTimeAsSeconds(definition) > diff) {
            return new HistoryOrderStepResultWarn(JobWarning.LONGER_THAN, String.format("Task runs longer than the expected duration of %s",
                    definition));
        }
        return null;
    }

    private HistoryOrderStepResultWarn shorterThan(Date startTime, Date endDate, String definition) {
        if (SOSString.isEmpty(definition)) {
            return null;
        }
        long diff = SOSDate.getSeconds(endDate) - SOSDate.getSeconds(startTime);
        if (SOSDate.getTimeAsSeconds(definition) < diff) {
            return new HistoryOrderStepResultWarn(JobWarning.SHORTER_THAN, String.format("Task runs shorter than the expected duration of %s",
                    definition));
        }
        return null;
    }

    private boolean insert(DBLayerMonitoring dbLayer, String orderId, Long historyId) {
        try {
            LOGGER.info(String.format("[%s][%s][order not found=%s, id=%s]read from history orders...", serviceIdentifier, IDENTIFIER, orderId,
                    historyId));

            DBItemMonitoringOrder item = convert(dbLayer.getHistoryOrder(historyId));
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

    private DBItemMonitoringOrder convert(DBItemHistoryOrder history) {
        if (history == null) {
            return null;
        }
        DBItemMonitoringOrder item = new DBItemMonitoringOrder();
        item.setHistoryId(history.getId());
        item.setControllerId(history.getControllerId());
        item.setOrderId(history.getOrderId());
        item.setWorkflowPath(history.getWorkflowPath());
        item.setWorkflowVersionId(history.getWorkflowVersionId());
        item.setWorkflowPosition(history.getWorkflowPosition());
        item.setWorkflowFolder(history.getWorkflowFolder());
        item.setWorkflowName(history.getWorkflowName());
        item.setWorkflowTitle(history.getWorkflowTitle());
        item.setMainParentId(history.getMainParentId());
        item.setParentId(history.getParentId());
        item.setParentOrderId(history.getParentOrderId());
        item.setHasChildren(history.getHasChildren());
        item.setName(history.getName());
        item.setTitle(history.getTitle());
        item.setStartCause(history.getStartCause());
        item.setStartTimePlanned(history.getStartTimePlanned());
        item.setStartTime(history.getStartTime());
        item.setStartWorkflowPosition(history.getStartWorkflowPosition());
        item.setStartParameters(history.getStartParameters());
        item.setCurrentHistoryOrderStepId(history.getCurrentHistoryOrderStepId());
        item.setEndTime(history.getEndTime());
        item.setEndWorkflowPosition(history.getEndWorkflowPosition());
        item.setEndHistoryOrderStepId(history.getEndHistoryOrderStepId());
        item.setSeverity(history.getSeverity());
        item.setState(history.getState());
        item.setStateTime(history.getStateTime());
        item.setError(history.getError());
        item.setErrorState(history.getErrorState());
        item.setErrorReason(history.getErrorReason());
        item.setErrorReturnCode(history.getErrorReturnCode());
        item.setErrorCode(history.getErrorCode());
        item.setErrorText(history.getErrorText());
        item.setLogId(history.getLogId());

        item.setCreated(new Date());
        item.setModified(item.getCreated());

        return item;
    }

    private void serialize() {
        if (payloads.size() > 0 || longerThan.size() > 0) {
            try {
                saveJocVariable(new SOSSerializer<SerializedResult>().serialize2bytes(new SerializedResult(payloads, longerThan)));
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
        try {
            DBItemJocVariable var = getJocVariable();
            if (var == null) {
                return;
            }
            SerializedResult sr = new SOSSerializer<SerializedResult>().deserialize(var.getBinaryValue());
            if (sr.getPayloads() != null) {
                // payloads can be not empty because event subscription
                payloads.addAll(sr.getPayloads());
            }
            if (sr.getLongerThan() != null) {
                // longerThan is empty ... ?
                longerThan.putAll(sr.getLongerThan());
            }

        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
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
        private final Map<Long, Date> longerThan;

        protected SerializedResult(Collection<AHistoryBean> payloads, Map<Long, Date> longerThan) {
            this.payloads = payloads;
            this.longerThan = longerThan;
        }

        protected Collection<AHistoryBean> getPayloads() {
            return payloads;
        }

        protected Map<Long, Date> getLongerThan() {
            return longerThan;
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
}
