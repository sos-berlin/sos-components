package com.sos.joc.classes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.StreamingOutput;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.db.history.DBItemLog;
import com.sos.jobscheduler.db.history.DBItemOrderStep;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.job.TaskFilter;

public class LogTaskContent extends LogContent {

    //private String jobschedulerId;
    private Long historyId;
    private Long eventId = null;
    private String jobName;
    private Long unCompressedLength = null;
    private boolean complete = false;

    public LogTaskContent(TaskFilter taskFilter) {
        super();
        this.historyId = taskFilter.getTaskId();
        //this.jobschedulerId = taskFilter.getJobschedulerId();
        this.eventId = taskFilter.getEventId();
    }
    
    public Long getUnCompressedLength() {
        return unCompressedLength;
    }
    
    public String getJobName() {
        return jobName;
    }
    
    public boolean isComplete() {
        return complete;
    }

    public StreamingOutput getStreamOutput() throws JocMissingRequiredParameterException, JocConfigurationException, DBOpenSessionException,
            SOSHibernateException, DBMissingDataException, JobSchedulerInvalidResponseDataException {
        if (historyId == null) {
            throw new JocMissingRequiredParameterException("undefined 'taskId'");
        }
        StreamingOutput out = null;
        byte[] compressedLog = null;
        if (eventId != null) {
            compressedLog = getLogRollingFromHistoryService();
        } else {
            compressedLog = getLogFromDb();
            if (compressedLog == null) {
                compressedLog = getLogFromHistoryService();
            }
        }
        if (compressedLog != null) {
            final InputStream inStream = new ByteArrayInputStream(compressedLog);
            out = new StreamingOutput() {

                @Override
                public void write(OutputStream output) throws IOException {
                    try {
                        byte[] buffer = new byte[4096];
                        int length;
                        while ((length = inStream.read(buffer)) > 0) {
                            output.write(buffer, 0, length);
                        }
                        output.flush();
                    } finally {
                        try {
                            output.close();
                        } catch (Exception e) {
                        }
                    }
                }
            };
        }
        if (out == null) {
            throw new JobSchedulerInvalidResponseDataException(String.format("Task Log (Id:%d) not found", historyId));
        }
        
        compressedLog = null;
        return out;
    }

    private byte[] getLogRollingFromHistoryService() {
        // TODO
        String s = "Running log is not yet implemented";
        complete = true;
        return s.getBytes(StandardCharsets.UTF_8);
    }

    private byte[] getLogFromHistoryService() {
        // TODO
        String s = "Snapshot log is not yet implemented";
        complete = true;
        return s.getBytes(StandardCharsets.UTF_8);
    }

    private byte[] getLogFromDb() throws JocConfigurationException, DBOpenSessionException, SOSHibernateException, DBMissingDataException {
        SOSHibernateSession connection = null;
        try {
            connection = Globals.createSosHibernateStatelessConnection("./task/log");
            DBItemOrderStep historyOrderStepItem = connection.get(DBItemOrderStep.class, historyId);
            if (historyOrderStepItem == null) {
                throw new DBMissingDataException(String.format("Task (Id:%d) not found", historyId));
            }
            jobName = historyOrderStepItem.getJobName();
            if (historyOrderStepItem.getLogId() == 0L) {
                if (historyOrderStepItem.getEndTime() == null) {
                    //Task is running
                    return null;
                } else {
                    throw new DBMissingDataException(String.format("The log of the job %s (task id:%d) doesn't found", jobName, historyId));
                }
            } else {
                DBItemLog historyDBItem = connection.get(DBItemLog.class, historyOrderStepItem.getLogId());
                if (historyDBItem == null) {
                    throw new DBMissingDataException(String.format("The log of the job %s (task id:%d) doesn't found", jobName, historyId));
                } else {
                    unCompressedLength = historyDBItem.getFileSizeUncomressed();
                    complete = true;
                    return historyDBItem.getFileCompressed();
                }
            }
        } finally {
            Globals.disconnect(connection);
        }
    }

}
