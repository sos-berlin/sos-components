package com.sos.joc.classes.documentation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.beans.SOSCommandResult;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocConfigurationException;

public class UploadFileSanitizer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadFileSanitizer.class);
    private boolean withUploadFileSanitizing = false;;
    private String uploadFileSanitizerCommand;
    private static final Path workdir = Paths.get("logs/sanitizer");
    private static final Path shellWorkdir = Paths.get("resources/joc");
    private static Path tmpWorkdir;
    
    public UploadFileSanitizer(String threadName) {
        
        uploadFileSanitizerCommand = Globals.sosCockpitProperties.getProperty("upload_file_sanitizer", "").trim();
        withUploadFileSanitizing = !uploadFileSanitizerCommand.isEmpty();
        
        if (withUploadFileSanitizing) {
            try {
                String uploadFileSanitizerExecutable = uploadFileSanitizerCommand.replaceFirst("^\"?([^\"]+)\"?", "$1");
                Path p = Globals.sosCockpitProperties.resolvePath(uploadFileSanitizerExecutable);
                if (!Files.exists(p)) {
                    throw new JocConfigurationException("Sanitizer not found: " + uploadFileSanitizerExecutable);
                }
                if(!uploadFileSanitizerCommand.contains("${source_file}")) {
                    throw new JocConfigurationException("missing '${source_file}' in command");
                }
                if(!uploadFileSanitizerCommand.contains("${destination_file}")) {
                    throw new JocConfigurationException("missing '${destination_file}' in command");
                }
                tmpWorkdir = workdir.resolve(threadName);
                Files.createDirectories(tmpWorkdir);
            } catch (Exception e) {
                withUploadFileSanitizing = false;
                LOGGER.warn("Sanitizing for uploaded file not possible.", e);
            }
        }
    }
    
    public byte[] sanitize(String filename, ByteArrayOutputStream stream) throws JocBadRequestException, IOException {
        if (!withUploadFileSanitizing) {
            return stream.toByteArray();
        }
        try {
            Path sourceFile = tmpWorkdir.resolve(filename);
            try(OutputStream outputStream = Files.newOutputStream(sourceFile)) {
                stream.writeTo(outputStream);
            }
            return sanitize(filename, sourceFile);
        } catch (JocBadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new JocBadRequestException("Upload file sanitizer error", e);
        }
    }
    
    public byte[] sanitize(String filename, InputStream stream) throws JocBadRequestException, IOException {
        if (!withUploadFileSanitizing) {
            return IOUtils.toByteArray(stream);
        }
        try {
            Path sourceFile = tmpWorkdir.resolve(filename);
            Files.copy(stream, sourceFile, StandardCopyOption.REPLACE_EXISTING);
            return sanitize(filename, sourceFile);
        } catch (JocBadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new JocBadRequestException("Upload file sanitizer error", e);
        }
    }
    
    public void clean() {
        try {
            SOSPath.deleteIfExists(tmpWorkdir);
            if (Files.list(workdir).count() == 0) {
                Files.deleteIfExists(workdir);
            }
        } catch (Exception e) {
            //
        }
    }
    
    private byte[] sanitize(String filename, Path sourceFile) throws IOException {
        Path destFile = tmpWorkdir.resolve("sanitizer-dest-" + filename);
        String command = uploadFileSanitizerCommand.replaceAll("\\$\\{source_file\\}", sourceFile.toAbsolutePath().toString()).replaceAll(
                "\\$\\{destination_file\\}", destFile.toAbsolutePath().toString());
        SOSCommandResult result = SOSShell.executeCommand(command, null, null, null, shellWorkdir);
        if (result.hasError()) {
            if (result.hasStdErr()) {
                throw new JocBadRequestException(String.format("Upload file sanitizer '%s': exit %d, stderr: %s", filename, result.getExitCode(),
                        result.getStdErr()));
            } else {
                throw new JocBadRequestException(String.format("Upload file sanitizer '%s': exit %d", filename, result.getExitCode()));
            }
        }
        byte[] ret = null;
        if (!Files.exists(destFile)) {
            ret =  Files.readAllBytes(sourceFile);
        }
        ret = Files.readAllBytes(destFile);
        
        Files.deleteIfExists(sourceFile);
        Files.deleteIfExists(destFile);
        
        return ret;
    }

}
