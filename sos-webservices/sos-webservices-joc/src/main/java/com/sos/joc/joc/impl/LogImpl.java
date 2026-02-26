package com.sos.joc.joc.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.classes.logs.IJocLog;
import com.sos.joc.classes.logs.RunningJocLog;
import com.sos.joc.joc.resource.ILogResource;
import com.sos.joc.model.JOClog;
import com.sos.joc.model.JOClogs;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.joc.RunningLogEvents;
import com.sos.joc.model.joc.RunningLogFilter;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.StreamingOutput;

@jakarta.ws.rs.Path("joc")
public class LogImpl extends JOCResourceImpl implements ILogResource, IJocLog {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogImpl.class);
    
    private static final String API_CALL = "./joc/log";
    private static final String API_CALL_RUNNING = API_CALL + "/running";
    private static final String logDirectory = "logs";
    private static final String currentLogFileName = "joc.log";
    
    private Lock lock = new ReentrantLock();
    private Condition condition = null;
    private volatile AtomicBoolean eventArrived = new AtomicBoolean(false);
    private static final Marker NOT_NOTIFY_LOGGER = WebserviceConstants.NOT_NOTIFY_LOGGER;

    @Override
    public JOCDefaultResponse postLog(String accessToken, byte[] filterBytes) {
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken, CategoryType.CONTROLLER);
            JOClog jocLog = Globals.objectMapper.readValue(filterBytes, JOClog.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).map(p -> p.getGetLog()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            return postLog(accessToken, jocLog);
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    @Override
    public JOCDefaultResponse postLogs(String accessToken) {
        try {
            initLogging(API_CALL + "s", "{}".getBytes(), accessToken, CategoryType.OTHERS);

            Path logDir = Paths.get(logDirectory);
            if (!Files.exists(logDir)) {
                throw new FileNotFoundException("Couldn't find JOC Cockpit logs directory:" + toAbsolutePath(logDir));
            }

            List<String> filenames = new ArrayList<String>();
            Predicate<String> pattern = Pattern.compile("^(joc.log|joc-.*\\.log\\.gz)$").asPredicate();
            for (Path logFile : getFileListStream(logDir, pattern)) {
                filenames.add(logFile.getFileName().toString());
            }
            filenames.sort(Comparator.reverseOrder());
            JOClogs entity = new JOClogs();
            entity.setFilenames(filenames);

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    @Override
    public JOCDefaultResponse getLog(String accessToken, String queryAccessToken, String filename) {
        try {
            if (accessToken == null) {
                accessToken = queryAccessToken;
            }
            if (filename != null) {
                String s = "{\"filename\":\"" + filename + "\"}";
                initLogging(API_CALL, s.getBytes(StandardCharsets.UTF_8), accessToken, CategoryType.CONTROLLER);
            } else {
                initLogging(API_CALL, "{}".getBytes(), accessToken, CategoryType.CONTROLLER);
            }
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).map(p -> p.getGetLog()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            JOClog jocLog = new JOClog();
            jocLog.setFilename(filename);
            return postLog(accessToken, jocLog);
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    @Override
    public JOCDefaultResponse postRunningLog(String accessToken, String acceptEncoding, byte[] filterBytes) {
        RunningJocLog runnigJocLog = RunningJocLog.getInstance();
        try {
            filterBytes = initLogging(API_CALL_RUNNING, filterBytes, accessToken, CategoryType.CONTROLLER);
            JsonValidator.validateFailFast(filterBytes, RunningLogFilter.class);
            RunningLogEvents jocLog = Globals.objectMapper.readValue(filterBytes, RunningLogEvents.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).map(p -> p.getGetLog()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            jocLog.setLogEvents(Collections.emptyList());

            if (runnigJocLog.hasEvents(jocLog.getEventId())) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e1) {
                }
                jocLog = runnigJocLog.getRunningLog(jocLog);
            } else {
                runnigJocLog.register(this);
                condition = lock.newCondition();
                waitingForEvents(TimeUnit.SECONDS.toMillis(57)); // < 1min to avoid gateway error behind reverse proxy

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(NOT_NOTIFY_LOGGER, "[end of waiting events]eventArrived=" + eventArrived.get());
                }
                if (eventArrived.get()) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e1) {
                    }
                    jocLog = runnigJocLog.getRunningLog(jocLog);
                }
            }
            
            if (jocLog.getLogEvents().size() < 100) {
                return responseStatus200(Globals.objectMapper.writeValueAsBytes(jocLog));
            }
            
            return responseRunningLog(acceptEncoding, jocLog); //with StreamingOutput for big responses
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            runnigJocLog.unRegister(this);
        }
    }
    
    private JOCDefaultResponse responseRunningLog(String acceptEncoding, RunningLogEvents jocLog) {

        boolean withGzipEncoding = acceptEncoding != null && acceptEncoding.contains("gzip");
        StreamingOutput entityStream = new StreamingOutput() {

            @Override
            public void write(OutputStream output) throws IOException {
                if (withGzipEncoding) {
                    output = new GZIPOutputStream(output);
                }
                try {
                    Globals.objectMapper.writeValue(output, jocLog);
                } finally {
                    try {
                        output.close();
                    } catch (Exception e) {
                    }
                }
            }
        };
        return JOCDefaultResponse.responseStatus200(entityStream, MediaType.APPLICATION_JSON, getGzipHeaders(withGzipEncoding), getJocAuditTrail());
    }
    
    private Map<String, Object> getGzipHeaders(boolean withGzipEncoding) {
        Map<String, Object> headers = new HashMap<String, Object>();
        if (withGzipEncoding) {
            headers.put("Content-Encoding", "gzip");
        }
        headers.put("Transfer-Encoding", "chunked");
        return headers;
    }

    private JOCDefaultResponse postLog(String accessToken, JOClog jocLog) throws FileNotFoundException {
        Path logDir = Paths.get(logDirectory);
        if (!Files.exists(logDir)) {
            throw new FileNotFoundException("Couldn't find JOC Cockpit logs directory:" + toAbsolutePath(logDir));
        }

        String logFilename = (jocLog.getFilename() != null && !jocLog.getFilename().isEmpty()) ? jocLog.getFilename() : currentLogFileName;
        final Path log = logDir.resolve(logFilename);
        if (!Files.isReadable(log)) {
            throw new FileNotFoundException("JOC Cockpit log is not readable: " + toAbsolutePath(log));
        }

        StreamingOutput fileStream = new StreamingOutput() {

            @Override
            public void write(OutputStream output) throws IOException {
                InputStream in = null;
                try {
                    in = Files.newInputStream(log);
                    byte[] buffer = new byte[4096];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        output.write(buffer, 0, length);
                    }
                    output.flush();
                } finally {
                    try {
                        output.close();
                    } catch (Exception e) {
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Exception e) {
                        }
                    }
                }
            }
        };

        // log file could be already compressed
        if (logFilename.endsWith(".gz")) {
            return responseOctetStreamDownloadStatus200(fileStream, log.getFileName().toString(), 0L);
        }
        return responseOctetStreamDownloadStatus200(fileStream, log.getFileName().toString());
    }

    private static String toAbsolutePath(Path p) {
        return p.toString().replace('\\', '/');
    }

    private static DirectoryStream<Path> getFileListStream(final Path folder, final Predicate<String> pattern) throws IOException {

        if (folder == null) {
            throw new FileNotFoundException("JOC Cockpit logs directory is not specified!!");
        }
        if (!Files.isDirectory(folder)) {
            throw new FileNotFoundException("JOC Cockpit logs directory does not exist: " + folder);
        }

        return Files.newDirectoryStream(folder, path -> {
            if (Files.isDirectory(path)) {
                return false;
            }
            if (Files.size(path) == 0) {
                return false;
            }
            if (!Files.isReadable(path)) {
                return false;
            }
            return pattern.test(path.getFileName().toString());
        });
    }
    
    public void signalArrivedEvent() {
        eventArrived.set(true);
        signalEvent();
    }
    
    private void waitingForEvents(long maxDelay) {
        try {
            if (condition != null && lock.tryLock(200L, TimeUnit.MILLISECONDS)) { // with timeout
                try {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(NOT_NOTIFY_LOGGER, "[waitingForEvents]await " + condition.hashCode());
                    }
                    condition.await(maxDelay, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e1) {
                } finally {
                    try {
                        lock.unlock();
                    } catch (IllegalMonitorStateException e) {
                        LOGGER.warn(NOT_NOTIFY_LOGGER, "IllegalMonitorStateException at unlock lock after await");
                    }
                }
            }
        } catch (InterruptedException e) {
        }
    }
    
    private synchronized void signalEvent() {
        try {
            LOGGER.debug(NOT_NOTIFY_LOGGER, "[signalEvent]" + (condition != null));
            if (condition != null && lock.tryLock(2L, TimeUnit.SECONDS)) { // with timeout
                try {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(NOT_NOTIFY_LOGGER, "[signalEvent]signalAll" + condition.hashCode());
                    }
                    condition.signalAll();
                } finally {
                    try {
                        lock.unlock();
                    } catch (IllegalMonitorStateException e) {
                        LOGGER.warn(NOT_NOTIFY_LOGGER, "IllegalMonitorStateException at unlock lock after signal");
                    }
                }
            } else {
                LOGGER.warn(NOT_NOTIFY_LOGGER, "signalEvent failed");
            }
        } catch (InterruptedException e) {
            LOGGER.warn(NOT_NOTIFY_LOGGER, "[signalEvent]" + e.toString());
        }
    }

}
