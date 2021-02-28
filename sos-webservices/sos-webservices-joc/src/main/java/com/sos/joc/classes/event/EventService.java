package com.sos.joc.classes.event;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.controller.model.workflow.WorkflowId;
import com.sos.joc.classes.event.EventServiceFactory.EventCondition;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.proxy.ProxyUser;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.cluster.ActiveClusterChangedEvent;
import com.sos.joc.event.bean.history.HistoryOrderEvent;
import com.sos.joc.event.bean.history.HistoryTaskEvent;
import com.sos.joc.event.bean.inventory.InventoryEvent;
import com.sos.joc.event.bean.inventory.InventoryTrashEvent;
import com.sos.joc.event.bean.problem.ProblemEvent;
import com.sos.joc.event.bean.proxy.ProxyClosed;
import com.sos.joc.event.bean.proxy.ProxyCoupled;
import com.sos.joc.event.bean.proxy.ProxyEvent;
import com.sos.joc.event.bean.proxy.ProxyRemoved;
import com.sos.joc.event.bean.proxy.ProxyRestarted;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.event.EventSnapshot;
import com.sos.joc.model.event.EventType;

import js7.data.agent.AgentId;
import js7.data.agent.AgentRefStateEvent;
import js7.data.cluster.ClusterEvent;
import js7.data.controller.ControllerEvent;
import js7.data.event.Event;
import js7.data.event.KeyedEvent;
import js7.data.event.Stamped;
import js7.data.item.ItemPath;
import js7.data.item.SimpleItemEvent;
import js7.data.item.SimpleItemId;
import js7.data.item.VersionedEvent.VersionedItemEvent;
import js7.data.lock.LockId;
import js7.data.order.OrderEvent;
import js7.data.order.OrderEvent.OrderAdded;
import js7.data.order.OrderEvent.OrderBroken;
import js7.data.order.OrderEvent.OrderFailed;
import js7.data.order.OrderEvent.OrderFailedInFork;
import js7.data.order.OrderEvent.OrderLockAcquired;
import js7.data.order.OrderEvent.OrderLockEvent;
import js7.data.order.OrderEvent.OrderLockQueued;
import js7.data.order.OrderEvent.OrderLockReleased;
import js7.data.order.OrderEvent.OrderProcessed;
import js7.data.order.OrderEvent.OrderProcessingKilled$;
import js7.data.order.OrderEvent.OrderProcessingStarted$;
import js7.data.order.OrderEvent.OrderRemoved$;
import js7.data.order.OrderEvent.OrderRetrying;
import js7.data.order.OrderEvent.OrderStarted$;
import js7.data.order.OrderEvent.OrderTerminated;
import js7.data.order.OrderId;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
import js7.data_for_java.workflow.JWorkflowId;
import js7.proxy.javaapi.eventbus.JControllerEventBus;

