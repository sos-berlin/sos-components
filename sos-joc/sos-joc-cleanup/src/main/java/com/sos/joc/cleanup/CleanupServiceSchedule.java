package com.sos.joc.cleanup;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.joc.cleanup.CleanupServiceConfiguration.Period;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;

public class CleanupServiceSchedule {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupServiceSchedule.class);

    private final CleanupService service;
    private ScheduledExecutorService threadPool = null;
    ScheduledFuture<JocClusterAnswer> resultFuture = null;
    private CleanupServiceTask task = null;
    private ZonedDateTime start = null;
    private ZonedDateTime end = null;

    public CleanupServiceSchedule(CleanupService service) {
        this.service = service;
        this.task = new CleanupServiceTask(service);
    }

    public void start(StartupMode mode) {
        try {
            try {
                long delay = computeNextDelay();
                if (delay > 0) {
                    long timeout = computeTimeout();
                    JocClusterAnswer answer = setSchedule(mode, delay, timeout);
                    LOGGER.info("RESULT = " + SOSString.toString(answer));
                } else {
                    LOGGER.error(String.format("[%s][%s][skip start]delay can't be computed", service.getIdentifier(), mode));
                }
            } catch (TimeoutException e) {
                LOGGER.info(String.format("[max end at %s reached]try stop..", end.toString()));
                close(mode);
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error(e.toString(), e);
                close(mode);
            } catch (CancellationException e) {
            }
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

    private JocClusterAnswer setSchedule(StartupMode mode, long delay, long timeout) throws Exception {
        close(mode);

        threadPool = Executors.newScheduledThreadPool(1, new JocClusterThreadFactory(service.getThreadGroup(), service.getIdentifier() + "-t"));
        LOGGER.info(String.format("[planned][start=%s][max end=%s][timeout=%s] ...", start.toString(), end.toString(), SOSDate.getDuration(Duration
                .ofSeconds(timeout))));
        resultFuture = threadPool.schedule(task, delay, TimeUnit.NANOSECONDS);
        return resultFuture.get(timeout, TimeUnit.SECONDS);
    }

    public JocClusterAnswer stop(StartupMode mode) {
        LOGGER.info(String.format("[%s][%s]stop...", service.getIdentifier(), mode));
        close(mode);
        LOGGER.info(String.format("[%s][%s]stopped", service.getIdentifier(), mode));
        return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
    }

    private long computeNextDelay() {
        ZonedDateTime now = Instant.now().atZone(service.getConfig().getZoneId());
        ZonedDateTime nextTo = null;
        long nanos = 0;
        switch (service.getConfig().getStartupMode()) {
        case WEEKLY:
            LOGGER.info("WEEKLY not implemented yet");
            break;
        case DAILY:
            try {
                Period configured = service.getConfig().getPeriod();
                ZonedDateTime nextFrom = ZonedDateTime.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), configured.getFrom().getHours(),
                        configured.getFrom().getMinutes(), configured.getFrom().getSeconds(), 0, service.getConfig().getZoneId());

                nextTo = ZonedDateTime.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), configured.getTo().getHours(), configured.getTo()
                        .getMinutes(), configured.getTo().getSeconds(), 0, service.getConfig().getZoneId());

                if (!nextTo.isAfter(nextFrom)) {
                    nextTo = nextTo.plusDays(1);
                    LOGGER.info(String.format("set nextTo=%s", nextTo));
                }
                if (now.isAfter(nextFrom)) {
                    if (now.isAfter(nextTo)) {
                        nextFrom = nextFrom.plusDays(1);
                        nextTo = nextTo.plusDays(1);

                        LOGGER.info(String.format("set nextFrom=%s, nextTo=%s", nextFrom, nextTo));
                    } else {
                        nextFrom = nextFrom.plusSeconds(5);// start

                        LOGGER.info(String.format("set nextFrom=%s", nextFrom));
                    }
                } else {
                    LOGGER.info(String.format("nextFrom=%s, nextTo=%s", nextFrom, nextTo));
                }
                now = Instant.now().atZone(service.getConfig().getZoneId());
                nanos = now.until(nextFrom, ChronoUnit.NANOS);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        default:
            break;
        }
        if (nanos > 0) {
            start = now.plusNanos(nanos);
            if (nextTo != null) {
                end = nextTo;
            }
            return now.until(start, ChronoUnit.NANOS);
        }
        return nanos;
    }

    private long computeTimeout() {
        ZonedDateTime now = Instant.now().atZone(service.getConfig().getZoneId());
        return now.until(end, ChronoUnit.SECONDS);
    }

    public void close(StartupMode mode) {
        if (task != null) {
            task.close();
        }
        if (resultFuture != null) {
            resultFuture.cancel(false);
            LOGGER.info(String.format("[%s][%s]resultFuture cancelled", service.getIdentifier(), mode));
        }
        if (threadPool != null) {
            JocCluster.shutdownThreadPool(mode, threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);
            threadPool = null;
        }
        resultFuture = null;
    }
}
