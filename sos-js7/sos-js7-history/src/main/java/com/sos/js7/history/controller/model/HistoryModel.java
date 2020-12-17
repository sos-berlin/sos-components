package com.sos.js7.history.controller.model;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
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
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.exception.SOSHibernateObjectOperationException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.model.event.EventType;
import com.sos.joc.Globals;
import com.sos.joc.classes.history.HistoryPosition;
import com.sos.joc.db.history.DBItemHistoryAgent;
import com.sos.joc.db.history.DBItemHistoryController;
import com.sos.joc.db.history.DBItemHistoryLog;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.db.history.DBItemHistoryOrderState;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.db.history.common.HistorySeverity;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.joc.DBItemJocVariable;
import com.sos.joc.model.inventory.common.JobCriticality;
import com.sos.joc.model.order.OrderStateText;
import com.sos.js7.event.controller.EventMeta;
import com.sos.js7.event.controller.configuration.controller.ControllerConfiguration;
import com.sos.js7.history.controller.HistoryMain;
import com.sos.js7.history.controller.configuration.HistoryConfiguration;
import com.sos.js7.history.controller.exception.FatEventOrderNotFoundException;
import com.sos.js7.history.controller.exception.FatEventOrderStepNotFoundException;
import com.sos.js7.history.controller.proxy.HistoryEventType;
import com.sos.js7.history.controller.proxy.fatevent.AFatEvent;
import com.sos.js7.history.controller.proxy.fatevent.AFatEventOrderProcessed;
import com.sos.js7.history.controller.proxy.fatevent.FatEventAgentReady;
import com.sos.js7.history.controller.proxy.fatevent.FatEventControllerReady;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderAdded;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderForked;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderJoined;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderResumeMarked;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderResumed;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderStepProcessed;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderStepStarted;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderStepStdWritten;
import com.sos.js7.history.controller.proxy.fatevent.FatEventWithProblem;
import com.sos.js7.history.controller.proxy.fatevent.FatForkedChild;
import com.sos.js7.history.controller.proxy.fatevent.FatOutcome;
import com.sos.js7.history.db.DBLayerHistory;
import com.sos.js7.history.helper.CachedAgent;
import com.sos.js7.history.helper.CachedOrder;
import com.sos.js7.history.helper.CachedOrderStep;
import com.sos.js7.history.helper.Counter;
import com.sos.js7.history.helper.HistoryUtil;
import com.sos.js7.history.helper.LogEntry;
import com.sos.js7.history.helper.LogEntry.LogLevel;
import com.sos.webservices.json.jobscheduler.history.order.Error;
import com.sos.webservices.json.jobscheduler.history.order.OrderLogEntry;

