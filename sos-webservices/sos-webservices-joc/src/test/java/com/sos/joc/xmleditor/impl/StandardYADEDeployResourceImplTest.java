package com.sos.joc.xmleditor.impl;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.UnitTestSimpleWSImplHelper;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.db.xmleditor.XmlEditorDbLayer;
import com.sos.joc.model.xmleditor.common.ObjectType;

public class StandardYADEDeployResourceImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardYADEDeployResourceImplTest.class);

    @Ignore
    @Test
    public void testDeploy() throws Exception {
        Long xmlEditorId = 30L;

        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new StandardYADEDeployResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");

        try {
            h.init();

            StringBuilder filter = getFilter(h.getControllerIds(), xmlEditorId);
            LOGGER.info("[filter]" + filter);

            h.post("deploy", filter);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

    private StringBuilder getFilter(List<String> controllerIds, Long xmlEditorId) throws Exception {
        XmlEditorDbLayer dbLayer = null;
        try {
            dbLayer = new XmlEditorDbLayer(Globals.getHibernateFactory().openStatelessSession("test"));
            DBItemXmlEditorConfiguration item = dbLayer.getObject(xmlEditorId);
            String configuration = item.getConfigurationDraft() == null ? item.getConfigurationReleased() : item.getConfigurationDraft();

            StringBuilder filter = new StringBuilder();
            filter.append("{");
            filter.append("\"objectType\": ").append(Globals.objectMapper.writeValueAsString(ObjectType.YADE));
            filter.append(",\"configuration\": ").append(Globals.objectMapper.writeValueAsString(configuration));
            filter.append(",\"id\": ").append(xmlEditorId);
            filter.append(",\"controllerIds\": [");
            filter.append(SOSString.join(controllerIds, ",", c -> "\"" + c + "\""));
            filter.append("]");
            filter.append("}");

            return filter;
        } catch (Exception e) {
            throw e;
        } finally {
            DBLayer.close(dbLayer);
        }
    }

}
