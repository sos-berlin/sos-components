package com.sos.commons.hibernate.configuration.resolver.dialect;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;

import com.sos.commons.hibernate.configuration.resolver.SOSHibernateFinalPropertiesResolver;

/** Used to set the Database MetaData without opening an additional Connection<br/>
 * Note: will not be called by Hibernate if hibernate.boot.allow_jdbc_metadata_access=false */
public class SOSHibernateDefaultDialectResolver implements DialectResolver {

    private static final long serialVersionUID = 1L;

    @Override
    public Dialect resolveDialect(DialectResolutionInfo info) {
        SOSHibernateFinalPropertiesResolver.populateDatabaseMetaData(info);
        return null;// standard resolver
    }

}
