package com.sos.joc.classes.logs;

import java.lang.ref.WeakReference;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
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
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
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

    public static final Duration RUNNING_LOG_MAX_THREAD_LIFETIME = Duration.ofMinutes(10L);

    // Thread name limited to 20 characters according to log4j settings
    private static final String CLEANUP_TIMER_THREAD_NAME = "Timer-CleanRunTask";
    private final static long CLEANUP_PERIOD = TimeUnit.MINUTES.toMillis(2);
    private final static String DEFAULT_SESSION_IDENTIFIER = "common_session";
    private final static String EVENT_KEY_DELIMITER = ";";

    // 1 running log thread per <histroryId(taskId)>_<sessionIdentifier>
    // WeakReference - the object(Thread) can be garbage collected immediately if no strong reference to it exists
    private final static String RUNNING_LOG_THREAD_IDENTIFIER_DELIMITER = ";;;";
    private final static ConcurrentHashMap<String, WeakReference<Thread>> runningLogThreadsCache = new ConcurrentHashMap<>();

    private static RunningTaskLogs runningTaskLogs;

    // key=<sessionIdentifier>, value=Set<TaskLogBean(orderHistoryId,historyId)>
    private volatile Map<String, Set<TaskLogBean>> subscribedHistoryIds = new ConcurrentHashMap<>();
    // key=<histroryId(taskId)>_<sessionIdentifier>
    private volatile Map<String, CopyOnWriteArraySet<RunningTaskLog>> events = new ConcurrentHashMap<>();
    private volatile Map<String, Set<Long>> completeLogs = new ConcurrentHashMap<>();
    // key=<histroryId>_<sessionIdentifier>, value=eventId(current milliseconds)
    private volatile Map<String, Long> subscriptionStartTimes = new ConcurrentHashMap<>();

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

        // isDaemon = true so the JVM can shut down without being blocked by this timer thread.
        // Any task that is already running will finish, but no new tasks will be started once shutdown begins.
        new Timer(CLEANUP_TIMER_THREAD_NAME, true).scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                boolean isDebugEnabled = LOGGER.isDebugEnabled();
                if (isDebugEnabled) {
                    LOGGER.debug("[RunningTaskLogs][cleanup][before]subscribedHistoryIds=" + subscribedHistoryIds.size() + ", events=" + events.size()
                            + ", completeLogs=" + completeLogs.size() + ", subscriptionStartTimes=" + subscriptionStartTimes.size()
                            + ", runningLogThreadsCache=" + runningLogThreadsCache.size());
                }
                long now = Instant.now().toEpochMilli();
                Long eventId = now - CLEANUP_PERIOD;
                Set<String> toDelete = new HashSet<>();
                events.forEach((historyIdAndSessionIdentifier, logs) -> {
                    if (!cleanupCheckIfIsSubscribed(historyIdAndSessionIdentifier)) {
                        toDelete.add(historyIdAndSessionIdentifier);
                    } else {
                        logs.removeIf(e -> e.getEventId() < eventId);
                    }
                });
                toDelete.forEach(historyIdAndSessionIdentifier -> {
                    cleanupSubscribedHistoryIds(historyIdAndSessionIdentifier);
                    events.remove(historyIdAndSessionIdentifier);
                    subscriptionStartTimes.remove(historyIdAndSessionIdentifier);
                });
                subscriptionStartTimes.entrySet().removeIf(entry -> (now - entry.getValue()) > RUNNING_LOG_MAX_THREAD_LIFETIME.toMillis());

                // remove while iteration
                Iterator<Map.Entry<String, Set<Long>>> iter = completeLogs.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, Set<Long>> entry = iter.next();
                    entry.getValue().removeIf(historyId -> !isSubscribed(entry.getKey(), historyId));
                    if (entry.getValue().size() == 0) {
                        iter.remove();
                    }
                }

                cleanupRunningLogThreadsCache(isDebugEnabled);

                if (isDebugEnabled) {
                    LOGGER.debug("[RunningTaskLogs][cleanup][after]subscribedHistoryIds=" + subscribedHistoryIds.size() + ", events=" + events.size()
                            + ", completeLogs=" + completeLogs.size() + ", subscriptionStartTimes=" + subscriptionStartTimes.size()
                            + ", runningLogThreadsCache=" + runningLogThreadsCache.size());
                }
            }

        }, CLEANUP_PERIOD, CLEANUP_PERIOD);
    }

    @Subscribe({ HistoryOrderTaskLog.class })
    public void createHistoryTaskEvent(HistoryOrderTaskLog evt) {
        if (isSubscribed(evt.getSessionIdentifier(), evt.getHistoryOrderStepId())) {
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

    protected synchronized void subscribe(String sessionIdentifier, TaskLogBean bean) {
        String key = getSessionIdentifier(sessionIdentifier);
        subscribedHistoryIds.computeIfAbsent(key, k -> new HashSet<>()).add(bean);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[subscribe][" + bean + "]subscribed");
        }
    }

    public synchronized void unsubscribe(String sessionIdentifier, Long historyId) {
        String key = getSessionIdentifier(sessionIdentifier);
        boolean removed = cleanupSubscribedHistoryIds(key, historyId);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[unsubscribe][historyId=" + historyId + "]" + (removed ? "unsubscribed" : "skip, not subscribed"));
        }
    }

    protected synchronized void unsubscribeFromOrder(String sessionIdentifier, Long orderHistoryId) {
        String key = getSessionIdentifier(sessionIdentifier);
        Set<Long> removedHistoryIds = cleanupSubscribedHistoryIdsFromOrder(key, orderHistoryId);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[unsubscribeFromOrder][orderHistoryId=" + orderHistoryId + "][unsubscribed]historyIds=" + SOSString.join(
                    removedHistoryIds));
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
            if (!isSubscribed(sessionIdentifier, historyId)) {
                return Mode.BROKEN;
            }
            return Mode.FALSE;
        }
    }

    public synchronized void registerSubscriptionStartTime(String sessionIdentifier, Long historyId) {
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

        // register
        long now = Instant.now().toEpochMilli();
        subscriptionStartTimes.put(eventKey, Instant.now().toEpochMilli());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[registerSubscriptionStartTime][historyId=" + historyId + "][registered time=" + now + "]oldEvents removed=" + oldEvents);
        }
    }

    @SuppressWarnings("unused")
    private synchronized void unregisterSubscriptionStartTime(String sessionIdentifier, Long historyId) {
        String eventKey = getEventKey(sessionIdentifier, historyId);
        // unregister
        Long removed = subscriptionStartTimes.remove(eventKey);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[unregisterSubscriptionStartTime][historyId=" + historyId + "]" + (removed == null ? "skip, not registered"
                    : "[registered time=" + removed + "]unregistered"));
        }
    }

    public synchronized boolean isBeforeSubscriptionStartTime(String sessionIdentifier, Long historyId, Long eventId, Long now, String range) {
        String key = getEventKey(sessionIdentifier, historyId);
        Long startTime = subscriptionStartTimes.get(key);
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        if (startTime == null) {
            if (isDebugEnabled) {
                LOGGER.debug("[isBeforeSubscriptionStartTime][historyId=" + historyId + "][result=false][" + range
                        + "][subscriptionStartTime=null, eventId=" + eventId + ", now=" + now + "][resolved UTC]subscriptionStartTime=null, eventId="
                        + SOSDate.tryGetDateTimeAsString(eventId) + ", now=" + SOSDate.tryGetDateTimeAsString(now));
            }
            return false;
        } else {
            boolean before = startTime.longValue() > eventId.longValue();
            if (!before) {
                before = startTime.longValue() > now.longValue();
            }
            if (isDebugEnabled) {
                if (isDebugEnabled) {
                    LOGGER.debug("[isBeforeSubscriptionStartTime][historyId=" + historyId + "][result=" + before + "][" + range
                            + "][subscriptionStartTime=" + startTime + ", eventId=" + eventId + ", now=" + now
                            + "][resolved UTC]subscriptionStartTime=" + SOSDate.tryGetDateTimeAsString(startTime) + ", eventId=" + SOSDate
                                    .tryGetDateTimeAsString(eventId) + ", now=" + SOSDate.tryGetDateTimeAsString(now));
                }
            }
            return before;
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

    protected boolean isSubscribed(String sessionIdentifier, Long historyId) {
        String key = getSessionIdentifier(sessionIdentifier);
        return Optional.ofNullable(subscribedHistoryIds.get(key)).map(set -> set.stream().anyMatch(bean -> Objects.equals(bean.getHistoryId(),
                historyId))).orElse(false);
    }

    protected static boolean jocSessionExists(String sessionIdentifier, Long historyId) {
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

    private boolean cleanupCheckIfIsSubscribed(String historyIdAndSessionIdentifier) {
        String[] arr = historyIdAndSessionIdentifier.split(EVENT_KEY_DELIMITER);
        try {
            String sessionIdentifier = arr[1];
            Long historyId = Long.valueOf(arr[0]);
            boolean subscribed = isSubscribed(sessionIdentifier, historyId);
            if (subscribed) {
                subscribed = jocSessionExists(sessionIdentifier, historyId);
            }
            return subscribed;
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[cleanupCheckIfIsSubscribed][" + historyIdAndSessionIdentifier + "][exception]" + e);
            }
            return false;
        }
    }

    private void cleanupSubscribedHistoryIds(String historyIdAndSessionIdentifier) {
        try {
            String[] arr = historyIdAndSessionIdentifier.split(EVENT_KEY_DELIMITER);
            cleanupSubscribedHistoryIds(arr[1], Long.valueOf(arr[0]));
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[cleanupSubscribedHistoryIds][" + historyIdAndSessionIdentifier + "][exception]" + e);
            }
        }
    }

    private boolean cleanupSubscribedHistoryIds(String sessionIdentifier, Long historyId) {
        final boolean[] removed = { false };
        subscribedHistoryIds.compute(sessionIdentifier, (k, l) -> {
            if (l == null) {
                return null;
            }
            removed[0] = l.removeIf(bean -> Objects.equals(bean.getHistoryId(), historyId));
            return l.isEmpty() ? null : l;
        });
        return removed[0];
    }

    private Set<Long> cleanupSubscribedHistoryIdsFromOrder(String sessionIdentifier, Long orderHistoryId) {
        final Set<Long> removedHistoryIds = new HashSet<>();
        subscribedHistoryIds.compute(sessionIdentifier, (k, l) -> {
            if (l == null) {
                return null;
            }
            l.removeIf(bean -> {
                if (Objects.equals(bean.getOrderHistoryId(), orderHistoryId)) {
                    removedHistoryIds.add(bean.getHistoryId());
                    return true;
                }
                return false;
            });
            return l.isEmpty() ? null : l;
        });
        return removedHistoryIds;
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
