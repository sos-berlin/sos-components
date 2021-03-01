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
import com.sos.joc.cleanup.exception.CleanupComputeException;
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
                throw new CleanupComputeException("delay can't be computed");
            }
        } catch (TimeoutException e) {
            LOGGER.info(String.format("[max end at %s reached]try stop..", end.toString()));
            closeTasks(mode);
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error(e.toString(), e);
            closeTasks(mode);
        } catch (CancellationException e) {
        } catch (CleanupComputeException e) {
            closeTasks(mode);
            throw e;
        }
    }

    private JocClusterAnswer schedule(StartupMode mode, long delay, long timeout) throws Exception {
        closeTasks(mode);
        AJocClusterService.setLogger(service.getIdentifier());
        threadPool = Executors.newScheduledThreadPool(1, new JocClusterThreadFactory(service.getThreadGroup(), service.getIdentifier() + "-t"));
        AJocClusterService.setLogger(service.getIdentifier());
        LOGGER.info(String.format("[planned][begin=%s][max end=%s][timeout=%s] ...", start.toString(), end.toString(), SOSDate.getDuration(Duration
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

            Period storedPeriod = null;
            ZonedDateTime storedFirstStart = null;
            ZonedDateTime storedNextBegin = null;
            ZonedDateTime storedNextEnd = null;
            JocClusterAnswerState storedState = null;

            Period period = service.getConfig().getPeriod();
            List<Integer> weekDays = service.getConfig().getPeriod().getWeekDays();
            ZonedDateTime nextEnd = null;
            ZonedDateTime nextBegin = null;

            long nanos = 0;
            ZonedDateTime now = Instant.now().atZone(service.getConfig().getZoneId());
            boolean computeNewPeriod = false;
            if (item != null) {
                String[] arr = item.getTextValue().split(DELIMITER);
                if (arr.length > 3) {
                    storedPeriod = parsePeriodFromDb(arr[0].trim());
                    storedNextBegin = parseDateFromDb(arr[1].trim());
                    storedNextEnd = parseDateFromDb(arr[2].trim());
                    storedFirstStart = parseDateFromDb(arr[3].trim());

                    LOGGER.info(String.format("[computeNextDelay][stored=%s][storedPeriod=%s]", item.getTextValue(), SOSString.toString(
                            storedPeriod)));

                    if (storedNextBegin != null && storedNextEnd != null) {
                        storedState = JocClusterAnswerState.RESTARTED;
                        if (storedNextBegin != null && arr.length > 4) {
                            String state = arr[4];
                            if (state.equalsIgnoreCase(JocClusterAnswerState.COMPLETED.name())) {
                                storedState = JocClusterAnswerState.COMPLETED;
                            } else {
                                if (state.startsWith(JocClusterAnswerState.UNCOMPLETED.name())) {
                                    storedState = JocClusterAnswerState.UNCOMPLETED;
                                    arr = state.split("=");// UNCOMPLETED=dayliplan,history
                                    if (arr.length > 1) {
                                        unclompleted = Stream.of(arr[1].split(",", -1)).collect(Collectors.toList());
                                    }
                                }
                            }
                        }
                        if (now.isAfter(storedNextBegin)) {
                            if (storedState.equals(JocClusterAnswerState.COMPLETED)) {
                                computeNewPeriod = true;
                                LOGGER.info(String.format("[computeNextDelay][%s]compute next period...", storedState));
                            } else {
                                if (storedState.equals(JocClusterAnswerState.UNCOMPLETED)) {
                                    LOGGER.info(String.format("[computeNextDelay][%s][stored][skip]now(%s) is after the storedNextBegin(%s)",
                                            storedState, now, storedNextBegin));
                                } else {
                                    LOGGER.info(String.format("[computeNextDelay][stored][skip]now(%s) is after the storedNextBegin(%s)", now,
                                            storedNextBegin));
                                }
                                storedFirstStart = null;
                                storedNextBegin = null;
                                storedNextEnd = null;
                            }
                        }
                        if (storedNextBegin != null) {
                            if (weekDays.contains(new Integer(storedNextBegin.getDayOfWeek().getValue()))) {
                                int storedNextDayOfWeek = storedNextBegin.getDayOfWeek().getValue();
                                int nowDayOfWeek = now.getDayOfWeek().getValue();
                                for (Integer weekDay : weekDays) {
                                    if (weekDay.intValue() < nowDayOfWeek) {
                                        continue;
                                    }
                                    if (weekDay.intValue() == nowDayOfWeek) {
                                        if (!storedPeriod.getWeekDays().contains(new Integer(nowDayOfWeek))) {
                                            LOGGER.info(String.format(
                                                    "[computeNextDelay][stored][skip]period was changed - today added (old=%s, new=%s)", storedPeriod
                                                            .getConfigured(), period.getConfigured()));
                                            storedFirstStart = null;
                                            storedNextBegin = null;
                                            storedNextEnd = null;
                                            break;
                                        }
                                    } else if (weekDay.intValue() < storedNextDayOfWeek) {
                                        LOGGER.info(String.format(
                                                "[computeNextDelay][stored][skip]period was changed - weekday(%s) added (old=%s, new=%s)", weekDay,
                                                storedPeriod.getConfigured(), period.getConfigured()));
                                        storedFirstStart = null;
                                        storedNextBegin = null;
                                        storedNextEnd = null;
                                        break;
                                    }
                                }
                            } else {
                                LOGGER.info(String.format("[computeNextDelay][stored][skip]stored dayOfWeek(%s) is no more configured",
                                        storedNextBegin.getDayOfWeek()));
                                storedFirstStart = null;
                                storedNextBegin = null;
                                storedNextEnd = null;
                            }
                        }
                    }
                } else {
                    LOGGER.info(String.format("[computeNextDelay][stored]%s", item.getTextValue()));
                    LOGGER.info(String.format("[computeNextDelay][stored][skip]old format"));
                }
            }

            if (storedNextBegin != null) {
                LOGGER.info(String.format("[computeNextDelay]use stored"));
                storedFirstStart = storedFirstStart.withHour(period.getBegin().getHours()).withMinute(period.getBegin().getMinutes()).withSecond(
                        period.getBegin().getSeconds()).withNano(0);

                nextBegin = storedNextBegin.withHour(period.getBegin().getHours()).withMinute(period.getBegin().getMinutes()).withSecond(period
                        .getBegin().getSeconds());
                nextEnd = storedNextEnd.withHour(period.getEnd().getHours()).withMinute(period.getEnd().getMinutes()).withSecond(period.getEnd()
                        .getSeconds());
            }

            int newPeriodDaysDiff = -1;
            if (nextBegin == null) {
                int nwd = now.getDayOfWeek().getValue();
                for (Integer wd : weekDays) {
                    if (wd == nwd) {
                        newPeriodDaysDiff = 0;
                        break;
                    }
                    if (wd > nwd) {
                        newPeriodDaysDiff = wd - nwd;
                        break;
                    }
                }
                nextBegin = ZonedDateTime.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth() + newPeriodDaysDiff, period.getBegin()
                        .getHours(), period.getBegin().getMinutes(), period.getBegin().getSeconds(), 0, service.getConfig().getZoneId());
                nextEnd = ZonedDateTime.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth() + newPeriodDaysDiff, period.getEnd().getHours(),
                        period.getEnd().getMinutes(), period.getEnd().getSeconds(), 0, service.getConfig().getZoneId());

                if (now.isAfter(nextBegin)) {
                    nwd = now.getDayOfWeek().getValue();
                    for (Integer wd : weekDays) {
                        if (wd > nwd) {
                            newPeriodDaysDiff = wd - nwd;
                            break;
                        }
                    }
                }
                LOGGER.info(String.format("[weekdays][newPeriodDaysDiff=%s][nextBegin=%s][nextEnd=%s]", newPeriodDaysDiff, nextBegin, nextEnd));
            } else {
                if (computeNewPeriod) {
                    int nwd = now.getDayOfWeek().getValue();
                    for (Integer wd : weekDays) {
                        if (wd > nwd) {
                            newPeriodDaysDiff = wd - nwd;
                            break;
                        }
                    }
                }
            }

            try {
                int nextEndDayDiff = 0;
                if (!nextEnd.isAfter(nextBegin)) {
                    nextEnd = nextEnd.plusDays(1);
                    nextEndDayDiff = 1;
                    LOGGER.info(String.format("[computeNextDelay]set nextEnd=(in 1d)%s", nextEnd));
                }

                if (!computeNewPeriod) {
                    if (now.isAfter(nextBegin)) {
                        if (now.isAfter(nextEnd)) {
                            computeNewPeriod = true;
                        } else {
                            nextBegin = null;
                        }
                    } else {
                        LOGGER.debug(String.format("[computeNextDelay][nextBegin=%s][nextEnd=%s]", nextBegin, nextEnd));
                    }
                }

                if (computeNewPeriod && nextBegin != null) {
                    nextBegin = nextBegin.plusDays(newPeriodDaysDiff);
                    nextEnd = nextEnd.plusDays(newPeriodDaysDiff);
                    storedFirstStart = null;
                    LOGGER.info(String.format("[computeNextDelay]set nextBegin=(in %sd)%s, nextEnd=(in %sd)%s", newPeriodDaysDiff, nextBegin,
                            (newPeriodDaysDiff + nextEndDayDiff), nextEnd));
                }

                now = Instant.now().atZone(service.getConfig().getZoneId());
                if (nextBegin == null) {
                    nextBegin = now.plusSeconds(30);
                    LOGGER.info(String.format("[computeNextDelay]set nextBegin=(in 30s)%s", nextBegin));
                }
                firstStart = storedFirstStart == null ? nextBegin : storedFirstStart;
                nanos = now.until(nextBegin, ChronoUnit.NANOS);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }

            if (nanos > 0) {
                start = now.plusNanos(nanos);
                if (nextEnd != null) {
                    end = nextEnd;
                }
                if (item == null) {
                    insertJocVariable();
                } else {
                    updateJocVariable(item);
                }
                return now.until(start, ChronoUnit.NANOS);
            }
            return nanos;

        } catch (

        Throwable e) {
            throw e;
        } finally {
            dbLayer.close();
        }
    }

    private Period parsePeriodFromDb(String period) {
        Period p = service.getConfig().new Period();
        if (p.parse(period)) {
            return p;
        }
        return service.getConfig().getPeriod();
    }

    private ZonedDateTime parseDateFromDb(String date) {
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
            LOGGER.info("[stored][inserted]" + item.getTextValue());
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
            LOGGER.info("[stored][updated]" + item.getTextValue());
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
            LOGGER.info("[stored][updated]" + item.getTextValue());
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
        return new StringBuilder(service.getConfig().getPeriod().getConfigured()).append(DELIMITER).append(start.toString()).append(DELIMITER).append(
                end.toString()).append(DELIMITER).append(firstStart.toString());
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
        AJocClusterService.setLogger(service.getIdentifier());
        if (factory != null) {
            factory.close();
            factory = null;
        }
        LOGGER.info(String.format("[%s]database factory closed", service.getIdentifier()));
    }

}
