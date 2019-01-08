package com.sos.commons.hibernate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSFile;

public class SOSHibernateFileProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateFileProcessor.class);

    String fileSpec = "^(.*)";
    boolean hasDirectory = false;
    boolean commitAtEnd = false;
    private ArrayList<String> successFiles;
    private LinkedHashMap<String, String> errorFiles;

    public SOSHibernateFileProcessor() {
        init();
    }

    private void init() {
        errorFiles = new LinkedHashMap<String, String>();
        successFiles = new ArrayList<String>();
    }

    public void process(SOSHibernateSession session, File inputFile) throws Exception {
        final String methodName = "process";
        boolean isEnd = false;
        try {
            if (inputFile.isDirectory()) {
                LOGGER.info(String.format("[%s][%s]fileSpec=%s", methodName, inputFile.getCanonicalPath(), getFileSpec()));
                hasDirectory = true;

                Vector<File> filelist = (Vector<File>) SOSFile.getFilelist(inputFile.getAbsolutePath(), getFileSpec(), 0);
                Iterator<File> iterator = filelist.iterator();
                while (iterator.hasNext()) {
                    this.process(session, iterator.next());
                }
                isEnd = true;

                LOGGER.info(String.format("[%s][%s]total=%s, success=%s, error=%s", methodName, inputFile.getCanonicalPath(), filelist.size(),
                        successFiles.size(), errorFiles.size()));
                if (!successFiles.isEmpty()) {
                    LOGGER.info(String.format("[%s]   success:", methodName));
                    for (int i = 0; i < successFiles.size(); i++) {
                        LOGGER.info(String.format("[%s]     %s) %s", methodName, i + 1, successFiles.get(i)));
                    }
                }
                if (!errorFiles.isEmpty()) {
                    LOGGER.info(String.format("[%s]   error:", methodName));
                    int i = 1;
                    for (Entry<String, String> entry : errorFiles.entrySet()) {
                        LOGGER.info(String.format("[%s]     %s) %s: %s", methodName, i, entry.getKey(), entry.getValue()));
                        i++;
                    }
                }

            } else {
                FileReader fr = null;
                BufferedReader br = null;
                StringBuilder sb = new StringBuilder();
                LOGGER.info(String.format("[%s]%s", methodName, inputFile.getCanonicalPath()));
                try {
                    fr = new FileReader(inputFile.getCanonicalPath());
                    br = new BufferedReader(fr);
                    String nextLine = "";
                    while ((nextLine = br.readLine()) != null) {
                        sb.append(nextLine);
                        sb.append("\n");
                    }
                } catch (Exception ex) {
                    throw ex;
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (Exception ex) {
                            //
                        }
                    }
                    if (fr != null) {
                        try {
                            fr.close();
                        } catch (Exception ex) {
                            //
                        }
                    }
                }
                session.getSQLExecutor().executeStatements(sb.toString());

                if (!hasDirectory) {
                    isEnd = true;
                }
                successFiles.add(inputFile.getCanonicalPath());
                LOGGER.info(String.format("[%s]file successfully processed %s", methodName, inputFile.getCanonicalPath()));

            }
        } catch (Exception e) {
            errorFiles.put(inputFile.getCanonicalPath(), e.toString());
            LOGGER.warn(String.format("[%s]an error occurred processing file [%s]%s", methodName, inputFile.getCanonicalPath(), e.toString()), e);

        } finally {
            try {
                if (session != null && isEnd) {
                    if (isCommitAtEnd()) {
                        session.commit();
                    } else {
                        session.executeUpdate("ROLLBACK");
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

            File inputFile = null;
            for (int i = 0; i < args.length; i++) {
                String param = args[i].trim();
                LOGGER.info(String.format("  %s) %s", i + 1, param));
                if (i == 1) {
                    inputFile = new File(param);
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