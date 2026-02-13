package com.sos.joc.db.inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.function.json.SOSHibernateJsonExists;
import com.sos.commons.hibernate.function.json.SOSHibernateJsonExists.JsonCaseSensitivity;
import com.sos.commons.hibernate.function.json.SOSHibernateJsonExists.JsonOperator;
import com.sos.commons.hibernate.function.json.SOSHibernateJsonExists.JsonPathType;
import com.sos.commons.hibernate.function.json.SOSHibernateJsonValue;
import com.sos.commons.hibernate.function.json.SOSHibernateJsonValue.ReturnType;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.job.JobCriticality;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.search.WorkflowSearcher;
import com.sos.joc.classes.inventory.search.WorkflowSearcher.WorkflowJob;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.common.SearchStringHelper;
import com.sos.joc.db.inventory.items.InventoryQuickSearchItem;
import com.sos.joc.db.inventory.items.InventorySearchItem;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.search.RequestSearchAdvancedItem;
import com.sos.joc.model.inventory.search.RequestSearchReturnType;

public class InventorySearchDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(InventorySearchDBLayer.class);

    private static final String FIND_ALL = "*";

    public InventorySearchDBLayer(SOSHibernateSession session) {
        super(session);
    }

    public List<InventoryQuickSearchItem> getQuickSearchInventoryConfigurations(Collection<RequestSearchReturnType> types, String search)
            throws SOSHibernateException {
        return getQuickSearchInventoryConfigurations(requestSearchReturnTypesToConfigurationTypeIntValues(types), search,
                DBLayer.DBITEM_INV_CONFIGURATIONS);
    }

    public List<InventoryQuickSearchItem> getQuickSearchReleasedConfigurations(Collection<RequestSearchReturnType> types, String search)
            throws SOSHibernateException {
        return getQuickSearchInventoryConfigurations(requestSearchReturnTypesToConfigurationTypeIntValues(types), search,
                DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
    }

    private static List<Integer> requestSearchReturnTypesToConfigurationTypeIntValues(Collection<RequestSearchReturnType> types) {
        if (types == null) {
            return Collections.emptyList();
        }
        // NOTE: CALENDAR — JOC-2172 added legacy/tolerate support for WORKINGDAYSCALENDAR and NONWORKINGDAYSCALENDAR
        boolean hasCalendar = types.contains(RequestSearchReturnType.CALENDAR);
        Stream<RequestSearchReturnType> typesStream = types.stream().distinct();
        if (hasCalendar) {
            typesStream = typesStream.filter(r -> !r.equals(RequestSearchReturnType.CALENDAR));
        }
        List<Integer> intTypes = typesStream.map(RequestSearchReturnType::value).map(ConfigurationType::fromValue).map(ConfigurationType::intValue)
                .collect(Collectors.toList());
        if (hasCalendar) {
            intTypes.addAll(JocInventory.getCalendarTypes());
        }
        return intTypes;
    }

    private List<InventoryQuickSearchItem> getQuickSearchInventoryConfigurations(List<Integer> types, String search, String table)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select path as path, type as type, folder as folder, name as name from ").append(table);
        List<String> whereClause = new ArrayList<>();
        if (types != null && !types.isEmpty()) {
            if (types.size() > 1) {
                whereClause.add("type in (:types)");
            } else {
                whereClause.add("type=:type");
            }
        } else {
            whereClause.add("type not in (:excludeTypes)");
        }
        if (SOSString.isEmpty(search) || search.equals(FIND_ALL)) {
            search = null;
        } else {
            whereClause.add("lower(name) like :search");
        }
        if (!whereClause.isEmpty()) {
            hql.append(whereClause.stream().collect(Collectors.joining(" and ", " where ", "")));
        }

        Query<InventoryQuickSearchItem> query = getSession().createQuery(hql.toString(), InventoryQuickSearchItem.class);
        if (types != null && !types.isEmpty()) {
            if (types.size() > 1) {
                query.setParameterList("types", types);
            } else {
                query.setParameter("type", types.iterator().next());
            }
        } else {
            query.setParameterList("excludeTypes", Arrays.asList(ConfigurationType.DEPLOYMENTDESCRIPTOR.intValue(), ConfigurationType.DESCRIPTORFOLDER
                    .intValue(), ConfigurationType.FOLDER.intValue()));
        }
        if (search != null) {
            // (only) on the right hand side always %
            query.setParameter("search", SearchStringHelper.globToSqlPattern(search.toLowerCase() + '%').replaceAll("%%+", "%"));
        }
        return getSession().getResultList(query);
    }

    public Stream<InventoryQuickSearchItem> getQuickSearchReleasedSchedulesWithDeployedWorkflows(String controllerId, String search,
            Collection<String> workflowNames) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
        hql.append(" where type=:type");
        if (!SOSString.isEmpty(search) && !search.equals(FIND_ALL)) {
            hql.append(" and lower(name) like :search");
        }
        Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.SCHEDULE.intValue());
        if (!SOSString.isEmpty(search) && !search.equals(FIND_ALL)) {
            // (only) on the right hand side always %
            query.setParameter("search", SearchStringHelper.globToSqlPattern(search.toLowerCase() + '%').replaceAll("%%+", "%"));
        }
        List<DBItemInventoryReleasedConfiguration> result = getSession().getResultList(query);
        if (result == null) {
            return Stream.empty();
        }
        Predicate<DBItemInventoryReleasedConfiguration> hasDeployedWorkflows = i -> {
            try {
                Schedule schedule = (Schedule) JocInventory.content2IJSObject(i.getContent(), ConfigurationType.SCHEDULE);
                return schedule.getWorkflowNames().stream().anyMatch(w -> workflowNames.contains(w));
            } catch (Exception e) {
                return false;
            }
        };
        Function<DBItemInventoryReleasedConfiguration, InventoryQuickSearchItem> mapper = i -> {
            InventoryQuickSearchItem searchItem = new InventoryQuickSearchItem();
            searchItem.setFolder(i.getFolder());
            searchItem.setName(i.getName());
            searchItem.setPath(i.getPath());
            return searchItem;
        };
        return result.stream().filter(hasDeployedWorkflows).map(mapper);
    }

    public List<InventorySearchItem> getBasicSearchInventoryConfigurations(RequestSearchReturnType type, String search, List<String> folders,
            List<String> tags, Boolean unDeployedUnReleaseObjects, Boolean valid) throws SOSHibernateException {

        boolean isReleasable = isReleasable(type);
        boolean searchInFolders = searchInFolders(folders);
        boolean searchInTags = tags != null && !tags.isEmpty();
        boolean onlyUnDeployedUnReleaseObjects = unDeployedUnReleaseObjects == Boolean.TRUE;

        StringBuilder hql = new StringBuilder("select mt.id as id ");
        hql.append(",mt.path as path ");
        hql.append(",mt.type as type ");
        hql.append(",mt.folder as folder ");
        hql.append(",mt.name as name ");
        hql.append(",mt.title as title ");
        hql.append(",mt.valid as valid ");
        hql.append(",mt.deleted as deleted ");
        hql.append(",mt.deployed as deployed ");
        hql.append(",mt.released as released ");
        if (isReleasable) {
            hql.append(",count(irc.id) as countReleased ");
            hql.append(",0 as countDeployed ");
            hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" mt ");
            hql.append("left join ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" irc ");
            hql.append("on mt.id=irc.cid ");
        } else {
            hql.append(",0 as countReleased ");
            hql.append(",count(dh.id) as countDeployed ");
            hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" mt ");
            hql.append("left join ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dh ");
            hql.append("on mt.id=dh.inventoryConfigurationId ");
        }
        if (searchInTags) {
            hql.append("left join ").append(DBLayer.DBITEM_INV_TAGGINGS).append(" tg on tg.cid=mt.id ");
            hql.append("left join ").append(DBLayer.DBITEM_INV_TAGS).append(" t on t.id=tg.tagId ");
        }
        if (RequestSearchReturnType.CALENDAR.equals(type)) {
            hql.append("where mt.type in (:types) ");
        } else {
            hql.append("where mt.type=:type ");
        }
        if (SOSString.isEmpty(search) || search.equals(FIND_ALL)) {
            search = null;
        } else {
            hql.append("and (lower(mt.name) like :search or lower(mt.title) like :search) ");
        }
        if (onlyUnDeployedUnReleaseObjects) {
            if (isReleasable) {
                hql.append("and mt.released = 0 ");
            } else {
                hql.append("and mt.deployed = 0 ");
            }
            if (valid != null) {
                hql.append("and mt.valid = ").append(valid ? 1 : 0).append(" ");
            }
        }
        if (searchInFolders) {
            hql.append("and (").append(foldersHql(folders)).append(") ");
        }
        if (searchInTags) {
            if (tags.size() == 1) {
                hql.append("and t.name=:tag ");
            } else {
                hql.append("and t.name in (:tags) ");
            }
        }
        hql.append("group by mt.id,mt.path,mt.type,mt.folder,mt.name,mt.title,mt.valid,mt.deleted,mt.deployed,mt.released ");

        Query<InventorySearchItem> query = getSession().createQuery(hql.toString(), InventorySearchItem.class);
        if (RequestSearchReturnType.CALENDAR.equals(type)) {
            query.setParameterList("types", JocInventory.getCalendarTypes());
        } else {
            query.setParameter("type", ConfigurationType.valueOf(type.value()).intValue());
        }
        if (search != null) {
            query.setParameter("search", SearchStringHelper.globToSqlPattern('%' + search.toLowerCase() + '%').replaceAll("%%+", "%"));
        }
        if (searchInFolders) {
            foldersQueryParameters(query, folders);
        }
        if (searchInTags) {
            if (tags.size() == 1) {
                query.setParameter("tag", tags.iterator().next());
            } else {
                query.setParameterList("tags", tags);
            }
        }
        return getSession().getResultList(query);
    }

    public List<InventorySearchItem> getBasicSearchDeployedOrReleasedConfigurations(RequestSearchReturnType type, String search, List<String> folders,
            List<String> tags, String controllerId) throws SOSHibernateException {

        boolean isReleasable = isReleasable(type);
        boolean searchInFolders = searchInFolders(folders);
        boolean searchInTags = tags != null && !tags.isEmpty();

        StringBuilder hql = new StringBuilder("select ");
        if (isReleasable) {
            hql.append("mt.cid as id");
            hql.append(",mt.path as path");
            hql.append(",mt.type as type");
            hql.append(",mt.folder as folder ");
            hql.append(",mt.name as name");
            hql.append(",mt.title as title ");
            hql.append(",true as valid ");
            hql.append(",false as deleted ");
            hql.append(",false as deployed ");
            hql.append(",true as released ");
            hql.append(",1 as countReleased ");
            hql.append(",0 as countDeployed ");
            hql.append("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" mt ");
            if (searchInTags) {
                hql.append("left join ").append(DBLayer.DBITEM_INV_TAGGINGS).append(" tg on tg.cid=mt.cid ");
                hql.append("left join ").append(DBLayer.DBITEM_INV_TAGS).append(" t on t.id=tg.tagId ");
            }
        } else {
            hql.append("mt.inventoryConfigurationId as id");
            hql.append(",mt.path as path");
            hql.append(",mt.type as type");
            hql.append(",mt.folder as folder ");
            hql.append(",mt.name as name");
            hql.append(",mt.title as title ");
            hql.append(",mt.controllerId as controllerId ");
            hql.append(",true as valid ");
            hql.append(",false as deleted ");
            hql.append(",true as deployed ");
            hql.append(",false as released ");
            hql.append(",0 as countReleased ");
            hql.append(",1 as countDeployed ");
            hql.append("from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" mt ");
            if (searchInTags) {
                hql.append("left join ").append(DBLayer.DBITEM_INV_TAGGINGS).append(" tg on tg.cid=mt.inventoryConfigurationId ");
                hql.append("left join ").append(DBLayer.DBITEM_INV_TAGS).append(" t on t.id=tg.tagId ");
            }
        }
        if (RequestSearchReturnType.CALENDAR.equals(type)) {
            hql.append("where mt.type in (:types) ");
        } else {
            hql.append("where mt.type=:type ");
        }
        if (SOSString.isEmpty(search) || search.equals(FIND_ALL)) {
            search = null;
        } else {
            hql.append("and (lower(mt.name) like :search or lower(mt.title) like :search) ");
        }
        if (searchInFolders) {
            hql.append("and (").append(foldersHql(folders)).append(") ");
        }
        if (!isReleasable && !SOSString.isEmpty(controllerId)) {
            hql.append("and mt.controllerId=:controllerId ");
        } else {
            controllerId = null;
        }
        if (searchInTags) {
            if (tags.size() == 1) {
                hql.append("and t.name=:tag ");
            } else {
                hql.append("and t.name in (:tags) ");
            }
            if (isReleasable) {
                hql.append("group by mt.cid,mt.path,mt.type,mt.folder,mt.name,mt.title");
            } else {
                hql.append("group by mt.inventoryConfigurationId,mt.path,mt.type,mt.folder,mt.name,mt.title,mt.controllerId");
            }
        }

        Query<InventorySearchItem> query = getSession().createQuery(hql.toString(), InventorySearchItem.class);
        if (RequestSearchReturnType.CALENDAR.equals(type)) {
            query.setParameterList("types", JocInventory.getCalendarTypes());
        } else {
            query.setParameter("type", ConfigurationType.valueOf(type.value()).intValue());
        }
        if (search != null) {
            query.setParameter("search", SearchStringHelper.globToSqlPattern('%' + search.toLowerCase() + '%').replaceAll("%%+", "%"));
        }
        if (searchInFolders) {
            foldersQueryParameters(query, folders);
        }
        if (controllerId != null) {
            query.setParameter("controllerId", controllerId);
        }
        if (searchInTags) {
            if (tags.size() == 1) {
                query.setParameter("tag", tags.iterator().next());
            } else {
                query.setParameterList("tags", tags);
            }
        }
        return getSession().getResultList(query);
    }

    public String getInventoryConfigurationsContent(Long id) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select content from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
        hql.append("where id=:id");

        Query<String> query = getSession().createQuery(hql.toString());
        query.setParameter("id", id);
        return query.getSingleResult();
    }

    public String getDeployedConfigurationsContent(Long id, String controllerId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select content from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" ");
        hql.append("where inventoryConfigurationId=:id ");
        if (controllerId != null) {
            hql.append("and controllerId=:controllerId ");
        }

        Query<String> query = getSession().createQuery(hql.toString());
        query.setParameter("id", id);
        if (controllerId != null) {
            query.setParameter("controllerId", controllerId);
        }
        List<String> l = getSession().getResultList(query);
        return l == null || l.size() == 0 ? null : l.get(0);
    }

    public List<InventorySearchItem> getAdvancedSearchInventoryConfigurations(RequestSearchReturnType type, String search,
            RequestSearchAdvancedItem advanced) throws SOSHibernateException {
        return getAdvancedSearchInventoryConfigurations(type, search, null, null, null, null, advanced);
    }

    // TODO merge all functions ...
    public List<InventorySearchItem> getAdvancedSearchInventoryConfigurations(RequestSearchReturnType type, String search, List<String> folders,
            List<String> tags, Boolean unDeployedUnReleaseObjects, Boolean valid, RequestSearchAdvancedItem advanced) throws SOSHibernateException {

        boolean isReleasable = isReleasable(type);
        boolean searchInFolders = searchInFolders(folders);
        boolean onlyUnDeployedUnReleaseObjects = unDeployedUnReleaseObjects == Boolean.TRUE;
        boolean searchInTags = tags != null && !tags.isEmpty();
        WorkflowSearchOracle oracle = createInstance(getSession());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[getAdvancedSearchInventoryConfigurations][isReleasable=%s][onlyUnDeployedUnReleaseObjects=%s]advanced=%s",
                    isReleasable, onlyUnDeployedUnReleaseObjects, SOSString.toString(advanced, true)));
        }

        StringBuilder hql = new StringBuilder("select mt.id as id ");
        hql.append(",mt.path as path ");
        hql.append(",mt.type as type ");
        hql.append(",mt.folder as folder ");
        hql.append(",mt.name as name ");
        hql.append(",mt.title as title ");
        hql.append(",mt.valid as valid ");
        hql.append(",mt.deleted as deleted ");
        hql.append(",mt.deployed as deployed ");
        hql.append(",mt.released as released ");
        if (isReleasable) {
            hql.append(",count(irc.id) as countReleased ");
            hql.append(",0 as countDeployed ");
            hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" mt ");
            hql.append("left join ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" irc ");
            hql.append("on mt.id=irc.cid ");
            // hql.append("left join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
            // hql.append("on mt.id=sw.inventoryConfigurationId and mt.released=sw.deployed ");
        } else {
            hql.append(",0 as countReleased ");
            hql.append(",count(dh.id) as countDeployed ");
            hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" mt ");
            hql.append("left join ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dh ");
            hql.append("on mt.id=dh.inventoryConfigurationId ");
            if (type.equals(RequestSearchReturnType.WORKFLOW)) {
                hql.append("left join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
                hql.append("on mt.id=sw.inventoryConfigurationId and mt.deployed=sw.deployed ");
            }
        }
        if (searchInTags) {
            hql.append("left join ").append(DBLayer.DBITEM_INV_TAGGINGS).append(" tg on tg.cid=mt.cid ");
            hql.append("left join ").append(DBLayer.DBITEM_INV_TAGS).append(" t on t.id=tg.tagId ");
        }
        if (RequestSearchReturnType.CALENDAR.equals(type)) {
            hql.append("where mt.type in (:types) ");
        } else {
            hql.append("where mt.type=:type ");
        }
        if (SOSString.isEmpty(search) || search.equals(FIND_ALL)) {
            search = null;
        } else {
            hql.append("and (lower(mt.name) like :search or lower(mt.title) like :search) ");
        }
        if (onlyUnDeployedUnReleaseObjects) {
            if (isReleasable) {
                hql.append("and mt.released = 0 ");
            } else {
                hql.append("and mt.deployed = 0 ");
            }
            if (valid != null) {
                hql.append("and mt.valid = ").append(valid ? 1 : 0).append(" ");
            }
        }
        if (searchInFolders) {
            hql.append("and (").append(foldersHql(folders)).append(") ");
        }
        if (searchInTags) {
            if (tags.size() == 1) {
                hql.append("and t.name=:tag ");
            } else {
                hql.append("and t.name in (:tags) ");
            }
        }

        /*------------------------*/
        String fileOrderSource = null;
        String schedule = null;
        String calendar = null;
        String workflow = null;
        Integer jobCountFrom = null;
        Integer jobCountTo = null;
        WorkflowSearchParamResult jobCriticality = null;
        WorkflowSearchParamResult agentName = null;
        WorkflowSearchParamResult jobName = null;
        boolean jobNameExactMatch = advanced.getJobNameExactMatch() != null && advanced.getJobNameExactMatch();
        String jobNameForExactMatch = SOSString.isEmpty(advanced.getJobName()) ? "" : advanced.getJobName();
        WorkflowSearchParamResult jobResource = null;
        WorkflowSearchParamResult jobTemplate = null;
        WorkflowSearchParamResult jobScript = null;
        WorkflowSearchParamResult includeScript = null;
        WorkflowSearchParamResult noticeBoard = null;
        WorkflowSearchParamResult lock = null;
        WorkflowSearchParamResult argumentName = null;
        WorkflowSearchParamResult argumentValue = null;
        WorkflowSearchParamResult envName = null;
        WorkflowSearchParamResult envValue = null;
        boolean isWorkflowType = false;

        boolean selectSchedule = !SOSString.isEmpty(advanced.getSchedule());
        boolean selectCalendar = !SOSString.isEmpty(advanced.getCalendar());
        Integer selectCalendarOfSpecificType = getAdvancedSearchSpecificCalendarTypeValue(type, selectCalendar);
        boolean selectFileOrderSource = !SOSString.isEmpty(advanced.getFileOrderSource());

        switch (type) {
        case WORKFLOW:
            isWorkflowType = true;
            if (selectSchedule || selectCalendar || selectFileOrderSource) {
                String add = "where";
                hql.append("and mt.name in (");
                if (!selectSchedule && !selectCalendar) {// only fileOrderSource
                    hql.append("select ").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subti.jsonContent", "$.workflowName"));
                    hql.append(" ");
                    hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" subti ");
                    hql.append("where subti.type=").append(ConfigurationType.FILEORDERSOURCE.intValue()).append(" ");
                } else {
                    hql.append("select subt.workflowName from ").append(DBLayer.DBITEM_INV_SCHEDULE2WORKFLOWS).append(" subt ");
                    if (selectCalendar) {
                        hql.append(",").append(DBLayer.DBITEM_INV_SCHEDULE2CALENDARS).append(" subtc ");
                    }
                    if (selectCalendarOfSpecificType != null) {
                        hql.append(",").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" subtic ");
                    }
                    if (selectFileOrderSource) {
                        hql.append(",").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" subti ");
                        hql.append("where subti.type=").append(ConfigurationType.FILEORDERSOURCE.intValue()).append(" ");
                        hql.append("and ").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subti.jsonContent", "$.workflowName")).append(
                                "=subt.workflowName").append(" ");
                        add = "and";
                    }
                    if (selectCalendar) {
                        hql.append(add).append(" subt.scheduleName=subtc.scheduleName ");
                        add = "and";
                    }
                    if (selectSchedule && !advanced.getSchedule().equals(FIND_ALL)) {
                        schedule = advanced.getSchedule();
                        hql.append(add).append(" lower(subt.scheduleName) like :schedule ");
                        add = "and";
                    }
                    if (selectCalendar && !advanced.getCalendar().equals(FIND_ALL)) {
                        calendar = advanced.getCalendar();
                        hql.append(add).append(" lower(subtc.calendarName) like :calendar ");
                        add = "and";
                    }
                    if (selectCalendarOfSpecificType != null) {
                        hql.append(add).append(" subtic.type=").append(selectCalendarOfSpecificType).append(" ");
                        hql.append("and subtic.name=subtc.calendarName ");
                        add = "and";
                    }
                }
                if (selectFileOrderSource && !advanced.getFileOrderSource().equals(FIND_ALL)) {
                    fileOrderSource = advanced.getFileOrderSource();
                    hql.append("and lower(subti.name) like :fileOrderSource ");
                }
                hql.append(") ");
            }

            if (advanced.getJobCountFrom() != null) {
                jobCountFrom = advanced.getJobCountFrom();
                hql.append("and sw.jobsCount >= :jobCountFrom ");
            }
            if (advanced.getJobCountTo() != null) {
                jobCountTo = advanced.getJobCountTo();
                hql.append("and sw.jobsCount <= :jobCountTo ");
            }

            jobCriticality = setHQLAndGetParameterValue(oracle, hql, advanced.getJobCriticality());
            agentName = setHQLAndGetParameterValue(oracle, hql, "and", "agentName", advanced.getAgentName(), "sw.jobs", "$.agentIds");
            if (jobNameExactMatch) {
                jobName = setHQLAndGetParameterValueExactMatch(oracle, hql, "and", "jobName", jobNameForExactMatch, "sw.jobs", "$.names");
            } else {
                jobName = setHQLAndGetParameterValue(oracle, hql, "and", "jobName", advanced.getJobName(), "sw.jobs", "$.names");
            }
            jobResource = setHQLAndGetParameterValue(oracle, hql, "and", "jobResources", advanced.getJobResource(), "sw.jobs", "$.jobResources");
            jobScript = setHQLAndGetParameterValue(oracle, hql, "and", "jobScript", advanced.getJobScript(), "sw.jobsScripts", "$.scripts");
            includeScript = setHQLAndGetParameterValue(oracle, hql, "and", "includeScript", advanced.getIncludeScript(), "sw.jobsScripts",
                    "$.scripts");
            jobTemplate = setHQLAndGetParameterValue(oracle, hql, "and", "jobTemplate", advanced.getJobTemplate(), "sw.jobs", "$.jobTemplates");
            noticeBoard = setHQLAndGetParameterValue(oracle, hql, "and", "noticeBoards", advanced.getNoticeBoard(), "sw.instructions",
                    "$.noticeBoardNames");
            lock = setHQLAndGetParameterValue(oracle, hql, "and", "lock", advanced.getLock(), "sw.instructions", "$.lockIds");
            envName = setHQLAndGetParameterValue(oracle, hql, "and", "envName", advanced.getEnvName(), "sw.args", "$.jobEnvNames");
            envValue = setHQLAndGetParameterValue(oracle, hql, "and", "envValue", advanced.getEnvValue(), "sw.args", "$.jobEnvValues");
            argumentName = setHQLAndGetArgNames(oracle, hql, advanced.getArgumentName());
            argumentValue = setHQLAndGetArgValues(oracle, hql, advanced.getArgumentValue());
            break;
        case FILEORDERSOURCE:
        case SCHEDULE:
        case CALENDAR:
        case WORKINGDAYSCALENDAR:
        case NONWORKINGDAYSCALENDAR:
            if (!SOSString.isEmpty(advanced.getWorkflow()) && !advanced.getWorkflow().equals(FIND_ALL)) {
                workflow = advanced.getWorkflow();
            }
            switch (type) {
            case FILEORDERSOURCE:
                if (selectSchedule || selectCalendar) {
                    String add = "where";
                    hql.append("and exists (");
                    hql.append("select subt.scheduleName from ").append(DBLayer.DBITEM_INV_SCHEDULE2WORKFLOWS).append(" subt ");
                    if (selectCalendar) {
                        hql.append(",").append(DBLayer.DBITEM_INV_SCHEDULE2CALENDARS).append(" subtc ");
                    }
                    if (selectCalendarOfSpecificType != null) {
                        hql.append(",").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" subtic ");
                    }
                    if (workflow != null) {
                        hql.append(",").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" subti ");
                    }
                    if (selectCalendar) {
                        hql.append("where subt.scheduleName=subtc.scheduleName ");
                        add = "and";
                    }
                    if (workflow != null) {
                        hql.append(add).append(" subti.type=").append(ConfigurationType.FILEORDERSOURCE.intValue()).append(" ");
                        hql.append("and ").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subti.jsonContent", "$.workflowName")).append(
                                "=subt.workflowName").append(" ");
                        hql.append(add).append(" lower(subt.workflowName) like :workflow ");
                        add = "and";
                    }

                    if (selectSchedule && !advanced.getSchedule().equals(FIND_ALL)) {
                        schedule = advanced.getSchedule();
                        hql.append(add).append(" lower(subt.scheduleName) like :schedule ");
                        add = "and";
                    }
                    if (selectCalendar && !advanced.getCalendar().equals(FIND_ALL)) {
                        calendar = advanced.getCalendar();
                        hql.append(add).append(" lower(subtc.calendarName) like :calendar ");
                        add = "and";
                    }
                    if (selectCalendarOfSpecificType != null) {
                        hql.append(add).append(" subtic.type=").append(selectCalendarOfSpecificType).append(" ");
                        hql.append("and subtic.name=subtc.calendarName ");
                        add = "and";
                    }
                    hql.append(") ");
                } else {
                    if (workflow != null) {
                        hql.append("and lower(");
                        hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "mt.jsonContent", "$.workflowName"));
                        hql.append(") ");
                        hql.append("like :workflow ");
                    }
                }
                break;
            case SCHEDULE:
                if (workflow != null || selectCalendar || selectFileOrderSource) {
                    String add = "where";
                    hql.append("and mt.name in (");
                    hql.append("select subt.scheduleName from ").append(DBLayer.DBITEM_INV_SCHEDULE2WORKFLOWS).append(" subt ");
                    if (selectCalendar) {
                        hql.append(",").append(DBLayer.DBITEM_INV_SCHEDULE2CALENDARS).append(" subtc ");
                    }
                    if (selectCalendarOfSpecificType != null) {
                        hql.append(",").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" subtic ");
                    }
                    if (selectFileOrderSource) {
                        hql.append(",").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" subti ");
                    }
                    if (selectCalendar) {
                        hql.append(add).append(" subt.scheduleName=subtc.scheduleName ");
                        add = "and";
                    }
                    if (selectFileOrderSource) {
                        hql.append(add).append(" subti.type=").append(ConfigurationType.FILEORDERSOURCE.intValue()).append(" ");
                        hql.append("and ").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subti.jsonContent", "$.workflowName")).append(
                                "=subt.workflowName").append(" ");
                        add = "and";
                    }
                    if (workflow != null) {
                        hql.append(add).append(" lower(subt.workflowName) like :workflow ");
                        add = "and";
                    }
                    if (selectCalendar && !advanced.getCalendar().equals(FIND_ALL)) {
                        calendar = advanced.getCalendar();
                        hql.append(add).append(" lower(subtc.calendarName) like :calendar ");
                        add = "and";
                    }
                    if (selectCalendarOfSpecificType != null) {
                        hql.append(add).append(" subtic.type=").append(selectCalendarOfSpecificType).append(" ");
                        hql.append("and subtic.name=subtc.calendarName ");
                        add = "and";
                    }
                    if (selectFileOrderSource && !advanced.getFileOrderSource().equals(FIND_ALL)) {
                        fileOrderSource = advanced.getFileOrderSource();
                        hql.append(add).append(" lower(subti.name) like :fileOrderSource ");
                        add = "and";
                    }
                    hql.append(") ");
                }
                break;
            case CALENDAR:
            case WORKINGDAYSCALENDAR:
            case NONWORKINGDAYSCALENDAR:
                if (workflow != null || selectSchedule || selectFileOrderSource) {
                    hql.append("and mt.name in (");
                    hql.append("select subtc.calendarName from ").append(DBLayer.DBITEM_INV_SCHEDULE2WORKFLOWS).append(" subt ");
                    hql.append(",").append(DBLayer.DBITEM_INV_SCHEDULE2CALENDARS).append(" subtc ");
                    if (selectFileOrderSource) {
                        hql.append(",").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" subti ");
                    }
                    hql.append("where subt.scheduleName=subtc.scheduleName ");
                    if (selectFileOrderSource) {
                        hql.append("and subti.type=").append(ConfigurationType.FILEORDERSOURCE.intValue()).append(" ");
                        hql.append("and ").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subti.jsonContent", "$.workflowName")).append(
                                "=subt.workflowName").append(" ");
                    }
                    if (workflow != null) {
                        hql.append("and lower(subt.workflowName) like :workflow ");
                    }
                    if (selectSchedule && !advanced.getSchedule().equals(FIND_ALL)) {
                        schedule = advanced.getSchedule();
                        hql.append("and lower(subt.scheduleName) like :schedule ");
                    }
                    if (selectFileOrderSource && !advanced.getFileOrderSource().equals(FIND_ALL)) {
                        fileOrderSource = advanced.getFileOrderSource();
                        hql.append("and lower(subti.name) like :fileOrderSource ");
                    }
                    hql.append(") ");
                }
                break;

            default:
                break;
            }

            hql.append("and exists(");// start exists
            hql.append("  select sw.id from ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
            hql.append("  where sw.inventoryConfigurationId in (");
            hql.append("    select ic.id from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
            switch (type) {
            case FILEORDERSOURCE:
                if (selectSchedule || selectCalendar) {
                    hql.append("    where ic.name in(");
                    hql.append("         select subt.workflowName from ").append(DBLayer.DBITEM_INV_SCHEDULE2WORKFLOWS).append(" subt ");
                    hql.append(",").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" subti ");
                    if (selectCalendar) {
                        hql.append(",").append(DBLayer.DBITEM_INV_SCHEDULE2CALENDARS).append(" subtc ");
                    }
                    if (selectCalendarOfSpecificType != null) {
                        hql.append(",").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" subtic ");
                    }
                    hql.append("where subti.type=").append(ConfigurationType.FILEORDERSOURCE.intValue()).append(" ");
                    hql.append("and mt.name=subti.name ");
                    hql.append("and ").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subti.jsonContent", "$.workflowName")).append(
                            "=subt.workflowName").append(" ");
                    if (workflow != null) {
                        hql.append(" and lower(subt.workflowName) like :workflow ");
                    }
                    if (selectCalendar) {
                        hql.append(" and subt.scheduleName=subtc.scheduleName ");
                        if (calendar != null) {
                            hql.append(" and  lower(subtc.calendarName) like :calendar ");
                        }
                    }
                    if (selectCalendarOfSpecificType != null) {
                        hql.append(" and subtic.type=").append(selectCalendarOfSpecificType).append(" ");
                        hql.append(" and subtic.name=subtc.calendarName ");
                    }
                    hql.append(") ");
                    break;
                } else {
                    if (workflow != null) {
                        hql.append(" where ic.name=").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "mt.jsonContent",
                                "$.workflowName"));
                        hql.append(" and lower(ic.name) like :workflow ");
                    } else {
                        hql.append(" where 1=1 ");
                    }
                }
                break;
            case SCHEDULE:
                hql.append("    where ic.name in(");
                hql.append("         select subt.workflowName from ").append(DBLayer.DBITEM_INV_SCHEDULE2WORKFLOWS).append(" subt ");
                if (selectCalendar) {
                    hql.append(",").append(DBLayer.DBITEM_INV_SCHEDULE2CALENDARS).append(" subtc ");
                }
                if (selectCalendarOfSpecificType != null) {
                    hql.append(",").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" subtic ");
                }
                if (selectFileOrderSource) {
                    hql.append(",").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" subti ");
                }
                hql.append(" where mt.name=subt.scheduleName ");
                if (selectFileOrderSource) {
                    hql.append("and subti.type=").append(ConfigurationType.FILEORDERSOURCE.intValue()).append(" ");
                    hql.append("and ").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subti.jsonContent", "$.workflowName")).append(
                            "=subt.workflowName").append(" ");
                    if (fileOrderSource != null) {
                        hql.append("and lower(subti.name) like :fileOrderSource ");
                    }
                }
                if (selectCalendar) {
                    hql.append(" and subt.scheduleName=subtc.scheduleName ");
                    if (calendar != null) {
                        hql.append(" and  lower(subtc.calendarName) like :calendar ");
                    }
                }
                if (selectCalendarOfSpecificType != null) {
                    hql.append(" and subtic.type=").append(selectCalendarOfSpecificType).append(" ");
                    hql.append(" and subtic.name=subtc.calendarName ");
                }
                if (workflow != null) {
                    hql.append(" and lower(subt.workflowName) like :workflow ");
                }
                hql.append(") ");
                break;
            case CALENDAR:
            case WORKINGDAYSCALENDAR:
            case NONWORKINGDAYSCALENDAR:
                hql.append("    where ic.name in(");
                hql.append("         select subt.workflowName from ").append(DBLayer.DBITEM_INV_SCHEDULE2WORKFLOWS).append(" subt ");
                hql.append("         ,").append(DBLayer.DBITEM_INV_SCHEDULE2CALENDARS).append(" subtc ");
                if (selectFileOrderSource) {
                    hql.append(",").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" subti ");
                }
                if (selectCalendarOfSpecificType != null) {
                    hql.append(",").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" subtic ");
                }
                hql.append(" where mt.name=subtc.calendarName ");
                hql.append(" and subt.scheduleName=subtc.scheduleName ");
                if (selectFileOrderSource) {
                    hql.append("and subti.type=").append(ConfigurationType.FILEORDERSOURCE.intValue()).append(" ");
                    hql.append("and ").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subti.jsonContent", "$.workflowName")).append(
                            "=subt.workflowName").append(" ");
                    if (fileOrderSource != null) {
                        hql.append("and lower(subti.name) like :fileOrderSource ");
                    }
                }
                if (workflow != null) {
                    hql.append(" and  lower(subt.workflowName) like :workflow ");
                }
                if (schedule != null) {
                    hql.append(" and  lower(subt.scheduleName) like :schedule ");
                }
                if (selectCalendarOfSpecificType != null) {
                    hql.append(" and subtic.type=").append(selectCalendarOfSpecificType).append(" ");
                    hql.append(" and subtic.name=subtc.calendarName ");
                }
                hql.append(") ");
                break;
            default:
                break;
            }
            hql.append(" and ic.type=").append(ConfigurationType.WORKFLOW.intValue()).append(" ");
            hql.append(") ");

            if (advanced.getJobCountFrom() != null) {
                jobCountFrom = advanced.getJobCountFrom();
                hql.append("and sw.jobsCount >= :jobCountFrom ");
            }
            if (advanced.getJobCountTo() != null) {
                jobCountTo = advanced.getJobCountTo();
                hql.append("and sw.jobsCount <= :jobCountTo ");
            }
            jobCriticality = setHQLAndGetParameterValue(oracle, hql, advanced.getJobCriticality());
            agentName = setHQLAndGetParameterValue(oracle, hql, "and", "agentName", advanced.getAgentName(), "sw.jobs", "$.agentIds");
            if (jobNameExactMatch) {
                jobName = setHQLAndGetParameterValueExactMatch(oracle, hql, "and", "jobName", jobNameForExactMatch, "sw.jobs", "$.names");
            } else {
                jobName = setHQLAndGetParameterValue(oracle, hql, "and", "jobName", advanced.getJobName(), "sw.jobs", "$.names");
            }
            jobResource = setHQLAndGetParameterValue(oracle, hql, "and", "jobResources", advanced.getJobResource(), "sw.jobs", "$.jobResources");
            jobScript = setHQLAndGetParameterValue(oracle, hql, "and", "jobScript", advanced.getJobScript(), "sw.jobsScripts", "$.scripts");
            includeScript = setHQLAndGetParameterValue(oracle, hql, "and", "includeScript", advanced.getIncludeScript(), "sw.jobsScripts",
                    "$.scripts");
            jobTemplate = setHQLAndGetParameterValue(oracle, hql, "and", "jobTemplate", advanced.getJobTemplate(), "sw.jobs", "$.jobTemplates");
            noticeBoard = setHQLAndGetParameterValue(oracle, hql, "and", "noticeBoards", advanced.getNoticeBoard(), "sw.instructions",
                    "$.noticeBoardNames");
            lock = setHQLAndGetParameterValue(oracle, hql, "and", "lock", advanced.getLock(), "sw.instructions", "$.lockIds");
            envName = setHQLAndGetParameterValue(oracle, hql, "and", "envName", advanced.getEnvName(), "sw.args", "$.jobEnvNames");
            envValue = setHQLAndGetParameterValue(oracle, hql, "and", "envValue", advanced.getEnvValue(), "sw.args", "$.jobEnvValues");
            hql.append(")");// end exists
            break;
        case JOBRESOURCE:
        case NOTICEBOARD:
        case LOCK:
        case REPORT:
        default:
            break;
        }
        hql.append(" group by mt.id,mt.path,mt.type,mt.folder,mt.name,mt.title,mt.valid,mt.deleted,mt.deployed,mt.released ");

        Query<InventorySearchItem> query = getSession().createQuery(hql.toString(), InventorySearchItem.class);
        if (RequestSearchReturnType.CALENDAR.equals(type)) {
            query.setParameterList("types", JocInventory.getCalendarTypes());
        } else {
            query.setParameter("type", ConfigurationType.valueOf(type.value()).intValue());
        }
        if (search != null) {
            query.setParameter("search", SearchStringHelper.globToSqlPattern('%' + search.toLowerCase() + '%').replaceAll("%%+", "%"));
        }
        if (searchInFolders) {
            foldersQueryParameters(query, folders);
        }
        if (searchInTags) {
            if (tags.size() == 1) {
                query.setParameter("tag", tags.iterator().next());
            } else {
                query.setParameterList("tags", tags);
            }
        }
        /*------------------------*/
        if (fileOrderSource != null) {
            query.setParameter("fileOrderSource", '%' + fileOrderSource.toLowerCase() + '%');
        }
        if (schedule != null) {
            query.setParameter("schedule", '%' + schedule.toLowerCase() + '%');
        }
        if (calendar != null) {
            query.setParameter("calendar", '%' + calendar.toLowerCase() + '%');
        }
        if (workflow != null) {
            query.setParameter("workflow", '%' + workflow.toLowerCase() + '%');
        }
        if (jobCountFrom != null) {
            query.setParameter("jobCountFrom", jobCountFrom);
        }
        if (jobCountTo != null) {
            query.setParameter("jobCountTo", jobCountTo);
        }
        if (agentName != null) {
            query.setParameter("agentName", agentName.getParamValueForLike());
        }
        if (jobName != null) {
            if (jobNameExactMatch) {
                if (oracle == null) {
                    query.setParameter("jobName", jobName.getParamValueForLike());
                } else {
                    query.setParameter("jobName", jobName.value);
                }
            } else {
                query.setParameter("jobName", jobName.getParamValueForLike());
            }
        }
        if (jobCriticality != null) {
            query.setParameter("jobCriticality", jobCriticality.getParamValueForLike());
        }
        if (jobResource != null) {
            query.setParameter("jobResources", jobResource.getParamValueForLike());
        }
        if (jobScript != null) {
            query.setParameter("jobScript", jobScript.getParamValueForLike());
        }
        if (includeScript != null) {
            query.setParameter("includeScript", includeScript.getParamValueForLike());
        }
        if (noticeBoard != null) {
            query.setParameter("noticeBoards", noticeBoard.getParamValueForLike());
        }
        if (jobTemplate != null) {
            query.setParameter("jobTemplate", jobTemplate.getParamValueForLike());
        }
        if (lock != null) {
            query.setParameter("lock", lock.getParamValueForLike());
        }
        if (envName != null) {
            query.setParameter("envName", envName.getParamValueForLike());
        }
        if (envValue != null) {
            query.setParameter("envValue", envValue.getParamValueForLike());
        }
        if (argumentName != null) {
            query.setParameter("argumentName", argumentName.getParamValueForLike());
        }
        if (argumentValue != null) {
            query.setParameter("argumentValue", argumentValue.getParamValueForLike());
        }
        if (oracle == null && isWorkflowType && jobNameExactMatch && !SOSString.isEmpty(jobNameForExactMatch)) {
            return checkJobNameExactMatch(getSession().getResultList(query), false, jobNameForExactMatch);
        } else {
            return getSession().getResultList(query);
        }
    }

    // extra check because possible database case insensitivity
    private List<InventorySearchItem> checkJobNameExactMatch(List<InventorySearchItem> workflows, boolean deployedOrReleased, String jobName)
            throws SOSHibernateException {
        if (workflows == null || workflows.size() == 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[checkJobNameExactMatch][skip]");
            }
            return workflows;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[checkJobNameExactMatch][deployedOrReleased=" + deployedOrReleased + "]jobName=" + jobName);
        }
        List<InventorySearchItem> result = new ArrayList<>();
        for (InventorySearchItem item : workflows) {
            String content = null;
            if (deployedOrReleased) {
                content = getDeployedConfigurationsContent(item.getId(), item.getControllerId());
            } else {
                content = getInventoryConfigurationsContent(item.getId());
            }
            if (!SOSString.isEmpty(content)) {
                Workflow w;
                try {
                    w = (Workflow) Globals.objectMapper.readValue(content, Workflow.class);
                    WorkflowSearcher ws = new WorkflowSearcher(w);
                    WorkflowJob job = ws.getJob(jobName);
                    if (job != null) {
                        result.add(item);
                    }
                } catch (Throwable e) {
                    LOGGER.error(String.format("[workflow][id=%s]%s", item.getId(), e.toString()), e);
                }
            }
        }
        return result;
    }

    public List<InventorySearchItem> getAdvancedSearchDeployedOrReleasedConfigurations(RequestSearchReturnType type, String search,
            List<String> folders, List<String> tags, RequestSearchAdvancedItem advanced, String controllerId) throws SOSHibernateException {

        boolean isReleasable = isReleasable(type);
        boolean searchInFolders = searchInFolders(folders);
        boolean searchInTags = tags != null && !tags.isEmpty();
        WorkflowSearchOracle oracle = createInstance(getSession());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[getAdvancedSearchDeployedOrReleasedConfigurations][isReleasable=%s]advanced=%s", isReleasable, SOSString
                    .toString(advanced, true)));
        }

        StringBuilder hql = new StringBuilder("select ");
        if (isReleasable) {
            hql.append("mt.cid as id");
            hql.append(",mt.path as path");
            hql.append(",mt.type as type");
            hql.append(",mt.folder as folder ");
            hql.append(",mt.name as name");
            hql.append(",mt.title as title ");
            hql.append(",true as valid ");
            hql.append(",false as deleted ");
            hql.append(",false as deployed ");
            hql.append(",true as released ");
            hql.append(",1 as countReleased ");
            hql.append(",0 as countDeployed ");
            hql.append("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" mt ");
            if (searchInTags) {
                hql.append("left join ").append(DBLayer.DBITEM_INV_TAGGINGS).append(" tg on tg.cid=mt.cid ");
                hql.append("left join ").append(DBLayer.DBITEM_INV_TAGS).append(" t on t.id=tg.tagId ");
            }
        } else {
            hql.append("mt.inventoryConfigurationId as id");
            hql.append(",mt.path as path");
            hql.append(",mt.type as type");
            hql.append(",mt.folder as folder ");
            hql.append(",mt.name as name");
            hql.append(",mt.title as title ");
            hql.append(",mt.controllerId as controllerId ");
            hql.append(",true as valid ");
            hql.append(",false as deleted ");
            hql.append(",true as deployed ");
            hql.append(",false as released ");
            hql.append(",0 as countReleased ");
            hql.append(",1 as countDeployed ");
            hql.append("from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" mt ");
            if (type.equals(RequestSearchReturnType.WORKFLOW)) {
                // hql.append("left join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
                // hql.append("on mt.inventoryConfigurationId=sw.inventoryConfigurationId and sw.deployed=1 ");
                hql.append("left join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
                hql.append("on mt.inventoryConfigurationId=sw.inventoryConfigurationId and sw.deployed=1 ");
                hql.append("left join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS_DEPLOYMENT_HISTORY).append(" swdh ");
                hql.append("on swdh.deploymentHistoryId=mt.id and swdh.searchWorkflowId=sw.id ");
                if (searchInTags) {
                    hql.append("left join ").append(DBLayer.DBITEM_INV_TAGGINGS).append(" tg on tg.cid=mt.inventoryConfigurationId ");
                    hql.append("left join ").append(DBLayer.DBITEM_INV_TAGS).append(" t on t.id=tg.tagId ");
                }
            }
        }
        if (RequestSearchReturnType.CALENDAR.equals(type)) {
            hql.append("where mt.type in (:types) ");
        } else {
            hql.append("where mt.type=:type ");
        }
        if (SOSString.isEmpty(search) || search.equals(FIND_ALL)) {
            search = null;
        } else {
            hql.append("and (lower(mt.name) like :search or lower(mt.title) like :search) ");
        }
        if (searchInFolders) {
            hql.append("and (").append(foldersHql(folders)).append(") ");
        }

        controllerId = SOSString.isEmpty(controllerId) ? null : controllerId;
        if (!isReleasable && controllerId != null) {
            hql.append("and mt.controllerId=:controllerId ");
        }

        if (searchInTags) {
            if (tags.size() == 1) {
                hql.append("and t.name=:tag ");
            } else {
                hql.append("and t.name in (:tags) ");
            }
        }

        /*------------------------*/
        String fileOrderSource = null;
        String schedule = null;
        String calendar = null;
        String workflow = null;
        Integer jobCountFrom = null;
        Integer jobCountTo = null;
        WorkflowSearchParamResult jobCriticality = null;
        WorkflowSearchParamResult agentName = null;
        WorkflowSearchParamResult jobName = null;
        boolean jobNameExactMatch = advanced.getJobNameExactMatch() != null && advanced.getJobNameExactMatch();
        String jobNameForExactMatch = SOSString.isEmpty(advanced.getJobName()) ? "" : advanced.getJobName();
        WorkflowSearchParamResult jobResource = null;
        WorkflowSearchParamResult jobTemplate = null;
        WorkflowSearchParamResult jobScript = null;
        WorkflowSearchParamResult includeScript = null;
        WorkflowSearchParamResult noticeBoard = null;
        WorkflowSearchParamResult lock = null;
        WorkflowSearchParamResult envName = null;
        WorkflowSearchParamResult envValue = null;
        WorkflowSearchParamResult argumentName = null;
        WorkflowSearchParamResult argumentValue = null;
        boolean isWorkflowType = false;

        boolean selectSchedule = !SOSString.isEmpty(advanced.getSchedule());
        boolean selectCalendar = !SOSString.isEmpty(advanced.getCalendar());
        Integer selectCalendarOfSpecificType = getAdvancedSearchSpecificCalendarTypeValue(type, selectCalendar);
        boolean selectFileOrderSource = !SOSString.isEmpty(advanced.getFileOrderSource());

        switch (type) {
        case WORKFLOW:
            isWorkflowType = true;
            if (selectSchedule || selectCalendar || selectFileOrderSource) {
                String add = "where";
                hql.append("and mt.name in (");
                if (!selectSchedule && !selectCalendar) {// only fileOrderSource
                    hql.append("select ").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subti.content", "$.workflowName"));
                    hql.append(" ");
                    hql.append("from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" subti ");
                    hql.append("where subti.type=").append(ConfigurationType.FILEORDERSOURCE.intValue()).append(" ");
                } else {
                    hql.append("select subt.workflowName from ").append(DBLayer.DBITEM_INV_RELEASED_SCHEDULE2WORKFLOWS).append(" subt ");
                    if (selectCalendar) {
                        hql.append(",").append(DBLayer.DBITEM_INV_RELEASED_SCHEDULE2CALENDARS).append(" subtc ");
                    }
                    if (selectCalendarOfSpecificType != null) {
                        hql.append(",").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" subtic ");
                    }
                    if (selectFileOrderSource) {
                        hql.append(",").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" subti ");
                        hql.append("where subti.type=").append(ConfigurationType.FILEORDERSOURCE.intValue()).append(" ");
                        hql.append("and ").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subti.content", "$.workflowName")).append(
                                "=subt.workflowName").append(" ");
                        add = "and";
                    }
                    if (selectCalendar) {
                        hql.append(add).append(" subt.scheduleName=subtc.scheduleName ");
                        add = "and";
                    }
                    if (selectSchedule && !advanced.getSchedule().equals(FIND_ALL)) {
                        schedule = advanced.getSchedule();
                        hql.append(add).append(" lower(subt.scheduleName) like :schedule ");
                        add = "and";
                    }
                    if (selectCalendar && !advanced.getCalendar().equals(FIND_ALL)) {
                        calendar = advanced.getCalendar();
                        hql.append(add).append(" lower(subtc.calendarName) like :calendar ");
                        add = "and";
                    }
                    if (selectCalendarOfSpecificType != null) {
                        hql.append(add).append(" subtic.type=").append(selectCalendarOfSpecificType).append(" ");
                        hql.append("and subtic.name=subtc.calendarName ");
                        add = "and";
                    }
                }

                if (selectFileOrderSource) {
                    if (controllerId != null) {
                        hql.append("and subti.controllerId=:controllerId ");
                    }
                    if (!advanced.getFileOrderSource().equals(FIND_ALL)) {
                        fileOrderSource = advanced.getFileOrderSource();
                        hql.append("and lower(subti.name) like :fileOrderSource ");
                    }
                }
                hql.append(") ");
            }

            if (advanced.getJobCountFrom() != null) {
                jobCountFrom = advanced.getJobCountFrom();
                hql.append("and sw.jobsCount >= :jobCountFrom ");
            }
            if (advanced.getJobCountTo() != null) {
                jobCountTo = advanced.getJobCountTo();
                hql.append("and sw.jobsCount <= :jobCountTo ");
            }

            jobCriticality = setHQLAndGetParameterValue(oracle, hql, advanced.getJobCriticality());
            agentName = setHQLAndGetParameterValue(oracle, hql, "and", "agentName", advanced.getAgentName(), "sw.jobs", "$.agentIds");
            if (jobNameExactMatch) {
                jobName = setHQLAndGetParameterValueExactMatch(oracle, hql, "and", "jobName", jobNameForExactMatch, "sw.jobs", "$.names");
            } else {
                jobName = setHQLAndGetParameterValue(oracle, hql, "and", "jobName", advanced.getJobName(), "sw.jobs", "$.names");
            }
            jobResource = setHQLAndGetParameterValue(oracle, hql, "and", "jobResources", advanced.getJobResource(), "sw.jobs", "$.jobResources");
            jobTemplate = setHQLAndGetParameterValue(oracle, hql, "and", "jobTemplate", advanced.getJobTemplate(), "sw.jobs", "$.jobTemplates");
            jobScript = setHQLAndGetParameterValue(oracle, hql, "and", "jobScript", advanced.getJobScript(), "sw.jobsScripts", "$.scripts");
            includeScript = setHQLAndGetParameterValue(oracle, hql, "and", "includeScript", advanced.getIncludeScript(), "sw.jobsScripts",
                    "$.scripts");
            noticeBoard = setHQLAndGetParameterValue(oracle, hql, "and", "noticeBoards", advanced.getNoticeBoard(), "sw.instructions",
                    "$.noticeBoardNames");
            lock = setHQLAndGetParameterValue(oracle, hql, "and", "lock", advanced.getLock(), "sw.instructions", "$.lockIds");
            envName = setHQLAndGetParameterValue(oracle, hql, "and", "envName", advanced.getEnvName(), "sw.args", "$.jobEnvNames");
            envValue = setHQLAndGetParameterValue(oracle, hql, "and", "envValue", advanced.getEnvValue(), "sw.args", "$.jobEnvValues");
            argumentName = setHQLAndGetArgNames(oracle, hql, advanced.getArgumentName());
            argumentValue = setHQLAndGetArgValues(oracle, hql, advanced.getArgumentValue());
            break;
        case FILEORDERSOURCE:
        case SCHEDULE:
        case CALENDAR:
        case NONWORKINGDAYSCALENDAR:
        case WORKINGDAYSCALENDAR:
            if (!SOSString.isEmpty(advanced.getWorkflow()) && !advanced.getWorkflow().equals(FIND_ALL)) {
                workflow = advanced.getWorkflow();
            }
            switch (type) {
            case FILEORDERSOURCE:
                if (selectSchedule || selectCalendar) {
                    String add = "where";
                    hql.append("and exists (");
                    hql.append("select subt.scheduleName from ").append(DBLayer.DBITEM_INV_RELEASED_SCHEDULE2WORKFLOWS).append(" subt ");
                    if (selectCalendar) {
                        hql.append(",").append(DBLayer.DBITEM_INV_RELEASED_SCHEDULE2CALENDARS).append(" subtc ");
                    }
                    if (selectCalendarOfSpecificType != null) {
                        hql.append(",").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" subtic ");
                    }
                    if (workflow != null) {
                        hql.append(",").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" subti ");
                    }
                    if (selectCalendar) {
                        hql.append("where subt.scheduleName=subtc.scheduleName ");
                        add = "and";
                    }
                    if (workflow != null) {
                        hql.append(add).append(" subti.type=").append(ConfigurationType.FILEORDERSOURCE.intValue()).append(" ");
                        hql.append("and ").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subti.content", "$.workflowName")).append(
                                "=subt.workflowName").append(" ");
                        hql.append(add).append(" lower(subt.workflowName) like :workflow ");
                        if (controllerId != null) {
                            hql.append("and subti.controllerId=:controllerId ");
                        }
                        add = "and";
                    }

                    if (selectSchedule && !advanced.getSchedule().equals(FIND_ALL)) {
                        schedule = advanced.getSchedule();
                        hql.append(add).append(" lower(subt.scheduleName) like :schedule ");
                        add = "and";
                    }
                    if (selectCalendar && !advanced.getCalendar().equals(FIND_ALL)) {
                        calendar = advanced.getCalendar();
                        hql.append(add).append(" lower(subtc.calendarName) like :calendar ");
                        add = "and";
                    }
                    if (selectCalendarOfSpecificType != null) {
                        hql.append(add).append(" subtic.type=").append(selectCalendarOfSpecificType).append(" ");
                        hql.append("and subtic.name=subtc.calendarName ");
                        add = "and";
                    }
                    hql.append(") ");
                } else {
                    // controllerId already used
                    if (workflow != null) {
                        hql.append("and lower(");
                        hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "mt.content", "$.workflowName"));
                        hql.append(") ");
                        hql.append("like :workflow ");
                    }
                }
                break;
            case SCHEDULE:
                if (workflow != null || selectCalendar || selectFileOrderSource) {
                    String add = "where";
                    hql.append("and mt.name in (");
                    hql.append("select subt.scheduleName from ").append(DBLayer.DBITEM_INV_RELEASED_SCHEDULE2WORKFLOWS).append(" subt ");
                    if (selectCalendar) {
                        hql.append(",").append(DBLayer.DBITEM_INV_RELEASED_SCHEDULE2CALENDARS).append(" subtc ");
                    }
                    if (selectCalendarOfSpecificType != null) {
                        hql.append(",").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" subtic ");
                    }
                    if (selectFileOrderSource) {
                        hql.append(",").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" subti ");
                    }
                    if (selectCalendar) {
                        hql.append(add).append(" subt.scheduleName=subtc.scheduleName ");
                        add = "and";
                    }
                    if (selectFileOrderSource) {
                        hql.append(add).append(" subti.type=").append(ConfigurationType.FILEORDERSOURCE.intValue()).append(" ");
                        hql.append("and ").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subti.content", "$.workflowName")).append(
                                "=subt.workflowName").append(" ");
                        if (controllerId != null) {
                            hql.append("and subti.controllerId=:controllerId ");
                        }
                        add = "and";
                    }
                    if (workflow != null) {
                        hql.append(add).append(" lower(subt.workflowName) like :workflow ");
                        add = "and";
                    }
                    if (selectCalendar && !advanced.getCalendar().equals(FIND_ALL)) {
                        calendar = advanced.getCalendar();
                        hql.append(add).append(" lower(subtc.calendarName) like :calendar ");
                        add = "and";
                    }
                    if (selectCalendarOfSpecificType != null) {
                        hql.append(add).append(" subtic.type=").append(selectCalendarOfSpecificType).append(" ");
                        hql.append("and subtic.name=subtc.calendarName ");
                        add = "and";
                    }
                    if (selectFileOrderSource && !advanced.getFileOrderSource().equals(FIND_ALL)) {
                        fileOrderSource = advanced.getFileOrderSource();
                        hql.append(add).append(" lower(subti.name) like :fileOrderSource ");
                        add = "and";
                    }
                    hql.append(") ");
                }
                break;
            case CALENDAR:
            case WORKINGDAYSCALENDAR:
            case NONWORKINGDAYSCALENDAR:
                if (workflow != null || selectSchedule || selectFileOrderSource) {
                    hql.append("and mt.name in (");
                    hql.append("select subtc.calendarName from ").append(DBLayer.DBITEM_INV_RELEASED_SCHEDULE2WORKFLOWS).append(" subt ");
                    hql.append(",").append(DBLayer.DBITEM_INV_RELEASED_SCHEDULE2CALENDARS).append(" subtc ");
                    if (selectFileOrderSource) {
                        hql.append(",").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" subti ");
                    }
                    hql.append("where subt.scheduleName=subtc.scheduleName ");
                    if (selectFileOrderSource) {
                        hql.append("and subti.type=").append(ConfigurationType.FILEORDERSOURCE.intValue()).append(" ");
                        hql.append("and ").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subti.content", "$.workflowName")).append(
                                "=subt.workflowName").append(" ");
                        if (controllerId != null) {
                            hql.append("and subti.controllerId=:controllerId ");
                        }
                    }
                    if (workflow != null) {
                        hql.append("and lower(subt.workflowName) like :workflow ");
                    }
                    if (selectSchedule && !advanced.getSchedule().equals(FIND_ALL)) {
                        schedule = advanced.getSchedule();
                        hql.append("and lower(subt.scheduleName) like :schedule ");
                    }
                    if (selectFileOrderSource && !advanced.getFileOrderSource().equals(FIND_ALL)) {
                        fileOrderSource = advanced.getFileOrderSource();
                        hql.append("and lower(subti.name) like :fileOrderSource ");
                    }
                    hql.append(") ");
                }
                break;

            default:
                break;
            }

            // ----------------------------------------------------
            hql.append("and exists(");// start exists
            hql.append("  select sw.id from ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
            hql.append("  ,").append(DBLayer.DBITEM_SEARCH_WORKFLOWS_DEPLOYMENT_HISTORY).append(" swdh ");
            hql.append("  where sw.id=swdh.searchWorkflowId ");
            if (controllerId != null) {
                hql.append(" and swdh.controllerId=:controllerId ");
            }
            hql.append("  and swdh.deploymentHistoryId in (");
            hql.append("    select ic.id from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" ic ");
            switch (type) {
            case FILEORDERSOURCE:
                if (selectSchedule || selectCalendar) {
                    hql.append("    where ic.name in(");
                    hql.append("         select subt.workflowName from ").append(DBLayer.DBITEM_INV_RELEASED_SCHEDULE2WORKFLOWS).append(" subt ");
                    hql.append(",").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" subti ");
                    if (selectCalendar) {
                        hql.append(",").append(DBLayer.DBITEM_INV_RELEASED_SCHEDULE2CALENDARS).append(" subtc ");
                    }
                    if (selectCalendarOfSpecificType != null) {
                        hql.append(",").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" subtic ");
                    }
                    hql.append("where subti.type=").append(ConfigurationType.FILEORDERSOURCE.intValue()).append(" ");
                    hql.append("and mt.name=subti.name ");
                    hql.append("and ").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subti.content", "$.workflowName")).append(
                            "=subt.workflowName").append(" ");
                    if (workflow != null) {
                        hql.append(" and lower(subt.workflowName) like :workflow ");
                    }
                    if (selectCalendar) {
                        hql.append(" and subt.scheduleName=subtc.scheduleName ");
                        if (calendar != null) {
                            hql.append(" and  lower(subtc.calendarName) like :calendar ");
                        }
                    }
                    if (selectCalendarOfSpecificType != null) {
                        hql.append(" and subtic.type=").append(selectCalendarOfSpecificType).append(" ");
                        hql.append(" and subtic.name=subtc.calendarName ");
                    }
                    if (controllerId != null) {
                        hql.append(" and subti.controllerId=:controllerId ");
                    }
                    hql.append(") ");
                    break;
                } else {
                    if (workflow != null) {
                        hql.append(" where ic.name=").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "mt.content", "$.workflowName"));
                        hql.append(" and lower(ic.name) like :workflow ");
                    } else {
                        hql.append(" where 1=1 ");
                    }
                }
                break;
            case SCHEDULE:
                hql.append("    where ic.name in(");
                hql.append("         select subt.workflowName from ").append(DBLayer.DBITEM_INV_RELEASED_SCHEDULE2WORKFLOWS).append(" subt ");
                if (selectCalendar) {
                    hql.append(",").append(DBLayer.DBITEM_INV_RELEASED_SCHEDULE2CALENDARS).append(" subtc ");
                }
                if (selectCalendarOfSpecificType != null) {
                    hql.append(",").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" subtic ");
                }
                if (selectFileOrderSource) {
                    hql.append(",").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" subti ");
                }
                hql.append(" where mt.name=subt.scheduleName ");
                if (selectFileOrderSource) {
                    hql.append("and subti.type=").append(ConfigurationType.FILEORDERSOURCE.intValue()).append(" ");
                    hql.append("and ").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subti.content", "$.workflowName")).append(
                            "=subt.workflowName").append(" ");
                    if (fileOrderSource != null) {
                        hql.append("and lower(subti.name) like :fileOrderSource ");
                    }
                    if (controllerId != null) {
                        hql.append(" and subti.controllerId=:controllerId ");
                    }
                }
                if (selectCalendar) {
                    hql.append(" and subt.scheduleName=subtc.scheduleName ");
                    if (calendar != null) {
                        hql.append(" and  lower(subtc.calendarName) like :calendar ");
                    }
                }
                if (selectCalendarOfSpecificType != null) {
                    hql.append(" and subtic.type=").append(selectCalendarOfSpecificType).append(" ");
                    hql.append(" and subtic.name=subtc.calendarName ");
                }
                if (workflow != null) {
                    hql.append(" and lower(subt.workflowName) like :workflow ");
                }
                hql.append(") ");
                break;
            case CALENDAR:
            case WORKINGDAYSCALENDAR:
            case NONWORKINGDAYSCALENDAR:
                hql.append("    where ic.name in(");
                hql.append("         select subt.workflowName from ").append(DBLayer.DBITEM_INV_RELEASED_SCHEDULE2WORKFLOWS).append(" subt ");
                hql.append("         ,").append(DBLayer.DBITEM_INV_RELEASED_SCHEDULE2CALENDARS).append(" subtc ");
                if (selectFileOrderSource) {
                    hql.append(",").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" subti ");
                }
                if (selectCalendarOfSpecificType != null) {
                    hql.append(",").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" subtic ");
                }
                hql.append(" where mt.name=subtc.calendarName ");
                hql.append(" and subt.scheduleName=subtc.scheduleName ");
                if (selectFileOrderSource) {
                    hql.append("and subti.type=").append(ConfigurationType.FILEORDERSOURCE.intValue()).append(" ");
                    hql.append("and ").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subti.content", "$.workflowName")).append(
                            "=subt.workflowName").append(" ");
                    if (fileOrderSource != null) {
                        hql.append("and lower(subti.name) like :fileOrderSource ");
                    }
                    if (controllerId != null) {
                        hql.append(" and subti.controllerId=:controllerId ");
                    }
                }
                if (workflow != null) {
                    hql.append(" and  lower(subt.workflowName) like :workflow ");
                }
                if (schedule != null) {
                    hql.append(" and  lower(subt.scheduleName) like :schedule ");
                }
                if (selectCalendarOfSpecificType != null) {
                    hql.append(" and subtic.type=").append(selectCalendarOfSpecificType).append(" ");
                    hql.append(" and subtic.name=subtc.calendarName ");
                }
                hql.append(") ");
                break;
            default:
                break;
            }
            // -------------------------------------------------------
            hql.append("    and ic.type=").append(ConfigurationType.WORKFLOW.intValue()).append(" ");
            if (controllerId != null) {
                hql.append("and ic.controllerId=:controllerId ");
            }
            hql.append("  ) ");
            if (advanced.getJobCountFrom() != null) {
                jobCountFrom = advanced.getJobCountFrom();
                hql.append("and sw.jobsCount >= :jobCountFrom ");
            }
            if (advanced.getJobCountTo() != null) {
                jobCountTo = advanced.getJobCountTo();
                hql.append("and sw.jobsCount <= :jobCountTo ");
            }
            jobCriticality = setHQLAndGetParameterValue(oracle, hql, advanced.getJobCriticality());
            agentName = setHQLAndGetParameterValue(oracle, hql, "and", "agentName", advanced.getAgentName(), "sw.jobs", "$.agentIds");
            if (jobNameExactMatch) {
                jobName = setHQLAndGetParameterValueExactMatch(oracle, hql, "and", "jobName", jobNameForExactMatch, "sw.jobs", "$.names");
            } else {
                jobName = setHQLAndGetParameterValue(oracle, hql, "and", "jobName", advanced.getJobName(), "sw.jobs", "$.names");
            }
            jobResource = setHQLAndGetParameterValue(oracle, hql, "and", "jobResources", advanced.getJobResource(), "sw.jobs", "$.jobResources");
            jobTemplate = setHQLAndGetParameterValue(oracle, hql, "and", "jobTemplate", advanced.getJobTemplate(), "sw.jobs", "$.jobTemplates");
            jobScript = setHQLAndGetParameterValue(oracle, hql, "and", "jobScript", advanced.getJobScript(), "sw.jobsScripts", "$.scripts");
            includeScript = setHQLAndGetParameterValue(oracle, hql, "and", "includeScript", advanced.getIncludeScript(), "sw.jobsScripts",
                    "$.scripts");
            noticeBoard = setHQLAndGetParameterValue(oracle, hql, "and", "noticeBoards", advanced.getNoticeBoard(), "sw.instructions",
                    "$.noticeBoardNames");
            lock = setHQLAndGetParameterValue(oracle, hql, "and", "lock", advanced.getLock(), "sw.instructions", "$.lockIds");
            envName = setHQLAndGetParameterValue(oracle, hql, "and", "envName", advanced.getEnvName(), "sw.args", "$.jobEnvNames");
            envValue = setHQLAndGetParameterValue(oracle, hql, "and", "envValue", advanced.getEnvValue(), "sw.args", "$.jobEnvValues");
            hql.append(")");// end exists
            break;
        case JOBRESOURCE:
        case NOTICEBOARD:
        case LOCK:
        case REPORT:
        default:
            break;
        }

        if (searchInTags) {
            if (isReleasable) {
                hql.append(" group by mt.cid,mt.path,mt.type,mt.folder,mt.name,mt.title ");
            } else {
                hql.append(" group by mt.inventoryConfigurationId,mt.path,mt.type,mt.folder,mt.name,mt.title,mt.controllerId ");
            }
        }
        Query<InventorySearchItem> query = getSession().createQuery(hql.toString(), InventorySearchItem.class);
        if (RequestSearchReturnType.CALENDAR.equals(type)) {
            query.setParameterList("types", JocInventory.getCalendarTypes());
        } else {
            query.setParameter("type", ConfigurationType.valueOf(type.value()).intValue());
        }
        if (search != null) {
            query.setParameter("search", SearchStringHelper.globToSqlPattern('%' + search.toLowerCase() + '%').replaceAll("%%+", "%"));
        }
        if (searchInFolders) {
            foldersQueryParameters(query, folders);
        }
        if (controllerId != null) {
            query.setParameter("controllerId", controllerId);
        }
        if (searchInTags) {
            if (tags.size() == 1) {
                query.setParameter("tag", tags.iterator().next());
            } else {
                query.setParameterList("tags", tags);
            }
        }
        /*------------------------*/
        if (fileOrderSource != null) {
            query.setParameter("fileOrderSource", '%' + fileOrderSource.toLowerCase() + '%');
        }
        if (schedule != null) {
            query.setParameter("schedule", '%' + schedule.toLowerCase() + '%');
        }
        if (calendar != null) {
            query.setParameter("calendar", '%' + calendar.toLowerCase() + '%');
        }
        if (workflow != null) {
            query.setParameter("workflow", '%' + workflow.toLowerCase() + '%');
        }
        if (jobCountFrom != null) {
            query.setParameter("jobCountFrom", jobCountFrom);
        }
        if (jobCountTo != null) {
            query.setParameter("jobCountTo", jobCountTo);
        }
        if (agentName != null) {
            query.setParameter("agentName", agentName.getParamValueForLike());
        }
        if (jobName != null) {
            if (jobNameExactMatch) {
                if (oracle == null) {
                    query.setParameter("jobName", jobName.getParamValueForLike());
                } else {
                    query.setParameter("jobName", jobName.value);
                }
            } else {
                query.setParameter("jobName", jobName.getParamValueForLike());
            }
        }
        if (jobCriticality != null) {
            query.setParameter("jobCriticality", jobCriticality.getParamValueForLike());
        }
        if (jobResource != null) {
            query.setParameter("jobResources", jobResource.getParamValueForLike());
        }
        if (jobTemplate != null) {
            query.setParameter("jobTemplate", jobTemplate.getParamValueForLike());
        }
        if (jobScript != null) {
            query.setParameter("jobScript", jobScript.getParamValueForLike());
        }
        if (includeScript != null) {
            query.setParameter("includeScript", includeScript.getParamValueForLike());
        }
        if (noticeBoard != null) {
            query.setParameter("noticeBoards", noticeBoard.getParamValueForLike());
        }
        if (lock != null) {
            query.setParameter("lock", lock.getParamValueForLike());
        }
        if (envName != null) {
            query.setParameter("envName", envName.getParamValueForLike());
        }
        if (envValue != null) {
            query.setParameter("envValue", envValue.getParamValueForLike());
        }
        if (argumentName != null) {
            query.setParameter("argumentName", argumentName.getParamValueForLike());
        }
        if (argumentValue != null) {
            query.setParameter("argumentValue", argumentValue.getParamValueForLike());
        }
        if (oracle == null && isWorkflowType && jobNameExactMatch && !SOSString.isEmpty(jobNameForExactMatch)) {
            return checkJobNameExactMatch(getSession().getResultList(query), true, jobNameForExactMatch);
        } else {
            return getSession().getResultList(query);
        }
    }

    /** Support for calendar scope (working / non-working) is implemented at the advanced search query level but is currently disabled because it is not
     * supported by the GUI.
     *
     * <p>
     * TODO:
     * <ol>
     * <li>Extend the views {@code INV_SCHEDULE2CALENDARS} and {@code INV_REL_SCHEDULE2CALENDARS} to expose {@code CALENDAR_TYPE} directly. Currently, the
     * calendar type is resolved via joins with {@code INV_CONFIGURATION} and related tables.
     * <p>
     * Proposed approach:
     * <ul>
     * <li>Introduce a helper view based on the existing view definition.</li>
     * <li>Create a new view joining {@code INV_CONFIGURATION -> INV_SCHEDULE2CALENDARS} and explicitly selecting {@code INV_CONFIGURATION.TYPE} as
     * {@code CALENDAR_TYPE}.</li>
     * </ul>
     * </li>
     * <li>Extend RAML and GUI to support calendar scope filtering (working / non-working), e.g. by introducing a new filter parameter(see above - someNewFilter
     * in code).</li>
     * </ol>
     */
    private static Integer getAdvancedSearchSpecificCalendarTypeValue(RequestSearchReturnType type, boolean selectCalendar) {
        if (!selectCalendar) {
            return null;
        }
        switch (type) {
        case CALENDAR:
            return null;
        case WORKINGDAYSCALENDAR:
            return ConfigurationType.WORKINGDAYSCALENDAR.intValue();
        case NONWORKINGDAYSCALENDAR:
            return ConfigurationType.NONWORKINGDAYSCALENDAR.intValue();
        default:
            // TODO
            // if(someNewFilter) {

            // }
            return null;
        }
    }

    private WorkflowSearchParamResult setHQLAndGetParameterValue(WorkflowSearchOracle oracle, StringBuilder hql, JobCriticality criticality) {
        WorkflowSearchParamResult result = new WorkflowSearchParamResult();
        if (criticality != null) {
            boolean isNormalCriticality = JobCriticality.NORMAL.equals(criticality);
            result.value = criticality.value();
            hql.append("and ");
            if (oracle == null) {
                String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "sw.jobs", "$.criticalities");
                if (isNormalCriticality) {
                    hql.append("(lower(").append(jsonFunc).append(") is null or ");
                }
                hql.append("lower(").append(jsonFunc).append(") like :jobCriticality");
                if (isNormalCriticality) {
                    hql.append(")");
                }
            } else {
                if (isNormalCriticality) {
                    // NOT JSON_EXISTS...
                    hql.append("(not ").append(SOSHibernateJsonExists.getFunction("sw.jobs", "$.criticalities", JsonPathType.ARRAY)).append(" or ");
                }
                if (oracle.jsonFallbackToRegex) {
                    result.value = SOSHibernateJsonExists.getOracleRegExLikeSearch("$.criticalities", result.value);
                    result.usePercentsForLike = false;
                }
                hql.append(SOSHibernateJsonExists.getFunction("sw.jobs", "$.criticalities", JsonPathType.ARRAY, JsonOperator.LIKE, ":jobCriticality",
                        JsonCaseSensitivity.INSENSITIVE));
                if (isNormalCriticality) {
                    hql.append(")");
                }
            }
            hql.append(" ");
        }
        return getReturn(result);
    }

    private WorkflowSearchParamResult setHQLAndGetArgNames(WorkflowSearchOracle oracle, StringBuilder hql, String paramValue) {
        WorkflowSearchParamResult result = new WorkflowSearchParamResult();
        if (!SOSString.isEmpty(paramValue)) {
            hql.append("and (");
            result = setHQLAndGetParameterValue(oracle, hql, null, "argumentName", paramValue, "sw.args", "$.orderPreparationParamNames");
            setHQLAndGetParameterValue(oracle, hql, "or", "argumentName", paramValue, "sw.args", "$.jobArgNames");
            setHQLAndGetParameterValue(oracle, hql, "or", "argumentName", paramValue, "sw.instructionsArgs", "$.jobArgNames");
            hql.append(")");
        }
        return getReturn(result);
    }

    private WorkflowSearchParamResult setHQLAndGetArgValues(WorkflowSearchOracle oracle, StringBuilder hql, String paramValue) {
        WorkflowSearchParamResult result = new WorkflowSearchParamResult();
        if (!SOSString.isEmpty(paramValue)) {
            hql.append("and (");
            result = setHQLAndGetParameterValue(oracle, hql, null, "argumentValue", paramValue, "sw.args", "$.orderPreparationParamValues");
            setHQLAndGetParameterValue(oracle, hql, "or", "argumentValue", paramValue, "sw.args", "$.jobArgValues");
            setHQLAndGetParameterValue(oracle, hql, "or", "argumentValue", paramValue, "sw.instructionsArgs", "$.jobArgValues");
            hql.append(")");
        }
        return getReturn(result);
    }

    private WorkflowSearchParamResult setHQLAndGetParameterValueExactMatch(WorkflowSearchOracle oracle, StringBuilder hql, String operator,
            String paramName, String paramValue, String columnName, String jsonAttribute) {
        WorkflowSearchParamResult result = new WorkflowSearchParamResult();
        if (!SOSString.isEmpty(paramValue)) {
            if (!SOSString.isEmpty(operator)) {
                hql.append(operator).append(" ");// and,or ..
            }
            boolean findAll = FIND_ALL.equals(paramValue);
            if (!findAll) {
                if (oracle != null) {
                    result.value = SOSString.trim(paramValue, "%");
                } else {
                    result.value = "\"" + paramValue + "\"";
                }
            }

            if (oracle == null) {
                String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, columnName, jsonAttribute);
                hql.append(jsonFunc).append(" ");
                if (findAll) {
                    hql.append("is not null ");
                } else {
                    hql.append("like :").append(paramName).append(" ");
                }
            } else {
                if (findAll) {
                    hql.append(SOSHibernateJsonExists.getFunction(columnName, jsonAttribute, JsonPathType.ARRAY)).append(" ");
                } else {
                    if (oracle.jsonFallbackToRegex) {
                        result.value = SOSHibernateJsonExists.getOracleRegExExactSearch(jsonAttribute, paramValue);
                        result.usePercentsForLike = false;
                        hql.append(SOSHibernateJsonExists.getFunction(columnName, jsonAttribute, JsonPathType.ARRAY, JsonOperator.EQUALS, ":"
                                + paramName, JsonCaseSensitivity.SENSITIVE));
                    } else {
                        hql.append(SOSHibernateJsonExists.getFunction(columnName, jsonAttribute, JsonPathType.ARRAY, JsonOperator.EQUALS, ":"
                                + paramName, JsonCaseSensitivity.SENSITIVE));
                    }
                }
            }
        }
        return getReturn(result);
    }

    private WorkflowSearchParamResult setHQLAndGetParameterValue(WorkflowSearchOracle oracle, StringBuilder hql, String operator, String paramName,
            String paramValue, String columnName, String jsonAttribute) {
        WorkflowSearchParamResult result = new WorkflowSearchParamResult();
        if (!SOSString.isEmpty(paramValue)) {
            if (!SOSString.isEmpty(operator)) {
                hql.append(operator).append(" ");// and,or ..
            }
            boolean findAll = FIND_ALL.equals(paramValue);
            if (findAll) {
                if (paramName.equals("includeScript")) {
                    findAll = false;
                    result.value = "!include ";
                }
            } else {
                result.value = paramValue;
                if (paramName.equals("includeScript")) {
                    result.value = "!include " + result.value;
                }
            }

            if (oracle == null) {
                String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, columnName, jsonAttribute);
                hql.append("lower(").append(jsonFunc).append(") ");
                if (findAll) {
                    hql.append("is not null ");
                } else {
                    hql.append("like :").append(paramName).append(" ");
                }
            } else {
                if (findAll) {
                    hql.append(SOSHibernateJsonExists.getFunction(columnName, jsonAttribute, JsonPathType.ARRAY)).append(" ");
                } else {
                    if (oracle.jsonFallbackToRegex) {
                        result.value = SOSHibernateJsonExists.getOracleRegExLikeSearch(jsonAttribute, result.value);
                        result.usePercentsForLike = false;
                    }
                    hql.append(SOSHibernateJsonExists.getFunction(columnName, jsonAttribute, JsonPathType.ARRAY, JsonOperator.LIKE, ":" + paramName,
                            JsonCaseSensitivity.INSENSITIVE));
                }
            }
        }
        return getReturn(result);
    }

    private WorkflowSearchParamResult getReturn(WorkflowSearchParamResult r) {
        return r == null || r.value == null ? null : r;
    }

    private boolean isReleasable(RequestSearchReturnType type) {
        switch (type) {
        case SCHEDULE:
        case INCLUDESCRIPT:
        case CALENDAR:
        case WORKINGDAYSCALENDAR:
        case NONWORKINGDAYSCALENDAR:
        case REPORT:
        case JOBTEMPLATE:
            return true;
        default:
            return false;
        }
    }

    private boolean searchInFolders(List<String> folders) {
        return folders != null && folders.size() > 0 && !folders.contains("/");
    }

    private String foldersHql(List<String> folders) {
        List<String> f = new ArrayList<>();
        for (int i = 0; i < folders.size(); i++) {
            f.add("lower(mt.folder) like :folder" + i + " ");
        }
        return String.join(" or ", f);
    }

    private void foldersQueryParameters(Query<InventorySearchItem> query, List<String> folders) {
        for (int i = 0; i < folders.size(); i++) {
            query.setParameter("folder" + i, folders.get(i).toLowerCase() + '%');
        }
    }

    private WorkflowSearchOracle createInstance(SOSHibernateSession session) {
        if (session.getFactory().dbmsIsOracle()) {
            return new WorkflowSearchOracle(session.getFactory().getDatabaseMetaData().getOracle().getJson().fallbackToRegex());
        }
        return null;
    }

    private class WorkflowSearchOracle {

        private final boolean jsonFallbackToRegex;

        private WorkflowSearchOracle(boolean jsonFallbackToRegex) {
            this.jsonFallbackToRegex = jsonFallbackToRegex;
        }
    }

    private class WorkflowSearchParamResult {

        private String value;
        private boolean usePercentsForLike = true;

        private String getParamValueForLike() {
            String val = value.toLowerCase();
            return usePercentsForLike ? "%" + val + "%" : val;
        }
    }
}
