package com.sos.commons.hibernate.function.regex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.query.Query;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernate.Dbms;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SOSHibernateTest;
import com.sos.commons.hibernate.function.json.SOSHibernateJsonValue;
import com.sos.commons.hibernate.function.json.SOSHibernateJsonValue.ReturnType;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class SOSHibernateRegExTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateRegExTest.class);
    private String regexpParamPrefixSuffix = "";

    @Ignore
    @Test
    public void testRegexp() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = SOSHibernateTest.createFactory();
            session = factory.openStatelessSession();

            StringBuilder hql = new StringBuilder("select name from " + DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
            hql.append("where ");
            hql.append(SOSHibernateRegexp.getFunction("name", ":name"));

            Query<String> query = session.createQuery(hql.toString());
            query.setParameter("name", "^workflow.*");

            List<String> result = session.getResultList(query);
            LOGGER.info("---- FOUND: " + result.size());
            for (String w : result) {
                LOGGER.info("   name=" + w);
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
    public void testRegexpValue() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = SOSHibernateTest.createFactory();
            session = factory.openStatelessSession();

            StringBuilder hql = new StringBuilder("select name from " + DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
            hql.append("where ");
            hql.append(SOSHibernateRegexp.getFunction("name", "_workflow_"));

            Query<String> query = session.createQuery(hql.toString());

            List<String> result = session.getResultList(query);
            LOGGER.info("---- FOUND: " + result.size());
            for (String w : result) {
                LOGGER.info("   name=" + w);
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
    public void testRegexpMSSQLExtraValue() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = SOSHibernateTest.createFactory();
            session = factory.openStatelessSession();

            StringBuilder hql = new StringBuilder("select name from " + DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
            hql.append("where ");
            hql.append(SOSHibernateRegexp.getFunction("name", "_workflow_", "__a"));

            Query<String> query = session.createQuery(hql.toString());

            List<String> result = session.getResultList(query);
            LOGGER.info("---- FOUND: " + result.size());
            for (String w : result) {
                LOGGER.info("   name=" + w);
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
    public void testGetUsedSchedulesByCalendarName() throws Exception {
        String calendarName = "Business.*";

        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = SOSHibernateTest.createFactory();
            setRegexpParamPrefixSuffix(factory.getDbms());
            session = factory.openStatelessSession();

            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
            hql.append("where type=:type ");
            hql.append("and (");
            String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "jsonContent", "$.calendars");
            String jsonFuncNonWorkingDays = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "jsonContent", "$.nonWorkingDayCalendars");
            hql.append(SOSHibernateRegexp.getFunction(jsonFunc, ":calendarName"));
            hql.append(" or ").append(SOSHibernateRegexp.getFunction(jsonFuncNonWorkingDays, ":calendarName")).append(" )");

            Query<DBItemInventoryConfiguration> query = session.createQuery(hql.toString());
            query.setParameter("type", ConfigurationType.SCHEDULE.intValue());
            query.setParameter("calendarName", getRegexpParameter(calendarName, "\""));

            List<DBItemInventoryConfiguration> result = session.getResultList(query);
            LOGGER.info("---- FOUND: " + result.size());
            for (DBItemInventoryConfiguration item : result) {
                LOGGER.info("[ID=" + item.getId() + "]" + item.getContent());
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
    public void getUsedWorkflowsByAgentNames() throws Exception {
        Collection<String> agentNames = new ArrayList<String>();
        agentNames.add("agent.*");
        boolean onlyInvalid = true;

        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = SOSHibernateTest.createFactory();
            setRegexpParamPrefixSuffix(factory.getDbms());
            session = factory.openStatelessSession();

            StringBuilder hql = new StringBuilder("select ic from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
            hql.append("left join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
            hql.append("on ic.id=sw.inventoryConfigurationId ");
            hql.append("where ic.type=:type ");
            hql.append("and ic.deployed=sw.deployed ");
            if (onlyInvalid) {
                hql.append("and ic.valid=0 ");
            }
            hql.append("and ");

            hql.append(agentNames.stream().map(agentName -> SOSHibernateRegexp.getFunction(SOSHibernateJsonValue.getFunction(ReturnType.JSON,
                    "sw.jobs", "$.agentIds"), getRegexpParameter(agentName, "\""))).collect(Collectors.joining(" or ", "(", ")")));

            Query<DBItemInventoryConfiguration> query = session.createQuery(hql.toString());
            query.setParameter("type", ConfigurationType.WORKFLOW.intValue());
            List<DBItemInventoryConfiguration> result = session.getResultList(query);
            LOGGER.info("---- FOUND: " + result.size());
            for (DBItemInventoryConfiguration item : result) {
                LOGGER.info("[ID=" + item.getId() + "]" + item.getContent());
            }

        } catch (Exception e) {
            throw e;
        } finally {
            if (factory != null) {
                factory.close(session);
            }
        }
    }

    private String getRegexpParameter(String param, String prefixSuffix) {
        return regexpParamPrefixSuffix + prefixSuffix + param + prefixSuffix + regexpParamPrefixSuffix;
    }

    private void setRegexpParamPrefixSuffix(Dbms dbms) {
        try {
            if (Dbms.MSSQL.equals(dbms)) {
                regexpParamPrefixSuffix = "%";
            }
        } catch (Throwable e) {
        }
    }

}
