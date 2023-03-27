package com.sos.js7.converter.commons.input.diagram;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.js7.converter.commons.config.items.DiagramConfig;

public abstract class AGraphvizDiagramWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AGraphvizDiagramWriter.class);

    public static final String NEW_LINE = "\n";
    public static final String HTML_NEW_LINE = "<br/>";
    public static final String HTML_NEW_LINE_ALIGN_LEFT = "<br align=\"left\" />";

    public static final String COLOR_NOT_OK = "#F21308";
    public static final String COLOR_OK = "#FFFF99";

    public static final int FONT_SIZE_GRAPH = 10;
    public static final int FONT_SIZE_NODE = 8;
    public static final int FONT_SIZE_CONDITION = 8;
    public static final int FONT_SIZE_EDGE = 6;
    public static final int FONT_SIZE_DETAILS = 6;

    public abstract String getContent();

    public boolean createDiagram(DiagramConfig config, String range, Path outputFile, String header) throws Exception {
        Path parent = outputFile.getParent().toAbsolutePath();
        if (parent != null) {
            parent.toFile().mkdirs();
        }

        Path dotFile = parent.resolve(outputFile.getFileName() + ".dot");
        SOSPath.append(dotFile, getStart(config, range, header));
        SOSPath.append(dotFile, getContent());
        SOSPath.append(dotFile, getEnd());

        StringBuilder sb = new StringBuilder();
        sb.append(" ").append(quote(config.getGraphvizExecutable().toString()));
        // sb.append(graphvizExecutable.toString());
        sb.append(" -x");
        sb.append(" -T").append(config.getOutputFormat());
        sb.append(" ").append(quote(dotFile.toString()));
        sb.append(" > ");
        sb.append(quote(parent.resolve(outputFile.getFileName() + "." + config.getOutputFormat()).toString()));

        SOSCommandResult r = SOSShell.executeCommand(sb.toString());
        if (r.hasError()) {
            LOGGER.error(r.toString());
            return false;
        }
        return true;
    }

    public static void cleanupDotFiles(Path dir) {
        if (Files.exists(dir)) {
            try {
                Files.walk(dir).sorted(Comparator.reverseOrder()).map(Path::toFile).filter(f -> f.getName().toLowerCase().endsWith(".dot")).forEach(
                        File::delete);
            } catch (IOException e) {
                LOGGER.error(e.toString(), e);
            }
        }
    }

    private String getStart(DiagramConfig config, String range, String header) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph ").append(quote("ct-sos")).append(" {").append(NEW_LINE);
        sb.append("rankdir=TB;").append(NEW_LINE);
        if (config.getSize() > 0) {
            sb.append("size=").append(quote(config.getSize())).append(";").append(NEW_LINE);
        }
        sb.append("graph [").append(NEW_LINE);
        sb.append("    label=").append(quote(getStartLabel(config, range, header))).append(NEW_LINE);
        sb.append("    fontsize=").append(FONT_SIZE_GRAPH).append(NEW_LINE);
        sb.append("];").append(NEW_LINE);

        sb.append("node [").append(NEW_LINE);
        sb.append("    fontsize=").append(FONT_SIZE_NODE).append(NEW_LINE);
        sb.append("    shape=").append(quote("box")).append(NEW_LINE);
        sb.append("    style=").append(quote("rounded,filled")).append(NEW_LINE);
        sb.append("    fontname=").append(quote("Arial")).append(NEW_LINE);
        sb.append("    fillcolor=").append(quote("#CCFF99")).append(NEW_LINE);
        sb.append("];").append(NEW_LINE);

        return sb.toString();
    }

    private String getStartLabel(DiagramConfig config, String range, String header) {
        StringBuilder sb = new StringBuilder();
        if (header != null) {
            sb.append(header).append(" -");
        }
        sb.append(" Created by JS7Converter (www.sos-berlin.com) at ").append(getCurrentDateTime());
        sb.append(" (").append(range).append(",").append(toString(config)).append(")");
        return sb.toString();
    }

    private String toString(DiagramConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("excludeStandalone=").append(config.getExcludeStandalone());
        if (config.getSize() > 0) {
            sb.append("size=").append(config.getSize());
        }
        return sb.toString();
    }

    private String getEnd() {
        return new StringBuilder().append("}").append(NEW_LINE).toString();
    }

    public static StringBuilder getEdge(final String from, final String to) {
        return getEdge(from, to, "style=solid");
    }

    public static StringBuilder getEdge(final String from, final String to, final String properties) {
        StringBuilder sb = new StringBuilder();
        sb.append(quote(from)).append(" -> ").append(quote(to)).append(" ");
        if (properties != null) {
            sb.append("[").append(properties).append("]");
        }
        return new StringBuilder().append(toHtml(sb.toString())).append(NEW_LINE);
    }

    private static String getCurrentDateTime() {
        try {
            return SOSDate.getCurrentDateTimeAsString();
        } catch (SOSInvalidDataException e) {
            return "";
        }
    }

    public static String toHtml(final String val) {
        if (SOSString.isEmpty(val)) {
            return val;
        }
        return val.replaceAll("á", "&#225;").replaceAll("ñ", "&#241;").replaceAll("ó", "&#243;").replaceAll("ì", "&#161;").replaceAll("í", "&#237;")
                .replaceAll("ú", "&#250;").replaceAll("Ó", "&#211;");
    }

    public static String quote(final String val) {
        if (SOSString.isEmpty(val)) {
            return val;
        }
        return "\"" + val.replaceAll("\"", "\"\"") + "\"";
    }

    public static String quote(final int val) {
        return "\"" + val + "\"";
    }

    public String singleQuote(final String val) {
        if (SOSString.isEmpty(val)) {
            return val;
        }
        return "'" + val.replaceAll("'", "''") + "'";
    }
}
