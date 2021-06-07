package com.sos.joc.joc.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.ws.rs.core.StreamingOutput;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.joc.resource.ILogResource;
import com.sos.joc.model.JOClog;
import com.sos.joc.model.JOClogs;

import io.vavr.control.Either;

@javax.ws.rs.Path("joc")
public class LogImpl extends JOCResourceImpl implements ILogResource {

    private static final String API_CALL = "./log";
    private static final String logDirectory = "logs";
    private static final String currentLogFileName = "joc.log";

    @Override
    public JOCDefaultResponse postLog(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JOClog jocLog = Globals.objectMapper.readValue(filterBytes, JOClog.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getGetLog());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
    
            return postLog(accessToken, jocLog);
        } catch (Exception e) {
            ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, getJocError(), null);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    @Override
    public JOCDefaultResponse postLogs(String accessToken) {
        try {
            initLogging(API_CALL + "s", null, accessToken);

            Path logDir = Paths.get(logDirectory);
            if (!Files.exists(logDir)) {
                throw new FileNotFoundException("JOC Cockpit logs directory not found:" + toAbsolutePath(logDir));
            }
            
            List<String> filenames = new ArrayList<String>();
            Predicate<String> pattern = Pattern.compile("^(joc.log|joc-.*\\.log\\.gz)$").asPredicate();
            for (Path logFile : getFileListStream(logDir, pattern)) {
                filenames.add(logFile.getFileName().toString());
            }
            filenames.sort(Comparator.reverseOrder());
            JOClogs entity = new JOClogs();
            entity.setFilenames(filenames);

            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    @Override
    public JOCDefaultResponse getLog(String accessToken, String queryAccessToken, String filename) {
        try {
            if (accessToken == null) {
                accessToken = queryAccessToken;
            }
            String s = "{\"filename\":\"" + filename + "\"}";
            initLogging(API_CALL, s.getBytes(), accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getGetLog());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            JOClog jocLog = new JOClog();
            jocLog.setFilename(filename);
            return postLog(accessToken, jocLog);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    private JOCDefaultResponse postLog(String accessToken, JOClog jocLog) throws FileNotFoundException {
        Path logDir = Paths.get(logDirectory);
        if (!Files.exists(logDir)) {
            throw new FileNotFoundException("JOC Cockpit logs directory not found:" + toAbsolutePath(logDir));
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
            return JOCDefaultResponse.responseOctetStreamDownloadStatus200(fileStream, log.getFileName().toString(), 0L);
        }
        return JOCDefaultResponse.responseOctetStreamDownloadStatus200(fileStream, log.getFileName().toString());
    }
    
    private static String toAbsolutePath(Path p) {
        return p.toString().replace('\\', '/');
    }

    private static DirectoryStream<Path> getFileListStream(final Path folder, final Predicate<String> pattern) throws IOException {

        if (folder == null) {
            throw new FileNotFoundException("JOC Cockpit logs directory not specified!!");
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
