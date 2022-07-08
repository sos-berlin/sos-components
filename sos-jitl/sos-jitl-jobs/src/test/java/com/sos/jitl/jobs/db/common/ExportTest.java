package com.sos.jitl.jobs.db.common;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSQLExecutor;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jitl.jobs.common.UnitTestJobHelper;
import com.sos.jitl.jobs.db.SQLExecutorJobArguments.ResultSetAsVariables;

public class ExportTest {

    @Ignore
    @Test
    public void testCSV() throws Exception {
        Path outputFile = Paths.get("src/test/resources/test_export.csv");
        String statement = "select *  from INV_CONFIGURATIONS";
        // statement = "SELECT DATE '2030-12-10','\"my_field\"', 123, 123.123 FROM DUAL";
        export(ResultSetAsVariables.CSV, outputFile, statement);
    }

    @Ignore
    @Test
    public void testXML() throws Exception {
        Path outputFile = Paths.get("src/test/resources/test_export.xml");
        String statement = "select *  from INV_CONFIGURATIONS";
        // statement = "SELECT DATE '2030-12-10','\"my_field\"', 123, 123.123 FROM DUAL";
        export(ResultSetAsVariables.XML, outputFile, statement);
    }

    @Ignore
    @Test
    public void testJSON() throws Exception {
        Path outputFile = Paths.get("src/test/resources/test_export.json");
        String statement = "select * from INV_CONFIGURATIONS";
        // statement = "SELECT DATE '2030-12-10','\"my_field\"', 123, 123.123 FROM DUAL";
        export(ResultSetAsVariables.JSON, outputFile, statement);
    }

    private void export(ResultSetAsVariables type, Path outputFile, String statement) throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = createFactory();
            session = factory.openStatelessSession();

            SOSHibernateSQLExecutor executor = session.getSQLExecutor();
            session.beginTransaction();

            ResultSet rs = null;
            try {
                rs = executor.getResultSet(statement);
                switch (type) {
                case CSV:
                    Export2CSV.export(rs, outputFile, UnitTestJobHelper.newJobLogger());
                    break;
                case XML:
                    Export2XML.export(rs, outputFile, UnitTestJobHelper.newJobLogger());
                    break;
                case JSON:
                    Export2JSON.export(rs, outputFile, UnitTestJobHelper.newJobLogger());
                    break;
                default:
                    break;
                }
                session.commit();
            } catch (Throwable e) {
                throw e;
            } finally {
                executor.close(rs);
            }
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

    private SOSHibernateFactory createFactory() throws Exception {
        SOSHibernateFactory factory = new SOSHibernateFactory(Paths.get("src/test/resources/hibernate.cfg.xml"));
        factory.build();
        return factory;
    }
}
