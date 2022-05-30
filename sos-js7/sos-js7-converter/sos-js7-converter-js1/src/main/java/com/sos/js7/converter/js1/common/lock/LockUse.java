package com.sos.js7.converter.js1.common.lock;

import java.util.Map;

import org.w3c.dom.Node;

import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.js7.converter.commons.JS7ConverterHelper;

public class LockUse {

    private static final String ATTR_LOCK = "lock";
    private static final String ATTR_EXCLUSIVE = "exclusive";

    private String lock; // The name of the lock
    private boolean exclusive; // yes|no (Initial value: yes) The lock can be made exclusive or non-exclusive

    public LockUse(SOSXMLXPath xpath, Node node) {
        Map<String, String> map = JS7ConverterHelper.attribute2map(node);
        this.lock = JS7ConverterHelper.stringValue(map.get(ATTR_LOCK));
        this.exclusive = JS7ConverterHelper.booleanValue(map.get(ATTR_EXCLUSIVE), true);
    }

    public String getLock() {
        return lock;
    }

    public boolean getExclusive() {
        return exclusive;
    }

}
