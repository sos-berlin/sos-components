package com.sos.commons.hibernate;

import java.nio.file.Paths;
import java.util.TimeZone;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;

/** More test - see sos-joc-db/src/test/java/com/sos/commons/hibernate/ */
public class SOSHibernateSessionTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateSessionTest.class);

    /** TODO <br/>
     * PgSQL session.getCurrentDateTime():<br/>
     * the returned current date time depends on TimeZone.set/getDefault.<br/>
     * For all others, the returned current date time is independent of TimeZone.set/getDefault. */
    @Ignore
    @Test
    public void testDateTime() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = createFactory();

            LOGGER.info(String.format("----[SYSTEM][%s]%s", TimeZone.getDefault().getID(), SOSDate.getCurrentDateTimeAsString()));

            session = factory.openStatelessSession("test");
            LOGGER.info(String.format("[DB][%s]%s", factory.getDbms(), session.getCurrentDateTime()));
            LOGGER.info(String.format("[DB][UTC]%s", session.getCurrentUTCDateTime()));
        } catch (Exception e) {
            throw e;
        } finally {
            if (factory != null) {
                factory.close(session);
            }
        }
    }

    @Ignore
    @Test
    public void testNativeQuery() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = createFactory();

            session = factory.openStatelessSession("test");
            String sv = session.getSingleValueNativeQueryAsString("select max(ID) from HISTORY_ORDERS");
            Long lv = session.getSingleValueNativeQuery("select max(ID) from HISTORY_ORDERS", Long.class);
            LOGGER.info(String.format("[STRING]%s", sv));
            LOGGER.info(String.format("[LONG]%s", lv));
        } catch (Exception e) {
            throw e;
        } finally {
            if (factory != null) {
                factory.close(session);
            }
        }
    }

    protected static SOSHibernateFactory createFactory() throws Exception {
        SOSHibernateFactory factory = new SOSHibernateFactory(Paths.get("src/test/resources/hibernate.cfg.mysql.xml"));
        factory.build();

        LOGGER.info("DBMS=" + factory.getDbms() + ", DIALECT=" + factory.getDialect());
        return factory;
    }

}
