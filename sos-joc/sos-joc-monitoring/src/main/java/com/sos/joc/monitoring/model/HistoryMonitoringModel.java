package com.sos.joc.monitoring.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
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
import com.sos.joc.cluster.service.JocClusterServiceLogger;
import com.sos.joc.db.joc.DBItemJocVariable;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.history.HistoryEvent;
import com.sos.joc.event.bean.history.HistoryOrderEvent;
import com.sos.joc.event.bean.history.HistoryOrderTaskLogFirstStderr;
import com.sos.joc.event.bean.history.HistoryTaskEvent;
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

    private ScheduledExecutorService threadPool;
    private CopyOnWriteArraySet<AHistoryBean> payloads = new CopyOnWriteArraySet<>();
    // concurrent because - close(serialization) is called from another thread.
    private Map<Long, HistoryOrderStepBean> longerThan = new ConcurrentHashMap<>();

    private AtomicLong lastActivityStart = new AtomicLong();
    private AtomicLong lastActivityEnd = new AtomicLong();

    private AtomicBoolean closed = new AtomicBoolean();
    private AtomicBoolean pause = new AtomicBoolean();
    private AtomicBoolean inProcess = new AtomicBoolean();

    // TODO ? commit after n db operations
    // private int maxTransactions = 100;

    public HistoryMonitoringModel(ThreadGroup threadGroup, SOSHibernateFactory factory, JocConfiguration jocConfiguration) {
        this.factory = factory;
        this.jocConfiguration = jocConfiguration;
        this.notifier = new OrderNotifierModel(threadGroup, factory.getConfigFile().get());
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
            JocCluster.shutdownThreadPool("[" + MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY + "][" + mode + "]", threadPool,
                    JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);
            threadPool = null;
            persistQueues();
        }
    }

    // from another thread
    public void startPause(String caller, int pauseDurationInSeconds) {
        if (!closed.get()) {
            MAX_PAUSE_IN_SECONDS = pauseDurationInSeconds + 10;
            pause.set(true);
            String msg = "[called from " + caller + "][startPause]maximum for " + pauseDurationInSeconds + "s...";

            // 1) write to e.g. cleanup log file
            LOGGER.info("[" + MonitorService.MAIN_SERVICE_IDENTIFIER + "][service][" + MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY + "]" + msg);

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
            LOGGER.info("[" + MonitorService.MAIN_SERVICE_IDENTIFIER + "][service][" + MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY + "]" + msg);

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
                LOGGER.info("[" + MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY + "][waitForNotInProcess][stopped]MAX_IN_PROCESS_IN_SECONDS="
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

            private AtomicLong currentEventId = new AtomicLong();
            private AtomicLong lastStart = new AtomicLong();
            private AtomicLong pauseCounter = new AtomicLong();

            private Long calculateEventId(Long eventId, long lastDuration) {
                if (eventId == null) {// no new events
                    long id = currentEventId.get();
                    if (id != 0 && lastDuration != 0) {
                        eventId = id + (lastDuration * 1_000);
                    }
                }
                if (eventId != null) {
                    currentEventId.set(eventId);
                }
                return eventId == null ? 0L : eventId;
            }

            @Override
            public void run() {
                long currentStart = new Date().getTime();
                long previousStart = lastStart.get();
                long lastDuration = 0;
                if (previousStart != 0) {
                    lastDuration = currentStart - previousStart;
                }
                lastStart.set(currentStart);
                try {
                    MonitorService.setLogger();
                    boolean isDebugEnabled = LOGGER.isDebugEnabled();

                    if (!closed.get()) {
                        if (pause.get()) {
                            pauseCounter.set(pauseCounter.get() + 1);
                            if (MAX_PAUSE_IN_SECONDS > 0 && pauseCounter.get() >= MAX_PAUSE_IN_SECONDS) {
                                pause.set(false);
                                LOGGER.info("[" + MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY + "][cause][stopped]MAX_PAUSE_IN_SECONDS="
                                        + MAX_PAUSE_IN_SECONDS + " reached");
                            }
                        } else {
                            pauseCounter.set(0L);
                            inProcess.set(true);

                            Instant start = Instant.now();
                            HistoryMonitoringPayloadHandler ph = new HistoryMonitoringPayloadHandler();
                            ToNotify toNotifyPayloads = ph.handlePayloads(model, isDebugEnabled);
                            if (closed.get()) {
                                if (toNotifyPayloads.getFirstEventId() != null) {
                                    LOGGER.info(String.format("[%s][%s-%s][UTC][%s-%s][%s][on close][size]payloads=%s, longerThan=%s",
                                            MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, toNotifyPayloads.getFirstEventId(), toNotifyPayloads
                                                    .getLastEventId(), eventIdAsTime(toNotifyPayloads.getFirstEventId()), eventIdAsTime(
                                                            toNotifyPayloads.getLastEventId()), SOSDate.getDuration(Duration.between(start, Instant
                                                                    .now())), payloads.size(), longerThan.size()));
                                }
                            } else {
                                // checks for warnings in all registered longerThan
                                // - not contains the longerThan warnings evaluated by handlePayloads(), since they have already been removed before
                                // handleLongerThan
                                ToNotify toNotifyLongerThanNotPayloadWarnings = handleLongerThan(calculateEventId(toNotifyPayloads.getLastEventId(),
                                        lastDuration));

                                if (toNotifyPayloads.getFirstEventId() != null) {
                                    LOGGER.info(String.format("[%s][%s-%s][UTC][%s-%s][%s][size for next iteration]payloads=%s, longerThan=%s",
                                            MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, toNotifyPayloads.getFirstEventId(), toNotifyPayloads
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

    private ToNotify handleLongerThan(Long calculatedEventId) {
        // LOGGER.info("calculatedEventId=" + calculatedEventId + "(" + eventIdAsTime(calculatedEventId) + "), size=" + longerThan.size());

        ToNotify toNotify = new ToNotify();
        if (longerThan.size() == 0) {
            return toNotify;
        }
        if (calculatedEventId == null || calculatedEventId.equals(0L)) {
            return toNotify;
        }

        MonitorService.setLogger();
        DBLayerMonitoring dbLayer = new DBLayerMonitoring(MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY);
        try {
            setLastActivityStart();
            // dbLayer.setSession(factory.openStatelessSession(dbLayer.getIdentifier()));

            Date endTime = getEventIdAsDate(calculatedEventId);
            Map<Long, MonitorOrderStepResult> w = new HashMap<>();
            Set<HistoryOrderStepBean> toRemove = new HashSet<>();
            boolean isDebugEnabled = LOGGER.isDebugEnabled();
            longerThan.entrySet().stream().takeWhile(c -> !closed.get()).forEach(entry -> {
                HistoryOrderStepBean hosb = entry.getValue();
                Date stepEndTime = hosb.getEndTime() == null ? endTime : hosb.getEndTime();

                MonitorOrderStepResultWarn warn = analyzeLongerThan(dbLayer, hosb, stepEndTime, false);
                if (warn != null) {
                    if (warn.isInvalid()) {
                        toRemove.add(hosb);
                    } else {
                        MonitorOrderStepResult r = new MonitorOrderStepResult(hosb, warn);
                        w.put(entry.getKey(), r);

                        if (isDebugEnabled) {
                            try {
                                LOGGER.debug(String.format(
                                        "[%s][handleLongerThan][UTC][start=%s, calculated current=%s][%s]orderId=%s, workflow=%s, job=%s(historyId=%s)",
                                        MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, SOSDate.getTimeAsString(hosb.getStartTime()), SOSDate
                                                .getTimeAsString(stepEndTime), warn.getText(), hosb.getOrderId(), hosb.getWorkflowPath(), hosb
                                                        .getJobName(), hosb.getHistoryId()));
                            } catch (Exception e) {
                                LOGGER.warn(e.toString(), e);
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
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][handleLongerThan][processed=%s][toNotify steps=%s]longerThan size for next iteration=%s",
                        MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, w.size(), toNotify.getSteps().size(), longerThan.size()));
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
            LOGGER.debug(String.format("[%s][longerThan=%s][caller=%s][put]job=%s(historyId=%s)", MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, hosb
                    .getHistoryId(), caller, hosb.getJobName(), hosb.getHistoryId()));
        }
    }

    protected void removeLongerThan(String caller, HistoryOrderStepBean hosb) {
        HistoryOrderStepBean r = longerThan.remove(hosb.getHistoryId());
        if (LOGGER.isDebugEnabled()) {
            String removed = r == null ? "not exists" : "removed";
            LOGGER.debug(String.format("[%s][longerThan=%s][caller=%s][%s]job=%s(historyId=%s)", MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, hosb
                    .getHistoryId(), caller, removed, hosb.getJobName(), hosb.getHistoryId()));
        }
    }

    protected boolean longerThanExists(String caller, HistoryOrderStepBean hosb) {
        boolean exists = longerThan.containsKey(hosb.getHistoryId());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s][longerThan=%s][caller=%s][exists=%s]job=%s(historyId=%s)", MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY,
                    hosb.getHistoryId(), caller, exists, hosb.getJobName(), hosb.getHistoryId()));
        }
        return exists;
    }

    protected MonitorOrderStepResultWarn analyzeLongerThan(DBLayerMonitoring dbLayer, HistoryOrderStepBean hosb, Date endTime,
            boolean isHandlePayload) {

        MonitorOrderStepResultWarn invalidWarn = new MonitorOrderStepResultWarn(true);
        try {
            if (isHandlePayload) {
                if (!longerThanExists("analyzeLongerThan", hosb)) {
                    return null;
                }
                removeLongerThan("analyzeLongerThan", hosb);
            }

            ExpectedSeconds expected = getExpectedSeconds(dbLayer, JobWarning.LONGER_THAN, hosb, hosb.getWarnIfLonger());
            if (expected == null || expected.getSeconds() == null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[%s][longerThan=%s][analyzeLongerThan][isHandlePayload=%s][skip][expected=%s]%s",
                            MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, hosb.getHistoryId(), isHandlePayload, SOSString.toString(expected),
                            SOSString.toString(hosb)));

                }
                return invalidWarn;
            }

            long diff = SOSDate.getSeconds(endTime) - SOSDate.getSeconds(hosb.getStartTime());
            if (diff < 0) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[%s][longerThan=%s][analyzeLongerThan][isHandlePayload=%s][diff=%s < 0][startTime=%s, endTime=%s]%s",
                            MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, hosb.getHistoryId(), isHandlePayload, diff, SOSDate.tryGetDateTimeAsString(
                                    hosb.getStartTime()), SOSDate.tryGetDateTimeAsString(endTime), SOSString.toString(hosb)));

                }
                return invalidWarn;
            }

            if (diff > expected.getSeconds()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format(
                            "[%s][longerThan=%s][analyzeLongerThan][isHandlePayload=%s][match][diff=%s > %s][startTime=%s, endTime=%s]%s",
                            MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, hosb.getHistoryId(), isHandlePayload, diff, expected.getSeconds(), SOSDate
                                    .tryGetDateTimeAsString(hosb.getStartTime()), SOSDate.tryGetDateTimeAsString(endTime), SOSString.toString(hosb)));

                }
                return new MonitorOrderStepResultWarn(JobWarning.LONGER_THAN, String.format("Job runs longer than the expected %s",
                        getExpectedDurationMessage(hosb.getWarnIfLonger(), expected)));
            } else {
                if (!isHandlePayload) {// remove old entries
                    if (diff > MAX_LONGER_THAN_SECONDS) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug(String.format("[%s][longerThan=%s][analyzeLongerThan][isHandlePayload=%s][skip][too old][diff=%s > %s]%s",
                                    MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, hosb.getHistoryId(), isHandlePayload, diff,
                                    MAX_LONGER_THAN_SECONDS, SOSString.toString(hosb)));

                        }
                        return invalidWarn;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn(String.format("[%s][analyzeLongerThan][skip onError][workflow=%s, orderId=%s, job=%s(historyid=%s)]%s",
                    MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, hosb.getWorkflowPath(), hosb.getOrderId(), hosb.getJobName(), hosb.getHistoryId(),
                    e.toString()), e);
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
                        LOGGER.debug(String.format("[%s][%s definition=%s, avg=%s]%s, workflowPath=%s, job=%s(historyId=%s)",
                                MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, warnReason, definition, avg, hosb.getControllerId(), hosb
                                        .getWorkflowPath(), hosb.getJobName(), hosb.getHistoryId()));
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
                LOGGER.warn(String.format("[%s][%s definition=%s][error on get jobAvg][%s, workflowPath=%s, job=%s(historyId=%s)]%s",
                        MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, warnReason, definition, hosb.getControllerId(), hosb.getWorkflowPath(), hosb
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
            LOGGER.debug(String.format("[%s][%s definition=%s, seconds=%s]%s, workflowPath=%s, job=%s(historyId=%s)",
                    MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, warnReason, definition, seconds, hosb.getControllerId(), hosb.getWorkflowPath(),
                    hosb.getJobName(), hosb.getHistoryId()));
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
                LOGGER.debug(String.format("[%s][missing eventDate]%s", MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, SOSString.toString(bean)));
            }
        } else {
            Instant now = Instant.now();
            if (isNotExpired(now, eventDate, MAX_PAYLOAD_SECONDS)) {
                payloads.add(bean);
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[%s][skip][now=%s-eventDate=%s > MAX_PAYLOAD_SECONDS=%s]%s",
                            MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, SOSDate.tryGetDateTimeAsString(Date.from(now)), SOSDate
                                    .tryGetDateTimeAsString(Date.from(eventDate)), MAX_PAYLOAD_SECONDS, SOSString.toString(bean)));
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
                LOGGER.info(String.format("[%s][persisted][history payloads=%s, longerThan=%s]notification candidates=%s, active=%s",
                        MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, payloadsSnapshot.size(), longerThanSnapshot.size(), notifierCandidatesSnapshot
                                .size(), notifierActiveSnapshot.size()));
            } catch (Exception e) {
                LOGGER.warn(String.format("[%s][persistQueues]%s", MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, e.toString()), e);
            }
            payloads.clear();
            longerThan.clear();
            notifier.clear();
        } else {
            LOGGER.info(String.format("[%s][persist][skip]no history/notification data found to persist",
                    MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY));
            deleteJocVariable();
        }
    }

    private void restoreQueues() {
        DBItemJocVariable var = null;
        try {
            var = getJocVariable();
            if (var == null) {
                LOGGER.info(String.format("[%s][restore][skip]no persisted history/notification data found",
                        MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY));
                return;
            }
            restoreQueues(var);
            deleteJocVariable();
        } catch (Exception e) {
            LOGGER.warn(String.format("[%s][restoreQueues]%s", MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, e.toString()), e);
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
        LOGGER.info(String.format("[%s][restored][history payloads=%s, longerThan=%s]notification candidates=%s, active=%s",
                MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY, payloadsSize, longerThanSize, notifierCandidatesSize, notifierActiveSize));
    }

    private DBItemJocVariable getJocVariable() throws Exception {
        DBLayerMonitoring dbLayer = new DBLayerMonitoring(MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY);
        try {
            dbLayer.setSession(factory.openStatelessSession(dbLayer.getIdentifier()));
            return dbLayer.getVariable();
        } catch (Exception e) {
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
