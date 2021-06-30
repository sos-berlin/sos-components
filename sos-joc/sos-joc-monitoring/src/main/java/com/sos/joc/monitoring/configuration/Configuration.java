package com.sos.joc.monitoring.configuration;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;

public class Configuration {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    private List<Notification> typeAll;
    private List<Notification> typeOnError;
    private List<Notification> typeOnSuccess;

    private boolean exists;

    public void process(String xml) {
        init();

        if (SOSString.isEmpty(xml)) {
            return;
        }

        Document doc;
        try {
            doc = SOSXML.parse(xml);

            NodeList notifications = SOSXML.newXPath().selectNodes(doc, "./Configurations/Notifications/Notification");
            if (notifications != null) {
                for (int i = 0; i < notifications.getLength(); i++) {
                    add2type(new Notification(doc, (Element) notifications.item(i)));
                    exists = true;
                }
            }
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
            exists = false;
        }
    }

    private void init() {
        typeAll = new ArrayList<>();
        typeOnError = new ArrayList<>();
        typeOnSuccess = new ArrayList<>();
        exists = false;
    }

    private void add2type(Notification n) {
        switch (n.getType()) {
        case ALL:
            typeAll.add(n);
            break;
        case ON_ERROR:
            typeOnError.add(n);
            break;
        case ON_SUCCESS:
            typeOnSuccess.add(n);
            break;
        }
    }

    public List<Notification> getTypeAll() {
        return typeAll;
    }

    public List<Notification> getTypeOnError() {
        return typeOnError;
    }

    public List<Notification> getTypeOnSuccess() {
        return typeOnSuccess;
    }

    public boolean exists() {
        return exists;
    }
}
