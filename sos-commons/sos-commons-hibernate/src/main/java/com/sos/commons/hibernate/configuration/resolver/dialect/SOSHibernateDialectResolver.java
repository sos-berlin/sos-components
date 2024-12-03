package com.sos.commons.hibernate.configuration.resolver.dialect;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;

/** Used to force MySQLDialect when using MariaDB JDBC driver to connect to a MySQL Database<br/>
 * Also, prevents a deprecation warning from being triggered by Hibernate. */
public class SOSHibernateDialectResolver implements DialectResolver {

    private static final long serialVersionUID = 1L;

    @Override
    public Dialect resolveDialect(DialectResolutionInfo info) {
        if ("MySQL".equalsIgnoreCase(info.getDatabaseName())) {
            return new MySQLDialect();
        }
        return null;// standard resolver
    }

}
