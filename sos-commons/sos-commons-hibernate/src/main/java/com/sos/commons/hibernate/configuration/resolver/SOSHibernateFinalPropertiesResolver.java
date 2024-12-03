package com.sos.commons.hibernate.configuration.resolver;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernate.Dbms;
import com.sos.commons.hibernate.exception.SOSHibernateConfigurationException;
import com.sos.commons.util.SOSString;

public class SOSHibernateFinalPropertiesResolver implements ISOSHibernateConfigurationResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateFinalPropertiesResolver.class);

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
        if (driver != null && !driver.isEmpty() && connectionUrl != null && !connectionUrl.isEmpty()) {
            if (driver.toLowerCase().contains("mariadb") && connectionUrl.contains("jdbc:mysql")) {
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
        if (configuration != null) {
            this.dbms = getDbms(configuration.getProperties());
            String dialect = configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT);
            if (dialect != null) {
                switch (this.dbms) {
                case MYSQL:
                    if (configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT).equals(
                            "org.hibernate.dialect.MySQLInnoDBDialect")) {
                        configuration.getProperties().setProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT, SOSHibernate.DEFAULT_DIALECT_MYSQL);
                    }
                    if (configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT).equals(
                            "org.hibernate.dialect.MariaDBDialect")) {
                        if (configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL).contains("jdbc:mysql")) {
                            configuration.getProperties().setProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT, SOSHibernate.DEFAULT_DIALECT_MYSQL);
                        } else {
                            if (configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_ALLOW_METADATA_ON_BOOT).equals("true")) {
                                removeProperty(configuration, SOSHibernate.HIBERNATE_PROPERTY_DIALECT);
                            }
                        }
                    }
                    break;
                case ORACLE:
                    if (configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT).equals(SOSHibernate.DEFAULT_DIALECT_ORACLE)
                            && configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_ALLOW_METADATA_ON_BOOT).equals("true")) {
                        removeProperty(configuration, SOSHibernate.HIBERNATE_PROPERTY_DIALECT);
                    }
                    break;
                case PGSQL:
                    if (configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT).equals(SOSHibernate.DEFAULT_DIALECT_PGSQL)
                            && configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_ALLOW_METADATA_ON_BOOT).equals("true")) {
                        removeProperty(configuration, SOSHibernate.HIBERNATE_PROPERTY_DIALECT);
                    }
                    break;
                case MSSQL:
                    if (!Arrays.asList("org.hibernate.dialect.SQLServer2012Dialect", "org.hibernate.dialect.SQLServer2016Dialect",
                            "org.hibernate.dialect.SQLServerDialect").contains(configuration.getProperties().getProperty(
                                    SOSHibernate.HIBERNATE_PROPERTY_DIALECT)) && configuration.getProperties().getProperty(
                                            SOSHibernate.HIBERNATE_PROPERTY_ALLOW_METADATA_ON_BOOT).equals("true")) {
                        removeProperty(configuration, SOSHibernate.HIBERNATE_PROPERTY_DIALECT);
                    }
                    break;
                case H2:
                    if (configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT).equals(SOSHibernate.DEFAULT_DIALECT_H2)
                            && configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_ALLOW_METADATA_ON_BOOT).equals("true")) {
                        removeProperty(configuration, SOSHibernate.HIBERNATE_PROPERTY_DIALECT);
                    }
                    break;
                default:
                    break;
                }
            }
        }
    }

    private Dbms getDbms(Properties properties) {
        Dbms dbms = Dbms.UNKNOWN;
        String connectionUrl = properties.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL);
        if (!SOSString.isEmpty(connectionUrl)) {
            dbms = SOSHibernate.JDBC_TO_DBMS.entrySet().stream().filter(entry -> connectionUrl.contains(entry.getKey())).map(Map.Entry::getValue)
                    .findFirst().orElse(Dbms.UNKNOWN);
        } else {
            String dbmsProduct = properties.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_DBMS_PRODUCT);
            if (!SOSString.isEmpty(dbmsProduct)) {
                dbms = SOSHibernate.DBMS_PRODUCT_TO_DBMS.entrySet().stream().filter(entry -> dbmsProduct.equalsIgnoreCase(entry.getKey())).map(
                        Map.Entry::getValue).findFirst().orElse(Dbms.UNKNOWN);
            } else {
                dbms = getDbmsFromDialect(properties.getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT));
            }
        }
        return dbms;
    }

    private static Dbms getDbmsFromDialect(String dialect) {
        if (SOSString.isEmpty(dialect)) {
            return Dbms.UNKNOWN;
        }
        final String d = dialect.toLowerCase();
        return SOSHibernate.DIALECT_TO_DBMS.entrySet().stream().filter(entry -> d.contains(entry.getKey())).map(Map.Entry::getValue).findFirst()
                .orElse(Dbms.UNKNOWN);
    }

    private void removeProperty(Configuration configuration, String key) {
        configuration.getProperties().remove(key);
        configuration.getStandardServiceRegistryBuilder().getSettings().remove(key);
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
        f.getProperties().put(SOSHibernate.HIBERNATE_SOS_PROPERTY_DBMS_PRODUCT, d);
        return d;
    }

    public static Dbms getDbms(SessionFactory f) {
        if (f == null) {
            return Dbms.UNKNOWN;
        }
        Dbms dbms = null;
        try {
            dbms = (Dbms) f.getProperties().get(SOSHibernate.HIBERNATE_SOS_PROPERTY_DBMS_PRODUCT);
        } catch (Throwable e) {
            LOGGER.warn(e.toString());
        }
        return dbms == null ? Dbms.UNKNOWN : dbms;
    }

    public Dbms getDbms() {
        return dbms;
    }

}
