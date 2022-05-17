package com.sos.commons.util.loganonymizer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

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

    private String executeReplace(String line) {
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
            if (outputDir == null) {
                outputDir = p.getParent().toString();
            }

            String output = outputDir + "/" + ANONYMIZED + p.getFileName();

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
                        output = output.substring(0, output.length() - 3);
                    }
                } else {
                    r = Files.newBufferedReader(Paths.get(logFilename));
                }

                writer = new BufferedWriter(new FileWriter(output));
                Path pOutput = Paths.get(output);
                LOGGER.info("Output file " + pOutput.toString());
                try {
                    for (String line; (line = r.readLine()) != null;) {
                        String replacedLine = executeReplace(line);
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

    public void setRules(String rules) {
        listOfRules.clear();
        Yaml yaml = new Yaml(new org.yaml.snakeyaml.constructor.Constructor(SOSRules.class));
        try {

            InputStream inputStream = Files.newInputStream(Paths.get(rules));

            SOSRules sosRules = yaml.load(inputStream);
            if (sosRules.getRules() != null) {
                listOfRules.addAll(sosRules.getRules());

            }
        } catch (YAMLException | IOException e) {
            e.printStackTrace();
        }
    }

    public void setLogfiles(String logfileName) {

        LOGGER.debug("Adding:" + logfileName);
        if (listOfLogfileNames == null) {
            listOfLogfileNames = new ArrayList<String>();
        }

        boolean isDirectory = false;
        boolean isFile = false;

        Path path = null;
        try {
            path = Paths.get(logfileName);

            isDirectory = Files.isDirectory(path);
            isFile = Files.isRegularFile(path);

            if (isFile) {
                LOGGER.debug("is file");
                if (Files.exists(path)) {
                    if (!path.getFileName().startsWith(ANONYMIZED)) {
                        listOfLogfileNames.add(path.toString());
                    }
                } else {
                    LOGGER.info("File not found: " + path.toString());
                }
            } else {
                if (isDirectory) {
                    LOGGER.debug("is directory");
                    try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(logfileName))) {
                        dirStream.forEach(pathLogfile -> {
                            if (Files.isRegularFile(pathLogfile) && !pathLogfile.getFileName().startsWith(ANONYMIZED)) {
                                listOfLogfileNames.add(pathLogfile.toString());
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    logfileName = logfileName.replace("\\", "/");
                    String[] logfileParts = logfileName.split("/");
                    String lastPart = logfileParts[logfileParts.length - 1];
                    String s = logfileName.replace("/" + lastPart, "");

                    LOGGER.debug("wildcard:" + lastPart);
                    
                    try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(s), lastPart)) {
                        dirStream.forEach(pathLogfile -> {

                            if (Files.isRegularFile(pathLogfile) && !pathLogfile.getFileName().startsWith(ANONYMIZED)) {
                                listOfLogfileNames.add(pathLogfile.toString());
                            }
                        });
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
            }
        } catch (InvalidPathException e) {

            logfileName = logfileName.replace("\\", "/");
            String[] logfileParts = logfileName.split("/");
            String lastPart = logfileParts[logfileParts.length - 1];
            String s = logfileName.replace("/" + lastPart, "");

            LOGGER.debug("Exception. try wildcard " + lastPart);
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(s), lastPart)) {
                dirStream.forEach(pathLogfile -> {
                    if (Files.isRegularFile(pathLogfile) && !pathLogfile.getFileName().startsWith(ANONYMIZED)) {
                        listOfLogfileNames.add(pathLogfile.toString());
                    }
                });
            } catch (IOException e2) {
                e2.printStackTrace();
            }

        }

    }

    public void exportRules(String exportFile) throws IOException {
        Path pInput = Paths.get(exportFile);
        Path path = Paths.get(pInput.toAbsolutePath().toString());
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
    protected void add(String item, String search, String... replace) {
        addRule(item, search, replace);
    }

    public void setOutputdir(String outputDir) {
        this.outputDir = outputDir;
    }

}
