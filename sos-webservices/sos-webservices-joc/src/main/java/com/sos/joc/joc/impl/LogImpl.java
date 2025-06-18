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
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.joc.resource.ILogResource;
import com.sos.joc.model.JOClog;
import com.sos.joc.model.JOClogs;
import com.sos.joc.model.audit.CategoryType;

import jakarta.ws.rs.core.StreamingOutput;

@jakarta.ws.rs.Path("joc")
public class LogImpl extends JOCResourceImpl implements ILogResource {

    private static final String API_CALL = "./joc/log";
    private static final String logDirectory = "logs";
    private static final String currentLogFileName = "joc.log";

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

}
