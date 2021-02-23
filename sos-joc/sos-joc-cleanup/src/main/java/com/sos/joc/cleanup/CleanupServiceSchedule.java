package com.sos.joc.cleanup;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
import com.sos.joc.cleanup.db.DBLayerCleanup;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.db.joc.DBItemJocVariable;

public class CleanupServiceSchedule {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupServiceSchedule.class);

    private final static String DELIMITER = "->";
    private final CleanupService service;
    private ScheduledExecutorService threadPool = null;
    private ScheduledFuture<JocClusterAnswer> resultFuture = null;
    private CleanupServiceTask task = null;
    private DBItemJocVariable item = null;
    private ZonedDateTime start = null;
    private ZonedDateTime end = null;

    public CleanupServiceSchedule(CleanupService service) {
        this.service = service;
        this.task = new CleanupServiceTask(this.service);
    }

    public void start(StartupMode mode) {
        try {
            try {
                long delay = computeNextDelay();
                if (delay > 0) {
                    long timeout = computeTimeout();
                    JocClusterAnswer answer = setSchedule(mode, delay, timeout);
                    LOGGER.info("RESULT = " + SOSString.toString(answer));
                    updateJocVariableOnResult(answer);
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

    private long computeNextDelay() throws Exception {
        DBLayerCleanup dbLayer = null;
        try {
            dbLayer = new DBLayerCleanup(service.getFactory().openStatelessSession());
            dbLayer.getSession().setIdentifier(service.getIdentifier());
            item = getJocVariable(dbLayer);

            boolean computeNewPeriod = false;
            ZonedDateTime now = Instant.now().atZone(service.getConfig().getZoneId());
            ZonedDateTime nextFromDB = null;
            ZonedDateTime nextToDB = null;
            String configuredDB = null;
            if (item != null) {
                LOGGER.info(String.format("[stored value]%s", item.getTextValue()));
                String[] arr = item.getTextValue().split(DELIMITER);
                configuredDB = arr[0].trim();
                try {
                    nextFromDB = ZonedDateTime.parse(arr[1].trim(), DateTimeFormatter.ISO_ZONED_DATE_TIME);
                } catch (Throwable e) {
                    LOGGER.warn(String.format("[%s]%s", arr[1].trim(), e.toString()), e);
                }
                try {
                    nextToDB = ZonedDateTime.parse(arr[2].trim(), DateTimeFormatter.ISO_ZONED_DATE_TIME);
                } catch (Throwable e) {
                    LOGGER.warn(String.format("[%s]%s", arr[2].trim(), e.toString()), e);
                }
                if (nextFromDB != null && nextToDB != null && arr.length > 3) {
                    String val = arr[3];
                    if (val.equalsIgnoreCase(JocClusterAnswerState.COMPLETED.name())) {
                        computeNewPeriod = true;
                        LOGGER.info(String.format("cleanup completed, compute next period..."));
                    }
                } else {

                }

            }
            ZonedDateTime nextTo = null;
            long nanos = 0;
            switch (service.getConfig().getStartupMode()) {
            case WEEKLY:
                LOGGER.info("WEEKLY not implemented yet");
                break;
            case DAILY:
                try {
                    Period configured = service.getConfig().getPeriod();
                    ZonedDateTime nextFrom = ZonedDateTime.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), configured.getFrom()
                            .getHours(), configured.getFrom().getMinutes(), configured.getFrom().getSeconds(), 0, service.getConfig().getZoneId());

                    nextTo = ZonedDateTime.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), configured.getTo().getHours(), configured
                            .getTo().getMinutes(), configured.getTo().getSeconds(), 0, service.getConfig().getZoneId());

                    if (!nextTo.isAfter(nextFrom)) {
                        nextTo = nextTo.plusDays(1);
                        LOGGER.info(String.format("set nextTo=%s", nextTo));
                    }

                    if (!computeNewPeriod) {
                        if (nextFromDB != null && nextToDB != null && nextFromDB.isAfter(nextFrom)) {
                            if (configuredDB != null && !configuredDB.equals(configured.getConfigured())) {
                                LOGGER.info(String.format("[stored value][skip]period was changed (old=%s, new=%s)", configuredDB, configured
                                        .getConfigured()));
                            } else {
                                nextFrom = nextFromDB;
                                nextTo = nextToDB;
                            }
                        }

                        if (now.isAfter(nextFrom)) {
                            if (now.isAfter(nextTo)) {
                                computeNewPeriod = true;
                            } else {
                                nextFrom = null;
                            }
                        } else {
                            LOGGER.info(String.format("nextFrom=%s, nextTo=%s", nextFrom, nextTo));
                        }
                    }

                    if (computeNewPeriod) {
                        nextFrom = nextFrom.plusDays(1);
                        nextTo = nextTo.plusDays(1);
                        LOGGER.info(String.format("set nextFrom=%s, nextTo=%s", nextFrom, nextTo));
                    }

                    now = Instant.now().atZone(service.getConfig().getZoneId());
                    if (nextFrom == null) {
                        nextFrom = now.plusSeconds(30);
                        LOGGER.info(String.format("set nextFrom=%s", nextFrom));
                    }
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
                if (item == null) {
                    insertJocVariable(dbLayer);
                } else {
                    updateJocVariable(dbLayer, item);
                }
                return now.until(start, ChronoUnit.NANOS);
            }
            return nanos;

        } catch (Throwable e) {
            throw e;
        } finally {
            if (dbLayer != null) {
                dbLayer.close();
            }
        }
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

    private DBItemJocVariable getJocVariable(DBLayerCleanup dbLayer) throws Exception {
        try {
            dbLayer.getSession().beginTransaction();
            DBItemJocVariable item = dbLayer.getVariable(service.getIdentifier());
            dbLayer.getSession().commit();
            return item;
        } catch (Exception e) {
            if (dbLayer != null) {
                try {
                    dbLayer.getSession().rollback();
                } catch (Throwable ex) {
                }
            }
            throw e;
        }
    }

    private DBItemJocVariable insertJocVariable(DBLayerCleanup dbLayer) throws Exception {
        try {
            dbLayer.getSession().beginTransaction();
            DBItemJocVariable item = dbLayer.insertJocVariable(service.getIdentifier(), getInitialValue().toString());
            dbLayer.getSession().commit();
            return item;
        } catch (Exception e) {
            if (dbLayer != null) {
                try {
                    dbLayer.getSession().rollback();
                } catch (Throwable ex) {
                }
            }
            throw e;
        }
    }

    private DBItemJocVariable updateJocVariable(DBLayerCleanup dbLayer, DBItemJocVariable item) throws Exception {
        try {
            dbLayer.getSession().beginTransaction();
            item.setTextValue(getInitialValue().toString());
            dbLayer.getSession().update(item);
            dbLayer.getSession().commit();
            return item;
        } catch (Exception e) {
            if (dbLayer != null) {
                try {
                    dbLayer.getSession().rollback();
                } catch (Throwable ex) {
                }
            }
            throw e;
        }
    }

    private void updateJocVariableOnResult(JocClusterAnswer answer) throws Exception {
        if (answer == null || item == null) {
            return;
        }

        StringBuilder state = new StringBuilder(answer.getState().name());
        if (!SOSString.isEmpty(answer.getMessage())) {
            state.append("=").append(answer.getMessage());
        }

        DBLayerCleanup dbLayer = null;
        try {
            dbLayer = new DBLayerCleanup(service.getFactory().openStatelessSession());
            dbLayer.getSession().setIdentifier(service.getIdentifier());

            dbLayer.getSession().beginTransaction();
            item.setTextValue(getInitialValue().append(DELIMITER).append(state).toString());
            dbLayer.getSession().update(item);
            dbLayer.getSession().commit();
        } catch (Exception e) {
            if (dbLayer != null) {
                try {
                    dbLayer.getSession().rollback();
                } catch (Throwable ex) {
                }
            }
            throw e;
        } finally {
            if (dbLayer != null) {
                dbLayer.close();
            }
        }
    }

    private StringBuilder getInitialValue() {
        return new StringBuilder(service.getConfig().getPeriod().getConfigured()).append(DELIMITER).append(start.toString()).append(DELIMITER).append(
                end.toString());
    }
}
