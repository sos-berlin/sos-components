package com.sos.js7.converter.js1.input;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.converter.commons.JS7ConverterConfig.ParserConfig;
import com.sos.js7.converter.commons.report.ParserReport;
import com.sos.js7.converter.js1.common.EConfigFileExtensions;
import com.sos.js7.converter.js1.common.Folder;

public class DirectoryParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryParser.class);

    public static DirectoryParserResult parse(ParserConfig config, Path dir) {
        DirectoryParserResult r = new DirectoryParser().new DirectoryParserResult();

        String method = "parse";
        if (Files.exists(dir)) {
            try {
                boolean checkExcluded = !config.isEmpty();

                r.setRoot(parseSingleDir(r, new Folder(dir)));
                try (Stream<Path> stream = Files.walk(dir)) {
                    for (Path p : stream.filter(f -> Files.isDirectory(f) && !f.equals(dir)).collect(Collectors.toList())) {
                        if (checkExcluded) {
                            if (config.getExcludedDirectoryNames().contains(p.getFileName().toString())) {
                                LOGGER.info(String.format("[%s][%s][skip]because excluded", method, p));
                                continue;
                            }
                        }

                        Folder fp = r.getRoot().findParent(p);
                        if (fp != null) {
                            fp.addFolder(parseSingleDir(r, new Folder(p)));
                        }
                    }
                }
            } catch (Throwable e) {
                LOGGER.error(String.format("[%s]%s", method, e.toString()), e);
                ParserReport.INSTANCE.addErrorRecord(dir, null, e);
            }
        } else {
            LOGGER.info(String.format("[%s][not found]%s", method, dir));
        }

        ParserReport.INSTANCE.addSummaryRecord("TOTAL FOLDERS", r.getCountFolders());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL JOB files", r.getCountJobs() + ", STANDALONE=" + r.getCountStandaloneJobs() + ", ORDER=" + r
                .getCountOrderJobs());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL JOB CHAIN files", r.getCountJobChains());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL JOB CHAIN ORDER files", r.getCountOrders());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL JOB CHAIN CONFIG files", r.getCountJobChainConfigs());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL LOCK files", r.getCountLocks());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL PROCESS CLASS files", r.getCountProcessClasses());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL SCHEDULE files", r.getCountSchedules());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL MONITOR files", r.getCountMonitors());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL ANOTHER files", r.getCountFiles());

        return r;
    }

    private static Folder parseSingleDir(DirectoryParserResult r, Folder folder) {
        r.addCountFolders();

        String method = "parseSingleDir";
        File[] files = folder.getPath().toAbsolutePath().toFile().listFiles(f -> !f.isDirectory());
        if (files.length == 0) {
            return folder;
        }

        Map<String, List<Path>> jobChains = new HashMap<>();
        for (File file : files) {
            String fileName = file.getName();
            if (fileName.endsWith(EConfigFileExtensions.ORDER.extension())) {
                r.addCountOrders();
                jobChains = addJobChainFile(jobChains, file, EConfigFileExtensions.getName(EConfigFileExtensions.ORDER, fileName));
            } else if (fileName.endsWith(EConfigFileExtensions.JOB_CHAIN.extension())) {
                r.addCountJobChains();
                jobChains = addJobChainFile(jobChains, file, EConfigFileExtensions.getName(EConfigFileExtensions.JOB_CHAIN, fileName));
            } else if (fileName.endsWith(EConfigFileExtensions.JOB_CHAIN_CONFIG.extension())) {
                r.addCountJobChainConfigs();
                jobChains = addJobChainFile(jobChains, file, EConfigFileExtensions.getName(EConfigFileExtensions.JOB_CHAIN_CONFIG, fileName));
            } else if (fileName.endsWith(EConfigFileExtensions.JOB.extension())) {
                r.addCountJobs();
                folder.addJob(r, file.toPath());
            } else if (fileName.endsWith(EConfigFileExtensions.LOCK.extension())) {
                r.addCountLocks();
                try {
                    folder.addLock(file.toPath());
                } catch (Exception e) {
                    LOGGER.error(String.format("[%s]%s", method, e.toString()), e);
                    ParserReport.INSTANCE.addErrorRecord(file.toPath(), null, e);
                }
            } else if (fileName.endsWith(EConfigFileExtensions.PROCESS_CLASS.extension())) {
                r.addCountProcessClasses();
                try {
                    folder.addProcessClass(file.toPath());
                } catch (Exception e) {
                    LOGGER.error(String.format("[%s]%s", method, e.toString()), e);
                    ParserReport.INSTANCE.addErrorRecord(file.toPath(), null, e);
                }
            } else if (fileName.endsWith(EConfigFileExtensions.SCHEDULE.extension())) {
                r.addCountSchedules();
                try {
                    folder.addSchedule(r, file.toPath());
                } catch (Exception e) {
                    LOGGER.error(String.format("[%s]%s", method, e.toString()), e);
                    ParserReport.INSTANCE.addErrorRecord(file.toPath(), null, e);
                }
            } else if (fileName.endsWith(EConfigFileExtensions.MONITOR.extension())) {
                r.addCountMonitors();
                folder.addMonitor(null);
            } else {
                r.addCountFiles();
                folder.addFile(file.toPath());
            }
        }

        for (Map.Entry<String, List<Path>> entry : jobChains.entrySet()) {
            try {
                folder.addJobChain(r, entry.getKey(), entry.getValue());
            } catch (Throwable e) {
                LOGGER.error(String.format("[%s]%s", method, e.toString()), e);
                ParserReport.INSTANCE.addErrorRecord(folder.getPath(), "job chain=" + entry.getKey(), e);
            }
        }

        r.addCountStandaloneJobs(folder.getStandaloneJobs().size());
        r.addCountOrderJobs(folder.getOrderJobs().size());

        return folder;
    }

    private static Map<String, List<Path>> addJobChainFile(Map<String, List<Path>> jobChains, File file, String jobChainName) {
        List<Path> al;
        if (jobChains.containsKey(jobChainName)) {
            al = jobChains.get(jobChainName);
        } else {
            al = new ArrayList<>();
        }
        al.add(file.toPath());
        jobChains.put(jobChainName, al);
        return jobChains;
    }

    public class DirectoryParserResult {

        private Folder root;

        private int countFolders = 0;
        private int countOrders = 0;
        private int countJobChains = 0;
        private int countJobChainConfigs = 0;
        private int countJobs = 0;
        private int countStandaloneJobs = 0;
        private int countOrderJobs = 0;
        private int countLocks = 0;
        private int countProcessClasses = 0;
        private int countSchedules = 0;
        private int countMonitors = 0;
        private int countFiles = 0;

        protected void setRoot(Folder f) {
            root = f;
        }

        protected void addCountFolders() {
            countFolders++;
        }

        protected void addCountOrders() {
            countOrders++;
        }

        protected void addCountJobChains() {
            countJobChains++;
        }

        protected void addCountJobChainConfigs() {
            countJobChainConfigs++;
        }

        protected void addCountJobs() {
            countJobs++;
        }

        protected void addCountStandaloneJobs(int size) {
            countStandaloneJobs += size;
        }

        protected void addCountOrderJobs(int size) {
            countOrderJobs += size;
        }

        protected void addCountLocks() {
            countLocks++;
        }

        protected void addCountProcessClasses() {
            countProcessClasses++;
        }

        protected void addCountSchedules() {
            countSchedules++;
        }

        protected void addCountMonitors() {
            countMonitors++;
        }

        protected void addCountFiles() {
            countFiles++;
        }

        public Folder getRoot() {
            return root;
        }

        public int getCountFolders() {
            return countFolders;
        }

        public int getCountOrders() {
            return countOrders;
        }

        public int getCountJobChains() {
            return countJobChains;
        }

        public int getCountJobChainConfigs() {
            return countJobChainConfigs;
        }

        public int getCountJobs() {
            return countJobs;
        }

        public int getCountStandaloneJobs() {
            return countStandaloneJobs;
        }

        public int getCountOrderJobs() {
            return countOrderJobs;
        }

        public int getCountLocks() {
            return countLocks;
        }

        public int getCountProcessClasses() {
            return countProcessClasses;
        }

        public int getCountSchedules() {
            return countSchedules;
        }

        public int getCountMonitors() {
            return countMonitors;
        }

        public int getCountFiles() {
            return countFiles;
        }

    }
}
