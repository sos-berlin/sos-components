package com.sos.commons.hibernate;

import java.nio.file.Paths;
import java.util.List;

import org.hibernate.ScrollableResults;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
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

    /* HQL Queries */

    @Ignore
    @Test
    public void testJoinWithCustomEntity() throws Exception {
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
    public void testJoinWithoutEntity() throws Exception {
        SOSHibernateSession session = null;
        try {
            session = factory.openStatelessSession();

            StringBuilder hql = new StringBuilder("select ho.orderKey ");
            hql.append(",hos.id, hos.jobName ");
            hql.append("from " + DBLayer.DBITEM_HISTORY_ORDER).append(" ho ");
            hql.append(",").append(DBLayer.DBITEM_HISTORY_ORDER_STEP).append(" hos ");
            hql.append("where ho.id=hos.orderId ");

            Query<Object[]> query = session.createQuery(hql.toString());
            query.setMaxResults(10); // only for this test
            List<Object[]> result = session.getResultList(query);
            for (Object[] item : result) {
                LOGGER.info(SOSHibernate.toString(item));
                LOGGER.info("   ho.orderKey=" + item[0]);
                LOGGER.info("   hos.id=" + item[1]);
                LOGGER.info("   hos.jobName=" + item[2]);
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

    /* Native SQL Queries */

    @Ignore
    @Test
    public void testNativeJoinWithCustomEntity() throws Exception {
        SOSHibernateSession session = null;
        try {
            session = factory.openStatelessSession();

            StringBuilder sql = new StringBuilder("select ");
            sql.append(factory.quoteColumn("ho.ORDER_KEY")).append(" as orderKey "); // quote columns and set aliases for all properties
            sql.append(",").append(factory.quoteColumn("hos.ID")).append(" as stepId ");
            sql.append(",").append(factory.quoteColumn("hos.JOB_NAME")).append(" as jobName ");
            sql.append("from " + DBLayer.TABLE_HISTORY_ORDERS).append(" ho ");
            sql.append(",").append(DBLayer.TABLE_HISTORY_ORDER_STEPS).append(" hos ");
            sql.append("where ");
            sql.append(factory.quoteColumn("ho.ID")).append("=").append(factory.quoteColumn("hos.ORDER_ID"));

            NativeQuery<MyJoinEntity> query = session.createNativeQuery(sql.toString(), MyJoinEntity.class); // pass MyJoinEntity as resultType
            query.addScalar("orderKey", StringType.INSTANCE); // map column value to property type
            query.addScalar("stepId", LongType.INSTANCE);
            query.addScalar("jobName", StringType.INSTANCE);

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

}
