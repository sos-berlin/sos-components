package com.sos.jobscheduler.history.master;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateObjectOperationException;
import com.sos.commons.hibernate.exception.SOSHibernateObjectOperationStaleStateException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.db.DBItemSchedulerLogs;
import com.sos.jobscheduler.db.DBItemSchedulerOrderHistory;
import com.sos.jobscheduler.db.DBItemSchedulerOrderStepHistory;
import com.sos.jobscheduler.db.DBItemSchedulerSettings;
import com.sos.jobscheduler.event.master.EventMeta;
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
    private final String identifier;
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

    public HistoryModel(SOSHibernateFactory factory, EventHandlerMasterSettings ms, String ident) {
        dbFactory = factory;
        masterSettings = ms;
        dbLayer = new DBLayerHistory("history_" + masterSettings.getSchedulerId());
        identifier = ident;
    }

    public Long process(Event event) {
        SOSHibernateSession session = null;
        Long newEventId = new Long(0);
        orders = new HashMap<String, DBItemSchedulerOrderHistory>();
        orderSteps = new HashMap<String, DBItemSchedulerOrderStepHistory>();
        counter = 0;
        try {
            session = dbFactory.openStatelessSession();
            session.setIdentifier(identifier);

            session.beginTransaction();
            for (IEntry en : event.getStampeds()) {
                Entry entry = (Entry) en;
                Long eventId = entry.getEventId();
                if (storedEventId >= eventId) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[%s][skip][%s] stored eventId=%s >= current eventId=%s %s", identifier, entry.getType(), entry
                                .getKey(), storedEventId, eventId, SOSString.toString(entry)));
                    }
                    continue;
                }
                counter++;

                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[%s]%s", identifier, entry.getType(), SOSString.toString(entry)));
                }

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
                if (newEventId > 0 && storedEventId != newEventId) {
                    dbLayer.updateSchedulerSettings(session, schedulerSettings, newEventId);
                    storedEventId = newEventId;
                }
                session.commit();
            }
            LOGGER.debug("storedEventId=" + storedEventId + ", newEventId=" + newEventId);
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
                item.setMainParentId(new Long(0));// TODO
                item.setParentId(new Long(0));
                item.setParentOrderKey(null);
            } else {
                DBItemSchedulerOrderHistory orderItem = getOrderHistory(session, entry.getParent());
                if (!orderItem.getHasChildren()) {
                    orderItem.setHasChildren(true);
                    session.update(orderItem);
                    addOrderToCache(orderItem.getOrderKey(), orderItem);
                }
                item.setMainParentId(orderItem.getMainParentId());
                item.setParentId(orderItem.getId());
                item.setParentOrderKey(entry.getParent());
            }

            item.setConstraintHash(HistoryUtil.hashString(masterSettings.getSchedulerId() + entry.getKey() + String.valueOf(entry.getEventId())));
            item.setHasChildren(false);
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
            item.setStartEventId(String.valueOf(entry.getEventId()));
            item.setStartParameters(EventMeta.map2Json(entry.getVariables()));
            item.setCurrentStepId(new Long(0));
            item.setEndTime(null);
            item.setEndWorkflowPosition(null);
            item.setEndStepId(new Long(0));
            item.setState("added");// TODO
            item.setStateText(null);// TODO
            item.setError(false);
            item.setErrorStepId(new Long(0));
            item.setErrorText(null);
            item.setEndEventId(null);
            item.setCreated(SOSDate.getCurrentDateUTC());
            item.setModified(item.getCreated());

            session.save(item);
            if (item.getMainParentId().equals(new Long(0))) {// TODO
                item.setMainParentId(item.getId());
                session.update(item);
            }

            Date chunkTimestamp = entry.getTimestamp() == null ? entry.getEventIdAsDate() : entry.getTimestampAsDate();
            storeLog(session, item.getOrderKey(), item.getMainParentId(), item.getId(), new Long(0), new Long(0), new Long(0), new Long(0), ".", ".",
                    "..", chunkTimestamp, String.format("order added: %s, workflowPath=%s(version=%s), plannedStartime=%s", item.getOrderKey(), item
                            .getWorkflowPath(), item.getWorkflowVersion(), item.getStartTimePlanned()));// TODO

            tryHandleTransaction(session, entry.getEventId());

            addOrderToCache(item.getOrderKey(), item);
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                LOGGER.error(e.toString(), e);
                throw e;
            }
            LOGGER.warn(String.format("%s[%s][%s]%s", identifier, entry.getType(), entry.getKey(), e.toString()));
            getOrderHistoryByStartEventId(session, entry.getKey(), String.valueOf(entry.getEventId()));
            tryHandleTransaction(session, entry.getEventId());
        }
    }

    private void orderProcessingStarted(SOSHibernateSession session, Entry entry) throws Exception {
        DBItemSchedulerOrderHistory orderItem = null;
        try {
            orderItem = getOrderHistory(session, entry.getKey());

            DBItemSchedulerOrderStepHistory item = new DBItemSchedulerOrderStepHistory();
            item.setSchedulerId(masterSettings.getSchedulerId());
            item.setOrderKey(entry.getKey());
            item.setWorkflowPosition(entry.getWorkflowPosition().getPositionAsString());
            item.setRetryCounter(new Long(0));// TODO

            item.setConstraintHash(HistoryUtil.hashString(masterSettings.getSchedulerId() + entry.getKey() + String.valueOf(entry.getEventId())));
            item.setMainOrderHistoryId(orderItem.getMainParentId());
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
            item.setStartEventId(String.valueOf(entry.getEventId()));
            item.setStartParameters(EventMeta.map2Json(entry.getVariables()));
            item.setEndTime(null);
            item.setEndEventId(null);
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

            // storeLog(session, orderKey, orderHistoryId, orderStepHistoryId, logType, logLevel, outType, jobPath, agentUri, agentTimezone, chunkTimestamp,
            // chunk);
            storeLog(session, item.getOrderKey(), item.getMainOrderHistoryId(), item.getId(), new Long(0), new Long(0), new Long(0), new Long(0), ".",
                    ".", "..", item.getStartTime(), String.format("order started: %s, workflowPosition=%s, cause=%s", orderItem.getOrderKey(),
                            orderItem.getWorkflowPosition(), orderItem.getStartCause()));// TODO

            storeLog(session, item.getOrderKey(), orderItem.getMainParentId(), orderItem.getId(), item.getId(), new Long(1), new Long(0), new Long(0),
                    item.getJobPath(), item.getAgentUri(), ".", item.getStartTime(), String.format(
                            "order step started: %s, jobPath=%s, agentUri=%s, workflowPosition=%s(version=%s), cause=%s", item.getOrderKey(), item
                                    .getJobPath(), item.getAgentUri(), item.getWorkflowPosition(), item.getWorkflowVersion(), item.getStartCause()));// TODO

            tryHandleTransaction(session, entry.getEventId());

            addOrderToCache(orderItem.getOrderKey(), orderItem);
            addOrderStepToCache(item.getOrderKey(), item);
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                LOGGER.error(e.toString(), e);
                throw e;
            }
            LOGGER.warn(String.format("%s[%s][%s]%s", identifier, entry.getType(), entry.getKey(), e.toString()));

            DBItemSchedulerOrderStepHistory item = getOrderStepHistoryByStartEventId(session, entry.getKey(), String.valueOf(entry.getEventId()));
            tryHandleTransaction(session, entry.getEventId());

            if (orderItem != null) {
                addOrderToCache(orderItem.getOrderKey(), orderItem);
            }
            if (item != null) {
                addOrderStepToCache(item.getOrderKey(), item);
            }
        }
    }

    private void orderOutWritten(SOSHibernateSession session, Entry entry, Long outType) throws Exception {
        DBItemSchedulerOrderStepHistory item = getOrderStepHistory(session, entry.getKey());
        if (item.getEndTime() == null) {
            // storeLog(session, orderKey, orderHistoryId, orderStepHistoryId, logType, logLevel, outType, jobPath, agentUri, agentTimezone, chunkTimestamp,
            // chunk);
            Date chunkTimestamp = entry.getTimestamp() == null ? entry.getEventIdAsDate() : entry.getTimestampAsDate();
            storeLog(session, item.getOrderKey(), item.getMainOrderHistoryId(), item.getOrderHistoryId(), item.getId(), new Long(4), new Long(0),
                    outType, item.getJobPath(), item.getAgentUri(), ".", chunkTimestamp, entry.getChunk());// TODO
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[%s][skip][%s]order step is already ended. log already written...[%s]", identifier, entry.getType(),
                        entry.getKey(), SOSString.toString(item)));
            }
        }

    }

    private void orderProcessed(SOSHibernateSession session, Entry entry) throws Exception {
        DBItemSchedulerOrderStepHistory item = getOrderStepHistory(session, entry.getKey());
        if (item.getEndTime() == null) {
            item.setEndTime(entry.getTimestamp() == null ? entry.getEventIdAsDate() : entry.getTimestampAsDate());
            item.setEndEventId(String.valueOf(entry.getEventId()));
            item.setEndParameters(EventMeta.map2Json(entry.getVariables()));
            item.setReturnCode(entry.getOutcome().getReturnCode());
            item.setState(entry.getOutcome().getType());
            item.setModified(SOSDate.getCurrentDateUTC());

            session.update(item);

            // storeLog(session, orderKey, orderHistoryId, orderStepHistoryId, logType, logLevel, outType, jobPath, agentUri, agentTimezone, chunkTimestamp,
            // chunk);
            storeLog(session, item.getOrderKey(), item.getMainOrderHistoryId(), item.getOrderHistoryId(), item.getId(), new Long(2), new Long(0),
                    new Long(0), item.getJobPath(), item.getAgentUri(), ".", item.getEndTime(), String.format(
                            "order step ended: %s, jobPath=%s, agentUri=%s, workflowPosition=%s(version=%s)", item.getOrderKey(), item.getJobPath(),
                            item.getAgentUri(), item.getWorkflowPosition(), item.getWorkflowVersion()));// TODO);// TODO

        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[%s][skip][%s]order step is already ended[%s]", identifier, entry.getType(), entry.getKey(), SOSString
                        .toString(item)));
            }
        }
        tryHandleTransaction(session, entry.getEventId());
        clearCache(item.getOrderKey(), CacheType.orderStep);
    }

    private void orderFinished(SOSHibernateSession session, Entry entry) throws Exception {
        DBItemSchedulerOrderHistory item = getOrderHistory(session, entry.getKey());
        if (item.getEndTime() == null) {
            item.setEndTime(entry.getTimestamp() == null ? entry.getEventIdAsDate() : entry.getTimestampAsDate());

            DBItemSchedulerOrderStepHistory stepItem = dbLayer.getOrderStepHistoryById(session, item.getCurrentStepId());

            item.setEndWorkflowPosition(stepItem.getWorkflowPosition());
            item.setEndStepId(stepItem.getId());
            item.setEndEventId(String.valueOf(entry.getEventId()));
            item.setState("finished");// TODO
            item.setError(stepItem.getError());
            item.setErrorCode(stepItem.getErrorCode());
            item.setErrorText(stepItem.getErrorText());
            item.setModified(SOSDate.getCurrentDateUTC());

            session.update(item);

            // storeLog(session, orderKey, orderHistoryId, orderStepHistoryId, logType, logLevel, outType, jobPath, agentUri, agentTimezone, chunkTimestamp,
            // chunk);
            storeLog(session, item.getOrderKey(), item.getMainParentId(), item.getId(), new Long(0), new Long(3), new Long(0), new Long(0), ".", ".",
                    ".", item.getEndTime(), String.format("order finished: %s, workflowPath=%s(version=%s)", item.getOrderKey(), item
                            .getWorkflowPath(), item.getWorkflowVersion()));// TODO
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[%s][skip][%s]order is already ended[%s]", identifier, entry.getType(), entry.getKey(), SOSString
                        .toString(item)));
            }
        }
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
            LOGGER.debug(String.format("%s[addOrderStepToCache][%s]jobPath=%s, workflowPosition=%s", identifier, orderKey, item.getJobPath(), item
                    .getWorkflowPosition()));
        }
        orderSteps.put(orderKey, item);
    }

    private DBItemSchedulerOrderHistory getOrderFromCache(String orderKey) {
        if (orders.containsKey(orderKey)) {
            DBItemSchedulerOrderHistory item = orders.get(orderKey);
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[getOrderFromCache][%s]%s", identifier, orderKey, SOSString.toString(item)));
            }
            return item;
        }
        return null;
    }

    private DBItemSchedulerOrderStepHistory getOrderStepFromCache(String orderKey) {
        if (orderSteps.containsKey(orderKey)) {
            DBItemSchedulerOrderStepHistory item = orderSteps.get(orderKey);
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[getOrderStepFromCache][%s]%s", identifier, orderKey, SOSString.toString(item)));
            }
            return item;
        }
        return null;
    }

    private void clearCache(String orderKey, CacheType cacheType) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[clearCache][%s]cacheType=%s", identifier, orderKey, cacheType));
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
            session.setIdentifier(identifier);

            session.beginTransaction();
            schedulerSettings = dbLayer.getSchedulerSettings(session);
            if (schedulerSettings == null) {
                schedulerSettings = dbLayer.insertSchedulerSettings(session, "0");
            }
            session.commit();
            LOGGER.info(String.format("%s eventId=%s", identifier, schedulerSettings.getTextValue()));
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
                throw new Exception(String.format("%s order not found. orderKey=%s", identifier, orderKey));
            } else {
                addOrderToCache(orderKey, item);
            }
        }
        return item;
    }

    private DBItemSchedulerOrderHistory getOrderHistoryByStartEventId(SOSHibernateSession session, String orderKey, String startEventId)
            throws Exception {
        DBItemSchedulerOrderHistory item = dbLayer.getOrderHistory(session, masterSettings.getSchedulerId(), orderKey, startEventId);
        if (item == null) {
            throw new Exception(String.format("%s order not found. orderKey=%s, startEventId=%s", identifier, orderKey, startEventId));
        } else {
            addOrderToCache(orderKey, item);
        }
        return item;
    }

    private DBItemSchedulerOrderStepHistory getOrderStepHistory(SOSHibernateSession session, String orderKey) throws Exception {
        DBItemSchedulerOrderStepHistory item = getOrderStepFromCache(orderKey);
        if (item == null) {
            item = dbLayer.getOrderStepHistory(session, masterSettings.getSchedulerId(), orderKey);
            if (item == null) {
                throw new Exception(String.format("%s order step not found. orderKey=%s", identifier, orderKey));
            } else {
                addOrderStepToCache(orderKey, item);
            }
        }
        return item;
    }

    private DBItemSchedulerOrderStepHistory getOrderStepHistoryByStartEventId(SOSHibernateSession session, String orderKey, String startEventId)
            throws Exception {
        DBItemSchedulerOrderStepHistory item = dbLayer.getOrderStepHistory(session, masterSettings.getSchedulerId(), orderKey, startEventId);
        if (item == null) {
            throw new Exception(String.format("%s order step not found. orderKey=%s, startEventId", identifier, orderKey, startEventId));
        } else {
            addOrderStepToCache(orderKey, item);
        }

        return item;
    }

    private void storeLog(SOSHibernateSession session, String orderKey, Long mainOrderHistoryId, Long orderHistoryId, Long orderStepHistoryId,
            Long logType, Long logLevel, Long outType, String jobPath, String agentUri, String agentTimezone, Date chunkTimestamp, String chunk)
            throws Exception {

        String[] arr = chunk.split("\\r?\\n");
        for (int i = 0; i < arr.length; i++) {
            DBItemSchedulerLogs item = new DBItemSchedulerLogs();

            item.setSchedulerId(masterSettings.getSchedulerId());
            item.setOrderKey(orderKey);
            item.setMainOrderHistoryId(mainOrderHistoryId);
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

    public boolean isLocked() {
        return isLocked;
    }
}
