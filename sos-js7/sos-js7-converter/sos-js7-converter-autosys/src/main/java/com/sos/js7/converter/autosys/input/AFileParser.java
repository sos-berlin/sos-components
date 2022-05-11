package com.sos.js7.converter.autosys.input;

import java.nio.file.Path;
import java.util.List;

import com.sos.js7.converter.autosys.common.v12.JobParser;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;

public abstract class AFileParser {

    public enum FileType {
        JIL, XML
    }

    private final FileType fileType;
    private final JobParser jobParser = new JobParser();

    public AFileParser(FileType fileType) {
        this.fileType = fileType;
    }

    public abstract List<ACommonJob> parse(Path file);

    public FileType getFileType() {
        return fileType;
    }

    public JobParser getJobParser() {
        return jobParser;
    }

}
