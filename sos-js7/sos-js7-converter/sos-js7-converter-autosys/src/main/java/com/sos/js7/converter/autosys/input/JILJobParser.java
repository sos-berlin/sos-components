package com.sos.js7.converter.autosys.input;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.config.AutosysConverterConfig;
import com.sos.js7.converter.autosys.input.DirectoryParser.DirectoryParserResult;
import com.sos.js7.converter.commons.report.ParserReport;

public class JILJobParser extends AFileParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(JILJobParser.class);

    public static int COUNTER_INSERT_JOB = 0;
    public static Map<String, Map<Path, Integer>> INSERT_JOBS = new TreeMap<>();
    public static Map<String, Map<String, List<String>>> MULTIPLE_ATTRIBUTES = new TreeMap<>();

    private static final String NEW_LINE = "\r\n";
    private static final String PROPERTY_NAME_INSERT_JOB = "insert_job";

    private XMLWriter xmlWriter;

    public JILJobParser(AutosysConverterConfig config, Path reportDir) {
        super(FileType.JIL, config, reportDir);
        COUNTER_INSERT_JOB = 0;
        INSERT_JOBS = new TreeMap<>();
        MULTIPLE_ATTRIBUTES = new TreeMap<>();
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

                        // - REPORT ---------------------------
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
                        // - REPORT ---------------------------

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
            String lineTrimmed = line.trim();
            if (lineTrimmed.startsWith(";") || lineTrimmed.startsWith("#")) {
                return;
            }
            // not split(:) because the value can have : (if a path value)
            int pos = line.indexOf(":");
            if (pos > 0) {
                String name = line.substring(0, pos).trim();
                String value = line.substring(pos + 1).trim();

                String v = p.get(name);
                if (v != null) {
                    // - REPORT ---------------------------
                    String jn = p.get(PROPERTY_NAME_INSERT_JOB);
                    Map<String, List<String>> m = MULTIPLE_ATTRIBUTES.get(jn);
                    if (m == null) {
                        m = new LinkedHashMap<>();
                    }
                    List<String> l = m.get(name);
                    if (l == null) {
                        l = new ArrayList<>();
                        l.add(v);
                    }
                    l.add(value);
                    m.put(name, l);
                    MULTIPLE_ATTRIBUTES.put(jn, m);
                    // - REPORT ---------------------------

                    value = v + ACommonJob.LIST_VALUE_DELIMITER + value;
                }
                p.put(name, value);
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
