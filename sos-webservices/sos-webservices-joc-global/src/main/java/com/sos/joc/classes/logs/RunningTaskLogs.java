package com.sos.joc.classes.logs;

import java.lang.ref.WeakReference;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
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

import com.sos.auth.classes.SOSAuthCurrentAccountsList;
import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.util.SOSDate;
import com.sos.controller.model.event.EventType;
import com.sos.joc.Globals;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.history.HistoryOrderTaskLog;
import com.sos.joc.event.bean.history.HistoryOrderTaskLogArrived;
import com.sos.joc.model.job.RunningTaskLog;

/** This class uses the local scope variables historyId instead of taskId to use the same names as in RunningTaskLogHandler */
public class RunningTaskLogs {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunningTaskLogs.class);

    private final static long CLEANUP_PERIOD = TimeUnit.MINUTES.toMillis(2);
    private final static String DEFAULT_SESSION_IDENTIFIER = "common_session";
    private final static String EVENT_KEY_DELIMITER = ";";

    private static RunningTaskLogs runningTaskLogs;

    // 1 running log thread per <histroryId(taskId)>_<sessionIdentifir>
    // WeakReference - the object(Thread) can be garbage collected immediately if no strong reference to it exists
    private final static String RUNNING_LOG_THREAD_IDENTIFIER_DELIMITER = ";;;";
    private static final ConcurrentHashMap<String, WeakReference<Thread>> runningLogThreadsCache = new ConcurrentHashMap<>();

    // key-><histroryId(taskId)>_<sessionIdentifir>
    private volatile Map<String, CopyOnWriteArraySet<RunningTaskLog>> events = new ConcurrentHashMap<>();
    private volatile Map<String, Set<Long>> completeLogs = new ConcurrentHashMap<>();
    private volatile Map<String, Set<Long>> registeredHistoryIds = new ConcurrentHashMap<>();
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

        // isDaemon=true to ensure the JVM can exit immediately without waiting for this timer thread
        new Timer("Timer-CleanupRunningTaskLogs", true).scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                boolean isDebugEnabled = LOGGER.isDebugEnabled();
                if (isDebugEnabled) {
                    LOGGER.debug("[RunningTaskLogs][cleanup][before]events=" + events.size() + ", completeLogs=" + completeLogs.size()
                            + ", lastLogAPICalls=" + lastLogAPICalls.size() + ", runningLogThreadsCache=" + runningLogThreadsCache.size());
                }
                Long eventId = Instant.now().toEpochMilli() - CLEANUP_PERIOD;
                Set<String> toDelete = new HashSet<>();
                events.forEach((taskIdAndSessionIdentifier, logs) -> {
                    if (!cleanupCheckIfIsRegistered(taskIdAndSessionIdentifier)) {
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
                    entry.getValue().removeIf(historyId -> !isRegistered(entry.getKey(), historyId));
                    if (entry.getValue().size() == 0) {
                        iter.remove();
                    }
                }

                cleanupRunningLogThreadsCache(isDebugEnabled);

                if (isDebugEnabled) {
                    LOGGER.debug("[RunningTaskLogs][cleanup][after]events=" + events.size() + ", completeLogs=" + completeLogs.size()
                            + ", lastLogAPICalls=" + lastLogAPICalls.size() + ", runningLogThreadsCache=" + runningLogThreadsCache.size());
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
                LOGGER.debug("[createHistoryTaskEvent][historyId=" + r.getTaskId() + "][" + evt.getKey() + "]complete=" + r.getComplete());
            }

            addEvent(evt.getSessionIdentifier(), r);
            if (r.getComplete()) {
                addCompleteness(evt.getSessionIdentifier(), r.getTaskId());
                unsubscribe(evt.getSessionIdentifier(), r.getTaskId());
            }
        }
    }

    public synchronized void subscribe(String sessionIdentifier, Long historyId) {
        String id = getSessionIdentifier(sessionIdentifier);
        registeredHistoryIds.computeIfAbsent(id, k -> new HashSet<>()).add(historyId);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[subscribe][historyId=" + historyId + "]observed for log events of this session");
        }
    }

    public synchronized void unsubscribe(String sessionIdentifier, Long historyId) {
        String id = getSessionIdentifier(sessionIdentifier);
        registeredHistoryIds.compute(id, (k, l) -> {
            if (l == null) {
                return null;
            }
            l.remove(historyId);
            return l.isEmpty() ? null : l;
        });
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[unsubscribe][historyId=" + historyId + "]no longer observed for log events of this session");
        }
    }

    public Mode hasEvents(String sessionIdentifier, Long eventId, Long historyId) {
        String eventKey = getEventKey(sessionIdentifier, historyId);
        if (events.containsKey(eventKey)) {
            if (isInCompleteness(sessionIdentifier, historyId)) {
                return Mode.COMPLETE;
            } else if (events.get(eventKey).stream().parallel().anyMatch(r -> eventId < r.getEventId())) {
                return Mode.TRUE;
            } else {
                return Mode.FALSE;
            }
        } else {
            if (!isRegistered(sessionIdentifier, historyId)) {
                return Mode.BROKEN;
            }
            return Mode.FALSE;
        }
    }

    public synchronized void registerLastLogAPICall(String sessionIdentifier, Long historyId) {
        // unsubscribe
        unsubscribe(sessionIdentifier, historyId);

        String eventKey = getEventKey(sessionIdentifier, historyId);
        int oldEvents = 0;
        // cleanup
        CopyOnWriteArraySet<RunningTaskLog> e = events.get(eventKey);
        if (e != null) {
            oldEvents = e.size();
            events.remove(eventKey);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[registerLastLogAPICall][historyId=" + historyId + "]oldEvents removed=" + oldEvents);
        }
        // register
        lastLogAPICalls.put(eventKey, Instant.now().toEpochMilli());
    }

    public synchronized boolean isBeforeLastLogAPICall(String sessionIdentifier, Long historyId, Long eventId, Long started, String range) {
        String key = getEventKey(sessionIdentifier, historyId);
        Long l = lastLogAPICalls.get(key);
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        if (l == null) {
            if (isDebugEnabled) {
                try {
                    LOGGER.debug("[isBeforeLastLogAPICall][historyId=" + historyId + "][lastLogAPICall=null][eventId=" + eventId + "(" + SOSDate
                            .getDateTimeAsString(eventId) + " UTC)][" + range + "]false");
                } catch (SOSInvalidDataException e) {

                }
            }
            return false;
        } else {
            boolean r = l.longValue() > eventId.longValue();
            if (!r) {
                r = l.longValue() > started.longValue();
            }
            if (isDebugEnabled) {
                try {
                    LOGGER.debug("[isBeforeLastLogAPICall][historyId=" + historyId + "][lastLogAPICall=" + l + "(" + SOSDate.getDateTimeAsString(l)
                            + " UTC)][eventId=" + eventId + "(" + SOSDate.getDateTimeAsString(eventId) + " UTC)][started=" + started + "(" + SOSDate
                                    .getDateTimeAsString(started) + " UTC)][" + range + "]" + r);
                } catch (SOSInvalidDataException e) {

                }
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
                    LOGGER.debug("[getRunningTaskLog][historyId=" + r.getTaskId() + "]complete=" + r.getComplete());
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

    public boolean isRegistered(String sessionIdentifier, Long historyId) {
        String id = getSessionIdentifier(sessionIdentifier);
        return Optional.ofNullable(registeredHistoryIds.get(id)).map(l -> l.contains(historyId)).orElse(false);
    }

    public static boolean jocSessionExists(String sessionIdentifier, Long historyId) {
        try {
            SOSAuthCurrentAccountsList accounts = Globals.jocWebserviceDataContainer.getCurrentAccountsList();
            if (accounts == null) {
                return false;
            }
            if (accounts.getAccount(sessionIdentifier) == null) {
                return false;
            }
            return true;
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[jocSessionExists][historyId=" + historyId + "][exception]" + e);
            }
            return false;
        }
    }

    public static ConcurrentHashMap<String, WeakReference<Thread>> getRunningLogThreadsCache() {
        return runningLogThreadsCache;
    }

    public static String getThreadIdentifier(LogTaskContent content) {
        return content.getSessionIdentifier() + RUNNING_LOG_THREAD_IDENTIFIER_DELIMITER + content.getHistoryId();
    }

    public static String getHistoryIdFromThreadIdentifier(String threadIdentifier) {
        if (threadIdentifier == null) {
            return null;
        }
        String[] arr = threadIdentifier.split(RUNNING_LOG_THREAD_IDENTIFIER_DELIMITER);
        return arr.length > 1 ? arr[1] : null;
    }

    private synchronized void addEvent(String sessionIdentifier, RunningTaskLog event) {
        // LOGGER.debug("try to add log event for taskId '" + event.getTaskId() + "'" );
        String eventKey = getEventKey(sessionIdentifier, event.getTaskId());
        events.putIfAbsent(eventKey, new CopyOnWriteArraySet<RunningTaskLog>());
        if (events.get(eventKey).add(event)) {
            EventBus.getInstance().post(new HistoryOrderTaskLogArrived(event.getTaskId(), event.getComplete(), sessionIdentifier));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[addEvent][historyId=" + event.getTaskId() + "]event posted");
            }
        }
    }

    private synchronized void addCompleteness(String sessionIdentifier, Long historyId) {
        String id = getSessionIdentifier(sessionIdentifier);
        completeLogs.computeIfAbsent(id, k -> new HashSet<>()).add(historyId);
    }

    private boolean isInCompleteness(String sessionIdentifier, Long historyId) {
        String id = getSessionIdentifier(sessionIdentifier);
        return Optional.ofNullable(completeLogs.get(id)).map(l -> l.contains(historyId)).orElse(false);
    }

    private String getEventKey(String sessionIdentifier, Long historyId) {
        return historyId + EVENT_KEY_DELIMITER + getSessionIdentifier(sessionIdentifier);
    }

    private boolean cleanupCheckIfIsRegistered(String historyIdAndSessionIdentifier) {
        String[] arr = historyIdAndSessionIdentifier.split(EVENT_KEY_DELIMITER);
        try {
            String sessionIdentifier = arr[1];
            Long historyId = Long.valueOf(arr[0]);
            boolean registered = isRegistered(sessionIdentifier, historyId);
            if (registered) {
                registered = jocSessionExists(sessionIdentifier, historyId);
            }
            return registered;
        } catch (Throwable e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[cleanupCheckIfIsRegistered][" + historyIdAndSessionIdentifier + "][exception]" + e);
            }
            return false;
        }
    }

    private static void cleanupRunningLogThreadsCache(boolean isDebugEnabled) {
        Map<String, Integer> nullCounts = new HashMap<>();
        Map<String, Integer> aliveCounts = new HashMap<>();
        Map<String, Integer> deadCounts = new HashMap<>();

        runningLogThreadsCache.entrySet().removeIf(entry -> {
            String historyId = isDebugEnabled ? getHistoryIdFromThreadIdentifier(entry.getKey()) : null;
            Thread t = entry.getValue().get();
            if (t == null) {
                if (isDebugEnabled) {
                    nullCounts.merge(historyId, 1, Integer::sum);
                }
                return true;
            } else {
                try {
                    if (t.isAlive()) {
                        if (isDebugEnabled) {
                            aliveCounts.merge(historyId, 1, Integer::sum);
                        }
                    } else {
                        if (isDebugEnabled) {
                            deadCounts.merge(historyId, 1, Integer::sum);
                        }
                        return true;
                    }
                }
                // if t suddenly became null, simply remove it by returning true
                catch (NullPointerException e) {
                    nullCounts.merge(historyId, 1, Integer::sum);
                    return true;
                }
            }
            return false;
        });
        if (isDebugEnabled) {
            Set<String> allHistoryIds = new HashSet<>();
            allHistoryIds.addAll(nullCounts.keySet());
            allHistoryIds.addAll(aliveCounts.keySet());
            allHistoryIds.addAll(deadCounts.keySet());

            for (String historyId : allHistoryIds) {
                int nulls = nullCounts.getOrDefault(historyId, 0);
                int alive = aliveCounts.getOrDefault(historyId, 0);
                int dead = deadCounts.getOrDefault(historyId, 0);
                int removed = nulls + dead;
                LOGGER.debug("[RunningTaskLogs][cleanup][runningLogThreadsCache][historyId=" + historyId + "][removed(dead+null(GS))=" + removed
                        + "][details]alive=" + alive + ", dead=" + dead + ", null(GC)=" + nulls);
            }
        }
    }

    private String getSessionIdentifier(String identifier) {
        return identifier != null ? identifier : DEFAULT_SESSION_IDENTIFIER;
    }

    public Map<String, Set<Long>> getCompleteness() {
        return completeLogs;
    }

    public Map<String, CopyOnWriteArraySet<RunningTaskLog>> getEvents() {
        return events;
    }

}
