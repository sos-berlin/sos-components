package com.sos.jobscheduler.history.master;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.exception.SOSHibernateObjectOperationException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.db.general.DBItemSetting;
import com.sos.jobscheduler.db.history.DBItemAgent;
import com.sos.jobscheduler.db.history.DBItemLog;
import com.sos.jobscheduler.db.history.DBItemLog.LogLevel;
import com.sos.jobscheduler.db.history.DBItemLog.LogType;
import com.sos.jobscheduler.db.history.DBItemLog.OutType;
import com.sos.jobscheduler.db.history.DBItemMaster;
import com.sos.jobscheduler.db.history.DBItemOrder;
import com.sos.jobscheduler.db.history.DBItemOrderStep;
import com.sos.jobscheduler.event.master.EventMeta;
import com.sos.jobscheduler.event.master.bean.Event;
import com.sos.jobscheduler.event.master.bean.IEntry;
import com.sos.jobscheduler.event.master.fatevent.bean.Entry;
import com.sos.jobscheduler.event.master.handler.EventHandlerMasterSettings;
import com.sos.jobscheduler.history.db.DBLayerHistory;
import com.sos.jobscheduler.history.helper.CachedAgent;
import com.sos.jobscheduler.history.helper.CachedOrder;
import com.sos.jobscheduler.history.helper.CachedOrderStep;
import com.sos.jobscheduler.history.helper.ChunkLogEntry;
import com.sos.jobscheduler.history.helper.ChunkParser;
import com.sos.jobscheduler.history.helper.HistoryUtil;

