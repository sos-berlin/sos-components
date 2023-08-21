package com.sos.joc.cleanup.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TimeZone;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSPath;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.db.DBLayer;

public class CleanupTaskTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskTest.class);

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

    @Ignore
    @Test
    public void testCleanupHistory() throws Exception {
        JocClusterHibernateFactory factory = null;
        try {
            factory = createFactory();
            CleanupTaskHistory t = new CleanupTaskHistory(factory, null, 1000);
            t.tryOpenSession();
            t.getDbLayer().beginTransaction();

            t.deleteControllers(Long.valueOf(1692712200000000L), new StringBuilder());
            t.deleteAgents(Long.valueOf(1692712200000000L), new StringBuilder());

            t.getDbLayer().commit();
            t.getDbLayer().close();

        } catch (Exception e) {
            throw e;
        } finally {
            if (factory != null) {
                factory.close();
            }
        }
    }

    @Ignore
    @Test
    public void testLastModified() throws SOSInvalidDataException {
        Path p = Paths.get("src/test/resources/hibernate.cfg.xml");
        LOGGER.info(String.format("[%s][ms=%s]%s", TimeZone.getDefault().getID(), SOSPath.getLastModified(p).getTime(), SOSDate.getDateTimeAsString(
                SOSPath.getLastModified(p))));

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        LOGGER.info(String.format("[%s][ms=%s]%s", TimeZone.getDefault().getID(), SOSPath.getLastModified(p).getTime(), SOSDate.getDateTimeAsString(
                SOSPath.getLastModified(p))));

    }

    private JocClusterHibernateFactory createFactory() throws Exception {
        JocClusterHibernateFactory factory = new JocClusterHibernateFactory(Paths.get("src/test/resources/hibernate.cfg.xml"), 1, 1);
        factory.addClassMapping(DBLayer.getJocClassMapping());
        factory.build();
        return factory;
    }
}
