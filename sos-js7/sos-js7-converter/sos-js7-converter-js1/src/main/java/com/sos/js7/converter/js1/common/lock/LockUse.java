package com.sos.js7.converter.js1.common.lock;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.w3c.dom.Node;

import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.report.ParserReport;
import com.sos.js7.converter.js1.common.EConfigFileExtensions;
import com.sos.js7.converter.js1.input.DirectoryParser.DirectoryParserResult;
import com.sos.js7.converter.js1.output.js7.JS12JS7Converter;

public class LockUse {

    private static final String ATTR_LOCK = "lock";
    private static final String ATTR_EXCLUSIVE = "exclusive";

    private Lock lock;
    private boolean exclusive; // yes|no (Initial value: yes) The lock can be made exclusive or non-exclusive

    public LockUse(DirectoryParserResult pr, Path currentPath, SOSXMLXPath xpath, Node node) {
        Map<String, String> map = JS7ConverterHelper.attribute2map(node);
        this.lock = getLock(pr, currentPath, map);
        this.exclusive = JS7ConverterHelper.booleanValue(map.get(ATTR_EXCLUSIVE), true);
    }

    private Lock getLock(DirectoryParserResult pr, Path currentPath, Map<String, String> m) {
        String includePath = JS7ConverterHelper.stringValue(m.get(ATTR_LOCK));
        if (SOSString.isEmpty(includePath)) {
            return null;
        }
        try {
            Path p = JS12JS7Converter.findIncludeFile(pr, currentPath, Paths.get(includePath + EConfigFileExtensions.LOCK.extension()));
            if (p != null) {
                return new Lock(p);
            } else {
                ParserReport.INSTANCE.addErrorRecord(currentPath, "[attribute=" + ATTR_LOCK + "]Lock not found=" + includePath, "");
                return null;
            }
        } catch (Throwable e) {
            ParserReport.INSTANCE.addErrorRecord(currentPath, "[attribute=" + ATTR_LOCK + "]Lock not found=" + includePath, e);
            return null;
        }
    }

    public Lock getLock() {
        return lock;
    }

    public boolean getExclusive() {
        return exclusive;
    }
    
    @Override
    public String toString() {
        return SOSString.toString(this);
    }

}
