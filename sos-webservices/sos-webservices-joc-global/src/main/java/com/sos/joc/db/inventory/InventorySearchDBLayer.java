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
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.items.InventorySearchItem;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.search.RequestSearchAdvancedItem;

public class InventorySearchDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(InventorySearchDBLayer.class);
    private boolean tmpShowLog = false; // TODO to remove

    private static final String FIND_ALL = "*";

    public InventorySearchDBLayer(SOSHibernateSession session) {
        super(session);
    }

    public List<InventorySearchItem> getBasicSearchInventoryConfigurations(ConfigurationType type, String search, List<String> folders)
            throws SOSHibernateException {

        boolean isReleasable = isReleasable(type);
        boolean searchInFolders = searchInFolders(folders);

        StringBuilder hql = new StringBuilder("select mt.id as id ");
        hql.append(",mt.path as path ");
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
        hql.append("where mt.type=:type ");
        if (SOSString.isEmpty(search) || search.equals(FIND_ALL)) {
            search = null;
        } else {
            hql.append("and (lower(mt.name) like :search or lower(mt.title) like :search) ");
        }
        if (searchInFolders) {
            hql.append("and (").append(foldersHql(folders)).append(") ");
        }
        hql.append("group by mt.id");

        Query<InventorySearchItem> query = getSession().createQuery(hql.toString(), InventorySearchItem.class);
        query.setParameter("type", type.intValue());
        if (search != null) {
            query.setParameter("search", '%' + search.toLowerCase() + '%');
        }
        if (searchInFolders) {
            foldersQueryParameters(query, folders);
        }
        if (tmpShowLog) {
            LOGGER.info("[getInventoryConfigurations]" + getSession().getSQLString(query));
        }
        return getSession().getResultList(query);
    }

    public List<InventorySearchItem> getBasicSearchDeployedOrReleasedConfigurations(ConfigurationType type, String search, List<String> folders,
            String controllerId) throws SOSHibernateException {

        boolean isReleasable = isReleasable(type);
        boolean searchInFolders = searchInFolders(folders);

        StringBuilder hql = new StringBuilder("select ");
        if (isReleasable) {
            hql.append("mt.cid as id");
            hql.append(",mt.path as path");
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
        hql.append("where mt.type=:type ");
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
        query.setParameter("type", type.intValue());
        if (search != null) {
            query.setParameter("search", '%' + search.toLowerCase() + '%');
        }
        if (searchInFolders) {
            foldersQueryParameters(query, folders);
        }
        if (controllerId != null) {
            query.setParameter("controllerId", controllerId);
        }
        if (tmpShowLog) {
            LOGGER.info("[getDeployedOrReleasedConfigurations]" + getSession().getSQLString(query));
        }
        return getSession().getResultList(query);
    }

    // TODO merge all functions ...
    public List<InventorySearchItem> getAdvancedSearchInventoryConfigurations(ConfigurationType type, String search, List<String> folders,
            RequestSearchAdvancedItem advanced) throws SOSHibernateException {

        boolean isReleasable = isReleasable(type);
        boolean searchInFolders = searchInFolders(folders);

        StringBuilder hql = new StringBuilder("select mt.id as id ");
        hql.append(",mt.path as path ");
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
            if (type.equals(ConfigurationType.WORKFLOW)) {
                hql.append("left join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
                hql.append("on mt.id=sw.inventoryConfigurationId and mt.deployed=sw.deployed ");
            }
        }
        hql.append("where mt.type=:type ");
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
        String workflow = null;
        Integer jobCountFrom = null;
        Integer jobCountTo = null;
        String jobCriticality = null;
        String agentName = null;
        String jobName = null;
        String jobResources = null;
        String boards = null;
        String lock = null;
        String argumentName = null;
        String argumentValue = null;

        switch (type) {
        case WORKFLOW:
            if (!SOSString.isEmpty(advanced.getFileOrderSource())) {
                hql.append("and mt.name in (");
                hql.append("select ").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subt.jsonContent", "$.workflowName")).append(" ");
                hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" subt ");
                hql.append("where subt.type=").append(ConfigurationType.FILEORDERSOURCE.intValue()).append(" ");
                if (!advanced.getFileOrderSource().equals(FIND_ALL)) {
                    fileOrderSource = advanced.getFileOrderSource();
                    hql.append("and lower(subt.name) like :fileOrderSource ");
                }
                hql.append(") ");
            }
            if (!SOSString.isEmpty(advanced.getSchedule())) {
                hql.append("and mt.name in (");
                hql.append("select ").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subt.jsonContent", "$.workflowName")).append(" ");
                hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" subt ");
                hql.append("where subt.type=").append(ConfigurationType.SCHEDULE.intValue()).append(" ");
                if (!advanced.getSchedule().equals(FIND_ALL)) {
                    schedule = advanced.getSchedule();
                    hql.append("and lower(subt.name) like :schedule ");
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
            jobName = setHQLAndGetParameterValue(hql, "and", "jobName", advanced.getJobName(), "sw.jobs", "$.names");
            jobResources = setHQLAndGetParameterValue(hql, "and", "jobResources", advanced.getJobResources(), "sw.jobs", "$.jobResources");
            boards = setHQLAndGetParameterValue(hql, "and", "boards", advanced.getBoards(), "sw.instructions", "$.boardNames");
            lock = setHQLAndGetParameterValue(hql, "and", "lock", advanced.getLock(), "sw.instructions", "$.lockIds");
            argumentName = setHQLAndGetArgNames(hql, advanced.getArgumentName());
            argumentValue = setHQLAndGetArgValues(hql, advanced.getArgumentValue());
            break;
        case FILEORDERSOURCE:
        case SCHEDULE:
            if (!SOSString.isEmpty(advanced.getWorkflow())) {
                if (!advanced.getWorkflow().equals(FIND_ALL)) {
                    workflow = advanced.getWorkflow();
                    hql.append("and lower(");
                    hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "mt.jsonContent", "$.workflowName"));
                    hql.append(") ");
                    hql.append("like :workflow ");
                }
            }

            if (!SOSString.isEmpty(advanced.getFileOrderSource())) {
                hql.append("and exists (");
                hql.append("select ").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subt.jsonContent", "$.workflowName")).append(" ");
                hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" subt ");
                hql.append("where subt.type=").append(ConfigurationType.FILEORDERSOURCE.intValue()).append(" ");
                if (!advanced.getFileOrderSource().equals(FIND_ALL)) {
                    fileOrderSource = advanced.getFileOrderSource();
                    hql.append("and lower(subt.name) like :fileOrderSource ");
                }
                if (workflow != null) {
                    hql.append("and lower(");
                    hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subt.jsonContent", "$.workflowName"));
                    hql.append(") ");
                    hql.append("like :workflow ");
                }
                hql.append(") ");
            }
            if (!SOSString.isEmpty(advanced.getSchedule())) {
                hql.append("and exists (");
                hql.append("select ").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subt.jsonContent", "$.workflowName")).append(" ");
                hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" subt ");
                hql.append("where subt.type=").append(ConfigurationType.SCHEDULE.intValue()).append(" ");
                if (!advanced.getSchedule().equals(FIND_ALL)) {
                    schedule = advanced.getSchedule();
                    hql.append("and lower(subt.name) like :schedule ");
                }
                if (workflow != null) {
                    hql.append("and lower(");
                    hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subt.jsonContent", "$.workflowName"));
                    hql.append(") ");
                    hql.append("like :workflow ");
                }
                hql.append(") ");
            }

            hql.append("and exists(");// start exists
            hql.append("  select sw.id from ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
            // hql.append(" where sw.deployed=mt.deployed ");
            // hql.append(" and sw.inventoryConfigurationId in (");
            hql.append("  where sw.inventoryConfigurationId in (");
            hql.append("    select ic.id from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
            hql.append("    where ic.name=").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "mt.jsonContent", "$.workflowName"));
            hql.append("    and ic.type=").append(ConfigurationType.WORKFLOW.intValue()).append(" ");
            hql.append("  )");
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
            jobName = setHQLAndGetParameterValue(hql, "and", "jobName", advanced.getJobName(), "sw.jobs", "$.names");
            jobResources = setHQLAndGetParameterValue(hql, "and", "jobResources", advanced.getJobResources(), "sw.jobs", "$.jobResources");
            boards = setHQLAndGetParameterValue(hql, "and", "boards", advanced.getBoards(), "sw.instructions", "$.boardNames");
            lock = setHQLAndGetParameterValue(hql, "and", "lock", advanced.getLock(), "sw.instructions", "$.lockIds");
            hql.append(")");// end exists
            break;
        case JOBRESOURCE:
        case BOARD:
        case LOCK:
        default:
            break;
        }
        hql.append("group by mt.id");

        Query<InventorySearchItem> query = getSession().createQuery(hql.toString(), InventorySearchItem.class);
        query.setParameter("type", type.intValue());
        if (search != null) {
            query.setParameter("search", '%' + search.toLowerCase() + '%');
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
            query.setParameter("jobName", '%' + jobName.toLowerCase() + '%');
        }
        if (jobCriticality != null) {
            query.setParameter("jobCriticality", '%' + jobCriticality.toLowerCase() + '%');
        }
        if (jobResources != null) {
            query.setParameter("jobResources", '%' + jobResources.toLowerCase() + '%');
        }
        if (boards != null) {
            query.setParameter("boards", '%' + boards.toLowerCase() + '%');
        }
        if (lock != null) {
            query.setParameter("lock", '%' + lock.toLowerCase() + '%');
        }
        if (argumentName != null) {
            query.setParameter("argumentName", '%' + argumentName.toLowerCase() + '%');
        }
        if (argumentValue != null) {
            query.setParameter("argumentValue", '%' + argumentValue.toLowerCase() + '%');
        }
        if (tmpShowLog) {
            LOGGER.info("[getAdvancedInventoryConfigurations]" + getSession().getSQLString(query));
        }
        return getSession().getResultList(query);
    }

    public List<InventorySearchItem> getAdvancedSearchDeployedOrReleasedConfigurations(ConfigurationType type, String search, List<String> folders,
            RequestSearchAdvancedItem advanced, String controllerId) throws SOSHibernateException {

        boolean isReleasable = isReleasable(type);
        boolean searchInFolders = searchInFolders(folders);

        StringBuilder hql = new StringBuilder("select ");
        if (isReleasable) {
            hql.append("mt.cid as id");
            hql.append(",mt.path as path");
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
            if (type.equals(ConfigurationType.WORKFLOW)) {
                // hql.append("left join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
                // hql.append("on mt.inventoryConfigurationId=sw.inventoryConfigurationId and sw.deployed=1 ");
                hql.append("left join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
                hql.append("on mt.inventoryConfigurationId=sw.inventoryConfigurationId and sw.deployed=1 ");
                hql.append("left join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS_DEPLOYMENT_HISTORY).append(" swdh ");
                hql.append("on swdh.deploymentHistoryId=mt.id and swdh.searchWorkflowId=sw.id ");
            }
        }
        hql.append("where mt.type=:type ");
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

        /*------------------------*/
        String fileOrderSource = null;
        String schedule = null;
        String workflow = null;
        Integer jobCountFrom = null;
        Integer jobCountTo = null;
        String jobCriticality = null;
        String agentName = null;
        String jobName = null;
        String jobResources = null;
        String boards = null;
        String lock = null;
        String argumentName = null;
        String argumentValue = null;

        switch (type) {
        case WORKFLOW:
            if (!SOSString.isEmpty(advanced.getFileOrderSource())) {
                hql.append("and mt.name in (");
                hql.append("select ").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subt.content", "$.workflowName")).append(" ");
                hql.append("from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" subt ");
                hql.append("where subt.type=").append(ConfigurationType.FILEORDERSOURCE.intValue()).append(" ");
                if (!advanced.getFileOrderSource().equals(FIND_ALL)) {
                    fileOrderSource = advanced.getFileOrderSource();
                    hql.append("and lower(subt.name) like :fileOrderSource ");
                }
                if (controllerId != null) {
                    hql.append("and subt.controllerId=:controllerId ");
                }
                hql.append(") ");
            }
            if (!SOSString.isEmpty(advanced.getSchedule())) {
                hql.append("and mt.name in (");
                hql.append("select ").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subt.jsonContent", "$.workflowName")).append(" ");
                hql.append("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" subt ");
                hql.append("where subt.type=").append(ConfigurationType.SCHEDULE.intValue()).append(" ");
                if (!advanced.getSchedule().equals(FIND_ALL)) {
                    schedule = advanced.getSchedule();
                    hql.append("and lower(subt.name) like :schedule ");
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
            jobName = setHQLAndGetParameterValue(hql, "and", "jobName", advanced.getJobName(), "sw.jobs", "$.names");
            jobResources = setHQLAndGetParameterValue(hql, "and", "jobResources", advanced.getJobResources(), "sw.jobs", "$.jobResources");
            boards = setHQLAndGetParameterValue(hql, "and", "boards", advanced.getBoards(), "sw.instructions", "$.boardNames");
            lock = setHQLAndGetParameterValue(hql, "and", "lock", advanced.getLock(), "sw.instructions", "$.lockIds");
            argumentName = setHQLAndGetArgNames(hql, advanced.getArgumentName());
            argumentValue = setHQLAndGetArgValues(hql, advanced.getArgumentValue());
            break;
        case FILEORDERSOURCE:
        case SCHEDULE:
            if (!SOSString.isEmpty(advanced.getWorkflow())) {
                if (!advanced.getWorkflow().equals(FIND_ALL)) {
                    workflow = advanced.getWorkflow();
                    hql.append("and lower(");
                    hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "mt.content", "$.workflowName"));
                    hql.append(") ");
                    hql.append("like :workflow ");
                }
            }

            if (!SOSString.isEmpty(advanced.getFileOrderSource())) {
                hql.append("and exists (");
                hql.append("select ").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subt.content", "$.workflowName")).append(" ");
                hql.append("from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" subt ");
                hql.append("where subt.type=").append(ConfigurationType.FILEORDERSOURCE.intValue()).append(" ");
                if (!advanced.getFileOrderSource().equals(FIND_ALL)) {
                    fileOrderSource = advanced.getFileOrderSource();
                    hql.append("and lower(subt.name) like :fileOrderSource ");
                }
                if (workflow != null) {
                    hql.append("and lower(");
                    hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subt.content", "$.workflowName"));
                    hql.append(") ");
                    hql.append("like :workflow ");
                }
                if (controllerId != null) {
                    hql.append("and subt.controllerId=:controllerId ");
                }
                hql.append(") ");
            }
            if (!SOSString.isEmpty(advanced.getSchedule())) {
                hql.append("and exists (");
                hql.append("select ").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subt.jsonContent", "$.workflowName")).append(" ");
                hql.append("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" subt ");
                hql.append("where subt.type=").append(ConfigurationType.SCHEDULE.intValue()).append(" ");
                if (!advanced.getSchedule().equals(FIND_ALL)) {
                    schedule = advanced.getSchedule();
                    hql.append("and lower(subt.name) like :schedule ");
                }
                if (workflow != null) {
                    hql.append("and lower(");
                    hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "subt.jsonContent", "$.workflowName"));
                    hql.append(") ");
                    hql.append("like :workflow ");
                }
                hql.append(") ");
            }

            hql.append("and exists(");// start exists
            hql.append("  select sw.id from ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
            hql.append("  ,").append(DBLayer.DBITEM_SEARCH_WORKFLOWS_DEPLOYMENT_HISTORY).append(" swdh ");
            hql.append("  where sw.id=swdh.searchWorkflowId ");
            hql.append("  and swdh.deploymentHistoryId in (");
            hql.append("    select dc.id from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" dc ");
            hql.append("    where dc.name=").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "mt.content", "$.workflowName"));
            hql.append("    and dc.type=").append(ConfigurationType.WORKFLOW.intValue()).append(" ");
            if (controllerId != null) {
                hql.append("and dc.controllerId=:controllerId ");
            }
            hql.append("  )");
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
            jobName = setHQLAndGetParameterValue(hql, "and", "jobName", advanced.getJobName(), "sw.jobs", "$.names");
            jobResources = setHQLAndGetParameterValue(hql, "and", "jobResources", advanced.getJobResources(), "sw.jobs", "$.jobResources");
            boards = setHQLAndGetParameterValue(hql, "and", "boards", advanced.getBoards(), "sw.instructions", "$.boardNames");
            lock = setHQLAndGetParameterValue(hql, "and", "lock", advanced.getLock(), "sw.instructions", "$.lockIds");
            hql.append(")");// end exists
            break;
        case JOBRESOURCE:
        case BOARD:
        case LOCK:
        default:
            break;
        }
        Query<InventorySearchItem> query = getSession().createQuery(hql.toString(), InventorySearchItem.class);
        query.setParameter("type", type.intValue());
        if (search != null) {
            query.setParameter("search", '%' + search.toLowerCase() + '%');
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
            query.setParameter("jobName", '%' + jobName.toLowerCase() + '%');
        }
        if (jobCriticality != null) {
            query.setParameter("jobCriticality", '%' + jobCriticality.toLowerCase() + '%');
        }
        if (jobResources != null) {
            query.setParameter("jobResources", '%' + jobResources.toLowerCase() + '%');
        }
        if (boards != null) {
            query.setParameter("boards", '%' + boards.toLowerCase() + '%');
        }
        if (lock != null) {
            query.setParameter("lock", '%' + lock.toLowerCase() + '%');
        }
        if (argumentName != null) {
            query.setParameter("argumentName", '%' + argumentName.toLowerCase() + '%');
        }
        if (argumentValue != null) {
            query.setParameter("argumentValue", '%' + argumentValue.toLowerCase() + '%');
        }
        if (tmpShowLog) {
            LOGGER.info("[getAdvancedInventoryConfigurations]" + getSession().getSQLString(query));
        }
        return getSession().getResultList(query);
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

    private String setHQLAndGetParameterValue(StringBuilder hql, String operator, String paramName, String paramValue, String columnName,
            String jsonAttribute) {
        String result = null;
        if (!SOSString.isEmpty(paramValue)) {
            String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, columnName, jsonAttribute);
            // hql.append("and ").append(SOSHibernateRegexp.getFunction(jsonFunc, ":jobName")).append(" ");
            if (!SOSString.isEmpty(operator)) {
                hql.append(operator).append(" ");// and,or ..
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

    private boolean isReleasable(ConfigurationType type) {
        switch (type) {
        case SCHEDULE:
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
