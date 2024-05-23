package com.sos.commons.hibernate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import org.hibernate.ScrollableResults;
import org.hibernate.query.Query;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.encipherment.DBItemEncAgentCertificate;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.db.inventory.DBItemInventoryTag;

/** HQL Tests<br/>
 * NativeQueries - see SOSHibernateNativeQueryTest<br/>
 */
public class SOSHibernateTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateTest.class);

    @Ignore
    @Test
    public void testGetResultList() throws Exception {
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
    public void testGet() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = createFactory();
            session = factory.openStatelessSession();

            Long id = session.getSingleValue("select min(id) from " + DBLayer.DBITEM_HISTORY_ORDERS);
            if (id == null) {
                LOGGER.info("not found");
            } else {
                DBItemHistoryOrder item = session.get(DBItemHistoryOrder.class, id);
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

            StringBuilder hql = new StringBuilder("select ");
            hql.append("ho.orderId as orderId "); // set aliases for all properties
            hql.append(",0 as numberValue");
            hql.append(",hos.id as stepId");
            hql.append(",hos.jobName as jobName ");
            hql.append("from " + DBLayer.DBITEM_HISTORY_ORDERS).append(" ho ");
            hql.append(",").append(DBLayer.DBITEM_HISTORY_ORDER_STEPS).append(" hos ");
            hql.append("where ho.id=hos.historyOrderId ");

            Query<MyJoinEntity> query = session.createQuery(hql.toString(), MyJoinEntity.class); // pass MyJoinEntity as resultType

            query.setMaxResults(10); // only for this test
            List<MyJoinEntity> result = session.getResultList(query);
            for (MyJoinEntity item : result) {
                LOGGER.info(SOSString.toString(item));
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
    public void testEqualsAndSetId() throws Exception {
        DBItemEncAgentCertificate i1 = new DBItemEncAgentCertificate();
        i1.setAgentId("agent_1");
        i1.setCertAlias("alias_1");

        DBItemEncAgentCertificate i2 = new DBItemEncAgentCertificate();
        i2.setAgentId("agent_1");
        i2.setCertAlias("alias_2");
        LOGGER.info("equals=" + i1.equals(i2));

        Object[] id = new Object[2];
        id[0] = "1";
        id[1] = "2";
        DBItemEncAgentCertificate i3 = new DBItemEncAgentCertificate();
        SOSHibernate.setId(i3, id);
        LOGGER.info("i3=" + SOSHibernate.toString(i3));
    }

    @Ignore
    @Test
    public void testInsertUpdateDeleteItem() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = createFactory();
            session = factory.openStatelessSession();

            DBItemInventoryTag t = new DBItemInventoryTag();
            t.setName(new Date().toString());
            t.setOrdering(Integer.valueOf(1));
            t.setModified(new Date());

            session.beginTransaction();
            session.save(t);

            t.setOrdering(Integer.valueOf(2));
            session.update(t);

            session.delete(t);

            session.commit();
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
    public void testDelete() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = createFactory();
            session = factory.openStatelessSession();

            session.beginTransaction();
            session.executeUpdate("delete from " + DBLayer.DBITEM_HISTORY_ORDER_STEPS + " where jobName='YxxxxYxxxx'");
            session.commit();
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
    public void testNormalizeValueLen() throws IOException {
        String s = SOSPath.readFile(Paths.get("my_file.txt"), StandardCharsets.UTF_8);
        String n = DBItemHistoryOrderStep.normalizeErrorText(s);
        LOGGER.info(n.length() + ":" + n);
    }

    public static SOSHibernateFactory createFactory() throws Exception {
        // System.setProperty("java.util.logging.config.file", Paths.get("src/test/resources/mssql/logging.properties").toString());
        Path hibernateFile = Paths.get("src/test/resources/hibernate.cfg.mysql.xml");
        tryDoInsertIfH2(hibernateFile, false);

        SOSHibernateFactory factory = new SOSHibernateFactory(hibernateFile);
        // factory.addClassMapping(DBItemInventoryTag.class);
        factory.addClassMapping(DBLayer.getJocClassMapping());
        // factory.setAutoCommit(true);
        factory.build(true);
        LOGGER.info("DBMS=" + factory.getDbms() + ", DIALECT=" + factory.getDialect());
        return factory;
    }

    /** one-time execution</br>
     * when SOSHibernateFileProcessor.main is called, the call ends with System.exit() - so, only insert is executed<br />
     * handle this behavior by setting insert=true|false */
    private static void tryDoInsertIfH2(Path hibernateFile, boolean insert) throws Exception {
        if (insert) {
            String hf = hibernateFile.toAbsolutePath().toString();
            if (hf.endsWith("h2.xml")) {
                Path h2InsertFile = Paths.get("src/test/resources/h2/insert.sql");
                SOSHibernateFileProcessor.main(new String[] { hf, h2InsertFile.toAbsolutePath().toString() });
            }
        }
    }

}
