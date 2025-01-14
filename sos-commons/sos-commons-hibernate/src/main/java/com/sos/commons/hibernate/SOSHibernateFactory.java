package com.sos.commons.hibernate;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate.Dbms;
import com.sos.commons.hibernate.configuration.SOSHibernateConfigurationResolver;
import com.sos.commons.hibernate.configuration.resolver.SOSHibernateFinalPropertiesResolver;
import com.sos.commons.hibernate.configuration.resolver.dialect.SOSHibernateDefaultDialectResolver;
import com.sos.commons.hibernate.exception.SOSHibernateConfigurationException;
import com.sos.commons.hibernate.exception.SOSHibernateFactoryBuildException;
import com.sos.commons.hibernate.exception.SOSHibernateOpenSessionException;
import com.sos.commons.hibernate.function.date.SOSHibernateSecondsDiff;
import com.sos.commons.hibernate.function.json.SOSHibernateJsonExists;
import com.sos.commons.hibernate.function.json.SOSHibernateJsonValue;
import com.sos.commons.hibernate.function.regex.SOSHibernateRegexp;
import com.sos.commons.util.SOSClassList;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSString;

import jakarta.persistence.PersistenceException;

public class SOSHibernateFactory implements Serializable {

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
    private boolean forceReadDatabaseMetaData;

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
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_CURRENT_SESSION_CONTEXT_CLASS, "jta");
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_PERSISTENCE_VALIDATION_MODE, "none");
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_ID_STRUCTURE_NAMING_STRATEGY, "legacy");

        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_AUTO_COMMIT, "false");
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_JPA_ID_GENERATOR_GLOBAL_SCOPE_COMPLIANCE, "false");
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_USE_SCROLLABLE_RESULTSET, "true");

        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_ALLOW_METADATA_ON_BOOT, "true");
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_DIALECT_RESOLVERS, SOSHibernateDefaultDialectResolver.class.getName());

        configurationProperties = new Properties();
        configurationResolver = new SOSHibernateConfigurationResolver();
    }

    public void build() throws SOSHibernateFactoryBuildException {
        build(false);
    }

    public void build(boolean forceReadDatabaseMetaData) throws SOSHibernateFactoryBuildException {
        try {
            this.forceReadDatabaseMetaData = forceReadDatabaseMetaData;

            createConfiguration();
            adjustConfiguration(configuration);
            showConfigurationProperties();
            configurationResolver = null;
            buildSessionFactory();
            if (LOGGER.isDebugEnabled()) {
                String method = SOSHibernate.getMethodName(logIdentifier, "build");
                int isolationLevel = getTransactionIsolation();
                LOGGER.debug(String.format("%s[%s][%s][%s]autoCommit=%s,transactionIsolation=%s", method, dbms, dialect, databaseMetaData,
                        getAutoCommit(), getTransactionIsolationName(isolationLevel)));
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

            mapDeprecatedConfigurationProperties();
        } catch (MalformedURLException e) {
            throw new SOSHibernateConfigurationException(String.format("exception on get configFile %s as url", configFile), e);
        } catch (PersistenceException e) {
            throw new SOSHibernateConfigurationException(e);
        }
    }

    private void addSQLFunctions() {
        if (configuration != null) {
            configuration.addSqlFunction(SOSHibernateJsonValue.NAME, new SOSHibernateJsonValue(this));
            configuration.addSqlFunction(SOSHibernateJsonExists.NAME, new SOSHibernateJsonExists(this));
            configuration.addSqlFunction(SOSHibernateRegexp.NAME, new SOSHibernateRegexp(this));
            configuration.addSqlFunction(SOSHibernateSecondsDiff.NAME, new SOSHibernateSecondsDiff(this));
        }
    }

    private void mapDeprecatedConfigurationProperties() {
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
        dbms = configurationResolver.getDbms();
    }

    private void buildSessionFactory() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(SOSHibernate.getMethodName(logIdentifier, "buildSessionFactory"));
        }
        sessionFactory = configuration.buildSessionFactory();

        JdbcServices jdbcServices = ((SessionFactoryImplementor) sessionFactory).getJdbcServices();
        dialect = jdbcServices.getDialect();
        dbms = SOSHibernateFinalPropertiesResolver.finalCheckAndSetDbms(sessionFactory, dialect, dbms);
        setDatabaseMetaData();
    }

    /** If hibernate.boot.allow_jdbc_metadata_access=true<br/>
     * - Database MetaData is set by using SOSHibernateDefaultDialectResolver without an additional session/connection<br/>
     * If hibernate.boot.allow_jdbc_metadata_access=false<br/>
     * - an additional session/connection is used<br/>
     * Notes:<br/>
     * - For Oracle, always read the Database MetaData (if null) as JSON processing differs between versions<br/>
     * -- see SOSHibernateDatabaseMetaData.supportJsonReturningClob<br/>
     * - Reread Database MetaData if DBMS do not match or Database MetaData was not set due to errors<br/>
     * -- SOSHibernateDatabaseMetaData was set early as the Factory's final DBMS<br/>
     * --- see SOSHibernateFinalPropertiesResolver.finalCheckAndSetDbms<br/>
     */
    private void setDatabaseMetaData() {
        databaseMetaData = SOSHibernateFinalPropertiesResolver.retrieveDatabaseMetaData(sessionFactory);
        if (databaseMetaData == null || !databaseMetaData.metaDataAvailable() || !databaseMetaData.getDbms().equals(dbms)) {
            if (Dbms.ORACLE.equals(dbms) || forceReadDatabaseMetaData) {
                try {
                    try (SOSHibernateSession session = openStatelessSession("setDatabaseMetaData")) {
                        session.doWork(connection -> {
                            databaseMetaData = new SOSHibernateDatabaseMetaData(dbms, connection.getMetaData());
                        });
                    }
                } catch (Throwable e) {
                    LOGGER.warn(String.format("[setDatabaseMetaData][%s]%s", dbms, e.toString()), e);
                }
            }
        }
        if (databaseMetaData == null) {
            databaseMetaData = new SOSHibernateDatabaseMetaData(dbms);
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

    public boolean dbmsIsOracle() {
        return Dbms.ORACLE.equals(dbms);
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