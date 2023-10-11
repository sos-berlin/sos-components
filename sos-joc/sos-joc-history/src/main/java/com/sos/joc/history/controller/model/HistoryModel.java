package com.sos.joc.history.controller.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.exception.SOSHibernateObjectOperationException;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSGzip;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.controller.model.event.EventType;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.job.JobCriticality;
import com.sos.joc.classes.history.HistoryPosition;
import com.sos.joc.cluster.bean.history.HistoryOrderBean;
import com.sos.joc.cluster.bean.history.HistoryOrderStepBean;
import com.sos.joc.cluster.common.JocClusterUtil;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocHistoryConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.db.history.DBItemHistoryAgent;
import com.sos.joc.db.history.DBItemHistoryController;
import com.sos.joc.db.history.DBItemHistoryLog;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.db.history.DBItemHistoryOrderState;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.db.history.common.HistorySeverity;
import com.sos.joc.db.joc.DBItemJocVariable;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.history.HistoryOrderLog;
import com.sos.joc.event.bean.history.HistoryOrderStarted;
import com.sos.joc.event.bean.history.HistoryOrderTaskLog;
import com.sos.joc.event.bean.history.HistoryOrderTaskLogFirstStderr;
import com.sos.joc.event.bean.history.HistoryOrderTaskStarted;
import com.sos.joc.event.bean.history.HistoryOrderTaskTerminated;
import com.sos.joc.event.bean.history.HistoryOrderTerminated;
import com.sos.joc.event.bean.history.HistoryOrderUpdated;
import com.sos.joc.history.controller.exception.model.HistoryModelException;
import com.sos.joc.history.controller.exception.model.HistoryModelOrderNotFoundException;
import com.sos.joc.history.controller.exception.model.HistoryModelOrderStepNotFoundException;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.OrderLock;
import com.sos.joc.history.controller.proxy.HistoryEventType;
import com.sos.joc.history.controller.proxy.fatevent.AFatEvent;
import com.sos.joc.history.controller.proxy.fatevent.AFatEventOrderBase;
import com.sos.joc.history.controller.proxy.fatevent.AFatEventOrderLocks;
import com.sos.joc.history.controller.proxy.fatevent.AFatEventOrderNotice;
import com.sos.joc.history.controller.proxy.fatevent.AFatEventOrderProcessed;
import com.sos.joc.history.controller.proxy.fatevent.FatEventAgentCouplingFailed;
import com.sos.joc.history.controller.proxy.fatevent.FatEventAgentReady;
import com.sos.joc.history.controller.proxy.fatevent.FatEventAgentShutDown;
import com.sos.joc.history.controller.proxy.fatevent.FatEventClusterCoupled;
import com.sos.joc.history.controller.proxy.fatevent.FatEventControllerReady;
import com.sos.joc.history.controller.proxy.fatevent.FatEventControllerShutDown;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderAttached;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderCancelled;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderCaught;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderCyclingPrepared;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderForked;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderJoined;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderMoved;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderNoticePosted;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderNoticesConsumed;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderNoticesConsumptionStarted;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderNoticesExpected;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderNoticesRead;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderPromptAnswered;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderPrompted;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderResumed;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderResumptionMarked;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderRetrying;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderStarted;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderStepProcessed;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderStepStarted;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderStepStdWritten;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderSuspended;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderSuspensionMarked;
import com.sos.joc.history.controller.proxy.fatevent.FatEventWithProblem;
import com.sos.joc.history.controller.proxy.fatevent.FatExpectNotice;
import com.sos.joc.history.controller.proxy.fatevent.FatExpectNotices;
import com.sos.joc.history.controller.proxy.fatevent.FatForkedChild;
import com.sos.joc.history.controller.proxy.fatevent.FatInstruction;
import com.sos.joc.history.controller.proxy.fatevent.FatOutcome;
import com.sos.joc.history.controller.proxy.fatevent.FatPostNotice;
import com.sos.joc.history.controller.yade.YadeHandler;
import com.sos.joc.history.db.DBLayerHistory;
import com.sos.joc.history.helper.CachedAgent;
import com.sos.joc.history.helper.CachedAgentCouplingFailed.AgentCouplingFailed;
import com.sos.joc.history.helper.CachedOrder;
import com.sos.joc.history.helper.CachedOrderStep;
import com.sos.joc.history.helper.CachedWorkflow;
import com.sos.joc.history.helper.CachedWorkflowJob;
import com.sos.joc.history.helper.Counter;
import com.sos.joc.history.helper.HistoryCacheHandler;
import com.sos.joc.history.helper.HistoryCacheHandler.CacheType;
import com.sos.joc.history.helper.HistoryUtil;
import com.sos.joc.history.helper.LogEntry;
import com.sos.joc.history.helper.LogExtAsyncHandler;
import com.sos.joc.history.helper.OrderStepProcessedResult;
import com.sos.joc.model.history.order.Lock;
import com.sos.joc.model.history.order.LockState;
import com.sos.joc.model.history.order.OrderLogEntry;
import com.sos.joc.model.history.order.OrderLogEntryError;
import com.sos.joc.model.history.order.OrderLogEntryInstruction;
import com.sos.joc.model.history.order.OrderLogEntryLogLevel;
import com.sos.joc.model.history.order.attached.Attached;
import com.sos.joc.model.history.order.common.WaitingForAdmission;
import com.sos.joc.model.history.order.cycle.Cycle;
import com.sos.joc.model.history.order.cycle.CyclePrepared;
import com.sos.joc.model.history.order.moved.Moved;
import com.sos.joc.model.history.order.moved.MovedSkipped;
import com.sos.joc.model.history.order.moved.MovedSkippedReason;
import com.sos.joc.model.history.order.moved.MovedTo;
import com.sos.joc.model.history.order.notice.BaseNotice;
import com.sos.joc.model.history.order.notice.ConsumeNotices;
import com.sos.joc.model.history.order.notice.ExpectNotices;
import com.sos.joc.model.history.order.notice.PostNotice;
import com.sos.joc.model.history.order.retry.Retrying;
import com.sos.joc.model.order.OrderStateText;

