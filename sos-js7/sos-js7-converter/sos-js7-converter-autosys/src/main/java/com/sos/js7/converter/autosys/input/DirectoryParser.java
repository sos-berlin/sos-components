package com.sos.js7.converter.autosys.input;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.commons.report.ParserReport;

public class DirectoryParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryParser.class);

    public static DirectoryParserResult parse(AFileParser parser, Path dir) {
        DirectoryParserResult r = new DirectoryParser().new DirectoryParserResult();

        String method = "parse";
        if (Files.exists(dir)) {
            try {
                try (Stream<Path> stream = Files.walk(dir)) {
                    for (Path p : stream.filter(f -> !f.equals(dir) && f.getFileName().toString().toUpperCase().endsWith("." + parser.getFileType()
                            .toString())).collect(Collectors.toList())) {
                        File f = p.toFile();
                        if (f.isFile()) {
                            r.addCountFiles();
                            r.addJobs(parser.parse(p));
                        }
                    }
                }
                LOGGER.info(String.format("[%s][total files=%s, main jobs=%s]", method, r.getCountFiles(), r.getJobs().size()));
            } catch (Throwable e) {
                LOGGER.warn(String.format("[%s]%s", method, e.toString()), e);
                ParserReport.INSTANCE.addErrorRecord(dir, null, e);
            }
        } else {
            LOGGER.info(String.format("[%s][not found]%s", method, dir));
        }
        return r;
    }

    public class DirectoryParserResult {

        private List<ACommonJob> jobs = new ArrayList<>();
        private int countFiles = 0;

        protected void addCountFiles() {
            countFiles++;
        }

        protected void addJobs(Collection<ACommonJob> c) {
            jobs.addAll(c);
        }

        public List<ACommonJob> getJobs() {
            return jobs;
        }

        public int getCountFiles() {
            return countFiles;
        }
    }
}
