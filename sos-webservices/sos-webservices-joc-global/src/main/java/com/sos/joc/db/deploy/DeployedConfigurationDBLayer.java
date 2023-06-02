package com.sos.joc.db.deploy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.criterion.MatchMode;
import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateFactory.Dbms;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.commons.hibernate.function.json.SOSHibernateJsonValue;
import com.sos.commons.hibernate.function.json.SOSHibernateJsonValue.ReturnType;
import com.sos.commons.hibernate.function.regex.SOSHibernateRegexp;
import com.sos.commons.util.SOSString;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.common.FolderPath;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.common.SearchStringHelper;
import com.sos.joc.db.deploy.items.Deployed;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.db.deploy.items.NumOfDeployment;
import com.sos.joc.db.inventory.items.InventoryNamePath;
import com.sos.joc.db.inventory.items.InventoryQuickSearchItem;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.tree.Tree;

public class DeployedConfigurationDBLayer {

    private SOSHibernateSession session;
    private String regexpParamPrefixSuffix = "";

    public DeployedConfigurationDBLayer(SOSHibernateSession connection) {
        this.session = connection;
        setRegexpParamPrefixSuffix();
    }
    
    public SOSHibernateSession getSession() {
        return session;
    }
    
    public List<InventoryQuickSearchItem> getQuickSearchInventoryConfigurations(String controllerId, Collection<Integer> types, String search)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select path as path, type as type, folder as folder, name as name from ").append(
                DBLayer.DBITEM_DEP_CONFIGURATIONS);
        List<String> whereClause = new ArrayList<>();
        if (types != null && !types.isEmpty()) {
            if (types.size() > 1) {
                whereClause.add("type in (:types)");
            } else {
                whereClause.add("type=:type");
            }
        }
        if (SOSString.isEmpty(search) || search.equals("*")) {
            search = null;
        } else {
            whereClause.add("lower(name) like :search");
        }
        if (SOSString.isEmpty(controllerId)) {
            controllerId = null;
        } else {
            whereClause.add("controllerId = :controllerId");
        }
        if (!whereClause.isEmpty()) {
            hql.append(whereClause.stream().collect(Collectors.joining(" and ", " where ", "")));
        }

        Query<InventoryQuickSearchItem> query = session.createQuery(hql.toString(), InventoryQuickSearchItem.class);
        if (types != null && !types.isEmpty()) {
            if (types.size() > 1) {
                query.setParameterList("types", types);
            } else {
                query.setParameter("type", types.iterator().next());
            }
        }
        if (search != null) {
            // (only) on the right hand side always %
            query.setParameter("search", SearchStringHelper.globToSqlPattern(search.toLowerCase() + '%').replaceAll("%%+", "%"));
        }
        if (controllerId != null) {
            query.setParameter("controllerId", controllerId);
        }
        return session.getResultList(query);
    }

    public DeployedContent getDeployedInventory(String controllerId, Integer type, String path) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            String name = JocInventory.pathToName(path);
            StringBuilder hql = new StringBuilder("select new ").append(DeployedContent.class.getName());
            hql.append("(path, name, title, content, commitId, created, true as isCurrentVersion) from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS);
            hql.append(" where controllerId = :controllerId");
            hql.append(" and type = :type");
            hql.append(" and name = :name");
            hql.append(" order by id desc");
            Query<DeployedContent> query = session.createQuery(hql.toString());
            query.setParameter("controllerId", controllerId);
            query.setParameter("type", type);
            query.setParameter("name", name);
            query.setMaxResults(1);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public DeployedContent getDeployedInventory(String controllerId, Integer type, String path, String commitId) throws DBConnectionRefusedException,
            DBInvalidDataException {
        if (commitId == null || commitId.isEmpty()) {
            return getDeployedInventory(controllerId, type, path);
        }
        try {
            String name = JocInventory.pathToName(path);
            StringBuilder hql = new StringBuilder("select new ").append(DeployedContent.class.getName());
            hql.append("(path, name, title, invContent, commitId, deploymentDate, false as isCurrentVersion) from ").append(
                    DBLayer.DBITEM_DEP_HISTORY);
            hql.append(" where controllerId = :controllerId");
            hql.append(" and type = :type");
            hql.append(" and name = :name");
            hql.append(" and commitId = :commitId");
            hql.append(" and operation = 0");
            hql.append(" and state = 0");
            hql.append(" order by id desc");
            Query<DeployedContent> query = session.createQuery(hql.toString());
            query.setParameter("controllerId", controllerId);
            query.setParameter("type", type);
            query.setParameter("name", name);
            query.setParameter("commitId", commitId);
            query.setMaxResults(1);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Map<ConfigurationType, Long> getNumOfDeployedObjects(String controllerId, Set<Folder> permittedFolders) {
        return getNumOfObjects(DBLayer.DBITEM_DEP_CONFIGURATIONS, controllerId, permittedFolders);
    }

    public Map<ConfigurationType, Long> getNumOfReleasedObjects(String controllerId, Set<Folder> permittedFolders) {
        return getNumOfObjects(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS, controllerId, permittedFolders);
    }

    private Map<ConfigurationType, Long> getNumOfObjects(String tableName, String controllerId, Set<Folder> permittedFolders) {
        try {
            boolean isReleasedObjects = DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS.equals(tableName);

            StringBuilder hql = new StringBuilder("select new ").append(NumOfDeployment.class.getName());
            hql.append("(type, count(id) as numof) from ").append(tableName);
            List<String> clauses = new ArrayList<>();
            if (isReleasedObjects) {// TODO or read all and override later ?
                clauses.add("type != :scheduleType");
            } else {
                if (!SOSString.isEmpty(controllerId)) {
                    clauses.add("controllerId = :controllerId");
                }
            }
            if (permittedFolders != null && !permittedFolders.isEmpty()) {
                String clause = permittedFolders.stream().map(folder -> {
                    if (folder.getRecursive()) {
                        return "(folder = '" + folder.getFolder() + "' or folder like '" + (folder.getFolder() + "/%").replaceAll("//+", "/") + "')";
                    } else {
                        return "folder = '" + folder.getFolder() + "'";
                    }
                }).collect(Collectors.joining(" or "));
                if (permittedFolders.size() > 1) {
                    clause = "(" + clause + ")";
                }
                clauses.add(clause);
            }
            if (!clauses.isEmpty()) {
                hql.append(clauses.stream().collect(Collectors.joining(" and ", " where ", "")));
            }
            hql.append(" group by type");

            Query<NumOfDeployment> query = session.createQuery(hql.toString());
            if (isReleasedObjects) {
                query.setParameter("scheduleType", ConfigurationType.SCHEDULE.intValue());
            } else {
                if (!SOSString.isEmpty(controllerId)) {
                    query.setParameter("controllerId", controllerId);
                }
            }

            Map<ConfigurationType, Long> result = new HashMap<>();
            List<NumOfDeployment> r = session.getResultList(query);
            if (r != null) {
                result = r.stream().filter(i -> i.getConfigurationType() != null).collect(Collectors.groupingBy(NumOfDeployment::getConfigurationType,
                        Collectors.summingLong(NumOfDeployment::getNumOf)));
            }
            if (isReleasedObjects) {
                result.put(ConfigurationType.SCHEDULE, getNumOfReleasedSchedules(controllerId, permittedFolders));
            }
            return result;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Long getNumOfReleasedSchedules(String controllerId, Set<Folder> permittedFolders) {
        try {
            StringBuilder hql = new StringBuilder("select count(distinct sw.scheduleName) ");
            hql.append("from ").append(DBLayer.DBITEM_INV_RELEASED_SCHEDULE2WORKFLOWS).append(" sw ");
            hql.append(",").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" dc ");
            hql.append("where dc.type=:workflowType ");
            hql.append("and sw.workflowName=dc.name ");
            if (!SOSString.isEmpty(controllerId)) {
                hql.append("and dc.controllerId=:controllerId ");
            }
            // folders
            boolean useFolders = FolderPath.useFolders(permittedFolders);
            Map<String, String> paramsFolder = new HashMap<>();
            Map<String, String> paramsLikeFolder = new HashMap<>();
            if (useFolders) {
                hql.append("and (");
                int i = 0;
                for (Folder folder : permittedFolders) {
                    if (i > 0) {
                        hql.append(" or ");
                    }
                    String paramNameFolder = "folder" + i;
                    if (folder.getRecursive()) {
                        String paramNameLikeFolder = "likeFolder" + i;
                        hql.append("(sw.scheduleFolder=:").append(paramNameFolder).append(" or sw.scheduleFolder like :").append(paramNameLikeFolder)
                                .append(") ");
                        paramsLikeFolder.put(paramNameLikeFolder, (folder.getFolder() + "/%").replaceAll("//+", "/"));
                    } else {
                        hql.append("sw.scheduleFolder=:").append(paramNameFolder).append(" ");
                    }
                    paramsFolder.put(paramNameFolder, folder.getFolder());
                    i++;
                }
                hql.append(") ");
            }

            Query<Long> query = session.createQuery(hql);
            query.setParameter("workflowType", DeployType.WORKFLOW.intValue());
            if (!SOSString.isEmpty(controllerId)) {
                query.setParameter("controllerId", controllerId);
            }
            if (useFolders) {
                paramsFolder.entrySet().stream().forEach(e -> {
                    query.setParameter(e.getKey(), e.getValue());
                });
                paramsLikeFolder.entrySet().stream().forEach(e -> {
                    query.setParameter(e.getKey(), e.getValue());
                });
            }
            Long result = session.getSingleResult(query);
            return result == null ? 0L : result;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Long getNumOfDeployedJobs(String controllerId, Set<Folder> permittedFolders) {
        try {
            StringBuilder hql = new StringBuilder("select sum(sw.jobsCount) as numofjobs from ");
            hql.append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
            hql.append("inner join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS_DEPLOYMENT_HISTORY).append(" swdh ");
            hql.append("on sw.id=swdh.searchWorkflowId ");
            hql.append("where sw.deployed=1 ");
            hql.append("and swdh.deploymentHistoryId in ");
            hql.append("(");
            hql.append(" select id from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS);
            hql.append(" where type=:workflowType");
            if (!SOSString.isEmpty(controllerId)) {
                hql.append(" and controllerId=:controllerId");
            }
            if (permittedFolders != null && !permittedFolders.isEmpty()) {
                String clause = permittedFolders.stream().map(folder -> {
                    if (folder.getRecursive()) {
                        return "(folder = '" + folder.getFolder() + "' or folder like '" + (folder.getFolder() + "/%").replaceAll("//+", "/") + "')";
                    } else {
                        return "folder = '" + folder.getFolder() + "'";
                    }
                }).collect(Collectors.joining(" or "));
                if (permittedFolders.size() > 1) {
                    clause = "(" + clause + ")";
                }
                hql.append(" and " + clause);
            }
            hql.append(")");
            Query<Long> query = session.createQuery(hql);
            query.setParameter("workflowType", DeployType.WORKFLOW.intValue());
            if (!SOSString.isEmpty(controllerId)) {
                query.setParameter("controllerId", controllerId);
            }
            Long result = session.getSingleResult(query);
            return result == null ? 0L : result;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DeployedContent> getDeployedInventory(DeployedConfigurationFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder hql = new StringBuilder("select new ").append(DeployedContent.class.getName());
            hql.append("(path, name, title, content, commitId, created, true as isCurrentVersion) from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS)
                    .append(getWhereForDepConfiguration(filter));
            Query<DeployedContent> query = createQuery(hql.toString(), filter);
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DeployedContent> getDeployedInventoryWithCommitIds(DeployedConfigurationFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder hql = new StringBuilder("select new ").append(DeployedContent.class.getName());
            hql.append("(path, name, title, invContent, commitId, deploymentDate, false as isCurrentVersion) from ").append(
                    DBLayer.DBITEM_DEP_HISTORY).append(getWhereForDepHistory(filter));
            Query<DeployedContent> query = createQuery(hql.toString(), filter);
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Map<Integer, Map<Long, String>> getDeployedNames(DeployedConfigurationFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder hql = new StringBuilder("select new ").append(Deployed.class.getName());
            hql.append("(inventoryConfigurationId, name, type) from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(getWhereForDepConfiguration(
                    filter));
            Query<Deployed> query = createQuery(hql.toString(), filter);
            List<Deployed> result = session.getResultList(query);
            if (result == null || result.isEmpty()) {
                return Collections.emptyMap();
            } else {
                return result.stream().collect(Collectors.groupingBy(Deployed::getObjectType, Collectors.toMap(Deployed::getInvCId, Deployed::getName,
                        (k, v) -> v)));
            }
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Map<Long, String> getDeployedName(String controllerId, Long invCId, Integer type) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder hql = new StringBuilder("select new ").append(Deployed.class.getName());
            hql.append("(inventoryConfigurationId, name, type) from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS);
            hql.append(" where controllerId = :controllerId and inventoryConfigurationId = :invCId and type = :type");
            Query<Deployed> query = session.createQuery(hql);
            query.setParameter("controllerId", controllerId);
            query.setParameter("invCId", invCId);
            query.setParameter("type", type);
            query.setMaxResults(1);
            Deployed result = session.getSingleResult(query);
            if (result == null) {
                return Collections.emptyMap();
            } else {
                return Collections.singletonMap(result.getInvCId(), result.getName());
            }
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Map<String, String> getNamePathMapping(String controllerId, List<String> names, Integer type) {
        if (names == null || names.isEmpty()) {
            return Collections.emptyMap();
        }
        if (names.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            Map<String, String> result = new HashMap<>();
            for (int i = 0; i < names.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                result.putAll(getNamePathMapping(controllerId, SOSHibernate.getInClausePartition(i, names), type));
            }
            return result;
        } else {
            try {
                StringBuilder hql = new StringBuilder("select new ").append(InventoryNamePath.class.getName());
                hql.append("(name, path) from ").append(DBLayer.DBITEM_DEP_NAMEPATHS);
                hql.append(" where name in (:names)");
                if (controllerId != null) {
                    hql.append(" and controllerId=:controllerId");
                }
                if (type != null) {
                    hql.append(" and type=:type");
                }
                Query<InventoryNamePath> query = session.createQuery(hql.toString());
                query.setParameterList("names", names);
                if (controllerId != null) {
                    query.setParameter("controllerId", controllerId);
                }
                if (type != null) {
                    query.setParameter("type", type);
                }
                List<InventoryNamePath> result = session.getResultList(query);
                if (result != null) {
                    return result.stream().distinct().collect(Collectors.toMap(InventoryNamePath::getName, InventoryNamePath::getPath));
                }
                return Collections.emptyMap();
            } catch (Exception e) {
                return Collections.emptyMap();
            }
        }
    }

    public List<WorkflowId> getUsedWorkflowsByPostNoticeBoard(String boardName, String controllerId) throws DBConnectionRefusedException,
            DBInvalidDataException {
        return getUsedWorkflowsByNoticeBoard(boardName, controllerId, "post");
    }

    public List<WorkflowId> getUsedWorkflowsByExpectedNoticeBoard(String boardName, String controllerId) throws DBConnectionRefusedException,
            DBInvalidDataException {
        return getUsedWorkflowsByNoticeBoard(boardName, controllerId, "expect");
    }
    
    public List<WorkflowId> getUsedWorkflowsByConsumeNoticeBoard(String boardName, String controllerId) throws DBConnectionRefusedException,
            DBInvalidDataException {
        return getUsedWorkflowsByNoticeBoard(boardName, controllerId, "consume");
    }

    // type = consume, post or expect
    private List<WorkflowId> getUsedWorkflowsByNoticeBoard(String boardName, String controllerId, String type) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder hql = new StringBuilder("select new ").append(WorkflowId.class.getName());
            hql.append("(dc.path, dc.commitId) from ");
            hql.append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" dc left join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
            hql.append("on dc.inventoryConfigurationId=sw.inventoryConfigurationId ");
            hql.append("where dc.type=:type ");
            hql.append("and dc.controllerId=:controllerId ");
            hql.append("and sw.deployed=1 ");
            hql.append("and ");

            String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "sw.instructions", "$." + type + "Notices");
            hql.append(SOSHibernateRegexp.getFunction(jsonFunc, ":boardName"));

            Query<WorkflowId> query = session.createQuery(hql.toString());
            query.setParameter("type", DeployType.WORKFLOW.intValue());
            query.setParameter("controllerId", controllerId);
            query.setParameter("boardName", getRegexpParameter(boardName, "\""));
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<String> getExpectedNoticeBoardWorkflows(String controllerId) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder hql = new StringBuilder("select dc.name from ");
            hql.append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" dc left join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
            hql.append("on dc.inventoryConfigurationId=sw.inventoryConfigurationId ");
            hql.append("where dc.type=:type ");
            hql.append("and dc.controllerId=:controllerId ");
            hql.append("and sw.deployed=1 ");
            hql.append("and ");
            hql.append(SOSHibernateJsonValue.getFunction(ReturnType.JSON, "sw.instructions", "$.expectNotices")).append(" is not null");

            Query<String> query = session.createQuery(hql.toString());
            query.setParameter("type", DeployType.WORKFLOW.intValue());
            query.setParameter("controllerId", controllerId);
            List<String> result = session.getResultList(query);
            if (result != null) {
                return result;
            }
            return Collections.emptyList();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<WorkflowId> getAddOrderWorkflowsByWorkflow(String workflowName, String controllerId) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder hql = new StringBuilder("select new ").append(WorkflowId.class.getName());
            hql.append("(dc.path, dc.commitId) from ");
            hql.append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" dc left join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
            hql.append("on dc.inventoryConfigurationId=sw.inventoryConfigurationId ");
            hql.append("where dc.type=:type ");
            hql.append("and dc.controllerId=:controllerId ");
            hql.append("and sw.deployed=1 ");
            hql.append("and ");

            String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "sw.instructions", "$.addOrders");
            hql.append(SOSHibernateRegexp.getFunction(jsonFunc, ":workflowName"));

            Query<WorkflowId> query = session.createQuery(hql.toString());
            query.setParameter("type", DeployType.WORKFLOW.intValue());
            query.setParameter("controllerId", controllerId);
            query.setParameter("workflowName", getRegexpParameter(workflowName, "\""));
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Set<String> getAddOrderWorkflows(String controllerId) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder hql = new StringBuilder("select ");
            hql.append(SOSHibernateJsonValue.getFunction(ReturnType.JSON, "sw.instructions", "$.addOrders")).append(" as addOrders from ");
            hql.append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" dc left join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
            hql.append("on dc.inventoryConfigurationId=sw.inventoryConfigurationId ");
            hql.append("where dc.type=:type ");
            hql.append("and dc.controllerId=:controllerId ");
            hql.append("and sw.deployed=1 ");
            hql.append("and ");
            hql.append(SOSHibernateJsonValue.getFunction(ReturnType.JSON, "sw.instructions", "$.addOrders")).append(" is not null");

            Query<String> query = session.createQuery(hql.toString());
            query.setParameter("type", DeployType.WORKFLOW.intValue());
            query.setParameter("controllerId", controllerId);
            List<String> result = session.getResultList(query);
            if (result != null) {
                return result.stream().map(s -> {
                    try {
                        return Arrays.asList(Globals.objectMapper.readValue(s, String[].class));
                    } catch (Exception e) {
                        return null;
                    }
                }).filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toSet());
            }
            return Collections.emptySet();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<WorkflowId> getWorkflowsIds(List<String> workflowNames, String controllerId) throws DBConnectionRefusedException,
            DBInvalidDataException {
        if (workflowNames != null && workflowNames.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            List<WorkflowId> result = new ArrayList<>();
            for (int i = 0; i < workflowNames.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                result.addAll(getWorkflowsIds(SOSHibernate.getInClausePartition(i, workflowNames), controllerId));
            }
            return result;
        } else {
            try {
                StringBuilder hql = new StringBuilder("select new ").append(WorkflowId.class.getName());
                hql.append("(path, commitId) from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS);
                hql.append(" where type=:type");
                hql.append(" and controllerId=:controllerId");
                if (workflowNames != null && !workflowNames.isEmpty()) {
                    hql.append(" and name in (:workflowNames)");
                }

                Query<WorkflowId> query = session.createQuery(hql.toString());
                query.setParameter("type", DeployType.WORKFLOW.intValue());
                query.setParameter("controllerId", controllerId);
                if (workflowNames != null && !workflowNames.isEmpty()) {
                    query.setParameterList("workflowNames", workflowNames);
                }
                List<WorkflowId> result = session.getResultList(query);
                if (result == null) {
                    return Collections.emptyList();
                }
                return result;
            } catch (SOSHibernateInvalidSessionException ex) {
                throw new DBConnectionRefusedException(ex);
            } catch (Exception ex) {
                throw new DBInvalidDataException(ex);
            }
        }
    }

    public Set<Tree> getFoldersByFolderAndType(String controllerId, String folderName, Collection<Integer> types) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select folder from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS);
            sql.append(" where controllerId = :controllerId");
            if (folderName != null && !folderName.isEmpty() && !folderName.equals("/")) {
                sql.append(" and (folder = :folderName or folder like :likeFolderName)");
            }
            if (types != null && !types.isEmpty()) {
                if (types.size() == 1) {
                    sql.append(" and type = :type");
                } else {
                    sql.append(" and type in (:type)");
                }
            }
            sql.append(" group by folder");
            Query<String> query = session.createQuery(sql.toString());
            query.setParameter("controllerId", controllerId);
            if (folderName != null && !folderName.isEmpty() && !folderName.equals("/")) {
                query.setParameter("folderName", folderName);
                query.setParameter("likeFolderName", MatchMode.START.toMatchString(folderName + "/"));
            }
            if (types != null && !types.isEmpty()) {
                if (types.size() == 1) {
                    query.setParameter("type", types.iterator().next());
                } else {
                    query.setParameterList("type", types);
                }
            }
            List<String> result = session.getResultList(query);
            if (result != null && !result.isEmpty()) {
                return result.stream().map(s -> {
                    Tree tree = new Tree();
                    tree.setPath(s);
                    return tree;
                }).collect(Collectors.toSet());
            } else if (folderName.equals(JocInventory.ROOT_FOLDER)) {
                Tree tree = new Tree();
                tree.setPath(JocInventory.ROOT_FOLDER);
                return Arrays.asList(tree).stream().collect(Collectors.toSet());
            }
            return Collections.emptySet();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    private String getWhereForDepHistory(DeployedConfigurationFilter filter) {
        return getWhere(filter, true);
    }

    private String getWhereForDepConfiguration(DeployedConfigurationFilter filter) {
        return getWhere(filter, false);
    }

    private String getWhere(DeployedConfigurationFilter filter, boolean withOperationAndState) {
        List<String> clauses = new ArrayList<String>();

        if (filter.getControllerId() != null && !filter.getControllerId().isEmpty()) {
            clauses.add("controllerId = :controllerId");
        }

        // TODO consider max in
        if (filter.getNames() != null && !filter.getNames().isEmpty()) {
            if (filter.getNames().size() == 1) {
                clauses.add("name = :name");
            } else {
                clauses.add("name in (:names)");
            }
        }

        // TODO consider max in
        if (filter.getWorkflowIds() != null && !filter.getWorkflowIds().isEmpty()) {
            if (filter.getWorkflowIds().size() == 1) {
                clauses.add("concat(name, '/', commitId) = :workflowId");
            } else {
                clauses.add("concat(name, '/', commitId) in (:workflowIds)");
            }
        }

        if (filter.getObjectTypes() != null && !filter.getObjectTypes().isEmpty()) {
            if (filter.getObjectTypes().size() == 1) {
                clauses.add("type = :type");
            } else {
                clauses.add("type in (:types)");
            }
        }

        if (filter.getFolders() != null && !filter.getFolders().isEmpty()) {
            String clause = filter.getFolders().stream().map(folder -> {
                if (folder.getRecursive()) {
                    return "(folder = '" + folder.getFolder() + "' or folder like '" + (folder.getFolder() + "/%").replaceAll("//+", "/") + "')";
                } else {
                    return "folder = '" + folder.getFolder() + "'";
                }
            }).collect(Collectors.joining(" or "));
            if (filter.getFolders().size() > 1) {
                clause = "(" + clause + ")";
            }
            clauses.add(clause);
        }

        if (withOperationAndState) {
            clauses.add("operation = 0");
            clauses.add("state = 0");
        }

        if (!clauses.isEmpty()) {
            return clauses.stream().collect(Collectors.joining(" and ", " where ", ""));
        }
        return "";
    }

    private <T> Query<T> createQuery(String hql, DeployedConfigurationFilter filter) throws SOSHibernateException {
        Query<T> query = session.createQuery(hql);
        if (filter.getControllerId() != null && !filter.getControllerId().isEmpty()) {
            query.setParameter("controllerId", filter.getControllerId());
        }
        if (filter.getNames() != null && !filter.getNames().isEmpty()) {
            if (filter.getNames().size() == 1) {
                query.setParameter("name", filter.getNames().iterator().next());
            } else {
                query.setParameterList("names", filter.getNames());
            }
        }
        if (filter.getWorkflowIds() != null && !filter.getWorkflowIds().isEmpty()) {
            if (filter.getWorkflowIds().size() == 1) {
                query.setParameter("workflowId", filter.getWorkflowIds().stream().map(w -> JocInventory.pathToName(w.getPath()) + "/" + w
                        .getVersionId()).iterator().next());
            } else {
                query.setParameterList("workflowIds", filter.getWorkflowIds().stream().map(w -> JocInventory.pathToName(w.getPath()) + "/" + w
                        .getVersionId()).collect(Collectors.toSet()));
            }
        }
        if (filter.getObjectTypes() != null && !filter.getObjectTypes().isEmpty()) {
            if (filter.getObjectTypes().size() == 1) {
                query.setParameter("type", filter.getObjectTypes().iterator().next());
            } else {
                query.setParameterList("types", filter.getObjectTypes());
            }
        }
        return query;
    }

    private String getRegexpParameter(String param, String prefixSuffix) {
        return regexpParamPrefixSuffix + prefixSuffix + param + prefixSuffix + regexpParamPrefixSuffix;
    }

    private void setRegexpParamPrefixSuffix() {
        try {
            if (Dbms.MSSQL.equals(session.getFactory().getDbms())) {
                regexpParamPrefixSuffix = "%";
            }
        } catch (Throwable e) {
        }
    }
}
