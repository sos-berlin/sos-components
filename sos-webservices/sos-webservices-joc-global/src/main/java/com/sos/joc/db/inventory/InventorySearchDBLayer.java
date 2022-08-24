package com.sos.joc.db.inventory;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.function.json.SOSHibernateJsonValue;
import com.sos.commons.hibernate.function.json.SOSHibernateJsonValue.ReturnType;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.job.JobCriticality;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.search.WorkflowSearcher;
import com.sos.joc.classes.inventory.search.WorkflowSearcher.WorkflowJob;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.common.SearchStringHelper;
import com.sos.joc.db.inventory.items.InventorySearchItem;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.search.RequestSearchAdvancedItem;
import com.sos.joc.model.inventory.search.RequestSearchReturnType;

public class InventorySearchDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(InventorySearchDBLayer.class);
    private boolean tmpShowLog = false; // TODO to remove

    private static final String FIND_ALL = "*";

    public InventorySearchDBLayer(SOSHibernateSession session) {
        super(session);
    }

    public List<InventorySearchItem> getBasicSearchInventoryConfigurations(RequestSearchReturnType type, String search, List<String> folders)
            throws SOSHibernateException {

        boolean isReleasable = isReleasable(type);
        boolean searchInFolders = searchInFolders(folders);

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
        hql.append("group by mt.id,mt.path,mt.type,mt.folder,mt.name,mt.title,mt.valid,mt.deleted,mt.deployed,mt.released ");

        Query<InventorySearchItem> query = getSession().createQuery(hql.toString(), InventorySearchItem.class);
        if (RequestSearchReturnType.CALENDAR.equals(type)) {
            query.setParameter("types", JocInventory.getCalendarTypes());
        } else {
            query.setParameter("type", ConfigurationType.valueOf(type.value()).intValue());
        }
        if (search != null) {
            query.setParameter("search", SearchStringHelper.globToSqlPattern('%' + search.toLowerCase() + '%').replaceAll("%%+", "%"));
        }
        if (searchInFolders) {
            foldersQueryParameters(query, folders);
        }
        if (tmpShowLog) {
            LOGGER.info("[getBasicSearchInventoryConfigurations]" + getSession().getSQLString(query));
        }
        return getSession().getResultList(query);
    }

    public List<InventorySearchItem> getBasicSearchDeployedOrReleasedConfigurations(RequestSearchReturnType type, String search, List<String> folders,
            String controllerId) throws SOSHibernateException {

        boolean isReleasable = isReleasable(type);
        boolean searchInFolders = searchInFolders(folders);

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

        Query<InventorySearchItem> query = getSession().createQuery(hql.toString(), InventorySearchItem.class);
        if (RequestSearchReturnType.CALENDAR.equals(type)) {
            query.setParameter("types", JocInventory.getCalendarTypes());
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
        if (tmpShowLog) {
            LOGGER.info("[getBasicSearchDeployedOrReleasedConfigurations]" + getSession().getSQLString(query));
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

    // TODO merge all functions ...
    public List<InventorySearchItem> getAdvancedSearchInventoryConfigurations(RequestSearchReturnType type, String search, List<String> folders,
            RequestSearchAdvancedItem advanced) throws SOSHibernateException {

        boolean isReleasable = isReleasable(type);
        boolean searchInFolders = searchInFolders(folders);

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

        /*------------------------*/
        String fileOrderSource = null;
        String schedule = null;
        String calendar = null;
        String workflow = null;
        Integer jobCountFrom = null;
        Integer jobCountTo = null;
        String jobCriticality = null;
        String agentName = null;
        String jobName = null;
        boolean jobNameExactMatch = advanced.getJobNameExactMatch() != null && advanced.getJobNameExactMatch();
        String jobNameForExactMatch = SOSString.isEmpty(advanced.getJobName()) ? "" : advanced.getJobName();
        String jobResource = null;
        String jobTemplate = null;
        String jobScript = null;
        String includeScript = null;
        String noticeBoard = null;
        String lock = null;
        String argumentName = null;
        String argumentValue = null;
        String envName = null;
        String envValue = null;
        boolean isWorkflowType = false;

        boolean selectSchedule = !SOSString.isEmpty(advanced.getSchedule());
        boolean selectCalendar = !SOSString.isEmpty(advanced.getCalendar());
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

            jobCriticality = setHQLAndGetParameterValue(hql, advanced.getJobCriticality());
            agentName = setHQLAndGetParameterValue(hql, "and", "agentName", advanced.getAgentName(), "sw.jobs", "$.agentIds");
            if (jobNameExactMatch) {
                jobName = setHQLAndGetParameterValueExactMatch(hql, "and", "jobName", jobNameForExactMatch, "sw.jobs", "$.names");
            } else {
                jobName = setHQLAndGetParameterValue(hql, "and", "jobName", advanced.getJobName(), "sw.jobs", "$.names");
            }
            jobResource = setHQLAndGetParameterValue(hql, "and", "jobResources", advanced.getJobResource(), "sw.jobs", "$.jobResources");
            jobScript = setHQLAndGetParameterValue(hql, "and", "jobScript", advanced.getJobScript(), "sw.jobsScripts", "$.scripts");
            includeScript = setHQLAndGetParameterValue(hql, "and", "includeScript", advanced.getIncludeScript(), "sw.jobsScripts", "$.scripts");
            jobTemplate = setHQLAndGetParameterValue(hql, "and", "jobTemplate", advanced.getJobTemplate(), "sw.jobs", "$.jobTemplates");
            noticeBoard = setHQLAndGetParameterValue(hql, "and", "noticeBoards", advanced.getNoticeBoard(), "sw.instructions",
                    "$.noticeBoardNames");
            lock = setHQLAndGetParameterValue(hql, "and", "lock", advanced.getLock(), "sw.instructions", "$.lockIds");
            envName = setHQLAndGetParameterValue(hql, "and", "envName", advanced.getEnvName(), "sw.args", "$.jobEnvNames");
            envValue = setHQLAndGetParameterValue(hql, "and", "envValue", advanced.getEnvValue(), "sw.args", "$.jobEnvValues");
            argumentName = setHQLAndGetArgNames(hql, advanced.getArgumentName());
            argumentValue = setHQLAndGetArgValues(hql, advanced.getArgumentValue());
            break;
        case FILEORDERSOURCE:
        case SCHEDULE:
        case CALENDAR:
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
                    if (selectFileOrderSource && !advanced.getFileOrderSource().equals(FIND_ALL)) {
                        fileOrderSource = advanced.getFileOrderSource();
                        hql.append(add).append(" lower(subti.name) like :fileOrderSource ");
                        add = "and";
                    }
                    hql.append(") ");
                }
                break;
            case CALENDAR:
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
                if (workflow != null) {
                    hql.append(" and lower(subt.workflowName) like :workflow ");
                }
                hql.append(") ");
                break;
            case CALENDAR:
                hql.append("    where ic.name in(");
                hql.append("         select subt.workflowName from ").append(DBLayer.DBITEM_INV_SCHEDULE2WORKFLOWS).append(" subt ");
                hql.append("         ,").append(DBLayer.DBITEM_INV_SCHEDULE2CALENDARS).append(" subtc ");
                if (selectFileOrderSource) {
                    hql.append(",").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" subti ");
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
            jobCriticality = setHQLAndGetParameterValue(hql, advanced.getJobCriticality());
            agentName = setHQLAndGetParameterValue(hql, "and", "agentName", advanced.getAgentName(), "sw.jobs", "$.agentIds");
            if (jobNameExactMatch) {
                jobName = setHQLAndGetParameterValueExactMatch(hql, "and", "jobName", jobNameForExactMatch, "sw.jobs", "$.names");
            } else {
                jobName = setHQLAndGetParameterValue(hql, "and", "jobName", advanced.getJobName(), "sw.jobs", "$.names");
            }
            jobResource = setHQLAndGetParameterValue(hql, "and", "jobResources", advanced.getJobResource(), "sw.jobs", "$.jobResources");
            jobScript = setHQLAndGetParameterValue(hql, "and", "jobScript", advanced.getJobScript(), "sw.jobsScripts", "$.scripts");
            includeScript = setHQLAndGetParameterValue(hql, "and", "includeScript", advanced.getIncludeScript(), "sw.jobsScripts", "$.scripts");
            jobTemplate = setHQLAndGetParameterValue(hql, "and", "jobTemplate", advanced.getJobTemplate(), "sw.jobs", "$.jobTemplates");
            noticeBoard = setHQLAndGetParameterValue(hql, "and", "noticeBoards", advanced.getNoticeBoard(), "sw.instructions",
                    "$.noticeBoardNames");
            lock = setHQLAndGetParameterValue(hql, "and", "lock", advanced.getLock(), "sw.instructions", "$.lockIds");
            envName = setHQLAndGetParameterValue(hql, "and", "envName", advanced.getEnvName(), "sw.args", "$.jobEnvNames");
            envValue = setHQLAndGetParameterValue(hql, "and", "envValue", advanced.getEnvValue(), "sw.args", "$.jobEnvValues");
            hql.append(")");// end exists
            break;
        case JOBRESOURCE:
        case NOTICEBOARD:
        case LOCK:
        default:
            break;
        }
        hql.append("group by mt.id,mt.path,mt.type,mt.folder,mt.name,mt.title,mt.valid,mt.deleted,mt.deployed,mt.released ");

        Query<InventorySearchItem> query = getSession().createQuery(hql.toString(), InventorySearchItem.class);
        if (RequestSearchReturnType.CALENDAR.equals(type)) {
            query.setParameter("types", JocInventory.getCalendarTypes());
        } else {
            query.setParameter("type", ConfigurationType.valueOf(type.value()).intValue());
        }
        if (search != null) {
            query.setParameter("search", SearchStringHelper.globToSqlPattern('%' + search.toLowerCase() + '%').replaceAll("%%+", "%"));
        }
        if (searchInFolders) {
            foldersQueryParameters(query, folders);
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
            query.setParameter("agentName", '%' + agentName.toLowerCase() + '%');
        }
        if (jobName != null) {
            if (jobNameExactMatch) {
                query.setParameter("jobName", '%' + jobName + '%');
            } else {
                query.setParameter("jobName", '%' + jobName.toLowerCase() + '%');
            }
        }
        if (jobCriticality != null) {
            query.setParameter("jobCriticality", '%' + jobCriticality.toLowerCase() + '%');
        }
        if (jobResource != null) {
            query.setParameter("jobResources", '%' + jobResource.toLowerCase() + '%');
        }
        if (jobScript != null) {
            query.setParameter("jobScript", '%' + jobScript.toLowerCase() + '%');
        }
        if (includeScript != null) {
            query.setParameter("includeScript", "%!include " + includeScript.toLowerCase() + '%');
        }
        if (noticeBoard != null) {
            query.setParameter("noticeBoards", '%' + noticeBoard.toLowerCase() + '%');
        }
        if (jobTemplate != null) {
            query.setParameter("jobTemplate", '%' + jobTemplate.toLowerCase() + '%');
        }
        if (lock != null) {
            query.setParameter("lock", '%' + lock.toLowerCase() + '%');
        }
        if (envName != null) {
            query.setParameter("envName", '%' + envName.toLowerCase() + '%');
        }
        if (envValue != null) {
            query.setParameter("envValue", '%' + envValue.toLowerCase() + '%');
        }
        if (argumentName != null) {
            query.setParameter("argumentName", '%' + argumentName.toLowerCase() + '%');
        }
        if (argumentValue != null) {
            query.setParameter("argumentValue", '%' + argumentValue.toLowerCase() + '%');
        }
        if (tmpShowLog) {
            LOGGER.info("[getAdvancedSearchInventoryConfigurations]" + getSession().getSQLString(query));
        }
        if (isWorkflowType && jobNameExactMatch && !SOSString.isEmpty(jobNameForExactMatch)) {
            return checkJobNameExactMatch(getSession().getResultList(query), false, jobNameForExactMatch);
        } else {
            return getSession().getResultList(query);
        }
    }

    // extra check because possible database case insensitivity
    private List<InventorySearchItem> checkJobNameExactMatch(List<InventorySearchItem> workflows, boolean deployedOrReleased, String jobName)
            throws SOSHibernateException {
        if (workflows == null || workflows.size() == 0) {
            return workflows;
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
            List<String> folders, RequestSearchAdvancedItem advanced, String controllerId) throws SOSHibernateException {

        boolean isReleasable = isReleasable(type);
        boolean searchInFolders = searchInFolders(folders);

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

        /*------------------------*/
        String fileOrderSource = null;
        String schedule = null;
        String calendar = null;
        String workflow = null;
        Integer jobCountFrom = null;
        Integer jobCountTo = null;
        String jobCriticality = null;
        String agentName = null;
        String jobName = null;
        boolean jobNameExactMatch = advanced.getJobNameExactMatch() != null && advanced.getJobNameExactMatch();
        String jobNameForExactMatch = SOSString.isEmpty(advanced.getJobName()) ? "" : advanced.getJobName();
        String jobResource = null;
        String jobTemplate = null;
        String jobScript = null;
        String includeScript = null;
        String noticeBoard = null;
        String lock = null;
        String envName = null;
        String envValue = null;
        String argumentName = null;
        String argumentValue = null;
        boolean isWorkflowType = false;

        boolean selectSchedule = !SOSString.isEmpty(advanced.getSchedule());
        boolean selectCalendar = !SOSString.isEmpty(advanced.getCalendar());
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

            jobCriticality = setHQLAndGetParameterValue(hql, advanced.getJobCriticality());
            agentName = setHQLAndGetParameterValue(hql, "and", "agentName", advanced.getAgentName(), "sw.jobs", "$.agentIds");
            if (jobNameExactMatch) {
                jobName = setHQLAndGetParameterValueExactMatch(hql, "and", "jobName", jobNameForExactMatch, "sw.jobs", "$.names");
            } else {
                jobName = setHQLAndGetParameterValue(hql, "and", "jobName", advanced.getJobName(), "sw.jobs", "$.names");
            }
            jobResource = setHQLAndGetParameterValue(hql, "and", "jobResources", advanced.getJobResource(), "sw.jobs", "$.jobResources");
            jobTemplate = setHQLAndGetParameterValue(hql, "and", "jobTemplate", advanced.getJobTemplate(), "sw.jobs", "$.jobTemplates");
            jobScript = setHQLAndGetParameterValue(hql, "and", "jobScript", advanced.getJobScript(), "sw.jobsScripts", "$.scripts");
            includeScript = setHQLAndGetParameterValue(hql, "and", "includeScript", advanced.getIncludeScript(), "sw.jobsScripts", "$.scripts");
            noticeBoard = setHQLAndGetParameterValue(hql, "and", "noticeBoards", advanced.getNoticeBoard(), "sw.instructions",
                    "$.noticeBoardNames");
            lock = setHQLAndGetParameterValue(hql, "and", "lock", advanced.getLock(), "sw.instructions", "$.lockIds");
            envName = setHQLAndGetParameterValue(hql, "and", "envName", advanced.getEnvName(), "sw.args", "$.jobEnvNames");
            envValue = setHQLAndGetParameterValue(hql, "and", "envValue", advanced.getEnvValue(), "sw.args", "$.jobEnvValues");
            argumentName = setHQLAndGetArgNames(hql, advanced.getArgumentName());
            argumentValue = setHQLAndGetArgValues(hql, advanced.getArgumentValue());
            break;
        case FILEORDERSOURCE:
        case SCHEDULE:
        case CALENDAR:
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
                    if (selectFileOrderSource && !advanced.getFileOrderSource().equals(FIND_ALL)) {
                        fileOrderSource = advanced.getFileOrderSource();
                        hql.append(add).append(" lower(subti.name) like :fileOrderSource ");
                        add = "and";
                    }
                    hql.append(") ");
                }
                break;
            case CALENDAR:
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
                if (workflow != null) {
                    hql.append(" and lower(subt.workflowName) like :workflow ");
                }
                hql.append(") ");
                break;
            case CALENDAR:
                hql.append("    where ic.name in(");
                hql.append("         select subt.workflowName from ").append(DBLayer.DBITEM_INV_RELEASED_SCHEDULE2WORKFLOWS).append(" subt ");
                hql.append("         ,").append(DBLayer.DBITEM_INV_RELEASED_SCHEDULE2CALENDARS).append(" subtc ");
                if (selectFileOrderSource) {
                    hql.append(",").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" subti ");
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
            jobCriticality = setHQLAndGetParameterValue(hql, advanced.getJobCriticality());
            agentName = setHQLAndGetParameterValue(hql, "and", "agentName", advanced.getAgentName(), "sw.jobs", "$.agentIds");
            if (jobNameExactMatch) {
                jobName = setHQLAndGetParameterValueExactMatch(hql, "and", "jobName", jobNameForExactMatch, "sw.jobs", "$.names");
            } else {
                jobName = setHQLAndGetParameterValue(hql, "and", "jobName", advanced.getJobName(), "sw.jobs", "$.names");
            }
            jobResource = setHQLAndGetParameterValue(hql, "and", "jobResources", advanced.getJobResource(), "sw.jobs", "$.jobResources");
            jobTemplate = setHQLAndGetParameterValue(hql, "and", "jobTemplate", advanced.getJobTemplate(), "sw.jobs", "$.jobTemplates");
            jobScript = setHQLAndGetParameterValue(hql, "and", "jobScript", advanced.getJobScript(), "sw.jobsScripts", "$.scripts");
            includeScript = setHQLAndGetParameterValue(hql, "and", "includeScript", advanced.getIncludeScript(), "sw.jobsScripts", "$.scripts");
            noticeBoard = setHQLAndGetParameterValue(hql, "and", "noticeBoards", advanced.getNoticeBoard(), "sw.instructions",
                    "$.noticeBoardNames");
            lock = setHQLAndGetParameterValue(hql, "and", "lock", advanced.getLock(), "sw.instructions", "$.lockIds");
            envName = setHQLAndGetParameterValue(hql, "and", "envName", advanced.getEnvName(), "sw.args", "$.jobEnvNames");
            envValue = setHQLAndGetParameterValue(hql, "and", "envValue", advanced.getEnvValue(), "sw.args", "$.jobEnvValues");
            hql.append(")");// end exists
            break;
        case JOBRESOURCE:
        case NOTICEBOARD:
        case LOCK:
        default:
            break;
        }
        Query<InventorySearchItem> query = getSession().createQuery(hql.toString(), InventorySearchItem.class);
        if (RequestSearchReturnType.CALENDAR.equals(type)) {
            query.setParameter("types", JocInventory.getCalendarTypes());
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
            query.setParameter("agentName", '%' + agentName.toLowerCase() + '%');
        }
        if (jobName != null) {
            if (jobNameExactMatch) {
                query.setParameter("jobName", '%' + jobName + '%');
            } else {
                query.setParameter("jobName", '%' + jobName.toLowerCase() + '%');
            }
        }
        if (jobCriticality != null) {
            query.setParameter("jobCriticality", '%' + jobCriticality.toLowerCase() + '%');
        }
        if (jobResource != null) {
            query.setParameter("jobResources", '%' + jobResource.toLowerCase() + '%');
        }
        if (jobTemplate != null) {
            query.setParameter("jobTemplate", '%' + jobTemplate.toLowerCase() + '%');
        }
        if (jobScript != null) {
            query.setParameter("jobScript", '%' + jobScript.toLowerCase() + '%');
        }
        if (includeScript != null) {
            query.setParameter("includeScript", "%!include " + includeScript.toLowerCase() + '%');
        }
        if (noticeBoard != null) {
            query.setParameter("noticeBoards", '%' + noticeBoard.toLowerCase() + '%');
        }
        if (lock != null) {
            query.setParameter("lock", '%' + lock.toLowerCase() + '%');
        }
        if (envName != null) {
            query.setParameter("envName", '%' + envName.toLowerCase() + '%');
        }
        if (envValue != null) {
            query.setParameter("envValue", '%' + envValue.toLowerCase() + '%');
        }
        if (argumentName != null) {
            query.setParameter("argumentName", '%' + argumentName.toLowerCase() + '%');
        }
        if (argumentValue != null) {
            query.setParameter("argumentValue", '%' + argumentValue.toLowerCase() + '%');
        }
        if (tmpShowLog) {
            LOGGER.info("[getAdvancedSearchDeployedOrReleasedConfigurations]" + getSession().getSQLString(query));
        }
        if (isWorkflowType && jobNameExactMatch && !SOSString.isEmpty(jobNameForExactMatch)) {
            return checkJobNameExactMatch(getSession().getResultList(query), true, jobNameForExactMatch);
        } else {
            return getSession().getResultList(query);
        }
    }

    private String setHQLAndGetParameterValue(StringBuilder hql, JobCriticality criticality) {
        String result = null;
        if (criticality != null) {
            String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "sw.jobs", "$.criticalities");
            hql.append("and lower(").append(jsonFunc).append(") ");
            // TODO check ..
            if (criticality.equals(JobCriticality.NORMAL)) {
                hql.append("is null ");
            } else {
                result = criticality.value();
                hql.append("like :jobCriticality ");
            }
        }
        return result;
    }

    private String setHQLAndGetArgNames(StringBuilder hql, String paramValue) {
        String result = null;
        if (!SOSString.isEmpty(paramValue)) {
            hql.append("and (");
            result = setHQLAndGetParameterValue(hql, null, "argumentName", paramValue, "sw.args", "$.orderPreparationParamNames");
            setHQLAndGetParameterValue(hql, "or", "argumentName", paramValue, "sw.args", "$.jobArgNames");
            setHQLAndGetParameterValue(hql, "or", "argumentName", paramValue, "sw.instructionsArgs", "$.jobArgNames");
            hql.append(")");
        }
        return result;
    }

    private String setHQLAndGetArgValues(StringBuilder hql, String paramValue) {
        String result = null;
        if (!SOSString.isEmpty(paramValue)) {
            hql.append("and (");
            result = setHQLAndGetParameterValue(hql, null, "argumentValue", paramValue, "sw.args", "$.orderPreparationParamValues");
            setHQLAndGetParameterValue(hql, "or", "argumentValue", paramValue, "sw.args", "$.jobArgValues");
            setHQLAndGetParameterValue(hql, "or", "argumentValue", paramValue, "sw.instructionsArgs", "$.jobArgValues");
            hql.append(")");
        }
        return result;
    }

    private String setHQLAndGetParameterValueExactMatch(StringBuilder hql, String operator, String paramName, String paramValue, String columnName,
            String jsonAttribute) {
        String result = null;
        if (!SOSString.isEmpty(paramValue)) {
            String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, columnName, jsonAttribute);
            // hql.append("and ").append(SOSHibernateRegexp.getFunction(jsonFunc, ":jobName")).append(" ");
            if (!SOSString.isEmpty(operator)) {
                hql.append(operator).append(" ");// and,or ..
            }
            hql.append(jsonFunc).append(" ");
            if (paramValue.equals(FIND_ALL)) {
                hql.append("is not null ");
            } else {
                result = "\"" + paramValue + "\"";
                hql.append("like :").append(paramName).append(" ");
            }
        }
        return result;
    }

    private String setHQLAndGetParameterValue(StringBuilder hql, String operator, String paramName, String paramValue, String columnName,
            String jsonAttribute) {
        String result = null;
        if (!SOSString.isEmpty(paramValue)) {
            if (!SOSString.isEmpty(operator)) {
                hql.append(operator).append(" ");// and,or ..
            }
            String jsonFunc = null;
            if (!getSession().getFactory().getDatabaseMetaData().supportJsonReturningClob() && jsonAttribute.equals("$.scripts")) {
                jsonFunc = columnName;
            } else {
                jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, columnName, jsonAttribute);
            }
            hql.append("lower(").append(jsonFunc).append(") ");
            if (paramValue.equals(FIND_ALL)) {
                hql.append("is not null ");
            } else {
                result = paramValue;
                hql.append("like :").append(paramName).append(" ");
            }
        }
        return result;
    }

    private boolean isReleasable(RequestSearchReturnType type) {
        switch (type) {
        case SCHEDULE:
        case INCLUDESCRIPT:
        case CALENDAR:
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
}
