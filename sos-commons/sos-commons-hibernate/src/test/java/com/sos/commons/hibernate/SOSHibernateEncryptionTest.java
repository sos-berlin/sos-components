package com.sos.commons.hibernate;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.commons.hibernate.configuration.resolver.SOSHibernateEncryptionResolver;
import com.sos.commons.hibernate.exception.SOSHibernateFactoryBuildException;

public class SOSHibernateEncryptionTest {

    private static final Path DIR_RESOURCES = Paths.get("src/test/resources/encryption");

    @Ignore
    @Test
    public void testEncryption() throws SOSHibernateFactoryBuildException {
        // Arrays.stream(Security.getProviders())
        // .flatMap(provider -> provider.getServices().stream())
        // .filter(service -> "Cipher".equals(service.getType()))
        // .map(Provider.Service::getAlgorithm)
        // .forEach(item -> LOGGER.info(item));
        SOSHibernateFactory factory = new SOSHibernateFactory(DIR_RESOURCES.resolve("hibernate.cfg.xml"));
        factory.build();
        factory.close();
    }

    @Ignore
    @Test
    public void testEncryptionWithSettings() throws SOSHibernateFactoryBuildException {
        SOSHibernateEncryptionResolver r = new SOSHibernateEncryptionResolver();
        // JOC-1770_keystore.p12 contains for key pairs, setting of an alias is required
        // JOC-1770_keystore2.p12 contains a single key pair, setting alias is not required
        r.setKeystorePath(DIR_RESOURCES.resolve("JOC-1770_keystore2.p12").toString());
        r.setKeystorePassword("jobscheduler");
        // d.setKeystoreKeyPassword("jobscheduler");
        // d.setKeystoreKeyAlias("ec_test");
        r.setKeystoreType("PKCS12");

        SOSHibernateFactory factory = new SOSHibernateFactory(DIR_RESOURCES.resolve("hibernate.cfg.xml"));
        factory.getConfigurationResolver().addResolver(r);

        factory.build();
        factory.close();
    }
}
