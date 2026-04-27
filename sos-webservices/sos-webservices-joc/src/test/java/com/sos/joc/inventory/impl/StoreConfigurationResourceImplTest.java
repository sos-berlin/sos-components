package com.sos.joc.inventory.impl;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.UnitTestSimpleWSImplHelper;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class StoreConfigurationResourceImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreConfigurationResourceImplTest.class);

    @Ignore
    @Test
    public void testPostStoreFolder() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new StoreConfigurationResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");

        ConfigurationObject in = new ConfigurationObject();
        in.setPath("/A");
        in.setObjectType(ConfigurationType.FOLDER);

        try {
            h.init();

            h.post("store", in);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

    @Ignore
    @Test
    public void testPostStoreWorkflow() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new StoreConfigurationResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");

        ConfigurationObject in = new ConfigurationObject();
        in.setPath("/a");
        in.setObjectType(ConfigurationType.WORKFLOW);

        try {
            h.init();

            h.post("store", in);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

}
