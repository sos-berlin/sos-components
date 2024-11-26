package com.sos.commons.hibernate;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.configuration.SOSHibernateConfigurationResolver;
import com.sos.commons.hibernate.exception.SOSHibernateConfigurationException;
import com.sos.commons.hibernate.exception.SOSHibernateFactoryBuildException;
import com.sos.commons.hibernate.exception.SOSHibernateOpenSessionException;
import com.sos.commons.hibernate.function.date.SOSHibernateSecondsDiff;
import com.sos.commons.hibernate.function.json.SOSHibernateJsonValue;
import com.sos.commons.hibernate.function.regex.SOSHibernateRegexp;
import com.sos.commons.hibernate.type.SOSHibernateJsonType;
import com.sos.commons.util.SOSClassList;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSReflection;
import com.sos.commons.util.SOSString;

import jakarta.persistence.PersistenceException;

public class SOSHibernateFactory implements Serializable {

    public enum Dbms {
        H2, MSSQL, MYSQL, ORACLE, PGSQL, UNKNOWN
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateFactory.class);
    private static final Logger CONNECTION_POOL_LOGGER = LoggerFactory.getLogger("ConnectionPool");
    private static final long serialVersionUID = 1L;

    private SOSHibernateDatabaseMetaData databaseMetaData;
    private SOSClassList classMapping;
    private Configuration configuration;
    private SessionFactory sessionFactory;
    private Dialect dialect;
    /** SOSHibernateConfigurationResolver is set to NULL after the configuration is processed */
    private SOSHibernateConfigurationResolver configurationResolver;
    private Properties configurationProperties;
    private Properties defaultConfigurationProperties;
    private Dbms dbms = Dbms.UNKNOWN;
    private Optional<Path> configFile = Optional.empty();

    private String identifier;
    private String logIdentifier;
    private String currentTimestampSelectString;
    private String currentUTCTimestampSelectString;
    private boolean useDefaultConfigurationProperties = true;
    private boolean readDatabaseMetaData;

    public SOSHibernateFactory() {
        this(asPath(null));
    }

    public SOSHibernateFactory(String hibernateConfigFile) {
        this(asPath(hibernateConfigFile));
    }

    public SOSHibernateFactory(Path hibernateConfigFile) {
        setIdentifier(null);
        setConfigFile(hibernateConfigFile);
        initClassMapping();
        initConfiguration();
    }

