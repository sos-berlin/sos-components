package com.sos.commons.hibernate.configuration.resolver.dialect;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;

/** Used to force MySQLDialect when using MariaDB JDBC driver to connect to a MySQL Database<br/>
 * Also, prevents a deprecation warning from being triggered by Hibernate.<br/>
 * Note: will not be called by Hibernate if hibernate.boot.allow_jdbc_metadata_access=false */
public class SOSHibernateForceMySQLDialectResolver extends SOSHibernateDefaultDialectResolver {

    private static final long serialVersionUID = 1L;

    @Override
    public Dialect resolveDialect(DialectResolutionInfo info) {
        super.resolveDialect(info);// set database metadata

        if ("MySQL".equalsIgnoreCase(info.getDatabaseName())) {
            return new MySQLDialect();
        }
        return null;// standard resolver if community or mariadb
    }

}
