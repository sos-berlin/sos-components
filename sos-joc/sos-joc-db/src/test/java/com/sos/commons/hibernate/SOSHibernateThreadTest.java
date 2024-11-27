package com.sos.commons.hibernate;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.hibernate.query.NativeQuery;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate.Dbms;

public class SOSHibernateThreadTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateThreadTest.class);

    private static final ConcurrentHashMap.KeySetView<SOSHibernateSession, Boolean> SESSIONS = ConcurrentHashMap.newKeySet();

    private static final int THREADS = 1;
    private static final int MAIN_WAIT_SECONDS = 10;
    private static final int DB_SLEEP_SECONDS = 59;
    // false - session.createNativeQuery, true - session.getSQLExecutor().execute
    private static final boolean USE_SQL_EXECUTER = false;
    private static final boolean USE_SESSION_EXECUTE_UPDATE = true;

    private String statement = null;

    @Ignore
    @Test
    public void testCreateThreadsWithNewSession() throws Exception {
        SESSIONS.clear();
        SOSHibernateFactory factory = null;
        try {
            factory = SOSHibernateTest.createFactory();
            setStatement(factory);

            for (int i = 1; i <= THREADS; i++) {
                Boolean autoCommit = i % 2 == 0;
                autoCommit = false;
                Thread thread = createThreadWithNewSession(factory, autoCommit);
                thread.start();
            }

            TimeUnit.SECONDS.sleep(MAIN_WAIT_SECONDS);
            for (SOSHibernateSession s : SESSIONS) {
                s.terminate();
            }

        } catch (Exception e) {
            throw e;
        } finally {
            if (factory != null) {
                factory.close();
            }
        }
    }

    @Ignore
    @Test
    public void testCreateThreadsWithOneSession() throws Exception {
        SESSIONS.clear();
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = SOSHibernateTest.createFactory();
            setStatement(factory);
            session = factory.openStatelessSession();
            SESSIONS.add(session);

            for (int i = 1; i <= THREADS; i++) {
                Thread thread = createThreadWithOneSession(session);
                thread.start();
            }

            TimeUnit.SECONDS.sleep(MAIN_WAIT_SECONDS);
        } catch (Exception e) {
            throw e;
        } finally {
            if (factory != null) {
                factory.close(session);
            }
        }
    }

    private Thread createThreadWithNewSession(SOSHibernateFactory factory, Boolean autoCommit) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                String name = Thread.currentThread().getName();
                LOGGER.info(String.format("[%s]start", name));
                executeStatement(factory, name, autoCommit);
                LOGGER.info(String.format("[%s]end", name));
            }
        });
        return thread;
    }

    private Thread createThreadWithOneSession(SOSHibernateSession session) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                String name = Thread.currentThread().getName();
                LOGGER.info(String.format("[%s]start", name));
                executeStatement(session, name);
                LOGGER.info(String.format("[%s]end", name));
            }
        });
        return thread;
    }

    private void executeStatement(SOSHibernateFactory factory, String threadName, Boolean autoCommit) {
        SOSHibernateSession session = null;
        try {
            session = factory.openStatelessSession();
            SESSIONS.add(session);
            if (autoCommit != null) {
                session.setAutoCommit(autoCommit);
            }
            executeStatement(session, threadName);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        } finally {
            if (session != null) {
                session.close();
                SESSIONS.remove(session);
            }
        }
    }

    private void executeStatement(SOSHibernateSession session, String threadName) {
        try {
            session.beginTransaction();
            if (USE_SQL_EXECUTER) {
                session.getSQLExecutor().execute(statement);
            } else {
                NativeQuery<?> query = session.createNativeQuery(statement);
                if (USE_SESSION_EXECUTE_UPDATE) {
                    LOGGER.info("[" + threadName + "][executeUpdate]" + SOSHibernate.toString(session.executeUpdate(query)));
                } else {
                    LOGGER.info("[" + threadName + "][getSingleResult]" + SOSHibernate.toString(session.getSingleResult(query)));
                }
            }
            session.commit();
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

    private void setStatement(SOSHibernateFactory factory) {
        if (Dbms.MYSQL.equals(factory.getDbms())) {
            statement = "select sleep(" + DB_SLEEP_SECONDS + ")";
        } else if (Dbms.MSSQL.equals(factory.getDbms())) {
            statement = "waitfor delay '00:00:" + DB_SLEEP_SECONDS + "'; select 1";
        } else if (Dbms.ORACLE.equals(factory.getDbms())) {
            // statement = "BEGIN\r\n" + "dbms_lock.sleep(" + DB_SLEEP_SECONDS + ");\r\n" + "END;";
            statement = "BEGIN dbms_lock.sleep(" + DB_SLEEP_SECONDS + "); END;";
        } else { // TODO
            statement = "SELECT pg_sleep(300)";
        }
    }

}