    private void initConfiguration() {
        defaultConfigurationProperties = new Properties();
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_TRANSACTION_ISOLATION, String.valueOf(
                Connection.TRANSACTION_READ_COMMITTED));
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_AUTO_COMMIT, "false");
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_ALLOW_METADATA_ON_BOOT, "true");
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_USE_SCROLLABLE_RESULTSET, "true");
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_CURRENT_SESSION_CONTEXT_CLASS, "jta");
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_PERSISTENCE_VALIDATION_MODE, "none");
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_ID_STRUCTURE_NAMING_STRATEGY, "legacy");
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_JPA_ID_GENERATOR_GLOBAL_SCOPE_COMPLIANCE, "false");
        // defaultConfigurationProperties.put("hibernate.jdbc.use_get_generated_keys", "false");

        configurationProperties = new Properties();
        configurationResolver = new SOSHibernateConfigurationResolver();
    }

    public void build() throws SOSHibernateFactoryBuildException {
        build(false);
    }

    public void build(boolean readDatabaseMetaData) throws SOSHibernateFactoryBuildException {
        try {
            // see SOSHibernateSession.onOpenSession
            this.readDatabaseMetaData = readDatabaseMetaData;

            createConfiguration();
            adjustConfiguration(configuration);
            showConfigurationProperties();
            configurationResolver = null;
            adjustAnnotations(dbms);
            buildSessionFactory();
            if (LOGGER.isDebugEnabled()) {
                String method = SOSHibernate.getMethodName(logIdentifier, "build");
                int isolationLevel = getTransactionIsolation();
                LOGGER.debug(String.format("%s autoCommit=%s, transactionIsolation=%s", method, getAutoCommit(), getTransactionIsolationName(
                        isolationLevel)));
            }
        } catch (SOSHibernateConfigurationException ex) {
            throw new SOSHibernateFactoryBuildException(ex, configFile);
        } catch (PersistenceException ex) {
            throw new SOSHibernateFactoryBuildException(ex);
        }
    }

    public void close(SOSHibernateSession session) {
        if (session != null) {
            session.close();
        }
        close();
    }

    public void close() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(SOSHibernate.getMethodName(logIdentifier, "close"));
        }
        try {
            if (sessionFactory != null && !sessionFactory.isClosed()) {
                sessionFactory.close();
            }
        } catch (Throwable e) {
            LOGGER.warn(e.toString(), e);
        }
        sessionFactory = null;
    }

    public SOSHibernateSession openSession() throws SOSHibernateOpenSessionException {
        return openSession(identifier);
    }

    public SOSHibernateSession openSession(String identifier) throws SOSHibernateOpenSessionException {
        SOSHibernateSession session = new SOSHibernateSession(this);
        session.setIdentifier(identifier);
        session.openSession();
        return session;
    }

    public SOSHibernateSession openStatelessSession() throws SOSHibernateOpenSessionException {
        return openStatelessSession(identifier);
    }

    public SOSHibernateSession openStatelessSession(String identifier) throws SOSHibernateOpenSessionException {
        SOSHibernateSession session = new SOSHibernateSession(this);
        session.setIsStatelessSession(true);
        session.setIdentifier(identifier);
        session.openSession();
        if (CONNECTION_POOL_LOGGER.isDebugEnabled()) {
            CONNECTION_POOL_LOGGER.debug("--------> GET CONNECTION: " + session.getIdentifier() + " (" + SOSClassUtil.getMethodName(3)
                    + ") --------");
        }
        return session;
    }

    public SOSHibernateSession getCurrentSession() throws SOSHibernateOpenSessionException {
        return getCurrentSession(identifier);
    }

    public SOSHibernateSession getCurrentSession(String identifier) throws SOSHibernateOpenSessionException {
        SOSHibernateSession session = new SOSHibernateSession(this);
        session.setIsGetCurrentSession(true);
        session.setIdentifier(identifier);
        session.openSession();
        return session;
    }

    public boolean getAutoCommit() throws SOSHibernateConfigurationException {
        if (configuration == null) {
            throw new SOSHibernateConfigurationException("configuration is NULL");
        }
        String p = configuration.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_AUTO_COMMIT);
        if (SOSString.isEmpty(p)) {
            throw new SOSHibernateConfigurationException(String.format("\"%s\" property is not configured ",
                    SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_AUTO_COMMIT));
        }
        return Boolean.parseBoolean(p);
    }

    /** TODO check: dialect.getSequenceSupport().getSequencePreviousValString(sequenceName);
     * 
     * Hibernate Dialect does not provide the functions to identify the last inserted sequence value.
     * 
     * only for the next value:
     * 
     * e.g. dialiect.getSelectSequenceNextValString(sequenceName),
     * 
     * dialect.getSequenceNextValString(sequenceName) */
    public String getSequenceLastValString(String sequenceName) {
        switch (dbms) {
        case MSSQL:
            return "SELECT @@IDENTITY";
        case MYSQL:
            return "SELECT LAST_INSERT_ID();";
        case ORACLE:
            return "SELECT " + sequenceName + ".currval FROM DUAL";
        case PGSQL:
            return "SELECT currval('" + sequenceName + "');";
        case H2:
            return "SELECT LAST_INSERT_ID();";
        case UNKNOWN:
            break;
        }

        return null;
    }

    public String getCurrentTimestampSelectString() {
        if (currentTimestampSelectString == null) {
            switch (dbms) {
            case H2:
                // extra because of org.hibernate.MappingException: No Dialect mapping for JDBC type: 2014 [call current_timestamp()]
                currentTimestampSelectString = "select now()";
                break;
            case ORACLE:
                // extra because of Oracle10gDialect.getCurrentTimestampSelectString "select systimestamp from dual" statement
                // and MappingException: "No Dialect mapping for JDBC type: -101"
                currentTimestampSelectString = "select sysdate from dual";
                break;
            default:
                currentTimestampSelectString = dialect.getCurrentTimestampSelectString();
                break;
            }
        }
        return currentTimestampSelectString;
    }

    public String getCurrentUTCTimestampSelectString() {
        if (currentUTCTimestampSelectString == null) {
            switch (dbms) {
            case H2:
                currentUTCTimestampSelectString = "select now()";// TODO UTC
                break;
            case MYSQL:
                currentUTCTimestampSelectString = "select utc_timestamp()";
                break;
            case ORACLE:
                currentUTCTimestampSelectString = "select cast(sys_extract_utc(systimestamp) as date) from dual";
                break;
            case MSSQL:
                currentUTCTimestampSelectString = "select getutcdate()";
                break;
            case PGSQL:
                currentUTCTimestampSelectString = "select timezone('UTC', now())";
                break;
            default:
                currentUTCTimestampSelectString = getCurrentTimestampSelectString();
                break;
            }
        }
        return currentUTCTimestampSelectString;
    }

    public int getTransactionIsolation() throws SOSHibernateConfigurationException {
        if (configuration == null) {
            throw new SOSHibernateConfigurationException("configuration is NULL");
        }
        String p = configuration.getProperty(SOSHibernate.HIBERNATE_PROPERTY_TRANSACTION_ISOLATION);
        if (SOSString.isEmpty(p)) {
            throw new SOSHibernateConfigurationException(String.format("\"%s\" property is not configured ",
                    SOSHibernate.HIBERNATE_PROPERTY_TRANSACTION_ISOLATION));
        }
        return Integer.parseInt(p);
    }

    public boolean isUseDefaultConfigurationProperties() {
        return useDefaultConfigurationProperties;
    }

    public String quoteColumn(String columnName) {
        return SOSHibernate.quoteColumn(dialect, columnName);
    }

    public void setAutoCommit(boolean commit) {
        configurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_AUTO_COMMIT, String.valueOf(commit));
    }

    public void setConfigFile(Path hibernateConfigFile) {
        if (hibernateConfigFile != null) {
            configFile = Optional.of(hibernateConfigFile);
        }
    }

    public void setConfigFile(String hibernateConfigFile) {
        setConfigFile(asPath(hibernateConfigFile));
    }

    private static Path asPath(String hibernateConfigFile) {
        return hibernateConfigFile == null ? null : Paths.get(hibernateConfigFile);
    }

    public void setConfigurationProperties(Properties properties) {
        if (configurationProperties.isEmpty()) {
            configurationProperties = properties;
        } else {
            if (properties != null) {
                for (Map.Entry<?, ?> entry : properties.entrySet()) {
                    String key = (String) entry.getKey();
                    String value = (String) entry.getValue();
                    configurationProperties.setProperty(key, value);
                }
            }
        }
    }

    // TODO method name - if configuration.configure is not called ...
    private void configure() throws SOSHibernateConfigurationException {
        try {
            addSQLFunctions();

            if (configFile.isPresent()) {
                Path cf = configFile.get();
                if (!Files.exists(cf)) {
                    throw new SOSHibernateConfigurationException(String.format("hibernate config file not found: %s", cf.toString()));
                }
                // TODO check if really needed
                configuration.configure(cf.toUri().toURL());
            } else {
                // 6.5.x throws an exception - can't locate hibernate.cfg.xml
                // configuration.configure();
            }
            if (LOGGER.isDebugEnabled()) {
                String method = SOSHibernate.getMethodName(logIdentifier, "configure");
                if (configFile.isPresent()) {
                    LOGGER.debug(String.format("%s %s", method, configFile.get().toAbsolutePath().toString()));
                } else {
                    LOGGER.debug(String.format("%s configure connection without the hibernate file", method));
                }
            }
            
            mapConfigurationProperties();
            setDbms(configuration.getProperties());
            updateConnectionUrlForMysqlWithMariaDriver(configuration.getProperties());
            databaseMetaData = new SOSHibernateDatabaseMetaData(dbms);
            mapDialect(dbms);
        } catch (MalformedURLException e) {
            throw new SOSHibernateConfigurationException(String.format("exception on get configFile %s as url", configFile), e);
        } catch (PersistenceException e) {
            throw new SOSHibernateConfigurationException(e);
        }
    }

    private void addSQLFunctions() {
        if (configuration != null) {
            configuration.addSqlFunction(SOSHibernateJsonValue.NAME, new SOSHibernateJsonValue(this));
            configuration.addSqlFunction(SOSHibernateRegexp.NAME, new SOSHibernateRegexp(this));
            configuration.addSqlFunction(SOSHibernateSecondsDiff.NAME, new SOSHibernateSecondsDiff(this));
        }
    }

    private void mapConfigurationProperties() {
        // map deprecated
        mapDeprecatedConfigurationProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_DRIVERCLASS_DEPRECATED,
                SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_DRIVERCLASS);
        mapDeprecatedConfigurationProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL_DEPRECATED, SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL);
        mapDeprecatedConfigurationProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_USERNAME_DEPRECATED,
                SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_USERNAME);
        mapDeprecatedConfigurationProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_PASSWORD_DEPRECATED,
                SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_PASSWORD);
    }

    private void mapDeprecatedConfigurationProperty(String deprecatedName, String newName) {
        String val = configuration.getProperties().getProperty(deprecatedName);
        if (val != null) {
            configuration.getProperties().setProperty(newName, val);
            configuration.getProperties().remove(deprecatedName);
        }
    }

    private void mapDialect(Dbms dbms) {
        // with Hibernate 6: Version-specific and spatial-specific dialects are deprecated
        // simply MySQL8Dialect, SQLServer2012Dialect, SQLServer2016Dialect are still implemented
        // so, old dialects are mapped: Example: org.hibernate.dialect.MySQLInnoDBDialect -> org.hibernate.dialect.MySQLDialect
        if (configuration != null) {
            String dialect = configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT);
            if (dialect != null) {
                switch (dbms) {
                case MYSQL:
                    if(configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT).equals("org.hibernate.dialect.MySQLInnoDBDialect")) {
                        configuration.getProperties().setProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT, SOSHibernate.DEFAULT_DIALECT_MYSQL);
                    }
                    if (configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT).equals("org.hibernate.dialect.MariaDBDialect")) {
                        if(configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL).contains("jdbc:mysql")) {
                            configuration.getProperties().setProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT, SOSHibernate.DEFAULT_DIALECT_MYSQL);
                        } else {
                            if(configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_ALLOW_METADATA_ON_BOOT).equals("true")) {
                                removeProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT);
                            }
                        }
                    }
