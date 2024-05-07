package com.sos.commons.hibernate.function.json;

import java.util.List;

import org.hibernate.query.Query;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SOSHibernateTest;
import com.sos.commons.hibernate.function.json.SOSHibernateJsonValue.ReturnType;
import com.sos.commons.hibernate.function.regex.SOSHibernateRegexp;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;

public class SOSHibernateJsonTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateJsonTest.class);

    private static final int SEARCH_CONFIG_TYPE = 7;// order

    private static final String SEARCH_WORKFLOW_PATH = "/my_workflow";
    private static final String SEARCH_CALENDAR_PATH_REGEXP = "my_calendar";

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
    public void testScalarSelectAndWhere() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = SOSHibernateTest.createFactory();
            session = factory.openStatelessSession();

            StringBuilder hql = new StringBuilder("select ");
            hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "content", "$.workflowPath")).append(" ");
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
            hql.append(SOSHibernateJsonValue.getFunction(ReturnType.JSON, "content", "$.variables")).append(" ");
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
            hql.append(SOSHibernateJsonValue.getFunction(ReturnType.JSON, "content", "$.workingCalendars")).append(" ");
            hql.append("as workingCalendars ");
            hql.append("from " + DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
            hql.append("where type=:type ");
            hql.append("and ");
            hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "jsonContent", "$.workflowPath")).append("=:workflowPath").append(" ");
            hql.append("and ");
            // hql.append(SOSHibernateRegexp.getFunction(SOSHibernateJsonValue.getFunction(ReturnType.JSON, "jsonContent", "$.workingCalendars"),
            // ":calendarPath"));
            String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "jsonContent", "$.workingCalendars");
            hql.append(SOSHibernateRegexp.getFunction(jsonFunc, ":calendarPath"));

            Query<String> query = session.createQuery(hql.toString());
            query.setParameter("type", SEARCH_CONFIG_TYPE);
            query.setParameter("workflowPath", SEARCH_WORKFLOW_PATH);
            query.setParameter("calendarPath", SEARCH_CALENDAR_PATH_REGEXP);

            List<String> result = session.getResultList(query);
            LOGGER.info("---- FOUND: " + result.size());
            for (String w : result) {
                LOGGER.info("   workingCalendars=" + w);
            }

        } catch (Exception e) {
            throw e;
        } finally {
            if (factory != null) {
                factory.close(session);
            }
        }
    }
}
