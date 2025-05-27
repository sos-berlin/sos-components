package com.sos.joc.xmleditor.impl;

import java.nio.file.Path;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.joc.UnitTestSimpleWSImplHelper;
import com.sos.joc.model.xmleditor.apply.ApplyConfiguration;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.xmleditor.commons.standard.StandardSchemaHandler;

public class ApplyResourceImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplyResourceImplTest.class);

    @Ignore
    @Test
    public void testProcess() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new ApplyResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");
        Path xml = Path.of("src/test/resources/ws/xmleditor/impl/yade.xml");
        try {
            h.init();

            ApplyConfiguration filter = new ApplyConfiguration();
            filter.setId(32L);
            filter.setName("yade");
            filter.setObjectType(ObjectType.YADE);
            filter.setSchemaIdentifier(StandardSchemaHandler.getYADESchemaIdentifier());
            filter.setConfiguration(SOSPath.readFile(xml));

            h.post("process", filter);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

}
