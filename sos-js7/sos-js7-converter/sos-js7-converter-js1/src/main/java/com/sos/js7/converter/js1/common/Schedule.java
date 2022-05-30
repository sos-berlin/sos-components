package com.sos.js7.converter.js1.common;

import java.nio.file.Path;
import java.util.Map;

import org.w3c.dom.Node;

import com.sos.js7.converter.commons.JS7ConverterHelper;

public class Schedule {

    private static final String ATTR_NAME = "name";
    private static final String ATTR_SUBSTITUTE = "substitute";
    private static final String ATTR_VALID_FROM = "valid_from";
    private static final String ATTR_VALID_TO = "valid_to";

    private String name;
    private String substitute;
    private String validFrom; // yyyy-mm-dd HH:MM[:ss]
    private String validTo; // yyyy-mm-dd HH:MM[:ss]

    public Schedule(Path file) throws Exception {
        Node node = JS7ConverterHelper.getDocumentRoot(file);

        Map<String, String> m = JS7ConverterHelper.attribute2map(node);
        this.name = JS7ConverterHelper.stringValue(m.get(ATTR_NAME));
        this.substitute = JS7ConverterHelper.stringValue(m.get(ATTR_SUBSTITUTE));
        this.validFrom = JS7ConverterHelper.stringValue(m.get(ATTR_VALID_FROM));
        this.validTo = JS7ConverterHelper.stringValue(m.get(ATTR_VALID_TO));
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

}
