package com.sos.commons.hibernate.configuration.resolver;

import org.hibernate.cfg.Configuration;

import com.sos.commons.hibernate.exception.SOSHibernateConfigurationException;

public interface ISOSHibernateConfigurationResolver {

    public Configuration resolve(Configuration configuration) throws SOSHibernateConfigurationException;
}
