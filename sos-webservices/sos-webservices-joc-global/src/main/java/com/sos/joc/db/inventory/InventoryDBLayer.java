package com.sos.joc.db.inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.tree.Tree;

public class InventoryDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;

    public InventoryDBLayer(SOSHibernateSession session) {
        super(session);
    }

    public List<DBItemInventoryConfiguration> getConfigurationsByFolder(String folder, Long type) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where folder=:folder");
        if (type != null) {
            hql.append(" and type=:type");
        }
        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("folder", folder);
        if (type != null) {
            query.setParameter("type", type);
        }
        return getSession().getResultList(query);
    }

    public DBItemInventoryConfiguration getConfiguration(Long id, Long type) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where id=:id");
        hql.append(" and type=:type");
        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("id", id);
        query.setParameter("type", type);
        return getSession().getSingleResult(query);
    }

    public DBItemInventoryConfiguration getConfiguration(String path, Long type) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where path=:path");
        hql.append(" and type=:type");
        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("path", path);
        query.setParameter("type", type);
        return getSession().getSingleResult(query);
    }

    public DBItemInventoryWorkflow getWorkflow(Long configId) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_WORKFLOWS);
        hql.append(" where configId=:configId");
        Query<DBItemInventoryWorkflow> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getSingleResult(query);
    }

    public DBItemInventoryWorkflowJob getWorkflowJob(Long configId) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_WORKFLOW_JOBS);
        hql.append(" where configId=:configId");
        Query<DBItemInventoryWorkflowJob> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getSingleResult(query);
    }

    public List<DBItemInventoryWorkflowJob> getWorkflowJobs(Long workflowConfigId) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_WORKFLOW_JOBS);
        hql.append(" where workflowConfigId=:workflowConfigId");
        Query<DBItemInventoryWorkflowJob> query = getSession().createQuery(hql.toString());
        query.setParameter("workflowConfigId", workflowConfigId);
        return getSession().getResultList(query);
    }

    public DBItemInventoryJobClass getJobClass(Long configId) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_JOB_CLASSES);
        hql.append(" where configId=:configId");
        Query<DBItemInventoryJobClass> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getSingleResult(query);
    }

    public DBItemInventoryAgentCluster getAgentCluster(Long configId) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_AGENT_CLUSTERS);
        hql.append(" where configId=:configId");
        Query<DBItemInventoryAgentCluster> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getSingleResult(query);
    }

    public List<DBItemInventoryAgentClusterMember> getAgentClusterMembers(Long agentClusterId) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_AGENT_CLUSTER_MEMBERS);
        hql.append(" where agentClusterId=:agentClusterId");
        Query<DBItemInventoryAgentClusterMember> query = getSession().createQuery(hql.toString());
        query.setParameter("agentClusterId", agentClusterId);
        return getSession().getResultList(query);
    }

    public DBItemInventoryLock getLock(Long configId) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_LOCKS);
        hql.append(" where configId=:configId");
        Query<DBItemInventoryLock> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getSingleResult(query);
    }

    public DBItemInventoryJunction getJunction(Long configId) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_JUNCTIONS);
        hql.append(" where configId=:configId");
        Query<DBItemInventoryJunction> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getSingleResult(query);
    }

    @SuppressWarnings("unchecked")
    public <T extends Tree> Set<T> getFoldersByFolderAndType(String folder, Set<Long> types) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            List<String> whereClause = new ArrayList<String>();
            StringBuilder sql = new StringBuilder();
            sql.append("select folder from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            if (folder != null && !folder.isEmpty() && !folder.equals("/")) {
                whereClause.add("(folder = :folder or folder like :likeFolder)");
            }
            if (types != null && !types.isEmpty()) {
                if (types.size() == 1) {
                    whereClause.add("type = :type");
                } else {
                    whereClause.add("type in (:type)");
                }
            }
            if (!whereClause.isEmpty()) {
                sql.append(whereClause.stream().collect(Collectors.joining(" and ", " where ", "")));
            }
            sql.append(" group by folder");
            Query<String> query = getSession().createQuery(sql.toString());
            if (folder != null && !folder.isEmpty() && !folder.equals("/")) {
                query.setParameter("folder", folder);
                query.setParameter("likeFolder", folder + "/%");
            }
            if (types != null && !types.isEmpty()) {
                if (types.size() == 1) {
                    // query.setParameter("type", InventoryMeta.ConfigurationType.valueOf(objectTypes.iterator().next()).value());
                    query.setParameter("type", types.iterator().next());
                } else {
                    query.setParameterList("type", types);// TODO
                }
            }
            List<String> result = getSession().getResultList(query);
            if (result != null && !result.isEmpty()) {
                return result.stream().map(s -> {
                    T tree = (T) new Tree(); // new JoeTree();
                    tree.setPath(s);
                    return tree;
                }).collect(Collectors.toSet());
            } else if (folder.equals("/")) {
                T tree = (T) new Tree();
                tree.setPath("/");
                return Arrays.asList(tree).stream().collect(Collectors.toSet());
            }
            return new HashSet<T>();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

}
