package com.sos.jobscheduler.history.master.model;

import java.io.BufferedWriter;
import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateFactory.Dbms;
import com.sos.commons.hibernate.exception.SOSHibernateObjectOperationException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSShell;
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
import com.sos.jobscheduler.event.master.fatevent.bean.Entry;
import com.sos.jobscheduler.event.master.fatevent.bean.OrderForkedChild;
import com.sos.jobscheduler.event.master.fatevent.bean.Outcome;
import com.sos.jobscheduler.event.master.handler.http.HttpClient;
import com.sos.jobscheduler.event.master.handler.http.RestServiceDuration;
import com.sos.jobscheduler.history.db.DBLayerHistory;
import com.sos.jobscheduler.history.helper.CachedAgent;
import com.sos.jobscheduler.history.helper.CachedOrder;
import com.sos.jobscheduler.history.helper.CachedOrderStep;
import com.sos.jobscheduler.history.helper.HistoryUtil;
import com.sos.jobscheduler.history.helper.LogEntry;
import com.sos.jobscheduler.history.master.configuration.HistoryConfiguration;
import com.sos.jobscheduler.model.event.EventType;
import com.sos.webservices.json.jobscheduler.history.order.Error;
import com.sos.webservices.json.jobscheduler.history.order.OrderLogEntry;

