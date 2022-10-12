package com.sos.joc.db.deploy;

import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.db.DBLayer;

public class DeployedConfigurationDBLayerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployedConfigurationDBLayerTest.class);

    @Ignore
    @Test
    public void testJsonMethods() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        String controllerId = "js7.x";
        try {
            factory = createFactory();
            session = factory.openStatelessSession();
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(session);

            Set<String> w = dbLayer.getAddOrderWorkflows(controllerId);
            LOGGER.info(String.format("[getAddOrderWorkflows]%s", w));

            List<String> en = dbLayer.getExpectedNoticeBoardWorkflows(controllerId);
            LOGGER.info(String.format("[getExpectedNoticeBoardWorkflows]%s", en));

        } catch (Exception e) {
            try {
                session.rollback();
            } catch (Throwable ex) {
            }
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
            if (factory != null) {
                factory.close();
            }
        }
    }

    private SOSHibernateFactory createFactory() throws Exception {
        SOSHibernateFactory factory = new SOSHibernateFactory(Paths.get("src/test/resources/hibernate.cfg.xml"));
        factory.addClassMapping(DBLayer.getJocClassMapping());
        factory.build();
        return factory;
    }
}
