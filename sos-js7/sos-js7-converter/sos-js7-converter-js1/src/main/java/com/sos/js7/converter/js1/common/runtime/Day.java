package com.sos.js7.converter.js1.common.runtime;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.commons.xml.exception.SOSXMLXPathException;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.report.ParserReport;

public class Day {

    private static final String ATTR_DAY = "day";

    private List<Period> periods;

    private String day;

    protected Day(Path path, SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        Map<String, String> m = JS7ConverterHelper.attribute2map(node);
        this.day = JS7ConverterHelper.stringValue(m.get(ATTR_DAY));
        this.periods = RunTime.convertPeriod(path, xpath, node);
    }

    public List<Period> getPeriods() {
        return periods;
    }

    public String getDay() {
        return day;
    }

    public List<Integer> getDays() {
        try {
            return JS7ConverterHelper.integerListValue(day, " ");
        } catch (Throwable e) {
            ParserReport.INSTANCE.addErrorRecord(null, "convert day=" + day + " to integer days", e);
            return null;
        }
    }

}
