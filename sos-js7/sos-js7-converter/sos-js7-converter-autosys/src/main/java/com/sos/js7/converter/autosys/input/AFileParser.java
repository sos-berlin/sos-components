package com.sos.js7.converter.autosys.input;

import java.nio.file.Path;
import java.util.List;

import com.sos.js7.converter.autosys.common.v12.JobParser;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.config.AutosysConverterConfig;

public abstract class AFileParser {

    public enum FileType {
        JIL, XML
    }

    private final FileType fileType;
    private final JobParser jobParser = new JobParser();
    private final AutosysConverterConfig config;
    private final Path reportDir;

    public AFileParser(FileType fileType, AutosysConverterConfig config, Path reportDir) {
        this.fileType = fileType;
        this.config = config;
        this.reportDir = reportDir;
    }

    public abstract List<ACommonJob> parse(Path file);

    public FileType getFileType() {
        return fileType;
    }

    public JobParser getJobParser() {
        return jobParser;
    }
    
    public AutosysConverterConfig getConfig() {
        return config;
    }

    public Path getReportDir() {
        return reportDir;
    }

}
