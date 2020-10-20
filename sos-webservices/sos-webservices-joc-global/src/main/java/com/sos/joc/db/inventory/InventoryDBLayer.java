package com.sos.joc.db.inventory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.commons.util.SOSString;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.items.InventoryDeployablesTreeFolderItem;
import com.sos.joc.db.inventory.items.InventoryDeploymentItem;
import com.sos.joc.db.inventory.items.InventoryReleaseItem;
import com.sos.joc.db.inventory.items.InventoryTreeFolderItem;
import com.sos.joc.db.joc.DBItemJocLock;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.tree.Tree;

public class InventoryDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;

    public InventoryDBLayer(SOSHibernateSession session) {
        super(session);
    }

    public InventoryDeploymentItem getLastDeploymentHistory(Long configId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select new ").append(InventoryDeploymentItem.class.getName());
        hql.append("(");
        hql.append("id as deploymentId,version,operation,deploymentDate,content,path,controllerId");
        hql.append(") ");
        hql.append("from ").append(DBLayer.DBITEM_DEP_HISTORY);
        hql.append(" where id=");
        hql.append("(");
        hql.append("  select max(dhsub.id) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dhsub ");
        hql.append("  where dhsub.inventoryConfigurationId=:configId");
        hql.append(") ");
        Query<InventoryDeploymentItem> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getSingleResult(query);
    }

    public List<InventoryDeploymentItem> getDeploymentHistory(Long configId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select new ").append(InventoryDeploymentItem.class.getName());
        hql.append("(");
        hql.append("id as deploymentId,version,operation,deploymentDate,path,controllerId");
        hql.append(") ");
        hql.append("from ").append(DBLayer.DBITEM_DEP_HISTORY);
        hql.append(" where inventoryConfigurationId=:configId ");
        hql.append(" order by id desc ");
        Query<InventoryDeploymentItem> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getResultList(query);
    }

    public List<InventoryReleaseItem> getReleasedConfigurations(Long configId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select new ").append(InventoryReleaseItem.class.getName());
        hql.append("(id, modified, path, controllerId)");
        hql.append(" from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
        hql.append(" where cid=:configId");
        hql.append(" order by modified desc");
        Query<InventoryReleaseItem> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getResultList(query);
    }

    public InventoryReleaseItem getLastReleasedConfiguration(Long configId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select new ").append(InventoryReleaseItem.class.getName());
        hql.append("(id, modified, path, content, controllerId)");
        hql.append(" from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
        hql.append(" where cid=:configId");
        hql.append(" order by modified desc");
        Query<InventoryReleaseItem> query = getSession().createQuery(hql.toString());
        query.setMaxResults(1);
        query.setParameter("configId", configId);
        return getSession().getSingleResult(query);
    }

    public <T> List<T> getReleasedConfigurationProperty(Long configId, String propertyName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select ").append(propertyName).append(" from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
        hql.append(" where cid=:configId");
        hql.append(" order by modified desc");
        Query<T> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getResultList(query);
    }

    public DBItemInventoryReleasedConfiguration getReleasedConfiguration(String path, Integer type) throws SOSHibernateException {
        boolean isCalendar = JocInventory.isCalendar(type);
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
        hql.append(" where lower(path)=:path");
        if (isCalendar) {
            hql.append(" and type in (:types)");
        } else {
            hql.append(" and type=:type");
        }
        Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("path", path.toLowerCase());
        if (isCalendar) {
            query.setParameterList("types", JocInventory.getCalendarTypes());
        } else {
            query.setParameter("type", type);
        }
        return getSession().getSingleResult(query);
    }
    
    public List<String> getDeletedFolders() throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select path from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where deleted = 1");
        hql.append(" and type = :type ");
        Query<String> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.FOLDER.intValue());
        return getSession().getResultList(query);
    }

