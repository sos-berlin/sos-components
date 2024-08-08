package com.sos.js7.converter.autosys.input;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sos.js7.converter.autosys.common.v12.JobParser;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.JobBOX;
import com.sos.js7.converter.autosys.config.AutosysConverterConfig;
import com.sos.js7.converter.autosys.input.DirectoryParser.DirectoryParserResult;

public abstract class AFileParser {

    public enum FileType {
        JIL, XML
    }

    public static final String EXPORT_FILE_PREFIX_BOX = "[BOX]";
    public static final String EXPORT_FILE_PREFIX_STANDALONE = "[ST]";

    private final FileType fileType;
    private final JobParser jobParser = new JobParser();
    private final AutosysConverterConfig config;
    private final Path reportDir;

    public AFileParser(FileType fileType, AutosysConverterConfig config, Path reportDir) {
        this.fileType = fileType;
        this.config = config;
        this.reportDir = reportDir;
    }

    public abstract FileParserResult parse(DirectoryParserResult r, Path file);

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

    public class BoxJobsHandler {

        private Map<String, JobBOX> mainJobs = new HashMap<>();
        private Map<String, Integer> mainJobsDuplicates = new HashMap<>();
        private Map<String, Integer> childrenJobsDuplicates = new HashMap<>();

        public void addMain(ACommonJob job) {
            String jobName = job.getName();
            if (mainJobs.containsKey(jobName)) {
                Integer count = mainJobsDuplicates.get(jobName);
                if (count == null) {
                    count = 0;
                }
                count++;
                mainJobsDuplicates.put(jobName, count);
            }
            mainJobs.put(jobName, (JobBOX) job);
        }

        public void addChild(String boxName, ACommonJob job) {
            JobBOX m = mainJobs.get(boxName);
            if (m == null) {
                return;
            }
            if (m.getJobs() == null) {
                m.setJobs(new ArrayList<>());
                m.getJobs().add(job);
            } else {
                List<ACommonJob> l = m.getJobs().stream().filter(e -> e.isNameEquals(job)).collect(Collectors.toList());
                if (l != null && l.size() > 0) {
                    childrenJobsDuplicates.put(job.getName(), l.size());
                } else {
                    m.getJobs().add(job);
                }
            }

        }

        public Map<String, JobBOX> getMainJobs() {
            return mainJobs;
        }

        public Map<String, Integer> getMainJobsDuplicates() {
            return mainJobsDuplicates;
        }

        public Map<String, Integer> getChildrenJobsDuplicates() {
            return childrenJobsDuplicates;
        }

    }

}
