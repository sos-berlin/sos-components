package com.sos.commons.hibernate.function.date;

import java.nio.file.Paths;

import org.hibernate.query.Query;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.db.DBLayer;

public class SOSHibernateSecondsDiffTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateSecondsDiffTest.class);

    @Ignore
    @Test
    public void testSum() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = createFactory();
            session = factory.openStatelessSession();

            StringBuilder hql = new StringBuilder("select ");
            hql.append("sum(").append(SOSHibernateSecondsDiff.getFunction("startTime", "endTime")).append(") ");
            hql.append("from ").append(DBLayer.DBITEM_HISTORY_ORDER_STEPS).append(" ");
            // hql.append("where id in (1,2,3)");

            Query<Long> query = session.createQuery(hql.toString());
            Long result = session.getSingleValue(query);
            LOGGER.info("---- SUM=" + result);

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

    @Ignore
    @Test
    public void testAvg() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = createFactory();
            session = factory.openStatelessSession();

            StringBuilder hql = new StringBuilder("select ");
            hql.append("round(");
            hql.append("sum(").append(SOSHibernateSecondsDiff.getFunction("startTime", "endTime")).append(")/count(id)");
            hql.append(",0) ");// ,0 precision only because of MSSQL
            hql.append("from ").append(DBLayer.DBITEM_HISTORY_ORDER_STEPS).append(" ");
            // hql.append("where id in (1,2,3)");

            // hibernate returns Long and not Double ...
            Query<Long> query = session.createQuery(hql.toString());
            Long result = session.getSingleValue(query);
            LOGGER.info("---- AVG=" + result);

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
        factory.addClassMapping(DBLayer.getJocClassMapping());
        factory.build();
        LOGGER.info("DBMS: " + factory.getDbms());
        return factory;
    }

}
