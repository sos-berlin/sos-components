package com.sos.js7.order.initiator.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.Schedule;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.DeploymentState;

public class DBLayerSchedules {

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

        String q = "from " + DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS + getWhere(filter) + filter.getOrderCriteria() + filter.getSortMode();
        Query<DBItemInventoryReleasedConfiguration> query = sosHibernateSession.createQuery(q);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        List<DBItemInventoryReleasedConfiguration> filteredResultset = new ArrayList<DBItemInventoryReleasedConfiguration>();
        List<DBItemInventoryReleasedConfiguration> resultset = sosHibernateSession.getResultList(query);

        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        boolean selectedByWorkflowNames = (filter.getListOfWorkflowNames() != null && filter.getListOfWorkflowNames().size() > 0);

        if (selectedByWorkflowNames) {
            InventoryDBLayer inventoryDBLayer = new InventoryDBLayer(sosHibernateSession);
           
            List<DBItemInventoryReleasedConfiguration> resultsetByWorkflowNames = inventoryDBLayer.getUsedReleasedSchedulesByWorkflowNames(filter.getListOfWorkflowNames());
            resultset.addAll(resultsetByWorkflowNames);      
        }

        FilterInventoryConfigurations filterInventoryConfigurations = new FilterInventoryConfigurations();
        filterInventoryConfigurations.setType(ConfigurationType.WORKFLOW);

        FilterDeployHistory filterDeployHistory = new FilterDeployHistory();
        filterDeployHistory.setListOfControllerIds(filter.getListOfControllerIds());
        filterDeployHistory.setOrderCriteria("deploymentDate");
        filterDeployHistory.setSortMode("desc");
        filterDeployHistory.setType(ConfigurationType.WORKFLOW);
        filterDeployHistory.setState(DeploymentState.DEPLOYED);

        DBLayerDeployHistory dbLayerDeploy = new DBLayerDeployHistory(sosHibernateSession);
        DBLayerInventoryConfigurations dbLayerInventoryConfigurations = new DBLayerInventoryConfigurations(sosHibernateSession);
        List<DBItemInventoryConfiguration> listOfWorkflows = dbLayerInventoryConfigurations.getInventoryConfigurations(filterInventoryConfigurations,
                0);

        Map<String, String> workflowPaths = new HashMap<String, String>();
        for (DBItemInventoryConfiguration dbItemInventoryConfiguration : listOfWorkflows) {
            filterDeployHistory.setInventoryId(dbItemInventoryConfiguration.getId());
            List<DBItemDeploymentHistory> l = dbLayerDeploy.getDeployments(filterDeployHistory, 0);
            if (l.size() > 0) {
                if (l.get(0).getOperation() == 0) {
                    workflowPaths.put(dbItemInventoryConfiguration.getName(), dbItemInventoryConfiguration.getPath());
                }
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
                schedule.setWorkflowPath(workflowPaths.get(schedule.getWorkflowName()));
                dbItemInventoryConfiguration.setSchedule(schedule);
                filteredResultset.add(dbItemInventoryConfiguration);
            }
        }

        return filteredResultset;
    }
    
    public Map<String, String> getSchedulePathNameMap(Collection<String> scheduleNamesOrPaths) throws SOSHibernateException {

        if (scheduleNamesOrPaths == null || scheduleNamesOrPaths.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Boolean, List<String>> namesAndPaths = scheduleNamesOrPaths.stream().filter(s -> s != null && !s.isEmpty()).collect(Collectors.groupingBy(
                s -> s.startsWith("/")));

        StringBuilder sql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" where");
        if (namesAndPaths.containsKey(true)) { // paths
            sql.append(" path in (:paths)");
        }
        if (namesAndPaths.containsKey(true) && namesAndPaths.containsKey(false)) { // paths and names
            sql.append(" or");
        }
        if (namesAndPaths.containsKey(false)) { // names
            sql.append(" name in (:names)");
        }

        Query<DBItemInventoryReleasedConfiguration> query = sosHibernateSession.createQuery(sql);
        if (namesAndPaths.containsKey(true)) { // paths
            query.setParameterList("paths", namesAndPaths.get(true));
        }
        if (namesAndPaths.containsKey(false)) { // names
            query.setParameterList("names", namesAndPaths.get(false));
        }

        List<DBItemInventoryReleasedConfiguration> resultset = sosHibernateSession.getResultList(query);
        if (resultset == null || resultset.isEmpty()) {
            return Collections.emptyMap();
        }

        return resultset.stream().distinct().collect(Collectors.toMap(DBItemInventoryReleasedConfiguration::getPath,
                DBItemInventoryReleasedConfiguration::getName));
    }

}