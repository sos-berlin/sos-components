package com.sos.js7.order.initiator.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.query.Query;
import org.hibernate.transform.Transformers;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.orders.DBItemDailyPlanWithHistory;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.DeploymentState;
import com.sos.webservices.order.initiator.model.Schedule;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;

public class DBLayerSchedules {

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
        String and = " and ";

        if (filter.getListOfSchedules() != null && filter.getListOfSchedules().size() > 0) {
            where += and + SearchStringHelper.getStringListSql(filter.getListOfSchedules(), "path");
            and = " and ";
        } else {
            if (filter.getListOfFolders() != null && filter.getListOfFolders().size() > 0) {
                where += and + "(";
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
                and = " and ";
            }
        }

        if (!"".equals(where.trim()))

        {
            where = " where " + where;
        }
        return where;
    }

    public List<DBItemInventoryReleasedConfiguration> getSchedules(FilterSchedules filter, final int limit) throws SOSHibernateException, JsonParseException, JsonMappingException, IOException {
 
        String q = "from " + DBItemInventoryReleasedConfiguration + getWhere(filter) + filter.getOrderCriteria() + filter.getSortMode();
        Query<DBItemInventoryReleasedConfiguration> query = sosHibernateSession.createQuery(q);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        List<DBItemInventoryReleasedConfiguration> filteredResultset = new ArrayList<DBItemInventoryReleasedConfiguration>();
        List<DBItemInventoryReleasedConfiguration> resultset = sosHibernateSession.getResultList(query);       

    
        
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Set<String> setOfWorkflows = new LinkedHashSet<String>();
        boolean filteredByControllerIds=(filter.getListOfControllerIds() != null && filter.getListOfControllerIds().size() > 0);

        if (filteredByControllerIds) {
            FilterDeployHistory filterDeployHistory = new FilterDeployHistory();
            filterDeployHistory.setType(ConfigurationType.WORKFLOW);
            filterDeployHistory.setListOfControllerIds(filter.getListOfControllerIds());
            filterDeployHistory.setState(DeploymentState.DEPLOYED);

            DBLayerDeployHistory dbLayerDeploy = new DBLayerDeployHistory(sosHibernateSession);
            List<DBItemDeploymentHistory> listOfWorkflows = dbLayerDeploy.getDeployments(filterDeployHistory, 0);

            for (DBItemDeploymentHistory dbItemDeploymentHistory: listOfWorkflows) {
                setOfWorkflows.add(dbItemDeploymentHistory.getPath());
            }
        }
        for (DBItemInventoryReleasedConfiguration dbItemInventoryConfiguration: resultset) {
            Schedule schedule = objectMapper.readValue(dbItemInventoryConfiguration.getContent(), Schedule.class);
            
            if (!filteredByControllerIds || setOfWorkflows.contains(schedule.getWorkflowPath())) {
                dbItemInventoryConfiguration.setSchedule(schedule);
                filteredResultset.add(dbItemInventoryConfiguration);
            }
        }
        return filteredResultset;
    }

}