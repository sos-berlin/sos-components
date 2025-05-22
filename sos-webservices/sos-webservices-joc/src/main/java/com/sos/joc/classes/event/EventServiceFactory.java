package com.sos.joc.classes.event;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.interfaces.ISOSSession;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.SessionNotExistException;
import com.sos.joc.model.common.Err;
import com.sos.joc.model.event.Event;
import com.sos.joc.model.event.EventApprovalNotification;
import com.sos.joc.model.event.EventMonitoring;
import com.sos.joc.model.event.EventOrderMonitoring;
import com.sos.joc.model.event.EventSnapshot;

public class EventServiceFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EventServiceFactory.class);
    private static EventServiceFactory eventServiceFactory;
    private volatile ConcurrentMap<String, EventService> eventServices = new ConcurrentHashMap<>();
    //private final static long cleanupPeriodInMillis = TimeUnit.MINUTES.toMillis(3);
    private static long responsePeriodInMillis = TimeUnit.SECONDS.toMillis(55);
    private final static long maxResponsePeriodInMillis = TimeUnit.MINUTES.toMillis(3);
    private final static long minResponsePeriodInMillis = TimeUnit.SECONDS.toMillis(30);
    protected static Lock lock = new ReentrantLock();
    public static AtomicBoolean isClosed = new AtomicBoolean(false);
    
    
    public enum Mode {
        IMMEDIATLY, TRUE, FALSE;
    }
    
    private EventServiceFactory() {
        setResponsePeriodInMillis();
    }
    
    private static EventServiceFactory getInstance() {
        if (eventServiceFactory == null) {
            eventServiceFactory = new EventServiceFactory();
        }
        return eventServiceFactory;
    }
    
    public static class EventCondition {
        
        private Condition eventArrived = null;
        private AtomicBoolean hold = new AtomicBoolean(false);
        private AtomicBoolean unhold = new AtomicBoolean(true);
        
        public EventCondition(Condition eventArrived) {
            this.eventArrived = eventArrived;
        }
        
        public boolean isHold() {
            return this.hold.get();
        }
        
        public boolean isUnHold() {
            return this.unhold.get();
        }
        
        public void signalAll() {
            try {
                this.eventArrived.signalAll();
                this.hold.set(false);
//                LOGGER.info("signalAll");
            } catch (IllegalMonitorStateException e) {
                LOGGER.warn("IllegalMonitorStateException at signalAll");
            } catch (Exception e) {
                //
            }
        }

        public void await(long time) throws InterruptedException {
            this.unhold.set(false);
            this.hold.set(true);
            this.eventArrived.await(time, TimeUnit.MILLISECONDS);
        }
    }
    
    private static void setResponsePeriodInMillis() {
        // responsePeriodInMillis between cleanupPeriodInMillis=3min and minResponsePeriodInMillis=30s
        responsePeriodInMillis = Math.min(maxResponsePeriodInMillis, Math.max(TimeUnit.SECONDS.toMillis(Globals.maxResponseDuration),
                minResponsePeriodInMillis)) - 1010; //1010 is empirically determined from the measured response times of the web developer tools
    }
    
    public static void closeEventServices() {
        LOGGER.info("closing all event services");
        EventServiceFactory.getInstance().eventServices.values().parallelStream().forEach(EventService::close);
        EventServiceFactory.isClosed.set(true);
    }
    
    public static Event getEvents(String controllerId, Long eventId, ISOSSession session) throws SessionNotExistException {
        return EventServiceFactory.getInstance()._getEvents(controllerId, eventId, session);
    }
    
    public EventService getEventService(String controllerId) {
        synchronized (eventServices) {
            if (!eventServices.containsKey(controllerId)) {
                eventServices.put(controllerId, new EventService(controllerId));
                // cleanup old event each 3 minutes
                new Timer().scheduleAtFixedRate(new TimerTask() {

                    @Override
                    public void run() {
                        Long eventId = (Instant.now().toEpochMilli() - responsePeriodInMillis - TimeUnit.SECONDS.toMillis(30)) / 1000;
                        eventServices.get(controllerId).getEvents().removeIf(e -> e.getEventId() < eventId);
                    }

                }, responsePeriodInMillis, responsePeriodInMillis);
            }
            EventService es = eventServices.get(controllerId);
            es.startEventService();
            return es;
        }
    }
    
    public static EventCondition createCondition() {
        return new EventCondition(lock.newCondition());
    }
    
    private Event _getEvents(String controllerId, Long eventId, ISOSSession session) throws SessionNotExistException {
        Event events = new Event();
        events.setControllerId(controllerId);
        events.setEventId(eventId); //default
        EventService service = null;
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        if (isDebugEnabled) {
            LOGGER.debug("Listen Events of '" + controllerId + "' since " + eventId);
        }
        setResponsePeriodInMillis();
        EventCondition eventArrived = createCondition();
        try {
            service = getEventService(controllerId);
            SortedSet<Long> evtIds = new TreeSet<>(Comparator.comparing(Long::longValue));
            Set<EventSnapshot> evt = new HashSet<>();
            Set<EventSnapshot> agentEvt = new HashSet<>();
            Set<EventMonitoring> evtM = new HashSet<>();
            Set<EventOrderMonitoring> evtO = new HashSet<>();
            Set<EventApprovalNotification> evtA = new HashSet<>();
            Mode mode = service.hasOldEvent(eventId, eventArrived);
            if (Mode.FALSE.equals(mode)) {
                long delay = Math.min(responsePeriodInMillis - 1000, getSessionTimeout(session));
                if (isDebugEnabled) {
                    LOGGER.debug("waiting for Events for " + controllerId + ": maxdelay " + delay + "ms");
                }
                if (delay > 200) {
                    mode = waitingForEvents(eventArrived, service, delay);
                } else {
                    if (delay > 0) {
                       TimeUnit.MILLISECONDS.sleep(delay); 
                    }
                    mode = Mode.TRUE;
                }
            }
            if (isDebugEnabled) {
                LOGGER.debug("received Events for " + controllerId + ": mode " + mode.name());
            }
            if (Mode.TRUE.equals(mode)) {
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e1) {
                }
                service.getEvents().iterator().forEachRemaining(e -> {
                    if (e.getEventId() != null && eventId < e.getEventId()) {
                        e.setEventId(e.getEventId() - 1L);
                        if (e instanceof EventSnapshot) {
                            if (((EventSnapshot) e).getEventType().equals("AgentCoupling")) {
                                agentEvt.add((EventSnapshot) e);
                            } else {
                                evt.add((EventSnapshot) e);
                            }
                        } else if (e instanceof EventMonitoring) {
                            evtM.add((EventMonitoring) e);
                        } else if (e instanceof EventOrderMonitoring) {
                            evtO.add((EventOrderMonitoring) e);
                        } else if (e instanceof EventApprovalNotification) {
                            evtA.add((EventApprovalNotification) e);
                        }
                        evtIds.add(e.getEventId());
                    }
                });
            } else if (Mode.IMMEDIATLY.equals(mode)) {
                service.getEvents().iterator().forEachRemaining(e -> {
                    if (e.getEventId() != null && eventId < e.getEventId()) {
                        e.setEventId(e.getEventId() - 1L);
                        if (e instanceof EventSnapshot) {
                            if (((EventSnapshot) e).getEventType().equals("AgentCoupling")) {
                                agentEvt.add((EventSnapshot) e);
                            } else {
                                evt.add((EventSnapshot) e);
                            }
                        } else if (e instanceof EventMonitoring) {
                            evtM.add((EventMonitoring) e);
                        } else if (e instanceof EventOrderMonitoring) {
                            evtO.add((EventOrderMonitoring) e);
                        } else if (e instanceof EventApprovalNotification) {
                            evtA.add((EventApprovalNotification) e);
                        }
                        evtIds.add(e.getEventId());
                    }
                });
            }
            agentEvt.stream().collect(Collectors.groupingBy(EventSnapshot::getPath)).forEach((a, e) -> {
                if (e.size() == 1) {
                    evt.add(e.get(0));
                }
            });
            if (evt.isEmpty() && agentEvt.isEmpty() && evtM.isEmpty() && evtO.isEmpty() && evtA.isEmpty()) {
                //events.setEventSnapshots(null);
            } else {
                if (isDebugEnabled) {
                    if (!evt.isEmpty()) {
                        LOGGER.debug("Events for " + controllerId + ": " + evt.toString());
                    }
                    if (!evtM.isEmpty()) {
                        LOGGER.debug("System monitoring events for " + controllerId + ": " + evtM.toString());
                    }
                    if (!evtO.isEmpty()) {
                        LOGGER.debug("Order monitoring events for " + controllerId + ": " + evtO.toString());
                    }
                    if (!evtA.isEmpty()) {
                        LOGGER.debug("Approval notification events for " + evtA.toString());
                    }
                }
                events.setEventId(evtIds.last());
                events.setEventSnapshots(evt.stream().map(e -> cloneEvent(e)).distinct().collect(Collectors.toList()));
                events.setEventsFromSystemMonitoring(evtM.stream().map(e -> cloneEventM(e)).distinct().collect(Collectors.toList()));
                events.setEventsFromOrderMonitoring(evtO.stream().map(e -> cloneEventO(e)).distinct().collect(Collectors.toList()));
                events.setEventsFromApprovalRequests(evtA.stream().map(e -> cloneEventA(e)).distinct().collect(Collectors.toList()));
            }
        } catch (SessionNotExistException e1) {
            throw e1;
        } catch (Throwable e1) {
            Err err = new Err();
            err.setCode("JOC-410");
            err.setMessage(e1.toString());
            events.setError(err);
            events.setEventSnapshots(null);
            LOGGER.error("", e1);
        } finally {
            if (service != null) {
                service.removeCondition(eventArrived);
            }
        }
        
        return events;
    }
    
    private static Mode waitingForEvents(EventCondition eventArrived, EventService service, long time) {
        try {
            if (eventArrived.isUnHold() && lock.tryLock(200L, TimeUnit.MILLISECONDS)) { // with timeout
                try {
//                    LOGGER.info("Waiting for Events ");
                    service.addCondition(eventArrived);
                    eventArrived.await(time);
                } catch (InterruptedException e1) {
                } finally {
                    try {
                        lock.unlock();
                    } catch (IllegalMonitorStateException e) {
                        LOGGER.warn("IllegalMonitorStateException at unlock lock after await");
                    }
                }
            }
        } catch (InterruptedException e) {
        }
//        if (events.stream().parallel().anyMatch(e -> EventType.PROBLEM.equals(e.getObjectType()))) {
//            LOGGER.info("ProblemEvent for " + controllerId + ": true");
//            return EventServiceFactory.Mode.IMMEDIATLY;
//        }
        return Mode.TRUE;
    }
    
    private static EventSnapshot cloneEvent(EventSnapshot e) {
        //LOGGER.info("Clone events for " + e.toString());
        EventSnapshot es = new EventSnapshot();
        es.setAccessToken(e.getAccessToken());
        es.setEventId(null);
        if ("AgentCoupling".equals(e.getEventType())) {
            es.setEventType("AgentStateChanged");
        } else {
            es.setEventType(e.getEventType());
        }
        es.setMessage(e.getMessage());
        es.setObjectType(e.getObjectType());
        es.setPath(e.getPath());
        es.setWorkflow(e.getWorkflow());
        return es;
    }
    
    private static EventMonitoring cloneEventM(EventMonitoring e) {
        //LOGGER.info("Clone events for " + e.toString());
        EventMonitoring es = new EventMonitoring();
        es.setCategory(e.getCategory());
        es.setEventId(null);
        es.setLevel(e.getLevel());
        es.setRequest(e.getRequest());
        es.setMessage(e.getMessage());
        es.setSource(e.getSource());
        es.setTimestamp(e.getTimestamp());
        return es;
    }
    
    private static EventOrderMonitoring cloneEventO(EventOrderMonitoring e) {
        //LOGGER.info("Clone events for " + e.toString());
        EventOrderMonitoring es = new EventOrderMonitoring();
        es.setEventId(null);
        es.setLevel(e.getLevel());
        es.setJobName(e.getJobName());
        es.setMessage(e.getMessage());
        es.setOrderId(e.getOrderId());
        es.setWorkflowName(e.getWorkflowName());
        es.setTimestamp(e.getTimestamp());
        return es;
    }
    
    private static EventApprovalNotification cloneEventA(EventApprovalNotification e) {
        //LOGGER.info("Clone events for " + e.toString());
        EventApprovalNotification es = new EventApprovalNotification();
        es.setEventId(null);
        es.setApprover(e.getApprover());
        es.setRequestor(e.getRequestor());
        es.setNumOfPendingApprovals(e.getNumOfPendingApprovals());
        es.setEventType(e.getEventType());
        es.setApproverState(e.getApproverState());
        return es;
    }
    
    private long getSessionTimeout(ISOSSession session) throws SessionNotExistException {
        try {
            if (session == null) {
                throw new SessionNotExistException("session is invalid");
            }
            long timeout = session.getTimeout();
            if (timeout < 0L) {
                return responsePeriodInMillis;
            }
            return Math.max(0L, timeout - 1000L);
        } catch (SessionNotExistException e) {
            throw e;
        } 
    }
    
    protected synchronized static void signalEvent(EventCondition eventArrived) {
        try {
            if (lock.tryLock(2L, TimeUnit.SECONDS)) {
                try {
                    eventArrived.signalAll();
                } finally {
                    try {
                        lock.unlock();
                    } catch (IllegalMonitorStateException e) {
                        LOGGER.warn("IllegalMonitorStateException at unlock lock after signal");
                    }
                }
            }
        } catch (InterruptedException e) {
        }
    }

}
