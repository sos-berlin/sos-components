package com.sos.joc.xmleditor.impl;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.UnitTestSimpleWSImplHelper;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.db.xmleditor.XmlEditorDbLayer;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.xmleditor.commons.standard.StandardSchemaHandler;

public class StandardNotificationReleaseResourceImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardNotificationReleaseResourceImplTest.class);

    @Ignore
    @Test
    public void testRelease() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new StandardNotificationReleaseResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");

        try {
            h.init();

            h.post("release", getFilter());
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

    private StringBuilder getFilter() throws Exception {
        XmlEditorDbLayer dbLayer = null;
        try {
            dbLayer = new XmlEditorDbLayer(Globals.getHibernateFactory().openStatelessSession("test"));
            String name = StandardSchemaHandler.getDefaultConfigurationName(ObjectType.NOTIFICATION);
            DBItemXmlEditorConfiguration item = dbLayer.getObject(ObjectType.NOTIFICATION.name(), name);
            String configuration = item.getConfigurationDraft() == null ? item.getConfigurationReleased() : item.getConfigurationDraft();

            StringBuilder filter = new StringBuilder();
            filter.append("{");
            filter.append("\"objectType\": ").append(Globals.objectMapper.writeValueAsString(ObjectType.NOTIFICATION));
            filter.append(",\"configuration\": ").append(Globals.objectMapper.writeValueAsString(configuration));
            filter.append("}");
            return filter;
        } catch (Exception e) {
            throw e;
        } finally {
            DBLayer.close(dbLayer);
        }
    }

}
