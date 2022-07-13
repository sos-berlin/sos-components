package com.sos.js7.converter.js1.common.runtime;

import java.nio.file.Path;
import java.util.Map;

import org.w3c.dom.Node;

import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.js1.common.EConfigFileExtensions;
import com.sos.js7.converter.js1.input.DirectoryParser.DirectoryParserResult;

public class Schedule {

    private static final String ATTR_NAME = "name";
    private static final String ATTR_SUBSTITUTE = "substitute";
    private static final String ATTR_VALID_FROM = "valid_from";
    private static final String ATTR_VALID_TO = "valid_to";

    private Path path;// extra

    private String name;
    private String substitute;
    private String validFrom; // yyyy-mm-dd HH:MM[:ss]
    private String validTo; // yyyy-mm-dd HH:MM[:ss]

    private RunTime runTime;

    protected Schedule(RunTime runTime, String name, Path file) {
        this.path = file;
        this.name = name;

        this.runTime = runTime;
    }

    public Schedule(DirectoryParserResult pr, Path file) throws Exception {
        this.path = file;
        Node node = JS7ConverterHelper.getDocumentRoot(file);

        Map<String, String> m = JS7ConverterHelper.attribute2map(node);
        this.name = JS7ConverterHelper.stringValue(m.get(ATTR_NAME));
        this.substitute = JS7ConverterHelper.stringValue(m.get(ATTR_SUBSTITUTE));
        this.validFrom = JS7ConverterHelper.stringValue(m.get(ATTR_VALID_FROM));
        this.validTo = JS7ConverterHelper.stringValue(m.get(ATTR_VALID_TO));

        this.runTime = new RunTime(SOSXML.newXPath(), node, file);

        if (SOSString.isEmpty(this.name)) {
            this.name = EConfigFileExtensions.getScheduleName(file);
        }
    }

    public Path getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getSubstitute() {
        return substitute;
    }

    public String getValidFrom() {
        return validFrom;
    }

    public String getValidTo() {
        return validTo;
    }

    public RunTime getRunTime() {
        return runTime;
    }
}
