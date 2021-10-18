package com.sos.commons.hibernate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

import org.hibernate.ScrollableResults;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.history.DBItemHistoryOrderStep;

public class SOSHibernateTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateTest.class);

    /* HQL Queries */

    @Ignore
    @Test
    public void test() throws IOException {
        String s = SOSPath.readFile(Paths.get("my_file.txt"), StandardCharsets.UTF_8);
        String n = DBItemHistoryOrderStep.normalizeErrorText(s);
        LOGGER.info(n.length() + ":" + n);
    }

    @Ignore
    @Test
    public void testJoinWithCustomEntity() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = createFactory();
            session = factory.openStatelessSession();

            StringBuilder hql = new StringBuilder("select ho.orderId as orderId "); // set aliases for all properties
            hql.append(",hos.id as stepId,hos.jobName as jobName ");
            hql.append("from " + DBLayer.DBITEM_HISTORY_ORDERS).append(" ho ");
            hql.append(",").append(DBLayer.DBITEM_HISTORY_ORDER_STEPS).append(" hos ");
            hql.append("where ho.id=hos.historyOrderId ");

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
            if (factory != null) {
                factory.close();
            }
        }
    }

    @Ignore
    @Test
    public void testJoinWithoutEntity() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = createFactory();
            session = factory.openStatelessSession();

            StringBuilder hql = new StringBuilder("select ho.orderId ");
            hql.append(",hos.id, hos.jobName ");
            hql.append("from " + DBLayer.DBITEM_HISTORY_ORDERS).append(" ho ");
            hql.append(",").append(DBLayer.DBITEM_HISTORY_ORDER_STEPS).append(" hos ");
            hql.append("where ho.id=hos.historyOrderId ");

            Query<Object[]> query = session.createQuery(hql.toString());
            query.setMaxResults(10); // only for this test
            List<Object[]> result = session.getResultList(query);
            for (Object[] item : result) {
                LOGGER.info(SOSHibernate.toString(item));
                LOGGER.info("   ho.orderId=" + item[0]);
                LOGGER.info("   hos.id=" + item[1]);
                LOGGER.info("   hos.jobName=" + item[2]);
            }

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
    public void testScroll() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        ScrollableResults sr = null;
        try {
            factory = createFactory();
            session = factory.openStatelessSession();

            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_ORDER_STEPS);
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
            if (factory != null) {
                factory.close();
            }
        }
    }

    /* Native SQL Queries */

    @Ignore
    @Test
    public void testNativeJoinWithCustomEntity() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = createFactory();
            session = factory.openStatelessSession();

            StringBuilder sql = new StringBuilder("select ");
            sql.append(factory.quoteColumn("ho.ORDER_ID")).append(" as orderId "); // quote columns and set aliases for all properties
            sql.append(",").append(factory.quoteColumn("hos.ID")).append(" as stepId ");
            sql.append(",").append(factory.quoteColumn("hos.JOB_NAME")).append(" as jobName ");
            sql.append("from " + DBLayer.TABLE_HISTORY_ORDERS).append(" ho ");
            sql.append(",").append(DBLayer.TABLE_HISTORY_ORDER_STEPS).append(" hos ");
            sql.append("where ");
            sql.append(factory.quoteColumn("ho.ID")).append("=").append(factory.quoteColumn("hos.HO_ID"));

            NativeQuery<MyJoinEntity> query = session.createNativeQuery(sql.toString(), MyJoinEntity.class); // pass MyJoinEntity as resultType
            query.addScalar("orderId", StringType.INSTANCE); // map column value to property type
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
            if (factory != null) {
                factory.close();
            }
        }
    }

    private SOSHibernateFactory createFactory() throws Exception {
        SOSHibernateFactory factory = new SOSHibernateFactory(Paths.get("src/test/resources/hibernate.cfg.xml"));
        factory.addClassMapping(DBLayer.getHistoryClassMapping());
        factory.build();
        return factory;
    }

}
