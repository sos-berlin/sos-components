package com.sos.js7.converter.autosys.input;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.js7.converter.autosys.common.v12.JobParser;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.JobBOX;
import com.sos.js7.converter.autosys.config.AutosysConverterConfig;
import com.sos.js7.converter.autosys.input.DirectoryParser.DirectoryParserResult;

public abstract class AFileParser {

    public enum ParserType {
        JIL, XML
    }

    public static final String EXPORT_FILE_PREFIX_BOX = "[BOX]";
    public static final String EXPORT_FILE_PREFIX_STANDALONE = "[ST]";

    private final ParserType parserType;
    private final JobParser jobParser = new JobParser();
    private final AutosysConverterConfig config;
    private final Path reportDir;
    private final boolean references;
    private final Set<String> extensions;

    public AFileParser(ParserType parserType, AutosysConverterConfig config, Path reportDir, boolean references) {
        this.parserType = parserType;
        this.config = config;
        this.reportDir = reportDir;
        this.references = references;
        this.extensions = new HashSet<>();
    }

    public abstract FileParserResult parse(DirectoryParserResult r, Path file);

    public ParserType getParserType() {
        return parserType;
    }

    public boolean isReferences() {
        return references;
    }

    public Set<String> getExtensions() {
        return extensions;
    }

    public boolean acceptFile(Path file) {
        String fileName = file.getFileName().toString().toLowerCase();
        if (config.getParserConfig().hasExcludedFileNames()) {
            for (String fn : config.getParserConfig().getExcludedFileNames()) {
                if (fn.equalsIgnoreCase(fileName)) {
                    return false;
                }
            }
        }
        for (String ex : extensions) {
            if (fileName.endsWith("." + ex.toLowerCase())) {
                return true;
            }
        }
        return false;
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

        private Map<String, JobBOX> mainJobs = new LinkedHashMap<>();
        private Map<String, Integer> mainJobsDuplicates = new LinkedHashMap<>();
        private Map<String, Integer> childrenJobsDuplicates = new LinkedHashMap<>();

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
