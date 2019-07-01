package com.sos.jobscheduler.history.db;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.db.DBLayer;

public class HistoryDBTest {

    public SOSHibernateFactory createFactory(String masterId, Path configFile, boolean autoCommit) throws Exception {
        SOSHibernateFactory factory = new SOSHibernateFactory(configFile);
        factory.setIdentifier("history");
        factory.setAutoCommit(autoCommit);
        factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        factory.addClassMapping(DBLayer.getHistoryClassMapping());
        factory.addClassMapping(DBItemAgentTest.class);
        factory.build();
        return factory;
    }

    public void executeUpdateFullTable(SOSHibernateSession session, int repeatCounter) throws Exception {
        for (int i = 0; i < repeatCounter; i++) {
            StringBuilder sql = new StringBuilder();
            sql.append("insert into ");
            sql.append(DBLayer.HISTORY_TABLE_LOGS);
            sql.append(
                    "(`AGENT_URI`, `CHUNK`,  `CHUNK_TIMESTAMP`, `CONSTRAINT_HASH`, `CREATED`, `EVENT_ID`, `JOB_PATH`, `LOG_LEVEL`, `LOG_TYPE`, `MAIN_ORDER_HISTORY_ID`, `ORDER_HISTORY_ID`, `ORDER_KEY`, `ORDER_STEP_HISTORY_ID`, `OUT_TYPE`, `MASTER_ID`, `TIMEZONE`)");
            sql.append("values ");
            sql.append("('.','x','2018-06-21 16:43:12.727','" + String.valueOf(new Date().getTime() + i)
                    + "','2018-06-21 16:43:12.727','1234567891234567','.',0,0,0,0,'xx',0,0,'x','.')");
            session.getSQLExecutor().executeUpdate(sql.toString());
        }
    }

    public void executeBatchFullTable(SOSHibernateSession session, int repeatCounter) throws Exception {
        ArrayList<String> sqls = new ArrayList<String>();
        for (int i = 0; i < repeatCounter; i++) {
            StringBuilder sql = new StringBuilder();
            sql.append("insert into ");
            sql.append(DBLayer.HISTORY_TABLE_LOGS);
            sql.append(
                    "(`AGENT_URI`, `CHUNK`,  `CHUNK_TIMESTAMP`, `CONSTRAINT_HASH`, `CREATED`, `EVENT_ID`, `JOB_PATH`, `LOG_LEVEL`, `LOG_TYPE`, `MAIN_ORDER_HISTORY_ID`, `ORDER_HISTORY_ID`, `ORDER_KEY`, `ORDER_STEP_HISTORY_ID`, `OUT_TYPE`, `MASTER_ID`, `TIMEZONE`)");
            sql.append("values ");
            sql.append("('.','x','2018-06-21 16:43:12.727','" + String.valueOf(new Date().getTime() + i)
                    + "','2018-06-21 16:43:12.727','1234567891234567','.',0,0,0,0,'xx',0,0,'x','.')");
            sqls.add(sql.toString());
        }
        session.getSQLExecutor().executeBatch(sqls);
    }

    public void executeUpdateShortTable(SOSHibernateSession session, int repeatCounter) throws Exception {
        for (int i = 0; i < repeatCounter; i++) {
            StringBuilder sql = new StringBuilder();
            sql.append("insert into ");
            sql.append(DBLayer.HISTORY_TABLE_LOGS);
            sql.append("(`MASTER_ID`)");
            sql.append("values ");
            sql.append(" ('.')");
            session.getSQLExecutor().executeUpdate(sql.toString());
        }
    }

    public void executeBatchShortTable(SOSHibernateSession session, int repeatCounter) throws Exception {
        ArrayList<String> sqls = new ArrayList<String>();
        for (int i = 0; i < repeatCounter; i++) {
            StringBuilder sql = new StringBuilder();
            sql.append("insert into ");
            sql.append(DBLayer.HISTORY_TABLE_LOGS);
            sql.append("(`MASTER_ID`)");
            sql.append("values ");
            sql.append(" ('.')");
            sqls.add(sql.toString());
        }
        session.getSQLExecutor().executeBatch(sqls);
    }

