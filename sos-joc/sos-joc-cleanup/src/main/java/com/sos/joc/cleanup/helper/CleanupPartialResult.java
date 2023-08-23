package com.sos.joc.cleanup.helper;

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
    private long deletedTotal;
    private int deletedLast;

    public CleanupPartialResult(String table) {
        this.table = table;
    }

    public void run(CleanupTaskModel task, StringBuilder deleteSQL, Long maxMainParentId) throws SOSHibernateException {

        int runCounter = 0;
        long lastRunsDeleted = 0;
        String logPrefix = "[" + task.getIdentifier() + "][maxMainParentId=" + maxMainParentId + "]";

        boolean doRun = true;
        while (doRun) {
            if (task.isStopped()) {
                setState(JocServiceTaskAnswerState.UNCOMPLETED);
                return;
            }

            task.tryOpenSession();

            task.getDbLayer().beginTransaction();
            addDeletedLast(task.getDbLayer().getSession().executeUpdate(task.getDbLayer().getSession().createNativeQuery(deleteSQL.toString())));
            task.getDbLayer().commit();

            if (getDeletedLast() == 0) {
                LOGGER.info(logPrefix + task.getDeleted(table, lastRunsDeleted, getDeletedTotal()).toString());
                return;
            }

            lastRunsDeleted += getDeletedLast();
            if (runCounter % LOG_AFTER_N_RUNS == 0) {
                LOGGER.info(logPrefix + task.getDeleted(table, lastRunsDeleted, getDeletedTotal()).toString());
                lastRunsDeleted = 0;
            }
            runCounter++;
        }
    }

    public JocServiceTaskAnswerState getState() {
        return state;
    }

    public void setState(JocServiceTaskAnswerState val) {
        state = val;
    }

    public long getDeletedTotal() {
        return deletedTotal;
    }

    public int getDeletedLast() {
        return deletedLast;
    }

    public void addDeletedLast(int val) {
        deletedLast = Math.abs(val);
        deletedTotal += deletedLast;
    }

}
