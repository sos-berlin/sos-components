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

    private static final String PROPERTIES_FILE = "joc.properties";
    private static final String HIBERNATE_CONFIGURATION = "hibernate.cfg.xml";

    private static final String DEFAULT_SECURITY_LEVEL = "low";

    private final Path dataDirectory;
    private final Path resourceDirectory;
    private final String timezone;

    private Path hibernateConfiguration;
    private String hostname;
    private String securityLevel;
    private String memberId;
    private String title;

    public JocConfiguration(String jocDataDirectory, String jocTimezone) {
        dataDirectory = Paths.get(jocDataDirectory);
        resourceDirectory = dataDirectory.resolve("resources").resolve("joc").normalize();
        timezone = jocTimezone;

        Properties p = readConfiguration(resourceDirectory.resolve(PROPERTIES_FILE).normalize());
        setHibernateConfiguration(p);
        if (p != null) {
            securityLevel = SOSString.isEmpty(p.getProperty("security_level")) ? DEFAULT_SECURITY_LEVEL : p.getProperty("security_level");
            title = SOSString.isEmpty(p.getProperty("title")) ? null : p.getProperty("title");
        }
        setHostname();
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

    private void setHibernateConfiguration(Properties p) {
        if (hibernateConfiguration == null) {
            hibernateConfiguration = resourceDirectory.resolve(HIBERNATE_CONFIGURATION).normalize();
            if (Files.exists(hibernateConfiguration)) {
                LOGGER.info(String.format("found hibernate configuration file %s", hibernateConfiguration));
            } else {
                if (p != null) {
                    hibernateConfiguration = resourceDirectory.resolve(p.getProperty("hibernate_configuration_file")).normalize();
                }
            }
        }
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
