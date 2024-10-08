package com.sos.joc.classes.logs;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.controller.model.event.EventType;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.history.HistoryOrderTaskLog;
import com.sos.joc.event.bean.history.HistoryOrderTaskLogArrived;
import com.sos.joc.model.job.RunningTaskLog;

public class RunningTaskLogs {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunningTaskLogs.class);

    private final static long CLEANUP_PERIOD = TimeUnit.MINUTES.toMillis(2);
    private final static String DEFAULT_SESSION_IDENTIFIER = "common_session";
    private final static String EVENT_KEY_DELIMITER = ";";

    private static RunningTaskLogs runningTaskLogs;

    // key-><taskId>_<sessionIdentifir>
    private volatile Map<String, CopyOnWriteArraySet<RunningTaskLog>> events = new ConcurrentHashMap<>();
    private volatile Map<String, Set<Long>> completeLogs = new ConcurrentHashMap<>();
    private volatile Map<String, Set<Long>> registeredTaskIds = new ConcurrentHashMap<>();
    // key-><taskId>_<sessionIdentifir>, value=eventId(current millis)
    private volatile Map<String, Long> lastLogAPICalls = new ConcurrentHashMap<>();

    public enum Mode {
        COMPLETE, TRUE, FALSE, BROKEN;
    }

    public static synchronized RunningTaskLogs getInstance() {
        if (runningTaskLogs == null) {
            runningTaskLogs = new RunningTaskLogs();
        }
        return runningTaskLogs;
    }

    // TODO: Remove/optimize timer functionality? - it is only required for the time after running logs, not for the entire lifetime of the JOC Cockpit
    private RunningTaskLogs() {
        EventBus.getInstance().register(this);

        new Timer().scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[RunningTaskLogs][cleanup][before]events=" + events.size() + ",completeLogs=" + completeLogs.size());
                }
                Long eventId = Instant.now().toEpochMilli() - CLEANUP_PERIOD;
                Set<String> toDelete = new HashSet<>();
                events.forEach((taskIdAndSessionIdentifier, logs) -> {
                    if (!isRegistered(taskIdAndSessionIdentifier)) {
                        toDelete.add(taskIdAndSessionIdentifier);
                    } else {
                        logs.removeIf(e -> e.getEventId() < eventId);
                    }
                });
                toDelete.forEach(taskIdAndSessionIdentifier -> {
                    events.remove(taskIdAndSessionIdentifier);
                    lastLogAPICalls.remove(taskIdAndSessionIdentifier);
                });

                // remove while iteration
                Iterator<Map.Entry<String, Set<Long>>> iter = completeLogs.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, Set<Long>> entry = iter.next();
                    entry.getValue().removeIf(taskId -> !isRegistered(entry.getKey(), taskId));
                    if (entry.getValue().size() == 0) {
                        iter.remove();
                    }
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[RunningTaskLogs][cleanup][after]events=" + events.size() + ",completeLogs=" + completeLogs.size()
                            + ",lastLogAPICalls=" + lastLogAPICalls.size());
                }
            }

        }, CLEANUP_PERIOD, CLEANUP_PERIOD);
    }

    @Subscribe({ HistoryOrderTaskLog.class })
    public void createHistoryTaskEvent(HistoryOrderTaskLog evt) {
        if (isRegistered(evt.getSessionIdentifier(), evt.getHistoryOrderStepId())) {
            // LOGGER.debug("log event for taskId '" + evt.getHistoryOrderStepId() + "' arrived" );
            RunningTaskLog r = new RunningTaskLog();
            r.setEventId(evt.getEventId());
            r.setLog(evt.getContent());
            r.setTaskId(evt.getHistoryOrderStepId());
            r.setComplete(EventType.OrderProcessed.value().equals(evt.getKey()));

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[createHistoryTaskEvent][" + r.getTaskId() + "][" + evt.getKey() + "]complete=" + r.getComplete());
            }

            addEvent(evt.getSessionIdentifier(), r);
            if (r.getComplete()) {
                addCompleteness(evt.getSessionIdentifier(), r.getTaskId());
                unsubscribe(evt.getSessionIdentifier(), r.getTaskId());
            }
        }
    }

    public synchronized void subscribe(String sessionIdentifier, Long taskId) {
        String id = getSessionIdentifier(sessionIdentifier);
        registeredTaskIds.computeIfAbsent(id, k -> new HashSet<>()).add(taskId);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[subscribe][" + taskId + "]observed for log events of this session");
        }
    }

    public synchronized void unsubscribe(String sessionIdentifier, Long taskId) {
        String id = getSessionIdentifier(sessionIdentifier);
        registeredTaskIds.compute(id, (k, l) -> {
            if (l == null) {
                return null;
            }
            l.remove(taskId);
            return l.isEmpty() ? null : l;
        });
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[unsubscribe][" + taskId + "]no longer observed for log events of this session");
        }
    }

    public Mode hasEvents(String sessionIdentifier, Long eventId, Long taskId) {
        String eventKey = getEventKey(sessionIdentifier, taskId);
        if (events.containsKey(eventKey)) {
            if (isInCompleteness(sessionIdentifier, taskId)) {
                return Mode.COMPLETE;
            } else if (events.get(eventKey).stream().parallel().anyMatch(r -> eventId < r.getEventId())) {
                return Mode.TRUE;
            } else {
                return Mode.FALSE;
            }
        } else {
            if (!isRegistered(sessionIdentifier, taskId)) {
                return Mode.BROKEN;
            }
            return Mode.FALSE;
        }
    }

    public synchronized void registerLastLogAPICall(String sessionIdentifier, Long taskId) {
        // unsubscribe
        unsubscribe(sessionIdentifier, taskId);

        String eventKey = getEventKey(sessionIdentifier, taskId);
        int oldEvents = 0;
        // cleanup
        CopyOnWriteArraySet<RunningTaskLog> e = events.get(eventKey);
        if (e != null) {
            oldEvents = e.size();
            events.remove(eventKey);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[registerLastLogAPICall][taskId=" + taskId + "]oldEvents removed=" + oldEvents);
        }
        // register
        lastLogAPICalls.put(eventKey, Instant.now().toEpochMilli());
    }

    public synchronized boolean isBeforeLastLogAPICall(String sessionIdentifier, Long taskId, Long eventId, Long started, String range) {
        String key = getEventKey(sessionIdentifier, taskId);
        Long l = lastLogAPICalls.get(key);
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        if (l == null) {
            if (isDebugEnabled) {
                LOGGER.debug("[isBeforeLastLogAPICall][taskId=" + taskId + "][lastLogAPICall=null][eventId=" + eventId + "][" + range + "]false");
            }
            return false;
        } else {
            boolean r = l.longValue() > eventId.longValue();
            if (!r) {
                r = l.longValue() > started.longValue();
            }
            if (isDebugEnabled) {
                LOGGER.debug("[isBeforeLastLogAPICall][taskId=" + taskId + "][lastLogAPICall=" + l + "][eventId=" + eventId + "][started=" + started
                        + "][" + range + "]" + r);
            }
            return r;
        }
    }

    public RunningTaskLog getRunningTaskLog(String sessionIdentifier, RunningTaskLog r) {
        SortedSet<Long> evtIds = new TreeSet<>(Comparator.comparing(Long::longValue));
        StringBuilder log = new StringBuilder();
        r.setComplete(false);

        events.getOrDefault(getEventKey(sessionIdentifier, r.getTaskId()), new CopyOnWriteArraySet<RunningTaskLog>()).forEach(e -> {
            if (e.getEventId() != null && r.getEventId() < e.getEventId()) {
                if (e.getComplete()) {
                    r.setComplete(true);
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[getRunningTaskLog][" + r.getTaskId() + "]complete=" + r.getComplete());
                }

                log.append(e.getLog());
                evtIds.add(e.getEventId());
            }
        });
        if (!evtIds.isEmpty()) {
            r.setEventId(evtIds.last());
            r.setLog(log.toString());
        }
        return r;
    }

    public boolean isRegistered(String sessionIdentifier, Long taskId) {
        String id = getSessionIdentifier(sessionIdentifier);
        return Optional.ofNullable(registeredTaskIds.get(id)).map(l -> l.contains(taskId)).orElse(false);
    }

    private synchronized void addEvent(String sessionIdentifier, RunningTaskLog event) {
        // LOGGER.debug("try to add log event for taskId '" + event.getTaskId() + "'" );
        String eventKey = getEventKey(sessionIdentifier, event.getTaskId());
        events.putIfAbsent(eventKey, new CopyOnWriteArraySet<RunningTaskLog>());
        if (events.get(eventKey).add(event)) {
            EventBus.getInstance().post(new HistoryOrderTaskLogArrived(event.getTaskId(), event.getComplete(), sessionIdentifier));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[addEvent][" + event.getTaskId() + "]event posted");
            }
        }
    }

    private synchronized void addCompleteness(String sessionIdentifier, Long taskId) {
        String id = getSessionIdentifier(sessionIdentifier);
        completeLogs.computeIfAbsent(id, k -> new HashSet<>()).add(taskId);
    }

    private boolean isInCompleteness(String sessionIdentifier, Long taskId) {
        String id = getSessionIdentifier(sessionIdentifier);
        return Optional.ofNullable(completeLogs.get(id)).map(l -> l.contains(taskId)).orElse(false);
    }

    private String getEventKey(String sessionIdentifier, Long taskId) {
        return taskId + EVENT_KEY_DELIMITER + getSessionIdentifier(sessionIdentifier);
    }

    private boolean isRegistered(String taskIdAndSessionIdentifier) {
        String[] arr = taskIdAndSessionIdentifier.split(EVENT_KEY_DELIMITER, 1);
        try {
            return isRegistered(arr[1], Long.valueOf(arr[0]));
        } catch (Throwable e) {
            return false;
        }
    }

    private String getSessionIdentifier(String identifier) {
        return identifier != null ? identifier : DEFAULT_SESSION_IDENTIFIER;
    }

    public Map<String, Set<Long>> getRegisteredTaskIds() {
        return registeredTaskIds;
    }

    public Map<String, Set<Long>> getCompleteness() {
        return completeLogs;
    }

    public Map<String, CopyOnWriteArraySet<RunningTaskLog>> getEvents() {
        return events;
    }

}
