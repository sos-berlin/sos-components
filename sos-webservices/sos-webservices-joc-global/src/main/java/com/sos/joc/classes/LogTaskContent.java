package com.sos.joc.classes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.history.DBItemHistoryLog;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.job.TaskFilter;

public class LogTaskContent {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogTaskContent.class);
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd' 'HH:mm:ss.SSSZ");
    // private String jobschedulerId;
    private Long historyId;
    private Long orderId;
    private String jobName;
    private String workflow;
    private Long unCompressedLength = null;
    private Long eventId = null;
    private boolean complete = false;

    public LogTaskContent(TaskFilter taskFilter) {
        this.historyId = taskFilter.getTaskId();
        // this.jobschedulerId = taskFilter.getJobschedulerId();
    }

    public LogTaskContent(Long taskId) {
        this.historyId = taskId;
    }

    public Map<String, Object> getHeaders() {
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Access-Control-Expose-Headers", "X-Log-Complete,X-Log-Event-Id,X-Uncompressed-Length");
        headers.put("X-Log-Complete", complete);
        if (unCompressedLength != null) {
            headers.put("X-Uncompressed-Length", unCompressedLength);
        }
        if (eventId != null) {
            headers.put("X-Log-Event-Id", eventId);
        }
        return headers;
    }

    public Long getUnCompressedLength() {
        return unCompressedLength;
    }

    public boolean isComplete() {
        return complete;
    }

    public String getDownloadFilename() throws UnsupportedEncodingException {
        return String.format("sos-%s-%d.task.log", URLEncoder.encode(workflow.replaceFirst("^/*", "").replace('/', ',') + "." + jobName,
                StandardCharsets.UTF_8.name()), historyId);
    }

    public StreamingOutput getStreamOutput() throws JocMissingRequiredParameterException, JocConfigurationException, DBOpenSessionException,
            SOSHibernateException, DBMissingDataException, JobSchedulerInvalidResponseDataException {
        if (historyId == null) {
            throw new JocMissingRequiredParameterException("undefined 'taskId'");
        }
        StreamingOutput out = null;
        byte[] compressedLog = getLogFromDb();
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
        } else {
            final InputStream inStream = getLogSnapshotFromHistoryService();
            out = new StreamingOutput() {

                @Override
                public void write(OutputStream output) throws IOException {
                    OutputStream zipStream = null;
                    try {
                        zipStream = new GZIPOutputStream(output);
                        byte[] buffer = new byte[4096];
                        int length;
                        while ((length = inStream.read(buffer)) > 0) {
                            zipStream.write(buffer, 0, length);
                        }
                        zipStream.flush();
                    } finally {
                        try {
                            if (zipStream != null) {
                                zipStream.close();
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            };
        }

        compressedLog = null;
        return out;
    }

    private InputStream getLogSnapshotFromHistoryService() {
        // TODO read joc.properties (history.propertis) to find logs/history folder
        eventId = Instant.now().toEpochMilli() * 1000;
        complete = false;
        try {
            Path tasklog = Paths.get("logs", "history", orderId.toString(), orderId.toString() + "_" + historyId + ".log");
            if (Files.exists(tasklog)) {
                unCompressedLength = Files.size(tasklog);
                return Files.newInputStream(tasklog);
            }
        } catch (IOException e) {
            LOGGER.warn(e.toString());
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }
        String s = ZonedDateTime.now().format(formatter) + " [INFO] Snapshot log not found\r\n";
        unCompressedLength = s.length() * 1L;
        return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
    }

    public InputStream getLogStream() throws JocConfigurationException, DBOpenSessionException, SOSHibernateException, DBMissingDataException,
            IOException {
        if (historyId != null) {
            byte[] compressedLog = getLogFromDb();
            if (compressedLog != null) {
                return new GZIPInputStream(new ByteArrayInputStream(compressedLog));
            } else {
                return getLogSnapshotFromHistoryService();
            }
        }
        return null;
    }

    private byte[] getLogFromDb() throws JocConfigurationException, DBOpenSessionException, SOSHibernateException, DBMissingDataException {
        SOSHibernateSession connection = null;
        try {
            connection = Globals.createSosHibernateStatelessConnection("./task/log");
            DBItemHistoryOrderStep historyOrderStepItem = connection.get(DBItemHistoryOrderStep.class, historyId);
            if (historyOrderStepItem == null) {
                throw new DBMissingDataException(String.format("Task (Id:%d) not found", historyId));
            }
            orderId = historyOrderStepItem.getOrderId();
            jobName = historyOrderStepItem.getJobName();
            workflow = historyOrderStepItem.getWorkflowPath();
            if (historyOrderStepItem.getLogId() == 0L) {
                if (historyOrderStepItem.getEndTime() == null) {
                    // Task is running
                    return null;
                } else {
                    throw new DBMissingDataException(String.format("The log of the job %s (task id:%d) doesn't found", jobName, historyId));
                }
            } else {
                DBItemHistoryLog historyDBItem = connection.get(DBItemHistoryLog.class, historyOrderStepItem.getLogId());
                if (historyDBItem == null) {
                    throw new DBMissingDataException(String.format("The log of the job %s (task id:%d) doesn't found", jobName, historyId));
                } else {
                    unCompressedLength = historyDBItem.getFileSizeUncomressed();
                    complete = true;
                    return historyDBItem.getFileContent();
                }
            }
        } finally {
            Globals.disconnect(connection);
        }
    }

}
