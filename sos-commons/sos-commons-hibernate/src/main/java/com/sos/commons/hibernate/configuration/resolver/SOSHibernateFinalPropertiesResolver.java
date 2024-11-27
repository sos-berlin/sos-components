package com.sos.commons.hibernate.configuration.resolver;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import org.hibernate.cfg.Configuration;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernate.Dbms;
import com.sos.commons.hibernate.exception.SOSHibernateConfigurationException;
import com.sos.commons.util.SOSString;


public class SOSHibernateFinalPropertiesResolver implements ISOSHibernateConfigurationResolver {

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

    private void mapDialect(Configuration configuration) {
        // with Hibernate 6: Version-specific and spatial-specific dialects are deprecated
        // simply MySQL8Dialect, SQLServer2012Dialect, SQLServer2016Dialect are still implemented
        // so, old dialects are mapped: Example: org.hibernate.dialect.MySQLInnoDBDialect -> org.hibernate.dialect.MySQLDialect
        if (configuration != null) {
            Dbms dbms = getDbms(configuration.getProperties());
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
                                removeProperty(configuration, SOSHibernate.HIBERNATE_PROPERTY_DIALECT);
                            }
                        }
                    }
                    break;
                case ORACLE:
                    if(configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT).equals(SOSHibernate.DEFAULT_DIALECT_ORACLE)
                            && configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_ALLOW_METADATA_ON_BOOT).equals("true")) {
                        removeProperty(configuration, SOSHibernate.HIBERNATE_PROPERTY_DIALECT);
                    }
                    break;
                case PGSQL:
                    if(configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT).equals(SOSHibernate.DEFAULT_DIALECT_PGSQL)
                            && configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_ALLOW_METADATA_ON_BOOT).equals("true")) {
                        removeProperty(configuration, SOSHibernate.HIBERNATE_PROPERTY_DIALECT);
                    }
                    break;
                case MSSQL:
                    if (!Arrays.asList("org.hibernate.dialect.SQLServer2012Dialect", "org.hibernate.dialect.SQLServer2016Dialect",
                            "org.hibernate.dialect.SQLServerDialect").contains(configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT))
                            && configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_ALLOW_METADATA_ON_BOOT).equals("true")) {
                        removeProperty(configuration, SOSHibernate.HIBERNATE_PROPERTY_DIALECT);
                    }
                    break;
                case H2:
                    if(configuration.getProperties().getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT).equals(SOSHibernate.DEFAULT_DIALECT_H2)
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
        String dbms_product = properties.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_DBMS_PRODUCT);
        String dialect = properties.getProperty(SOSHibernate.HIBERNATE_PROPERTY_DIALECT);
        if (!SOSString.isEmpty(connectionUrl)) {
            dbms = SOSHibernate.JDBC2DBMS.entrySet().stream().filter(entry -> connectionUrl.contains(entry.getKey())).map(Map.Entry::getValue)
                .findFirst().orElse(Dbms.UNKNOWN);
        } else if (dbms_product != null && !dbms_product.isEmpty()) {
            if(dbms_product.contains("h2")) {
                dbms = Dbms.H2;
            } else if(dbms_product.contains("mssql")) {
                dbms = Dbms.MSSQL;
            } else if(dbms_product.contains("oracle")) {
                dbms = Dbms.ORACLE;
            } else if(dbms_product.contains("pgsql")) {
                dbms = Dbms.PGSQL;
            } else if(dbms_product.contains("mysql")
                    || dbms_product.contains("mariadb")) {
                dbms = Dbms.MYSQL;
            }
        } else if (dialect != null && !dialect.isEmpty()) {
            dialect = dialect.toLowerCase();
            if (dialect.contains("h2")) {
                dbms = Dbms.H2;
            } else if (dialect.contains("sqlserver")) {
                dbms = Dbms.MSSQL;
            } else if (dialect.contains("mysql") || dialect.contains("mariadb")) {
                dbms = Dbms.MYSQL;
            } else if (dialect.contains("oracle")) {
                dbms = Dbms.ORACLE;
            } else if (dialect.contains("postgre")) {
                dbms = Dbms.PGSQL;
            }
        }
        return dbms;
    }

    private void removeProperty(Configuration configuration, String key) {
        configuration.getProperties().remove(key);
        configuration.getStandardServiceRegistryBuilder().getSettings().remove(key);
    }

}
