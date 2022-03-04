package com.sos.joc.cleanup.model;

import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.db.DBLayer;

public class CleanupTaskTest {

    @Ignore
    @Test
    public void testCleanupDeployment() throws Exception {
        JocClusterHibernateFactory factory = null;
        try {
            factory = createFactory();
            CleanupTaskDeployment t = new CleanupTaskDeployment(factory, 1, "deployment");

            t.cleanupSearch();

            t.getDbLayer().close();

        } catch (Exception e) {
            throw e;
        } finally {
            if (factory != null) {
                factory.close();
            }
        }
    }

    private JocClusterHibernateFactory createFactory() throws Exception {
        JocClusterHibernateFactory factory = new JocClusterHibernateFactory(Paths.get("src/test/resources/hibernate.cfg.xml"), 1, 1);
        factory.addClassMapping(DBLayer.getJocClassMapping());
        factory.build();
        return factory;
    }
}
