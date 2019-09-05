package com.sos.joc.classes;

import java.io.IOException;
import java.nio.file.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.db.history.DBItemLog;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerObjectNotExistException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;

public class LogTaskContent extends LogContent {

    private Long historyId;
    private String prefix = "sos-%d.task.log-download-";

    public LogTaskContent(Long historyId, DBItemInventoryInstance dbItemInventoryInstance) {
        super();
        this.historyId = historyId;
    }

    public Path writeGzipLogFile() throws SOSHibernateException, JocConfigurationException, DBOpenSessionException, IOException,
            JobSchedulerObjectNotExistException, JocMissingRequiredParameterException {

        if (historyId == null) {
            throw new JocMissingRequiredParameterException("undefined 'taskId'");
        }
        Path path = writeGzipLogFileFromDB();
        // TODO web service call for runnning logs
        if (path == null) {
            path = writeGzipLogFileFromHistoryService();
        }
        if (path == null) {
            String msg = String.format("Task log with id %s is missing", historyId);
            throw new JobSchedulerObjectNotExistException(msg);
        }
        return path;
    }

    private Path writeGzipLogFileFromDB() throws SOSHibernateException, IOException, JocConfigurationException, DBOpenSessionException {
        SOSHibernateSession connection = null;
        try {
            connection = Globals.createSosHibernateStatelessConnection("./task/log");
            DBItemLog historyDBItem = connection.get(DBItemLog.class, historyId);
            if (historyDBItem == null) {
                return null;
            } else {
                return historyDBItem.writeGzipLogFile(String.format(prefix, historyId));
            }
        } finally {
            Globals.disconnect(connection);
        }
    }

    private Path writeGzipLogFileFromHistoryService() {
        // TODO Auto-generated method stub
        return null;
    }

}
