package com.sos.joc.classes.event;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.joc.classes.agent.AgentClusterWatch;
import com.sos.joc.classes.event.EventServiceFactory.EventCondition;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.ClusterWatch;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.proxy.ProxyUser;
import com.sos.joc.classes.workflow.WorkflowRefs;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.JOCEvent;
import com.sos.joc.event.bean.agent.AgentClusterNodeLossEvent;
import com.sos.joc.event.bean.agent.AgentInventoryEvent;
import com.sos.joc.event.bean.auditlog.AuditlogChangedEvent;
import com.sos.joc.event.bean.auditlog.AuditlogWorkflowEvent;
import com.sos.joc.event.bean.cluster.ActiveClusterChangedEvent;
import com.sos.joc.event.bean.dailyplan.DailyPlanEvent;
import com.sos.joc.event.bean.dailyplan.DailyPlanProjectionEvent;
import com.sos.joc.event.bean.documentation.DocumentationEvent;
import com.sos.joc.event.bean.history.HistoryOrderEvent;
import com.sos.joc.event.bean.history.HistoryTaskEvent;
import com.sos.joc.event.bean.inventory.InventoryEvent;
import com.sos.joc.event.bean.inventory.InventoryGroupsEvent;
import com.sos.joc.event.bean.inventory.InventoryJobTagsEvent;
import com.sos.joc.event.bean.inventory.InventoryObjectEvent;
import com.sos.joc.event.bean.inventory.InventoryTagEvent;
import com.sos.joc.event.bean.inventory.InventoryTagsEvent;
import com.sos.joc.event.bean.inventory.InventoryTrashEvent;
import com.sos.joc.event.bean.monitoring.MonitoringGuiEvent;
import com.sos.joc.event.bean.monitoring.NotificationCreated;
import com.sos.joc.event.bean.problem.ProblemEvent;
import com.sos.joc.event.bean.proxy.ClusterNodeLossEvent;
import com.sos.joc.event.bean.proxy.ProxyClosed;
import com.sos.joc.event.bean.proxy.ProxyCoupled;
import com.sos.joc.event.bean.proxy.ProxyEvent;
import com.sos.joc.event.bean.proxy.ProxyRemoved;
import com.sos.joc.event.bean.proxy.ProxyRestarted;
import com.sos.joc.event.bean.reporting.ReportingEvent;
import com.sos.joc.event.bean.yade.YadeEvent;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.common.IEventObject;
import com.sos.joc.model.event.EventMonitoring;
import com.sos.joc.model.event.EventOrderMonitoring;
import com.sos.joc.model.event.EventSnapshot;
import com.sos.joc.model.event.EventType;
import com.sos.monitoring.notification.NotificationType;

import js7.data.agent.AgentPath;
import js7.data.agent.AgentRefStateEvent;
import js7.data.board.BoardPath;
import js7.data.board.NoticeEvent;
import js7.data.cluster.ClusterEvent;
import js7.data.cluster.ClusterWatchProblems;
import js7.data.controller.ControllerEvent;
import js7.data.event.Event;
import js7.data.event.KeyedEvent;
import js7.data.event.Stamped;
import js7.data.item.BasicItemEvent.ItemAttached;
import js7.data.item.BasicItemEvent.ItemDeleted;
import js7.data.item.InventoryItemKey;
import js7.data.item.UnsignedItemEvent;
import js7.data.item.UnsignedSimpleItemEvent;
import js7.data.item.UnsignedSimpleItemPath;
import js7.data.item.UnsignedVersionedItemId;
import js7.data.item.VersionedControlPath;
import js7.data.item.VersionedEvent.VersionedItemAddedOrChanged;
import js7.data.item.VersionedItemId;
import js7.data.item.VersionedItemPath;
import js7.data.lock.LockPath;
import js7.data.order.OrderEvent;
import js7.data.order.OrderEvent.OrderAddedX;
import js7.data.order.OrderEvent.OrderBroken;
import js7.data.order.OrderEvent.OrderCancellationMarked;
import js7.data.order.OrderEvent.OrderDeleted$;
import js7.data.order.OrderEvent.OrderFailed;
import js7.data.order.OrderEvent.OrderFailedInFork;
import js7.data.order.OrderEvent.OrderLockEvent;
import js7.data.order.OrderEvent.OrderLocksAcquired;
import js7.data.order.OrderEvent.OrderLocksQueued;
import js7.data.order.OrderEvent.OrderLocksReleased;
import js7.data.order.OrderEvent.OrderNoticeEvent;
import js7.data.order.OrderEvent.OrderOrderAdded;
import js7.data.order.OrderEvent.OrderProcessed;
import js7.data.order.OrderEvent.OrderProcessingKilled$;
import js7.data.order.OrderEvent.OrderProcessingStarted;
import js7.data.order.OrderEvent.OrderPromptAnswered;
import js7.data.order.OrderEvent.OrderPrompted;
import js7.data.order.OrderEvent.OrderResumed;
import js7.data.order.OrderEvent.OrderResumptionMarked;
import js7.data.order.OrderEvent.OrderRetrying;
import js7.data.order.OrderEvent.OrderSleeping;
import js7.data.order.OrderEvent.OrderStarted$;
import js7.data.order.OrderEvent.OrderStopped$;
import js7.data.order.OrderEvent.OrderSuspended$;
import js7.data.order.OrderEvent.OrderSuspensionMarked;
import js7.data.order.OrderEvent.OrderTerminated;
import js7.data.order.OrderEvent.OrderTransferred;
import js7.data.order.OrderId;
import js7.data.plan.PlanEvent;
import js7.data.plan.PlanId;
import js7.data.subagent.SubagentBundleId;
import js7.data.subagent.SubagentId;
import js7.data.subagent.SubagentItemStateEvent;
import js7.data.workflow.WorkflowPath;
import js7.data.workflow.WorkflowPathControlPath;
import js7.data.workflow.instructions.NoticeInstruction;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.workflow.JWorkflowId;
import js7.proxy.javaapi.eventbus.JControllerEventBus;
import scala.collection.JavaConverters;

