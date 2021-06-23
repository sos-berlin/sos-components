package com.sos.joc.notification.configuration.monitor;

import java.nio.file.Paths;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;

public class MonitorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorTest.class);

    @Ignore
    @Test
    public void test() throws Exception {

        Document doc = SOSXML.parse(Paths.get("src/test/resources/Configurations.xml"));

        Node notification = SOSXML.newXPath().selectNode(doc, "./Configurations/Notifications/Notification[1]/NotificationMonitors");
        if (notification != null) {
            List<Element> elements = SOSXML.getChildElemens(notification);
            for (Element el : elements) {
                LOGGER.info("EL: " + el.getNodeName());
                switch (el.getNodeName()) {
                case AMonitor.ELEMENT_NAME_COMMAND_FRAGMENT_REF:
                    MonitorCommand mc = new MonitorCommand(doc, (Node) el);
                    LOGGER.info("   " + SOSString.toString(mc));
                    LOGGER.info("   " + mc.getMessage());
                    LOGGER.info("   " + mc.getCommand());
                    break;
                case AMonitor.ELEMENT_NAME_MAIL_FRAGMENT_REF:
                    MonitorMail mm = new MonitorMail(doc, (Node) el);
                    LOGGER.info("   " + SOSString.toString(mm));
                    LOGGER.info("   " + mm.getMessage());
                    break;
                }
            }
        }

    }
}
