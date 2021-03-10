package com.sos.joc.cluster.configuration;

import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.joc.model.common.JocSecurityLevel;

public class JocConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocConfiguration.class);

    private final Path dataDirectory;
    private final Path hibernateConfiguration;
    private final Path resourceDirectory;
    private final JocSecurityLevel securityLevel;
    private final String memberId;

    private String timeZone;
    private String title;
    private Integer ordering;
    private String hostname;

    public JocConfiguration(String jocDataDirectory, String jocTimeZone, Path jocHibernateConfig, Path jocResourceDir,
            JocSecurityLevel jocSecurityLevel, String jocTitle, Integer jocOrdering) {
        setHostname();
        dataDirectory = Paths.get(jocDataDirectory);
        timeZone = jocTimeZone;
        hibernateConfiguration = jocHibernateConfig;
        resourceDirectory = jocResourceDir;
        securityLevel = jocSecurityLevel;
        title = SOSString.isEmpty(jocTitle) ? hostname : jocTitle;
        ordering = jocOrdering == null ? 0 : jocOrdering;
        memberId = hostname + ":" + SOSString.hash(dataDirectory.toString());
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

    public Path getResourceDirectory() {
        return resourceDirectory;
    }

    public String getHostname() {
        return hostname;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String val) {
        title = val;
    }

    public Integer getOrdering() {
        return ordering;
    }

    public JocSecurityLevel getSecurityLevel() {
        return securityLevel;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String val) {
        timeZone = val;
    }

    public Path getHibernateConfiguration() {
        return hibernateConfiguration;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }
}
