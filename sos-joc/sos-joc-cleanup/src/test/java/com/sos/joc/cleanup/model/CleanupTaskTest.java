package com.sos.joc.cleanup.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSPath;
import com.sos.joc.cleanup.model.CleanupTaskHistory.Range;
import com.sos.joc.cleanup.model.CleanupTaskHistory.Scope;
import com.sos.joc.cleanup.model.CleanupTaskMonitoring.MontitoringRange;
import com.sos.joc.cleanup.model.CleanupTaskMonitoring.MontitoringScope;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;
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
        CleanupTaskHistory t = null;
        try {
            factory = createFactory();
            t = new CleanupTaskHistory(factory, null, 1000);

            Date d = SOSDate.add(new Date(), -9, ChronoUnit.DAYS);

            JocServiceTaskAnswerState state = t.cleanupOrders(Scope.MAIN, Range.ALL, d, "", true);
            LOGGER.info("[STATE]" + state);
        } catch (Exception e) {
            if (t != null && t.getDbLayer() != null) {
                t.getDbLayer().rollback();
            }
            throw e;
        } finally {
            if (t != null && t.getDbLayer() != null) {
                t.getDbLayer().close();
            }
            if (factory != null) {
                factory.close();
            }
        }
    }

    @Ignore
    @Test
    public void testCleanupMonitoring() throws Exception {
        JocClusterHibernateFactory factory = null;
        CleanupTaskMonitoring t = null;
        try {
            factory = createFactory();
            t = new CleanupTaskMonitoring(factory, null, 1000);

            Date d = SOSDate.add(new Date(), -2, ChronoUnit.DAYS);

            JocServiceTaskAnswerState state = t.cleanupOrders(MontitoringScope.MAIN, MontitoringRange.ALL, d, "");
            LOGGER.info("[STATE]" + state);
        } catch (Exception e) {
            if (t != null && t.getDbLayer() != null) {
                t.getDbLayer().rollback();
            }
            throw e;
        } finally {
            if (t != null && t.getDbLayer() != null) {
                t.getDbLayer().close();
            }
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
        factory.setAutoCommit(false);
        factory.build();
        return factory;
    }
}
