package com.sos.jobscheduler.history.master.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateFactory.Dbms;
import com.sos.commons.hibernate.exception.SOSHibernateObjectOperationException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.db.general.DBItemVariable;
import com.sos.jobscheduler.db.history.DBItemAgent;
import com.sos.jobscheduler.db.history.DBItemLog;
import com.sos.jobscheduler.db.history.DBItemMaster;
import com.sos.jobscheduler.db.history.DBItemOrder;
import com.sos.jobscheduler.db.history.DBItemOrderStatus;
import com.sos.jobscheduler.db.history.DBItemOrderStep;
import com.sos.jobscheduler.event.master.EventMeta;
import com.sos.jobscheduler.event.master.bean.Event;
import com.sos.jobscheduler.event.master.bean.IEntry;
import com.sos.jobscheduler.event.master.fatevent.EventMeta.EventType;
import com.sos.jobscheduler.event.master.fatevent.bean.Entry;
import com.sos.jobscheduler.event.master.fatevent.bean.OrderForkedChild;
import com.sos.jobscheduler.event.master.fatevent.bean.Outcome;
import com.sos.jobscheduler.event.master.handler.RestServiceDuration;
import com.sos.jobscheduler.history.db.DBLayerHistory;
import com.sos.jobscheduler.history.helper.CachedAgent;
import com.sos.jobscheduler.history.helper.CachedOrder;
import com.sos.jobscheduler.history.helper.CachedOrderStep;
import com.sos.jobscheduler.history.helper.ChunkLogEntry;
import com.sos.jobscheduler.history.helper.ChunkLogEntry.LogLevel;
import com.sos.jobscheduler.history.helper.ChunkLogEntry.LogType;
import com.sos.jobscheduler.history.helper.ChunkLogEntry.OutType;
import com.sos.jobscheduler.history.helper.HistoryRestApiClient;
import com.sos.jobscheduler.history.helper.HistoryUtil;
import com.sos.jobscheduler.history.master.configuration.HistoryMasterConfiguration;

