package com.sos.js7.converter.js1.common.jobchain;

import java.nio.file.Path;
import java.util.Map;

import org.w3c.dom.Node;

import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.js1.common.EConfigFileExtensions;
import com.sos.js7.converter.js1.common.Params;
import com.sos.js7.converter.js1.common.RunTime;
import com.sos.js7.converter.js1.input.DirectoryParser.DirectoryParserResult;

public class JobChainOrder {

    private static final String ATTR_PRIORITY = "priority";
    private static final String ATTR_TITLE = "title";

    private static final String ELEMENT_PARAMS = "params";
    private static final String ELEMENT_RUN_TIME = "run_time";

    private Path path;// extra
    private String name;// extra

    private Params params;
    private RunTime runTime;
    // TODO xml_payload ???

    private final Integer priority;
    private final String title;

    public JobChainOrder(DirectoryParserResult pr, Path file) throws Exception {
        this.path = file;
        this.name = EConfigFileExtensions.getOrderName(file);
        Node node = JS7ConverterHelper.getDocumentRoot(file);
        Map<String, String> m = JS7ConverterHelper.attribute2map(node);
        this.priority = JS7ConverterHelper.integerValue(m.get(ATTR_PRIORITY));
        this.title = m.get(ATTR_TITLE);

        SOSXMLXPath xpath = SOSXML.newXPath();
        Node params = xpath.selectNode(node, "./" + ELEMENT_PARAMS);
        if (params != null) {
            this.params = new Params(xpath, params);
        }
        Node runTime = xpath.selectNode(node, "./" + ELEMENT_RUN_TIME);
        if (runTime != null) {
            this.runTime = new RunTime(pr, xpath, runTime, file);
        }
    }

    public Path getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public Params getParams() {
        return params;
    }

    public RunTime getRunTime() {
        return runTime;
    }

    public Integer getPriority() {
        return priority;
    }

    public String getTitle() {
        return title;
    }

}
