package com.sos.commons.hibernate.function.json;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import org.hibernate.dialect.Dialect;
import org.hibernate.query.Query;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SOSHibernateTest;
import com.sos.commons.hibernate.function.json.SOSHibernateJsonValue.ReturnType;
import com.sos.commons.hibernate.function.regex.SOSHibernateRegexp;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSReflection;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;

public class SOSHibernateJsonTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateJsonTest.class);

    private static final int SEARCH_CONFIG_TYPE = 1;// order

    private static final String SEARCH_WORKFLOW_PATH = "/test_shell";
    private static final String SEARCH_CALENDAR_PATH_REGEXP = "my_calendar";

    @Ignore
    @Test
    public void testChangeFinalStatic() throws Exception {
        List<Field> l = SOSReflection.getAllDeclaredFields(SOSHibernateJsonTest.class);
        for (Field f : l) {
            LOGGER.info(f.getName());
            if (f.getName().equals("SEARCH_WORKFLOW_PATH")) {
                f.setAccessible(true);

                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);

                f.set(null, "xyz");
            }
        }
        LOGGER.info(SEARCH_WORKFLOW_PATH);
    }

    @Ignore
    @Test
    public void testSelectConfigurations() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = SOSHibernateTest.createFactory();
            session = factory.openStatelessSession();

            StringBuilder hql = new StringBuilder("from " + DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");

            Query<DBItemInventoryConfiguration> query = session.createQuery(hql.toString());
            query.setMaxResults(1);

            List<DBItemInventoryConfiguration> result = session.getResultList(query);
            LOGGER.info("---- FOUND: " + result.size());
            session.beginTransaction();
            for (DBItemInventoryConfiguration c : result) {
                c.setContent(c.getContent());
                LOGGER.info(c.getType() + "=" + c.getJsonContent());
                session.update(c);
            }
            session.commit();

            hql = new StringBuilder("from " + DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
            hql.append("where type=:type ");
            hql.append("and ");
            hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "jsonContent", "$.timeZone")).append("=:timeZone");

            query = session.createQuery(hql.toString());
            query.setParameter("type", 1);
            query.setParameter("timeZone", "Europe/Berlin");

            result = session.getResultList(query);
            LOGGER.info("---- FOUND: " + result.size());
            LOGGER.info("---- DIALECT " + factory.getDialect());
            LOGGER.info(Dialect.class.getPackageName());
        } catch (Exception e) {
            throw e;
        } finally {
            if (factory != null) {
                factory.close(session);
            }
        }
    }

    // search in INV_CONFIGURATIONS for order objects which contains the workflowPath json property equals the given search workflow
    // ReturnType.SCALAR - return type is a single value
    // ReturnType.JSON - return type is a json array/object

    @Ignore
    @Test
    public void testScalarWhere() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = SOSHibernateTest.createFactory();
            session = factory.openStatelessSession();

            StringBuilder hql = new StringBuilder("from " + DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
            hql.append("where type=:type ");
            hql.append("and ");
            hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "jsonContent", "$.workflowPath")).append("=:workflowPath");

            Query<DBItemInventoryConfiguration> query = session.createQuery(hql.toString());
            query.setParameter("type", SEARCH_CONFIG_TYPE);
            query.setParameter("workflowPath", SEARCH_WORKFLOW_PATH);

            List<DBItemInventoryConfiguration> result = session.getResultList(query);
            LOGGER.info("---- FOUND: " + result.size());

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
    public void testSelectJson() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = SOSHibernateTest.createFactory();
            session = factory.openStatelessSession();

            StringBuilder hql = new StringBuilder("select ");
            hql.append(SOSHibernateJsonValue.getFunction(ReturnType.JSON, "jsonContent", "$.calendars[0].periods")).append(" ");
            hql.append("as calendars ");
            hql.append("from " + DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
            hql.append("where type=7 ");

            Query<String> query = session.createQuery(hql.toString());
            List<String> result = session.getResultList(query);
            LOGGER.info("---- FOUND: " + result.size());
            for (String w : result) {
                LOGGER.info("   calendars=" + w);
            }

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
    public void testScalarSelectAndWhere() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = SOSHibernateTest.createFactory();
            session = factory.openStatelessSession();

            StringBuilder hql = new StringBuilder("select ");
            hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "jsonContent", "$.workflowPath")).append(" ");
            hql.append("as workflow ");
            hql.append("from " + DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
            hql.append("where type=:type ");
            hql.append("and ");
            hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "jsonContent", "$.workflowPath")).append("=:workflowPath");

            Query<String> query = session.createQuery(hql.toString());
            query.setParameter("type", SEARCH_CONFIG_TYPE);
            query.setParameter("workflowPath", SEARCH_WORKFLOW_PATH);

            List<String> result = session.getResultList(query);
            LOGGER.info("---- FOUND: " + result.size());
            for (String w : result) {
                LOGGER.info("   workflow=" + w);
            }

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
    public void testJsonSelectAndScalarWhere() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = SOSHibernateTest.createFactory();
            session = factory.openStatelessSession();

            StringBuilder hql = new StringBuilder("select ");
            hql.append(SOSHibernateJsonValue.getFunction(ReturnType.JSON, "jsonContent", "$.variables")).append(" ");
            hql.append("as variables ");
            hql.append("from " + DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
            hql.append("where type=:type ");
            hql.append("and ");
            hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "jsonContent", "$.workflowPath")).append("=:workflowPath");

            Query<String> query = session.createQuery(hql.toString());
            query.setParameter("type", SEARCH_CONFIG_TYPE);
            query.setParameter("workflowPath", SEARCH_WORKFLOW_PATH);

            List<String> result = session.getResultList(query);
            LOGGER.info("---- FOUND: " + result.size());
            for (String w : result) {
                LOGGER.info("   variables=" + w);
            }

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
    public void testJsonSelectAndJsonWhere() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = SOSHibernateTest.createFactory();
            session = factory.openStatelessSession();

            StringBuilder hql = new StringBuilder("select ");
            hql.append(SOSHibernateJsonValue.getFunction(ReturnType.JSON, "jsonContent", "$.calendars")).append(" ");
            hql.append("as calendars ");
            hql.append("from " + DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
            hql.append("where type=:type ");
            hql.append("and ");
            hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "jsonContent", "$.workflowPath")).append("=:workflowPath").append(" ");
            hql.append("and ");
            // hql.append(SOSHibernateRegexp.getFunction(SOSHibernateJsonValue.getFunction(ReturnType.JSON, "jsonContent", "$.calendars"),
            // ":calendarPath"));
            String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "jsonContent", "$.calendars");
            hql.append(SOSHibernateRegexp.getFunction(jsonFunc, ":calendarPath"));

            Query<String> query = session.createQuery(hql.toString());
            query.setParameter("type", SEARCH_CONFIG_TYPE);
            query.setParameter("workflowPath", SEARCH_WORKFLOW_PATH);
            query.setParameter("calendarPath", SEARCH_CALENDAR_PATH_REGEXP);

            List<String> result = session.getResultList(query);
            LOGGER.info("---- FOUND: " + result.size());
            for (String w : result) {
                LOGGER.info("   calendars=" + w);
            }

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
    public void generateLargeJsonFileToManuallyInsertIntoDBTestTable() throws Exception {
        Path outputFile = Paths.get("src/test/resources/large_json_file.json");

        LOGGER.info("[START]...");
        JsonObjectBuilder builder = Json.createObjectBuilder();
        JsonArrayBuilder ab = Json.createArrayBuilder();
        for (int i = 1; i <= 1_000; i++) {
            JsonObjectBuilder ob = Json.createObjectBuilder();
            ob.add("person_" + i, Json.createObjectBuilder().add("name", "Fritz Tester" + i).add("age", 30));
            ab.add(ob);
        }
        builder.add("persons", ab);

        ObjectMapper mapper = new ObjectMapper();
        SOSPath.overwrite(outputFile, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(builder.build()));
        LOGGER.info("[END]" + outputFile);
    }
}
