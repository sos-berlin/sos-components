package com.sos.joc.classes.logs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.sos.controller.model.event.EventType;
import com.sos.joc.classes.cluster.JocClusterService;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.history.HistoryOrderTaskLog;

import jakarta.ws.rs.core.StreamingOutput;

public class RunningTaskLogHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunningTaskLogHandler.class);

    private static final long RUNNING_LOG_SLEEP_TIMEOUT = 1; // seconds
    private static final int RUNNING_LOG_READ_FILE_BYTEBUFFER_ALLOCATE_SIZE = 64 * 1024;
    private static final int RUNNING_LOG_READ_GZIP_BUFFER_SIZE = 4 * 1024;
    private static final int RUNNING_LOG_MAX_ITERATIONS = 1_000_000;

    // 1 running log thread per sessionIdentifier
    private static final ConcurrentHashMap<String, Thread> runningLogThreads = new ConcurrentHashMap<>();

    private final Object lockObject = new Object();

    private final LogTaskContent content;
    private final boolean forDownload;
    private final Path taskLogFile;

    // private AtomicLong position = new AtomicLong(0);
    // a class instance has a “short” lifetime – can be defined as an instance property...
    private boolean isDebugEnabled = LOGGER.isDebugEnabled();

    public RunningTaskLogHandler(LogTaskContent content, boolean forDownload) {
        this.content = content;
        this.forDownload = forDownload;

        this.taskLogFile = Paths.get("logs", "history", content.getOrderMainParentId().toString(), content.getOrderId() + "_" + content.getHistoryId()
                + ".log");
    }

    public StreamingOutput getStreamingOutput() {
        InputStream inputStream = getInputStream();
        StreamingOutput out = new StreamingOutput() {

            @Override
            public void write(OutputStream output) throws IOException {
                OutputStream zipStream = null;
                long position = 0;
                boolean tooBig = false;
                try {
                    zipStream = new GZIPOutputStream(output);
                    byte[] buffer = new byte[4096];
                    int length;
                    x: while ((length = inputStream.read(buffer)) > 0) {
                        zipStream.write(buffer, 0, length);
                        position += length;

                        if (position > content.getMaxLogSize()) {
                            tooBig = true;
                            break x;
                        }
                    }
                    if (tooBig) {
                        zipStream.flush();
                        content.setComplete(true);
                        content.setUnCompressedLength(position);
                        InputStream is = content.getTooBigMessageInputStream(" snapshot");
                        while ((length = is.read(buffer)) > 0) {
                            zipStream.write(buffer, 0, length);
                        }
                    }

                    zipStream.flush();
                } finally {
                    try {
                        if (zipStream != null) {
                            zipStream.close();
                        }
                    } catch (Throwable e) {
                    }
                    if (isDebugEnabled) {
                        String add = tooBig ? "[tooBig]" : "";
                        if (content.isComplete()) {
                            add += "[skip][runningTaskLogMonitor]";
                        } else {
                            add += "[start][runningTaskLogMonitor...]";
                        }
                        LOGGER.debug("[getStreamingOutput][" + content.getHistoryId() + "][" + Thread.currentThread().getName() + "][isComplete="
                                + content.isComplete() + "]" + add + "position=" + position);
                    }
                    if (!content.isComplete()) {
                        runningTaskLogMonitor(position);
                    }
                }
            }
        };

        return out;
    }

    protected InputStream getInputStream() {
        if (!forDownload) {
            RunningTaskLogs.getInstance().unsubscribe(content.getSessionIdentifier(), content.getHistoryId());
            removeRunningLogThreadIfExists(true);

            if (content.isComplete() && content.getUnCompressedLength() > content.getMaxLogSize()) {
                return content.getTooBigMessageInputStream("");
            }
        }
        try {
            if (Files.exists(taskLogFile)) {
                content.setEventId();
                content.setUnCompressedLength(Files.size(taskLogFile));
                if (forDownload) {
                    content.setComplete(true);
                    return Files.newInputStream(taskLogFile);
                } else {
                    if (content.getUnCompressedLength() > content.getMaxLogSize()) {
                        content.setComplete(true);
                        if (isDebugEnabled) {
                            LOGGER.debug("[getInputStream][" + content.getHistoryId() + "][" + Thread.currentThread().getName()
                                    + "][isComplete=true][tooBig]current unCompressedLength=" + content.getUnCompressedLength() + "b > maxLogSize="
                                    + content.getMaxLogSize() + "b");
                        }
                        return content.getTooBigMessageInputStream(" snapshot");
                    }
                }

                content.setComplete(false);
                return Files.newInputStream(taskLogFile);
            }
        } catch (IOException e) {
            LOGGER.warn(e.toString());
        }

        content.setComplete(true);
        String s = content.getLogLineNow();
        if (JocClusterService.getInstance().isRunning()) {
            s += " [INFO] Couldn't find the snapshot log\r\n";
        } else {
            s += " [INFO] Standby JOC Cockpit instance has no access to the snapshot log\r\n";
        }
        content.setUnCompressedLength(s.length() * 1L);
        return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
    }

    // TODO - stop monitoring if the log window is closed - JavaScript window unload -> calls new web service
    // TODO - sendEvent - RUNNING_LOG_BYTEBUFFER_ALLOCATE_SIZE best value ?
    // TODO - sendEvent - open/close running log multiple times for order log (OrderProcessed duplicates)
    // DONE - sendEvent - check if the monitoring not trigger for this HistoryOrderTaskLog events
    // DONE - sendEvent - open/close running log multiple sessions
    // DONE - if the same taskId is opened in two/multiple users sessions - the logs from other sessions are added to log because of HistoryOrderTaskLog
    // DONE - sendEvent - open/close running log multiple times for task log
    private void runningTaskLogMonitor(final long startPosition) {
        final String logPrefix = "[runningTaskLogMonitor][" + content.getHistoryId() + "]";
        if (content.isComplete()) {
            if (isDebugEnabled) {
                LOGGER.debug(logPrefix + "[skip][completed=true]startPosition=" + startPosition);
            }
            return;
        }

        if (isDebugEnabled) {
            LOGGER.debug(logPrefix + "startPosition=" + startPosition);
        }

        // boolean sleepAlways = false; // to remove - sleep only if no new content provided(bytesRead == -1) all after each iteration...
        Thread workerThread = new Thread(() -> {

            // AsynchronousFileChannel for read - do not block the log file as it will be deleted by the history service
            boolean isInterupped = false;
            String threadName = null;
            if (isDebugEnabled) {
                threadName = Thread.currentThread().getName();
            }
            long position = startPosition;
            try (AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(taskLogFile, StandardOpenOption.READ)) {
                ByteBuffer buffer = ByteBuffer.allocate(RUNNING_LOG_READ_FILE_BYTEBUFFER_ALLOCATE_SIZE);
                int currentIteration = 0;
                r: while (!content.isComplete()) {

                    if (isDebugEnabled) {
                        LOGGER.debug(logPrefix + "[" + threadName + "][currentIteration=" + currentIteration + "][before]position=" + position);
                    }

                    currentIteration++;
                    // to avoid endless loop
                    if (currentIteration > RUNNING_LOG_MAX_ITERATIONS) {
                        break r;
                    }

                    buffer.clear();
                    int bytesRead = fileChannel.read(buffer, position).get();
                    if (bytesRead == -1) {// wait 1 second if no new content
                        if (!continueRunningLogMonitorAfterSleep(taskLogFile)) {
                            if (isDebugEnabled) {
                                LOGGER.debug(logPrefix + "[" + threadName + "][continueRunningLogMonitorAfterSleep][break]position=" + position);
                            }
                            break r;
                        }
                    } else {
                        position += bytesRead;

                        if (isDebugEnabled) {
                            LOGGER.debug(logPrefix + "[" + threadName + "][currentIteration=" + currentIteration + "][after]position=" + position);
                        }

                        content.setUnCompressedLength(position);
                        if (content.getUnCompressedLength() > content.getMaxLogSize()) {
                            try {
                                content.setComplete(true);
                                String logContent = content.getTooBigMessage(" snapshot");
                                content.addUnCompressedLength(logContent.getBytes().length);
                                EventBus.getInstance().post(new HistoryOrderTaskLog(EventType.OrderStdoutWritten.value(), content.getOrderId(),
                                        content.getHistoryId(), logContent, content.getSessionIdentifier()));
                            } catch (Throwable e) {
                                LOGGER.warn(logPrefix + "[EventBus.getInstance().post]" + e, e);
                            } finally {
                                buffer.clear();
                            }
                            break r;
                        }

                        try {
                            buffer.flip();
                            byte[] bytes = new byte[buffer.remaining()];
                            buffer.get(bytes);
                            String logContent = new String(bytes, Charsets.UTF_8);
                            try {
                                if (isDebugEnabled) {
                                    LOGGER.debug(logPrefix + "[" + threadName + "][EVENT][OrderStdoutWritten]position=" + position);
                                }

                                EventBus.getInstance().post(new HistoryOrderTaskLog(EventType.OrderStdoutWritten.value(), content.getOrderId(),
                                        content.getHistoryId(), logContent, content.getSessionIdentifier()));
                            } catch (Throwable e) {
                                LOGGER.warn(logPrefix + "[EventBus.getInstance().post]" + e, e);
                            }
                        } catch (Throwable e) {
                            LOGGER.info(logPrefix + e.toString(), e);
                            break r;
                        } finally {
                            buffer.clear();
                        }
                    }
                }
            } catch (InterruptedException e) {
                isInterupped = true;
            } catch (Throwable e) {
                LOGGER.info(logPrefix + e.toString(), e);
            } finally {
                if (isInterupped) {
                    if (isDebugEnabled) {
                        LOGGER.debug(logPrefix + "[" + threadName + "][FINALLY][INTERRUPTED]position=" + position);
                    }
                    position = 0;
                } else {
                    if (Files.exists(taskLogFile)) {
                        if (isDebugEnabled) {
                            LOGGER.debug(logPrefix + "[" + threadName + "][FINALLY][FILE_EXISTS]position=" + position);
                        }
                        position = 0;
                    } else {
                        content.setComplete(true);
                        try {
                            byte[] compressedLog = content.getLogFromDb(false, Long.valueOf(position));
                            if (isDebugEnabled) {
                                LOGGER.debug(logPrefix + "[" + threadName + "][FINALLY][DB][isLogReadedFromDb=" + content.isLogReadedFromDb()
                                        + "]position=" + position);
                            }
                            if (compressedLog == null) {
                                // the task log is not currently in the database, but may be written in the same second...
                                if (!content.isLogReadedFromDb()) {
                                    waitFor(1);
                                    compressedLog = content.getLogFromDb(false, Long.valueOf(position));
                                }
                            }

                            String logContent = null;
                            if (compressedLog != null) {
                                logContent = decompressAfterPosition(compressedLog, position);
                            }
                            if (isDebugEnabled) {
                                LOGGER.debug(logPrefix + "[" + threadName + "][FINALLY][DB][position=" + position + "]" + (logContent == null ? "null"
                                        : logContent.length() + " characters"));
                            }
                            try {
                                logContent = logContent == null ? "" : logContent;
                                EventBus.getInstance().post(new HistoryOrderTaskLog(EventType.OrderProcessed.value(), content.getOrderId(), content
                                        .getHistoryId(), logContent, content.getSessionIdentifier()));
                            } catch (Throwable e) {
                                LOGGER.warn(logPrefix + "[EventBus.getInstance().post]" + e, e);
                            }

                        } catch (Throwable e) {
                            LOGGER.info(logPrefix + "[decompressAfterPosition]" + e.toString(), e);
                        } finally {
                            if (isDebugEnabled) {
                                LOGGER.debug(logPrefix + "[" + threadName + "][FINALLY]PROCESSED");
                            }
                        }
                    }
                }
            }
        });
        if (Files.exists(taskLogFile) && !content.isComplete()) {
            putRunningLogThread(workerThread);
            RunningTaskLogs.getInstance().subscribe(content.getSessionIdentifier(), content.getHistoryId());
            workerThread.start();
        } else {
            close();
            RunningTaskLogs.getInstance().unsubscribe(content.getSessionIdentifier(), content.getHistoryId());
            removeRunningLogThreadIfExists(false);
        }
    }

    private String decompressAfterPosition(byte[] compressedData, long startPosition) {
        try {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(compressedData); GZIPInputStream gzipIn = new GZIPInputStream(bis);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[RUNNING_LOG_READ_GZIP_BUFFER_SIZE];
                int len;
                long totalBytesRead = 0;
                boolean started = false;

                while ((len = gzipIn.read(buffer)) > 0) {
                    totalBytesRead += len;
                    if (!started && totalBytesRead > startPosition) {
                        int writeOffset = (int) (startPosition - (totalBytesRead - len));
                        bos.write(buffer, writeOffset, len - writeOffset);
                        started = true;
                    } else if (started) {
                        bos.write(buffer, 0, len);
                    }
                }
                byte[] r = bos.toByteArray();
                return r == null || r.length == 0 ? null : new String(r, StandardCharsets.UTF_8);
            }
        } catch (Throwable e) {
            LOGGER.info("[decompressAfterPosition][" + content.getHistoryId() + "][position=" + startPosition + "]" + e.toString(), e);
            return null;
        }
    }

    private boolean continueRunningLogMonitorAfterSleep(Path logFile) {
        if (!Files.exists(logFile)) {
            return false;
        }
        try {
            waitFor(RUNNING_LOG_SLEEP_TIMEOUT);
            if (!Files.exists(logFile)) {
                return false;
            }
        } catch (InterruptedException e) {
            try {
                Thread.currentThread().interrupt();
            } catch (Throwable ex) {
            }
            return false;
        }
        return true;
    }

    private void close() {
        content.setComplete(true);
        synchronized (lockObject) {
            lockObject.notifyAll();
        }
    }

    private void removeRunningLogThreadIfExists(boolean withInterrupt) {
        try {
            Thread previousWorkerThread = runningLogThreads.remove(getThreadIdentifier());
            if (withInterrupt && previousWorkerThread != null) {
                if (isDebugEnabled) {
                    LOGGER.debug("[removeRunningLogThreadIfExists][" + content.getHistoryId() + "][" + previousWorkerThread.getName()
                            + "]interrupt previous thread...");
                }
                previousWorkerThread.interrupt();
                try {
                    previousWorkerThread.join();
                    if (isDebugEnabled) {
                        LOGGER.debug("[removeRunningLogThreadIfExists][" + content.getHistoryId() + "][" + previousWorkerThread.getName()
                                + "]previous thread was closed");
                    }
                } catch (InterruptedException e) {
                    if (isDebugEnabled) {
                        LOGGER.debug("[removeRunningLogThreadIfExists][" + content.getHistoryId() + "][" + previousWorkerThread.getName()
                                + "]InterruptedException");
                    }
                    // Thread.currentThread().interrupt();
                }
            }
        } catch (Throwable e) {
        } finally {
            if (isDebugEnabled) {
                LOGGER.debug("[removeRunningLogThreadIfExists][" + content.getHistoryId() + "]runningLogThreads=" + runningLogThreads.size());
            }
        }
    }

    private void putRunningLogThread(Thread workerThread) {
        try {
            Thread previousWorkerThread = runningLogThreads.put(getThreadIdentifier(), workerThread);
            if (previousWorkerThread != null) {
                if (isDebugEnabled) {
                    LOGGER.debug("[putRunningLogThread][" + content.getHistoryId() + "][" + previousWorkerThread.getName()
                            + "]interrupt previous thread...");
                }
                previousWorkerThread.interrupt();
                try {
                    previousWorkerThread.join();
                    if (isDebugEnabled) {
                        LOGGER.debug("[putRunningLogThread][" + content.getHistoryId() + "][" + previousWorkerThread.getName()
                                + "]previous thread was closed");
                    }
                } catch (InterruptedException e) {
                    // Thread.currentThread().interrupt();
                }
            }
        } catch (Throwable e) {
        }
    }

    private String getThreadIdentifier() {
        return content.getSessionIdentifier() + "_" + content.getHistoryId();
    }

    private void waitFor(long seconds) throws InterruptedException {
        if (!content.isComplete() && seconds > 0) {
            synchronized (lockObject) {
                lockObject.wait(seconds * 1_000);
            }
        }
    }

}
