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
import com.sos.controller.model.event.EventType;
import com.sos.inventory.model.job.JobCriticality;
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
import com.sos.joc.model.order.OrderStateText;
import com.sos.js7.event.controller.EventMeta;
import com.sos.js7.event.controller.configuration.controller.ControllerConfiguration;
import com.sos.js7.history.controller.HistoryMain;
import com.sos.js7.history.controller.configuration.HistoryConfiguration;
import com.sos.js7.history.controller.exception.FatEventOrderNotFoundException;
import com.sos.js7.history.controller.exception.FatEventOrderStepNotFoundException;
import com.sos.js7.history.controller.proxy.HistoryEventType;
import com.sos.js7.history.controller.proxy.fatevent.AFatEvent;
import com.sos.js7.history.controller.proxy.fatevent.AFatEventOrderLock;
import com.sos.js7.history.controller.proxy.fatevent.AFatEventOrderProcessed;
import com.sos.js7.history.controller.proxy.fatevent.FatEventAgentCouplingFailed;
import com.sos.js7.history.controller.proxy.fatevent.FatEventAgentReady;
import com.sos.js7.history.controller.proxy.fatevent.FatEventControllerReady;
import com.sos.js7.history.controller.proxy.fatevent.FatEventControllerShutDown;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderCancelled;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderForked;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderJoined;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderResumed;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderStarted;
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
import com.sos.webservices.json.jobscheduler.history.order.Lock;
import com.sos.webservices.json.jobscheduler.history.order.LockState;
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
                    case ControllerShutDown:
                        controllerShutDown(dbLayer, (FatEventControllerShutDown) entry);
                        counter.getController().addShutdown();
                        break;
                    case AgentCouplingFailed:
                        agentCouplingFailed(dbLayer, (FatEventAgentCouplingFailed) entry);
                        counter.getAgent().addCouplingFailed();
                        break;
                    case AgentReady:
                        agentReady(dbLayer, (FatEventAgentReady) entry);
                        counter.getAgent().addReady();
                        break;
                    case OrderStarted:
                        orderStarted(dbLayer, (FatEventOrderStarted) entry);
                        counter.getOrder().addStarted();
                        break;
                    case OrderResumed:
                        orderResumed(dbLayer, (FatEventOrderResumed) entry);
                        counter.getOrder().addResumed();
                        break;
                    case OrderResumeMarked:
                        orderLog(dbLayer, (AFatEventOrderProcessed) entry, EventType.OrderResumeMarked);
                        counter.getOrder().addResumeMarked();
                        break;
                    case OrderSuspendMarked:
                        // orderNotCompleted(dbLayer, (AFatEventOrderProcessed) entry, EventType.OrderSuspendMarked, endedOrderSteps);
                        orderLog(dbLayer, (AFatEventOrderProcessed) entry, EventType.OrderSuspendMarked);
                        counter.getOrder().addSuspendMarked();
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
                    case OrderCancelled:
                        FatEventOrderCancelled oc = (FatEventOrderCancelled) entry;
                        if (oc.isStarted() != null && !oc.isStarted()) {
                            counter.getOrder().addCancelledNotStarted();
                        } else {
                            orderProcessed(dbLayer, oc, EventType.OrderCancelled, endedOrderSteps);
                            counter.getOrder().addCancelled();
                        }
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
                    case OrderLockAcquired:
                        orderLock(dbLayer, (AFatEventOrderLock) entry, EventType.OrderLockAcquired);
                        counter.getOrder().addLockAcquired();
                        break;
                    case OrderLockQueued:
                        orderLock(dbLayer, (AFatEventOrderLock) entry, EventType.OrderLockQueued);
                        counter.getOrder().addLockQueued();
                        break;
                    case OrderLockReleased:
                        orderLock(dbLayer, (AFatEventOrderLock) entry, EventType.OrderLockReleased);
                        counter.getOrder().addLockReleased();
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
            item.setControllerId(controllerConfiguration.getCurrent().getId());
            item.setUri(controllerConfiguration.getCurrent().getUri());
            item.setTimezone(entry.getTimezone());
            item.setReadyTime(eventDate);
            item.setIsPrimary(controllerConfiguration.getCurrent().isPrimary());// TODO
            item.setReadyEventId(String.valueOf(entry.getEventId()));
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

    private void controllerShutDown(DBLayerHistory dbLayer, FatEventControllerShutDown entry) throws Exception {
        DBItemHistoryController item = dbLayer.getControllerByShutDownEventId(controllerConfiguration.getCurrent().getId(), String.valueOf(entry
                .getEventId()));
        if (item == null) {
            LOGGER.warn(String.format("[%s][%s][%s][skip]not found controller entry with the ready time < %s", identifier, entry.getType(),
                    controllerConfiguration.getCurrent().getId(), getDateAsString(entry.getEventDatetime())));
        } else {
            if (item.getShutdownTime() == null) {
                item.setShutdownTime(entry.getEventDatetime());
                dbLayer.getSession().update(item);
            } else {
                LOGGER.info(String.format("[%s][%s][%s][skip]found with the ready time < %s and shutdown time=%s", identifier, entry.getType(),
                        controllerConfiguration.getCurrent().getId(), getDateAsString(entry.getEventDatetime()), getDateAsString(item
                                .getShutdownTime())));
            }
            if (controllerTimezone == null) {
                controllerTimezone = item.getTimezone();
            }
        }
        tryStoreCurrentState(dbLayer, entry.getEventId());
    }

    private void checkControllerTimezone(DBLayerHistory dbLayer) throws Exception {
        if (controllerTimezone == null) {
            controllerTimezone = dbLayer.getLastControllerTimezone(controllerConfiguration.getCurrent().getId());
            if (controllerTimezone == null) {
                // TODO read from controller api, and instances
                // throw new Exception(String.format("controller not found: %s", controllerConfiguration.getCurrent().getId()));
                LOGGER.warn(String.format("[%s][%s]controller not found in the history. set controller timezone=UTC", identifier,
                        controllerConfiguration.getCurrent().getId()));
                controllerTimezone = "UTC";
            }
        }
    }

    private void agentCouplingFailed(DBLayerHistory dbLayer, FatEventAgentCouplingFailed entry) throws Exception {
        DBItemHistoryAgent item = dbLayer.getAgentByCouplingFailedEventId(controllerConfiguration.getCurrent().getId(), entry.getId(), String.valueOf(
                entry.getEventId()));
        if (item == null) {
            LOGGER.warn(String.format("[%s][%s][%s][skip]not found agent entry with the ready time < %s", identifier, entry.getType(), entry.getId(),
                    getDateAsString(entry.getEventDatetime())));
        } else {
            if (item.getCouplingFailedTime() == null) {
                item.setCouplingFailedTime(entry.getEventDatetime());
                dbLayer.getSession().update(item);
            } else {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s][skip]found with the ready time < %s and coupling failed time=%s", identifier, entry
                            .getType(), entry.getId(), getDateAsString(entry.getEventDatetime()), getDateAsString(item.getCouplingFailedTime())));
                }
            }
        }
        tryStoreCurrentState(dbLayer, entry.getEventId());
    }

    private void agentReady(DBLayerHistory dbLayer, FatEventAgentReady entry) throws Exception {
        DBItemHistoryAgent item = new DBItemHistoryAgent();

        try {
            checkControllerTimezone(dbLayer);

            item.setControllerId(controllerConfiguration.getCurrent().getId());
            item.setAgentId(entry.getId());
            item.setUri(entry.getUri());
            item.setTimezone(entry.getTimezone());
            item.setReadyTime(entry.getEventDatetime());
            item.setReadyEventId(String.valueOf(entry.getEventId()));
            item.setCouplingFailedTime(null);
            item.setCreated(new Date());

            dbLayer.getSession().save(item);

            tryStoreCurrentState(dbLayer, entry.getEventId());
            addCachedAgent(item.getAgentId(), new CachedAgent(item));
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                LOGGER.error(e.toString(), e);
                throw e;
            }
            LOGGER.warn(String.format("[%s][%s][%s]%s", identifier, entry.getType(), entry.getId(), e.toString()), e);
            LOGGER.warn(String.format("[%s][ConstraintViolation item]%s", identifier, SOSHibernate.toString(item)));
            addCachedAgentByReadyEventId(dbLayer, entry.getId(), entry.getEventId());
        }
    }

    private void orderStarted(DBLayerHistory dbLayer, FatEventOrderStarted entry) throws Exception {
        DBItemHistoryOrder item = new DBItemHistoryOrder();
        String constraintHash = null;

        try {
            checkControllerTimezone(dbLayer);

            item.setControllerId(controllerConfiguration.getCurrent().getId());
            item.setOrderId(entry.getOrderId());

            item.setWorkflowPath(entry.getWorkflowPath());
            item.setWorkflowVersionId(entry.getWorkflowVersionId());
            item.setWorkflowPosition(SOSString.isEmpty(entry.getPosition()) ? "0" : entry.getPosition());
            item.setWorkflowFolder(HistoryUtil.getFolderFromPath(item.getWorkflowPath()));
            item.setWorkflowName(HistoryUtil.getBasenameFromPath(item.getWorkflowPath()));
            item.setWorkflowTitle(null);// TODO

            item.setMainParentId(new Long(0L));// TODO see below
            item.setParentId(new Long(0L));
            item.setParentOrderId(null);
            item.setHasChildren(false);
            item.setRetryCounter(HistoryPosition.getRetry(entry.getPosition()));

            item.setName(entry.getOrderId());
            item.setTitle(null);// TODO

            item.setStartCause(OrderStartCause.order.name());// TODO

            item.setStartTimePlanned(entry.getPlanned() == null ? entry.getEventDatetime() : entry.getPlanned());
            item.setStartTime(entry.getEventDatetime());
            item.setStartWorkflowPosition(item.getWorkflowPosition());
            item.setStartEventId(String.valueOf(entry.getEventId()));
            item.setStartParameters(entry.getArgumentsAsJsonString());

            item.setCurrentHistoryOrderStepId(new Long(0));

            item.setEndTime(null);
            item.setEndWorkflowPosition(null);
            item.setEndHistoryOrderStepId(new Long(0));

            item.setSeverity(OrderStateText.RUNNING);
            item.setState(OrderStateText.RUNNING);

            item.setStateTime(entry.getEventDatetime());
            item.setStateText(null);
            item.setHasStates(false);

            item.setError(false);
            item.setErrorState(null);
            item.setErrorReason(null);
            item.setErrorReturnCode(null);
            item.setErrorCode(null);
            item.setErrorText(null);
            item.setEndEventId(null);

            item.setLogId(new Long(0));

            constraintHash = hashOrderConstraint(entry.getEventId(), item.getOrderId(), item.getWorkflowPosition());
            item.setConstraintHash(constraintHash);
            item.setCreated(new Date());
            item.setModified(item.getCreated());

            dbLayer.getSession().save(item);

            item.setMainParentId(item.getId()); // TODO see above
            dbLayer.setMainParentId(item.getId(), item.getMainParentId());

            CachedOrder co = new CachedOrder(item);
            LogEntry le = new LogEntry(LogEntry.LogLevel.MAIN, EventType.OrderStarted, entry.getEventDatetime(), null);
            le.onOrder(co, item.getWorkflowPosition());
            storeLog2File(le);
            addCachedOrder(item.getOrderId(), co);

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
        orderUpdate(dbLayer, eventType, entry.getEventId(), entry.getOrderId(), entry.getEventDatetime(), entry.getOutcome(), entry.getPosition(),
                endedOrderSteps, true);
    }

    private void orderNotCompleted(DBLayerHistory dbLayer, AFatEventOrderProcessed entry, EventType eventType,
            Map<String, CachedOrderStep> endedOrderSteps) throws Exception {
        orderUpdate(dbLayer, eventType, entry.getEventId(), entry.getOrderId(), entry.getEventDatetime(), entry.getOutcome(), entry.getPosition(),
                endedOrderSteps, false);
    }

    private CachedOrder orderUpdate(DBLayerHistory dbLayer, EventType eventType, Long eventId, String orderId, Date eventDate, FatOutcome outcome,
            String position, Map<String, CachedOrderStep> endedOrderSteps, boolean completeOrder) throws Exception {

        CachedOrder co = null;
        if (EventType.OrderCancelled.equals(eventType)) {
            try {
                co = getCachedOrder(dbLayer, orderId, eventDate, eventId);
            } catch (FatEventOrderNotFoundException e) {
                return null;
            }
        } else {
            co = getCachedOrder(dbLayer, orderId, eventDate, eventId);
        }

        if (co.getEndTime() == null) {
            checkControllerTimezone(dbLayer);

            CachedOrderStep cos = getCurrentOrderStep(dbLayer, co, endedOrderSteps);
            LogEntry le = createOrderLogEntry(eventId, outcome, cos, eventType);

            Date endTime = null;
            String endWorkflowPosition = null;
            Long endHistoryOrderStepId = null;
            String endEventId = null;
            if (completeOrder) {
                endTime = eventDate;
                endWorkflowPosition = (cos == null) ? co.getWorkflowPosition() : cos.getWorkflowPosition();
                endHistoryOrderStepId = (cos == null) ? co.getCurrentHistoryOrderStepId() : cos.getId();
                endEventId = String.valueOf(eventId);
            }

            if (le.isError() && SOSString.isEmpty(le.getErrorText())) {
                if (cos != null && cos.getError() != null && !SOSString.isEmpty(cos.getStdErr())) {
                    // le.setErrorText(cos.getStdErr());
                    le.setErrorText(String.format("[%s][%s]%s", cos.getJobName(), cos.getWorkflowPosition(), cos.getStdErr()));
                }
            }
            Date startTime = null;
            String startEventId = null;
            String orderErrorText = le.getErrorText();
            if (cos == null) {
                // orderErrorText = SOSString.isEmpty(le.getErrorText()) ? null : le.getErrorText();
            } else {
                // orderErrorText = SOSString.isEmpty(le.getErrorText()) ? null : String.format("[%s][%s]%s", cos.getJobName(), cos
                // .getWorkflowPosition(), le.getErrorText());
            }

            String stateErrorText = null;
            switch (eventType) {
            case OrderJoined:
                if (le.isError()) {
                    co.setHasStates(true);
                }
                le.setLogLevel(LogLevel.DETAIL);
                break;
            case OrderBroken:
            case OrderCancelled:
            case OrderSuspended:
            case OrderSuspendMarked:
            case OrderResumed:
            case OrderResumeMarked:
                co.setHasStates(true);
                break;
            case OrderFailed:
                co.setHasStates(true);
                stateErrorText = orderErrorText;
                break;
            default:
                break;
            }
            dbLayer.setOrderEnd(co.getId(), endTime, endWorkflowPosition, endHistoryOrderStepId, endEventId, le.getState(), eventDate, co
                    .getHasStates(), le.isError(), le.getErrorState(), le.getErrorReason(), le.getReturnCode(), le.getErrorCode(), orderErrorText,
                    startTime, startEventId);

            if (co.getHasStates()) {
                saveOrderState(dbLayer, co, le.getState(), eventDate, eventId, le.getErrorCode(), stateErrorText);
            }

            le.onOrder(co, position == null ? co.getWorkflowPosition() : position);
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
                clearCache(CacheType.order, orderId);
            }
            tryStoreCurrentState(dbLayer, eventId);
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][skip][%s]order is already completed[%s]", identifier, eventType, orderId, SOSString.toString(
                        co)));
            }
            clearCache(CacheType.order, co.getOrderId());
        }
        return co;
    }

    private CachedOrderStep getCurrentOrderStep(DBLayerHistory dbLayer, CachedOrder co, Map<String, CachedOrderStep> endedOrderSteps)
            throws Exception {
        CachedOrderStep step = null;
        if (co.getCurrentHistoryOrderStepId().longValue() > 0L) {// forked = 0
            step = endedOrderSteps.get(co.getOrderId());
            if (step == null || !step.getId().equals(co.getCurrentHistoryOrderStepId())) {
                if (step != null) {
                    if (isDebugEnabled)
                        LOGGER.debug(String.format("[%s][%s][currentStep id mismatch]orderCurrentStepId=%s != cachedStepId=%s", identifier, co
                                .getOrderId(), co.getCurrentHistoryOrderStepId(), step.getId()));
                    step = null;
                }
                DBItemHistoryOrderStep item = dbLayer.getOrderStep(co.getCurrentHistoryOrderStepId());
                if (item == null) {
                    LOGGER.warn(String.format("[%s][%s][currentStep not found]id=%s", identifier, co.getOrderId(), co
                            .getCurrentHistoryOrderStepId()));
                } else {
                    CachedAgent ca = getCachedAgent(dbLayer, item.getAgentId());
                    step = new CachedOrderStep(item, ca.getTimezone());
                    if (item.getError()) {
                        step.setError(item.getErrorState(), item.getErrorReason(), item.getErrorCode(), item.getErrorText());
                    }
                }
            } else if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][currentStep found]%s", identifier, co.getOrderId(), SOSString.toString(step)));
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
            le.setState(OrderStateText.SUSPENDED.intValue());// TODO check
            le.setLogLevel(LogLevel.INFO);
            break;
        case OrderResumed:
            le.setState(OrderStateText.RUNNING.intValue());// TODO check
            le.setLogLevel(LogLevel.MAIN);
            break;
        case OrderResumeMarked:
            le.setState(OrderStateText.RUNNING.intValue()); // TODO check
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
        co.setState(OrderStateText.RUNNING.intValue());
        co.setHasStates(true);
        // addCachedOrder(co.getOrderId(), co);

        dbLayer.updateOrderOnResumed(co.getId(), co.getState(), entry.getEventDatetime());
        saveOrderState(dbLayer, co, co.getState(), entry.getEventDatetime(), entry.getEventId(), null, null);

        LogEntry le = new LogEntry(LogEntry.LogLevel.MAIN, EventType.OrderResumed, entry.getEventDatetime(), null);
        le.onOrder(co, null);
        storeLog2File(le);
    }

    private void orderLog(DBLayerHistory dbLayer, AFatEventOrderProcessed entry, EventType eventType) throws Exception {
        checkControllerTimezone(dbLayer);

        CachedOrder co = getCachedOrder(dbLayer, entry.getOrderId(), entry.getEventDatetime(), entry.getEventId());

        LogEntry le = new LogEntry(LogEntry.LogLevel.DETAIL, eventType, entry.getEventDatetime(), null);
        le.onOrder(co, entry.getPosition());
        storeLog2File(le);
    }

    private void orderLock(DBLayerHistory dbLayer, AFatEventOrderLock entry, EventType eventType) throws Exception {
        checkControllerTimezone(dbLayer);

        CachedOrder co = getCachedOrder(dbLayer, entry.getOrderId(), entry.getEventDatetime(), entry.getEventId());

        LogEntry le = new LogEntry(LogEntry.LogLevel.DETAIL, eventType, entry.getEventDatetime(), null);
        le.onOrderLock(co, entry);
        storeLog2File(le);
    }

    private void orderForked(DBLayerHistory dbLayer, FatEventOrderForked entry) throws Exception {
        checkControllerTimezone(dbLayer);

        CachedOrder co = getCachedOrder(dbLayer, entry.getOrderId(), entry.getEventDatetime(), entry.getEventId());
        co.setHasChildren(true);
        co.setState(OrderStateText.RUNNING.intValue());

        dbLayer.updateOrderOnFork(co.getId(), co.getState(), entry.getEventDatetime());

        LogEntry le = new LogEntry(LogEntry.LogLevel.DETAIL, EventType.OrderForked, entry.getEventDatetime(), null);
        le.onOrder(co, entry.getPosition(), entry.getChilds());
        storeLog2File(le);

        for (FatForkedChild fc : entry.getChilds()) {
            orderForkedStarted(dbLayer, entry, co, fc);
        }
    }

    private void orderForkedStarted(DBLayerHistory dbLayer, FatEventOrderForked entry, CachedOrder parentOrder, FatForkedChild forkOrder)
            throws Exception {

        DBItemHistoryOrder item = new DBItemHistoryOrder();
        String constraintHash = null;

        try {
            checkControllerTimezone(dbLayer);

            item.setControllerId(controllerConfiguration.getCurrent().getId());
            item.setOrderId(forkOrder.getOrderId());

            item.setWorkflowPath(entry.getWorkflowPath());
            item.setWorkflowVersionId(entry.getWorkflowVersionId());
            item.setWorkflowPosition(HistoryPosition.asString(forkOrder.getPosition()));
            item.setWorkflowFolder(HistoryUtil.getFolderFromPath(item.getWorkflowPath()));
            item.setWorkflowName(HistoryUtil.getBasenameFromPath(item.getWorkflowPath()));
            item.setWorkflowTitle(null);// TODO

            item.setMainParentId(parentOrder.getMainParentId());
            item.setParentId(parentOrder.getId());
            item.setParentOrderId(parentOrder.getOrderId());
            item.setHasChildren(false);
            item.setRetryCounter(HistoryPosition.getRetry(entry.getPosition()));

            item.setName(forkOrder.getBranchId());// TODO
            item.setTitle(null);// TODO

            item.setStartCause(OrderStartCause.fork.name());// TODO
            item.setStartTimePlanned(entry.getEventDatetime());
            item.setStartTime(entry.getEventDatetime());
            item.setStartWorkflowPosition(SOSString.isEmpty(entry.getPosition()) ? "0" : entry.getPosition());
            item.setStartEventId(String.valueOf(entry.getEventId()));
            item.setStartParameters(entry.getArgumentsAsJsonString()); // TODO or forkOrder arguments ???

            item.setCurrentHistoryOrderStepId(new Long(0L));

            item.setEndTime(null);
            item.setEndWorkflowPosition(null);
            item.setEndHistoryOrderStepId(new Long(0L));

            item.setSeverity(OrderStateText.RUNNING);
            item.setState(OrderStateText.RUNNING);
            item.setStateTime(entry.getEventDatetime());
            item.setStateText(null);

            item.setError(false);
            item.setErrorState(null);
            item.setErrorReason(null);
            item.setErrorReturnCode(null);
            item.setErrorCode(null);
            item.setErrorText(null);
            item.setEndEventId(null);

            item.setLogId(new Long(0L));

            constraintHash = hashOrderConstraint(entry.getEventId(), item.getOrderId(), item.getWorkflowPosition());
            item.setConstraintHash(constraintHash);
            item.setCreated(new Date());
            item.setModified(item.getCreated());

            dbLayer.getSession().save(item);

            CachedOrder co = new CachedOrder(item);
            LogEntry le = new LogEntry(LogEntry.LogLevel.DETAIL, EventType.OrderStarted, item.getStartTime(), null);
            le.onOrder(co, item.getWorkflowPosition());
            storeLog2File(le);
            addCachedOrder(item.getOrderId(), co);

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
        for (FatForkedChild child : entry.getChilds()) {
            orderUpdate(dbLayer, EventType.OrderJoined, entry.getEventId(), child.getOrderId(), endTime, entry.getOutcome(), null, endedOrderSteps,
                    true);
        }

        LogEntry le = new LogEntry(LogEntry.LogLevel.DETAIL, EventType.OrderJoined, HistoryUtil.getEventIdAsDate(entry.getEventId()), null);
        CachedOrder co = getCachedOrder(dbLayer, entry.getOrderId(), entry.getEventDatetime(), entry.getEventId());
        le.onOrderJoined(co, entry.getPosition(), entry.getChilds().stream().map(s -> s.getOrderId()).collect(Collectors.toList()), entry
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

            ca = getCachedAgent(dbLayer, entry.getAgentId());
            co = getCachedOrder(dbLayer, entry.getOrderId(), entry.getEventDatetime(), entry.getEventId());

            Date agentStartTime = entry.getEventDatetime();

            item = new DBItemHistoryOrderStep();
            item.setControllerId(controllerConfiguration.getCurrent().getId());
            item.setOrderId(entry.getOrderId());

            item.setWorkflowPath(entry.getWorkflowPath());
            item.setWorkflowVersionId(entry.getWorkflowVersionId());
            item.setWorkflowPosition(entry.getPosition());
            item.setWorkflowFolder(HistoryUtil.getFolderFromPath(item.getWorkflowPath()));
            item.setWorkflowName(HistoryUtil.getBasenameFromPath(item.getWorkflowPath()));

            item.setHistoryOrderMainParentId(co.getMainParentId());
            item.setHistoryOrderId(co.getId());
            item.setPosition(HistoryPosition.getLast(entry.getPosition()));
            item.setRetryCounter(HistoryPosition.getRetry(entry.getPosition()));

            item.setJobName(entry.getJobName());
            item.setJobTitle(null);// TODO
            item.setCriticality(JobCriticality.NORMAL);// TODO

            item.setAgentId(entry.getAgentId());
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

            constraintHash = hashOrderStepConstraint(entry.getEventId(), item.getOrderId(), item.getWorkflowPosition());
            item.setConstraintHash(constraintHash);
            item.setCreated(new Date());
            item.setModified(item.getCreated());

            dbLayer.getSession().save(item);

            co.setCurrentHistoryOrderStepId(item.getId());
            dbLayer.updateOrderOnOrderStep(co.getId(), co.getCurrentHistoryOrderStepId());

            cos = new CachedOrderStep(item, ca.getTimezone());
            LogEntry le = new LogEntry(LogEntry.LogLevel.MAIN, EventType.OrderProcessingStarted, HistoryUtil.getEventIdAsDate(entry.getEventId()),
                    agentStartTime);
            le.onOrderStep(cos, ca.getTimezone());
            storeLog2File(le);
            addCachedOrderStep(item.getOrderId(), cos);

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
                addCachedOrder(co.getOrderId(), co);
            }
            addCachedOrderStepByStartTime(dbLayer, ca, entry.getOrderId(), entry.getEventDatetime(), entry.getEventId(), constraintHash);
        }
    }

    private void saveOrderState(DBLayerHistory dbLayer, CachedOrder co, Integer state, Date stateDate, Long stateEventId, String stateCode,
            String stateText) throws Exception {
        DBItemHistoryOrderState item = new DBItemHistoryOrderState();
        item.setHistoryOrderMainParentId(co.getMainParentId());
        item.setHistoryOrderParentId(co.getParentId());
        item.setHistoryOrderId(co.getId());
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
            DBItemHistoryLog logItem = storeLogFile2Db(dbLayer, cos.getHistoryOrderMainParentId(), cos.getHistoryOrderId(), cos.getId(), true, log);
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
        clearCache(CacheType.orderStep, cos.getOrderId());
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

    private void addCachedOrder(String orderId, CachedOrder order) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][addCachedOrder][%s]%s", identifier, orderId, SOSString.toString(order)));
        }
        cachedOrders.put(orderId, order);
    }

    private CachedOrder getCachedOrder(DBLayerHistory dbLayer, String orderId, Date currentEventTime, Long currentEventId) throws Exception {
        CachedOrder co = getCachedOrder(orderId);
        if (co == null) {
            DBItemHistoryOrder item = dbLayer.getOrderBeforeCurrentEvent(controllerConfiguration.getCurrent().getId(), orderId, currentEventTime);
            if (item == null) {
                throw new FatEventOrderNotFoundException(String.format("[%s][%s][%s]order not found", identifier, orderId, currentEventId), orderId,
                        currentEventId);
            } else {
                co = new CachedOrder(item);
                addCachedOrder(orderId, co);
            }
        }
        return co;
    }

    private CachedOrder getCachedOrder(String orderId) {
        if (cachedOrders.containsKey(orderId)) {
            CachedOrder co = cachedOrders.get(orderId);
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][getCachedOrder][%s]%s", identifier, orderId, SOSString.toString(co)));
            }
            return co;
        }
        return null;
    }

    private void addCachedOrderByStateTime(DBLayerHistory dbLayer, String orderId, Date stateTime, Long stateEventId, String constraintHash)
            throws Exception {
        DBItemHistoryOrder item = null;
        if (constraintHash != null) {
            item = dbLayer.getOrderByConstraint(constraintHash);
            if (item == null) {
                LOGGER.warn(String.format("[%s][%s][%s][%s]order by constraintHash not found, try to find by state time...", identifier, orderId,
                        stateEventId, constraintHash));
            }
        }
        if (item == null) {
            item = dbLayer.getOrderByStateTime(controllerConfiguration.getCurrent().getId(), orderId, stateTime);
        }
        if (item == null) {
            throw new FatEventOrderNotFoundException(String.format("[%s][%s][%s]order not found", identifier, orderId, stateEventId), orderId,
                    stateEventId);
        } else {
            addCachedOrder(orderId, new CachedOrder(item));
        }
    }

    private void addCachedOrderStep(String orderId, CachedOrderStep co) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][addCachedOrderStep][%s]%s", identifier, orderId, SOSString.toString(co)));
        }
        cachedOrderSteps.put(orderId, co);
    }

    private CachedOrderStep getCachedOrderStep(DBLayerHistory dbLayer, String orderId, Date currentEventTime, Long currentEventId) throws Exception {
        CachedOrderStep co = getCachedOrderStep(orderId);
        if (co == null) {
            DBItemHistoryOrderStep item = dbLayer.getOrderStepLastBeforeCurrentEvent(controllerConfiguration.getCurrent().getId(), orderId,
                    currentEventTime, currentEventId);
            if (item == null) {
                throw new FatEventOrderStepNotFoundException(String.format("[%s][%s]order step not found. orderId=%s", identifier, currentEventId,
                        orderId), orderId, currentEventId);
            } else {
                DBItemHistoryAgent agent = dbLayer.getLastAgent(controllerConfiguration.getCurrent().getId(), item.getAgentId());
                if (agent == null) {
                    LOGGER.warn(String.format(
                            "[%s][agent is null]agent timezone can't be identified. set agent log timezone to controller timezone ...", item
                                    .getAgentId()));
                    co = new CachedOrderStep(item, controllerTimezone);
                } else {
                    co = new CachedOrderStep(item, agent.getTimezone());
                }
                addCachedOrderStep(orderId, co);
            }
        }
        return co;
    }

    private CachedOrderStep getCachedOrderStep(String orderId) {
        if (cachedOrderSteps.containsKey(orderId)) {
            CachedOrderStep co = cachedOrderSteps.get(orderId);
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][getCachedOrderStep][%s]%s", identifier, orderId, SOSString.toString(co)));
            }
            return co;
        }
        return null;
    }

    private void addCachedOrderStepByStartTime(DBLayerHistory dbLayer, CachedAgent agent, String orderId, Date startTime, Long startEventId,
            String constraintHash) throws Exception {

        DBItemHistoryOrderStep item = null;
        if (constraintHash != null) {
            item = dbLayer.getOrderStepByConstraint(constraintHash);
            if (item == null) {
                LOGGER.warn(String.format("[%s][%s][%s][%s]order step by constraintHash not found, try to find by start time...", identifier, orderId,
                        startEventId, constraintHash));
            }
        }
        if (item == null) {
            item = dbLayer.getOrderStepByStartTime(controllerConfiguration.getCurrent().getId(), orderId, startTime, startEventId);
        }
        if (item == null) {
            throw new FatEventOrderStepNotFoundException(String.format("[%s]order step not found. orderId=%s, startEventId=%s", identifier, orderId,
                    startEventId), orderId, startEventId);
        } else {
            if (agent == null) {
                LOGGER.warn(String.format(
                        "[%s][agent not found]agent timezone can't be identified. set agent log timezone to controller timezone ...", item
                                .getAgentId()));
                addCachedOrderStep(orderId, new CachedOrderStep(item, controllerTimezone));
            } else {
                addCachedOrderStep(orderId, new CachedOrderStep(item, agent.getTimezone()));
            }
        }
    }

    private void addCachedAgent(String agentId, CachedAgent ca) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][addCachedAgent][%s]%s", identifier, agentId, SOSString.toString(ca)));
        }
        cachedAgents.put(agentId, ca);
    }

    private CachedAgent addCachedAgentByReadyEventId(DBLayerHistory dbLayer, String agentId, Long readyEventId) {
        CachedAgent ca = null;
        try {
            ca = new CachedAgent(dbLayer.getAgentByReadyEventId(controllerConfiguration.getCurrent().getId(), agentId, String.valueOf(readyEventId)));
        } catch (Throwable e) {
            ca = getCachedAgent(agentId);
        }
        addCachedAgent(agentId, ca);
        return ca;
    }

    private CachedAgent getCachedAgent(DBLayerHistory dbLayer, String agentId) throws Exception {
        CachedAgent ca = getCachedAgent(agentId);
        if (ca == null) {
            DBItemHistoryAgent item = dbLayer.getLastAgent(controllerConfiguration.getCurrent().getId(), agentId);
            if (item == null) {
                LOGGER.warn(String.format("[%s][%s][%s]agent not found in the history. try to find in the agent instances...", identifier,
                        controllerConfiguration.getCurrent().getId(), agentId));
                DBItemInventoryAgentInstance inst = dbLayer.getAgentInstance(controllerConfiguration.getCurrent().getId(), agentId);
                // TODO read from controller API?
                if (inst == null) {
                    Date readyTime = new Date();
                    String uri = "http://localhost:4445";

                    LOGGER.warn(String.format(
                            "[%s][%s][%s]agent not found in the agent instances. set agent timezone to controller timezone=%s, ready time=%s, uri=%s",
                            identifier, controllerConfiguration.getCurrent().getId(), agentId, controllerTimezone, getDateAsString(readyTime), uri));

                    item = new DBItemHistoryAgent();
                    item.setControllerId(controllerConfiguration.getCurrent().getId());
                    item.setAgentId(agentId);
                    item.setUri(uri);
                    item.setTimezone(controllerTimezone);
                    item.setReadyTime(readyTime);
                    item.setReadyEventId(String.valueOf(HistoryUtil.getDateAsEventId(readyTime)));
                    item.setCreated(new Date());
                } else {
                    Date readyTime = inst.getStartedAt();
                    if (readyTime == null) {
                        readyTime = new Date();
                    }
                    LOGGER.info(String.format(
                            "[%s][%s][%s]agent found in the agent instances. set agent timezone to controller timezone=%s, ready time=%s", identifier,
                            controllerConfiguration.getCurrent().getId(), agentId, controllerTimezone, getDateAsString(readyTime)));

                    item = new DBItemHistoryAgent();
                    item.setControllerId(controllerConfiguration.getCurrent().getId());
                    item.setAgentId(agentId);
                    item.setUri(inst.getUri());
                    item.setTimezone(controllerTimezone);
                    item.setReadyTime(readyTime);
                    item.setReadyEventId(String.valueOf(HistoryUtil.getDateAsEventId(readyTime)));
                    item.setCreated(new Date());
                }
                dbLayer.getSession().save(item);
            }

            ca = new CachedAgent(item);
            addCachedAgent(agentId, ca);
        }
        return ca;
    }

    private CachedAgent getCachedAgent(String agentId) {
        if (cachedAgents.containsKey(agentId)) {
            CachedAgent co = cachedAgents.get(agentId);
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][getCachedAgent][%s]%s", identifier, agentId, SOSString.toString(co)));
            }
            return co;
        }
        return null;
    }

    private void clearCache(CacheType cacheType, String orderId) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][clearCache][%s]%s", identifier, cacheType, orderId));
        }
        switch (cacheType) {
        case orderStep:
            cachedOrderSteps.entrySet().removeIf(entry -> entry.getKey().equals(orderId));
            break;
        case order:
            cachedOrders.entrySet().removeIf(entry -> entry.getKey().equals(orderId));
            cachedOrderSteps.entrySet().removeIf(entry -> entry.getKey().startsWith(orderId));
            break;
        default:
            break;
        }
    }

    private DBItemHistoryLog storeLogFile2Db(DBLayerHistory dbLayer, Long orderMainParentId, Long orderId, Long orderStepId, boolean compressed,
            Path file) throws Exception {

        DBItemHistoryLog item = null;
        if (Files.exists(file)) {
            item = new DBItemHistoryLog();
            item.setControllerId(controllerConfiguration.getCurrent().getId());

            item.setHistoryOrderMainParentId(orderMainParentId);
            item.setHistoryOrderId(orderId);
            item.setHistoryOrderStepId(orderStepId);
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
        // return dir.resolve(entry.getHistoryOrderMainParentId() + ".log");
        return dir.resolve(orderId + ".log");
    }

    private Path getOrderStepLog(Path dir, LogEntry entry) {
        return dir.resolve(entry.getHistoryOrderId() + "_" + entry.getHistoryOrderStepId() + ".log");
    }

    private Path getOrderLogDirectory(LogEntry entry) {
        return HistoryMain.getOrderLogDirectory(Paths.get(historyConfiguration.getLogDir()), entry.getHistoryOrderMainParentId());
        // return Paths.get(historyConfiguration.getLogDir(), String.valueOf(entry.getHistoryOrderMainParentId()));
    }

    private OrderLogEntry createOrderLogEntry(LogEntry logEntry) {
        OrderLogEntry entry = new OrderLogEntry();
        entry.setOrderId(logEntry.getOrderId());
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
        if (logEntry.getOrderLock() != null) {
            Lock lock = new Lock();
            lock.setLockId(logEntry.getOrderLock().getLockId());
            lock.setLimit(logEntry.getOrderLock().getLimit());
            lock.setCount(logEntry.getOrderLock().getCount());
            if (logEntry.getOrderLock().getState() != null) {
                LockState lockState = new LockState();

                List<String> l = logEntry.getOrderLock().getState().getOrderIds();
                lockState.setOrderIds(l == null || l.size() == 0 ? null : String.join(",", l));

                l = logEntry.getOrderLock().getState().getQueuedOrderIds();
                lockState.setQueuedOrderIds(l == null || l.size() == 0 ? null : String.join(",", l));
                lock.setLockState(lockState);
            }
            entry.setLock(lock);
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
            orderEntry.setAgentPath(entry.getAgentId());
            orderEntry.setAgentUrl(entry.getAgentUri());
            orderEntry.setJob(entry.getJobName());
            orderEntry.setTaskId(entry.getHistoryOrderStepId());
            orderEntryContent = new StringBuilder((new ObjectMapper()).writeValueAsString(orderEntry));
            write2file(getOrderLog(dir, entry.getHistoryOrderId()), orderEntryContent, newLine);

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
            orderEntry.setTaskId(entry.getHistoryOrderStepId());
            orderEntryContent = new StringBuilder((new ObjectMapper()).writeValueAsString(orderEntry));
            write2file(getOrderLog(dir, entry.getHistoryOrderId()), orderEntryContent, newLine);

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
            file = getOrderLog(dir, entry.getHistoryOrderId());

            orderEntry = createOrderLogEntry(entry);
            orderEntry.setControllerDatetime(getDateAsString(entry.getControllerDatetime(), controllerTimezone));
            if (entry.getAgentDatetime() != null && entry.getAgentTimezone() != null) {
                orderEntry.setAgentDatetime(getDateAsString(entry.getAgentDatetime(), entry.getAgentTimezone()));
            }
            content.append((new ObjectMapper()).writeValueAsString(orderEntry));
        }

        try {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][%s]%s", identifier, entry.getEventType().value(), entry.getOrderId(), file));
            }
            write2file(file, content, newLine);
        } catch (NoSuchFileException e) {
            if (!Files.exists(dir)) {
                Files.createDirectory(dir);
            }
            write2file(file, content, newLine);
        } catch (Exception e) {
            LOGGER.error(String.format("[%s][%s][%s][%s]%s", identifier, entry.getEventType().value(), entry.getOrderId(), file, e.toString()), e);
            throw e;
        }

        if (orderEntry != null && !entry.getHistoryOrderId().equals(entry.getHistoryOrderMainParentId())) {
            write2MainOrderLog(entry, dir, (orderEntryContent == null ? content : orderEntryContent), newLine);
        }

        return file;
    }

    private void write2MainOrderLog(LogEntry entry, Path dir, StringBuilder content, boolean newLine) throws Exception {
        Path file = getOrderLog(dir, entry.getHistoryOrderMainParentId());
        try {
            write2file(file, content, newLine);
        } catch (Exception e) {
            LOGGER.error(String.format("[%s][%s][%s][%s]%s", identifier, entry.getEventType().value(), entry.getOrderId(), file, e.toString()), e);
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

    // TODO controllerId+orderId
    private String hashOrderConstraint(Long eventId, String orderId, String workflowPosition) {
        return SOSString.hash(new StringBuilder(controllerConfiguration.getCurrent().getId()).append(String.valueOf(eventId)).append(orderId).append(
                workflowPosition).toString());
    }

    // TODO controllerId+orderId+workflowPosition
    private String hashOrderStepConstraint(Long eventId, String orderId, String workflowPosition) {
        return hashOrderConstraint(eventId, orderId, workflowPosition);
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