public class EventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventService.class);
    private static boolean isDebugEnabled = LOGGER.isDebugEnabled();
    // OrderAdded, OrderProcessed, OrderProcessingStarted$ extends OrderCoreEvent
    // OrderStarted, OrderProcessingKilled$, OrderFailed, OrderFailedInFork, OrderRetrying, OrderBroken extends OrderActorEvent
    // OrderFinished, OrderCancelled, OrderRemoved$ extends OrderTerminated
    private static List<Class<? extends Event>> eventsOfController = Arrays.asList(ControllerEvent.class, ClusterEvent.class,
            AgentRefStateEvent.class, OrderStarted$.class, OrderProcessingKilled$.class, OrderFailed.class, OrderFailedInFork.class,
            OrderRetrying.class, OrderBroken.class, OrderTerminated.class, OrderAdded.class, OrderProcessed.class,
            OrderProcessingStarted$.class, OrderRemoved$.class, VersionedItemEvent.class, SimpleItemEvent.class, 
            OrderLockAcquired.class, OrderLockQueued.class, OrderLockReleased.class);
    private String controllerId;
    private volatile CopyOnWriteArraySet<EventSnapshot> events = new CopyOnWriteArraySet<>();
    private AtomicBoolean isCurrentController = new AtomicBoolean(false);
    private JControllerEventBus evtBus = null;
    private volatile CopyOnWriteArraySet<EventCondition> conditions = new CopyOnWriteArraySet<>();
    private volatile Map<String, WorkflowId> orders = new ConcurrentHashMap<>();

    public EventService(String controllerId) {
        this.controllerId = controllerId;
        EventBus.getInstance().register(this);
        startEventService();
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
                    evtBus.subscribe(eventsOfController, callbackOfController);
                    setOrders();
                }
            }
        } catch (Exception e) {
            LOGGER.warn(e.toString());
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
    
    public CopyOnWriteArraySet<EventSnapshot> getEvents() {
        return events;
    }

    public void setIsCurrentController(boolean val) {
        isCurrentController.set(val);
    }
    
    @Subscribe({ ProxyRestarted.class, ProxyClosed.class, ProxyRemoved.class })
    public void processProxyEvent(ProxyEvent evt) throws JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException,
            DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            ExecutionException {
        if (evt.getControllerId().equals(controllerId) && ProxyUser.JOC.name().equals(evt.getKey())) {
            if (evtBus != null) {
                LOGGER.info("try to close EventBus");
                evtBus.close();
                evtBus = null;
            }
            if (evtBus == null && (evt instanceof ProxyRestarted || evt instanceof ProxyClosed)) {
                LOGGER.info("try to restart EventBus");
                startEventService();
            }
        }
    }

    @Subscribe({ ProblemEvent.class })
    public void createEvent(ProblemEvent evt) {
        if (evt.getControllerId() == null || evt.getControllerId().isEmpty() || evt.getControllerId().equals(controllerId)) {
            EventSnapshot eventSnapshot = new EventSnapshot();
            eventSnapshot.setEventId(evt.getEventId());
            eventSnapshot.setEventType("ProblemEvent");
            eventSnapshot.setObjectType(EventType.PROBLEM);
            eventSnapshot.setAccessToken(evt.getKey());
            eventSnapshot.setMessage(evt.getVariables().get("message"));
            addEvent(eventSnapshot);
        }
    }
    
    @Subscribe({ HistoryOrderEvent.class })
    public void createHistoryOrderEvent(HistoryOrderEvent evt) {
        if (controllerId.equals(evt.getControllerId())) {
            EventSnapshot eventSnapshot = new EventSnapshot();
            eventSnapshot.setEventId(evt.getEventId());
            String orderId = evt.getVariables().get("orderId");
            if (orderId.contains("|")) {
                // HistoryChildOrderStarted, HistoryChildOrderTerminated, HistoryChildOrderUpdated
                eventSnapshot.setEventType(evt.getKey().replaceFirst("Order", "ChildOrder"));
            } else {
                // HistoryOrderStarted, HistoryOrderTerminated, HistoryOrderUpdated
                eventSnapshot.setEventType(evt.getKey());
            }
            eventSnapshot.setWorkflow(orders.get(orderId.substring(0, 24)));
            eventSnapshot.setObjectType(EventType.ORDERHISTORY);
            //eventSnapshot.setPath(orderId);
            addEvent(eventSnapshot);
        }
    }
    
    @Subscribe({ HistoryTaskEvent.class })
    public void createHistoryTaskEvent(HistoryTaskEvent evt) {
        if (controllerId.equals(evt.getControllerId())) {
            EventSnapshot eventSnapshot = new EventSnapshot();
            eventSnapshot.setEventId(evt.getEventId());
            // HistoryTaskStarted, HistoryTaskTerminated
            eventSnapshot.setEventType(evt.getKey().replaceFirst("Order", ""));
            eventSnapshot.setObjectType(EventType.TASKHISTORY);
            eventSnapshot.setWorkflow(orders.get(evt.getOrderId().substring(0, 24)));
            //eventSnapshot.setPath(evt.getJobName());
            addEvent(eventSnapshot);
        }
    }
    
    @Subscribe({ InventoryEvent.class, InventoryTrashEvent.class })
    public void createInventoryEvent(InventoryEvent evt) {
        EventSnapshot eventSnapshot = new EventSnapshot();
        eventSnapshot.setEventId(evt.getEventId());
        eventSnapshot.setEventType(evt.getKey()); // InventoryUpdated, InventoryTrashUpdated
        eventSnapshot.setObjectType(EventType.FOLDER);
        eventSnapshot.setPath(evt.getFolder());
        addEvent(eventSnapshot);
    }
    
    @Subscribe({ ActiveClusterChangedEvent.class })
    public void createEvent(ActiveClusterChangedEvent evt) {
        EventSnapshot eventSnapshot = new EventSnapshot();
        eventSnapshot.setEventId(evt.getEventId());
        eventSnapshot.setEventType("JOCStateChanged");
        eventSnapshot.setObjectType(EventType.JOCCLUSTER);
        addEvent(eventSnapshot);
    }
    
    @Subscribe({ ProxyCoupled.class })
    public void createEvent(ProxyCoupled evt) {
        if (controllerId.equals(evt.getControllerId())) {
            if (evt.isCoupled()) {
                setOrders();
            }
            addEvent(createProxyEvent(evt.getEventId(), evt.isCoupled()));
        } else {
            // to update Controller Status widget for other controllers
            addEvent(createControllerEvent(evt.getEventId()));
        }
    }

    BiConsumer<Stamped<KeyedEvent<Event>>, JControllerState> callbackOfController = (stampedEvt, currentState) -> {
        try {
            KeyedEvent<Event> event = stampedEvt.value();
            long eventId = stampedEvt.eventId() / 1000000; //eventId per second
            Object key = event.key();
            Event evt = event.event();

            if (evt instanceof OrderEvent) {
                final OrderId orderId = (OrderId) key;
                Optional<JOrder> opt = currentState.idToOrder(orderId);
                if (opt.isPresent()) {
                    String mainOrderId = orderId.string().substring(0, 24);
                    WorkflowId w = orders.get(mainOrderId);
                    if (w == null) {
                        w = mapWorkflowId(opt.get().workflowId());
                        orders.put(mainOrderId, w);
                    }
                    addEvent(createWorkflowEventOfOrder(eventId, w));
                    if (evt instanceof OrderProcessingStarted$ || evt instanceof OrderProcessed || evt instanceof OrderProcessingKilled$) {
                        addEvent(createTaskEventOfOrder(eventId, w));
                    }
                } else {
                    if (evt instanceof OrderRemoved$) {
                        String mainOrderId = orderId.string().substring(0, 24);
                        if (orders.containsKey(mainOrderId)) {
                            addEvent(createWorkflowEventOfOrder(eventId, orders.get(mainOrderId)));
                            orders.remove(mainOrderId);
                        }
                    }
                }
                
            } else if (evt instanceof ControllerEvent || evt instanceof ClusterEvent) {
                addEvent(createControllerEvent(eventId));
                
            } else if (evt instanceof VersionedItemEvent) {
                // VersionedItemAdded and VersionedItemChanged etc.
                String eventType = evt.getClass().getSimpleName().replaceFirst("Versioned", "");
                ItemPath path = ((VersionedItemEvent) evt).path();
                if (path instanceof WorkflowPath) {
                    addEvent(createWorkflowEvent(eventId, path.string(), eventType));
                } else {
                    // TODO other versioned objects
                }
                
            }  else if (evt instanceof SimpleItemEvent) {
                // SimpleItemAdded SimpleItemAddedAndChanged SimpleItemDeleted and SimpleItemChanged etc.
                String eventType = evt.getClass().getSimpleName().replaceFirst("Simple", "");
                SimpleItemId itemId = ((SimpleItemEvent) evt).id();
                if (itemId instanceof AgentId) {
                    eventType = evt.getClass().getSimpleName().replaceFirst("SimpleItem", "Agent");
                    addEvent(createAgentEvent(eventId, itemId.string(), eventType));
                } else if (itemId instanceof LockId) {
                    addEvent(createLockEvent(eventId, itemId.string(), eventType));
                } else {
                    // TODO other simple objects
                }
                
            } else if (evt instanceof AgentRefStateEvent && !(evt instanceof AgentRefStateEvent.AgentEventsObserved)) {
                addEvent(createAgentEvent(eventId, ((AgentId) key).string()));
                
            } else if (evt instanceof OrderLockEvent) {
                addEvent(createLockEvent(eventId, ((LockId) key).string()));
            }
            
        } catch (Exception e) {
            LOGGER.warn(e.toString());
        }
    };
    
    private WorkflowId mapWorkflowId(JWorkflowId workflowId) {
        WorkflowId w = new WorkflowId();
        w.setPath(workflowId.path().string());
        w.setVersionId(workflowId.versionId().string());
        return w;
    }

    private EventSnapshot createWorkflowEventOfOrder(long eventId, WorkflowId workflowId) {
        EventSnapshot evt = new EventSnapshot();
        evt.setEventId(eventId);
        evt.setEventType("WorkflowStateChanged");
        evt.setObjectType(EventType.WORKFLOW);
        evt.setWorkflow(workflowId);
        return evt;
    }

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
    
    private EventSnapshot createAgentEvent(long eventId, String path) {
        return createAgentEvent(eventId, path, "AgentStateChanged");
    }
    
    private EventSnapshot createAgentEvent(long eventId, String path, String eventType) {
        EventSnapshot evt = new EventSnapshot();
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
        evt.setEventType(eventType);
        evt.setPath(path);
        evt.setObjectType(EventType.LOCK);
        return evt;
    }
    
    private EventSnapshot createWorkflowEvent(long eventId, String path, String eventType) {
        EventSnapshot evt = new EventSnapshot();
        evt.setEventType(eventType);
        evt.setPath(path);
        evt.setObjectType(EventType.WORKFLOW);
        return evt;
    }

    private void addEvent(EventSnapshot eventSnapshot) {
        if (eventSnapshot != null && eventSnapshot.getEventId() != null && events.add(eventSnapshot)) {
            if (isDebugEnabled) {
                LOGGER.debug("add event for " + controllerId + ": " + eventSnapshot.toString());
            }
            if (atLeastOneConditionIsHold()) {
                signalAll();
            }
        }
    }
    
    private synchronized void signalAll() {
        try {
            if (isDebugEnabled) {
                LOGGER.debug("Try signal all Events of '" + controllerId + "'");
            }
            if (atLeastOneConditionIsHold() && EventServiceFactory.lock.tryLock(200L, TimeUnit.MILLISECONDS)) {
                try {
                    conditions.stream().forEach(EventCondition::signalAll); //without .parallel()
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
        //LOGGER.info(events.toString());
        if (events.stream().parallel().anyMatch(e -> eventId < e.getEventId())) {
            if (isDebugEnabled) {
                LOGGER.debug("has old Event for " + controllerId + ": true");
            }
//            if (isCurrentController.get() && events.stream().parallel().anyMatch(e -> EventType.PROBLEM.equals(e.getObjectType()))) {
//                LOGGER.info("hasProblemEvent for " + controllerId + ": true");
//                EventServiceFactory.signalEvent(eventArrived);
//                return EventServiceFactory.Mode.IMMEDIATLY;
//            }
            EventServiceFactory.signalEvent(eventArrived);
            return EventServiceFactory.Mode.TRUE;
        }
        if (isDebugEnabled) {
            LOGGER.debug("has old Event for " + controllerId + ": false");
        }
        return EventServiceFactory.Mode.FALSE;
    }

    private void setOrders() {
        try {
            orders = Proxy.of(controllerId).currentState().ordersBy(JOrderPredicates.any())
                .collect(Collectors.toMap(o -> o.id().string().substring(0,24), o -> mapWorkflowId(o.workflowId())));
        } catch (Exception e) {
            //
        }
    }

}
