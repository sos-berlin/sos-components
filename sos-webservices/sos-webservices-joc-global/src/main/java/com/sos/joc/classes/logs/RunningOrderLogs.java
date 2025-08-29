package com.sos.joc.classes.logs;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.controller.model.event.EventType;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.history.HistoryOrderLog;
import com.sos.joc.event.bean.history.HistoryOrderLogArrived;
import com.sos.joc.model.history.order.OrderLogEntry;
import com.sos.joc.model.order.RunningOrderLogEvent;
import com.sos.joc.model.order.RunningOrderLogEvents;

public class RunningOrderLogs {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunningOrderLogs.class);

    protected static final EnumSet<EventType> completeTypes = EnumSet.of(EventType.OrderBroken, EventType.OrderCancelled, EventType.OrderFinished);
    private static final long CLEANUP_PERIOD = TimeUnit.MINUTES.toMillis(2);

    private static RunningOrderLogs runningOrderLogs;

    private volatile Set<Long> subscribedHistoryIds = new CopyOnWriteArraySet<>();
    private volatile ConcurrentMap<Long, CopyOnWriteArraySet<RunningOrderLogEvent>> events = new ConcurrentHashMap<>();
    private volatile Set<Long> completeLogs = new CopyOnWriteArraySet<>();

    public enum Mode {
        COMPLETE, TRUE, FALSE, BROKEN;
    }

    private RunningOrderLogs() {
        EventBus.getInstance().register(this);

        // isDaemon=true to ensure the JVM can exit immediately without waiting for this timer thread
        // thread name limited to 20 characters according to log4j settings
        new Timer("Timer-CleanRunOrder", true).scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                boolean isDebugEnabled = LOGGER.isDebugEnabled();
                if (isDebugEnabled) {
                    LOGGER.debug("[RunningOrderLogs][cleanup][before]subscribedHistoryIds=" + subscribedHistoryIds.size() + ", events=" + events
                            .size() + ", completeLogs=" + completeLogs.size());
                }
                Long eventId = Instant.now().toEpochMilli() - CLEANUP_PERIOD;
                Set<Long> toDelete = new HashSet<>();
                events.forEach((historyId, logs) -> {
                    if (!subscribedHistoryIds.contains(historyId)) {
                        toDelete.add(historyId);
                    } else {
                        logs.removeIf(e -> e.getEventId() < eventId);
                    }
                });
                toDelete.forEach(historyId -> events.remove(historyId));
                completeLogs.removeIf(historyId -> !subscribedHistoryIds.contains(historyId));

                if (isDebugEnabled) {
                    LOGGER.debug("[RunningOrderLogs][cleanup][after]subscribedHistoryIds=" + subscribedHistoryIds.size() + ", events=" + events.size()
                            + ", completeLogs=" + completeLogs.size());
                }
            }

        }, CLEANUP_PERIOD, CLEANUP_PERIOD);
    }

    public static synchronized RunningOrderLogs getInstance() {
        if (runningOrderLogs == null) {
            runningOrderLogs = new RunningOrderLogs();
        }
        return runningOrderLogs;
    }

    public synchronized void subscribe(Long historyId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[subscribe][historyId=" + historyId + "]subscribed");
        }
        subscribedHistoryIds.add(historyId);
    }

    public synchronized void unsubscribe(String sessionIdentifier, Long historyId) {
        boolean removed = subscribedHistoryIds.remove(historyId);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[unsubscribe][historyId=" + historyId + "]" + (removed ? "unsubscribed" : "skip, not subscribed"));
        }
        RunningTaskLogs.getInstance().unsubscribeFromOrder(sessionIdentifier, historyId);
    }

    public Mode hasEvents(Long eventId, Long historyId) {
        if (events.containsKey(historyId)) {
            if (completeLogs.contains(historyId)) {
                return Mode.COMPLETE;
            } else if (events.get(historyId).stream().parallel().anyMatch(r -> eventId < r.getEventId())) {
                return Mode.TRUE;
            } else {
                return Mode.FALSE;
            }
        } else {
            if (!subscribedHistoryIds.contains(historyId)) {
                return Mode.BROKEN;
            }
            return Mode.FALSE;
        }
    }

    public RunningOrderLogEvents getRunningOrderLog(RunningOrderLogEvents r) {
        SortedSet<Long> evtIds = new TreeSet<>(Comparator.comparing(Long::longValue));
        List<OrderLogEntry> logEvents = new ArrayList<>();
        r.setComplete(false);
        events.get(r.getHistoryId()).iterator().forEachRemaining(e -> {
            if (e.getEventId() != null && r.getEventId() < e.getEventId()) {
                if (e.getComplete()) {
                    r.setComplete(true);
                }
                logEvents.add(e.getLogEvent());
                evtIds.add(e.getEventId());
            }
        });
        if (!evtIds.isEmpty()) {
            r.setEventId(evtIds.last());
            r.setLogEvents(logEvents.stream().map(item -> LogOrderContent.getMappedLogItem(item)).collect(Collectors.toList()));
        }
        return r;
    }

    @Subscribe({ HistoryOrderLog.class })
    public void createHistoryOrderEvent(HistoryOrderLog evt) {
        if (isSubscribed(evt.getHistoryOrderId())) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("log event for historyId '" + evt.getHistoryOrderId() + "' arrived");
            }
            try {
                RunningOrderLogEvent r = new RunningOrderLogEvent();
                r.setHistoryId(evt.getHistoryOrderId());
                r.setEventId(evt.getEventId());
                r.setComplete(completeTypes.contains(EventType.fromValue(evt.getKey())));
                r.setLogEvent((OrderLogEntry) evt.getOrderLogEntry());
                addEvent(evt.getSessionIdentifier(), r);
                if (r.getComplete()) {
                    addCompleteness(r.getHistoryId());
                    unsubscribe(evt.getSessionIdentifier(), r.getHistoryId());
                }
            } catch (Exception e) {
                LOGGER.debug("error at log event for historyId '" + e.toString());
            }
        }
    }

    private boolean isSubscribed(Long historyId) {
        return subscribedHistoryIds.contains(historyId);
    }

    private synchronized void addEvent(String sessionIdentifier, RunningOrderLogEvent event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("try to add log event for historyId '" + event.getHistoryId() + "'");
        }
        events.putIfAbsent(event.getHistoryId(), new CopyOnWriteArraySet<RunningOrderLogEvent>());
        if (events.get(event.getHistoryId()).add(event)) {
            EventBus.getInstance().post(new HistoryOrderLogArrived(event.getHistoryId(), event.getComplete(), sessionIdentifier));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("log event for historyId '" + event.getHistoryId() + "' published");
            }
        }
    }

    private synchronized void addCompleteness(Long historyId) {
        completeLogs.add(historyId);
    }

}
