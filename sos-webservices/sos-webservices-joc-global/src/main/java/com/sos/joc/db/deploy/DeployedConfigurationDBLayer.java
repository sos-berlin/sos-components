package com.sos.joc.db.deploy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.criterion.MatchMode;
import org.hibernate.query.Query;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.db.deploy.items.NumOfDeployment;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.tree.Tree;
import com.sos.joc.model.workflow.WorkflowFilter;

public class DeployedConfigurationDBLayer {

    private SOSHibernateSession session;

    public DeployedConfigurationDBLayer(SOSHibernateSession connection) {
        this.session = connection;
    }

    public DeployedContent getDeployedInventory(String controllerId, Integer type, String path) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder hql = new StringBuilder("select new ").append(DeployedContent.class.getName());
            hql.append("(path, content) from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS);
            hql.append(" where controllerId = :controllerId");
            hql.append(" and type = :type");
            hql.append(" and path = :path");
            Query<DeployedContent> query = session.createQuery(hql.toString());
            query.setParameter("controllerId", controllerId);
            query.setParameter("type", type);
            query.setParameter("path", path);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public DeployedContent getDeployedInventory(String controllerId, Integer type, String path, String commitId)
            throws DBConnectionRefusedException, DBInvalidDataException {
        if (commitId == null || commitId.isEmpty()) {
            return getDeployedInventory(controllerId, type, path);
        }
        try {
            StringBuilder hql = new StringBuilder("select new ").append(DeployedContent.class.getName());
            hql.append("(path, content) from ").append(DBLayer.DBITEM_DEP_HISTORY);
            hql.append(" where controllerId = :controllerId");
            hql.append(" and type = :type");
            hql.append(" and path = :path");
            hql.append(" and commitId = :commitId");
            Query<DeployedContent> query = session.createQuery(hql.toString());
            query.setParameter("controllerId", controllerId);
            query.setParameter("type", type);
            query.setParameter("path", path);
            query.setParameter("commitId", commitId);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public Workflow getDeployedInventory(WorkflowFilter workflowFilter) throws DBConnectionRefusedException, DBInvalidDataException,
            JsonParseException, JsonMappingException, IOException {
        DeployedContent content = getDeployedInventory(workflowFilter.getControllerId(), DeployType.WORKFLOW.intValue(), workflowFilter.getWorkflowId()
                .getPath(), workflowFilter.getWorkflowId().getVersionId());
        if (content != null && content.getContent() != null && !content.getContent().isEmpty()) {
            Workflow workflow =  Globals.objectMapper.readValue(content.getContent(), Workflow.class);
            workflow.setPath(content.getPath());
            return workflow;
        } else {
            return null;
        }
    }
    
    public Map<ConfigurationType, Long> getNumOfDeployedObjects(String controllerId) {
        try {
            StringBuilder hql = new StringBuilder("select new ").append(NumOfDeployment.class.getName());
            hql.append("(type, count(id) as numof) from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS);
            if (controllerId != null && !controllerId.isEmpty()) {
                hql.append(" where controllerId = :controllerId");
            }
            hql.append(" group by type");
            Query<NumOfDeployment> query = session.createQuery(hql.toString());
            if (controllerId != null && !controllerId.isEmpty()) {
                query.setParameter("controllerId", controllerId);
            }
            List<NumOfDeployment> result = session.getResultList(query);
            if (result != null) {
                return result.stream().filter(i -> i.getConfigurationType() != null).collect(Collectors.toMap(NumOfDeployment::getConfigurationType,
                        NumOfDeployment::getNumOf));
            }
            return Collections.emptyMap();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public Long getNumOfDeployedJobs(String controllerId) {
        try {
            StringBuilder hql = new StringBuilder("select sum(sw.jobsCount) as numofjobs from ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS);
            hql.append(" sw inner join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS_DEPLOYMENT_HISTORY).append(" swdh");
            hql.append(" on sw.id = swdh.searchWorkflowId");
            hql.append(" where sw.deployed = 1");
            if (controllerId != null && !controllerId.isEmpty()) {
                hql.append(" and swdh.controllerId = :controllerId");
            }
            Query<Long> query = session.createQuery(hql.toString());
            if (controllerId != null && !controllerId.isEmpty()) {
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
            hql.append("(path, content) from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(getWhere(filter));
            Query<DeployedContent> query = createQuery(hql.toString(), filter);
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public List<DeployedContent> getDeployedInventoryWithCommitIds(DeployedConfigurationFilter filter) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder hql = new StringBuilder("select new ").append(DeployedContent.class.getName());
            hql.append("(path, content) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(getWhere(filter));
            Query<DeployedContent> query = createQuery(hql.toString(), filter);
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public Map<String, String> getNamePathMapping(Collection<String> names, Integer type) throws SOSHibernateException {
        if (names == null || names.isEmpty()) {
            return Collections.emptyMap();
        }
        StringBuilder hql = new StringBuilder("select path, name from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS);
        hql.append(" where name in (:names)");
        if (type != null) {
            hql.append(" and type=:type");
        }
        Query<String[]> query = session.createQuery(hql.toString());
        query.setParameterList("names", names);
        if (type != null) {
            query.setParameter("type", type);
        }
        
        List<String[]> result = session.getResultList(query);
        if (result != null) {
            return result.stream().collect(Collectors.toMap(item -> item[1], item -> item[0]));
        }
        return Collections.emptyMap();
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

    private String getWhere(DeployedConfigurationFilter filter) {
        List<String> clauses = new ArrayList<String>();

        if (filter.getControllerId() != null && !filter.getControllerId().isEmpty()) {
            clauses.add("controllerId = :controllerId");
        }

        if (filter.getPaths() != null && !filter.getPaths().isEmpty()) {
            if (filter.getPaths().size() == 1) {
                clauses.add("path = :path");
            } else {
                clauses.add("path in (:paths)");
            }
        }
        
        if (filter.getWorkflowIds() != null && !filter.getWorkflowIds().isEmpty()) {
            if (filter.getWorkflowIds().size() == 1) {
                clauses.add("concat(path, '/', commitId) = :workflowId");
            } else {
                clauses.add("concat(path, '/', commitId) in (:workflowIds)");
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
        if (filter.getPaths() != null && !filter.getPaths().isEmpty()) {
            if (filter.getPaths().size() == 1) {
                query.setParameter("path", filter.getPaths().iterator().next());
            } else {
                query.setParameterList("paths", filter.getPaths());
            }
        }
        if (filter.getWorkflowIds() != null && !filter.getWorkflowIds().isEmpty()) {
            if (filter.getWorkflowIds().size() == 1) {
                query.setParameter("workflowId", filter.getWorkflowIds().stream().map(w -> w.getPath() + "/" + w.getVersionId()).iterator().next());
            } else {
                query.setParameterList("workflowIds", filter.getWorkflowIds().stream().map(w -> w.getPath() + "/" + w.getVersionId()).collect(
                        Collectors.toSet()));
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

}
