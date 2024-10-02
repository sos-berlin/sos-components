package com.sos.joc.cleanup;

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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.cleanup.CleanupServiceConfiguration.Period;
import com.sos.joc.cleanup.db.DBLayerCleanup;
import com.sos.joc.cleanup.exception.CleanupComputeException;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.db.joc.DBItemJocVariable;
import com.sos.joc.model.cluster.common.state.JocClusterState;

public class CleanupServiceSchedule {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupServiceSchedule.class);

    private static final String DELIMITER = "->";
    /** seconds */
    private static final int MAX_AWAIT_TERMINATION_TIMEOUT_ON_PERIOD_REACHED = 5 * 60;
    private static final long DEFAULT_RUN_SERVICE_NOW_TIMEOUT = 4 * 60 * 60;

    private final CleanupService service;
    private final CleanupServiceTask task;

    private ScheduledExecutorService threadPool = null;
    private ScheduledFuture<JocClusterAnswer> resultFuture = null;
    private DBItemJocVariable item = null;
    private ZonedDateTime firstStart = null;
    private ZonedDateTime start = null;
    private ZonedDateTime end = null;
    private List<String> uncompleted = null;
    private AtomicBoolean isBusy = new AtomicBoolean(false);
    private String logPrefix = null;

    public CleanupServiceSchedule(CleanupService service) {
        this.service = service;
        this.task = new CleanupServiceTask(this);
    }

    private void setLogPrefix(StartupMode mode) {
        logPrefix = "[" + service.getIdentifier() + "][" + mode + "]";
    }

    public void start(StartupMode mode, boolean runNow) throws Exception {
        if (runNow) {
            setBusy(true);
            service.setRunServiceNow("start", false);
        }
        task.setStartMode(mode);
        setLogPrefix(mode);
        try {
            LOGGER.info(String.format("%s[runNow=%s]%s", logPrefix, runNow, getService().getConfig().toString()));
            long delay = runNow ? computeRunNowDelay() : computeNextDelay(mode);
            if (delay > 0) {
                long timeout = computeTimeout();
                JocClusterAnswer answer = schedule(delay, timeout);
                LOGGER.info(SOSString.toString(answer));
                updateJocVariableOnResult(answer, runNow);
            } else {
                throw new CleanupComputeException("[invalid delay=" + delay + "]delay must be greater than 0");
            }
        } catch (TimeoutException e) {
            LOGGER.info(String.format("%s[max end at %s reached][max await termination timeout=%ss]try stop..", logPrefix, end.toString(),
                    MAX_AWAIT_TERMINATION_TIMEOUT_ON_PERIOD_REACHED));
            closeTasks(MAX_AWAIT_TERMINATION_TIMEOUT_ON_PERIOD_REACHED, runNow);
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error(e.toString(), e);
            closeTasks();
        } catch (CancellationException e) {
        } catch (CleanupComputeException e) {
            closeTasks();
            throw e;
        } finally {
            if (runNow) {
                setBusy(false);
            }
        }
    }

    public void runNow(StartupMode mode) throws Exception {
        setBusy(true);
        service.setRunServiceNow("runNow", true);

        task.setStartMode(mode);
        setLogPrefix(mode);
        try {
            closeTasksForRunNow();
        } catch (Throwable e) {
            service.setRunServiceNow("runNow", false);
            setBusy(false);

            throw e;
        }
    }

    protected boolean isBusy() {
        return isBusy.get();
    }

    private JocClusterAnswer schedule(long delay, long timeout) throws Exception {
        closeTasks();
        CleanupService.setServiceLogger();
        threadPool = Executors.newScheduledThreadPool(1, new JocClusterThreadFactory(service.getThreadGroup(), service.getIdentifier() + "-t"));
        CleanupService.setServiceLogger();
        LOGGER.info(String.format("%s[schedule][begin=%s][max end=%s][timeout=%s] ...", logPrefix, start.toString(), end.toString(), SOSDate
                .getDuration(Duration.ofSeconds(timeout))));
        resultFuture = threadPool.schedule(task, delay, TimeUnit.NANOSECONDS);
        return resultFuture.get(timeout, TimeUnit.SECONDS);
    }

    private long computeRunNowDelay() {
        uncompleted = null;

        long diff = DEFAULT_RUN_SERVICE_NOW_TIMEOUT;
        try {
            int pb = service.getConfig().getPeriod().getBegin().asSeconds();
            int pe = service.getConfig().getPeriod().getEnd().asSeconds();
            diff = Math.abs(pe - pb);
        } catch (Throwable e) {
            LOGGER.info(logPrefix + "[computeRunNowDelay]" + e.toString(), e);
        }
        if (service.getConfig().getForceCleanup().force()) {
            if (service.getConfig().getForceCleanup().getHistoryPauseDurationAge().getSeconds() > diff) {
                diff = service.getConfig().getForceCleanup().getHistoryPauseDurationAge().getSeconds() + 5;
            }
        }

        ZonedDateTime now = service.getNow();
        start = now.plusSeconds(1);
        end = now.plusSeconds(diff);
        firstStart = start;
        return now.until(start, ChronoUnit.NANOS);
    }

    private long computeNextDelay(StartupMode mode) throws Exception {
        CleanupService.setServiceLogger();
        setLogPrefix(mode);
        uncompleted = null;

        String method = "computeNextDelay";
        DBLayerCleanup dbLayer = new DBLayerCleanup(service.getIdentifier());
        try {
            dbLayer.setSession(Globals.createSosHibernateStatelessConnection(service.getIdentifier()));

            switch (mode) {
            case manual:
            case manual_restart:
            case settings_changed:
                deleteJocVariable(method, dbLayer, mode);
                break;
            default:
                item = dbLayer.getVariable(service.getIdentifier());
                break;

            }

            Period storedPeriod = null;
            ZonedDateTime storedFirstStart = null;
            ZonedDateTime storedNextBegin = null;
            ZonedDateTime storedNextEnd = null;
            JocClusterState storedState = null;

            Period period = service.getConfig().getPeriod();
            List<Integer> weekDays = service.getConfig().getPeriod().getWeekDays();
            ZonedDateTime nextEnd = null;
            ZonedDateTime nextBegin = null;

            long nanos = 0;
            ZonedDateTime now = service.getNow(); // Instant.now().atZone(service.getConfig().getZoneId());
            boolean computeNewPeriod = false;
            if (item != null) {
                String[] arr = item.getTextValue().split(DELIMITER);
                if (arr.length > 3) {
                    storedPeriod = parsePeriodFromDb(arr[0].trim());
                    storedNextBegin = parseDateFromDb(dbLayer, mode, arr[1].trim());
                    storedNextEnd = parseDateFromDb(dbLayer, mode, arr[2].trim());
                    storedFirstStart = parseDateFromDb(dbLayer, mode, arr[3].trim());

                    LOGGER.info(String.format("%s[%s][stored=%s][storedPeriod=%s]", logPrefix, method, item.getTextValue(), storedPeriod));

                    if (storedNextBegin != null && storedNextEnd != null) {
                        storedState = JocClusterState.RESTARTED;
                        if (storedNextBegin != null && arr.length > 4) {
                            String state = arr[4];
                            if (state.equalsIgnoreCase(JocClusterState.COMPLETED.name())) {
                                storedState = JocClusterState.COMPLETED;
                            } else {
                                if (state.startsWith(JocClusterState.UNCOMPLETED.name())) {
                                    storedState = JocClusterState.UNCOMPLETED;
                                    arr = state.split("=");// UNCOMPLETED=dayliplan,history
                                    if (arr.length > 1) {
                                        uncompleted = Stream.of(arr[1].split(",", -1)).collect(Collectors.toList());
                                    }
                                }
                            }
                        }
                        if (now.isAfter(storedNextBegin)) {
                            if (storedState.equals(JocClusterState.COMPLETED)) {
                                computeNewPeriod = true;
                                LOGGER.info(String.format("%s[%s][%s]compute next period...", logPrefix, method, storedState));
                            } else {
                                if (storedState.equals(JocClusterState.UNCOMPLETED)) {
                                    LOGGER.info(String.format("%s[%s][%s][stored][skip]now(%s) is after the storedNextBegin(%s)", logPrefix, method,
                                            storedState, now, storedNextBegin));
                                } else {
                                    LOGGER.info(String.format("%s[%s][stored][skip]now(%s) is after the storedNextBegin(%s)", logPrefix, method, now,
                                            storedNextBegin));
                                }
                                storedFirstStart = null;
                                storedNextBegin = null;
                                storedNextEnd = null;
                            }
                        }
                        if (storedNextBegin != null) {
                            if (weekDays.contains(Integer.valueOf(storedNextBegin.getDayOfWeek().getValue()))) {
                                int storedNextDayOfWeek = storedNextBegin.getDayOfWeek().getValue();
                                int nowDayOfWeek = now.getDayOfWeek().getValue();
                                for (Integer weekDay : weekDays) {
                                    if (weekDay.intValue() < nowDayOfWeek) {
                                        continue;
                                    }
                                    if (weekDay.intValue() == nowDayOfWeek) {
                                        if (!storedPeriod.getWeekDays().contains(Integer.valueOf(nowDayOfWeek))) {
                                            LOGGER.info(String.format("%s[%s][stored][skip]period was changed - today added (old=%s, new=%s)",
                                                    logPrefix, method, storedPeriod.getConfigured(), period.getConfigured()));
                                            storedFirstStart = null;
                                            storedNextBegin = null;
                                            storedNextEnd = null;
                                            break;
                                        }
                                    } else if (weekDay.intValue() < storedNextDayOfWeek) {
                                        LOGGER.info(String.format("%s[%s][stored][skip]period was changed - weekday(%s) added (old=%s, new=%s)",
                                                logPrefix, method, weekDay, storedPeriod.getConfigured(), period.getConfigured()));
                                        storedFirstStart = null;
                                        storedNextBegin = null;
                                        storedNextEnd = null;
                                        break;
                                    }
                                }
                            } else {
                                LOGGER.info(String.format("%s[%s][stored][skip]stored dayOfWeek(%s) is no more configured", logPrefix, method,
                                        storedNextBegin.getDayOfWeek()));
                                storedFirstStart = null;
                                storedNextBegin = null;
                                storedNextEnd = null;
                            }
                        }
                    }
                } else {
                    LOGGER.info(String.format("%s[%s][stored]%s", logPrefix, method, item.getTextValue()));
                    LOGGER.info(String.format("%s[%s][stored][skip]old format", logPrefix, method));
                }
            }

            if (storedNextBegin != null) {
                storedFirstStart = storedFirstStart.withHour(period.getBegin().getHours()).withMinute(period.getBegin().getMinutes()).withSecond(
                        period.getBegin().getSeconds()).withNano(0);

                nextBegin = storedNextBegin.withHour(period.getBegin().getHours()).withMinute(period.getBegin().getMinutes()).withSecond(period
                        .getBegin().getSeconds());
                nextEnd = storedNextEnd.withHour(period.getEnd().getHours()).withMinute(period.getEnd().getMinutes()).withSecond(period.getEnd()
                        .getSeconds());
                LOGGER.info(String.format("%s[%s][use stored][begin=%s, end=%s]", logPrefix, method, nextBegin, nextEnd));
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
                if (newPeriodDaysDiff == -1) {
                    for (Integer wd : weekDays) {
                        if (wd < nwd) {
                            newPeriodDaysDiff = (7 - nwd) + wd;
                            break;
                        }
                    }
                }
                ZonedDateTime nowZINextBegin = ZonedDateTime.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), period.getBegin().getHours(),
                        period.getBegin().getMinutes(), period.getBegin().getSeconds(), 0, service.getConfig().getZoneId());
                ZonedDateTime nowZINextEnd = ZonedDateTime.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), period.getEnd().getHours(),
                        period.getEnd().getMinutes(), period.getEnd().getSeconds(), 0, service.getConfig().getZoneId());

                // nextBegin = ZonedDateTime.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth() + newPeriodDaysDiff, period.getBegin()
                // .getHours(), period.getBegin().getMinutes(), period.getBegin().getSeconds(), 0, service.getConfig().getZoneId());
                // nextEnd = ZonedDateTime.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth() + newPeriodDaysDiff, period.getEnd().getHours(),
                // period.getEnd().getMinutes(), period.getEnd().getSeconds(), 0, service.getConfig().getZoneId());

                nextBegin = nowZINextBegin.plusDays(newPeriodDaysDiff);
                nextEnd = nowZINextEnd.plusDays(newPeriodDaysDiff);

                if (now.isAfter(nextBegin)) {
                    newPeriodDaysDiff = -1;
                    nwd = now.getDayOfWeek().getValue();
                    for (Integer wd : weekDays) {
                        if (wd > nwd) {
                            newPeriodDaysDiff = wd - nwd;
                            break;
                        }
                    }
                    if (newPeriodDaysDiff == -1) {
                        for (Integer wd : weekDays) {
                            if (wd < nwd) {
                                newPeriodDaysDiff = (7 - nwd) + wd;
                                break;
                            }
                        }
                    }
                    if (newPeriodDaysDiff == -1) {
                        newPeriodDaysDiff = 7;
                    }
                    // if (newPeriodDaysDiff > 0) {
                    // nextBegin.plusDays(newPeriodDaysDiff);
                    // nextEnd.plusDays(newPeriodDaysDiff);
                    // }
                }
                LOGGER.info(String.format("%s[%s][weekdays][newPeriodDaysDiff=%s][nextBegin=%s][nextEnd=%s]", logPrefix, method, newPeriodDaysDiff,
                        nextBegin, nextEnd));
            } else {
                if (computeNewPeriod) {
                    newPeriodDaysDiff = -1;
                    int nwd = now.getDayOfWeek().getValue();
                    for (Integer wd : weekDays) {
                        if (wd > nwd) {
                            newPeriodDaysDiff = wd - nwd;
                            break;
                        }
                    }
                    if (newPeriodDaysDiff == -1) {
                        for (Integer wd : weekDays) {
                            if (wd < nwd) {
                                newPeriodDaysDiff = (7 - nwd) + wd;
                                break;
                            }
                        }
                    }
                    if (newPeriodDaysDiff == -1) {
                        newPeriodDaysDiff = 7;
                    }
                }
            }

            try {
                int nextEndDayDiff = 0;
                if (!nextEnd.isAfter(nextBegin)) {
                    nextEnd = nextEnd.plusDays(1);
                    nextEndDayDiff = 1;
                    LOGGER.info(String.format("%s[%s]set nextEnd=(in 1d)%s", logPrefix, method, nextEnd));
                }

                if (!computeNewPeriod) {
                    if (now.isAfter(nextBegin)) {
                        if (now.isAfter(nextEnd)) {
                            computeNewPeriod = true;
                        } else {
                            nextBegin = null;
                        }
                    } else {
                        LOGGER.debug(String.format("%s[%s][nextBegin=%s][nextEnd=%s]", logPrefix, method, nextBegin, nextEnd));
                    }
                }

                if (computeNewPeriod && nextBegin != null) {
                    nextBegin = nextBegin.plusDays(newPeriodDaysDiff);
                    nextEnd = nextEnd.plusDays(newPeriodDaysDiff);
                    storedFirstStart = null;
                    LOGGER.info(String.format("%s[%s]set nextBegin=(in %sd)%s, nextEnd=(in %sd)%s", logPrefix, method, newPeriodDaysDiff, nextBegin,
                            (newPeriodDaysDiff + nextEndDayDiff), nextEnd));
                }

                now = Instant.now().atZone(service.getConfig().getZoneId());
                if (nextBegin == null) {
                    nextBegin = now.plusSeconds(30);
                    LOGGER.info(String.format("%s[%s]set nextBegin=(in 30s)%s", logPrefix, method, nextBegin));
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
                    insertJocVariable(method, dbLayer);
                } else {
                    updateJocVariable(method, dbLayer, item);
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

    private Period parsePeriodFromDb(String period) {
        Period p = service.getConfig().new Period();
        if (p.parse(period)) {
            return p;
        }
        return service.getConfig().getPeriod();
    }

    private ZonedDateTime parseDateFromDb(DBLayerCleanup dbLayer, StartupMode mode, String date) {
        try {
            return ZonedDateTime.parse(date, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        } catch (Throwable e) {
            try {
                deleteJocVariable("parseDateFromDb", dbLayer, mode);
            } catch (Exception e1) {
                LOGGER.error(String.format("[%s]%s", date, e.toString()), e);
            }
            LOGGER.warn(String.format("%s[%s]%s", logPrefix, date, e.toString()));
            return null;
        }
    }

    private long computeTimeout() {
        ZonedDateTime now = Instant.now().atZone(service.getConfig().getZoneId());
        return now.until(end, ChronoUnit.SECONDS);
    }

    private void closeTasksForRunNow() {
        closeTasks(-1, true);
    }

    private void closeTasks() {
        closeTasks(-1, false);
    }

    private synchronized void closeTasks(int timeout, boolean runNow) {
        JocClusterAnswer answer = task.stop(timeout);
        if (answer != null && answer.getState().equals(JocClusterState.UNCOMPLETED)) {
            try {
                updateJocVariableOnResult(answer, runNow);
            } catch (Throwable e) {
                LOGGER.error(e.toString(), e);
            }
        }

        if (resultFuture != null) {
            resultFuture.cancel(false);// without interruption
            CleanupService.setServiceLogger();
            LOGGER.info(String.format("%s[closeTasks][runNow=%s]previous schedule cancelled", logPrefix, runNow));
        }
        if (threadPool != null) {
            CleanupService.setServiceLogger();
            JocCluster.shutdownThreadPool(logPrefix, threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);
            threadPool = null;
        }
        resultFuture = null;
    }

    public void stop(StartupMode mode) {
        task.setStartMode(mode);

        closeTasks();
    }

    private void deleteJocVariable(String caller, DBLayerCleanup dbLayer, StartupMode mode) throws Exception {
        try {
            DBItemJocVariable jv = dbLayer.getVariable(service.getIdentifier());
            if (jv != null) {
                dbLayer.beginTransaction();
                dbLayer.getSession().delete(jv);
                dbLayer.commit();
                LOGGER.info(String.format("%s[%s][deleted]because %s", logPrefix, caller, mode));
            }
            item = null;
        } catch (Exception e) {
            dbLayer.rollback();
            throw e;
        }
    }

    private DBItemJocVariable insertJocVariable(String caller, DBLayerCleanup dbLayer) throws Exception {
        return insertJocVariable(caller, dbLayer, getInitialValue().toString());
    }

    private DBItemJocVariable insertJocVariable(String caller, DBLayerCleanup dbLayer, String value) throws Exception {
        try {
            dbLayer.beginTransaction();
            DBItemJocVariable item = dbLayer.insertVariable(service.getIdentifier(), value);
            dbLayer.commit();
            LOGGER.info(String.format("%s[%s][inserted]%s", logPrefix, caller, item.getTextValue()));
            return item;
        } catch (Exception e) {
            dbLayer.rollback();
            throw e;
        }
    }

    private DBItemJocVariable updateJocVariable(String caller, DBLayerCleanup dbLayer, DBItemJocVariable item) throws Exception {
        return updateJocVariable(caller, dbLayer, item, getInitialValue().toString());
    }

    private DBItemJocVariable updateJocVariable(String caller, DBLayerCleanup dbLayer, DBItemJocVariable item, String value) throws Exception {
        try {
            item.setTextValue(value);

            dbLayer.beginTransaction();
            dbLayer.getSession().update(item);
            dbLayer.commit();
            LOGGER.info(String.format("%s[%s][updated]%s", logPrefix, caller, item.getTextValue()));
            return item;
        } catch (Exception e) {
            dbLayer.rollback();
            throw e;
        }
    }

    private void updateJocVariableOnResult(JocClusterAnswer answer, boolean runNow) throws Exception {
        String method = "updateJocVariableOnResult";
        if (runNow) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("%s[%s][skip store]runNow=true", logPrefix, method));
            }
            return;
        }

        if (answer == null || answer.getState().equals(JocClusterState.STOPPED)) {
            LOGGER.info(String.format("%s[%s][skip store]STOPPED", logPrefix, method));
            return;
        }

        StringBuilder state = new StringBuilder(answer.getState().name());
        if (!SOSString.isEmpty(answer.getMessage())) {
            state.append("=").append(answer.getMessage());
        }

        DBLayerCleanup dbLayer = new DBLayerCleanup(service.getIdentifier());
        try {
            dbLayer.setSession(Globals.createSosHibernateStatelessConnection(service.getIdentifier()));
            String val = getInitialValue().append(DELIMITER).append(state).toString();
            if (item == null) {
                item = dbLayer.getVariable(service.getIdentifier());
                if (item == null) {
                    item = insertJocVariable(method, dbLayer, val);
                    val = null;
                }
            }
            if (val != null) {
                item = updateJocVariable(method, dbLayer, item, val);
            }
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
        return uncompleted;
    }

    protected void setBusy(boolean val) {
        isBusy.set(val);
    }

    private StringBuilder getInitialValue() {
        return new StringBuilder(service.getConfig().getPeriod().getConfigured()).append(DELIMITER).append(start.toString()).append(DELIMITER).append(
                end.toString()).append(DELIMITER).append(firstStart.toString());
    }

}
