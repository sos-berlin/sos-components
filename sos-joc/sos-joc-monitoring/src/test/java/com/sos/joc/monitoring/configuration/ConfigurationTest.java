package com.sos.joc.monitoring.configuration;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationTest.class);

    @Ignore
    @Test
    public void testReverseProxy() throws Exception {
        Configuration.setJocReverseProxyUri("https://reverse.proxy.com/");
        LOGGER.info(Configuration.getJocReverseProxyUri());
    }

}
