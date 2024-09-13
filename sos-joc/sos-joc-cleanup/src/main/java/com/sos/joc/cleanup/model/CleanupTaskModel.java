package com.sos.joc.cleanup.model;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.hibernate.SOSHibernateFactory.Dbms;
import com.sos.commons.hibernate.exception.SOSHibernateOpenSessionException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.joc.cleanup.CleanupServiceConfiguration.ForceCleanup;
import com.sos.joc.cleanup.CleanupServiceTask.TaskDateTime;
import com.sos.joc.cleanup.db.DBLayerCleanup;
import com.sos.joc.cleanup.helper.CleanupPauseHandler;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.common.JocClusterServiceActivity;
import com.sos.joc.cluster.service.active.IJocActiveMemberService;
import com.sos.joc.model.cluster.common.state.JocClusterServiceTaskState;

public class CleanupTaskModel implements ICleanupTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskModel.class);

    public enum TaskType {
        SERVICE_TASK, MANUAL_TASK
    }

    /** seconds */
    protected static final int WAIT_INTERVAL_ON_BUSY = 15;
    protected static final int WAIT_INTERVAL_ON_ERROR = 30;
    /** days */
    protected static final int REMAINING_AGE = 2;

    /** seconds */
    private static final int WAIT_INTERVAL_ON_COMPLETING = 1;

    private final JocClusterHibernateFactory factory;
    private final DBLayerCleanup dbLayer;
    private final IJocActiveMemberService service;
    private final int batchSize;
    private final ForceCleanup forceCleanup;
    private final TaskType type;
    private final String identifier;
    private final Object lock = new Object();

    private JocClusterServiceTaskState state = null;
    // private ServicePauseConfig servicePauseConfig = null;
    private CleanupPauseHandler pauseHandler;
    private AtomicBoolean stopped = new AtomicBoolean(false);
    private AtomicBoolean completed = new AtomicBoolean(false);

    // Manual Tasks
    protected CleanupTaskModel(JocClusterHibernateFactory factory, int batchSize, String identifier, ForceCleanup forceCleanup) {
        this(factory, null, batchSize, identifier, forceCleanup);
    }

    // Service Tasks - dailyplan,history,monitoring
    protected CleanupTaskModel(JocClusterHibernateFactory factory, IJocActiveMemberService service, int batchSize, ForceCleanup forceCleanup) {
        this(factory, service, batchSize, null, forceCleanup);
    }

    private CleanupTaskModel(JocClusterHibernateFactory factory, IJocActiveMemberService service, int batchSize, String identifier,
            ForceCleanup forceCleanup) {
        this.factory = factory;
        this.service = service;
        this.batchSize = batchSize;
        this.forceCleanup = forceCleanup;
        if (this.service == null) {
            this.type = TaskType.MANUAL_TASK;
            this.identifier = identifier;
        } else {
            this.type = TaskType.SERVICE_TASK;
            this.identifier = service.getIdentifier();
        }
        this.dbLayer = new DBLayerCleanup(this.identifier);
        this.pauseHandler = new CleanupPauseHandler();
    }

    @Override
    public void start(List<TaskDateTime> datetimes) {
        start(datetimes, -1);
    }

    @Override
    public void start(int counter) {
        start(null, counter);
    }

    private void start(List<TaskDateTime> datetimes, int counter) {
        state = JocClusterServiceTaskState.UNCOMPLETED;
        stopped.set(false);
        completed.set(false);

        boolean run = true;
        while (run) {
            try {
                if (isStopped()) {
                    completed.set(true);
                    return;
                }
                if (askService()) {
                    if (datetimes == null) {
                        setState(cleanup(counter));
                    } else {
                        setState(cleanup(datetimes));
                    }
                    run = false;
                } else {
                    waitFor(WAIT_INTERVAL_ON_BUSY);
                }
            } catch (Throwable e) {
                LOGGER.error(e.toString(), e);
                waitFor(WAIT_INTERVAL_ON_ERROR);
            }
        }
    }

    @Override
    public JocClusterServiceTaskState stop(int maxTimeoutSeconds) {
        stopped.set(true);

        synchronized (lock) {
            lock.notifyAll();
        }

        waitForCompleting(maxTimeoutSeconds);

        if (!completed.get()) {
            if (dbLayer == null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[" + identifier + "][skip dbLayer.terminate()]dbLayer=null");
                }
            } else {
                dbLayer.terminate();
            }
            completed.set(true);
        }
        pauseHandler.stop();

        return state;
    }

    @Override
    public JocClusterServiceTaskState getState() {
        return state;
    }

    @Override
    public boolean isStopped() {
        return stopped.get();
    }

    @Override
    public boolean isCompleted() {
        return completed.get();
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getTypeName() {
        return type == null ? "null" : type.name().toLowerCase();
    }

    public JocClusterServiceTaskState cleanup(List<TaskDateTime> datetimes) throws Exception {
        return state;
    }

    public JocClusterServiceTaskState cleanup(int counter) throws Exception {
        return state;
    }

    protected void close() {
        if (dbLayer != null) {
            try {
                dbLayer.close();
            } catch (Throwable e) {
            }
        }
        completed.set(true);
    }

    public TaskType getType() {
        return type;
    }

    public void setState(JocClusterServiceTaskState val) {
        state = val;
    }

    public JocClusterHibernateFactory getFactory() {
        return factory;
    }

    public DBLayerCleanup getDbLayer() {
        return dbLayer;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public IJocActiveMemberService getService() {
        return service;
    }

    public void tryOpenSession() throws SOSHibernateOpenSessionException {
        if (dbLayer != null && dbLayer.getSession() == null) {
            dbLayer.setSession(factory.openStatelessSession(getIdentifier()));
        }
    }

    protected boolean askService() {
        if (!forceCleanup.force() && TaskType.SERVICE_TASK.equals(this.type)) {
            JocClusterServiceActivity activity = getService().getActivity();
            boolean doCleanup = !activity.isBusy();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][ask service][doCleanup=%s]%s", identifier, doCleanup, SOSString.toString(activity)));
            }
            return doCleanup;
        }
        return true;
    }

    public CleanupPauseHandler getPauseHandler() {
        return pauseHandler;
    }

    protected void waitFor(int interval) {
        if (!stopped.get() && interval > 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][wait]%ss ...", identifier, interval));
            }
            try {
                synchronized (lock) {
                    lock.wait(interval * 1_000);
                }
            } catch (InterruptedException e) {
                if (stopped.get()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("[" + identifier + "][wait]sleep interrupted due to task stop");
                    }
                } else {
                    LOGGER.warn(String.format("[%s][wait]%s", identifier, e.toString()), e);
                }
            }
        }
    }

    private void waitForCompleting(int maxTimeoutSeconds) {
        if (!completed.get()) {
            boolean run = true;
            int counter = 0;
            while (run) {
                try {
                    TimeUnit.SECONDS.sleep(WAIT_INTERVAL_ON_COMPLETING);

                    if (completed.get()) {
                        return;
                    }

                    counter++;
                    if (counter >= maxTimeoutSeconds) {
                        return;
                    }
                } catch (InterruptedException e) {
                    run = false;
                }
            }
        }
    }

    public StringBuilder getDeleted(String table, long current, long total) {
        return new StringBuilder("[").append(table).append("=").append(current).append(" total=").append(total).append("]");
    }

    public ForceCleanup getForceCleanup() {
        return forceCleanup;
    }

    protected String getDateTime(Date date) {
        if (date == null) {
            return "";
        }
        try {
            return SOSDate.getDateTimeAsString(date);
        } catch (SOSInvalidDataException e) {
            return date.toString();
        }
    }

    protected boolean isPGSQL() {
        return factory.getDbms().equals(Dbms.PGSQL);
    }

    protected String getLimitWhere() {
        switch (factory.getDbms()) {
        case MYSQL:
            return "limit " + batchSize;
        case ORACLE:
            return "and ROWNUM <= " + batchSize;
        default:
            return "";
        }
    }

    protected String getLimitTop() {
        if (factory.getDbms().equals(Dbms.MSSQL)) {
            return " top (" + batchSize + ") ";
        }
        return "";
    }

    protected boolean isCompleted(JocClusterServiceTaskState state) {
        return state == null || state.equals(JocClusterServiceTaskState.COMPLETED);
    }

    protected Date getRemainingStartTime(TaskDateTime datetime) {
        return SOSDate.add(datetime.getDatetime(), -1 * REMAINING_AGE, ChronoUnit.DAYS);
    }

    protected String getRemainingAgeInfo(TaskDateTime datetime) {
        return datetime.getAge().getConfigured() + "+" + REMAINING_AGE + "d";
    }

}
