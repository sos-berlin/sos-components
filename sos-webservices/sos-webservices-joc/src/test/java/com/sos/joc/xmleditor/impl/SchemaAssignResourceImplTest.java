package com.sos.joc.xmleditor.impl;

import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.UnitTestSimpleWSImplHelper;

public class SchemaAssignResourceImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaAssignResourceImplTest.class);

    @Ignore
    @Test
    public void testProcessOther() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new SchemaAssignResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");
        try {
            h.init();
           
            h.post("process", Paths.get("src/test/resources/ws/xmleditor/impl/request-SchemaAssignResourceImpl-process-OTHER.json"));
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }
}
