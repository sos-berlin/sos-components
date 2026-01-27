package com.sos.js7.converter.autosys.input;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.input.AFileParser.ParserType;
import com.sos.js7.converter.autosys.output.js7.helper.Report;
import com.sos.js7.converter.commons.config.items.ParserConfig;
import com.sos.js7.converter.commons.report.ParserReport;

public class DirectoryParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryParser.class);

    public static DirectoryParserResult parse(ParserConfig config, AFileParser parser, Path input, Path reportDir) {
        DirectoryParserResult r = new DirectoryParser().new DirectoryParserResult(input, parser.getParserType());

        String method = "parse";
        if (Files.exists(input)) {
            try {
                JILJobParser jilParser = (parser instanceof JILJobParser) ? (JILJobParser) parser : null;
                boolean inputIsDirectory = false;
                if (Files.isDirectory(input)) {
                    inputIsDirectory = true;
                    if (jilParser != null) {
                        jilParser.writeXMLStart(reportDir, input.getFileName().toString());
                    }

                    try (Stream<Path> stream = Files.walk(input)) {
                        for (Path p : stream.filter(f -> !f.equals(input) && parser.acceptFile(f)).collect(Collectors.toList())) {
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
                        jilParser.writeXMLStart(reportDir, input.getFileName().toString());
                        if (parser.acceptFile(input)) {
                            parser.parse(r, input);
                        }
                    } else {
                        r.addCountFiles();
                        r.addJobs(parser.parse(r, input));
                    }
                }

                if (jilParser != null) {
                    jilParser.writeXMLEnd();

                    if (!jilParser.isReferences()) {

                        Report.writeJILParserDuplicatesReport(reportDir);
                        Report.writeJILParserMultipleAttributes(reportDir);

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
                    }
                    JILJobParser.INSERT_JOBS.clear();

                    LOGGER.info("[parse][xml][created by the JS7 converter][" + jilParser.getXMLFile() + "]start...");
                    XMLJobParser xmlParser = new XMLJobParser(jilParser.getConfig(), jilParser.getReportDir(), jilParser.isReferences());
                    r = DirectoryParser.parse(config, xmlParser, jilParser.getXMLFile(), reportDir);
                    LOGGER.info("[parse][xml][created by the JS7 converter][" + jilParser.getXMLFile() + "]end");

                    if (xmlParser.getSplitConfigurationMainDir() != null) {
                        if (inputIsDirectory) {
                            copyJILFiles2SplitConfigurationFolders(input, xmlParser.getSplitConfigurationMainDir());
                        }
                    }
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

    private static void copyJILFiles2SplitConfigurationFolders(Path inputDirWithJILFiles, Path splitConfigurationMainDir) throws Exception {
        String method = "copyJILFiles2SplitConfigurationFolders";
        List<Path> fl = SOSPath.getFolderList(splitConfigurationMainDir);
        LOGGER.info(String.format("[%s][total][application]%s", method, fl.size()));
        for (Path folder : fl) {
            String application = folder.getFileName().toString();
            LOGGER.info(String.format("[%s][application=%s]search for %s file ...", method, application, application + ".jil"));
            List<Path> jilFiles = SOSPath.getFileList(inputDirWithJILFiles, application + "\\.jil", Pattern.CASE_INSENSITIVE, true);
            LOGGER.info(String.format("    %s file(s) found", jilFiles.size()));
            for (Path jilFile : jilFiles) {
                Path jilFileCopy = folder.resolve(jilFile.getParent().getFileName() + "_" + jilFile.getFileName());
                LOGGER.info("    copy as " + jilFileCopy.getFileName());
                SOSPath.deleteIfExists(jilFileCopy);
                SOSPath.copyFile(jilFile, jilFileCopy);
            }
        }

    }

    public class DirectoryParserResult {

        private final Path input;
        private final ParserType parserType;

        private Set<String> jobNames = new HashSet<>();
        private List<ACommonJob> jobs = new ArrayList<>();
        private int countFiles = 0;

        private DirectoryParserResult(Path input, ParserType parserType) {
            this.input = input;
            this.parserType = parserType;
        }

        public void reset() {
            jobNames = new HashSet<>();
            jobs = new ArrayList<>();
            countFiles = 0;
        }

        public void mergeReferences(DirectoryParserResult other) {
            int bj = jobs.size();
            int bjn = jobNames.size();
            for (ACommonJob j : other.getJobs()) {
                if (!jobNames.contains(j.getName())) {
                    jobs.add(j);
                }
            }
            // jobNames is a HashSet - not need to be filtered
            jobNames.addAll(other.getJobNames());
            countFiles += other.getCountFiles();

            int aj = jobs.size();
            int ajn = jobNames.size();

            LOGGER.info(String.format(
                    "[mergeReferences][input jobs=%s(jobNames=%s)][references added jobs=%s(jobNames=%s)]total jobs=%s(jobNames=%s)", bj, bjn, (aj
                            - bj), (ajn - bjn), aj, ajn));
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

        public List<ACommonJob> getNonReferenceJobs() {
            return jobs.stream().filter(j -> !j.isReference()).collect(Collectors.toList());
        }

        public int getCountFiles() {
            return countFiles;
        }

        public boolean isXMLParser() {
            return ParserType.XML.equals(parserType);
        }

        public Path getInput() {
            return input;
        }

    }
}