public class HistoryModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryModel.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();

    private static final Logger LOGGER_DIAGNOSTIC = LoggerFactory.getLogger("HistoryDiagnostic");

    private static final long MAX_LOCK_VERSION = 10_000_000;
    private final SOSHibernateFactory dbFactory;
    private HistoryConfiguration historyConfiguration;
    private MasterConfiguration masterConfiguration;
    private HttpClient httpClient;
    private String identifier;
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

    public HistoryModel(SOSHibernateFactory factory, HistoryConfiguration historyConf, MasterConfiguration masterConf) {
        dbFactory = factory;
        isMySQL = dbFactory.getDbms().equals(Dbms.MYSQL);
        historyConfiguration = historyConf;
        masterConfiguration = masterConf;
        variable = "history_" + masterConfiguration.getCurrent().getJobSchedulerId();
        maxTransactions = historyConfiguration.getMaxTransactions();
        initCache();
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

        closed = false;
        transactionCounter = 0;
        DBLayerHistory dbLayer = null;
        Duration duration = null;
        Map<String, CachedOrderStep> endedOrderSteps = new HashMap<>();
        Instant start = Instant.now();
        Long startEventId = storedEventId;
        Long firstEventId = new Long(0L);
        Long lastSuccessEventId = new Long(0L);
        int total = event.getStamped().size();
        int processed = 0;

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s][start][%s][%s]%s total", identifier, method, storedEventId, start, total));
        }

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
                if (storedEventId >= eventId) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][%s][skip][%s] stored eventId=%s > current eventId=%s %s", identifier, method, entry
                                .getType(), entry.getKey(), storedEventId, eventId, SOSString.toString(entry)));
                    }
                    processed++;
                    continue;
                }

                if (isDebugEnabled) {
                    LOGGER.debug("--- " + entry.getType() + " ------------------------------------------");
                    if (isTraceEnabled) {
                        LOGGER.trace(String.format("[%s][%s][%s]%s", identifier, method, entry.getType(), SOSString.toString(entry)));
                    }
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
                    orderJoined(dbLayer, entry, endedOrderSteps);
                    break;
                case OrderProcessingStartedFat:
                    orderStepStarted(dbLayer, entry);
                    break;
                case OrderStdoutWrittenFat:
                    orderStepStd(dbLayer, entry, EventType.ORDER_STDOUT_WRITTEN);
                    break;
                case OrderStderrWrittenFat:
                    orderStepStd(dbLayer, entry, EventType.ORDER_STDERR_WRITTEN);
                    break;
                case OrderProcessedFat:
                    orderStepEnded(dbLayer, entry, endedOrderSteps);
                    break;
                case OrderFailedFat:
                    orderUpdate(dbLayer, entry, EventType.ORDER_FAILED, endedOrderSteps);
                    break;
                case OrderCancelledFat:
                    orderEnded(dbLayer, entry, EventType.ORDER_CANCELLED, endedOrderSteps);
                    break;
                case OrderFinishedFat:
                    orderEnded(dbLayer, entry, EventType.ORDER_FINISHED, endedOrderSteps);
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
            duration = showSummary(lastRestServiceDuration, startEventId, firstEventId, start, total, processed);
            transactionCounter = 0L;
            closed = true;
        }

        doDiagnostic("onHistory", duration, historyConfiguration.getDiagnosticStartIfHistoryExecutionLongerThan());

        return storedEventId;
    }

    private Duration showSummary(RestServiceDuration lastRestServiceDuration, Long startEventId, Long firstEventId, Instant start, int total,
            int processed) {
        String startEventIdAsTime = startEventId.equals(new Long(0L)) ? "0" : SOSDate.getTime(EventMeta.eventId2Instant(startEventId));
        String endEventIdAsTime = storedEventId.equals(new Long(0L)) ? "0" : SOSDate.getTime(EventMeta.eventId2Instant(storedEventId));
        String firstEventIdAsTime = firstEventId.equals(new Long(0L)) ? "0" : SOSDate.getTime(EventMeta.eventId2Instant(firstEventId));
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        LOGGER.info(String.format("[%s][%s][%s(%s)-%s][%s(%s)-%s][%s-%s][%s]%s-%s", identifier, lastRestServiceDuration, startEventId, firstEventId,
                storedEventId, startEventIdAsTime, firstEventIdAsTime, endEventIdAsTime, SOSDate.getTime(start), SOSDate.getTime(end), SOSDate
                        .getDuration(duration), processed, total));
        showCachedSummary();
        return duration;
    }

    private void showCachedSummary() {
        // TODO remove cached items - dependent of the created time
        int coSize = cachedOrders.size();
        int cosSize = cachedOrderSteps.size();
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[cachedAgents=%s][cachedOrders=%s][cachedOrderSteps=%s]", cachedAgents.size(), coSize, cosSize));
            LOGGER.debug(SOSString.mapToString(cachedAgents, true));
        }
        if (coSize >= 1_000) {
            LOGGER.warn(SOSString.mapToString(cachedOrders, true));
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(SOSString.mapToString(cachedOrders, true));
            }
        }
        if (cosSize >= 1_000) {
            LOGGER.warn(SOSString.mapToString(cachedOrderSteps, true));
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(SOSString.mapToString(cachedOrderSteps, true));
            }
        }
    }

    private void initCache() {
        cachedOrders = new HashMap<>();
        cachedOrderSteps = new HashMap<>();
        cachedAgents = new HashMap<>();
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
                    String identifier = Thread.currentThread().getName() + "-" + masterConfiguration.getCurrent().getJobSchedulerId();
                    LOGGER_DIAGNOSTIC.info(String.format("[%s]duration=%s", range, SOSDate.getDuration(duration)));
                    SOSShell.printCpuLoad(LOGGER_DIAGNOSTIC);
                    if (!SOSString.isEmpty(historyConfiguration.getDiagnosticAdditionalScript())) {
                        SOSShell.executeCommand(historyConfiguration.getDiagnosticAdditionalScript(), LOGGER_DIAGNOSTIC);
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
                        // String identifier = Thread.currentThread().getName() + "-" + masterConfiguration.getCurrent().getJobSchedulerId();
                        LOGGER_DIAGNOSTIC.info(String.format("[%s]duration=%s", range, SOSDate.getDuration(duration)));
                        SOSShell.printCpuLoad(LOGGER_DIAGNOSTIC);
                        if (!SOSString.isEmpty(historyConfiguration.getDiagnosticAdditionalScript())) {
                            SOSShell.executeCommand(historyConfiguration.getDiagnosticAdditionalScript(), LOGGER_DIAGNOSTIC);
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
        DBItemMaster item = new DBItemMaster();
        try {
            Date eventDate = entry.getEventDate();
            item.setJobSchedulerId(masterConfiguration.getCurrent().getJobSchedulerId());
            item.setUri(masterConfiguration.getCurrent().getUri());
            item.setTimezone(entry.getTimezone());
            item.setStartTime(eventDate);
            item.setPrimaryMaster(masterConfiguration.getCurrent().isPrimary());
            item.setEventId(String.valueOf(entry.getEventId()));
            item.setCreated(new Date());

            dbLayer.getSession().save(item);

            masterTimezone = item.getTimezone();
            LogEntry le = new LogEntry(LogEntry.LogLevel.Info, EventType.MASTER_READY, eventDate, null);
            le.onMaster(masterConfiguration);
            storeLog2File(le);

            tryStoreCurrentState(dbLayer, entry.getEventId());
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                LOGGER.error(e.toString(), e);
                throw e;
            }
            LOGGER.warn(String.format("[%s][%s][%s]%s", identifier, entry.getType(), entry.getKey(), e.toString()), e);
            LOGGER.warn(String.format("[%s][ConstraintViolation item]%s", identifier, SOSHibernate.toString(item)));
        } finally {
            if (masterTimezone == null) {
                masterTimezone = entry.getTimezone();
            }
        }
    }

    private void checkMasterTimezone(DBLayerHistory dbLayer) throws Exception {
        if (masterTimezone == null) {
            masterTimezone = dbLayer.getMasterTimezone(masterConfiguration.getCurrent().getJobSchedulerId());
            if (masterTimezone == null) {
                throw new Exception(String.format("master not founded: %s", masterConfiguration.getCurrent().getJobSchedulerId()));
            }
        }
    }

    private void agentReady(DBLayerHistory dbLayer, Entry entry) throws Exception {
        DBItemAgent item = new DBItemAgent();
        CachedAgent ca = null;
        try {
            checkMasterTimezone(dbLayer);

            try {
                ca = getCachedAgent(dbLayer, entry.getKey());
            } catch (Exception ex) {
            }

            item.setJobSchedulerId(masterConfiguration.getCurrent().getJobSchedulerId());
            item.setPath(entry.getKey());
            item.setUri(ca == null ? "." : ca.getUri());// TODO
            item.setTimezone(entry.getTimezone());
            item.setStartTime(entry.getEventDate());
            item.setEventId(String.valueOf(entry.getEventId()));
            item.setCreated(new Date());

            dbLayer.getSession().save(item);

            ca = new CachedAgent(item);
            LogEntry le = new LogEntry(LogEntry.LogLevel.Info, EventType.AGENT_READY, entry.getEventIdAsDate(), entry.getEventDate());
            le.onAgent(ca);
            storeLog2File(le);
            addCachedAgent(item.getPath(), ca);

            tryStoreCurrentState(dbLayer, entry.getEventId());
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                LOGGER.error(e.toString(), e);
                throw e;
            }
            LOGGER.warn(String.format("[%s][%s][%s]%s", identifier, entry.getType(), entry.getKey(), e.toString()), e);
            LOGGER.warn(String.format("[%s][ConstraintViolation item]%s", identifier, SOSHibernate.toString(item)));
        }
    }

    private void orderAdded(DBLayerHistory dbLayer, Entry entry) throws Exception {
        DBItemOrder item = new DBItemOrder();
        try {
            checkMasterTimezone(dbLayer);

            item.setJobSchedulerId(masterConfiguration.getCurrent().getJobSchedulerId());
            item.setOrderKey(entry.getKey());

            item.setWorkflowPath(entry.getWorkflowPosition().getWorkflowId().getPath());
            item.setWorkflowVersionId(entry.getWorkflowPosition().getWorkflowId().getVersionId());
            item.setWorkflowPosition(entry.getWorkflowPosition().getOrderPositionAsString());
            item.setWorkflowFolder(HistoryUtil.getFolderFromPath(item.getWorkflowPath()));
            item.setWorkflowName(HistoryUtil.getBasenameFromPath(item.getWorkflowPath()));
            item.setWorkflowTitle(null);// TODO

            item.setMainParentId(new Long(0L));// TODO see below item.setParentId(new Long(0));
            item.setParentId(new Long(0L));
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
            LogEntry le = new LogEntry(LogEntry.LogLevel.Debug, EventType.ORDER_ADDED, entry.getEventDate(), null);
            le.onOrder(co, item.getWorkflowPosition());
            storeLog2File(le);
            addCachedOrder(item.getOrderKey(), co);

            tryStoreCurrentState(dbLayer, entry.getEventId());
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                LOGGER.error(e.toString(), e);
                throw e;
            }
            LOGGER.warn(String.format("[%s][%s][%s]%s", identifier, entry.getType(), entry.getKey(), e.toString()), e);
            LOGGER.warn(String.format("[%s][ConstraintViolation item]%s", identifier, SOSHibernate.toString(item)));
            addCachedOrderByStartEventId(dbLayer, entry.getKey(), String.valueOf(entry.getEventId()));
        }
    }

    private void orderEnded(DBLayerHistory dbLayer, Entry entry, EventType eventType, Map<String, CachedOrderStep> endedOrderSteps) throws Exception {
        orderUpdate(dbLayer, eventType, entry.getEventId(), entry.getKey(), entry.getEventDate(), entry.getOutcome(), endedOrderSteps, true);
    }

    private void orderUpdate(DBLayerHistory dbLayer, Entry entry, EventType eventType, Map<String, CachedOrderStep> endedOrderSteps)
            throws Exception {
        orderUpdate(dbLayer, eventType, entry.getEventId(), entry.getKey(), entry.getEventDate(), entry.getOutcome(), endedOrderSteps, false);
    }

    private CachedOrder orderUpdate(DBLayerHistory dbLayer, EventType eventType, Long eventId, String orderKey, Date eventDate, Outcome outcome,
            Map<String, CachedOrderStep> endedOrderSteps, boolean completeOrder) throws Exception {
        CachedOrder co = getCachedOrder(dbLayer, orderKey);
        if (co.getEndTime() == null) {
            checkMasterTimezone(dbLayer);

            CachedOrderStep cos = getCurrentOrderStep(dbLayer, co, endedOrderSteps);
            LogEntry le = createOrderLogEntry(eventId, outcome, cos, eventType);
            String state = null;

            switch (eventType) {
            case ORDER_FAILED:
                state = OrderState.failed.name();
                break;
            case ORDER_CANCELLED:
                state = OrderState.cancelled.name();
                break;
            default:
                state = le.isError() ? OrderState.failed.name() : OrderState.finished.name();
                break;
            }

            Date endTime = null;
            String endWorkflowPosition = null;
            Long endOrderStepId = null;
            String endEventId = null;
            if (completeOrder) {
                endTime = eventDate;
                endWorkflowPosition = (cos == null) ? co.getWorkflowPosition() : cos.getWorkflowPosition();
                endOrderStepId = (cos == null) ? co.getCurrentOrderStepId() : cos.getId();
                endEventId = String.valueOf(eventId);
            }
            dbLayer.setOrderEnd(co.getId(), endTime, endWorkflowPosition, endOrderStepId, endEventId, state, eventDate, le.isError(), le
                    .getErrorState(), le.getErrorReason(), le.getReturnCode(), le.getErrorCode(), le.getErrorText(), new Date());
            le.onOrder(co, co.getWorkflowPosition());
            Path logFile = storeLog2File(le);
            if (completeOrder && co.getParentId().longValue() == 0L) {
                DBItemLog logItem = storeLogFile2Db(dbLayer, co.getMainParentId(), co.getId(), new Long(0L), false, logFile);
                if (logItem != null)
                    dbLayer.setOrderLogId(co.getId(), logItem.getId());
            }
            tryStoreCurrentState(dbLayer, eventId);
            if (completeOrder) {
                clearCache(CacheType.order, orderKey);
            }
            if (completeOrder && co.getParentId().longValue() == 0L) {
                send2Executor("order_id=" + co.getId());
            }

        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][skip][%s]order is already completed[%s]", identifier, eventType, orderKey, SOSString.toString(
                        co)));
            }
            clearCache(CacheType.order, co.getOrderKey());
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

    private CachedOrderStep getCurrentOrderStep(DBLayerHistory dbLayer, CachedOrder co, Map<String, CachedOrderStep> endedOrderSteps)
            throws Exception {
        CachedOrderStep step = null;
        if (co.getCurrentOrderStepId().longValue() > 0L) {// forked = 0
            step = endedOrderSteps.get(co.getOrderKey());
            if (step == null || !step.getId().equals(co.getCurrentOrderStepId())) {
                if (step != null) {
                    if (isDebugEnabled)
                        LOGGER.debug(String.format("[%s][%s][currentStep id mismatch]orderCurrentStepId=%s != cachedStepId=%s", identifier, co
                                .getOrderKey(), co.getCurrentOrderStepId(), step.getId()));
                    step = null;
                }
                DBItemOrderStep item = dbLayer.getOrderStep(co.getCurrentOrderStepId());
                if (item == null) {
                    LOGGER.warn(String.format("[%s][%s][currentStep not found]id=%s", identifier, co.getOrderKey(), co.getCurrentOrderStepId()));
                } else {
                    CachedAgent ca = getCachedAgent(item.getAgentPath());
                    step = new CachedOrderStep(item, ca.getTimezone());
                    if (item.getError())
                        step.setError(item.getErrorState(), item.getErrorReason(), item.getErrorCode(), item.getErrorText());
                }
            } else if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][currentStep found]%s", identifier, co.getOrderKey(), SOSString.toString(step)));
            }
        }
        return step;
    }

    private LogEntry createOrderLogEntry(Long eventId, Outcome outcome, CachedOrderStep cos, EventType eventType) {
        LogEntry le = new LogEntry(LogEntry.LogLevel.Info, eventType, Date.from(EventMeta.eventId2Instant(eventId)), null);
        boolean stepHasError = (cos != null && cos.getError() != null);
        if (outcome != null) {
            le.setReturnCode(outcome.getReturnCode());
            if (outcome.getType().equalsIgnoreCase(OrderErrorType.failed.name()) || outcome.getType().equalsIgnoreCase(OrderErrorType.disrupted
                    .name())) {
                boolean setError = true;
                if (eventType.equals(EventType.ORDER_JOINED))
                    if (stepHasError) {
                        if (le.getReturnCode() != null && le.getReturnCode().equals(Long.valueOf(0L)))
                            le.setReturnCode(cos.getReturnCode());
                    } else {
                        setError = false;
                    }
                if (setError) {
                    String errorReason = null;
                    String errorText = null;
                    if (outcome.getReason() != null) {
                        errorReason = outcome.getReason().getType();
                        errorText = outcome.getReason().getProblem().getMessage();
                    }
                    le.setError(outcome.getType().toLowerCase(), errorReason, errorText);
                }
            }
        }
        if (!le.isError() && stepHasError) {
            le.setReturnCode(cos.getReturnCode());
            le.setError(OrderState.failed.name(), null, cos.getError().getText());
        }
        return le;
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

        LogEntry le = new LogEntry(LogEntry.LogLevel.Info, EventType.ORDER_FORKED, startTime, null);
        le.onOrder(co, entry.getWorkflowPosition().getOrderPositionAsString(), entry.getChildren());
        storeLog2File(le);

        for (int i = 0; i < entry.getChildren().size(); i++) {
            orderForkedStarted(dbLayer, entry, co, entry.getChildren().get(i), startTime);
        }
    }

    private void orderForkedStarted(DBLayerHistory dbLayer, Entry entry, CachedOrder parentOrder, OrderForkedChild forkOrder, Date startTime)
            throws Exception {

        DBItemOrder item = new DBItemOrder();

        try {
            checkMasterTimezone(dbLayer);

            item.setJobSchedulerId(masterConfiguration.getCurrent().getJobSchedulerId());
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

            item.setCurrentOrderStepId(new Long(0L));

            item.setEndTime(null);
            item.setEndWorkflowPosition(null);
            item.setEndOrderStepId(new Long(0L));

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

            item.setLogId(new Long(0L));

            item.setConstraintHash(hashOrderConstaint(entry.getEventId(), item.getOrderKey(), item.getWorkflowPosition()));
            item.setCreated(new Date());
            item.setModified(item.getCreated());

            dbLayer.getSession().save(item);

            CachedOrder co = new CachedOrder(item);
            LogEntry le = new LogEntry(LogEntry.LogLevel.Info, EventType.ORDER_STARTED, startTime, null);
            le.onOrder(co, item.getWorkflowPosition());
            storeLog2File(le);
            addCachedOrder(item.getOrderKey(), co);

            tryStoreCurrentState(dbLayer, entry.getEventId());
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                LOGGER.error(e.toString(), e);
                throw e;
            }
            LOGGER.warn(String.format("[%s][%s][%s]%s", identifier, entry.getType(), entry.getKey(), e.toString()), e);
            LOGGER.warn(String.format("[%s][ConstraintViolation item]%s", identifier, SOSHibernate.toString(item)));
            addCachedOrderByStartEventId(dbLayer, forkOrder.getOrderId(), String.valueOf(entry.getEventId()));
        }
    }

    private void orderJoined(DBLayerHistory dbLayer, Entry entry, Map<String, CachedOrderStep> endedOrderSteps) throws Exception {
        checkMasterTimezone(dbLayer);

        Date endTime = entry.getEventDate();
        CachedOrder fco = null;
        for (int i = 0; i < entry.getChildOrderIds().size(); i++) {
            fco = orderUpdate(dbLayer, EventType.ORDER_JOINED, entry.getEventId(), entry.getChildOrderIds().get(i), endTime, entry.getOutcome(),
                    endedOrderSteps, true);
        }
        LogEntry le = new LogEntry(LogEntry.LogLevel.Info, EventType.ORDER_JOINED, entry.getEventIdAsDate(), null);
        CachedOrder co = getCachedOrder(dbLayer, entry.getKey());
        le.onOrderJoined(co, fco.getWorkflowPosition(), entry.getChildOrderIds(), entry.getOutcome());
        storeLog2File(le);
    }

    private void orderStepStarted(DBLayerHistory dbLayer, Entry entry) throws Exception {
        CachedAgent ca = null;
        CachedOrder co = null;
        CachedOrderStep cos = null;
        DBItemOrderStep item = null;
        try {
            checkMasterTimezone(dbLayer);

            ca = getCachedAgent(dbLayer, entry.getAgentRefPath());
            co = getCachedOrder(dbLayer, entry.getKey());
            if (!ca.getUri().equals(entry.getAgentUri())) {// TODO
                ca.setUri(entry.getAgentUri());
                dbLayer.updateAgent(ca.getId(), ca.getUri());
            }
            Date agentStartTime = entry.getEventDate();

            item = new DBItemOrderStep();
            item.setJobSchedulerId(masterConfiguration.getCurrent().getJobSchedulerId());
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

                LogEntry logEntry = new LogEntry(LogEntry.LogLevel.Info, EventType.ORDER_STARTED, entry.getEventIdAsDate(), agentStartTime);
                logEntry.onOrder(co, item.getWorkflowPosition());
                logEntry.setAgentTimezone(ca.getTimezone());
                storeLog2File(logEntry);
            } else {
                dbLayer.updateOrderOnOrderStep(co.getId(), co.getCurrentOrderStepId(), new Date());
            }
            cos = new CachedOrderStep(item, ca.getTimezone());
            LogEntry le = new LogEntry(LogEntry.LogLevel.Info, EventType.ORDER_PROCESSING_STARTED, entry.getEventIdAsDate(), agentStartTime);
            le.onOrderStep(cos, ca.getTimezone());
            storeLog2File(le);
            addCachedOrderStep(item.getOrderKey(), cos);

            tryStoreCurrentState(dbLayer, entry.getEventId());
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                LOGGER.error(e.toString(), e);
                throw e;
            }
            LOGGER.warn(String.format("[%s][%s][%s]%s", identifier, entry.getType(), entry.getKey(), e.toString()), e);
            LOGGER.warn(String.format("[%s][ConstraintViolation item]%s", identifier, SOSHibernate.toString(item)));

            if (co != null) {
                addCachedOrder(co.getOrderKey(), co);
            }
            addCachedOrderStepByStartEventId(dbLayer, ca, entry.getKey(), String.valueOf(entry.getEventId()));
        }
    }

    private void orderStepEnded(DBLayerHistory dbLayer, Entry entry, Map<String, CachedOrderStep> endedOrderSteps) throws Exception {
        CachedOrderStep cos = getCachedOrderStep(dbLayer, entry.getKey());
        if (cos.getEndTime() == null) {
            checkMasterTimezone(dbLayer);
            cos.setEndTime(entry.getEventDate());

            LogEntry le = new LogEntry(LogEntry.LogLevel.Info, EventType.ORDER_PROCESSED, entry.getEventIdAsDate(), cos.getEndTime());
            if (entry.getOutcome() != null) {
                cos.setReturnCode(entry.getOutcome().getReturnCode());
                le.setReturnCode(cos.getReturnCode());
                if (entry.getOutcome().getType().equalsIgnoreCase(OrderStepErrorType.failed.name()) || entry.getOutcome().getType().equalsIgnoreCase(
                        OrderStepErrorType.disrupted.name())) {
                    String errorReason = null;
                    String errorText = null;
                    if (entry.getOutcome().getReason() != null) {
                        errorReason = entry.getOutcome().getReason().getType();
                        errorText = entry.getOutcome().getReason().getProblem().getMessage();
                    }
                    le.setError(entry.getOutcome().getType().toLowerCase(), errorReason, errorText);
                }
            }
            dbLayer.setOrderStepEnd(cos.getId(), cos.getEndTime(), String.valueOf(entry.getEventId()), EventMeta.map2Json(entry.getKeyValues()), le
                    .getReturnCode(), OrderStepState.processed.name(), le.isError(), le.getErrorState(), le.getErrorReason(), le.getErrorCode(), le
                            .getErrorText(), new Date());
            le.onOrderStep(cos);

            DBItemLog logItem = storeLogFile2Db(dbLayer, cos.getMainOrderId(), cos.getOrderId(), cos.getId(), true, storeLog2File(le));
            if (logItem != null) {
                dbLayer.setOrderStepLogId(cos.getId(), logItem.getId());
            }
            endedOrderSteps.put(entry.getKey(), cos);

            tryStoreCurrentState(dbLayer, entry.getEventId());
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][skip][%s]order step is already ended[%s]", identifier, entry.getType(), entry.getKey(), SOSString
                        .toString(cos)));
            }
        }
        clearCache(CacheType.orderStep, cos.getOrderKey());
    }

    private void orderStepStd(DBLayerHistory dbLayer, Entry entry, EventType eventType) throws Exception {
        CachedOrderStep cos = getCachedOrderStep(dbLayer, entry.getKey());
        if (cos.getEndTime() == null) {
            LogEntry le = new LogEntry(LogEntry.LogLevel.Info, eventType, entry.getEventIdAsDate(), entry.getEventDate());
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
            List<DBItemOrder> items = dbLayer.getOrder(masterConfiguration.getCurrent().getJobSchedulerId(), key);
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
        List<DBItemOrder> items = dbLayer.getOrder(masterConfiguration.getCurrent().getJobSchedulerId(), key);
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
            DBItemOrderStep item = dbLayer.getOrderStep(masterConfiguration.getCurrent().getJobSchedulerId(), key);
            if (item == null) {
                throw new Exception(String.format("[%s]order step not found. orderKey=%s", identifier, key));
            } else {
                DBItemAgent agent = dbLayer.getAgent(masterConfiguration.getCurrent().getJobSchedulerId(), item.getAgentPath());
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
        DBItemOrderStep item = dbLayer.getOrderStep(masterConfiguration.getCurrent().getJobSchedulerId(), key, startEventId);
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
            DBItemAgent item = dbLayer.getAgent(masterConfiguration.getCurrent().getJobSchedulerId(), key);
            if (item == null) {
                throw new Exception(String.format("[%s]agent not found. jobSchedulerId=%s, agentPath=%s", identifier, masterConfiguration.getCurrent()
                        .getJobSchedulerId(), key));
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

    private void clearCache(CacheType cacheType, String orderKey) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][clearCache][%s]%s", identifier, cacheType, orderKey));
        }
        switch (cacheType) {
        case orderStep:
            cachedOrderSteps.entrySet().removeIf(entry -> entry.getKey().equals(orderKey));
            break;
        case order:
            cachedOrders.entrySet().removeIf(entry -> entry.getKey().equals(orderKey));
            cachedOrderSteps.entrySet().removeIf(entry -> entry.getKey().startsWith(orderKey));
            break;
        default:
            break;
        }
    }

    private DBItemLog storeLogFile2Db(DBLayerHistory dbLayer, Long mainOrderId, Long orderId, Long orderStepId, boolean compressed, Path file)
            throws Exception {

        DBItemLog item = null;
        File f = SOSPath.toFile(file);
        if (f.exists()) {
            item = new DBItemLog();
            item.setJobSchedulerId(masterConfiguration.getCurrent().getJobSchedulerId());

            item.setMainOrderId(mainOrderId);
            item.setOrderId(orderId);
            item.setOrderStepId(orderStepId);
            item.setCompressed(compressed);

            item.setFileBasename(com.google.common.io.Files.getNameWithoutExtension(f.getName()));
            item.setFileSizeUncomressed(f.length());
            item.setFileLinesUncomressed(Files.lines(file).count());
            if (item.getCompressed()) {// task
                item.setFileContent(SOSPath.gzip(file));
            } else {// order
                item.setFileContent(Files.lines(file).collect(Collectors.joining(",", "[", "]")).getBytes(StandardCharsets.UTF_8));
            }
            item.setCreated(new Date());

            dbLayer.getSession().save(item);
        } else {
            LOGGER.error(String.format("[%s][%s]file not found", identifier, f.getCanonicalPath()));
        }
        return item;
    }

    private Path getMasterAndAgentsLog() {
        return Paths.get(historyConfiguration.getLogDir(), "0.log");
    }

    private Path getOrderLog(Path dir, LogEntry entry) {
        return dir.resolve(entry.getMainOrderId() + ".log");
    }

    private Path getOrderStepLog(Path dir, LogEntry entry) {
        return dir.resolve(entry.getMainOrderId() + "_" + entry.getOrderStepId() + ".log");
    }

    private Path getOrderLogDirectory(LogEntry entry) {
        return Paths.get(historyConfiguration.getLogDir(), String.valueOf(entry.getMainOrderId()));
    }

    private OrderLogEntry createOrderLogEntry(LogEntry logEntry) {
        OrderLogEntry entry = new OrderLogEntry();
        entry.setOrderId(logEntry.getOrderKey());
        entry.setLogLevel(logEntry.getLogLevel().name().toUpperCase());
        entry.setLogEvent(logEntry.getEventType());
        entry.setPosition(logEntry.getPosition());
        entry.setReturnCode(logEntry.getReturnCode());
        if (logEntry.isError()) {
            Error error = new Error();
            error.setErrorState(logEntry.getErrorState());
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

    private Path storeLog2File(LogEntry entry) throws Exception {
        return storeLog2File(entry, null);
    }

    private Path storeLog2File(LogEntry entry, CachedOrderStep cos) throws Exception {

        OrderLogEntry orderEntry;
        LinkedHashMap<String, String> hm;
        StringBuilder content = new StringBuilder();
        Path dir = getOrderLogDirectory(entry);
        Path file = null;
        boolean newLine;
        boolean append;

        switch (entry.getEventType()) {
        case ORDER_PROCESSING_STARTED:
        case ORDER_PROCESSED:
            // order log
            newLine = true;
            orderEntry = createOrderLogEntry(entry);
            orderEntry.setMasterDatetime(getDateAsString(entry.getMasterDatetime(), masterTimezone));
            orderEntry.setAgentDatetime(getDateAsString(entry.getAgentDatetime(), entry.getAgentTimezone()));
            orderEntry.setAgentPath(entry.getAgentPath());
            orderEntry.setAgentUrl(entry.getAgentUri());
            orderEntry.setJob(entry.getJobName());
            orderEntry.setTaskId(entry.getOrderStepId());
            write2file(getOrderLog(dir, entry), new StringBuilder((new ObjectMapper()).writeValueAsString(orderEntry)), newLine);

            // task log
            file = getOrderStepLog(dir, entry);
            content.append(getDateAsString(entry.getAgentDatetime(), entry.getAgentTimezone())).append(" ");
            content.append("[").append(entry.getLogLevel().name().toUpperCase()).append("]");
            content.append(entry.getChunk());
            break;

        case ORDER_STDOUT_WRITTEN:
        case ORDER_STDERR_WRITTEN:
            newLine = false;
            append = false;
            file = getOrderStepLog(dir, entry);
            if (cos.isLastStdEndsWithNewLine() == null && SOSPath.endsWithNewLine(file)) {
                append = true;
            } else if (cos.isLastStdEndsWithNewLine().booleanValue()) {
                append = true;
            }
            if (append) {
                String outType = entry.getEventType().equals(EventType.ORDER_STDOUT_WRITTEN) ? "STDOUT" : "STDERR";
                content.append(getDateAsString(entry.getAgentDatetime(), entry.getAgentTimezone())).append(" ");
                content.append("[").append(outType).append("]");
            }
            cos.setLastStdEndsWithNewLine(entry.getChunk().endsWith("\n"));
            content.append(entry.getChunk());
            break;
        case AGENT_READY:
        case MASTER_READY:
            newLine = true;
            file = getMasterAndAgentsLog();
            hm = new LinkedHashMap<>();
            hm.put("masterDatetime", getDateAsString(entry.getMasterDatetime(), masterTimezone));
            if (entry.getAgentDatetime() != null && entry.getAgentTimezone() != null) {
                hm.put("agentDatetime", getDateAsString(entry.getAgentDatetime(), entry.getAgentTimezone()));
            }
            hm.put("logLevel", entry.getLogLevel().name().toUpperCase());
            hm.put("logEvent", entry.getEventType().value());
            if (entry.isError()) {
                hm.put("error", "1");
                hm.put("errorState", entry.getErrorState());
                hm.put("errorReason", entry.getErrorReason());
                hm.put("errorCode", entry.getErrorCode());
                hm.put("errorReturnCode", (entry.getReturnCode() == null) ? "" : String.valueOf(entry.getReturnCode()));
                hm.put("errorText", entry.getErrorText());
            }
            content.append(hm);
            break;
        case ORDER_ADDED:
            if (!Files.exists(dir)) {
                Files.createDirectory(dir);
            }
        default:
            // order log
            newLine = true;
            file = getOrderLog(dir, entry);

            orderEntry = createOrderLogEntry(entry);
            orderEntry.setMasterDatetime(getDateAsString(entry.getMasterDatetime(), masterTimezone));
            if (entry.getAgentDatetime() != null && entry.getAgentTimezone() != null) {
                orderEntry.setAgentDatetime(getDateAsString(entry.getAgentDatetime(), entry.getAgentTimezone()));
            }
            content.append((new ObjectMapper()).writeValueAsString(orderEntry));
        }

        try {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][%s]%s", identifier, entry.getEventType().value(), entry.getOrderKey(), file));
            }
            write2file(file, content, newLine);
        } catch (NoSuchFileException e) {
            if (!Files.exists(dir)) {
                Files.createDirectory(dir);
            }
            write2file(file, content, newLine);
        } catch (Exception e) {
            LOGGER.error(String.format("[%s][%s][%s][%s]%s", identifier, entry.getEventType().value(), entry.getOrderKey(), file, e.toString()), e);
            throw e;
        }

        return file;
    }

    private void write2file(Path file, StringBuilder content, boolean newLine) throws Exception {
        BufferedWriter writer = null;
        try {
            writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            writer.write(content.toString());
            if (newLine) {
                writer.write(HistoryUtil.NEW_LINE);
            }
        } catch (Exception e) {
            throw e;
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

    private String hashOrderConstaint(Long eventId, String orderKey, String workflowPosition) {
        return HistoryUtil.hashString(new StringBuilder(masterConfiguration.getCurrent().getJobSchedulerId()).append(String.valueOf(eventId)).append(
                orderKey).append(workflowPosition).toString());
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

    public MasterConfiguration getMasterConfiguration() {
        return masterConfiguration;
    }

    public void setIdentifier(String val) {
        identifier = val;
    }
}
