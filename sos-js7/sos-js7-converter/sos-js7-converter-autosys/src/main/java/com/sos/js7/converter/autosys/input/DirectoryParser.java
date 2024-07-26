package com.sos.js7.converter.autosys.input;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.input.AFileParser.FileType;
import com.sos.js7.converter.commons.config.items.ParserConfig;
import com.sos.js7.converter.commons.report.ParserReport;

public class DirectoryParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryParser.class);

    public static DirectoryParserResult parse(ParserConfig config, AFileParser parser, Path input) {
        DirectoryParserResult r = new DirectoryParser().new DirectoryParserResult(parser.getFileType());

        String method = "parse";
        if (Files.exists(input)) {
            try {
                if (Files.isDirectory(input)) {
                    try (Stream<Path> stream = Files.walk(input)) {
                        for (Path p : stream.filter(f -> !f.equals(input) && f.getFileName().toString().toUpperCase().endsWith("." + parser
                                .getFileType().toString())).collect(Collectors.toList())) {
                            File f = p.toFile();
                            if (f.isFile()) {
                                r.addCountFiles();
                                r.addJobs(parser.parse(r, p));
                            }
                        }
                    }
                } else {
                    r.addCountFiles();
                    r.addJobs(parser.parse(r, input));
                }
                LOGGER.info(String.format("[%s][total files=%s, main jobs=%s]", method, r.getCountFiles(), r.getJobs().size()));
            } catch (Throwable e) {
                LOGGER.warn(String.format("[%s]%s", method, e.toString()), e);
                ParserReport.INSTANCE.addErrorRecord(input, null, e);
            }
        } else {
            LOGGER.info(String.format("[%s][not found]%s", method, input));
        }
        return r;
    }

    public class DirectoryParserResult {

        private final FileType parserType;

        private Set<String> jobNames = new HashSet<>();
        private List<ACommonJob> jobs = new ArrayList<>();
        private int countFiles = 0;

        private DirectoryParserResult(FileType parserType) {
            this.parserType = parserType;
        }

        protected void addCountFiles() {
            countFiles++;
        }

        protected void addJobs(FileParserResult r) {
            jobs.addAll(r.getAllJobs());
        }

        public Set<String> getJobNames() {
            return jobNames;
        }

        public List<ACommonJob> getJobs() {
            return jobs;
        }

        public int getCountFiles() {
            return countFiles;
        }

        public boolean isXMLParser() {
            return FileType.XML.equals(parserType);
        }

        public void reset() {
            jobNames = new HashSet<>();
            jobs = new ArrayList<>();
            countFiles = 0;
        }
    }
}
