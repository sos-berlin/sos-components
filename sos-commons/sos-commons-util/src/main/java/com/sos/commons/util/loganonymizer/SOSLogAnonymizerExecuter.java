package com.sos.commons.util.loganonymizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sos.commons.util.loganonymizer.classes.Rule;
import com.sos.commons.util.loganonymizer.classes.SOSRules;

public class SOSLogAnonymizerExecuter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSLogAnonymizerExecuter.class);

    private List<String> listOfLogfileNames;
    private List<Rule> listOfDefaultRules;
    private List<Rule> listOfRules;

    private List<Rule> listOfDefaultAgentRules;
    private List<Rule> listOfDefaultControllerRules;
    private List<Rule> listOfDefaultCockpitRules;
    private List<Rule> listOfAgentRules;
    private List<Rule> listOfControllerRules;
    private List<Rule> listOfJocCockpitRules;
    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public SOSLogAnonymizerExecuter() {
        super();
        initDefaultRules();
    }

    private void initDefaultRules() {

    }

    public void addLogfileName(String logfileName) {
        if (listOfLogfileNames == null) {
            listOfLogfileNames = new ArrayList<String>();
            listOfLogfileNames.add(logfileName);
        }
    }

    private int getLogfileSource(String filename) {
        return 1;
    }

    private String executeReplace(int logFileSource, String line) {
        String ret = line;
        switch (logFileSource) {
        case 1:
            for (Rule rule : listOfRules) {
                ret = ret.replaceAll(rule.getSearch(), rule.getReplace());
            }
            break;
        }
        return ret;
    }

    public void executeSubstitution() {
        for (String logFilename : listOfLogfileNames) {
            LOGGER.debug("input --->" + logFilename);
            Path input = Paths.get(logFilename);
            String output = input.getParent() + "/anonymized_" + input.getFileName();

            LOGGER.debug("output --->" + logFilename);

            int logFileSource = getLogfileSource(logFilename);
            try (BufferedReader r = Files.newBufferedReader(Paths.get(logFilename)); BufferedWriter writer = new BufferedWriter(new FileWriter(
                    output));) {
                for (String line; (line = r.readLine()) != null;) {
                    String replacedLine = executeReplace(logFileSource, line);
                    writer.write(replacedLine);
                    writer.newLine();

                    if (!line.equals(replacedLine)) {
                        LOGGER.debug(line);
                        LOGGER.debug(replacedLine);
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Rule> getListOfDefaultAgentRules() {
        return listOfDefaultAgentRules;
    }

    public List<Rule> getListOfDefaultRules() {
        return listOfDefaultRules;
    }

    public List<Rule> getListOfDefaultControllerRules() {
        return listOfDefaultControllerRules;
    }

    public List<Rule> getListOfDefaultJocCockpitRules() {
        return listOfDefaultCockpitRules;
    }

    public void addAgentRule(Rule rule) {
        if (listOfAgentRules == null) {
            listOfAgentRules = new ArrayList<Rule>();
        }
        listOfAgentRules.add(rule);
    }

    public void addRulesRule(Rule rule) {
        if (listOfRules == null) {
            listOfRules = new ArrayList<Rule>();
        }
        listOfRules.add(rule);
    }

    public void addControllerRule(Rule rule) {
        if (listOfControllerRules == null) {
            listOfControllerRules = new ArrayList<Rule>();
        }
        listOfControllerRules.add(rule);
    }

    public void addJocCockpitRule(Rule rule) {
        if (listOfJocCockpitRules == null) {
            listOfJocCockpitRules = new ArrayList<Rule>();
        }
        listOfJocCockpitRules.add(rule);
    }

    public void setRules(String rules) {
        mapper.findAndRegisterModules();
        try {
            SOSRules sosRules = mapper.readValue(new File(rules), SOSRules.class);
            for (Rule rule : sosRules.getRules()) {
                addRulesRule(rule);
            }
            for (Rule rule : sosRules.getAgent()) {
                // addAgentRule(rule);
            }
            for (Rule rule : sosRules.getController()) {
                // addControllerRule(rule);
            }
            for (Rule rule : sosRules.getJocCockpit()) {
                // addJocCockpitRule(rule);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLogfiles(String log) {
        if (listOfLogfileNames == null) {
            listOfLogfileNames = new ArrayList<String>();
        }
        String[] logs = log.split(",");
        for (String logfile : logs) {
            File dir = null;
            FileFilter fileFilter = null;
            try {
                boolean isDirectory = false;
                boolean isFile = false;
                Path path = Paths.get(logfile);

                isDirectory = Files.isDirectory(path);
                isFile = Files.isRegularFile(path);
                if (isFile) {
                    if (Files.exists(path)) {
                        listOfLogfileNames.add(logfile);
                    }
                } else {
                    if (isDirectory) {
                        dir = new File(logfile);
                        fileFilter = new WildcardFileFilter("*.*");
                    }
                }
            } catch (InvalidPathException e) {
                logfile = logfile.replace("\\", "/");
                String[] logfileParts = logfile.split("/");
                String lastPart = logfileParts[logfileParts.length - 1];
                fileFilter = new WildcardFileFilter(lastPart);
                String s = logfile.replace("/" + lastPart, "");
                dir = new File(s);
            }

            if (dir != null) {
                File[] files = dir.listFiles(fileFilter);
                for (int i = 0; i < files.length; i++) {
                    listOfLogfileNames.add(files[i].getAbsolutePath());
                }
            }
        }
        for (String l : listOfLogfileNames) {
            LOGGER.debug(l);
        }
    }

    public void exportRules(String exportFile) throws IOException {
        try {
            SOSRules defaultRules = new SOSRules();
            defaultRules.setRules(getListOfDefaultRules());
            defaultRules.setAgent(getListOfDefaultAgentRules());
            defaultRules.setController(getListOfDefaultControllerRules());
            defaultRules.setJocCockpit(getListOfDefaultJocCockpitRules());
            mapper.writeValue(new File(exportFile), defaultRules);
        } catch (IOException e) {
            throw e;
        }
    }

    public List<Rule> getListOfRules() {
        return listOfRules;
    }

}
