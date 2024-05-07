package com.sos.commons.hibernate.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hibernate.cfg.Configuration;

import com.sos.commons.hibernate.configuration.resolver.ISOSHibernateConfigurationResolver;
import com.sos.commons.hibernate.configuration.resolver.SOSHibernateCredentiaStoreResolver;
import com.sos.commons.hibernate.configuration.resolver.SOSHibernateEncryptionResolver;
import com.sos.commons.hibernate.configuration.resolver.SOSHibernateJS7Resolver;
import com.sos.commons.hibernate.exception.SOSHibernateConfigurationException;

public class SOSHibernateConfigurationResolver {

    private final List<ISOSHibernateConfigurationResolver> resolvers;

    public SOSHibernateConfigurationResolver() {
        resolvers = new ArrayList<>();

        // 1 - decrypt entries
        resolvers.add(new SOSHibernateEncryptionResolver());
        // 2- resolve environment variables
        resolvers.add(new SOSHibernateJS7Resolver());
        // 3 - resolve cs:// entries
        resolvers.add(new SOSHibernateCredentiaStoreResolver());
    }

    public Configuration resolve(Configuration configuration) throws SOSHibernateConfigurationException {
        for (ISOSHibernateConfigurationResolver r : resolvers) {
            configuration = r.resolve(configuration);
        }
        return configuration;
    }

    public void addResolver(ISOSHibernateConfigurationResolver resolver) {
        Optional<ISOSHibernateConfigurationResolver> existingInstance = resolvers.stream().filter(instance -> instance.getClass().equals(resolver
                .getClass())).findFirst();
        // Add or replace the existing resolver of the same type as the new resolver
        existingInstance.ifPresentOrElse(instance -> resolvers.set(resolvers.indexOf(instance), resolver), () -> resolvers.add(resolver));
    }
}
