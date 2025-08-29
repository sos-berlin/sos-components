package com.sos.joc.classes.logs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.sos.commons.util.SOSDate;
import com.sos.controller.model.event.EventType;
import com.sos.joc.classes.cluster.JocClusterService;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.history.HistoryOrderTaskLog;

import jakarta.ws.rs.core.StreamingOutput;

public class RunningTaskLogHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunningTaskLogHandler.class);

    private static final String RUNNING_LOG_THREAD_PREFIX = "running-task-log-";
    private static final AtomicInteger runningLogThreadCounter = new AtomicInteger(1);

    // Wait interval between rereading the history file if the history file does not return a new position (no new data).
    private static final long RUNNING_LOG_WAIT_INTERVAL_IF_NO_NEW_CONTENT_IN_SECONDS = 2;
    private static final long RUNNING_LOG_WAIT_INTERVAL_LOG_STEP = 30; // log first wait and all 30(wait log step)*2(wait interval) seconds

    private static final int RUNNING_LOG_READ_FILE_BYTEBUFFER_ALLOCATE_SIZE = 64 * 1_024;
    private static final int RUNNING_LOG_READ_GZIP_BUFFER_SIZE = 4 * 1_024;

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
            removePreviousThreadIfExists(true);

            if (content.isComplete() && content.getUnCompressedLength() > content.getMaxLogSize()) {
                return content.getTooBigMessageInputStream("");
            }
        }
        try {
            if (Files.exists(taskLogFile)) {
                if (!forDownload) {
                    // before content.setEventId();
                    // - (see comparison RunningTaskLogs.getInstance().isBeforeSubscriptionStartTime)
                    RunningTaskLogs.getInstance().registerSubscriptionStartTime(content.getSessionIdentifier(), content.getHistoryId());
                }

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
        final String logPrefix = "[runningTaskLogMonitor][historyId=" + content.getHistoryId() + "]";
        if (content.isComplete()) {
            if (isDebugEnabled) {
                LOGGER.debug(logPrefix + "[skip][completed=true]startPosition=" + startPosition);
            }
            return;
        }

        if (isDebugEnabled) {
            LOGGER.debug(logPrefix + "startPosition=" + startPosition);
        }

        Thread workerThread = new Thread(() -> {
            boolean isInterupped = false;
            String thread = null;
            if (isDebugEnabled) {
                thread = "thread=" + Thread.currentThread().getName();
            }
            long position = startPosition;
            // AsynchronousFileChannel for read - do not block the log file as it will be deleted by the history service
            try (AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(taskLogFile, StandardOpenOption.READ)) {
                ByteBuffer buffer = ByteBuffer.allocate(RUNNING_LOG_READ_FILE_BYTEBUFFER_ALLOCATE_SIZE);
                Instant start = Instant.now();
                Instant deadline = start.plus(RunningTaskLogs.RUNNING_LOG_MAX_THREAD_LIFETIME);
                long waitCounter = 0;
                r: while (!content.isComplete()) {
                    buffer.clear();

                    Instant now = Instant.now();

                    // to avoid endless loop
                    if (!continueRunningLogMonitor(logPrefix, thread, position, start, deadline, now)) {
                        break r;
                    }

                    int bytesRead = fileChannel.read(buffer, position).get();
                    if (bytesRead == -1) {// wait n seconds if no new content
                        buffer.clear();
                        waitCounter++;

                        if (isDebugEnabled) {
                            int logFirstWaits = 2;
                            boolean logNextWaits = waitCounter % RUNNING_LOG_WAIT_INTERVAL_LOG_STEP == 0;
                            if (waitCounter <= logFirstWaits || logNextWaits) {
                                Duration elapsed = Duration.between(start, now);
                                String waitAdd = "";
                                if (waitCounter == logFirstWaits || logNextWaits) {
                                    waitAdd = " (the next possible 'wait' log entry comes after " + (RUNNING_LOG_WAIT_INTERVAL_LOG_STEP
                                            - logFirstWaits) + " repetitions)";
                                }
                                LOGGER.debug(logPrefix + "[" + thread + "][runningTime=" + SOSDate.getDuration(elapsed) + "][position=" + position
                                        + "][waitCounter=" + waitCounter + waitAdd + "]wait " + RUNNING_LOG_WAIT_INTERVAL_IF_NO_NEW_CONTENT_IN_SECONDS
                                        + "s, because no new content...");
                            }
                        }

                        if (!continueRunningLogMonitorAfterSleep(logPrefix, thread, position, taskLogFile)) {
                            break r;
                        }
                    } else {
                        waitCounter = 0;
                        long oldPosition = position;
                        position += bytesRead;

                        if (isDebugEnabled) {
                            Duration elapsed = Duration.between(start, now);
                            LOGGER.debug(logPrefix + "[" + thread + "][runningTime=" + SOSDate.getDuration(elapsed) + "][old position=" + oldPosition
                                    + "]position=" + position);
                        }
                        oldPosition = 0;

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
                                    LOGGER.debug(logPrefix + "[" + thread + "][post EVENT][OrderStdoutWritten]position=" + position);
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
                // content.setComplete(true);
                if (isInterupped) {
                    if (isDebugEnabled) {
                        LOGGER.debug(logPrefix + "[" + thread + "][FINALLY][INTERRUPTED]position=" + position);
                    }
                    position = 0;
                } else {
                    if (Files.exists(taskLogFile)) {
                        if (isDebugEnabled) {
                            LOGGER.debug(logPrefix + "[" + thread + "][FINALLY][FILE_EXISTS]position=" + position);
                        }
                        position = 0;
                    } else {
                        content.setComplete(true);
                        try {
                            byte[] compressedLog = content.getLogFromDb(false, Long.valueOf(position));
                            if (isDebugEnabled) {
                                LOGGER.debug(logPrefix + "[" + thread + "][FINALLY][DB][isLogReadedFromDb=" + content.isLogReadedFromDb()
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
                                LOGGER.debug(logPrefix + "[" + thread + "][FINALLY][DB][position=" + position + "]" + (logContent == null ? "null"
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
                                LOGGER.debug(logPrefix + "[" + thread + "][FINALLY]PROCESSED");
                            }
                        }
                    }
                }
            }
            unsubscribe(content.isComplete());
            if (isDebugEnabled) {
                LOGGER.debug(logPrefix + "[" + thread + "][FINALLY][Thread]end");
            }
        }, RUNNING_LOG_THREAD_PREFIX + runningLogThreadCounter.getAndIncrement());
        if (Files.exists(taskLogFile) && !content.isComplete()) {
            if (isDebugEnabled) {
                LOGGER.debug(logPrefix + "[START]Thread/Subscribe session");
            }
            putRunningLogThread(workerThread);
            RunningTaskLogs.getInstance().subscribe(content.getSessionIdentifier(), new TaskLogBean(content));
            workerThread.start();
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(logPrefix + "[skip]already processed");
            }
            unsubscribe(true);
            removePreviousThreadIfExists(false);
        }
    }

    private boolean continueRunningLogMonitor(String logPrefix, String thread, long position, Instant start, Instant deadline, Instant now) {
        try {
            if (!RunningTaskLogs.jocSessionExists(content.getSessionIdentifier(), content.getHistoryId())) {
                if (LOGGER.isDebugEnabled()) {
                    Duration elapsed = Duration.between(start, now);
                    LOGGER.debug(logPrefix + "[" + thread + "][runningTime=" + SOSDate.getDuration(elapsed) + "][position=" + position
                            + "][break]the calling joc session no longer exists");
                }
                return false;
            }
        } catch (Exception e) {// not really needed but ...
            if (LOGGER.isDebugEnabled()) {
                Duration elapsed = Duration.between(start, now);
                LOGGER.debug(logPrefix + "[" + thread + "][runningTime=" + SOSDate.getDuration(elapsed) + "][position=" + position
                        + "][check calling joc session][exception]" + e);
            }
        }

        if (!RunningTaskLogs.getInstance().isSubscribed(content.getSessionIdentifier(), content.getHistoryId())) {
            if (LOGGER.isDebugEnabled()) {
                Duration elapsed = Duration.between(start, now);
                LOGGER.debug(logPrefix + "[" + thread + "][runningTime=" + SOSDate.getDuration(elapsed) + "][position=" + position
                        + "][break]no more registered");
            }
            return false;
        }

        if (now.isAfter(deadline)) {
            if (LOGGER.isDebugEnabled()) {
                Duration elapsed = Duration.between(start, now);
                LOGGER.debug(logPrefix + "[" + thread + "][runningTime=" + SOSDate.getDuration(elapsed) + "][position=" + position
                        + "][break]deadline reached");
            }
            return false;
        }

        return true;
    }

    private boolean continueRunningLogMonitorAfterSleep(String logPrefix, String thread, long position, Path logFile) {
        if (!Files.exists(logFile)) {
            if (isDebugEnabled) {
                LOGGER.debug(logPrefix + "[" + thread + "][continueRunningLogMonitorAfterSleep][position=" + position
                        + "][break][before wait]history file does not exist");
            }
            return false;
        }
        try {
            waitFor(RUNNING_LOG_WAIT_INTERVAL_IF_NO_NEW_CONTENT_IN_SECONDS);
            if (!Files.exists(logFile)) {
                if (isDebugEnabled) {
                    LOGGER.debug(logPrefix + "[" + thread + "][continueRunningLogMonitorAfterSleep][position=" + position
                            + "][break][after wait]history file does not exist");
                }
                return false;
            }
        } catch (InterruptedException e) {
            try {
                Thread.currentThread().interrupt();
            } catch (Throwable ex) {
            }
            if (isDebugEnabled) {
                LOGGER.debug(logPrefix + "[" + thread + "][continueRunningLogMonitorAfterSleep][position=" + position + "][break]thread interrupted");
            }
            return false;
        }
        return true;
    }

    private void unsubscribe(boolean complete) {
        content.setComplete(complete);
        synchronized (lockObject) {
            lockObject.notifyAll();
        }
        RunningTaskLogs.getInstance().unsubscribe(content.getSessionIdentifier(), content.getHistoryId());
    }

    private void removePreviousThreadIfExists(boolean withInterrupt) {
        try {
            WeakReference<Thread> previousWorkerThreadRef = RunningTaskLogs.getRunningLogThreadsCache().remove(RunningTaskLogs.getThreadIdentifier(
                    content));
            Thread previousWorkerThread = previousWorkerThreadRef != null ? previousWorkerThreadRef.get() : null;
            if (withInterrupt && previousWorkerThread != null) {
                if (isDebugEnabled) {
                    LOGGER.debug("[removePreviousThreadIfExists][reread][historyId=" + content.getHistoryId() + "][previous thread="
                            + previousWorkerThread.getName() + "]interrupt previous thread...");
                }
                previousWorkerThread.interrupt();
                try {
                    previousWorkerThread.join();
                    if (isDebugEnabled) {
                        LOGGER.debug("[removePreviousThreadIfExists][historyId=" + content.getHistoryId() + "][previous thread="
                                + previousWorkerThread.getName() + "]previous thread was closed");
                    }
                } catch (InterruptedException e) {
                    if (isDebugEnabled) {
                        LOGGER.debug("[removePreviousThreadIfExists][historyId=" + content.getHistoryId() + "][previous thread="
                                + previousWorkerThread.getName() + "]InterruptedException");
                    }
                    // Thread.currentThread().interrupt();
                }
            }
        } catch (Throwable e) {
        } finally {
            logRunningLogThreadsCache();
        }
    }

    private void logRunningLogThreadsCache() {
        if (isDebugEnabled) {
            try {
                int size = RunningTaskLogs.getRunningLogThreadsCache().size();
                if (size == 0) {
                    LOGGER.debug("[logRunningLogThreadsCache][runningLogThreadsCache]total=" + size);

                } else {
                    // Map<HistoryId, List<Thread>>
                    Map<String, List<Thread>> groupedByHistoryId = new LinkedHashMap<>();
                    size = 0; // recalculate size
                    for (Map.Entry<String, WeakReference<Thread>> entry : RunningTaskLogs.getRunningLogThreadsCache().entrySet()) {
                        try {
                            size++;
                            String historyId = RunningTaskLogs.getHistoryIdFromThreadIdentifier(entry.getKey());
                            Thread thread = entry.getValue().get();
                            groupedByHistoryId.computeIfAbsent(historyId, k -> new ArrayList<>()).add(thread);
                        } catch (Exception ex) {
                            // ignore
                        }
                    }
                    LOGGER.debug("[logRunningLogThreadsCache][runningLogThreadsCache]total=" + size + ":");
                    for (Map.Entry<String, List<Thread>> entry : groupedByHistoryId.entrySet()) {
                        String historyId = entry.getKey();
                        List<Thread> threads = entry.getValue();

                        long total = threads.size();
                        int countAlive = 0;
                        int countDead = 0;
                        int countNull = 0;
                        StringBuilder threadInfo = new StringBuilder();
                        for (Thread t : threads) {
                            if (t != null) {
                                threadInfo.append(t.getName()).append("(");
                                if (t.isAlive()) {
                                    threadInfo.append("alive");
                                    countAlive++;
                                } else {
                                    threadInfo.append("dead");
                                    countDead++;
                                }
                                threadInfo.append("), ");
                            } else {
                                countNull++;
                            }
                        }

                        if (threadInfo.length() > 2) {
                            threadInfo.setLength(threadInfo.length() - 2); // remove trailing comma
                        }

                        LOGGER.debug("   [historyId=" + historyId + "][referenced threads size=" + total + "(alive=" + countAlive + ", dead="
                                + countDead + ", null(GC)=" + countNull + ")]referenced threads(alive, dead)=" + threadInfo);
                    }
                }
            } catch (Exception e) {

            }
        }
    }

    private void putRunningLogThread(Thread workerThread) {
        try {
            WeakReference<Thread> previousWorkerThreadRef = RunningTaskLogs.getRunningLogThreadsCache().put(RunningTaskLogs.getThreadIdentifier(
                    content), new WeakReference<>(workerThread));
            Thread previousWorkerThread = previousWorkerThreadRef != null ? previousWorkerThreadRef.get() : null;

            if (previousWorkerThread != null) {
                if (previousWorkerThread.isAlive()) {
                    if (isDebugEnabled) {
                        LOGGER.debug("[putRunningLogThread][historyId=" + content.getHistoryId() + "][previous thread=" + previousWorkerThread
                                .getName() + "]interrupt previous thread...");
                    }
                    previousWorkerThread.interrupt();
                    try {
                        previousWorkerThread.join();
                        if (isDebugEnabled) {
                            LOGGER.debug("[putRunningLogThread][historyId=" + content.getHistoryId() + "][previous thread=" + previousWorkerThread
                                    .getName() + "]previous thread was closed");
                        }
                    } catch (InterruptedException e) {
                        // Thread.currentThread().interrupt();
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug("[putRunningLogThread][historyId=" + content.getHistoryId() + "][previous thread=" + previousWorkerThread
                                .getName() + "]was already terminated");
                    }
                }
            }
        } catch (Throwable e) {
            if (isDebugEnabled) {
                LOGGER.debug("[putRunningLogThread][historyId=" + content.getHistoryId() + "][Unexpected error]" + e);
            }
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
            LOGGER.info("[decompressAfterPosition][historyId=" + content.getHistoryId() + "][position=" + startPosition + "]" + e.toString(), e);
            return null;
        }
    }

    private void waitFor(long seconds) throws InterruptedException {
        if (!content.isComplete() && seconds > 0) {
            synchronized (lockObject) {
                lockObject.wait(seconds * 1_000);
            }
        }
    }

}
