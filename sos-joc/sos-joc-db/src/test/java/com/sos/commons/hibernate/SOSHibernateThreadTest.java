package com.sos.commons.hibernate;

import java.util.concurrent.TimeUnit;

import org.hibernate.query.Query;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory.Dbms;

public class SOSHibernateThreadTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateThreadTest.class);

    private static final int THREADS = 5;
    private static final int WAIT_SECONDS = 40;
    // false - session.createNativeQuery, true - session.getSQLExecutor().execute
    private static final boolean USE_SQL_EXECUTER = false;

    private String statement = null;

    @Ignore
    @Test
    public void testCreateThreadsWithNewSession() throws Exception {
        SOSHibernateFactory factory = null;
        try {
            factory = SOSHibernateTest.createFactory();
            setStatement(factory);

            for (int i = 1; i <= THREADS; i++) {
                Boolean autoCommit = i % 2 == 0;
                // autoCommit = null;
                Thread thread = createThreadWithNewSession(factory, autoCommit);
                thread.start();
            }

            TimeUnit.SECONDS.sleep(WAIT_SECONDS);

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
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = SOSHibernateTest.createFactory();
            setStatement(factory);
            session = factory.openStatelessSession();

            for (int i = 1; i <= THREADS; i++) {
                Thread thread = createThreadWithOneSession(session);
                thread.start();
            }

            TimeUnit.SECONDS.sleep(WAIT_SECONDS);
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
            if (autoCommit != null) {
                session.setAutoCommit(autoCommit);
            }
            executeStatement(session, threadName);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private void executeStatement(SOSHibernateSession session, String threadName) {
        try {
            if (USE_SQL_EXECUTER) {
                session.getSQLExecutor().execute(statement);
            } else {
                Query<?> query = session.createNativeQuery(statement);
                LOGGER.info("[" + threadName + "][first]" + SOSHibernate.toString(session.getSingleResult(query)));
            }
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

    private void setStatement(SOSHibernateFactory factory) {
        if (Dbms.MYSQL.equals(factory.getDbms())) {
            statement = "select sleep(5)";
        } else if (Dbms.MSSQL.equals(factory.getDbms())) {
            statement = "waitfor delay '00:00:05'; select 1";
        } else { // TODO
            statement = "select 1";
        }
    }

}
