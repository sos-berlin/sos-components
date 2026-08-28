package com.sos.joc.monitoring.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSSerializer;
import com.sos.commons.util.SOSString;
import com.sos.history.JobWarning;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.bean.history.AHistoryBean;
import com.sos.joc.cluster.bean.history.HistoryOrderStepBean;
import com.sos.joc.cluster.common.JocClusterUtil;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.service.JocClusterServiceLogger;
import com.sos.joc.db.joc.DBItemJocVariable;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.history.HistoryEvent;
import com.sos.joc.event.bean.history.HistoryOrderEvent;
import com.sos.joc.event.bean.history.HistoryOrderTaskLogFirstStderr;
import com.sos.joc.event.bean.history.HistoryTaskEvent;
import com.sos.joc.monitoring.HistoryMonitorService;
import com.sos.joc.monitoring.MonitorService;
import com.sos.joc.monitoring.configuration.Configuration;
import com.sos.joc.monitoring.db.DBLayerMonitoring;
import com.sos.joc.monitoring.model.bean.AMonitorResult;
import com.sos.joc.monitoring.model.bean.ExpectedSeconds;
import com.sos.joc.monitoring.model.bean.MonitorOrderStepResult;
import com.sos.joc.monitoring.model.bean.MonitorOrderStepResultWarn;
import com.sos.joc.monitoring.model.bean.NotifierTask;
import com.sos.joc.monitoring.model.bean.ToNotify;

