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
    private final String jocId;
    private final boolean isApiServer;
    private final String version;
    
    private String clusterId;
    private String timeZone;
    private String title;
    private Integer ordering;
    private String hostname;
    private String uri;
    private boolean clusterMode;

    public JocConfiguration(String jocDataDirectory, String jocTimeZone, Path jocHibernateConfig, Path jocResourceDir,
            JocSecurityLevel jocSecurityLevel, boolean isApiServer, String jocTitle, String jocClusterId, Integer jocOrdering, String jocId,
            String version) {

        setHostname();

        this.dataDirectory = Paths.get(jocDataDirectory);
        this.timeZone = jocTimeZone;
        this.hibernateConfiguration = jocHibernateConfig;
        this.resourceDirectory = jocResourceDir;

        this.securityLevel = jocSecurityLevel;
        this.isApiServer = isApiServer;
        this.version = version;
        this.title = SOSString.isEmpty(jocTitle) ? hostname : jocTitle;
        this.clusterId = SOSString.isEmpty(jocClusterId) ? "joc" : jocClusterId;
        this.ordering = jocOrdering == null ? 0 : jocOrdering;
        this.jocId = jocId;

        this.memberId = hostname + ":" + SOSString.hash256(dataDirectory.toString());
    }

    private String setHostname() {
        if (hostname == null) {
            hostname = "unknown";
            try {
                hostname = SOSShell.getLocalHostName();
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

    public String getClusterId() {
        return clusterId;
    }

    public String getJocId() {
        return jocId;
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

    public String getUri() {
        return uri;
    }

    public void setUri(String val) {
        uri = val;
    }

    public void setClusterMode(boolean val) {
        clusterMode = val;
    }

    public boolean getClusterMode() {
        return clusterMode;
    }

    public boolean isApiServer() {
        return isApiServer;
    }
    
    public String getVersion() {
        return version;
    }
}
