package com.sos.joc.db.inventory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.commons.util.SOSString;
import com.sos.joc.classes.inventory.JocInventory;
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

    public List<DBItemInventoryConfiguration> getConfigurationsByFolder(String folder, boolean recursive) throws Exception {
        return getConfigurationsByFolder(folder, recursive, null);
    }

    public List<DBItemInventoryConfiguration> getConfigurationsByFolder(String folder, boolean recursive, Long type) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where ");
        if (recursive) {
            hql.append("(folder=:folder or folder like :likeFolder) ");
        } else {
            hql.append("folder=:folder ");
        }
        if (type != null) {
            hql.append("and type=:type");
        }
        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("folder", folder);
        if (recursive) {
            query.setParameter("likeFolder", folder + "/%");
        }
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
        hql.append(" where cid=:configId");
        Query<DBItemInventoryWorkflow> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getSingleResult(query);
    }

    public DBItemInventoryWorkflowJob getWorkflowJob(Long configId) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_WORKFLOW_JOBS);
        hql.append(" where cid=:configId");
        Query<DBItemInventoryWorkflowJob> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getSingleResult(query);
    }

    public List<DBItemInventoryWorkflowJob> getWorkflowJobs(Long workflowConfigId) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_WORKFLOW_JOBS);
        hql.append(" where cidWorkflow=:workflowConfigId");
        Query<DBItemInventoryWorkflowJob> query = getSession().createQuery(hql.toString());
        query.setParameter("workflowConfigId", workflowConfigId);
        return getSession().getResultList(query);
    }

    public DBItemInventoryJobClass getJobClass(Long configId) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_JOB_CLASSES);
        hql.append(" where cid=:configId");
        Query<DBItemInventoryJobClass> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getSingleResult(query);
    }

    public DBItemInventoryAgentCluster getAgentCluster(Long configId) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_AGENT_CLUSTERS);
        hql.append(" where cid=:configId");
        Query<DBItemInventoryAgentCluster> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getSingleResult(query);
    }

    public List<DBItemInventoryAgentClusterMember> getAgentClusterMembers(Long agentClusterId) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_AGENT_CLUSTER_MEMBERS);
        hql.append(" where cidAgentCluster=:agentClusterId");
        Query<DBItemInventoryAgentClusterMember> query = getSession().createQuery(hql.toString());
        query.setParameter("agentClusterId", agentClusterId);
        return getSession().getResultList(query);
    }

    public DBItemInventoryLock getLock(Long configId) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_LOCKS);
        hql.append(" where cid=:configId");
        Query<DBItemInventoryLock> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getSingleResult(query);
    }

    public DBItemInventoryJunction getJunction(Long configId) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_JUNCTIONS);
        hql.append(" where cid=:configId");
        Query<DBItemInventoryJunction> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getSingleResult(query);
    }

    public DBItemInventoryCalendar getCalendar(Long configId) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CALENDARS);
        hql.append(" where cid=:configId");
        Query<DBItemInventoryCalendar> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getSingleResult(query);
    }

    public DBItemInventoryWorkflowOrder getWorkflowOrder(Long configId) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_WORKFLOW_ORDERS);
        hql.append(" where cid=:configId");
        Query<DBItemInventoryWorkflowOrder> query = getSession().createQuery(hql.toString());
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

        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where id in (");
        hql.append("select cid from ").append(DBLayer.DBITEM_INV_WORKFLOW_JOBS).append(" where cidWorkflow=:configId");
        hql.append(")");
        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        getSession().executeUpdate(query);

        executeDelete(DBLayer.DBITEM_INV_WORKFLOW_JOB_ARGUMENTS, configId, "cidWorkflow");
        executeDelete(DBLayer.DBITEM_INV_WORKFLOW_JOB_NODE_ARGUMENTS, configId, "cidWorkflow");
        executeDelete(DBLayer.DBITEM_INV_WORKFLOW_JOB_NODES, configId, "cidWorkflow");
        result.setWorkflowJobs(executeDelete(DBLayer.DBITEM_INV_WORKFLOW_JOBS, configId, "cidWorkflow"));

        hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where id in (");
        hql.append("select cid from ").append(DBLayer.DBITEM_INV_WORKFLOW_ORDERS).append(" where cidWorkflow=:configId");
        hql.append(")");
        query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        getSession().executeUpdate(query);
        executeDelete(DBLayer.DBITEM_INV_WORKFLOW_ORDERS, configId, "cidWorkflow");
        executeDelete(DBLayer.DBITEM_INV_WORKFLOW_ORDER_VARIABLES, configId, "cidWorkflow");

        // TODO delete from Job2Lock

        result.setWorkflowJunctions(executeDelete(DBLayer.DBITEM_INV_WORKFLOW_JUNCTIONS, configId, "cidWorkflow"));
        result.setWorkflows(executeDelete(DBLayer.DBITEM_INV_WORKFLOWS, configId));
        result.setConfigurations(executeDelete(DBLayer.DBITEM_INV_CONFIGURATIONS, configId, "id"));

        return result;
    }

    public InvertoryDeleteResult deleteWorkflowJob(Long configId) throws Exception {
        InvertoryDeleteResult result = new InvertoryDeleteResult();

        executeDelete(DBLayer.DBITEM_INV_WORKFLOW_JOB_ARGUMENTS, configId, "cidJob");
        executeDelete(DBLayer.DBITEM_INV_WORKFLOW_JOB_NODE_ARGUMENTS, configId, "cidJob");
        executeDelete(DBLayer.DBITEM_INV_WORKFLOW_JOB_NODES, configId, "cidJob");
        result.setWorkflowJobs(executeDelete(DBLayer.DBITEM_INV_WORKFLOW_JOBS, configId));
        // TODO delete from Job2Lock
        result.setConfigurations(executeDelete(DBLayer.DBITEM_INV_CONFIGURATIONS, configId, "id"));

        return result;
    }

    public InvertoryDeleteResult deleteJobClass(Long configId) throws Exception {
        InvertoryDeleteResult result = new InvertoryDeleteResult();

        result.setJobClasses(executeDelete(DBLayer.DBITEM_INV_JOB_CLASSES, configId));

        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_INV_WORKFLOW_JOBS);
        hql.append(" set cidJobClass=:newConfigId");
        hql.append(" where cidJobClass=:configId");
        Query<DBItemInventoryWorkflowJob> query = getSession().createQuery(hql.toString());
        query.setParameter("newConfigId", 0L);
        query.setParameter("configId", configId);
        result.setWorkflowJobs(getSession().executeUpdate(query));

        result.setConfigurations(executeDelete(DBLayer.DBITEM_INV_CONFIGURATIONS, configId, "id"));

        return result;
    }

    public InvertoryDeleteResult deleteAgentCluster(Long configId) throws Exception {
        InvertoryDeleteResult result = new InvertoryDeleteResult();

        result.setAgentClusterMembers(executeDelete(DBLayer.DBITEM_INV_AGENT_CLUSTER_MEMBERS, configId, "cidAgentCluster"));
        result.setAgentClusters(executeDelete(DBLayer.DBITEM_INV_AGENT_CLUSTERS, configId));

        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_INV_WORKFLOW_JOBS);
        hql.append(" set cidAgentCluster=:newConfigId");
        hql.append(" where cidAgentCluster=:configId");
        Query<DBItemInventoryWorkflowJob> query = getSession().createQuery(hql.toString());
        query.setParameter("newConfigId", 0L);
        query.setParameter("configId", configId);
        result.setWorkflowJobs(getSession().executeUpdate(query));

        result.setConfigurations(executeDelete(DBLayer.DBITEM_INV_CONFIGURATIONS, configId, "id"));

        return result;
    }

    public InvertoryDeleteResult deleteLock(Long configId) throws Exception {
        InvertoryDeleteResult result = new InvertoryDeleteResult();

        result.setLocks(executeDelete(DBLayer.DBITEM_INV_LOCKS, configId));
        // TODO delete from Job2Lock
        result.setConfigurations(executeDelete(DBLayer.DBITEM_INV_CONFIGURATIONS, configId, "id"));

        return result;
    }

    public InvertoryDeleteResult deleteJunction(Long configId) throws Exception {
        InvertoryDeleteResult result = new InvertoryDeleteResult();

        result.setJunctions(executeDelete(DBLayer.DBITEM_INV_JUNCTIONS, configId));
        result.setWorkflowJunctions(executeDelete(DBLayer.DBITEM_INV_WORKFLOW_JUNCTIONS, configId, "cidJunction"));
        // TODO delete from workflow ??
        result.setConfigurations(executeDelete(DBLayer.DBITEM_INV_CONFIGURATIONS, configId, "id"));

        return result;
    }

    public InvertoryDeleteResult deleteCalendar(Long configId) throws Exception {
        InvertoryDeleteResult result = new InvertoryDeleteResult();

        result.setCalendars(executeDelete(DBLayer.DBITEM_INV_CALENDARS, configId));
        // TODO delete from xxxx ??
        result.setConfigurations(executeDelete(DBLayer.DBITEM_INV_CONFIGURATIONS, configId, "id"));
        return result;
    }

    public InvertoryDeleteResult deleteWorkflowOrder(Long configId) throws Exception {
        InvertoryDeleteResult result = new InvertoryDeleteResult();

        result.setWorkflowOrders(executeDelete(DBLayer.DBITEM_INV_WORKFLOW_ORDERS, configId));
        executeDelete(DBLayer.DBITEM_INV_WORKFLOW_ORDER_VARIABLES, configId, "cidWorkflow");
        // TODO delete from xxxx ??
        result.setConfigurations(executeDelete(DBLayer.DBITEM_INV_CONFIGURATIONS, configId, "id"));
        return result;
    }

    public InvertoryDeleteResult deleteConfiguration(Long configId) throws Exception {
        InvertoryDeleteResult result = new InvertoryDeleteResult();
        result.setConfigurations(executeDelete(DBLayer.DBITEM_INV_CONFIGURATIONS, configId, "id"));

        return result;
    }

    private int executeDelete(String dbItem, Long configId) throws Exception {
        return executeDelete(dbItem, configId, null);
    }

    private int executeDelete(final String dbItem, final Long configId, String entity) throws Exception {
        if (SOSString.isEmpty(entity)) {
            entity = "cid";
        }
        StringBuilder hql = new StringBuilder("delete from ").append(dbItem);
        hql.append(" where ").append(entity).append("=:configId");
        Query<?> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().executeUpdate(query);
    }

    public void deleteAll() throws Exception {
        // TODO all inventory tables
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_AGENT_CLUSTER_MEMBERS);
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_AGENT_CLUSTERS);
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_CALENDARS);
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_CONFIGURATIONS);
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_JOB_CLASSES);
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_JUNCTIONS);
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_LOCKS);
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_WORKFLOW_JOB_ARGUMENTS);
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_WORKFLOW_JOB_NODE_ARGUMENTS);
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_WORKFLOW_JOB_NODES);
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_WORKFLOW_JOBS);
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_WORKFLOW_JUNCTIONS);
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_WORKFLOW_ORDERS);
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_WORKFLOW_ORDER_VARIABLES);
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_WORKFLOWS);
    }

    @SuppressWarnings("unchecked")
    public <T extends Tree> Set<T> getFoldersByFolderAndType(String folder, Set<Long> inventoryTypes, Set<Long> calendarTypes)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            List<String> whereClause = new ArrayList<String>();
            StringBuilder sql = new StringBuilder();
            sql.append("select folder from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            if (folder != null && !folder.isEmpty() && !folder.equals(JocInventory.ROOT_FOLDER)) {
                whereClause.add("(folder = :folder or folder like :likeFolder)");
            }
            if (inventoryTypes != null && !inventoryTypes.isEmpty()) {
                if (inventoryTypes.size() == 1) {
                    whereClause.add("type = :type");
                } else {
                    whereClause.add("type in (:type)");
                }
            }
            if (calendarTypes != null && calendarTypes.size() == 1) {
                whereClause.add("id in (select cid from " + DBLayer.DBITEM_INV_CALENDARS + " where type=:calendarType)");
            }
            if (!whereClause.isEmpty()) {
                sql.append(whereClause.stream().collect(Collectors.joining(" and ", " where ", "")));
            }
            sql.append(" group by folder");
            Query<String> query = getSession().createQuery(sql.toString());
            if (folder != null && !folder.isEmpty() && !folder.equals(JocInventory.ROOT_FOLDER)) {
                query.setParameter("folder", folder);
                query.setParameter("likeFolder", folder + "/%");
            }
            if (inventoryTypes != null && !inventoryTypes.isEmpty()) {
                if (inventoryTypes.size() == 1) {
                    query.setParameter("type", inventoryTypes.iterator().next());
                } else {
                    query.setParameterList("type", inventoryTypes);
                }
            }
            if (calendarTypes != null && calendarTypes.size() == 1) {
                query.setParameter("calendarType", calendarTypes.iterator().next());
            }

            List<String> result = getSession().getResultList(query);
            if (result != null && !result.isEmpty()) {
                return result.stream().map(s -> {
                    T tree = (T) new Tree(); // new JoeTree();
                    tree.setPath(s);
                    return tree;
                }).collect(Collectors.toSet());
            } // else if (folder.equals(JocInventory.ROOT_FOLDER)) {
              // T tree = (T) new Tree();
              // tree.setPath(JocInventory.ROOT_FOLDER);
              // return Arrays.asList(tree).stream().collect(Collectors.toSet());
              // }
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
        private int workflowOrders;
        private int jobClasses;
        private int agentClusters;
        private int agentClusterMembers;
        private int locks;
        private int junctions;
        private int calendars;

        public boolean deleted() {
            return configurations > 0 || workflows > 0 || workflowJunctions > 0 || workflowJobs > 0 || workflowOrders > 0 || jobClasses > 0
                    || agentClusters > 0 || agentClusterMembers > 0 || locks > 0 || junctions > 0 || calendars > 0;
        }

        public int getConfigurations() {
            return configurations;
        }

        public void setConfigurations(int val) {
            configurations = val;
        }

        public int getWorkflows() {
            return workflows;
        }

        public void setWorkflows(int val) {
            workflows = val;
        }

        public int getWorkflowJunctions() {
            return workflowJunctions;
        }

        public void setWorkflowJunctions(int val) {
            workflowJunctions = val;
        }

        public int getWorkflowJobs() {
            return workflowJobs;
        }

        public void setWorkflowJobs(int val) {
            workflowJobs = val;
        }

        public int getJobClasses() {
            return jobClasses;
        }

        public void setJobClasses(int val) {
            jobClasses = val;
        }

        public int getAgentClusters() {
            return agentClusters;
        }

        public void setAgentClusters(int val) {
            agentClusters = val;
        }

        public int getAgentClusterMembers() {
            return agentClusterMembers;
        }

        public void setAgentClusterMembers(int val) {
            agentClusterMembers = val;
        }

        public int getLocks() {
            return locks;
        }

        public void setLocks(int val) {
            locks = val;
        }

        public int getJunctions() {
            return junctions;
        }

        public void setJunctions(int val) {
            junctions = val;
        }

        public int getCalendars() {
            return calendars;
        }

        public void setCalendars(int calendars) {
            this.calendars = calendars;
        }

        public int getWorkflowOrders() {
            return workflowOrders;
        }

        public void setWorkflowOrders(int workflowOrders) {
            this.workflowOrders = workflowOrders;
        }
    }

}
