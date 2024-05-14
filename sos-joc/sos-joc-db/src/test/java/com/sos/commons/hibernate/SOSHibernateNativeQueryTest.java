package com.sos.commons.hibernate;

import java.util.List;
import java.util.Map;

import org.hibernate.query.NativeQuery;
import org.hibernate.type.StandardBasicTypes;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBLayer;

public class SOSHibernateNativeQueryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateNativeQueryTest.class);

    /* Native SQL Queries */

    @Ignore
    @Test
    public void testNativeSimpleList() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = SOSHibernateTest.createFactory();
            session = factory.openStatelessSession();

            StringBuilder sql = new StringBuilder("select ");
            sql.append(factory.quoteColumn("ho.ORDER_ID"));
            sql.append(",").append(factory.quoteColumn("hos.ID"));
            sql.append(",").append(factory.quoteColumn("hos.JOB_NAME"));
            sql.append(",").append(factory.quoteColumn("hos.CREATED")).append(" ");
            sql.append("from " + DBLayer.TABLE_HISTORY_ORDERS).append(" ho ");
            sql.append(",").append(DBLayer.TABLE_HISTORY_ORDER_STEPS).append(" hos ");
            sql.append("where ");
            sql.append(factory.quoteColumn("ho.ID")).append("=").append(factory.quoteColumn("hos.HO_ID"));

            NativeQuery<?> query = session.createNativeQuery(sql.toString());
            query.setMaxResults(5); // only for this test

            LOGGER.info("[getResultListAsMaps]-----------------------]");
            int i = 0;
            List<Map<String, Object>> result1 = session.getResultListAsMaps(query);
            for (Map<String, Object> r : result1) {
                i++;
                LOGGER.info("-" + i + ")----------");
                r.entrySet().stream().forEach(e -> {
                    LOGGER.info("    [" + e.getKey() + "][" + (e.getValue() == null ? "" : e.getValue().getClass()) + "]" + e.getValue());
                });
            }

            LOGGER.info("[getResultListAsStringMaps]-----------------------]");
            i = 0;
            List<Map<String, String>> result2 = session.getResultListAsStringMaps(query);
            for (Map<String, String> r : result2) {
                i++;
                LOGGER.info("-" + i + ")----------");
                r.entrySet().stream().forEach(e -> {
                    LOGGER.info("    [" + e.getKey() + "][" + (e.getValue() == null ? "" : e.getValue().getClass()) + "]" + e.getValue());
                });
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
    public void testNativeJoinWithCustomEntity() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = SOSHibernateTest.createFactory();
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
            query.addScalar("orderId", StandardBasicTypes.STRING); // map column value to property type
            query.addScalar("stepId", StandardBasicTypes.LONG);
            query.addScalar("jobName", StandardBasicTypes.STRING);

            query.setMaxResults(10); // only for this test
            List<MyJoinEntity> result = session.getResultList(query);
            for (MyJoinEntity item : result) {
                LOGGER.info(SOSString.toString(item));
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
    public void testNativeJoinWithoutCustomEntity() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = SOSHibernateTest.createFactory();
            session = factory.openStatelessSession();

            StringBuilder sql = new StringBuilder("select ");
            sql.append(factory.quoteColumn("ho.ORDER_ID"));
            sql.append(",").append(factory.quoteColumn("hos.ID"));
            sql.append(",").append(factory.quoteColumn("hos.JOB_NAME")).append(" ");
            sql.append("from " + DBLayer.TABLE_HISTORY_ORDERS).append(" ho ");
            sql.append(",").append(DBLayer.TABLE_HISTORY_ORDER_STEPS).append(" hos ");
            sql.append("where ");
            sql.append(factory.quoteColumn("ho.ID")).append("=").append(factory.quoteColumn("hos.HO_ID"));

            NativeQuery<Object[]> query = session.createNativeQuery(sql.toString());
            query.setMaxResults(10); // only for this test
            List<Object[]> result = session.getResultList(query);
            for (Object[] item : result) {
                LOGGER.info(SOSHibernate.toString(item));
                LOGGER.info("   ho.ORDER_ID=" + item[0]);
                LOGGER.info("   hos.ID=" + item[1]);
                LOGGER.info("   hos.JOB_NAME=" + item[2]);
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
    public void testNativeGetSingleResultAsMap() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = SOSHibernateTest.createFactory();
            session = factory.openStatelessSession();

            StringBuilder sql = new StringBuilder("select ");
            sql.append(factory.quoteColumn("ho.ORDER_ID"));
            sql.append(",").append(factory.quoteColumn("hos.ID"));
            sql.append(",").append(factory.quoteColumn("hos.JOB_NAME"));
            sql.append(",").append(factory.quoteColumn("hos.CREATED")).append(" ");
            sql.append("from " + DBLayer.TABLE_HISTORY_ORDERS).append(" ho ");
            sql.append(",").append(DBLayer.TABLE_HISTORY_ORDER_STEPS).append(" hos ");
            sql.append("where ");
            sql.append(factory.quoteColumn("ho.ID")).append("=").append(factory.quoteColumn("hos.HO_ID"));

            NativeQuery<?> query = session.createNativeQuery(sql.toString());
            query.setMaxResults(1); // only for this test
            Map<String, Object> result1 = session.getSingleResultAsMap(query);
            LOGGER.info("[getSingleResultAsMap]-----------------------]");
            result1.entrySet().stream().forEach(e -> {
                LOGGER.info("[" + e.getKey() + "][" + (e.getValue() == null ? "" : e.getValue().getClass()) + "]" + e.getValue());
            });

            LOGGER.info("[getSingleResultAsStringMap]-----------------------]");
            Map<String, String> result2 = session.getSingleResultAsStringMap(query);// (query,"YYYY");
            result2.entrySet().stream().forEach(e -> {
                LOGGER.info("[" + e.getKey() + "][" + (e.getValue() == null ? "" : e.getValue().getClass()) + "]" + e.getValue());
            });

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
    public void testNativeGetResultListAsMaps() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = SOSHibernateTest.createFactory();
            session = factory.openStatelessSession();

            StringBuilder sql = new StringBuilder("select ");
            sql.append(factory.quoteColumn("ho.ORDER_ID"));
            sql.append(",").append(factory.quoteColumn("hos.ID"));
            sql.append(",").append(factory.quoteColumn("hos.JOB_NAME"));
            sql.append(",").append(factory.quoteColumn("hos.CREATED")).append(" ");
            sql.append("from " + DBLayer.TABLE_HISTORY_ORDERS).append(" ho ");
            sql.append(",").append(DBLayer.TABLE_HISTORY_ORDER_STEPS).append(" hos ");
            sql.append("where ");
            sql.append(factory.quoteColumn("ho.ID")).append("=").append(factory.quoteColumn("hos.HO_ID"));

            NativeQuery<?> query = session.createNativeQuery(sql.toString());
            query.setMaxResults(5); // only for this test

            LOGGER.info("[getResultListAsMaps]-----------------------]");
            int i = 0;
            List<Map<String, Object>> result1 = session.getResultListAsMaps(query);
            for (Map<String, Object> r : result1) {
                i++;
                LOGGER.info("-" + i + ")----------");
                r.entrySet().stream().forEach(e -> {
                    LOGGER.info("    [" + e.getKey() + "][" + (e.getValue() == null ? "" : e.getValue().getClass()) + "]" + e.getValue());
                });
            }

            LOGGER.info("[getResultListAsStringMaps]-----------------------]");
            i = 0;
            List<Map<String, String>> result2 = session.getResultListAsStringMaps(query);
            for (Map<String, String> r : result2) {
                i++;
                LOGGER.info("-" + i + ")----------");
                r.entrySet().stream().forEach(e -> {
                    LOGGER.info("    [" + e.getKey() + "][" + (e.getValue() == null ? "" : e.getValue().getClass()) + "]" + e.getValue());
                });
            }

        } catch (Exception e) {
            throw e;
        } finally {
            if (factory != null) {
                factory.close(session);
            }
        }
    }

}