public class EventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventService.class);
    // private static boolean isTraceEnabled = LOGGER.isTraceEnabled();
    // OrderAdded, OrderProcessed, OrderProcessingStarted$ extends OrderCoreEvent
    // OrderStarted, OrderProcessingKilled$, OrderFailed, OrderFailedInFork, OrderRetrying, OrderBroken, OrderSuspended$
    // OrderResumed, OrderResumptionMarked, OrderCancellationMarked extends OrderActorEvent
    // OrderFinished, OrderCancelled, OrderDeleted$ extends OrderTerminated
    private static List<Class<? extends Event>> eventsOfController = Arrays.asList(ControllerEvent.class, ClusterEvent.class,
            AgentRefStateEvent.class, OrderStarted$.class, OrderProcessingKilled$.class, OrderFailed.class, OrderFailedInFork.class,
            OrderRetrying.class, OrderBroken.class, OrderTerminated.class, OrderAddedX.class, OrderProcessed.class, OrderSuspended$.class,
            OrderSuspensionMarked.class, OrderResumed.class, OrderResumptionMarked.class, OrderCancellationMarked.class, OrderPrompted.class,
            OrderPromptAnswered.class, OrderProcessingStarted.class, OrderDeleted$.class, OrderStopped$.class, OrderSleeping.class, OrderOrderAdded.class,  
            VersionedItemAddedOrChanged.class, UnsignedSimpleItemEvent.class, UnsignedItemEvent.class, ItemDeleted.class, ItemAttached.class, 
            NoticeEvent.class, OrderLocksAcquired.class, OrderLocksQueued.class, OrderLocksReleased.class, OrderNoticeEvent.class, 
            OrderTransferred.class, SubagentItemStateEvent.class, PlanEvent.class);
    private String controllerId;
    private volatile CopyOnWriteArraySet<IEventObject> events = new CopyOnWriteArraySet<>();
    private AtomicBoolean isCurrentController = new AtomicBoolean(false);
    private JControllerEventBus evtBus = null;
    private volatile CopyOnWriteArraySet<EventCondition> conditions = new CopyOnWriteArraySet<>();
    private volatile ConcurrentMap<String, WorkflowId> orders = new ConcurrentHashMap<>();
    private volatile CopyOnWriteArraySet<String> uncoupledAgents = new CopyOnWriteArraySet<>();
    private volatile CopyOnWriteArraySet<String> uncoupledSubagents = new CopyOnWriteArraySet<>();
    private AtomicBoolean burstFilter = new AtomicBoolean(true);
    private static final EnumSet<NotificationType> notificationFailureTypes = EnumSet.of(NotificationType.ERROR, NotificationType.WARNING);

    public EventService(String controllerId) {
        this.controllerId = controllerId;
        EventBus.getInstance().register(this);
        startEventService();
        ClusterWatch.getInstance().getAndCleanLastMessage(controllerId).ifPresent(m -> addEvent(createNodeLossProblem(Instant.now().toEpochMilli()
                / 1000, controllerId, m)));
        
        //TODO init lostNodeIds
        AgentClusterWatch.init(controllerId);
    }

    protected void close() {
        if (evtBus != null) {
            evtBus.close();
        }
        signalAll();
    }

    public void startEventService() {
        try {
            if (evtBus == null) {
                evtBus = Proxy.of(controllerId).controllerEventBus();
                if (evtBus != null) {
                    LOGGER.info("Start EventBus " + controllerId);
                    evtBus.subscribe(eventsOfController, callbackOfController);
                    burstFilter.set(true);
                    // setOrders();
                }
            }
        } catch (Exception e) {
            if (burstFilter.getAndSet(false)) {
                LOGGER.warn(e.toString());
            }
        }
    }

    public void addCondition(EventCondition cond) {
        conditions.add(cond);
    }

    public void removeCondition(EventCondition cond) {
        conditions.remove(cond);
    }

    private boolean atLeastOneConditionIsHold() {
        return conditions.stream().parallel().anyMatch(EventCondition::isHold);
    }

    public CopyOnWriteArraySet<IEventObject> getEvents() {
        return events;
    }

    public void setIsCurrentController(boolean val) {
        isCurrentController.set(val);
    }

    @Subscribe({ ProxyRestarted.class, ProxyClosed.class, ProxyRemoved.class })
    public void processProxyEvent(ProxyEvent evt) throws ControllerConnectionResetException, ControllerConnectionRefusedException,
            DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            ExecutionException {
        if (evt.getControllerId().equals(controllerId) && ProxyUser.JOC.name().equals(evt.getKey())) {
            if (evtBus != null) {
                LOGGER.info("try to close EventBus " + controllerId);
                evtBus.close();
                evtBus = null;
            }
            if (evtBus == null && (evt instanceof ProxyRestarted || evt instanceof ProxyClosed)) {
                LOGGER.debug("try to restart EventBus " + controllerId);
                startEventService();
            }
        }
    }

    @Subscribe({ ProblemEvent.class })
    public void createEvent(ProblemEvent evt) {
        if (evt.getControllerId() == null || evt.getControllerId().isEmpty() || evt.getControllerId().equals(controllerId)) {
            EventSnapshot eventSnapshot = new EventSnapshot();
            eventSnapshot.setEventId(evt.getEventId() / 1000);
            if (evt.isOnlyHint()) {
                eventSnapshot.setEventType("ProblemAsHintEvent");
            } else {
                eventSnapshot.setEventType("ProblemEvent");
            }
            eventSnapshot.setObjectType(EventType.PROBLEM);
            eventSnapshot.setAccessToken(evt.getKey());
            eventSnapshot.setMessage(evt.getMessage());
            addEvent(eventSnapshot);
        }
    }

    @Subscribe({ HistoryOrderEvent.class })
    public void createHistoryOrderEvent(HistoryOrderEvent evt) {
        if (controllerId.equals(evt.getControllerId())) {
            EventSnapshot eventSnapshot = new EventSnapshot();
            eventSnapshot.setEventId(evt.getEventId() / 1000);
            String orderId = evt.getOrderId();
            if (orderId.contains("|")) {
                // HistoryChildOrderStarted, HistoryChildOrderTerminated, HistoryChildOrderUpdated
                eventSnapshot.setEventType(evt.getKey().replaceFirst("Order", "ChildOrder"));
            } else {
                // HistoryOrderStarted, HistoryOrderTerminated, HistoryOrderUpdated
                eventSnapshot.setEventType(evt.getKey());
            }
            eventSnapshot.setWorkflow(new WorkflowId(evt.getWorkflowName(), evt.getWorkflowVersionId()));
            eventSnapshot.setObjectType(EventType.ORDERHISTORY);
            addEvent(eventSnapshot);
        }
    }

    @Subscribe({ HistoryTaskEvent.class })
    public void createHistoryTaskEvent(HistoryTaskEvent evt) {
        if (controllerId.equals(evt.getControllerId())) {
            EventSnapshot eventSnapshot = new EventSnapshot();
            eventSnapshot.setEventId(evt.getEventId() / 1000);
            // HistoryTaskStarted, HistoryTaskTerminated
            eventSnapshot.setEventType(evt.getKey().replaceFirst("Order", ""));
            eventSnapshot.setObjectType(EventType.TASKHISTORY);
            eventSnapshot.setWorkflow(new WorkflowId(evt.getWorkflowName(), evt.getWorkflowVersionId()));
            addEvent(eventSnapshot);
        }
    }

    @Subscribe({ YadeEvent.class })
    public void createHistoryTaskEvent(YadeEvent evt) {
        if (controllerId.equals(evt.getControllerId())) {
            EventSnapshot eventSnapshot = new EventSnapshot();
            eventSnapshot.setEventId(evt.getEventId() / 1000);
            eventSnapshot.setEventType(evt.getKey());
            eventSnapshot.setObjectType(EventType.FILETRANSFER);
            // eventSnapshot.setPath(evt.getTransferId().toString());
            addEvent(eventSnapshot);
        }
    }

    @Subscribe({ InventoryEvent.class, InventoryTrashEvent.class })
    public void createInventoryEvent(InventoryEvent evt) {
        EventSnapshot eventSnapshot = new EventSnapshot();
        eventSnapshot.setEventId(evt.getEventId() / 1000);
        eventSnapshot.setEventType(evt.getKey()); // InventoryUpdated, InventoryTrashUpdated
        eventSnapshot.setObjectType(EventType.FOLDER);
        eventSnapshot.setPath(evt.getFolder());
        addEvent(eventSnapshot);
    } //InventoryObjectEvent
    
    @Subscribe({ InventoryObjectEvent.class })
    public void createInventoryEvent(InventoryObjectEvent evt) {
        try {
            EventSnapshot eventSnapshot = new EventSnapshot();
            eventSnapshot.setEventId(evt.getEventId() / 1000);
            eventSnapshot.setEventType(evt.getKey()); // InventoryObjectUpdated
            eventSnapshot.setObjectType(EventType.fromValue(evt.getObjectType()));
            eventSnapshot.setPath(evt.getPath());
            addEvent(eventSnapshot);
        } catch (Exception e) {
            //
        }
    }
    
    @Subscribe({ InventoryTagEvent.class })
    public void createInventoryTagEvent(InventoryTagEvent evt) {
        EventSnapshot eventSnapshot = new EventSnapshot();
        eventSnapshot.setEventId(evt.getEventId() / 1000);
        eventSnapshot.setEventType(evt.getKey()); // Inventory(Job)TaggingUpdated, Inventory(Job)TagAdded, Inventory(Job)TagDeleted
        eventSnapshot.setObjectType(EventType.TAG);
        eventSnapshot.setPath(evt.getTag());
        addEvent(eventSnapshot);
    }
    
    @Subscribe({ InventoryTagsEvent.class, InventoryJobTagsEvent.class, InventoryGroupsEvent.class })
    public void createInventoryTagEvent(JOCEvent evt) {
        EventSnapshot eventSnapshot = new EventSnapshot();
        eventSnapshot.setEventId(evt.getEventId() / 1000);
        eventSnapshot.setEventType(evt.getKey()); // InventoryTagsUpdated, InventoryJobTagsUpdated, InventoryGroupsUpdated
        eventSnapshot.setObjectType(EventType.TAG);
        addEvent(eventSnapshot);
    }
    
    @Subscribe({ ReportingEvent.class })
    public void createReportingEvent(ReportingEvent evt) {
        EventSnapshot eventSnapshot = new EventSnapshot();
        eventSnapshot.setEventId(evt.getEventId() / 1000);
        eventSnapshot.setEventType(evt.getKey()); // ReportsUpdated, ReportRunsUpdated
        eventSnapshot.setObjectType(EventType.REPORT);
        addEvent(eventSnapshot);
    }

    @Subscribe({ DocumentationEvent.class })
    public void createDocumentationEvent(DocumentationEvent evt) {
        EventSnapshot eventSnapshot = new EventSnapshot();
        eventSnapshot.setEventId(evt.getEventId() / 1000);
        eventSnapshot.setEventType(evt.getKey()); // DocumentationUpdated
        eventSnapshot.setObjectType(EventType.FOLDER);
        eventSnapshot.setPath(evt.getFolder());
        addEvent(eventSnapshot);
    }

    @Subscribe({ ActiveClusterChangedEvent.class })
    public void createEvent(ActiveClusterChangedEvent evt) {
        EventSnapshot eventSnapshot = new EventSnapshot();
        eventSnapshot.setEventId(evt.getEventId() / 1000);
        eventSnapshot.setEventType("JOCStateChanged");
        eventSnapshot.setObjectType(EventType.JOCCLUSTER);
        addEvent(eventSnapshot);
    }

    @Subscribe({ DailyPlanEvent.class })
    public void createEvent(DailyPlanEvent evt) {
        if (evt.getControllerId() == null || controllerId.equals(evt.getControllerId())) {
            EventSnapshot eventSnapshot = new EventSnapshot();
            eventSnapshot.setEventId(evt.getEventId() / 1000);
            eventSnapshot.setEventType(evt.getKey());
            eventSnapshot.setObjectType(EventType.DAILYPLAN);
            if (evt.getDailyPlanDate() != null && !evt.getDailyPlanDate().isEmpty()) {
                eventSnapshot.setMessage(evt.getDailyPlanDate());
            }
            addEvent(eventSnapshot);
        }
    }
    
    @Subscribe({ DailyPlanProjectionEvent.class })
    public void createEvent(DailyPlanProjectionEvent evt) {
        EventSnapshot eventSnapshot = new EventSnapshot();
        eventSnapshot.setEventId(evt.getEventId() / 1000);
        eventSnapshot.setEventType(evt.getKey());
        eventSnapshot.setObjectType(EventType.DAILYPLAN);
        addEvent(eventSnapshot);
    }

    @Subscribe({ NotificationCreated.class })
    public void createEvent(NotificationCreated evt) {
        // TODO if (evt.getControllerId() == null || controllerId.equals(evt.getControllerId())) ?
        EventSnapshot eventSnapshot = new EventSnapshot();
        eventSnapshot.setEventId(evt.getEventId() / 1000);
        eventSnapshot.setEventType(evt.getKey());
        eventSnapshot.setObjectType(EventType.MONITORINGNOTIFICATION);
        eventSnapshot.setMessage(evt.getNotificationId().toString());
        addEvent(eventSnapshot);
        
        NotificationType type = getNotificationType(evt.getLevel());
        if (notificationFailureTypes.contains(type)) {
            EventOrderMonitoring eventO = new EventOrderMonitoring();
            Long evtId = evt.getEventId() / 1000;
            eventO.setEventId(evtId);
            eventO.setLevel(type);
            eventO.setWorkflowName(evt.getWorkflowName());
            eventO.setOrderId(evt.getOrderId());
            eventO.setJobName(evt.getJobName());
            eventO.setTimestamp(evt.getDate());
            if (evt.getDate() == null) {
                eventO.setTimestamp(Date.from(Instant.ofEpochSecond(evtId)));
            }
            eventO.setMessage(evt.getMessage());
            addEventO(eventO);
        }
    }

    @Subscribe({ AuditlogChangedEvent.class })
    public void createEvent(AuditlogChangedEvent evt) {
        if (evt.getControllerId() == null || controllerId.equals(evt.getControllerId())) {
            EventSnapshot eventSnapshot = new EventSnapshot();
            eventSnapshot.setEventId(evt.getEventId() / 1000);
            eventSnapshot.setEventType(evt.getKey());
            eventSnapshot.setObjectType(EventType.AUDITLOG);
            addEvent(eventSnapshot);
        }
    }

    @Subscribe({ AuditlogWorkflowEvent.class })
    public void createEvent(AuditlogWorkflowEvent evt) {
        if (controllerId.equals(evt.getControllerId())) {
            EventSnapshot eventSnapshot = new EventSnapshot();
            eventSnapshot.setEventId(evt.getEventId() / 1000);
            eventSnapshot.setEventType(evt.getKey());
            eventSnapshot.setObjectType(EventType.WORKFLOW);
            eventSnapshot.setWorkflow(new WorkflowId(evt.getWorkflowPath(), evt.getVersionId()));
            addEvent(eventSnapshot);
        }
    }

    @Subscribe({ AgentInventoryEvent.class })
    public void createEvent(AgentInventoryEvent evt) {
        EventSnapshot eventSnapshot = new EventSnapshot();
        eventSnapshot.setEventId(evt.getEventId() / 1000);
        eventSnapshot.setEventType(evt.getKey());
        eventSnapshot.setObjectType(EventType.AGENT);
        eventSnapshot.setMessage(evt.getAgentId());
        addEvent(eventSnapshot);
    }
    
    @Subscribe({ MonitoringGuiEvent.class })
    public void createEvent(MonitoringGuiEvent evt) {
        try {
            String source = evt.getSource();
            EventMonitoring eventM = new EventMonitoring();
            Long evtId = evt.getEventId() / 1000;
            eventM.setEventId(evtId);
            eventM.setLevel(getNotificationType(evt.getLevel()));
            if ("LogNotification".equalsIgnoreCase(source)) {
                eventM.setSource(evt.getRequest());
            } else {
                eventM.setSource(evt.getSource());
                eventM.setRequest(evt.getRequest());
            }
            eventM.setCategory(evt.getCategory());
            eventM.setTimestamp(evt.getDate());
            if (evt.getDate() == null) {
                eventM.setTimestamp(Date.from(Instant.ofEpochSecond(evtId)));
            }
            eventM.setMessage(evt.getMessage());
            addEventM(eventM);
        } catch (Exception e) {
            //
        }
    }
    
    @Subscribe({ ClusterNodeLossEvent.class })
    public void createEvent(ClusterNodeLossEvent evt) {
        if (!evt.onlyProblem()) {
            addEvent(createControllerEvent(evt.getEventId() / 1000));
        }
        String message = evt.getMessage();
        if (evt.getNodeId() != null) {
            String msg = "Loss of '" + evt.getNodeId().toUpperCase() + "' instance in Controller Cluster '" + evt.getControllerId()
                    + "' requires confirmation";
            if (message == null) {
                message = msg;
            } else {
                message = msg + ": " + message;
            }
        }
        addEvent(createNodeLossProblem(evt.getEventId() / 1000, evt.getControllerId(), message));
    }
    
    @Subscribe({ AgentClusterNodeLossEvent.class })
    public void createEvent(AgentClusterNodeLossEvent evt) {
        if (controllerId.equals(evt.getControllerId())) {
            // if (!evt.onlyProblem()) {
            // addEvent(createControllerEvent(evt.getEventId() / 1000));
            // }
            addEvent(createNodeLossProblem(evt.getEventId() / 1000, evt.getControllerId(), evt.getMessage()));
        }
    }
    
    @Subscribe({ ProxyCoupled.class })
    public void createEvent(ProxyCoupled evt) {
        if (controllerId.equals(evt.getControllerId())) {
            // if (evt.isCoupled()) {
            // setOrders();
            // }
            if (evt.isCoupled() != null) {
                addEvent(createProxyEvent(evt.getEventId() / 1000, evt.isCoupled()));
            }
        } else {
            // to update Controller Status widget for other controllers
            addEvent(createControllerEvent(evt.getEventId() / 1000));
        }
    }

    BiConsumer<Stamped<KeyedEvent<Event>>, JControllerState> callbackOfController = (stampedEvt, currentState) -> {
        try {
            KeyedEvent<Event> event = stampedEvt.value();
            long eventId = stampedEvt.eventId() / 1000000; // eventId per second
            Object key = event.key();
            Event evt = event.event();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(evt.toString() + ", key=" + SOSString.toString(key));
            }
            
            if (evt instanceof OrderEvent) {
                final OrderId orderId = (evt instanceof OrderOrderAdded) ? ((OrderOrderAdded) evt).orderId() : (OrderId) key;
                String mainOrderId = orderId.string().substring(0, OrdersHelper.mainOrderIdLength);
                JOrder optOrder = currentState.idToOrder().get(orderId);
                if (optOrder != null) {
                    // LOGGER.info(opt.get().toString());
                    WorkflowId w = orders.get(mainOrderId);
                    if (w == null) {
                        w = mapWorkflowId(optOrder.workflowId());
                        orders.put(mainOrderId, w);
                    }
                    if (evt instanceof OrderTransferred) {
                        w = mapWorkflowId(((OrderTransferred) evt).workflowPosition().workflowId());
                        orders.put(mainOrderId, w);
                    }
                    addEvent(createWorkflowEventOfOrder(eventId, w));
                    addEvent(createWorkflowPlanEvent(eventId, w, optOrder));
                    if (evt instanceof OrderProcessingStarted || evt instanceof OrderProcessed || evt instanceof OrderProcessingKilled$) {
                        addEvent(createTaskEventOfOrder(eventId, w));
                    } else if (evt instanceof OrderLockEvent) {
                        OrderLockEvent lockEvt = (OrderLockEvent) evt;
                        JavaConverters.asJava(lockEvt.lockPaths()).forEach(lock -> addEvent(createLockEvent(eventId, lock.string())));
                    } else if (evt instanceof OrderNoticeEvent) {
                        orderPositionToBoardPaths(optOrder, currentState).ifPresent(boardPaths -> boardPaths.forEach(
                                boardPath -> createBoardEvent(eventId, boardPath.string())));
                    }
                } else {
                    // LOGGER.info("Order is not in current state");
                    if (evt instanceof OrderDeleted$) {
                        if (orders.containsKey(mainOrderId)) {
                            addEvent(createWorkflowEventOfOrder(eventId, orders.get(mainOrderId)));
                            orders.remove(mainOrderId);
                        } else {
                            // addEvent(createWorkflowEventOfDeletedOrder(eventId, orderId.string()));
                            // LOGGER.warn("OrderDeleted event without known orderId is received: " + event.toString());
                        }
                    } else {
                        LOGGER.warn("Order event without orderId is received: " + event.toString());
                    }
                }

            } else if (evt instanceof ControllerEvent || evt instanceof ClusterEvent) {
                addEvent(createControllerEvent(eventId));

            } else if (evt instanceof VersionedItemAddedOrChanged) {
                // VersionedItemAdded, VersionedItemChanged
                // VersionedItemRemoved -> see ItemDeleted
                String eventType = evt.getClass().getSimpleName().replaceFirst("Versioned", "");
                VersionedItemPath path = ((VersionedItemAddedOrChanged) evt).path();
                if (path instanceof WorkflowPath) {
                    addEvent(createWorkflowEvent(eventId, path.string(), eventType));
                } else {
                    // TODO other versioned objects
                }

            } else if (evt instanceof UnsignedSimpleItemEvent) {
                // UnsignedSimpleItemAdded SimpleItemAddedAndChanged and SimpleItemChanged etc.
                String eventType = evt.getClass().getSimpleName().replaceFirst(".*Simple", "");
                UnsignedSimpleItemPath itemId = ((UnsignedSimpleItemEvent) evt).key();
                if (itemId instanceof AgentPath || itemId instanceof SubagentId || itemId instanceof SubagentBundleId) {
                    // eventType = evt.getClass().getSimpleName().replaceFirst(".*SimpleItem", "Agent");
                    addEvent(createAgentEvent(eventId, itemId.string(), eventType));
                } else if (itemId instanceof LockPath) {
                    addEvent(createLockEvent(eventId, itemId.string(), eventType));
                    // } else if (itemId instanceof OrderWatchPath) {
                    // // We don't need an Item event for FileOrderSource
                    // addEvent(createFileOrderSourceEvent(eventId, itemId.string(), eventType));
                } else if (itemId instanceof BoardPath) {
                    addEvent(createBoardEvent(eventId, itemId.string(), eventType));
                } else if (itemId instanceof WorkflowPathControlPath) {
                    addEvent(createWorkflowUpdatedEvent(eventId, itemId.string()));
                }

                // } else if (evt instanceof SignedItemEvent) {
                // // We don't need an Item event for JobResource
                // String eventType = evt.getClass().getSimpleName().replaceFirst(".*Signed", "");
                // SignableItemKey itemId = ((SignedItemEvent) evt).key();
                // if (itemId instanceof JobResourcePath) {
                // addEvent(createJobResourceEvent(eventId, ((JobResourcePath) itemId).string(), eventType));
                // }

            } else if (evt instanceof UnsignedItemEvent) {
                UnsignedVersionedItemId<? extends VersionedControlPath> itemId = ((UnsignedItemEvent) evt).key();
                addEvent(createWorkflowUpdatedEvent(eventId, itemId.path().string()));

            } else if (evt instanceof ItemDeleted) {
                InventoryItemKey itemId = ((ItemDeleted) evt).key();
                String eventType = "ItemDeleted";
                if (itemId instanceof AgentPath || itemId instanceof SubagentId || itemId instanceof SubagentBundleId) {
                    addEvent(createAgentEvent(eventId, itemId.path().string(), eventType));
                } else if (itemId instanceof LockPath) {
                    addEvent(createLockEvent(eventId, itemId.path().string(), eventType));
                } else if (itemId instanceof BoardPath) {
                    addEvent(createBoardEvent(eventId, itemId.path().string(), eventType));
                } else if (itemId instanceof WorkflowPathControlPath) {
                    addEvent(createWorkflowUpdatedEvent(eventId, itemId.path().string()));
                } else if (itemId instanceof VersionedControlPath) {
                    addEvent(createWorkflowUpdatedEvent(eventId, itemId.path().string()));
                } else if (itemId instanceof VersionedItemId<?>) {
                    addEvent(createWorkflowEvent(eventId, mapWorkflowId((VersionedItemId<?>) itemId), eventType));
                } // JobResourcePath, OrderWatchPath

            } else if (evt instanceof ItemAttached) {
                InventoryItemKey itemId = ((ItemAttached) evt).key();
                if (itemId instanceof WorkflowPathControlPath) {
                    addEvent(createWorkflowUpdatedEvent(eventId, itemId.path().string()));
                }

            } else if (evt instanceof AgentRefStateEvent.AgentCouplingFailed) {
                String agentPath = ((AgentPath) key).string();
                if (!uncoupledAgents.contains(agentPath)) {
                    uncoupledAgents.add(agentPath);
                    addEvent(createAgentEvent(eventId, agentPath, "AgentCoupling"));
                }
            } else if (evt instanceof AgentRefStateEvent.AgentCoupled$) {
                String agentPath = ((AgentPath) key).string();
                addEvent(createAgentEvent(eventId, agentPath, "AgentCoupling"));
                uncoupledAgents.remove(agentPath);
            } else if (evt instanceof AgentRefStateEvent.AgentClusterWatchManuallyConfirmed$) {
                AgentPath agentPath = (AgentPath) key;
                //don't send event because ClusterWatch has still "old" cluster state: addEvent(createAgentEvent(eventId, agentPath));
                // TODO cleanup stored lostNostId for repeated events
                AgentClusterWatch.clean(controllerId, agentPath);
                uncoupledAgents.remove(agentPath.string());
            } else if (evt instanceof AgentRefStateEvent.AgentClusterWatchConfirmationRequired) {
                AgentPath agentPath = (AgentPath) key;
                AgentRefStateEvent.AgentClusterWatchConfirmationRequired clusterWatchEvt = (AgentRefStateEvent.AgentClusterWatchConfirmationRequired) evt;
                ClusterWatchProblems.ClusterNodeLossNotConfirmedProblem problem = clusterWatchEvt.problem();
                // TODO store lostNostId for confirm command and for repeated events
                addEvent(createNodeLossProblem(eventId, agentPath, problem));
                addEvent(createAgentEvent(eventId, agentPath.string()));
                uncoupledAgents.remove(agentPath.string());
            } else if (evt instanceof AgentRefStateEvent && !(evt instanceof AgentRefStateEvent.AgentEventsObserved)) {
                String agentPath = ((AgentPath) key).string();
                addEvent(createAgentEvent(eventId, agentPath));
                uncoupledAgents.remove(agentPath);
            } else if (evt instanceof SubagentItemStateEvent.SubagentCouplingFailed) {
                String subagentPath = ((SubagentId) key).string();
                if (!uncoupledSubagents.contains(subagentPath)) {
                    uncoupledSubagents.add(subagentPath);
                    addEvent(createAgentEvent(eventId, subagentPath));
                }
            } else if (evt instanceof SubagentItemStateEvent && !(evt instanceof SubagentItemStateEvent.SubagentEventsObserved)) {
                String subagentPath = ((SubagentId) key).string();
                addEvent(createAgentEvent(eventId, subagentPath));
                uncoupledSubagents.remove(subagentPath);
            } else if (evt instanceof NoticeEvent) {
                addEvent(createBoardEvent(eventId, ((BoardPath) key).string()));
            } else if (evt instanceof PlanEvent) {
                addEvent(createPlanEvent(eventId, (PlanId) key));
            }

        } catch (Exception e) {
            LOGGER.warn(e.toString());
        }
    };
    
    private NotificationType getNotificationType(Integer type) {
        try {
            return NotificationType.fromValue(type);
        } catch (Throwable e) {
            return NotificationType.ERROR;
        }
    }

    private static Optional<Set<BoardPath>> orderPositionToBoardPaths(JOrder order, JControllerState controllerState) {
        Optional<JOrder> orderOpt = order == null ? Optional.empty() : Optional.of(order);
        return orderOpt.map(o -> controllerState.asScala().instruction(o.asScala().workflowPosition())).flatMap(instruction -> tryCast(
                NoticeInstruction.class, instruction)).map(postNotice -> JavaConverters.asJava(postNotice.referencedBoardPaths()));
    }

    @SuppressWarnings("unchecked")
    private static <T> Optional<T> tryCast(Class<T> clazz, Object object) {
        return clazz.isAssignableFrom(object.getClass()) ? Optional.of((T) object) : Optional.empty();
    }

    private WorkflowId mapWorkflowId(JWorkflowId workflowId) {
        WorkflowId w = new WorkflowId();
        w.setPath(workflowId.path().string());
        w.setVersionId(workflowId.versionId().string());
        return w;
    }

    private WorkflowId mapWorkflowId(VersionedItemId<?> workflowId) {
        WorkflowId w = new WorkflowId();
        w.setPath(workflowId.path().string());
        w.setVersionId(workflowId.versionId().string());
        return w;
    }

    private EventSnapshot createWorkflowEventOfOrder(long eventId, WorkflowId workflowId) {
        return createWorkflowEvent(eventId, workflowId, "WorkflowStateChanged");
    }
    
    private EventSnapshot createWorkflowPlanEvent(long eventId, WorkflowId workflowId, JOrder jOrder) {
        if (WorkflowRefs.getWorkflowNamesWithBoards(controllerId, workflowId.getPath()) != null) {
            PlanId pId = jOrder.asScala().planId();
            EventSnapshot evt = new EventSnapshot();
            evt.setEventId(eventId);
            evt.setEventType("WorkflowPlanChanged");
            if (PlanId.Global.equals(pId)) {
                evt.setPath("Global");
            } else {
                evt.setPath(pId.planSchemaId().string() + "/" + pId.planKey().string());
            }
            evt.setObjectType(EventType.PLAN);
            return evt;
        }
        return null;
    }

    private EventSnapshot createWorkflowEvent(long eventId, WorkflowId workflowId, String eventType) {
        EventSnapshot evt = new EventSnapshot();
        evt.setEventId(eventId);
        evt.setEventType(eventType);
        evt.setObjectType(EventType.WORKFLOW);
        evt.setWorkflow(workflowId);
        return evt;
    }

    // private EventSnapshot createWorkflowEventOfDeletedOrder(long eventId, String orderId) {
    // EventSnapshot evt = new EventSnapshot();
    // evt.setEventId(eventId);
    // evt.setEventType("WorkflowStateChanged");
    // evt.setObjectType(EventType.WORKFLOW);
    // evt.setMessage(orderId);
    // return evt;
    // }

    private EventSnapshot createTaskEventOfOrder(long eventId, WorkflowId workflowId) {
        EventSnapshot evt = new EventSnapshot();
        evt.setEventId(eventId);
        evt.setEventType("JobStateChanged");
        evt.setObjectType(EventType.JOB);
        evt.setWorkflow(workflowId);
        return evt;
    }

    private EventSnapshot createProxyEvent(long eventId, Boolean isCoupled) {
        EventSnapshot evt = new EventSnapshot();
        evt.setEventId(eventId);
        if (isCoupled) {
            evt.setEventType("ProxyCoupled");
        } else {
            evt.setEventType("ProxyDecoupled");
        }
        evt.setObjectType(EventType.CONTROLLER);
        return evt;
    }

    private EventSnapshot createControllerEvent(long eventId) {
        EventSnapshot evt = new EventSnapshot();
        evt.setEventId(eventId);
        evt.setEventType("ControllerStateChanged");
        evt.setObjectType(EventType.CONTROLLER);
        return evt;
    }
    
    private EventSnapshot createNodeLossProblem(long eventId, String controllerId, String message) {
        EventSnapshot evt = new EventSnapshot();
        evt.setEventId(eventId);
        evt.setEventType("NodeLossProblemEvent");
        evt.setObjectType(EventType.PROBLEM);
        evt.setPath(controllerId);
        evt.setMessage(message);
        return evt;
    }
    
    private EventSnapshot createNodeLossProblem(long eventId, AgentPath agentPath, ClusterWatchProblems.ClusterNodeLossNotConfirmedProblem problem) {
        String msg = AgentClusterWatch.put(controllerId, agentPath, problem);
        EventSnapshot evt = new EventSnapshot();
        evt.setEventId(eventId);
        evt.setEventType("NodeLossProblemEvent");
        evt.setObjectType(EventType.PROBLEM);
        evt.setPath(controllerId);
        evt.setMessage(msg);
        return evt;
    }

    private EventSnapshot createAgentEvent(long eventId, String path) {
        return createAgentEvent(eventId, path, "AgentStateChanged");
    }

    private EventSnapshot createAgentEvent(long eventId, String path, String eventType) {
        EventSnapshot evt = new EventSnapshot();
        evt.setEventId(eventId);
        evt.setEventType(eventType);
        evt.setPath(path);
        evt.setObjectType(EventType.AGENT);
        return evt;
    }

    private EventSnapshot createLockEvent(long eventId, String path) {
        return createLockEvent(eventId, path, "LockStateChanged");
    }

    private EventSnapshot createLockEvent(long eventId, String path, String eventType) {
        EventSnapshot evt = new EventSnapshot();
        evt.setEventId(eventId);
        evt.setEventType(eventType);
        evt.setPath(path);
        evt.setObjectType(EventType.LOCK);
        return evt;
    }

    private EventSnapshot createBoardEvent(long eventId, String path) {
        return createBoardEvent(eventId, path, "NoticeBoardStateChanged");
    }

    private EventSnapshot createBoardEvent(long eventId, String path, String eventType) {
        EventSnapshot evt = new EventSnapshot();
        evt.setEventId(eventId);
        evt.setEventType(eventType);
        evt.setPath(path);
        evt.setObjectType(EventType.NOTICEBOARD);
        return evt;
    }

    // private EventSnapshot createFileOrderSourceEvent(long eventId, String path, String eventType) {
    // EventSnapshot evt = new EventSnapshot();
    // evt.setEventId(eventId);
    // evt.setEventType(eventType);
    // evt.setPath(path);
    // evt.setObjectType(EventType.FILEORDERSOURCE);
    // return evt;
    // }

    // private EventSnapshot createJobResourceEvent(long eventId, String path, String eventType) {
    // EventSnapshot evt = new EventSnapshot();
    // evt.setEventId(eventId);
    // evt.setEventType(eventType);
    // evt.setPath(path);
    // evt.setObjectType(EventType.JOBRESOURCE);
    // return evt;
    // }

    private EventSnapshot createWorkflowEvent(long eventId, String path, String eventType) {
        EventSnapshot evt = new EventSnapshot();
        evt.setEventId(eventId);
        evt.setEventType(eventType);
        evt.setPath(path);
        evt.setObjectType(EventType.WORKFLOW);
        return evt;
    }

    private EventSnapshot createWorkflowUpdatedEvent(long eventId, String path) {
        return createWorkflowEvent(eventId, path, "WorkflowUpdated");
    }
    
    private EventSnapshot createPlanEvent(long eventId, PlanId pId) {
        EventSnapshot evt = new EventSnapshot();
        evt.setEventId(eventId);
        evt.setEventType("PlanUpdated");
        if (PlanId.Global.equals(pId)) {
            evt.setPath("Global");
        } else {
            evt.setPath(pId.planSchemaId().string() + "/" + pId.planKey().string());
        }
        evt.setObjectType(EventType.PLAN);
        return evt;
    }

    private void addEvent(EventSnapshot eventSnapshot) {
        if (eventSnapshot != null && eventSnapshot.getEventId() != null && events.add(eventSnapshot)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("add event for " + controllerId + ": " + eventSnapshot.toString());
            }
            if (atLeastOneConditionIsHold()) {
                signalAll();
            }
        }
    }
    
    private void addEventM(EventMonitoring eventMonitoring) {
        if (eventMonitoring != null && eventMonitoring.getEventId() != null && events.add(eventMonitoring)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("add system monitoring event for " + controllerId + ": " + eventMonitoring.toString());
            }
            if (atLeastOneConditionIsHold()) {
                signalAll();
            }
        }
    }
    
    private void addEventO(EventOrderMonitoring eventOrderMonitoring) {
        if (eventOrderMonitoring != null && eventOrderMonitoring.getEventId() != null && events.add(eventOrderMonitoring)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("add order monitoring event for " + controllerId + ": " + eventOrderMonitoring.toString());
            }
            if (atLeastOneConditionIsHold()) {
                signalAll();
            }
        }
    }

    private synchronized void signalAll() {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Try signal all Events of '" + controllerId + "'");
            }
            if (atLeastOneConditionIsHold() && EventServiceFactory.lock.tryLock(200L, TimeUnit.MILLISECONDS)) {
                try {
                    conditions.stream().forEach(EventCondition::signalAll); // without .parallel()
                } catch (Exception e) {
                    LOGGER.warn(e.toString());
                } finally {
                    try {
                        EventServiceFactory.lock.unlock();
                    } catch (IllegalMonitorStateException e) {
                        LOGGER.warn("IllegalMonitorStateException at unlock lock after signalAll");
                    }
                }
            }
        } catch (InterruptedException e) {
        } catch (Exception e) {
            LOGGER.warn(e.toString());
        }
    }

    protected EventServiceFactory.Mode hasOldEvent(Long eventId, EventCondition eventArrived) {
        // LOGGER.info(events.toString());
        if (events.stream().parallel().anyMatch(e -> eventId < e.getEventId())) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("has old Event for " + controllerId + ": true");
            }
            // if (isCurrentController.get() && events.stream().parallel().anyMatch(e -> EventType.PROBLEM.equals(e.getObjectType()))) {
            // LOGGER.info("hasProblemEvent for " + controllerId + ": true");
            // EventServiceFactory.signalEvent(eventArrived);
            // return EventServiceFactory.Mode.IMMEDIATLY;
            // }
            EventServiceFactory.signalEvent(eventArrived);
            return EventServiceFactory.Mode.TRUE;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("has old Event for " + controllerId + ": false");
        }
        return EventServiceFactory.Mode.FALSE;
    }

    // private void setOrders() {
    // try {
    // // possibly IllegalStateException
    // // orders = Proxy.of(controllerId).currentState().ordersBy(JOrderPredicates.any())
    // // .collect(Collectors.toMap(o -> o.id().string().substring(0,OrdersHelper.mainOrderIdLength), o -> mapWorkflowId(o.workflowId())));
    // Proxy.of(controllerId).currentState().ordersBy(JOrderPredicates.any()).forEach(o -> orders.put(o.id().string().substring(0,
    // OrdersHelper.mainOrderIdLength),
    // mapWorkflowId(o.workflowId())));
    // } catch (Exception e) {
    // //
    // }
    // }

}
