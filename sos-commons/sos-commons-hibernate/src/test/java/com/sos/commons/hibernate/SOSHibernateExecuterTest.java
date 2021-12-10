package com.sos.commons.hibernate;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.commons.hibernate.common.SOSBatchObject;

public class SOSHibernateExecuterTest {

    @Ignore
    @Test
    public void test() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = createFactory();
            session = factory.openStatelessSession();

            SOSHibernateSQLExecutor executor = session.getSQLExecutor();
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO TABLE_TEST(NAME,AGE)");
            sql.append("VALUES(?,?)");

            List<SOSBatchObject> values = new ArrayList<>();
            values.add(new SOSBatchObject(0, "NAME", "Tester"));
            values.add(new SOSBatchObject(1, "AGE", new Integer(33)));

            session.beginTransaction();
            executor.executeBatch("TABLE_TEST", sql.toString(), values);
            session.commit();
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
