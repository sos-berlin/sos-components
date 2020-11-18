package com.sos.joc.classes.event;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.common.JobSchedulerObjectType;
import com.sos.joc.model.event.EventSnapshot;

import js7.controller.data.events.ControllerEvent;
//import js7.data.agent.AgentRefEvent;
import js7.data.cluster.ClusterEvent;
import js7.data.event.Event;
import js7.data.event.KeyedEvent;
import js7.data.event.Stamped;
import js7.data.order.OrderEvent;
import js7.data.order.OrderEvent.OrderActorEvent;
import js7.data.order.OrderEvent.OrderAdded;
import js7.data.order.OrderEvent.OrderBroken;
import js7.data.order.OrderEvent.OrderCoreEvent;
import js7.data.order.OrderEvent.OrderFailed;
import js7.data.order.OrderEvent.OrderFailedInFork;
import js7.data.order.OrderEvent.OrderProcessed;
//import js7.data.order.OrderEvent.OrderProcessingKilled$;
import js7.data.order.OrderEvent.OrderProcessingStarted$;
import js7.data.order.OrderEvent.OrderRetrying;
import js7.data.order.OrderEvent.OrderStarted$;
import js7.data.order.OrderEvent.OrderTerminated;
import js7.data.order.OrderId;
import js7.proxy.javaapi.data.controller.JControllerState;
import js7.proxy.javaapi.data.order.JOrder;
import js7.proxy.javaapi.eventbus.JControllerEventBus;

public class EventService {
    
    // OrderAdded, OrderProcessed, OrderProcessingStarted$s extends OrderCoreEvent
    // OrderStarted, OrderProcessingKilled$, OrderFailed, OrderFailedInFork, OrderRetrying, OrderBroken extends OrderActorEvent
    // OrderFinished, OrderCancelled extends OrderTerminated
    private static List<Class<? extends Event>> eventsOfController = null; //Arrays.asList(ControllerEvent.class, ClusterEvent.class, AgentRefEvent.class,
//            OrderStarted$.class, OrderProcessingKilled$.class, OrderFailed.class, OrderFailedInFork.class, OrderRetrying.class, OrderBroken.class,
//            OrderTerminated.class, OrderCoreEvent.class, OrderAdded.class, OrderProcessed.class, OrderProcessingStarted$.class);
    private String controllerId;
    private String accessToken;
    private volatile Events eventSnapshots = new Events();
    //private volatile Map<String, EventSnapshot> events = new ConcurrentHashMap<>();
    
    public EventService(String accessToken, String controllerId) {
        this.accessToken = accessToken;
        this.controllerId = controllerId;
    }
    
    public void startEventService(JControllerEventBus evtBus) throws JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException,
            DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            ExecutionException {
        if (evtBus == null) {
            evtBus = Proxy.of(controllerId).controllerEventBus();
        }
        evtBus.subscribe(eventsOfController, callbackOfController);
    }
    
    public List<EventSnapshot> getEvents() {
        return eventSnapshots.values();
    }
    
    BiConsumer<Stamped<KeyedEvent<Event>>, JControllerState> callbackOfController = (stampedEvt, currentState) -> {
        KeyedEvent<Event> event = stampedEvt.value();
        Object key = event.key();
        Event evt = event.event();
        
        EventSnapshot eventSnapshot = new EventSnapshot();
        
        if (evt instanceof ControllerEvent || evt instanceof ClusterEvent) {
            eventSnapshot.setEventType("ControllerStateChanged");
            eventSnapshot.setObjectType(JobSchedulerObjectType.CONTROLLER);
            eventSnapshot.setPath(controllerId);
//        } else if (evt instanceof AgentRefEvent) {
//            eventSnapshot.setEventType(evt.getClass().getSimpleName()); //AgentAdded and AgentUpdated
//            eventSnapshot.setObjectType(JobSchedulerObjectType.AGENTCLUSTER);
//            eventSnapshot.setPath(controllerId);
        } else if (evt instanceof OrderEvent) {
            final OrderId orderId = (OrderId) key;
            Optional<JOrder> opt = currentState.idToOrder(orderId);
            if (opt.isPresent()) {
                eventSnapshots.put(createWorkflowEventOfOrder(opt.get().workflowId().path().string()));
                eventSnapshot.setPath(opt.get().workflowId().path().string() + "," + orderId.string());
            } else {
                eventSnapshot.setPath(orderId.string());
            }
            eventSnapshot.setObjectType(JobSchedulerObjectType.ORDER);
            eventSnapshot.setEventType("OrderStateChanged");
            if (evt instanceof OrderAdded) {
                eventSnapshot.setEventType("OrderAdded");
            } else if (evt instanceof OrderTerminated) {
                eventSnapshot.setEventType("OrderTerminated");
            }
        }
        
        if (eventSnapshot.getObjectType() != null) {
            //eventSnapshot.setEventId(stampedEvt.eventId());
            eventSnapshots.put(eventSnapshot);
        }
    };
    
    private EventSnapshot createWorkflowEventOfOrder(String workflowPath) {
        EventSnapshot workflowEventSnapshot = new EventSnapshot();
        workflowEventSnapshot.setEventType("WorkflowStateChanged");
        workflowEventSnapshot.setObjectType(JobSchedulerObjectType.WORKFLOW);
        workflowEventSnapshot.setPath(workflowPath);
        return workflowEventSnapshot;
    }

}
