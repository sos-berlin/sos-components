package com.sos.jobscheduler.history.master;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateObjectOperationException;
import com.sos.commons.hibernate.exception.SOSHibernateObjectOperationStaleStateException;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.db.DBItemSchedulerLogs;
import com.sos.jobscheduler.db.DBItemSchedulerLogs.LogLevel;
import com.sos.jobscheduler.db.DBItemSchedulerLogs.LogType;
import com.sos.jobscheduler.db.DBItemSchedulerLogs.OutType;
import com.sos.jobscheduler.db.DBItemSchedulerOrderHistory;
import com.sos.jobscheduler.db.DBItemSchedulerOrderStepHistory;
import com.sos.jobscheduler.db.DBItemSchedulerSettings;
import com.sos.jobscheduler.event.master.EventMeta;
import com.sos.jobscheduler.event.master.bean.Event;
import com.sos.jobscheduler.event.master.bean.IEntry;
import com.sos.jobscheduler.event.master.fatevent.bean.Entry;
import com.sos.jobscheduler.event.master.handler.EventHandlerMasterSettings;
import com.sos.jobscheduler.history.db.DBLayerHistory;
import com.sos.jobscheduler.history.helper.ChunkLogEntry;
import com.sos.jobscheduler.history.helper.ChunkParser;
import com.sos.jobscheduler.history.helper.HistoryUtil;

