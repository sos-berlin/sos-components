package com.sos.joc.xmleditor.impl;

import java.nio.file.Path;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.joc.UnitTestSimpleWSImplHelper;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.store.StoreConfiguration;
import com.sos.joc.xmleditor.commons.standard.StandardSchemaHandler;

public class StoreResourceImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreResourceImplTest.class);

    @Ignore
    @Test
    public void testProcessYADE() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new StoreResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");
        try {
            StoreConfiguration in = new StoreConfiguration();

            in.setObjectType(ObjectType.YADE);
            in.setSchemaIdentifier(StandardSchemaHandler.getYADESchemaIdentifier());
            in.setName("test-sftp");
            in.setId(4L);
            in.setConfiguration(SOSPath.readFile(Path.of("D:/Downloads/test-sftp(1).xml")));

            h.init();
            h.post("process", in);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

}
