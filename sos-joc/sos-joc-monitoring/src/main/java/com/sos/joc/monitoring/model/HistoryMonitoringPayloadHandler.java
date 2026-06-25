package com.sos.joc.monitoring.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateObjectOperationException;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.controller.model.event.EventType;
import com.sos.history.JobWarning;
import com.sos.joc.cluster.bean.history.AHistoryBean;
import com.sos.joc.cluster.bean.history.HistoryOrderBean;
import com.sos.joc.cluster.bean.history.HistoryOrderStepBean;
import com.sos.joc.db.monitoring.DBItemMonitoringOrder;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;
import com.sos.joc.monitoring.MonitorService;
import com.sos.joc.monitoring.db.DBLayerMonitoring;
import com.sos.joc.monitoring.model.bean.ExpectedSeconds;
import com.sos.joc.monitoring.model.bean.MonitorOrderResult;
import com.sos.joc.monitoring.model.bean.MonitorOrderStepResult;
import com.sos.joc.monitoring.model.bean.MonitorOrderStepResultWarn;
import com.sos.joc.monitoring.model.bean.ToNotify;

public class HistoryMonitoringPayloadHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryMonitoringPayloadHandler.class);

    public ToNotify handlePayloads(HistoryMonitoringModel model, boolean isDebugEnabled) {
        ToNotify toNotify = new ToNotify();
        if (model.getPayloads().size() == 0) {
            return toNotify;
        }

        model.setLastActivityStart();
        List<AHistoryBean> toRemove = new ArrayList<>();
        DBLayerMonitoring dbLayer = new DBLayerMonitoring(MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY);
        try {
            List<AHistoryBean> copy = new ArrayList<>(model.getPayloads());
            copy.sort(Comparator.comparing(AHistoryBean::getEventId));

            dbLayer.setSession(model.getFactory().openStatelessSession(dbLayer.getIdentifier()));
            dbLayer.getSession().beginTransaction();

            HistoryOrderBean hob;
            for (AHistoryBean b : copy) {
                if (isDebugEnabled) {
                    LOGGER.debug("[PAYLOAD]" + b.getEventType() + "=" + SOSString.toString(b));
                }
                if (model.getClosed().get()) {
                    break;
                }

                // todo - toRemove - cleanup too old payloads ???

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

                    toNotify.getErrorOrders().add(new MonitorOrderResult(hob));
                    break;
                case OrderStopped:
                    hob = (HistoryOrderBean) b;
                    orderStopped(dbLayer, hob);

                    toNotify.getErrorOrders().add(new MonitorOrderResult(hob));
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

                    toNotify.getErrorOrders().add(new MonitorOrderResult(hob));
                    break;
                case OrderFinished:
                    hob = (HistoryOrderBean) b;
                    orderFinished(dbLayer, hob);
                    if (hob.getError()) {
                        toNotify.getErrorOrders().add(new MonitorOrderResult(hob));
                    } else {
                        toNotify.getSuccessOrders().add(new MonitorOrderResult(hob));
                    }
                    break;
                // OrderStep
                case OrderProcessingStarted:
                    orderStepStarted(model, dbLayer, (HistoryOrderStepBean) b);
                    break;
                case OrderProcessed:
                    toNotify.getSteps().add(orderStepProcessed(model, dbLayer, (HistoryOrderStepBean) b));
                    break;
                case OrderStderrWritten:

                    toNotify.getSteps().add(getOrderStepStderrWarning((HistoryOrderStepBean) b));
                    break;
                default:
                    break;
                }
                toRemove.add(b);
            }
            dbLayer.getSession().commit();

            toNotify.setFirstEventId(copy.get(0).getEventId());
            toNotify.setLastEventId(copy.get(copy.size() - 1).getEventId());
        } catch (Exception e) {
            dbLayer.rollback();
            LOGGER.warn(e.toString(), e);
        } finally {
            dbLayer.close();
            model.getPayloads().removeAll(toRemove);
            model.setLastActivityEnd();
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

    private void orderStopped(DBLayerMonitoring dbLayer, HistoryOrderBean hob) throws SOSHibernateException {
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

    private void orderStepStarted(HistoryMonitoringModel model, DBLayerMonitoring dbLayer, HistoryOrderStepBean hosb) throws SOSHibernateException {
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
        item.setAgentName(hosb.getAgentName());
        item.setAgentUri(hosb.getAgentUri());
        item.setSubagentClusterId(hosb.getSubagentClusterId());

        item.setStartCause(hosb.getStartCause());
        item.setStartTime(hosb.getStartTime());
        item.setStartVariables(hosb.getStartVariables());

        item.setError(false);

        item.setCreated(new Date());
        item.setModified(item.getCreated());

        try {
            dbLayer.getSession().save(item);
            if (!dbLayer.updateOrderOnOrderStep(item.getHistoryOrderId(), item.getHistoryId())) {
                insert(dbLayer, hosb.getEventType(), hosb.getOrderId(), item.getHistoryOrderId());
            }
            if (!SOSString.isEmpty(hosb.getWarnIfLonger())) {
                model.putLongerThan("orderStepStarted", hosb);
            }
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                throw e;
            }
        }
    }

    private MonitorOrderStepResult getOrderStepStderrWarning(HistoryOrderStepBean hosb) {
        MonitorOrderStepResultWarn warn = new MonitorOrderStepResultWarn(JobWarning.STDERR, "Job reports output to stderr: " + hosb
                .getFirstChunkStdError());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s][%s][workflowPath=%s, job=%s(historyId=%s)]%s", MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, hosb
                    .getControllerId(), hosb.getWorkflowPath(), hosb.getJobName(), hosb.getHistoryId(), SOSString.toString(warn)));
        }
        return new MonitorOrderStepResult(hosb, warn);
    }

    private MonitorOrderStepResult orderStepProcessed(HistoryMonitoringModel model, DBLayerMonitoring dbLayer, HistoryOrderStepBean hosb)
            throws SOSHibernateException {
        MonitorOrderStepResult r = analyzeExecutionTime(model, dbLayer, hosb);
        r.addWarn(analyzeReturnCode(hosb));

        dbLayer.setOrderStepEnd(r);

        model.removeLongerThan("orderStepProcessed", hosb);

        if (LOGGER.isDebugEnabled()) {
            if (!SOSCollection.isEmpty(r.getWarnings())) {
                LOGGER.debug(String.format("[%s][orderStepProcessed][warnings][%s][workflowPath=%s, job=%s(historyId=%s, step=%s)]warnings=%s",
                        MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, hosb.getControllerId(), hosb.getWorkflowPath(), hosb.getJobName(), hosb
                                .getHistoryId(), r.getStep(), r.getWarnings().size()));
                for (MonitorOrderStepResultWarn warn : r.getWarnings()) {
                    LOGGER.debug(String.format("    %s", SOSString.toString(warn)));
                }
            }
        }

        return r;
    }

    private boolean insert(DBLayerMonitoring dbLayer, EventType eventType, String orderId, Long historyId) {
        try {
            LOGGER.info(String.format("[%s][%s][order not found=%s, id=%s]read from history orders...", MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY,
                    eventType.name(), orderId, historyId));

            DBItemMonitoringOrder item = dbLayer.convert(dbLayer.getHistoryOrder(historyId));
            if (item != null) {
                try {
                    dbLayer.getSession().save(item);
                    return true;
                } catch (Exception e) {
                    LOGGER.warn(String.format("[%s][save]%s", historyId, e.toString()), e);
                }
            }
        } catch (Exception e) {
            LOGGER.warn(String.format("[%s][get]%s", historyId, e.toString()), e);
        }
        return false;
    }

    private MonitorOrderStepResult analyzeExecutionTime(HistoryMonitoringModel model, DBLayerMonitoring dbLayer, HistoryOrderStepBean hosb) {
        if (hosb.getStartTime() == null) {
            return new MonitorOrderStepResult(hosb, null);
        }

        MonitorOrderStepResultWarn warn = model.analyzeLongerThan(dbLayer, hosb, hosb.getEndTime(), true);
        if (warn != null && warn.isInvalid()) {
            model.removeLongerThan("analyzeExecutionTime", hosb);
            warn = null;
        }
        if (warn == null) {
            warn = analyzeShorterThan(model, dbLayer, hosb);
        }
        if (warn != null && warn.isInvalid()) {
            model.removeLongerThan("analyzeExecutionTime", hosb);
            warn = null;
        }
        return new MonitorOrderStepResult(hosb, warn);
    }

    private MonitorOrderStepResultWarn analyzeReturnCode(HistoryOrderStepBean hosb) {
        MonitorOrderStepResultWarn warn = null;
        if (hosb.isInWarnReturnCodes(hosb.getReturnCode())) {
            warn = new MonitorOrderStepResultWarn(JobWarning.RETURN_CODE, "Job return code " + hosb.getReturnCode()
                    + " matches the configured warning return codes " + hosb.getWarnReturnCodes());
        }
        return warn;
    }

    private MonitorOrderStepResultWarn analyzeShorterThan(HistoryMonitoringModel model, DBLayerMonitoring dbLayer, HistoryOrderStepBean hosb) {
        ExpectedSeconds expected = model.getExpectedSeconds(dbLayer, JobWarning.SHORTER_THAN, hosb, hosb.getWarnIfShorter());
        if (expected == null || expected.getSeconds() == null) {
            return null;
        }
        long diff = SOSDate.getSeconds(hosb.getEndTime()) - SOSDate.getSeconds(hosb.getStartTime());
        if (diff < 0) {
            if (LOGGER.isDebugEnabled()) {
                try {
                    LOGGER.debug(String.format("[%s][analyzeShorterThan][diff=%s < 0][startTime=%s, endTime=%s]%s",
                            MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, diff, SOSDate.getDateTimeAsString(hosb.getStartTime()), SOSDate
                                    .getDateTimeAsString(hosb.getEndTime()), SOSString.toString(hosb)));
                } catch (Exception e) {
                    LOGGER.warn(e.toString(), e);
                }
            }
            return null;
        }

        if (diff < expected.getSeconds()) {
            return new MonitorOrderStepResultWarn(JobWarning.SHORTER_THAN, String.format("Job runs shorter than the expected %s", model
                    .getExpectedDurationMessage(hosb.getWarnIfShorter(), expected)));
        }
        return null;
    }

}
