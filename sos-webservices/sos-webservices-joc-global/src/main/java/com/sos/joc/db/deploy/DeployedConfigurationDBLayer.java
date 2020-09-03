package com.sos.joc.db.deploy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.jobscheduler.model.deploy.DeployObject;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.deployment.DBItemDepConfiguration;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.tree.Tree;
import com.sos.joc.model.workflow.WorkflowFilter;

public class DeployedConfigurationDBLayer {

    private SOSHibernateSession session;

    public DeployedConfigurationDBLayer(SOSHibernateSession connection) {
        this.session = connection;
    }

    public String getDeployedInventory(String controllerId, Integer objectType, String path) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select content from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS);
            sql.append(" where controllerId = :controllerId");
            sql.append(" and objectType = :objectType");
            sql.append(" and path = :path");
            Query<String> query = session.createQuery(sql.toString());
            query.setParameter("controllerId", controllerId);
            query.setParameter("objectType", objectType);
            query.setParameter("path", path);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public String getDeployedInventory(String controllerId, Integer objectType, String path, String commitId)
            throws DBConnectionRefusedException, DBInvalidDataException {
        if (commitId == null || commitId.isEmpty()) {
            return getDeployedInventory(controllerId, objectType, path);
        }
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select content from ").append(DBLayer.DBITEM_DEP_HISTORY);
            sql.append(" where controllerId = :controllerId");
            sql.append(" and objectType = :objectType");
            sql.append(" and path = :path");
            sql.append(" and commitId = :commitId");
            Query<String> query = session.createQuery(sql.toString());
            query.setParameter("controllerId", controllerId);
            query.setParameter("objectType", objectType);
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
        String content = getDeployedInventory(workflowFilter.getJobschedulerId(), DeployType.WORKFLOW.ordinal(), workflowFilter.getWorkflowId()
                .getPath(), workflowFilter.getWorkflowId().getVersionId());
        if (content != null && !content.isEmpty()) {
            return (Workflow) Globals.objectMapper.readValue(content, Workflow.class);
        } else {
            return null;
        }
    }

    public List<DBItemDepConfiguration> getDeployedInventory(DeployedConfigurationFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            Query<DBItemDepConfiguration> query = createQuery("from " + DBLayer.DBITEM_DEP_CONFIGURATIONS + getWhere(filter), filter);
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
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
                    sql.append(" and objectType = :objectType");
                } else {
                    sql.append(" and objectType in (:objectType)");
                }
            }
            sql.append(" group by folder");
            Query<String> query = session.createQuery(sql.toString());
            query.setParameter("controllerId", controllerId);
            if (folderName != null && !folderName.isEmpty() && !folderName.equals("/")) {
                query.setParameter("folderName", folderName);
                query.setParameter("likeFolderName", folderName + "/%");
            }
            if (types != null && !types.isEmpty()) {
                if (types.size() == 1) {
                    query.setParameter("objectType", types.iterator().next());
                } else {
                    query.setParameterList("objectType", types);
                }
            }
            List<String> result = session.getResultList(query);
            if (result != null && !result.isEmpty()) {
                return result.stream().map(s -> {
                    Tree tree = new Tree();
                    tree.setPath(s);
                    return tree;
                }).collect(Collectors.toSet());
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

        if (filter.getObjectTypes() != null && !filter.getObjectTypes().isEmpty()) {
            if (filter.getObjectTypes().size() == 1) {
                clauses.add("objectType = :objectType");
            } else {
                clauses.add("objectType in (:objectTypes)");
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
        if (filter.getObjectTypes() != null && !filter.getObjectTypes().isEmpty()) {
            if (filter.getPaths().size() == 1) {
                query.setParameter("objectType", filter.getObjectTypes().iterator().next());
            } else {
                query.setParameterList("objectTypes", filter.getObjectTypes());
            }
        }
        return query;
    }

}
