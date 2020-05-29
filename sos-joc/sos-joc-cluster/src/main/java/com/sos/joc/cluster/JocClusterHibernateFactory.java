package com.sos.joc.cluster;

import java.nio.file.Path;

import org.hibernate.cfg.Configuration;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.exception.SOSHibernateConfigurationException;

public class JocClusterHibernateFactory extends SOSHibernateFactory {

    private static final long serialVersionUID = 1L;

    private final int minPool;
    private final int maxPool;

    public JocClusterHibernateFactory(Path config, int minPoolSize, int maxPoolSize) throws SOSHibernateConfigurationException {
        super(config);
        minPool = minPoolSize;
        maxPool = maxPoolSize;
    }

    @Override
    public void adjustConfiguration(Configuration config) {
        config.getProperties().entrySet().removeIf(entry -> entry.getKey().toString().startsWith("hibernate.c3p0"));

        config.setProperty("hibernate.c3p0.minPoolSize", String.valueOf(minPool));
        config.setProperty("hibernate.c3p0.maxPoolSize", String.valueOf(maxPool));
        config.setProperty("hibernate.c3p0.initialPoolSize", "1");
        config.setProperty("hibernate.c3p0.acquireIncrement", "1");
        config.setProperty("hibernate.c3p0.maxIdleTime", "14400");
        config.setProperty("hibernate.c3p0.maxConnectionAge", "14400");
        config.setProperty("hibernate.c3p0.idleConnectionTestPeriod", "16200");
        config.setProperty("hibernate.c3p0.checkoutTimeout", "0");
        config.setProperty("hibernate.c3p0.acquireRetryAttempts", "1");
    }

}
