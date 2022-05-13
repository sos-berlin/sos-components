package com.sos.commons.util.loganonymizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import com.sos.commons.util.loganonymizer.classes.DefaultRulesTable;
import com.sos.commons.util.loganonymizer.classes.Rule;
import com.sos.commons.util.loganonymizer.classes.SOSRules;

public class SOSLogAnonymizerExecuter extends DefaultRulesTable {

    private static final String ANONYMIZED = "anonymized_";

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSLogAnonymizerExecuter.class);

    private List<String> listOfLogfileNames = new ArrayList<String>();
    private List<Rule> listOfDefaultRules;
    private List<Rule> listOfRules = new ArrayList<Rule>();

    private List<Rule> listOfDefaultAgentRules = new ArrayList<Rule>();
    private List<Rule> listOfDefaultControllerRules = new ArrayList<Rule>();
    private List<Rule> listOfDefaultCockpitRules = new ArrayList<Rule>();
    private List<Rule> listOfAgentRules = new ArrayList<Rule>();
    private List<Rule> listOfControllerRules = new ArrayList<Rule>();
    private List<Rule> listOfJocCockpitRules = new ArrayList<Rule>();

    public SOSLogAnonymizerExecuter() {
        super();
        initDefaultRules();
    }

    private void addRule(String item, String search, String... replace) {
        if (listOfDefaultRules == null) {
            listOfDefaultRules = new ArrayList<Rule>();
        }
        Rule rule = new Rule();
        rule.setItem(item);
        rule.setReplace(replace);
        rule.setSearch(search);
        listOfDefaultRules.add(rule);
    }

    private void initDefaultRules() {
        listOfRules.addAll(listOfDefaultRules);
    }

    public void addLogfileName(String logfileName) {
        listOfLogfileNames.add(logfileName);
    }

    private int getLogfileSource(String filename) {
        return 1;
    }

    private String executeReplace(int logFileSource, String line) {
        String ret = line;
        List<String> replaceSearch = new ArrayList<String>();
        for (Rule rule : listOfRules) {
            Matcher m = Pattern.compile(rule.getSearch()).matcher(ret);
            if (m.find()) {
                for (int g = 1; g <= m.groupCount(); g++) {
                    if (rule.getReplace().length >= g) {
                        replaceSearch.add(line.substring(m.start(g), m.end(g)));
                    }
                }
                for (int s = 0; s < rule.getReplace().length; s++) {
                    if (replaceSearch.size() > s) {
                        ret = ret.replace(replaceSearch.get(s), rule.getReplace()[s]);
                    }
                }
            }
            replaceSearch.clear();
            line = ret;
        }
        return ret;
    }

    public void executeSubstitution() {
        for (String logFilename : listOfLogfileNames) {
            LOGGER.debug("input --->" + logFilename);
            File input = new File(logFilename);
            Path p = Paths.get(input.getAbsolutePath());
            String output = p.getParent() + "/" + ANONYMIZED + p.getFileName();

            int logFileSource = getLogfileSource(logFilename);
            try (BufferedReader r = Files.newBufferedReader(Paths.get(logFilename)); BufferedWriter writer = new BufferedWriter(new FileWriter(
                    output));) {

                File f = new File(output);
                LOGGER.info("Output file " + f.getAbsolutePath());
                System.out.println("File " + f.getAbsolutePath());
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

    public void setRules(String rules) {
        listOfRules.clear();
        Yaml yaml = new Yaml(new org.yaml.snakeyaml.constructor.Constructor(SOSRules.class));
        try {

            InputStream inputStream = new FileInputStream(new File(rules));

            SOSRules sosRules = yaml.load(inputStream);
            if (sosRules.getRules() != null) {
                listOfRules.addAll(sosRules.getRules());

            }
        } catch (YAMLException | FileNotFoundException e) {
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
                    if (Files.exists(path) && !logfile.startsWith(ANONYMIZED)) {
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
                    String s = files[i].getAbsolutePath();
                    if (!s.startsWith(ANONYMIZED)) {
                        listOfLogfileNames.add(files[i].getAbsolutePath());
                    }
                }
            }
        }
        for (String l : listOfLogfileNames) {
            LOGGER.debug(l);
        }
    }

    public void exportRules(String exportFile) throws IOException {
        final DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setProcessComments(true);
        options.setCanonical(false);
        Representer representer = new Representer();
        representer.addClassTag(com.sos.commons.util.loganonymizer.classes.SOSRules.class, Tag.MAP);

        final Yaml yaml = new Yaml(representer, options);

        SOSRules defaultRules = new SOSRules();
        defaultRules.getRules().addAll(getListOfDefaultRules());
        FileWriter writer = new FileWriter(exportFile);
        yaml.dump(defaultRules, writer);
    }

    public List<Rule> getListOfRules() {
        return listOfRules;
    }

    @Override
    protected void a(String item, String search, String... replace) {
        addRule(item, search, replace);
    }

}
