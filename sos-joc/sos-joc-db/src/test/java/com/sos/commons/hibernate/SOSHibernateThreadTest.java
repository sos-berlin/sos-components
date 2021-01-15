package com.sos.commons.hibernate;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.hibernate.query.Query;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory.Dbms;
import com.sos.commons.hibernate.common.SOSHibernateThreadFactory;

public class SOSHibernateThreadTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateThreadTest.class);

    private static final int THREADS = 5;
    private static final int MAX_POOL_SIZE = 1;

    private static final int WAIT_SECONDS = 40;

    private String statement = null;

    @Ignore
    @Test
    public void testCreateThreadsWithNewSession() throws Exception {
        SOSHibernateFactory factory = null;
        try {
            factory = createFactory();
            setStatement(factory);

            for (int i = 1; i <= THREADS; i++) {
                Thread thread = createThreadWithNewSession(factory);
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
            factory = createFactory();
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
            if (session != null) {
                session.close();
            }
            if (factory != null) {
                factory.close();
            }
        }
    }

    private Thread createThreadWithNewSession(SOSHibernateFactory factory) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                String name = Thread.currentThread().getName();
                LOGGER.info(String.format("[%s]start", name));
                executeStatement(factory, name);
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

    private void executeStatement(SOSHibernateFactory factory, String threadName) {
        SOSHibernateSession session = null;
        try {
            session = factory.openStatelessSession();

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
            Query<?> query = session.createNativeQuery(statement);
            LOGGER.info("[" + threadName + "][first]" + SOSHibernate.toString(session.getSingleResult(query)));
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

    private SOSHibernateFactory createFactory() throws Exception {
        SOSHibernateFactory factory = new SOSHibernateThreadFactory(Paths.get("src/test/resources/hibernate.cfg.xml"), 1, MAX_POOL_SIZE);
        factory.build();

        return factory;
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
