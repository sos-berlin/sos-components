package com.sos.joc.xmleditor.impl;

import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.UnitTestSimpleWSImplHelper;

public class ReadResourceImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadResourceImplTest.class);

    @Ignore
    @Test
    public void testProcessYADE() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new ReadResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");
        try {
            h.init();

            h.post("process", Paths.get("src/test/resources/ws/xmleditor/impl/request-ReadResourceImpl-process-YADE.json"));
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

    @Ignore
    @Test
    public void testProcessNotification() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new ReadResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");
        try {
            h.init();

            h.post("process", Paths.get("src/test/resources/ws/xmleditor/impl/request-ReadResourceImpl-process-NOTIFICATION.json"));
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }
}
