package com.sos.joc.classes.event;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jobscheduler.model.workflow.WorkflowId;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.proxy.ProxyUser;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.problem.ProblemEvent;
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

import js7.controller.data.events.AgentRefStateEvent;
import js7.controller.data.events.ControllerEvent;
import js7.data.agent.AgentName;
import js7.data.agent.AgentRefEvent;
import js7.data.cluster.ClusterEvent;
import js7.data.event.Event;
import js7.data.event.KeyedEvent;
import js7.data.event.Stamped;
import js7.data.item.RepoEvent.ItemEvent;
import js7.data.order.OrderEvent;
import js7.data.order.OrderEvent.OrderAdded;
import js7.data.order.OrderEvent.OrderBroken;
import js7.data.order.OrderEvent.OrderCoreEvent;
import js7.data.order.OrderEvent.OrderFailed;
import js7.data.order.OrderEvent.OrderFailedInFork;
import js7.data.order.OrderEvent.OrderProcessed;
import js7.data.order.OrderEvent.OrderProcessingKilled$;
import js7.data.order.OrderEvent.OrderProcessingStarted$;
import js7.data.order.OrderEvent.OrderRetrying;
import js7.data.order.OrderEvent.OrderStarted$;
import js7.data.order.OrderEvent.OrderTerminated;
import js7.data.order.OrderId;
import js7.proxy.javaapi.data.controller.JControllerState;
import js7.proxy.javaapi.data.order.JOrder;
import js7.proxy.javaapi.data.workflow.JWorkflowId;
import js7.proxy.javaapi.eventbus.JControllerEventBus;

