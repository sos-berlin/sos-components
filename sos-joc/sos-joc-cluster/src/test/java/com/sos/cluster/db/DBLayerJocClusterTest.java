package com.sos.cluster.db;

import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.cluster.configuration.JocClusterConfiguration;
import com.sos.joc.cluster.db.DBLayerJocCluster;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.joc.DBItemJocCluster;

public class DBLayerJocClusterTest {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerJocClusterTest.class);

    @Ignore
    @Test
    public void test() throws Exception {

        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = createFactory();
            session = factory.openStatelessSession();

            DBLayerJocCluster dbLayer = new DBLayerJocCluster(session);

            dbLayer.beginTransaction();
            DBItemJocCluster item = dbLayer.getCluster();
            if (item == null) {
                item = new DBItemJocCluster();
                item.setId(JocClusterConfiguration.IDENTIFIER);
                item.setMemberId("xxxx");
                dbLayer.getSession().save(item);

            }
            dbLayer.commit();

            dbLayer.beginTransaction();
            item = dbLayer.getCluster();
            item.setSwitchMemberId("yyyy");
            dbLayer.getSession().update(item);
            dbLayer.commit();

        } catch (Exception e) {
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
        factory.addClassMapping(DBLayer.getJocClusterClassMapping());
        factory.build();
        return factory;
    }

}
