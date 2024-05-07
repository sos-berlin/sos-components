package com.sos.commons.hibernate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

import org.hibernate.ScrollableResults;
import org.hibernate.query.Query;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.db.history.DBItemHistoryOrderStep;

/** HQL Tests<br/>
 * NativeQueries - see SOSHibernateNativeQueryTest<br/>
 */
public class SOSHibernateTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateTest.class);

    @Ignore
    @Test
    public void testEntity() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = createFactory();
            session = factory.openStatelessSession();

            StringBuilder hql = new StringBuilder("from " + DBLayer.DBITEM_HISTORY_ORDERS);
            Query<DBItemHistoryOrder> query = session.createQuery(hql.toString());

            query.setMaxResults(10); // only for this test
            List<DBItemHistoryOrder> result = session.getResultList(query);
            for (DBItemHistoryOrder item : result) {
                LOGGER.info(SOSHibernate.toString(item));
            }

        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            throw e;
        } finally {
            if (factory != null) {
                factory.close(session);
            }
        }
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
            LOGGER.error(e.toString(), e);
            throw e;
        } finally {
            if (factory != null) {
                factory.close(session);
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
            if (factory != null) {
                factory.close(session);
            }
        }
    }

    @Ignore
    @Test
    public void testScroll() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        ScrollableResults<DBItemHistoryOrderStep> sr = null;
        try {
            factory = createFactory();
            session = factory.openStatelessSession();

            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_ORDER_STEPS);
            Query<DBItemHistoryOrderStep> query = session.createQuery(hql.toString());
            query.setMaxResults(10); // only for this test

            sr = session.scroll(query);
            while (sr.next()) {
                DBItemHistoryOrderStep step = sr.get();
                LOGGER.info(SOSHibernate.toString(step));
            }

        } catch (Exception e) {
            throw e;
        } finally {
            if (sr != null) { // close ScrollableResults
                sr.close();
            }
            if (factory != null) {
                factory.close(session);
            }
        }
    }

    @Ignore
    @Test
    public void testNormalizeValueLen() throws IOException {
        String s = SOSPath.readFile(Paths.get("my_file.txt"), StandardCharsets.UTF_8);
        String n = DBItemHistoryOrderStep.normalizeErrorText(s);
        LOGGER.info(n.length() + ":" + n);
    }

    public static SOSHibernateFactory createFactory() throws Exception {
        SOSHibernateFactory factory = new SOSHibernateFactory(Paths.get("src/test/resources/hibernate.cfg.mysql.xml"));
        factory.addClassMapping(DBLayer.getJocClassMapping());
        factory.build();
        LOGGER.info("DBMS=" + factory.getDbms() + ", DIALECT=" + factory.getDialect());
        return factory;
    }

}
