package com.sos.joc.classes.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.sos.auth.rest.SOSShiroCurrentUsersList;
import com.sos.joc.Globals;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.event.EventServiceStart;
import com.sos.joc.event.bean.event.EventServiceStop;
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
import js7.data.cluster.ClusterEvent;
import js7.data.event.Event;
import js7.data.event.KeyedEvent;
import js7.data.event.Stamped;
import js7.proxy.javaapi.data.controller.JControllerState;
import js7.proxy.javaapi.eventbus.JControllerEventBus;

public class EventServiceFactory {
    
    /**
     * Map<accesstoken, Map<controllerId, List<events>>
     */
    private volatile Map<String, Map<String, List<EventSnapshot>>> eventSnapshots = new ConcurrentHashMap<>();
    private volatile Map<String, JControllerEventBus> eventBusses = new ConcurrentHashMap<>();
    private static List<Class<? extends Event>> eventsOfController = Arrays.asList(ControllerEvent.class, ClusterEvent.class);
    private static EventServiceFactory eventServiceFactory;
    private volatile Map<String, EventService> eventServices = new ConcurrentHashMap<>();
    
    private EventServiceFactory() {
        EventBus.getInstance().register(this);
    }
    
    protected static EventServiceFactory getInstance() {
        if (eventServiceFactory == null) {
            eventServiceFactory = new EventServiceFactory();
        }
        return eventServiceFactory;
    }
    
    protected EventService getEventService(String accessToken, String controllerId) {
        synchronized (eventServices) {
            EventService eventService = eventServices.get(accessToken + "," + controllerId);
            if (eventService == null) {
                eventService = new EventService(accessToken, controllerId);
                eventServices.put(accessToken + "," + controllerId, eventService);
            }
            return eventService;
        }
    }
    
    @Subscribe
    public void startEventService(EventServiceStart evt) {
        Map<String, List<EventSnapshot>> m = new ConcurrentHashMap<>();
        m.put(evt.getControllerId(), new ArrayList<>());
        eventSnapshots.put(evt.getAccessToken(), m);
        //collectProxyEvents();
    }
    
    @Subscribe
    public void stopEventService(EventServiceStop evt) {
        eventSnapshots.remove(evt.getAccessToken());
    }
    
    public void startEventService(String accessToken, String controllerId) throws JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException, DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, ExecutionException {
        CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.MINUTES.sleep(6);
                removeObsoleteAccessTokens();
            } catch (InterruptedException e) {
            }
        });
        JControllerEventBus evtBus = eventBusses.get(controllerId);
        if (evtBus == null) {
            evtBus = Proxy.of(controllerId).controllerEventBus();
        }
        evtBus.subscribe(eventsOfController, callbackOfController);
    }
    
    
    private synchronized void removeObsoleteAccessTokens() {
        obsoleteAccessTokens().stream().forEach(s -> eventSnapshots.remove(s));
    }
    
    private Set<String> obsoleteAccessTokens() {
        Set<String> currents = currentAccessTokens();
        return eventSnapshots.keySet().stream().filter(accessToken -> !currents.contains(accessToken)).collect(Collectors.toSet());
    }
    
    private static Set<String> currentAccessTokens() {
        SOSShiroCurrentUsersList userList = Globals.jocWebserviceDataContainer.getCurrentUsersList();
        if (userList != null) {
            return userList.getAccessTokens();
        }
        // should never occur
        return Collections.emptySet();
    }
    
    BiConsumer<Stamped<KeyedEvent<Event>>, JControllerState> callbackOfController = (stampedEvt, state) -> {
        KeyedEvent<Event> event = stampedEvt.value();
        Event evt = event.event();
        EventSnapshot eventSnapshot = new EventSnapshot();
        
        if (evt instanceof ControllerEvent || evt instanceof ClusterEvent) {
            eventSnapshot.setEventType("SchedulerStateChanged");
            eventSnapshot.setObjectType(JobSchedulerObjectType.CONTROLLER);
//            eventSnapshot.setPath(in.getJobscheduler().get(0).getJobschedulerId());
//            eventSnapshots.add(eventSnapshot);
        }
    };

}
