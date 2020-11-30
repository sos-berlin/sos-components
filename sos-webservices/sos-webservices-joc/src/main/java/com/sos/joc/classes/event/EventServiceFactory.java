package com.sos.joc.classes.event;

import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
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
    
    private EventServiceFactory() {
//        EventBus.getInstance().register(this);
    }
    
    private static EventServiceFactory getInstance() {
        if (eventServiceFactory == null) {
            eventServiceFactory = new EventServiceFactory();
        }
        return eventServiceFactory;
    }
    
    public static JobSchedulerEvent getEvents(String controllerId, Long eventId, Session session) {
        return EventServiceFactory.getInstance()._getEvents(controllerId, eventId, session);
    }
    
    private EventService getEventService(String controllerId) {
        synchronized (eventServices) {
            if (!eventServices.containsKey(controllerId)) {
                eventServices.put(controllerId, new EventService(controllerId));
                // cleanup old event each 5 Minutes
                new Timer().scheduleAtFixedRate(new TimerTask() {

                    @Override
                    public void run() {
                        Long eventId = (Instant.now().toEpochMilli() - cleanupPeriod) * 1000;
                        eventServices.get(controllerId).getEvents().removeIf(e -> e.getEventId() < eventId);
                    }
                    
                }, cleanupPeriod, cleanupPeriod);
            }
            return eventServices.get(controllerId);
        }
    }
    
    private JobSchedulerEvent _getEvents(String controllerId, Long eventId, Session session) {
        JobSchedulerEvent events = new JobSchedulerEvent();
        events.setControllerId(controllerId);
        SortedSet<EventSnapshot> evt;
        try {
            EventService service = getEventService(controllerId);
            evt = new TreeSet<>(Comparator.comparing(EventSnapshot::getEventId));
            long delay = Math.min(cleanupPeriod - 1000, getSessionTimeout(session));
            LOGGER.info("waiting for Events for " + controllerId + ": maxdelay " + delay + "ms");
            EventService.Mode mode = service.hasEvent(eventId, delay);
            LOGGER.info("received Events for " + controllerId + ": mode " + mode.name());
            if (mode == EventService.Mode.TRUE) {
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e1) {
                }
                service.getEvents().iterator().forEachRemaining(e -> {
                    if (eventId <= e.getEventId()) {
                        evt.add(e);
                        e.setEventId(null);
                    }
                });
            } else if (mode == EventService.Mode.IMMEDIATLY) {
                service.getEvents().iterator().forEachRemaining(e -> {
                    if (eventId <= e.getEventId()) {
                        evt.add(e);
                        e.setEventId(null);
                    }
                });
            }
            
            if (evt.isEmpty()) {
                events.setEventId(eventId);
                events.setEventSnapshots(null);
            } else {
                events.setEventId(evt.last().getEventId());
                events.setEventSnapshots(evt.stream().map(e -> {
                    e.setEventId(null); 
                    return e;
                }).collect(Collectors.toList()));
            }
        } catch (Exception e1) {
            events.setEventId(eventId);
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
            if (l >= Long.MAX_VALUE) {
                return Long.MAX_VALUE;
            }
            return l;
        } catch (SessionNotExistException e) {
            throw e;
        } catch (InvalidSessionException e) {
            throw new SessionNotExistException(e);
        }
    }

}
