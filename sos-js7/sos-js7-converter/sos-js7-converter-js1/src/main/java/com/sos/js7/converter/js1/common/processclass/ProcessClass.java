package com.sos.js7.converter.js1.common.processclass;

import java.nio.file.Path;
import java.util.Map;

import org.w3c.dom.Node;

import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.js1.common.EConfigFileExtensions;

public class ProcessClass {

    private static final String ATTR_MAX_PROCESSES = "max_processes";
    // private static final String ATTR_NAME = "name";
    private static final String ATTR_REMOTE_SCHEDULER = "remote_scheduler";
    private static final String ATTR_REPLACE = "replace";
    private static final String ATTR_SPOOLER_ID = "spooler_id";

    private static final String ELEMENT_REMOTE_SCHEDULERS = "remote_schedulers";

    private RemoteSchedulers remoteSchedulers;

    private Path path;

    private Integer maxProcesses;
    private String name;

    private String remoteScheduler; // host:port Task execution on remote computers
    private Boolean replace; // yes|no (Initial value: yes)
    private String spoolerId;

    public ProcessClass(Path path) throws Exception {
        this.path = path;

        SOSXMLXPath xpath = SOSXML.newXPath();
        Node node = JS7ConverterHelper.getDocumentRoot(path);

        Map<String, String> map = JS7ConverterHelper.attribute2map(node);
        this.maxProcesses = JS7ConverterHelper.integerValue(map.get(ATTR_MAX_PROCESSES));
        // this.name = JS7ConverterHelper.stringValue(map.get(ATTR_NAME));
        this.name = EConfigFileExtensions.getName(EConfigFileExtensions.PROCESS_CLASS, path.getFileName().toString());
        this.remoteScheduler = JS7ConverterHelper.stringValue(map.get(ATTR_REMOTE_SCHEDULER));
        this.replace = JS7ConverterHelper.booleanValue(map.get(ATTR_REPLACE));
        this.spoolerId = JS7ConverterHelper.stringValue(map.get(ATTR_SPOOLER_ID));

        Node n = xpath.selectNode(node, "./" + ELEMENT_REMOTE_SCHEDULERS);
        if (n != null) {
            this.remoteSchedulers = new RemoteSchedulers(xpath, n);
        }
    }

    public Path getPath() {
        return path;
    }

    public boolean isAgent() {
        return !SOSString.isEmpty(remoteScheduler) || remoteSchedulers != null;
    }

    public RemoteSchedulers getRemoteSchedulers() {
        return remoteSchedulers;
    }

    public Integer getMaxProcesses() {
        return maxProcesses;
    }

    public String getName() {
        return name;
    }

    public String getRemoteScheduler() {
        return remoteScheduler;
    }

    public boolean isReplace() {
        return replace == null ? true : replace;
    }

    public String getSpoolerId() {
        return spoolerId;
    }

}
