package com.sos.jobscheduler.history.master;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.db.DBItemJobSchedulerLogs;
import com.sos.jobscheduler.db.DBLayer;

public class HistoryDBTest {

    public SOSHibernateFactory createFactory(String schedulerId, Path configFile, boolean autoCommit) throws Exception {
        SOSHibernateFactory factory = new SOSHibernateFactory(configFile);
        factory.setIdentifier("history");
        factory.setAutoCommit(autoCommit);
        factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        factory.addClassMapping(DBLayer.getHistoryClassMapping());
        factory.build();
        return factory;
    }

    public void insertDBItem(SOSHibernateSession session, int repeatCounter) throws SOSHibernateException {
        for (int i = 0; i < repeatCounter; i++) {
            DBItemJobSchedulerLogs l = new DBItemJobSchedulerLogs();
            l.setSchedulerId("x");
            l.setOrderKey("xx");
            l.setMainOrderHistoryId(new Long(0));
            l.setOrderHistoryId(new Long(0));
            l.setOrderStepHistoryId(new Long(0));
            l.setLogType(new Long(0));
            l.setLogLevel(new Long(0));
            l.setOutType(new Long(0));
            l.setEventId("1234567891234567");
            l.setJobPath(".");
            l.setAgentUri(".");
            l.setTimezone(".");
            l.setChunkTimestamp(new Date());
            l.setChunk("x");
            l.setConstraintHash(String.valueOf(new Date().getTime() + i));
            l.setCreated(new Date());
            session.save(l);
        }
    }

    public void executeUpdateFullTable(SOSHibernateSession session, int repeatCounter) throws Exception {
        for (int i = 0; i < repeatCounter; i++) {
            StringBuilder sql = new StringBuilder();
            sql.append("insert ");
            sql.append("into SCHEDULER_LOGS");
            sql.append(
                    "(`AGENT_URI`, `CHUNK`,  `CHUNK_TIMESTAMP`, `CONSTRAINT_HASH`, `CREATED`, `EVENT_ID`, `JOB_PATH`, `LOG_LEVEL`, `LOG_TYPE`, `MAIN_ORDER_HISTORY_ID`, `ORDER_HISTORY_ID`, `ORDER_KEY`, `ORDER_STEP_HISTORY_ID`, `OUT_TYPE`, `SCHEDULER_ID`, `TIMEZONE`)");
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
            sql.append("insert ");
            sql.append("into SCHEDULER_LOGS");
            sql.append(
                    "(`AGENT_URI`, `CHUNK`,  `CHUNK_TIMESTAMP`, `CONSTRAINT_HASH`, `CREATED`, `EVENT_ID`, `JOB_PATH`, `LOG_LEVEL`, `LOG_TYPE`, `MAIN_ORDER_HISTORY_ID`, `ORDER_HISTORY_ID`, `ORDER_KEY`, `ORDER_STEP_HISTORY_ID`, `OUT_TYPE`, `SCHEDULER_ID`, `TIMEZONE`)");
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
            sql.append("insert ");
            sql.append("into SCHEDULER_LOGS");
            sql.append("(`SCHEDULER_ID`)");
            sql.append("values ");
            sql.append(" ('.')");
            session.getSQLExecutor().executeUpdate(sql.toString());
        }
    }

    public void executeBatchShortTable(SOSHibernateSession session, int repeatCounter) throws Exception {
        ArrayList<String> sqls = new ArrayList<String>();
        for (int i = 0; i < repeatCounter; i++) {
            StringBuilder sql = new StringBuilder();
            sql.append("insert ");
            sql.append("into SCHEDULER_LOGS");
            sql.append("(`SCHEDULER_ID`)");
            sql.append("values ");
            sql.append(" ('.')");
            sqls.add(sql.toString());
        }
        session.getSQLExecutor().executeBatch(sqls);
    }

    
    public static void main(String[] args) throws Exception {
        HistoryDBTest t = new HistoryDBTest();
        String schedulerId = "jobscheduler2";
        Path hibernateConfigFile = Paths.get("src/test/resources/hibernate.cfg.xml");
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;

        try {
            boolean autoCommit = false;

            factory = t.createFactory(schedulerId, hibernateConfigFile, autoCommit);
            session = factory.openStatelessSession();

            for (int i = 0; i < 2; i++) {
                session.beginTransaction();

                t.insertDBItem(session, 10);

                // t.executeUpdateFullTable(session, 10);
                // t.executeBatchFullTable(session, 10);

                // t.executeUpdateShortTable(session, 10);
                // t.executeBatchShortTable(session, 10);

                session.commit();
            }
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
