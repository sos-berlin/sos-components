package com.sos.joc.monitoring.configuration.monitor.jms;

import javax.jms.DeliveryMode;
import javax.jms.Session;

import org.apache.activemq.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.joc.monitoring.configuration.Configuration;
import com.sos.joc.monitoring.configuration.monitor.AMonitor;
import com.sos.joc.monitoring.notification.notifier.NotifierJMS;
import com.sos.monitoring.MonitorType;

public class MonitorJMS extends AMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorJMS.class);

    private static int DEFAULT_ACKNOWLEDGE_MODE = Session.CLIENT_ACKNOWLEDGE;
    private static int DEFAULT_PRIOPITY = Message.DEFAULT_PRIORITY;
    private static int DEFAULT_DELIVERY_MODE = Message.DEFAULT_DELIVERY_MODE;
    private static long DEFAULT_TIME_TO_LIVE = Message.DEFAULT_TIME_TO_LIVE;
    private static String DEFAULT_DESTINATION = "Queue";

    public static String ELEMENT_NAME_CONNECTION_FACTORY = "ConnectionFactory";
    public static String ELEMENT_NAME_CONNECTION_JNDI = "ConnectionJNDI";

    private static String ATTRIBUTE_NAME_CLIENT_ID = "client_id";
    private static String ATTRIBUTE_NAME_DESTINATION_NAME = "destination_name";
    private static String ATTRIBUTE_NAME_DESTINATION = "destination";
    private static String ATTRIBUTE_NAME_ACKNOWLEDGE_MODE = "acknowledge_mode";
    private static String ATTRIBUTE_NAME_DELIVERY_MODE = "delivery_mode";
    private static String ATTRIBUTE_NAME_PRIORITY = "priority";
    private static String ATTRIBUTE_NAME_TIME_TO_LIVE = "time_to_live";

    private ConnectionFactory connectionFactory;
    private ConnectionJNDI connectionJNDI;

    private final String clientId;
    private final String destinationName;
    private final String destination;
    private final boolean isQueueDestination;
    private final int acknowledgeMode;
    private final int priority;
    private final int deliveryMode;
    private final long timeToLive;

    public MonitorJMS(Document document, Node node) throws Exception {
        super(document, node);

        Node cf = SOSXML.getChildNode(getRefElement(), ELEMENT_NAME_CONNECTION_FACTORY);
        if (cf != null) {
            connectionFactory = new ConnectionFactory(cf);
        }
        Node cj = SOSXML.getChildNode(getRefElement(), ELEMENT_NAME_CONNECTION_JNDI);
        if (cj != null) {
            connectionJNDI = new ConnectionJNDI(cj);
        }

        clientId = getValue(getRefElement().getAttribute(ATTRIBUTE_NAME_CLIENT_ID));
        destinationName = getValue(getRefElement().getAttribute(ATTRIBUTE_NAME_DESTINATION_NAME));
        destination = getValue(getRefElement().getAttribute(ATTRIBUTE_NAME_DESTINATION), DEFAULT_DESTINATION);
        isQueueDestination = destination.toLowerCase().equals(DEFAULT_DESTINATION.toLowerCase());
        acknowledgeMode = getAcknowledgeMode(getRefElement().getAttribute(ATTRIBUTE_NAME_ACKNOWLEDGE_MODE));
        priority = getValue(getRefElement().getAttribute(ATTRIBUTE_NAME_PRIORITY), DEFAULT_PRIOPITY);
        deliveryMode = getDeliveryMode(getRefElement().getAttribute(ATTRIBUTE_NAME_DELIVERY_MODE));
        timeToLive = getTimeToLive(getRefElement().getAttribute(ATTRIBUTE_NAME_TIME_TO_LIVE));
    }

    @Override
    public NotifierJMS createNotifier(Configuration conf) throws Exception {
        return new NotifierJMS(this, conf);
    }

    @Override
    public MonitorType getType() {
        return MonitorType.JMS;
    }

    private long getTimeToLive(String val) {
        try {
            return SOSDate.resolveAge("ms", val);
        } catch (Throwable ex) {
            LOGGER.warn(ex.toString(), ex);
            return DEFAULT_TIME_TO_LIVE;
        }
    }

    private int getAcknowledgeMode(String mode) {
        if (!SOSString.isEmpty(mode)) {
            switch (mode.trim().toUpperCase()) {
            case "SESSION.CLIENT_ACKNOWLEDGE":
                return Session.CLIENT_ACKNOWLEDGE;
            case "SESSION.AUTO_ACKNOWLEDGE":
                return Session.AUTO_ACKNOWLEDGE;
            case "SESSION.DUPS_OK_ACKNOWLEDGE":
                return Session.DUPS_OK_ACKNOWLEDGE;
            }
        }
        return DEFAULT_ACKNOWLEDGE_MODE;
    }

    private int getDeliveryMode(String mode) {
        if (!SOSString.isEmpty(mode)) {
            switch (mode.trim().toUpperCase()) {
            case "DELIVERYMODE.PERSISTENT":
                return DeliveryMode.PERSISTENT;
            case "DELIVERYMODE.NON_PERSISTENT":
                return DeliveryMode.NON_PERSISTENT;
            }
        }
        return DEFAULT_DELIVERY_MODE;
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public ConnectionJNDI getConnectionJNDI() {
        return connectionJNDI;
    }

    public String getClientId() {
        return clientId;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public String getDestination() {
        return destination;
    }

    public boolean isQueueDestination() {
        return isQueueDestination;
    }

    public int getAcknowledgeMode() {
        return acknowledgeMode;
    }

    public int getPriority() {
        return priority;
    }

    public int getDeliveryMode() {
        return deliveryMode;
    }

    public long getTimeToLive() {
        return timeToLive;
    }
}
