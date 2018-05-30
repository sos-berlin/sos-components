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
import com.sos.jobscheduler.db.DBItemSchedulerLogs;
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
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private final static long MAX_COUNTER = 10;
    private final SOSHibernateFactory dbFactory;
    private final DBLayerHistory dbLayer;
    private final EventHandlerMasterSettings masterSettings;
    private boolean isLocked = false;
    private String lockCause = null;
    private Map<String, DBItemSchedulerOrderHistory> orders;
    private Map<String, DBItemSchedulerOrderStepHistory> orderSteps;
    private DBItemSchedulerSettings schedulerSettings;
    private Long storedEventId;
    private long counter;

    private static enum CacheType {
        order, orderStep
    };

    public HistoryModel(SOSHibernateFactory factory, EventHandlerMasterSettings ms) {
        dbFactory = factory;
        masterSettings = ms;
        dbLayer = new DBLayerHistory("history_" + masterSettings.getSchedulerId());
    }

    public Long process(Event event) {
        SOSHibernateSession session = null;
        Long newEventId = new Long(0);
        orders = new HashMap<String, DBItemSchedulerOrderHistory>();
        orderSteps = new HashMap<String, DBItemSchedulerOrderStepHistory>();
        counter = 0;
        try {
            session = dbFactory.openStatelessSession();

            session.beginTransaction();
            for (IEntry en : event.getStampeds()) {
                Entry entry = (Entry) en;

                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s]%s", entry.getType(), SOSString.toString(entry)));
                }

                Long eventId = entry.getEventId();
                if (storedEventId > eventId) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][skip] stored eventId=%s > current eventId=%s", entry.getType(), entry.getKey(),
                                storedEventId, eventId));
                    }
                    continue;
                }
                counter++;

                switch (entry.getType()) {
                case OrderAddedFat:
                    orderAdded(session, entry);
                    break;
                case OrderProcessingStartedFat:
                    orderProcessingStarted(session, entry);
                    break;
                case OrderStdoutWrittenFat:
                    orderOutWritten(session, entry, new Long(0));
                    break;
                case OrderStderrWrittenFat:
                    orderOutWritten(session, entry, new Long(1));
                    break;
                case OrderProcessedFat:
                    orderProcessed(session, entry);
                    break;
                case OrderFinishedFat:
                    orderFinished(session, entry);
                    break;
                }
                newEventId = eventId;
                LOGGER.debug("---------------------------------------------");
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
        return storedEventId;
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
            DBItemSchedulerOrderHistory item = new DBItemSchedulerOrderHistory();
            item.setSchedulerId(masterSettings.getSchedulerId());
            item.setOrderKey(entry.getKey());
            item.setWorkflowPosition(entry.getWorkflowPosition().getOrderPositionAsString());
            item.setRetryCounter(new Long(0));// TODO

            if (entry.getParent() == null) {
                item.setParentId(new Long(0));
                item.setParentOrderKey(null);
            } else {
                DBItemSchedulerOrderHistory orderItem = getOrderHistory(session, entry.getParent());
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
            item.setEventId(String.valueOf(entry.getEventId()));
            item.setCreated(SOSDate.getCurrentDateUTC());
            item.setModified(item.getCreated());

            session.save(item);
            storeParameters(session, entry, new Long(0), item.getId(), new Long(0));

            Date chunkTimestamp = entry.getTimestamp() == null ? entry.getEventIdAsDate() : entry.getTimestampAsDate();
            storeLog(session, item.getOrderKey(), item.getId(), new Long(0), new Long(0), new Long(0), new Long(0), ".", ".", "..", chunkTimestamp,
                    String.format("order added: %s, workflowPath=%s(version=%s), plannedStartime=%s", item.getOrderKey(), item.getWorkflowPath(), item
                            .getWorkflowVersion(), item.getStartTimePlanned()));// TODO

            tryHandleTransaction(session, entry.getEventId());

            addOrderToCache(item.getOrderKey(), item);
        } catch (SOSHibernateException e) {
            LOGGER.error(e.toString(), e);
            throw e;
        }
    }

    private void orderProcessingStarted(SOSHibernateSession session, Entry entry) throws Exception {
        DBItemSchedulerOrderHistory orderItem = getOrderHistory(session, entry.getKey());

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
        item.setEventId(String.valueOf(entry.getEventId()));
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

        // storeLog(session, orderKey, orderHistoryId, orderStepHistoryId, logType, logLevel, outType, jobPath, agentUri, agentTimezone, chunkTimestamp, chunk);

        storeLog(session, item.getOrderKey(), item.getId(), new Long(0), new Long(0), new Long(0), new Long(0), ".", ".", "..", item.getStartTime(),
                String.format("order started: %s, workflowPosition=%s, cause=%s", orderItem.getOrderKey(), orderItem.getWorkflowPosition(), orderItem
                        .getStartCause()));// TODO

        storeLog(session, item.getOrderKey(), orderItem.getId(), item.getId(), new Long(1), new Long(0), new Long(0), item.getJobPath(), item
                .getAgentUri(), ".", item.getStartTime(), String.format(
                        "order step started: %s, jobPath=%s, agentUri=%s, workflowPosition=%s(version=%s), cause=%s", item.getOrderKey(), item
                                .getJobPath(), item.getAgentUri(), item.getWorkflowPosition(), item.getWorkflowVersion(), item.getStartCause()));// TODO

        tryHandleTransaction(session, entry.getEventId());

        addOrderToCache(orderItem.getOrderKey(), orderItem);
        addOrderStepToCache(item.getOrderKey(), item);
    }

    private void orderOutWritten(SOSHibernateSession session, Entry entry, Long outType) throws Exception {
        DBItemSchedulerOrderStepHistory item = getOrderStepHistory(session, entry.getKey());

        // storeLog(session, orderKey, orderHistoryId, orderStepHistoryId, logType, logLevel, outType, jobPath, agentUri, agentTimezone, chunkTimestamp, chunk);
        Date chunkTimestamp = entry.getTimestamp() == null ? entry.getEventIdAsDate() : entry.getTimestampAsDate();
        storeLog(session, item.getOrderKey(), item.getOrderHistoryId(), item.getId(), new Long(4), new Long(0), outType, item.getJobPath(), item
                .getAgentUri(), ".", chunkTimestamp, entry.getChunk());// TODO

    }

    private void orderProcessed(SOSHibernateSession session, Entry entry) throws Exception {
        DBItemSchedulerOrderStepHistory item = getOrderStepHistory(session, entry.getKey());
        item.setEndTime(entry.getTimestamp() == null ? entry.getEventIdAsDate() : entry.getTimestampAsDate());
        item.setReturnCode(entry.getOutcome().getReturnCode());
        item.setState(entry.getOutcome().getType());

        session.update(item);
        storeParameters(session, entry, new Long(2), item.getOrderHistoryId(), item.getId());

        // storeLog(session, orderKey, orderHistoryId, orderStepHistoryId, logType, logLevel, outType, jobPath, agentUri, agentTimezone, chunkTimestamp, chunk);
        storeLog(session, item.getOrderKey(), item.getOrderHistoryId(), item.getId(), new Long(2), new Long(0), new Long(0), item.getJobPath(), item
                .getAgentUri(), ".", item.getEndTime(), String.format(
                        "order step ended: %s, jobPath=%s, agentUri=%s, workflowPosition=%s(version=%s)", item.getOrderKey(), item.getJobPath(), item
                                .getAgentUri(), item.getWorkflowPosition(), item.getWorkflowVersion()));// TODO);// TODO

        tryHandleTransaction(session, entry.getEventId());

        clearCache(item.getOrderKey(), CacheType.orderStep);
    }

    private void orderFinished(SOSHibernateSession session, Entry entry) throws Exception {
        DBItemSchedulerOrderHistory item = getOrderHistory(session, entry.getKey());
        item.setEndTime(entry.getTimestamp() == null ? entry.getEventIdAsDate() : entry.getTimestampAsDate());

        DBItemSchedulerOrderStepHistory stepItem = dbLayer.getOrderStepHistoryById(session, item.getCurrentStepId());

        item.setEndWorkflowPosition(stepItem.getWorkflowPosition());
        item.setEndStepId(stepItem.getId());
        item.setState("finished");// TODO
        item.setError(stepItem.getError());
        item.setErrorCode(stepItem.getErrorCode());
        item.setErrorText(stepItem.getErrorText());
        item.setModified(SOSDate.getCurrentDateUTC());

        session.update(item);

        // storeLog(session, orderKey, orderHistoryId, orderStepHistoryId, logType, logLevel, outType, jobPath, agentUri, agentTimezone, chunkTimestamp, chunk);
        storeLog(session, item.getOrderKey(), item.getId(), new Long(0), new Long(3), new Long(0), new Long(0), ".", ".", ".", item.getEndTime(),
                String.format("order finished: %s, workflowPath=%s(version=%s)", item.getOrderKey(), item.getWorkflowPath(), item
                        .getWorkflowVersion()));// TODO

        tryHandleTransaction(session, entry.getEventId());

        clearCache(item.getOrderKey(), CacheType.order);
    }

    private void addOrderToCache(String orderKey, DBItemSchedulerOrderHistory item) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[addOrderToCache][%s]workflowPosition=%s", orderKey, item.getWorkflowPosition()));
        }
        orders.put(orderKey, item);
    }

    private void addOrderStepToCache(String orderKey, DBItemSchedulerOrderStepHistory item) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[addOrderStepToCache][%s]jobPath=%s, workflowPosition=%s", orderKey, item.getJobPath(), item
                    .getWorkflowPosition()));
        }
        orderSteps.put(orderKey, item);
    }

    private DBItemSchedulerOrderHistory getOrderFromCache(String orderKey) {
        if (orders.containsKey(orderKey)) {
            DBItemSchedulerOrderHistory item = orders.get(orderKey);
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[getOrderFromCache][%s]%s", orderKey, SOSString.toString(item)));
            }
            return item;
        }
        return null;
    }

    private DBItemSchedulerOrderStepHistory getOrderStepFromCache(String orderKey) {
        if (orderSteps.containsKey(orderKey)) {
            DBItemSchedulerOrderStepHistory item = orderSteps.get(orderKey);
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[getOrderStepFromCache][%s]%s", orderKey, SOSString.toString(item)));
            }
            return item;
        }
        return null;
    }

    private void clearCache(String orderKey, CacheType cacheType) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[clearCache][%s]cacheType=%s", orderKey, cacheType));
        }
        switch (cacheType) {
        case orderStep:
            orderSteps.entrySet().removeIf(entry -> entry.getKey().equals(orderKey));
            break;
        case order:
            orders.entrySet().removeIf(entry -> entry.getKey().startsWith(orderKey));
            orderSteps.entrySet().removeIf(entry -> entry.getKey().startsWith(orderKey));
            break;
        }
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

    private DBItemSchedulerOrderHistory getOrderHistory(SOSHibernateSession session, String orderKey) throws Exception {
        DBItemSchedulerOrderHistory item = getOrderFromCache(orderKey);
        if (item == null) {
            item = dbLayer.getOrderHistory(session, masterSettings.getSchedulerId(), orderKey);
            if (item == null) {
                throw new Exception(String.format("order not found. schedulerId=%s, orderKey=%s", masterSettings.getSchedulerId(), orderKey));
            } else {
                addOrderToCache(orderKey, item);
            }
        }
        return item;
    }

    private DBItemSchedulerOrderStepHistory getOrderStepHistory(SOSHibernateSession session, String orderKey) throws Exception {
        DBItemSchedulerOrderStepHistory item = getOrderStepFromCache(orderKey);
        if (item == null) {
            item = dbLayer.getOrderStepHistory(session, masterSettings.getSchedulerId(), orderKey);
            if (item == null) {
                throw new Exception(String.format("order step not found. schedulerId=%s, orderKey=%s", masterSettings.getSchedulerId(), orderKey));
            } else {
                addOrderStepToCache(orderKey, item);
            }
        }
        return item;
    }

    private void storeLog(SOSHibernateSession session, String orderKey, Long orderHistoryId, Long orderStepHistoryId, Long logType, Long logLevel,
            Long outType, String jobPath, String agentUri, String agentTimezone, Date chunkTimestamp, String chunk) throws Exception {

        String[] arr = chunk.split("\\r?\\n");
        for (int i = 0; i < arr.length; i++) {
            DBItemSchedulerLogs item = new DBItemSchedulerLogs();

            item.setSchedulerId(masterSettings.getSchedulerId());
            item.setOrderKey(orderKey);
            item.setOrderHistoryId(orderHistoryId);
            item.setOrderStepHistoryId(orderStepHistoryId);
            item.setLogType(logType);
            item.setLogLevel(logLevel);
            item.setOutType(outType);
            item.setJobPath(jobPath);
            item.setAgentUri(agentUri);
            item.setAgentTimezone(agentTimezone);
            item.setChunkTimestamp(chunkTimestamp);
            item.setChunk(arr[i]);

            item.setCreated(SOSDate.getCurrentDateUTC());

            session.save(item);
        }
    }

    private void storeParameters(SOSHibernateSession session, Entry entry, Long paramType, Long orderId, Long orderStepId) throws Exception {
        if (entry.getVariables() != null) {
            for (Map.Entry<String, String> param : entry.getVariables().entrySet()) {
                DBItemSchedulerParameterHistory pItem = new DBItemSchedulerParameterHistory();
                pItem.setParamType(paramType);
                pItem.setOrderHistoryId(orderId);
                pItem.setOrderStepHistoryId(orderStepId);
                pItem.setParamName(param.getKey());
                pItem.setParamValue(param.getValue());
                pItem.setCreated(SOSDate.getCurrentDateUTC());

                session.save(pItem);
            }
        }
    }

    public boolean isLocked() {
        return isLocked;
    }
}
