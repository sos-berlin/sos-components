package com.sos.joc.notification.configuration.monitor;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class MonitorNSCA extends AMonitor {

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
        serviceHost = getRefElement().getAttribute(ATTRIBUTE_NAME_SERVICE_HOST);
        monitorHost = getRefElement().getAttribute(ATTRIBUTE_NAME_MONITOR_HOST);
        monitorEncryption = getRefElement().getAttribute(ATTRIBUTE_NAME_MONITOR_ENCRYPTION);
        monitorPassword = getRefElement().getAttribute(ATTRIBUTE_NAME_MONITOR_PASSWORD);

        monitorPort = set(ATTRIBUTE_NAME_MONITOR_PORT, -1);
        monitorConnectionTimeout = set(ATTRIBUTE_NAME_MONITOR_CONNECTION_TIMEOUT, -1);
        monitorResponseTimeout = set(ATTRIBUTE_NAME_MONITOR_RESPONSE_TIMEOUT, -1);

        // from Element
        serviceNameOnError = getElement().getAttribute(ATTRIBUTE_NAME_SERVICE_NAME_ON_ERROR);
        serviceStatusOnError = getElement().getAttribute(ATTRIBUTE_NAME_SERVICE_STATUS_ON_ERROR);
        serviceNameOnSuccess = getElement().getAttribute(ATTRIBUTE_NAME_SERVICE_NAME_ON_SUCCESS);
        serviceStatusOnSuccess = getElement().getAttribute(ATTRIBUTE_NAME_SERVICE_STATUS_ON_SUCCESS);
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
