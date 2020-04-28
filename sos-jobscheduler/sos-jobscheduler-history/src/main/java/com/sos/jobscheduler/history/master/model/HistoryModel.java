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
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
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
import com.sos.webservices.json.jobscheduler.history.order.Error;
import com.sos.webservices.json.jobscheduler.history.order.OrderLogEntry;

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

    private static enum OrderStartCause {
        order, fork, file_trigger, setback, unskip, unstop
    };

    private static enum OrderStepStartCause {
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

        Instant start = Instant.now();
        Duration duration = null;
        Long startEventId = storedEventId;
        Long firstEventId = new Long(0);
        Long lastSuccessEventId = new Long(0);
        int processed = 0;
        int total = event.getStamped().size();

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

                if (processed == 0) {
                    firstEventId = eventId;
                }
                // TODO must be >= instead of > (workaround for: eventId by fork is the same).
                // Changed - Testing
                if (storedEventId >= eventId) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][%s][skip][%s] stored eventId=%s > current eventId=%s %s", identifier, method, entry
                                .getType(), entry.getKey(), storedEventId, eventId, SOSString.toString(entry)));
                    }
                    processed++;
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
                    masterReady(dbLayer, entry);
                    break;
                case AgentReadyFat:
                    agentReady(dbLayer, entry);
                    break;
                case OrderAddedFat:
                    orderAdded(dbLayer, entry);
                    break;
                case OrderForkedFat:
                    orderForked(dbLayer, entry);
                    break;
                case OrderJoinedFat:
                    orderJoined(dbLayer, entry);
                    break;
                case OrderProcessingStartedFat:
                    orderStepStarted(dbLayer, entry);
                    break;
                case OrderStdoutWrittenFat:
                    orderStepStd(dbLayer, entry, OutType.Stdout);
                    break;
                case OrderStderrWrittenFat:
                    orderStepStd(dbLayer, entry, OutType.Stderr);
                    break;
                case OrderProcessedFat:
                    orderStepEnded(dbLayer, entry);
                    break;
                case OrderFailedFat:
                case OrderCancelledFat:
                case OrderFinishedFat:
                    orderEnd(dbLayer, entry);
                    break;
                }
                processed++;
                lastSuccessEventId = eventId;
            }
        } catch (Throwable e) {
            throw new Exception(String.format("[%s][%s][end]%s", identifier, method, e.toString()), e);
        } finally {
            try {
                tryStoreCurrentStateAtEnd(dbLayer, lastSuccessEventId);
            } catch (Exception e1) {
                LOGGER.error(String.format("[%s][%s][end][on_error]error on store lastSuccessEventId=%s: %s", identifier, method, lastSuccessEventId,
                        e1.toString()), e1);
            }
            dbLayer.close();
            cachedOrders = null;
            cachedOrderSteps = null;
            transactionCounter = 0;
            closed = true;

            String startEventIdAsTime = startEventId.equals(new Long(0)) ? "0" : SOSDate.getTime(EventMeta.eventId2Instant(startEventId));
            String endEventIdAsTime = storedEventId.equals(new Long(0)) ? "0" : SOSDate.getTime(EventMeta.eventId2Instant(storedEventId));
            String firstEventIdAsTime = firstEventId.equals(new Long(0)) ? "0" : SOSDate.getTime(EventMeta.eventId2Instant(firstEventId));
            Instant end = Instant.now();
            duration = Duration.between(start, end);
            LOGGER.info(String.format("[%s][%s][%s(%s)-%s][%s(%s)-%s][%s-%s][%s]%s-%s", identifier, lastRestServiceDuration, startEventId,
                    firstEventId, storedEventId, startEventIdAsTime, firstEventIdAsTime, endEventIdAsTime, SOSDate.getTime(start), SOSDate.getTime(
                            end), SOSDate.getDuration(duration), processed, total));
        }

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
        if (transactionCounter % maxTransactions == 0) {
            updateVariable(dbLayer, eventId);
            if (!isMySQL && dbLayer.getSession().isTransactionOpened()) {
                dbLayer.getSession().commit();
                dbLayer.getSession().beginTransaction();
            }
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

    private void masterReady(DBLayerHistory dbLayer, Entry entry) throws Exception {

        try {
            Date eventDate = entry.getEventDate();
            DBItemMaster item = new DBItemMaster();
            item.setJobSchedulerId(masterConfiguration.getCurrent().getId());
            item.setUri(masterConfiguration.getCurrent().getUri());
            item.setTimezone(entry.getTimezone());
            item.setStartTime(eventDate);
            item.setPrimaryMaster(masterConfiguration.getCurrent().isPrimary());
            item.setEventId(String.valueOf(entry.getEventId()));
            item.setCreated(new Date());

            dbLayer.getSession().save(item);

            masterTimezone = item.getTimezone();
            LogEntry le = new LogEntry(LogLevel.Info, OutType.Stdout, LogType.MasterReady, eventDate, null);
            le.onMaster(masterConfiguration);
            storeLog2File(le, entry.getType());

            tryStoreCurrentState(dbLayer, entry.getEventId());
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                LOGGER.error(e.toString(), e);
                throw e;
            }
            LOGGER.warn(String.format("[%s][%s][%s]%s", identifier, entry.getType(), entry.getKey(), e.toString()), e);
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

    private void agentReady(DBLayerHistory dbLayer, Entry entry) throws Exception {

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
            LogEntry le = new LogEntry(LogLevel.Info, OutType.Stdout, LogType.AgentReady, entry.getEventIdAsDate(), entry.getEventDate());
            le.onAgent(ca);
            storeLog2File(le, entry.getType());

            tryStoreCurrentState(dbLayer, entry.getEventId());

            addCachedAgent(item.getPath(), ca);

        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                LOGGER.error(e.toString(), e);
                throw e;
            }
            LOGGER.warn(String.format("[%s][%s][%s]%s", identifier, entry.getType(), entry.getKey(), e.toString()), e);
        }
    }

    private void orderAdded(DBLayerHistory dbLayer, Entry entry) throws Exception {

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

            item.setStartCause(OrderStartCause.order.name());// TODO
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
            LogEntry le = new LogEntry(LogLevel.Debug, OutType.Stdout, LogType.OrderAdded, entry.getEventDate(), null);
            le.onOrder(co, item.getWorkflowPosition());
            storeLog2File(le, entry.getType());

            tryStoreCurrentState(dbLayer, entry.getEventId());

            addCachedOrder(item.getOrderKey(), co);
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                LOGGER.error(e.toString(), e);
                throw e;
            }
            LOGGER.warn(String.format("[%s][%s][%s]%s", identifier, entry.getType(), entry.getKey(), e.toString()), e);
            addCachedOrderByStartEventId(dbLayer, entry.getKey(), String.valueOf(entry.getEventId()));
        }
    }

    private void orderEnd(DBLayerHistory dbLayer, Entry entry) throws Exception {
        orderEnd(dbLayer, LogType.OrderEnd, entry.getType(), entry.getEventId(), entry.getKey(), entry.getEventDate(), entry.getOutcome());
    }

    private CachedOrder orderEnd(DBLayerHistory dbLayer, LogType logType, EventType eventType, Long eventId, String orderKey, Date eventDate,
            Outcome outcome) throws Exception {
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

            LogEntry le = new LogEntry(LogLevel.Info, OutType.Stdout, logType, Date.from(EventMeta.eventId2Instant(eventId)), null);
            le.onOrder(co, co.getWorkflowPosition());

            Path logFile = storeLog2File(le, eventType);
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

        LogEntry le = new LogEntry(LogLevel.Info, OutType.Stdout, LogType.Fork, startTime, null);
        le.onOrder(co, entry.getWorkflowPosition().getOrderPositionAsString(), entry.getChildren());
        storeLog2File(le, entry.getType());

        for (int i = 0; i < entry.getChildren().size(); i++) {
            orderForkedAdded(dbLayer, entry, co, entry.getChildren().get(i), startTime);
        }
    }

    private void orderForkedAdded(DBLayerHistory dbLayer, Entry entry, CachedOrder parentOrder, OrderForkedChild forkOrder, Date startTime)
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

            item.setStartCause(OrderStartCause.fork.name());// TODO
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

            LogEntry le = new LogEntry(LogLevel.Debug, OutType.Stdout, LogType.ForkBranchStarted, startTime, null);
            le.onOrder(co, item.getWorkflowPosition());
            storeLog2File(le, entry.getType());

            tryStoreCurrentState(dbLayer, entry.getEventId());

            addCachedOrder(item.getOrderKey(), co);

        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                LOGGER.error(e.toString(), e);
                throw e;
            }
            LOGGER.warn(String.format("[%s][%s][%s]%s", identifier, entry.getType(), entry.getKey(), e.toString()), e);
            addCachedOrderByStartEventId(dbLayer, forkOrder.getOrderId(), String.valueOf(entry.getEventId()));
        }
    }

    private void orderJoined(DBLayerHistory dbLayer, Entry entry) throws Exception {
        checkMasterTimezone(dbLayer);

        CachedOrder co = getCachedOrder(dbLayer, entry.getKey());
        Date endTime = entry.getEventDate();

        CachedOrder fco = null;
        for (int i = 0; i < entry.getChildOrderIds().size(); i++) {
            fco = orderEnd(dbLayer, LogType.ForkBranchEnd, entry.getType(), entry.getEventId(), entry.getChildOrderIds().get(i), endTime, entry
                    .getOutcome());
        }

        LogEntry le = new LogEntry(LogLevel.Info, OutType.Stdout, LogType.ForkJoin, entry.getEventIdAsDate(), null);
        le.onOrderJoined(co, fco.getWorkflowPosition(), entry.getChildOrderIds());
        storeLog2File(le, entry.getType());
    }

    private void orderStepStarted(DBLayerHistory dbLayer, Entry entry) throws Exception {
        CachedOrder co = null;
        CachedOrderStep cos = null;
        Date agentStartTime = entry.getEventDate();
        DBItemOrderStep item = null;
        CachedAgent ca = null;
        try {
            checkMasterTimezone(dbLayer);

            co = getCachedOrder(dbLayer, entry.getKey());
            ca = getCachedAgent(dbLayer, entry.getAgentRefPath());
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
            item.setCriticality(DBItemOrderStep.Criticality.normal.name());// TODO

            item.setAgentPath(entry.getAgentRefPath());
            item.setAgentUri(ca.getUri());

            item.setStartCause(OrderStepStartCause.order.name());// TODO
            item.setStartTime(agentStartTime);
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
                LogEntry le = new LogEntry(LogLevel.Info, OutType.Stdout, LogType.OrderStarted, entry.getEventIdAsDate(), agentStartTime);
                le.onOrder(co, item.getWorkflowPosition());
                le.setAgentTimezone(ca.getTimezone());
                storeLog2File(le, LogType.OrderStarted.name());

            } else {
                dbLayer.updateOrderOnOrderStep(co.getId(), co.getCurrentOrderStepId(), new Date());
            }
            // addCachedOrder(co.getOrderKey(), co);
            cos = new CachedOrderStep(item, ca.getTimezone());
            addCachedOrderStep(item.getOrderKey(), cos);

            LogEntry le = new LogEntry(LogLevel.Info, OutType.Stdout, LogType.OrderStepStart, entry.getEventIdAsDate(), agentStartTime);
            le.onOrderStep(cos, ca.getTimezone());
            storeLog2File(le, entry.getType());

            tryStoreCurrentState(dbLayer, entry.getEventId());
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                LOGGER.error(e.toString(), e);
                throw e;
            }
            LOGGER.warn(String.format("[%s][%s][%s]%s", identifier, entry.getType(), entry.getKey(), e.toString()), e);
            if (co != null) {
                addCachedOrder(co.getOrderKey(), co);
            }
            addCachedOrderStepByStartEventId(dbLayer, ca, entry.getKey(), String.valueOf(entry.getEventId()));
        }
    }

    private void orderStepEnded(DBLayerHistory dbLayer, Entry entry) throws Exception {
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

            LogEntry le = new LogEntry(LogLevel.Info, OutType.Stdout, LogType.OrderStepEnd, entry.getEventIdAsDate(), endTime);
            le.onOrderStep(cos);

            DBItemLog logItem = storeLogFile2Db(dbLayer, cos.getMainOrderId(), cos.getOrderId(), cos.getId(), true, storeLog2File(le, entry
                    .getType()));
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
            LogEntry le = new LogEntry(LogLevel.Info, outType, LogType.OrderStepOut, entry.getEventIdAsDate(), entry.getEventDate());
            le.onOrderStep(cos, entry.getChunk());
            storeLog2File(le, entry.getType().name(), cos);

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
            List<DBItemOrder> items = dbLayer.getOrder(masterConfiguration.getCurrent().getId(), key);
            DBItemOrder item = getOrder(items, null);
            if (item == null) {

                if (items == null || items.size() == 0) {
                    throw new Exception(String.format("[%s][%s]order not found", identifier, key));
                } else {
                    LOGGER.info(String.format("[%s][%s]%s orders found:", identifier, key, items.size()));
                    for (int i = 0; i < items.size(); i++) {
                        LOGGER.info(String.format("%s) %s", i, SOSHibernate.toString(items.get(i))));
                    }
                    throw new Exception(String.format("[%s][%s]order not found", identifier, key));
                }
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
        List<DBItemOrder> items = dbLayer.getOrder(masterConfiguration.getCurrent().getId(), key);
        DBItemOrder item = getOrder(items, startEventId);
        if (item == null) {
            if (items == null || items.size() == 0) {
                throw new Exception(String.format("[%s][%s]order not found", identifier, key));
            } else {
                LOGGER.info(String.format("[%s][%s]%s orders found:", identifier, key, items.size()));
                for (int i = 0; i < items.size(); i++) {
                    LOGGER.info(String.format("%s) %s", i, SOSHibernate.toString(items.get(i))));
                }
                throw new Exception(String.format("[%s][%s]order with startEventId=%s not found", identifier, key, startEventId));
            }
        } else {
            addCachedOrder(key, new CachedOrder(item));
        }
    }

    private DBItemOrder getOrder(List<DBItemOrder> items, String startEventId) {
        if (items != null) {
            switch (items.size()) {
            case 0:
                return null;
            case 1:
                return items.get(0);
            default:
                DBItemOrder order = null;
                if (startEventId == null) {
                    Long eventId = new Long(0);
                    for (DBItemOrder item : items) {
                        Long itemEventId = Long.parseLong(item.getStartEventId());
                        if (itemEventId > eventId) {
                            order = item;
                            eventId = itemEventId;
                        }
                    }
                } else {
                    for (DBItemOrder item : items) {
                        if (item.getStartEventId().equals(startEventId)) {
                            order = item;
                            break;
                        }
                    }
                }
                return order;
            }
        }
        return null;
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
                DBItemAgent agent = dbLayer.getAgent(masterConfiguration.getCurrent().getId(), item.getAgentPath());
                if (agent == null) {
                    LOGGER.warn(String.format("[%s][agent is null]agent timezone can't be identified. set agent log timezone to master timezone ...",
                            item.getAgentPath()));
                    co = new CachedOrderStep(item, masterTimezone);
                } else {
                    co = new CachedOrderStep(item, agent.getTimezone());
                }
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

    private void addCachedOrderStepByStartEventId(DBLayerHistory dbLayer, CachedAgent agent, String key, String startEventId) throws Exception {
        DBItemOrderStep item = dbLayer.getOrderStep(masterConfiguration.getCurrent().getId(), key, startEventId);
        if (item == null) {
            throw new Exception(String.format("[%s]order step not found. orderKey=%s, startEventId=%s", identifier, key, startEventId));
        } else {
            if (agent == null) {
                LOGGER.warn(String.format("[%s][agent not found]agent timezone can't be identified. set agent log timezone to master timezone ...",
                        item.getAgentPath()));
                addCachedOrderStep(key, new CachedOrderStep(item, masterTimezone));
            } else {
                addCachedOrderStep(key, new CachedOrderStep(item, agent.getTimezone()));
            }
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

    private Path storeLog2File(LogEntry entry, EventType eventType) throws Exception {
        return storeLog2File(entry, eventType.name(), null);
    }

    private Path storeLog2File(LogEntry entry, String eventType) throws Exception {
        return storeLog2File(entry, eventType, null);
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

    private OrderLogEntry createOrderLogEntry(LogEntry logEntry, String eventType) {
        OrderLogEntry entry = new OrderLogEntry();
        entry.setOrderId(logEntry.getOrderKey());
        entry.setLogLevel(logEntry.getLogLevel().name().toUpperCase());
        try {
            entry.setLogEvent(logEntry.toEventType(eventType));
        } catch (Exception e) {
            LOGGER.error(String.format("illegal event type: %s", e.toString()), e);
            LOGGER.error(String.format("[%s][illegal event type]%s", eventType, e.toString()));
        }
        entry.setPosition(logEntry.getPosition());
        if (logEntry.isError()) {
            Error error = new Error();
            error.setErrorStatus(logEntry.getErrorState());
            error.setErrorReason(logEntry.getErrorReason());
            error.setErrorCode(logEntry.getErrorCode());
            error.setErrorText(logEntry.getErrorText());
            entry.setError(error);
        }
        return entry;
    }

    private String getDateAsString(Date date, String timeZone) throws Exception {
        return SOSDate.getDateAsString(date, "yyyy-MM-dd HH:mm:ss.SSSZZZZ", TimeZone.getTimeZone(timeZone));
    }

    private Path storeLog2File(LogEntry entry, String eventType, CachedOrderStep cos) throws Exception {

        Path file = null;
        StringBuilder content = new StringBuilder();
        boolean newLine = true;

        switch (entry.getLogType()) {
        case OrderStepStart:
        case OrderStepEnd:
            OrderLogEntry orderEntry = createOrderLogEntry(entry, eventType);
            orderEntry.setMasterDatetime(getDateAsString(entry.getMasterDatetime(), masterTimezone));
            orderEntry.setAgentDatetime(getDateAsString(entry.getAgentDatetime(), entry.getAgentTimezone()));
            orderEntry.setAgentPath(entry.getAgentPath());
            orderEntry.setAgentUrl(entry.getAgentUri());
            orderEntry.setJob(entry.getJobName());
            orderEntry.setTaskId(entry.getOrderStepId());
            // order log
            write2file(getOrderLog(entry), new StringBuilder(new ObjectMapper().writeValueAsString(orderEntry)), newLine);

            // order step log - meta infos
            file = getOrderStepLog(entry);
            content.append(getDateAsString(entry.getAgentDatetime(), entry.getAgentTimezone())).append(" ");
            content.append("[").append(entry.getOutType().name().toUpperCase()).append("]");
            content.append("[").append(entry.getLogLevel().name().toUpperCase()).append("]");
            content.append(entry.getChunk());

            break;

        case OrderStepOut:
            // order step log - stdout|stderr
            file = getOrderStepLog(entry);
            if (cos.getLastStdHasNewLine() == null || cos.getLastStdHasNewLine()) {
                content.append(getDateAsString(entry.getAgentDatetime(), entry.getAgentTimezone())).append(" ");
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
            hm.put("masterDatetime", getDateAsString(entry.getMasterDatetime(), masterTimezone));
            if (entry.getAgentDatetime() != null && entry.getAgentTimezone() != null) {
                hm.put("agentDatetime", getDateAsString(entry.getAgentDatetime(), entry.getAgentTimezone()));
            }
            hm.put("logLevel", entry.getLogLevel().name().toUpperCase());
            hm.put("logEvent", entry.getLogType().name());

            if (entry.isError()) {
                hm.put("error", "1");
                hm.put("errorState", entry.getErrorState());
                hm.put("errorReason", entry.getErrorReason());
                hm.put("errorCode", entry.getErrorCode());
                hm.put("errorReturnCode", entry.getReturnCode() == null ? "" : String.valueOf(entry.getReturnCode()));
                hm.put("errorText", entry.getErrorText());
            }
            content.append(hm);
            break;
        default:
            // ORDER LOG
            file = getOrderLog(entry);
            orderEntry = createOrderLogEntry(entry, eventType);
            orderEntry.setMasterDatetime(getDateAsString(entry.getMasterDatetime(), masterTimezone));
            if (entry.getAgentDatetime() != null && entry.getAgentTimezone() != null) {
                orderEntry.setAgentDatetime(getDateAsString(entry.getAgentDatetime(), entry.getAgentTimezone()));
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
        return HistoryUtil.hashString(new StringBuilder(masterConfiguration.getCurrent().getId()).append(String.valueOf(eventId)).append(orderKey)
                .append(workflowPosition).toString());
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
