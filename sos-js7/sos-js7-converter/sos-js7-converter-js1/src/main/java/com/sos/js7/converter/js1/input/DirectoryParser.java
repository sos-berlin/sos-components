package com.sos.js7.converter.js1.input;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSGzip;
import com.sos.commons.util.SOSPath;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.config.JS7ConverterConfig.ParserConfig;
import com.sos.js7.converter.commons.output.ZipCompress;
import com.sos.js7.converter.commons.report.ParserReport;
import com.sos.js7.converter.js1.common.EConfigFileExtensions;
import com.sos.js7.converter.js1.common.Folder;

public class DirectoryParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryParser.class);

    public static DirectoryParserResult parse(ParserConfig config, Path input, Path outputDir) throws Exception {
        DirectoryParserResult r = new DirectoryParser().new DirectoryParserResult();

        String method = "parse";
        if (Files.exists(input)) {
            boolean removeDir = false;
            Path dir = null;
            try {
                if (Files.isDirectory(input)) {
                    dir = input;
                } else {
                    dir = decompressInputFile(input, outputDir);
                    removeDir = true;
                }

                int level = 0;
                boolean checkExcludedDirectoryNames = config.hasExcludedDirectoryNames();
                boolean checkExcludedDirectoryPaths = config.hasExcludedDirectoryPaths();

                Path yadeConfiguration = dir.resolve("sos").resolve(".configuration").resolve("yade").resolve("yade.xml");
                if (Files.exists(yadeConfiguration)) {
                    r.setYadeConfiguration(yadeConfiguration);
                }

                r.setRoot(new Folder(dir));
                r.setRoot(parseFiles(r, r.getRoot()));
                r.addCountFolders();
                parseDirectory(config, r, r.getRoot(), level, checkExcludedDirectoryNames, checkExcludedDirectoryPaths);
            } catch (Throwable e) {
                LOGGER.error(String.format("[%s]%s", method, e.toString()), e);
                ParserReport.INSTANCE.addErrorRecord(input, null, e);
            } finally {
                if (removeDir && dir != null) {
                    try {
                        LOGGER.info(String.format("[temp input dir][delete]%s", dir));
                        SOSPath.deleteIfExists(dir);
                    } catch (Throwable e) {
                        LOGGER.error(String.format("[temp input dir][delete][%s]%s", dir, e.toString()), e);
                    }
                }
            }
        } else {
            throw new FileNotFoundException("[input]" + input.toString());
        }
        return r;
    }

    private static Path decompressInputFile(Path inputFile, Path outputDir) throws Exception {
        Path dir = outputDir.getParent().resolve("input-" + SOSDate.getCurrentDateTimeAsString().replaceAll(" ", "-").replaceAll(":", ""));
        boolean isTarGZ = false;

        String fn = inputFile.getFileName().toString().toLowerCase();
        if (fn.endsWith(".tar.gz")) {
            isTarGZ = true;
        } else if (fn.endsWith(".zip")) {
        } else {
            throw new Exception(String.format("[temp input dir][decompress][failed][%s]supported file extensions: .tar.gz, .zip", inputFile));
        }

        LOGGER.info(String.format("[temp input dir][decompress][%s]%s", inputFile, dir));
        if (Files.exists(dir)) {
            SOSPath.cleanupDirectory(dir);
        } else {
            Files.createDirectories(dir);
        }
        if (isTarGZ) {
            SOSGzip.decompress(inputFile, dir, false);
        } else {
            ZipCompress.decompress(inputFile, dir);
        }
        return dir;
    }

    private static Folder parseDirectory(ParserConfig config, DirectoryParserResult r, Folder parentFolder, int level,
            boolean checkExcludedDirectoryNames, boolean checkExcludedDirectoryPaths) {
        String method = "parseDirectory";
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][level=%s][parent]%s", method, level, parentFolder.getPath()));
        }
        File[] childFolders = parentFolder.getPath().toAbsolutePath().toFile().listFiles(f -> f.isDirectory());
        if (childFolders.length == 0) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][level=%s][parent][%s][skip]0 subfolders", method, level, parentFolder.getPath()));
            }
            return parentFolder;
        }

        level += 1;
        for (File childFolder : childFolders) {
            LOGGER.info(String.format("[%s][level=%s]%s", method, level, childFolder.getPath()));

            if (checkExcludedDirectoryNames) {
                if (config.getExcludedDirectoryNames().contains(childFolder.getName())) {
                    LOGGER.info(String.format("[%s][level=%s][%s][skip]because excluded name '%s'", method, level, childFolder, childFolder
                            .getName()));
                    continue;
                }
            }
            if (checkExcludedDirectoryPaths) {
                Set<String> excludedPaths = config.getExcludedDirectoryPaths().get(Integer.valueOf(level));
                if (excludedPaths != null) {
                    String found = excludedPaths.stream().filter(e -> JS7ConverterHelper.normalizeDirectoryPath(childFolder.getPath()).endsWith(e))
                            .findAny().orElse(null);
                    if (found != null) {
                        LOGGER.info(String.format("[%s][level=%s][%s][skip]because excluded path '%s'", method, level, childFolder, found));
                        continue;
                    }
                }
            }

            Folder folder = new Folder(childFolder.toPath());
            r.addCountFolders();
            parentFolder.addFolder(parseFiles(r, folder));
            parentFolder = parseDirectory(config, r, folder, level, checkExcludedDirectoryNames, checkExcludedDirectoryPaths);
        }

        return parentFolder;
    }

    private static Folder parseFiles(DirectoryParserResult r, Folder folder) {
        String method = "parseFiles";
        File[] files = folder.getPath().toAbsolutePath().toFile().listFiles(f -> !f.isDirectory());
        if (files.length == 0) {
            return folder;
        }

        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        Map<String, List<Path>> jobChains = new HashMap<>();
        for (File file : files) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s]%s", method, file.getPath()));
            }
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
                    ParserReport.INSTANCE.addErrorRecord(file.toPath(), "error on addLock", e);
                }
            } else if (fileName.endsWith(EConfigFileExtensions.PROCESS_CLASS.extension())) {
                r.addCountProcessClasses();
                try {
                    folder.addProcessClass(file.toPath());
                } catch (Exception e) {
                    LOGGER.error(String.format("[%s]%s", method, e.toString()), e);
                    ParserReport.INSTANCE.addErrorRecord(file.toPath(), "error on addProcessClass", e);
                }
            } else if (fileName.endsWith(EConfigFileExtensions.SCHEDULE.extension())) {
                r.addCountSchedules();
                try {
                    folder.addSchedule(r, file.toPath());
                } catch (Exception e) {
                    LOGGER.error(String.format("[%s]%s", method, e.toString()), e);
                    ParserReport.INSTANCE.addErrorRecord(file.toPath(), "error on addSchedule", e);
                }
            } else if (fileName.endsWith(EConfigFileExtensions.MONITOR.extension())) {
                r.addCountMonitors();
                folder.addMonitor(null);
            } else {
                r.addCountFiles();
                Path p = file.toPath();
                folder.addFile(p);

                // e.g. calendars or jobstreams
                if (fileName.endsWith(".json")) {
                    r.addJsonFile(p);
                }
            }
        }

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s]jobChains...", method, folder.getPath()));
        }
        for (Map.Entry<String, List<Path>> entry : jobChains.entrySet()) {
            try {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][jobChains]%s", method, folder.getPath(), entry.getKey()));
                }
                folder.addJobChain(r, entry.getKey(), entry.getValue());
            } catch (Throwable e) {
                if (e instanceof FileNotFoundException) {
                    LOGGER.error(String.format("[%s][folder=%s][job chain=%s]%s", method, folder.getPath(), entry.getKey(), e.toString()));
                } else {
                    LOGGER.error(String.format("[%s][folder=%s][job chain=%s]%s", method, folder.getPath(), entry.getKey(), e.toString()), e);
                }
                ParserReport.INSTANCE.addErrorRecord(folder.getPath(), "job chain=" + entry.getKey(), e.toString());
            }
        }

        r.addCountStandaloneJobs(folder.getStandaloneJobs().size());
        r.addCountOrderJobs(folder.getOrderJobs().size());

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s]end", method, folder.getPath()));
        }
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
        private Path yadeConfiguration;
        private List<Path> jsonFiles = new ArrayList<>();

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

        public void setYadeConfiguration(Path f) {
            yadeConfiguration = f;
        }

        public void addJsonFile(Path file) {
            jsonFiles.add(file);
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

        public Path getYadeConfiguration() {
            return yadeConfiguration;
        }

        public List<Path> getJsonFiles() {
            return jsonFiles;
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
