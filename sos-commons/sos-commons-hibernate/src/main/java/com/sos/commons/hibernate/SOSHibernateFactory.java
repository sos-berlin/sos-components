package com.sos.commons.hibernate;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.persistence.PersistenceException;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.NumericBooleanType;
import org.hibernate.type.TimestampType;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.keepass.SOSKeePassResolver;
import com.sos.commons.hibernate.exception.SOSHibernateConfigurationException;
import com.sos.commons.hibernate.exception.SOSHibernateConvertException;
import com.sos.commons.hibernate.exception.SOSHibernateFactoryBuildException;
import com.sos.commons.hibernate.exception.SOSHibernateOpenSessionException;
import com.sos.commons.hibernate.function.date.SOSHibernateSecondsDiff;
import com.sos.commons.hibernate.function.json.SOSHibernateJsonValue;
import com.sos.commons.hibernate.function.regex.SOSHibernateRegexp;
import com.sos.commons.hibernate.type.SOSHibernateJsonType;
import com.sos.commons.util.SOSClassList;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSReflection;
import com.sos.commons.util.SOSString;

public class SOSHibernateFactory implements Serializable {

    public enum Dbms {
        DB2, H2, FBSQL, MSSQL, MYSQL, ORACLE, PGSQL, SYBASE, UNKNOWN
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateFactory.class);
    private static final Logger CONNECTION_POOL_LOGGER = LoggerFactory.getLogger("ConnectionPool");
    private static final long serialVersionUID = 1L;

    private SOSClassList classMapping;
    private Configuration configuration;
    private SessionFactory sessionFactory;
    private Dialect dialect;
    private Properties configurationProperties;
    private Properties defaultConfigurationProperties;
    private Dbms dbms = Dbms.UNKNOWN;
    private Optional<Path> configFile = Optional.empty();
    private Optional<Integer> jdbcFetchSize = Optional.empty();

    private String identifier;
    private String logIdentifier;
    private String dbmsVersion;
    private Boolean supportJsonReturningClob;
    private boolean useDefaultConfigurationProperties = true;

    public SOSHibernateFactory() throws SOSHibernateConfigurationException {
        this((String) null);
    }

    public SOSHibernateFactory(Path hibernateConfigFile) throws SOSHibernateConfigurationException {
        setIdentifier(null);
        setConfigFile(hibernateConfigFile);
        initClassMapping();
        initConfigurationProperties();
    }

