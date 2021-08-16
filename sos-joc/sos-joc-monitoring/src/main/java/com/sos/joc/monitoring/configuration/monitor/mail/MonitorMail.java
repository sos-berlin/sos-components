package com.sos.joc.monitoring.configuration.monitor.mail;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.joc.monitoring.configuration.Configuration;
import com.sos.joc.monitoring.configuration.monitor.AMonitor;
import com.sos.joc.monitoring.notification.notifier.NotifierMail;
import com.sos.monitoring.MonitorType;

public class MonitorMail extends AMonitor {

   private static final String ELEMENT_NAME_FROM = "From";
    private static final String ELEMENT_NAME_TO = "To";
    private static final String ELEMENT_NAME_CC = "CC";
    private static final String ELEMENT_NAME_BCC = "BCC";
    private static final String ELEMENT_NAME_SUBJECT = "Subject";

    private static final String ATTRIBUTE_NAME_JOB_RESOURCES = "job_resources";
    private static final String ATTRIBUTE_NAME_CONTENT_TYPE = "content_type";
    private static final String ATTRIBUTE_NAME_CHARSET = "charset";
    private static final String ATTRIBUTE_NAME_ENCODING = "encoding";
    private static final String ATTRIBUTE_NAME_PRIORITY = "priority";

    private static final String JOB_RESOURCES_DELIMITER = ",";

    private final List<String> jobResources;
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

        jobResources = SOSString.toList(getValue(getRefElement().getAttribute(ATTRIBUTE_NAME_JOB_RESOURCES)), JOB_RESOURCES_DELIMITER);
        contentType = getValue(getRefElement().getAttribute(ATTRIBUTE_NAME_CONTENT_TYPE));
        charset = getValue(getRefElement().getAttribute(ATTRIBUTE_NAME_CHARSET));
        encoding = getValue(getRefElement().getAttribute(ATTRIBUTE_NAME_ENCODING));
        priority = getValue(getRefElement().getAttribute(ATTRIBUTE_NAME_PRIORITY));

        resolve();
    }

    @Override
    public NotifierMail createNotifier(Configuration conf) throws Exception {
        return new NotifierMail(this, conf);
    }

    @Override
    public MonitorType getType() {
        return MonitorType.MAIL;
    }

    public StringBuilder getInfo() {
        StringBuilder sb = new StringBuilder(super.getInfo());
        sb.append(" job_resources=").append(jobResources == null ? "" : String.join(",", jobResources));
        return sb;
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

    public List<String> getJobResources() {
        return jobResources;
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
