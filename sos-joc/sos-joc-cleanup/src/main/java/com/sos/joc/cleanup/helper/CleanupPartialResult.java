package com.sos.joc.cleanup.helper;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.query.NativeQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.cleanup.model.CleanupTaskModel;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;

public class CleanupPartialResult {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupPartialResult.class);

    // private static final long MAX_RUNS = 10_000;//*1000 - 10.000.000
    private static final int LOG_AFTER_N_RUNS = 10;

    private final String table;

    private JocServiceTaskAnswerState state;
    private Map<String, Object> parameters = null;
    private long deletedTotal;
    private int deletedLast;

    public CleanupPartialResult(String table) {
        this.table = table;
    }

    public void run(CleanupTaskModel task, StringBuilder deleteSQL) throws SOSHibernateException {
        run(task, deleteSQL, null);
    }

    public void run(CleanupTaskModel task, StringBuilder deleteSQL, Long maxMainParentId) throws SOSHibernateException {

        int runCounter = 0;
        long lastRunsDeleted = 0;
        String logPrefix = "[" + task.getIdentifier() + "]";
        if (maxMainParentId != null) {
            logPrefix += "[maxMainParentId=" + maxMainParentId + "]";
        }

        boolean doRun = true;
        state = JocServiceTaskAnswerState.UNCOMPLETED;
        while (doRun) {
            if (task.isStopped()) {
                return;
            }

            task.tryOpenSession();
            NativeQuery<?> query = task.getDbLayer().getSession().createNativeQuery(deleteSQL.toString());
            if (parameters != null) {
                parameters.entrySet().forEach(e -> {
                    query.setParameter(e.getKey(), e.getValue());
                });
            }

            try {
                task.getDbLayer().beginTransaction();
                addDeletedLast(task.getDbLayer().getSession().executeUpdate(query));
                task.getDbLayer().commit();
            } catch (Throwable e) {
                if (task.isStopped()) {
                    LOGGER.info("[" + task.getIdentifier() + "][STOPPED]" + e);
                    return;
                } else {
                    throw e;
                }
            }

            if (getDeletedLast() == 0) {
                state = JocServiceTaskAnswerState.COMPLETED;
                if (deletedTotal > 0) {
                    int r = runCounter + 1;
                    LOGGER.info(logPrefix + "[run=" + r + "]" + task.getDeleted(table, lastRunsDeleted, getDeletedTotal()).toString());
                }
                return;
            }

            lastRunsDeleted += getDeletedLast();
            if (runCounter % LOG_AFTER_N_RUNS == 0) {
                int r = runCounter + 1;
                LOGGER.info(logPrefix + "[run=" + r + "]" + task.getDeleted(table, lastRunsDeleted, getDeletedTotal()).toString());
                lastRunsDeleted = 0;
            }
            runCounter++;
        }
    }

    public JocServiceTaskAnswerState getState() {
        return state;
    }

    public void setParameters(Map<String, Object> val) {
        parameters = val;
    }

    public void addParameter(String name, Object val) {
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        parameters.put(name, val);
    }

    public long getDeletedTotal() {
        return deletedTotal;
    }

    public int getDeletedLast() {
        return deletedLast;
    }

    private void addDeletedLast(int val) {
        deletedLast = Math.abs(val);
        deletedTotal += deletedLast;
    }

}
