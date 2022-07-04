package com.sos.js7.converter.js1.common.runtime;

import java.util.Map;

import org.w3c.dom.Node;

import com.sos.js7.converter.commons.JS7ConverterHelper;

public class At {

    private static final String ATTR_AT = "at";

    private String at; // yyyy-mm-dd hh:mm[:ss]

    protected At(Node node) {
        Map<String, String> m = JS7ConverterHelper.attribute2map(node);
        this.at = JS7ConverterHelper.stringValue(m.get(ATTR_AT));
    }

    public String getAt() {
        return at;
    }

}