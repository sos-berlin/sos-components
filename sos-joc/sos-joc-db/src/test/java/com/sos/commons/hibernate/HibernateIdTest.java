package com.sos.commons.hibernate;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Date;

import org.hibernate.query.Query;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.function.date.SOSHibernateCurrentTimestampUtc;
import com.sos.commons.hibernate.helpers.dbitems.DBItemATest;
import com.sos.commons.util.SOSClassList;
import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.history.DBItemHistoryOrder;

public class HibernateIdTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateIdTest.class);

    @Ignore
    @Test
    public void testInsert() throws Exception {

        SOSClassList mapping = new SOSClassList();
        mapping.add(DBItemATest.class);

        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = SOSHibernateTest.createFactory(mapping);
            session = factory.openStatelessSession();

            DBItemATest item = new DBItemATest();
            item.setName("xxxx");
            item.setJavaDateManual(LocalDateTime.now(Clock.systemUTC()));

            session.beginTransaction();
            session.save(item);

            item.setName("xxxx-updated");

            // set DateNullable with current UTC timestamp
            // see explanation below : ... item.setDateNullable(item.getDbCurrentTimestampUtcAuto());
            item.setDateNullable(session.getCurrentTimestampAsLocalDateTime());

            session.update(item);
            LOGGER.info("[AFTER_UPDATE]" + SOSString.toString(item));

            StringBuilder hql = new StringBuilder();
            hql.append("update ").append(DBItemATest.class.getSimpleName()).append(" ");
            hql.append("set dbCurrentTimestampUtcAuto=").append(SOSHibernateCurrentTimestampUtc.getFunction()).append(" ");
            hql.append("where id = (select min(id) from ").append(DBItemATest.class.getSimpleName()).append(")");
            Query<?> query = session.createQuery(hql);

            session.executeUpdate(query);

            // Does NOT work with StatelessSession - getDbCurrentTimestampUtcAuto remains null after save because StatelessSession has no persistence context
            // and does not update the
            // entity.
            // item.setDateNullable(item.getDbCurrentTimestampUtcAuto());
            // session.update(item);

            session.commit();

        } catch (Throwable e) {
            if (session != null) {
                session.rollback();
            }
            LOGGER.error(e.toString(), e);
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
    public void testInsertHistoryOrders() throws Exception {

        SOSClassList mapping = DBLayer.getHistoryClassMapping();

        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = SOSHibernateTest.createFactory(mapping);
            session = factory.openStatelessSession();

            DBItemHistoryOrder item = new DBItemHistoryOrder();
            item.setControllerId("js7.x");
            item.setOrderId("re-test");
            item.setWorkflowPath("/test");
            item.setWorkflowVersionId("1");
            item.setWorkflowPosition("0/try+0:0");
            item.setWorkflowFolder("/");
            item.setWorkflowName("test");
            item.setMainParentId(0L);
            item.setParentId(0L);
            item.setParentOrderId(".");
            item.setHasChildren(false);
            item.setRetryCounter(0);
            item.setName("re-test");
            item.setStartCause("order");
            item.setStartTimeScheduled(new Date());
            item.setStartTime(new Date());
            item.setStartWorkflowPosition("0");
            item.setStartEventId(1775837409816001L);
            item.setCurrentHistoryOrderStepId(0L);
            item.setEndHistoryOrderStepId(0L);
            item.setSeverity(1);
            item.setState(2);
            item.setStateTime(new Date());
            item.setHasStates(false);
            item.setLogId(0L);
            item.setConstraintHash("54c2914b229acc864073658dfa8b8b01c958d62254bddcc7c91f0303de0f19b6");

            session.beginTransaction();
            session.save(item);
            session.delete(item);
            session.commit();

        } catch (Throwable e) {
            if (session != null) {
                session.rollback();
            }
            LOGGER.error(e.toString(), e);
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
    public void testPreparedStatement() throws Exception {
        SOSClassList mapping = new SOSClassList();

        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = SOSHibernateTest.createFactory(mapping);
            session = factory.openStatelessSession();

            session.beginTransaction();
            String sql =
                    "INSERT INTO A_TEST (ID, JAVA_DATE_MANUAL, NAME, JAVA_DATE_AUTO, DB_CURRENT_TIMESTAMP_AUTO, DB_CURRENT_TIMESTAMP_UTC_AUTO) VALUES (?, sysdate, ?, sysdate, sysdate, sysdate)";
            try (PreparedStatement stmt = session.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, 103L);
                stmt.setString(2, "test");
                stmt.executeUpdate();
            }
            session.commit();

        } catch (Throwable e) {
            if (session != null) {
                session.rollback();
            }
            LOGGER.error(e.toString(), e);
        } finally {
            if (session != null) {
                session.close();
            }
            if (factory != null) {
                factory.close();
            }
        }
    }

}
