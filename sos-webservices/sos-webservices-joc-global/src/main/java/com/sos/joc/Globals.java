package com.sos.joc;

import java.io.InputStream;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.TimeZone;

import javax.json.Json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateFactoryBuildException;
import com.sos.commons.hibernate.exception.SOSHibernateOpenSessionException;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.JocWebserviceDataContainer;
import com.sos.joc.classes.SSLContext;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsDailyPlan;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsGit;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsIdentityService;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsLogNotification;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsUser;
import com.sos.joc.db.DBLayer;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.Version;
import com.sos.joc.model.common.JocSecurityLevel;

import jakarta.ws.rs.core.UriInfo;

public class Globals {

    public static SOSHibernateFactory sosHibernateFactory;
    public static JocWebserviceDataContainer jocWebserviceDataContainer = JocWebserviceDataContainer.getInstance();
    public static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(
            DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, true).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).configure(
                    SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
    public static ObjectMapper prettyPrintObjectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, true).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false).configure(SerializationFeature.INDENT_OUTPUT, true);
    public static JocCockpitProperties sosCockpitProperties;
    public static TimeZone jocTimeZone = TimeZone.getDefault();
    public static Path servletContextRealPath = null;
    public static URI servletBaseUri = null;
    public static String servletContextContextPath = null; // /joc
    public static String apiVersion = "";
    public static String inventoryVersion = "";
    public static Version curVersion = new Version();
    public static boolean isApiServer = false;
    public static Boolean prevWasApiServer = null;

    public static long maxSizeOfLogsToDisplay = 1024 * 1024 * 10L; // 10MB
    public static long timeoutToDeleteTempFiles = 1000 * 60 * 3L;
    public static int httpConnectionTimeout = 2000;
    public static int httpSocketTimeout = 5000;
    public static int maxResponseDuration = 55;
    public static boolean withHostnameVerification = false;

    private static final String HIBERNATE_CONFIGURATION_FILE = "hibernate_configuration_file";
    private static final Logger LOGGER = LoggerFactory.getLogger(Globals.class);
    private static JocSecurityLevel jocSecurityLevel = null;
    private static ConfigurationGlobals configurationGlobals = null;
    private static String clusterId = null;
    private static Integer ordering = null;
    public static Integer iamSessionTimeout;

    public static synchronized SOSHibernateFactory getHibernateFactory() throws JocConfigurationException {
        if (sosHibernateFactory == null || sosHibernateFactory.getSessionFactory() == null) {
            try {
                Path confFile = getHibernateConfFile();
                sosHibernateFactory = new SOSHibernateFactory(confFile);
                sosHibernateFactory.addClassMapping(DBLayer.getJocClassMapping());
                sosHibernateFactory.setAutoCommit(true);
                sosHibernateFactory.build(true);
            } catch (SOSHibernateFactoryBuildException e) {
                sosHibernateFactory = null;
                throw new JocConfigurationException(e);
            }
        }
        return sosHibernateFactory;
    }

    public static SOSHibernateSession createSosHibernateStatelessConnection(String identifier) throws JocConfigurationException,
            DBOpenSessionException {
        try {
            getHibernateFactory();
            SOSHibernateSession sosHibernateSession = sosHibernateFactory.openStatelessSession(identifier);
            return sosHibernateSession;
        } catch (SOSHibernateOpenSessionException e) {
            throw new DBOpenSessionException(e);
        }
    }

    public static void readUnmodifiables() {
        readJocCockpitVersion();
        readApiSchemaVersion();
        readInventorySchemaVersion();
        LOGGER.info("Security Level = " + getJocSecurityLevel().value());
        if (isApiServer) {
            LOGGER.info("Started as API Server");
        }
        setMaxResponseDuration();
    }

    public static void setProperties() {
        if (sosCockpitProperties.isChanged()) {
            setJobSchedulerConnectionTimeout();
            setJobSchedulerSocketTimeout();
            setHostnameVerification();
        }
        setSSLContext();
    }

    private static void setSSLContext() {
        SSLContext.getInstance().setSSLContext(sosCockpitProperties);
    }

    public static void beginTransaction(SOSHibernateSession session) {
        try {
            if (session != null) {
                session.beginTransaction();
            }
        } catch (Exception e) {
        }
    }

    public static void rollback(SOSHibernateSession session) {
        try {
            if (session != null && session.isTransactionOpened()) {
                session.rollback();
            }
        } catch (Exception e) {
        }
    }

    public static void commit(SOSHibernateSession session) {
        try {
            if (session != null) {
                session.commit();
            }
        } catch (Exception e) {
        }
    }

    public static Path getHibernateConfFile() throws JocConfigurationException {
        String confFile = null;

        if (sosCockpitProperties == null) {
            throw new JocConfigurationException("JOC Properties are not initialized");
        }

        confFile = sosCockpitProperties.getProperty(HIBERNATE_CONFIGURATION_FILE, "hibernate.cfg.xml");
        if (confFile.trim().isEmpty()) {
            throw new JocConfigurationException(String.format("Couldn't find the property '%1$s' in %2$s", HIBERNATE_CONFIGURATION_FILE,
                    sosCockpitProperties.getPropertiesFile()));
        }

        confFile = confFile.trim();
        Path p = sosCockpitProperties.resolvePath(confFile);
        if (p != null) {
            if (!Files.exists(p) || Files.isDirectory(p)) {
                throw new JocConfigurationException(String.format("hibernate configuration (%1$s) is set but couldn't find the file (%2$s).",
                        confFile, p.toString()));
            } else {
                confFile = p.toString().replace('\\', '/');
            }
        } else {
            throw new JocConfigurationException(String.format("hibernate configuration (%1$s) is set but couldn't find the file.", confFile));
        }
        return p;
    }

    public static String getStrippedInventoryVersion() {
        if (inventoryVersion == null) {
            return null;
        }
        return inventoryVersion.replaceFirst("-.*$", "");
    }

    private static void readJocCockpitVersion() {
        InputStream stream = null;
        String versionFile = "/version.json";
        Version version = new Version();
        LOGGER.info("Java version = " + System.getProperty("java.version"));
        try {
            // search in WEB-INF/classes of the web app
            stream = Globals.class.getClassLoader().getResourceAsStream(versionFile);
            if (stream != null) {
                // version = Json.createReader(stream).readObject().getString("version", "unknown");
                version = objectMapper.readValue(stream, Version.class);
                LOGGER.info("JOC Cockpit version = " + version.getVersion());
            } else {
                // fallback: search in root folder of the web app
                stream = Globals.class.getResourceAsStream(versionFile);
                if (stream != null) {
                    // version = Json.createReader(stream).readObject().getString("version", "unknown");
                    version = objectMapper.readValue(stream, Version.class);
                    LOGGER.info("JOC Cockpit version = " + version.getVersion());
                } else {
                    LOGGER.warn(String.format("Couldn't find the version file '%1$s' in the classpath", versionFile));
                }
            }
            if (version.getVersion() != null && !version.getVersion().isEmpty()) {
                curVersion = version;
            }
        } catch (Exception e) {
            LOGGER.warn(String.format("Error while reading %1$s from the classpath: ", versionFile), e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
            }
        }
    }

    private static void readApiSchemaVersion() {
        InputStream stream = null;
        String versionFile = "/api-schema-version.json";
        String version = "";
        try {
            // search in WEB-INF/classes of the web app
            stream = Globals.class.getClassLoader().getResourceAsStream(versionFile);
            if (stream != null) {
                version = Json.createReader(stream).readObject().getString("version", "unknown");
                LOGGER.info("API schema version = " + version);
            } else {
                // fallback: search in root folder of the web app
                stream = Globals.class.getResourceAsStream(versionFile);
                if (stream != null) {
                    version = Json.createReader(stream).readObject().getString("version", "unknown");
                    LOGGER.info("API schema version = " + version);
                } else {
                    LOGGER.warn(String.format("Couldn't find the version file '%1$s' in the classpath", versionFile));
                }
            }
            if (!"unknown".equals(version)) {
                apiVersion = version;
            }
        } catch (Exception e) {
            LOGGER.warn(String.format("Error while reading %1$s from the classpath: ", versionFile), e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
            }
        }
    }

    private static void readInventorySchemaVersion() {
        InputStream stream = null;
        String versionFile = "/inventory-schema-version.json";
        String version = "";
        try {
            // fallback: search in root folder of the web app
            stream = Globals.class.getClassLoader().getResourceAsStream(versionFile);
            if (stream != null) {
                version = Json.createReader(stream).readObject().getString("version", "unknown");
                LOGGER.info("Inventory schema version = " + version);
            } else {
                // Workaround for Grizzly
                stream = Globals.class.getResourceAsStream(versionFile);
                if (stream != null) {
                    version = Json.createReader(stream).readObject().getString("version", "unknown");
                    LOGGER.info("Inventory schema version = " + version);
                } else {
                    LOGGER.warn(String.format("Couldn't find the version file '%1$s' in the classpath", versionFile));
                }
            }
            if (!"unknown".equals(version)) {
                inventoryVersion = version;
            }
        } catch (Exception e) {
            LOGGER.warn(String.format("Error while reading %1$s from the classpath: ", versionFile), e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
            }
        }
    }

    private static void setMaxResponseDuration() {
        if (sosCockpitProperties != null) {
            maxResponseDuration = sosCockpitProperties.getProperty("max_response_duration", maxResponseDuration);
            // LOGGER.info("Max. response duration for ./events api = " +
            // maxResponseDuration + "s");
        }
    }

    private static void setJobSchedulerConnectionTimeout() {
        int defaultSeconds = 2;
        if (sosCockpitProperties != null) {
            int seconds = sosCockpitProperties.getProperty("controller_connection_timeout", defaultSeconds);
            httpConnectionTimeout = seconds * 1000;
            LOGGER.info("HTTP(S) connection timeout = " + seconds + "s");
        }
    }

    private static void setJobSchedulerSocketTimeout() {
        int defaultSeconds = 5;
        if (sosCockpitProperties != null) {
            int seconds = sosCockpitProperties.getProperty("controller_socket_timeout", defaultSeconds);
            httpSocketTimeout = seconds * 1000;
            LOGGER.info("HTTP(S) socket timeout = " + seconds + "s");
        }
    }

    private static void setHostnameVerification() {
        boolean defaultVerification = false;
        if (sosCockpitProperties != null) {
            withHostnameVerification = sosCockpitProperties.getProperty("https_with_hostname_verification", defaultVerification);
            LOGGER.info("HTTPS with hostname verification in certificate = " + withHostnameVerification);
        }
    }

    public static void disconnect(SOSHibernateSession sosHibernateSession) {
        if (sosHibernateSession != null) {
            sosHibernateSession.close();
        }
    }

    public static void closeFactory() {
        if (sosHibernateFactory != null) {
            sosHibernateFactory.close();
            sosHibernateFactory = null;
        }
    }

    public static String normalizePath(String path) {
        if (path == null) {
            return null;
        }
        return Paths.get("/").resolve(path).normalize().toString().replace('\\', '/');
    }

    public static JocSecurityLevel getJocSecurityLevel() {
        // the JocSecurity classes should have a method getJocSecurityLevel which is
        // callable static during an abstract class
        if (jocSecurityLevel == null) {
            jocSecurityLevel = JocSecurityLevel.LOW; // default
            try {
                InputStream stream = Globals.class.getResourceAsStream("/joc-settings.properties");
                if (stream != null) {
                    Properties properties = new Properties();
                    properties.load(stream);
                    try {
                        jocSecurityLevel = JocSecurityLevel.fromValue(properties.getProperty("security_level", JocSecurityLevel.LOW.value())
                                .toUpperCase());
                    } catch (Exception e) {
                        //
                    }
                    isApiServer = properties.getProperty("as_api_server", "no").equals("yes");
                    String prevAsApiServer = properties.getProperty("prev_as_api_server");
                    if (prevAsApiServer != null) {
                        prevWasApiServer = prevAsApiServer.equals("yes");
                    }
                }
            } catch (Exception e) {
                LOGGER.error(String.format("Error while reading %1$s:", "joc-settings.properties"), e);
            }
        }
        return jocSecurityLevel;
    }

    // Only called by tests
    public static void setJocSecurityLevel(JocSecurityLevel level) {
        getJocSecurityLevel();
        jocSecurityLevel = level;
    }

    public static String getClusterId() throws JocConfigurationException {
        if (clusterId == null) {
            getJocSecurityLevel();
            clusterId = isApiServer ? "api" : "joc";

            // if (sosCockpitProperties == null) {
            // sosCockpitProperties = new JocCockpitProperties();
            // }
            //
            // clusterId = sosCockpitProperties.getProperty("cluster_id");
            //
            // if (clusterId == null || clusterId.isEmpty()) {
            // throw new JocConfigurationException("The 'cluster_id' setting in the joc.properties file is not defined.");
            // } else if (clusterId.length() > 10) {
            // throw new JocConfigurationException("The 'cluster_id' setting in the joc.properties file can be only max. 10 characters long.");
            // }
        }
        return clusterId;
    }

    public static void setClusterId(String val) {
        clusterId = val;
    }

    public static String getJocId() throws JocConfigurationException {
        return getClusterId() + "#" + getOrdering();
    }

    public static Integer getOrdering() throws JocConfigurationException {
        if (ordering == null) {
            if (sosCockpitProperties == null) {
                sosCockpitProperties = new JocCockpitProperties();
            }

            ordering = Integer.valueOf(sosCockpitProperties.getProperty("ordering", -1));

            if (ordering.intValue() < 0 || ordering.intValue() > 99) {
                throw new JocConfigurationException("The 'ordering' setting in the joc.properties file must have an integer value from 0 to 99.");
            }
        }
        return ordering;
    }

    public static void setOrdering(Integer val) {
        ordering = val;
    }

    public static String getDataDirectory() {
        return Paths.get(System.getProperty("user.dir")).toString();
    }

    public static String getMemberId() {
        return getHostname() + ":" + SOSString.hash256(getDataDirectory());
    }

    public static String getHostname() {
        try {
            return SOSShell.getHostname();
        } catch (UnknownHostException e) {
            LOGGER.error(e.toString(), e);
        }
        return "unknown";
    }

    public static void setServletBaseUri(UriInfo uriInfo) {

        if (uriInfo == null) {
            return;
        }
        try {
            if (servletBaseUri == null) {
                if (servletContextContextPath == null) {
                    servletBaseUri = uriInfo.getBaseUri();
                } else {
                    String baseUri = uriInfo.getBaseUri().toString();
                    // baseUri = http://localhost:4446/joc/api/
                    // Globals.servletContextContextPath = /joc
                    LOGGER.debug(String.format("servletContextContextPath=%s, baseUri=%s", servletContextContextPath, baseUri));
                    int indx = baseUri.indexOf(servletContextContextPath);
                    if (indx > -1) {
                        baseUri = baseUri.substring(0, indx + servletContextContextPath.length());
                    }
                    servletBaseUri = URI.create(baseUri + "/");
                }
                LOGGER.info("servletBaseUri=" + servletBaseUri);
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("can't evaluate the base url: %s", e.toString()), e);
        }
    }

    public static ConfigurationGlobals getConfigurationGlobals() {
        return configurationGlobals;
    }

    public synchronized static void setConfigurationGlobals(ConfigurationGlobals val) {
        configurationGlobals = val;
    }

    public static ConfigurationGlobalsJoc getConfigurationGlobalsJoc() {
        return configurationGlobals == null ? new ConfigurationGlobalsJoc() : (ConfigurationGlobalsJoc) configurationGlobals.getConfigurationSection(
                DefaultSections.joc);
    }

    public static ConfigurationGlobalsUser getConfigurationGlobalsUser() {
        return configurationGlobals == null ? new ConfigurationGlobalsUser() : (ConfigurationGlobalsUser) configurationGlobals
                .getConfigurationSection(DefaultSections.user);
    }

    public static ConfigurationGlobalsDailyPlan getConfigurationGlobalsDailyPlan() {
        return configurationGlobals == null ? new ConfigurationGlobalsDailyPlan() : (ConfigurationGlobalsDailyPlan) configurationGlobals
                .getConfigurationSection(DefaultSections.dailyplan);
    }

    public static ConfigurationGlobalsGit getConfigurationGlobalsGit() {
        return configurationGlobals == null ? new ConfigurationGlobalsGit() : (ConfigurationGlobalsGit) configurationGlobals.getConfigurationSection(
                DefaultSections.git);
    }

    public static ConfigurationGlobalsLogNotification getConfigurationGlobalsLogNotification() {
        return configurationGlobals == null ? new ConfigurationGlobalsLogNotification() : (ConfigurationGlobalsLogNotification) configurationGlobals
                .getConfigurationSection(DefaultSections.lognotification);
    }

    public static ConfigurationGlobalsIdentityService getConfigurationGlobalsIdentityService() {
        return null;
 //       return configurationGlobals == null ? new ConfigurationGlobalsIdentityService() : (ConfigurationGlobalsIdentityService) configurationGlobals
 //               .getConfigurationSection(DefaultSections.identityService);
    }

    // -1: current version is older, 0: current version is equal, 1: current version
    // is younger
    public static int curVersionCompareWith(String version) {
        try {
            String currentVersion = curVersion.getVersion();
            if (currentVersion == null || currentVersion.isEmpty()) {
                currentVersion = "1.0.0-SNAPSHOT";
            }
            String[] curVersionsComplete = (currentVersion + "- ").split("-");
            String[] curVersions = curVersionsComplete[0].split("\\.");
            String[] versionsComplete = (version + "- ").split("-");
            String[] versions = versionsComplete[0].split("\\.");
            String curVersionsStr = curVersions[0];
            String versionsStr = versions[0];
            for (int i = 1; i < curVersions.length; i++) {
                curVersionsStr += curVersions[i].replaceFirst("^(\\d)$", "0$1");
            }
            curVersionsStr += curVersionsComplete[1].replaceFirst("SNAPSHOT", "00").replaceFirst("RC(\\d)", "$1").replaceFirst(" ", "99")
                    .replaceFirst("^(\\d)$", "0$1");
            for (int i = 1; i < versions.length; i++) {
                versionsStr += versions[i].replaceFirst("^(\\d)$", "0$1");
            }
            versionsStr += versionsComplete[1].replaceFirst("SNAPSHOT", "00").replaceFirst("RC(\\d)", "$1").replaceFirst(" ", "99").replaceFirst(
                    "^(\\d)$", "0$1");
            return Integer.valueOf(curVersionsStr).compareTo(Integer.valueOf(versionsStr));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public static void setSystemProperties() {
        System.setProperty("log4j2.contextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
        // Use less CPU when idling than default "Timeout":
        System.setProperty("log4j2.asyncLoggerWaitStrategy", "Block");
        // Because AsyncLoggerContextSelector flushes:
        System.setProperty("js7.log4j.immediateFlush", "false");
    }

}
