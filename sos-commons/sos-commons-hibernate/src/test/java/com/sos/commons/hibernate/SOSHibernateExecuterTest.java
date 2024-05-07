package com.sos.commons.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.commons.hibernate.common.SOSBatchObject;

public class SOSHibernateExecuterTest {

    @Ignore
    @Test
    public void testInfos() throws Exception {
        SOSHibernateFactory factory = null;
        try {
            factory = SOSHibernateSessionTest.createFactory();
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
    public void testH2() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = SOSHibernateSessionTest.createFactory();

            JsonObjectBuilder builder = Json.createObjectBuilder();
            builder.add("workflowNames", Json.createArrayBuilder().add("name1").add("name2").add("name3"));
            builder.add("workflowName", "myWorkflowName");

            StringBuilder sql = new StringBuilder("update INV_CONFIGURATIONS ");
            // H2 - use "format JSON" otherwise the content will be stored as quoted string,
            // e.g.: \"{\"workflowNames\":...}\" instead of {"workflowNames":...}
            sql.append("set CONTENT='").append(builder.build().toString()).append("' format JSON ");
            sql.append("where ID=6");

            session = factory.openStatelessSession();
            // execute
            session.beginTransaction();
            session.getSQLExecutor().execute(sql.toString());
            session.commit();

        } catch (Exception e) {
            throw e;
        } finally {
            if (factory != null) {
                factory.close(session);
            }
        }
    }

    @Ignore
    @Test
    public void testBatchInsert() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            // prepare
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO TABLE_TEST(NAME,AGE,COUNTRY)");
            sql.append("VALUES(?,?,?)");// count ? = count columns

            Collection<Collection<SOSBatchObject>> rows = new ArrayList<>();
            rows.add(addRow());
            rows.add(addRow());
            rows.add(addRow());
            rows.add(addRow());

            // create connection
            factory = SOSHibernateSessionTest.createFactory();
            session = factory.openStatelessSession();

            // execute
            session.beginTransaction();
            session.getSQLExecutor().executeBatch("TABLE_TEST", sql.toString(), rows);
            session.commit();
        } catch (Exception e) {
            throw e;
        } finally {
            if (factory != null) {
                factory.close(session);
            }
        }
    }

    private List<SOSBatchObject> addRow() {
        // index - see INSERT INTO TABLE_TEST(NAME,AGE,COUNTRY)
        // NAME <- index 1 etc

        List<SOSBatchObject> rowValues = new ArrayList<>();
        int index = 1;// beginning with 1
        rowValues.add(new SOSBatchObject(index, "NAME", "Tester"));
        rowValues.add(new SOSBatchObject(++index, "AGE", Integer.valueOf(33)));
        rowValues.add(new SOSBatchObject(++index, "COUNTRY", "DE"));
        return rowValues;
    }

}