public class HistoryModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryModel.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();

    private static final long MAX_LOCK_VERSION = 10_000_000;
    private final SOSHibernateFactory dbFactory;
    private HistoryConfiguration historyConfiguration;
    private ControllerConfiguration controllerConfiguration;
    private String identifier;
    private final String variableName;
    private Long lockVersion;
    private Long storedEventId;
    private boolean closed = false;
    private int maxTransactions = 100;
    private long transactionCounter;
    private String controllerTimezone;
    private boolean cleanupLogFiles = true;

    private Map<String, CachedOrder> cachedOrders;
    private Map<String, CachedOrderStep> cachedOrderSteps;
    private Map<String, CachedAgent> cachedAgents;

    private static enum CacheType {
        order, orderStep
    };

    private static enum OrderStartCause {
        order, fork, file_trigger, setback, unskip, unstop
    };

    private static enum OrderStepStartCause {
        order, file_trigger, setback, unskip, unstop
    };

    public HistoryModel(SOSHibernateFactory factory, HistoryConfiguration historyConf, ControllerConfiguration controllerConf) {
        dbFactory = factory;
        historyConfiguration = historyConf;
        controllerConfiguration = controllerConf;
        variableName = "history_" + controllerConfiguration.getCurrent().getId();
        maxTransactions = historyConfiguration.getMaxTransactions();
        initCache();
    }

    public Long getEventId() throws Exception {
        DBLayerHistory dbLayer = null;
        try {
            dbLayer = new DBLayerHistory(dbFactory.openStatelessSession());
            dbLayer.getSession().setIdentifier(identifier);
            dbLayer.getSession().beginTransaction();
            DBItemJocVariable item = dbLayer.getVariable(variableName);
            if (item == null) {
                item = dbLayer.insertJocVariable(variableName, "0");
            }
            dbLayer.getSession().commit();

            lockVersion = item.getLockVersion();
            return Long.parseLong(item.getTextValue());
        } catch (Exception e) {
            if (dbLayer != null) {
                try {
                    dbLayer.getSession().rollback();
                } catch (Throwable ex) {
                }
            }
            throw e;
        } finally {
            if (dbLayer != null) {
                dbLayer.close();
            }
        }
    }

    public Long process(List<AFatEvent> list) throws Exception {
        String method = "process";

        if (closed) {
            return storedEventId;
        }

        transactionCounter = 0;
        DBLayerHistory dbLayer = null;
        Map<String, CachedOrderStep> endedOrderSteps = new HashMap<>();
        Instant start = Instant.now();
        Long startEventId = storedEventId;
        Long firstEventId = new Long(0L);
        Long lastSuccessEventId = new Long(0L);

        Counter counter = new Counter();
        counter.setTotal(list.size());

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s][start][%s][%s]%s total", identifier, method, storedEventId, start, counter.getTotal()));
        }

        try {
            dbLayer = new DBLayerHistory(dbFactory.openStatelessSession());
            dbLayer.getSession().setIdentifier(identifier);
            dbLayer.getSession().beginTransaction();

            for (AFatEvent entry : list) {
                if (closed) {// TODO
                    LOGGER.info(String.format("[%s][%s][skip]is closed", identifier, method));
                    break;
                }
                Long eventId = entry.getEventId();

                if (counter.getProcessed() == 0) {
                    firstEventId = eventId;
                }
                if (storedEventId >= eventId) {
                    if (entry.getType().equals(HistoryEventType.EventWithProblem)) { // EventWithProblem can sets eventId=-1L
                        LOGGER.warn(String.format("[%s][%s][%s][skip]stored eventId=%s > current eventId=%s %s", identifier, method, entry.getType(),
                                storedEventId, eventId, SOSString.toString(entry)));

                        FatEventWithProblem ep = (FatEventWithProblem) entry;
                        LOGGER.warn(String.format("[entry]%s", SOSString.toString(ep.getEntry())));
                        LOGGER.error(ep.getError().toString(), ep.getError());
                    } else {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][%s][skip]stored eventId=%s > current eventId=%s %s", identifier, method, entry
                                    .getType(), storedEventId, eventId, SOSString.toString(entry)));
                        }
                    }

                    counter.addProcessed();
                    counter.addSkipped();
                    continue;
                }

                if (isDebugEnabled) {
                    LOGGER.debug("--- " + entry.getType() + " ------------------------------------------");
                    if (isTraceEnabled) {
                        LOGGER.trace(String.format("[%s][%s][%s]%s", identifier, method, entry.getType(), SOSString.toString(entry)));
                    }
                }

                transactionCounter++;

                try {
                    switch (entry.getType()) {
                    case ControllerReady:
                        controllerReady(dbLayer, (FatEventControllerReady) entry);
                        counter.getController().addReady();
                        break;
                    case AgentReady:
                        agentReady(dbLayer, (FatEventAgentReady) entry);
                        counter.getAgent().addReady();
                        break;
                    case OrderAdded:
                        orderAdded(dbLayer, (FatEventOrderAdded) entry);
                        counter.getOrder().addAdded();
                        break;
                    case OrderResumed:
                        orderResumed(dbLayer, (FatEventOrderResumed) entry);
                        counter.getOrder().addResumed();
                        break;
                    case OrderResumeMarked:
                        orderResumeMarked(dbLayer, (FatEventOrderResumeMarked) entry);
                        counter.getOrder().addResumeMarked();
                        break;
                    case OrderForked:
                        orderForked(dbLayer, (FatEventOrderForked) entry);
                        counter.getOrder().addForked();
                        break;
                    case OrderJoined:
                        orderJoined(dbLayer, (FatEventOrderJoined) entry, endedOrderSteps);
                        counter.getOrder().addJoined();
                        break;
                    case OrderStepStarted:
                        orderStepStarted(dbLayer, (FatEventOrderStepStarted) entry);
                        counter.getOrderStep().addStarted();
                        break;
                    case OrderStepStdoutWritten:
                        orderStepStd(dbLayer, (FatEventOrderStepStdWritten) entry, EventType.OrderStdoutWritten);
                        counter.getOrderStep().addStdWritten();
                        break;
                    case OrderStepStderrWritten:
                        orderStepStd(dbLayer, (FatEventOrderStepStdWritten) entry, EventType.OrderStderrWritten);
                        counter.getOrderStep().addStdWritten();
                        break;
                    case OrderStepProcessed:
                        orderStepProcessed(dbLayer, (FatEventOrderStepProcessed) entry, endedOrderSteps);
                        counter.getOrderStep().addProcessed();
                        break;
                    case OrderFailed:
                        orderNotCompleted(dbLayer, (AFatEventOrderProcessed) entry, EventType.OrderFailed, endedOrderSteps);
                        counter.getOrder().addFailed();
                        break;
                    case OrderSuspended:
                        orderNotCompleted(dbLayer, (AFatEventOrderProcessed) entry, EventType.OrderSuspended, endedOrderSteps);
                        counter.getOrder().addSuspended();
                        break;
                    case OrderSuspendMarked:
                        orderNotCompleted(dbLayer, (AFatEventOrderProcessed) entry, EventType.OrderSuspendMarked, endedOrderSteps);
                        counter.getOrder().addSuspendMarked();
                        break;
                    case OrderCancelled:
                        orderProcessed(dbLayer, (AFatEventOrderProcessed) entry, EventType.OrderCancelled, endedOrderSteps);
                        counter.getOrder().addCancelled();
                        break;
                    case OrderBroken:
                        // TODO update main order when a child is broken
                        orderProcessed(dbLayer, (AFatEventOrderProcessed) entry, EventType.OrderBroken, endedOrderSteps);
                        counter.getOrder().addBroken();
                        break;
                    case OrderFinished:
                        orderProcessed(dbLayer, (AFatEventOrderProcessed) entry, EventType.OrderFinished, endedOrderSteps);
                        counter.getOrder().addFinished();
                        break;
                    case EventWithProblem:
                        FatEventWithProblem ep = (FatEventWithProblem) entry;
                        LOGGER.warn(String.format("[entry]%s", SOSString.toString(ep.getEntry())));
                        LOGGER.error(ep.getError().toString(), ep.getError());
                        break;
                    }
                    counter.addProcessed();
                    lastSuccessEventId = eventId;
                } catch (FatEventOrderNotFoundException | FatEventOrderStepNotFoundException e) { // TODO ask proxy
                    LOGGER.warn(String.format("[%s][%s][%s][failed]%s[%s]", identifier, method, entry.getType(), e.toString(), SOSString.toString(
                            entry)));
                    counter.addProcessed();
                    lastSuccessEventId = eventId;
                    counter.addFailed();
                }
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
            showSummary(startEventId, firstEventId, start, counter);
            transactionCounter = 0L;
        }

        return storedEventId;
    }

    private Duration showSummary(Long startEventId, Long firstEventId, Instant start, Counter counter) {
        String startEventIdAsTime = startEventId.equals(new Long(0L)) ? "0" : SOSDate.getTime(EventMeta.eventId2Instant(startEventId));
        String endEventIdAsTime = storedEventId.equals(new Long(0L)) ? "0" : SOSDate.getTime(EventMeta.eventId2Instant(storedEventId));
        String firstEventIdAsTime = firstEventId.equals(new Long(0L)) ? "0" : SOSDate.getTime(EventMeta.eventId2Instant(firstEventId));
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        LOGGER.info(String.format("[%s][%s(%s)-%s][%s(%s)-%s][%s-%s][%s]%s %s", identifier, startEventId, firstEventId, storedEventId,
                startEventIdAsTime, firstEventIdAsTime, endEventIdAsTime, SOSDate.getTime(start), SOSDate.getTime(end), SOSDate.getDuration(duration),
                counter.toString(), getCachedSummary()));
        return duration;
    }

    private String getCachedSummary() {
        // TODO remove cached items - dependent of the created time
        int coSize = cachedOrders.size();
        int cosSize = cachedOrderSteps.size();
        StringBuilder sb = new StringBuilder();
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[cachedAgents=%s][cachedOrders=%s][cachedOrderSteps=%s]", cachedAgents.size(), coSize, cosSize));
        }
        if (coSize >= 1_000) {
            sb.append("cachedOrders=").append(coSize);
        } else {
            if (isDebugEnabled && coSize > 0) {
                // LOGGER.debug(SOSString.mapToString(cachedOrders, true));
            }
        }
        if (cosSize >= 1_000) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("cachedOrderSteps=").append(cosSize);
        } else {
            if (isDebugEnabled && cosSize > 0) {
                // LOGGER.debug(SOSString.mapToString(cachedOrderSteps, true));
            }
        }
        return sb.toString();
    }

    private void initCache() {
        cachedOrders = new HashMap<>();
        cachedOrderSteps = new HashMap<>();
        cachedAgents = new HashMap<>();
    }

    public void close() {
        closed = true;
    }

    private void tryStoreCurrentState(DBLayerHistory dbLayer, Long eventId) throws Exception {
        if (transactionCounter % maxTransactions == 0) {
            storeCurrentState(dbLayer, eventId);
            dbLayer.getSession().beginTransaction();
        }
    }

    private void tryStoreCurrentStateAtEnd(DBLayerHistory dbLayer, Long eventId) throws Exception {
        if (eventId > 0 && !storedEventId.equals(eventId)) {
            storeCurrentState(dbLayer, eventId);
        }
    }

    private void storeCurrentState(DBLayerHistory dbLayer, Long eventId) throws Exception {
        // if (!isMySQL && dbLayer.getSession().isTransactionOpened()) {// TODO
        if (!dbLayer.getSession().isTransactionOpened()) {
            dbLayer.getSession().beginTransaction();
        }
        updateJocVariable(dbLayer, eventId);
        dbLayer.getSession().commit();
        storedEventId = eventId;
    }

    private void updateJocVariable(DBLayerHistory dbLayer, Long eventId) throws Exception {
        boolean resetLockVersion = lockVersion != null && lockVersion > MAX_LOCK_VERSION;// TODO lockVersion reset
        dbLayer.updateJocVariable(variableName, eventId, resetLockVersion);
    }

    private void controllerReady(DBLayerHistory dbLayer, FatEventControllerReady entry) throws Exception {
        DBItemHistoryController item = new DBItemHistoryController();
        try {
            Date eventDate = entry.getEventDatetime();
            item.setJobSchedulerId(controllerConfiguration.getCurrent().getId());
            item.setUri(controllerConfiguration.getCurrent().getUri());
            item.setTimezone(entry.getTimezone());
            item.setStartTime(eventDate);
            item.setIsPrimary(controllerConfiguration.getCurrent().isPrimary());// TODO
            item.setEventId(String.valueOf(entry.getEventId()));
            item.setCreated(new Date());

            dbLayer.getSession().save(item);

            controllerTimezone = item.getTimezone();
            tryStoreCurrentState(dbLayer, entry.getEventId());
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                LOGGER.error(e.toString(), e);
                throw e;
            }
            LOGGER.warn(String.format("[%s][%s][%s]%s", identifier, entry.getType(), controllerConfiguration.getCurrent().getUri(), e.toString()), e);
            LOGGER.warn(String.format("[%s][ConstraintViolation item]%s", identifier, SOSHibernate.toString(item)));
        } finally {
            if (controllerTimezone == null) {
                controllerTimezone = entry.getTimezone();
            }
        }
    }

    private void checkControllerTimezone(DBLayerHistory dbLayer) throws Exception {
        if (controllerTimezone == null) {
            controllerTimezone = dbLayer.getControllerTimezone(controllerConfiguration.getCurrent().getId());
            if (controllerTimezone == null) {
                // TODO read from controller api, and instances
                // throw new Exception(String.format("controller not found: %s", controllerConfiguration.getCurrent().getId()));
                LOGGER.warn(String.format("[%s][%s]controller not found in the history. set controller timezone=UTC", identifier,
                        controllerConfiguration.getCurrent().getId()));
                controllerTimezone = "UTC";
            }
        }
    }

    private void agentReady(DBLayerHistory dbLayer, FatEventAgentReady entry) throws Exception {
        DBItemHistoryAgent item = new DBItemHistoryAgent();
        CachedAgent ca = null;

        try {
            checkControllerTimezone(dbLayer);

            try {
                ca = getCachedAgent(dbLayer, entry.getPath());
            } catch (Exception ex) {
            }

            item.setJobSchedulerId(controllerConfiguration.getCurrent().getId());
            item.setPath(entry.getPath());
            item.setUri(entry.getUri());
            item.setTimezone(entry.getTimezone());
            item.setStartTime(entry.getEventDatetime());
            item.setEventId(String.valueOf(entry.getEventId()));
            item.setCreated(new Date());

            dbLayer.getSession().save(item);

            ca = new CachedAgent(item);
            addCachedAgent(item.getPath(), ca);

            tryStoreCurrentState(dbLayer, entry.getEventId());
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                LOGGER.error(e.toString(), e);
                throw e;
            }
            LOGGER.warn(String.format("[%s][%s][%s]%s", identifier, entry.getType(), entry.getPath(), e.toString()), e);
            LOGGER.warn(String.format("[%s][ConstraintViolation item]%s", identifier, SOSHibernate.toString(item)));
        }
    }

    private void orderAdded(DBLayerHistory dbLayer, FatEventOrderAdded entry) throws Exception {
        DBItemHistoryOrder item = new DBItemHistoryOrder();
        String constraintHash = null;

        try {
            checkControllerTimezone(dbLayer);

            item.setJobSchedulerId(controllerConfiguration.getCurrent().getId());
            item.setOrderKey(entry.getOrderId());

            item.setWorkflowPath(entry.getWorkflowPath());
            item.setWorkflowVersionId(entry.getWorkflowVersionId());
            item.setWorkflowPosition(HistoryPosition.getParentAsString(entry.getPosition()));
            item.setWorkflowFolder(HistoryUtil.getFolderFromPath(item.getWorkflowPath()));
            item.setWorkflowName(HistoryUtil.getBasenameFromPath(item.getWorkflowPath()));
            item.setWorkflowTitle(null);// TODO

            item.setMainParentId(new Long(0L));// TODO see below item.setParentId(new Long(0));
            item.setParentId(new Long(0L));
            item.setParentOrderKey(null);
            item.setHasChildren(false);
            item.setRetryCounter(HistoryPosition.getRetry(entry.getPosition()));

            item.setName(entry.getOrderId());// TODO
            item.setTitle(null);// TODO

            item.setStartCause(OrderStartCause.order.name());// TODO

            Date planned = entry.getPlanned() == null ? entry.getEventDatetime() : entry.getPlanned();
            item.setStartTimePlanned(planned);

            item.setStartTime(Globals.HISTORY_DEFAULT_DATE);// 1970-01-01 01:00:00
            item.setStartWorkflowPosition(HistoryPosition.asString(entry.getPosition()));
            item.setStartEventId(String.valueOf(entry.getEventId()));
            item.setStartParameters(entry.getArgumentsAsJsonString());

            item.setCurrentOrderStepId(new Long(0));

            item.setEndTime(null);
            item.setEndWorkflowPosition(null);
            item.setEndOrderStepId(new Long(0));

            item.setSeverity(OrderStateText.PENDING);
            item.setState(OrderStateText.PENDING);

            item.setStateTime(entry.getEventDatetime());
            item.setStateText(null);// TODO
            item.setHasStates(false);

            item.setError(false);
            item.setErrorState(null);
            item.setErrorReason(null);
            item.setErrorReturnCode(null);
            item.setErrorCode(null);
            item.setErrorText(null);
            item.setEndEventId(null);

            item.setLogId(new Long(0));

            constraintHash = hashOrderConstraint(entry.getEventId(), item.getOrderKey(), item.getWorkflowPosition());
            item.setConstraintHash(constraintHash);
            item.setCreated(new Date());
            item.setModified(item.getCreated());

            dbLayer.getSession().save(item);

            item.setMainParentId(item.getId()); // TODO see above
            dbLayer.setMainParentId(item.getId(), item.getMainParentId());

            CachedOrder co = new CachedOrder(item);
            LogEntry le = new LogEntry(LogEntry.LogLevel.DETAIL, EventType.OrderAdded, entry.getEventDatetime(), null);
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
            LOGGER.warn(String.format("[%s][%s][%s]%s", identifier, entry.getType(), entry.getOrderId(), e.toString()), e);
            LOGGER.warn(String.format("[%s][ConstraintViolation current item]%s", identifier, SOSHibernate.toString(item)));
            addCachedOrderByStateTime(dbLayer, entry.getOrderId(), entry.getEventDatetime(), entry.getEventId(), constraintHash);
        }
    }

    private void orderProcessed(DBLayerHistory dbLayer, AFatEventOrderProcessed entry, EventType eventType,
            Map<String, CachedOrderStep> endedOrderSteps) throws Exception {
        orderUpdate(dbLayer, eventType, entry.getEventId(), entry.getOrderId(), entry.getEventDatetime(), entry.getOutcome(), endedOrderSteps, true);
    }

    private void orderNotCompleted(DBLayerHistory dbLayer, AFatEventOrderProcessed entry, EventType eventType,
            Map<String, CachedOrderStep> endedOrderSteps) throws Exception {
        orderUpdate(dbLayer, eventType, entry.getEventId(), entry.getOrderId(), entry.getEventDatetime(), entry.getOutcome(), endedOrderSteps, false);
    }

    private CachedOrder orderUpdate(DBLayerHistory dbLayer, EventType eventType, Long eventId, String orderKey, Date eventDate, FatOutcome outcome,
            Map<String, CachedOrderStep> endedOrderSteps, boolean completeOrder) throws Exception {
        CachedOrder co = getCachedOrder(dbLayer, orderKey, eventDate, eventId);
        if (co.getEndTime() == null) {
            checkControllerTimezone(dbLayer);

            CachedOrderStep cos = getCurrentOrderStep(dbLayer, co, endedOrderSteps);
            LogEntry le = createOrderLogEntry(eventId, outcome, cos, eventType);

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

            if (le.isError() && SOSString.isEmpty(le.getErrorText()) && cos != null) {
                le.setErrorText(cos.getStdErr());
            }
            Date startTime = null;
            String startEventId = null;
            switch (eventType) {
            case OrderJoined:
                if (le.isError()) {
                    co.setHasStates(true);
                }
                break;
            case OrderBroken:
            case OrderFailed:
            case OrderCancelled:
            case OrderSuspended:
            case OrderSuspendMarked:
            case OrderResumed:
            case OrderResumeMarked:
                co.setHasStates(true);
                break;
            default:
                break;
            }
            dbLayer.setOrderEnd(co.getId(), endTime, endWorkflowPosition, endOrderStepId, endEventId, le.getState(), eventDate, co.getHasStates(), le
                    .isError(), le.getErrorState(), le.getErrorReason(), le.getReturnCode(), le.getErrorCode(), le.getErrorText(), startTime,
                    startEventId);

            if (co.getHasStates()) {
                saveOrderState(dbLayer, co, le.getState(), eventDate, eventId, le.getErrorCode(), le.getErrorText());
            }

            le.onOrder(co, co.getWorkflowPosition());
            Path log = storeLog2File(le);
            // if (completeOrder && co.getParentId().longValue() == 0L) {
            if (completeOrder) {
                DBItemHistoryLog logItem = storeLogFile2Db(dbLayer, co.getMainParentId(), co.getId(), new Long(0L), false, log);
                if (logItem != null) {
                    dbLayer.setOrderLogId(co.getId(), logItem.getId());
                    if (cleanupLogFiles) {
                        if (co.getParentId().longValue() == 0L) {
                            SOSPath.deleteDirectory(log.getParent());
                        } else {
                            Files.delete(log);
                        }
                    }
                }
            }
            tryStoreCurrentState(dbLayer, eventId);
            if (completeOrder) {
                clearCache(CacheType.order, orderKey);
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
                DBItemHistoryOrderStep item = dbLayer.getOrderStep(co.getCurrentOrderStepId());
                if (item == null) {
                    LOGGER.warn(String.format("[%s][%s][currentStep not found]id=%s", identifier, co.getOrderKey(), co.getCurrentOrderStepId()));
                } else {
                    CachedAgent ca = getCachedAgent(dbLayer, item.getAgentPath());
                    step = new CachedOrderStep(item, ca.getTimezone());
                    if (item.getError()) {
                        step.setError(item.getErrorState(), item.getErrorReason(), item.getErrorCode(), item.getErrorText());
                    }
                }
            } else if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][currentStep found]%s", identifier, co.getOrderKey(), SOSString.toString(step)));
            }
        }
        return step;
    }

    private LogEntry createOrderLogEntry(Long eventId, FatOutcome outcome, CachedOrderStep cos, EventType eventType) {
        LogEntry le = new LogEntry(LogEntry.LogLevel.DETAIL, eventType, Date.from(EventMeta.eventId2Instant(eventId)), null);
        boolean stepHasError = (cos != null && cos.getError() != null);
        if (outcome != null) {
            le.setReturnCode(outcome.getReturnCode());
            if (outcome.isFailed()) {
                boolean setError = true;
                if (eventType.equals(EventType.OrderJoined))
                    if (stepHasError) {
                        if (le.getReturnCode() != null && le.getReturnCode().equals(0)) {
                            le.setReturnCode(cos.getReturnCode());
                        }
                    } else {
                        setError = false;
                    }
                if (setError) {
                    le.setError(OrderStateText.FAILED.value(), outcome);
                }
            }
        }
        if (!le.isError() && stepHasError) {
            le.setReturnCode(cos.getReturnCode());
            le.setError(OrderStateText.FAILED.value(), cos);
        }

        switch (eventType) {
        case OrderFailed:
        case OrderFailedinFork:
            le.setState(OrderStateText.FAILED.intValue());
            le.setLogLevel(LogLevel.ERROR);
            break;
        case OrderBroken:
            le.setState(OrderStateText.BROKEN.intValue());
            le.setLogLevel(LogLevel.ERROR);
            break;
        case OrderCancelled:
            le.setState(OrderStateText.CANCELLED.intValue());
            le.setLogLevel(LogLevel.MAIN);
            break;
        case OrderSuspended:
            le.setState(OrderStateText.SUSPENDED.intValue());
            le.setLogLevel(LogLevel.MAIN);
            break;
        case OrderSuspendMarked:
            le.setState(OrderStateText.SUSPENDMARKED.intValue());
            le.setLogLevel(LogLevel.INFO);
            break;
        case OrderResumed:
            le.setState(OrderStateText.RESUMED.intValue());
            le.setLogLevel(LogLevel.MAIN);
            break;
        case OrderResumeMarked:
            le.setState(OrderStateText.RESUMEMARKED.intValue());
            le.setLogLevel(LogLevel.INFO);
            break;
        case OrderFinished:
            if (le.isError()) {// TODO ??? error on order_finished ???
                le.setState(OrderStateText.FAILED.intValue());
                le.setLogLevel(LogLevel.ERROR);
            } else {
                le.setState(OrderStateText.FINISHED.intValue());
                le.setLogLevel(LogLevel.MAIN);
            }
            break;
        default:
            if (le.isError()) {
                le.setState(OrderStateText.FAILED.intValue());
                le.setLogLevel(LogLevel.ERROR);
            } else {
                le.setState(OrderStateText.FINISHED.intValue());
                le.setLogLevel(LogLevel.MAIN);
            }
            break;
        }

        return le;
    }

    private void orderResumed(DBLayerHistory dbLayer, FatEventOrderResumed entry) throws Exception {
        checkControllerTimezone(dbLayer);

        CachedOrder co = getCachedOrder(dbLayer, entry.getOrderId(), entry.getEventDatetime(), entry.getEventId());
        co.setState(OrderStateText.RESUMED.intValue());
        co.setHasStates(true);
        // addCachedOrder(co.getOrderKey(), co);

        dbLayer.updateOrderOnResumed(co.getId(), co.getState(), entry.getEventDatetime());
        saveOrderState(dbLayer, co, co.getState(), entry.getEventDatetime(), entry.getEventId(), null, null);

        LogEntry le = new LogEntry(LogEntry.LogLevel.MAIN, EventType.OrderResumed, entry.getEventDatetime(), null);
        le.onOrder(co, null);
        storeLog2File(le);
    }

    private void orderResumeMarked(DBLayerHistory dbLayer, FatEventOrderResumeMarked entry) throws Exception {
        checkControllerTimezone(dbLayer);

        CachedOrder co = getCachedOrder(dbLayer, entry.getOrderId(), entry.getEventDatetime(), entry.getEventId());
        co.setState(OrderStateText.RESUMEMARKED.intValue());
        co.setHasStates(true);
        // addCachedOrder(co.getOrderKey(), co);

        dbLayer.updateOrderOnResumed(co.getId(), co.getState(), entry.getEventDatetime());
        saveOrderState(dbLayer, co, co.getState(), entry.getEventDatetime(), entry.getEventId(), null, null);

        LogEntry le = new LogEntry(LogEntry.LogLevel.MAIN, EventType.OrderResumeMarked, entry.getEventDatetime(), null);
        le.onOrder(co, null);
        storeLog2File(le);
    }

    private void orderForked(DBLayerHistory dbLayer, FatEventOrderForked entry) throws Exception {
        checkControllerTimezone(dbLayer);

        CachedOrder co = getCachedOrder(dbLayer, entry.getOrderId(), entry.getEventDatetime(), entry.getEventId());
        co.setHasChildren(true);
        if (co.getState().equals(OrderStateText.PENDING.intValue())) {
            co.setState(OrderStateText.RUNNING.intValue());
        }
        String startEventId = null;
        if (SOSDate.equals(co.getStartTime(), Globals.HISTORY_DEFAULT_DATE)) {
            startEventId = String.valueOf(entry.getEventId());
            co.setStartTime(entry.getEventDatetime());
        }
        // addCachedOrder(co.getOrderKey(), co);

        dbLayer.updateOrderOnFork(co.getId(), co.getState(), entry.getEventDatetime(), startEventId, co.getStartTime());

        String parentPosition = HistoryPosition.getParentAsString(entry.getPosition());
        LogEntry le = new LogEntry(LogEntry.LogLevel.DETAIL, EventType.OrderForked, co.getStartTime(), null);
        le.onOrder(co, parentPosition, entry.getChilds());
        storeLog2File(le);

        for (FatForkedChild fc : entry.getChilds()) {
            orderForkedStarted(dbLayer, entry, co, fc, co.getStartTime());
        }
    }

    private void orderForkedStarted(DBLayerHistory dbLayer, FatEventOrderForked entry, CachedOrder parentOrder, FatForkedChild forkOrder,
            Date startTime) throws Exception {

        DBItemHistoryOrder item = new DBItemHistoryOrder();
        String constraintHash = null;

        try {
            checkControllerTimezone(dbLayer);

            item.setJobSchedulerId(controllerConfiguration.getCurrent().getId());
            item.setOrderKey(forkOrder.getOrderId());

            item.setWorkflowPath(entry.getWorkflowPath());
            item.setWorkflowVersionId(entry.getWorkflowVersionId());
            item.setWorkflowPosition(HistoryPosition.getParentAsString(entry.getPosition()));// TODO erweitern um branch
            item.setWorkflowFolder(HistoryUtil.getFolderFromPath(item.getWorkflowPath()));
            item.setWorkflowName(HistoryUtil.getBasenameFromPath(item.getWorkflowPath()));
            item.setWorkflowTitle(null);// TODO

            item.setMainParentId(parentOrder.getMainParentId());
            item.setParentId(parentOrder.getId());
            item.setParentOrderKey(parentOrder.getOrderKey());
            item.setHasChildren(false);
            item.setRetryCounter(HistoryPosition.getRetry(entry.getPosition()));

            item.setName(forkOrder.getBranchId());// TODO
            item.setTitle(null);// TODO

            item.setStartCause(OrderStartCause.fork.name());// TODO
            item.setStartTimePlanned(startTime);
            item.setStartTime(startTime);
            item.setStartWorkflowPosition(HistoryPosition.asString(entry.getPosition()));
            item.setStartEventId(String.valueOf(entry.getEventId()));
            item.setStartParameters(entry.getArgumentsAsJsonString()); // TODO or forkOrder arguments ???

            item.setCurrentOrderStepId(new Long(0L));

            item.setEndTime(null);
            item.setEndWorkflowPosition(null);
            item.setEndOrderStepId(new Long(0L));

            item.setSeverity(OrderStateText.RUNNING);
            item.setState(OrderStateText.RUNNING);
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

            constraintHash = hashOrderConstraint(entry.getEventId(), item.getOrderKey(), item.getWorkflowPosition());
            item.setConstraintHash(constraintHash);
            item.setCreated(new Date());
            item.setModified(item.getCreated());

            dbLayer.getSession().save(item);

            CachedOrder co = new CachedOrder(item);
            LogEntry le = new LogEntry(LogEntry.LogLevel.DETAIL, EventType.OrderStarted, startTime, null);
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
            LOGGER.warn(String.format("[%s][%s][%s][%s]%s", identifier, entry.getType(), entry.getOrderId(), forkOrder.getBranchId(), e.toString()),
                    e);
            LOGGER.warn(String.format("[%s][ConstraintViolation current item]%s", identifier, SOSHibernate.toString(item)));
            addCachedOrderByStateTime(dbLayer, forkOrder.getOrderId(), entry.getEventDatetime(), entry.getEventId(), constraintHash);
        }
    }

    private void orderJoined(DBLayerHistory dbLayer, FatEventOrderJoined entry, Map<String, CachedOrderStep> endedOrderSteps) throws Exception {
        checkControllerTimezone(dbLayer);

        Date endTime = entry.getEventDatetime();
        CachedOrder fco = null;

        for (FatForkedChild child : entry.getChilds()) {
            fco = orderUpdate(dbLayer, EventType.OrderJoined, entry.getEventId(), child.getOrderId(), endTime, entry.getOutcome(), endedOrderSteps,
                    true);
        }

        LogEntry le = new LogEntry(LogEntry.LogLevel.DETAIL, EventType.OrderJoined, HistoryUtil.getEventIdAsDate(entry.getEventId()), null);
        CachedOrder co = getCachedOrder(dbLayer, entry.getOrderId(), entry.getEventDatetime(), entry.getEventId());
        le.onOrderJoined(co, fco.getWorkflowPosition(), entry.getChilds().stream().map(s -> s.getOrderId()).collect(Collectors.toList()), entry
                .getOutcome());
        storeLog2File(le);
    }

    private void orderStepStarted(DBLayerHistory dbLayer, FatEventOrderStepStarted entry) throws Exception {
        CachedAgent ca = null;
        CachedOrder co = null;
        CachedOrderStep cos = null;
        DBItemHistoryOrderStep item = null;
        String constraintHash = null;

        try {
            checkControllerTimezone(dbLayer);

            ca = getCachedAgent(dbLayer, entry.getAgentPath());
            co = getCachedOrder(dbLayer, entry.getOrderId(), entry.getEventDatetime(), entry.getEventId());

            Date agentStartTime = entry.getEventDatetime();

            item = new DBItemHistoryOrderStep();
            item.setJobSchedulerId(controllerConfiguration.getCurrent().getId());
            item.setOrderKey(entry.getOrderId());

            item.setWorkflowPath(entry.getWorkflowPath());
            item.setWorkflowVersionId(entry.getWorkflowVersionId());
            item.setWorkflowPosition(HistoryPosition.asString(entry.getPosition()));
            item.setWorkflowFolder(HistoryUtil.getFolderFromPath(item.getWorkflowPath()));
            item.setWorkflowName(HistoryUtil.getBasenameFromPath(item.getWorkflowPath()));

            item.setMainOrderId(co.getMainParentId());
            item.setOrderId(co.getId());
            item.setPosition(HistoryPosition.getLast(entry.getPosition()));
            item.setRetryCounter(HistoryPosition.getRetry(entry.getPosition()));

            item.setJobName(entry.getJobName());
            item.setJobTitle(null);// TODO
            item.setCriticality(JobCriticality.NORMAL);// TODO

            item.setAgentPath(entry.getAgentPath());
            item.setAgentUri(ca.getUri());

            item.setStartCause(OrderStepStartCause.order.name());// TODO
            item.setStartTime(agentStartTime);
            item.setStartEventId(String.valueOf(entry.getEventId()));
            item.setStartParameters(entry.getArgumentsAsJsonString());// TODO check

            item.setEndTime(null);
            item.setEndEventId(null);

            item.setReturnCode(null);
            item.setSeverity(OrderStateText.RUNNING);

            item.setError(false);
            item.setErrorCode(null);
            item.setErrorText(null);

            item.setLogId(new Long(0));

            constraintHash = hashOrderStepConstraint(entry.getEventId(), item.getOrderKey(), item.getWorkflowPosition());
            item.setConstraintHash(constraintHash);
            item.setCreated(new Date());
            item.setModified(item.getCreated());

            dbLayer.getSession().save(item);

            co.setCurrentOrderStepId(item.getId());
            if (SOSDate.equals(co.getStartTime(), Globals.HISTORY_DEFAULT_DATE)) {
                // if (item.getWorkflowPosition().equals(co.getStartWorkflowPosition())) {// + order.startTime != default
                // ORDER START
                co.setState(OrderStateText.RUNNING.intValue());
                co.setStartTime(item.getStartTime());

                dbLayer.updateOrderOnOrderStep(co.getId(), item.getStartTime(), item.getStartEventId(), co.getState(), entry.getEventDatetime(), co
                        .getCurrentOrderStepId());

                if (co.getHasStates()) {
                    saveOrderState(dbLayer, co, co.getState(), item.getStartTime(), entry.getEventId(), null, null);
                }

                LogEntry logEntry = new LogEntry(LogEntry.LogLevel.MAIN, EventType.OrderStarted, HistoryUtil.getEventIdAsDate(entry.getEventId()),
                        agentStartTime);
                logEntry.onOrder(co, item.getWorkflowPosition());
                logEntry.setAgentTimezone(ca.getTimezone());
                storeLog2File(logEntry);
            } else {
                dbLayer.updateOrderOnOrderStep(co.getId(), co.getCurrentOrderStepId());
            }
            cos = new CachedOrderStep(item, ca.getTimezone());
            LogEntry le = new LogEntry(LogEntry.LogLevel.MAIN, EventType.OrderProcessingStarted, HistoryUtil.getEventIdAsDate(entry.getEventId()),
                    agentStartTime);
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
            LOGGER.warn(String.format("[%s][%s][%s]%s", identifier, entry.getType(), entry.getOrderId(), e.toString()), e);
            LOGGER.warn(String.format("[%s][ConstraintViolation current item]%s", identifier, SOSHibernate.toString(item)));
            if (co != null) {
                addCachedOrder(co.getOrderKey(), co);
            }
            addCachedOrderStepByStartTime(dbLayer, ca, entry.getOrderId(), entry.getEventDatetime(), entry.getEventId(), constraintHash);
        }
    }

    private void saveOrderState(DBLayerHistory dbLayer, CachedOrder co, Integer state, Date stateDate, Long stateEventId, String stateCode,
            String stateText) throws Exception {
        DBItemHistoryOrderState item = new DBItemHistoryOrderState();
        item.setMainParentId(co.getMainParentId());
        item.setParentId(co.getParentId());
        item.setOrderId(co.getId());
        item.setState(state);
        item.setStateTime(stateDate);
        item.setStateEventId(String.valueOf(stateEventId));
        item.setStateCode(stateCode);
        item.setStateText(stateText);
        item.setCreated(new Date());
        dbLayer.getSession().save(item);
    }

    private void orderStepProcessed(DBLayerHistory dbLayer, FatEventOrderStepProcessed entry, Map<String, CachedOrderStep> endedOrderSteps)
            throws Exception {
        CachedOrderStep cos = getCachedOrderStep(dbLayer, entry.getOrderId(), entry.getEventDatetime(), entry.getEventId());
        if (cos.getEndTime() == null) {
            checkControllerTimezone(dbLayer);
            cos.setEndTime(entry.getEventDatetime());

            LogEntry le = new LogEntry(LogEntry.LogLevel.MAIN, EventType.OrderProcessed, HistoryUtil.getEventIdAsDate(entry.getEventId()), cos
                    .getEndTime());
            if (entry.getOutcome() != null) {
                cos.setReturnCode(entry.getOutcome().getReturnCode());
                le.setReturnCode(cos.getReturnCode());
                if (entry.getOutcome().isFailed()) {
                    le.setError(OrderStateText.FAILED.value(), entry.getOutcome());
                }
            }
            if (le.isError() && SOSString.isEmpty(le.getErrorText())) {
                le.setErrorText(cos.getStdErr());
            }
            Integer severity = HistorySeverity.map2DbSeverity(le.isError() ? OrderStateText.FAILED : OrderStateText.FINISHED);
            dbLayer.setOrderStepEnd(cos.getId(), cos.getEndTime(), String.valueOf(entry.getEventId()), entry.getOutcome()
                    .getNamedValuesAsJsonString(), le.getReturnCode(), severity, le.isError(), le.getErrorState(), le.getErrorReason(), le
                            .getErrorCode(), le.getErrorText(), new Date());
            le.onOrderStep(cos);

            Path log = storeLog2File(le);
            DBItemHistoryLog logItem = storeLogFile2Db(dbLayer, cos.getMainOrderId(), cos.getOrderId(), cos.getId(), true, log);
            if (logItem != null) {
                dbLayer.setOrderStepLogId(cos.getId(), logItem.getId());
                if (cleanupLogFiles) {
                    Files.delete(log);
                }
            }
            endedOrderSteps.put(entry.getOrderId(), cos);

            tryStoreCurrentState(dbLayer, entry.getEventId());
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][skip][%s]order step is already ended[%s]", identifier, entry.getType(), entry.getOrderId(),
                        SOSString.toString(cos)));
            }
        }
        clearCache(CacheType.orderStep, cos.getOrderKey());
    }

    private void orderStepStd(DBLayerHistory dbLayer, FatEventOrderStepStdWritten entry, EventType eventType) throws Exception {
        CachedOrderStep cos = getCachedOrderStep(dbLayer, entry.getOrderId(), entry.getEventDatetime(), entry.getEventId());
        if (EventType.OrderStderrWritten.equals(eventType)) {
            cos.setStdError(entry.getChunck());
        }
        if (cos.getEndTime() == null) {
            LogEntry le = new LogEntry(LogEntry.LogLevel.INFO, eventType, HistoryUtil.getEventIdAsDate(entry.getEventId()), entry.getEventDatetime());

            le.onOrderStep(cos, entry.getChunck());
            storeLog2File(le, cos);

            tryStoreCurrentState(dbLayer, entry.getEventId());
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][skip][%s]order step is already ended. log already written...[%s]", identifier, entry.getType(),
                        entry.getOrderId(), SOSString.toString(cos)));
            }
        }
    }

    private void addCachedOrder(String key, CachedOrder order) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][addCachedOrder][%s]%s", identifier, key, SOSString.toString(order)));
        }
        cachedOrders.put(key, order);
    }

    private CachedOrder getCachedOrder(DBLayerHistory dbLayer, String key, Date currentEventTime, Long currentEventId) throws Exception {
        CachedOrder co = getCachedOrder(key);
        if (co == null) {
            DBItemHistoryOrder item = dbLayer.getOrderBeforeCurrentEvent(controllerConfiguration.getCurrent().getId(), key, currentEventTime);
            if (item == null) {
                throw new FatEventOrderNotFoundException(String.format("[%s][%s][%s]order not found", identifier, key, currentEventId), key,
                        currentEventId);
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

    private void addCachedOrderByStateTime(DBLayerHistory dbLayer, String key, Date stateTime, Long stateEventId, String constraintHash)
            throws Exception {
        DBItemHistoryOrder item = null;
        if (constraintHash != null) {
            item = dbLayer.getOrderByConstraint(constraintHash);
            if (item == null) {
                LOGGER.warn(String.format("[%s][%s][%s][%s]order by constraintHash not found, try to find by state time...", identifier, key,
                        stateEventId, constraintHash));
            }
        }
        if (item == null) {
            item = dbLayer.getOrderByStateTime(controllerConfiguration.getCurrent().getId(), key, stateTime);
        }
        if (item == null) {
            throw new FatEventOrderNotFoundException(String.format("[%s][%s][%s]order not found", identifier, key, stateEventId), key, stateEventId);
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

    private CachedOrderStep getCachedOrderStep(DBLayerHistory dbLayer, String key, Date currentEventTime, Long currentEventId) throws Exception {
        CachedOrderStep co = getCachedOrderStep(key);
        if (co == null) {
            DBItemHistoryOrderStep item = dbLayer.getOrderStepLastBeforeCurrentEvent(controllerConfiguration.getCurrent().getId(), key,
                    currentEventTime, currentEventId);
            if (item == null) {
                throw new FatEventOrderStepNotFoundException(String.format("[%s][%s]order step not found. orderKey=%s", identifier, currentEventId,
                        key), key, currentEventId);
            } else {
                DBItemHistoryAgent agent = dbLayer.getAgent(controllerConfiguration.getCurrent().getId(), item.getAgentPath());
                if (agent == null) {
                    LOGGER.warn(String.format(
                            "[%s][agent is null]agent timezone can't be identified. set agent log timezone to controller timezone ...", item
                                    .getAgentPath()));
                    co = new CachedOrderStep(item, controllerTimezone);
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

    private void addCachedOrderStepByStartTime(DBLayerHistory dbLayer, CachedAgent agent, String key, Date startTime, Long startEventId,
            String constraintHash) throws Exception {

        DBItemHistoryOrderStep item = null;
        if (constraintHash != null) {
            item = dbLayer.getOrderStepByConstraint(constraintHash);
            if (item == null) {
                LOGGER.warn(String.format("[%s][%s][%s][%s]order step by constraintHash not found, try to find by start time...", identifier, key,
                        startEventId, constraintHash));
            }
        }
        if (item == null) {
            item = dbLayer.getOrderStepByStartTime(controllerConfiguration.getCurrent().getId(), key, startTime, startEventId);
        }
        if (item == null) {
            throw new FatEventOrderStepNotFoundException(String.format("[%s]order step not found. orderKey=%s, startEventId=%s", identifier, key,
                    startEventId), key, startEventId);
        } else {
            if (agent == null) {
                LOGGER.warn(String.format(
                        "[%s][agent not found]agent timezone can't be identified. set agent log timezone to controller timezone ...", item
                                .getAgentPath()));
                addCachedOrderStep(key, new CachedOrderStep(item, controllerTimezone));
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
            DBItemHistoryAgent item = dbLayer.getAgent(controllerConfiguration.getCurrent().getId(), key);
            if (item == null) {
                LOGGER.warn(String.format("[%s][%s][%s]agent not found in the history. try to find in the agent instances...", identifier,
                        controllerConfiguration.getCurrent().getId(), key));
                DBItemInventoryAgentInstance inst = dbLayer.getAgentInstance(controllerConfiguration.getCurrent().getId(), key);
                // TODO read from controller API?
                if (inst == null) {
                    Date startTime = new Date();
                    String uri = "http://localhost:4445";

                    LOGGER.warn(String.format(
                            "[%s][%s][%s]agent not found in the agent instances. set agent timezone to controller timezone=%s, start time=%s, uri=%s",
                            identifier, controllerConfiguration.getCurrent().getId(), key, controllerTimezone, getDateAsString(startTime), uri));

                    item = new DBItemHistoryAgent();
                    item.setJobSchedulerId(controllerConfiguration.getCurrent().getId());
                    item.setPath(key);
                    item.setUri(uri);
                    item.setTimezone(controllerTimezone);
                    item.setStartTime(startTime);
                    item.setEventId(String.valueOf(HistoryUtil.getDateAsEventId(startTime)));
                    item.setCreated(new Date());
                } else {
                    Date startTime = inst.getStartedAt();
                    if (startTime == null) {
                        startTime = new Date();
                    }
                    LOGGER.info(String.format(
                            "[%s][%s][%s]agent found in the agent instances. set agent timezone to controller timezone=%s, start time=%s", identifier,
                            controllerConfiguration.getCurrent().getId(), key, controllerTimezone, getDateAsString(startTime)));

                    item = new DBItemHistoryAgent();
                    item.setJobSchedulerId(controllerConfiguration.getCurrent().getId());
                    item.setPath(key);
                    item.setUri(inst.getUri());
                    item.setTimezone(controllerTimezone);
                    item.setStartTime(startTime);
                    item.setEventId(String.valueOf(HistoryUtil.getDateAsEventId(startTime)));
                    item.setCreated(new Date());
                }
                dbLayer.getSession().save(item);
            }

            co = new CachedAgent(item);
            addCachedAgent(key, co);
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

    private DBItemHistoryLog storeLogFile2Db(DBLayerHistory dbLayer, Long mainOrderId, Long orderId, Long orderStepId, boolean compressed, Path file)
            throws Exception {

        DBItemHistoryLog item = null;
        if (Files.exists(file)) {
            item = new DBItemHistoryLog();
            item.setJobSchedulerId(controllerConfiguration.getCurrent().getId());

            item.setMainOrderId(mainOrderId);
            item.setOrderId(orderId);
            item.setOrderStepId(orderStepId);
            item.setCompressed(compressed);

            item.setFileBasename(SOSPath.getFileNameWithoutExtension(file.getFileName()));
            item.setFileSizeUncomressed(Files.size(file));
            item.setFileLinesUncomressed(SOSPath.getLineCount(file));

            if (item.getCompressed()) {// task
                item.setFileContent(SOSPath.gzipFile(file));
            } else {// order
                item.setFileContent(SOSPath.readFile(file, Collectors.joining(",", "[", "]")).getBytes(StandardCharsets.UTF_8));
            }

            item.setCreated(new Date());

            dbLayer.getSession().save(item);
        } else {
            LOGGER.error(String.format("[%s][%s]file not found", identifier, file.toString()));
        }
        return item;
    }

    private Path getOrderLog(Path dir, Long orderId) {
        // return dir.resolve(entry.getMainOrderId() + ".log");
        return dir.resolve(orderId + ".log");
    }

    private Path getOrderStepLog(Path dir, LogEntry entry) {
        return dir.resolve(entry.getOrderId() + "_" + entry.getOrderStepId() + ".log");
    }

    private Path getOrderLogDirectory(LogEntry entry) {
        return HistoryMain.getOrderLogDirectory(Paths.get(historyConfiguration.getLogDir()), entry.getMainOrderId());
        // return Paths.get(historyConfiguration.getLogDir(), String.valueOf(entry.getMainOrderId()));
    }

    private OrderLogEntry createOrderLogEntry(LogEntry logEntry) {
        OrderLogEntry entry = new OrderLogEntry();
        entry.setOrderId(logEntry.getOrderKey());
        entry.setLogLevel(logEntry.getLogLevel().name());
        entry.setLogEvent(logEntry.getEventType());
        entry.setPosition(SOSString.isEmpty(logEntry.getPosition()) ? null : logEntry.getPosition());
        entry.setReturnCode(logEntry.getReturnCode() == null ? null : logEntry.getReturnCode().longValue());// TODO change to Integer
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

    private String getDateAsString(Date date) throws Exception {
        return SOSDate.getDateAsString(date, "yyyy-MM-dd HH:mm:ss.SSSZZZZ");
    }

    private Path storeLog2File(LogEntry entry) throws Exception {
        return storeLog2File(entry, null);
    }

    private Path storeLog2File(LogEntry entry, CachedOrderStep cos) throws Exception {

        OrderLogEntry orderEntry = null;
        StringBuilder content = new StringBuilder();
        StringBuilder orderEntryContent = null;
        Path dir = getOrderLogDirectory(entry);
        Path file = null;
        boolean newLine;
        boolean append;

        switch (entry.getEventType()) {
        case OrderProcessingStarted:
            // order log
            newLine = true;
            orderEntry = createOrderLogEntry(entry);
            orderEntry.setControllerDatetime(getDateAsString(entry.getControllerDatetime(), controllerTimezone));
            orderEntry.setAgentDatetime(getDateAsString(entry.getAgentDatetime(), entry.getAgentTimezone()));
            orderEntry.setAgentPath(entry.getAgentPath());
            orderEntry.setAgentUrl(entry.getAgentUri());
            orderEntry.setJob(entry.getJobName());
            orderEntry.setTaskId(entry.getOrderStepId());
            orderEntryContent = new StringBuilder((new ObjectMapper()).writeValueAsString(orderEntry));
            write2file(getOrderLog(dir, entry.getOrderId()), orderEntryContent, newLine);

            // task log
            file = getOrderStepLog(dir, entry);
            content.append(getDateAsString(entry.getAgentDatetime(), entry.getAgentTimezone())).append(" ");
            content.append("[").append(entry.getLogLevel().name()).append("]    ");
            content.append(entry.getChunk());
            break;
        case OrderProcessed:
            // order log
            newLine = true;
            orderEntry = createOrderLogEntry(entry);
            orderEntry.setControllerDatetime(getDateAsString(entry.getControllerDatetime(), controllerTimezone));
            orderEntry.setAgentDatetime(getDateAsString(entry.getAgentDatetime(), entry.getAgentTimezone()));
            // orderEntry.setAgentPath(entry.getAgentPath());
            // orderEntry.setAgentUrl(entry.getAgentUri());
            orderEntry.setJob(entry.getJobName());
            orderEntry.setTaskId(entry.getOrderStepId());
            orderEntryContent = new StringBuilder((new ObjectMapper()).writeValueAsString(orderEntry));
            write2file(getOrderLog(dir, entry.getOrderId()), orderEntryContent, newLine);

            // task log
            file = getOrderStepLog(dir, entry);
            content.append(getDateAsString(entry.getAgentDatetime(), entry.getAgentTimezone())).append(" ");
            content.append("[").append(entry.getLogLevel().name()).append("]    ");
            content.append(entry.getChunk());
            break;

        case OrderStdoutWritten:
        case OrderStderrWritten:
            newLine = false;
            append = false;
            file = getOrderStepLog(dir, entry);

            if (cos.isLastStdEndsWithNewLine() == null) {
                try {
                    if (SOSPath.endsWithNewLine(file)) {
                        append = true;
                    }
                } catch (FileNotFoundException e) {
                    LOGGER.error(String.format("[%s]%s", file, e.toString()));
                }
            } else {
                if (cos.isLastStdEndsWithNewLine().booleanValue()) {
                    append = true;
                }
            }

            if (append) {
                String outType = entry.getEventType().equals(EventType.OrderStdoutWritten) ? "STDOUT" : "STDERR";
                content.append(getDateAsString(entry.getAgentDatetime(), entry.getAgentTimezone())).append(" ");
                content.append("[").append(outType).append("]  ");
            }
            cos.setLastStdEndsWithNewLine(entry.getChunk().endsWith("\n"));
            content.append(entry.getChunk());
            break;
        case OrderAdded:
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        default:
            // order log
            newLine = true;
            file = getOrderLog(dir, entry.getOrderId());

            orderEntry = createOrderLogEntry(entry);
            orderEntry.setControllerDatetime(getDateAsString(entry.getControllerDatetime(), controllerTimezone));
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

        if (orderEntry != null && !entry.getOrderId().equals(entry.getMainOrderId())) {
            write2MainOrderLog(entry, dir, (orderEntryContent == null ? content : orderEntryContent), newLine);
        }

        return file;
    }

    private void write2MainOrderLog(LogEntry entry, Path dir, StringBuilder content, boolean newLine) throws Exception {
        Path file = getOrderLog(dir, entry.getMainOrderId());
        try {
            write2file(file, content, newLine);
        } catch (Exception e) {
            LOGGER.error(String.format("[%s][%s][%s][%s]%s", identifier, entry.getEventType().value(), entry.getOrderKey(), file, e.toString()), e);
            throw e;
        }
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

    private String hashOrderConstraint(Long eventId, String orderKey, String workflowPosition) {
        return SOSString.hash(new StringBuilder(controllerConfiguration.getCurrent().getId()).append(String.valueOf(eventId)).append(orderKey).append(
                workflowPosition).toString());
    }

    private String hashOrderStepConstraint(Long eventId, String orderKey, String workflowPosition) {
        return hashOrderConstraint(eventId, orderKey, workflowPosition);
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

    public ControllerConfiguration getControllerConfiguration() {
        return controllerConfiguration;
    }

    public void setIdentifier(String val) {
        identifier = val;
    }

    public HistoryConfiguration getHistoryConfiguration() {
        return historyConfiguration;
    }
}