//                    if (!dialect.equals("org.hibernate.dialect.MySQL8Dialect")) {
//                        configuration.getProperties().setProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT, SOSHibernate.DEFAULT_DIALECT_MYSQL);
//                    }
                    break;
                case ORACLE:
                    if(configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT).equals(SOSHibernate.DEFAULT_DIALECT_ORACLE)) {
                        removeProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT);
                    }
//                    configuration.getProperties().setProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT, SOSHibernate.DEFAULT_DIALECT_ORACLE);
                    break;
                case PGSQL:
                    if(configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT).equals(SOSHibernate.DEFAULT_DIALECT_PGSQL)) {
                        removeProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT);
                    }
//                    configuration.getProperties().setProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT, SOSHibernate.DEFAULT_DIALECT_PGSQL);
                    break;
                case MSSQL:
                    if (!Arrays.asList("org.hibernate.dialect.SQLServer2012Dialect", "org.hibernate.dialect.SQLServer2016Dialect",
                            "org.hibernate.dialect.SQLServerDialect").contains(configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT))) {
                        removeProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT);
                    }
//                    if (!Arrays.asList("org.hibernate.dialect.SQLServer2012Dialect", "org.hibernate.dialect.SQLServer2016Dialect").contains(
//                            dialect)) {
//                        configuration.getProperties().setProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT, SOSHibernate.DEFAULT_DIALECT_MSSQL);
//                    }
                    break;
                case H2:
                    if(configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT).equals(SOSHibernate.DEFAULT_DIALECT_H2)) {
                        removeProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT);
                    }
