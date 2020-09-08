package com.sos.joc.db.inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.commons.util.SOSString;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.meta.ConfigurationType;
import com.sos.joc.db.inventory.items.InventoryDeployablesTreeFolderItem;
import com.sos.joc.db.inventory.items.InventoryDeploymentItem;
import com.sos.joc.db.inventory.items.InventoryTreeFolderItem;
import com.sos.joc.db.joc.DBItemJocLock;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.tree.Tree;

public class InventoryDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;

    public InventoryDBLayer(SOSHibernateSession session) {
        super(session);
    }

    public InventoryDeploymentItem getLastDeploymentHistory(Long configId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select new ").append(InventoryDeploymentItem.class.getName());
        hql.append("(");
        hql.append("dh.id as deploymentId,dh.version,dh.operation,dh.deploymentDate,dh.content,dh.path");
        hql.append(",jsi.schedulerId");
        hql.append(") ");
        hql.append("from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dh,");
        hql.append(DBLayer.DBITEM_INV_JS_INSTANCES).append(" jsi ");
        hql.append("where dh.id=");
        hql.append("(");
        hql.append("  select max(dhsub.id) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dhsub ");
        hql.append("  where  dhsub.inventoryConfigurationId=:configId");
        hql.append(") ");
        hql.append("and dh.controllerInstanceId=jsi.id");
        Query<InventoryDeploymentItem> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getSingleResult(query);
    }

    public List<InventoryDeploymentItem> getDeploymentHistory(Long configId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select new ").append(InventoryDeploymentItem.class.getName());
        hql.append("(");
        hql.append("dh.id as deploymentId,dh.version,dh.operation,dh.deploymentDate,dh.content,dh.path");
        hql.append(",jsi.schedulerId");
        hql.append(") ");
        hql.append("from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dh,");
        hql.append(DBLayer.DBITEM_INV_JS_INSTANCES).append(" jsi ");
        hql.append("where dh.inventoryConfigurationId=:configId ");
        hql.append("and dh.controllerInstanceId=jsi.id");
        Query<InventoryDeploymentItem> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getResultList(query);
    }

    public List<InventoryTreeFolderItem> getConfigurationsByFolder(String folder, boolean recursive) throws SOSHibernateException {
        return getConfigurationsByFolder(folder, recursive, null, null);
    }

    public List<InventoryTreeFolderItem> getConfigurationsByFolder(String folder, boolean recursive, Integer configType, Integer calendarType)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select new ").append(InventoryTreeFolderItem.class.getName());
        hql.append("(ic.id, ic.type, ic.name, ic.title, ic.valide, ic.deleted, ic.deployed, count(dh.id)) from ").append(
                DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
        hql.append("left join ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dh ");
        hql.append("on ic.id=dh.inventoryConfigurationId ");
        hql.append("where ");
        if (recursive) {
            hql.append("(ic.folder=:folder or ic.folder like :likeFolder) ");
        } else {
            hql.append("ic.folder=:folder ");
        }
        if (configType != null) {
            hql.append("and ic.type=:configType ");
        }
        if (calendarType != null) {
            hql.append("and ic.id in (select cid from " + DBLayer.DBITEM_INV_CALENDARS + " where type=:calendarType) ");
        }
        hql.append("group by ic.id");

        Query<InventoryTreeFolderItem> query = getSession().createQuery(hql.toString());
        query.setParameter("folder", folder);
        if (recursive) {
            query.setParameter("likeFolder", folder + "/%");
        }
        if (configType != null) {
            query.setParameter("configType", configType);
        }
        if (calendarType != null) {
            query.setParameter("calendarType", calendarType);
        }
        return getSession().getResultList(query);
    }

    public Long getCountConfigurationsByFolder(String folder, boolean recursive) throws SOSHibernateException {
        return getCountConfigurationsByFolder(folder, recursive, null);
    }

    public Long getCountConfigurationsByFolder(String folder, boolean recursive, Integer configType) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select count(id) from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
        hql.append("where type != :folderType ");
        if (recursive) {
            hql.append("and (folder=:folder or folder like :likeFolder) ");
        } else {
            hql.append("and folder=:folder ");
        }
        if (configType != null) {
            hql.append("and type=:configType ");
        }
        Query<Long> query = getSession().createQuery(hql.toString());
        query.setParameter("folderType", ConfigurationType.FOLDER.intValue());
        query.setParameter("folder", folder);
        if (recursive) {
            query.setParameter("likeFolder", folder + "/%");
        }
        if (configType != null) {
            query.setParameter("configType", configType);
        }
        return getSession().getSingleResult(query);
    }

    public List<InventoryDeployablesTreeFolderItem> getConfigurationsWithMaxDeployment(String folder, boolean recursive)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select new ").append(InventoryDeployablesTreeFolderItem.class.getName());
        hql.append("(");
        hql.append("ic.id as configId,ic.path,ic.folder,ic.name,ic.type,ic.valide,ic.deleted,ic.deployed,ic.modified");
        hql.append(",dh.id as deploymentId,dh.version,dh.operation,dh.deploymentDate,dh.path");
        hql.append(",jsi.schedulerId");
        hql.append(") ");
        hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
        hql.append("left join ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dh ");
        hql.append("on ic.id=dh.inventoryConfigurationId ");
        hql.append("and dh.id=(");
        hql.append("select max(dhsub.id) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dhsub where ic.id=dhsub.inventoryConfigurationId");
        hql.append(") ");
        hql.append("left join ").append(DBLayer.DBITEM_INV_JS_INSTANCES).append(" jsi ");
        hql.append("on jsi.id=dh.controllerInstanceId ");
        if (folder != null) {
            if (recursive) {
                hql.append("where (ic.folder=:folder or ic.folder like :likeFolder) ");
            } else {
                hql.append("where ic.folder=:folder ");
            }
        }
        Query<InventoryDeployablesTreeFolderItem> query = getSession().createQuery(hql.toString());
        if (folder != null) {
            query.setParameter("folder", folder);
            if (recursive) {
                if (folder.equals("/")) {
                    query.setParameter("likeFolder", folder + "%");
                } else {
                    query.setParameter("likeFolder", folder + "/%");
                }
            }
        }
        return getSession().getResultList(query);
    }

    public InventoryDeployablesTreeFolderItem getConfigurationWithMaxDeployment(Long configId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select new ").append(InventoryDeployablesTreeFolderItem.class.getName());
        hql.append("(");
        hql.append("ic.id as configId,ic.path,ic.folder,ic.name,ic.type,ic.valide,ic.deleted,ic.deployed,ic.modified");
        hql.append(",dh.id as deploymentId,dh.version,dh.operation,dh.deploymentDate,dh.path");
        hql.append(",jsi.schedulerId");
        hql.append(") ");
        hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
        hql.append("left join ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dh ");
        hql.append("on ic.id=dh.inventoryConfigurationId ");
        hql.append("and dh.id=(");
        hql.append("select max(dhsub.id) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dhsub where ic.id=dhsub.inventoryConfigurationId");
        hql.append(") ");
        hql.append("left join ").append(DBLayer.DBITEM_INV_JS_INSTANCES).append(" jsi ");
        hql.append("on jsi.id=dh.controllerInstanceId ");
        hql.append("where ic.id=:configId ");

        Query<InventoryDeployablesTreeFolderItem> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getSingleResult(query);
    }

    public List<InventoryDeployablesTreeFolderItem> getConfigurationsWithAllDeployments(Long configId) throws SOSHibernateException {
        return getConfigurationsWithAllDeployments(configId, null, null);
    }

    public List<InventoryDeployablesTreeFolderItem> getConfigurationsWithAllDeployments(String folder, Integer type) throws SOSHibernateException {
        return getConfigurationsWithAllDeployments(null, folder, type);
    }

    private List<InventoryDeployablesTreeFolderItem> getConfigurationsWithAllDeployments(Long configId, String folder, Integer type)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select new ").append(InventoryDeployablesTreeFolderItem.class.getName());
        hql.append("(");
        hql.append("ic.id as configId,ic.path,ic.folder,ic.name,ic.type,ic.valide,ic.deleted,ic.deployed,ic.modified");
        hql.append(",dh.id as deploymentId,dh.version,dh.operation,dh.deploymentDate,dh.path");
        hql.append(",jsi.schedulerId");
        hql.append(") ");
        hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
        hql.append("left join ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dh ");
        hql.append("on ic.id=dh.inventoryConfigurationId ");
        hql.append("left join ").append(DBLayer.DBITEM_INV_JS_INSTANCES).append(" jsi ");
        hql.append("on jsi.id=dh.controllerInstanceId ");
        if (configId != null) {
            hql.append("where ic.id=:configId ");
        } else if (folder != null) {
            hql.append("where ic.folder=:folder ");
            if (type != null) {
                hql.append("and ic.type=:type ");
            }
        }
        hql.append("order by ic.id");

        Query<InventoryDeployablesTreeFolderItem> query = getSession().createQuery(hql.toString());
        if (configId != null) {
            query.setParameter("configId", configId);
        } else if (folder != null) {
            query.setParameter("folder", folder);
            if (type != null) {
                query.setParameter("type", type);
            }
        }
        return getSession().getResultList(query);
    }

    public DBItemInventoryConfiguration getConfiguration(Long id) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where id=:id");
        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("id", id);
        return getSession().getSingleResult(query);
    }

    public Object getConfigurationProperty(Long id, String propertyName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select ").append(propertyName).append(" from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where id=:id");
        Query<Object> query = getSession().createQuery(hql.toString());
        query.setParameter("id", id);
        return getSession().getSingleValue(query);
    }

    public List<Object[]> getConfigurationProperties(Set<Long> ids, String propertyNames) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select ").append(propertyNames).append(" from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where id in (:ids)");
        Query<Object[]> query = getSession().createQuery(hql.toString());
        query.setParameterList("ids", ids);
        return getSession().getResultList(query);
    }

    public Object getConfigurationProperty(String path, Integer type, String propertyName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select ").append(propertyName).append(" from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where lower(path)=:path");
        hql.append(" and type=:type");
        Query<Object> query = getSession().createQuery(hql.toString());
        query.setParameter("path", path.toLowerCase());
        query.setParameter("type", type);
        return getSession().getSingleValue(query);
    }

    public Object[] getConfigurationProperties(Long id, String propertyNames) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select ").append(propertyNames).append(" from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where id=:id");
        Query<Object[]> query = getSession().createQuery(hql.toString());
        query.setParameter("id", id);
        return getSession().getSingleResult(query);
    }

    public DBItemInventoryConfiguration getConfiguration(String path, Integer type) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where lower(path)=:path");
        hql.append(" and type=:type");
        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("path", path.toLowerCase());
        query.setParameter("type", type);
        return getSession().getSingleResult(query);
    }

    public DBItemInventoryWorkflowJob getWorkflowJob(Long configId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_WORKFLOW_JOBS);
        hql.append(" where cid=:configId");
        Query<DBItemInventoryWorkflowJob> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getSingleResult(query);
    }

    public List<DBItemInventoryWorkflowJob> getWorkflowJobs(Long workflowConfigId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_WORKFLOW_JOBS);
        hql.append(" where cidWorkflow=:workflowConfigId");
        Query<DBItemInventoryWorkflowJob> query = getSession().createQuery(hql.toString());
        query.setParameter("workflowConfigId", workflowConfigId);
        return getSession().getResultList(query);
    }

    public DBItemInventoryJobClass getJobClass(Long configId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_JOB_CLASSES);
        hql.append(" where cid=:configId");
        Query<DBItemInventoryJobClass> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getSingleResult(query);
    }

    public DBItemInventoryAgentCluster getAgentCluster(Long configId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_AGENT_CLUSTERS);
        hql.append(" where cid=:configId");
        Query<DBItemInventoryAgentCluster> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getSingleResult(query);
    }

    public List<DBItemInventoryAgentClusterMember> getAgentClusterMembers(Long agentClusterId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_AGENT_CLUSTER_MEMBERS);
        hql.append(" where cidAgentCluster=:agentClusterId");
        Query<DBItemInventoryAgentClusterMember> query = getSession().createQuery(hql.toString());
        query.setParameter("agentClusterId", agentClusterId);
        return getSession().getResultList(query);
    }

    public DBItemInventoryLock getLock(Long configId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_LOCKS);
        hql.append(" where cid=:configId");
        Query<DBItemInventoryLock> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getSingleResult(query);
    }

    public DBItemInventoryJunction getJunction(Long configId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_JUNCTIONS);
        hql.append(" where cid=:configId");
        Query<DBItemInventoryJunction> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getSingleResult(query);
    }

    public DBItemInventoryCalendar getCalendar(Long configId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CALENDARS);
        hql.append(" where cid=:configId");
        Query<DBItemInventoryCalendar> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getSingleResult(query);
    }

    public DBItemInventoryWorkflowOrder getWorkflowOrder(Long configId) throws SOSHibernateException {
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

    public InvertoryDeleteResult deleteWorkflow(Long configId) throws SOSHibernateException {
        InvertoryDeleteResult result = new InvertoryDeleteResult();

        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_INV_WORKFLOW_JOB_NODE_ARGUMENTS).append(" ");
        hql.append("where workflowJobNodeId in (");
        hql.append("   select id from ").append(DBLayer.DBITEM_INV_WORKFLOW_JOB_NODES);
        hql.append("   where workflowJobId in (");
        hql.append("       select id from ").append(DBLayer.DBITEM_INV_WORKFLOW_JOBS).append(" where cidWorkflow=:configId");
        hql.append("   )");
        hql.append(")");
        Query<?> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        getSession().executeUpdate(query);

        hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_INV_WORKFLOW_JOB_NODES).append(" ");
        hql.append("where workflowJobId in (");
        hql.append("  select id from ").append(DBLayer.DBITEM_INV_WORKFLOW_JOBS).append(" where cidWorkflow=:configId");
        hql.append(")");
        query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        getSession().executeUpdate(query);

        hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_INV_WORKFLOW_JOB_ARGUMENTS).append(" ");
        hql.append("where workflowJobId in (");
        hql.append("  select id from ").append(DBLayer.DBITEM_INV_WORKFLOW_JOBS).append(" where cidWorkflow=:configId");
        hql.append(")");
        query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        getSession().executeUpdate(query);
        result.setWorkflowJobs(executeDelete(DBLayer.DBITEM_INV_WORKFLOW_JOBS, configId, "cidWorkflow"));

        // TODO delete from Job2Lock

        hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_INV_WORKFLOW_ORDER_VARIABLES).append(" ");
        hql.append("where cidWorkflowOrder in (");
        hql.append("  select cid from ").append(DBLayer.DBITEM_INV_WORKFLOW_ORDERS).append(" where cidWorkflow=:configId");
        hql.append(")");
        query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        getSession().executeUpdate(query);

        result.setWorkflowOrders(executeDelete(DBLayer.DBITEM_INV_WORKFLOW_ORDERS, configId, "cidWorkflow"));

        result.setWorkflowJunctions(executeDelete(DBLayer.DBITEM_INV_WORKFLOW_JUNCTIONS, configId, "cidWorkflow"));
        result.setConfigurations(executeDelete(DBLayer.DBITEM_INV_CONFIGURATIONS, configId, "id"));

        return result;
    }

    public InvertoryDeleteResult deleteWorkflowJob(Long configId) throws SOSHibernateException {
        InvertoryDeleteResult result = new InvertoryDeleteResult();

        return result;
    }

    public InvertoryDeleteResult deleteJobClass(Long configId) throws SOSHibernateException {
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

    public InvertoryDeleteResult deleteAgentCluster(Long configId) throws SOSHibernateException {
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

    public InvertoryDeleteResult deleteLock(Long configId) throws SOSHibernateException {
        InvertoryDeleteResult result = new InvertoryDeleteResult();

        result.setLocks(executeDelete(DBLayer.DBITEM_INV_LOCKS, configId));
        // TODO delete from Job2Lock
        result.setConfigurations(executeDelete(DBLayer.DBITEM_INV_CONFIGURATIONS, configId, "id"));

        return result;
    }

    public InvertoryDeleteResult deleteJunction(Long configId) throws SOSHibernateException {
        InvertoryDeleteResult result = new InvertoryDeleteResult();

        result.setJunctions(executeDelete(DBLayer.DBITEM_INV_JUNCTIONS, configId));
        result.setWorkflowJunctions(executeDelete(DBLayer.DBITEM_INV_WORKFLOW_JUNCTIONS, configId, "cidJunction"));
        // TODO delete from workflow ??
        result.setConfigurations(executeDelete(DBLayer.DBITEM_INV_CONFIGURATIONS, configId, "id"));

        return result;
    }

    public InvertoryDeleteResult deleteCalendar(Long configId) throws SOSHibernateException {
        InvertoryDeleteResult result = new InvertoryDeleteResult();

        result.setCalendars(executeDelete(DBLayer.DBITEM_INV_CALENDARS, configId));
        // TODO delete from xxxx ??
        result.setConfigurations(executeDelete(DBLayer.DBITEM_INV_CONFIGURATIONS, configId, "id"));
        return result;
    }

    public InvertoryDeleteResult deleteWorkflowOrder(Long configId) throws SOSHibernateException {
        InvertoryDeleteResult result = new InvertoryDeleteResult();

        executeDelete(DBLayer.DBITEM_INV_WORKFLOW_ORDER_VARIABLES, configId, "cidWorkflowOrder");
        result.setWorkflowOrders(executeDelete(DBLayer.DBITEM_INV_WORKFLOW_ORDERS, configId));

        // TODO delete from xxxx ??
        result.setConfigurations(executeDelete(DBLayer.DBITEM_INV_CONFIGURATIONS, configId, "id"));
        return result;
    }

    public InvertoryDeleteResult deleteConfiguration(Long configId) throws SOSHibernateException {
        InvertoryDeleteResult result = new InvertoryDeleteResult();
        result.setConfigurations(executeDelete(DBLayer.DBITEM_INV_CONFIGURATIONS, configId, "id"));

        return result;
    }

    public void deleteConfigurations(Set<Long> ids) throws SOSHibernateException {
        executeDelete(DBLayer.DBITEM_INV_CONFIGURATIONS, ids, "id");
    }

    private int executeDelete(String dbItem, Long configId) throws SOSHibernateException {
        return executeDelete(dbItem, configId, null);
    }

    private int executeDelete(final String dbItem, final Long configId, String entity) throws SOSHibernateException {
        if (SOSString.isEmpty(entity)) {
            entity = "cid";
        }
        StringBuilder hql = new StringBuilder("delete from ").append(dbItem);
        hql.append(" where ").append(entity).append("=:configId");
        Query<?> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().executeUpdate(query);
    }

    private int executeDelete(final String dbItem, final Set<Long> ids, String entity) throws SOSHibernateException {
        if (SOSString.isEmpty(entity)) {
            entity = "cid";
        }
        StringBuilder hql = new StringBuilder("delete from ").append(dbItem);
        hql.append(" where ").append(entity).append(" in (:ids)");
        Query<?> query = getSession().createQuery(hql.toString());
        query.setParameterList("ids", ids);
        return getSession().executeUpdate(query);
    }

    public int resetConfigurationDraft(final Long configId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
        hql.append("set deployed=false");
        hql.append(",content=null ");
        hql.append("where id=:configId ");
        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().executeUpdate(query);
    }

    public int markConfigurationAsDeleted(final Long configId, boolean deleted) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
        hql.append("set modified=:modified ");
        hql.append(",deleted=:deleted ");
        hql.append("where id=:configId");
        Query<?> query = getSession().createQuery(hql.toString());
        query.setParameter("modified", new Date());
        query.setParameter("configId", configId);
        query.setParameter("deleted", deleted);
        return getSession().executeUpdate(query);
    }

    public int markFolderAsDeleted(final String folder, boolean deleted) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
        hql.append("set modified=:modified ");
        hql.append(",deleted=:deleted ");
        hql.append("where folder=:folder ");
        hql.append("and type=:type");
        Query<?> query = getSession().createQuery(hql.toString());
        query.setParameter("modified", new Date());
        query.setParameter("folder", folder);
        query.setParameter("deleted", deleted);
        query.setParameter("type", ConfigurationType.FOLDER.intValue());
        return getSession().executeUpdate(query);
    }

    public void deleteAll() throws SOSHibernateException {
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
    }

    @SuppressWarnings("unchecked")
    public <T extends Tree> Set<T> getFoldersByFolderAndType(String folder, Set<Integer> inventoryTypes, Set<Integer> calendarTypes) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            List<String> whereClause = new ArrayList<String>();
            StringBuilder sql = new StringBuilder();
            sql.append("select folder,deleted from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
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
            sql.append(" group by folder, deleted");
            Query<Object[]> query = getSession().createQuery(sql.toString());
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

            List<Object[]> result = getSession().getResultList(query);
            if (result != null && !result.isEmpty()) {
                return result.stream().map(s -> {
                    T tree = (T) new Tree(); // new JoeTree();
                    tree.setPath((String) s[0]);
                    tree.setDeleted((Boolean) s[1]);
                    return tree;
                }).collect(Collectors.toSet());
            } else if (folder.equals(JocInventory.ROOT_FOLDER)) {
                T tree = (T) new Tree();
                tree.setPath(JocInventory.ROOT_FOLDER);
                tree.setDeleted(false);
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
