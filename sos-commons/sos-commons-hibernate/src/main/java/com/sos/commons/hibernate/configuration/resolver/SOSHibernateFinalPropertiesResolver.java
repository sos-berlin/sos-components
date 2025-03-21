package com.sos.commons.hibernate.configuration.resolver;

import java.util.Map;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernate.Dbms;
import com.sos.commons.hibernate.SOSHibernateDatabaseMetaData;
import com.sos.commons.hibernate.configuration.resolver.dialect.SOSHibernateForceMySQLDialectResolver;
import com.sos.commons.hibernate.exception.SOSHibernateConfigurationException;
import com.sos.commons.util.SOSString;

public class SOSHibernateFinalPropertiesResolver implements ISOSHibernateConfigurationResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateFinalPropertiesResolver.class);

    private static final String DIALECT_RESOLVER_FORCE_MYSQL = SOSHibernateForceMySQLDialectResolver.class.getName();

    private Dbms dbms;

    @Override
    public Configuration resolve(Configuration configuration) throws SOSHibernateConfigurationException {
        updateConnectionUrlForMysqlWithMariaDriver(configuration.getProperties());
        mapDialect(configuration);
        return configuration;
    }

    private void updateConnectionUrlForMysqlWithMariaDriver(Properties properties) {
        String driver = properties.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_DRIVERCLASS);
        String connectionUrl = properties.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL);
        if (!SOSString.isEmpty(driver) && !SOSString.isEmpty(connectionUrl)) {
            if (driver.toLowerCase().contains("mariadb") && isMySQLURL(connectionUrl)) {
                if (!connectionUrl.contains("permitMysqlScheme")) {
                    if (connectionUrl.contains("?")) {
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

    private void mapDialect(Configuration configuration) {
        // with Hibernate 6: Version-specific and spatial-specific dialects are deprecated
        // simply MySQL8Dialect, SQLServer2012Dialect, SQLServer2016Dialect are still implemented
        // so, old dialects are mapped: Example: org.hibernate.dialect.MySQLInnoDBDialect -> org.hibernate.dialect.MySQLDialect
        this.dbms = getDbms(configuration.getProperties());
        populateDbms(configuration);

        String dialect = configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT);
        boolean allowMetadataOnBoot = isAllowMetadataOnBoot(configuration);
        switch (this.dbms) {
        case MYSQL:
            if (dialect == null) {
                if (isMySQLURL(configuration)) {
                    populateDialectResolverForceMySQL(configuration);
                }
            } else if (dialect.equals(SOSHibernate.DEFAULT_DIALECT_MARIADB)) {
                if (isMySQLURL(configuration)) {
                    removeProperty(configuration, SOSHibernate.HIBERNATE_PROPERTY_DIALECT);
                    populateDialectResolverForceMySQL(configuration);
                }
            } else {
                mapDialect(configuration, allowMetadataOnBoot, dialect, SOSHibernate.DEFAULT_DIALECT_MYSQL);
                dialect = configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT);
                if (dialect == null) {
                    populateDialectResolverForceMySQL(configuration);
                }
            }
            break;
        case ORACLE:
            mapDialect(configuration, allowMetadataOnBoot, dialect, SOSHibernate.DEFAULT_DIALECT_ORACLE);
            break;
        case PGSQL:
            mapDialect(configuration, allowMetadataOnBoot, dialect, SOSHibernate.DEFAULT_DIALECT_PGSQL);
            break;
        case MSSQL:
            mapDialect(configuration, allowMetadataOnBoot, dialect, SOSHibernate.DEFAULT_DIALECT_MSSQL);
            break;
        case H2:
            mapDialect(configuration, allowMetadataOnBoot, dialect, SOSHibernate.DEFAULT_DIALECT_H2);
            break;
        default:
            break;
        }
    }

    private boolean isMySQLURL(Configuration configuration) {
        return isMySQLURL(configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL));
    }

    private boolean isMySQLURL(String url) {
        if (url == null) {
            return false;
        }
        return url.contains("jdbc:mysql");
    }

    private boolean isAllowMetadataOnBoot(Configuration configuration) {
        return configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_ALLOW_METADATA_ON_BOOT).equals("true");
    }

    private String getConfiguredDialectOrDefault(Configuration configuration, String dialect, String defaultDialect) {
        if (dialect.startsWith(SOSHibernate.DEFAULT_DIALECT_PACKAGE)) {
            configuration.getProperties().setProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT, defaultDialect);
            return defaultDialect;
        }
        return dialect;
    }

    private void populateDialectResolverForceMySQL(Configuration configuration) {
        setProperty(configuration, SOSHibernate.HIBERNATE_PROPERTY_DIALECT_RESOLVERS, DIALECT_RESOLVER_FORCE_MYSQL);
    }

    private void mapDialect(Configuration configuration, boolean allowMetadataOnBoot, String dialect, String defaultDialect) {
        if (dialect == null) {
            return;
        }
        dialect = getConfiguredDialectOrDefault(configuration, dialect, defaultDialect);
        if (dialect.equals(defaultDialect) && allowMetadataOnBoot) {
            removeProperty(configuration, SOSHibernate.HIBERNATE_PROPERTY_DIALECT);
        }
    }

    private Dbms getDbms(Properties properties) {
        Dbms dbms = Dbms.UNKNOWN;
        String connectionUrl = properties.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL);
        if (!SOSString.isEmpty(connectionUrl)) {
            dbms = SOSHibernate.JDBC_TO_DBMS.entrySet().stream().filter(entry -> SOSString.containsIgnoreCase(connectionUrl, entry.getKey())).map(
                    Map.Entry::getValue).findFirst().orElse(Dbms.UNKNOWN);
        } else {
            String dbmsProduct = properties.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_DBMS_PRODUCT);
            if (!SOSString.isEmpty(dbmsProduct)) {
                dbms = SOSHibernate.SOS_DBMS_PRODUCT_TO_DBMS.entrySet().stream().filter(entry -> dbmsProduct.equalsIgnoreCase(entry.getKey())).map(
                        Map.Entry::getValue).findFirst().orElse(Dbms.UNKNOWN);
            } else {
                dbms = getDbmsFromDialect(properties.getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT));
            }
        }
        return dbms;
    }

    private void populateDbms(Configuration configuration) {
        setProperty(configuration, SOSHibernate.SESSION_FACTORY_VAR_DBMS, this.dbms);
    }

    private static Dbms getDbmsFromDialect(String dialect) {
        if (SOSString.isEmpty(dialect)) {
            return Dbms.UNKNOWN;
        }
        return SOSHibernate.DIALECT_PART_TO_DBMS.entrySet().stream().filter(entry -> SOSString.containsIgnoreCase(dialect, entry.getKey())).map(
                Map.Entry::getValue).findFirst().orElse(Dbms.UNKNOWN);
    }

    private static Dbms getDbms(Dialect dialect) {
        return getDbmsFromDialect(dialect == null ? null : dialect.getClass().getSimpleName());
    }

    public static Dbms finalCheckAndSetDbms(SessionFactory f, Dialect dialect, Dbms dbms) {
        Dbms d = dbms;
        if (d == null) {
            d = Dbms.UNKNOWN;
        }
        if (f == null) {
            return d;
        }
        if (Dbms.UNKNOWN.equals(d)) {
            d = getDbms(dialect);
        }
        f.getProperties().put(SOSHibernate.SESSION_FACTORY_VAR_DBMS, d);
        return d;
    }

    public static Dbms retrieveDbms(SessionFactory f) {
        if (f == null) {
            return Dbms.UNKNOWN;
        }
        Dbms dbms = null;
        try {
            dbms = (Dbms) f.getProperties().get(SOSHibernate.SESSION_FACTORY_VAR_DBMS);
        } catch (Throwable e) {
            LOGGER.warn(e.toString());
        }
        return dbms == null ? Dbms.UNKNOWN : dbms;
    }

    private static Dbms retrieveDbms(DialectResolutionInfo info) {
        Object o = info.getConfigurationValues().get(SOSHibernate.SESSION_FACTORY_VAR_DBMS);
        return o == null ? Dbms.UNKNOWN : (Dbms) o;
    }

    public static void populateDatabaseMetaData(DialectResolutionInfo info) {
        Dbms dbms = retrieveDbms(info);
        info.getConfigurationValues().put(SOSHibernate.SESSION_FACTORY_VAR_DATABASE_METADATA, new SOSHibernateDatabaseMetaData(dbms, info
                .getDatabaseMetadata()));
    }

    // one-time operation
    public static SOSHibernateDatabaseMetaData retrieveDatabaseMetaData(SessionFactory f) {
        Object o = f.getProperties().get(SOSHibernate.SESSION_FACTORY_VAR_DATABASE_METADATA);
        if (o == null) {
            return null;
        }
        // remove from SessionFactory-Object properties
        f.getProperties().remove(SOSHibernate.SESSION_FACTORY_VAR_DATABASE_METADATA);
        return (SOSHibernateDatabaseMetaData) o;
    }

    private void removeProperty(Configuration configuration, String key) {
        configuration.getProperties().remove(key);
        configuration.getStandardServiceRegistryBuilder().getSettings().remove(key);
    }

    private void setProperty(Configuration configuration, String key, Object value) {
        configuration.getProperties().put(key, value);
        configuration.getStandardServiceRegistryBuilder().getSettings().remove(key);
    }

    public Dbms getDbms() {
        return dbms;
    }

}
