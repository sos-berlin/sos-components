package com.sos.joc.monitoring.model;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.deploy.DeployHistoryJobResourceEvent;
import com.sos.joc.event.bean.monitoring.MonitoringEvent;
import com.sos.joc.event.bean.monitoring.NotificationConfigurationReleased;
import com.sos.joc.event.bean.monitoring.NotificationConfigurationRemoved;
import com.sos.joc.event.bean.monitoring.NotificationLogEvent;
import com.sos.joc.monitoring.MonitorService;
import com.sos.joc.monitoring.SystemMonitorService;
import com.sos.joc.monitoring.bean.SystemMonitoringEvent;
import com.sos.joc.monitoring.bean.SystemMonitoringEvent.Category;
import com.sos.joc.monitoring.configuration.Configuration;
import com.sos.joc.monitoring.configuration.SystemNotification;

public class SystemMonitoringModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemMonitoringModel.class);

    /** seconds */
    private static final long SCHEDULE_DELAY = 5;
    private static final int THREAD_POOL_CORE_POOL_SIZE = 1;

    private static final Set<String> SKIPPED_NOTIFIERS = new HashSet<>(Arrays.asList(SystemNotification.class.getName(), SystemNotifierModel.class
            .getName(), SystemMonitoringModel.class.getName(), ProblemHelper.class.getName()));

    // ms
    private static final long MAX_ADDED_TIME = 2 * 60 * 1_000; // 2m

    private final SystemMonitorService service;
    private final SystemNotifierModel notifier;

    private CopyOnWriteArraySet<SystemMonitoringEvent> allEvents = new CopyOnWriteArraySet<>();
    private ConcurrentHashMap<String, SystemMonitoringEvent> lastSystemEvents = new ConcurrentHashMap<>();// ConcurrentHashMap??
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

    @Subscribe({ NotificationConfigurationReleased.class, NotificationConfigurationRemoved.class })
    public void handleMonitoringEvents(MonitoringEvent evt) {
        if (Configuration.INSTANCE.exists()) {
            MonitorService.setLogger();
            LOGGER.info(String.format("[%s][configuration]%s", service.getIdentifier(), evt.getClass().getSimpleName()));
            if (evt instanceof NotificationConfigurationRemoved) {
                Configuration.INSTANCE.clear();
            } else {
                Configuration.INSTANCE.load(service.getIdentifier(), service.getJocConfig().getTitle(), service.getJocConfig().getUri());
            }
        }
    }

    @Subscribe({ DeployHistoryJobResourceEvent.class })
    public void handleMonitoringEvents(DeployHistoryJobResourceEvent evt) {
        if (Configuration.INSTANCE.exists() && evt.getName() != null) {
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
            return JocCluster.getOKAnswer(JocClusterAnswerState.STARTED);
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
            JocCluster.shutdownThreadPool(mode, threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);
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

                ToNotify toNotify = handleEvents();
                if (toNotify != null) {
                    notifier.notify(toNotify.notification, toNotify.events);
                    logCache();
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
            lastSystemEvents.clear();
            return null;
        }
        if (allEvents.size() == 0) {
            return null;
        }
        MonitorService.setLogger();
        boolean isDebugEnabled = LOGGER.isDebugEnabled();

        long currentTime = new Date().getTime();
        lastSystemEvents.entrySet().removeIf(e -> (currentTime - e.getValue().getEpochMillis()) >= MAX_ADDED_TIME);

        List<SystemMonitoringEvent> copy = new ArrayList<>(allEvents);
        copy.sort(Comparator.comparing(SystemMonitoringEvent::getEpochMillis));
        Map<String, SystemMonitoringEvent> currentAdded = new LinkedHashMap<>();
        try {
            for (SystemMonitoringEvent evt : copy) {
                if (service.closed()) {
                    break;
                }
                try {
                    if (!doAdd(toNotify, evt, isDebugEnabled)) {
                        continue;
                    }

                    if (evt.getLoggerName().equals(HistoryNotifierModel.class.getName()) || evt.getLoggerName().equals(HistoryMonitoringModel.class
                            .getName())) {
                        evt.setCategory(Category.JOC);
                    }

                    if (evt.getCategory().equals(Category.JOC)) {
                        currentAdded.put(evt.getLoggerName() + "_" + evt.getType(), evt);
                    } else {
                        currentAdded.put(evt.getSection() + "_" + evt.getType(), evt);
                    }
                } catch (Throwable ee) {

                }
            }

            for (Map.Entry<String, SystemMonitoringEvent> e : currentAdded.entrySet()) {
                if (e.getValue().getCategory().equals(Category.JOC)) {
                    toNotify.add(e.getValue());
                } else {
                    if (!lastSystemEvents.containsKey(e.getKey())) {
                        lastSystemEvents.put(e.getKey(), e.getValue());

                        boolean skip = false;
                        if (e.getKey().equals(SystemMonitoringEvent.SECTION_DATABASE_WARNING)) {
                            if (currentAdded.containsKey(SystemMonitoringEvent.SECTION_DATABASE_ERROR)) {
                                skip = true;
                            }
                        }
                        if (!skip) {
                            toNotify.add(e.getValue());
                        }
                    }
                }
            }

        } finally {
            allEvents.removeAll(copy);
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

        return true;
    }

    private void logCache() {
        LOGGER.info(String.format("[cached]allEvents=%s,lastSystemEvents=%s", allEvents.size(), lastSystemEvents.size()));
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
