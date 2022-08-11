package com.sos.joc.classes.logs;

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

import com.sos.auth.classes.SOSAuthFolderPermissions;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.cluster.JocClusterService;
import com.sos.joc.db.history.DBItemHistoryLog;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.exceptions.ControllerInvalidResponseDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.job.TaskFilter;

public class LogTaskContent {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogTaskContent.class);
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd' 'HH:mm:ss.SSSZ");
    // private String controllerId;
    private Long historyId;
    private Long orderMainParentId;
    private Long orderId;
    private String jobName;
    private String workflow;
    private Long unCompressedLength = null;
    private Long eventId = null;
    private boolean complete = false;
    private SOSAuthFolderPermissions folderPermissions = null;
    private static final long MAX_LOG_SIZE = 10 * 1024 * 1024L; //10MB

    public LogTaskContent(TaskFilter taskFilter, SOSAuthFolderPermissions folderPermissions) {
        this.historyId = taskFilter.getTaskId();
        this.folderPermissions = folderPermissions;
        // this.controllerId = taskFilter.getControllerId();
    }

    public LogTaskContent(Long taskId, SOSAuthFolderPermissions folderPermissions) {
        this.historyId = taskId;
        this.folderPermissions = folderPermissions;
    }
    
    public LogTaskContent(Long taskId) {
        this.historyId = taskId;
    }

    public Map<String, Object> getHeaders() {
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Access-Control-Expose-Headers", "X-Log-Complete,X-Log-Event-Id,X-Log-Task-Id,X-Uncompressed-Length");
        headers.put("X-Log-Complete", complete);
        headers.put("X-Log-Task-Id", historyId);
        if (unCompressedLength != null) {
            headers.put("X-Uncompressed-Length", unCompressedLength);
        }
        if (!isComplete()) {
            headers.put("X-Log-Task-Id", historyId);
            if (eventId != null) {
                headers.put("X-Log-Event-Id", eventId);
            }
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

    public StreamingOutput getStreamOutput(boolean forDownload) throws JocMissingRequiredParameterException, JocConfigurationException, DBOpenSessionException,
            SOSHibernateException, DBMissingDataException, ControllerInvalidResponseDataException {
        if (historyId == null) {
            throw new JocMissingRequiredParameterException("undefined 'taskId'");
        }
        StreamingOutput out = null;
        byte[] compressedLog = getLogFromDb(forDownload);
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
            final InputStream inStream = getLogSnapshotFromHistoryService(forDownload);
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

    private InputStream getLogSnapshotFromHistoryService(boolean forDownload) {
        if (!forDownload && complete && unCompressedLength > MAX_LOG_SIZE) {
            return getTooBigMessage("");
        }
        try {
            Path tasklog = Paths.get("logs", "history", orderMainParentId.toString(), orderId.toString() + "_" + historyId + ".log");
            if (Files.exists(tasklog)) {
                eventId = Instant.now().toEpochMilli();
                complete = false;
                unCompressedLength = Files.size(tasklog);
                RunningTaskLogs.getInstance().subscribe(historyId);
                if (!forDownload && unCompressedLength > MAX_LOG_SIZE) {
                    return getTooBigMessage(" snapshot");
                }
                return Files.newInputStream(tasklog);
            }
        } catch (IOException e) {
            LOGGER.warn(e.toString());
        }
        String s = ZonedDateTime.now().format(formatter);
        if (JocClusterService.getInstance().isRunning()) {
            s += " [INFO] Couldn't find the snapshot log\r\n";
        } else {
            s += " [INFO] Standby JOC Cockpit instance has no access to the snapshot log\r\n";
        }
        unCompressedLength = s.length() * 1L;
        complete = true;
        return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
    }
    
    private InputStream getTooBigMessage(String kindOfLog) {
        String s = ZonedDateTime.now().format(formatter);
        String s1 = s + " [INFO] The size of the" + kindOfLog + " log is too big: " + unCompressedLength + " bytes\r\n";
        s1 += s + " [INFO] Try to download the log\r\n";
        unCompressedLength = s1.length() * 1L;
        return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
    }

    public InputStream getLogStream() throws JocConfigurationException, DBOpenSessionException, SOSHibernateException, DBMissingDataException,
            IOException {
        if (historyId != null) {
            byte[] compressedLog = getLogFromDb(true);
            if (compressedLog != null) {
                return new GZIPInputStream(new ByteArrayInputStream(compressedLog));
            } else {
                return getLogSnapshotFromHistoryService(true);
            }
        }
        return null;
    }

    private byte[] getLogFromDb(boolean forDownload) throws JocConfigurationException, DBOpenSessionException, SOSHibernateException, DBMissingDataException {
        SOSHibernateSession connection = null;
        try {
            connection = Globals.createSosHibernateStatelessConnection("./task/log");
            DBItemHistoryOrderStep historyOrderStepItem = connection.get(DBItemHistoryOrderStep.class, historyId);
            if (historyOrderStepItem == null) {
                throw new DBMissingDataException(String.format("Couldn't find the Task (Id:%d)", historyId));
            }
            if (folderPermissions != null && !folderPermissions.isPermittedForFolder(historyOrderStepItem.getWorkflowFolder())) {
                throw new JocFolderPermissionsException("folder access denied: " + historyOrderStepItem.getWorkflowFolder());
            }
            orderId = historyOrderStepItem.getHistoryOrderId();
            orderMainParentId = historyOrderStepItem.getHistoryOrderMainParentId();
            jobName = historyOrderStepItem.getJobName();
            workflow = historyOrderStepItem.getWorkflowPath();
            if (historyOrderStepItem.getLogId() == 0L) {
                if (historyOrderStepItem.getEndTime() == null) {
                    // Task is running
                    return null;
                } else {
                    throw new DBMissingDataException(String.format("Couldn't find the log of the job %s (task id:%d)", jobName, historyId));
                }
            } else {
                DBItemHistoryLog historyDBItem = connection.get(DBItemHistoryLog.class, historyOrderStepItem.getLogId());
                if (historyDBItem == null) {
                    throw new DBMissingDataException(String.format("Couldn't find the log of the job %s (task id:%d)", jobName, historyId));
                } else {
                    unCompressedLength = historyDBItem.getFileSizeUncomressed();
                    complete = true;
                    if (!forDownload && unCompressedLength > MAX_LOG_SIZE) {
                        return null;
                    }
                    return historyDBItem.getFileContent();
                }
            }
        } finally {
            Globals.disconnect(connection);
        }
    }

}
