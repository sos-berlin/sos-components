package com.sos.joc.classes.event;

import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.exceptions.SessionNotExistException;
import com.sos.joc.model.common.Err;
import com.sos.joc.model.event.EventSnapshot;
import com.sos.joc.model.event.JobSchedulerEvent;

public class EventServiceFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EventServiceFactory.class);
    private static EventServiceFactory eventServiceFactory;
    private volatile Map<String, EventService> eventServices = new ConcurrentHashMap<>();
    private final static Long cleanupPeriod = TimeUnit.MINUTES.toMillis(6);
    protected static Lock lock = new ReentrantLock();
    protected static Condition eventArrived  = lock.newCondition();
    
    public enum Mode {
        IMMEDIATLY, TRUE, FALSE;
    }
    
    private EventServiceFactory() {
//        EventBus.getInstance().register(this);
    }
    
    private static EventServiceFactory getInstance() {
        if (eventServiceFactory == null) {
            eventServiceFactory = new EventServiceFactory();
        }
        return eventServiceFactory;
    }
    
    public static JobSchedulerEvent getEvents(String controllerId, Long eventId, Session session, boolean isCurrentController) {
        return EventServiceFactory.getInstance()._getEvents(controllerId, eventId, session, isCurrentController);
    }
    
    public EventService getEventService(String controllerId) {
        synchronized (eventServices) {
            if (!eventServices.containsKey(controllerId)) {
                eventServices.put(controllerId, new EventService(controllerId));
                // cleanup old event each 6 Minutes
                new Timer().scheduleAtFixedRate(new TimerTask() {

                    @Override
                    public void run() {
                        Long eventId = (Instant.now().toEpochMilli() - cleanupPeriod - TimeUnit.SECONDS.toMillis(30)) * 1000;
                        eventServices.get(controllerId).getEvents().removeIf(e -> e.getEventId() < eventId);
                    }
                    
                }, cleanupPeriod, cleanupPeriod);
            }
            return eventServices.get(controllerId);
        }
    }
    
    private JobSchedulerEvent _getEvents(String controllerId, Long eventId, Session session, boolean isCurrentController) {
        JobSchedulerEvent events = new JobSchedulerEvent();
        events.setControllerId(controllerId);
        events.setEventId(eventId); //default
        SortedSet<EventSnapshot> evt;
        try {
            EventService service = getEventService(controllerId);
            service.setIsCurrentController(isCurrentController);
            evt = new TreeSet<>(Comparator.comparing(EventSnapshot::getEventId));
            Mode mode = service.hasOldEvent(eventId);
            if (mode == Mode.FALSE) {
                long delay = Math.min(cleanupPeriod - 1000, getSessionTimeout(session));
                LOGGER.info("waiting for Events for " + controllerId + ": maxdelay " + delay + "ms");
                ScheduledFuture<Void> watchdog = startWatchdog(delay);
                mode = service.hasEvent();
                if (watchdog.isDone()) {
                    LOGGER.info("Event for " + controllerId + " arrived");
                    watchdog.cancel(false);
                    LOGGER.info("event watchdog is cancelled");
                } else {
                    LOGGER.info("watchdog has stopped waiting events for " + controllerId);
                }
            }
            LOGGER.info("received Events for " + controllerId + ": mode " + mode.name());
            if (mode == Mode.TRUE) {
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e1) {
                }
                service.getEvents().iterator().forEachRemaining(e -> {
                    if (eventId <= e.getEventId()) {
                        evt.add(e);
                    }
                });
            } else if (mode == Mode.IMMEDIATLY) {
                service.getEvents().iterator().forEachRemaining(e -> {
                    if (eventId <= e.getEventId()) {
                        evt.add(e);
                    }
                });
            }
            LOGGER.info("Events for " + controllerId + ": " + evt);
            if (evt.isEmpty()) {
                events.setEventSnapshots(null);
            } else {
                events.setEventId(evt.last().getEventId());
                events.setEventSnapshots(evt.stream().map(e -> {
                    e.setEventId(null); 
                    return e;
                }).collect(Collectors.toList()));
            }
        } catch (Exception e1) {
            Err err = new Err();
            err.setCode("JOC-410");
            err.setMessage(e1.toString());
            events.setError(err);
        }
        
        return events;
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
    
    protected static void signalEvent() {
        lock.lock();
        try {
            eventArrived.signal();
        } finally {
            lock.unlock();
        }
    }
    
    private static ScheduledFuture<Void> startWatchdog(long maxDelay) {
        return Executors.newScheduledThreadPool(1).schedule(() -> {
            LOGGER.info("start watchdog which stops waiting after for " + maxDelay + "ms");
            signalEvent();
            return null;
        }, maxDelay, TimeUnit.MILLISECONDS);
    }

}