public class EventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventService.class);
    // OrderAdded, OrderProcessed, OrderProcessingStarted$ extends OrderCoreEvent
    // OrderStarted, OrderProcessingKilled$, OrderFailed, OrderFailedInFork, OrderRetrying, OrderBroken extends OrderActorEvent
    // OrderFinished, OrderCancelled, OrderRemoved$ extends OrderTerminated
    private static List<Class<? extends Event>> eventsOfController = Arrays.asList(ControllerEvent.class, ClusterEvent.class, AgentRefEvent.class,
            AgentRefStateEvent.class, OrderStarted$.class, OrderProcessingKilled$.class, OrderFailed.class, OrderFailedInFork.class, OrderRetrying.class, OrderBroken.class,
            OrderTerminated.class, OrderCoreEvent.class, OrderAdded.class, OrderProcessed.class, OrderProcessingStarted$.class, ItemEvent.class);
    private String controllerId;
    private volatile CopyOnWriteArraySet<EventSnapshot> events = new CopyOnWriteArraySet<>();
    private AtomicBoolean isCurrentController = new AtomicBoolean(false);
    private JControllerEventBus evtBus = null;
    private volatile CopyOnWriteArraySet<Condition> conditions = new CopyOnWriteArraySet<>();

    public EventService(String controllerId) throws JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException,
            DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            ExecutionException {
        this.controllerId = controllerId;
        EventBus.getInstance().register(this);
        startEventService();
    }

    public void startEventService() throws JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException, DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, ExecutionException {
        if (evtBus == null) {
            evtBus = Proxy.of(controllerId).controllerEventBus();
        }
        evtBus.subscribe(eventsOfController, callbackOfController);
    }

    public void addCondition(Condition cond) {
        conditions.add(cond);
    }
    
    public void removeCondition(Condition cond) {
        conditions.remove(cond);
    }
    
    public CopyOnWriteArraySet<EventSnapshot> getEvents() {
        return events;
    }

    public void setIsCurrentController(boolean val) {
        isCurrentController.set(val);
    }
    
    @Subscribe({ ProxyRestarted.class, ProxyRemoved.class })
    public void doSomethingWithEvent(ProxyEvent evt) throws JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException,
            DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            ExecutionException {
        if (evt.getControllerId().equals(controllerId) && evt.getKey().equals(ProxyUser.JOC.name())) {
            evtBus.close();
            if (evt instanceof ProxyRestarted) {
                evtBus = Proxy.of(controllerId).controllerEventBus();
                evtBus.subscribe(eventsOfController, callbackOfController);
            }
        }
    }

    @Subscribe({ ProblemEvent.class })
    public void createEvent(ProblemEvent evt) {
        //if (isCurrentController.get() && evt.getControllerId().equals(controllerId)) {
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
    
    @Subscribe({ com.sos.joc.event.bean.cluster.ClusterEvent.class })
    public void createEvent(com.sos.joc.event.bean.cluster.ClusterEvent evt) {
        EventSnapshot eventSnapshot = new EventSnapshot();
        eventSnapshot.setEventId(evt.getEventId());
        eventSnapshot.setEventType("JOCStateChanged");
        eventSnapshot.setObjectType(EventType.JOCCLUSTER);
        addEvent(eventSnapshot);
    }

    BiConsumer<Stamped<KeyedEvent<Event>>, JControllerState> callbackOfController = (stampedEvt, currentState) -> {
        KeyedEvent<Event> event = stampedEvt.value();
        long eventId = stampedEvt.eventId() / 1000000; //eventId per second
        Object key = event.key();
        Event evt = event.event();

        EventSnapshot eventSnapshot = new EventSnapshot();

        if (evt instanceof OrderEvent) {
            final OrderId orderId = (OrderId) key;
            Optional<JOrder> opt = currentState.idToOrder(orderId);
            if (opt.isPresent()) {
                WorkflowId w = mapWorkflowId(opt.get().workflowId());
                addEvent(createWorkflowEventOfOrder(eventId, w));
                eventSnapshot.setPath(orderId.string());
                eventSnapshot.setWorkflow(w);
            } else {
                eventSnapshot.setPath(orderId.string());
            }
            eventSnapshot.setObjectType(EventType.ORDER);
            eventSnapshot.setEventType("OrderStateChanged");
            if (evt instanceof OrderAdded) {
                eventSnapshot.setEventType("OrderAdded");
            } else if (evt instanceof OrderTerminated) {
                eventSnapshot.setEventType("OrderTerminated");
            } else if (evt instanceof OrderProcessingStarted$ || evt instanceof OrderProcessed || evt instanceof OrderProcessingKilled$) {
                if (opt.isPresent()) {
                    addEvent(createTaskEventOfOrder(eventId, mapWorkflowId(opt.get().workflowId())));
                }
            }
            
        } else if (evt instanceof ControllerEvent || evt instanceof ClusterEvent) {
            eventSnapshot.setEventType("ControllerStateChanged");
            eventSnapshot.setObjectType(EventType.CONTROLLER);
            
        } else if (evt instanceof ItemEvent) {
            final String p = ((ItemEvent) evt).path().string();
            String[] pathParts = p.split(":", 2);
            eventSnapshot.setEventType(evt.getClass().getSimpleName()); // ItemAdded and ItemUpdated etc.
            eventSnapshot.setPath(pathParts[1]);
            try {
                eventSnapshot.setObjectType(EventType.fromValue(pathParts[0].toUpperCase()));
            } catch (Exception e) {
                //
            }
            
        } else if (evt instanceof AgentRefEvent.AgentAdded || evt instanceof AgentRefEvent.AgentUpdated) {
            eventSnapshot.setEventType(evt.getClass().getSimpleName());
            eventSnapshot.setPath(((AgentName) key).string());
            eventSnapshot.setObjectType(EventType.AGENT);
            
        } else if (evt instanceof AgentRefStateEvent && !(evt instanceof AgentRefStateEvent.AgentEventsObserved)) {
            eventSnapshot.setEventType("AgentStateChanged");
            eventSnapshot.setPath(((AgentName) key).string());
            eventSnapshot.setObjectType(EventType.AGENT);
        }

        if (eventSnapshot.getObjectType() != null) {
            eventSnapshot.setEventId(eventId);
            addEvent(eventSnapshot);
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

    private void addEvent(EventSnapshot eventSnapshot) {
        if (events.add(eventSnapshot)) {
            LOGGER.debug("addEvent for " + controllerId + ": " + eventSnapshot.toString());
            EventServiceFactory.lock.lock();
            conditions.stream().parallel().forEach(Condition::signalAll);
            EventServiceFactory.lock.unlock();
        }
    }

    protected EventServiceFactory.Mode hasOldEvent(Long eventId, Condition eventArrived) {
        if (events.stream().parallel().anyMatch(e -> eventId < e.getEventId())) {
            LOGGER.debug("has old Event for " + controllerId + ": true");
//            if (isCurrentController.get() && events.stream().parallel().anyMatch(e -> EventType.PROBLEM.equals(e.getObjectType()))) {
//                LOGGER.info("hasProblemEvent for " + controllerId + ": true");
//                EventServiceFactory.signalEvent(eventArrived);
//                return EventServiceFactory.Mode.IMMEDIATLY;
//            }
            EventServiceFactory.signalEvent(eventArrived);
            return EventServiceFactory.Mode.TRUE;
        }
        LOGGER.debug("has old Event for " + controllerId + ": false");
        return EventServiceFactory.Mode.FALSE;
    }

    protected EventServiceFactory.Mode hasEvent(Condition eventArrived) {
        EventServiceFactory.lock.lock();
        try {
            eventArrived.await();
        } catch (InterruptedException e1) {
        } finally {
            EventServiceFactory.lock.unlock();
        }
//        if (events.stream().parallel().anyMatch(e -> EventType.PROBLEM.equals(e.getObjectType()))) {
//            LOGGER.info("ProblemEvent for " + controllerId + ": true");
//            return EventServiceFactory.Mode.IMMEDIATLY;
//        }
        return EventServiceFactory.Mode.TRUE;
    }

}
