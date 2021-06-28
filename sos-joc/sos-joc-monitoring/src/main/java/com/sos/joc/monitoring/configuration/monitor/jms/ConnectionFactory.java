package com.sos.joc.monitoring.configuration.monitor.jms;

import java.util.LinkedHashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.joc.monitoring.configuration.AElement;

public class ConnectionFactory extends AElement {

    public static String DEFAULT_CONNECTION_FACTORY = "org.apache.activemq.ActiveMQConnectionFactory";

    private static String ELEMENT_NAME_CONSTRUCTOR_ARGUMENTS = "ConstructorArguments";
    private static String ELEMENT_NAME_ARGUMENT = "Argument";

    private static String ATTRIBUTE_NAME_JAVA_CLASS = "java_class";
    private static String ATTRIBUTE_NAME_USER_NAME = "user_name";
    private static String ATTRIBUTE_NAME_PASSWORD = "password";
    private static String ATTRIBUTE_NAME_TYPE = "type";

    private final String javaClass;
    private final String userName;
    private final String password;
    private final LinkedHashMap<String, String> constructorArguments;

    public ConnectionFactory(Node node) throws Exception {
        super(node);

        javaClass = getValue(getElement().getAttribute(ATTRIBUTE_NAME_JAVA_CLASS), DEFAULT_CONNECTION_FACTORY);
        userName = getValue(getElement().getAttribute(ATTRIBUTE_NAME_USER_NAME));
        password = getValue(getElement().getAttribute(ATTRIBUTE_NAME_PASSWORD));
        constructorArguments = readConstructorArguments();
    }

    private LinkedHashMap<String, String> readConstructorArguments() throws Exception {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        NodeList nl = null;
        NodeList nlca = getElement().getElementsByTagName(ELEMENT_NAME_CONSTRUCTOR_ARGUMENTS);
        if (nlca != null && nlca.getLength() > 0) {
            nl = ((Element) nlca.item(0)).getElementsByTagName(ELEMENT_NAME_ARGUMENT);
        }
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                Element ca = (Element) nl.item(i);
                String type = ca.getAttribute(ATTRIBUTE_NAME_TYPE);
                if (!SOSString.isEmpty(type)) {
                    String value = SOSXML.getValue(ca);
                    if (!SOSString.isEmpty(value)) {
                        map.put(type, value);
                    }
                }
            }
        }
        return map;
    }

    public String getJavaClass() {
        return javaClass;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public LinkedHashMap<String, String> getConstructorArguments() {
        return constructorArguments;
    }
}
