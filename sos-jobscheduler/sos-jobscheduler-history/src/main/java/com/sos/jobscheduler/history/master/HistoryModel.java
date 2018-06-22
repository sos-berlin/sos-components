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
import com.sos.jobscheduler.history.helper.CachedOrder;
import com.sos.jobscheduler.history.helper.CachedOrderStep;
import com.sos.jobscheduler.history.helper.ChunkLogEntry;
import com.sos.jobscheduler.history.helper.ChunkParser;
import com.sos.jobscheduler.history.helper.HistoryUtil;

public class HistoryModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryModel.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final long MAX_LOCK_VERSION = 10_000_000;
    private final SOSHibernateFactory dbFactory;
    private final DBLayerHistory dbLayer;
    private final String schedulerId;
    private final String identifier;
    private Map<String, CachedOrder> cachedOrders;
    private Map<String, CachedOrderStep> cachedOrderSteps;
    private DBItemSchedulerSettings schedulerSettings;
    private Long storedEventId;
    private boolean closed = false;
    private int maxTransactions = 100;
    private long transactionCounter;

    private static enum CacheType {
        order, orderStep
    };

    public HistoryModel(SOSHibernateFactory factory, EventHandlerMasterSettings ms, String ident) {
        dbFactory = factory;
        schedulerId = ms.getSchedulerId();
        dbLayer = new DBLayerHistory("history_" + schedulerId);
        identifier = ident;

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public Long getEventId() throws Exception {
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
            return Long.parseLong(schedulerSettings.getTextValue());
        } catch (Exception e) {
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public Long process(Event event) {
        String method = "process";
        cachedOrders = new HashMap<String, CachedOrder>();
        cachedOrderSteps = new HashMap<String, CachedOrderStep>();
        closed = false;
        transactionCounter = 0;
        SOSHibernateSession session = null;
        Long lastSuccessEventId = new Long(0);
        int processedEventsCounter = 0;
        int total = event.getStamped().size();
        try {
            LOGGER.info(String.format("[%s][%s][start]storedEventId=%s, %s events", identifier, method, storedEventId, total));

            session = dbFactory.openStatelessSession();
            session.setIdentifier(identifier);
            session.beginTransaction();

            for (IEntry en : event.getStamped()) {
                if (closed) {// TODO
                    LOGGER.info(String.format("[%s][%s][skip]is closed", identifier, method));
                    break;
                }
                Entry entry = (Entry) en;
                Long eventId = entry.getEventId();
                if (storedEventId > eventId) {// TODO must be >= instead of > (workaround for: eventId by fork is the same)
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][%s][skip][%s] stored eventId=%s > current eventId=%s %s", identifier, method, entry
                                .getType(), entry.getKey(), storedEventId, eventId, SOSString.toString(entry)));
                    }
                    processedEventsCounter++;
                    continue;
                }

                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s]%s", identifier, method, entry.getType(), SOSString.toString(entry)));
                }
                transactionCounter++;
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
                processedEventsCounter++;
                lastSuccessEventId = eventId;
                if (isDebugEnabled) {
                    LOGGER.debug("---------------------------------------------");
                }
            }

            tryStoreCurrentStateAtEnd(session, lastSuccessEventId);

            LOGGER.info(String.format("[%s][%s][end]storedEventId=%s, %s of %s events processed", identifier, method, storedEventId,
                    processedEventsCounter, total));
        } catch (Exception e) {
            LOGGER.error(String.format("[%s][%s][end]%s", identifier, method, e.toString()), e);
            try {
                tryStoreCurrentStateAtEnd(session, lastSuccessEventId);
                LOGGER.info(String.format("[%s][%s][end][on_error]storedEventId=%s, %s of %s events processed", identifier, method, storedEventId,
                        processedEventsCounter, total));
            } catch (Exception e1) {
                LOGGER.error(String.format("[%s][%s][end][on_error]error on store lastSuccessEventId=%s: %s", identifier, method, lastSuccessEventId,
                        e.toString()), e);
                LOGGER.info(String.format("[%s][%s][end][on_error]storedEventId=%s, %s of %s events processed", identifier, method, storedEventId,
                        processedEventsCounter, total));
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

    private void tryStoreCurrentState(SOSHibernateSession session, Long eventId) throws Exception {
        if (transactionCounter % maxTransactions == 0 && session.isTransactionOpened()) {
            updateSchedulerSettings(session, eventId);
            session.commit();
            session.beginTransaction();
        }
    }

    private void tryStoreCurrentStateAtEnd(SOSHibernateSession session, Long eventId) throws Exception {
        if (eventId > 0 && storedEventId != eventId) {
            if (!session.isTransactionOpened()) {
                session.beginTransaction();
            }
            updateSchedulerSettings(session, eventId);
            session.commit();
        }
    }

    private void updateSchedulerSettings(SOSHibernateSession session, Long eventId) throws Exception {
        dbLayer.updateSchedulerSettings(session, schedulerSettings, eventId);
        if (schedulerSettings.getLockVersion() > MAX_LOCK_VERSION) {
            dbLayer.resetLockVersion(session, schedulerSettings.getName());
        }
        storedEventId = eventId;
    }

    private void orderAdd(SOSHibernateSession session, Entry entry) throws Exception {

        try {
            DBItemSchedulerOrderHistory item = new DBItemSchedulerOrderHistory();
            item.setSchedulerId(schedulerId);
            item.setOrderKey(entry.getKey());
            item.setWorkflowPosition(entry.getWorkflowPosition().getOrderPositionAsString());
            item.setRetryCounter(new Long(0));// TODO

            if (entry.getParent() == null) {
                item.setMainParentId(new Long(0));// TODO see below
                item.setParentId(new Long(0));
                item.setParentOrderKey(null);
            } else {
                CachedOrder pco = getCachedOrder(session, entry.getParent());
                if (!pco.getHasChildren()) {
                    pco.setHasChildren(true);
                    dbLayer.setHasChildren(session, pco.getId());
                    addCachedOrder(pco.getOrderKey(), pco);
                }
                item.setMainParentId(pco.getMainParentId());
                item.setParentId(pco.getId());
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
            item.setStartTime(new Date(0));// 1970-01-01 01:00:00 TODO
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
                dbLayer.setMainParentId(session, item.getId(), item.getMainParentId());
            }

            CachedOrder co = new CachedOrder(item);
            storeLog(session, new ChunkLogEntry(LogLevel.Debug, OutType.Stdout, LogType.OrderAdded, entry, co));
            tryStoreCurrentState(session, entry.getEventId());

            addCachedOrder(item.getOrderKey(), co);
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                LOGGER.error(e.toString(), e);
                throw e;
            }
            LOGGER.debug(String.format("[%s][%s][%s]%s", identifier, entry.getType(), entry.getKey(), e.toString()));
            addCachedOrderByStartEventId(session, entry.getKey(), String.valueOf(entry.getEventId()));
        }
    }

    private void orderEnd(SOSHibernateSession session, Entry entry) throws Exception {
        CachedOrder co = getCachedOrder(session, entry.getKey());
        if (co.getEndTime() == null) {
            Date endTime = entry.getTimestamp() == null ? entry.getEventIdAsDate() : entry.getTimestampAsDate();
            DBItemSchedulerOrderStepHistory stepItem = dbLayer.getOrderStepHistoryById(session, co.getCurrentStepId());
            // TODO finished
            dbLayer.setOrderEnd(session, co.getId(), endTime, stepItem.getWorkflowPosition(), stepItem.getId(), String.valueOf(entry.getEventId()),
                    "finished", stepItem.getError(), stepItem.getErrorCode(), stepItem.getErrorText(), new Date());

            storeLog(session, new ChunkLogEntry(LogLevel.Info, OutType.Stdout, LogType.OrderEnd, entry, co));

            tryStoreCurrentState(session, entry.getEventId());
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][skip][%s]order is already ended[%s]", identifier, entry.getType(), entry.getKey(), SOSString
                        .toString(co)));
            }
        }
        clearCache(co.getOrderKey(), CacheType.order);
    }

    private void orderStepStart(SOSHibernateSession session, Entry entry) throws Exception {
        CachedOrder co = null;
        CachedOrderStep cos = null;
        boolean isOrderStart = false;
        try {
            co = getCachedOrder(session, entry.getKey());

            DBItemSchedulerOrderStepHistory item = new DBItemSchedulerOrderStepHistory();
            item.setSchedulerId(schedulerId);
            item.setOrderKey(entry.getKey());
            item.setWorkflowPosition(entry.getWorkflowPosition().getPositionAsString());
            item.setRetryCounter(new Long(0));// TODO

            item.setConstraintHash(hashOrderConstaint(entry));
            item.setMainOrderHistoryId(co.getMainParentId());
            item.setOrderHistoryId(co.getId());

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

            Date orderStartTime = null;
            String orderState = null;
            if (item.getWorkflowPosition().equals(co.getStartWorkflowPosition())) {// + order.startTime != default
                orderStartTime = item.getStartTime();
                orderState = "started";// TODO
                isOrderStart = true;
            }
            co.setCurrentStepId(item.getId());

            dbLayer.updateOrderOnOrderStep(session, co.getId(), orderStartTime, orderState, co.getCurrentStepId(), new Date());

            addCachedOrder(co.getOrderKey(), co);
            cos = new CachedOrderStep(item);
            addCachedOrderStep(item.getOrderKey(), cos);
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                LOGGER.error(e.toString(), e);
                throw e;
            }
            LOGGER.debug(String.format("[%s][%s][%s]%s", identifier, entry.getType(), entry.getKey(), e.toString()));
            if (co != null) {
                addCachedOrder(co.getOrderKey(), co);
            }
            addCachedOrderStepByStartEventId(session, entry.getKey(), String.valueOf(entry.getEventId()));
        }
        if (cos != null) {// inserted
            if (isOrderStart) {
                storeLog(session, new ChunkLogEntry(LogLevel.Info, OutType.Stdout, LogType.OrderStart, entry, co));
            }
            storeLog(session, new ChunkLogEntry(LogLevel.Info, OutType.Stdout, LogType.OrderStepStart, entry, cos));
            tryStoreCurrentState(session, entry.getEventId());
        }
    }

    private void orderStepEnd(SOSHibernateSession session, Entry entry) throws Exception {
        CachedOrderStep cos = getCachedOrderStep(session, entry.getKey());
        if (cos.getEndTime() == null) {
            Date endTime = entry.getTimestamp() == null ? entry.getEventIdAsDate() : entry.getTimestampAsDate();
            dbLayer.setOrderStepEnd(session, cos.getId(), endTime, String.valueOf(entry.getEventId()), EventMeta.map2Json(entry.getVariables()), entry
                    .getOutcome().getReturnCode(), entry.getOutcome().getType(), new Date());

            storeLog(session, new ChunkLogEntry(LogLevel.Info, OutType.Stdout, LogType.OrderStepEnd, entry, cos));

            tryStoreCurrentState(session, entry.getEventId());
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][skip][%s]order step is already ended[%s]", identifier, entry.getType(), entry.getKey(), SOSString
                        .toString(cos)));
            }
        }
        clearCache(cos.getOrderKey(), CacheType.orderStep);
    }

    private void orderStepStd(SOSHibernateSession session, Entry entry, OutType outType) throws Exception {
        CachedOrderStep cos = getCachedOrderStep(session, entry.getKey());
        if (cos.getEndTime() == null) {
            storeLog(session, new ChunkLogEntry(LogLevel.Info, outType, LogType.OrderStepStd, entry, cos));

            tryStoreCurrentState(session, entry.getEventId());
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][skip][%s]order step is already ended. log already written...[%s]", identifier, entry.getType(),
                        entry.getKey(), SOSString.toString(cos)));
            }
        }
    }

    private void addCachedOrder(String orderKey, CachedOrder order) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][addCachedOrder][%s]workflowPosition=%s", identifier, orderKey, order.getWorkflowPosition()));
        }
        cachedOrders.put(orderKey, order);
    }

    private CachedOrder getCachedOrder(SOSHibernateSession session, String orderKey) throws Exception {
        CachedOrder co = getCachedOrder(orderKey);
        if (co == null) {
            DBItemSchedulerOrderHistory item = dbLayer.getOrderHistory(session, schedulerId, orderKey);
            if (item == null) {
                throw new Exception(String.format("[%s]order not found. orderKey=%s", identifier, orderKey));
            } else {
                co = new CachedOrder(item);
                addCachedOrder(orderKey, co);
            }
        }
        return co;
    }

    private CachedOrder getCachedOrder(String orderKey) {
        if (cachedOrders.containsKey(orderKey)) {
            CachedOrder co = cachedOrders.get(orderKey);
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][getCachedOrder][%s]%s", identifier, orderKey, SOSString.toString(co)));
            }
            return co;
        }
        return null;
    }

    private void addCachedOrderByStartEventId(SOSHibernateSession session, String orderKey, String startEventId) throws Exception {
        DBItemSchedulerOrderHistory item = dbLayer.getOrderHistory(session, schedulerId, orderKey, startEventId);
        if (item == null) {
            throw new Exception(String.format("[%s]order not found. orderKey=%s, startEventId=%s", identifier, orderKey, startEventId));
        } else {
            addCachedOrder(orderKey, new CachedOrder(item));
        }
    }

    private void addCachedOrderStep(String orderKey, CachedOrderStep cos) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][addCachedOrderStep][%s]jobPath=%s, workflowPosition=%s", identifier, orderKey, cos.getJobPath(), cos
                    .getWorkflowPosition()));
        }
        cachedOrderSteps.put(orderKey, cos);
    }

    private CachedOrderStep getCachedOrderStep(SOSHibernateSession session, String orderKey) throws Exception {
        CachedOrderStep cos = getCachedOrderStep(orderKey);
        if (cos == null) {
            DBItemSchedulerOrderStepHistory item = dbLayer.getOrderStepHistory(session, schedulerId, orderKey);
            if (item == null) {
                throw new Exception(String.format("[%s]order step not found. orderKey=%s", identifier, orderKey));
            } else {
                cos = new CachedOrderStep(item);
                addCachedOrderStep(orderKey, cos);
            }
        }
        return cos;
    }

    private CachedOrderStep getCachedOrderStep(String orderKey) {
        if (cachedOrderSteps.containsKey(orderKey)) {
            CachedOrderStep cos = cachedOrderSteps.get(orderKey);
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][getCachedOrderStep][%s]%s", identifier, orderKey, SOSString.toString(cos)));
            }
            return cos;
        }
        return null;
    }

    private void addCachedOrderStepByStartEventId(SOSHibernateSession session, String orderKey, String startEventId) throws Exception {
        DBItemSchedulerOrderStepHistory item = dbLayer.getOrderStepHistory(session, schedulerId, orderKey, startEventId);
        if (item == null) {
            throw new Exception(String.format("[%s]order step not found. orderKey=%s, startEventId", identifier, orderKey, startEventId));
        } else {
            addCachedOrderStep(orderKey, new CachedOrderStep(item));
        }
    }

    private void clearCache(String orderKey, CacheType cacheType) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][clearCache][%s]cacheType=%s", identifier, orderKey, cacheType));
        }
        switch (cacheType) {
        case orderStep:
            cachedOrderSteps.entrySet().removeIf(entry -> entry.getKey().equals(orderKey));
            break;
        case order:
            cachedOrders.entrySet().removeIf(entry -> entry.getKey().startsWith(orderKey));
            cachedOrderSteps.entrySet().removeIf(entry -> entry.getKey().startsWith(orderKey));
            break;
        }
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
            item.setSchedulerId(schedulerId);
            item.setOrderKey(logEntry.getOrderKey());
            item.setMainOrderHistoryId(logEntry.getMainOrderHistoryId());
            item.setOrderHistoryId(logEntry.getOrderHistoryId());
            item.setOrderStepHistoryId(logEntry.getOrderStepHistoryId());
            item.setLogType(logEntry.getLogType().getValue());
            item.setOutType(logEntry.getOutType().getValue());
            item.setLogLevel(logLevel.getValue());
            item.setJobPath(logEntry.getJobPath());
            item.setAgentUri(logEntry.getAgentUri());
            item.setTimezone(logEntry.getTimezone());
            item.setEventId(String.valueOf(logEntry.getEventId()));
            item.setChunkTimestamp(chunkTimestamp);
            item.setChunk(arr[i]);
            item.setConstraintHash(hashLogConstaint(logEntry, i));

            item.setCreated(new Date());

            if (i > 0) {
                transactionCounter++;
            }

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
        // return HistoryUtil.hashString(schedulerId + String.valueOf(entry.getEventId())); //MUST BE
        return HistoryUtil.hashString(schedulerId + String.valueOf(entry.getEventId()) + entry.getKey()); // TODO
    }

    private String hashLogConstaint(ChunkLogEntry logEntry, int i) {
        return HistoryUtil.hashString(schedulerId + String.valueOf(logEntry.getEventId()) + logEntry.getOrderKey() + logEntry.getLogType().name()
                + String.valueOf(i));
    }

    public void setStoredEventId(Long eventId) {
        storedEventId = eventId;
    }

    public Long getStoredEventId() {
        return storedEventId;
    }

    public void setMaxTransactions(int val) {
        maxTransactions = val;
    }
}