public class HistoryModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryModel.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
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
    private boolean closed = false;

    private static enum CacheType {
        order, orderStep
    };

    public HistoryModel(SOSHibernateFactory factory, EventHandlerMasterSettings ms, String ident) {
        dbFactory = factory;
        masterSettings = ms;
        dbLayer = new DBLayerHistory("history_" + masterSettings.getSchedulerId());
        identifier = ident;

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public Long getEventId() {
        isLocked = false;
        lockCause = null;
        SOSHibernateSession session = null;
        try {
            session = dbFactory.openStatelessSession();
            session.setIdentifier(identifier);

            schedulerSettings = dbLayer.getSchedulerSettings(session);
            if (schedulerSettings == null) {
                schedulerSettings = dbLayer.insertSchedulerSettings(session, "0");
            }
            LOGGER.info(String.format("[%s][getEventId]start eventId=%s", identifier, schedulerSettings.getTextValue()));
            storedEventId = Long.parseLong(schedulerSettings.getTextValue());
            return storedEventId;
        } catch (SOSHibernateObjectOperationStaleStateException e) {
            isLocked = true;
            lockCause = "locked by an another instance";
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return new Long(0);
    }

    public Long process(Event event) {
        String method = "process";
        orders = new HashMap<String, DBItemSchedulerOrderHistory>();
        orderSteps = new HashMap<String, DBItemSchedulerOrderStepHistory>();
        closed = false;
        SOSHibernateSession session = null;
        Long lastSuccessEventId = new Long(0);
        int counter = 0;
        int total = event.getStamped().size();
        try {
            LOGGER.info(String.format("[%s][%s][start]storedEventId=%s, %s events", identifier, method, storedEventId, total));

            session = dbFactory.openStatelessSession();
            session.setIdentifier(identifier);
            for (IEntry en : event.getStamped()) {
                if (closed) {// TODO
                    LOGGER.info(String.format("[%s][%s][skip]is closed", identifier, method));
                    break;
                }
                counter++;
                Entry entry = (Entry) en;
                Long eventId = entry.getEventId();
                if (storedEventId > eventId) {// TODO must be >= instead of > (workaround for: eventId by fork is the same)
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][%s][skip][%s] stored eventId=%s > current eventId=%s %s", identifier, method, entry
                                .getType(), entry.getKey(), storedEventId, eventId, SOSString.toString(entry)));
                    }
                    continue;
                }

                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s]%s", identifier, method, entry.getType(), SOSString.toString(entry)));
                }

                switch (entry.getType()) {
                case OrderAddedFat:
                    orderAdd(session, entry);
                    break;
                case OrderProcessingStartedFat:
                    orderStepStart(session, entry);
                    break;
                case OrderStdoutWrittenFat:
                    orderStepStd(session, entry, OutType.Stdout);
                    break;
                case OrderStderrWrittenFat:
                    orderStepStd(session, entry, OutType.Stderr);
                    break;
                case OrderProcessedFat:
                    orderStepEnd(session, entry);
                    break;
                case OrderFinishedFat:
                    orderEnd(session, entry);
                    break;
                }
                lastSuccessEventId = eventId;
                LOGGER.debug("---------------------------------------------");
            }

            updateSchedulerSettings(session, lastSuccessEventId);

            LOGGER.info(String.format("[%s][%s][end]storedEventId=%s, %s of %s events processed", identifier, method, storedEventId, counter, total));
        } catch (Exception e) {
            LOGGER.error(String.format("[%s][%s][end]%s", identifier, method, e.toString()), e);
            try {
                updateSchedulerSettings(session, lastSuccessEventId);
                LOGGER.info(String.format("[%s][%s][end]storedEventId=%s, %s of %s events processed", identifier, method, storedEventId, counter,
                        total));
            } catch (Exception e1) {
                LOGGER.error(String.format("[%s][%s][end]error on store lastSuccessEventId=%s: %s", identifier, method, lastSuccessEventId, e
                        .toString()), e);
                LOGGER.info(String.format("[%s][%s][end]storedEventId=%s, %s of %s events processed", identifier, method, storedEventId, counter,
                        total));
            }
        } finally {
            closed = true;
            if (session != null) {
                session.close();
            }
        }
        return storedEventId;
    }

    public void close() {
        closed = true;
    }

    private void orderAdd(SOSHibernateSession session, Entry entry) throws Exception {

        try {
            DBItemSchedulerOrderHistory item = new DBItemSchedulerOrderHistory();
            item.setSchedulerId(masterSettings.getSchedulerId());
            item.setOrderKey(entry.getKey());
            item.setWorkflowPosition(entry.getWorkflowPosition().getOrderPositionAsString());
            item.setRetryCounter(new Long(0));// TODO

            if (entry.getParent() == null) {
                item.setMainParentId(new Long(0));// TODO see below
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

            item.setConstraintHash(hashOrderConstaint(entry));
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
            item.setStartTime(new Date(0));// TODO
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
            item.setCreated(new Date());
            item.setModified(item.getCreated());

            session.save(item);
            if (item.getMainParentId().equals(new Long(0))) {// TODO see above
                item.setMainParentId(item.getId());
                session.update(item);
            }
            addOrderToCache(item.getOrderKey(), item);

            storeLog(session, new ChunkLogEntry(LogLevel.Debug, OutType.Stdout, LogType.OrderAdded, entry, item));

        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                LOGGER.error(e.toString(), e);
                throw e;
            }
            LOGGER.warn(String.format("[%s][%s][%s]%s", identifier, entry.getType(), entry.getKey(), e.toString()));
            getOrderHistoryByStartEventId(session, entry.getKey(), String.valueOf(entry.getEventId()));
        }
    }

    private void orderEnd(SOSHibernateSession session, Entry entry) throws Exception {
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
            item.setModified(new Date());

            session.update(item);

            storeLog(session, new ChunkLogEntry(LogLevel.Info, OutType.Stdout, LogType.OrderEnd, entry, item));

        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][skip][%s]order is already ended[%s]", identifier, entry.getType(), entry.getKey(), SOSString
                        .toString(item)));
            }
        }
        clearCache(item.getOrderKey(), CacheType.order);
    }

    private void orderStepStart(SOSHibernateSession session, Entry entry) throws Exception {
        DBItemSchedulerOrderStepHistory item = null;
        DBItemSchedulerOrderHistory orderItem = null;
        boolean hasError = false;
        boolean isOrderStart = false;
        try {
            orderItem = getOrderHistory(session, entry.getKey());

            item = new DBItemSchedulerOrderStepHistory();
            item.setSchedulerId(masterSettings.getSchedulerId());
            item.setOrderKey(entry.getKey());
            item.setWorkflowPosition(entry.getWorkflowPosition().getPositionAsString());
            item.setRetryCounter(new Long(0));// TODO

            item.setConstraintHash(hashOrderConstaint(entry));
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
            item.setCreated(new Date());
            item.setModified(item.getCreated());

            session.save(item);

            if (item.getWorkflowPosition().equals(orderItem.getStartWorkflowPosition())) {// + order.startTime != default
                orderItem.setStartTime(item.getStartTime());
                orderItem.setState("started");// TODO
                isOrderStart = true;
            }
            orderItem.setCurrentStepId(item.getId());
            orderItem.setModified(new Date());
            session.update(orderItem);
            // tryHandleTransaction(session, entry.getEventId());

            addOrderToCache(orderItem.getOrderKey(), orderItem);
            addOrderStepToCache(item.getOrderKey(), item);

        } catch (SOSHibernateObjectOperationException e) {
            hasError = true;
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                LOGGER.error(e.toString(), e);
                throw e;
            }
            LOGGER.warn(String.format("[%s][%s][%s]%s", identifier, entry.getType(), entry.getKey(), e.toString()));

            item = getOrderStepHistoryByStartEventId(session, entry.getKey(), String.valueOf(entry.getEventId()));
            // tryHandleTransaction(session, entry.getEventId());

            if (orderItem != null) {
                addOrderToCache(orderItem.getOrderKey(), orderItem);
            }
            if (item != null) {
                addOrderStepToCache(item.getOrderKey(), item);
            }
        }
        if (!hasError) {
            if (isOrderStart) {
                storeLog(session, new ChunkLogEntry(LogLevel.Info, OutType.Stdout, LogType.OrderStart, entry, orderItem));
            }
            storeLog(session, new ChunkLogEntry(LogLevel.Info, OutType.Stdout, LogType.OrderStepStart, entry, item));
        }
    }

    private void orderStepEnd(SOSHibernateSession session, Entry entry) throws Exception {
        DBItemSchedulerOrderStepHistory item = getOrderStepHistory(session, entry.getKey());
        if (item.getEndTime() == null) {
            item.setEndTime(entry.getTimestamp() == null ? entry.getEventIdAsDate() : entry.getTimestampAsDate());
            item.setEndEventId(String.valueOf(entry.getEventId()));
            item.setEndParameters(EventMeta.map2Json(entry.getVariables()));
            item.setReturnCode(entry.getOutcome().getReturnCode());
            item.setState(entry.getOutcome().getType());
            item.setModified(new Date());

            session.update(item);

            storeLog(session, new ChunkLogEntry(LogLevel.Info, OutType.Stdout, LogType.OrderStepEnd, entry, item));
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][skip][%s]order step is already ended[%s]", identifier, entry.getType(), entry.getKey(), SOSString
                        .toString(item)));
            }
        }
        clearCache(item.getOrderKey(), CacheType.orderStep);
    }

    private void orderStepStd(SOSHibernateSession session, Entry entry, OutType outType) throws Exception {
        DBItemSchedulerOrderStepHistory item = getOrderStepHistory(session, entry.getKey());
        if (item.getEndTime() == null) {
            storeLog(session, new ChunkLogEntry(LogLevel.Info, outType, LogType.OrderStepStd, entry, item));
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][skip][%s]order step is already ended. log already written...[%s]", identifier, entry.getType(),
                        entry.getKey(), SOSString.toString(item)));
            }
        }
    }

    private void updateSchedulerSettings(SOSHibernateSession session, Long eventId) throws Exception {
        if (eventId > 0 && storedEventId != eventId) {
            dbLayer.updateSchedulerSettings(session, schedulerSettings, eventId);
            storedEventId = eventId;
        }
    }

    private void addOrderToCache(String orderKey, DBItemSchedulerOrderHistory item) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][addOrderToCache][%s]workflowPosition=%s", identifier, orderKey, item.getWorkflowPosition()));
        }
        orders.put(orderKey, item);
    }

    private void addOrderStepToCache(String orderKey, DBItemSchedulerOrderStepHistory item) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][addOrderStepToCache][%s]jobPath=%s, workflowPosition=%s", identifier, orderKey, item.getJobPath(), item
                    .getWorkflowPosition()));
        }
        orderSteps.put(orderKey, item);
    }

    private DBItemSchedulerOrderHistory getOrderFromCache(String orderKey) {
        if (orders.containsKey(orderKey)) {
            DBItemSchedulerOrderHistory item = orders.get(orderKey);
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][getOrderFromCache][%s]%s", identifier, orderKey, SOSString.toString(item)));
            }
            return item;
        }
        return null;
    }

    private DBItemSchedulerOrderStepHistory getOrderStepFromCache(String orderKey) {
        if (orderSteps.containsKey(orderKey)) {
            DBItemSchedulerOrderStepHistory item = orderSteps.get(orderKey);
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][getOrderStepFromCache][%s]%s", identifier, orderKey, SOSString.toString(item)));
            }
            return item;
        }
        return null;
    }

    private void clearCache(String orderKey, CacheType cacheType) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][clearCache][%s]cacheType=%s", identifier, orderKey, cacheType));
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

    private DBItemSchedulerOrderHistory getOrderHistory(SOSHibernateSession session, String orderKey) throws Exception {
        DBItemSchedulerOrderHistory item = getOrderFromCache(orderKey);
        if (item == null) {
            item = dbLayer.getOrderHistory(session, masterSettings.getSchedulerId(), orderKey);
            if (item == null) {
                throw new Exception(String.format("[%s]order not found. orderKey=%s", identifier, orderKey));
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
            throw new Exception(String.format("[%s]order not found. orderKey=%s, startEventId=%s", identifier, orderKey, startEventId));
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
                throw new Exception(String.format("[%s]order step not found. orderKey=%s", identifier, orderKey));
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
            throw new Exception(String.format("[%s]order step not found. orderKey=%s, startEventId", identifier, orderKey, startEventId));
        } else {
            addOrderStepToCache(orderKey, item);
        }

        return item;
    }

    private void storeLog(SOSHibernateSession session, ChunkLogEntry logEntry) throws Exception {

        boolean useParser = logEntry.getLogType().equals(LogType.OrderStepStd);
        String[] arr = logEntry.getChunk().split("\\r?\\n");
        for (int i = 0; i < arr.length; i++) {
            LogLevel logLevel = logEntry.getLogLevel();
            Date chunkTimestamp = logEntry.getDate();
            if (useParser) {
                ChunkParser cp = new ChunkParser(logLevel, chunkTimestamp, arr[i]);
                cp.parse();
                logLevel = cp.getLogLevel();
                chunkTimestamp = cp.getDate();
            }
            DBItemSchedulerLogs item = new DBItemSchedulerLogs();
            item.setSchedulerId(masterSettings.getSchedulerId());
            item.setOrderKey(logEntry.getOrderKey());
            item.setMainOrderHistoryId(logEntry.getMainOrderHistoryId());
            item.setOrderHistoryId(logEntry.getOrderHistoryId());
            item.setOrderStepHistoryId(logEntry.getOrderStepHistoryId());
            item.setLogType(logEntry.getLogType().getValue());
            item.setOutType(logEntry.getOutType().getValue());
            item.setLogLevel(logLevel.getValue());
            item.setJobPath(logEntry.getJobPath());
            item.setAgentUri(logEntry.getAgentUri());
            item.setAgentTimezone(logEntry.getAgentTimezone());
            item.setChunkTimestamp(chunkTimestamp);
            item.setChunk(arr[i]);
            item.setConstraintHash(hashLogConstaint(logEntry, i));

            item.setCreated(new Date());
            try {
                session.save(item);
            } catch (SOSHibernateObjectOperationException e) {
                Exception cve = SOSHibernate.findConstraintViolationException(e);
                if (cve == null) {
                    LOGGER.error(e.toString(), e);
                    throw e;
                }
                LOGGER.warn(String.format("[%s][%s][%s]%s", identifier, logEntry.getLogType().name(), logEntry.getOrderKey(), e.toString()));
            }
        }
    }

    private String hashOrderConstaint(Entry entry) {
        // return HistoryUtil.hashString(masterSettings.getSchedulerId() + String.valueOf(entry.getEventId())); //MUST BE
        return HistoryUtil.hashString(masterSettings.getSchedulerId() + String.valueOf(entry.getEventId()) + entry.getKey()); // TODO
    }

    private String hashLogConstaint(ChunkLogEntry logEntry, int i) {
        return HistoryUtil.hashString(masterSettings.getSchedulerId() + String.valueOf(logEntry.getEventId()) + logEntry.getOrderKey() + logEntry
                .getLogType().name() + String.valueOf(i));
    }

    public boolean isLocked() {
        return isLocked;
    }
}
