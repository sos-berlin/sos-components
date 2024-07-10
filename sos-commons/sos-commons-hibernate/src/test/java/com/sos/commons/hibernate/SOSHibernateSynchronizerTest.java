package com.sos.commons.hibernate;

import java.util.Collections;
import java.util.TimeZone;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSHibernateSynchronizerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateSynchronizerTest.class);

    @Ignore
    @Test
    public void testSync() {
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            SOSHibernateFactory sourceFactory = null;
            SOSHibernateSession sourceSession = null;

            SOSHibernateFactory targetFactory = null;
            SOSHibernateSession targetSession = null;
            try {
                sourceFactory = SOSHibernateSessionTest.createFactory();
                sourceSession = sourceFactory.openStatelessSession("source");

                targetFactory = SOSHibernateSessionTest.createFactory();
                targetSession = targetFactory.openStatelessSession("target");

                SOSHibernateSynchronizer s = new SOSHibernateSynchronizer();
                s.setParameters(Collections.singletonMap(Integer.valueOf(1), Long.valueOf(1)));

                String sourceTable = "a_test_source_history_orders";
                String targetTable = "a_test_target_history_orders";

                s.process(sourceSession, sourceTable, "select * from a_test_source_history_orders where id>?", "ID", targetSession, targetTable);

            } catch (Exception e) {
                throw e;
            } finally {
                if (sourceFactory != null) {
                    sourceFactory.close(sourceSession);
                }
                if (targetFactory != null) {
                    targetFactory.close(targetSession);
                }
            }
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

}
