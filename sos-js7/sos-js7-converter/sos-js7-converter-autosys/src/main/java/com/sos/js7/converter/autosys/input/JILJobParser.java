package com.sos.js7.converter.autosys.input;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.js7.converter.autosys.config.AutosysConverterConfig;
import com.sos.js7.converter.autosys.input.DirectoryParser.DirectoryParserResult;
import com.sos.js7.converter.commons.report.ParserReport;

public class JILJobParser extends AFileParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(JILJobParser.class);

    public static int COUNTER_INSERT_JOB = 0;
    public static Map<String, Map<Path, Integer>> INSERT_JOBS = new TreeMap<>();

    private static final String NEW_LINE = "\r\n";
    private static final String PROPERTY_NAME_INSERT_JOB = "insert_job";

    private XMLWriter xmlWriter;

    public JILJobParser(AutosysConverterConfig config, Path reportDir) {
        super(FileType.JIL, config, reportDir);
        COUNTER_INSERT_JOB = 0;
        INSERT_JOBS = new TreeMap<>();
    }

    public void writeXMLStart(Path parent, String name) {
        xmlWriter = new XMLWriter(parent, name);
        xmlWriter.write("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>");
        xmlWriter.write("<ArrayOfJIL>");
    }

    public void writeXMLEnd() {
        xmlWriter.write("</ArrayOfJIL>");
    }

    public Path getXMLFile() {
        return xmlWriter.file;
    }

    @Override
    public FileParserResult parse(DirectoryParserResult r, Path file) {

        try {

            int counterFileInsertJob = 0;

            LinkedHashMap<String, String> m = null;
            try (BufferedReader br = new BufferedReader(new FileReader(file.toFile()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (SOSString.isEmpty(line)) {
                        continue;
                    }
                    String l = line.trim();
                    if (l.startsWith("/*")) {
                        continue;
                    }

                    if (l.startsWith("insert_job")) {
                        COUNTER_INSERT_JOB++;
                        counterFileInsertJob++;

                        if (m != null && m.size() > 0) {
                            xmlWriter.write(m);
                        }
                        m = createProperties(l);

                        // ----------------------------
                        Map<Path, Integer> ijt = INSERT_JOBS.get(m.get(PROPERTY_NAME_INSERT_JOB));
                        if (ijt == null) {
                            ijt = new HashMap<>();
                        }
                        Integer ijc = ijt.get(file);
                        if (ijc == null) {
                            ijc = Integer.valueOf(0);
                        }
                        ijc++;
                        ijt.put(file, ijc);
                        INSERT_JOBS.put(m.get(PROPERTY_NAME_INSERT_JOB), ijt);
                        // -----------------------------

                    } else {
                        toProperty(m, l);
                    }
                }
            }

            if (m != null && m.size() > 0) {
                xmlWriter.write(m);
            }
            LOGGER.info("[insert_job][total=" + COUNTER_INSERT_JOB + "][" + file + "]" + counterFileInsertJob);

        } catch (Throwable e) {
            LOGGER.error(String.format("[%s]%s", file, e.toString()), e);
            ParserReport.INSTANCE.addErrorRecord(file, null, e);
        }

        return new FileParserResult(new ArrayList<>());
    }

    private LinkedHashMap<String, String> createProperties(String line) throws Exception {
        LinkedHashMap<String, String> p = new LinkedHashMap<>();

        // insert_job: PRD_SysAlarm job_type: CMD
        String[] arr = line.split("\\:");
        if (arr.length == 3) {
            int pos = arr[1].indexOf(" job_type");
            p.put(PROPERTY_NAME_INSERT_JOB, arr[1].substring(0, pos).trim());
            p.put("job_type", arr[2].trim());
        } else {
            throw new Exception("[not parsable][insert_job line]" + line);
        }
        return p;
    }

    private void toProperty(LinkedHashMap<String, String> p, String line) throws Exception {
        if (p != null) {
            // not split(:) because the value can have : (if a path value)
            int pos = line.indexOf(":");
            if (pos > 0) {
                p.put(line.substring(0, pos).trim(), line.substring(pos + 1).trim());
            } else {
                throw new Exception("[not parsable][line][: pos=" + pos + "]" + line);
            }
        }
    }

    private class XMLWriter {

        private final Path file;

        private XMLWriter(Path parent, String name) {
            this.file = parent.resolve("js7_converter_" + name + ".xml");
            try {
                SOSPath.deleteIfExists(this.file);
                // Files.createDirectory(parent);
            } catch (Exception e) {
                LOGGER.error("[SOSPath.deleteIfExists][" + parent + "]" + e.toString(), e);
            }
            LOGGER.info("[file]" + file);
        }

        private void write(String content) {
            try {
                SOSPath.append(file, content, NEW_LINE);
            } catch (Throwable e) {
                LOGGER.error("[write][" + file + "][" + content + "]" + e.toString(), e);
            }
        }

        private void write(LinkedHashMap<String, String> properties) {
            write("<JIL>");

            properties.entrySet().forEach(e -> {
                String key = e.getKey();
                write("    <" + key + "><![CDATA[" + e.getValue() + "]]></" + key + ">");
            });

            write("</JIL>");
        }
    }

}
