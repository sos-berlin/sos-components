package com.sos.joc.cluster.configuration;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;

public class JocConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocConfiguration.class);

    private final Path dataDirectory;
    private final String timezone;
    private final Path hibernateConfiguration;
    private final Path resourceDirectory;
    private final String securityLevel;
    private final String memberId;
    private final String title;
    private final Integer ordering;

    private String hostname;

    public JocConfiguration(String jocDataDirectory, String jocTimezone, Path jocHibernateConfig, Path jocResourceDir, String jocSecurityLevel,
            String jocTitle, Integer jocOrdering) {
        setHostname();
        dataDirectory = Paths.get(jocDataDirectory);
        timezone = jocTimezone;
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

    public static Properties readConfiguration(Path path) {
        String method = "readConfiguration";
        if (!Files.exists(path)) {
            LOGGER.info(String.format("[%s][%s]not found. use defaults.", method, path));
            return new Properties();
        }

        LOGGER.info(String.format("[%s]%s", method, path));

        Properties conf = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            conf.load(in);
        } catch (Exception ex) {
            String addition = "";
            if (ex instanceof FileNotFoundException) {
                if (Files.exists(path) && !Files.isReadable(path)) {
                    addition = " (exists but not readable)";
                }
            }
            LOGGER.error(String.format("[%s][%s]error on read the properties file%s: %s", method, path, addition, ex.toString()), ex);

        }
        LOGGER.info(String.format("[%s]%s", method, conf));
        return conf;
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

    public Integer getOrdering() {
        return ordering;
    }

    public String getSecurityLevel() {
        return securityLevel;
    }

    public String getTimezone() {
        return timezone;
    }

    public Path getHibernateConfiguration() {
        return hibernateConfiguration;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }
}
