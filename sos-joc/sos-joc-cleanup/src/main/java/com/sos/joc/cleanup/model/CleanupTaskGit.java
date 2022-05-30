package com.sos.joc.cleanup.model;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.cleanup.CleanupServiceTask.TaskDateTime;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;

public class CleanupTaskGit extends CleanupTaskModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskGit.class);

    public CleanupTaskGit(JocClusterHibernateFactory factory, int batchSize, String identifier) {
        super(factory, batchSize, identifier);
    }

    /** Git cleanup is datetimes independent */
    @Override
    public JocServiceTaskAnswerState cleanup(List<TaskDateTime> datetimes) throws Exception {
        try {
            LOGGER.info(String.format("[%s]start cleanup", getIdentifier()));
            return cleanupGit();
        } catch (Throwable e) {
            getDbLayer().rollback();
            throw e;
        } finally {
            close();
        }

    }

    private JocServiceTaskAnswerState cleanupGit() throws Exception {
        // the tryOpenSession() ... getDbLayer().close() block can be repeated if necessary to avoid leaving a session open for a long time ...
        tryOpenSession();
        // do select
        getDbLayer().close();

        // do cleanup "local" repositories

        if (isStopped()) { // the cleanup service was stopped because the execution time window was exceeded
            return JocServiceTaskAnswerState.UNCOMPLETED;
        }

        // do cleanup "rollout" repositories

        return JocServiceTaskAnswerState.COMPLETED;
    }

}
