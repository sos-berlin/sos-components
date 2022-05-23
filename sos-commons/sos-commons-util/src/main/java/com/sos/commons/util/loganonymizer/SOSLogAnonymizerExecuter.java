package com.sos.commons.util.loganonymizer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import com.sos.commons.util.loganonymizer.classes.DefaultRulesTable;
import com.sos.commons.util.loganonymizer.classes.Rule;
import com.sos.commons.util.loganonymizer.classes.SOSRules;

public class SOSLogAnonymizerExecuter extends DefaultRulesTable {

    private static final String ANONYMIZED = "anonymized-";

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSLogAnonymizerExecuter.class);

    private Path outputDir;
    private List<String> listOfLogfileNames = new ArrayList<>();
    private List<Rule> listOfDefaultRules;
    private List<Rule> listOfRules = new ArrayList<>();

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
        for (Rule rule : listOfRules) {
            Matcher m = Pattern.compile(rule.getSearch()).matcher(line);
            int start = 0;
            StringBuilder ret = new StringBuilder();
            while (m.find()) {
                for (int g = 1; g <= m.groupCount(); g++) {
                    if (rule.getReplace().length >= g) {
                        ret.append(line.substring(start, m.start(g)) + rule.getReplace()[g-1]);
                        start = m.end(g);
                    }
                }
            }
            if (start > 0) {
                ret.append(line.substring(start));
                line = ret.toString();
            }
        }
        return line;
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
            LOGGER.error("", e);
            return false;
        }
        return magic == GZIPInputStream.GZIP_MAGIC;
    }

    public int executeSubstitution() {
        int ret = 0;
        for (String logFilename : listOfLogfileNames) {
            LOGGER.debug("input ---> " + logFilename);
            
            Path pLogFilename = Paths.get(logFilename);
            if (outputDir == null) {
                outputDir = pLogFilename.toAbsolutePath().normalize().getParent();
            }
            
            Path output = outputDir.resolve(ANONYMIZED + pLogFilename.getFileName().toString());

            BufferedReader r = null;
            BufferedWriter writer = null;
            InputStream is = null;
            try {

                is = Files.newInputStream(pLogFilename);
                if (isGZipped(is)) {
                    InputStream fileInputStream = new FileInputStream(logFilename);
                    GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
                    Reader isReader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8);
                    r = new BufferedReader(isReader);
                    if (logFilename.endsWith(".gz")) {
                        output = outputDir.resolve(ANONYMIZED + Paths.get(logFilename.substring(0, logFilename.length() - 3)).getFileName()
                                .toString());
                    }
                } else {
                    r = Files.newBufferedReader(pLogFilename);
                }
                
                writer = Files.newBufferedWriter(output);
                LOGGER.info("Output file " + output.toString());
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
                LOGGER.error("", e);
                ret++;
            } finally {
                if (is != null) {
                    try {
                        is.close();
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
        return ret;
    }

    public void setRules(String rules) throws Exception {
        if (rules == null || rules.isEmpty()) {
            throw new IOException("No rule file specified."); 
        }
        listOfRules.clear();
        Yaml yaml = new Yaml(new org.yaml.snakeyaml.constructor.Constructor(SOSRules.class));
        Path rulesPath = Paths.get(rules);
        if (!Files.exists(rulesPath)) {
            throw new FileNotFoundException(rules + " doesn't exist.");
        }

        SOSRules sosRules = yaml.load(Files.newInputStream(rulesPath));
        if (sosRules.getRules() != null) {
            listOfRules.addAll(sosRules.getRules());
        }
    }

    public void setLogfiles(String logfileName) {

        LOGGER.debug("Adding log file:" + logfileName);
        if (logfileName != null && !logfileName.isEmpty()) {
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
                        if (!path.getFileName().toString().startsWith(ANONYMIZED)) {
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
                                if (Files.isRegularFile(pathLogfile) && !pathLogfile.getFileName().toString().startsWith(ANONYMIZED)) {
                                    listOfLogfileNames.add(pathLogfile.toString());
                                }
                            });
                        } catch (IOException e) {
                            LOGGER.error("", e);
                        }
                    } else {
                        logfileName = logfileName.replace('\\', '/');
                        String[] logfileParts = logfileName.split("/");
                        String lastPart = logfileParts[logfileParts.length - 1];
                        String s = logfileName.replace("/" + lastPart, "");

                        LOGGER.debug("wildcard:" + lastPart);

                        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(s), lastPart)) {
                            dirStream.forEach(pathLogfile -> {

                                if (Files.isRegularFile(pathLogfile) && !pathLogfile.getFileName().toString().startsWith(ANONYMIZED)) {
                                    listOfLogfileNames.add(pathLogfile.toString());
                                }
                            });
                        } catch (IOException e3) {
                            LOGGER.error("", e3);
                        }
                    }
                }
            } catch (InvalidPathException e) {

                logfileName = logfileName.replace('\\', '/');
                String[] logfileParts = logfileName.split("/");
                String lastPart = logfileParts[logfileParts.length - 1];
                String s = logfileName.replace("/" + lastPart, "");

                LOGGER.debug("Exception. try wildcard " + lastPart);
                try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(s), lastPart)) {
                    dirStream.forEach(pathLogfile -> {
                        if (Files.isRegularFile(pathLogfile) && !pathLogfile.getFileName().toString().startsWith(ANONYMIZED)) {
                            listOfLogfileNames.add(pathLogfile.toString());
                        }
                    });
                } catch (IOException e2) {
                    LOGGER.error("", e2);
                }

            }
        }
    }

    public void exportRules(String exportFile) throws IOException {
        if (exportFile == null || exportFile.isEmpty()) {
            throw new IOException("No export file is specified.");
        }
        Path exportPath = Paths.get(exportFile);
        if (Files.isDirectory(exportPath)) {
            throw new IOException(exportFile + " is a directory.");
        } else {
            Path parent = exportPath.toAbsolutePath().normalize().getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
        }
        
        final DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setProcessComments(true);
        options.setCanonical(false);
        Representer representer = new Representer();
        representer.addClassTag(com.sos.commons.util.loganonymizer.classes.SOSRules.class, Tag.MAP);

        final Yaml yaml = new Yaml(representer, options);

        SOSRules defaultRules = new SOSRules();
        defaultRules.getRules().addAll(listOfDefaultRules);
        yaml.dump(defaultRules, Files.newBufferedWriter(exportPath));
    }

    public List<Rule> getListOfRules() {
        return listOfRules;
    }

    @Override
    protected void add(String item, String search, String... replace) {
        addRule(item, search, replace);
    }

    public void setOutputdir(String outputDir) throws IOException {
        if (outputDir != null && !outputDir.isEmpty()) {
            this.outputDir = Paths.get(outputDir);
            if (!Files.isDirectory(this.outputDir)) {
                throw new IOException(outputDir + " is not a directory");
            }
        }
    }

}
