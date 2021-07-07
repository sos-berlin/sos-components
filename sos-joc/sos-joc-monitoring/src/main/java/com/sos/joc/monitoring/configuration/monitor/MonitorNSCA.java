package com.sos.joc.monitoring.configuration.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.sos.joc.monitoring.configuration.Configuration;
import com.sos.joc.monitoring.notification.notifier.NotifierNSCA;

public class MonitorNSCA extends AMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorNSCA.class);

    private static String ATTRIBUTE_NAME_SERVICE_HOST = "service_host";
    private static String ATTRIBUTE_NAME_MONITOR_HOST = "monitor_host";
    private static String ATTRIBUTE_NAME_MONITOR_PORT = "monitor_port";
    private static String ATTRIBUTE_NAME_MONITOR_ENCRYPTION = "monitor_encryption";
    private static String ATTRIBUTE_NAME_MONITOR_PASSWORD = "monitor_password";
    private static String ATTRIBUTE_NAME_MONITOR_CONNECTION_TIMEOUT = "monitor_connection_timeout";
    private static String ATTRIBUTE_NAME_MONITOR_RESPONSE_TIMEOUT = "monitor_response_timeout";

    private static String ATTRIBUTE_NAME_SERVICE_NAME_ON_ERROR = "service_name_on_error";
    private static String ATTRIBUTE_NAME_SERVICE_STATUS_ON_ERROR = "service_status_on_error";
    private static String ATTRIBUTE_NAME_SERVICE_NAME_ON_SUCCESS = "service_name_on_success";
    private static String ATTRIBUTE_NAME_SERVICE_STATUS_ON_SUCCESS = "service_status_on_success";

    private final String serviceHost;
    private final int monitorPort;
    private final String monitorHost;
    private final String monitorEncryption;
    private final String monitorPassword;
    private final int monitorConnectionTimeout;
    private final int monitorResponseTimeout;

    private final String serviceNameOnError;
    private final String serviceStatusOnError;
    private final String serviceNameOnSuccess;
    private final String serviceStatusOnSuccess;

    public MonitorNSCA(Document document, Node node) throws Exception {
        super(document, node);

        // from RefElement
        serviceHost = getValue(getRefElement().getAttribute(ATTRIBUTE_NAME_SERVICE_HOST));
        monitorHost = getValue(getRefElement().getAttribute(ATTRIBUTE_NAME_MONITOR_HOST));
        monitorEncryption = getValue(getRefElement().getAttribute(ATTRIBUTE_NAME_MONITOR_ENCRYPTION));
        monitorPassword = getRefElement().getAttribute(ATTRIBUTE_NAME_MONITOR_PASSWORD);

        monitorPort = set(ATTRIBUTE_NAME_MONITOR_PORT, -1);
        monitorConnectionTimeout = set(ATTRIBUTE_NAME_MONITOR_CONNECTION_TIMEOUT, -1);
        monitorResponseTimeout = set(ATTRIBUTE_NAME_MONITOR_RESPONSE_TIMEOUT, -1);

        // from Element
        serviceNameOnError = getAttributeValue(ATTRIBUTE_NAME_SERVICE_NAME_ON_ERROR);
        serviceStatusOnError = getAttributeValue(ATTRIBUTE_NAME_SERVICE_STATUS_ON_ERROR);
        serviceNameOnSuccess = getAttributeValue(ATTRIBUTE_NAME_SERVICE_NAME_ON_SUCCESS);
        serviceStatusOnSuccess = getAttributeValue(ATTRIBUTE_NAME_SERVICE_STATUS_ON_SUCCESS);
    }

    @Override
    public NotifierNSCA createNotifier(Configuration conf) {
        try {
            return new NotifierNSCA(this, conf);
        } catch (Throwable e) {
            LOGGER.error(String.format("[createNotifier]%s", e.toString()), e);
            return null;
        }
    }

    private int set(String attrName, int defaultValue) {
        int result = defaultValue;
        String val = getRefElement().getAttribute(attrName);
        if (val != null) {
            try {
                result = Integer.parseInt(val);
            } catch (Throwable e) {
            }
        }
        return result;
    }

    public String getServiceHost() {
        return serviceHost;
    }

    public String getMonitorPassword() {
        return monitorPassword;
    }

    public int getMonitorConnectionTimeout() {
        return monitorConnectionTimeout;
    }

    public int getMonitorResponseTimeout() {
        return monitorResponseTimeout;
    }

    public int getMonitorPort() {
        return monitorPort;
    }

    public String getMonitorHost() {
        return monitorHost;
    }

    public String getMonitorEncryption() {
        return monitorEncryption;
    }

    public String getServiceNameOnError() {
        return serviceNameOnError;
    }

    public String getServiceStatusOnError() {
        return serviceStatusOnError;
    }

    public String getServiceNameOnSuccess() {
        return serviceNameOnSuccess;
    }

    public String getServiceStatusOnSuccess() {
        return serviceStatusOnSuccess;
    }
}
