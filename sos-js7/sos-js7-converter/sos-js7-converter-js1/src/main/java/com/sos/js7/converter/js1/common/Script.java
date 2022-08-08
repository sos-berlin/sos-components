package com.sos.js7.converter.js1.common;

import java.util.Map;

import org.w3c.dom.Node;

import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.js7.converter.commons.JS7ConverterHelper;

public class Script {

    public static final String JAVA_JITL_SPLITTER_JOB = "com.sos.jitl.splitter.JobChainSplitterJSAdapterClass";
    public static final String JAVA_JITL_JOIN_JOB = "com.sos.jitl.join.JobSchedulerJoinOrdersJSAdapterClass";
    public static final String JAVA_JITL_SYNCHRONIZER_JOB = "com.sos.jitl.sync.JobSchedulerSynchronizeJobChainsJSAdapterClass";

    private static final String ATTR_COM_CLASS = "com_class";
    private static final String ATTR_FILENAME = "filename";
    private static final String ATTR_JAVA_CLASS = "java_class";
    private static final String ATTR_JAVA_CLASS_PATH = "java_class_path";
    private static final String ATTR_LANGUAGE = "language";

    private static final String ELEMENT_INCLUDE = "include";

    private Include include;

    private String comClass;
    private String filename;
    private String javaClass;
    private String javaClassPath;
    private String language;

    private String script;

    public Script(SOSXMLXPath xpath, Node node) throws Exception {
        Node include = xpath.selectNode(node, "./" + ELEMENT_INCLUDE);
        if (include != null) {
            this.include = new Include(xpath, include);
        }

        Map<String, String> attributes = JS7ConverterHelper.attribute2map(node);
        comClass = JS7ConverterHelper.stringValue(attributes.get(ATTR_COM_CLASS));
        filename = JS7ConverterHelper.stringValue(attributes.get(ATTR_FILENAME));
        javaClass = JS7ConverterHelper.stringValue(attributes.get(ATTR_JAVA_CLASS));
        javaClassPath = JS7ConverterHelper.stringValue(attributes.get(ATTR_JAVA_CLASS_PATH));
        language = JS7ConverterHelper.stringValue(attributes.get(ATTR_LANGUAGE));
        script = JS7ConverterHelper.getTextValue(node);
    }

    public boolean isJavaJITLSplitterJob() {
        return javaClass != null && javaClass.equals(JAVA_JITL_SPLITTER_JOB);
    }

    public boolean isJavaJITLJoinJob() {
        return javaClass != null && javaClass.equals(JAVA_JITL_JOIN_JOB);
    }

    public boolean isJavaJITLSynchronizerJob() {
        return javaClass != null && javaClass.equals(JAVA_JITL_SYNCHRONIZER_JOB);
    }

    public Include getInclude() {
        return include;
    }

    public String getComClass() {
        return comClass;
    }

    public String getFilename() {
        return filename;
    }

    public String getJavaClass() {
        return javaClass;
    }

    public String getJavaClassPath() {
        return javaClassPath;
    }

    public String getLanguage() {
        return language;
    }

    public String getScript() {
        return script;
    }
}