public class HistoryModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryModel.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();

    private static final Logger LOGGER_DIAGNOSTIC = LoggerFactory.getLogger("HistoryDiagnostic");
    private static final Logger LOGGER_HISTORY_FILE_WRITER = LoggerFactory.getLogger("HistoryLogFileWriter");
    private static final String LOGGER_HISTORY_FILE_WRITER_PARAM_LOGDIR = "historyLogDirectory";
    private static final String LOGGER_HISTORY_FILE_WRITER_PARAM_FILENAME = "historyLogFileName";

    private static final long MAX_LOCK_VERSION = 10_000_000;
    private final SOSHibernateFactory dbFactory;
    private HistoryMasterConfiguration configuration;
    private HistoryRestApiClient restClient;
    private final String identifier;
    private DBItemVariable dbItemVariable;
    private final String variable;
    private Long storedEventId;
    private boolean closed = false;
    private int maxTransactions = 100;
    private long transactionCounter;
    private boolean isMySQL;
    private String masterTimezone;

    private Map<String, CachedOrder> cachedOrders;
    private Map<String, CachedOrderStep> cachedOrderSteps;
    private Map<String, CachedAgent> cachedAgents;

    private static enum CacheType {
        master, agent, order, orderStep
    };

    private static enum OrderStartCase {
        order, fork, file_trigger, setback, unskip, unstop
    };

    public static enum OrderStatus {
        planned, started, running, completed, stopped, cancelled, failed, suspended
    };

    private static enum OrderStepStartCase {
        order, file_trigger, setback, unskip, unstop
    };

    public static enum OrderStepStatus {
        running, completed, stopped, failed, skipped
    };

    public HistoryModel(SOSHibernateFactory factory, HistoryMasterConfiguration conf, String ident) {
        dbFactory = factory;
        isMySQL = dbFactory.getDbms().equals(Dbms.MYSQL);
        configuration = conf;
        identifier = ident;
        variable = "history_" + configuration.getCurrent().getId();
        maxTransactions = configuration.getMaxTransactions();
        restClient = new HistoryRestApiClient(identifier);
    }

    public Long getEventId() throws Exception {
        DBLayerHistory dbLayer = null;
        try {
            dbLayer = new DBLayerHistory(dbFactory.openStatelessSession());
            dbLayer.getSession().setIdentifier(identifier);
            dbLayer.getSession().beginTransaction();

            dbItemVariable = dbLayer.getVariable(variable);
            if (dbItemVariable == null) {
                dbItemVariable = dbLayer.insertVariable(variable, "0");
            }

            dbLayer.getSession().commit();
            return Long.parseLong(dbItemVariable.getTextValue());
        } catch (Exception e) {
            throw e;
        } finally {
            if (dbLayer != null) {
                dbLayer.close();
            }
        }
    }

    public Long process(Event event, RestServiceDuration lastRestServiceDuration) throws Exception {
        String method = "process";

        doDiagnostic("onMasterNonEmptyEventResponse", lastRestServiceDuration.getDuration(), configuration
                .getDiagnosticStartIfNotEmptyEventLongerThan());

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
        Long firstEventId = new Long(0);

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

                if (processedEventsCounter == 0) {
                    firstEventId = eventId;
                }
                // TODO must be >= instead of > (workaround for: eventId by fork is the same).
                // Changed - Testing
                if (storedEventId >= eventId) {
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
                    orderForked(dbLayer, entry);
                    break;
                case OrderJoinedFat:
                    orderJoined(dbLayer, entry);
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
                case OrderStoppedFat:
                case OrderCanceledFat:
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
        } catch (Throwable e) {
            // LOGGER.error(String.format("[%s][%s][end]%s", identifier, method, e.toString()), e);
            try {
                tryStoreCurrentStateAtEnd(dbLayer, lastSuccessEventId);
            } catch (Exception e1) {
                LOGGER.error(String.format("[%s][%s][end][on_error]error on store lastSuccessEventId=%s: %s", identifier, method, lastSuccessEventId,
                        e1.toString()), e1);
            }
            throw new Exception(String.format("[%s][%s][end]%s", identifier, method, e.toString()), e);
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
        String firstEventIdAsTime = firstEventId.equals(new Long(0)) ? "0" : SOSDate.getTime(EventMeta.eventId2Instant(firstEventId));
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        LOGGER.info(String.format("[%s][%s][%s(%s)-%s][%s(%s)-%s][%s-%s][%s]%s-%s", identifier, lastRestServiceDuration, startEventId, firstEventId,
                storedEventId, startEventIdAsTime, firstEventIdAsTime, endEventIdAsTime, SOSDate.getTime(start), SOSDate.getTime(end), SOSDate
                        .getDuration(duration), processedEventsCounter, total));

        doDiagnostic("onHistory", duration, configuration.getDiagnosticStartIfHistoryExecutionLongerThan());

        return storedEventId;
    }

    public void close() {
        closed = true;
    }

    @SuppressWarnings("unused")
    private void doDiagnosticXXX(String range, Duration duration, long maxTime) {

        if (duration == null || maxTime <= 0) {
            return;
        }

        if (duration.toMillis() > maxTime) {
            final SimpleTimeLimiter timeLimiter = new SimpleTimeLimiter(Executors.newSingleThreadExecutor());
            @SuppressWarnings("unchecked")
            final Callable<Boolean> timeLimitedCall = timeLimiter.newProxy(new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {
                    String identifier = Thread.currentThread().getName() + "-" + configuration.getCurrent().getId();
                    LOGGER_DIAGNOSTIC.info(String.format("[%s]duration=%s", range, SOSDate.getDuration(duration)));
                    HistoryUtil.printCpuLoad(LOGGER_DIAGNOSTIC, identifier);
                    if (!SOSString.isEmpty(configuration.getDiagnosticAdditionalScript())) {
                        HistoryUtil.executeCommand(LOGGER_DIAGNOSTIC, configuration.getDiagnosticAdditionalScript(), identifier);
                    }
                    return true;
                }
            }, Callable.class, 5, TimeUnit.SECONDS);
            try {
                timeLimitedCall.call();
            } catch (Exception e) {
                LOGGER.error(String.format("[doDiagnostic]%s", e.toString()), e);
            }
        }

    }

    private void doDiagnostic(String range, Duration duration, long maxTime) {

        if (duration == null || maxTime <= 0) {
            return;
        }

        if (duration.toMillis() > maxTime) {
            try {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        String identifier = Thread.currentThread().getName() + "-" + configuration.getCurrent().getId();
                        LOGGER_DIAGNOSTIC.info(String.format("[%s]duration=%s", range, SOSDate.getDuration(duration)));
                        HistoryUtil.printCpuLoad(LOGGER_DIAGNOSTIC, identifier);
                        if (!SOSString.isEmpty(configuration.getDiagnosticAdditionalScript())) {
                            HistoryUtil.executeCommand(LOGGER_DIAGNOSTIC, configuration.getDiagnosticAdditionalScript(), identifier);
                        }
                    }
                }).start();

            } catch (Throwable e) {
                LOGGER.error(String.format("[doDiagnostic]%s", e.toString()), e);
            }
        }

    }

    private void tryStoreCurrentState(DBLayerHistory dbLayer, Long eventId) throws Exception {
        if (isMySQL) {
            return;
        }
        if (transactionCounter % maxTransactions == 0 && dbLayer.getSession().isTransactionOpened()) {
            updateVariable(dbLayer, eventId);
            dbLayer.getSession().commit();
            dbLayer.getSession().beginTransaction();
        }
    }

    private void tryStoreCurrentStateAtEnd(DBLayerHistory dbLayer, Long eventId) throws Exception {
        if (eventId > 0 && !storedEventId.equals(eventId)) {
            if (!dbLayer.getSession().isTransactionOpened()) {
                dbLayer.getSession().beginTransaction();
            }
            updateVariable(dbLayer, eventId);
            dbLayer.getSession().commit();
        }
    }

    private void updateVariable(DBLayerHistory dbLayer, Long eventId) throws Exception {
        dbLayer.updateVariable(dbItemVariable, eventId);
        if (dbItemVariable.getLockVersion() > MAX_LOCK_VERSION) {
            dbLayer.resetLockVersion(dbItemVariable.getName());
        }
        storedEventId = eventId;
    }

    private void masterAdd(DBLayerHistory dbLayer, Entry entry) throws Exception {

        try {
            DBItemMaster item = new DBItemMaster();
            item.setMasterId(configuration.getCurrent().getId());
            item.setUri(configuration.getCurrent().getUri());
            item.setTimezone(entry.getTimezone());
            item.setStartTime(entry.getEventDate());
            item.setPrimaryMaster(configuration.getCurrent().isPrimary());
            item.setEventId(String.valueOf(entry.getEventId()));
            item.setCreated(new Date());

            dbLayer.getSession().save(item);

            masterTimezone = item.getTimezone();
            ChunkLogEntry cle = new ChunkLogEntry(LogLevel.Debug, OutType.Stdout, LogType.MasterReady, masterTimezone, entry.getEventId(), entry
                    .getTimestamp(), item.getStartTime());
            cle.onMaster(configuration);
            storeLog2File(cle);

            tryStoreCurrentState(dbLayer, entry.getEventId());
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                LOGGER.error(e.toString(), e);
                throw e;
            }
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][%s]%s", identifier, entry.getType(), entry.getKey(), e.toString()));
            }
        } finally {
            if (masterTimezone == null) {
                masterTimezone = entry.getTimezone();
            }
        }
    }

    private void checkMasterTimezone(DBLayerHistory dbLayer) throws Exception {
        if (masterTimezone == null) {
            masterTimezone = dbLayer.getMasterTimezone(configuration.getCurrent().getId());
            if (masterTimezone == null) {
                throw new Exception(String.format("master not founded: %s", configuration.getCurrent().getId()));
            }
        }
    }

    private void agentAdd(DBLayerHistory dbLayer, Entry entry) throws Exception {

        try {
            checkMasterTimezone(dbLayer);

            DBItemAgent item = new DBItemAgent();
            item.setMasterId(configuration.getCurrent().getId());
            item.setPath(entry.getKey());
            item.setUri(".");// TODO
            item.setTimezone(entry.getTimezone());
            item.setStartTime(entry.getEventDate());
            item.setEventId(String.valueOf(entry.getEventId()));
            item.setCreated(new Date());

            dbLayer.getSession().save(item);

            CachedAgent ca = new CachedAgent(item);

            ChunkLogEntry cle = new ChunkLogEntry(LogLevel.Debug, OutType.Stdout, LogType.AgentReady, masterTimezone, entry.getEventId(), entry
                    .getTimestamp(), item.getStartTime());
            cle.onAgent(ca);
            storeLog2File(cle);

            tryStoreCurrentState(dbLayer, entry.getEventId());

            addCachedAgent(item.getPath(), ca);

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
            checkMasterTimezone(dbLayer);

            DBItemOrder item = new DBItemOrder();
            item.setMasterId(configuration.getCurrent().getId());
            item.setOrderKey(entry.getKey());

            item.setWorkflowPath(entry.getWorkflowPosition().getWorkflowId().getPath());
            item.setWorkflowVersionId(entry.getWorkflowPosition().getWorkflowId().getVersionId());
            item.setWorkflowPosition(entry.getWorkflowPosition().getOrderPositionAsString());
            item.setWorkflowFolder(HistoryUtil.getFolderFromPath(item.getWorkflowPath()));
            item.setWorkflowName(HistoryUtil.getBasenameFromPath(item.getWorkflowPath()));
            item.setWorkflowTitle(null);// TODO

            item.setMainParentId(new Long(0));// TODO see below item.setParentId(new Long(0));
            item.setParentId(new Long(0));
            item.setParentOrderKey(null);
            item.setHasChildren(false);
            item.setRetryCounter(entry.getWorkflowPosition().getRetry());

            item.setName(entry.getKey());// TODO
            item.setTitle(null);// TODO

            item.setStartCause(OrderStartCase.order.name());// TODO
            item.setStartTimePlanned(entry.getSchedulerForAsDate());
            item.setStartTime(new Date(0));// 1970-01-01 01:00:00 TODO
            item.setStartWorkflowPosition(entry.getWorkflowPosition().getPositionAsString());
            item.setStartEventId(String.valueOf(entry.getEventId()));
            item.setStartParameters(EventMeta.map2Json(entry.getArguments()));

            item.setCurrentOrderStepId(new Long(0));

            item.setEndTime(null);
            item.setEndWorkflowPosition(null);
            item.setEndOrderStepId(new Long(0));

            item.setStatus(OrderStatus.planned.name());// TODO
            item.setStatusTime(entry.getEventDate());
            item.setStateText(null);// TODO

            item.setError(false);
            item.setErrorText(null);
            item.setEndEventId(null);

            item.setLogId(new Long(0));

            item.setConstraintHash(hashOrderConstaint(entry.getEventId(), item.getOrderKey(), item.getWorkflowPosition()));
            item.setCreated(new Date());
            item.setModified(item.getCreated());

            dbLayer.getSession().save(item);

            item.setMainParentId(item.getId()); // TODO see above
            dbLayer.setMainParentId(item.getId(), item.getMainParentId());

            CachedOrder co = new CachedOrder(item);

            ChunkLogEntry cle = new ChunkLogEntry(LogLevel.Debug, OutType.Stdout, LogType.OrderAdded, masterTimezone, entry.getEventId(), entry
                    .getTimestamp(), entry.getEventDate());
            cle.onOrder(co, item.getWorkflowPosition());
            storeLog2File(cle);

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
        orderEnd(dbLayer, LogType.OrderEnd, entry.getType(), entry.getEventId(), entry.getKey(), entry.getTimestamp(), entry.getEventDate(), entry
                .getOutcome());
    }

    private CachedOrder orderEnd(DBLayerHistory dbLayer, LogType logType, EventType eventType, Long eventId, String orderKey, Long eventTimestamp,
            Date eventDate, Outcome outcome) throws Exception {
        CachedOrder co = getCachedOrder(dbLayer, orderKey);
        if (co.getEndTime() == null) {
            checkMasterTimezone(dbLayer);

            DBItemOrderStep stepItem = dbLayer.getOrderStep(co.getCurrentOrderStepId());

            Date endTime = eventDate;
            String endWorkflowPosition = stepItem.getWorkflowPosition();
            Long endOrderStepId = stepItem.getId();
            String endEventId = String.valueOf(eventId);
            String status = null;
            boolean error = false;

            switch (eventType) {
            case OrderStoppedFat:
                endTime = null;
                endWorkflowPosition = null;
                endOrderStepId = null;
                endEventId = null;
                logType = LogType.OrderStopped;
                status = OrderStatus.stopped.name();
                break;
            case OrderCanceledFat:
                logType = LogType.OrderCancelled;
                status = OrderStatus.cancelled.name();
                break;
            default:
                status = OrderStatus.completed.name();
            }

            if (stepItem.getError() || (outcome != null && outcome.getType().equalsIgnoreCase(OrderStatus.failed.name()))) {
                error = true;
            }

            dbLayer.setOrderEnd(co.getId(), endTime, endWorkflowPosition, endOrderStepId, endEventId, status, eventDate, error, stepItem
                    .getErrorCode(), stepItem.getErrorText(), new Date());

            saveOrderStatus(dbLayer, co, status, stepItem.getWorkflowPath(), stepItem.getWorkflowVersionId(), stepItem.getWorkflowPosition(),
                    eventDate, eventId);

            ChunkLogEntry cle = new ChunkLogEntry(LogLevel.Info, OutType.Stdout, logType, masterTimezone, eventId, eventTimestamp, eventDate);
            cle.onOrder(co, stepItem.getWorkflowPosition());

            Path logFile = storeLog2File(cle);
            if (logType.equals(LogType.OrderEnd)) {
                DBItemLog logItem = storeLogFile2Db(dbLayer, co.getMainParentId(), co.getId(), new Long(0), logFile);
                if (logItem != null) {
                    dbLayer.setOrderLogId(co.getId(), logItem.getId());
                }
            }

            tryStoreCurrentState(dbLayer, eventId);

            if (logType.equals(LogType.OrderEnd) && co.getParentId() == 0) {
                send2Executor("order_id=" + co.getId());
            }
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][skip][%s]order is already completed[%s]", identifier, eventType, orderKey, SOSString.toString(
                        co)));
            }
        }
        if (logType.equals(LogType.OrderEnd)) {
            clearCache(co.getOrderKey(), CacheType.order);
        }
        return co;
    }

    private void send2Executor(String params) {
        if (!SOSString.isEmpty(configuration.getUriHistoryExecutor())) {
            try {
                String response = restClient.doPost(new URI(configuration.getUriHistoryExecutor()), params);
                LOGGER.info(String.format("[%s][%s][%s][%s]%s", identifier, restClient.getLastRestServiceDuration(), configuration
                        .getUriHistoryExecutor(), params, response));
            } catch (Throwable t) {
                LOGGER.warn(String.format("[%s][%s][exception]%s", identifier, params, t.toString()), t);
            }
        }
    }

    private void orderForked(DBLayerHistory dbLayer, Entry entry) throws Exception {
        checkMasterTimezone(dbLayer);

        Date startTime = entry.getEventDate();

        CachedOrder co = getCachedOrder(dbLayer, entry.getKey());
        co.setHasChildren(true);
        addCachedOrder(co.getOrderKey(), co);
        dbLayer.setHasChildren(co.getId());

        ChunkLogEntry cle = new ChunkLogEntry(LogLevel.Info, OutType.Stdout, LogType.Fork, masterTimezone, entry.getEventId(), entry.getTimestamp(),
                startTime);
        cle.onOrder(co, entry.getWorkflowPosition().getOrderPositionAsString(), entry.getChildren());
        storeLog2File(cle);

        for (int i = 0; i < entry.getChildren().size(); i++) {
            orderForkedAdd(dbLayer, entry, co, entry.getChildren().get(i), startTime);
        }
    }

    private void orderForkedAdd(DBLayerHistory dbLayer, Entry entry, CachedOrder parentOrder, OrderForkedChild forkOrder, Date startTime)
            throws Exception {
        try {
            checkMasterTimezone(dbLayer);

            DBItemOrder item = new DBItemOrder();
            item.setMasterId(configuration.getCurrent().getId());
            item.setOrderKey(forkOrder.getOrderId());

            item.setWorkflowPath(entry.getWorkflowPosition().getWorkflowId().getPath());
            item.setWorkflowVersionId(entry.getWorkflowPosition().getWorkflowId().getVersionId());
            item.setWorkflowPosition(entry.getWorkflowPosition().getOrderPositionAsString());// TODO erweitern um branch
            item.setWorkflowFolder(HistoryUtil.getFolderFromPath(item.getWorkflowPath()));
            item.setWorkflowName(HistoryUtil.getBasenameFromPath(item.getWorkflowPath()));
            item.setWorkflowTitle(null);// TODO

            item.setMainParentId(parentOrder.getMainParentId());
            item.setParentId(parentOrder.getId());
            item.setParentOrderKey(parentOrder.getOrderKey());
            item.setHasChildren(false);
            item.setRetryCounter(entry.getWorkflowPosition().getRetry());

            item.setName(forkOrder.getBranchId());// TODO
            item.setTitle(null);// TODO

            item.setStartCause(OrderStartCase.fork.name());// TODO
            item.setStartTimePlanned(startTime);
            item.setStartTime(startTime);
            item.setStartWorkflowPosition(entry.getWorkflowPosition().getPositionAsString());
            item.setStartEventId(String.valueOf(entry.getEventId()));
            item.setStartParameters(EventMeta.map2Json(forkOrder.getArguments()));

            item.setCurrentOrderStepId(new Long(0));

            item.setEndTime(null);
            item.setEndWorkflowPosition(null);
            item.setEndOrderStepId(new Long(0));

            item.setStatus(OrderStatus.running.name());// TODO
            item.setStatusTime(startTime);
            item.setStateText(null);// TODO

            item.setError(false);
            item.setErrorText(null);
            item.setEndEventId(null);

            item.setLogId(new Long(0));

            item.setConstraintHash(hashOrderConstaint(entry.getEventId(), item.getOrderKey(), item.getWorkflowPosition()));
            item.setCreated(new Date());
            item.setModified(item.getCreated());

            dbLayer.getSession().save(item);

            CachedOrder co = new CachedOrder(item);

            ChunkLogEntry cle = new ChunkLogEntry(LogLevel.Debug, OutType.Stdout, LogType.ForkBranchStart, masterTimezone, entry.getEventId(), entry
                    .getTimestamp(), startTime);
            cle.onOrder(co, item.getWorkflowPosition());
            storeLog2File(cle);

            tryStoreCurrentState(dbLayer, entry.getEventId());

            addCachedOrder(item.getOrderKey(), co);

            saveOrderStatus(dbLayer, co, OrderStatus.started.name(), item.getWorkflowPath(), item.getWorkflowVersionId(), item.getWorkflowPosition(),
                    entry.getEventDate(), entry.getEventId());

        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                LOGGER.error(e.toString(), e);
                throw e;
            }
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][%s]%s", identifier, entry.getType(), entry.getKey(), e.toString()));
            }
            addCachedOrderByStartEventId(dbLayer, forkOrder.getOrderId(), String.valueOf(entry.getEventId()));
        }
    }

    private void orderJoined(DBLayerHistory dbLayer, Entry entry) throws Exception {
        checkMasterTimezone(dbLayer);

        CachedOrder co = getCachedOrder(dbLayer, entry.getKey());
        Date endTime = entry.getEventDate();

        CachedOrder fco = null;
        for (int i = 0; i < entry.getChildOrderIds().size(); i++) {
            fco = orderEnd(dbLayer, LogType.ForkBranchEnd, entry.getType(), entry.getEventId(), entry.getChildOrderIds().get(i), entry.getTimestamp(),
                    endTime, entry.getOutcome());
        }

        ChunkLogEntry cle = new ChunkLogEntry(LogLevel.Info, OutType.Stdout, LogType.ForkJoin, masterTimezone, entry.getEventId(), entry
                .getTimestamp(), endTime);
        cle.onOrderJoined(co, fco.getWorkflowPosition(), entry.getChildOrderIds());
        storeLog2File(cle);
    }

    private void orderStepStart(DBLayerHistory dbLayer, Entry entry) throws Exception {
        CachedOrder co = null;
        CachedOrderStep cos = null;
        boolean isOrderStart = false;
        Date startTime = entry.getEventDate();
        DBItemOrderStep item = null;
        try {
            checkMasterTimezone(dbLayer);

            co = getCachedOrder(dbLayer, entry.getKey());

            CachedAgent ca = getCachedAgent(dbLayer, entry.getAgentRefPath());
            // TODO temp solution
            if (!ca.getUri().equals(entry.getAgentUri())) {

                dbLayer.updateAgent(ca.getId(), entry.getAgentUri());
                ca.setUri(entry.getAgentUri());

                addCachedAgent(entry.getAgentRefPath(), ca);
            }

            item = new DBItemOrderStep();
            item.setMasterId(configuration.getCurrent().getId());
            item.setOrderKey(entry.getKey());

            item.setWorkflowPath(entry.getWorkflowPosition().getWorkflowId().getPath());
            item.setWorkflowVersionId(entry.getWorkflowPosition().getWorkflowId().getVersionId());
            item.setWorkflowPosition(entry.getWorkflowPosition().getPositionAsString());

            item.setMainOrderId(co.getMainParentId());
            item.setOrderId(co.getId());
            item.setPosition(entry.getWorkflowPosition().getLastPosition());
            item.setRetryCounter(entry.getWorkflowPosition().getRetry());

            item.setJobName(entry.getJobName());
            item.setJobTitle(null);// TODO

            item.setAgentPath(entry.getAgentRefPath());
            item.setAgentUri(ca.getUri());

            item.setStartCause(OrderStepStartCase.order.name());// TODO
            item.setStartTime(startTime);
            item.setStartEventId(String.valueOf(entry.getEventId()));
            item.setStartParameters(EventMeta.map2Json(entry.getKeyValues()));

            item.setEndTime(null);
            item.setEndEventId(null);

            item.setReturnCode(null);
            item.setStatus(OrderStepStatus.running.name());

            item.setError(false);
            item.setErrorCode(null);
            item.setErrorText(null);

            item.setLogId(new Long(0));

            item.setConstraintHash(hashOrderStepConstaint(entry.getEventId(), item.getOrderKey(), item.getWorkflowPosition()));
            item.setCreated(new Date());
            item.setModified(item.getCreated());

            dbLayer.getSession().save(item);

            Date orderStartTime = null;
            String orderStatus = null;
            // TODO check for Fork -
            if (item.getWorkflowPosition().equals(co.getStartWorkflowPosition())) {// + order.startTime != default
                orderStartTime = item.getStartTime();
                orderStatus = OrderStatus.running.name();
                isOrderStart = true;
            }
            co.setCurrentOrderStepId(item.getId());

            dbLayer.updateOrderOnOrderStep(co.getId(), orderStartTime, orderStatus, co.getCurrentOrderStepId(), new Date());

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
                saveOrderStatus(dbLayer, co, OrderStatus.started.name(), item.getWorkflowPath(), item.getWorkflowVersionId(), cos
                        .getWorkflowPosition(), entry.getEventDate(), entry.getEventId());

                ChunkLogEntry cle = new ChunkLogEntry(LogLevel.Info, OutType.Stdout, LogType.OrderStart, masterTimezone, entry.getEventId(), entry
                        .getTimestamp(), startTime);
                cle.onOrder(co, cos.getWorkflowPosition());
                storeLog2File(cle);
            }

            ChunkLogEntry cle = new ChunkLogEntry(LogLevel.Info, OutType.Stdout, LogType.OrderStepStart, masterTimezone, entry.getEventId(), entry
                    .getTimestamp(), startTime);
            cle.onOrderStep(cos);
            storeLog2File(cle);

            tryStoreCurrentState(dbLayer, entry.getEventId());
        }
    }

    private void orderStepEnd(DBLayerHistory dbLayer, Entry entry) throws Exception {
        CachedOrderStep cos = getCachedOrderStep(dbLayer, entry.getKey());
        if (cos.getEndTime() == null) {
            checkMasterTimezone(dbLayer);

            if (entry.getOutcome().getType().equalsIgnoreCase(OrderStepStatus.failed.name())) {
                cos.setError(true);
            }
            if (entry.getOutcome().getReason() != null && entry.getOutcome().getReason().getProblem() != null) {
                cos.setError(true);
                cos.setErrorText(entry.getOutcome().getReason().getProblem().getMessage());
            }
            cos.setReturnCode(entry.getOutcome().getReturnCode());

            Date endTime = entry.getEventDate();
            dbLayer.setOrderStepEnd(cos.getId(), endTime, String.valueOf(entry.getEventId()), EventMeta.map2Json(entry.getKeyValues()), entry
                    .getOutcome().getReturnCode(), entry.getOutcome().getType().toLowerCase(), cos.getError(), cos.getErrorText(), new Date());

            ChunkLogEntry cle = new ChunkLogEntry(LogLevel.Info, OutType.Stdout, LogType.OrderStepEnd, masterTimezone, entry.getEventId(), entry
                    .getTimestamp(), endTime);
            cle.onOrderStep(cos);

            DBItemLog logItem = storeLogFile2Db(dbLayer, cos.getMainOrderId(), cos.getOrderId(), cos.getId(), storeLog2File(cle));
            if (logItem != null) {
                dbLayer.setOrderStepLogId(cos.getId(), logItem.getId());
            }

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
            CachedAgent ca = getCachedAgent(dbLayer, cos.getAgentPath());

            ChunkLogEntry cle = new ChunkLogEntry(LogLevel.Info, outType, LogType.OrderStepOut, ca.getTimezone(), entry.getEventId(), entry
                    .getTimestamp(), entry.getEventDate());
            cle.onOrderStep(cos, entry.getChunk());
            storeLog2File(cle);

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
            DBItemOrder item = dbLayer.getOrder(configuration.getCurrent().getId(), key);
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
        DBItemOrder item = dbLayer.getOrder(configuration.getCurrent().getId(), key, startEventId);
        if (item == null) {
            throw new Exception(String.format("[%s]order not found. orderKey=%s, startEventId=%s", identifier, key, startEventId));
        } else {
            addCachedOrder(key, new CachedOrder(item));
        }
    }

    private void addCachedOrderStep(String key, CachedOrderStep co) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][addCachedOrderStep][%s]jobName=%s, workflowPosition=%s", identifier, key, co.getJobName(), co
                    .getWorkflowPosition()));
        }
        cachedOrderSteps.put(key, co);
    }

    private CachedOrderStep getCachedOrderStep(DBLayerHistory dbLayer, String key) throws Exception {
        CachedOrderStep co = getCachedOrderStep(key);
        if (co == null) {
            DBItemOrderStep item = dbLayer.getOrderStep(configuration.getCurrent().getId(), key);
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
        DBItemOrderStep item = dbLayer.getOrderStep(configuration.getCurrent().getId(), key, startEventId);
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
            DBItemAgent item = dbLayer.getAgent(configuration.getCurrent().getId(), key);
            if (item == null) {
                throw new Exception(String.format("[%s]agent not found. masterId=%s, agentPath=%s", identifier, configuration.getCurrent().getId(),
                        key));
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
        default:
            break;
        }
    }

    private DBItemLog storeLogFile2Db(DBLayerHistory dbLayer, Long mainOrderId, Long orderId, Long orderStepId, Path file) throws Exception {
        if (!configuration.getLogStoreLog2Db()) {
            return null;
        }

        DBItemLog item = null;
        File f = file.toFile();
        if (f.exists()) {
            item = new DBItemLog();
            item.setMasterId(configuration.getCurrent().getId());

            item.setMainOrderId(mainOrderId);
            item.setOrderId(orderId);
            item.setOrderStepId(orderStepId);

            item.setFileBasename(com.google.common.io.Files.getNameWithoutExtension(f.getName()));
            item.setFileSizeUncomressed(f.length());
            item.setFileLinesUncomressed(Files.lines(file).count());
            item.setFileCompressed(HistoryUtil.gzipCompress(file));
            item.setCreated(new Date());

            dbLayer.getSession().save(item);
        }
        return item;
    }

    private Path storeLog2File(ChunkLogEntry logEntry) throws Exception {

        StringBuilder content = new StringBuilder();
        Path file = null;
        boolean newLine = true;
        LinkedHashMap<String, String> hm = null;

        switch (logEntry.getLogType()) {
        case OrderStepStart:
        case OrderStepEnd:
            // ORDER LOG
            hm = new LinkedHashMap<>();
            hm.put("date", SOSDate.getDateAsString(logEntry.getDate(), "yyyy-MM-dd HH:mm:ss.SSS"));
            hm.put("log_level", logEntry.getLogLevel().name().toUpperCase());
            hm.put("log_type", logEntry.getLogType().name().toUpperCase());
            hm.put("orderKey", logEntry.getOrderKey());
            hm.put("position", logEntry.getPosition());
            hm.put("agent_path", logEntry.getAgentPath());
            hm.put("agent_uri", logEntry.getAgentUri());
            hm.put("jobName", logEntry.getJobName());
            if (logEntry.getLogType().equals(LogType.OrderStepEnd)) {
                hm.put("returnCode", logEntry.getReturnCode() == null ? "" : String.valueOf(logEntry.getReturnCode()));
                if (logEntry.isError()) {
                    hm.put("error", "1");
                    hm.put("error_text", logEntry.getErrorText());
                } else {
                    hm.put("error", "0");
                }

            }
            write2file(Paths.get(configuration.getLogDir(), logEntry.getMainOrderId() + ".log"), new StringBuilder(hm.toString()), newLine);

            // STEP LOG
            file = Paths.get(configuration.getLogDir(), logEntry.getMainOrderId() + "_" + logEntry.getOrderStepId() + ".log");
            content.append("[").append(SOSDate.getDateAsString(logEntry.getDate(), "yyyy-MM-dd HH:mm:ss.SSS")).append("]");
            content.append("[").append(logEntry.getLogLevel().name().toUpperCase()).append("]");
            content.append(logEntry.getChunk());

            break;

        case OrderStepOut:
            // STEP LOG
            file = Paths.get(configuration.getLogDir(), logEntry.getMainOrderId() + "_" + logEntry.getOrderStepId() + ".log");
            content.append("[").append(SOSDate.getDateAsString(logEntry.getDate(), "yyyy-MM-dd HH:mm:ss.SSS")).append("]");
            content.append("[").append(logEntry.getOutType().name().toUpperCase()).append("]");
            content.append(logEntry.getChunk());

            newLine = false;
            break;
        default:
            // ORDER LOG
            file = Paths.get(configuration.getLogDir(), logEntry.getMainOrderId() + ".log");
            hm = new LinkedHashMap<>();
            hm.put("date", SOSDate.getDateAsString(logEntry.getDate(), "yyyy-MM-dd HH:mm:ss.SSS"));
            hm.put("log_level", logEntry.getLogLevel().name().toUpperCase());
            hm.put("log_type", logEntry.getLogType().name().toUpperCase());
            hm.put("orderKey", logEntry.getOrderKey());
            hm.put("position", logEntry.getPosition());
            content.append(hm);
        }

        try {
            write2file(file, content, newLine);
        } catch (Throwable t) {
            LOGGER.error(String.format("[%s][%s][%s][%s]%s", identifier, logEntry.getLogType().name(), logEntry.getOrderKey(), file, t.toString()),
                    t);
            throw t;
        }

        return file;
    }

    private void write2file(Path file, StringBuilder content, boolean newLine) throws Exception {

        if (configuration.getLogUseLog4j2Writer()) {
            if (newLine) {
                content.append(HistoryUtil.NEW_LINE);
            }
            MDC.put(LOGGER_HISTORY_FILE_WRITER_PARAM_LOGDIR, configuration.getLogDir());
            MDC.put(LOGGER_HISTORY_FILE_WRITER_PARAM_FILENAME, String.valueOf(file.toFile().getCanonicalPath()));
            LOGGER_HISTORY_FILE_WRITER.info(content.toString());
            MDC.clear();
        } else {
            OutputStreamWriter writer = null;
            try {
                writer = new OutputStreamWriter(new FileOutputStream(file.toString(), true), "UTF-8");

                writer.write(content.toString());
                if (newLine) {
                    writer.write(HistoryUtil.NEW_LINE);
                }
            } catch (Throwable t) {
                throw t;
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (Exception ex) {
                    }
                }
            }
        }
    }

    private void saveOrderStatus(DBLayerHistory dbLayer, CachedOrder co, String status, String workflowPath, String workflowVersionId,
            String workflowPosition, Date statusTime, Long eventId) throws Exception {
        if (configuration.getSaveOrderStatus()) {
            DBItemOrderStatus item = new DBItemOrderStatus();
            item.setMasterId(configuration.getCurrent().getId());
            item.setOrderKey(co.getOrderKey());

            item.setWorkflowPath(workflowPath);
            item.setWorkflowVersionId(workflowVersionId);
            item.setWorkflowPosition(workflowPosition);

            item.setMainOrderId(co.getMainParentId());
            item.setOrderId(co.getId());
            item.setOrderStepId(co.getCurrentOrderStepId());

            item.setStatus(status);
            item.setStatusTime(statusTime);

            item.setConstraintHash(hashStatusConstaint(eventId, co.getOrderKey(), item.getOrderStepId()));
            item.setCreated(new Date());

            try {
                dbLayer.getSession().save(item);
            } catch (SOSHibernateObjectOperationException e) {
                Exception cve = SOSHibernate.findConstraintViolationException(e);
                if (cve == null) {
                    LOGGER.error(e.toString(), e);
                    throw e;
                }
                LOGGER.warn(String.format("[%s][saveOrderStatus][exception][%s]%s", identifier, SOSString.toString(item), e.toString()), e);
            }
        }
    }

    private String hashOrderConstaint(Long eventId, String orderKey, String workflowPosition) {
        return HistoryUtil.hashString(configuration.getCurrent().getId() + String.valueOf(eventId) + orderKey + workflowPosition);
    }

    private String hashOrderStepConstaint(Long eventId, String orderKey, String workflowPosition) {
        return hashOrderConstaint(eventId, orderKey, workflowPosition);
    }

    private String hashStatusConstaint(Long eventId, String orderKey, Long orderStepId) {
        return HistoryUtil.hashString(configuration.getCurrent().getId() + String.valueOf(eventId) + orderKey + String.valueOf(orderStepId));
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
