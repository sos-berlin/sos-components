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
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import com.sos.auth.classes.SOSAuthFolderPermissions;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.history.DBItemHistoryLog;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.exceptions.ControllerInvalidResponseDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.job.TaskFilter;

import jakarta.ws.rs.core.StreamingOutput;

public class LogTaskContent {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd' 'HH:mm:ss.SSSZ");

    private final SOSAuthFolderPermissions folderPermissions;
    private final Long historyId;
    private final String sessionIdentifier;
    private final Long maxLogSize;// 10 * 1024 * 1024L; // 10MB

    private Long unCompressedLength = null;
    private Long eventId = null;
    private Long orderMainParentId;
    private Long orderId;

    private String jobName;
    private String workflow;
    private volatile boolean complete = false;

    private boolean logReadedFromDb = false;

    public LogTaskContent(TaskFilter taskFilter, SOSAuthFolderPermissions folderPermissions, String sessionIdentifier) {
        this.historyId = taskFilter.getTaskId();
        this.sessionIdentifier = sessionIdentifier;
        this.folderPermissions = folderPermissions;
        this.maxLogSize = Globals.getConfigurationGlobalsJoc().getMaxDisplaySizeInBytes();
    }

    public LogTaskContent(Long taskId, SOSAuthFolderPermissions folderPermissions, String sessionIdentifier) {
        this.historyId = taskId;
        this.sessionIdentifier = sessionIdentifier;
        this.folderPermissions = folderPermissions;
        this.maxLogSize = Globals.getConfigurationGlobalsJoc().getMaxDisplaySizeInBytes();

    }

    public LogTaskContent(Long taskId, String sessionIdentifier) {
        this.historyId = taskId;
        this.sessionIdentifier = sessionIdentifier;
        this.folderPermissions = null;
        this.maxLogSize = Globals.getConfigurationGlobalsJoc().getMaxDisplaySizeInBytes();
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

    public String getDownloadFilename() throws UnsupportedEncodingException {
        return String.format("sos-%s-%d.task.log", URLEncoder.encode(workflow.replaceFirst("^/*", "").replace('/', ',') + "." + jobName,
                StandardCharsets.UTF_8.name()), historyId);
    }

    public StreamingOutput getStreamOutput(boolean forDownload) throws JocMissingRequiredParameterException, JocConfigurationException,
            DBOpenSessionException, SOSHibernateException, DBMissingDataException, ControllerInvalidResponseDataException {
        if (historyId == null) {
            throw new JocMissingRequiredParameterException("undefined 'taskId'");
        }
        StreamingOutput out = null;
        byte[] compressedLog = getLogFromDb(forDownload, null);
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
            out = new RunningTaskLogHandler(this, forDownload).getStreamingOutput();
        }
        compressedLog = null;
        return out;
    }

    // TODO - UTC is used ...
    protected String getLogLineNow() {
        return ZonedDateTime.now().format(FORMATTER);
    }

    protected String getTooBigMessage(String kindOfLog) {
        float f = unCompressedLength.floatValue() / (1024 * 1024);
        String unCompressedLengthInMB = (f + ".0").replaceAll("(\\d+)(\\.\\d)?[\\d.]*", "$1$2") + "MB";

        StringBuilder sb = new StringBuilder();
        // first line
        sb.append(getLogLineNow());
        sb.append(" [INFO] The size of the").append(kindOfLog).append(" log is too big: ").append(unCompressedLengthInMB);
        sb.append("\r\n");
        // second line
        sb.append(getLogLineNow());
        sb.append(" [INFO] ");
        if (kindOfLog.isEmpty()) {
            sb.append("Try to download the log.");
        } else {
            sb.append("No running log available. ");
            sb.append("Try to download the ").append(kindOfLog).append(" log.");
        }
        sb.append("\r\n");

        unCompressedLength = sb.length() * 1L;
        complete = true;
        return sb.toString();
    }

    protected InputStream getTooBigMessageInputStream(String kindOfLog) {
        return new ByteArrayInputStream(getTooBigMessage(kindOfLog).getBytes(StandardCharsets.UTF_8));
    }

    public InputStream getLogStream() throws JocConfigurationException, DBOpenSessionException, SOSHibernateException, DBMissingDataException,
            IOException {
        if (historyId != null) {
            byte[] compressedLog = getLogFromDb(true, null);
            if (compressedLog != null) {
                return new GZIPInputStream(new ByteArrayInputStream(compressedLog));
            } else {
                return new RunningTaskLogHandler(this, true).getInputStream();
            }
        }
        return null;
    }

    public void toFile(Path targetFile) throws JocConfigurationException, DBOpenSessionException, DBMissingDataException, SOSHibernateException,
            IOException {

        InputStream is = getLogStream();
        if (is != null) {
            try (OutputStream out = Files.newOutputStream(targetFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                is.transferTo(out);
                try {
                    is.close();
                } catch (Throwable e) {
                }
            }
        }
    }

    protected byte[] getLogFromDb(boolean forDownload, Long position) throws JocConfigurationException, DBOpenSessionException, SOSHibernateException,
            DBMissingDataException {
        logReadedFromDb = false;
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("./task/log");
            DBItemHistoryOrderStep step = session.get(DBItemHistoryOrderStep.class, historyId);
            if (step == null) {
                throw new DBMissingDataException(String.format("Couldn't find the Task (Id:%d)", historyId));
            }
            if (folderPermissions != null && !folderPermissions.isPermittedForFolder(step.getWorkflowFolder())) {
                throw new JocFolderPermissionsException("folder access denied: " + step.getWorkflowFolder());
            }
            orderId = step.getHistoryOrderId();
            orderMainParentId = step.getHistoryOrderMainParentId();
            jobName = step.getJobName();
            workflow = step.getWorkflowPath();
            if (step.getLogId() == 0L) {
                if (step.getEndTime() == null) {
                    // Task is running
                    return null;
                } else {
                    throw new DBMissingDataException(String.format("Couldn't find the log of the job %s (task id:%d)", jobName, historyId));
                }
            } else {
                logReadedFromDb = true;
                complete = true;

                DBItemHistoryLog log = session.get(DBItemHistoryLog.class, step.getLogId());
                if (log == null) {
                    throw new DBMissingDataException(String.format("Couldn't find the log of the job %s (task id:%d)", jobName, historyId));
                } else {
                    unCompressedLength = log.getFileSizeUncomressed();
                    if (!forDownload) {
                        if (unCompressedLength > maxLogSize) {
                            return null;
                        }
                        // API (e.g. running logs) has already read up to position
                        if (position != null && position >= unCompressedLength) {
                            return null;
                        }
                    }
                    return log.getFileContent();
                }
            }
        } finally {
            Globals.disconnect(session);
        }
    }

    public boolean isComplete() {
        return complete;
    }

    protected void setComplete(boolean val) {
        complete = val;
    }

    protected void setEventId() {
        eventId = Instant.now().toEpochMilli();
    }

    protected Long getOrderId() {
        return orderId;
    }

    protected Long getOrderMainParentId() {
        return orderMainParentId;
    }

    protected Long getHistoryId() {
        return historyId;
    }

    protected String getSessionIdentifier() {
        return sessionIdentifier;
    }

    public Long getUnCompressedLength() {
        return unCompressedLength;
    }

    protected void setUnCompressedLength(Long val) {
        unCompressedLength = val;
    }

    protected void addUnCompressedLength(int val) {
        unCompressedLength += val;
    }

    protected Long getMaxLogSize() {
        return maxLogSize;
    }

    protected boolean isLogReadedFromDb() {
        return logReadedFromDb;
    }
}
