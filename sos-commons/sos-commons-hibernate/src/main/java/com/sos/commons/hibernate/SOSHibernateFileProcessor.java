package com.sos.commons.hibernate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSHibernateFileProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateFileProcessor.class);

    private String fileSpec = "^(.*)";
    private boolean hasDirectory = false;
    private boolean commitAtEnd = false;
    private List<String> successFiles;
    private Map<String, String> errorFiles;

    public SOSHibernateFileProcessor() {
        init();
    }

    private void init() {
        errorFiles = new LinkedHashMap<String, String>();
        successFiles = new ArrayList<String>();
    }

    public void process(SOSHibernateSession session, Path inputFile) throws Exception {
        final String method = "process";
        boolean isEnd = false;
        try {
            if (Files.isDirectory(inputFile)) {
                LOGGER.info(String.format("[%s][directory][%s]fileSpec=%s", method, inputFile.toString(), getFileSpec()));
                hasDirectory = true;
                final Pattern pattern = Pattern.compile(getFileSpec(), 0);
                Set<Path> filelist = Files.list(inputFile).filter(p -> !Files.isDirectory(p) && 
                		pattern.matcher(p.getFileName().toString()).find()).collect(Collectors.toSet());
                Iterator<Path> iterator = filelist.iterator();
                while (iterator.hasNext()) {
                    this.process(session, iterator.next());
                }
                isEnd = true;

                LOGGER.info(String.format("[%s][%s][success=%s][error=%s][total=%s]", method, inputFile.toString(), successFiles.size(),
                        errorFiles.size(), filelist.size()));
                if (!successFiles.isEmpty()) {
                    LOGGER.info(String.format("[%s][%s][success]:", method, inputFile.toString()));
                    for (int i = 0; i < successFiles.size(); i++) {
                        LOGGER.info(String.format("[%s]     %s) %s", method, i + 1, successFiles.get(i)));
                    }
                }
                if (!errorFiles.isEmpty()) {
                    LOGGER.info(String.format("[%s][%s][error]:", method, inputFile.toString()));
                    int i = 1;
                    for (Entry<String, String> entry : errorFiles.entrySet()) {
                        LOGGER.info(String.format("[%s]     %s) %s: %s", method, i, entry.getKey(), entry.getValue()));
                        i++;
                    }
                }

            } else {
                LOGGER.info(String.format("[%s][file]%s", method, inputFile.toString()));
                session.getSQLExecutor().executeStatements(new String(Files.readAllBytes(inputFile), "UTF-8"));

                if (!hasDirectory) {
                    isEnd = true;
                }
                successFiles.add(inputFile.toString());
                LOGGER.info(String.format("[%s][file][processed]%s", method, inputFile.toString()));

            }
        } catch (Exception e) {
            errorFiles.put(inputFile.toString(), e.toString());
            LOGGER.warn(String.format("[%s][exception][%s]%s", method, inputFile.toString(), e.toString()), e);

        } finally {
            try {
                if (session != null && isEnd) {
                    if (isCommitAtEnd()) {
                        session.commit();
                    } else {
                        session.rollback();
                    }
                }
            } catch (Exception ex) {
                //
            }
        }
    }

    public String getFileSpec() {
        return fileSpec;
    }

    public void setFileSpec(String fileSpec) {
        this.fileSpec = fileSpec;
    }

    public boolean isCommitAtEnd() {
        return commitAtEnd;
    }

    public void setCommitAtEnd(boolean commitAtEnd) {
        this.commitAtEnd = commitAtEnd;
    }

    public static void main(String args[]) throws Exception {
        if (args.length < 2) {
            LOGGER.info(String.format("Usage: %s hibernate_config_file   input_path   [file_spec]   [-commit-at-end]   [-auto-commit]",
                    SOSHibernateFileProcessor.class.getSimpleName()));
            LOGGER.info("            hibernate_config_file : required");
            LOGGER.info("                                    path to the hibernate configuration file");
            LOGGER.info("            input_path            : required");
            LOGGER.info("                                    directory or file path");
            LOGGER.info("            file_spec             : optional");
            LOGGER.info("                                    file specification (regex)");
            LOGGER.info("            -commit-at-end        : optional");
            LOGGER.info("                                    execute COMMIT at end");
            LOGGER.info("            -auto-commit          : optional");
            LOGGER.info("                                    auto commit");
            return;
        }

        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        SOSHibernateFileProcessor processor = null;
        boolean logToStdErr = false;
        int exitCode = 0;
        try {
            factory = new SOSHibernateFactory(args[0]);
            processor = new SOSHibernateFileProcessor();
            logToStdErr = Arrays.asList(args).contains("-execute-from-setup");

            Path inputFile = null;
            for (int i = 0; i < args.length; i++) {
                String param = args[i].trim();
                LOGGER.info(String.format("  %s) %s", i + 1, param));
                if (i == 1) {
                    inputFile = Paths.get(param);
                } else if (i == 2) {
                    processor.setFileSpec(param);
                } else if (i > 3) {
                    if ("-commit-at-end".equalsIgnoreCase(param)) {
                        processor.setCommitAtEnd(true);
                    } else if ("-auto-commit".equalsIgnoreCase(param)) {
                        factory.setAutoCommit(true);
                    }
                }
            }

            factory.build();
            session = factory.openStatelessSession();

            processor.process(session, inputFile);
            
            if (processor.errorFiles != null) {
                exitCode = processor.errorFiles.size();
                if (logToStdErr) {
                    for (Entry<String, String> entry : processor.errorFiles.entrySet()) {
                    	System.err.println(String.format("%s: %s", entry.getKey(), entry.getValue()));
                	}
                }
            }
            if (logToStdErr && processor.successFiles != null) {
            	for (String str : processor.successFiles) {
            		System.err.println(String.format("%s processed successfully", str));
            	}
            }
        } catch (Exception e) {
            exitCode = 1;
            if (logToStdErr) {
                e.printStackTrace(System.err);
            } else {
                e.printStackTrace(System.out);
            }
        } finally {
            if (session != null) {
                session.close();
            }
            if (factory != null) {
                factory.close();
            }
        }
        System.exit(exitCode);
    }

}