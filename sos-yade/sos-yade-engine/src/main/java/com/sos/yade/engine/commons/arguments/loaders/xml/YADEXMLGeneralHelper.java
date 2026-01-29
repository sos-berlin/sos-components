package com.sos.yade.engine.commons.arguments.loaders.xml;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.xml.SOSXML;

public class YADEXMLGeneralHelper {

    protected static void parse(YADEXMLArgumentsLoader argsLoader, Node general) throws Exception {
        if (general == null) {
            return;
        }
        NodeList nl = general.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "SystemPropertyFiles":
                    parseSystemPropertyFiles(argsLoader, n);
                    break;
                case "Notifications":
                    parseNotifications(argsLoader, n, general.getLocalName());
                    break;
                case "RetryOnConnectionError":
                    parseRetryOnConnectionError(argsLoader, n);
                    break;
                default:
                    break;
                }
            }
        }
    }

    public static void parseSystemPropertyFiles(YADEXMLArgumentsLoader argsLoader, Node systemPropertyFiles) {
        NodeList nl = systemPropertyFiles.getChildNodes();
        if (nl == null) {
            return;
        }
        List<Path> files = new ArrayList<>();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && "SystemPropertyFile".equals(n.getNodeName())) {
                files.add(Path.of(argsLoader.getValue(n)));
            }
        }
        if (files.size() > 0) {
            argsLoader.getClientArgs().getSystemPropertyFiles().setValue(files);
        }
    }

    public static void parseNotifications(YADEXMLArgumentsLoader argsLoader, Node notifications, String parentInfo) throws Exception {
        argsLoader.initializeNotificationArgsIfNull();

        NodeList nl = notifications.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "MailServerFragmentRef":
                    parseMailServerFragmentRef(argsLoader, n, parentInfo);
                    break;
                }
            }
        }
    }

    public static void parseMailServerFragmentRef(YADEXMLArgumentsLoader argsLoader, Node ref, String parentInfo) throws Exception {
        String exp = "Fragments/MailServerFragments/MailServerFragment[@name='" + SOSXML.getAttributeValue(ref, "ref") + "']";
        Node fragment = argsLoader.getXPath().selectNode(argsLoader.getRoot(), exp);
        if (fragment == null) {
            throw new SOSMissingDataException("[" + parentInfo + "][" + exp + "]referenced MailServerFragment not found");
        }
        NodeList nl = fragment.getChildNodes();
        int len = nl.getLength();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    switch (n.getNodeName()) {
                    case "MailHost":
                        parseMailHost(argsLoader, n);
                        break;
                    case "QueueDirectory":
                        argsLoader.setStringArgumentValue(argsLoader.getNotificationArgs().getMailServer().getQueueDirectory(), n);
                        break;
                    }
                }
            }
        }
    }

    public static void parseRetryOnConnectionError(YADEXMLArgumentsLoader argsLoader, Node retryOnConnectionError) throws Exception {
        NodeList nl = retryOnConnectionError.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "RetryCountMax":
                    argsLoader.setIntegerArgumentValue(argsLoader.getArgs().getConnectionErrorRetryCountMax(), n);
                    break;
                case "RetryInterval":
                    argsLoader.setStringArgumentValue(argsLoader.getArgs().getConnectionErrorRetryInterval(), n);
                    break;
                }
            }
        }
    }

    private static void parseMailHost(YADEXMLArgumentsLoader argsLoader, Node mailHost) throws Exception {
        NodeList nl = mailHost.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "BasicConnection":
                    parseMailHostBasicConnection(argsLoader, n);
                    break;
                case "BasicAuthentication":
                    parseMailHostBasicAuthentication(argsLoader, n);
                    break;
                case "MailSettings":
                    parseMailHostMailSettings(argsLoader, n);
                    break;
                }
            }
        }
    }

    private static void parseMailHostBasicConnection(YADEXMLArgumentsLoader argsLoader, Node mailHostBasicConnection) throws Exception {
        NodeList nl = mailHostBasicConnection.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Hostname":
                    argsLoader.getNotificationArgs().getMailServer().applyHostname(argsLoader.getValue(n));
                    break;
                case "Port":
                    argsLoader.getNotificationArgs().getMailServer().applyPort(argsLoader.getValue(n));
                    break;
                case "ConnectTimeout":
                    argsLoader.getNotificationArgs().getMailServer().applyConnectTimeout(argsLoader.getValue(n));
                    break;
                }
            }
        }
    }

    private static void parseMailHostBasicAuthentication(YADEXMLArgumentsLoader argsLoader, Node mailHostBasicAuthentication) throws Exception {
        NodeList nl = mailHostBasicAuthentication.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Account":
                    argsLoader.getNotificationArgs().getMailServer().applyAccount(argsLoader.getValue(n));
                    break;
                case "Password":
                    argsLoader.getNotificationArgs().getMailServer().applyPassword(argsLoader.getValue(n));
                    break;
                }
            }
        }
    }

    private static void parseMailHostMailSettings(YADEXMLArgumentsLoader argsLoader, Node mailHostMailSettings) throws Exception {
        NodeList nl = mailHostMailSettings.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "MailSetting":
                    String name = argsLoader.getValue(n, "name");
                    String value = argsLoader.getValue(n);
                    argsLoader.getNotificationArgs().getMailServer().addMailSetting(name, value);
                    break;
                }
            }
        }
    }
}
