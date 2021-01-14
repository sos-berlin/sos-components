package com.sos.joc.classes.event;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.SessionNotExistException;
import com.sos.joc.model.common.Err;
import com.sos.joc.model.event.EventSnapshot;
import com.sos.joc.model.event.Event;

public class EventServiceFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EventServiceFactory.class);
    private static boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static EventServiceFactory eventServiceFactory;
    private volatile Map<String, EventService> eventServices = new ConcurrentHashMap<>();
    private final static long cleanupPeriodInMillis = TimeUnit.MINUTES.toMillis(6);
    protected static Lock lock = new ReentrantLock();
    
    public enum Mode {
        IMMEDIATLY, TRUE, FALSE;
    }
    
    private EventServiceFactory() {
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
            this.eventArrived.signalAll();
            this.hold.set(false);
        }

        public void await() throws InterruptedException {
            this.unhold.set(false);
            this.hold.set(true);
            this.eventArrived.await(6, TimeUnit.MINUTES);
        }
    }
    
    public static Event getEvents(String controllerId, Long eventId, String accessToken, EventCondition eventArrived, Session session, boolean isCurrentController) {
        return EventServiceFactory.getInstance()._getEvents(controllerId, eventId, accessToken, eventArrived, session, isCurrentController);
    }
    
    public EventService getEventService(String controllerId) throws JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException,
            DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            ExecutionException {
        synchronized (eventServices) {
            if (!eventServices.containsKey(controllerId)) {
                eventServices.put(controllerId, new EventService(controllerId));
                // cleanup old event each 6 minutes
                new Timer().scheduleAtFixedRate(new TimerTask() {

                    @Override
                    public void run() {
                        Long eventId = (Instant.now().toEpochMilli() - cleanupPeriodInMillis - TimeUnit.SECONDS.toMillis(30)) / 1000;
                        eventServices.get(controllerId).getEvents().removeIf(e -> e.getEventId() < eventId);
                    }

                }, cleanupPeriodInMillis, cleanupPeriodInMillis);
            }
            return eventServices.get(controllerId);
        }
    }
    
    public static EventCondition createCondition() {
        return new EventCondition(lock.newCondition());
    }
    
    private Event _getEvents(String controllerId, Long eventId, String accessToken, EventCondition eventArrived, Session session,
            boolean isCurrentController) {
        Event events = new Event();
        events.setNotifications(null); // TODO not yet implemented
        events.setControllerId(controllerId);
        events.setEventId(eventId); //default
        EventService service = null;
        if (isDebugEnabled) {
            LOGGER.debug("Listen Events of '" + controllerId + "' since " + eventId);
        }
        try {
            service = getEventService(controllerId);
            service.addCondition(eventArrived);
            //service.setIsCurrentController(isCurrentController);
            SortedSet<Long> evtIds = new TreeSet<>(Comparator.comparing(Long::longValue));
            Set<EventSnapshot> evt = new HashSet<>();
            Mode mode = service.hasOldEvent(eventId, eventArrived);
            if (Mode.FALSE.equals(mode)) {
                long delay = Math.min(cleanupPeriodInMillis - 1000, getSessionTimeout(session));
                if (isDebugEnabled) {
                    LOGGER.debug("waiting for Events for " + controllerId + ": maxdelay " + delay + "ms");
                }
                ScheduledFuture<Void> watchdog = startWatchdog(delay, eventArrived);
                mode = waitingForEvents(eventArrived);
                if (!watchdog.isDone()) {
                    watchdog.cancel(false);
                    if (isDebugEnabled) {
                        LOGGER.debug("event watchdog is cancelled");
                    }
                } else if (isDebugEnabled) {
                    LOGGER.debug("watchdog has stopped waiting events for " + controllerId);
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
                        evt.add(e);
                        evtIds.add(e.getEventId());
                    }
                });
            } else if (Mode.IMMEDIATLY.equals(mode)) {
                service.getEvents().iterator().forEachRemaining(e -> {
                    if (e.getEventId() != null && eventId < e.getEventId()) {
                        e.setEventId(e.getEventId() - 1L);
                        evt.add(e);
                        evtIds.add(e.getEventId());
                    }
                });
            }
            if (evt.isEmpty()) {
                //events.setEventSnapshots(null);
            } else {
                if (isDebugEnabled) {
                    LOGGER.debug("Events for " + controllerId + ": " + evt.toString());
                }
                events.setEventId(evtIds.last());
                events.setEventSnapshots(evt.stream().map(e -> cloneEvent(e)).distinct().collect(Collectors.toList()));
                //events.setEventSnapshots(evt.stream().collect(Collectors.toList()));
            }
        } catch (Exception e1) {
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
    
    private synchronized Mode waitingForEvents(EventCondition eventArrived) {
        try {
            if (eventArrived.isUnHold() && lock.tryLock(200L, TimeUnit.MILLISECONDS)) { // with timeout
                try {
                    //LOGGER.info("Waiting for Events ");
                    eventArrived.await();
                } catch (InterruptedException e1) {
                } finally {
                    lock.unlock();
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
        es.setEventType(e.getEventType());
        es.setMessage(e.getMessage());
        es.setObjectType(e.getObjectType());
        es.setPath(e.getPath());
        es.setWorkflow(e.getWorkflow());
        return es;
    }
    
    private long getSessionTimeout(Session session) throws SessionNotExistException {
        try {
            if (session == null) {
                throw new SessionNotExistException("session is invalid");
            }
            long l = session.getTimeout()-1000;
            if (l < 0) {
                return 0L;
            }
            return l;
        } catch (SessionNotExistException e) {
            throw e;
        } catch (InvalidSessionException e) {
            throw new SessionNotExistException(e);
        }
    }
    
    protected static synchronized void signalEvent(EventCondition eventArrived) {
        try {
            if (lock.tryLock(2L, TimeUnit.SECONDS)) {
                try {
                    eventArrived.signalAll();
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
        }
    }
    
    private static ScheduledFuture<Void> startWatchdog(long maxDelay, EventCondition eventArrived) {
        return Executors.newScheduledThreadPool(1).schedule(() -> {
            if (isDebugEnabled) {
                LOGGER.debug("start watchdog which stops waiting after for " + maxDelay + "ms");
            }
            signalEvent(eventArrived);
            return null;
        }, maxDelay, TimeUnit.MILLISECONDS);
    }

}
