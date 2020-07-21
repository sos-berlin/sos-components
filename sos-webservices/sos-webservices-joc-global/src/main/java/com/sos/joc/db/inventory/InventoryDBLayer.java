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
import com.sos.joc.db.joc.DBItemJocLock;
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

    public List<DBItemJocLock> getJocLocks() throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_JOC_LOCKS);
            hql.append(" where range=:range");
            Query<DBItemJocLock> query = getSession().createQuery(hql.toString());
            query.setParameter("range", DBItemJocLock.LockRange.INVENTORY.value());
            return getSession().getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public InvertoryDeleteResult deleteWorkflow(Long configId) throws Exception {
        InvertoryDeleteResult result = new InvertoryDeleteResult();

        String hql = String.format("delete from %s where configIdWorkflow=:configId", DBLayer.DBITEM_INV_WORKFLOW_JUNCTIONS);
        Query<DBItemInventoryWorkflowJunction> queryWj = getSession().createQuery(hql.toString());
        queryWj.setParameter("configId", configId);
        result.setWorkflowJunctions(getSession().executeUpdate(queryWj));

        // TODO delete workflowJobs, workflowJobNodes, workflowJobNodeArgs

        hql = String.format("delete from %s where configId=:configId", DBLayer.DBITEM_INV_WORKFLOWS);
        Query<DBItemInventoryWorkflow> queryW = getSession().createQuery(hql.toString());
        queryW.setParameter("configId", configId);
        result.setWorkflows(getSession().executeUpdate(queryW));

        result.setConfigurations(deleteConfiguration(configId));

        return result;
    }

    private int deleteConfiguration(Long id) throws Exception {
        String hql = String.format("delete from %s where id=:id", DBLayer.DBITEM_INV_CONFIGURATIONS);
        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("id", id);
        return getSession().executeUpdate(query);
    }

    public InvertoryDeleteResult deleteJobClass(Long configId) throws Exception {
        InvertoryDeleteResult result = new InvertoryDeleteResult();

        String hql = String.format("delete from %s where configId=:configId", DBLayer.DBITEM_INV_JOB_CLASSES);
        Query<DBItemInventoryJobClass> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        result.setJobClasses(getSession().executeUpdate(query));

        // TODO update workflowJobs ????

        result.setConfigurations(deleteConfiguration(configId));

        return result;
    }

    public InvertoryDeleteResult deleteAgentCluster(Long configId) throws Exception {
        InvertoryDeleteResult result = new InvertoryDeleteResult();

        String hql = String.format("delete from %s where agentClusterId=:configId", DBLayer.DBITEM_INV_AGENT_CLUSTER_MEMBERS);
        Query<DBItemInventoryAgentClusterMember> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        result.setAgentClusterMembers(getSession().executeUpdate(query));

        hql = String.format("delete from %s where configId=:configId", DBLayer.DBITEM_INV_AGENT_CLUSTERS);
        Query<DBItemInventoryAgentCluster> queryAc = getSession().createQuery(hql.toString());
        queryAc.setParameter("configId", configId);
        result.setAgentClusters(getSession().executeUpdate(queryAc));

        result.setConfigurations(deleteConfiguration(configId));

        return result;
    }

    public void deleteAll() throws Exception {
        // TODO all inventory tables
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_AGENT_CLUSTER_MEMBERS);
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_AGENT_CLUSTERS);
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_CONFIGURATIONS);
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_JOB_CLASSES);
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_JUNCTIONS);
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_LOCKS);
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_WORKFLOW_JOB_ARGUMENTS);
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_WORKFLOW_JOB_NODE_ARGUMENTS);
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_WORKFLOW_JOB_NODES);
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_WORKFLOW_JOBS);
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_WORKFLOW_JUNCTIONS);
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_WORKFLOWS);
    }

    public InvertoryDeleteResult deleteLock(Long configId) throws Exception {
        InvertoryDeleteResult result = new InvertoryDeleteResult();

        String hql = String.format("delete from %s where configId=:configId", DBLayer.DBITEM_INV_LOCKS);
        Query<DBItemInventoryLock> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        result.setLocks(getSession().executeUpdate(query));

        // TODO delete from join table

        result.setConfigurations(deleteConfiguration(configId));

        return result;
    }

    public InvertoryDeleteResult deleteJunction(Long configId) throws Exception {
        InvertoryDeleteResult result = new InvertoryDeleteResult();

        String hql = String.format("delete from %s where configId=:configId", DBLayer.DBITEM_INV_JUNCTIONS);
        Query<DBItemInventoryJunction> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        result.setLocks(getSession().executeUpdate(query));

        hql = String.format("delete from %s where configIdJunction=:configId", DBLayer.DBITEM_INV_WORKFLOW_JUNCTIONS);
        Query<DBItemInventoryWorkflowJunction> queryWj = getSession().createQuery(hql.toString());
        queryWj.setParameter("configId", configId);
        result.setWorkflowJunctions(getSession().executeUpdate(queryWj));

        // TODO delete from workflow ??

        result.setConfigurations(deleteConfiguration(configId));

        return result;
    }

    public List<DBItemInventoryConfiguration> getConfigurationsByFolder(String folder) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where (folder=:folder or folder like :likeFolder)");

        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("folder", folder);
        query.setParameter("likeFolder", folder + "/%");
        return getSession().getResultList(query);
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
                    query.setParameter("type", types.iterator().next());
                } else {
                    query.setParameterList("type", types);
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

    public class InvertoryDeleteResult {

        private int configurations;
        private int workflows;
        private int workflowJunctions;
        private int workflowJobs;
        private int jobClasses;
        private int agentClusters;
        private int agentClusterMembers;
        private int locks;

        public int getConfigurations() {
            return configurations;
        }

        public void setConfigurations(int configurations) {
            this.configurations = configurations;
        }

        public int getWorkflows() {
            return workflows;
        }

        public void setWorkflows(int workflows) {
            this.workflows = workflows;
        }

        public int getWorkflowJunctions() {
            return workflowJunctions;
        }

        public void setWorkflowJunctions(int workflowJunctions) {
            this.workflowJunctions = workflowJunctions;
        }

        public int getWorkflowJobs() {
            return workflowJobs;
        }

        public void setWorkflowJobs(int workflowJobs) {
            this.workflowJobs = workflowJobs;
        }

        public int getJobClasses() {
            return jobClasses;
        }

        public void setJobClasses(int jobClasses) {
            this.jobClasses = jobClasses;
        }

        public int getAgentClusters() {
            return agentClusters;
        }

        public void setAgentClusters(int agentClusters) {
            this.agentClusters = agentClusters;
        }

        public int getAgentClusterMembers() {
            return agentClusterMembers;
        }

        public void setAgentClusterMembers(int agentClusterMembers) {
            this.agentClusterMembers = agentClusterMembers;
        }

        public int getLocks() {
            return locks;
        }

        public void setLocks(int locks) {
            this.locks = locks;
        }
    }

}
