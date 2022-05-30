package com.sos.js7.converter.js1.common;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.w3c.dom.Node;

import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.js7.converter.commons.JS7ConverterHelper;

public class Include {

    private static final String ATTR_FILE = "file";
    private static final String ATTR_LIVE_FILE = "live_file";
    private static final String ATTR_NODE = "node";

    private final String file; // filename - Path to file to be included
    private final String liveFile; // path - Path to the file to be added from the configuration directory
    private final String node; // xpath - XPath expression

    public Include(SOSXMLXPath xpath, Node node) throws Exception {
        Map<String, String> m = JS7ConverterHelper.attribute2map(node);
        this.file = m.get(ATTR_FILE);
        this.liveFile = m.get(ATTR_LIVE_FILE);
        this.node = m.get(ATTR_NODE);

        if (SOSString.isEmpty(this.file) && SOSString.isEmpty(this.liveFile)) {
            throw new Exception("missing file or live_file");
        }
    }

    public String getFile() {
        return file;
    }

    public String getLiveFile() {
        return liveFile;
    }

    public Path getIncludeFile() {
        String f = file;
        if (SOSString.isEmpty(f)) {
            f = liveFile;
        }
        return Paths.get(f);
    }

    public String getNode() {
        return node;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("file=").append(file);
        if (liveFile != null) {
            sb.append(",liveFile=").append(liveFile);
        }
        if (node != null) {
            sb.append(",node=").append(node);
        }
        return sb.toString();
    }

}
