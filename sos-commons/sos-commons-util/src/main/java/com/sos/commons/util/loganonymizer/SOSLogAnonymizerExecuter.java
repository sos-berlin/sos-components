package com.sos.commons.util.loganonymizer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

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

    private static final String ANONYMIZED = "anonymized-";

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSLogAnonymizerExecuter.class);

    private String outputDir;
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

    public static boolean isGZipped(InputStream in) {
        if (!in.markSupported()) {
            in = new BufferedInputStream(in);
        }
        in.mark(2);
        int magic = 0;
        try {
            magic = in.read() & 0xff | ((in.read() << 8) & 0xff00);
            in.reset();
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return false;
        }
        return magic == GZIPInputStream.GZIP_MAGIC;
    }

    public void executeSubstitution() {
        for (String logFilename : listOfLogfileNames) {
            LOGGER.debug("input --->" + logFilename);
            Path p = Paths.get(logFilename);

            String output = outputDir + "/" + ANONYMIZED + p.getFileName();

            int logFileSource = getLogfileSource(logFilename);
            BufferedReader r = null;
            BufferedWriter writer = null;
            GZIPInputStream gzipInputStream = null;
            try {

                InputStream is = Files.newInputStream(Paths.get(logFilename));
                if (isGZipped(is)) {
                    InputStream fileInputStream = new FileInputStream(logFilename);
                    gzipInputStream = new GZIPInputStream(fileInputStream);
                    Reader isReader = new InputStreamReader(gzipInputStream, StandardCharsets.ISO_8859_1);
                    r = new BufferedReader(isReader);
                    if (output.endsWith(".gz")) {
                        output =  output.substring(0,output.length() - 3);
                    }
                } else {
                    r = Files.newBufferedReader(Paths.get(logFilename));
                }

                writer = new BufferedWriter(new FileWriter(output));
                File f = new File(output);
                LOGGER.info("Output file " + f.getAbsolutePath());
                try {
                    for (String line; (line = r.readLine()) != null;) {
                        String replacedLine = executeReplace(logFileSource, line);
                        writer.write(replacedLine);
                        writer.newLine();

                        if (!line.equals(replacedLine)) {
                            LOGGER.debug(line);
                            LOGGER.debug(replacedLine);
                        }

                    }
                } catch (Exception e) {
                    LOGGER.warn(e.getMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (gzipInputStream != null) {
                    try {
                        gzipInputStream.close();
                    } catch (IOException e) {
                    }

                }
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                    }

                }
                if (r != null) {
                    try {
                        r.close();
                    } catch (IOException e) {
                    }

                }
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

                File input = new File(logfile);
                Path path = Paths.get(input.getAbsolutePath());

                isDirectory = Files.isDirectory(path);
                isFile = Files.isRegularFile(path);

                if (isFile) {
                    if (Files.exists(path)) {
                        if (!logfile.startsWith(ANONYMIZED)) {
                            listOfLogfileNames.add(path.toString());
                        }
                    } else {
                        LOGGER.info("File not found: " + path.toString());
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
                    Path path = Paths.get(s);

                    if (!Files.isDirectory(path) && !s.startsWith(ANONYMIZED)) {
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
        File input = new File(exportFile);
        Path path = Paths.get(input.getAbsolutePath());
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
        FileWriter writer = new FileWriter(path.toString());
        yaml.dump(defaultRules, writer);
    }

    public List<Rule> getListOfRules() {
        return listOfRules;
    }

    @Override
    protected void a(String item, String search, String... replace) {
        addRule(item, search, replace);
    }

    public void setOutputdir(String outputDir) {
        this.outputDir = outputDir;
    }

}