//                    configuration.getProperties().setProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT, SOSHibernate.DEFAULT_DIALECT_H2);
                    break;
                default:
                    break;
                }
            }
        }
    }

    public static Dbms getDbms(Path configFile) throws SOSHibernateConfigurationException {
        String dialect = null;
        try {
            Configuration conf = new Configuration();
            conf.configure(configFile.toUri().toURL());
            dialect = conf.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT);
        } catch (MalformedURLException e) {
            throw new SOSHibernateConfigurationException(String.format("exception on get configFile %s as url", configFile), e);
        } catch (PersistenceException e) {
            throw new SOSHibernateConfigurationException(e);
        }
        return getDbms(dialect);
    }

    public static Dbms getDbms(Dialect dialect) {
        return getDbms(dialect == null ? null : dialect.getClass().getSimpleName());
    }

    public static Dbms getDbms(String dialect) {
        Dbms dbms = Dbms.UNKNOWN;
        if (dialect != null) {
            String dialectClassName = dialect.toLowerCase();
            if (dialectClassName.contains("h2")) {
                dbms = Dbms.H2;
            } else if (dialectClassName.contains("sqlserver")) {
                dbms = Dbms.MSSQL;
            } else if (dialectClassName.contains("mysql") || dialectClassName.contains("mariadb")) {
                dbms = Dbms.MYSQL;
            } else if (dialectClassName.contains("oracle")) {
                dbms = Dbms.ORACLE;
            } else if (dialectClassName.contains("postgre")) {
                dbms = Dbms.PGSQL;
            }
        }
        return dbms;
    }

    private void initClassMapping() {
        classMapping = new SOSClassList();
    }

    private void createConfiguration() throws SOSHibernateConfigurationException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(SOSHibernate.getMethodName(logIdentifier, "createConfiguration"));
        }
        configuration = new Configuration();
        setConfigurationClassMapping();
        setDefaultConfigurationProperties();
        configure();
        setConfigurationProperties();
        configuration = configurationResolver.resolve(configuration);
    }

    private void buildSessionFactory() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(SOSHibernate.getMethodName(logIdentifier, "buildSessionFactory"));
        }
        sessionFactory = configuration.buildSessionFactory();
        dialect = ((SessionFactoryImplementor) sessionFactory).getJdbcServices().getDialect();
        if (Dbms.UNKNOWN.equals(dbms)) {
            setDbms(configuration.getProperties());
        }
    }

    private void showConfigurationProperties() {
        String property = configuration.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_SHOW_CONFIGURATION_PROPERTIES);
        if (property != null && property.toLowerCase().equals("true")) {
            String method = SOSHibernate.getMethodName(logIdentifier, "showConfigurationProperties");
            for (Map.Entry<?, ?> entry : configuration.getProperties().entrySet()) {
                String key = (String) entry.getKey();
                String value = null;
                if (configurationResolver.propertyValueChanged(key)) {
                    value = configurationResolver.getOldValueOrNewPropertyValue(property);
                } else if (key.toLowerCase().endsWith("password")) {
                    value = "***";
                } else {
                    value = (String) entry.getValue();
                }
                LOGGER.info(String.format("%s %s=%s", method, key, value));
            }
        }
    }

    private void setConfigurationClassMapping() {
        if (classMapping != null) {
            boolean isTraceEnabled = LOGGER.isTraceEnabled();
            String method = isTraceEnabled ? SOSHibernate.getMethodName(logIdentifier, "setConfigurationClassMapping") : "";
            for (Class<?> c : classMapping.getClasses()) {
                if (isTraceEnabled) {
                    LOGGER.trace(String.format("%s %s", method, c.getCanonicalName()));
                }
                configuration.addAnnotatedClass(c);
            }
        }
    }

    private void setConfigurationProperties() {
        if (configurationProperties != null) {
            boolean isTraceEnabled = LOGGER.isTraceEnabled();
            String method = isTraceEnabled ? SOSHibernate.getMethodName(logIdentifier, "setConfigurationProperties") : "";
            for (Map.Entry<?, ?> entry : configurationProperties.entrySet()) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                configuration.setProperty(key, value);
                if (isTraceEnabled) {
                    LOGGER.trace(String.format("%s %s=%s", method, key, value));
                }
            }
        }
    }

    private void setDbms(String dialect) {
        dbms = getDbms(dialect);
    }
    
    private void setDbms(Properties properties) {
        dbms = Dbms.UNKNOWN;
        if(properties.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL) != null 
                && !properties.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL).isEmpty()) {
            if(properties.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL).contains("jdbc:h2")) {
                dbms = Dbms.H2;
            } else if (properties.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL).contains("jdbc:sqlserver")) {
                dbms = Dbms.MSSQL;
            } else if (properties.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL).contains("jdbc:oracle")) {
                dbms = Dbms.ORACLE;
            } else if (properties.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL).contains("jdbc:postgresql")) {
                dbms = Dbms.PGSQL;
            } else if (properties.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL).contains("jdbc:mysql") 
                    || properties.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL).contains("jdbc:mariadb")) {
                dbms = Dbms.MYSQL;
            }
        } else if (properties.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_DBMS_PRODUCT) != null 
                && !properties.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_DBMS_PRODUCT).isEmpty()) {
            if(properties.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_DBMS_PRODUCT).contains("h2")) {
                dbms = Dbms.H2;
            } else if(properties.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_DBMS_PRODUCT).contains("mssql")) {
                dbms = Dbms.MSSQL;
            } else if(properties.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_DBMS_PRODUCT).contains("oracle")) {
                dbms = Dbms.ORACLE;
            } else if(properties.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_DBMS_PRODUCT).contains("pgsql")) {
                dbms = Dbms.PGSQL;
            } else if(properties.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_DBMS_PRODUCT).contains("mysql")
                    || properties.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_DBMS_PRODUCT).contains("mariadb")) {
                dbms = Dbms.MYSQL;
            }
        } else if (properties.getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT) != null 
                && !properties.getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT).isEmpty()) {
            setDbms(properties.getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT));
        }
    }
    
    private void updateConnectionUrlForMysqlWithMariaDriver(Properties properties) {
        String driver = properties.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_DRIVERCLASS);
        String connectionUrl = properties.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL);
        if (driver != null && !driver.isEmpty() && connectionUrl != null && !connectionUrl.isEmpty()) {
            if(driver.toLowerCase().contains("mariadb") && connectionUrl.contains("jdbc:mysql")) {
                if(!connectionUrl.contains("permitMysqlScheme")) {
                    if(connectionUrl.contains("?")) {
                        connectionUrl = connectionUrl + "&amp;permitMysqlScheme";
                    } else {
                        connectionUrl = connectionUrl + "?permitMysqlScheme";
                    }
                }
                properties.setProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL, connectionUrl);
                properties.setProperty("hibernate." + SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL, connectionUrl);
            }
        }
        
    }

    private void setDefaultConfigurationProperties() {
        if (useDefaultConfigurationProperties && defaultConfigurationProperties != null) {
            boolean isTraceEnabled = LOGGER.isTraceEnabled();
            String method = isTraceEnabled ? SOSHibernate.getMethodName(logIdentifier, "setDefaultConfigurationProperties") : "";
            for (Map.Entry<?, ?> entry : defaultConfigurationProperties.entrySet()) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                if (isTraceEnabled) {
                    LOGGER.trace(String.format("%s %s=%s", method, key, value));
                }
                configuration.setProperty(key, value);
            }
        }
    }

    private void adjustAnnotations(Dbms dbms) {
        if (classMapping != null) {
            if (Dbms.H2.equals(dbms)) {
                changeJsonAnnotations4H2();
            }
        }
    }

    // TODO to remove ...
    private void changeJsonAnnotations4H2() {
        for (Class<?> c : classMapping.getClasses()) {
            List<Field> fields = Arrays.stream(c.getDeclaredFields()).filter(m -> m.isAnnotationPresent(org.hibernate.annotations.Type.class) && m
                    .getAnnotation(org.hibernate.annotations.Type.class).value().equals(SOSHibernateJsonType.class)).collect(Collectors.toList());
            if (fields != null && fields.size() > 0) {
                for (Field field : fields) {
                    field.setAccessible(true);
                    org.hibernate.annotations.ColumnTransformer ct = field.getAnnotation(org.hibernate.annotations.ColumnTransformer.class);
                    if (ct != null) {
                        try {
                            if (!ct.write().equals(SOSHibernateJsonType.COLUMN_TRANSFORMER_WRITE_H2)) {
                                SOSReflection.changeAnnotationValue(ct, "write", SOSHibernateJsonType.COLUMN_TRANSFORMER_WRITE_H2);
                            }
                        } catch (Throwable e) {
                            LOGGER.warn(String.format("[%s.%s]%s", c.getSimpleName(), field.getName(), e.toString()), e);
                        }
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug(String.format("[ColumnTransformer][%s.%s]%s", c.getSimpleName(), field.getName(), ct.write()));
                        }
                    }
                }
            }
        }
    }

    protected boolean readDatabaseMetaData() {
        return readDatabaseMetaData;
    }

    public static String getTransactionIsolationName(int isolationLevel) throws SOSHibernateConfigurationException {
        switch (isolationLevel) {
        case Connection.TRANSACTION_NONE:
            return "TRANSACTION_NONE";
        case Connection.TRANSACTION_READ_UNCOMMITTED:
            return "TRANSACTION_READ_UNCOMMITTED";
        case Connection.TRANSACTION_READ_COMMITTED:
            return "TRANSACTION_READ_COMMITTED";
        case Connection.TRANSACTION_REPEATABLE_READ:
            return "TRANSACTION_REPEATABLE_READ";
        case Connection.TRANSACTION_SERIALIZABLE:
            return "TRANSACTION_SERIALIZABLE";
        default:
            throw new SOSHibernateConfigurationException(String.format("invalid transaction isolation level=%s", isolationLevel));
        }
    }
    
    private void removeProperty(String key) {
        configuration.getProperties().remove(key);
        configuration.getStandardServiceRegistryBuilder().getSettings().remove(key);
    }

    public void addClassMapping(Class<?> c) {
        classMapping.add(c);
    }

    public void addClassMapping(SOSClassList list) {
        classMapping.merge(list.getClasses());
    }

    // to override
    public void adjustConfiguration(Configuration config) {

    }

    public boolean dbmsIsPostgres() {
        return Dbms.PGSQL.equals(dbms);
    }

    public boolean dbmsIsH2() {
        return Dbms.H2.equals(dbms);
    }

    public void setIdentifier(String val) {
        identifier = val;
        logIdentifier = SOSHibernate.getLogIdentifier(identifier);
    }

    public void setTransactionIsolation(int level) {
        configurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_TRANSACTION_ISOLATION, String.valueOf(level));
    }

    public void setUseDefaultConfigurationProperties(boolean val) {
        useDefaultConfigurationProperties = val;
    }

    public SOSHibernateDatabaseMetaData getDatabaseMetaData() {
        return databaseMetaData;
    }

    public SOSHibernateConfigurationResolver getConfigurationResolver() {
        return configurationResolver;
    }

    public SOSClassList getClassMapping() {
        return classMapping;
    }

    public Optional<Path> getConfigFile() {
        return configFile;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Properties getConfigurationProperties() {
        return configurationProperties;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public Dbms getDbms() {
        return dbms;
    }

    public Properties getDefaultConfigurationProperties() {
        return defaultConfigurationProperties;
    }

    public Dialect getDialect() {
        return dialect;
    }

    public String getIdentifier() {
        return identifier;
    }

}