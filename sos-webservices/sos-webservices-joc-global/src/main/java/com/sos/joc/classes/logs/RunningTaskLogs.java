package com.sos.joc.classes.logs;

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
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.controller.model.event.EventType;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.history.HistoryOrderTaskLog;
import com.sos.joc.model.job.RunningTaskLog;

public class RunningTaskLogs {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RunningTaskLogs.class);
    private final static long cleanupPeriodInMillis = TimeUnit.MINUTES.toMillis(2);
    private static RunningTaskLogs runningTaskLogs;
    private volatile Map<Long, CopyOnWriteArraySet<RunningTaskLog>> events = new ConcurrentHashMap<>();
    private volatile Set<Long> completeLogs = new CopyOnWriteArraySet<>();
    private volatile Set<Long> registeredTaskIds = new CopyOnWriteArraySet<>();
    
    public enum Mode {
        IMMEDIATLY, TRUE, FALSE;
    }
    
    private RunningTaskLogs() {
        EventBus.getInstance().register(this);
        
        new Timer().scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                Long eventId = Instant.now().toEpochMilli() - cleanupPeriodInMillis;
                Set<Long> toDelete = new HashSet<>();
                events.forEach((taskId, logs) -> {
                    if (!registeredTaskIds.contains(taskId)) {
                        toDelete.add(taskId);
                    } else {
                        logs.removeIf(e -> e.getEventId() < eventId);
                    }
                });
                toDelete.forEach(taskId -> events.remove(taskId));
                completeLogs.removeIf(taskId -> !registeredTaskIds.contains(taskId));
            }

        }, cleanupPeriodInMillis, cleanupPeriodInMillis);
    }

    public static synchronized RunningTaskLogs getInstance() {
        if (runningTaskLogs == null) {
            runningTaskLogs = new RunningTaskLogs();
        }
        return runningTaskLogs;
    }
    
    public synchronized void subscribe(Long taskId) {
        LOGGER.info("taskId '" + taskId + "' will be observed for log events" );
        registeredTaskIds.add(taskId);
    }
    
    public synchronized void unsubscribe(Long taskId) {
        registeredTaskIds.remove(taskId);
        LOGGER.info("taskId '" + taskId + "' won't be longer observed for log events" );
    }
    
    public Mode hasEvents(Long eventId, Long taskId) {
        if (events.containsKey(taskId)) {
            if (completeLogs.contains(taskId)) {
               return Mode.IMMEDIATLY; 
            } else if (events.get(taskId).stream().parallel().anyMatch(r -> eventId < r.getEventId())) {
                return Mode.TRUE;
            } else {
                return Mode.FALSE;
            }
        } else {
            return Mode.FALSE;
        }
    }
    
    public RunningTaskLog getRunningTaskLog(RunningTaskLog r) {
        SortedSet<Long> evtIds = new TreeSet<>(Comparator.comparing(Long::longValue));
        StringBuilder log = new StringBuilder();
        r.setComplete(false);
        events.get(r.getTaskId()).iterator().forEachRemaining(e -> {
            if (e.getEventId() != null && r.getEventId() < e.getEventId()) {
                if (e.getComplete()) {
                    r.setComplete(true);
                }
                log.append(e.getLog());
                evtIds.add(e.getEventId());
            }
        });
        if (!evtIds.isEmpty()) {
            r.setEventId(evtIds.last());
            r.setLog(log.toString());
        }
        return null;
    }
    
    @Subscribe({ HistoryOrderTaskLog.class })
    public void createHistoryTaskEvent(HistoryOrderTaskLog evt) {
        if (isRegistered(evt.getHistoryOrderStepId())) {
            LOGGER.info("log event for taskId " + evt.getHistoryOrderStepId() + " arrived" );
            RunningTaskLog event = new RunningTaskLog();
            event.setEventId(evt.getEventId());
            event.setComplete(EventType.OrderProcessed.value().equals(evt.getKey()));
            event.setLog(evt.getContent());
            event.setTaskId(evt.getHistoryOrderStepId());
            addEvent(event);
            if (event.getComplete()) {
                addCompleteness(event.getTaskId());
                unsubscribe(event.getTaskId());
            }
        }
    }
    
    private boolean isRegistered(Long taskId) {
        return registeredTaskIds.contains(taskId);
    }

    private synchronized void addEvent(RunningTaskLog event) {
        events.putIfAbsent(event.getTaskId(), new CopyOnWriteArraySet<RunningTaskLog>());
        if (events.get(event.getTaskId()).add(event)) {
            //signalAll
        }
    }
    
    private synchronized void addCompleteness(Long taskId) {
        completeLogs.add(taskId);
    }

}
