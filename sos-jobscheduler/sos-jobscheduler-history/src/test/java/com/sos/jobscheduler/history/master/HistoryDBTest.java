package com.sos.jobscheduler.history.master;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.db.history.DBItemLog;

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

    public void insertDBItem(SOSHibernateSession session, int repeatCounter) throws SOSHibernateException {
        for (int i = 0; i < repeatCounter; i++) {
            DBItemLog l = new DBItemLog();
            l.setMasterId("x");
            l.setOrderKey("xx");
            l.setMainOrderId(new Long(0));
            l.setOrderId(new Long(0));
            l.setOrderStepId(new Long(0));
            l.setLogType(new Long(0));
            l.setLogLevel(new Long(0));
            l.setOutType(new Long(0));
            l.setEventId("1234567891234567");
            l.setJobName(".");
            l.setAgentUri(".");
            l.setTimezone(".");
            l.setChunkDatetime(new Date());
            l.setChunk("x");
            l.setConstraintHash(String.valueOf(new Date().getTime() + i));
            l.setCreated(new Date());
            session.save(l);
        }
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

            Map<String, Object> fields = HistoryThreadTest.getUniqueConstraintFields(agent);
            HistoryThreadTest.insertObject(session, agent.getClass(), agent, fields);

            // session.save(agent);

            // t.insertDBItem(session, 10);

            // t.executeUpdateFullTable(session, 10);
            // t.executeBatchFullTable(session, 10);

            // t.executeUpdateShortTable(session, 10);
            // t.executeBatchShortTable(session, 10);

            session.commit();
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

            String hql = "from DBItemAgentTest";
            Query<DBItemAgentTest> query = session.createQuery(hql);
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
