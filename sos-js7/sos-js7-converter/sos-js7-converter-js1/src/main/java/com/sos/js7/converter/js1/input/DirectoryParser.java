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

import com.sos.js7.converter.commons.report.ParserReport;
import com.sos.js7.converter.js1.common.EConfigFileExtensions;
import com.sos.js7.converter.js1.common.Folder;

public class DirectoryParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryParser.class);

    public static DirectoryParserResult parse(Path dir) {
        DirectoryParserResult r = new DirectoryParser().new DirectoryParserResult();

        String method = "parse";
        if (Files.exists(dir)) {
            try {
                r.setRoot(parseSingleDir(new Folder(dir)));
                try (Stream<Path> stream = Files.walk(dir)) {
                    for (Path p : stream.filter(f -> Files.isDirectory(f) && !f.equals(dir)).collect(Collectors.toList())) {
                        Folder fp = r.getRoot().findParent(p);
                        if (fp != null) {
                            fp.addFolder(parseSingleDir(new Folder(p)));
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
        return r;
    }

    private static Folder parseSingleDir(Folder folder) {
        String method = "parseSingleDir";

        File[] files = folder.getPath().toAbsolutePath().toFile().listFiles(f -> !f.isDirectory());
        if (files.length == 0) {
            return folder;
        }

        Map<String, List<Path>> jobChains = new HashMap<>();
        for (File file : files) {
            String fileName = file.getName();
            if (fileName.endsWith(EConfigFileExtensions.ORDER.extension())) {
                jobChains = addJobChainFile(jobChains, file, EConfigFileExtensions.getName(EConfigFileExtensions.ORDER, fileName));
            } else if (fileName.endsWith(EConfigFileExtensions.JOB_CHAIN.extension())) {
                jobChains = addJobChainFile(jobChains, file, EConfigFileExtensions.getName(EConfigFileExtensions.JOB_CHAIN, fileName));
            } else if (fileName.endsWith(EConfigFileExtensions.JOB_CHAIN_CONFIG.extension())) {
                jobChains = addJobChainFile(jobChains, file, EConfigFileExtensions.getName(EConfigFileExtensions.JOB_CHAIN_CONFIG, fileName));
            } else if (fileName.endsWith(EConfigFileExtensions.JOB.extension())) {
                folder.addJob(file.toPath());
            } else if (fileName.endsWith(EConfigFileExtensions.LOCK.extension())) {
                try {
                    folder.addLock(file.toPath());
                } catch (Exception e) {
                    LOGGER.error(String.format("[%s]%s", method, e.toString()), e);
                    ParserReport.INSTANCE.addErrorRecord(file.toPath(), null, e);
                }
            } else if (fileName.endsWith(EConfigFileExtensions.PROCESS_CLASS.extension())) {
                try {
                    folder.addProcessClass(file.toPath());
                } catch (Exception e) {
                    LOGGER.error(String.format("[%s]%s", method, e.toString()), e);
                    ParserReport.INSTANCE.addErrorRecord(file.toPath(), null, e);
                }
            } else if (fileName.endsWith(EConfigFileExtensions.SCHEDULE.extension())) {
                try {
                    folder.addSchedule(file.toPath());
                } catch (Exception e) {
                    LOGGER.error(String.format("[%s]%s", method, e.toString()), e);
                    ParserReport.INSTANCE.addErrorRecord(file.toPath(), null, e);
                }
            } else if (fileName.endsWith(EConfigFileExtensions.MONITOR.extension())) {
                folder.addMonitor(null);
            } else {
                folder.addFile(file.toPath());
            }
        }

        for (Map.Entry<String, List<Path>> entry : jobChains.entrySet()) {
            try {
                folder.addJobChain(entry.getKey(), entry.getValue());
            } catch (Throwable e) {
                LOGGER.error(String.format("[%s]%s", method, e.toString()), e);
                ParserReport.INSTANCE.addErrorRecord(folder.getPath(), "job chain=" + entry.getKey(), e);
            }
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
        private int countFiles = 0;
        private int countFolders = 0;

        protected void setRoot(Folder f) {
            root = f;
        }

        protected void addCountFiles() {
            countFiles++;
        }

        protected void addCountFolders() {
            countFolders++;
        }

        public Folder getRoot() {
            return root;
        }

        public int getCountFiles() {
            return countFiles;
        }

        public int getCountFolders() {
            return countFolders;
        }
    }
}
