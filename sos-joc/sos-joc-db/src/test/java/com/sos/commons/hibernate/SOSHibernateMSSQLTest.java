package com.sos.commons.hibernate;

import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.db.DBLayer;

/** <br />
 * https://kb.sos-berlin.com/display/JS7/JS7+-+Database<br/>
 * https://learn.microsoft.com/en-us/sql/connect/jdbc/setting-the-connection-properties?view=sql-server-2016<br/>
 * Lock timeout definition in milliseconds:<br/>
 * <property name="hibernate.connection.url">jdbc:sqlserver://<arguments>;lockTimeout=30000</property><br/>
 * -- Note: lockTimeout setting - available in MSSQL JDBC drivers (oldest verified version - sqljdbc_6.0(sqljdbc42.jar)) */
public class SOSHibernateMSSQLTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateMSSQLTest.class);

    private static final int SESSION1_LOCK_TABLE_INTERVAL_IN_SECONDS = 10;

    @Ignore
    @Test
    /** <br />
     * - session1 starts and locks a table for SESSION1_LOCK_TABLE_INTERVAL_IN_SECONDS<br/>
     * - session2 starts 2 seconds later and then<br/>
     * -- 1) Lock Timeout > SESSION1_LOCK_TABLE_INTERVAL_IN_SECONDS<br/>
     * ---- Expected result: Success<br/>
     * -- 2) Lock timeout < SESSION1_LOCK_TABLE_INTERVAL_IN_SECONDS<br/>
     * ---- Expected result: LockTimeoutException<br/>
     * 
     * @throws Exception */
    public void testLockTimeout() throws Exception {
        SOSHibernateFactory factory = null;
        try {
            factory = SOSHibernateTest.createFactory();
            Thread t1 = session1(factory);
            Thread t2 = session2(factory);

            t1.start();
            TimeUnit.SECONDS.sleep(2);
            t2.start();

            TimeUnit.SECONDS.sleep(SESSION1_LOCK_TABLE_INTERVAL_IN_SECONDS + 2);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
            throw e;
        } finally {
            if (factory != null) {
                factory.close();
            }
        }
    }

    /** Create a lock for SESSION1_LOCK_TABLE_INTERVAL_IN_SECONDS
     * 
     * @param factory
     * @return */
    private Thread session1(SOSHibernateFactory factory) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                SOSHibernateSession session = null;
                try {
                    session = factory.openStatelessSession();
                    session.beginTransaction();

                    StringBuilder sql = new StringBuilder("SELECT * FROM ").append(DBLayer.TABLE_HISTORY_ORDERS).append(" ");
                    sql.append("WITH (TABLOCKX, HOLDLOCK) ");
                    sql.append("WHERE ID=(SELECT MAX(ID) FROM ").append(DBLayer.TABLE_HISTORY_ORDERS).append(")");
                    StringBuilder sqlWait = new StringBuilder("WAITFOR DELAY '00:00:").append(SESSION1_LOCK_TABLE_INTERVAL_IN_SECONDS).append("'");

                    session.getResultListNativeQueryAsMaps(sql.toString() + " " + sqlWait.toString());
                    // session.executeUpdateNativeQuery("WAITFOR DELAY '00:00:10'");

                    session.getResultListNativeQueryAsMaps(sql.toString());

                    session.rollback();
                } catch (Throwable e) {
                    LOGGER.error(e.toString(), e);
                } finally {
                    if (session != null) {
                        session.close();
                    }
                }
            }
        });
        return thread;
    }

    /** runs in parallel (small delay) to session 1
     * 
     * @param factory
     * @return */
    private Thread session2(SOSHibernateFactory factory) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {

                SOSHibernateSession session = null;
                try {
                    session = factory.openStatelessSession();
                    session.beginTransaction();

                    StringBuilder sql = new StringBuilder("SELECT * FROM ").append(DBLayer.TABLE_HISTORY_ORDERS).append(" ");
                    sql.append("WITH (TABLOCKX, HOLDLOCK) ");
                    sql.append("WHERE ID=(SELECT MAX(ID) FROM ").append(DBLayer.TABLE_HISTORY_ORDERS).append(")");
                    session.getResultListNativeQueryAsMaps(sql.toString());

                    session.commit();
                } catch (Throwable e) {
                    LOGGER.error(e.toString(), e);
                } finally {
                    if (session != null) {
                        session.close();
                    }
                }
            }
        });
        return thread;
    }
}
