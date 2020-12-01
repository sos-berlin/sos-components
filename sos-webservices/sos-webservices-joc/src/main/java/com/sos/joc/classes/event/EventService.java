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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.problem.ProblemEvent;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.event.EventSnapshot;
import com.sos.joc.model.event.EventType;

import js7.controller.data.events.ControllerEvent;
import js7.data.agent.AgentRefEvent;
import js7.data.cluster.ClusterEvent;
import js7.data.event.Event;
import js7.data.event.KeyedEvent;
import js7.data.event.Stamped;
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
import js7.proxy.javaapi.eventbus.JControllerEventBus;

public class EventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventService.class);
    // OrderAdded, OrderProcessed, OrderProcessingStarted$ extends OrderCoreEvent
    // OrderStarted, OrderProcessingKilled$, OrderFailed, OrderFailedInFork, OrderRetrying, OrderBroken extends OrderActorEvent
    // OrderFinished, OrderCancelled, OrderRemoved$ extends OrderTerminated
    private static List<Class<? extends Event>> eventsOfController = Arrays.asList(ControllerEvent.class, ClusterEvent.class, AgentRefEvent.class,
            OrderStarted$.class, OrderProcessingKilled$.class, OrderFailed.class, OrderFailedInFork.class, OrderRetrying.class, OrderBroken.class,
            OrderTerminated.class, OrderCoreEvent.class, OrderAdded.class, OrderProcessed.class, OrderProcessingStarted$.class);
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

    @Subscribe({ ProblemEvent.class })
    public void doSomethingWithProblemEvent(ProblemEvent evt) throws JsonProcessingException {
        //if (isCurrentController.get() && evt.getControllerId().equals(controllerId)) {
        if (evt.getControllerId().equals(controllerId)) {
            EventSnapshot eventSnapshot = new EventSnapshot();
            eventSnapshot.setEventId(evt.getEventId());
            eventSnapshot.setObjectType(EventType.PROBLEM);
            eventSnapshot.setAccessToken(evt.getKey());
            eventSnapshot.setMessage(evt.getVariables().get("message"));
            addEvent(eventSnapshot);
        }
    }

    BiConsumer<Stamped<KeyedEvent<Event>>, JControllerState> callbackOfController = (stampedEvt, currentState) -> {
        KeyedEvent<Event> event = stampedEvt.value();
        Object key = event.key();
        Event evt = event.event();

        EventSnapshot eventSnapshot = new EventSnapshot();

        if (evt instanceof OrderEvent) {
            final OrderId orderId = (OrderId) key;
            Optional<JOrder> opt = currentState.idToOrder(orderId);
            if (opt.isPresent()) {
                // eventSnapshots.put(createWorkflowEventOfOrder(opt.get().workflowId().path().string()));
                addEvent(createWorkflowEventOfOrder(opt.get().workflowId().path().string()));
                eventSnapshot.setPath(opt.get().workflowId().path().string() + "," + orderId.string());
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
                addEvent(createTaskEventOfOrder(opt.get().workflowId().path().string()));
            }
        } else if (evt instanceof ControllerEvent || evt instanceof ClusterEvent) {
            eventSnapshot.setEventType("ControllerStateChanged");
            eventSnapshot.setObjectType(EventType.CONTROLLER);
            eventSnapshot.setPath(controllerId);
        } else if (evt instanceof AgentRefEvent) {
            eventSnapshot.setEventType(evt.getClass().getSimpleName()); // AgentAdded and AgentUpdated
            eventSnapshot.setObjectType(EventType.AGENT);
            eventSnapshot.setPath(controllerId);
        }

        if (eventSnapshot.getObjectType() != null) {
            eventSnapshot.setEventId(stampedEvt.eventId());
            // eventSnapshots.put(eventSnapshot);
            addEvent(eventSnapshot);
        }
    };

    private EventSnapshot createWorkflowEventOfOrder(String workflowPath) {
        EventSnapshot workflowEventSnapshot = new EventSnapshot();
        workflowEventSnapshot.setEventType("WorkflowStateChanged");
        workflowEventSnapshot.setObjectType(EventType.WORKFLOW);
        workflowEventSnapshot.setPath(workflowPath);
        return workflowEventSnapshot;
    }

    private EventSnapshot createTaskEventOfOrder(String workflowPath) {
        EventSnapshot workflowEventSnapshot = new EventSnapshot();
        workflowEventSnapshot.setEventType("JobStateChanged");
        workflowEventSnapshot.setObjectType(EventType.JOB);
        workflowEventSnapshot.setPath(workflowPath);
        return workflowEventSnapshot;
    }

    private void addEvent(EventSnapshot eventSnapshot) {
        // consider that eventId is deleted from equals and hashcode method in EventSnapshot
        events.add(eventSnapshot);
        LOGGER.info("addEvent for " + controllerId + ": " + eventSnapshot.toString());
        EventServiceFactory.lock.lock();
        conditions.stream().parallel().forEach(Condition::signalAll);
        EventServiceFactory.lock.unlock();
    }

    protected EventServiceFactory.Mode hasOldEvent(Long eventId, Condition eventArrived) {
        if (events.stream().parallel().anyMatch(e -> eventId < e.getEventId())) {
            LOGGER.info("has old Event for " + controllerId + ": true");
//            if (isCurrentController.get() && events.stream().parallel().anyMatch(e -> EventType.PROBLEM.equals(e.getObjectType()))) {
//                LOGGER.info("hasProblemEvent for " + controllerId + ": true");
//                EventServiceFactory.signalEvent(eventArrived);
//                return EventServiceFactory.Mode.IMMEDIATLY;
//            }
            EventServiceFactory.signalEvent(eventArrived);
            return EventServiceFactory.Mode.TRUE;
        }
        LOGGER.info("has old Event for " + controllerId + ": false");
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
