package com.sos.joc.cleanup.model;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.cleanup.CleanupServiceTask.TaskDateTime;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;

public class CleanupTaskUserProfiles extends CleanupTaskModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskUserProfiles.class);

    public CleanupTaskUserProfiles(JocClusterHibernateFactory factory, int batchSize, String identifier) {
        super(factory, batchSize, identifier);
    }

    /** datetimes - see:<br/>
     * com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsCleanup.userLastLoginAge<br/>
     * com.sos.joc.cleanup.CleanupServiceConfiguration.userLastLoginAge<br/>
     */
    @Override
    public JocServiceTaskAnswerState cleanup(List<TaskDateTime> datetimes) throws Exception {
        try {
            TaskDateTime datetime = datetimes.get(0);
            LOGGER.info(String.format("[%s][%s][%s]start cleanup", getIdentifier(), datetime.getAge().getConfigured(), datetime.getZonedDatetime()));

            return cleanupUserProfiles(datetime);
        } catch (Throwable e) {
            getDbLayer().rollback();
            throw e;
        } finally {
            close();
        }
    }

    private JocServiceTaskAnswerState cleanupUserProfiles(TaskDateTime datetime) throws Exception {
        if (datetime != null) {
            // the tryOpenSession() ... getDbLayer().close() block can be repeated if necessary to avoid leaving a session open for a long time ...
            tryOpenSession();
            // do select/cleanup
            // where xxx < datetime.getDatetime()
            getDbLayer().close();
        }
        return JocServiceTaskAnswerState.COMPLETED;
    }

}
