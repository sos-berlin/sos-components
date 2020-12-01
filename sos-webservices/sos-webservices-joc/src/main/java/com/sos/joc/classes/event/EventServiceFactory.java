package com.sos.joc.classes.event;

import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
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
import com.sos.joc.model.event.JobSchedulerEvent;

public class EventServiceFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EventServiceFactory.class);
    private static EventServiceFactory eventServiceFactory;
    private volatile Map<String, EventService> eventServices = new ConcurrentHashMap<>();
    private final static Long cleanupPeriod = TimeUnit.MINUTES.toMillis(6);
    protected static Lock lock = new ReentrantLock();
    //protected static Condition eventArrived  = lock.newCondition();
    
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
    
    public static JobSchedulerEvent getEvents(String controllerId, Long eventId, String accessToken, Condition eventArrived, Session session, boolean isCurrentController) {
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
                        Long eventId = (Instant.now().toEpochMilli() - cleanupPeriod - TimeUnit.SECONDS.toMillis(30)) * 1000;
                        eventServices.get(controllerId).getEvents().removeIf(e -> e.getEventId() < eventId);
                    }

                }, cleanupPeriod, cleanupPeriod);
            }
            return eventServices.get(controllerId);
        }
    }
    
    public static Condition createCondition() {
        return lock.newCondition();
    }
    
    private JobSchedulerEvent _getEvents(String controllerId, Long eventId, String accessToken, Condition eventArrived, Session session, boolean isCurrentController) {
        JobSchedulerEvent events = new JobSchedulerEvent();
        events.setNotifications(null); // TODO not yet implemented
        events.setControllerId(controllerId);
        events.setEventId(eventId); //default
        SortedSet<EventSnapshot> evt;
        EventService service = null;
        try {
            service = getEventService(controllerId);
            service.addCondition(eventArrived);
            //service.setIsCurrentController(isCurrentController);
            evt = new TreeSet<>(Comparator.comparing(EventSnapshot::getEventId));
            Mode mode = service.hasOldEvent(eventId, eventArrived);
            if (mode == Mode.FALSE) {
                long delay = Math.min(cleanupPeriod - 1000, getSessionTimeout(session));
                LOGGER.debug("waiting for Events for " + controllerId + ": maxdelay " + delay + "ms");
                ScheduledFuture<Void> watchdog = startWatchdog(delay, eventArrived);
                mode = service.hasEvent(eventArrived);
                if (watchdog.isDone()) {
                    LOGGER.debug("Event for " + controllerId + " arrived");
                    watchdog.cancel(false);
                    LOGGER.debug("event watchdog is cancelled");
                } else {
                    LOGGER.debug("watchdog has stopped waiting events for " + controllerId);
                }
            }
            LOGGER.debug("received Events for " + controllerId + ": mode " + mode.name());
            if (mode == Mode.TRUE) {
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e1) {
                }
                service.getEvents().iterator().forEachRemaining(e -> {
                    if (e.getEventId() != null && eventId < e.getEventId()) {
                        evt.add(e);
                    }
                });
            } else if (mode == Mode.IMMEDIATLY) {
                service.getEvents().iterator().forEachRemaining(e -> {
                    if (e.getEventId() != null && eventId < e.getEventId()) {
                        evt.add(e);
                    }
                });
            }
            LOGGER.info("Events for " + controllerId + ": " + evt);
            if (evt.isEmpty()) {
                //events.setEventSnapshots(null);
            } else {
                events.setEventId(evt.last().getEventId());
                events.setEventSnapshots(evt.stream().collect(Collectors.toList()));
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
    
    protected static void signalEvent(Condition eventArrived) {
        lock.lock();
        try {
            eventArrived.signal();
        } finally {
            lock.unlock();
        }
    }
    
    private static ScheduledFuture<Void> startWatchdog(long maxDelay, Condition eventArrived) {
        return Executors.newScheduledThreadPool(1).schedule(() -> {
            LOGGER.debug("start watchdog which stops waiting after for " + maxDelay + "ms");
            signalEvent(eventArrived);
            return null;
        }, maxDelay, TimeUnit.MILLISECONDS);
    }

}
