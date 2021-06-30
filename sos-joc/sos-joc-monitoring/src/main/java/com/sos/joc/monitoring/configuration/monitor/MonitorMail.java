package com.sos.joc.monitoring.configuration.monitor;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.sos.commons.xml.SOSXML;

public class MonitorMail extends AMonitor {

    private static String ELEMENT_NAME_FROM = "From";
    private static String ELEMENT_NAME_TO = "To";
    private static String ELEMENT_NAME_CC = "CC";
    private static String ELEMENT_NAME_BCC = "BCC";
    private static String ELEMENT_NAME_SUBJECT = "Subject";

    private static String ATTRIBUTE_NAME_JOB_RESOURCE = "job_resource";
    private static String ATTRIBUTE_NAME_CONTENT_TYPE = "content_type";
    private static String ATTRIBUTE_NAME_CHARSET = "charset";
    private static String ATTRIBUTE_NAME_ENCODING = "encoding";
    private static String ATTRIBUTE_NAME_PRIORITY = "priority";

    private final String jobResource;
    private final String contentType;
    private final String charset;
    private final String encoding;
    private final String priority;

    private String from;
    private String to;
    private String cc;
    private String bcc;
    private String subject;

    public MonitorMail(Document document, Node node) throws Exception {
        super(document, node);

        jobResource = getValue(getRefElement().getAttribute(ATTRIBUTE_NAME_JOB_RESOURCE));
        contentType = getValue(getRefElement().getAttribute(ATTRIBUTE_NAME_CONTENT_TYPE));
        charset = getValue(getRefElement().getAttribute(ATTRIBUTE_NAME_CHARSET));
        encoding = getValue(getRefElement().getAttribute(ATTRIBUTE_NAME_ENCODING));
        priority = getValue(getRefElement().getAttribute(ATTRIBUTE_NAME_PRIORITY));

        resolve();
    }

    private void resolve() {
        from = resolveElement(ELEMENT_NAME_FROM);
        to = resolveElement(ELEMENT_NAME_TO);
        cc = resolveElement(ELEMENT_NAME_CC);
        bcc = resolveElement(ELEMENT_NAME_BCC);
        subject = resolveElement(ELEMENT_NAME_SUBJECT);
    }

    private String resolveElement(String name) {
        Node node = SOSXML.getChildNode(getElement(), name);
        if (node == null) {
            node = SOSXML.getChildNode(getRefElement(), name);
        }
        return node == null ? null : SOSXML.getTrimmedValue(node);
    }

    public String getJobResource() {
        return jobResource;
    }

    public String getContentType() {
        return contentType;
    }

    public String getCharset() {
        return charset;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getPriority() {
        return priority;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getCC() {
        return cc;
    }

    public String getBCC() {
        return bcc;
    }

    public String getSubject() {
        return subject;
    }
}
