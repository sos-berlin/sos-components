package com.sos.joc.cleanup;

import java.nio.file.Path;
import java.sql.Connection;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.joc.cleanup.CleanupServiceConfiguration.Period;
import com.sos.joc.cleanup.db.DBLayerCleanup;
import com.sos.joc.cluster.AJocClusterService;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.joc.DBItemJocVariable;

public class CleanupServiceSchedule {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupServiceSchedule.class);

    private final static String DELIMITER = "->";
    private static final int FACTORY_MAX_POOL_SIZE = 3;

    private final CleanupService service;
    private JocClusterHibernateFactory factory;
    private DBLayerCleanup dbLayer;
    private ScheduledExecutorService threadPool = null;
    private ScheduledFuture<JocClusterAnswer> resultFuture = null;
    private CleanupServiceTask task = null;
    private DBItemJocVariable item = null;
    private ZonedDateTime firstStart = null;
    private ZonedDateTime start = null;
    private ZonedDateTime end = null;
    List<String> unclompleted = null;

    public CleanupServiceSchedule(CleanupService service) {
        this.service = service;
        this.task = new CleanupServiceTask(this);
        this.dbLayer = new DBLayerCleanup(service.getIdentifier());
    }

    public void start(StartupMode mode) throws Exception {
        try {
            if (factory == null) {
                createFactory(service.getConfig().getHibernateConfiguration());
            }
            try {
                long delay = computeNextDelay();
                if (delay > 0) {
                    long timeout = computeTimeout();
                    JocClusterAnswer answer = schedule(mode, delay, timeout);
                    LOGGER.info(SOSString.toString(answer));
                    updateJocVariableOnResult(answer);
                } else {
                    LOGGER.error(String.format("[%s][%s][skip start]delay can't be computed", service.getIdentifier(), mode));
                }
            } catch (TimeoutException e) {
                LOGGER.info(String.format("[max end at %s reached]try stop..", end.toString()));
                closeTasks(mode);
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error(e.toString(), e);
                closeTasks(mode);
            } catch (CancellationException e) {
            }

        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

    private JocClusterAnswer schedule(StartupMode mode, long delay, long timeout) throws Exception {
        closeTasks(mode);

        threadPool = Executors.newScheduledThreadPool(1, new JocClusterThreadFactory(service.getThreadGroup(), service.getIdentifier() + "-t"));
        LOGGER.info(String.format("[planned][start=%s][max end=%s][timeout=%s] ...", start.toString(), end.toString(), SOSDate.getDuration(Duration
                .ofSeconds(timeout))));
        resultFuture = threadPool.schedule(task, delay, TimeUnit.NANOSECONDS);
        return resultFuture.get(timeout, TimeUnit.SECONDS);
    }

    private long computeNextDelay() throws Exception {
        AJocClusterService.setLogger(service.getIdentifier());

        unclompleted = null;
        try {
            dbLayer.setSession(getFactory().openStatelessSession(service.getIdentifier()));
            item = getJocVariable();

            String storedConfigure = null;
            ZonedDateTime storedFirstStart = null;
            ZonedDateTime storedNextFrom = null;
            ZonedDateTime storedNextTo = null;

            boolean computeNewPeriod = false;
            ZonedDateTime now = Instant.now().atZone(service.getConfig().getZoneId());
            if (item != null) {
                LOGGER.info(String.format("[computeNextDelay][stored]%s", item.getTextValue()));
                String[] arr = item.getTextValue().split(DELIMITER);
                if (arr.length > 3) {
                    storedConfigure = arr[0].trim();
                    storedFirstStart = parseFromDb(arr[1].trim());
                    storedNextFrom = parseFromDb(arr[2].trim());
                    storedNextTo = parseFromDb(arr[3].trim());

                    if (storedNextFrom != null && storedNextTo != null && arr.length > 4) {
                        String state = arr[4];
                        if (state.equalsIgnoreCase(JocClusterAnswerState.COMPLETED.name())) {
                            computeNewPeriod = true;
                            LOGGER.info(String.format("cleanup completed, compute next period..."));
                        } else {
                            if (state.startsWith(JocClusterAnswerState.UNCOMPLETED.name())) {
                                arr = state.split("=");// UNCOMPLETED=dayliplan,history
                                if (arr.length > 1) {
                                    unclompleted = Stream.of(arr[1].split(",", -1)).collect(Collectors.toList());
                                }
                            }
                        }
                    }
                } else {
                    LOGGER.info(String.format("[stored][skip]old format"));
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
                        if (storedNextFrom != null && storedNextTo != null && storedNextFrom.isAfter(nextFrom)) {
                            if (storedConfigure != null && !storedConfigure.equals(configured.getConfigured())) {
                                LOGGER.info(String.format("[stored][skip]period was changed (old=%s, new=%s)", storedConfigure, configured
                                        .getConfigured()));
                                storedFirstStart = null;
                            } else {
                                nextFrom = storedNextFrom;
                                nextTo = storedNextTo;
                            }
                        }

                        if (now.isAfter(nextFrom)) {
                            if (now.isAfter(nextTo)) {
                                computeNewPeriod = true;
                            } else {
                                nextFrom = null;
                            }
                        } else {
                            LOGGER.debug(String.format("nextFrom=%s, nextTo=%s", nextFrom, nextTo));
                        }
                    }

                    if (computeNewPeriod) {
                        nextFrom = nextFrom.plusDays(1);
                        nextTo = nextTo.plusDays(1);
                        storedFirstStart = null;
                        LOGGER.info(String.format("set nextFrom=%s, nextTo=%s", nextFrom, nextTo));
                    }

                    now = Instant.now().atZone(service.getConfig().getZoneId());
                    if (nextFrom == null) {
                        nextFrom = now.plusSeconds(30);
                        LOGGER.info(String.format("set nextFrom=(in 30s)%s", nextFrom));
                    }
                    firstStart = storedFirstStart == null ? nextFrom : storedFirstStart;
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
                    insertJocVariable();
                } else {
                    updateJocVariable(item);
                }
                return now.until(start, ChronoUnit.NANOS);
            }
            return nanos;

        } catch (Throwable e) {
            throw e;
        } finally {
            dbLayer.close();
        }
    }

    private ZonedDateTime parseFromDb(String date) {
        try {
            return ZonedDateTime.parse(date, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        } catch (Throwable e) {
            try {
                deleteJocVariable();
            } catch (Exception e1) {
                LOGGER.error(String.format("[%s]%s", date, e.toString()), e);
            }
            LOGGER.warn(String.format("[%s]%s", date, e.toString()));
            return null;
        }
    }

    private long computeTimeout() {
        ZonedDateTime now = Instant.now().atZone(service.getConfig().getZoneId());
        return now.until(end, ChronoUnit.SECONDS);
    }

    private void closeTasks(StartupMode mode) {
        if (task != null) {
            JocClusterAnswer answer = task.stop();
            if (answer != null && answer.getState().equals(JocClusterAnswerState.UNCOMPLETED)) {
                try {
                    updateJocVariableOnResult(answer);
                } catch (Throwable e) {
                    LOGGER.error(e.toString(), e);
                }
            }
        }
        if (resultFuture != null) {
            resultFuture.cancel(false);
            AJocClusterService.setLogger(service.getIdentifier());
            LOGGER.info(String.format("[%s][%s]resultFuture cancelled", service.getIdentifier(), mode));
            AJocClusterService.clearLogger();
        }
        if (threadPool != null) {
            AJocClusterService.setLogger(service.getIdentifier());
            JocCluster.shutdownThreadPool(mode, threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);
            AJocClusterService.clearLogger();
            threadPool = null;
        }
        resultFuture = null;
    }

    public void stop(StartupMode mode) {
        closeTasks(mode);
        closeFactory();
    }

    private DBItemJocVariable getJocVariable() throws Exception {
        try {
            dbLayer.getSession().beginTransaction();
            DBItemJocVariable item = dbLayer.getVariable(service.getIdentifier());
            dbLayer.getSession().commit();
            return item;
        } catch (Exception e) {
            dbLayer.rollback();
            throw e;
        }
    }

    private void deleteJocVariable() throws Exception {
        if (item == null) {
            return;
        }
        try {
            dbLayer.getSession().beginTransaction();
            dbLayer.deleteVariable(service.getIdentifier());
            dbLayer.getSession().commit();
            item = null;
        } catch (Exception e) {
            dbLayer.rollback();
            throw e;
        }
    }

    private DBItemJocVariable insertJocVariable() throws Exception {
        return insertJocVariable(getInitialValue().toString());
    }

    private DBItemJocVariable insertJocVariable(String value) throws Exception {
        try {
            dbLayer.getSession().beginTransaction();
            DBItemJocVariable item = dbLayer.insertJocVariable(service.getIdentifier(), value);
            dbLayer.getSession().commit();
            return item;
        } catch (Exception e) {
            dbLayer.rollback();
            throw e;
        }
    }

    private DBItemJocVariable updateJocVariable(DBItemJocVariable item) throws Exception {
        try {
            dbLayer.getSession().beginTransaction();
            item.setTextValue(getInitialValue().toString());
            dbLayer.getSession().update(item);
            dbLayer.getSession().commit();
            return item;
        } catch (Exception e) {
            dbLayer.rollback();
            throw e;
        }
    }

    private void updateJocVariableOnResult(JocClusterAnswer answer) throws Exception {
        if (answer == null || answer.getState().equals(JocClusterAnswerState.STOPPED)) {
            LOGGER.info("[skip store]STOPPED");
            return;
        }

        StringBuilder state = new StringBuilder(answer.getState().name());
        if (!SOSString.isEmpty(answer.getMessage())) {
            state.append("=").append(answer.getMessage());
        }

        try {
            dbLayer.setSession(factory.openStatelessSession(service.getIdentifier()));
            String val = getInitialValue().append(DELIMITER).append(state).toString();
            if (item == null) {
                item = getJocVariable();
                if (item == null) {
                    item = insertJocVariable(val);
                    val = null;
                }
            }
            if (val != null) {
                dbLayer.getSession().beginTransaction();
                item.setTextValue(val);
                dbLayer.getSession().update(item);
                dbLayer.getSession().commit();
            }
            LOGGER.info("[stored]" + item.getTextValue());
        } catch (Exception e) {
            dbLayer.rollback();
            throw e;
        } finally {
            dbLayer.close();
        }
    }

    public CleanupService getService() {
        return service;
    }

    public ZonedDateTime getFirstStart() {
        return firstStart;
    }

    public List<String> getUncompleted() {
        return unclompleted;
    }

    private StringBuilder getInitialValue() {
        return new StringBuilder(service.getConfig().getPeriod().getConfigured()).append(DELIMITER).append(firstStart.toString()).append(DELIMITER)
                .append(start.toString()).append(DELIMITER).append(end.toString());
    }

    public JocClusterHibernateFactory getFactory() {
        return factory;
    }

    private void createFactory(Path configFile) throws Exception {
        factory = new JocClusterHibernateFactory(configFile, 1, FACTORY_MAX_POOL_SIZE);
        factory.setIdentifier(service.getIdentifier());
        factory.setAutoCommit(false);
        factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        factory.addClassMapping(DBLayer.getJocClassMapping());
        factory.build();
    }

    private void closeFactory() {
        if (factory != null) {
            factory.close();
            factory = null;
        }
        LOGGER.info(String.format("[%s]database factory closed", service.getIdentifier()));
    }

}
