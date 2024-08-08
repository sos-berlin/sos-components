package com.sos.js7.converter.autosys.input;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.input.AFileParser.FileType;
import com.sos.js7.converter.autosys.output.js7.helper.Report;
import com.sos.js7.converter.commons.config.items.ParserConfig;
import com.sos.js7.converter.commons.report.ParserReport;

public class DirectoryParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryParser.class);

    public static DirectoryParserResult parse(ParserConfig config, AFileParser parser, Path input) {
        DirectoryParserResult r = new DirectoryParser().new DirectoryParserResult(input, parser.getFileType());

        String method = "parse";
        if (Files.exists(input)) {
            try {
                JILJobParser jilParser = (parser instanceof JILJobParser) ? (JILJobParser) parser : null;
                Path parent = null;
                if (Files.isDirectory(input)) {
                    if (jilParser != null) {
                        parent = input.getParent();
                        jilParser.writeXMLStart(parent, input.getFileName().toString());
                    }

                    try (Stream<Path> stream = Files.walk(input)) {
                        for (Path p : stream.filter(f -> !f.equals(input) && f.getFileName().toString().toUpperCase().endsWith("." + parser
                                .getFileType().toString())).collect(Collectors.toList())) {
                            File f = p.toFile();
                            if (f.isFile()) {
                                if (jilParser != null) {
                                    parser.parse(r, p);
                                } else {
                                    r.addCountFiles();
                                    r.addJobs(parser.parse(r, p));
                                }
                            }
                        }
                    }
                } else {
                    if (jilParser != null) {
                        try {
                            parent = input.getParent().getParent();
                        } catch (Throwable e) {
                            parent = input.getParent();
                        }
                        jilParser.writeXMLStart(parent, input.getFileName().toString());
                        parser.parse(r, input);
                    } else {
                        r.addCountFiles();
                        r.addJobs(parser.parse(r, input));
                    }
                }

                if (jilParser != null) {
                    jilParser.writeXMLEnd();

                    Report.writeJILParserDuplicatesReport(parent);

                    int totalDuplicates = 0;
                    for (Map.Entry<String, Map<Path, Integer>> e : JILJobParser.INSERT_JOBS.entrySet()) {
                        int counter = 0;
                        for (Map.Entry<Path, Integer> v : e.getValue().entrySet()) {
                            counter += v.getValue();
                        }
                        if (counter > 1) {

                            totalDuplicates += (counter - 1);

                            LOGGER.info("[DUPLICATE][insert_job]" + e.getKey());
                            for (Map.Entry<Path, Integer> v : e.getValue().entrySet()) {
                                LOGGER.info("    [" + v.getKey() + "]" + v.getValue());
                            }
                        }
                    }
                    LOGGER.info("[TOTAL][DUPLICATE][insert_job]" + totalDuplicates);
                    LOGGER.info("[TOTAL][WITHOUT DUPLICATES]" + (JILJobParser.COUNTER_INSERT_JOB - totalDuplicates));
                    JILJobParser.INSERT_JOBS.clear();

                    LOGGER.info("[parse][" + jilParser.getXMLFile() + "]start...");
                    r = DirectoryParser.parse(config, new XMLJobParser(jilParser.getConfig(), jilParser.getReportDir()), jilParser.getXMLFile());
                    LOGGER.info("[parse][" + jilParser.getXMLFile() + "]end");
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

        private final Path input;
        private final FileType parserType;

        private Set<String> jobNames = new HashSet<>();
        private List<ACommonJob> jobs = new ArrayList<>();
        private int countFiles = 0;

        private DirectoryParserResult(Path input, FileType parserType) {
            this.input = input;
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

        public Path getInput() {
            return input;
        }

        public void reset() {
            jobNames = new HashSet<>();
            jobs = new ArrayList<>();
            countFiles = 0;
        }
    }
}
