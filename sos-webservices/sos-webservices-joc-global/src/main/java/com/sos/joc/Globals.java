package com.sos.joc;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.json.Json;
import javax.ws.rs.core.UriInfo;

import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.auth.rest.SOSShiroCurrentUser;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateConfigurationException;
import com.sos.commons.hibernate.exception.SOSHibernateFactoryBuildException;
import com.sos.commons.hibernate.exception.SOSHibernateOpenSessionException;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.JocWebserviceDataContainer;
import com.sos.joc.classes.SSLContext;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.common.JocSecurityLevel;

public class Globals {

    private static final String SHIRO_INI_FILENAME = "shiro.ini";
    private static final String HIBERNATE_CONFIGURATION_FILE = "hibernate_configuration_file";
    private static final Logger LOGGER = LoggerFactory.getLogger(Globals.class);
    private static String trustStoreLocationDefault = "?????";
    private static String trustStorePasswordDefault = "?????";
    private static JocSecurityLevel jocSecurityLevel = null;
    public static final String SESSION_KEY_FOR_SEND_EVENTS_IMMEDIATLY = "send_events_immediatly";
    public static final String DEFAULT_SHIRO_INI_PATH = "classpath:shiro.ini";
    public static final String DEFAULT_SHIRO_INI_FILENAME = "shiro.ini";
    public static SOSHibernateFactory sosHibernateFactory;
    public static Map<String, SOSHibernateFactory> sosSchedulerHibernateFactories;
    public static JocCockpitProperties sosCockpitProperties;
    public static Map<String, DBItemInventoryJSInstance> urlFromJobSchedulerId = new HashMap<String, DBItemInventoryJSInstance>();
    public static Map<String, Boolean> jobSchedulerIsRunning = new HashMap<String, Boolean>();
    public static int httpConnectionTimeout = 2000;
    public static int httpSocketTimeout = 2000;
    public static boolean withHostnameVerification = false;
    public static boolean auditLogCommentsAreRequired = false;
    public static long maxSizeOfLogsToDisplay = 1024 * 1024 * 10L; // 10MB
    public static JocWebserviceDataContainer jocWebserviceDataContainer = JocWebserviceDataContainer.getInstance();
    public static JocCockpitProperties jocConfigurationProperties;
    public static IniSecurityManagerFactory factory = null;
    public static long timeoutToDeleteTempFiles = 1000 * 60 * 3L;
    public static TimeZone jocTimeZone = TimeZone.getDefault();
    public static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    public static String servletContextContextPath = null; // /joc
    public static Path servletContextRealPath = null;
    public static URI servletBaseUri = null;
    public static Map<String, String> schedulerVariables = null;