//    public Set<DBItemInventoryConfiguration> getDeletedConfigurationsWithDeletedFolderContent(Collection<Integer> types, Collection<String> deletedFolders)
//            throws SOSHibernateException {
//        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
//        hql.append(" where deleted = 1");
//        if (types != null && !types.isEmpty()) {
//            hql.append(" and type in (:types) ");
//        }
//        if (deletedFolders != null && !deletedFolders.isEmpty()) {
//            List<String> clause = new ArrayList<>();
//            for (String folder : deletedFolders) {
//                clause.add("(folder = '" + folder + "' or folder like '"+ folder + "/%')");
//            }
//            hql.append(clause.stream().collect(Collectors.joining(" or ", " or (", ")")));
//        }
//        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
//        if (types != null && !types.isEmpty()) {
//            query.setParameterList("types", types);
//        }
//        List<DBItemInventoryConfiguration> result = getSession().getResultList(query);
//        if (result != null) {
//            return result.stream().collect(Collectors.toSet()); 
//        }
//        return Collections.emptySet();
//    }
    
    public List<DBItemInventoryConfiguration> getDeletedConfigurations(Collection<Integer> types, String folder, boolean recursive,
            Collection<String> deletedFolders) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where deleted = 1");
        if (types != null && !types.isEmpty()) {
            hql.append(" and type in (:types) ");
        }
        if (deletedFolders != null && !deletedFolders.isEmpty()) {
            List<String> clause = new ArrayList<>();
            for (String deletedFolder : deletedFolders) {
                clause.add("folder not like '" + deletedFolder + "/%'");
            }
            hql.append(clause.stream().collect(Collectors.joining(" and ", " and (", ")")));
        }
        if (folder != null && !folder.isEmpty()) {
            if (recursive) {
                hql.append(" and (folder = :folder or folder like :likeFolder)");
            } else {
                hql.append(" and folder = :folder");
            }
        }
        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        if (types != null && !types.isEmpty()) {
            query.setParameterList("types", types);
        }
        if (folder != null && !folder.isEmpty()) {
            query.setParameter("folder", folder);
            if (recursive) {
                query.setParameter("likeFolder", (folder + "/%").replaceAll("//+", "/"));
            }
        }
        return getSession().getResultList(query);
    }

    public Set<Long> getNotDeletedConfigurations(Collection<Integer> types, String folder, boolean recursive, Collection<String> deletedFolders)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select id from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where deleted = 0");
        if (types != null && !types.isEmpty()) {
            hql.append(" and type in (:types) ");
        }
        if (deletedFolders != null && !deletedFolders.isEmpty()) {
            List<String> clause = new ArrayList<>();
            for (String deleteFolder : deletedFolders) {
                clause.add("(folder != '" + deleteFolder + "' and folder not like '" + deleteFolder + "/%')");
            }
            hql.append(clause.stream().collect(Collectors.joining(" and ", " and (", ")")));
        }
        if (folder != null && !folder.isEmpty()) {
            if (recursive) {
                hql.append(" and (folder = :folder or folder like :likeFolder)");
            } else {
                hql.append(" and folder = :folder");
            }
        }
        Query<Long> query = getSession().createQuery(hql.toString());
        if (types != null && !types.isEmpty()) {
            query.setParameterList("types", types);
        }
        if (folder != null && !folder.isEmpty()) {
            query.setParameter("folder", folder);
            if (recursive) {
                query.setParameter("likeFolder", (folder + "/%").replaceAll("//+", "/"));
            }
        }
        List<Long> result = getSession().getResultList(query);
        if (result != null) {
            return result.stream().collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    public List<InventoryTreeFolderItem> getConfigurationsByFolder(String folder, boolean recursive) throws SOSHibernateException {
        return getConfigurationsByFolder(folder, recursive, null, false);
    }

    public List<InventoryTreeFolderItem> getConfigurationsByFolder(String folder, boolean recursive, Collection<Integer> configTypes,
            Boolean onlyValidObjects) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select new ").append(InventoryTreeFolderItem.class.getName());
        hql.append("(ic, count(dh.id)) from ").append(
                DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
        hql.append("left join ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dh ");
        hql.append("on ic.id=dh.inventoryConfigurationId ");
        hql.append("where ");
        if (recursive) {
            if (!"/".equals(folder)) {
                hql.append("(ic.folder=:folder or ic.folder like :likeFolder) ");
            } else {
                hql.append("1=1 ");
            }
        } else {
            hql.append("ic.folder=:folder ");
        }
        if (onlyValidObjects == Boolean.TRUE) {
            hql.append("and valid = 1 ");
        }
        if (configTypes != null && !configTypes.isEmpty()) {
            hql.append("and ic.type in (:configTypes) ");
        }
        hql.append("group by ic.id");

        Query<InventoryTreeFolderItem> query = getSession().createQuery(hql.toString());
        if (recursive) {
            if (!"/".equals(folder)) {
                query.setParameter("folder", folder);
                query.setParameter("likeFolder", folder + "/%");
            }
        } else {
            query.setParameter("folder", folder);
        }
        if (configTypes != null && !configTypes.isEmpty()) {
            query.setParameterList("configTypes", configTypes);
        }
        return getSession().getResultList(query);
    }

    public Long getCountConfigurationsByFolder(String folder, boolean recursive) throws SOSHibernateException {
        return getCountConfigurationsByFolder(folder, recursive, null);
    }

    public Long getCountConfigurationsByFolder(String folder, boolean recursive, Integer configType) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select count(id) from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
        if (recursive) {
            hql.append("where (folder=:folder or folder like :likeFolder) ");
        } else {
            hql.append("where folder=:folder ");
        }
        if (configType != null) {
            hql.append("and type=:configType ");
        }
        Query<Long> query = getSession().createQuery(hql.toString());
        query.setParameter("folder", folder);
        if (recursive) {
            query.setParameter("likeFolder", folder + "/%");
        }
        if (configType != null) {
            query.setParameter("configType", configType);
        }
        return getSession().getSingleResult(query);
    }

    public List<InventoryDeployablesTreeFolderItem> getConfigurationsWithMaxDeployment(Collection<Long> configIds) throws SOSHibernateException {
        if (configIds != null && !configIds.isEmpty()) {
            StringBuilder hql = new StringBuilder("select new ").append(InventoryDeployablesTreeFolderItem.class.getName());
            hql.append("(");
            hql.append("ic,dh.id as deploymentId,dh.version,dh.operation,dh.deploymentDate,dh.path,dh.controllerId");
            hql.append(") ");
            hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
            hql.append("left join ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dh ");
            hql.append("on ic.id=dh.inventoryConfigurationId ");
            hql.append("and dh.id=(");
            hql.append("select max(dhsub.id) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dhsub where ic.id=dhsub.inventoryConfigurationId");
            hql.append(") ");
            hql.append("where ic.id in (:configIds) ");

            Query<InventoryDeployablesTreeFolderItem> query = getSession().createQuery(hql.toString());
            if (configIds != null && !configIds.isEmpty()) {
                query.setParameterList("configIds", configIds);
            }
            return getSession().getResultList(query);
        } else {
            return Collections.emptyList();
        }
    }

    public InventoryDeployablesTreeFolderItem getConfigurationWithMaxDeployment(Long configId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select new ").append(InventoryDeployablesTreeFolderItem.class.getName());
        hql.append("(");
        hql.append("ic,dh.id as deploymentId,dh.version,dh.operation,dh.deploymentDate,dh.path,dh.controllerId");
        hql.append(") ");
        hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
        hql.append("left join ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dh ");
        hql.append("on ic.id=dh.inventoryConfigurationId ");
        hql.append("and dh.id=(");
        hql.append("select max(dhsub.id) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dhsub where ic.id=dhsub.inventoryConfigurationId");
        hql.append(") ");
        hql.append("where ic.id=:configId ");

        Query<InventoryDeployablesTreeFolderItem> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getSingleResult(query);
    }

    public Map<DBItemInventoryConfiguration, Set<InventoryDeploymentItem>> getConfigurationsWithAllDeployments(Collection<Long> configIds)
            throws SOSHibernateException {
        if (configIds != null && !configIds.isEmpty()) {
            StringBuilder hql = new StringBuilder("select new ").append(InventoryDeployablesTreeFolderItem.class.getName());
            hql.append("(");
            hql.append("ic");
            hql.append(",dh.id as deploymentId,dh.version,dh.operation,dh.deploymentDate,dh.path,dh.controllerId");
            hql.append(") ");
            hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
            hql.append("left join ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dh ");
            hql.append("on ic.id=dh.inventoryConfigurationId ");
            hql.append("where ic.id in (:configIds) ");

            Query<InventoryDeployablesTreeFolderItem> query = getSession().createQuery(hql.toString());
            if (configIds != null && !configIds.isEmpty()) {
                query.setParameterList("configIds", configIds);
            }
            List<InventoryDeployablesTreeFolderItem> result = getSession().getResultList(query);
            if (result != null && !result.isEmpty()) {
                Comparator<InventoryDeploymentItem> comp = Comparator.nullsFirst(Comparator.comparing(InventoryDeploymentItem::getDeploymentDate)
                        .reversed());
                return result.stream().collect(Collectors.groupingBy(InventoryDeployablesTreeFolderItem::getConfiguration, Collectors.mapping(
                        InventoryDeployablesTreeFolderItem::getDeployment, Collectors.toCollection(() -> new TreeSet<>(comp)))));
            }
        }
        return Collections.emptyMap();
    }

    public DBItemInventoryConfiguration getConfiguration(Long id) throws SOSHibernateException {
        return getSession().get(DBItemInventoryConfiguration.class, id);
    }

    public <T> T getConfigurationProperty(Long id, String propertyName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select ").append(propertyName).append(" from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where id=:id");
        Query<T> query = getSession().createQuery(hql.toString());
        query.setParameter("id", id);
        return getSession().getSingleValue(query);
    }

    public <T> T getConfigurationProperty(String path, Integer type, String propertyName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select ").append(propertyName).append(" from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where lower(path)=:path");
        hql.append(" and type=:type");
        Query<T> query = getSession().createQuery(hql.toString());
        query.setParameter("path", path.toLowerCase());
        query.setParameter("type", type);
        return getSession().getSingleValue(query);
    }

    public DBItemInventoryConfiguration getConfiguration(String path, Integer type) throws SOSHibernateException {
        boolean isCalendar = JocInventory.isCalendar(type);
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where lower(path)=:path");
        if (isCalendar) {
            hql.append(" and type in (:types)");
        } else {
            hql.append(" and type=:type");
        }
        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("path", path.toLowerCase());
        if (isCalendar) {
            query.setParameterList("types", JocInventory.getCalendarTypes());
        } else {
            query.setParameter("type", type);
        }
        return getSession().getSingleResult(query);
    }

    public DBItemInventoryConfiguration getCalendar(String path) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where lower(path)=:path");
        hql.append(" and type in (:types)");
        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("path", path.toLowerCase());
        query.setParameterList("types", JocInventory.getCalendarTypes());
        return getSession().getSingleResult(query);
    }

    public List<DBItemInventoryConfiguration> getCalendars(Stream<String> pathsStream) throws SOSHibernateException {
        Set<String> paths = pathsStream.map(String::toLowerCase).collect(Collectors.toSet());
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where type in (:types)");
        if (!paths.isEmpty()) {
            hql.append(" and lower(path) in (:paths)");
        }
        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        if (!paths.isEmpty()) {
            query.setParameterList("paths", paths);
        }
        query.setParameterList("types", JocInventory.getCalendarTypes());
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryConfiguration> getConfigurations(Stream<String> pathsStream, Collection<Integer> types, Boolean onlyValidObjects)
            throws SOSHibernateException {
        Set<String> paths = pathsStream.map(String::toLowerCase).collect(Collectors.toSet());
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        List<String> clause = new ArrayList<>();
        if (!paths.isEmpty()) {
            clause.add("lower(path) in (:paths)");
        }
        if (types != null && !types.isEmpty()) {
            clause.add("type in (:types)");
        }
        if (onlyValidObjects == Boolean.TRUE) {
            clause.add("valid = 1");
        }
        hql.append(clause.stream().collect(Collectors.joining(" and ", " where ", "")));
        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        if (!paths.isEmpty()) {
            query.setParameterList("paths", paths);
        }
        if (types != null && !types.isEmpty()) {
            query.setParameterList("types", types);
        }
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryConfiguration> getConfigurations(Collection<Long> ids) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        if (ids != null && !ids.isEmpty()) {
            hql.append(" where id in (:ids)");
        }
        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        if (ids != null && !ids.isEmpty()) {
            query.setParameterList("ids", ids);
        }
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryConfiguration> getFolderContent(String folder, boolean recursive) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        if (recursive) {
            if (!"/".equals(folder)) {
                hql.append(" where folder=:folder or folder like :likeFolder");
            }
        } else {
            hql.append(" where folder=:folder");
        }
        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        if (recursive) {
            if (!"/".equals(folder)) {
                query.setParameter("folder", folder);
                query.setParameter("likeFolder", folder + "/%");
            }
        } else {
            query.setParameter("folder", folder);
        }
        return getSession().getResultList(query);
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

    public InventoryDeleteResult deleteWorkflow(Long configId) throws SOSHibernateException {
        InventoryDeleteResult result = new InventoryDeleteResult();

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

    public InventoryDeleteResult deleteWorkflowJob(Long configId) throws SOSHibernateException {
        InventoryDeleteResult result = new InventoryDeleteResult();

        return result;
    }

    public InventoryDeleteResult deleteJobClass(Long configId) throws SOSHibernateException {
        InventoryDeleteResult result = new InventoryDeleteResult();

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

    public InventoryDeleteResult deleteAgentCluster(Long configId) throws SOSHibernateException {
        InventoryDeleteResult result = new InventoryDeleteResult();

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

    public InventoryDeleteResult deleteLock(Long configId) throws SOSHibernateException {
        InventoryDeleteResult result = new InventoryDeleteResult();

        result.setLocks(executeDelete(DBLayer.DBITEM_INV_LOCKS, configId));
        // TODO delete from Job2Lock
        result.setConfigurations(executeDelete(DBLayer.DBITEM_INV_CONFIGURATIONS, configId, "id"));

        return result;
    }

    public InventoryDeleteResult deleteJunction(Long configId) throws SOSHibernateException {
        InventoryDeleteResult result = new InventoryDeleteResult();

        result.setJunctions(executeDelete(DBLayer.DBITEM_INV_JUNCTIONS, configId));
        result.setWorkflowJunctions(executeDelete(DBLayer.DBITEM_INV_WORKFLOW_JUNCTIONS, configId, "cidJunction"));
        // TODO delete from workflow ??
        result.setConfigurations(executeDelete(DBLayer.DBITEM_INV_CONFIGURATIONS, configId, "id"));

        return result;
    }

    public InventoryDeleteResult deleteWorkflowOrder(Long configId) throws SOSHibernateException {
        InventoryDeleteResult result = new InventoryDeleteResult();

        executeDelete(DBLayer.DBITEM_INV_WORKFLOW_ORDER_VARIABLES, configId, "cidWorkflowOrder");
        result.setWorkflowOrders(executeDelete(DBLayer.DBITEM_INV_WORKFLOW_ORDERS, configId));

        // TODO delete from xxxx ??
        result.setConfigurations(executeDelete(DBLayer.DBITEM_INV_CONFIGURATIONS, configId, "id"));
        return result;
    }

    public InventoryDeleteResult deleteConfiguration(Long configId) throws SOSHibernateException {
        InventoryDeleteResult result = new InventoryDeleteResult();
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

    public int markConfigurationAsDeleted(final Long configId, boolean deleted) throws SOSHibernateException {
        return markConfigurationsAsDeleted(Arrays.asList(configId), deleted);
    }

    public int markConfigurationsAsDeleted(final Collection<Long> configIds, boolean deleted) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
        hql.append("set modified=:modified ");
        hql.append(",deleted=:deleted ");
        hql.append("where id in (:configIds) ");
        hql.append("and deleted=:notDeleted");
        Query<?> query = getSession().createQuery(hql.toString());
        query.setParameter("modified", new Date());
        query.setParameterList("configIds", configIds);
        query.setParameter("deleted", deleted);
        query.setParameter("notDeleted", !deleted);
        return getSession().executeUpdate(query);
    }

    public int markFoldersAsDeleted(final Collection<String> folders, boolean deleted) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
        hql.append("set modified=:modified ");
        hql.append(",deleted=:deleted ");
        hql.append("where path in (:folders) ");
        hql.append("and type=:type ");
        hql.append("and deleted=:notDeleted");
        Query<?> query = getSession().createQuery(hql.toString());
        query.setParameter("modified", new Date());
        query.setParameterList("folders", folders);
        query.setParameter("deleted", deleted);
        query.setParameter("notDeleted", !deleted);
        query.setParameter("type", ConfigurationType.FOLDER.intValue());
        return getSession().executeUpdate(query);
    }

    public void deleteAll() throws SOSHibernateException {
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
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_WORKFLOW_ORDERS);
        getSession().getSQLExecutor().execute("TRUNCATE TABLE " + DBLayer.TABLE_INV_WORKFLOW_ORDER_VARIABLES);
    }

    public Set<Tree> getFoldersByFolderAndType(String folder, Set<Integer> inventoryTypes, Boolean onlyValidObjects)
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
            if (onlyValidObjects == Boolean.TRUE) {
                whereClause.add("valid = 1");
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

            List<String> result = getSession().getResultList(query);
            if (result != null && !result.isEmpty()) {
                Set<String> folderWithParents = new HashSet<>();
                for (String f : result) {
                    Path p = Paths.get(f);
                    for (int i = 0; i < p.getNameCount(); i++) {
                        folderWithParents.add(("/" + p.subpath(0, i + 1)).replace('\\', '/'));
                    }
                }
                Set<Tree> tree = getFoldersByFolder(folderWithParents);
                Tree root = new Tree();
                root.setPath(JocInventory.ROOT_FOLDER);
                root.setDeleted(false);
                tree.add(root);

                return tree;
            }
            return new HashSet<>();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    private Set<Tree> getFoldersByFolder(Collection<String> folders) throws SOSHibernateException {
        if (folders != null && !folders.isEmpty()) {
            StringBuilder sql = new StringBuilder();
            sql.append("select path, deleted from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            sql.append(" where path in (:folders) and type=:type");
            Query<Object[]> query = getSession().createQuery(sql.toString());
            query.setParameterList("folders", folders);
            query.setParameter("type", ConfigurationType.FOLDER.intValue());
            List<Object[]> result = getSession().getResultList(query);
            if (result != null && !result.isEmpty()) {
                return result.stream().map(s -> {
                    Tree tree = new Tree();
                    tree.setPath((String) s[0]);
                    tree.setDeleted((Boolean) s[1]);
                    return tree;
                }).collect(Collectors.toSet());
            }
        }
        return new HashSet<>();
    }

    public class InventoryDeleteResult {

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
