package com.sos.commons.hibernate;

import java.nio.file.Paths;
import java.util.List;

import org.hibernate.ScrollableResults;
import org.hibernate.query.Query;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.db.DBLayer;
import com.sos.joc.db.history.DBItemHistoryOrderStep;

public class SOSHibernateTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateTest.class);
    private static SOSHibernateFactory factory = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        LOGGER.info("---------- [@BeforeClass] ----------");
        factory = new SOSHibernateFactory(Paths.get("src/test/resources/hibernate.cfg.xml"));
        factory.addClassMapping(DBLayer.getHistoryClassMapping());
        factory.build();
        LOGGER.info("---------- [@BeforeClass] ----------");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        LOGGER.info("---------- [@AfterClass] ----------");
        factory.close();
        LOGGER.info("---------- [@AfterClass] ----------");
    }

    @Ignore
    @Test
    public void testJoin() throws Exception {
        SOSHibernateSession session = null;
        try {
            session = factory.openStatelessSession();

            StringBuilder hql = new StringBuilder("select ho.orderKey as orderKey "); // set aliases for all properties
            hql.append(",hos.id as stepId,hos.jobName as jobName ");
            hql.append("from " + DBLayer.DBITEM_HISTORY_ORDER).append(" ho ");
            hql.append(",").append(DBLayer.DBITEM_HISTORY_ORDER_STEP).append(" hos ");
            hql.append("where ho.id=hos.orderId ");

            Query<MyJoinEntity> query = session.createQuery(hql.toString(), MyJoinEntity.class); // pass MyJoinEntity as resultType
            query.setMaxResults(10); // only for this test
            List<MyJoinEntity> result = session.getResultList(query);
            for (MyJoinEntity item : result) {
                LOGGER.info(SOSHibernate.toString(item));
            }

        } catch (Exception e) {
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Ignore
    @Test
    public void testScroll() throws Exception {

        SOSHibernateSession session = null;
        ScrollableResults sr = null;
        try {
            session = factory.openStatelessSession();

            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_ORDER_STEP);
            Query<DBItemHistoryOrderStep> query = session.createQuery(hql.toString());
            query.setMaxResults(10); // only for this test

            sr = session.scroll(query);
            while (sr.next()) {
                DBItemHistoryOrderStep step = (DBItemHistoryOrderStep) sr.get(0);
                LOGGER.info(SOSHibernate.toString(step));
            }

        } catch (Exception e) {
            throw e;
        } finally {
            if (sr != null) { // close ScrollableResults
                sr.close();
            }

            if (session != null) {
                session.close();
            }
        }
    }

}