    public SOSHibernateFactory(String hibernateConfigFile) throws SOSHibernateConfigurationException {
        setIdentifier(null);
        setConfigFile(hibernateConfigFile);
        initClassMapping();
        initConfigurationProperties();
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

    public void build() throws SOSHibernateFactoryBuildException {
        try {
            initConfiguration();
            adjustConfiguration(configuration);
            showConfigurationProperties();
            adjustAnnotations(dbms);
            initSessionFactory();
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

    // to override
    public void adjustConfiguration(Configuration config) {

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

    public boolean dbmsIsPostgres() {
        return Dbms.PGSQL.equals(dbms);
    }

    public boolean dbmsIsH2() {
        return Dbms.H2.equals(dbms);
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

    public Optional<Integer> getJdbcFetchSize() {
        return jdbcFetchSize;
    }

    /** Hibernate Dialect does not provide the functions to identify the last inserted sequence value.
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
        case DB2:
            return "SELECT IDENTITY_VAL_LOCAL() AS INSERT_ID FROM SYSIBM.SYSDUMMY1";
        case SYBASE:
            return "SELECT @@IDENTITY";
        case FBSQL:
        case UNKNOWN:
            break;
        }
        return null;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
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
        CONNECTION_POOL_LOGGER.debug("--------> GET CONNECTION: " + session.getIdentifier() + " (" + SOSClassUtil.getMethodName(3) + ") --------");
        return session;
    }

    public String quote(Type type, Object value) throws SOSHibernateConvertException {
        if (value == null) {
            return "NULL";
        }
        try {
            if (type instanceof org.hibernate.type.NumericBooleanType) {
                return NumericBooleanType.INSTANCE.objectToSQLString((Boolean) value, dialect);
            } else if (type instanceof org.hibernate.type.LongType) {
                return org.hibernate.type.LongType.INSTANCE.objectToSQLString((Long) value, dialect);
            } else if (type instanceof org.hibernate.type.StringType) {
                return "'" + value.toString().replaceAll("'", "''") + "'";
            } else if (type instanceof org.hibernate.type.TimestampType) {
                String val;
                switch (dbms) {
                case ORACLE:
                    val = SOSDate.format((Date) value, "yyyy-MM-dd HH:mm:ss");
                    return "to_date('" + val + "','yyyy-mm-dd HH24:MI:SS')";
                case MSSQL:
                    val = SOSDate.format((Date) value, "yyyy-MM-dd HH:mm:ss.SSS");
                    return "'" + val.replace(" ", "T") + "'";
                default:
                    return TimestampType.INSTANCE.objectToSQLString((Date) value, dialect);
                }
            }
        } catch (Exception e) {
            throw new SOSHibernateConvertException(String.format("can't convert value=%s to SQL string", value), e);
        }
        return value + "";
    }

    public String quoteColumn(String columnName) {
        return SOSHibernate.quoteColumn(dialect, columnName);
    }

    public void setAutoCommit(boolean commit) {
        configurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_AUTO_COMMIT, String.valueOf(commit));
    }

    public void setConfigFile(Path hibernateConfigFile) throws SOSHibernateConfigurationException {
        if (hibernateConfigFile != null) {
            if (!Files.exists(hibernateConfigFile)) {
                throw new SOSHibernateConfigurationException(String.format("hibernate config file not found: %s", hibernateConfigFile.toString()));
            }
            configFile = Optional.of(hibernateConfigFile);
        }
    }

    public void setConfigFile(String hibernateConfigFile) throws SOSHibernateConfigurationException {
        setConfigFile(hibernateConfigFile == null ? null : Paths.get(hibernateConfigFile));
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

    private void configure() throws SOSHibernateConfigurationException {
        try {
            addSQLFunctions();

            if (configFile.isPresent()) {
                configuration.configure(configFile.get().toUri().toURL());
            } else {
                configuration.configure();
            }
            if (LOGGER.isDebugEnabled()) {
                String method = SOSHibernate.getMethodName(logIdentifier, "configure");
                if (configFile.isPresent()) {
                    LOGGER.debug(String.format("%s %s", method, configFile.get().toAbsolutePath().toString()));
                } else {
                    LOGGER.debug(String.format("%s configure connection without the hibernate file", method));
                }

            }
            setDbms(configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT));
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
            if (dialectClassName.contains("db2")) {
                dbms = Dbms.DB2;
            } else if (dialectClassName.contains("h2")) {
                dbms = Dbms.H2;
            } else if (dialectClassName.contains("firebird")) {
                dbms = Dbms.FBSQL;
            } else if (dialectClassName.contains("sqlserver")) {
                dbms = Dbms.MSSQL;
            } else if (dialectClassName.contains("mysql")) {
                dbms = Dbms.MYSQL;
            } else if (dialectClassName.contains("oracle")) {
                dbms = Dbms.ORACLE;
            } else if (dialectClassName.contains("postgre")) {
                dbms = Dbms.PGSQL;
            } else if (dialectClassName.contains("sybase")) {
                dbms = Dbms.SYBASE;
            }
        }
        return dbms;
    }

    private void initClassMapping() {
        classMapping = new SOSClassList();
    }

    private void initConfiguration() throws SOSHibernateConfigurationException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(SOSHibernate.getMethodName(logIdentifier, "initConfiguration"));
        }
        configuration = new Configuration();
        setConfigurationClassMapping();
        setDefaultConfigurationProperties();
        configure();
        setConfigurationProperties();
        resolveCredentialStoreProperties();
    }

    private void initConfigurationProperties() {
        defaultConfigurationProperties = new Properties();
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_TRANSACTION_ISOLATION, String.valueOf(
                Connection.TRANSACTION_READ_COMMITTED));
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_AUTO_COMMIT, "false");
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_USE_SCROLLABLE_RESULTSET, "true");
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_CURRENT_SESSION_CONTEXT_CLASS, "jta");
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_JAVAX_PERSISTENCE_VALIDATION_MODE, "none");
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_ID_NEW_GENERATOR_MAPPINGS, "false");
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_SOS_PROPERTY_MSSQL_LOCK_TIMEOUT, "30000");// 30s
        configurationProperties = new Properties();
    }

    private void initSessionFactory() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(SOSHibernate.getMethodName(logIdentifier, "initSessionFactory"));
        }
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        dialect = ((SessionFactoryImplementor) sessionFactory).getJdbcServices().getDialect();
    }

    private void showConfigurationProperties() {
        String property = configuration.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_SHOW_CONFIGURATION_PROPERTIES);
        if (property != null && property.toLowerCase().equals("true")) {
            String method = SOSHibernate.getMethodName(logIdentifier, "showConfigurationProperties");
            for (Map.Entry<?, ?> entry : configuration.getProperties().entrySet()) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                if (key.equals(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_PASSWORD)) {
                    value = "***";
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
            if (configuration.getProperty(SOSHibernate.HIBERNATE_PROPERTY_JDBC_FETCH_SIZE) != null) {
                try {
                    jdbcFetchSize = Optional.of(Integer.parseInt(configuration.getProperty(SOSHibernate.HIBERNATE_PROPERTY_JDBC_FETCH_SIZE)));
                } catch (Exception ex) {
                    //
                }
            }
        }
    }

    private void resolveCredentialStoreProperties() throws SOSHibernateConfigurationException {
        if (configuration == null) {
            return;
        }

        try {
            String f = configuration.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_CREDENTIAL_STORE_FILE);
            String kf = configuration.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_CREDENTIAL_STORE_KEY_FILE);
            String p = configuration.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_CREDENTIAL_STORE_PASSWORD);
            String ep = configuration.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_CREDENTIAL_STORE_ENTRY_PATH);
            SOSKeePassResolver r = new SOSKeePassResolver(f, kf, p);
            r.setEntryPath(ep);

            String url = configuration.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL);
            if (url != null) {
                configuration.setProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL, r.resolve(url));
            }
            String username = configuration.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_USERNAME);
            if (username != null) {
                configuration.setProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_USERNAME, r.resolve(username));
            }
            String password = configuration.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_PASSWORD);
            if (password != null) {
                configuration.setProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_PASSWORD, r.resolve(password));
            }

        } catch (Throwable e) {
            throw new SOSHibernateConfigurationException(e.toString(), e);
        }
    }

    private void setDbms(String dialect) {
        dbms = getDbms(dialect);
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

    private void changeJsonAnnotations4H2() {
        for (Class<?> c : classMapping.getClasses()) {
            List<Field> fields = Arrays.stream(c.getDeclaredFields()).filter(m -> m.isAnnotationPresent(org.hibernate.annotations.Type.class) && m
                    .getAnnotation(org.hibernate.annotations.Type.class).type().equals(SOSHibernateJsonType.TYPE_NAME)).collect(Collectors.toList());
            if (fields != null && fields.size() > 0) {
                for (Field field : fields) {
                    field.setAccessible(true);
                    org.hibernate.annotations.ColumnTransformer ct = field.getAnnotation(org.hibernate.annotations.ColumnTransformer.class);
                    if (ct != null) {
                        try {
                            SOSReflection.changeAnnotationValue(ct, "write", SOSHibernateJsonType.COLUMN_TRANSFORMER_WRITE_H2);
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

    public void setDbmsVersion(String val) {
        dbmsVersion = val;
    }

    public String getDbmsVersion() {
        return dbmsVersion;
    }

    public boolean getSupportJsonReturningClob() {
        String method = SOSHibernate.getMethodName(logIdentifier, "getSupportJsonReturningClob");
        if (supportJsonReturningClob == null) {
            supportJsonReturningClob = true;
            if (dbms != null && dbms.equals(Dbms.ORACLE)) {
                supportJsonReturningClob = false;
                if (dbmsVersion != null) {
                    // 12.2.0.1
                    // 18
                    try {
                        int major = -1;
                        int idx = dbmsVersion.indexOf(".");
                        if (idx > -1) {
                            major = Integer.parseInt(dbmsVersion.substring(0, idx));
                        } else {
                            major = Integer.parseInt(dbmsVersion);
                        }
                        if (major >= 18) {
                            supportJsonReturningClob = true;
                        }
                    } catch (Throwable e) {
                        LOGGER.warn(String.format("[%s][dbmsVersion=%s]%s", method, dbmsVersion, e.toString()), e);
                    }
                }
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s][%s][dbmsVersion=%s]supportJsonReturningClob=%s", method, dbms, dbmsVersion, supportJsonReturningClob));
        }
        return supportJsonReturningClob.booleanValue();
    }
}