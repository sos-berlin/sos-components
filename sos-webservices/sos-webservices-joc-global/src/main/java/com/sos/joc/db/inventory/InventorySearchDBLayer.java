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
import com.sos.joc.classes.inventory.search.WorkflowSearcher;
import com.sos.joc.classes.inventory.search.WorkflowSearcher.WorkflowJob;
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
        hql.append("where mt.type=:type ");
        if (SOSString.isEmpty(search) || search.equals(FIND_ALL)) {
            search = null;
        } else {
            hql.append("and (lower(mt.name) like :search or lower(mt.title) like :search) ");
        }
        if (searchInFolders) {
            hql.append("and (").append(foldersHql(folders)).append(") ");
        }
        hql.append("group by mt.id,mt.path,mt.folder,mt.name,mt.title,mt.valid,mt.deleted,mt.deployed,mt.released ");

        Query<InventorySearchItem> query = getSession().createQuery(hql.toString(), InventorySearchItem.class);
        query.setParameter("type", type.intValue());
        if (search != null) {
            query.setParameter("search", '%' + search.toLowerCase() + '%');
        }
        if (searchInFolders) {
            foldersQueryParameters(query, folders);
        }
        if (tmpShowLog) {
            LOGGER.info("[getBasicSearchInventoryConfigurations]" + getSession().getSQLString(query));
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

    public String getDeployedConfigurationsContent(Long id) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select content from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" ");
        hql.append("where inventoryConfigurationId=:id");

        Query<String> query = getSession().createQuery(hql.toString());
        query.setParameter("id", id);
        return query.getSingleResult();
    }

    // TODO merge all functions ...
    public List<InventorySearchItem> getAdvancedSearchInventoryConfigurations(ConfigurationType type, String search, List<String> folders,
            RequestSearchAdvancedItem advanced) throws SOSHibernateException {

        boolean isReleasable = isReleasable(type);
        boolean searchInFolders = searchInFolders(folders);

        StringBuilder hql = new StringBuilder("select mt.id as id ");
        hql.append(",mt.path as path ");
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
        boolean jobNameExactMatch = advanced.getJobNameExactMatch() != null && advanced.getJobNameExactMatch();
        String jobNameForExactMatch = SOSString.isEmpty(advanced.getJobName()) ? "" : advanced.getJobName();
        String jobResources = null;
        String jobScript = null;
        String noticeBoards = null;
        String lock = null;
        String argumentName = null;
        String argumentValue = null;
        String envName = null;
        String envValue = null;
        boolean isWorkflowType = false;

        switch (type) {
        case WORKFLOW:
            isWorkflowType = true;
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
                hql.append("select workflowName from ").append(DBLayer.DBITEM_INV_SCHEDULE2WORKFLOWS).append(" subt ");
                if (!advanced.getSchedule().equals(FIND_ALL)) {
                    schedule = advanced.getSchedule();
                    hql.append("where lower(subt.scheduleName) like :schedule ");
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
            jobResources = setHQLAndGetParameterValue(hql, "and", "jobResources", advanced.getJobResources(), "sw.jobs", "$.jobResources");
            jobScript = setHQLAndGetParameterValue(hql, "and", "jobScript", advanced.getJobScript(), "sw.jobsScripts", "$.scripts");
            noticeBoards = setHQLAndGetParameterValue(hql, "and", "noticeBoards", advanced.getNoticeBoards(), "sw.instructions",
                    "$.noticeBoardNames");
            lock = setHQLAndGetParameterValue(hql, "and", "lock", advanced.getLock(), "sw.instructions", "$.lockIds");
            envName = setHQLAndGetParameterValue(hql, "and", "envName", advanced.getEnvName(), "sw.args", "$.jobEnvNames");
            envValue = setHQLAndGetParameterValue(hql, "and", "envValue", advanced.getEnvValue(), "sw.args", "$.jobEnvValues");
            argumentName = setHQLAndGetArgNames(hql, advanced.getArgumentName());
            argumentValue = setHQLAndGetArgValues(hql, advanced.getArgumentValue());
            break;
        case FILEORDERSOURCE:
        case SCHEDULE:
            if (!SOSString.isEmpty(advanced.getWorkflow())) {
                if (!advanced.getWorkflow().equals(FIND_ALL)) {
                    workflow = advanced.getWorkflow();

                    switch (type) {
                    case FILEORDERSOURCE:
                        hql.append("and lower(");
                        hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "mt.jsonContent", "$.workflowName"));
                        hql.append(") ");
                        hql.append("like :workflow ");
                        break;
                    case SCHEDULE:
                        hql.append("and exists (");
                        hql.append("select scheduleName from ").append(DBLayer.DBITEM_INV_SCHEDULE2WORKFLOWS).append(" subt ");
                        hql.append("where mt.name=subt.scheduleName ");
                        hql.append("and lower(subt.workflowName) like :workflow ");
                        hql.append(")");
                        break;
                    default:
                        break;
                    }
                }
            }

            // type - SCHEDULE
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

            // type - FILEORDERSOURCE
            if (!SOSString.isEmpty(advanced.getSchedule())) {
                hql.append("and exists (");
                hql.append("select scheduleName from ").append(DBLayer.DBITEM_INV_SCHEDULE2WORKFLOWS).append(" subt ");
                String add = " where ";
                if (!advanced.getSchedule().equals(FIND_ALL)) {
                    schedule = advanced.getSchedule();
                    hql.append(add).append("lower(subt.scheduleName) like :schedule ");
                    add = " and ";
                }
                if (workflow != null) {
                    hql.append(add).append("lower(subt.workflowName) like :workflow ");
                }
                hql.append(") ");
            }

            hql.append("and exists(");// start exists
            hql.append("  select sw.id from ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
            hql.append("  where sw.inventoryConfigurationId in (");
            hql.append("    select ic.id from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
            switch (type) {
            case FILEORDERSOURCE:
                hql.append("    where ic.name=").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "mt.jsonContent", "$.workflowName"));
                break;
            case SCHEDULE:
                hql.append("    where ic.name in(");
                hql.append("         select workflowName from ").append(DBLayer.DBITEM_INV_SCHEDULE2WORKFLOWS).append(" subt ");
                hql.append("         where mt.name=subt.scheduleName ");
                hql.append("    )");
                break;
            default:
                break;
            }
            hql.append("    and ic.type=").append(ConfigurationType.WORKFLOW.intValue()).append(" ");
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
            jobResources = setHQLAndGetParameterValue(hql, "and", "jobResources", advanced.getJobResources(), "sw.jobs", "$.jobResources");
            jobScript = setHQLAndGetParameterValue(hql, "and", "jobScript", advanced.getJobScript(), "sw.jobsScripts", "$.scripts");
            noticeBoards = setHQLAndGetParameterValue(hql, "and", "noticeBoards", advanced.getNoticeBoards(), "sw.instructions",
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
        hql.append("group by mt.id,mt.path,mt.folder,mt.name,mt.title,mt.valid,mt.deleted,mt.deployed,mt.released ");

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
            if (jobNameExactMatch) {
                query.setParameter("jobName", '%' + jobName + '%');
            } else {
                query.setParameter("jobName", '%' + jobName.toLowerCase() + '%');
            }
        }
        if (jobCriticality != null) {
            query.setParameter("jobCriticality", '%' + jobCriticality.toLowerCase() + '%');
        }
        if (jobResources != null) {
            query.setParameter("jobResources", '%' + jobResources.toLowerCase() + '%');
        }
        if (jobScript != null) {
            query.setParameter("jobScript", '%' + jobScript.toLowerCase() + '%');
        }
        if (noticeBoards != null) {
            query.setParameter("noticeBoards", '%' + noticeBoards.toLowerCase() + '%');
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
        if (isWorkflowType && jobNameExactMatch) {
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
                content = getDeployedConfigurationsContent(item.getId());
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

    public List<InventorySearchItem> getAdvancedSearchDeployedOrReleasedConfigurations(ConfigurationType type, String search, List<String> folders,
            RequestSearchAdvancedItem advanced, String controllerId) throws SOSHibernateException {

        boolean isReleasable = isReleasable(type);
        boolean searchInFolders = searchInFolders(folders);

        StringBuilder hql = new StringBuilder("select ");
        if (isReleasable) {
            hql.append("mt.cid as id");
            hql.append(",mt.path as path");
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
        boolean jobNameExactMatch = advanced.getJobNameExactMatch() != null && advanced.getJobNameExactMatch();
        String jobNameForExactMatch = SOSString.isEmpty(advanced.getJobName()) ? "" : advanced.getJobName();
        String jobResources = null;
        String jobScript = null;
        String noticeBoards = null;
        String lock = null;
        String envName = null;
        String envValue = null;
        String argumentName = null;
        String argumentValue = null;
        boolean isWorkflowType = false;

        switch (type) {
        case WORKFLOW:
            isWorkflowType = true;
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
                hql.append("select workflowName from ").append(DBLayer.DBITEM_INV_RELEASED_SCHEDULE2WORKFLOWS).append(" subt ");
                if (!advanced.getSchedule().equals(FIND_ALL)) {
                    schedule = advanced.getSchedule();
                    hql.append("where lower(subt.scheduleName) like :schedule ");
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
            jobResources = setHQLAndGetParameterValue(hql, "and", "jobResources", advanced.getJobResources(), "sw.jobs", "$.jobResources");
            jobScript = setHQLAndGetParameterValue(hql, "and", "jobScript", advanced.getJobScript(), "sw.jobsScripts", "$.scripts");
            noticeBoards = setHQLAndGetParameterValue(hql, "and", "noticeBoards", advanced.getNoticeBoards(), "sw.instructions",
                    "$.noticeBoardNames");
            lock = setHQLAndGetParameterValue(hql, "and", "lock", advanced.getLock(), "sw.instructions", "$.lockIds");
            envName = setHQLAndGetParameterValue(hql, "and", "envName", advanced.getEnvName(), "sw.args", "$.jobEnvNames");
            envValue = setHQLAndGetParameterValue(hql, "and", "envValue", advanced.getEnvValue(), "sw.args", "$.jobEnvValues");
            argumentName = setHQLAndGetArgNames(hql, advanced.getArgumentName());
            argumentValue = setHQLAndGetArgValues(hql, advanced.getArgumentValue());
            break;
        case FILEORDERSOURCE:
        case SCHEDULE:
            if (!SOSString.isEmpty(advanced.getWorkflow())) {
                if (!advanced.getWorkflow().equals(FIND_ALL)) {
                    workflow = advanced.getWorkflow();

                    switch (type) {
                    case FILEORDERSOURCE:
                        hql.append("and lower(");
                        hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "mt.content", "$.workflowName"));
                        hql.append(") ");
                        hql.append("like :workflow ");
                        break;
                    case SCHEDULE:
                        hql.append("and exists (");
                        hql.append("select scheduleName from ").append(DBLayer.DBITEM_INV_RELEASED_SCHEDULE2WORKFLOWS).append(" subt ");
                        hql.append("where mt.name=subt.scheduleName ");
                        hql.append("and lower(subt.workflowName) like :workflow ");
                        hql.append(")");
                        break;
                    default:
                        break;
                    }
                }
            }

            // type - SCHEDULE
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

            // type - FILEORDERSOURCE
            if (!SOSString.isEmpty(advanced.getSchedule())) {
                hql.append("and exists (");
                hql.append("select scheduleName from ").append(DBLayer.DBITEM_INV_RELEASED_SCHEDULE2WORKFLOWS).append(" subt ");
                String add = " where ";
                if (!advanced.getSchedule().equals(FIND_ALL)) {
                    schedule = advanced.getSchedule();
                    hql.append(add).append("lower(subt.scheduleName) like :schedule ");
                    add = " and ";
                }
                if (workflow != null) {
                    hql.append(add).append("lower(subt.workflowName) like :workflow ");
                }
                hql.append(") ");
            }

            hql.append("and exists(");// start exists
            hql.append("  select sw.id from ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
            hql.append("  ,").append(DBLayer.DBITEM_SEARCH_WORKFLOWS_DEPLOYMENT_HISTORY).append(" swdh ");
            hql.append("  where sw.id=swdh.searchWorkflowId ");
            hql.append("  and swdh.deploymentHistoryId in (");
            hql.append("    select dc.id from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" dc ");
            switch (type) {
            case FILEORDERSOURCE:
                hql.append("    where dc.name=").append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "mt.content", "$.workflowName"));
                break;
            case SCHEDULE:
                hql.append("    where dc.name in(");
                hql.append("         select workflowName from ").append(DBLayer.DBITEM_INV_RELEASED_SCHEDULE2WORKFLOWS).append(" subt ");
                hql.append("         where mt.name=subt.scheduleName ");
                hql.append("    )");
                break;
            default:
                break;
            }
            hql.append("    and dc.type=").append(ConfigurationType.WORKFLOW.intValue()).append(" ");
            if (controllerId != null) {
                hql.append("and dc.controllerId=:controllerId ");
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
            jobResources = setHQLAndGetParameterValue(hql, "and", "jobResources", advanced.getJobResources(), "sw.jobs", "$.jobResources");
            jobScript = setHQLAndGetParameterValue(hql, "and", "jobScript", advanced.getJobScript(), "sw.jobsScripts", "$.scripts");
            noticeBoards = setHQLAndGetParameterValue(hql, "and", "noticeBoards", advanced.getNoticeBoards(), "sw.instructions",
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
            if (jobNameExactMatch) {
                query.setParameter("jobName", '%' + jobName + '%');
            } else {
                query.setParameter("jobName", '%' + jobName.toLowerCase() + '%');
            }
        }
        if (jobCriticality != null) {
            query.setParameter("jobCriticality", '%' + jobCriticality.toLowerCase() + '%');
        }
        if (jobResources != null) {
            query.setParameter("jobResources", '%' + jobResources.toLowerCase() + '%');
        }
        if (jobScript != null) {
            query.setParameter("jobScript", '%' + jobScript.toLowerCase() + '%');
        }
        if (noticeBoards != null) {
            query.setParameter("noticeBoards", '%' + noticeBoards.toLowerCase() + '%');
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
        if (isWorkflowType && jobNameExactMatch) {
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
