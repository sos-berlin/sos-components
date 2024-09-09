package com.sos.joc.monitoring.model;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.deploy.DeployHistoryJobResourceEvent;
import com.sos.joc.event.bean.monitoring.MonitoringEvent;
import com.sos.joc.event.bean.monitoring.NotificationConfigurationReleased;
import com.sos.joc.event.bean.monitoring.NotificationConfigurationRemoved;
import com.sos.joc.event.bean.monitoring.NotificationLogEvent;
import com.sos.joc.event.bean.monitoring.SystemNotificationLogEvent;
import com.sos.joc.model.cluster.common.state.JocClusterState;
import com.sos.joc.monitoring.MonitorService;
import com.sos.joc.monitoring.SystemMonitorService;
import com.sos.joc.monitoring.bean.SystemMonitoringEvent;
import com.sos.joc.monitoring.configuration.Configuration;
import com.sos.joc.monitoring.configuration.SystemNotification;

public class SystemMonitoringModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemMonitoringModel.class);

    /** seconds */
    private static final long SCHEDULE_DELAY = 5;
    private static final int THREAD_POOL_CORE_POOL_SIZE = 1;

    private static final Set<String> SKIPPED_NOTIFIERS = new HashSet<>(Arrays.asList(SystemNotification.class.getName(), SystemNotifierModel.class
            .getName(), SystemMonitoringModel.class.getName(), WebserviceConstants.AUDIT_LOGGER, "org.hibernate.engine.jdbc.spi.SqlExceptionHelper",
            "js7.common.system.ThreadPools"));

    private static final Set<String> SKIPPED_WARN_NOTIFIERS = new HashSet<>(Arrays.asList("js7.cluster.watch.ClusterWatchService"));
    private static final Map<String, Set<String>> SKIPPED_WARN_NOTIFIERS_BY_MESSAGE_START = Stream.of(new AbstractMap.SimpleEntry<>(
            "js7.proxy.ControllerApi", new HashSet<>(Arrays.asList("akka.stream.scaladsl.TcpIdleTimeoutException")))).collect(Collectors.toMap(
                    Map.Entry::getKey, Map.Entry::getValue));

    private static final Map<String, Set<String>> SKIPPED_ERROR_NOTIFIERS_BY_MESSAGE_START = Stream.of(new AbstractMap.SimpleEntry<>(
            "js7.proxy.JournaledProxy", new HashSet<>(Arrays.asList("UnknownEventId:")))).collect(Collectors.toMap(Map.Entry::getKey,
                    Map.Entry::getValue));

    // ms
    private static final long MAX_ADDED_TIME = 2 * 60 * 1_000; // 2m

    private final SystemMonitorService service;
    private final SystemNotifierModel notifier;

    private CopyOnWriteArraySet<SystemMonitoringEvent> allEvents = new CopyOnWriteArraySet<>();
    private ConcurrentHashMap<String, Long> lastNotifiedEvents = new ConcurrentHashMap<>();// ConcurrentHashMap??
    private ScheduledExecutorService threadPool;

    public SystemMonitoringModel(SystemMonitorService service) {
        this.service = service;
        this.notifier = new SystemNotifierModel(service.getThreadGroup());

        EventBus.getInstance().register(this);
    }

    @Subscribe({ NotificationLogEvent.class })
    public void handleNotificationLogEvent(NotificationLogEvent evt) {
        if (Configuration.INSTANCE.getSystemNotification() != null) {
            allEvents.add(new SystemMonitoringEvent(evt));
        }
    }

    @Subscribe({ SystemNotificationLogEvent.class })
    public void handleNotificationLogEvent(SystemNotificationLogEvent evt) {
        if (Configuration.INSTANCE.getSystemNotification() != null) {
            allEvents.add(new SystemMonitoringEvent(evt));
        }
    }

    @Subscribe({ NotificationConfigurationReleased.class, NotificationConfigurationRemoved.class })
    public void handleMonitoringEvents(MonitoringEvent evt) {
        MonitorService.setLogger();
        LOGGER.info(String.format("[%s][configuration]%s", service.getIdentifier(), evt.getClass().getSimpleName()));
        if (evt instanceof NotificationConfigurationRemoved) {
            Configuration.INSTANCE.clear();
        } else {
            Configuration.INSTANCE.load(service.getIdentifier(), service.getJocConfig().getTitle(), service.getJocConfig().getUri());
        }
    }

    @Subscribe({ DeployHistoryJobResourceEvent.class })
    public void handleMonitoringEvents(DeployHistoryJobResourceEvent evt) {
        if (evt.getName() != null && Configuration.INSTANCE.getMailResources() != null) {
            MonitorService.setLogger();
            List<String> names = Configuration.INSTANCE.getMailResources().entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
            if (names.contains(evt.getName())) {
                LOGGER.info(String.format("[%s][configuration]%s jr=%s", service.getIdentifier(), evt.getClass().getSimpleName(), evt.getName()));
                Configuration.INSTANCE.load(service.getIdentifier(), service.getJocConfig().getTitle(), service.getJocConfig().getUri());
            }
        }
    }

    public JocClusterAnswer start(StartupMode mode) {
        try {
            Configuration.INSTANCE.loadIfNotExists(service.getIdentifier(), service.getJocConfig().getTitle(), service.getJocConfig().getUri());
            schedule(service.getThreadGroup());
            return JocCluster.getOKAnswer(JocClusterState.STARTED);
        } catch (Exception e) {
            return JocCluster.getErrorAnswer(e);
        }
    }

    public void close(StartupMode mode) {
        if (notifier != null) {
            notifier.close(mode);
        }
        if (threadPool != null) {
            MonitorService.setLogger();
            JocCluster.shutdownThreadPool("[" + service.getIdentifier() + "][" + mode + "]", threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);
            threadPool = null;
        }
    }

    private void schedule(ThreadGroup threadGroup) {
        this.threadPool = Executors.newScheduledThreadPool(THREAD_POOL_CORE_POOL_SIZE, new JocClusterThreadFactory(threadGroup, service
                .getIdentifier() + "-sys"));
        this.threadPool.scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                MonitorService.setLogger();

                if (!service.closed()) {
                    ToNotify toNotify = handleEvents();
                    if (toNotify != null) {
                        // tmpLog(toNotify);
                        notifier.notify(toNotify.notification, toNotify.events);
                    }
                }
            }
        }, 0 /* start delay */, SCHEDULE_DELAY /* delay */, TimeUnit.SECONDS);

    }

    private ToNotify handleEvents() {
        ToNotify toNotify = new ToNotify();

        if (Configuration.INSTANCE.getSystemNotification() != null) {
            try {
                toNotify.notification = Configuration.INSTANCE.getSystemNotification().clone();
            } catch (Throwable e) {
            }
        }

        if (toNotify.notification == null) {
            allEvents.clear();
            lastNotifiedEvents.clear();
            return null;
        }
        if (allEvents.size() == 0) {
            return null;
        }
        MonitorService.setLogger();
        boolean isDebugEnabled = LOGGER.isDebugEnabled();

        List<SystemMonitoringEvent> copy = new ArrayList<>(allEvents);
        copy.sort(Comparator.comparing(SystemMonitoringEvent::getEpochMillis));
        Map<String, SystemMonitoringEvent> currentToNotify = new LinkedHashMap<>();
        try {
            for (SystemMonitoringEvent evt : copy) {
                if (service.closed()) {
                    break;
                }
                try {
                    if (!doAdd(toNotify, evt, isDebugEnabled)) {
                        continue;
                    }
                    evt.init();
                    currentToNotify.put(evt.getKey(), evt);
                } catch (Throwable ee) {

                }
            }

            for (Map.Entry<String, SystemMonitoringEvent> e : currentToNotify.entrySet()) {
                if (e.getValue().forceNotify()) {
                    if (!e.getValue().skip(currentToNotify)) {
                        toNotify.add(e.getValue());
                    }
                } else {
                    if (!lastNotifiedEvents.containsKey(e.getKey())) {
                        lastNotifiedEvents.put(e.getKey(), e.getValue().getEpochMillis());
                        if (!e.getValue().skip(currentToNotify)) {
                            toNotify.add(e.getValue());
                        }
                    }
                }
            }

        } finally {
            allEvents.removeAll(copy);

            long currentTime = new Date().getTime();
            lastNotifiedEvents.entrySet().removeIf(e -> (currentTime - e.getValue().longValue()) >= MAX_ADDED_TIME);
        }

        return toNotify;
    }

    private boolean doAdd(ToNotify toNotify, SystemMonitoringEvent evt, boolean isDebugEnabled) {
        if (!toNotify.notification.getTypes().contains(evt.getType())) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[handleEvents][skip][event type=%s not match to notification types=%s]%s", evt.getType(),
                        toNotify.notification.getTypesAsString(), evt.toString()));
            }
            return false;
        }
        if (SKIPPED_NOTIFIERS.contains(evt.getLoggerName())) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[handleEvents][skip][skip all events of %s]%s", evt.getLoggerName(), evt.toString()));
            }
            return false;
        }
        switch (evt.getType()) {
        case ERROR:
            if (SKIPPED_ERROR_NOTIFIERS_BY_MESSAGE_START.containsKey(evt.getLoggerName()) && !SOSString.isEmpty(evt.getMessage())) {
                long c = SKIPPED_ERROR_NOTIFIERS_BY_MESSAGE_START.get(evt.getLoggerName()).stream().filter(m -> evt.getMessage().trim().startsWith(m))
                        .count();
                if (c > 0) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[handleEvents][skip][skip this error of %s]%s", evt.getLoggerName(), evt.toString()));
                    }
                    return false;
                }
            }
            break;
        case WARNING:
            if (SKIPPED_WARN_NOTIFIERS.contains(evt.getLoggerName())) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[handleEvents][skip][skip all warnings of %s]%s", evt.getLoggerName(), evt.toString()));
                }
                return false;
            }
            if (SKIPPED_WARN_NOTIFIERS_BY_MESSAGE_START.containsKey(evt.getLoggerName()) && !SOSString.isEmpty(evt.getMessage())) {
                long c = SKIPPED_WARN_NOTIFIERS_BY_MESSAGE_START.get(evt.getLoggerName()).stream().filter(m -> evt.getMessage().trim().startsWith(m))
                        .count();
                if (c > 0) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[handleEvents][skip][skip this warning of %s]%s", evt.getLoggerName(), evt.toString()));
                    }
                    return false;
                }
            }
            break;
        default:
            break;
        }
        return true;
    }

    @SuppressWarnings("unused")
    private void tmpLog(ToNotify toNotify) {
        LOGGER.info(String.format("[tmpLog][toNotify]events=%s", toNotify.events.size()));
        toNotify.events.forEach(e -> {
            LOGGER.info("    " + e);
        });
    }

    private class ToNotify {

        private SystemNotification notification;
        private List<SystemMonitoringEvent> events;

        private ToNotify() {
            this.events = new ArrayList<>();
        }

        private void add(SystemMonitoringEvent evt) {
            this.events.add(evt);
        }
    }

}