public class HistoryModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryModel.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();
    private static final long MAX_LOCK_VERSION = 10_000_000;
    private final SOSHibernateFactory dbFactory;
    private EventHandlerMasterSettings masterSettings;
    private final String identifier;
    private DBItemSetting setting;
    private final String settingName;
    private Long storedEventId;
    private boolean closed = false;
    private int maxTransactions = 100;
    private long transactionCounter;
    private String masterTimezone;

    private Map<String, CachedOrder> cachedOrders;
    private Map<String, CachedOrderStep> cachedOrderSteps;
    private Map<String, CachedAgent> cachedAgents;

    private static enum CacheType {
        order, orderStep
    };

    public HistoryModel(SOSHibernateFactory factory, EventHandlerMasterSettings ms, String ident) {
        dbFactory = factory;
        masterSettings = ms;
        identifier = ident;
        settingName = "history_" + masterSettings.getMasterId();
    }

    public Long getEventId() throws Exception {
        DBLayerHistory dbLayer = null;
        try {
            dbLayer = new DBLayerHistory(dbFactory.openStatelessSession());
            dbLayer.getSession().setIdentifier(identifier);
            dbLayer.getSession().beginTransaction();

            setting = dbLayer.getSetting(settingName);
            if (setting == null) {
                setting = dbLayer.insertSetting(settingName, "0");
            }

            dbLayer.getSession().commit();
            return Long.parseLong(setting.getTextValue());
        } catch (Exception e) {
            throw e;
        } finally {
            if (dbLayer != null) {
                dbLayer.close();
            }
        }
    }

    public Long process(Event event, Duration lastRestServiceDuration) {
        String method = "process";

        // TODO initialize on process?
        cachedOrders = new HashMap<String, CachedOrder>();
        cachedOrderSteps = new HashMap<String, CachedOrderStep>();
        cachedAgents = new HashMap<String, CachedAgent>();

        closed = false;
        transactionCounter = 0;

        Long lastSuccessEventId = new Long(0);
        int processedEventsCounter = 0;
        int total = event.getStamped().size();
        Instant start = Instant.now();
        Long startEventId = storedEventId;

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s][start][%s][%s]%s total", identifier, method, storedEventId, start, total));
        }

        DBLayerHistory dbLayer = null;
        try {
            dbLayer = new DBLayerHistory(dbFactory.openStatelessSession());
            dbLayer.getSession().setIdentifier(identifier);
            dbLayer.getSession().beginTransaction();

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

                if (isTraceEnabled) {
                    LOGGER.trace(String.format("[%s][%s][%s]%s", identifier, method, entry.getType(), SOSString.toString(entry)));
                }
                transactionCounter++;
                switch (entry.getType()) {
                case MasterReadyFat:
                    masterAdd(dbLayer, entry);
                    break;
                case AgentReadyFat:
                    agentAdd(dbLayer, entry);
                    break;
                case OrderAddedFat:
                    orderAdd(dbLayer, entry);
                    break;
                case OrderForkedFat:
                    break;
                case OrderJoinedFat:
                    break;
                case OrderProcessingStartedFat:
                    orderStepStart(dbLayer, entry);
                    break;
                case OrderStdoutWrittenFat:
                    orderStepStd(dbLayer, entry, OutType.Stdout);
                    break;
                case OrderStderrWrittenFat:
                    orderStepStd(dbLayer, entry, OutType.Stderr);
                    break;
                case OrderProcessedFat:
                    orderStepEnd(dbLayer, entry);
                    break;
                case OrderFinishedFat:
                    orderEnd(dbLayer, entry);
                    break;
                }
                processedEventsCounter++;
                lastSuccessEventId = eventId;
                if (isDebugEnabled) {
                    LOGGER.debug("---------------------------------------------");
                }
            }

            tryStoreCurrentStateAtEnd(dbLayer, lastSuccessEventId);
        } catch (Exception e) {
            LOGGER.error(String.format("[%s][%s][end]%s", identifier, method, e.toString()), e);
            try {
                tryStoreCurrentStateAtEnd(dbLayer, lastSuccessEventId);
            } catch (Exception e1) {
                LOGGER.error(String.format("[%s][%s][end][on_error]error on store lastSuccessEventId=%s: %s", identifier, method, lastSuccessEventId,
                        e.toString()), e);
            }
        } finally {
            if (dbLayer != null) {
                dbLayer.close();
            }
            cachedOrders = null;
            cachedOrderSteps = null;
            transactionCounter = 0;
            closed = true;
        }

        String startEventIdAsTime = startEventId.equals(new Long(0)) ? "0" : SOSDate.getTime(EventMeta.eventId2Instant(startEventId));
        String endEventIdAsTime = storedEventId.equals(new Long(0)) ? "0" : SOSDate.getTime(EventMeta.eventId2Instant(storedEventId));
        Instant end = Instant.now();

        LOGGER.info(String.format("[%s][%s][%s-%s][%s-%s][%s-%s][%s]%s-%s", identifier, SOSDate.getDuration(lastRestServiceDuration), startEventId,
                storedEventId, startEventIdAsTime, endEventIdAsTime, SOSDate.getTime(start), SOSDate.getTime(end), SOSDate.getDuration(start, end),
                processedEventsCounter, total));

        return storedEventId;
    }

    public void close() {
        closed = true;
    }

    private void tryStoreCurrentState(DBLayerHistory dbLayer, Long eventId) throws Exception {
        if (transactionCounter % maxTransactions == 0 && dbLayer.getSession().isTransactionOpened()) {
            updateSchedulerSettings(dbLayer, eventId);
            dbLayer.getSession().commit();
            dbLayer.getSession().beginTransaction();
        }
    }

    private void tryStoreCurrentStateAtEnd(DBLayerHistory dbLayer, Long eventId) throws Exception {
        if (eventId > 0 && storedEventId != eventId) {
            if (!dbLayer.getSession().isTransactionOpened()) {
                dbLayer.getSession().beginTransaction();
            }
            updateSchedulerSettings(dbLayer, eventId);
            dbLayer.getSession().commit();
        }
    }

    private void updateSchedulerSettings(DBLayerHistory dbLayer, Long eventId) throws Exception {
        dbLayer.updateSetting(setting, eventId);
        if (setting.getLockVersion() > MAX_LOCK_VERSION) {
            dbLayer.resetLockVersion(setting.getName());
        }
        storedEventId = eventId;
    }

    private void masterAdd(DBLayerHistory dbLayer, Entry entry) throws Exception {

        try {
            DBItemMaster item = new DBItemMaster();
            item.setMasterId(entry.getMasterId());
            item.setHostname(masterSettings.getHostname());
            item.setPort(Long.parseLong(masterSettings.getPort()));
            item.setTimezone(entry.getTimezone());
            item.setStartTime(entry.getTimestamp() == null ? entry.getEventIdAsDate() : entry.getTimestampAsDate());
            item.setLastEntry(true);
            item.setCreated(new Date());

            dbLayer.setMasterLastEntry(masterSettings.getMasterId(), false);
            dbLayer.getSession().save(item);

            masterTimezone = item.getTimezone();

        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                LOGGER.error(e.toString(), e);
                throw e;
            }
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][%s]%s", identifier, entry.getType(), entry.getKey(), e.toString()));
            }
        }
    }

    private void agentAdd(DBLayerHistory dbLayer, Entry entry) throws Exception {

        try {
            DBItemAgent item = new DBItemAgent();
            item.setAgentKey(entry.getKey());
            item.setUri(".");// TODO
            item.setTimezone(entry.getTimezone());
            item.setStartTime(entry.getTimestamp() == null ? entry.getEventIdAsDate() : entry.getTimestampAsDate());
            item.setLastEntry(true);
            item.setCreated(new Date());

            dbLayer.setAgentLastEntry(masterSettings.getMasterId(), item.getAgentKey(), false);
            dbLayer.getSession().save(item);

            CachedAgent ca = new CachedAgent(item);
            storeLog(dbLayer, new ChunkLogEntry(LogLevel.Debug, OutType.Stdout, LogType.AgentReady, entry, ca, masterTimezone));
            tryStoreCurrentState(dbLayer, entry.getEventId());

            addCachedAgent(item.getAgentKey(), ca);

        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                LOGGER.error(e.toString(), e);
                throw e;
            }
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][%s]%s", identifier, entry.getType(), entry.getKey(), e.toString()));
            }
        }
    }

    private void orderAdd(DBLayerHistory dbLayer, Entry entry) throws Exception {

        try {
            DBItemOrder item = new DBItemOrder();
            item.setMasterId(masterSettings.getMasterId());
            item.setOrderKey(entry.getKey());
            item.setWorkflowPosition(entry.getWorkflowPosition().getOrderPositionAsString());
            item.setRetryCounter(new Long(0));// TODO

            /** if (entry.getParent() == null) { item.setMainParentId(new Long(0));// TODO see below item.setParentId(new Long(0));
             * item.setParentOrderKey(null); } else { CachedOrder pco = getCachedOrder(dbLayer, entry.getParent()); if (!pco.getHasChildren()) {
             * pco.setHasChildren(true); dbLayer.setHasChildren(pco.getId()); addCachedOrder(pco.getOrderKey(), pco); }
             * item.setMainParentId(pco.getMainParentId()); item.setParentId(pco.getId()); item.setParentOrderKey(entry.getParent()); } */

            item.setConstraintHash(hashOrderConstaint(entry));
            item.setHasChildren(false);
            item.setName(entry.getKey());// TODO
            item.setTitle(null);// TODO
            item.setWorkflowVersion(entry.getWorkflowPosition().getWorkflowId().getVersionId());
            item.setWorkflowPath(entry.getWorkflowPosition().getWorkflowId().getPath());
            item.setWorkflowFolder(HistoryUtil.getFolderFromPath(item.getWorkflowPath()));
            item.setWorkflowName(HistoryUtil.getBasenameFromPath(item.getWorkflowPath()));
            item.setWorkflowTitle(null);// TODO
            // item.setStartCause(entry.getCause());
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

            dbLayer.getSession().save(item);
            if (item.getMainParentId().equals(new Long(0))) {// TODO see above
                item.setMainParentId(item.getId());
                dbLayer.setMainParentId(item.getId(), item.getMainParentId());
            }

            CachedOrder co = new CachedOrder(item);
            storeLog(dbLayer, new ChunkLogEntry(LogLevel.Debug, OutType.Stdout, LogType.OrderAdded, entry, co, masterTimezone));
            tryStoreCurrentState(dbLayer, entry.getEventId());

            addCachedOrder(item.getOrderKey(), co);
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                LOGGER.error(e.toString(), e);
                throw e;
            }
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][%s]%s", identifier, entry.getType(), entry.getKey(), e.toString()));
            }
            addCachedOrderByStartEventId(dbLayer, entry.getKey(), String.valueOf(entry.getEventId()));
        }
    }

    private void orderEnd(DBLayerHistory dbLayer, Entry entry) throws Exception {
        CachedOrder co = getCachedOrder(dbLayer, entry.getKey());
        if (co.getEndTime() == null) {
            Date endTime = entry.getTimestamp() == null ? entry.getEventIdAsDate() : entry.getTimestampAsDate();
            DBItemOrderStep stepItem = dbLayer.getOrderStepById(co.getCurrentStepId());
            // TODO finished
            dbLayer.setOrderEnd(co.getId(), endTime, stepItem.getWorkflowPosition(), stepItem.getId(), String.valueOf(entry.getEventId()), "finished",
                    stepItem.getError(), stepItem.getErrorCode(), stepItem.getErrorText(), new Date());

            storeLog(dbLayer, new ChunkLogEntry(LogLevel.Info, OutType.Stdout, LogType.OrderEnd, entry, co, masterTimezone));

            tryStoreCurrentState(dbLayer, entry.getEventId());
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][skip][%s]order is already ended[%s]", identifier, entry.getType(), entry.getKey(), SOSString
                        .toString(co)));
            }
        }
        clearCache(co.getOrderKey(), CacheType.order);
    }

    private void orderStepStart(DBLayerHistory dbLayer, Entry entry) throws Exception {
        CachedOrder co = null;
        CachedOrderStep cos = null;
        boolean isOrderStart = false;
        try {
            co = getCachedOrder(dbLayer, entry.getKey());

            DBItemOrderStep item = new DBItemOrderStep();
            item.setMasterId(masterSettings.getMasterId());
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

            dbLayer.getSession().save(item);

            Date orderStartTime = null;
            String orderState = null;
            if (item.getWorkflowPosition().equals(co.getStartWorkflowPosition())) {// + order.startTime != default
                orderStartTime = item.getStartTime();
                orderState = "started";// TODO
                isOrderStart = true;
            }
            co.setCurrentStepId(item.getId());

            dbLayer.updateOrderOnOrderStep(co.getId(), orderStartTime, orderState, co.getCurrentStepId(), new Date());

            addCachedOrder(co.getOrderKey(), co);
            cos = new CachedOrderStep(item);
            addCachedOrderStep(item.getOrderKey(), cos);
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                LOGGER.error(e.toString(), e);
                throw e;
            }
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][%s]%s", identifier, entry.getType(), entry.getKey(), e.toString()));
            }
            if (co != null) {
                addCachedOrder(co.getOrderKey(), co);
            }
            addCachedOrderStepByStartEventId(dbLayer, entry.getKey(), String.valueOf(entry.getEventId()));
        }
        if (cos != null) {// inserted
            if (isOrderStart) {
                storeLog(dbLayer, new ChunkLogEntry(LogLevel.Info, OutType.Stdout, LogType.OrderStart, entry, co, masterTimezone));
            }
            storeLog(dbLayer, new ChunkLogEntry(LogLevel.Info, OutType.Stdout, LogType.OrderStepStart, entry, cos, masterTimezone));
            tryStoreCurrentState(dbLayer, entry.getEventId());
        }
    }

    private void orderStepEnd(DBLayerHistory dbLayer, Entry entry) throws Exception {
        CachedOrderStep cos = getCachedOrderStep(dbLayer, entry.getKey());
        if (cos.getEndTime() == null) {
            Date endTime = entry.getTimestamp() == null ? entry.getEventIdAsDate() : entry.getTimestampAsDate();
            dbLayer.setOrderStepEnd(cos.getId(), endTime, String.valueOf(entry.getEventId()), EventMeta.map2Json(entry.getVariables()), entry
                    .getOutcome().getReturnCode(), entry.getOutcome().getType(), new Date());

            storeLog(dbLayer, new ChunkLogEntry(LogLevel.Info, OutType.Stdout, LogType.OrderStepEnd, entry, cos, masterTimezone));

            tryStoreCurrentState(dbLayer, entry.getEventId());
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][skip][%s]order step is already ended[%s]", identifier, entry.getType(), entry.getKey(), SOSString
                        .toString(cos)));
            }
        }
        clearCache(cos.getOrderKey(), CacheType.orderStep);
    }

    private void orderStepStd(DBLayerHistory dbLayer, Entry entry, OutType outType) throws Exception {
        CachedOrderStep cos = getCachedOrderStep(dbLayer, entry.getKey());
        if (cos.getEndTime() == null) {
            CachedAgent ca = getCachedAgent(dbLayer,entry.getAgentPath());
            
            storeLog(dbLayer, new ChunkLogEntry(LogLevel.Info, outType, LogType.OrderStepStd, entry, cos, ca.getTimezone()));

            tryStoreCurrentState(dbLayer, entry.getEventId());
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][skip][%s]order step is already ended. log already written...[%s]", identifier, entry.getType(),
                        entry.getKey(), SOSString.toString(cos)));
            }
        }
    }

    private void addCachedOrder(String key, CachedOrder order) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][addCachedOrder][%s]workflowPosition=%s", identifier, key, order.getWorkflowPosition()));
        }
        cachedOrders.put(key, order);
    }

    private CachedOrder getCachedOrder(DBLayerHistory dbLayer, String key) throws Exception {
        CachedOrder co = getCachedOrder(key);
        if (co == null) {
            DBItemOrder item = dbLayer.getOrder(masterSettings.getMasterId(), key);
            if (item == null) {
                throw new Exception(String.format("[%s]order not found. orderKey=%s", identifier, key));
            } else {
                co = new CachedOrder(item);
                addCachedOrder(key, co);
            }
        }
        return co;
    }

    private CachedOrder getCachedOrder(String key) {
        if (cachedOrders.containsKey(key)) {
            CachedOrder co = cachedOrders.get(key);
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][getCachedOrder][%s]%s", identifier, key, SOSString.toString(co)));
            }
            return co;
        }
        return null;
    }

    private void addCachedOrderByStartEventId(DBLayerHistory dbLayer, String key, String startEventId) throws Exception {
        DBItemOrder item = dbLayer.getOrder(masterSettings.getMasterId(), key, startEventId);
        if (item == null) {
            throw new Exception(String.format("[%s]order not found. orderKey=%s, startEventId=%s", identifier, key, startEventId));
        } else {
            addCachedOrder(key, new CachedOrder(item));
        }
    }

    private void addCachedOrderStep(String key, CachedOrderStep co) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][addCachedOrderStep][%s]jobPath=%s, workflowPosition=%s", identifier, key, co.getJobPath(), co
                    .getWorkflowPosition()));
        }
        cachedOrderSteps.put(key, co);
    }

    private CachedOrderStep getCachedOrderStep(DBLayerHistory dbLayer, String key) throws Exception {
        CachedOrderStep co = getCachedOrderStep(key);
        if (co == null) {
            DBItemOrderStep item = dbLayer.getOrderStep(masterSettings.getMasterId(), key);
            if (item == null) {
                throw new Exception(String.format("[%s]order step not found. orderKey=%s", identifier, key));
            } else {
                co = new CachedOrderStep(item);
                addCachedOrderStep(key, co);
            }
        }
        return co;
    }

    private CachedOrderStep getCachedOrderStep(String key) {
        if (cachedOrderSteps.containsKey(key)) {
            CachedOrderStep co = cachedOrderSteps.get(key);
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][getCachedOrderStep][%s]%s", identifier, key, SOSString.toString(co)));
            }
            return co;
        }
        return null;
    }

    private void addCachedOrderStepByStartEventId(DBLayerHistory dbLayer, String key, String startEventId) throws Exception {
        DBItemOrderStep item = dbLayer.getOrderStep(masterSettings.getMasterId(), key, startEventId);
        if (item == null) {
            throw new Exception(String.format("[%s]order step not found. orderKey=%s, startEventId=%s", identifier, key, startEventId));
        } else {
            addCachedOrderStep(key, new CachedOrderStep(item));
        }
    }

    private void addCachedAgent(String key, CachedAgent co) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][addCachedAgent]%s", identifier, key));
        }
        cachedAgents.put(key, co);
    }

    private CachedAgent getCachedAgent(DBLayerHistory dbLayer, String key) throws Exception {
        CachedAgent co = getCachedAgent(key);
        if (co == null) {
            DBItemAgent item = dbLayer.getAgent(masterSettings.getMasterId(), key, true);
            if (item == null) {
                throw new Exception(String.format("[%s]agent not found. agentKey=%s", identifier, key));
            } else {
                co = new CachedAgent(item);
                addCachedAgent(key, co);
            }
        }
        return co;
    }

    private CachedAgent getCachedAgent(String key) {
        if (cachedAgents.containsKey(key)) {
            CachedAgent co = cachedAgents.get(key);
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][getCachedAgent][%s]%s", identifier, key, SOSString.toString(co)));
            }
            return co;
        }
        return null;
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

    private void storeLog(DBLayerHistory dbLayer, ChunkLogEntry logEntry) throws Exception {

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
            DBItemLog item = new DBItemLog();
            item.setMasterId(masterSettings.getMasterId());
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
                dbLayer.getSession().save(item);
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
        // return HistoryUtil.hashString(masterId + String.valueOf(entry.getEventId())); //MUST BE
        return HistoryUtil.hashString(masterSettings.getMasterId() + String.valueOf(entry.getEventId()) + entry.getKey()); // TODO
    }

    private String hashLogConstaint(ChunkLogEntry logEntry, int i) {
        return HistoryUtil.hashString(masterSettings.getMasterId() + String.valueOf(logEntry.getEventId()) + logEntry.getOrderKey() + logEntry
                .getLogType().name() + String.valueOf(i));
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
