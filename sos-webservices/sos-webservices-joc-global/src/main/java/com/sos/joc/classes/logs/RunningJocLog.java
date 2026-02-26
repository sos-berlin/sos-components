package com.sos.joc.classes.logs;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.model.joc.LogEntry;
import com.sos.joc.model.joc.LogLevel;
import com.sos.joc.model.joc.RunningLogEvents;

public class RunningJocLog {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunningJocLog.class);
    private static final Marker NOT_NOTIFY_LOGGER = WebserviceConstants.NOT_NOTIFY_LOGGER;

    // Thread name limited to 20 characters according to log4j settings
    private static final String CLEANUP_TIMER_THREAD_NAME = "Timer-CleanRunJocLog";
    private static final long CLEANUP_PERIOD = TimeUnit.MINUTES.toMillis(2);
    
    private static final Set<String> SKIPPED_NOTIFIERS = new HashSet<>(Arrays.asList(WebserviceConstants.AUDIT_OBJECTS_LOGGER,
            WebserviceConstants.AUDIT_TRAIL_LOGGER, RunningJocLog.class.getName()));

    public static final String NO_CONTEXT_SOURCE_NAME = "main";
    
    private static RunningJocLog runningJocLog;

    private volatile CopyOnWriteArrayList<LogEvent> events = new CopyOnWriteArrayList<>();
    private volatile CopyOnWriteArraySet<IJocLog> listeners = new CopyOnWriteArraySet<>();

    private RunningJocLog() {

        // isDaemon = true so the JVM can shut down without being blocked by this timer thread.
        // Any task that is already running will finish, but no new tasks will be started once shutdown begins.
        new Timer(CLEANUP_TIMER_THREAD_NAME, true).scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                boolean isDebugEnabled = LOGGER.isDebugEnabled();
                if (isDebugEnabled) {
                    LOGGER.debug(NOT_NOTIFY_LOGGER, "[RunningJocLog][cleanup][before]events=" + events.size());
                }
                
                int size = events.size();
                if (size > 20) { // retain at least last 20 events
                    Long eventId = Instant.now().toEpochMilli() - CLEANUP_PERIOD;
                    events.subList(0, size - 20).removeIf(e -> e.getInstant().getEpochMillisecond() < eventId);
                    
                    if (isDebugEnabled) {
                        LOGGER.debug(NOT_NOTIFY_LOGGER, "[RunningOrderLogs][cleanup][after]events=" + events.size());
                    }
                }
            }

        }, CLEANUP_PERIOD, CLEANUP_PERIOD);
    }

    public static synchronized RunningJocLog getInstance() { //synchronized necessary?
        if (runningJocLog == null) {
            runningJocLog = new RunningJocLog();
        }
        return runningJocLog;
    }
    
    public void collectEvent(LogEvent evt) {
        if (SKIPPED_NOTIFIERS.contains(evt.getLoggerName())) {
            return;
        }
//        if (LOGGER.isDebugEnabled()) {
//            LOGGER.debug(NOT_NOTIFY_LOGGER, "[RunningOrderLogs][collectEvent]" + evt.toString());
//        }
        events.add(evt);
        listeners.forEach(l -> l.signalArrivedEvent());
    }
    
    public void register(IJocLog obj) {
        listeners.add(obj);
    }
    
    public void unRegister(IJocLog obj) {
        listeners.remove(obj);
    }
    
    private static LogEntry map(LogEvent evt) {
        LogEntry le = new LogEntry();
        le.setLogger(evt.getLoggerName());
        le.setLogLevel(LogLevel.fromValue(evt.getLevel().name().toUpperCase()));
        le.setMessage(evt.getMessage().getFormattedMessage());
        le.setSource(getSource(evt.getContextData()));
        le.setThread(evt.getThreadName());
        le.setThrown(Optional.ofNullable(evt.getThrown()).map(Throwable::toString).orElse(null));
        le.setTimestamp(Date.from(Instant.ofEpochMilli(evt.getInstant().getEpochMillisecond())));
        return le;
    }
    
    private static String getSource(ReadOnlyStringMap contextData) {
        String source = NO_CONTEXT_SOURCE_NAME;
        if (contextData.containsKey("clusterService")) {
            source = contextData.getValue("clusterService");
        } else if (contextData.containsKey("context")) {
            source = contextData.getValue("context");
        }
        return source;
    }

    public boolean hasEvents(Long eventId) {
        return events.parallelStream().anyMatch(r -> eventId < r.getInstant().getEpochMillisecond());
    }

    public RunningLogEvents getRunningLog(RunningLogEvents r) {
        SortedSet<Long> evtIds = new TreeSet<>(Comparator.comparing(Long::longValue));
        List<LogEntry> logEvents = new ArrayList<>();
        events.iterator().forEachRemaining(e -> {
            long eventId = e.getInstant().getEpochMillisecond();
            if (r.getEventId() < eventId) {
                logEvents.add(map(e));
                evtIds.add(eventId);
            }
        });
        if (!evtIds.isEmpty()) {
            r.setEventId(evtIds.last());
            r.setLogEvents(logEvents);
        }
        return r;
    }

}