    public void insertObjectTest(SOSHibernateSession session) throws Exception {
        for (int i = 0; i < 2; i++) {
            session.beginTransaction();

            DBItemAgentTest agent = new DBItemAgentTest();
            agent.setMasterId("jobscheduler2");
            agent.setPath("agent_4445");
            agent.setUri("http://localhost:4445");
            agent.setTimezone("Europe/Berlin");
            agent.setStartTime(new Date());
            agent.setLastEntry(false);
            agent.setEventId("xx");
            agent.setCreated(new Date());

            Map<String, Object> fields = getUniqueConstraintFields(agent);
            insertObject(session, agent.getClass(), agent, fields);

            // session.save(agent);

            // t.insertDBItem(session, 10);

            // t.executeUpdateFullTable(session, 10);
            // t.executeBatchFullTable(session, 10);

            // t.executeUpdateShortTable(session, 10);
            // t.executeBatchShortTable(session, 10);

            session.commit();
        }
    }

    public static Map<String, Object> getUniqueConstraintFields(Object o) throws Exception {
        Map<String, Object> fields = new LinkedHashMap<>();
        Class<?> clazz = o.getClass();
        Table ta = clazz.getDeclaredAnnotation(Table.class);
        if (ta == null) {
            throw new Exception(String.format("[%s]missing @Table annotation", clazz.getSimpleName()));
        }
        UniqueConstraint[] ucs = ta.uniqueConstraints();
        if (ucs == null || ucs.length == 0) {
            throw new Exception(String.format("[%s][@Table]uniqueConstraints annotation is null or empty", clazz.getSimpleName()));
        }

        for (int i = 0; i < ucs.length; i++) {
            UniqueConstraint uc = ucs[i];
            String[] columnNames = uc.columnNames();
            if (columnNames == null || columnNames.length == 0) {
                throw new Exception(String.format("[%s][@Table][uniqueConstraints @UniqueConstraint]columnNames annotation is null or empty", clazz
                        .getSimpleName()));
            }
            for (int j = 0; j < columnNames.length; j++) {
                String columnName = columnNames[j];
                Optional<Field> of = Arrays.stream(clazz.getDeclaredFields()).filter(m -> m.isAnnotationPresent(Column.class) && m.getAnnotation(
                        Column.class).name().equals(columnName)).findFirst();
                if (of.isPresent()) {
                    Field field = of.get();
                    field.setAccessible(true);
                    fields.put(field.getName(), field.get(o));
                } else {
                    throw new Exception(String.format("[%s][@Table][uniqueConstraints @UniqueConstraint]can't find %s annoted field", clazz
                            .getSimpleName(), columnName));
                }
            }
        }

        return fields.size() == 0 ? null : fields;
    }

    public static void insertObject(SOSHibernateSession session, Class<?> clazz, Object o, Map<String, Object> fields) throws Exception {

        // SOSHibernateFactory f = new SOSHibernateFactory();
        // SOSHibernateSession s = f.openStatelessSession();

        StringBuilder hql = new StringBuilder("from ").append(clazz.getSimpleName()).append(" where ");
        int i = 0;
        int size = fields.size();
        for (String key : fields.keySet()) {
            i++;

            // check is null?
            hql.append(key).append("=:").append(key);
            if (i < size) {
                hql.append(" and ");
            }
        }

        Query<?> query = session.createQuery(hql.toString(), clazz);
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            query.setParameter(key, value);
        }

        Object result = session.getSingleResult(query);
        if (result == null) {
            session.save(o);
        } else {
            Object id = getId(result);
            setId(o, id);
            session.update(o);
        }
        System.out.println(hql);

    }

    public static Object getId(Object item) throws Exception {
        if (item != null) {
            Optional<Field> of = Arrays.stream(item.getClass().getDeclaredFields()).filter(m -> m.isAnnotationPresent(Id.class)).findFirst();
            if (of.isPresent()) {
                Field field = of.get();
                field.setAccessible(true);
                return field.get(item);
            }
        }
        return null;
    }

    public static void setId(Object item, Object value) throws Exception {
        if (item != null) {
            Optional<Field> of = Arrays.stream(item.getClass().getDeclaredFields()).filter(m -> m.isAnnotationPresent(Id.class)).findFirst();
            if (of.isPresent()) {
                Field field = of.get();
                field.setAccessible(true);
                field.set(item, value);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        HistoryDBTest t = new HistoryDBTest();
        String masterId = "jobscheduler2";
        Path hibernateConfigFile = Paths.get("src/test/resources/hibernate.cfg.xml");
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;

        try {
            boolean autoCommit = false;

            factory = t.createFactory(masterId, hibernateConfigFile, autoCommit);
            session = factory.openStatelessSession();

            // String hql = "from DBItemAgentTest";
            // Query<DBItemAgentTest> query = session.createQuery(hql);
            // query.setParameter("id",new Long(0));

            session.close();
            session = null;
        } catch (Exception e) {
            if (session != null) {
                session.rollback();
            }
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

}
