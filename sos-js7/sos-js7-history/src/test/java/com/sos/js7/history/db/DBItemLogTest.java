package com.sos.js7.history.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.Date;
import java.util.TimeZone;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSGzip;
import com.sos.commons.util.SOSPath;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.history.DBItemHistoryLog;

public class DBItemLogTest {

    public SOSHibernateFactory createFactory(Path configFile) throws Exception {
        SOSHibernateFactory factory = new SOSHibernateFactory(configFile);
        factory.setIdentifier("history");
        factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        factory.getConfigurationProperties().put(" hibernate.bytecode.use_reflection_optimizer", "xxx");
        factory.addClassMapping(DBLayer.getHistoryClassMapping());
        factory.build();
        return factory;
    }

    public void closeFactory(SOSHibernateFactory factory) {
        if (factory != null) {
            factory.close();
            factory = null;
        }
    }

    public DBItemHistoryLog storeLog(SOSHibernateSession session, Path logFile) throws Exception {
        DBItemHistoryLog item = null;
        if (Files.exists(logFile)) {
            item = new DBItemHistoryLog();
            item.setControllerId("jobscheduler2.0");

            item.setHistoryOrderMainParentId(Long.valueOf(0));
            item.setHistoryOrderId(Long.valueOf(0));
            item.setHistoryOrderStepId(Long.valueOf(0));

            item.setFileBasename(SOSPath.getFileNameWithoutExtension(logFile.getFileName()));
            item.setFileSizeUncomressed(Files.size(logFile));
            Long lines = 0L;
            try {
                lines = Files.lines(logFile).count();
            } catch (Exception e) {
                System.err.println(String.format("[storeLog][%s]can't get file lines: %s", logFile.toString(), e.toString()));
            }
            item.setFileLinesUncomressed(lines);
            item.setCompressed(true);
            item.setFileContent(SOSGzip.compress(logFile, false).getCompressed());
            item.setCreated(new Date());

            session.save(item);
        }
        return item;
    }

    public DBItemHistoryLog getLog(SOSHibernateSession session, Long id) throws Exception {
        // return session.getSingleResult("from " + DBItemLog.class.getSimpleName() + " where id=" + id);
        return session.get(DBItemHistoryLog.class, id);
    }

    public static void main(String[] args) throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        DBItemLogTest t = new DBItemLogTest();

        // Path hibernateConfigFile = Paths.get("src/test/resources/hibernate.cfg.xml");
        // Path logFile = Paths.get("src/test/resources/hibernate.cfg.xml");

        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            // factory = t.createFactory(hibernateConfigFile);
            // session = factory.openStatelessSession();

            // DBItemLog item1 = t.storeLog(session, logFile);

            // DBItemLog item2 = t.getLog(session, item1.getId());

            // System.out.println(SOSHibernate.toString(item2));

        } catch (Throwable ex) {
            throw ex;
        } finally {
            if (session != null) {
                session.close();
            }
            t.closeFactory(factory);
        }
    }

}
