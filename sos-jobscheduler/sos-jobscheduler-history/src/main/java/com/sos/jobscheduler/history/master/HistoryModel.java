package com.sos.jobscheduler.history.master;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateObjectOperationStaleStateException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.db.DBItemSchedulerOrderHistory;
import com.sos.jobscheduler.db.DBItemSchedulerOrderStepHistory;
import com.sos.jobscheduler.db.DBItemSchedulerParameterHistory;
import com.sos.jobscheduler.db.DBItemSchedulerSettings;
import com.sos.jobscheduler.event.master.bean.Event;
import com.sos.jobscheduler.event.master.bean.IEntry;
import com.sos.jobscheduler.event.master.fatevent.bean.Entry;
import com.sos.jobscheduler.event.master.handler.EventHandlerMasterSettings;
import com.sos.jobscheduler.history.db.DBLayerHistory;
import com.sos.jobscheduler.history.helper.HistoryUtil;

public class HistoryModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryModel.class);
    // private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private final static long MAX_COUNTER = 10;
    private final SOSHibernateFactory dbFactory;
    private final DBLayerHistory dbLayer;
    private final EventHandlerMasterSettings masterSettings;
    private boolean isLocked = false;
    private String lockCause = null;
    private Map<String, DBItemSchedulerOrderHistory> orders;
    private Map<String, DBItemSchedulerOrderStepHistory> jobs;
    private DBItemSchedulerSettings schedulerSettings;
    private Long storedEventId;
    private long counter;

    public HistoryModel(SOSHibernateFactory factory, EventHandlerMasterSettings ms) {
        dbFactory = factory;
        masterSettings = ms;
        dbLayer = new DBLayerHistory("history_" + masterSettings.getSchedulerId());
    }

    public Long process(Event event) {
        SOSHibernateSession session = null;
        Long newEventId = new Long(0);
        orders = new HashMap<String, DBItemSchedulerOrderHistory>();
        jobs = new HashMap<String, DBItemSchedulerOrderStepHistory>();
        counter = 0;
        try {
            session = dbFactory.openStatelessSession();

            session.beginTransaction();
            for (IEntry en : event.getStampeds()) {
                Entry entry = (Entry) en;

                Long eventId = entry.getEventId();
                if (eventId <= storedEventId) {
                    LOGGER.debug(String.format("[skip] stored eventId=%s >= current eventId=%s", storedEventId, eventId));
                    continue;
                }
                counter++;
                System.out.println("");
                System.out.println(entry);

                switch (entry.getType()) {
                case OrderAddedFat:
                    orderAdded(session, entry);
                    break;
                case OrderProcessingStartedFat:
                    orderProcessingStarted(session, entry);
                    break;
                case OrderStdoutWrittenFat:
                    orderOutWritten(session, entry);
                    break;
                case OrderStderrWrittenFat:
                    orderOutWritten(session, entry);
                    break;
                case OrderProcessedFat:
                    orderProcessed(session, entry);
                    break;
                case OrderFinishedFat:
                    orderFinished(session, entry);
                    break;
                }
                newEventId = eventId;
                System.out.println("---------------------------------------------");
            }
            if (session.isTransactionOpened()) {
                if (storedEventId != newEventId) {
                    dbLayer.updateSchedulerSettings(session, schedulerSettings, newEventId);
                }
                session.commit();
            }
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            try {
                session.rollback();
            } catch (Exception ex) {
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return newEventId;
    }

    private void tryHandleTransaction(SOSHibernateSession session, Long eventId) throws Exception {
        if (counter % MAX_COUNTER == 0 && session.isTransactionOpened()) {
            dbLayer.updateSchedulerSettings(session, schedulerSettings, eventId);
            storedEventId = eventId;
            session.commit();
            session.beginTransaction();
        }
    }

    private void orderAdded(SOSHibernateSession session, Entry entry) throws Exception {

        try {
            System.out.println("--- ORDER ADDED ---");

            DBItemSchedulerOrderHistory item = new DBItemSchedulerOrderHistory();
            item.setSchedulerId(masterSettings.getSchedulerId());
            item.setOrderKey(entry.getKey());
            item.setWorkflowPosition(entry.getWorkflowPosition().getOrderPositionAsString());
            item.setRetryCounter(new Long(0));// TODO

            if (entry.getParent() == null) {
                item.setParentId(new Long(0));
                item.setParentOrderKey(null);
            } else {
                DBItemSchedulerOrderHistory orderItem = getOrderHistory(session, entry.getParent(), "0");// TODO parent workflowPosition
                item.setParentId(orderItem.getId());
                item.setParentOrderKey(entry.getParent());
            }

            item.setName(entry.getKey());// TODO
            item.setTitle(null);// TODO
            item.setWorkflowVersion(entry.getWorkflowPosition().getWorkflowId().getVersionId());
            item.setWorkflowPath(entry.getWorkflowPosition().getWorkflowId().getPath());
            item.setWorkflowFolder(HistoryUtil.getFolderFromPath(item.getWorkflowPath()));
            item.setWorkflowName(HistoryUtil.getBasenameFromPath(item.getWorkflowPath()));
            item.setWorkflowTitle(null);// TODO
            item.setStartCause(entry.getCause());
            item.setStartTimePlanned(entry.getSchedulerAtAsDate());
            item.setStartTime(new Date());// TODO
            item.setStartWorkflowPosition(entry.getWorkflowPosition().getPositionAsString());
            item.setCurrentStepId(new Long(0));
            item.setEndTime(null);
            item.setEndWorkflowPosition(null);
            item.setEndStepId(new Long(0));
            item.setState("added");// TODO
            item.setStateText(null);// TODO
            item.setError(false);
            item.setErrorStepId(new Long(0));
            item.setErrorText(null);
            item.setCreated(SOSDate.getCurrentDateUTC());
            item.setModified(item.getCreated());

            session.save(item);
            storeParameters(session, entry, new Long(0), item.getId(), new Long(0));
            tryHandleTransaction(session, entry.getEventId());

            orders.put(item.getOrderKey(), item);
        } catch (SOSHibernateException e) {
            LOGGER.error(e.toString(), e);
            throw e;
        }
    }

    private void orderProcessingStarted(SOSHibernateSession session, Entry entry) throws Exception {

        System.out.println("--- ORDER PROCESSING STARTED ---");

        DBItemSchedulerOrderHistory orderItem = getOrderHistory(session, entry.getKey(), entry.getWorkflowPosition().getOrderPositionAsString());

        DBItemSchedulerOrderStepHistory item = new DBItemSchedulerOrderStepHistory();
        item.setSchedulerId(masterSettings.getSchedulerId());
        item.setOrderKey(entry.getKey());
        item.setWorkflowPosition(entry.getWorkflowPosition().getPositionAsString());
        item.setRetryCounter(new Long(0));// TODO

        item.setOrderHistoryId(orderItem.getId());

        item.setPosition(entry.getWorkflowPosition().getLastPosition());
        item.setWorkflowPath(entry.getWorkflowPosition().getWorkflowId().getPath());
        item.setWorkflowVersion(entry.getWorkflowPosition().getWorkflowId().getVersionId());
        item.setJobPath(entry.getJobPath());
        item.setJobFolder(HistoryUtil.getFolderFromPath(item.getJobPath()));
        item.setJobName(HistoryUtil.getBasenameFromPath(item.getJobPath()));
        item.setAgentUri(entry.getAgentUri());
        item.setStartCause("order");// TODO
        item.setStartTime(entry.getTimestamp() == null ? entry.getEventIdAsDate() : entry.getTimestampAsDate());
        item.setEndTime(null);
        item.setReturnCode(null);
        item.setState("running");// TODO
        item.setError(false);
        item.setErrorCode(null);
        item.setErrorText(null);
        item.setCreated(SOSDate.getCurrentDateUTC());
        item.setModified(item.getCreated());

        session.save(item);

        if (item.getWorkflowPosition().equals(orderItem.getStartWorkflowPosition())) {// + order.startTime != default
            orderItem.setStartTime(item.getStartTime());
            orderItem.setState("started");// TODO
        }
        orderItem.setCurrentStepId(item.getId());
        orderItem.setModified(SOSDate.getCurrentDateUTC());
        session.update(orderItem);

        storeParameters(session, entry, new Long(1), orderItem.getId(), item.getId());

        tryHandleTransaction(session, entry.getEventId());

        jobs.put(item.getOrderKey(), item);
        orders.put(orderItem.getOrderKey(), orderItem);
    }

    private void orderOutWritten(SOSHibernateSession session, Entry entry) {
        if (1 == 1)
            return;
        System.out.println("    Type: " + entry.getType());
        System.out.println("    eventId: " + entry.getEventId() + " (" + entry.getEventIdAsDate() + ")");
        System.out.println("    timestamp: " + entry.getTimestamp() + " (" + entry.getTimestampAsDate() + ")");
        System.out.println("    key: " + entry.getKey());

        System.out.println("    chunk: " + entry.getChunk());
    }

    private void orderProcessed(SOSHibernateSession session, Entry entry) throws Exception {
        System.out.println("--- ORDER PROCESSED ---");

        DBItemSchedulerOrderStepHistory item = getOrderStepHistory(session, entry.getKey());
        item.setEndTime(entry.getTimestamp() == null ? entry.getEventIdAsDate() : entry.getTimestampAsDate());
        item.setReturnCode(entry.getOutcome().getReturnCode());
        item.setState(entry.getOutcome().getType());

        session.update(item);
        storeParameters(session, entry, new Long(2), item.getOrderHistoryId(), item.getId());
        tryHandleTransaction(session, entry.getEventId());
    }

    private DBItemSchedulerOrderHistory getOrderHistory(SOSHibernateSession session, String orderKey, String workflowPosition) throws Exception {
        if (orders.containsKey(orderKey)) {
            return orders.get(orderKey);
        } else {
            DBItemSchedulerOrderHistory item = dbLayer.getOrderHistory(session, masterSettings.getSchedulerId(), orderKey, workflowPosition);
            if (item == null) {
                throw new Exception(String.format("order not found. schedulerId=%s, orderKey=%s, workflowPosition=%s ", masterSettings
                        .getSchedulerId(), orderKey, workflowPosition));
            } else {
                orders.put(orderKey, item);
                return item;
            }
        }
    }

    private DBItemSchedulerOrderStepHistory getOrderStepHistory(SOSHibernateSession session, String orderKey) throws Exception {

        if (jobs.containsKey(orderKey)) {
            return jobs.get(orderKey);
        } else {
            DBItemSchedulerOrderStepHistory item = dbLayer.getOrderStepHistory(session, masterSettings.getSchedulerId(), orderKey);
            if (item == null) {
                throw new Exception(String.format("order step not found. schedulerId=%s, orderKey=%s", masterSettings.getSchedulerId(), orderKey));
            } else {
                jobs.put(orderKey, item);
                return item;
            }
        }
    }

    private void orderFinished(SOSHibernateSession session, Entry entry) throws Exception {

        System.out.println("--- ORDER FINISHED ---");

        DBItemSchedulerOrderHistory item = getOrderHistory(session, entry.getKey(), "0");// TODO
        item.setEndTime(entry.getTimestamp() == null ? entry.getEventIdAsDate() : entry.getTimestampAsDate());

        DBItemSchedulerOrderStepHistory stepItem = dbLayer.getOrderStepHistoryById(session, item.getCurrentStepId());
        // int lastStepPosition = Integer.parseInt(entry.getWorkflowPosition().getPositionAsString()) - 1;
        // DBItemSchedulerOrderStepHistory lastStep = dbLayer.getLastOrderStepHistoryByWorkflowPosition(session, item.getId(), lastStepPosition + "");

        item.setEndWorkflowPosition(stepItem.getWorkflowPosition());
        item.setEndStepId(stepItem.getId());
        item.setState("finished");// TODO
        item.setError(stepItem.getError());
        item.setErrorCode(stepItem.getErrorCode());
        item.setErrorText(stepItem.getErrorText());
        item.setModified(SOSDate.getCurrentDateUTC());

        session.update(item);
        tryHandleTransaction(session, entry.getEventId());
    }

    public Long getEventId() {
        isLocked = false;
        lockCause = null;
        SOSHibernateSession session = null;
        try {
            session = dbFactory.openStatelessSession();

            session.beginTransaction();
            schedulerSettings = dbLayer.getSchedulerSettings(session);
            if (schedulerSettings == null) {
                schedulerSettings = dbLayer.insertSchedulerSettings(session, "0");
            }
            session.commit();
            LOGGER.info(String.format("eventId=%s", schedulerSettings.getTextValue()));
            storedEventId = Long.parseLong(schedulerSettings.getTextValue());
            return storedEventId;
        } catch (SOSHibernateObjectOperationStaleStateException e) {
            isLocked = true;
            lockCause = "locked by an another instance";
            try {
                session.rollback();
            } catch (Exception ex) {
            }
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            try {
                session.rollback();
            } catch (Exception ex) {
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return new Long(0);
    }

    private void storeParameters(SOSHibernateSession session, Entry entry, Long paramType, Long orderId, Long orderStepId) throws Exception {
        if (entry.getVariables() != null && 1 == 2) {
            for (Map.Entry<String, String> param : entry.getVariables().entrySet()) {
                DBItemSchedulerParameterHistory pItem = new DBItemSchedulerParameterHistory();
                pItem.setParamType(paramType);
                pItem.setOrderHistoryId(orderId);
                pItem.setOrderStepHistoryId(orderStepId);
                pItem.setParamName(param.getKey());
                pItem.setParamValue(param.getValue());
                pItem.setCreated(SOSDate.getCurrentDateUTC());

                session.save(pItem);
                System.out.println("param:" + SOSString.toString(pItem));
            }

        }
    }

    public boolean isLocked() {
        return isLocked;
    }
}
