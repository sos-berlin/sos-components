package com.sos.commons.hibernate.configuration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.cfg.Configuration;

import com.sos.commons.hibernate.configuration.resolver.ISOSHibernateConfigurationResolver;
import com.sos.commons.hibernate.configuration.resolver.SOSHibernateCredentiaStoreResolver;
import com.sos.commons.hibernate.configuration.resolver.SOSHibernateEncryptionResolver;
import com.sos.commons.hibernate.configuration.resolver.SOSHibernateFinalPropertiesResolver;
import com.sos.commons.hibernate.configuration.resolver.SOSHibernateJS7Resolver;
import com.sos.commons.hibernate.exception.SOSHibernateConfigurationException;

public class SOSHibernateConfigurationResolver {

    private final List<ISOSHibernateConfigurationResolver> resolvers;
    private Map<String, String> result;

    public SOSHibernateConfigurationResolver() {
        resolvers = new ArrayList<>();

        // 1 - decrypt entries
        resolvers.add(new SOSHibernateEncryptionResolver());
        // 2- resolve environment variables
        resolvers.add(new SOSHibernateJS7Resolver());
        // 3 - resolve cs:// entries
        resolvers.add(new SOSHibernateCredentiaStoreResolver());
        // 4(last)- resolve configuration properties
        resolvers.add(new SOSHibernateFinalPropertiesResolver());
    }

    public Configuration resolve(Configuration configuration) throws SOSHibernateConfigurationException {
        Properties cloned = clone(configuration);
        for (ISOSHibernateConfigurationResolver r : resolvers) {
            configuration = r.resolve(configuration);
        }
        setResult(configuration, cloned);
        return configuration;
    }

    public void addResolver(ISOSHibernateConfigurationResolver resolver) {
        Optional<ISOSHibernateConfigurationResolver> existingInstance = resolvers.stream().filter(instance -> instance.getClass().equals(resolver
                .getClass())).findFirst();
        // Add or replace the existing resolver of the same type as the new resolver
        existingInstance.ifPresentOrElse(instance -> resolvers.set(resolvers.indexOf(instance), resolver), () -> resolvers.add(resolver));
    }

    public boolean propertyValueChanged(String propertyName) {
        return result != null && result.containsKey(propertyName);
    }

    /** Returns the changed properties with the "old" values and the new properties with masked values
     * 
     * @param propertyName
     * @return */
    public String getOldValueOrNewPropertyValue(String propertyName) {
        return result == null ? null : result.get(propertyName);
    }

    private Properties clone(Configuration configuration) {
        Properties cloned = new Properties();
        configuration.getProperties().stringPropertyNames().forEach(key -> cloned.setProperty(key, configuration.getProperties().getProperty(key)));
        return cloned;
    }

    private void setResult(Configuration configuration, Properties old) {
        Set<String> allKeys = new HashSet<String>(configuration.getProperties().stringPropertyNames());
        allKeys.addAll(old.stringPropertyNames());

        result = allKeys.stream().filter(key -> {
            String oldValue = old.getProperty(key);
            String newValue = configuration.getProperties().getProperty(key);
            return (oldValue == null && newValue != null) || (oldValue != null && newValue == null) || (oldValue != null && newValue != null
                    && !oldValue.equals(newValue));
        }).collect(Collectors.toMap(key -> key, key -> {
            String oldValue = old.getProperty(key);
            return oldValue != null ? oldValue : "***";
        }));
    }
}