public class HistoryModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryModel.class);

    public static final String RETURN_CODE_KEY = "returnCode";

    private static final String RETURN_MESSAGE_KEY = "returnMessage";
    private static final String AGENT_COUPLING_FAILED_SHUTDOWN_MESSAGE = "shutting down";// lower case

    private static final boolean CLEANUP_LOG_FILES = true;

    private final SOSHibernateFactory dbFactory;
    private JocHistoryConfiguration historyConfiguration;
    private ControllerConfiguration controllerConfiguration;
    private YadeHandler yadeHandler;
    private HistoryCacheHandler cacheHandler;
    private final LogExtAsyncHandler logExtHandler;
    private String identifier;
    private final String variableName;
    private Long storedEventId;
    private boolean closed = false;
    private int maxTransactions = 100;
    private long transactionCounter;
    private String controllerTimezone;

    private boolean isDebugEnabled;
    private boolean isTraceEnabled;

    private static final Map<EventType, String> messages = Collections.unmodifiableMap(new HashMap<EventType, String>() {

        private static final long serialVersionUID = 1L;

        {
            put(EventType.OrderLocksQueued, "starting to acquire resource locks %s");
            put(EventType.OrderLocksAcquired, "acquired resource locks %s");
            put(EventType.OrderLocksReleased, "releasing resource locks %s");
        }

    });

    private static enum OrderStartCause {
        order, fork, file_trigger, setback, unskip, unstop
    };

    private static enum OrderStepStartCause {
        order, file_trigger, setback, unskip, unstop
    };

    public HistoryModel(SOSHibernateFactory factory, JocHistoryConfiguration historyConf, ControllerConfiguration controllerConf) {
        dbFactory = factory;
        historyConfiguration = historyConf;
        controllerConfiguration = controllerConf;
        variableName = "history_" + controllerConfiguration.getCurrent().getId();
        maxTransactions = historyConfiguration.getMaxTransactions();
        yadeHandler = new YadeHandler(controllerConfiguration.getCurrent().getId());
        cacheHandler = new HistoryCacheHandler(controllerConfiguration.getCurrent().getId(), identifier);
        logExtHandler = new LogExtAsyncHandler(historyConfiguration, variableName + "_logExt");
    }

    public Long getEventId() throws Exception {
        DBLayerHistory dbLayer = null;
        try {
            dbLayer = new DBLayerHistory(dbFactory.openStatelessSession());
            dbLayer.getSession().setIdentifier(identifier);
            dbLayer.beginTransaction();
            DBItemJocVariable item = dbLayer.getControllerVariable(variableName);
            if (item == null) {
                item = dbLayer.insertControllerVariable(variableName, "0");
            }
            dbLayer.commit();

            return Long.parseLong(item.getTextValue());
        } catch (Exception e) {
            if (dbLayer != null) {
                dbLayer.rollback();
            }
            throw e;
        } finally {
            if (dbLayer != null) {
                dbLayer.close();
            }
        }
    }

    public void checkConnection(String identifier) throws Exception {
        DBLayerHistory dbLayer = null;
        try {
            dbLayer = new DBLayerHistory(dbFactory.openStatelessSession());
            dbLayer.getSession().setIdentifier(identifier);
            dbLayer.getControllerVariable(variableName);
        } catch (Exception e) {
            throw e;
        } finally {
            if (dbLayer != null) {
                dbLayer.close();
            }
        }
    }

    private void initLogLevels() {
        isDebugEnabled = LOGGER.isDebugEnabled();
        isTraceEnabled = LOGGER.isTraceEnabled();

        cacheHandler.initLogLevels();
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
        Long firstEventId = Long.valueOf(0);
        Long lastSuccessEventId = Long.valueOf(0);

        Counter counter = new Counter();
        counter.setTotal(list.size());

        initLogLevels();

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s][start][%s][%s]%s total", identifier, method, storedEventId, start, counter.getTotal()));
        }

        boolean hasError = false;
        try {
            dbLayer = new DBLayerHistory(dbFactory.openStatelessSession());
            dbLayer.getSession().setIdentifier(identifier);
            dbLayer.beginTransaction();

            for (AFatEvent entry : list) {
                if (closed) {// TODO
                    LOGGER.info(String.format("[%s][%s][skip]is closed", identifier, method));
                    break;
                }
                Long eventId = entry.getEventId();
                if (eventId == null) {
                    continue;
                }

                if (counter.getProcessed() == 0) {
                    firstEventId = eventId;
                }
                if (storedEventId >= eventId) {
                    if (entry.getType().equals(HistoryEventType.EventWithProblem)) { // EventWithProblem can sets eventId=-1L
                        LOGGER.info(String.format("[%s][%s][%s][skip]stored eventId=%s > current eventId=%s %s", identifier, method, entry.getType(),
                                storedEventId, eventId, SOSString.toString(entry)));

                        FatEventWithProblem ep = (FatEventWithProblem) entry;
                        LOGGER.info(String.format("[FatEventWithProblem][eventType=%s]problem=%s", ep.getEventType(), ep.getError()));
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

                HistoryOrderBean hob;
                HistoryOrderStepBean hosb;
                try {
                    switch (entry.getType()) {
                    case ClusterCoupled:
                        FatEventClusterCoupled fecc = (FatEventClusterCoupled) entry;
                        if (controllerConfiguration.getSecondary() != null) {
                            controllerConfiguration.setCurrent(fecc.isPrimary() ? controllerConfiguration.getPrimary() : controllerConfiguration
                                    .getSecondary());
                            LOGGER.info(String.format("[%s][ClusterCoupled %s, %s][%s][%s]", identifier, fecc.getActiveId(), controllerConfiguration
                                    .getCurrent().getUri4Log(), eventId, eventIdAsTime(eventId)));
                        }
                        counter.getController().addClusterCoupled();
                        break;
                    case ControllerReady:
                        controllerReady(dbLayer, (FatEventControllerReady) entry);
                        counter.getController().addReady();
                        break;
                    case ControllerShutDown:
                        controllerShutDown(dbLayer, (FatEventControllerShutDown) entry);
                        counter.getController().addShutdown();
                        break;
                    case AgentReady:
                        agentReady(dbLayer, (FatEventAgentReady) entry);
                        counter.getAgent().addReady();
                        break;
                    case AgentSubagentDedicated:
                        counter.getAgent().addSubagentDedicated();
                        break;
                    case AgentCouplingFailed:
                        agentCouplingFailed(dbLayer, (FatEventAgentCouplingFailed) entry);
                        counter.getAgent().addCouplingFailed();
                        break;
                    case AgentShutDown:
                        agentShutDown(dbLayer, (FatEventAgentShutDown) entry);
                        counter.getAgent().addShutdown();
                        break;
                    case OrderStarted:
                        hob = orderStarted(dbLayer, (FatEventOrderStarted) entry);
                        counter.getOrder().addStarted();

                        postEventOrderStarted(hob);
                        break;
                    case OrderResumed:
                        hob = orderResumed(dbLayer, (FatEventOrderResumed) entry);
                        counter.getOrder().addResumed();

                        postEventOrderUpdated(hob);
                        break;
                    case OrderResumptionMarked:
                        FatEventOrderResumptionMarked eorm = (FatEventOrderResumptionMarked) entry;

                        orderLog(dbLayer, eorm, EventType.OrderResumptionMarked, OrderLogEntryLogLevel.DETAIL, null, hasOrderStarted(eorm
                                .getOrderId(), eorm.isStarted()));
                        counter.getOrder().addResumeMarked();
                        break;
                    case OrderSuspensionMarked:
                        FatEventOrderSuspensionMarked eosm = (FatEventOrderSuspensionMarked) entry;

                        orderLog(dbLayer, eosm, EventType.OrderSuspensionMarked, OrderLogEntryLogLevel.DETAIL, null, hasOrderStarted(eosm
                                .getOrderId(), eosm.isStarted()));
                        counter.getOrder().addSuspendMarked();
                        break;
                    case OrderForked:
                        hob = orderForked(dbLayer, (FatEventOrderForked) entry);
                        counter.getOrder().addForked();

                        postEventOrderUpdated(hob);
                        break;
                    case OrderJoined:
                        hob = orderJoined(dbLayer, (FatEventOrderJoined) entry, endedOrderSteps);
                        counter.getOrder().addJoined();

                        postEventOrderUpdated(hob);
                        break;
                    case OrderStepStarted:
                        hosb = orderStepStarted(dbLayer, (FatEventOrderStepStarted) entry);
                        counter.getOrderStep().addStarted();
                        postEventOrderTaskStarted(dbLayer, (FatEventOrderStepStarted) entry, hosb);
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
                        hosb = orderStepProcessed(dbLayer, (FatEventOrderStepProcessed) entry, endedOrderSteps);
                        counter.getOrderStep().addProcessed();

                        postEventOrderTaskTerminated(dbLayer, (FatEventOrderStepProcessed) entry, hosb);
                        break;
                    case OrderOutcomeAdded:
                        hob = orderNotCompleted(dbLayer, (AFatEventOrderProcessed) entry, EventType.OrderOutcomeAdded, endedOrderSteps, null);
                        counter.getOrder().addOutcomeAdded();

                        postEventOrderUpdated(hob);
                        break;
                    case OrderFailed:
                        hob = orderNotCompleted(dbLayer, (AFatEventOrderProcessed) entry, EventType.OrderFailed, endedOrderSteps, null);
                        counter.getOrder().addFailed();

                        postEventOrderUpdated(hob);
                        break;
                    case OrderStopped:
                        hob = orderNotCompleted(dbLayer, (AFatEventOrderProcessed) entry, EventType.OrderStopped, endedOrderSteps, null);
                        counter.getOrder().addFailed();

                        postEventOrderUpdated(hob);
                        break;
                    case OrderSuspended:
                        FatEventOrderSuspended eos = (FatEventOrderSuspended) entry;
                        if (hasOrderStarted(eos.getOrderId(), eos.isStarted())) {
                            hob = orderNotCompleted(dbLayer, eos, EventType.OrderSuspended, endedOrderSteps, eos.getStoppedInstruction());
                            postEventOrderUpdated(hob);
                        } else {
                            orderLog(dbLayer, eos, EventType.OrderSuspended, OrderLogEntryLogLevel.MAIN, eos.getStoppedInstruction(), false);
                        }
                        counter.getOrder().addSuspended();

                        break;
                    case OrderCancelled:
                        FatEventOrderCancelled oc = (FatEventOrderCancelled) entry;
                        hob = orderTerminated(dbLayer, oc, EventType.OrderCancelled, endedOrderSteps, null);
                        counter.getOrder().addCancelled();

                        postEventOrderTerminated(hob);
                        break;
                    case OrderBroken:
                        // TODO update main order when a child is broken
                        hob = orderTerminated(dbLayer, (AFatEventOrderProcessed) entry, EventType.OrderBroken, endedOrderSteps, null);
                        counter.getOrder().addBroken();

                        postEventOrderTerminated(hob);
                        break;
                    case OrderFinished:
                        hob = orderTerminated(dbLayer, (AFatEventOrderProcessed) entry, EventType.OrderFinished, endedOrderSteps, null);
                        counter.getOrder().addFinished();

                        postEventOrderTerminated(hob);
                        break;
                    case OrderLocksAcquired:
                        orderLock(dbLayer, (AFatEventOrderLocks) entry, EventType.OrderLocksAcquired);
                        counter.getOrder().addLocksAcquired();
                        break;
                    case OrderLocksQueued:
                        orderLock(dbLayer, (AFatEventOrderLocks) entry, EventType.OrderLocksQueued);
                        counter.getOrder().addLocksQueued();
                        break;
                    case OrderLocksReleased:
                        orderLock(dbLayer, (AFatEventOrderLocks) entry, EventType.OrderLocksReleased);
                        counter.getOrder().addLocksReleased();
                        break;
                    case OrderNoticesConsumed:
                        orderLogNotice(dbLayer, (FatEventOrderNoticesConsumed) entry, EventType.OrderNoticesConsumed);
                        counter.getOrder().addNoticesConsumed();
                        break;
                    case OrderNoticesConsumptionStarted:
                        orderLogNotice(dbLayer, (FatEventOrderNoticesConsumptionStarted) entry, EventType.OrderNoticesConsumptionStarted);
                        counter.getOrder().addNoticesConsumptionStarted();
                        break;
                    // if expected notice(s) exists
                    case OrderNoticesRead:
                        orderLogNotice(dbLayer, (FatEventOrderNoticesRead) entry, EventType.OrderNoticesRead);
                        counter.getOrder().addNoticesRead();
                        break;
                    // if expected notice(s) not exist
                    case OrderNoticesExpected:
                        orderLogNotice(dbLayer, (FatEventOrderNoticesExpected) entry, EventType.OrderNoticesExpected);
                        counter.getOrder().addNoticesExpected();
                        break;
                    case OrderNoticePosted:
                        orderLogNotice(dbLayer, (FatEventOrderNoticePosted) entry, EventType.OrderNoticePosted);
                        counter.getOrder().addNoticePosted();
                        break;
                    case OrderCaught:
                        orderLog(dbLayer, (FatEventOrderCaught) entry, EventType.OrderCaught);
                        counter.getOrder().addCaught();
                        break;
                    case OrderRetrying:
                        orderLog(dbLayer, (FatEventOrderRetrying) entry, EventType.OrderRetrying);
                        counter.getOrder().addRetrying();
                        break;
                    case OrderMoved:
                        orderLogMoved(dbLayer, (FatEventOrderMoved) entry, EventType.OrderMoved);
                        counter.getOrder().addMoved();
                        break;
                    case OrderAttached:
                        orderLogAttached(dbLayer, (FatEventOrderAttached) entry, EventType.OrderAttached);
                        counter.getOrder().addAttached();
                        break;
                    case OrderPrompted:
                        orderLog(dbLayer, (FatEventOrderPrompted) entry, EventType.OrderPrompted);
                        break;
                    case OrderPromptAnswered:
                        orderLog(dbLayer, (FatEventOrderPromptAnswered) entry, EventType.OrderPromptAnswered);
                        break;
                    case OrderCyclingPrepared:
                        orderLog(dbLayer, (FatEventOrderCyclingPrepared) entry, EventType.OrderCyclingPrepared);
                        break;
                    case EventWithProblem:
                        try {
                            FatEventWithProblem ep = (FatEventWithProblem) entry;
                            LOGGER.info(String.format("[%s][%s][%s][%s][%s]%s", controllerConfiguration.getCurrent().getId(), entry.getType(), ep
                                    .getOrderId(), ep.getEventType(), ep.getEventId(), ep.getError()));
                        } catch (Throwable tep) {
                        }
                        break;
                    case Empty:
                        break;
                    }
                    counter.addProcessed();
                    lastSuccessEventId = eventId;
                } catch (HistoryModelOrderNotFoundException | HistoryModelOrderStepNotFoundException e) { // TODO ask proxy
                    LOGGER.info(String.format("[%s][%s][%s][failed]%s[%s]", identifier, method, entry.getType(), e.toString(), SOSString.toString(
                            entry)));
                    counter.addProcessed();
                    lastSuccessEventId = eventId;
                    counter.addFailed();
                }
            }
        } catch (Throwable e) {
            hasError = true;
            throw new HistoryModelException(controllerConfiguration.getCurrent().getId(), String.format("[%s][%s][end]%s", identifier, method, e
                    .toString()), e);
        } finally {
            Throwable ex = null;
            try {
                tryStoreCurrentStateAtEnd(dbLayer, lastSuccessEventId);
            } catch (Throwable e1) {
                ex = e1;
            }
            dbLayer.close();

            if (counter.getProcessed() == 0 && firstEventId == 0 && counter.getTotal() > 0) {
                try {
                    firstEventId = list.get(0).getEventId();
                } catch (Throwable e2) {
                }
            }

            showSummary(startEventId, firstEventId, start, counter);
            transactionCounter = 0L;

            if (ex != null && !hasError) {
                throw new HistoryModelException(controllerConfiguration.getCurrent().getId(), String.format(
                        "[%s][%s][error on store lastSuccessEventId=%s]%s", identifier, method, lastSuccessEventId, ex.toString()), ex);
            }

        }

        return storedEventId;
    }

    private boolean hasOrderStarted(String orderId, boolean currentIsStarted) {
        if (!currentIsStarted) {
            return cacheHandler.hasOrder(orderId);
        }
        return true;
    }

    // Another thread
    public void updateHistoryConfiguration(JocHistoryConfiguration config) {
        historyConfiguration = config;
        // if(logExtHandler)
    }

    // OrderStarted
    private void postEventOrderStarted(HistoryOrderBean hob) {
        if (hob == null) {
            return;
        }

        EventBus.getInstance().post(new HistoryOrderStarted(controllerConfiguration.getCurrent().getId(), hob.getOrderId(), hob.getWorkflowPath(), hob
                .getWorkflowVersionId(), hob));
    }

    // OrderCancelled, OrderFinished, OrderBroken
    private void postEventOrderTerminated(HistoryOrderBean hob) {
        if (hob == null) {
            return;
        }
        cacheHandler.clear(CacheType.orderWithChildOrders, hob.getOrderId());

        EventBus.getInstance().post(new HistoryOrderTerminated(controllerConfiguration.getCurrent().getId(), hob.getOrderId(), hob.getWorkflowPath(),
                hob.getWorkflowVersionId(), hob));
    }

    private void postEventOrderUpdated(HistoryOrderBean hob) {
        if (hob == null) {
            return;
        }

        EventBus.getInstance().post(new HistoryOrderUpdated(controllerConfiguration.getCurrent().getId(), hob.getOrderId(), hob.getWorkflowPath(), hob
                .getWorkflowVersionId(), hob));
    }

    private void postEventOrderTaskStarted(DBLayerHistory dbLayer, FatEventOrderStepStarted evt, HistoryOrderStepBean hosb) {
        if (hosb == null) {
            return;
        }

        CachedOrder co = null;
        try {
            co = cacheHandler.getOrderByCurrentOrderId(dbLayer, evt.getOrderId(), evt.getEventId());
        } catch (Exception e) {
            //
        }
        if (co != null) {
            EventBus.getInstance().post(new HistoryOrderTaskStarted(controllerConfiguration.getCurrent().getId(), co.getOrderId(), co
                    .getWorkflowPath(), co.getWorkflowVersionId(), hosb));
        }
    }

    private void postEventOrderTaskTerminated(DBLayerHistory dbLayer, FatEventOrderStepProcessed evt, HistoryOrderStepBean hosb) {
        if (hosb == null) {
            return;
        }
        cacheHandler.clear(CacheType.orderStep, hosb.getOrderId());

        CachedOrder co = null;
        try {
            co = cacheHandler.getOrderByCurrentOrderId(dbLayer, evt.getOrderId(), evt.getEventId());
        } catch (Exception e) {
            //
        }
        if (co != null) {
            EventBus.getInstance().post(new HistoryOrderTaskTerminated(controllerConfiguration.getCurrent().getId(), co.getOrderId(), co
                    .getWorkflowPath(), co.getWorkflowVersionId(), hosb));
        }
    }

    private void postEventTaskLogFirstStderr(Long eventId, CachedOrder co, CachedOrderStep cos, CachedWorkflowJob job) {
        if (co != null && cos != null && cos.getFirstChunkStdError() != null) {
            HistoryOrderStepBean hosb = cos.convert(EventType.OrderStderrWritten, eventId, controllerConfiguration.getCurrent().getId(), co
                    .getWorkflowPath());
            hosb.setCriticality(getJobCriticality(job));
            EventBus.getInstance().post(new HistoryOrderTaskLogFirstStderr(controllerConfiguration.getCurrent().getId(), hosb));
        }
    }

    private void postEventTaskLog(LogEntry entry, String content, boolean newLine) {
        EventBus.getInstance().post(new HistoryOrderTaskLog(entry.getEventType().value(), entry.getHistoryOrderId(), entry.getHistoryOrderStepId(),
                content, newLine));
    }

    private void postEventOrderLog(LogEntry entry, OrderLogEntry orderEntry) {
        EventBus.getInstance().post(new HistoryOrderLog(entry.getEventType().value(), entry.getHistoryOrderId(), orderEntry));
    }

    private Duration showSummary(Long startEventId, Long firstEventId, Instant start, Counter counter) {
        String startEventIdAsTime = eventIdAsTime(startEventId);
        String endEventIdAsTime = eventIdAsTime(storedEventId);
        String firstEventIdAsTime = eventIdAsTime(firstEventId);
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        LOGGER.info(String.format("[%s][%s(%s)-%s][UTC][%s(%s)-%s][%s-%s][%s]%s%s", identifier, startEventId, firstEventId, storedEventId,
                startEventIdAsTime, firstEventIdAsTime, endEventIdAsTime, SOSDate.getTimeAsString(start), SOSDate.getTimeAsString(end), SOSDate
                        .getDuration(duration), counter.toString(), cacheHandler.getCachedSummary()));
        return duration;
    }

    private String eventIdAsTime(Long eventId) {
        return eventId.equals(Long.valueOf(0)) ? "0" : SOSDate.getTimeAsString(JocClusterUtil.eventId2Instant(eventId));
    }

    public void close(StartupMode mode) {
        closed = true;

        if (logExtHandler != null) {
            logExtHandler.close(mode);
        }
    }

    private void tryStoreCurrentState(DBLayerHistory dbLayer, Long eventId) throws Exception {
        if (transactionCounter % maxTransactions == 0) {
            storeCurrentState(dbLayer, eventId);
            dbLayer.beginTransaction();
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
            dbLayer.beginTransaction();
        }
        dbLayer.updateControllerVariable(variableName, eventId);
        dbLayer.commit();
        storedEventId = eventId;

        logExtHandler.process();
    }

    private void controllerReady(DBLayerHistory dbLayer, FatEventControllerReady event) throws Exception {
        try {
            DBItemHistoryController item = new DBItemHistoryController();
            item.setReadyEventId(event.getEventId());
            item.setControllerId(controllerConfiguration.getCurrent().getId());
            item.setUri(controllerConfiguration.getCurrent().getUri());
            item.setTimezone(HistoryUtil.getTimeZone("controllerReady " + item.getControllerId(), event.getTimezone()));
            item.setReadyTime(event.getEventDatetime());
            item.setLastKnownTime(item.getReadyTime());
            item.setTotalRunningTime(event.getTotalRunningTime());
            item.setCreated(new Date());
            dbLayer.getSession().save(item);

            controllerTimezone = item.getTimezone();
            setPreviousControllerLastKnownTime(dbLayer, item);
            tryStoreCurrentState(dbLayer, event.getEventId());
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                throw e;
            }
            LOGGER.info(String.format("[%s][ConstraintViolation][%s][eventId=%s]%s", identifier, event.getType(), event.getEventId(), e.toString()));
        } finally {
            if (controllerTimezone == null) {
                controllerTimezone = HistoryUtil.getTimeZone("controllerReady " + controllerConfiguration.getCurrent().getId(), event.getTimezone());
            }
        }
    }

    private void setPreviousControllerLastKnownTime(DBLayerHistory dbLayer, DBItemHistoryController current) throws Exception {
        DBItemHistoryController previous = dbLayer.getControllerByNextEventId(controllerConfiguration.getCurrent().getId(), current
                .getReadyEventId());
        if (previous != null && previous.getShutdownTime() == null) {
            Date knownTime = getLastKnownTime(dbLayer, previous.getLastKnownTime(), previous.getReadyEventId(), current.getReadyEventId(), null);
            if (knownTime != null) {
                previous.setLastKnownTime(knownTime);
                dbLayer.getSession().update(previous);
            }
        }
    }

    private Date getLastKnownTime(DBLayerHistory dbLayer, Date itemLastKnownTime, Long fromEventId, Long toEventId, String agentId) throws Exception {
        Object[] result = dbLayer.getLastExecution(controllerConfiguration.getCurrent().getId(), fromEventId, toEventId, agentId);
        if (result != null) {
            Date startTime = (Date) result[0];
            Date endTime = (Date) result[1];
            Date knownTime = endTime == null ? startTime : endTime;
            if (itemLastKnownTime == null) {
                return knownTime;
            } else if (knownTime.getTime() > itemLastKnownTime.getTime()) {
                return knownTime;
            }
        }
        return null;
    }

    private void controllerShutDown(DBLayerHistory dbLayer, FatEventControllerShutDown event) throws Exception {
        DBItemHistoryController item = dbLayer.getControllerByNextEventId(controllerConfiguration.getCurrent().getId(), event.getEventId());
        if (item == null) {
            LOGGER.info(String.format("[%s][%s][%s][skip]not found controller entry with the ready time < %s", identifier, event.getType(),
                    controllerConfiguration.getCurrent().getId(), getDateAsString(event.getEventDatetime())));
        } else {
            if (item.getShutdownTime() == null) {
                item.setShutdownTime(event.getEventDatetime());
                item.setLastKnownTime(item.getShutdownTime());
                dbLayer.getSession().update(item);
            } else {
                LOGGER.info(String.format("[%s][%s][%s][skip]found with the ready time < %s and shutdown time=%s", identifier, event.getType(),
                        controllerConfiguration.getCurrent().getId(), getDateAsString(event.getEventDatetime()), getDateAsString(item
                                .getShutdownTime())));
            }
            if (controllerTimezone == null) {
                controllerTimezone = HistoryUtil.getTimeZone("controllerShutDown " + item.getControllerId(), item.getTimezone());
            }
        }
        tryStoreCurrentState(dbLayer, event.getEventId());
    }

    private void checkControllerTimezone(DBLayerHistory dbLayer) throws Exception {
        if (controllerTimezone == null) {
            controllerTimezone = HistoryUtil.getTimeZone("checkControllerTimezone " + controllerConfiguration.getCurrent().getId(), dbLayer
                    .getLastControllerTimezone(controllerConfiguration.getCurrent().getId()));
        }
    }

    private void agentCouplingFailed(DBLayerHistory dbLayer, FatEventAgentCouplingFailed event) throws Exception {
        DBItemHistoryAgent item = dbLayer.getAgentByNextEventId(controllerConfiguration.getCurrent().getId(), event.getId(), event.getEventId());
        if (item == null) {
            LOGGER.info(String.format("[%s][%s][%s][skip]not found agent entry with the ready time < %s", identifier, event.getType(), event.getId(),
                    getDateAsString(event.getEventDatetime())));
        } else {
            if (item.getShutdownTime() == null) {
                if (item.getCouplingFailedTime() == null) {
                    item.setCouplingFailedMessage(event.getMessage());
                    item.setCouplingFailedTime(event.getEventDatetime());
                    item.setLastKnownTime(item.getCouplingFailedTime());
                    tmpAgentShuttingDown(item);
                    dbLayer.getSession().update(item);
                } else {
                    cacheHandler.addAgentsCouplingFailed(item.getAgentId(), event.getEventId(), event.getMessage());
                }
            }
        }
        tryStoreCurrentState(dbLayer, event.getEventId());
    }

    private void agentShutDown(DBLayerHistory dbLayer, FatEventAgentShutDown event) throws Exception {
        DBItemHistoryAgent item = dbLayer.getAgentByNextEventId(controllerConfiguration.getCurrent().getId(), event.getId(), event.getEventId());
        if (item == null) {
            LOGGER.info(String.format("[%s][%s][%s][skip]not found agent entry with the ready time < %s", identifier, event.getType(), event.getId(),
                    getDateAsString(event.getEventDatetime())));
        } else {
            if (item.getShutdownTime() == null || isAgentCouplingFailedBecauseShutdown(item)) {
                item.setShutdownTime(event.getEventDatetime());
                item.setLastKnownTime(item.getShutdownTime());
                dbLayer.getSession().update(item);
            }
            cacheHandler.removeAgentsCouplingFailed(item.getAgentId());
        }
        tryStoreCurrentState(dbLayer, event.getEventId());
    }

    private void agentReady(DBLayerHistory dbLayer, FatEventAgentReady event) throws Exception {
        try {
            checkControllerTimezone(dbLayer);

            DBItemHistoryAgent item = new DBItemHistoryAgent();
            item.setReadyEventId(event.getEventId());
            item.setControllerId(controllerConfiguration.getCurrent().getId());
            item.setAgentId(event.getId());
            item.setUri(event.getUri());
            item.setTimezone(HistoryUtil.getTimeZone("agentReady " + item.getAgentId(), event.getTimezone()));
            item.setReadyTime(event.getEventDatetime());
            item.setCouplingFailedTime(null);
            item.setLastKnownTime(item.getReadyTime());
            item.setCreated(new Date());

            dbLayer.getSession().save(item);

            setPreviousAgentLastKnownTime(dbLayer, item);
            tryStoreCurrentState(dbLayer, event.getEventId());
            cacheHandler.addAgent(item.getAgentId(), new CachedAgent(item));
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                throw e;
            }
            LOGGER.info(String.format("[%s][ConstraintViolation][%s][eventId=%s]%s", identifier, event.getType(), event.getEventId(), e.toString()));
            cacheHandler.addAgentByReadyEventId(dbLayer, event.getId(), event.getEventId());
        }
    }

    private void setPreviousAgentLastKnownTime(DBLayerHistory dbLayer, DBItemHistoryAgent current) throws Exception {
        DBItemHistoryAgent previous = dbLayer.getAgentByNextEventId(controllerConfiguration.getCurrent().getId(), current.getAgentId(), current
                .getReadyEventId());
        if (previous != null && previous.getShutdownTime() == null) {
            boolean update = false;
            Date knownTime = getLastKnownTime(dbLayer, previous.getLastKnownTime(), previous.getReadyEventId(), current.getReadyEventId(), current
                    .getAgentId());
            if (knownTime != null) {
                previous.setLastKnownTime(knownTime);
                update = true;
            }
            AgentCouplingFailed cf = cacheHandler.getLastAgentCouplingFailed(current.getAgentId(), current.getReadyEventId());
            if (cf != null) {
                previous.setCouplingFailedTime(JocClusterUtil.getEventIdAsDate(cf.getEventId()));
                previous.setCouplingFailedMessage(cf.getMessage());

                if (previous.getLastKnownTime() != null) {
                    if (previous.getCouplingFailedTime().getTime() > previous.getLastKnownTime().getTime()) {
                        previous.setLastKnownTime(previous.getCouplingFailedTime());
                    }
                }
                tmpAgentShuttingDown(previous);
                update = true;
            }
            if (update) {
                dbLayer.getSession().update(previous);
            }
        }
        cacheHandler.removeAgentsCouplingFailed(current.getAgentId());
    }

    private void tmpAgentShuttingDown(DBItemHistoryAgent item) {
        if (isAgentCouplingFailedBecauseShutdown(item)) {
            item.setShutdownTime(item.getCouplingFailedTime());
            item.setLastKnownTime(item.getShutdownTime());
        }
    }

    private boolean isAgentCouplingFailedBecauseShutdown(DBItemHistoryAgent item) {
        return item.getCouplingFailedMessage() != null && item.getCouplingFailedMessage().toLowerCase().contains(
                AGENT_COUPLING_FAILED_SHUTDOWN_MESSAGE);
    }

    private HistoryOrderBean orderStarted(DBLayerHistory dbLayer, FatEventOrderStarted eo) throws Exception {
        String constraintHash = hashOrderConstraint(eo.getEventId(), eo.getOrderId());
        try {
            checkControllerTimezone(dbLayer);

            DBItemHistoryOrder item = new DBItemHistoryOrder();
            item.setControllerId(controllerConfiguration.getCurrent().getId());
            item.setOrderId(eo.getOrderId());

            String workflowName = JocClusterUtil.getBasenameFromPath(eo.getWorkflowPath());
            CachedWorkflow cw = cacheHandler.getWorkflow(dbLayer, workflowName, eo.getWorkflowVersionId());

            item.setWorkflowPath(cw.getPath());
            item.setWorkflowVersionId(eo.getWorkflowVersionId());
            item.setWorkflowPosition(SOSString.isEmpty(eo.getPosition()) ? "0" : eo.getPosition());
            item.setWorkflowFolder(HistoryUtil.getFolderFromPath(item.getWorkflowPath()));
            item.setWorkflowName(workflowName);
            item.setWorkflowTitle(cw.getTitle());

            item.setMainParentId(Long.valueOf(0));// TODO see below
            item.setParentId(Long.valueOf(0));
            item.setParentOrderId(null);
            item.setHasChildren(false);
            item.setRetryCounter(HistoryPosition.getRetry(eo.getPosition()));

            item.setName(eo.getOrderId());
            item.setStartCause(OrderStartCause.order.name());// TODO

            item.setStartTimeScheduled(eo.getScheduledFor() == null ? eo.getEventDatetime() : eo.getScheduledFor());
            item.setStartTime(eo.getEventDatetime());
            item.setStartWorkflowPosition(item.getWorkflowPosition());
            item.setStartEventId(eo.getEventId());

            Variables arguments = HistoryUtil.toVariables(eo.getArguments(), cw.getOrderPreparation());
            item.setStartVariables(HistoryUtil.toJsonString(arguments));

            item.setCurrentHistoryOrderStepId(Long.valueOf(0));

            item.setEndTime(null);
            item.setEndWorkflowPosition(null);
            item.setEndHistoryOrderStepId(Long.valueOf(0));
            item.setEndReturnCode(null);
            item.setEndMessage(null);

            item.setSeverity(OrderStateText.RUNNING);
            item.setState(OrderStateText.RUNNING);

            item.setStateTime(eo.getEventDatetime());
            item.setStateText(null);
            item.setHasStates(false);

            item.setError(false);
            item.setErrorState(null);
            item.setErrorReason(null);
            item.setErrorReturnCode(null);
            item.setErrorCode(null);
            item.setErrorText(null);
            item.setEndEventId(null);

            item.setLogId(Long.valueOf(0));

            item.setConstraintHash(constraintHash);
            item.setCreated(new Date());
            item.setModified(item.getCreated());

            dbLayer.getSession().save(item);

            item.setMainParentId(item.getId()); // TODO see above
            dbLayer.setMainParentId(item.getId(), item.getMainParentId());

            // if (eo.maybePreviousStatesLogged()) {
            handleNotStartedOrderLog(item.getOrderId(), item.getId());
            // }

            LogEntry le = new LogEntry(OrderLogEntryLogLevel.MAIN, EventType.OrderStarted, eo.getEventDatetime(), null);
            CachedOrder co = new CachedOrder(item);
            le.onOrder(co, item.getWorkflowPosition());
            le.setArguments(arguments);
            storeLog2File(le);
            cacheHandler.addOrder(item.getOrderId(), co);

            tryStoreCurrentState(dbLayer, eo.getEventId());

            return new HistoryOrderBean(EventType.OrderStarted, eo.getEventId(), item);
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                throw e;
            }
            LOGGER.info(String.format("[%s][ConstraintViolation][%s][eventId=%s][%s]%s", identifier, eo.getType(), eo.getEventId(), eo.getOrderId(), e
                    .toString()));

            StringBuilder sb = new StringBuilder(controllerConfiguration.getCurrent().getId());
            sb.append("-").append(eo.getEventId());
            sb.append("-").append(eo.getOrderId());

            cacheHandler.getOrderByConstraint(dbLayer, constraintHash, eo.getOrderId(), sb.toString());
            return null;
        }
    }

    private void handleNotStartedOrderLog(String orderId, Long mainParentId) {
        String method = "handleNotStartedOrderLog";
        try {
            Path notStartedOrderLog = HistoryUtil.getOrderLog(historyConfiguration.getLogDirTmpOrders(), orderId);
            if (notStartedOrderLog == null) {
                return;
            }
            if (notStartedOrderLog.toFile().exists()) {
                Path orderDir = HistoryUtil.getOrderLogDirectory(historyConfiguration.getLogDir(), mainParentId);
                Path orderLog = HistoryUtil.getOrderLog(orderDir, mainParentId);
                boolean removeNotStartedOrderLog = false;
                if (Files.exists(orderDir)) {
                    if (Files.exists(orderLog)) {
                        removeNotStartedOrderLog = true;
                        LOGGER.info(String.format("[%s][%s][%s][skip move file][order log file already exists]%s", method, orderId,
                                notStartedOrderLog, orderLog));
                    }
                } else {
                    try {
                        Files.createDirectories(orderDir);
                    } catch (Throwable e) {
                        removeNotStartedOrderLog = true;
                        LOGGER.warn(String.format("[%s][%s][%s][cannot create directory=%s]%s", method, orderId, notStartedOrderLog, orderDir, e
                                .toString()), e);
                    }
                }

                try {
                    if (removeNotStartedOrderLog) {
                        Files.delete(notStartedOrderLog);
                        LOGGER.info(String.format("[%s][%s][%s]tmp order log file deleted", method, orderId, notStartedOrderLog));
                    } else {
                        SOSPath.renameTo(notStartedOrderLog, orderLog);
                    }
                } catch (Throwable e) {
                    LOGGER.warn(String.format("[%s][%s][%s]%s", method, orderId, notStartedOrderLog, e.toString()), e);
                }
            }
        } catch (Throwable e) {
            LOGGER.warn(String.format("[%s][%s]%s", method, orderId, e.toString()), e);
        }
    }

    private HistoryOrderBean orderTerminated(DBLayerHistory dbLayer, AFatEventOrderProcessed eo, EventType eventType,
            Map<String, CachedOrderStep> endedOrderSteps, FatInstruction instruction) throws Exception {
        return orderUpdate(dbLayer, eventType, eo.getEventId(), eo.getOrderId(), eo.getEventDatetime(), eo.getOutcome(), eo.getPosition(),
                endedOrderSteps, instruction, true);
    }

    private HistoryOrderBean orderNotCompleted(DBLayerHistory dbLayer, AFatEventOrderProcessed eo, EventType eventType,
            Map<String, CachedOrderStep> endedOrderSteps, FatInstruction instruction) throws Exception {
        return orderUpdate(dbLayer, eventType, eo.getEventId(), eo.getOrderId(), eo.getEventDatetime(), eo.getOutcome(), eo.getPosition(),
                endedOrderSteps, instruction, false);
    }

    private HistoryOrderBean orderUpdate(DBLayerHistory dbLayer, EventType eventType, Long eventId, String orderId, Date eventDate,
            FatOutcome outcome, String position, Map<String, CachedOrderStep> endedOrderSteps, FatInstruction instruction, boolean terminateOrder)
            throws Exception {

        HistoryOrderBean hob = null;
        CachedOrder co = null;
        if (EventType.OrderCancelled.equals(eventType)) {
            try {
                co = cacheHandler.getOrderByCurrentOrderId(dbLayer, orderId, eventId);
            } catch (HistoryModelOrderNotFoundException e) {
                return null;
            }
        } else {
            co = cacheHandler.getOrderByCurrentOrderId(dbLayer, orderId, eventId);
        }

        if (co.getEndTime() == null) {
            checkControllerTimezone(dbLayer);

            CachedOrderStep cos = getCurrentOrderStep(dbLayer, co, endedOrderSteps);
            LogEntry le = createOrderLogEntry(eventType, eventId, outcome, co);
            co.setState(le.getState());

            Date endTime = null;
            String endWorkflowPosition = null;
            Long endHistoryOrderStepId = null;
            Long endEventId = null;
            Integer endReturnCode = null;
            String endMessage = null;
            Long currentHistoryOrderStepId = (cos == null) ? co.getCurrentHistoryOrderStepId() : cos.getId();
            if (terminateOrder) {
                endTime = eventDate;
                endWorkflowPosition = (cos == null) ? co.getWorkflowPosition() : cos.getWorkflowPosition();
                endHistoryOrderStepId = currentHistoryOrderStepId;
                endEventId = eventId;
            }

            String orderErrorText = le.getErrorText();
            String stateErrorText = null;
            switch (eventType) {
            case OrderJoined:
                if (le.isError()) {
                    co.setHasStates(true);
                }
                le.setLogLevel(OrderLogEntryLogLevel.DETAIL);
                break;
            case OrderSuspended:
                le.setInstruction(instruction);
            case OrderBroken:
            case OrderCancelled:
            case OrderSuspensionMarked:
            case OrderResumed:
            case OrderResumptionMarked:
                co.setHasStates(true);
                break;
            case OrderFailed:
            case OrderStopped:
                co.setHasStates(true);
                stateErrorText = orderErrorText;
                break;
            case OrderFinished:
                if (outcome != null) {
                    endReturnCode = outcome.getReturnCode();
                    if (outcome.isSucceeded()) {
                        if (outcome.getNamedValues() != null && outcome.getNamedValues().containsKey(RETURN_MESSAGE_KEY)) {
                            String rm = HistoryUtil.toString(outcome.getNamedValues().get(RETURN_MESSAGE_KEY));
                            if (!SOSString.isEmpty(rm)) {
                                endMessage = rm;
                                le.setReturnMessage(endMessage);
                            }
                        }
                    } else {
                        endMessage = orderErrorText;
                    }
                }
                break;
            default:
                break;
            }
            dbLayer.setOrderEnd(co.getId(), le.getState(), eventDate, co.getHasStates(), le.isError(), le.getErrorState(), le.getErrorReason(), le
                    .getReturnCode(), le.getErrorCode(), orderErrorText, endTime, endWorkflowPosition, endHistoryOrderStepId, endEventId,
                    endReturnCode, endMessage);

            if (co.getHasStates()) {
                saveOrderState(dbLayer, co, le.getState(), eventDate, eventId, le.getErrorCode(), stateErrorText);
            }

            hob = co.convert(eventType, eventId, controllerConfiguration.getCurrent().getId());
            hob.setCurrentHistoryOrderStepId(currentHistoryOrderStepId);
            hob.setEndTime(endTime);
            hob.setEndWorkflowPosition(endWorkflowPosition);
            hob.setEndHistoryOrderStepId(endHistoryOrderStepId);
            hob.setEndReturnCode(endReturnCode);
            hob.setEndMessage(endMessage);
            hob.setState(le.getState());
            hob.setStateTime(eventDate);
            hob.setSeverity(HistorySeverity.map2DbSeverity(hob.getState()));
            hob.setError(le.isError());
            hob.setErrorState(le.getErrorState());
            hob.setErrorReason(le.getErrorReason());
            hob.setErrorReturnCode(le.getReturnCode());
            hob.setErrorCode(le.getErrorCode());
            hob.setErrorText(orderErrorText);

            if (terminateOrder) {
                if (isStartTimeAfterEndTime(hob.getStartTime(), hob.getEndTime())) {
                    LOGGER.warn(String.format("[%s][%s][%s][startTime=%s > endTime=%s]%s", identifier, eventType, orderId, SOSDate
                            .getDateTimeAsString(hob.getStartTime()), SOSDate.getDateTimeAsString(hob.getEndTime()), SOSString.toString(hob)));
                }
            }

            le.onOrder(co, position == null ? co.getWorkflowPosition() : position);
            Path log = storeLog2File(le);
            // if (completeOrder && co.getParentId().longValue() == 0L) {
            if (terminateOrder) {
                storeLogFile2Db(dbLayer, log, hob, null);
                cacheHandler.clear(CacheType.order, orderId);
            }
            tryStoreCurrentState(dbLayer, eventId);
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][skip][%s]order is already completed[%s]", identifier, eventType, orderId, SOSString.toString(
                        co)));
            }
            cacheHandler.clear(CacheType.order, co.getOrderId());
            co = null;
            hob = null;
        }
        return hob;
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
                    LOGGER.info(String.format("[%s][%s][currentStep not found]id=%s", identifier, co.getOrderId(), co
                            .getCurrentHistoryOrderStepId()));
                } else {
                    CachedAgent ca = cacheHandler.getAgent(dbLayer, item.getAgentId(), controllerTimezone);
                    step = new CachedOrderStep(item, ca.getTimezone());
                }
            } else if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][currentStep found]%s", identifier, co.getOrderId(), SOSString.toString(step)));
            }
        }
        return step;
    }

    private LogEntry createOrderLogEntry(EventType eventType, Long eventId, FatOutcome outcome, CachedOrder co) {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        LogEntry le = new LogEntry(OrderLogEntryLogLevel.DETAIL, eventType, JocClusterUtil.getEventIdAsDate(eventId), null);

        if (outcome != null) {
            le.setReturnCode(outcome.getReturnCode());
            if (outcome.isFailed()) {
                boolean setError = true;
                if (eventType.equals(EventType.OrderJoined) && co.getLastStepError() == null) {
                    setError = false;
                }
                if (setError) {
                    le.setError(co, outcome);
                }
            } else if (outcome.isSucceeded()) {
                co.resetLastStepError();
            }
        }
        if (!le.isError()) {
            if (co.getLastStepError() != null) {
                le.setError(OrderStateText.FAILED.value(), co);
                le.setReturnCode(co.getLastStepError().getReturnCode());
            }
        }
        if (le.isError()) {
            co.setLastStepError(le);
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s]co.setLastStepError", eventType, co.getOrderId()));
            }
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s][isError=%s][co.getLastStepError=%s]", eventType, co.getOrderId(), le.isError(), SOSString.toString(co
                    .getLastStepError())));
        }

        switch (eventType) {
        case OrderOutcomeAdded:
            if (le.isError()) {
                le.setState(OrderStateText.FAILED.intValue());
                // le.setLogLevel(LogLevel.ERROR);
            } else {
                le.setState(OrderStateText.RUNNING.intValue());
            }
            break;
        case OrderFailed:
        case OrderFailedinFork:
        case OrderStopped:
            le.setState(OrderStateText.FAILED.intValue());
            le.setLogLevel(OrderLogEntryLogLevel.ERROR);
            break;
        case OrderBroken:
            le.setState(OrderStateText.BROKEN.intValue());
            le.setLogLevel(OrderLogEntryLogLevel.ERROR);
            break;
        case OrderCancelled:
            le.setState(OrderStateText.CANCELLED.intValue());
            if (le.isError()) {
                le.setLogLevel(OrderLogEntryLogLevel.ERROR);
            } else {
                le.setLogLevel(OrderLogEntryLogLevel.MAIN);
            }
            break;
        case OrderSuspended:
            le.setState(OrderStateText.SUSPENDED.intValue());
            le.setLogLevel(OrderLogEntryLogLevel.MAIN);
            break;
        case OrderSuspensionMarked:
            le.setState(OrderStateText.SUSPENDED.intValue());// TODO check
            le.setLogLevel(OrderLogEntryLogLevel.INFO);
            break;
        case OrderResumed:
            le.setState(OrderStateText.RUNNING.intValue());// TODO check
            le.setLogLevel(OrderLogEntryLogLevel.MAIN);
            break;
        case OrderResumptionMarked:
            le.setState(OrderStateText.RUNNING.intValue()); // TODO check
            le.setLogLevel(OrderLogEntryLogLevel.INFO);
            break;
        default:
            if (le.isError()) {
                le.setState(OrderStateText.FAILED.intValue());
                le.setLogLevel(OrderLogEntryLogLevel.ERROR);
            } else {
                le.setState(OrderStateText.FINISHED.intValue());
                le.setLogLevel(OrderLogEntryLogLevel.MAIN);
            }
            break;
        }
        return le;
    }

    private HistoryOrderBean orderResumed(DBLayerHistory dbLayer, FatEventOrderResumed eo) throws Exception {
        checkControllerTimezone(dbLayer);
        HistoryOrderBean hob = null;
        LogEntry le = new LogEntry(OrderLogEntryLogLevel.MAIN, EventType.OrderResumed, eo.getEventDatetime(), null);
        le.setInstruction(eo.getInstruction());
        if (hasOrderStarted(eo.getOrderId(), eo.isStarted())) {
            CachedOrder co = cacheHandler.getOrderByCurrentOrderId(dbLayer, eo.getOrderId(), eo.getEventId());
            co.setState(OrderStateText.RUNNING.intValue());
            co.setHasStates(true);
            // addCachedOrder(co.getOrderId(), co);

            dbLayer.updateOrderOnResumed(co.getId(), co.getState(), eo.getEventDatetime());
            saveOrderState(dbLayer, co, co.getState(), eo.getEventDatetime(), eo.getEventId(), null, null);

            le.onOrder(co, eo.getPosition());
            storeLog2File(le);

            hob = co.convert(EventType.OrderResumed, eo.getEventId(), controllerConfiguration.getCurrent().getId());
            hob.setStateTime(eo.getEventDatetime());
        } else {
            le.onNotStartedOrder(eo.getOrderId(), eo.getPosition());
            storeLog2File(le);
        }
        return hob;
    }

    private void orderLog(DBLayerHistory dbLayer, AFatEventOrderProcessed eo, EventType eventType, OrderLogEntryLogLevel logLevel,
            FatInstruction instruction, boolean isOrderStarted) throws Exception {
        checkControllerTimezone(dbLayer);

        LogEntry le = new LogEntry(logLevel, eventType, eo.getEventDatetime(), null);
        le.setInstruction(instruction);
        if (isOrderStarted) {
            CachedOrder co = cacheHandler.getOrderByCurrentOrderId(dbLayer, eo.getOrderId(), eo.getEventId());
            le.onOrder(co, eo.getPosition());
        } else {
            le.onNotStartedOrder(eo.getOrderId(), eo.getPosition());
        }
        storeLog2File(le);
    }

    private void orderLog(DBLayerHistory dbLayer, AFatEventOrderBase eo, EventType eventType) throws Exception {
        checkControllerTimezone(dbLayer);

        CachedOrder co = cacheHandler.getOrderByCurrentOrderId(dbLayer, eo.getOrderId(), eo.getEventId());

        LogEntry le = new LogEntry(OrderLogEntryLogLevel.DETAIL, eventType, eo.getEventDatetime(), null);
        le.onOrderBase(co, eo.getPosition(), eo);
        storeLog2File(le);
    }

    private void orderLogNotice(DBLayerHistory dbLayer, AFatEventOrderNotice eo, EventType eventType) throws Exception {
        checkControllerTimezone(dbLayer);

        CachedOrder co = cacheHandler.getOrderByCurrentOrderId(dbLayer, eo.getOrderId(), eo.getEventId());

        LogEntry le = new LogEntry(OrderLogEntryLogLevel.DETAIL, eventType, eo.getEventDatetime(), null);
        le.onOrderNotice(co, eo);
        storeLog2File(le);
    }

    private void orderLogMoved(DBLayerHistory dbLayer, FatEventOrderMoved eo, EventType eventType) throws Exception {
        checkControllerTimezone(dbLayer);

        LogEntry le = new LogEntry(OrderLogEntryLogLevel.DETAIL, eventType, eo.getEventDatetime(), null);

        CachedOrder co = null;
        if (eo.isStarted()) {
            co = cacheHandler.getOrderByCurrentOrderId(dbLayer, eo.getOrderId(), eo.getEventId());
        }
        le.onOrderMoved(co, eo);
        storeLog2File(le);
    }

    private void orderLogAttached(DBLayerHistory dbLayer, FatEventOrderAttached eo, EventType eventType) throws Exception {
        checkControllerTimezone(dbLayer);

        LogEntry le = new LogEntry(OrderLogEntryLogLevel.DETAIL, eventType, eo.getEventDatetime(), null);

        CachedOrder co = null;
        if (eo.isStarted()) {
            co = cacheHandler.getOrderByCurrentOrderId(dbLayer, eo.getOrderId(), eo.getEventId());
        }
        le.onOrderAttached(co, eo);
        storeLog2File(le);
    }

    private void orderLock(DBLayerHistory dbLayer, AFatEventOrderLocks eo, EventType eventType) throws Exception {
        checkControllerTimezone(dbLayer);

        CachedOrder co = cacheHandler.getOrderByCurrentOrderId(dbLayer, eo.getOrderId(), eo.getEventId());

        LogEntry le = new LogEntry(OrderLogEntryLogLevel.DETAIL, eventType, eo.getEventDatetime(), null);
        le.onOrderLock(co, eo);
        storeLog2File(le);
    }

    private HistoryOrderBean orderForked(DBLayerHistory dbLayer, FatEventOrderForked eo) throws Exception {
        checkControllerTimezone(dbLayer);

        CachedOrder co = cacheHandler.getOrderByCurrentOrderId(dbLayer, eo.getOrderId(), eo.getEventId());
        co.setState(OrderStateText.RUNNING.intValue());

        dbLayer.updateOrderOnFork(co.getId(), co.getState(), eo.getEventDatetime());

        LogEntry le = new LogEntry(OrderLogEntryLogLevel.DETAIL, EventType.OrderForked, eo.getEventDatetime(), null);
        le.onOrder(co, eo.getPosition(), eo.getChilds());
        storeLog2File(le);

        List<HistoryOrderBean> children = new ArrayList<HistoryOrderBean>();
        for (FatForkedChild fc : eo.getChilds()) {
            children.add(orderForkedStarted(dbLayer, eo, co, fc));
        }

        HistoryOrderBean hob = co.convert(EventType.OrderForked, eo.getEventId(), controllerConfiguration.getCurrent().getId());
        hob.setStateTime(eo.getEventDatetime());
        hob.setChildren(children);
        return hob;
    }

    private HistoryOrderBean orderForkedStarted(DBLayerHistory dbLayer, FatEventOrderForked eo, CachedOrder parentOrder, FatForkedChild forkOrder)
            throws Exception {
        String constraintHash = hashOrderConstraint(eo.getEventId(), forkOrder.getOrderId());
        try {
            checkControllerTimezone(dbLayer);

            DBItemHistoryOrder item = new DBItemHistoryOrder();
            item.setControllerId(controllerConfiguration.getCurrent().getId());
            item.setOrderId(forkOrder.getOrderId());

            String workflowName = JocClusterUtil.getBasenameFromPath(eo.getWorkflowPath());
            CachedWorkflow cw = cacheHandler.getWorkflow(dbLayer, workflowName, eo.getWorkflowVersionId());

            item.setWorkflowPath(cw.getPath());
            item.setWorkflowVersionId(eo.getWorkflowVersionId());
            item.setWorkflowPosition(forkOrder.getPosition());
            item.setWorkflowFolder(HistoryUtil.getFolderFromPath(item.getWorkflowPath()));
            item.setWorkflowName(workflowName);
            item.setWorkflowTitle(cw.getTitle());

            item.setMainParentId(parentOrder.getMainParentId());
            item.setParentId(parentOrder.getId());
            item.setParentOrderId(parentOrder.getOrderId());
            item.setHasChildren(false);
            item.setRetryCounter(HistoryPosition.getRetry(eo.getPosition()));

            item.setName(forkOrder.getBranchIdOrName());
            item.setStartCause(OrderStartCause.fork.name());// TODO
            item.setStartTimeScheduled(eo.getEventDatetime());
            item.setStartTime(eo.getEventDatetime());
            item.setStartWorkflowPosition(SOSString.isEmpty(eo.getPosition()) ? "0" : eo.getPosition());
            item.setStartEventId(eo.getEventId());
            // item.setStartVariables(HistoryUtil.toJsonString(entry.getArguments())); // TODO or forkOrder arguments ???
            item.setStartVariables(null);

            item.setCurrentHistoryOrderStepId(Long.valueOf(0));

            item.setEndTime(null);
            item.setEndWorkflowPosition(null);
            item.setEndHistoryOrderStepId(Long.valueOf(0));
            item.setEndReturnCode(null);
            item.setEndMessage(null);

            item.setSeverity(OrderStateText.RUNNING);
            item.setState(OrderStateText.RUNNING);
            item.setStateTime(eo.getEventDatetime());
            item.setStateText(null);

            item.setError(false);
            item.setErrorState(null);
            item.setErrorReason(null);
            item.setErrorReturnCode(null);
            item.setErrorCode(null);
            item.setErrorText(null);
            item.setEndEventId(null);

            item.setLogId(Long.valueOf(0));

            item.setConstraintHash(constraintHash);
            item.setCreated(new Date());
            item.setModified(item.getCreated());

            dbLayer.getSession().save(item);

            LogEntry le = new LogEntry(OrderLogEntryLogLevel.DETAIL, EventType.OrderStarted, item.getStartTime(), null);
            CachedOrder co = new CachedOrder(item);
            le.onOrder(co, item.getWorkflowPosition());
            storeLog2File(le);
            cacheHandler.addOrder(item.getOrderId(), co);

            tryStoreCurrentState(dbLayer, eo.getEventId());

            return new HistoryOrderBean(EventType.OrderStarted, eo.getEventId(), item);
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                throw e;
            }
            LOGGER.info(String.format("[%s][ConstraintViolation][%s][eventId=%s][%s][%s]%s", identifier, eo.getType(), eo.getEventId(), eo
                    .getOrderId(), forkOrder.getBranchIdOrName(), e.toString()));

            StringBuilder sb = new StringBuilder(controllerConfiguration.getCurrent().getId());
            sb.append("-").append(eo.getEventId());
            sb.append("-").append(eo.getOrderId());

            cacheHandler.getOrderByConstraint(dbLayer, constraintHash, forkOrder.getOrderId(), sb.toString());
            return null;
        }
    }

    private HistoryOrderBean orderJoined(DBLayerHistory dbLayer, FatEventOrderJoined eo, Map<String, CachedOrderStep> endedOrderSteps)
            throws Exception {
        checkControllerTimezone(dbLayer);

        Date endTime = eo.getEventDatetime();
        List<HistoryOrderBean> children = new ArrayList<HistoryOrderBean>();
        for (FatForkedChild child : eo.getChilds()) {
            children.add(orderUpdate(dbLayer, EventType.OrderJoined, eo.getEventId(), child.getOrderId(), endTime, eo.getOutcome(), null,
                    endedOrderSteps, null, true));
        }

        CachedOrder co = cacheHandler.getOrderByCurrentOrderId(dbLayer, eo.getOrderId(), eo.getEventId());
        LogEntry le = new LogEntry(OrderLogEntryLogLevel.DETAIL, EventType.OrderJoined, JocClusterUtil.getEventIdAsDate(eo.getEventId()), null);
        le.onOrderJoined(co, eo.getOutcome(), eo.getPosition(), eo.getChilds().stream().map(s -> s.getOrderId()).collect(Collectors.toList()));
        storeLog2File(le);

        HistoryOrderBean hob = co.convert(EventType.OrderJoined, eo.getEventId(), controllerConfiguration.getCurrent().getId());
        hob.setChildren(children);
        return hob;
    }

    private HistoryOrderStepBean orderStepStarted(DBLayerHistory dbLayer, FatEventOrderStepStarted eos) throws Exception {
        CachedAgent ca = null;
        CachedOrder co = null;
        String constraintHash = null;

        try {
            checkControllerTimezone(dbLayer);

            ca = cacheHandler.getAgent(dbLayer, eos.getAgentId(), controllerTimezone);
            co = cacheHandler.getOrderByCurrentOrderId(dbLayer, eos.getOrderId(), eos.getEventId());

            Date agentStartTime = eos.getEventDatetime();

            DBItemHistoryOrderStep item = new DBItemHistoryOrderStep();
            item.setControllerId(controllerConfiguration.getCurrent().getId());
            item.setOrderId(eos.getOrderId());

            String workflowName = JocClusterUtil.getBasenameFromPath(eos.getWorkflowPath());
            CachedWorkflow cw = cacheHandler.getWorkflow(dbLayer, workflowName, eos.getWorkflowVersionId());
            CachedWorkflowJob job = cw.getJob(eos.getJobName());

            item.setWorkflowPath(cw.getPath());
            item.setWorkflowVersionId(eos.getWorkflowVersionId());
            item.setWorkflowPosition(eos.getPosition());
            item.setWorkflowFolder(HistoryUtil.getFolderFromPath(item.getWorkflowPath()));
            item.setWorkflowName(workflowName);

            item.setHistoryOrderMainParentId(co.getMainParentId());
            item.setHistoryOrderId(co.getId());
            item.setPosition(HistoryPosition.getLast(eos.getPosition()));
            item.setRetryCounter(HistoryPosition.getRetry(eos.getPosition()));

            item.setJobName(eos.getJobName());
            item.setJobLabel(eos.getJobLabel());
            item.setJobTitle(job.getTitle());
            item.setCriticality(job.getCriticality());
            item.setJobNotification(job.getNotification());

            item.setAgentId(eos.getAgentId());
            item.setAgentName(job.getAgentName());
            item.setAgentUri(eos.getAgentUri() == null ? ca.getUri() : eos.getAgentUri());
            item.setSubagentClusterId(job.getSubagentClusterId());

            item.setStartCause(OrderStepStartCause.order.name());// TODO
            item.setStartTime(agentStartTime);
            item.setStartEventId(eos.getEventId());

            Variables arguments = HistoryUtil.toVariables(eos.getArguments());
            item.setStartVariables(HistoryUtil.toJsonString(arguments));

            item.setEndTime(null);
            item.setEndEventId(null);

            item.setReturnCode(null);
            item.setSeverity(OrderStateText.RUNNING);

            item.setError(false);
            item.setErrorCode(null);
            item.setErrorText(null);

            item.setLogId(Long.valueOf(0));

            constraintHash = hashOrderStepConstraint(eos.getEventId(), item.getOrderId(), item.getWorkflowPosition());
            item.setConstraintHash(constraintHash);
            item.setCreated(new Date());
            item.setModified(item.getCreated());

            dbLayer.getSession().save(item);

            co.setCurrentHistoryOrderStepId(item.getId());
            dbLayer.updateOrderOnOrderStep(co.getId(), co.getCurrentHistoryOrderStepId());

            CachedOrderStep cos = new CachedOrderStep(item, ca.getTimezone());
            LogEntry le = new LogEntry(OrderLogEntryLogLevel.MAIN, EventType.OrderProcessingStarted, JocClusterUtil.getEventIdAsDate(eos
                    .getEventId()), agentStartTime);
            le.onOrderStep(cos, ca.getTimezone());
            le.setArguments(arguments);
            storeLog2File(le);
            cacheHandler.addOrderStep(item.getOrderId(), cos);

            tryStoreCurrentState(dbLayer, eos.getEventId());

            return new HistoryOrderStepBean(EventType.OrderProcessingStarted, eos.getEventId(), item, job.getWarnIfLonger(), job.getWarnIfShorter(),
                    job.getWarnReturnCodes(), item.getJobNotification());
        } catch (SOSHibernateObjectOperationException e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                throw e;
            }
            LOGGER.info(String.format("[%s][ConstraintViolation][%s][eventId=%s][%s][%s]%s", identifier, eos.getType(), eos.getEventId(), eos
                    .getOrderId(), eos.getPosition(), e.toString()));

            StringBuilder sb = new StringBuilder(controllerConfiguration.getCurrent().getId());
            sb.append("-").append(eos.getEventId());
            sb.append("-").append(eos.getOrderId());
            sb.append("-").append(eos.getPosition());

            if (co != null) {
                cacheHandler.addOrder(co.getOrderId(), co);
            }
            cacheHandler.getOrderStepByConstraint(dbLayer, ca, controllerTimezone, constraintHash, eos.getOrderId(), eos.getEventId(), sb.toString());
            return null;
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

    private HistoryOrderStepBean orderStepProcessed(DBLayerHistory dbLayer, FatEventOrderStepProcessed eos,
            Map<String, CachedOrderStep> endedOrderSteps) throws Exception {
        checkControllerTimezone(dbLayer);

        CachedOrder co = cacheHandler.getOrderByCurrentOrderId(dbLayer, eos.getOrderId(), eos.getEventId());
        CachedOrderStep cos = cacheHandler.getOrderStepByOrder(dbLayer, co, controllerTimezone, eos.getPosition());
        HistoryOrderStepBean hosb = null;
        if (cos.getEndTime() == null) {
            cos.setEndTime(eos.getEventDatetime());

            LogEntry le = new LogEntry(OrderLogEntryLogLevel.MAIN, EventType.OrderProcessed, JocClusterUtil.getEventIdAsDate(eos.getEventId()), cos
                    .getEndTime());
            if (eos.getOutcome() != null) {
                cos.setReturnCode(eos.getOutcome().getReturnCode());
                le.setReturnCode(cos.getReturnCode());
                if (eos.getOutcome().isFailed()) {
                    le.setError(OrderStateText.FAILED.value(), eos.getOutcome());
                }
            }

            OrderStepProcessedResult r = new OrderStepProcessedResult(eos, controllerConfiguration.getCurrent().getId(), co, cos);
            if (r.getYadeTransferResult() != null) {
                if (le.isError()) {
                    le.setErrorText(r.getYadeTransferResult().getErrorMessage());
                }
                yadeHandler.process(r.getYadeTransferResult(), co.getWorkflowPath(), co.getOrderId(), cos.getId(), cos.getJobName(), cos
                        .getWorkflowPosition());
            }

            if (le.isError() && SOSString.isEmpty(le.getErrorText())) {
                le.setErrorText(cos.getFirstChunkStdError());
            }
            co.setLastStepError(le, cos);
            cos.setSeverity(HistorySeverity.map2DbSeverity(le.isError() ? OrderStateText.FAILED : OrderStateText.FINISHED));

            Variables outcome = HistoryUtil.toVariables(r.getReturnValues());
            String endVariables = HistoryUtil.toJsonString(outcome);
            dbLayer.setOrderStepEnd(cos.getId(), cos.getEndTime(), eos.getEventId(), endVariables, le.getReturnCode(), cos.getSeverity(), le
                    .isError(), le.getErrorState(), le.getErrorReason(), le.getErrorCode(), le.getErrorText(), new Date());
            le.onOrderStep(cos);
            le.setArguments(outcome);

            hosb = onOrderStepProcessed(dbLayer, eos.getEventId(), co, cos, le, endVariables);

            if (isStartTimeAfterEndTime(hosb.getStartTime(), hosb.getEndTime())) {
                LOGGER.warn(String.format("[%s][%s][%s][startTime=%s > endTime=%s]%s", identifier, EventType.OrderProcessed, co.getOrderId(), SOSDate
                        .getDateTimeAsString(hosb.getStartTime()), SOSDate.getDateTimeAsString(hosb.getEndTime()), SOSString.toString(hosb)));
            }

            Path log = storeLog2File(le);
            storeLogFile2Db(dbLayer, log, null, hosb);
            endedOrderSteps.put(eos.getOrderId(), cos);

            tryStoreCurrentState(dbLayer, eos.getEventId());
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][skip][%s]order step is already ended[%s]", identifier, eos.getType(), eos.getOrderId(), SOSString
                        .toString(cos)));
            }
            hosb = null;
        }
        return hosb;
    }

    private void handleOrderLog(Path file, HistoryOrderBean hob) {
        if (hob.getParentId().equals(0L)) {
            boolean oh = logExtHandler.isLogExt(historyConfiguration.getLogExtOrderHistory(), hob.getError());
            boolean o = logExtHandler.isLogExt(historyConfiguration.getLogExtOrder(), hob.getError());
            if (oh || o) {
                com.sos.joc.history.helper.LogExt.Type t = com.sos.joc.history.helper.LogExt.Type.order_history;
                if (oh && o) {
                    t = com.sos.joc.history.helper.LogExt.Type.order_all;
                } else if (o) {
                    t = com.sos.joc.history.helper.LogExt.Type.order;
                }
                logExtHandler.add(t, file, hob.getLogId(), hob.getWorkflowName(), hob.getOrderId(), hob.getHistoryId(), null, null);
            } else {
                deleteLogOrderDirectory(file, hob.getOrderId());
            }
        } else {
            deleteLogFile(file);
        }
    }

    private void handleTaskLog(Path file, HistoryOrderStepBean hosb) {
        if (logExtHandler.isLogExt(historyConfiguration.getLogExtTask(), hosb.getError())) {
            logExtHandler.add(com.sos.joc.history.helper.LogExt.Type.task, file, hosb.getLogId(), JocClusterUtil.getBasenameFromPath(hosb
                    .getWorkflowPath()), HistoryUtil.getMainOrderId(hosb.getOrderId()), hosb.getHistoryOrderMainParentId(), hosb.getHistoryId(), hosb
                            .getJobLabel());
        } else {
            deleteLogFile(file);
        }
    }

    private void deleteLogFile(Path file) {
        if (CLEANUP_LOG_FILES) {
            try {
                Files.delete(file);
            } catch (IOException e) {
                LOGGER.warn(String.format("[%s][error on delete log file][%s]%s", identifier, file, e.toString()), e);
            }
        }
    }

    private void deleteLogOrderDirectory(Path file, String orderId) {
        try {
            SOSPath.deleteIfExists(file.getParent());
        } catch (Throwable e) {
            LOGGER.warn(String.format("[%s][%s][error on delete order directory][%s]%s", identifier, orderId, file.getParent(), e.toString()), e);
        }
    }

    private boolean isStartTimeAfterEndTime(Date startTime, Date endTime) {
        if (startTime == null || endTime == null) {
            return false;
        }
        return startTime.getTime() > endTime.getTime();
    }

    private HistoryOrderStepBean onOrderStepProcessed(DBLayerHistory dbLayer, Long eventId, CachedOrder co, CachedOrderStep cos, LogEntry le,
            String endVariables) throws Exception {
        String workflowName = JocClusterUtil.getBasenameFromPath(co.getWorkflowPath());
        CachedWorkflow cw = cacheHandler.getWorkflow(dbLayer, workflowName, co.getWorkflowVersionId());
        CachedWorkflowJob job = cw.getJob(cos.getJobName());

        HistoryOrderStepBean hosb = cos.convert(EventType.OrderProcessed, eventId, controllerConfiguration.getCurrent().getId(), co
                .getWorkflowPath());
        hosb.setEndVariables(endVariables);
        hosb.setError(le.isError());
        hosb.setErrorCode(le.getErrorCode());
        hosb.setErrorReason(le.getErrorReason());
        hosb.setErrorState(le.getErrorState());
        hosb.setErrorText(le.getErrorText());
        if (job != null) {
            hosb.setCriticality(getJobCriticality(job));
            hosb.setWarnIfLonger(job.getWarnIfLonger());
            hosb.setWarnIfShorter(job.getWarnIfShorter());
            hosb.setWarnReturnCodes(job.getWarnReturnCodes());
        }
        return hosb;
    }

    private int getJobCriticality(CachedWorkflowJob job) {
        return job.getCriticality() == null ? JobCriticality.NORMAL.intValue() : job.getCriticality().intValue();
    }

    private void orderStepStd(DBLayerHistory dbLayer, FatEventOrderStepStdWritten eos, EventType eventType) throws Exception {
        CachedOrder co = cacheHandler.getOrderByCurrentOrderId(dbLayer, eos.getOrderId(), eos.getEventId());
        CachedOrderStep cos = cacheHandler.getOrderStepByOrder(dbLayer, co, controllerTimezone, null);

        if (EventType.OrderStderrWritten.equals(eventType)) {
            if (cos.getFirstChunkStdError() == null) {
                cos.setFirstChunkStdError(eos.getChunck());
            }
            warnOnOrderStepStderr(dbLayer, eos.getEventId(), co, cos);
        }
        if (cos.getEndTime() == null) {
            LogEntry le = new LogEntry(OrderLogEntryLogLevel.INFO, eventType, JocClusterUtil.getEventIdAsDate(eos.getEventId()), eos
                    .getEventDatetime());

            le.onOrderStep(cos);
            storeLog2File(le, cos, eos.getChunck());

            tryStoreCurrentState(dbLayer, eos.getEventId());
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][skip][%s]order step is already ended. log already written...[%s]", identifier, eos.getType(), eos
                        .getOrderId(), SOSString.toString(cos)));
            }
        }
    }

    private void warnOnOrderStepStderr(DBLayerHistory dbLayer, Long eventId, CachedOrder co, CachedOrderStep cos) {
        if (cos.getWarnOnStderr() == null) {
            try {
                if (cos.getFirstChunkStdError() != null) {
                    String workflowName = JocClusterUtil.getBasenameFromPath(co.getWorkflowPath());
                    CachedWorkflow cw = cacheHandler.getWorkflow(dbLayer, workflowName, co.getWorkflowVersionId());
                    CachedWorkflowJob job = cw.getJob(cos.getJobName());
                    if (job != null && job.getWarnOnErrorWritten() != null && job.getWarnOnErrorWritten()) {
                        postEventTaskLogFirstStderr(eventId, co, cos, job);
                    }
                }
            } catch (Throwable e) {
                LOGGER.info(String.format("[%s][%s][warnOnOrderStepStderr][workflow=%s][orderId=%s][job=%s %s]%s", identifier, controllerConfiguration
                        .getCurrent().getId(), co.getWorkflowPath(), co.getOrderId(), cos.getJobName(), SOSString.toString(cos), e.toString()));
            } finally {
                cos.setWarnOnStderr(false);
            }
        }
    }

    private DBItemHistoryLog storeLogFile2Db(DBLayerHistory dbLayer, Path file, HistoryOrderBean hob, HistoryOrderStepBean hosb) throws Exception {

        String method = "storeLogFile2Db";
        DBItemHistoryLog item = null;
        try {
            if (Files.exists(file)) {
                Long historyOrderMainParentId = 0L;
                Long historyOrderId = 0L;
                Long historyOrderStepId = 0L;
                boolean isTask = false;
                if (hosb == null) {
                    historyOrderMainParentId = hob.getMainParentId();
                    historyOrderId = hob.getHistoryId();
                } else {
                    historyOrderMainParentId = hosb.getHistoryOrderMainParentId();
                    historyOrderId = hosb.getHistoryOrderId();
                    historyOrderStepId = hosb.getHistoryId();
                    isTask = true;
                }

                item = new DBItemHistoryLog();
                item.setControllerId(controllerConfiguration.getCurrent().getId());

                item.setHistoryOrderMainParentId(historyOrderMainParentId);
                item.setHistoryOrderId(historyOrderId);
                item.setHistoryOrderStepId(historyOrderStepId);
                item.setCompressed(isTask);

                item.setFileBasename(SOSPath.getFileNameWithoutExtension(file.getFileName()));
                item.setFileSizeUncomressed(Files.size(file));
                item.setFileLinesUncomressed(SOSPath.getLineCount(file));

                if (isTask) {
                    boolean truncate = false;
                    boolean truncateIsMaximum = false;
                    int truncateExeededMBSize = 0;
                    if (item.getFileSizeUncomressed() > historyConfiguration.getLogMaximumByteSize()) {
                        truncate = true;
                        truncateIsMaximum = true;
                        truncateExeededMBSize = historyConfiguration.getLogMaximumMBSize();
                    } else if (item.getFileSizeUncomressed() > historyConfiguration.getLogApplicableByteSize()) {
                        truncate = true;
                        truncateExeededMBSize = historyConfiguration.getLogApplicableMBSize();
                    }
                    if (truncate) {
                        Path f = JocClusterUtil.truncateHistoryOriginalLogFile(identifier + "][" + method, file, item.getFileSizeUncomressed(),
                                truncateExeededMBSize, truncateIsMaximum);
                        if (f == null) {
                            item = null;
                        } else {
                            item.setFileSizeUncomressed(Files.size(f));
                            item.setFileLinesUncomressed(SOSPath.getLineCount(f));
                            item.setFileContent(SOSGzip.compress(f, false).getCompressed());
                        }
                    } else {
                        try {
                            item.setFileContent(SOSGzip.compress(file, false).getCompressed());
                        } catch (Throwable e) {
                            Path f = moveOriginalLogFile(file, item.getFileSizeUncomressed(), e);
                            if (f == null) {
                                item = null;
                            } else {
                                item.setFileSizeUncomressed(Files.size(f));
                                item.setFileLinesUncomressed(SOSPath.getLineCount(f));
                                item.setFileContent(SOSGzip.compress(f, false).getCompressed());
                            }
                        }
                    }
                } else {// order
                    item.setFileContent(SOSPath.readFile(file, Collectors.joining(",", "[", "]")).getBytes(StandardCharsets.UTF_8));
                }
                if (item != null) {
                    item.setCreated(new Date());
                    dbLayer.getSession().save(item);

                    if (isTask) {
                        hosb.setLogId(item.getId());
                        dbLayer.setOrderStepLogId(hosb.getHistoryId(), hosb.getLogId());

                        handleTaskLog(file, hosb);
                    } else {
                        hob.setLogId(item.getId());
                        dbLayer.setOrderLogId(hob.getHistoryId(), hob.getLogId());

                        handleOrderLog(file, hob);
                    }
                }
            } else {
                LOGGER.error(String.format("[%s][%s][%s]file not found", identifier, method, file.toString()));
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][%s][%s]%s", identifier, method, file.toString(), e.toString()), e);
            item = null;
        }
        return item;
    }

    private Path moveOriginalLogFile(Path file, Long fileSizeUncomressed, Throwable t) {
        StringBuilder prefix = new StringBuilder();
        prefix.append("[JOC][History][").append(file).append("]");

        StringBuilder result = new StringBuilder();
        result.append(prefix).append("Log file ");
        result.append("(uncompressed size=").append(SOSShell.byteCountToDisplaySize(fileSizeUncomressed)).append(") ");
        result.append("will be moved because exception:").append(JocClusterUtil.HISTORY_LOG_NEW_LINE);
        result.append(SOSClassUtil.getStackTrace(t));
        result.append(JocClusterUtil.HISTORY_LOG_NEW_LINE);

        Path historyLogParentDir = historyConfiguration.getLogDir().getParent();
        if (historyLogParentDir == null) {
            result.append(prefix).append("Log file cannot be moved because a history parent directory not exists.");
        } else {
            try {
                Path target = historyLogParentDir.resolve("history_" + file.getFileName());
                SOSPath.renameTo(file, target);
                result.append(prefix).append("Log file moved to ").append(target).append(".");
            } catch (Throwable ex) {
                try {
                    result.append(prefix).append("Log file cannot be moved to ").append(historyLogParentDir).append(":").append(
                            JocClusterUtil.HISTORY_LOG_NEW_LINE);
                    result.append(SOSClassUtil.getStackTrace(ex)).append(JocClusterUtil.HISTORY_LOG_NEW_LINE);
                    result.append(prefix).append("Log file will be deleted.");
                    SOSPath.deleteIfExists(file);
                } catch (Throwable e) {
                    result.append(JocClusterUtil.HISTORY_LOG_NEW_LINE);
                    result.append(prefix).append("Log file cannot be deleted:").append(JocClusterUtil.HISTORY_LOG_NEW_LINE);
                    result.append(SOSClassUtil.getStackTrace(e));
                }
            }
        }

        try {
            file = Files.write(file, result.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Throwable e) {
            LOGGER.info(String.format("[moveOriginalLogFile][%s][truncate existing]%s", file, e.toString()));
            return null;
        }
        return file;
    }

    private Path getOrderLogDirectory(LogEntry entry) {
        return HistoryUtil.getOrderLogDirectory(historyConfiguration.getLogDir(), entry.getHistoryOrderMainParentId());
    }

    // see HistoryMapper.toString
    private OrderLogEntry createOrderLogEntry(LogEntry le) {
        OrderLogEntry ole = new OrderLogEntry();
        ole.setOrderId(le.getOrderId());
        ole.setLogLevel(le.getLogLevel());
        ole.setLogEvent(le.getEventType());
        ole.setPosition(SOSString.isEmpty(le.getPosition()) ? null : le.getPosition());
        ole.setReturnCode(le.getReturnCode() == null ? null : le.getReturnCode().longValue());// TODO change to Integer
        ole.setReturnMessage(le.getReturnMessage());
        ole.setLocks(null);
        if (le.isError()) {
            OrderLogEntryError error = new OrderLogEntryError();
            error.setErrorState(le.getErrorState());
            error.setErrorReason(le.getErrorReason());
            if (error.getErrorState() != null && error.getErrorReason() != null) {
                if (error.getErrorState().equals(error.getErrorReason())) {
                    error.setErrorReason(null);
                }
            }
            error.setErrorCode(le.getErrorCode());
            error.setErrorText(le.getErrorText());
            ole.setError(error);
        }
        if (le.getOrderLocks() != null) {
            ole.setMsg(String.format(messages.get(le.getEventType()), le.getOrderLocks().stream().map(OrderLock::getLockId).collect(Collectors
                    .joining(", "))));
            ole.setLocks(le.getOrderLocks().stream().map(ol -> {
                Lock lock = new Lock();
                lock.setLockName(ol.getLockId());
                lock.setLimit(ol.getLimit());
                lock.setCount(ol.getCount());
                if (ol.getState() != null) {
                    LockState lockState = new LockState();

                    List<String> l = ol.getState().getOrderIds();
                    lockState.setOrderIds(l == null || l.size() == 0 ? null : String.join(",", l));

                    l = ol.getState().getQueuedOrderIds();
                    lockState.setQueuedOrderIds(l == null || l.size() == 0 ? null : String.join(",", l));
                    lock.setLockState(lockState);
                }
                return lock;
            }).collect(Collectors.toList()));
        } else if (le.getOrderNotice() != null) {
            ExpectNotices en;
            List<FatExpectNotice> fenl;
            ConsumeNotices cn;
            switch (le.getEventType()) {
            case OrderNoticesConsumed:
                cn = new ConsumeNotices();
                cn.setConsuming(null);
                cn.setConsumed(!((FatEventOrderNoticesConsumed) le.getOrderNotice()).isFailed());
                ole.setConsumeNotices(cn);
                break;
            case OrderNoticesConsumptionStarted:
                cn = new ConsumeNotices();
                fenl = ((FatEventOrderNoticesConsumptionStarted) le.getOrderNotice()).getNotices();
                cn.setConsumed(null);
                cn.setConsuming(fenl.stream().map(e -> {
                    BaseNotice nen = new BaseNotice();
                    nen.setBoardName(e.getBoardPath());
                    nen.setId(e.getNoticeId());
                    return nen;
                }).collect(Collectors.toList()));
                ole.setConsumeNotices(cn);
                break;
            case OrderNoticesRead:
                en = new ExpectNotices();
                FatExpectNotices fen = ((FatEventOrderNoticesRead) le.getOrderNotice()).getNotices();
                en.setConsumed(fen == null ? "" : fen.getBoardPaths());
                en.setWaitingFor(null);
                ole.setExpectNotices(en);
                break;
            case OrderNoticesExpected:
                en = new ExpectNotices();
                fenl = ((FatEventOrderNoticesExpected) le.getOrderNotice()).getNotices();
                en.setConsumed(null);
                en.setWaitingFor(fenl.stream().map(e -> {
                    BaseNotice nen = new BaseNotice();
                    nen.setBoardName(e.getBoardPath());
                    nen.setId(e.getNoticeId());
                    return nen;
                }).collect(Collectors.toList()));
                ole.setExpectNotices(en);
                break;
            case OrderNoticePosted:
                PostNotice pn = new PostNotice();
                FatPostNotice fpn = ((FatEventOrderNoticePosted) le.getOrderNotice()).getNotice();
                if (fpn != null) {
                    pn.setBoardName(fpn.getBoardPath());
                    pn.setId(fpn.getNoticeId());
                    try {
                        pn.setEndOfLife(getDateAsString(fpn.getEndOfLife(), controllerTimezone));
                    } catch (Throwable e) {
                        LOGGER.info(String.format("[createOrderLogEntry][OrderNoticePosted][boardName=%s][%s]%s", pn.getBoardName(), fpn
                                .getEndOfLife(), e.toString()));
                    }
                }
                ole.setPostNotice(pn);
                break;
            default:
                break;
            }
        } else if (le.getDelayedUntil() != null) {
            Retrying r = new Retrying();
            try {
                r.setDelayedUntil(getDateAsString(le.getDelayedUntil(), controllerTimezone));
                ole.setRetrying(r);
            } catch (Throwable e) {
                LOGGER.info(String.format("[createOrderLogEntry][OrderRetrying][delayedUntil=%s]%s", le.getDelayedUntil(), e.toString()));
            }
        } else if (le.getCaught() != null) {
            ole.setCaught(le.getCaught());
        } else if (le.getOrderMoved() != null) {
            Moved m = new Moved();

            MovedTo mt = new MovedTo();
            mt.setPosition(le.getOrderMoved().getTo());
            m.setTo(mt);

            if (le.getOrderMoved().getReason() != null) {
                MovedSkipped ms = new MovedSkipped();
                ms.setInstruction(toOrderLogEntryInstruction(le.getOrderMoved().getInstruction()));
                try {
                    ms.setReason(MovedSkippedReason.valueOf(le.getOrderMoved().getReason()));
                } catch (Throwable e) {
                    ms.setReason(MovedSkippedReason.Unknown);
                }
                m.setSkipped(ms);
            }

            if (le.getOrderMoved().getWaitingForAdmission() != null && le.getOrderMoved().getWaitingForAdmission().size() > 0) {
                m.setWaitingForAdmission(getWaitingForAdmission(le.getOrderMoved().getWaitingForAdmission()));
            }

            ole.setMoved(m);
        } else if (le.getOrderAttached() != null) {
            if (le.getOrderAttached().getWaitingForAdmission() != null && le.getOrderAttached().getWaitingForAdmission().size() > 0) {
                Attached a = new Attached();
                a.setWaitingForAdmission(getWaitingForAdmission(le.getOrderAttached().getWaitingForAdmission()));

                ole.setAttached(a);
            }
        } else if (le.getOrderCyclingPrepared() != null) {
            try {
                Cycle c = new Cycle();
                CyclePrepared cp = new CyclePrepared();
                cp.setNext(getDateAsString(le.getOrderCyclingPrepared().getNext(), controllerTimezone));
                cp.setEnd(getDateAsString(le.getOrderCyclingPrepared().getEnd(), controllerTimezone));
                c.setPrepared(cp);
                ole.setCycle(c);
            } catch (Throwable t) {
                LOGGER.info(String.format("[createOrderLogEntry][OrderCyclingPrepared][next=%s,end=%s]%s", le.getOrderCyclingPrepared().getNext(), le
                        .getOrderCyclingPrepared().getEnd(), t.toString()));
            }
        } else if (le.getInstruction() != null) {
            switch (le.getEventType()) {
            case OrderSuspended:
                ole.setStopped(toOrderLogEntryInstruction(le.getInstruction()));
                break;
            case OrderResumed:
                ole.setResumed(toOrderLogEntryInstruction(le.getInstruction()));
                break;
            default:
                break;
            }
        } else if (le.getQuestion() != null) {
            ole.setQuestion(le.getQuestion());
        }
        return ole;
    }

    private WaitingForAdmission getWaitingForAdmission(List<Date> l) {
        WaitingForAdmission w = new WaitingForAdmission();
        w.setEntries(l.stream().map(e -> {
            try {
                return getDateAsString(e, controllerTimezone);
            } catch (Throwable t) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList()));
        return w;
    }

    private OrderLogEntryInstruction toOrderLogEntryInstruction(FatInstruction in) {
        if (in == null) {
            return null;
        }
        OrderLogEntryInstruction oin = new OrderLogEntryInstruction();
        if (SOSString.isEmpty(in.getJobName())) {
            oin.setInstruction(in.getInstructionName());
        } else {
            oin.setJob(in.getJobName());
        }
        return oin;
    }

    private String getDateAsString(Date date, String timeZone) throws Exception {
        TimeZone tz = null;
        try {
            tz = TimeZone.getTimeZone(timeZone);
        } catch (Throwable e) {
            tz = TimeZone.getTimeZone(HistoryUtil.getTimeZone("getDateAsString", timeZone));
        }
        return SOSDate.format(date, "yyyy-MM-dd HH:mm:ss.SSSZZZZ", tz);
    }

    public static String getDateAsString(Date date) throws Exception {
        return SOSDate.format(date, "yyyy-MM-dd HH:mm:ss.SSSZZZZ");
    }

    private Path storeLog2File(LogEntry entry) throws Exception {
        return storeLog2File(entry, null, null);
    }

    private Path storeLog2File(LogEntry le, CachedOrderStep cos, String stdout) throws Exception {

        OrderLogEntry ole = null;
        StringBuilder content = new StringBuilder();
        String contentAsString = null;
        StringBuilder orderEntryContent = null;
        Path dir = getOrderLogDirectory(le);
        Path file = null;
        boolean newLine;
        boolean append;

        boolean log2file = true;
        boolean postEvent = true;

        switch (le.getEventType()) {
        case OrderProcessingStarted:
            // order log
            newLine = true;
            ole = createOrderLogEntry(le);
            ole.setArguments(le.getArguments());
            ole.setControllerDatetime(getDateAsString(le.getControllerDatetime(), controllerTimezone));
            ole.setAgentDatetime(getDateAsString(le.getAgentDatetime(), le.getAgentTimezone()));
            ole.setAgentId(le.getAgentId());
            ole.setAgentName(le.getAgentName());
            ole.setAgentUrl(le.getAgentUri());
            ole.setSubagentClusterId(le.getSubagentClusterId());
            ole.setJob(le.getJobName());
            ole.setLabel(le.getLabel());
            ole.setTaskId(le.getHistoryOrderStepId());
            orderEntryContent = new StringBuilder(HistoryUtil.toJsonString(ole));
            postEventOrderLog(le, ole);
            log2file(HistoryUtil.getOrderLog(dir, le.getHistoryOrderId()), orderEntryContent.toString(), newLine, le.getEventType());

            // task log
            file = HistoryUtil.getOrderStepLog(dir, le);
            content.append(getDateAsString(le.getAgentDatetime(), le.getAgentTimezone())).append(" ");
            content.append("[").append(le.getLogLevel().name()).append("]    ");
            content.append(le.getInfo());

            contentAsString = content.toString();
            postEventTaskLog(le, contentAsString, newLine);
            break;
        case OrderProcessed:
            // order log
            newLine = true;
            ole = createOrderLogEntry(le);
            ole.setReturnValues(le.getArguments());
            ole.setLogLevel(ole.getError() == null ? OrderLogEntryLogLevel.SUCCESS : OrderLogEntryLogLevel.ERROR);
            ole.setControllerDatetime(getDateAsString(le.getControllerDatetime(), controllerTimezone));
            ole.setAgentDatetime(getDateAsString(le.getAgentDatetime(), le.getAgentTimezone()));
            // orderEntry.setAgentPath(entry.getAgentPath());
            // orderEntry.setAgentUrl(entry.getAgentUri());
            ole.setJob(le.getJobName());
            ole.setLabel(le.getLabel());
            ole.setTaskId(le.getHistoryOrderStepId());
            orderEntryContent = new StringBuilder(HistoryUtil.toJsonString(ole));
            postEventOrderLog(le, ole);
            log2file(HistoryUtil.getOrderLog(dir, le.getHistoryOrderId()), orderEntryContent.toString(), newLine, le.getEventType());

            // task log
            file = HistoryUtil.getOrderStepLog(dir, le);
            content.append(getDateAsString(le.getAgentDatetime(), le.getAgentTimezone())).append(" ");
            content.append("[").append(le.getLogLevel().name()).append("]    ");
            content.append(le.getInfo());

            contentAsString = content.toString();
            postEventTaskLog(le, contentAsString, newLine);
            break;

        case OrderStdoutWritten:
        case OrderStderrWritten:
            newLine = false;
            append = false;
            file = HistoryUtil.getOrderStepLog(dir, le);

            if (cos.isLastStdEndsWithNewLine() == null) {
                try {
                    if (Files.exists(file)) {
                        if (SOSPath.endsWithNewLine(file)) {
                            append = true;
                        }
                        cos.setLogSize(Files.size(file));
                        if (cos.getLogSize() > historyConfiguration.getLogMaximumDisplayByteSize()) {
                            postEvent = false;
                        }
                        if (cos.getLogSize() > historyConfiguration.getLogMaximumByteSize()) {
                            log2file = false;
                        }
                    } else {
                        cos.addLogSize(stdout.getBytes().length);
                    }
                } catch (Throwable e) {
                    LOGGER.info(String.format("[%s][%s][%s]%s", identifier, le.getEventType(), file, e.toString()));
                }
            } else {
                if (cos.isLastStdEndsWithNewLine().booleanValue()) {
                    append = true;
                }
                if (cos.getLogSize() > historyConfiguration.getLogMaximumDisplayByteSize()) {
                    postEvent = false;
                }
                if (cos.getLogSize() > historyConfiguration.getLogMaximumByteSize()) {
                    log2file = false;
                } else {
                    cos.addLogSize(stdout.getBytes().length);
                }
            }

            if (log2file) {
                if (append) {
                    String outType = le.getEventType().equals(EventType.OrderStdoutWritten) ? "STDOUT" : "STDERR";
                    content.append(getDateAsString(le.getAgentDatetime(), le.getAgentTimezone())).append(" ");
                    content.append("[").append(outType).append("]  ");
                }
                cos.setLastStdEndsWithNewLine(SOSPath.endsWithNewLine(stdout));
                content.append(stdout);
                contentAsString = content.toString();
                if (postEvent) {
                    postEventTaskLog(le, contentAsString, newLine);
                }
            }
            break;
        case OrderStarted:
        case OrderAdded:
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        default:
            // order log
            newLine = true;

            if (le.isOrderStarted()) {
                file = HistoryUtil.getOrderLog(dir, le.getHistoryOrderId());
            } else {
                if (!Files.exists(dir)) {
                    Files.createDirectories(dir);
                }
                file = HistoryUtil.getOrderLog(dir, le.getOrderId());
            }

            ole = createOrderLogEntry(le);
            ole.setArguments(le.getArguments());
            ole.setControllerDatetime(getDateAsString(le.getControllerDatetime(), controllerTimezone));
            if (le.getAgentDatetime() != null && le.getAgentTimezone() != null) {
                ole.setAgentDatetime(getDateAsString(le.getAgentDatetime(), le.getAgentTimezone()));
            }
            content.append(HistoryUtil.toJsonString(ole));
            contentAsString = content.toString();
            if (le.isOrderStarted()) {
                postEventOrderLog(le, ole);
            }
        }
        content = null;

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s][%s][%s]log2file=%s", identifier, le.getEventType().value(), le.getOrderId(), file, log2file));
        }
        if (log2file && file != null) {
            log2file(file, contentAsString, newLine, le.getEventType());

            if (ole != null && !le.getHistoryOrderId().equals(le.getHistoryOrderMainParentId())) {
                write2MainOrderLog(le, dir, (orderEntryContent == null ? contentAsString : orderEntryContent.toString()), newLine, le.getEventType());
            }
        }
        return file;
    }

    private void write2MainOrderLog(LogEntry le, Path dir, String content, boolean newLine, EventType eventType) throws Exception {
        Path file = HistoryUtil.getOrderLog(dir, le.getHistoryOrderMainParentId());
        log2file(file, content, newLine, eventType);
    }

    private void log2file(Path file, String content, boolean newLine, EventType eventType) {
        try {
            write2file(file, content, newLine);
        } catch (NoSuchFileException e) {// e.g. folders deleted
            LOGGER.info(String.format("[%s][NoSuchFileException][%s][%s]create the parent directories if not exists and try again ...", identifier,
                    eventType, file));
            try {
                Path parent = file.getParent();
                if (!Files.exists(parent)) {
                    Files.createDirectories(parent);
                }
                write2file(file, content, newLine);
            } catch (Throwable ee) {
                LOGGER.error(String.format("[%s][%s]%s", identifier, file, ee.toString()), ee);
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][%s]%s", identifier, file, e.toString()), e);
        }
    }

    private void write2file(Path file, String content, boolean newLine) throws Exception {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(content);
            writer.flush();
            if (newLine) {
                writer.write(JocClusterUtil.HISTORY_LOG_NEW_LINE);
            }
        }
    }

    private String hashOrderConstraint(Long eventId, String orderId) {
        return SOSString.hash256(new StringBuilder(controllerConfiguration.getCurrent().getId()).append(eventId).append(orderId).toString());

    }

    private String hashOrderStepConstraint(Long eventId, String orderId, String workflowPosition) {
        return SOSString.hash256(new StringBuilder(controllerConfiguration.getCurrent().getId()).append(eventId).append(orderId).append(
                workflowPosition).toString());
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
        if (cacheHandler != null) {
            cacheHandler.setIdentifier(val);
        }
    }

    public JocHistoryConfiguration getHistoryConfiguration() {
        return historyConfiguration;
    }

    public HistoryCacheHandler getCacheHandler() {
        return cacheHandler;
    }

    public LogExtAsyncHandler getLogExtHandler() {
        return logExtHandler;
    }
}
