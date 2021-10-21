package com.sos.js7.order.initiator.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class DBLayerSchedules {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerSchedules.class);
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
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        if (filter.getListOfWorkflowNames() != null && filter.getListOfWorkflowNames().size() > 0) {
            InventoryDBLayer dbLayer = new InventoryDBLayer(sosHibernateSession);
            List<DBItemInventoryReleasedConfiguration> result = dbLayer.getUsedReleasedSchedulesByWorkflowNames(filter.getListOfWorkflowNames());

            List<String> scheduleNames = filter.getListOfScheduleNames();
            if (scheduleNames == null) {
                scheduleNames = new ArrayList<>();
            }
            if (result != null && result.size() > 0) {
                for (DBItemInventoryReleasedConfiguration item : result) {
                    if (scheduleNames.contains(item.getName())) {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[schedulesByWorkflowNames][%s][skip][already added]%s", item.getName(), SOSString.toString(
                                    item)));
                        }
                    } else {
                        scheduleNames.add(item.getName());
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[schedulesByWorkflowNames][%s][added]%s", item.getName(), SOSString.toString(item)));
                        }
                    }
                }
            } else {
                LOGGER.debug("[schedulesByWorkflowNames]not found");
            }
            filter.setListOfScheduleNames(scheduleNames);
        }

        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" ");
        hql.append(getWhere(filter));
        hql.append(filter.getOrderCriteria());
        hql.append(filter.getSortMode());
        Query<DBItemInventoryReleasedConfiguration> query = sosHibernateSession.createQuery(hql.toString());
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        List<DBItemInventoryReleasedConfiguration> filtered = new ArrayList<DBItemInventoryReleasedConfiguration>();
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<DBItemInventoryReleasedConfiguration> result = sosHibernateSession.getResultList(query);
        for (DBItemInventoryReleasedConfiguration item : result) {
            Schedule schedule;
            if (item.getSchedule() != null) {
                schedule = item.getSchedule();
            } else {
                schedule = objectMapper.readValue(item.getContent(), Schedule.class);
                schedule.setPath(item.getPath());
            }
            if (schedule == null) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[filtered][skip][schedule is null]%s", SOSString.toString(item)));
                }
                continue;
            }

            String path = WorkflowPaths.getPathOrNull(schedule.getWorkflowName());
            if (path == null) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[filtered][skip][deployment path not found]%s", SOSString.toString(item)));
                }
            } else {
                schedule.setWorkflowPath(path);
                item.setSchedule(schedule);
                filtered.add(item);
            }
        }
        if (isDebugEnabled) {
            for (DBItemInventoryReleasedConfiguration item : filtered) {
                LOGGER.debug(String.format("[filtered]%s", SOSString.toString(item)));
            }
        }
        return filtered;
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