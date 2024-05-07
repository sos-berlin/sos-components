package com.sos.commons.hibernate.configuration.resolver;

import org.hibernate.cfg.Configuration;

import com.sos.commons.credentialstore.keepass.SOSKeePassResolver;
import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.exception.SOSHibernateConfigurationException;

public class SOSHibernateCredentiaStoreResolver implements ISOSHibernateConfigurationResolver {

    @Override
    public Configuration resolve(Configuration configuration) throws SOSHibernateConfigurationException {
        if (configuration == null) {
            return configuration;
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
            return configuration;
        } catch (Throwable e) {
            throw new SOSHibernateConfigurationException(e.toString(), e);
        }
    }

}
