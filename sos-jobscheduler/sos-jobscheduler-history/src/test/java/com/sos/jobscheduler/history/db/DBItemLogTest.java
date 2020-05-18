package com.sos.jobscheduler.history.db;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Date;
import java.util.TimeZone;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSPath;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.db.history.DBItemLog;

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

    public DBItemLog storeLog(SOSHibernateSession session, Path logFile) throws Exception {
        File f = logFile.toFile();
        DBItemLog item = null;
        if (f.exists()) {
            item = new DBItemLog();
            item.setJobSchedulerId("jobscheduler2.0");

            item.setMainOrderId(new Long(0));
            item.setOrderId(new Long(0));
            item.setOrderStepId(new Long(0));

            item.setFileBasename(com.google.common.io.Files.getNameWithoutExtension(f.getName()));
            item.setFileSizeUncomressed(f.length());
            Long lines = 0L;
            try {
                lines = Files.lines(logFile).count();
            } catch (Exception e) {
                System.err.println(String.format("[storeLog][%s]can't get file lines: %s", f.getCanonicalPath(), e.toString()));
            }
            item.setFileLinesUncomressed(lines);
            item.setCompressed(true);
            item.setFileContent(SOSPath.gzipFile(logFile));
            item.setCreated(new Date());

            session.save(item);
        }
        return item;
    }

    public DBItemLog getLog(SOSHibernateSession session, Long id) throws Exception {
        // return session.getSingleResult("from " + DBItemLog.class.getSimpleName() + " where id=" + id);
        return session.get(DBItemLog.class, id);
    }

    public static void main(String[] args) throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        DBItemLogTest t = new DBItemLogTest();

        Path hibernateConfigFile = Paths.get("src/test/resources/hibernate.cfg.xml");
        Path logFile = Paths.get("src/test/resources/hibernate.cfg.xml");

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
