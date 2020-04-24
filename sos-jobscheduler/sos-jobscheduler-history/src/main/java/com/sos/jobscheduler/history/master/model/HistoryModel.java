package com.sos.jobscheduler.history.master.model;

import java.io.BufferedWriter;
import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.sos.jobscheduler.db.history.DBItemOrderStep;
import com.sos.jobscheduler.event.master.EventMeta;
import com.sos.jobscheduler.event.master.bean.Event;
import com.sos.jobscheduler.event.master.bean.IEntry;
import com.sos.jobscheduler.event.master.configuration.master.MasterConfiguration;
import com.sos.jobscheduler.event.master.fatevent.EventMeta.EventType;
import com.sos.jobscheduler.event.master.fatevent.bean.Entry;
import com.sos.jobscheduler.event.master.fatevent.bean.OrderForkedChild;
import com.sos.jobscheduler.event.master.fatevent.bean.Outcome;
import com.sos.jobscheduler.event.master.handler.http.HttpClient;
import com.sos.jobscheduler.event.master.handler.http.RestServiceDuration;
import com.sos.jobscheduler.history.db.DBLayerHistory;
import com.sos.jobscheduler.history.helper.CachedAgent;
import com.sos.jobscheduler.history.helper.CachedOrder;
import com.sos.jobscheduler.history.helper.CachedOrderStep;
import com.sos.jobscheduler.history.helper.LogEntry;
import com.sos.jobscheduler.history.helper.LogEntry.LogLevel;
import com.sos.jobscheduler.history.helper.LogEntry.LogType;
import com.sos.jobscheduler.history.helper.LogEntry.OutType;
import com.sos.jobscheduler.history.helper.HistoryUtil;
import com.sos.jobscheduler.history.master.configuration.HistoryConfiguration;

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
    private HistoryConfiguration historyConfiguration;
    private MasterConfiguration masterConfiguration;
    private HttpClient httpClient;
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

    public static enum OrderState {
        planned, running, finished, failed, cancelled
    };

    public static enum OrderStepState {
        running, processed
    };

    public static enum OrderErrorType {
        failed, disrupted
    }

    public static enum OrderStepErrorType {
        failed, disrupted
    }

    private static enum CacheType {
        master, agent, order, orderStep
    };

    private static enum OrderStartCase {
        order, fork, file_trigger, setback, unskip, unstop
    };

    private static enum OrderStepStartCase {
        order, file_trigger, setback, unskip, unstop
    };

    public HistoryModel(SOSHibernateFactory factory, HistoryConfiguration historyConf, MasterConfiguration masterConf, String ident) {
        dbFactory = factory;
        isMySQL = dbFactory.getDbms().equals(Dbms.MYSQL);
        historyConfiguration = historyConf;
        masterConfiguration = masterConf;
        identifier = ident;
        variable = "history_" + masterConfiguration.getCurrent().getId();
        maxTransactions = historyConfiguration.getMaxTransactions();
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

        doDiagnostic("onMasterNonEmptyEventResponse", lastRestServiceDuration.getDuration(), historyConfiguration
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
                if (isDebugEnabled) {
                    LOGGER.debug("--- " + entry.getType() + " ------------------------------------------");
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
                case OrderFailedFat:
                case OrderCancelledFat:
                case OrderFinishedFat:
                    orderEnd(dbLayer, entry);
                    break;
                }
                processedEventsCounter++;
                lastSuccessEventId = eventId;
            }

            tryStoreCurrentStateAtEnd(dbLayer, lastSuccessEventId);
        } catch (Throwable e) {
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

        doDiagnostic("onHistory", duration, historyConfiguration.getDiagnosticStartIfHistoryExecutionLongerThan());

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
                    String identifier = Thread.currentThread().getName() + "-" + masterConfiguration.getCurrent().getId();
                    LOGGER_DIAGNOSTIC.info(String.format("[%s]duration=%s", range, SOSDate.getDuration(duration)));
                    HistoryUtil.printCpuLoad(LOGGER_DIAGNOSTIC, identifier);
                    if (!SOSString.isEmpty(historyConfiguration.getDiagnosticAdditionalScript())) {
                        HistoryUtil.executeCommand(LOGGER_DIAGNOSTIC, historyConfiguration.getDiagnosticAdditionalScript(), identifier);
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

        if (duration.toMillis() > maxTime * 1_000) {
            try {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        String identifier = Thread.currentThread().getName() + "-" + masterConfiguration.getCurrent().getId();
                        LOGGER_DIAGNOSTIC.info(String.format("[%s]duration=%s", range, SOSDate.getDuration(duration)));
                        HistoryUtil.printCpuLoad(LOGGER_DIAGNOSTIC, identifier);
                        if (!SOSString.isEmpty(historyConfiguration.getDiagnosticAdditionalScript())) {
                            HistoryUtil.executeCommand(LOGGER_DIAGNOSTIC, historyConfiguration.getDiagnosticAdditionalScript(), identifier);
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
            item.setJobSchedulerId(masterConfiguration.getCurrent().getId());
            item.setUri(masterConfiguration.getCurrent().getUri());
            item.setTimezone(entry.getTimezone());
            item.setStartTime(entry.getEventDate());
            item.setPrimaryMaster(masterConfiguration.getCurrent().isPrimary());
            item.setEventId(String.valueOf(entry.getEventId()));
            item.setCreated(new Date());

            dbLayer.getSession().save(item);

            masterTimezone = item.getTimezone();
            LogEntry le = new LogEntry(LogLevel.Debug, OutType.Stdout, LogType.MasterReady, masterTimezone, entry.getEventId(), entry.getTimestamp(),
                    item.getStartTime());
            le.onMaster(masterConfiguration);
            storeLog2File(le);

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
            masterTimezone = dbLayer.getMasterTimezone(masterConfiguration.getCurrent().getId());
            if (masterTimezone == null) {
                throw new Exception(String.format("master not founded: %s", masterConfiguration.getCurrent().getId()));
            }
        }
    }

    private void agentAdd(DBLayerHistory dbLayer, Entry entry) throws Exception {

        try {
            checkMasterTimezone(dbLayer);

            DBItemAgent item = new DBItemAgent();
            item.setJobSchedulerId(masterConfiguration.getCurrent().getId());
            item.setPath(entry.getKey());
            item.setUri(".");// TODO
            item.setTimezone(entry.getTimezone());
            item.setStartTime(entry.getEventDate());
            item.setEventId(String.valueOf(entry.getEventId()));
            item.setCreated(new Date());

            dbLayer.getSession().save(item);

            CachedAgent ca = new CachedAgent(item);
            LogEntry le = new LogEntry(LogLevel.Debug, OutType.Stdout, LogType.AgentReady, masterTimezone, entry.getEventId(), entry.getTimestamp(),
                    item.getStartTime());
            le.onAgent(ca);
            storeLog2File(le);

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
            item.setJobSchedulerId(masterConfiguration.getCurrent().getId());
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
            Date planned = entry.getScheduledForAsDate();
            if (planned == null) {
                planned = entry.getEventDate();
            }
            item.setStartTimePlanned(planned);
            item.setStartTime(new Date(0));// 1970-01-01 01:00:00 TODO
            item.setStartWorkflowPosition(entry.getWorkflowPosition().getPositionAsString());
            item.setStartEventId(String.valueOf(entry.getEventId()));
            item.setStartParameters(EventMeta.map2Json(entry.getArguments()));

            item.setCurrentOrderStepId(new Long(0));

            item.setEndTime(null);
            item.setEndWorkflowPosition(null);
            item.setEndOrderStepId(new Long(0));

            item.setState(OrderState.planned.name());// TODO
            item.setStateTime(entry.getEventDate());
            item.setStateText(null);// TODO

            item.setError(false);
            item.setErrorState(null);
            item.setErrorReason(null);
            item.setErrorReturnCode(null);
            item.setErrorCode(null);
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
            LogEntry le = new LogEntry(LogLevel.Debug, OutType.Stdout, LogType.OrderAdded, masterTimezone, entry.getEventId(), entry.getTimestamp(),
                    entry.getEventDate());
            le.onOrder(co, item.getWorkflowPosition());
            storeLog2File(le);

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

            DBItemOrderStep currentStep = null;
            Date endTime = null;
            String endWorkflowPosition = null;
            Long endOrderStepId = null;
            String endEventId = null;
            String state = null;
            boolean isOrderEnd = false;

            switch (eventType) {
            case OrderFailedFat:
                logType = LogType.OrderFailed;
                state = OrderState.failed.name();
                break;
            case OrderCancelledFat:
                logType = LogType.OrderCancelled;
                state = OrderState.cancelled.name();
                isOrderEnd = true;
                break;
            default:
                state = OrderState.finished.name();
                isOrderEnd = true;
                break;
            }

            if (co.getCurrentOrderStepId() > 0) {// when starts with fork <- step id is 0
                currentStep = dbLayer.getOrderStep(co.getCurrentOrderStepId());
                if (currentStep == null) {
                    LOGGER.debug(String.format("[%s][%s][%s]currentStep not found, id=%s", identifier, eventType, orderKey, co
                            .getCurrentOrderStepId()));
                }
            }

            if (isOrderEnd) {
                endTime = eventDate;
                endWorkflowPosition = currentStep == null ? co.getWorkflowPosition() : currentStep.getWorkflowPosition();
                endOrderStepId = currentStep == null ? co.getCurrentOrderStepId() : currentStep.getId();
                endEventId = String.valueOf(eventId);
            }

            if (outcome != null) {
                if (outcome.getType().equalsIgnoreCase(OrderErrorType.failed.name()) || outcome.getType().equalsIgnoreCase(OrderErrorType.disrupted
                        .name())) {

                    boolean setError = true;
                    if (logType.equals(LogType.ForkBranchEnd) && currentStep != null && !currentStep.getError()) { // TODO tmp solution for Fork
                        setError = false;
                    }
                    if (setError) {
                        co.setError(true);
                        co.setErrorState(outcome.getType().toLowerCase());
                        co.setErrorReturnCode(outcome.getReturnCode());// not null by Fail
                        if (outcome.getReason() != null) {
                            co.setErrorReason(outcome.getReason().getType());
                            co.setErrorText(outcome.getReason().getProblem().getMessage());
                        }
                    }
                }
            }
            if (!co.getError() && currentStep != null && currentStep.getError()) {
                co.setError(true);
                co.setErrorReturnCode(currentStep.getReturnCode());
                co.setErrorText(currentStep.getErrorText());
            }

            if (logType.equals(LogType.ForkBranchEnd) && co.getError() && state.equals(OrderState.finished.name())) {// TODO tmp for Fork
                state = OrderState.failed.name();
            }

            dbLayer.setOrderEnd(co.getId(), endTime, endWorkflowPosition, endOrderStepId, endEventId, state, eventDate, co.getError(), co
                    .getErrorState(), co.getErrorReason(), co.getErrorReturnCode(), co.getErrorCode(), co.getErrorText(), new Date());

            LogEntry le = new LogEntry(LogLevel.Info, OutType.Stdout, logType, masterTimezone, eventId, eventTimestamp, eventDate);
            le.onOrder(co, co.getWorkflowPosition());

            Path logFile = storeLog2File(le);
            if (isOrderEnd) {
                DBItemLog logItem = storeLogFile2Db(dbLayer, co.getMainParentId(), co.getId(), new Long(0), false, logFile);
                if (logItem != null) {
                    dbLayer.setOrderLogId(co.getId(), logItem.getId());
                }
            }

            tryStoreCurrentState(dbLayer, eventId);

            if (isOrderEnd && co.getParentId() == 0) {
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
        if (!SOSString.isEmpty(historyConfiguration.getUriHistoryExecutor())) {
            try {
                if (httpClient == null) {
                    httpClient = new HttpClient();
                }
                URI uri = new URI(historyConfiguration.getUriHistoryExecutor());
                String response = httpClient.executePost(uri, params, null, true);
                LOGGER.info(String.format("[%s][%s][%s][%s]%s", identifier, httpClient.getLastRestServiceDuration(), historyConfiguration
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
        if (co.getState().equals(OrderState.planned.name())) {
            co.setState(OrderState.running.name());
        }
        co.setHasChildren(true);
        // addCachedOrder(co.getOrderKey(), co);
        if (entry.getWorkflowPosition().getOrderPositionAsString().equals(co.getStartWorkflowPosition())) {
            dbLayer.updateOrderOnFork(co.getId(), startTime, co.getState());
        } else {
            dbLayer.updateOrderOnFork(co.getId(), co.getState());
        }

        LogEntry le = new LogEntry(LogLevel.Info, OutType.Stdout, LogType.Fork, masterTimezone, entry.getEventId(), entry.getTimestamp(), startTime);
        le.onOrder(co, entry.getWorkflowPosition().getOrderPositionAsString(), entry.getChildren());
        storeLog2File(le);

        for (int i = 0; i < entry.getChildren().size(); i++) {
            orderForkedAdd(dbLayer, entry, co, entry.getChildren().get(i), startTime);
        }
    }

    private void orderForkedAdd(DBLayerHistory dbLayer, Entry entry, CachedOrder parentOrder, OrderForkedChild forkOrder, Date startTime)
            throws Exception {
        try {
            checkMasterTimezone(dbLayer);

            DBItemOrder item = new DBItemOrder();
            item.setJobSchedulerId(masterConfiguration.getCurrent().getId());
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

            item.setState(OrderState.running.name());// TODO
            item.setStateTime(startTime);
            item.setStateText(null);// TODO

            item.setError(false);
            item.setErrorState(null);
            item.setErrorReason(null);
            item.setErrorReturnCode(null);
            item.setErrorCode(null);
            item.setErrorText(null);
            item.setEndEventId(null);

            item.setLogId(new Long(0));

            item.setConstraintHash(hashOrderConstaint(entry.getEventId(), item.getOrderKey(), item.getWorkflowPosition()));
            item.setCreated(new Date());
            item.setModified(item.getCreated());

            dbLayer.getSession().save(item);

            CachedOrder co = new CachedOrder(item);

            LogEntry le = new LogEntry(LogLevel.Debug, OutType.Stdout, LogType.ForkBranchStart, masterTimezone, entry.getEventId(), entry
                    .getTimestamp(), startTime);
            le.onOrder(co, item.getWorkflowPosition());
            storeLog2File(le);

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

        LogEntry le = new LogEntry(LogLevel.Info, OutType.Stdout, LogType.ForkJoin, masterTimezone, entry.getEventId(), entry.getTimestamp(),
                endTime);
        le.onOrderJoined(co, fco.getWorkflowPosition(), entry.getChildOrderIds());
        storeLog2File(le);
    }

    private void orderStepStart(DBLayerHistory dbLayer, Entry entry) throws Exception {
        CachedOrder co = null;
        CachedOrderStep cos = null;
        Date startTime = entry.getEventDate();
        DBItemOrderStep item = null;
        try {
            checkMasterTimezone(dbLayer);

            co = getCachedOrder(dbLayer, entry.getKey());

            CachedAgent ca = getCachedAgent(dbLayer, entry.getAgentRefPath());
            // TODO temp solution
            if (!ca.getUri().equals(entry.getAgentUri())) {
                ca.setUri(entry.getAgentUri());
                dbLayer.updateAgent(ca.getId(), ca.getUri());
                // addCachedAgent(entry.getAgentRefPath(), ca);
            }

            item = new DBItemOrderStep();
            item.setJobSchedulerId(masterConfiguration.getCurrent().getId());
            item.setOrderKey(entry.getKey());

            item.setWorkflowPath(entry.getWorkflowPosition().getWorkflowId().getPath());
            item.setWorkflowVersionId(entry.getWorkflowPosition().getWorkflowId().getVersionId());
            item.setWorkflowPosition(entry.getWorkflowPosition().getPositionAsString());
            item.setWorkflowFolder(HistoryUtil.getFolderFromPath(item.getWorkflowPath()));
            item.setWorkflowName(HistoryUtil.getBasenameFromPath(item.getWorkflowPath()));

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
            item.setState(OrderStepState.running.name());

            item.setError(false);
            item.setErrorCode(null);
            item.setErrorText(null);

            item.setLogId(new Long(0));

            item.setConstraintHash(hashOrderStepConstaint(entry.getEventId(), item.getOrderKey(), item.getWorkflowPosition()));
            item.setCreated(new Date());
            item.setModified(item.getCreated());

            dbLayer.getSession().save(item);

            co.setCurrentOrderStepId(item.getId());

            // TODO check for Fork -
            if (item.getWorkflowPosition().equals(co.getStartWorkflowPosition())) {// + order.startTime != default
                // ORDER START
                co.setState(OrderState.running.name());
                dbLayer.updateOrderOnOrderStep(co.getId(), item.getStartTime(), co.getState(), co.getCurrentOrderStepId(), new Date());

                // addCachedOrder(co.getOrderKey(), co);
                LogEntry le = new LogEntry(LogLevel.Info, OutType.Stdout, LogType.OrderStart, masterTimezone, entry.getEventId(), entry
                        .getTimestamp(), startTime);
                le.onOrder(co, item.getWorkflowPosition());
                storeLog2File(le);

            } else {
                dbLayer.updateOrderOnOrderStep(co.getId(), co.getCurrentOrderStepId(), new Date());
            }
            // addCachedOrder(co.getOrderKey(), co);
            cos = new CachedOrderStep(item);
            addCachedOrderStep(item.getOrderKey(), cos);

            LogEntry le = new LogEntry(LogLevel.Info, OutType.Stdout, LogType.OrderStepStart, masterTimezone, entry.getEventId(), entry
                    .getTimestamp(), startTime);
            le.onOrderStep(cos);
            storeLog2File(le);

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
            if (co != null) {
                addCachedOrder(co.getOrderKey(), co);
            }
            addCachedOrderStepByStartEventId(dbLayer, entry.getKey(), String.valueOf(entry.getEventId()));
        }
    }

    private void orderStepEnd(DBLayerHistory dbLayer, Entry entry) throws Exception {
        CachedOrderStep cos = getCachedOrderStep(dbLayer, entry.getKey());
        if (cos.getEndTime() == null) {
            checkMasterTimezone(dbLayer);

            if (entry.getOutcome() != null) {
                if (entry.getOutcome().getType().equalsIgnoreCase(OrderStepErrorType.failed.name()) || entry.getOutcome().getType().equalsIgnoreCase(
                        OrderStepErrorType.disrupted.name())) {
                    cos.setError(true);

                    cos.setErrorState(entry.getOutcome().getType().toLowerCase());
                    if (entry.getOutcome().getReason() != null) {
                        cos.setErrorReason(entry.getOutcome().getReason().getType());
                        cos.setErrorText(entry.getOutcome().getReason().getProblem().getMessage());
                    }
                }
            }
            cos.setReturnCode(entry.getOutcome().getReturnCode());

            Date endTime = entry.getEventDate();
            dbLayer.setOrderStepEnd(cos.getId(), endTime, String.valueOf(entry.getEventId()), EventMeta.map2Json(entry.getKeyValues()), entry
                    .getOutcome().getReturnCode(), OrderStepState.processed.name(), cos.getError(), cos.getErrorState(), cos.getErrorReason(), cos
                            .getErrorCode(), cos.getErrorText(), new Date());

            LogEntry le = new LogEntry(LogLevel.Info, OutType.Stdout, LogType.OrderStepEnd, masterTimezone, entry.getEventId(), entry.getTimestamp(),
                    endTime);
            le.onOrderStep(cos);

            DBItemLog logItem = storeLogFile2Db(dbLayer, cos.getMainOrderId(), cos.getOrderId(), cos.getId(), true, storeLog2File(le));
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
            LogEntry le = new LogEntry(LogLevel.Info, outType, LogType.OrderStepOut, ca.getTimezone(), entry.getEventId(), entry.getTimestamp(), entry
                    .getEventDate());
            le.onOrderStep(cos, entry.getChunk());
            storeLog2File(le, cos);

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
            LOGGER.debug(String.format("[%s][addCachedOrder][%s]%s", identifier, key, SOSString.toString(order)));
        }
        cachedOrders.put(key, order);
    }

    private CachedOrder getCachedOrder(DBLayerHistory dbLayer, String key) throws Exception {
        CachedOrder co = getCachedOrder(key);
        if (co == null) {
            DBItemOrder item = dbLayer.getOrder(masterConfiguration.getCurrent().getId(), key);
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
        DBItemOrder item = dbLayer.getOrder(masterConfiguration.getCurrent().getId(), key, startEventId);
        if (item == null) {
            throw new Exception(String.format("[%s]order not found. orderKey=%s, startEventId=%s", identifier, key, startEventId));
        } else {
            addCachedOrder(key, new CachedOrder(item));
        }
    }

    private void addCachedOrderStep(String key, CachedOrderStep co) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][addCachedOrderStep][%s]%s", identifier, key, SOSString.toString(co)));
        }
        cachedOrderSteps.put(key, co);
    }

    private CachedOrderStep getCachedOrderStep(DBLayerHistory dbLayer, String key) throws Exception {
        CachedOrderStep co = getCachedOrderStep(key);
        if (co == null) {
            DBItemOrderStep item = dbLayer.getOrderStep(masterConfiguration.getCurrent().getId(), key);
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
        DBItemOrderStep item = dbLayer.getOrderStep(masterConfiguration.getCurrent().getId(), key, startEventId);
        if (item == null) {
            throw new Exception(String.format("[%s]order step not found. orderKey=%s, startEventId=%s", identifier, key, startEventId));
        } else {
            addCachedOrderStep(key, new CachedOrderStep(item));
        }
    }

    private void addCachedAgent(String key, CachedAgent ca) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][addCachedAgent][%s]%s", identifier, key, SOSString.toString(ca)));
        }
        cachedAgents.put(key, ca);
    }

    private CachedAgent getCachedAgent(DBLayerHistory dbLayer, String key) throws Exception {
        CachedAgent co = getCachedAgent(key);
        if (co == null) {
            DBItemAgent item = dbLayer.getAgent(masterConfiguration.getCurrent().getId(), key);
            if (item == null) {
                throw new Exception(String.format("[%s]agent not found. jobSchedulerId=%s, agentPath=%s", identifier, masterConfiguration.getCurrent()
                        .getId(), key));
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

    private DBItemLog storeLogFile2Db(DBLayerHistory dbLayer, Long mainOrderId, Long orderId, Long orderStepId, boolean isOrderStepLog, Path file)
            throws Exception {
        if (!historyConfiguration.getLogStoreLog2Db()) {
            return null;
        }

        DBItemLog item = null;
        File f = file.toFile();
        if (f.exists()) {
            item = new DBItemLog();
            item.setJobSchedulerId(masterConfiguration.getCurrent().getId());

            item.setMainOrderId(mainOrderId);
            item.setOrderId(orderId);
            item.setOrderStepId(orderStepId);
            item.setCompressed(isOrderStepLog);

            item.setFileBasename(com.google.common.io.Files.getNameWithoutExtension(f.getName()));
            item.setFileSizeUncomressed(f.length());
            Long lines = new Long(0);
            try {
                lines = Files.lines(file).count();
            } catch (Exception e) {
                LOGGER.error(String.format("[%s][storeLogFile2Db][%s]can't get file lines: %s", identifier, f.getCanonicalPath(), e.toString()), e);
            }
            item.setFileLinesUncomressed(lines);
            if (item.getCompressed()) {// task
                item.setFileContent(HistoryUtil.gzipCompress(file));
            } else {// order
                item.setFileContent(new StringBuilder("[").append(Files.readAllBytes(file)).append("]").toString().getBytes());
            }
            item.setCreated(new Date());

            dbLayer.getSession().save(item);
        }
        return item;
    }

    private Path storeLog2File(LogEntry entry) throws Exception {
        return storeLog2File(entry, null);
    }

    private Path getMasterAndAgentsLog() {
        return Paths.get(historyConfiguration.getLogDir(), "0.log");
    }

    private Path getOrderLog(LogEntry entry) {
        return Paths.get(historyConfiguration.getLogDir(), entry.getMainOrderId() + ".log");
    }

    private Path getOrderStepLog(LogEntry entry) {
        return Paths.get(historyConfiguration.getLogDir(), entry.getMainOrderId() + "_" + entry.getOrderStepId() + ".log");
    }

    private Path storeLog2File(LogEntry entry, CachedOrderStep cos) throws Exception {

        com.sos.jobscheduler.history.order.LogEntry orderEntry = null;
        Path file = null;
        StringBuilder content = new StringBuilder();
        boolean newLine = true;

        switch (entry.getLogType()) {
        case OrderStepStart:
        case OrderStepEnd:
            orderEntry = new com.sos.jobscheduler.history.order.LogEntry();
            orderEntry.setDate(entry.getDate());
            orderEntry.setLogLevel(entry.getLogLevel().name().toUpperCase());
            orderEntry.setLogType(entry.getLogType().name().toUpperCase());
            orderEntry.setOrderKey(entry.getOrderKey());
            orderEntry.setPosition(entry.getPosition());
            orderEntry.setAgentPath(entry.getAgentPath());
            orderEntry.setAgentUrl(entry.getAgentUri());
            orderEntry.setJobName(entry.getJobName());
            if (entry.getLogType().equals(LogType.OrderStepEnd)) {
                orderEntry.setReturnCode(entry.getReturnCode());
                orderEntry.setError(entry.isError());
                if (entry.isError()) {
                    orderEntry.setErrorStatus(entry.getErrorState());
                    orderEntry.setErrorReason(entry.getErrorReason());
                    orderEntry.setErrorCode(entry.getErrorCode());
                    orderEntry.setErrorText(entry.getErrorText());
                }
            }
            // order log
            write2file(getOrderLog(entry), new StringBuilder(new ObjectMapper().writeValueAsString(orderEntry)), newLine);

            // order step log - meta infos
            file = getOrderStepLog(entry);
            content.append("[").append(SOSDate.getDateAsString(entry.getDate(), "yyyy-MM-dd HH:mm:ss.SSS")).append("]");
            content.append("[").append(entry.getLogLevel().name().toUpperCase()).append("]");
            content.append(entry.getChunk());

            break;

        case OrderStepOut:
            // order step log - stdout|stderr
            file = getOrderStepLog(entry);
            if (cos.getLastStdHasNewLine() == null || cos.getLastStdHasNewLine()) {
                content.append("[").append(SOSDate.getDateAsString(entry.getDate(), "yyyy-MM-dd HH:mm:ss.SSS")).append("]");
                content.append("[").append(entry.getOutType().name().toUpperCase()).append("]");
            }
            content.append(entry.getChunk());
            cos.setLastStdHasNewLine(entry.getChunk().endsWith("\n"));

            newLine = false;
            break;
        case AgentReady:
        case MasterReady:
            file = getMasterAndAgentsLog();
            LinkedHashMap<String, String> hm = new LinkedHashMap<>();
            hm.put("date", SOSDate.getDateAsString(entry.getDate(), "yyyy-MM-dd HH:mm:ss.SSS"));
            hm.put("log_level", entry.getLogLevel().name().toUpperCase());
            hm.put("log_type", entry.getLogType().name().toUpperCase());
            hm.put("orderKey", entry.getOrderKey());
            hm.put("position", entry.getPosition());

            if (entry.isError()) {
                hm.put("error", "1");
                hm.put("error_state", entry.getErrorState());
                hm.put("error_reason", entry.getErrorReason());
                hm.put("error_code", entry.getErrorCode());
                hm.put("error_return_code", entry.getReturnCode() == null ? "" : String.valueOf(entry.getReturnCode()));
                hm.put("error_text", entry.getErrorText());
            }
            content.append(hm);
            break;
        default:
            // ORDER LOG
            file = getOrderLog(entry);

            orderEntry = new com.sos.jobscheduler.history.order.LogEntry();
            orderEntry.setDate(entry.getDate());
            orderEntry.setLogLevel(entry.getLogLevel().name().toUpperCase());
            orderEntry.setLogType(entry.getLogType().name().toUpperCase());
            orderEntry.setOrderKey(entry.getOrderKey());
            orderEntry.setPosition(entry.getPosition());
            if (entry.isError()) {
                orderEntry.setError(true);
                orderEntry.setErrorStatus(entry.getErrorState());
                orderEntry.setErrorReason(entry.getErrorReason());
                orderEntry.setErrorCode(entry.getErrorCode());
                orderEntry.setReturnCode(entry.getReturnCode());
                orderEntry.setErrorText(entry.getErrorText());
            }
            content.append(new ObjectMapper().writeValueAsString(orderEntry));
        }

        try {
            write2file(file, content, newLine);
        } catch (Throwable t) {
            LOGGER.error(String.format("[%s][%s][%s][%s]%s", identifier, entry.getLogType().name(), entry.getOrderKey(), file, t.toString()), t);
            throw t;
        }

        return file;
    }

    private void write2file(Path file, StringBuilder content, boolean newLine) throws Exception {

        if (historyConfiguration.getLogUseLog4j2Writer()) {
            if (newLine) {
                content.append(HistoryUtil.NEW_LINE);
            }
            MDC.put(LOGGER_HISTORY_FILE_WRITER_PARAM_LOGDIR, historyConfiguration.getLogDir());
            MDC.put(LOGGER_HISTORY_FILE_WRITER_PARAM_FILENAME, String.valueOf(file.toFile().getCanonicalPath()));
            LOGGER_HISTORY_FILE_WRITER.info(content.toString());
            MDC.clear();
        } else {
            BufferedWriter writer = null;
            try {
                writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                writer.write(content.toString());
                if (newLine) {
                    writer.write(HistoryUtil.NEW_LINE);
                }
            } catch (Throwable t) {
                throw t;
            } finally {
                if (writer != null) {
                    try {
                        writer.flush();
                    } catch (Exception ex) {
                    }
                    try {
                        writer.close();
                    } catch (Exception ex) {
                    }
                }
            }
        }
    }

    private String hashOrderConstaint(Long eventId, String orderKey, String workflowPosition) {
        return HistoryUtil.hashString(masterConfiguration.getCurrent().getId() + String.valueOf(eventId) + orderKey + workflowPosition);
    }

    private String hashOrderStepConstaint(Long eventId, String orderKey, String workflowPosition) {
        return hashOrderConstaint(eventId, orderKey, workflowPosition);
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
