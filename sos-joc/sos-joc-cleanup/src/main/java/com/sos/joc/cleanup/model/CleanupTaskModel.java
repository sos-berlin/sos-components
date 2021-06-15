package com.sos.joc.cleanup.model;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.joc.cleanup.CleanupServiceTask.TaskDateTime;
import com.sos.joc.cleanup.db.DBLayerCleanup;
import com.sos.joc.cluster.IJocClusterService;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer.JocServiceAnswerState;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;

public class CleanupTaskModel implements ICleanupTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskModel.class);

    public enum TaskType {
        SERVICE_TASK, MANUAL_TASK
    }

    protected static final int WAIT_INTERVAL_ON_BUSY = 65;
    protected static final int WAIT_INTERVAL_ON_ERROR = 60;

    private final JocClusterHibernateFactory factory;
    private final DBLayerCleanup dbLayer;
    private final IJocClusterService service;
    private final int batchSize;
    private final TaskType type;
    private final String identifier;
    private final Object lock = new Object();

    private JocServiceTaskAnswerState state = null;
    private AtomicBoolean stopped = new AtomicBoolean(false);

    protected CleanupTaskModel(JocClusterHibernateFactory factory, int batchSize, String identifier) {
        this(factory, null, batchSize, identifier);
    }

    protected CleanupTaskModel(JocClusterHibernateFactory factory, IJocClusterService service, int batchSize) {
        this(factory, service, batchSize, null);
    }

    private CleanupTaskModel(JocClusterHibernateFactory factory, IJocClusterService service, int batchSize, String identifier) {
        this.factory = factory;
        this.service = service;
        this.batchSize = batchSize;
        if (this.service == null) {
            this.type = TaskType.MANUAL_TASK;
            this.identifier = identifier;
        } else {
            this.type = TaskType.SERVICE_TASK;
            this.identifier = service.getIdentifier();
        }
        this.dbLayer = new DBLayerCleanup(this.identifier);
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
        state = JocServiceTaskAnswerState.UNCOMPLETED;
        stopped.set(false);

        boolean run = true;
        while (run) {
            try {
                if (isStopped()) {
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
    public JocServiceTaskAnswer stop() {
        stopped.set(true);

        synchronized (lock) {
            lock.notifyAll();
        }
        return new JocServiceTaskAnswer(state);
    }

    @Override
    public JocServiceTaskAnswerState getState() {
        return state;
    }

    @Override
    public boolean isStopped() {
        return stopped.get();
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getTypeName() {
        return type == null ? "null" : type.name().toLowerCase();
    }

    public JocServiceTaskAnswerState cleanup(List<TaskDateTime> datetimes) throws Exception {
        return state;
    }

    public JocServiceTaskAnswerState cleanup(int counter) throws Exception {
        return state;
    }

    public TaskType getType() {
        return type;
    }

    public void setState(JocServiceTaskAnswerState val) {
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

    public IJocClusterService getService() {
        return service;
    }

    protected boolean askService() {
        if (this.type.equals(TaskType.SERVICE_TASK)) {
            JocServiceAnswer info = getService().getInfo();
            boolean rc = info.getState().equals(JocServiceAnswerState.RELAX);
            if (!rc) {
                LOGGER.info(String.format("[%s][ask service]%s", identifier, SOSString.toString(info)));
            }
            return rc;
        }
        return true;
    }

    protected void waitFor(int interval) {
        if (!stopped.get() && interval > 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[wait]%ss ...", interval));
            }
            try {
                synchronized (lock) {
                    lock.wait(interval * 1_000);
                }
            } catch (InterruptedException e) {
                if (stopped.get()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("[wait]sleep interrupted due to task stop");
                    }
                } else {
                    LOGGER.warn(String.format("[wait]%s", e.toString()), e);
                }
            }
        }
    }

    protected StringBuilder getDeleted(String table, int current, int total) {
        return new StringBuilder("[").append(table).append("=").append(current).append(" total=").append(total).append("]");
    }

}