    public static SOSHibernateFactory getHibernateFactory() throws JocConfigurationException {
        if (sosHibernateFactory == null || sosHibernateFactory.getSessionFactory() == null) {
            try {
                Path confFile = getHibernateConfFile();
                sosHibernateFactory = new SOSHibernateFactory(confFile);
                sosHibernateFactory.addClassMapping(DBLayer.getJocClassMapping());
                sosHibernateFactory.setAutoCommit(true);
                sosHibernateFactory.build();
            } catch (SOSHibernateConfigurationException | SOSHibernateFactoryBuildException e) {
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

    public static IniSecurityManagerFactory getShiroIniSecurityManagerFactory() {
        String iniFile = getShiroIniInClassPath();
        if (factory == null) {
            factory = new IniSecurityManagerFactory(getIniFileForShiro(iniFile));
        } else {
            Ini oldShiroIni = factory.getIni();
            Ini currentShiroIni = Ini.fromResourcePath(getIniFileForShiro(iniFile));
            if (!oldShiroIni.equals(currentShiroIni)) {
                LOGGER.debug(getIniFileForShiro(iniFile) + " is changed");
                factory = new IniSecurityManagerFactory();
                factory.setIni(currentShiroIni);
            }
        }
        return factory;
    }

    public static Ini getIniFromSecurityManagerFactory() {
        if (factory == null) {
            String iniFile = getShiroIniInClassPath();
            factory = new IniSecurityManagerFactory(getIniFileForShiro(iniFile));
        }
        return factory.getIni();
    }

    public static String getIniFileForShiro(String iniFile) {
        return iniFile + ".active";
    }

    public static String getShiroIniInClassPath() {
        if (sosCockpitProperties != null) {
            Path p = sosCockpitProperties.resolvePath(SHIRO_INI_FILENAME);
            return "file:" + p.toString().replace('\\', '/');
        }
        return DEFAULT_SHIRO_INI_PATH;
    }

    public static Path getShiroIniFile() {
        if (sosCockpitProperties != null) {
            return sosCockpitProperties.resolvePath(getIniFileForShiro(SHIRO_INI_FILENAME));
        }
        return Paths.get(getIniFileForShiro(DEFAULT_SHIRO_INI_FILENAME));
    }

    public static void setProperties() {
        readVersion();
        setJobSchedulerConnectionTimeout();
        setJobSchedulerSocketTimeout();
        setHostnameVerification();
        setForceCommentsForAuditLog();
        setTrustStore();
        setTrustStoreType();
        setTrustStorePassword();
        setTimeoutForTempFiles();
        setConfigurationProperties();
        setClientCertificate();
    }

    private static void setClientCertificate() {
        SSLContext.loadKeyStore(sosCockpitProperties);
    }

    public static void beginTransaction(SOSHibernateSession connection) {
        try {
            if (connection != null) {
                connection.beginTransaction();
            }
        } catch (Exception e) {
        }
    }

    public static void rollback(SOSHibernateSession connection) {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (Exception e) {
        }
    }

    public static void commit(SOSHibernateSession connection) {
        try {
            if (connection != null) {
                connection.commit();
            }
        } catch (Exception e) {
        }
    }

    public static void forceClosingHttpClients(SOSShiroCurrentUser sosShiroCurrentUser, String accessToken) {
        if (sosShiroCurrentUser != null) {
            try {
                for (JOCJsonCommand command : sosShiroCurrentUser.getJocJsonCommands()) {
                    command.forcedClosingHttpClient();
                }
            } catch (Exception e) {
            }
        }
    }

    public static Path getHibernateConfFile() throws JocConfigurationException {
        String confFile = null;

        if (sosCockpitProperties == null) {
            throw new JocConfigurationException("JOC Properties are not initialized");
        }

        confFile = sosCockpitProperties.getProperty(HIBERNATE_CONFIGURATION_FILE, "hibernate.cfg.xml");
        if (confFile.trim().isEmpty()) {
            throw new JocConfigurationException(String.format("Property '%1$s' not found in %2$s", HIBERNATE_CONFIGURATION_FILE,
                    sosCockpitProperties.getPropertiesFile()));
        }

        confFile = confFile.trim();
        Path p = sosCockpitProperties.resolvePath(confFile);
        if (p != null) {
            if (!Files.exists(p) || Files.isDirectory(p)) {
                throw new JocConfigurationException(String.format("hibernate configuration (%1$s) is set but file (%2$s) not found.", confFile, p
                        .toString()));
            } else {
                confFile = p.toString().replace('\\', '/');
            }
        } else {
            throw new JocConfigurationException(String.format("hibernate configuration (%1$s) is set but file not found.", confFile));
        }
        return p;
    }

    private static void readVersion() {
        InputStream stream = null;
        String versionFile = "/version.json";
        LOGGER.info("Java version = " + System.getProperty("java.version"));
        try {
            stream = Globals.class.getClassLoader().getResourceAsStream(versionFile);
            if (stream != null) {
                LOGGER.info("JOC Cockpit version = " + Json.createReader(stream).readObject().getString("version", "unknown"));
            } else {
                LOGGER.warn(String.format("Version file %1$s not found in classpath", versionFile));
            }
        } catch (Exception e) {
            LOGGER.warn(String.format("Error while reading %1$s from classpath: ", versionFile), e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
            }
        }
    }

    private static void setJobSchedulerConnectionTimeout() {
        int defaultSeconds = 2;
        if (sosCockpitProperties != null) {
            int seconds = sosCockpitProperties.getProperty("jobscheduler_connection_timeout", defaultSeconds);
            httpConnectionTimeout = seconds * 1000;
            LOGGER.info("HTTP(S) connection timeout = " + seconds + "s");
        }
    }

    private static void setJobSchedulerSocketTimeout() {
        int defaultSeconds = 5;
        if (sosCockpitProperties != null) {
            int seconds = sosCockpitProperties.getProperty("jobscheduler_socket_timeout", defaultSeconds);
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

    private static void setTrustStore() {
        if ("?????".equals(trustStoreLocationDefault)) {
            trustStoreLocationDefault = System.getProperty("javax.net.ssl.trustStore");
        }
        if (sosCockpitProperties != null) {
            String truststore = sosCockpitProperties.getProperty("truststore_path");
            if (truststore != null && !truststore.trim().isEmpty()) {
                Path p = sosCockpitProperties.resolvePath(truststore.trim());
                if (p != null) {
                    if (!Files.exists(p)) {
                        LOGGER.error(String.format("truststore path (%1$s) is set but file (%2$s) not found.", truststore, p.toString()));
                    } else {
                        truststore = p.toString();
                        System.setProperty("javax.net.ssl.trustStore", truststore);
                    }
                }
            } else {
                if (trustStoreLocationDefault == null) {
                    System.clearProperty("javax.net.ssl.trustStore");
                } else {
                    System.setProperty("javax.net.ssl.trustStore", trustStoreLocationDefault);
                }
            }
        }
    }

    private static void setTrustStoreType() {
        if (sosCockpitProperties != null) {
            String truststoreType = sosCockpitProperties.getProperty("truststore_type", KeyStore.getDefaultType());
            System.setProperty("javax.net.ssl.trustStoreType", truststoreType);
        }
    }

    private static void setTrustStorePassword() {
        if ("?????".equals(trustStorePasswordDefault)) {
            trustStorePasswordDefault = System.getProperty("javax.net.ssl.trustStorePassword");
        }
        if (sosCockpitProperties != null) {
            String truststorePassw = sosCockpitProperties.getProperty("truststore_password");
            if (truststorePassw != null) {
                System.setProperty("javax.net.ssl.trustStorePassword", truststorePassw);
            } else {
                if (trustStorePasswordDefault == null) {
                    System.clearProperty("javax.net.ssl.trustStorePassword");
                } else {
                    System.setProperty("javax.net.ssl.trustStorePassword", trustStorePasswordDefault);
                }
            }
        }
    }

    private static void setConfigurationProperties() {
        if (sosCockpitProperties != null) {
            String confFile = sosCockpitProperties.getProperty("configuration_file", "");
            if (confFile != null && !confFile.trim().isEmpty()) {
                String defaultConfFile = "joc.configuration.properties";
                Path p = sosCockpitProperties.resolvePath(confFile.trim());
                if (p != null) {
                    if (!Files.exists(p)) {
                        if (!confFile.equals(defaultConfFile)) {
                            LOGGER.error(String.format("configuration file (%1$s) is set but file (%2$s) not found.", confFile, p.toString()));
                        }
                    } else {
                        jocConfigurationProperties = new JocCockpitProperties(p);
                    }
                }
            }
        }
    }

    private static void setForceCommentsForAuditLog() {
        boolean defaultForceCommentsForAuditLog = false;
        if (sosCockpitProperties != null) {
            auditLogCommentsAreRequired = sosCockpitProperties.getProperty("force_comments_for_audit_log", defaultForceCommentsForAuditLog);
            LOGGER.info("force comments for audit log = " + auditLogCommentsAreRequired);
        }
    }

    private static void setTimeoutForTempFiles() {
        long defaultTimeout = 1000 * 60 * 3L;
        if (sosCockpitProperties != null) {
            timeoutToDeleteTempFiles = sosCockpitProperties.getProperty("timeout_to_delete_temp_files", defaultTimeout);
        }
    }

    public static void forceRollback(Object object) {
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
        return ("/" + path.trim()).replaceAll("//+", "/").replaceFirst("/$", "");
    }

    public static JocSecurityLevel getJocSecurityLevel() {
        // the JocSecurity classes should have a method getJocSecurityLevel which is callable static during an abstract class
        if (Globals.jocSecurityLevel == null) {
            Globals.jocSecurityLevel = JocSecurityLevel.LOW; //default
            try {
                InputStream stream = Globals.class.getResourceAsStream("/joc-settings.properties");
                if (stream != null) {
                    Properties properties = new Properties();
                    properties.load(stream);
                    try {
                        Globals.jocSecurityLevel = JocSecurityLevel.fromValue(properties.getProperty("security_level", JocSecurityLevel.LOW.value())
                                .toUpperCase());
                    } catch (Exception e) {
                        //
                    }
                }
            } catch (Exception e) {
                LOGGER.error(String.format("Error while reading %1$s:", "joc-settings.properties"), e);
            }
        }
        return Globals.jocSecurityLevel;
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

    public static String getDefaultProfileUserAccount() {
        String defaultAccount = null;
        if (sosCockpitProperties != null) {
            defaultAccount = sosCockpitProperties.getProperty("default_profile_account", "root");
        }
        
        return defaultAccount;
    }
}
