package com.sos.joc.classes.logs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.sos.auth.classes.SOSAuthFolderPermissions;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.controller.model.event.EventType;
import com.sos.joc.Globals;
import com.sos.joc.classes.cluster.JocClusterService;
import com.sos.joc.db.history.DBItemHistoryLog;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.history.HistoryOrderTaskLog;
import com.sos.joc.exceptions.ControllerInvalidResponseDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.job.TaskFilter;

import jakarta.ws.rs.core.StreamingOutput;

public class LogTaskContent {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogTaskContent.class);
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd' 'HH:mm:ss.SSSZ");
    private static final long RUNNING_LOG_SLEEP_TIMEOUT = 1; // seconds
    private static final int RUNNING_LOG_BYTEBUFFER_ALLOCATE_SIZE = 64 * 1024;
    private static final int RUNNING_LOG_MAX_ITERATIONS = 100_000;

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
    private Long maxLogSize = 10 * 1024 * 1024L; // 10MB

    public LogTaskContent(TaskFilter taskFilter, SOSAuthFolderPermissions folderPermissions) {
        this.historyId = taskFilter.getTaskId();
        this.folderPermissions = folderPermissions;
        this.maxLogSize = Globals.getConfigurationGlobalsJoc().getMaxDisplaySizeInBytes();
        // this.controllerId = taskFilter.getControllerId();
    }

    public LogTaskContent(Long taskId, SOSAuthFolderPermissions folderPermissions) {
        this.historyId = taskId;
        this.folderPermissions = folderPermissions;
        this.maxLogSize = Globals.getConfigurationGlobalsJoc().getMaxDisplaySizeInBytes();
    }

    public LogTaskContent(Long taskId) {
        this.historyId = taskId;
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

    public StreamingOutput getStreamOutput(boolean forDownload) throws JocMissingRequiredParameterException, JocConfigurationException,
            DBOpenSessionException, SOSHibernateException, DBMissingDataException, ControllerInvalidResponseDataException {
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
        if (!forDownload && complete && unCompressedLength > maxLogSize) {
            return getTooBigMessageInputStream("");
        }
        try {
            Path tasklog = Paths.get("logs", "history", orderMainParentId.toString(), orderId.toString() + "_" + historyId + ".log");
            if (Files.exists(tasklog)) {
                eventId = Instant.now().toEpochMilli();
                complete = false;
                unCompressedLength = Files.size(tasklog);
                if (forDownload) {
                    return Files.newInputStream(tasklog);
                } else {
                    if (unCompressedLength > maxLogSize) {
                        return getTooBigMessageInputStream(" snapshot");
                    }
                }

                RunningTaskLogs.getInstance().subscribe(historyId);
                runningTaskLogMonitor(tasklog);
                // sent empty to avoid line order issue
                return new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            LOGGER.warn(e.toString());
        }
        String s = getLogLineNow();
        if (JocClusterService.getInstance().isRunning()) {
            s += " [INFO] Couldn't find the snapshot log\r\n";
        } else {
            s += " [INFO] Standby JOC Cockpit instance has no access to the snapshot log\r\n";
        }
        unCompressedLength = s.length() * 1L;
        complete = true;
        return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
    }

    // TODO - UTC is used ...
    private String getLogLineNow() {
        return ZonedDateTime.now().format(formatter);
    }

    // TODO - stop monitoring if the log window is closed - JavaScript window unload -> calls new web service
    // TODO - sleepAlways - true behavior?
    // TODO - sendEvent - RUNNING_LOG_BYTEBUFFER_ALLOCATE_SIZE best value ?
    // DONE - sendEvent - check if the monitoring not trigger for this HistoryOrderTaskLog events
    private void runningTaskLogMonitor(Path taskLog) {
        final String logPrefix = "[runningTaskLogMonitor][" + taskLog + "]";
        boolean sleepAlways = true; // to remove - sleep only if no new content provided(bytesRead == -1) all after each iteration...
        CompletableFuture.runAsync(() -> {
            // AsynchronousFileChannel for read - do not block the log file as it will be deleted by the history service
            try (AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(taskLog, StandardOpenOption.READ)) {

                ByteBuffer buffer = ByteBuffer.allocate(RUNNING_LOG_BYTEBUFFER_ALLOCATE_SIZE);
                long position = 0;
                int currentIteration = 0;
                // while (true) {
                r: while (historyId != null && !complete && RunningTaskLogs.getInstance().isRegistered(historyId)) {
                    currentIteration++;
                    // to avoid endless loop
                    if (currentIteration > RUNNING_LOG_MAX_ITERATIONS) {
                        break r;
                    }

                    buffer.clear();
                    int bytesRead = fileChannel.read(buffer, position).get();
                    if (bytesRead == -1) {// wait 1 second if no new content
                        if (!sleepAlways) {
                            if (!continueRunningLogMonitorAfterSleep(taskLog)) {
                                break r;
                            }
                        }
                    } else {
                        unCompressedLength = position;
                        if (unCompressedLength > maxLogSize) {
                            try {
                                String content = getTooBigMessage(" snapshot");
                                unCompressedLength += content.getBytes().length;
                                EventBus.getInstance().post(new HistoryOrderTaskLog(EventType.OrderStdoutWritten.value(), orderId, historyId, content,
                                        false));
                            } catch (Throwable e) {
                                LOGGER.warn(logPrefix + "[EventBus.getInstance().post]" + e, e);
                            }
                            break r;
                        }

                        position += bytesRead;
                        try {
                            buffer.flip();
                            byte[] bytes = new byte[buffer.remaining()];
                            buffer.get(bytes);
                            String content = new String(bytes, Charsets.UTF_8);
                            try {
                                EventBus.getInstance().post(new HistoryOrderTaskLog(EventType.OrderStdoutWritten.value(), orderId, historyId, content,
                                        false));
                            } catch (Throwable e) {
                                LOGGER.warn(logPrefix + "[EventBus.getInstance().post]" + e, e);
                            }
                        } catch (Throwable e) {
                            LOGGER.info(logPrefix + e.toString(), e);
                            break r;
                        }

                    }
                    if (sleepAlways) {
                        if (!continueRunningLogMonitorAfterSleep(taskLog)) {
                            break r;
                        }
                    }
                }
            } catch (Throwable e) {
                LOGGER.info(logPrefix + e.toString(), e);
            } finally {
                complete = true;
                RunningTaskLogs.getInstance().unsubscribe(historyId);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(logPrefix + "[END]RegisteredTaskIds=" + RunningTaskLogs.getInstance().getRegisteredTaskIds());
                }
            }
        });
    }

    private boolean continueRunningLogMonitorAfterSleep(Path logFile) {
        try {
            TimeUnit.SECONDS.sleep(RUNNING_LOG_SLEEP_TIMEOUT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        if (!Files.exists(logFile)) {
            return false;
        }
        return true;
    }

    private String getTooBigMessage(String kindOfLog) {
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

    private InputStream getTooBigMessageInputStream(String kindOfLog) {
        return new ByteArrayInputStream(getTooBigMessage(kindOfLog).getBytes(StandardCharsets.UTF_8));
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

    // !! not tested
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

    private byte[] getLogFromDb(boolean forDownload) throws JocConfigurationException, DBOpenSessionException, SOSHibernateException,
            DBMissingDataException {
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
                    if (!forDownload && unCompressedLength > maxLogSize) {
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
