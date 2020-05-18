package com.sos.joc.cluster.configuration;

import java.net.UnknownHostException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;

public class JocConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocConfiguration.class);

    private static final String DEFAULT_SECURITY_LEVEL = "low";
    private static final int DEFAULT_PORT = 4446;

    private String hibernateConfigurationFile;
    private String hostname;
    private String securityLevel;
    private String memberId;
    private int port;

    public JocConfiguration(Properties p) {
        hibernateConfigurationFile = p.getProperty("hibernate_configuration_file");
        securityLevel = SOSString.isEmpty(p.getProperty("security_level")) ? DEFAULT_SECURITY_LEVEL : p.getProperty("security_level");
        port = SOSString.isEmpty(p.getProperty("port")) ? DEFAULT_PORT : Integer.parseInt(p.getProperty("port"));
        setHostname();
        memberId = hostname + ":" + port;
    }

    private String setHostname() {
        if (hostname == null) {
            hostname = "unknown";
            try {
                hostname = SOSShell.getHostname();
            } catch (UnknownHostException e) {
                LOGGER.error(e.toString(), e);
            }
        }
        return hostname;
    }

    public String getHibernateConfigurationFile() {
        return hibernateConfigurationFile;
    }

    public String getHostname() {
        return hostname;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getSecurityLevel() {
        return securityLevel;
    }

    public int getPort() {
        return port;
    }

}