public class HistoryMonitoringModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryMonitoringModel.class);

    /** seconds */
    private static final long SCHEDULE_DELAY = 2;
    private static final int THREAD_POOL_CORE_POOL_SIZE = 1;
    /** 1day */
    private static final int MAX_LONGER_THAN_SECONDS = 24 * 60 * 60;
    /** 1day */
    private static final int MAX_PAYLOAD_SECONDS = 24 * 60 * 60;

    private static final int MAX_IN_PROCESS_IN_SECONDS = 60; // 1 minute

    private static int MAX_PAUSE_IN_SECONDS = -1;

    private final SOSHibernateFactory factory;
    private final JocConfiguration jocConfiguration;
    private final OrderNotifierModel notifier;
    private final List<String> controllerIds;

    private ScheduledExecutorService threadPool;
    private CopyOnWriteArraySet<AHistoryBean> payloads = new CopyOnWriteArraySet<>();
    // concurrent because - close(serialization) is called from another thread.
    private Map<Long, HistoryOrderStepBean> longerThan = new ConcurrentHashMap<>();

    private final Map<String, Long> eventIdByController = new ConcurrentHashMap<>();
    private AtomicLong lastActivityStart = new AtomicLong();
    private AtomicLong lastActivityEnd = new AtomicLong();

    private AtomicBoolean closed = new AtomicBoolean();
    private AtomicBoolean pause = new AtomicBoolean();
    private AtomicBoolean inProcess = new AtomicBoolean();

    // TODO ? commit after n db operations
    // private int maxTransactions = 100;

    public HistoryMonitoringModel(ThreadGroup threadGroup, SOSHibernateFactory factory, JocConfiguration jocConfiguration,
            List<ControllerConfiguration> controllers) {
        this.factory = factory;
        this.jocConfiguration = jocConfiguration;
        this.notifier = new OrderNotifierModel(threadGroup, factory.getConfigFile().get());
        this.controllerIds = new ArrayList<>();
        for (ControllerConfiguration c : controllers) {
            this.controllerIds.add(c.getCurrent().getId());
        }
        EventBus.getInstance().register(this);
    }

    @Subscribe({ HistoryOrderEvent.class, HistoryTaskEvent.class, HistoryOrderTaskLogFirstStderr.class })
    public void handleHistoryEvents(HistoryEvent evt) {
        // allow new events if closed
        MonitorService.setLogger();
        // LOGGER.info("[EV]" + SOSString.toString(evt));
        if (evt.getPayload() != null) {
            add2Payload((AHistoryBean) evt.getPayload());
        }
    }

    public void start(ThreadGroup threadGroup) {
        closed.set(false);

        restoreQueues();
        Configuration.INSTANCE.loadIfNotExists(MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, jocConfiguration.getTitle(), jocConfiguration.getUri());
        schedule(threadGroup);
    }

    public void close(StartupMode mode) {
        closed.set(true);

        if (notifier != null) {
            notifier.close(mode);
        }

        if (threadPool != null) {
            MonitorService.setLogger();
            JocCluster.shutdownThreadPool(HistoryMonitorService.LOG_IDENTIFIER + "[" + mode + "]", threadPool,
                    JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);
            threadPool = null;
            persistQueues();
            eventIdByController.clear();
        }
    }

    // from another thread
    public void startPause(String caller, int pauseDurationInSeconds) {
        if (!closed.get()) {
            MAX_PAUSE_IN_SECONDS = pauseDurationInSeconds + 10;
            pause.set(true);
            String msg = "[called from " + caller + "][startPause]maximum for " + pauseDurationInSeconds + "s...";

            // 1) write to e.g. cleanup log file
            LOGGER.info("[" + MonitorService.MAIN_SERVICE_IDENTIFIER + "][service]" + HistoryMonitorService.LOG_IDENTIFIER + msg);

            // 2) write to history log file
            JocClusterServiceLogger.setLogger(MonitorService.MAIN_SERVICE_IDENTIFIER);
            LOGGER.info("[" + MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY + "]" + msg);
            JocClusterServiceLogger.removeLogger(MonitorService.MAIN_SERVICE_IDENTIFIER);

            waitForNotInProcess();
        }
    }

    // from another thread
    public void stopPause(String caller) {
        if (pause.get()) {
            pause.set(false);
            String msg = "[called from " + caller + "][stopPause]...";

            // 1) write to e.g. cleanup log file
            LOGGER.info("[" + MonitorService.MAIN_SERVICE_IDENTIFIER + "][service]" + HistoryMonitorService.LOG_IDENTIFIER + msg);

            // 2) write to history log file
            JocClusterServiceLogger.setLogger(MonitorService.MAIN_SERVICE_IDENTIFIER);
            LOGGER.info("[" + MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY + "]" + msg);
            JocClusterServiceLogger.removeLogger(MonitorService.MAIN_SERVICE_IDENTIFIER);
        }
    }

    // from another thread
    private void waitForNotInProcess() {
        int counter = 0;
        x: while (inProcess.get() && !closed.get()) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break x;
            }
            counter++;
            if (counter >= MAX_IN_PROCESS_IN_SECONDS) {
                inProcess.set(false);
                JocClusterServiceLogger.setLogger(MonitorService.MAIN_SERVICE_IDENTIFIER);
                LOGGER.info(HistoryMonitorService.LOG_IDENTIFIER + "[waitForNotInProcess][stopped]MAX_IN_PROCESS_IN_SECONDS="
                        + MAX_IN_PROCESS_IN_SECONDS + " reached");
                JocClusterServiceLogger.removeLogger(MonitorService.MAIN_SERVICE_IDENTIFIER);
            }
        }
    }

    private void schedule(ThreadGroup threadGroup) {

        HistoryMonitoringModel model = this;
        this.threadPool = Executors.newScheduledThreadPool(THREAD_POOL_CORE_POOL_SIZE, new JocClusterThreadFactory(threadGroup,
                MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY + "-sh"));
        this.threadPool.scheduleWithFixedDelay(new Runnable() {

            private final AtomicLong lastStartMillis = new AtomicLong();
            private final AtomicLong pauseCounter = new AtomicLong();

            @Override
            public void run() {
                long currentStartMillis = System.currentTimeMillis();
                long previousStartMillis = lastStartMillis.get();
                long elapsedSinceLastStartMillis = previousStartMillis == 0 ? 0 : currentStartMillis - previousStartMillis;
                lastStartMillis.set(currentStartMillis);
                try {
                    MonitorService.setLogger();
                    boolean isDebugEnabled = LOGGER.isDebugEnabled();

                    if (!closed.get()) {
                        if (pause.get()) {
                            pauseCounter.set(pauseCounter.get() + 1);
                            if (MAX_PAUSE_IN_SECONDS > 0 && pauseCounter.get() >= MAX_PAUSE_IN_SECONDS) {
                                pause.set(false);
                                LOGGER.info(HistoryMonitorService.LOG_IDENTIFIER + "[cause][stopped]MAX_PAUSE_IN_SECONDS=" + MAX_PAUSE_IN_SECONDS
                                        + " reached");
                            }
                        } else {
                            pauseCounter.set(0L);
                            inProcess.set(true);

                            Instant start = Instant.now();
                            HistoryMonitoringPayloadHandler ph = new HistoryMonitoringPayloadHandler();
                            ToNotify toNotifyPayloads = ph.handlePayloads(model, isDebugEnabled);
                            if (closed.get()) {
                                if (toNotifyPayloads.getFirstEventId() != null) {
                                    LOGGER.info(String.format("%s[%s-%s][UTC][%s-%s][%s][on close][size]payloads=%s, longerThan=%s",
                                            HistoryMonitorService.LOG_IDENTIFIER, toNotifyPayloads.getFirstEventId(), toNotifyPayloads
                                                    .getLastEventId(), eventIdAsTime(toNotifyPayloads.getFirstEventId()), eventIdAsTime(
                                                            toNotifyPayloads.getLastEventId()), SOSDate.getDuration(Duration.between(start, Instant
                                                                    .now())), payloads.size(), longerThan.size()));
                                }
                            } else {
                                // checks for warnings in all registered longerThan
                                // - not contains the longerThan warnings evaluated by handlePayloads(), since they have already been removed before
                                // handleLongerThan
                                ToNotify toNotifyLongerThanNotPayloadWarnings = handleLongerThan(toNotifyPayloads, elapsedSinceLastStartMillis);

                                if (toNotifyPayloads.getFirstEventId() != null) {
                                    LOGGER.info(String.format("%s[%s-%s][UTC][%s-%s][%s][size for next iteration]payloads=%s, longerThan=%s",
                                            HistoryMonitorService.LOG_IDENTIFIER, toNotifyPayloads.getFirstEventId(), toNotifyPayloads
                                                    .getLastEventId(), eventIdAsTime(toNotifyPayloads.getFirstEventId()), eventIdAsTime(
                                                            toNotifyPayloads.getLastEventId()), SOSDate.getDuration(Duration.between(start, Instant
                                                                    .now())), payloads.size(), longerThan.size()));
                                }
                                notifier.notify(toNotifyPayloads, toNotifyLongerThanNotPayloadWarnings);
                            }
                        }
                    }
                } catch (Exception e) {
                    MonitorService.setLogger();
                    LOGGER.warn(e.toString(), e);
                } finally {
                    inProcess.set(false);
                }
            }
        }, 0 /* start delay */, SCHEDULE_DELAY /* delay */, TimeUnit.SECONDS);

    }

    // For each controller:
    // - If toNotifyPayloads provides a last event ID, use it as the last known controller event ID
    // - If no last event ID is provided (no new event in this iteration):
    // -- calculate the controller event ID based on the last known event ID and the duration since the previous iteration.
    private void calculateControllerEventIdsForLongerThan(ToNotify toNotifyPayloads, long elapsedSinceLastStartMillis) {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        for (String controllerId : controllerIds) {
            Long payloadLastEventId = toNotifyPayloads.getLastEventIdByController().get(controllerId);
            if (payloadLastEventId != null && payloadLastEventId > 0) {
                // use last controller event
                eventIdByController.put(controllerId, payloadLastEventId);
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[%s][handleLongerThan][calculateControllerEventIds][eventId=%s][UTC][%s]last payload event ID",
                            HistoryMonitorService.LOG_IDENTIFIER, controllerId, payloadLastEventId, SOSDate.tryGetDateTimeAsString(eventId2Instant(
                                    payloadLastEventId))));
                }
                continue;
            }

            Long lastEventId = eventIdByController.get(controllerId);
            if (lastEventId == null || lastEventId == 0) {
                // case - no events have been received from this controller yet - eventId cannot be calculated
                if (isDebugEnabled) {
                    if (lastEventId == null) {
                        LOGGER.debug(String.format(
                                "%s[%s][handleLongerThan][calculateControllerEventIds][eventId=0]unknown - no events have been received from this controller yet",
                                HistoryMonitorService.LOG_IDENTIFIER, controllerId));
                    }
                }
                eventIdByController.put(controllerId, 0L);
            } else {
                // last known event ID + the duration since the previous iteration
                eventIdByController.put(controllerId, lastEventId + (elapsedSinceLastStartMillis * 1_000));
                if (isDebugEnabled) {
                    Long eventId = eventIdByController.get(controllerId);
                    LOGGER.debug(String.format(
                            "%s[%s][handleLongerThan][calculateControllerEventIds][eventId=%s][UTC][%s]last known event ID(%s %s) + the duration since the previous iteration(%sms)",
                            HistoryMonitorService.LOG_IDENTIFIER, controllerId, eventId, SOSDate.tryGetDateTimeAsString(eventId2Instant(eventId)),
                            lastEventId, SOSDate.tryGetDateTimeAsString(eventId2Instant(lastEventId)), elapsedSinceLastStartMillis));
                }
            }
        }
    }

    private ToNotify handleLongerThan(ToNotify toNotifyPayloads, long elapsedSinceLastStartMillis) {
        ToNotify toNotify = new ToNotify();
        if (longerThan.size() == 0) {
            return toNotify;
        }

        calculateControllerEventIdsForLongerThan(toNotifyPayloads, elapsedSinceLastStartMillis);

        if (SOSCollection.isEmpty(eventIdByController)) {
            return toNotify;
        }

        MonitorService.setLogger();
        DBLayerMonitoring dbLayer = new DBLayerMonitoring(MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY);
        try {
            setLastActivityStart();

            Map<Long, MonitorOrderStepResult> w = new HashMap<>();
            Set<HistoryOrderStepBean> toRemove = new HashSet<>();
            boolean isDebugEnabled = LOGGER.isDebugEnabled();

            Map<String, Date> estimatedComparisonTimeCacheByController = new HashMap<>();
            longerThan.entrySet().stream().takeWhile(c -> !closed.get()).forEach(entry -> {
                HistoryOrderStepBean hosb = entry.getValue();
                boolean comparisonTimeEstimated = hosb.getEndTime() == null;
                Date comparisonTime = hosb.getEndTime();
                if (comparisonTimeEstimated) {
                    comparisonTime = estimatedComparisonTimeCacheByController.get(hosb.getControllerId());
                    if (comparisonTime == null) {
                        Long eventId = eventIdByController.get(hosb.getControllerId());
                        if (eventId != null && eventId > 0) {
                            comparisonTime = getEventIdAsDate(eventId);
                            estimatedComparisonTimeCacheByController.put(hosb.getControllerId(), comparisonTime);
                        }
                    }
                }

                if (comparisonTime == null) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format(
                                "%s[%s][handleLongerThan][longerThan historyId=%s][skip][comparison time cannot be estimated][UTC][startTime=%s, endTime=null]orderId=%s, workflow=%s, job=%s",
                                HistoryMonitorService.LOG_IDENTIFIER, hosb.getControllerId(), hosb.getHistoryId(), SOSDate.tryGetDateTimeAsString(hosb
                                        .getStartTime()), hosb.getOrderId(), hosb.getWorkflowPath(), hosb.getJobName()));
                    }
                } else {
                    MonitorOrderStepResultWarn warn = analyzeLongerThan(dbLayer, hosb, comparisonTime, false, comparisonTimeEstimated);
                    if (warn != null) {
                        if (warn.isInvalid()) {
                            toRemove.add(hosb);
                        } else {
                            MonitorOrderStepResult r = new MonitorOrderStepResult(hosb, warn);
                            w.put(entry.getKey(), r);

                            if (isDebugEnabled) {
                                try {
                                    String comparisonTimeLabel = "endTime";
                                    if (comparisonTimeEstimated) {
                                        comparisonTimeLabel = "estimated comparisonTime";
                                    }

                                    LOGGER.debug(String.format(
                                            "%s[%s][handleLongerThan][longerThan historyId=%s][UTC][startTime=%s, %s=%s][%s]orderId=%s, workflow=%s, job=%s",
                                            HistoryMonitorService.LOG_IDENTIFIER, hosb.getControllerId(), hosb.getHistoryId(), SOSDate
                                                    .tryGetDateTimeAsString(hosb.getStartTime()), comparisonTimeLabel, SOSDate.tryGetDateTimeAsString(
                                                            comparisonTime), warn.getText(), hosb.getOrderId(), hosb.getWorkflowPath(), hosb
                                                                    .getJobName()));
                                } catch (Exception e) {
                                    LOGGER.warn(e.toString(), e);
                                }
                            }
                        }
                    }
                }
            });
            for (HistoryOrderStepBean hosb : toRemove) {
                removeLongerThan("handleLongerThan", hosb);
            }

            if (w.size() == 0) {
                return toNotify;
            }

            for (Map.Entry<Long, MonitorOrderStepResult> entry : w.entrySet()) {
                if (closed.get()) {
                    break;
                }
                removeLongerThan("handleLongerThan", entry.getValue().getStep());
                toNotify.getSteps().add(entry.getValue());
            }

            boolean eventIdByControllerCleaned = false;
            if (longerThan.size() == 0) {
                eventIdByController.clear();
                eventIdByControllerCleaned = true;
            }

            if (isDebugEnabled) {
                LOGGER.debug(String.format(
                        "%s[handleLongerThan][processed=%s][toNotify steps=%s][eventIdByControllerCleaned=%s]longerThan size for next iteration=%s",
                        HistoryMonitorService.LOG_IDENTIFIER, w.size(), toNotify.getSteps().size(), eventIdByControllerCleaned, longerThan.size()));
            }
        } catch (Exception ex) {
            // dbLayer.rollback();
            LOGGER.warn(ex.toString(), ex);
        } finally {
            dbLayer.close();
            setLastActivityEnd();
        }
        return toNotify;
    }

    protected void putLongerThan(String caller, HistoryOrderStepBean hosb) {
        longerThan.put(hosb.getHistoryId(), hosb);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("%s[%s][longerThan historyId=%s][caller=%s][put]job=%s", HistoryMonitorService.LOG_IDENTIFIER, hosb
                    .getControllerId(), hosb.getHistoryId(), caller, SOSString.toString(hosb, true)));
        }
    }

    protected void removeLongerThan(String caller, HistoryOrderStepBean hosb) {
        HistoryOrderStepBean r = longerThan.remove(hosb.getHistoryId());
        if (LOGGER.isDebugEnabled()) {
            String removed = r == null ? "not exists" : "removed";
            LOGGER.debug(String.format("%s[%s][longerThan historyId=%s][caller=%s][%s]job=%s", HistoryMonitorService.LOG_IDENTIFIER, hosb
                    .getControllerId(), hosb.getHistoryId(), caller, removed, hosb.getJobName()));
        }
    }

    protected boolean longerThanExists(String caller, HistoryOrderStepBean hosb) {
        boolean exists = longerThan.containsKey(hosb.getHistoryId());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("%s[%s][longerThan historyId=%s][caller=%s][exists=%s]job=%s", HistoryMonitorService.LOG_IDENTIFIER, hosb
                    .getControllerId(), hosb.getHistoryId(), caller, exists, hosb.getJobName()));
        }
        return exists;
    }

    protected MonitorOrderStepResultWarn analyzeLongerThan(DBLayerMonitoring dbLayer, HistoryOrderStepBean hosb, Date comparisonTime,
            boolean isHandlePayload, boolean comparisonTimeEstimated) {

        MonitorOrderStepResultWarn invalidWarn = new MonitorOrderStepResultWarn(true);
        try {
            if (isHandlePayload) { // orderStepProcessed
                if (!longerThanExists("analyzeLongerThan", hosb)) {
                    return null;
                }
                removeLongerThan("analyzeLongerThan", hosb);
            }

            ExpectedSeconds expected = getExpectedSeconds(dbLayer, JobWarning.LONGER_THAN, hosb, hosb.getWarnIfLonger());
            if (expected == null || expected.getSeconds() == null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("%s[%s][analyzeLongerThan][longerThan historyId=%s][skip][expected=%s][isHandlePayload=%s]%s",
                            HistoryMonitorService.LOG_IDENTIFIER, hosb.getControllerId(), hosb.getHistoryId(), SOSString.toString(expected),
                            isHandlePayload, SOSString.toString(hosb)));

                }
                return invalidWarn;
            }

            long elapsedSeconds = MonitorOrderStepResultWarn.calculateElapsedSeconds(hosb.getStartTime(), comparisonTime);
            if (elapsedSeconds < 0) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format(
                            "%s[%s][analyzeLongerThan][longerThan historyId=%s][elapsedSeconds=%s < 0][startTime=%s, comparisonTime=%s][isHandlePayload=%s]%s",
                            HistoryMonitorService.LOG_IDENTIFIER, hosb.getControllerId(), hosb.getHistoryId(), elapsedSeconds, SOSDate
                                    .tryGetDateTimeAsString(hosb.getStartTime()), SOSDate.tryGetDateTimeAsString(comparisonTime), isHandlePayload,
                            SOSString.toString(hosb)));

                }
                return invalidWarn;
            }

            if (elapsedSeconds > expected.getSeconds()) {
                if (LOGGER.isDebugEnabled()) {
                    String comparisonTimeLabel = "endTime";
                    if (comparisonTimeEstimated) {
                        comparisonTimeLabel = "estimated comparisonTime";
                        if (isHandlePayload) {
                            comparisonTimeLabel = comparisonTimeLabel.trim() + "(orderStepProcessed endTime missing) ";
                        }
                    }

                    LOGGER.debug(String.format(
                            "%s[%s][analyzeLongerThan][longerThan historyId=%s][match][elapsedSeconds=%s > %s][startTime=%s, %s=%s][isHandlePayload=%s]%s",
                            HistoryMonitorService.LOG_IDENTIFIER, hosb.getControllerId(), hosb.getHistoryId(), elapsedSeconds, expected.getSeconds(),
                            SOSDate.tryGetDateTimeAsString(hosb.getStartTime()), comparisonTimeLabel, SOSDate.tryGetDateTimeAsString(comparisonTime),
                            isHandlePayload, SOSString.toString(hosb)));

                }
                // isHandlePayload is based on the actual start/end time (orderStepProcessed).
                // Set NO_EXPECTED_SECONDS to avoid a duplicate "longer than expected" check, as this is already covered by the start/end time calculation.
                long expectedSeconds = MonitorOrderStepResultWarn.NO_EXPECTED_SECONDS;
                if (comparisonTimeEstimated) {
                    expectedSeconds = expected.getSeconds();
                }
                return new MonitorOrderStepResultWarn(JobWarning.LONGER_THAN, expectedSeconds, String.format("Job runs longer than the expected %s",
                        getExpectedDurationMessage(hosb.getWarnIfLonger(), expected)));
            } else {
                if (!isHandlePayload) {// remove old entries
                    if (elapsedSeconds > MAX_LONGER_THAN_SECONDS) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug(String.format(
                                    "%s[%s][analyzeLongerThan][longerThan historyId=%s][skip][too old][elapsedSeconds=%s > %s][isHandlePayload=%s]%s",
                                    HistoryMonitorService.LOG_IDENTIFIER, hosb.getControllerId(), hosb.getHistoryId(), elapsedSeconds,
                                    MAX_LONGER_THAN_SECONDS, isHandlePayload, SOSString.toString(hosb)));

                        }
                        return invalidWarn;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn(String.format("%s[%s][analyzeLongerThan][skip onError][workflow=%s, orderId=%s, job=%s(historyid=%s)][isHandlePayload=%s]%s",
                    HistoryMonitorService.LOG_IDENTIFIER, hosb.getControllerId(), hosb.getWorkflowPath(), hosb.getOrderId(), hosb.getJobName(), hosb
                            .getHistoryId(), isHandlePayload, e.toString()), e);
            return invalidWarn;
        }
        return null;
    }

    protected String getExpectedDurationMessage(String definition, ExpectedSeconds expected) {
        if (isPercentage(definition)) {
            String avg = expected.getAvg() == null ? "" : SOSDate.getDurationOfSeconds(expected.getAvg());
            return String.format("duration of %s (avg=%s, configured=%s)", SOSDate.getDurationOfSeconds(expected.getSeconds()), avg, definition);
        } else if (isTime(definition)) {
            return String.format("duration of %s (configured=%s)", SOSDate.getDurationOfSeconds(expected.getSeconds()), definition);
        }
        return String.format("duration of %s", SOSDate.getDurationOfSeconds(expected.getSeconds()));
    }

    protected ExpectedSeconds getExpectedSeconds(DBLayerMonitoring dbLayer, JobWarning warnReason, HistoryOrderStepBean hosb, String definition) {
        if (SOSString.isEmpty(definition)) {
            return null;
        }
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        Long seconds = 0L;
        Long avg = 0L;
        if (isPercentage(definition)) {
            try {
                int percentage = Integer.parseInt(definition.substring(0, definition.length() - 1));
                if (percentage != 0) {
                    // get from cache
                    HistoryOrderStepBean hosbLt = longerThan.get(hosb.getHistoryId());
                    avg = hosbLt == null ? null : hosbLt.getWarnIfLongerAvgSeconds();

                    if (avg == null) {
                        // get from database
                        if (dbLayer.getSession() == null) {
                            dbLayer.setSession(factory.openStatelessSession(dbLayer.getIdentifier()));
                        }
                        avg = dbLayer.getJobAvg(hosb.getControllerId(), hosb.getWorkflowPath(), hosb.getJobName());
                    }
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[%s][getExpectedSeconds][%s definition=%s, avg=%s]workflowPath=%s, job=%s(historyId=%s)",
                                HistoryMonitorService.LOG_IDENTIFIER, hosb.getControllerId(), warnReason, definition, avg, hosb.getWorkflowPath(),
                                hosb.getJobName(), hosb.getHistoryId()));
                    }
                    if (avg == null || avg.equals(0L)) {
                        avg = 0L;
                        seconds = 0L;
                    } else {// successfully job runs found
                        Double r = Double.valueOf(percentage) / 100 * Double.valueOf(avg);
                        seconds = new BigDecimal(r).setScale(0, RoundingMode.HALF_UP).longValue();
                    }

                    if (hosbLt != null && hosbLt.getWarnIfLongerAvgSeconds() == null) {
                        hosbLt.setWarnIfLongerAvgSeconds(avg);
                        // set cache
                        putLongerThan("getExpectedSeconds", hosbLt);
                    }
                }
            } catch (SOSHibernateException e) {
                LOGGER.warn(String.format(
                        "%s[%s][getExpectedSeconds][%s definition=%s][error on get jobAvg][workflowPath=%s, job=%s(historyId=%s)]%s",
                        HistoryMonitorService.LOG_IDENTIFIER, hosb.getControllerId(), warnReason, definition, hosb.getWorkflowPath(), hosb
                                .getJobName(), hosb.getHistoryId(), e.toString()), e);
            }
        } else if (isSeconds(definition)) {
            seconds = Long.parseLong(definition.substring(0, definition.length() - 1));
        } else if (isTime(definition)) {
            seconds = SOSDate.getTimeAsSeconds(definition);
        } else {
            seconds = Long.parseLong(definition);
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[%s][getExpectedSeconds][%s definition=%s, seconds=%s]workflowPath=%s, job=%s(historyId=%s)",
                    HistoryMonitorService.LOG_IDENTIFIER, hosb.getControllerId(), warnReason, definition, seconds, hosb.getWorkflowPath(), hosb
                            .getJobName(), hosb.getHistoryId()));
        }
        return new ExpectedSeconds(seconds, avg);
    }

    // TODO duplicate HistoryUtil.eventIdAsTime
    protected String eventIdAsTime(Long eventId) {
        return eventId.equals(Long.valueOf(0)) ? "0" : SOSDate.getTimeAsString(eventId2Instant(eventId));
    }

    protected void setLastActivityStart() {
        lastActivityStart.set(Instant.now().toEpochMilli());
    }

    protected void setLastActivityEnd() {
        lastActivityEnd.set(Instant.now().toEpochMilli());
    }

    protected CopyOnWriteArraySet<AHistoryBean> getPayloads() {
        return payloads;
    }

    protected SOSHibernateFactory getFactory() {
        return factory;
    }

    protected AtomicBoolean getClosed() {
        return closed;
    }

    private void add2Payload(AHistoryBean bean) {
        if (bean == null || bean.getEventId() == null) {
            return;
        }
        Instant eventDate = JocClusterUtil.eventId2Instant(bean.getEventId());
        if (eventDate == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("%s[missing eventDate]%s", HistoryMonitorService.LOG_IDENTIFIER, SOSString.toString(bean)));
            }
        } else {
            Instant now = Instant.now();
            if (isNotExpired(now, eventDate, MAX_PAYLOAD_SECONDS)) {
                payloads.add(bean);
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("%s[%s][skip][now=%s-eventDate=%s > MAX_PAYLOAD_SECONDS=%s]%s", HistoryMonitorService.LOG_IDENTIFIER,
                            bean.getControllerId(), SOSDate.tryGetDateTimeAsString(Date.from(now)), SOSDate.tryGetDateTimeAsString(Date.from(
                                    eventDate)), MAX_PAYLOAD_SECONDS, SOSString.toString(bean)));
                }
            }
        }
    }

    private boolean isNotExpired(Instant now, Instant event, int maxTime) {
        return (now.getEpochSecond() - event.getEpochSecond()) <= maxTime;
    }

    private boolean isPercentage(String definition) {
        return definition.endsWith("%");
    }

    private boolean isSeconds(String definition) {
        return definition.toLowerCase().endsWith("s");
    }

    private boolean isTime(String definition) {
        return definition.contains(":");
    }

    private void persistQueues() {
        if (payloads.size() > 0 || longerThan.size() > 0 || notifier.getCandidatesSize() > 0 || notifier.getActiveSize() > 0) {
            try {
                CopyOnWriteArraySet<AHistoryBean> payloadsSnapshot = new CopyOnWriteArraySet<>(payloads);
                Map<Long, HistoryOrderStepBean> longerThanSnapshot = new HashMap<>(longerThan);
                List<AMonitorResult> notifierCandidatesSnapshot = notifier.getCandidatesSnapshot();
                Set<NotifierTask> notifierActiveSnapshot = notifier.getActiveSnapshot();

                saveJocVariable(new SOSSerializer<SerializedHistoryResult>().serializeCompressed2bytes(new SerializedHistoryResult(payloadsSnapshot,
                        longerThanSnapshot, notifierCandidatesSnapshot, notifierActiveSnapshot)));
                LOGGER.info(String.format("%s[persisted][history payloads=%s, longerThan=%s]notification candidates=%s, active=%s",
                        HistoryMonitorService.LOG_IDENTIFIER, payloadsSnapshot.size(), longerThanSnapshot.size(), notifierCandidatesSnapshot.size(),
                        notifierActiveSnapshot.size()));
            } catch (Exception e) {
                LOGGER.warn(String.format("%s[persistQueues]%s", HistoryMonitorService.LOG_IDENTIFIER, e.toString()), e);
            }
            payloads.clear();
            longerThan.clear();
            notifier.clear();
        } else {
            LOGGER.info(String.format("%s[persist][skip]no history/notification data found to persist", HistoryMonitorService.LOG_IDENTIFIER));
            deleteJocVariable();
        }
    }

    private void restoreQueues() {
        DBItemJocVariable var = null;
        try {
            var = getJocVariable();
            if (var == null) {
                LOGGER.info(String.format("%s[restore][skip]no persisted history/notification data found", HistoryMonitorService.LOG_IDENTIFIER));
                return;
            }
            restoreQueues(var);
            deleteJocVariable();
        } catch (Exception e) {
            LOGGER.warn(String.format("%s[restoreQueues]%s", HistoryMonitorService.LOG_IDENTIFIER, e.toString()), e);
        }
    }

    private void restoreQueues(DBItemJocVariable var) throws Exception {
        int payloadsSize = 0;
        int longerThanSize = 0;
        int notifierCandidatesSize = 0;
        int notifierActiveSize = 0;

        SerializedHistoryResult sr = new SOSSerializer<SerializedHistoryResult>().deserializeCompressed(var.getBinaryValue());
        if (sr.getPayloads() != null) {
            payloadsSize = sr.getPayloads().size();
            // payloads on start is maybe not empty (because event subscription)
            payloads.addAll(sr.getPayloads());
        }
        if (sr.getLongerThan() != null) {
            longerThanSize = sr.getLongerThan().size();
            // longerThan on start is empty ... ?
            longerThan.putAll(sr.getLongerThan());
        }
        if (sr.getNotifierCandidates() != null) {
            notifierCandidatesSize = sr.getNotifierCandidates().size();
            notifier.setCandidates(sr.getNotifierCandidates());
        }
        if (sr.getNotifierActive() != null) {
            notifierActiveSize = sr.getNotifierActive().size();
            notifier.runRestoredActiveNotifiers(sr.getNotifierActive());
        }
        LOGGER.info(String.format("%s[restored][history payloads=%s, longerThan=%s]notification candidates=%s, active=%s",
                HistoryMonitorService.LOG_IDENTIFIER, payloadsSize, longerThanSize, notifierCandidatesSize, notifierActiveSize));
    }

    private DBItemJocVariable getJocVariable() throws Exception {
        DBLayerMonitoring dbLayer = new DBLayerMonitoring(MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY);
        try {
            dbLayer.setSession(factory.openStatelessSession(dbLayer.getIdentifier()));
            dbLayer.getSession().beginTransaction();
            DBItemJocVariable item = dbLayer.getVariable();
            dbLayer.getSession().commit();
            return item;
        } catch (Exception e) {
            dbLayer.rollback();
            throw e;
        } finally {
            dbLayer.close();
        }
    }

    private void saveJocVariable(byte[] val) throws Exception {
        DBLayerMonitoring dbLayer = new DBLayerMonitoring(MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY);
        try {
            dbLayer.setSession(factory.openStatelessSession(dbLayer.getIdentifier()));
            dbLayer.getSession().beginTransaction();
            dbLayer.saveVariable(val);
            dbLayer.getSession().commit();
        } catch (Exception e) {
            dbLayer.rollback();
            throw e;
        } finally {
            dbLayer.close();
        }
    }

    private void deleteJocVariable() {
        DBLayerMonitoring dbLayer = new DBLayerMonitoring(MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY);
        try {
            dbLayer.setSession(factory.openStatelessSession(dbLayer.getIdentifier()));
            dbLayer.getSession().beginTransaction();
            dbLayer.deleteVariable();
            dbLayer.getSession().commit();
        } catch (Exception e) {
            dbLayer.rollback();
            LOGGER.warn(e.toString(), e);
        } finally {
            dbLayer.close();
        }
    }

    // TODO duplicate HistoryUtil.eventId2Instant
    private static Instant eventId2Instant(Long eventId) {
        return Instant.ofEpochMilli(eventId / 1000);
    }

    // TODO duplicate HistoryUtil.getEventIdAsDate
    private static Date getEventIdAsDate(Long eventId) {
        return eventId == null ? null : Date.from(eventId2Instant(eventId));
    }

    public AtomicLong getLastActivityStart() {
        return lastActivityStart;
    }

    public AtomicLong getLastActivityEnd() {
        return lastActivityEnd;
    }

}
