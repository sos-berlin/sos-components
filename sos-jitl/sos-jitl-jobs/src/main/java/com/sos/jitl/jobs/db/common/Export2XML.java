package com.sos.jitl.jobs.db.common;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import com.sos.commons.hibernate.SOSHibernateSQLExecutor;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSPath;
import com.sos.commons.xml.SOSXML;
import com.sos.jitl.jobs.common.JobLogger;

public class Export2XML {

    private static final String NEW_LINE = "\r\n";
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static void export(ResultSet resultSet, Path outputFile, JobLogger logger) throws Exception {
        if (resultSet == null) {
            throw new Exception("missing ResultSet");
        }
        if (outputFile == null) {
            throw new Exception("missing outputFile");
        }

        BufferedWriter writer = null;
        boolean removeOutputFile = false;

        try {
            Instant start = Instant.now();
            Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NEW_LINE);
            writer.write("<RESULTSET>" + NEW_LINE);

            int dataRows = 0;
            int columnCount = resultSet.getMetaData().getColumnCount();
            String[] columnLabels = normalizeLabels(SOSHibernateSQLExecutor.getColumnLabels(resultSet));
            while (resultSet.next()) {
                writer.write("    <ROW>" + NEW_LINE);
                for (int i = 1; i <= columnCount; ++i) {
                    String label = columnLabels[i - 1];
                    writer.write("        <" + label + ">" + getElementText(dom, resultSet.getObject(i)) + "</" + label + ">" + NEW_LINE);
                }
                writer.write("    </ROW>" + NEW_LINE);
                dataRows++;

                if (logger != null) {
                    int count = dataRows;
                    if (count % 1_000 == 0) {
                        logger.info("[export]%s entries processed ...", count);
                    }
                }
            }
            writer.write("</RESULTSET>");
            if (logger != null) {
                logger.info("[export][%s]total data rows written=%s, duration=%s", outputFile, dataRows, SOSDate.getDuration(start, Instant.now()));
            }
        } catch (Throwable e) {
            removeOutputFile = true;
            String f = outputFile.toString();
            try {
                f = outputFile.toAbsolutePath().toString();
            } catch (Throwable ee) {

            }
            throw new Exception(String.format("[%s]%s", f, e.toString()), e);
        } finally {
            if (writer != null) {
                try {
                    writer.flush();
                } catch (Exception e) {
                }
                try {
                    writer.close();
                } catch (Exception e) {
                }
            }
            if (removeOutputFile) {
                try {
                    SOSPath.deleteIfExists(outputFile);
                } catch (Exception ex) {
                }
            }
        }
    }

    private static String getElementText(Document dom, Object o) {
        if (o != null) {
            try {
                // use dom for CDATA because e.g. nested CDATA
                return SOSXML.nodeToString(dom.createCDATASection(SOSHibernateSQLExecutor.sqlValueToString(o)));
            } catch (Throwable e) {
                return new StringBuilder("<![CDATA[").append(o.toString()).append("]]>").toString();
            }
        }
        return "";
    }

    private static String[] normalizeLabels(String[] labels) {
        String[] r = new String[labels.length];
        for (int i = 0; i < labels.length; i++) {
            r[i] = normalizeLabel(labels[i]);
        }
        return r;
    }

    private static String normalizeLabel(String label) {
        String r = label.replaceAll("//s|'|:|\\.|,|<|>|-|\"", "");
        if (isNumeric(r)) {
            r = "Column" + r;
        }
        return r;
    }

    private static boolean isNumeric(String val) {
        if (val == null) {
            return false;
        }
        return NUMERIC_PATTERN.matcher(val).matches();
    }
}
