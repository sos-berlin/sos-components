package com.sos.js7.order.initiator.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.Schedule;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.orders.DBItemDailyPlanHistory;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.DeploymentState;
import com.sos.js7.order.initiator.classes.OrderInitiatorGlobals;

public class DBLayerSchedules {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerSchedules.class);

    private static final String DBItemInventoryReleasedConfiguration = com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration.class
            .getSimpleName();

    private final SOSHibernateSession sosHibernateSession;

    public DBLayerSchedules(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public FilterSchedules resetFilter() {
        FilterSchedules filter = new FilterSchedules();
        return filter;
    }

    private String getWhere(FilterSchedules filter) {
        String where = " type = " + ConfigurationType.SCHEDULE.intValue();
        String and = " and (";
        String kzu = "";

        if (filter.getListOfScheduleNames() != null && filter.getListOfScheduleNames().size() > 0) {
            where += and + SearchStringHelper.getStringListSql(filter.getListOfScheduleNames(), "name");
            and = " or ";
            kzu = ")";
        }

        if (filter.getListOfFolders() != null && filter.getListOfFolders().size() > 0) {
            where += and + "(";
            kzu = ")";
            for (Folder filterFolder : filter.getListOfFolders()) {
                if (filterFolder.getRecursive()) {
                    String likeFolder = (filterFolder.getFolder() + "/%").replaceAll("//+", "/");
                    where += " ( folder ='" + filterFolder.getFolder() + "' or folder like '" + likeFolder + "') ";
                } else {
                    where += String.format(" folder %s '" + filterFolder.getFolder() + "'", SearchStringHelper.getSearchOperator(filterFolder
                            .getFolder()));
                }
                where += " or ";
            }
            where += " 0=1)";
            and = " or ";
        }

        if (!"".equals(where.trim())) {
            where = " where " + where + kzu;
        }
        return where;
    }

    public List<DBItemInventoryReleasedConfiguration> getSchedules(FilterSchedules filter, final int limit) throws SOSHibernateException,
            JsonParseException, JsonMappingException, IOException {

        DBLayerDailyPlanHistory dbLayerDailyPlanHistory = new DBLayerDailyPlanHistory(sosHibernateSession);
        String q = "from " + DBItemInventoryReleasedConfiguration + getWhere(filter) + filter.getOrderCriteria() + filter.getSortMode();
        Query<DBItemInventoryReleasedConfiguration> query = sosHibernateSession.createQuery(q);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        List<DBItemInventoryReleasedConfiguration> filteredResultset = new ArrayList<DBItemInventoryReleasedConfiguration>();
        List<DBItemInventoryReleasedConfiguration> resultset = sosHibernateSession.getResultList(query);

        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Set<String> setOfWorkflows = new LinkedHashSet<String>();
        boolean selectedByWorkflowNames = (filter.getListOfWorkflowNames() != null && filter.getListOfWorkflowNames().size() > 0);

        if (selectedByWorkflowNames) {
            FilterSchedules filterSelectByWorkflowNames = new FilterSchedules();

            q = "from " + DBItemInventoryReleasedConfiguration + getWhere(filterSelectByWorkflowNames) + filter.getOrderCriteria() + filter
                    .getSortMode();
            query = sosHibernateSession.createQuery(q);

            if (limit > 0) {
                query.setMaxResults(limit);
            }
            List<DBItemInventoryReleasedConfiguration> resultsetByWorkflowNames = sosHibernateSession.getResultList(query);
            for (DBItemInventoryReleasedConfiguration dbItemInventoryConfiguration : resultsetByWorkflowNames) {
                Schedule schedule = objectMapper.readValue(dbItemInventoryConfiguration.getContent(), Schedule.class);

                if (filter.getListOfWorkflowNames().contains(schedule.getWorkflowName())) {
                    dbItemInventoryConfiguration.setSchedule(schedule);
                    resultset.add(dbItemInventoryConfiguration);
                }
            }

        }

        FilterInventoryConfigurations filterInventoryConfigurations = new FilterInventoryConfigurations();
        filterInventoryConfigurations.setListOfControllerIds(filter.getListOfControllerIds());
        filterInventoryConfigurations.setDeployed(true);
        filterInventoryConfigurations.setType(ConfigurationType.WORKFLOW);

        FilterDeployHistory filterDeployHistory = new FilterDeployHistory();
        filterDeployHistory.setType(ConfigurationType.WORKFLOW);
        filterDeployHistory.setListOfControllerIds(filter.getListOfControllerIds());
        filterDeployHistory.setOperation(0);
        filterDeployHistory.setState(DeploymentState.DEPLOYED);

        DBLayerDeployHistory dbLayerDeploy = new DBLayerDeployHistory(sosHibernateSession);
        DBLayerInventoryConfigurations dbLayerInventoryConfigurations = new DBLayerInventoryConfigurations(sosHibernateSession);
        List<DBItemInventoryConfiguration> listOfWorkflows = dbLayerInventoryConfigurations.getInventoryConfigurations(filterInventoryConfigurations,
                0);

        for (DBItemInventoryConfiguration dbItemInventoryConfiguration : listOfWorkflows) {
            filterDeployHistory.setInventoryId(dbItemInventoryConfiguration.getId());
            List<DBItemDeploymentHistory> l = dbLayerDeploy.getDeployments(filterDeployHistory, 0);
            if (l.size() > 0) {
                setOfWorkflows.add(dbItemInventoryConfiguration.getName());
            }
        }

        for (DBItemInventoryReleasedConfiguration dbItemInventoryConfiguration : resultset) {
            Schedule schedule;
            if (dbItemInventoryConfiguration.getSchedule() != null) {
                schedule = dbItemInventoryConfiguration.getSchedule();
            } else {
                schedule = objectMapper.readValue(dbItemInventoryConfiguration.getContent(), Schedule.class);
                schedule.setPath(dbItemInventoryConfiguration.getPath());
            }

            if (schedule != null) {
                if (setOfWorkflows.contains(schedule.getWorkflowName())) {

                    dbItemInventoryConfiguration.setSchedule(schedule);
                    filteredResultset.add(dbItemInventoryConfiguration);
                } else {
                    String s = "Workflow " + schedule.getWorkflowName() + " is not deployed. scheduler->" + dbItemInventoryConfiguration.getContent();
                    DBItemDailyPlanHistory dbItemDailyPlanHistory = new DBItemDailyPlanHistory();
                    dbItemDailyPlanHistory.setCategory("ERROR");
                    if (filter.getListOfControllerIds().size() > 0) {
                        dbItemDailyPlanHistory.setControllerId(filter.getListOfControllerIds().get(0));
                    }
                    dbItemDailyPlanHistory.setCreated(JobSchedulerDate.nowInUtc());
                    dbItemDailyPlanHistory.setMessage(s);
                    dbItemDailyPlanHistory.setDailyPlanDate(OrderInitiatorGlobals.dailyPlanDate);
                    dbItemDailyPlanHistory.setUserAccount(OrderInitiatorGlobals.orderInitiatorSettings.getUserAccount());
                    dbLayerDailyPlanHistory.storeDailyPlanHistory(dbItemDailyPlanHistory);

                    LOGGER.debug("Warn:" + s);
                }
            }
        }

        return filteredResultset;
    }

}