package com.sos.joc.db.inventory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.JsonConverter;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.items.FolderItem;
import com.sos.joc.db.inventory.items.InventoryDeployablesTreeFolderItem;
import com.sos.joc.db.inventory.items.InventoryDeploymentItem;
import com.sos.joc.db.inventory.items.InventoryTreeFolderItem;
import com.sos.joc.db.joc.DBItemJocLock;
import com.sos.joc.db.search.DBItemSearchWorkflow;
import com.sos.joc.db.search.DBItemSearchWorkflow2DeploymentHistory;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.DeploymentState;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.model.tree.Tree;

public class InventoryDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;

    private String regexpParamPrefixSuffix = "";

    public InventoryDBLayer(SOSHibernateSession session) {
        super(session);
        setRegexpParamPrefixSuffix();
    }

    public InventoryDeploymentItem getLastDeploymentHistory(Long configId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select new ").append(InventoryDeploymentItem.class.getName());
        hql.append("(");
        hql.append("id as deploymentId,commitId,version,operation,deploymentDate,path,controllerId");
        hql.append(") ");
        hql.append("from ").append(DBLayer.DBITEM_DEP_HISTORY);
        hql.append(" where id=");
        hql.append("(");
        hql.append("  select max(dhsub.id) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dhsub ");
        hql.append("  where dhsub.inventoryConfigurationId=:configId");
        hql.append("  and dhsub.state = :state");
        hql.append(") ");
        Query<InventoryDeploymentItem> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        query.setParameter("state", DeploymentState.DEPLOYED.value());
        return getSession().getSingleResult(query);
    }

    public InventoryDeploymentItem getLastDeployedContent(Long configId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select controllerId, max(id) from ").append(DBLayer.DBITEM_DEP_HISTORY);
        hql.append(" where inventoryConfigurationId = :configId");
        hql.append(" and state = :state ");
        hql.append(" group by controllerId");
        Query<Object[]> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        query.setParameter("state", DeploymentState.DEPLOYED.value());
        List<Object[]> result = getSession().getResultList(query);

        if (result != null && !result.isEmpty()) {
            hql = new StringBuilder("select new ").append(InventoryDeploymentItem.class.getName());
            hql.append("(");
            hql.append("id as deploymentId,commitId,version,operation,deploymentDate,invContent,path,controllerId");
            hql.append(") ");
            hql.append("from ").append(DBLayer.DBITEM_DEP_HISTORY);
            hql.append(" where id in (:ids)");
            Query<InventoryDeploymentItem> query2 = getSession().createQuery(hql.toString());
            query2.setParameterList("ids", result.stream().map(item -> (Long) item[1]).collect(Collectors.toSet()));
            List<InventoryDeploymentItem> result2 = getSession().getResultList(query2);

            if (result2 != null && !result2.isEmpty()) {
                Optional<InventoryDeploymentItem> lastItem = result2.stream().filter(item -> OperationType.UPDATE.value().equals(item.getOperation())
                        && item.getContent() != null).max(Comparator.comparingLong(InventoryDeploymentItem::getId));
                if (lastItem.isPresent()) {
                    return lastItem.get();
                }
            }
        }
        return null;
    }

    public List<InventoryDeploymentItem> getDeploymentHistory(Long configId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select new ").append(InventoryDeploymentItem.class.getName());
        hql.append("(");
        hql.append("id as deploymentId,commitId,version,operation,deploymentDate,path,controllerId");
        hql.append(") ");
        hql.append("from ").append(DBLayer.DBITEM_DEP_HISTORY);
        hql.append(" where inventoryConfigurationId = :configId ");
        hql.append(" and state = :state ");
        hql.append(" order by id desc ");
        Query<InventoryDeploymentItem> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        query.setParameter("state", DeploymentState.DEPLOYED.value());
        return getSession().getResultList(query);
    }
    
    public String getDeployedInventoryContent(Long configId, String commitId) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder hql = new StringBuilder("select invContent from ").append(DBLayer.DBITEM_DEP_HISTORY);
            hql.append(" where inventoryConfigurationId = :configId");
            hql.append(" and commitId = :commitId");
            hql.append(" and state = :state");
            Query<String> query = getSession().createQuery(hql.toString());
            query.setParameter("configId", configId);
            query.setParameter("commitId", commitId);
            query.setParameter("state", DeploymentState.DEPLOYED.value());
            query.setMaxResults(1);
            return getSession().getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public DBItemInventoryReleasedConfiguration getReleasedItemByConfigurationId(Long configId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
        hql.append(" where cid=:configId");
        Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getSingleResult(query);
    }

    public Map<Long, List<DBItemInventoryReleasedConfiguration>> getReleasedItemsByConfigurationIds(Collection<Integer> types, String folder,
            boolean recursive, Collection<String> deletedFolders) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" irc ");
        hql.append("where irc.cid in (").append(getNotDeletedConfigurationsHQL(types, folder, recursive, deletedFolders)).append(")");
        Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql.toString());
        if (types != null && !types.isEmpty()) {
            query.setParameterList("types", types);
        }
        if (folder != null && !folder.isEmpty()) {
            query.setParameter("folder", folder);
            if (recursive) {
                query.setParameter("likeFolder", (folder + "/%").replaceAll("//+", "/"));
            }
        }
        List<DBItemInventoryReleasedConfiguration> result = getSession().getResultList(query);
        if (result != null) {
            return result.stream().collect(Collectors.groupingBy(DBItemInventoryReleasedConfiguration::getCid));
        }
        return Collections.emptyMap();
    }

    public int deleteReleasedItemsByConfigurationIds(List<Long> configIds) throws SOSHibernateException {
        if (configIds == null || configIds.isEmpty()) {
            return 0;
        }
        if (configIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            int j = 0;
            for (int i = 0; i < configIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                j += deleteReleasedItemsByConfigurationIds(SOSHibernate.getInClausePartition(i, configIds));
            }
            return j;
        } else {
            StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
            hql.append(" where cid in (:configIds)");
            Query<Integer> query = getSession().createQuery(hql.toString());
            query.setParameter("configIds", configIds);
            return getSession().executeUpdate(query);
        }
    }

    public <T> List<T> getReleasedItemPropertyByConfigurationId(Long configId, String propertyName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select ").append(propertyName).append(" from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
        hql.append(" where cid=:configId");
        hql.append(" order by modified desc");
        Query<T> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getResultList(query);
    }

    public DBItemInventoryReleasedConfiguration getReleasedConfiguration(Long id) throws SOSHibernateException {
        return getSession().get(DBItemInventoryReleasedConfiguration.class, id);
    }

    public DBItemInventoryReleasedConfiguration getReleasedConfiguration(String name, Integer type) throws SOSHibernateException {
        boolean isCalendar = JocInventory.isCalendar(type);
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
        hql.append(" where lower(name)=:name");
        if (isCalendar) {
            hql.append(" and type in (:types)");
        } else {
            hql.append(" and type=:type");
        }
        Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("name", name.toLowerCase());
        query.setMaxResults(1);
        if (isCalendar) {
            query.setParameterList("types", JocInventory.getCalendarTypes());
        } else {
            query.setParameter("type", type);
        }
        return getSession().getSingleResult(query);
    }
    
    public DBItemInventoryReleasedConfiguration getReleasedConfigurationByInvId(Long cid) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
        hql.append(" where cid=:cid");
        Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("cid", cid);
        return getSession().getSingleResult(query);
    }

    public int deleteContraintViolatedReleasedConfigurations(Long id, String name, Integer type) throws SOSHibernateException {
        boolean isCalendar = JocInventory.isCalendar(type);
        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
        hql.append(" where lower(name)=:name");
        if (isCalendar) {
            hql.append(" and type in (:types)");
        } else {
            hql.append(" and type=:type");
        }
        if (id != null) {
            hql.append(" and id != :id");
        }
        Query<?> query = getSession().createQuery(hql.toString());
        query.setParameter("name", name.toLowerCase());
        if (isCalendar) {
            query.setParameterList("types", JocInventory.getCalendarTypes());
        } else {
            query.setParameter("type", type);
        }
        if (id != null) {
            query.setParameter("id", id);
        }
        return getSession().executeUpdate(query);
    }

    public List<String> getDeletedFolders() throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select path from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where deleted = 1");
        hql.append(" and type = :type ");
        Query<String> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.FOLDER.intValue());
        return getSession().getResultList(query);
    }

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
                clause.add("folder != '" + deletedFolder + "' and folder not like '" + deletedFolder + "/%'");
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

    public List<DBItemInventoryConfiguration> getNotDeletedConfigurations(Collection<Integer> types, String folder, boolean recursive,
            Collection<String> deletedFolders) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
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
        List<DBItemInventoryConfiguration> result = getSession().getResultList(query);
        if (result != null) {
            return result;
        }
        return Collections.emptyList();
    }

    private String getNotDeletedConfigurationsHQL(Collection<Integer> types, String folder, boolean recursive, Collection<String> deletedFolders)
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
        return hql.toString();
    }

    public List<InventoryTreeFolderItem> getConfigurationsByFolder(String folder, boolean recursive) throws SOSHibernateException {
        return getConfigurationsByFolder(folder, recursive, null, false);
    }

    public List<InventoryTreeFolderItem> getConfigurationsByFolder(String folder, boolean recursive, Collection<Integer> configTypes,
            Boolean onlyValidObjects) throws SOSHibernateException {
        return getConfigurationsByFolder(folder, recursive, configTypes, onlyValidObjects, false);
    }

    public List<InventoryTreeFolderItem> getConfigurationsByFolder(String folder, boolean recursive, Collection<Integer> configTypes,
            Boolean onlyValidObjects, boolean forTrash) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select ");
        hql.append("ic.id as id");
        hql.append(",ic.name as name");
        hql.append(",ic.title as title");
        hql.append(",ic.valid as valid");
        hql.append(",ic.type as type");
        hql.append(",ic.path as path");
        if (forTrash) {
            hql.append(",false as deleted ");// TODO?
            hql.append(",false as deployed ");
            hql.append(",false as released ");
            hql.append(",0 as countDeployed ");
            hql.append(",0 as countReleased ");
            hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATION_TRASH).append(" ic ");
        } else {
            hql.append(",ic.deleted as deleted ");
            hql.append(",ic.deployed as deployed ");
            hql.append(",ic.released as released ");
            hql.append(",count(dh.id) as countDeployed ");
            hql.append(",count(irc.id) as countReleased  ");
            hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
            hql.append("left join ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dh ");
            hql.append("on ic.id=dh.inventoryConfigurationId ");
            hql.append("left join ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" irc ");
            hql.append("on ic.id=irc.cid ");
        }
        List<String> where = new ArrayList<>();
        if (recursive) {
            if (!"/".equals(folder)) {
                where.add("(ic.folder=:folder or ic.folder like :likeFolder)");
            }
        } else {
            where.add("ic.folder=:folder");
        }
        if (onlyValidObjects == Boolean.TRUE) {
            where.add("ic.valid = 1");
        }
        if (configTypes != null && !configTypes.isEmpty()) {
            where.add("ic.type in (:configTypes)");
        }
        if (where.size() > 0) {
            hql.append("where ").append(String.join(" and ", where)).append(" ");
        }
        if (!forTrash) {
            hql.append("group by ic.id,ic.name,ic.title,ic.valid,ic.type,ic.path,ic.deleted,ic.deployed,ic.released");
        }
        Query<InventoryTreeFolderItem> query = getSession().createQuery(hql.toString(), InventoryTreeFolderItem.class);
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

    public Long getCountConfigurationsByFolder(String folder, boolean recursive, Collection<Integer> configTypes) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select count(id) from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        if (recursive) {
            hql.append(" where (folder=:folder or folder like :likeFolder)");
        } else {
            hql.append(" where folder=:folder");
        }
        if (configTypes != null && !configTypes.isEmpty()) {
            hql.append(" and type in (:configTypes) ");
        }
        Query<Long> query = getSession().createQuery(hql.toString());
        query.setParameter("folder", folder);
        if (recursive) {
            query.setParameter("likeFolder", folder + "/%");
        }
        if (configTypes != null && !configTypes.isEmpty()) {
            query.setParameterList("configTypes", configTypes);
        }
        return getSession().getSingleResult(query);
    }

    public Map<DBItemInventoryConfiguration, Set<InventoryDeploymentItem>> getConfigurationsWithAllDeployments(Collection<Integer> types,
            String folder, boolean recursive, Collection<String> deletedFolders) throws SOSHibernateException {

        StringBuilder hql = new StringBuilder("select ");
        hql.append("ic.id as icId");
        hql.append(",ic.type as icType");
        hql.append(",ic.path as icPath");
        hql.append(",ic.name as icName");
        hql.append(",ic.folder as icFolder");
        hql.append(",ic.title as icTitle");
        hql.append(",ic.valid as icValid");
        hql.append(",ic.deleted as icDeleted");
        hql.append(",ic.deployed as icDeployed");
        hql.append(",ic.released as icReleased");
        hql.append(",ic.auditLogId as icAuditLogId");
        hql.append(",ic.created as icCreated");
        hql.append(",ic.modified as icModified");
        hql.append(",dh.id as dhId");
        hql.append(",dh.commitId as dhCommitId");
        hql.append(",dh.version as dhVersion");
        hql.append(",dh.operation as dhOperation");
        hql.append(",dh.deploymentDate as dhDeploymentDate");
        hql.append(",dh.path as dhPath");
        hql.append(",dh.controllerId as dhControllerId");
        hql.append(" from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
        hql.append("left join ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dh ");
        hql.append("on ic.id=dh.inventoryConfigurationId ");
        hql.append("where ic.id in (").append(getNotDeletedConfigurationsHQL(types, folder, recursive, deletedFolders)).append(")");
        hql.append("and (dh.state = :state or dh.id is null)");

        Query<InventoryDeployablesTreeFolderItem> query = getSession().createQuery(hql.toString(), InventoryDeployablesTreeFolderItem.class);
        if (types != null && !types.isEmpty()) {
            query.setParameterList("types", types);
        }
        if (folder != null && !folder.isEmpty()) {
            query.setParameter("folder", folder);
            if (recursive) {
                query.setParameter("likeFolder", (folder + "/%").replaceAll("//+", "/"));
            }
        }
        query.setParameter("state", DeploymentState.DEPLOYED.value());

        List<InventoryDeployablesTreeFolderItem> result = getSession().getResultList(query);
        if (result != null) {
            Comparator<InventoryDeploymentItem> comp = Comparator.nullsFirst(Comparator.comparing(InventoryDeploymentItem::getDeploymentDate)
                    .reversed());
            return result.stream().map(InventoryDeployablesTreeFolderItem::map).collect(Collectors.groupingBy(
                    InventoryDeployablesTreeFolderItem::getConfiguration, Collectors.mapping(InventoryDeployablesTreeFolderItem::getDeployment,
                            Collectors.toCollection(() -> new TreeSet<>(comp)))));
        }

        return Collections.emptyMap();
    }

    public DBItemInventoryConfiguration getConfiguration(Long id) throws SOSHibernateException {
        return getSession().get(DBItemInventoryConfiguration.class, id);
    }

    public DBItemInventoryConfigurationTrash getTrashConfiguration(Long id) throws SOSHibernateException {
        return getSession().get(DBItemInventoryConfigurationTrash.class, id);
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
        return getConfiguration(path, type, DBLayer.DBITEM_INV_CONFIGURATIONS);
    }

    public DBItemInventoryConfigurationTrash getTrashConfiguration(String path, Integer type) throws SOSHibernateException {
        return getConfiguration(path, type, DBLayer.DBITEM_INV_CONFIGURATION_TRASH);
    }

    public <T extends DBItem> T getConfiguration(String path, Integer type, String tableName) throws SOSHibernateException {
        boolean isCalendar = JocInventory.isCalendar(type);
        StringBuilder hql = new StringBuilder("from ").append(tableName);
        hql.append(" where lower(path)=:path");
        if (isCalendar) {
            hql.append(" and type in (:types)");
        } else {
            hql.append(" and type=:type");
        }
        Query<T> query = getSession().createQuery(hql.toString());
        query.setParameter("path", path.toLowerCase());
        if (isCalendar) {
            query.setParameterList("types", JocInventory.getCalendarTypes());
        } else {
            query.setParameter("type", type);
        }
        return getSession().getSingleResult(query);
    }

    public List<DBItemInventoryConfiguration> getConfigurationByName(String name, Integer type) throws DBConnectionRefusedException,
            DBInvalidDataException {
        return getConfigurationByName(name, type, DBLayer.DBITEM_INV_CONFIGURATIONS);
    }

    public List<DBItemInventoryConfigurationTrash> getTrashConfigurationByName(String name, Integer type) throws DBConnectionRefusedException,
            DBInvalidDataException {
        return getConfigurationByName(name, type, DBLayer.DBITEM_INV_CONFIGURATION_TRASH);
    }

    public <T extends DBItem> List<T> getConfigurationByName(String name, Integer type, String tableName) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            boolean isCalendar = JocInventory.isCalendar(type);
            StringBuilder hql = new StringBuilder("from ").append(tableName);
            hql.append(" where lower(name)=:name");
            if (isCalendar) {
                hql.append(" and type in (:types)");
            } else {
                hql.append(" and type=:type");
            }
            Query<T> query = getSession().createQuery(hql.toString());
            query.setParameter("name", name.toLowerCase());
            if (isCalendar) {
                query.setParameterList("types", JocInventory.getCalendarTypes());
            } else {
                query.setParameter("type", type);
            }
            List<T> result = getSession().getResultList(query);
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

    public List<DBItemInventoryConfiguration> getConfigurationByNames(List<String> names, Integer type) throws SOSHibernateException {
        boolean isCalendar = JocInventory.isCalendar(type);
        if (names == null) {
            names = Collections.emptyList();
        }
        if (names.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            List<DBItemInventoryConfiguration> result = new ArrayList<>();
            for (int i = 0; i < names.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                result.addAll(getConfigurationByNames(SOSHibernate.getInClausePartition(i, names), type));
            }
            return result;
        } else {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            if (isCalendar) {
                hql.append(" where type in (:types)");
            } else {
                hql.append(" where type=:type");
            }
            if (!names.isEmpty()) {
                hql.append(" and lower(name) in (:names)");
            }

            Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
            if (!names.isEmpty()) {
                query.setParameterList("names", names.stream().map(String::toLowerCase).collect(Collectors.toSet()));
            }
            if (isCalendar) {
                query.setParameterList("types", JocInventory.getCalendarTypes());
            } else {
                query.setParameter("type", type);
            }
            List<DBItemInventoryConfiguration> result = getSession().getResultList(query);
            if (result == null) {
                return Collections.emptyList();
            }
            return result;
        }
    }
    
    public List<String> getBoardNames() throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select name from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where type=:type");
        Query<String> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.NOTICEBOARD.intValue());
        List<String> result = getSession().getResultList(query);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }

    public Integer getSuffixNumber(String suffix, String name, Integer type) throws SOSHibernateException {
        if (name == null || name.isEmpty() || type == ConfigurationType.FOLDER.intValue()) {
            name = "%";
        } else {
            name = name.toLowerCase().replaceFirst("-" + suffix + "[0-9]*$", "");
        }
        StringBuilder hql = new StringBuilder("select name from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where lower(name) like :likename");
        if (type == null || type == ConfigurationType.FOLDER.intValue()) {
            hql.append(" and type != :type");
        } else {
            hql.append(" and type = :type");
        }
        Query<String> query = getSession().createQuery(hql.toString());
        query.setParameter("likename", name + "-" + suffix.toLowerCase() + "%");
        if (type == null || type == ConfigurationType.FOLDER.intValue()) {
            query.setParameter("type", ConfigurationType.FOLDER.intValue());
        } else {
            query.setParameter("type", type);
        }
        List<String> result = getSession().getResultList(query);
        if (result == null || result.isEmpty()) {
            return 0;
        }
        Predicate<String> predicate = Pattern.compile(".+-" + suffix + "[0-9]*$", Pattern.CASE_INSENSITIVE).asPredicate();
        Function<String, Integer> mapper = n -> Integer.parseInt(n.replaceFirst(".+-" + suffix.toLowerCase() + "([0-9]*)$", "0$1"));
        SortedSet<Integer> numbers = result.stream().map(String::toLowerCase).distinct().filter(predicate).map(mapper).sorted().collect(Collectors
                .toCollection(TreeSet::new));
        if (numbers.isEmpty()) {
            return 0;
        }
        Integer num = 0;
        for (Integer number : numbers) {
            if (num < number) {
                break;
            }
            num = number + 1;
        }
        return num;
    }

    public Integer getPrefixNumber(String prefix, String name, Integer type) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select name from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where lower(name) like :likename");
        if (type == null || type == ConfigurationType.FOLDER.intValue()) {
            hql.append(" and type != :type");
        } else {
            hql.append(" and type = :type");
        }
        Query<String> query = getSession().createQuery(hql.toString());
        if (name == null || name.isEmpty() || type == ConfigurationType.FOLDER.intValue()) {
            query.setParameter("likename", prefix.toLowerCase() + "%");
        } else {
            query.setParameter("likename", prefix.toLowerCase() + "%-" + name.toLowerCase().replaceFirst("^" + prefix + "[0-9]*-", ""));
        }
        if (type == null || type == ConfigurationType.FOLDER.intValue()) {
            query.setParameter("type", ConfigurationType.FOLDER.intValue());
        } else {
            query.setParameter("type", type);
        }
        List<String> result = getSession().getResultList(query);
        if (result == null || result.isEmpty()) {
            return 0;
        }
        Predicate<String> predicate = Pattern.compile("^" + prefix + "[0-9]*-.+", Pattern.CASE_INSENSITIVE).asPredicate();
        Function<String, Integer> mapper = n -> Integer.parseInt(n.replaceFirst("^" + prefix.toLowerCase() + "([0-9]*)-.+", "0$1"));
        SortedSet<Integer> numbers = result.stream().map(String::toLowerCase).distinct().filter(predicate).map(mapper).sorted().collect(Collectors
                .toCollection(TreeSet::new));
        if (numbers.isEmpty()) {
            return 0;
        }
        Integer num = 0;
        for (Integer number : numbers) {
            if (num < number) {
                break;
            }
            num = number + 1;
        }
        return num;
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

    public DBItemInventoryConfiguration getCalendarByName(String name) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where lower(name)=:name");
        hql.append(" and type in (:types)");
        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("name", name.toLowerCase());
        query.setParameterList("types", JocInventory.getCalendarTypes());
        query.setMaxResults(1);
        return getSession().getSingleResult(query);
    }

    public List<DBItemInventoryConfiguration> getCalendarsByNames(List<String> names) throws SOSHibernateException {
        if (names == null) {
            names = Collections.emptyList();
        }
        if (names.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            List<DBItemInventoryConfiguration> result = new ArrayList<>();
            for (int i = 0; i < names.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                result.addAll(getCalendarsByNames(SOSHibernate.getInClausePartition(i, names)));
            }
            return result;
        } else {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            hql.append(" where type in (:types)");
            if (!names.isEmpty()) {
                hql.append(" and lower(name) in (:names)");
            }
            Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
            if (!names.isEmpty()) {
                query.setParameterList("names", names.stream().map(String::toLowerCase).collect(Collectors.toSet()));
            }
            query.setParameterList("types", JocInventory.getCalendarTypes());
            List<DBItemInventoryConfiguration> result = getSession().getResultList(query);
            if (result == null) {
                return Collections.emptyList();
            }
            return result;
        }
    }

    public List<DBItemInventoryReleasedConfiguration> getReleasedCalendarsByNames(List<String> names) throws SOSHibernateException {
        if (names == null) {
            names = Collections.emptyList();
        }
        if (names.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            List<DBItemInventoryReleasedConfiguration> result = new ArrayList<>();
            for (int i = 0; i < names.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                result.addAll(getReleasedCalendarsByNames(SOSHibernate.getInClausePartition(i, names)));
            }
            return result;
        } else {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
            hql.append(" where type in (:types)");
            if (!names.isEmpty()) {
                hql.append(" and lower(name) in (:names)");
            }
            Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql.toString());
            if (!names.isEmpty()) {
                query.setParameterList("names", names.stream().map(String::toLowerCase).collect(Collectors.toSet()));
            }
            query.setParameterList("types", JocInventory.getCalendarTypes());
            List<DBItemInventoryReleasedConfiguration> result = getSession().getResultList(query);
            if (result == null) {
                return Collections.emptyList();
            }
            return result;
        }
    }
    
    public List<DBItemInventoryReleasedConfiguration> getReleasedJobTemplatesByNames(List<String> names) throws SOSHibernateException {
        if (names == null) {
            names = Collections.emptyList();
        }
        if (names.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            List<DBItemInventoryReleasedConfiguration> result = new ArrayList<>();
            for (int i = 0; i < names.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                result.addAll(getReleasedJobTemplatesByNames(SOSHibernate.getInClausePartition(i, names)));
            }
            return result;
        } else {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
            hql.append(" where type = :type");
            if (!names.isEmpty()) {
                hql.append(" and lower(name) in (:names)");
            }
            Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql.toString());
            if (!names.isEmpty()) {
                query.setParameterList("names", names.stream().map(String::toLowerCase).collect(Collectors.toSet()));
            }
            query.setParameter("type", ConfigurationType.JOBTEMPLATE.intValue());
            List<DBItemInventoryReleasedConfiguration> result = getSession().getResultList(query);
            if (result == null) {
                return Collections.emptyList();
            }
            return result;
        }
    }

    public List<DBItemInventoryReleasedConfiguration> getConfigurationsByType(Collection<Integer> types) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
        if (types != null && !types.isEmpty()) {
            hql.append(" where type in (:types)");
        }
        Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql.toString());
        if (types != null && !types.isEmpty()) {
            query.setParameterList("types", types);
        }
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryReleasedConfiguration> getReleasedConfigurations(List<Long> ids) throws SOSHibernateException {
        if (ids == null) {
            ids = Collections.emptyList();
        }
        if (ids.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            List<DBItemInventoryReleasedConfiguration> result = new ArrayList<>();
            for (int i = 0; i < ids.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                result.addAll(getReleasedConfigurations(SOSHibernate.getInClausePartition(i, ids)));
            }
            return result;
        } else {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
            if (!ids.isEmpty()) {
                hql.append(" where id in (:ids)");
            }
            Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql.toString());
            if (!ids.isEmpty()) {
                query.setParameterList("ids", ids);
            }
            List<DBItemInventoryReleasedConfiguration> result = getSession().getResultList(query);
            if (result == null) {
                return Collections.emptyList();
            }
            return result;
        }
    }

    public List<DBItemInventoryConfiguration> getFolderContent(String folder, boolean recursive, Collection<Integer> types)
            throws SOSHibernateException {
        return getFolderContent(folder, recursive, types, DBLayer.DBITEM_INV_CONFIGURATIONS);
    }

    public List<DBItemInventoryConfigurationTrash> getTrashFolderContent(String folder, boolean recursive, Collection<Integer> types)
            throws SOSHibernateException {
        return getFolderContent(folder, recursive, types, DBLayer.DBITEM_INV_CONFIGURATION_TRASH);
    }

    public <T> List<T> getFolderContent(String folder, boolean recursive, Collection<Integer> types, String tableName) throws SOSHibernateException {
        if (folder == null) {
            folder = "/";
        }
        StringBuilder hql = new StringBuilder("from ").append(tableName);
        if (recursive) {
            if (!"/".equals(folder)) {
                hql.append(" where (folder=:folder or folder like :likeFolder)");
            } else {
                hql.append(" where 1=1");
            }
        } else {
            hql.append(" where folder=:folder");
        }
        if (types != null && !types.isEmpty()) {
            hql.append(" and type in (:types)");
        }
        Query<T> query = getSession().createQuery(hql.toString());
        if (recursive) {
            if (!"/".equals(folder)) {
                query.setParameter("folder", folder);
                query.setParameter("likeFolder", folder + "/%");
            }
        } else {
            query.setParameter("folder", folder);
        }
        if (types != null && !types.isEmpty()) {
            query.setParameterList("types", types);
        }
        List<T> result = getSession().getResultList(query);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }

    public Set<String> getScriptNames() throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select name from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" where type=:type");
        Query<String> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.INCLUDESCRIPT.intValue());
        List<String> result = getSession().getResultList(query);
        if (result == null) {
            return Collections.emptySet();
        }
        return result.stream().collect(Collectors.toSet());
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

    public DBItemSearchWorkflow getSearchWorkflow(Long inventoryId, String hash) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            boolean isDraft = SOSString.isEmpty(hash);
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" ");
            hql.append("where inventoryConfigurationId=:inventoryId ");
            if (isDraft) {
                hql.append("and deployed=false ");
            } else {
                hql.append("and deployed=true ");
                hql.append("and contentHash=:hash ");
            }
            Query<DBItemSearchWorkflow> query = getSession().createQuery(hql.toString());
            query.setParameter("inventoryId", inventoryId);
            if (isDraft) {
                // workaround for multiple draft entries - only 1 is allowed
                List<DBItemSearchWorkflow> result = getSession().getResultList(query);
                if (result == null || result.size() == 0) {
                    return null;
                }
                if (result.size() == 1) {
                    return result.get(0);
                } else {
                    List<DBItemSearchWorkflow> sorted = result.stream().sorted(Comparator.comparing(DBItemSearchWorkflow::getId)).collect(Collectors
                            .toList());
                    DBItemSearchWorkflow last = sorted.get(sorted.size() - 1);
                    for (DBItemSearchWorkflow w : result) {
                        if (!w.getId().equals(last.getId())) {
                            getSession().delete(w);
                        }
                    }
                    return last;
                }

            } else {
                query.setParameter("hash", hash);
                // return getSession().getSingleResult(query);
                List<DBItemSearchWorkflow> result = getSession().getResultList(query);
                if (result == null || result.size() == 0) {
                    return null;
                }
                return result.get(0);
            }
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public void searchWorkflow2DeploymentHistory(Long searchWorkflowId, Long inventoryId, String controllerId, List<Long> deploymentIds,
            boolean delete) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            List<DBItemSearchWorkflow2DeploymentHistory> items = getSearchWorkflow2DeploymentHistory(inventoryId);
            if (items != null && items.size() > 0) {
                List<Long> toDelete = new ArrayList<Long>();
                List<Long> toHold = new ArrayList<Long>();
                for (DBItemSearchWorkflow2DeploymentHistory item : items) {
                    if (item.getControllerId().equals(controllerId)) {
                        if (item.getSearchWorkflowId().equals(searchWorkflowId)) {
                            if (delete) {
                                if (!toDelete.contains(item.getSearchWorkflowId())) {
                                    toDelete.add(item.getSearchWorkflowId());
                                }
                            }
                        } else {
                            if (!toDelete.contains(item.getSearchWorkflowId())) {
                                toDelete.add(item.getSearchWorkflowId());
                            }
                        }
                        getSession().delete(item);
                    } else {
                        if (!toHold.contains(item.getSearchWorkflowId())) {
                            toHold.add(item.getSearchWorkflowId());
                        }
                    }
                }
                for (Long swId : toDelete) {
                    if (!toHold.contains(swId)) {
                        deleteSearchWorkflow(swId, true);
                    }
                }
            }
            if (!delete) {
                // for (Long deploymentId : deploymentIds) {
                DBItemSearchWorkflow2DeploymentHistory item = new DBItemSearchWorkflow2DeploymentHistory();
                item.setSearchWorkflowId(searchWorkflowId);
                item.setInventoryConfigurationId(inventoryId);
                item.setControllerId(controllerId);
                // item.setDeploymentHistoryId(deploymentId);
                item.setDeploymentHistoryId(deploymentIds.stream().max(Comparator.comparing(Long::valueOf)).get());
                getSession().save(item);
                // }
            }

        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    private List<DBItemSearchWorkflow2DeploymentHistory> getSearchWorkflow2DeploymentHistory(Long inventoryId) {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS_DEPLOYMENT_HISTORY);
            hql.append(" where inventoryConfigurationId=:inventoryId");

            Query<DBItemSearchWorkflow2DeploymentHistory> query = getSession().createQuery(hql.toString());
            query.setParameter("inventoryId", inventoryId);

            return getSession().getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    private int deleteSearchWorkflow(Long id, boolean deployed) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS);
        hql.append(" where id=:id");
        hql.append(" and deployed=:deployed");

        Query<DBItemSearchWorkflow> query = getSession().createQuery(hql.toString());
        query.setParameter("id", id);
        query.setParameter("deployed", deployed);

        DBItemSearchWorkflow item = getSession().getSingleResult(query);
        if (item != null) {
            getSession().delete(item);
            return 1;
        }
        return 0;
    }

    public int deleteSearchWorkflowByInventoryId(Long inventoryId, boolean deployed) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS);
        hql.append(" where inventoryConfigurationId=:inventoryId");
        hql.append(" and deployed=:deployed");

        Query<DBItemSearchWorkflow> query = getSession().createQuery(hql.toString());
        query.setParameter("inventoryId", inventoryId);
        query.setParameter("deployed", deployed);

        DBItemSearchWorkflow item = getSession().getSingleResult(query);
        if (item != null) {
            getSession().delete(item);
            return 1;
        }
        return 0;
    }

    public int deleteTrashFolder(String folder) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_INV_CONFIGURATION_TRASH);
        if (!JocInventory.ROOT_FOLDER.equals(folder)) {
            hql.append(" where path =: folder or path like :likeFolder");
        }
        Query<?> query = getSession().createQuery(hql.toString());
        if (!JocInventory.ROOT_FOLDER.equals(folder)) {
            query.setParameter("folder", folder);
            query.setParameter("likeFolder", folder + "/%");
        }
        return getSession().executeUpdate(query);
    }

    public Set<Tree> getFoldersByFolderAndTypeForViews(String folder, Set<Integer> inventoryTypes) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            List<String> whereClause = new ArrayList<String>();
            StringBuilder sql = new StringBuilder();
            sql.append("select folder from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
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
                return result.stream().map(s -> {
                    Tree tree = new Tree();
                    tree.setPath(s);
                    return tree;
                }).collect(Collectors.toSet());
            } else if (folder.equals(JocInventory.ROOT_FOLDER)) {
                Tree tree = new Tree();
                tree.setPath(JocInventory.ROOT_FOLDER);
                return Arrays.asList(tree).stream().collect(Collectors.toSet());
            }
            return new HashSet<>();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Set<Tree> getFoldersByFolderAndTypeForInventory(String folder, Set<Integer> inventoryTypes, Boolean onlyValidObjects)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            List<String> whereClause = new ArrayList<String>();
            StringBuilder sql = new StringBuilder();
            sql.append("select folder, type, path from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
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

            List<Object[]> result = getSession().getResultList(query);
            if (result != null && !result.isEmpty()) {
                Set<String> folders = result.stream().map(item -> {
                    Integer type = (Integer) item[1];
                    if (type.equals(ConfigurationType.FOLDER.intValue())) {
                        return (String) item[2];
                    }
                    return (String) item[0];
                }).collect(Collectors.toSet());

                Set<String> folderWithParents = new HashSet<>();
                for (String f : folders) {
                    Path p = Paths.get(f);
                    for (int i = 0; i < p.getNameCount(); i++) {
                        folderWithParents.add(("/" + p.subpath(0, i + 1)).replace('\\', '/'));
                    }
                }
                Set<Tree> tree = getFoldersByFolder(new ArrayList<String>(folderWithParents));
                Tree root = new Tree();
                root.setPath(JocInventory.ROOT_FOLDER);
                root.setDeleted(false);
                tree.add(root);

                return tree;
            } else if (JocInventory.ROOT_FOLDER.equals(folder)) {
                Set<Tree> tree = new HashSet<>();
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

    private Set<Tree> getFoldersByFolder(List<String> folders) throws SOSHibernateException {
        return _getFoldersByFolder(folders).stream().collect(Collectors.toSet());
    }

    private List<FolderItem> _getFoldersByFolder(List<String> folders) throws SOSHibernateException {
        if (folders == null) {
            folders = Collections.emptyList();
        }
        if (folders.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            List<FolderItem> result = new ArrayList<>();
            for (int i = 0; i < folders.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                result.addAll(_getFoldersByFolder(SOSHibernate.getInClausePartition(i, folders)));
            }
            return result;
        } else if (!folders.isEmpty()) {
            StringBuilder sql = new StringBuilder();
            sql.append("select new ").append(FolderItem.class.getName()).append("(path, deleted, repoControlled) from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            //sql.append("select new ").append(FolderItem.class.getName()).append("(path, deleted) from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            sql.append(" where path in (:folders) and type=:type");
            Query<FolderItem> query = getSession().createQuery(sql.toString());
            query.setParameterList("folders", folders);
            query.setParameter("type", ConfigurationType.FOLDER.intValue());
            List<FolderItem> result = getSession().getResultList(query);
            if (result != null) {
                return result;
            }
        }
        return Collections.emptyList();
    }

    public List<DBItemInventoryConfiguration> getUsedWorkflowsByLockId(String lockName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select ic from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
        hql.append("left join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
        hql.append("on ic.id=sw.inventoryConfigurationId ");
        hql.append("where ic.type=:type ");
        hql.append("and ic.deployed=sw.deployed ");
        hql.append("and ");
        hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "sw.instructions", "$.locks.\"" + lockName + "\"")).append(" is not null");

        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.WORKFLOW.intValue());
        return getSession().getResultList(query);
    }
    
    public List<DBItemInventoryConfiguration> getUsedWorkflowsByJobTemplateName(String jobTemplateName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select ic from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
        hql.append("left join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
        hql.append("on ic.id=sw.inventoryConfigurationId ");
        hql.append("where ic.type=:type ");
        hql.append("and ic.deployed=sw.deployed ");
        hql.append("and ");
        hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "sw.jobs", "$.jobTemplates.\"" + jobTemplateName + "\"")).append(" is not null");

        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.WORKFLOW.intValue());
        List<DBItemInventoryConfiguration> result = getSession().getResultList(query);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }
    
//    public List<DBItemInventoryConfiguration> getUsedWorkflowsByJobTemplateNames(Collection<String> jobTemplateNames) throws SOSHibernateException {
//        if (jobTemplateNames == null || jobTemplateNames.isEmpty()) {
//            return Collections.emptyList();
//        }
//        StringBuilder hql = new StringBuilder("select ic from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
//        hql.append("left join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
//        hql.append("on ic.id=sw.inventoryConfigurationId ");
//        hql.append("where ic.type=:type ");
//        hql.append("and ic.deployed=sw.deployed ");
//        hql.append("and (");
//        hql.append(jobTemplateNames.stream().map(jobTemplateName -> SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "sw.jobs",
//                "$.jobTemplates.\"" + jobTemplateName + "\"") + " is not null").collect(Collectors.joining(" or ")));
//        hql.append(")");
//
//        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
//        query.setParameter("type", ConfigurationType.WORKFLOW.intValue());
//        List<DBItemInventoryConfiguration> result = getSession().getResultList(query);
//        if (result == null) {
//            return Collections.emptyList();
//        }
//        return result;
//    }
    
//    public String getUsedJobTemplateUsedByWorkflow(Long invId) throws SOSHibernateException {
//        StringBuilder hql = new StringBuilder("select inventoryConfigurationId as invId, ");
//        hql.append(SOSHibernateJsonValue.getFunction(ReturnType.JSON, "sw.jobs", "$.jobTemplates")).append(" as json");
//        hql.append(" from ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS);
//        hql.append("where inventoryConfigurationId=:invId ");
//        
//        Query<String> query = getSession().createQuery(hql.toString());
//        query.setParameter("invId", invId);
//        String result = getSession().getSingleResult(query);
//        if (result == null) {
//            return "{}";
//        }
//        return result;
//    }

    public List<DBItemInventoryConfiguration> getUsedWorkflowsByAddOrdersWorkflowName(String workflowName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select ic from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
        hql.append("left join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
        hql.append("on ic.id=sw.inventoryConfigurationId ");
        hql.append("where ic.type=:type ");
        hql.append("and ic.deployed=sw.deployed ");
        hql.append("and ");

        String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "sw.instructions", "$.addOrders");
        hql.append(SOSHibernateRegexp.getFunction(jsonFunc, ":workflowName"));

        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.WORKFLOW.intValue());
        query.setParameter("workflowName", getRegexpParameter(workflowName, "\""));
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryConfiguration> getUsedWorkflowsByBoardName(String boardName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select ic from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
        hql.append("left join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
        hql.append("on ic.id=sw.inventoryConfigurationId ");
        hql.append("where ic.type=:type ");
        hql.append("and ic.deployed=sw.deployed ");
        hql.append("and ");

        String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "sw.instructions", "$.noticeBoardNames");
        hql.append(SOSHibernateRegexp.getFunction(jsonFunc, ":boardName"));

        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.WORKFLOW.intValue());
        query.setParameter("boardName", getRegexpParameter(boardName, "\""));
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryConfiguration> getUsedWorkflowsByJobResource(String jobResourceName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select ic from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
        hql.append("left join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
        hql.append("on ic.id=sw.inventoryConfigurationId ");
        hql.append("where ic.type=:type ");
        hql.append("and ic.deployed=sw.deployed ");
        hql.append("and (");
        String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "ic.jsonContent", "$.jobResourceNames");
        hql.append(SOSHibernateRegexp.getFunction(jsonFunc, ":jobResourceName"));
        hql.append(" or ");
        String jsonFunc2 = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "sw.jobs", "$.jobResources");
        hql.append(SOSHibernateRegexp.getFunction(jsonFunc2, ":jobResourceName"));
        hql.append(")");

        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("jobResourceName", getRegexpParameter(jobResourceName, "\""));
        query.setParameter("type", ConfigurationType.WORKFLOW.intValue());
        return getSession().getResultList(query);
    }
    
    public List<DBItemInventoryConfiguration> getUsedJobTemplatesByJobResource(String jobResourceName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where type = :type and ");
        String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "jsonContent", "$.jobResourceNames");
        hql.append(SOSHibernateRegexp.getFunction(jsonFunc, ":jobResourceName"));
        
        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.JOBTEMPLATE.intValue());
        query.setParameter("jobResourceName", getRegexpParameter(jobResourceName, "\""));
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryConfiguration> getWorkflowsAndJobTemplatesWithIncludedScripts() throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where type in (:types) and content like :include");
        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameterList("types", Arrays.asList(ConfigurationType.WORKFLOW.intValue(), ConfigurationType.JOBTEMPLATE.intValue()));
        query.setParameter("include", "%" + JsonConverter.scriptInclude + "%");
        return getSession().getResultList(query);
    }
    
//    public List<DBItemInventoryConfiguration> getWorkflowsWithJobTemplates() throws SOSHibernateException {
//        StringBuilder hql = new StringBuilder("select ic from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
//        hql.append("left join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
//        hql.append("on ic.id=sw.inventoryConfigurationId ");
//        hql.append("where ic.type=:type ");
//        hql.append("and ic.deployed=sw.deployed ");
//        hql.append("and (");
//        String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "ic.jsonContent", "$.jobTemplateNames");
//        hql.append(SOSHibernateRegexp.getFunction(jsonFunc, ":jobTemplateName"));
//        hql.append(" or ");
//        String jsonFunc2 = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "sw.jobs", "$.jobResources");
//        hql.append(SOSHibernateRegexp.getFunction(jsonFunc2, ":jobTemplateName"));
//        hql.append(")");
//
//        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
//        query.setParameter("jobResourceName", getRegexpParameter(jobResourceName, ""));
//        query.setParameter("type", ConfigurationType.WORKFLOW.intValue());
//        return getSession().getResultList(query);
//    }

    public List<DBItemInventoryConfiguration> getUsedSchedulesByWorkflowName(String workflowName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select c from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" c ");
        hql.append(",").append(DBLayer.DBITEM_INV_SCHEDULE2WORKFLOWS).append(" sw ");
        hql.append("where c.type = :type ");
        hql.append("and sw.scheduleName = c.name ");
        hql.append("and sw.workflowName = :workflowName ");

        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.SCHEDULE.intValue());
        query.setParameter("workflowName", workflowName);
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryReleasedConfiguration> getUsedReleasedSchedulesByWorkflowName(String workflowName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select c from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" c ");
        hql.append(",").append(DBLayer.DBITEM_INV_RELEASED_SCHEDULE2WORKFLOWS).append(" sw ");
        hql.append("where c.type = :type ");
        hql.append("and sw.scheduleName = c.name ");
        hql.append("and sw.workflowName = :workflowName ");

        Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.SCHEDULE.intValue());
        query.setParameter("workflowName", workflowName);
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryConfiguration> getUsedSchedulesByWorkflowNames(List<String> workflowNames) throws SOSHibernateException {
        if (workflowNames == null) {
            workflowNames = Collections.emptyList();
        }
        int size = workflowNames.size();
        if (size > SOSHibernate.LIMIT_IN_CLAUSE) {
            List<DBItemInventoryConfiguration> result = new ArrayList<>();
            for (int i = 0; i < workflowNames.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                result.addAll(getUsedSchedulesByWorkflowNames(SOSHibernate.getInClausePartition(i, workflowNames)));
            }
            return result;
        } else {
            StringBuilder hql = null;
            if (size > 0) {
                hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" c ");
                hql.append(",").append(DBLayer.DBITEM_INV_SCHEDULE2WORKFLOWS).append(" sw ");
                hql.append("where c.type = :type ");
                hql.append("and sw.scheduleName = c.name ");
                hql.append("and sw.workflowName in (:workflowNames) ");
            } else {
                hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
                hql.append("where type=:type ");
            }

            Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
            query.setParameter("type", ConfigurationType.SCHEDULE.intValue());
            if (size > 0) {
                query.setParameterList("workflowNames", workflowNames);
            }
            List<DBItemInventoryConfiguration> result = getSession().getResultList(query);
            if (result == null) {
                return Collections.emptyList();
            }
            return result;
        }
    }

    public List<DBItemInventoryConfiguration> getUsedFileOrderSourcesByWorkflowName(String workflowName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
        hql.append("where type=:type ");
        hql.append("and ");
        hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "jsonContent", "$.workflowName")).append("=:workflowName");

        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.FILEORDERSOURCE.intValue());
        query.setParameter("workflowName", workflowName);
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryConfiguration> getUsedSchedulesByCalendarPath(String calendarPath) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
        hql.append("where type=:type ");
        hql.append("and ");
        String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "jsonContent", "$.calendars");
        hql.append(SOSHibernateRegexp.getFunction(jsonFunc, ":calendarPath"));

        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.SCHEDULE.intValue());
        query.setParameter("calendarPath", getRegexpParameter(calendarPath, "\""));
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryConfiguration> getUsedSchedulesByCalendarName(String calendarName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
        hql.append("where type=:type ");
        hql.append("and ");
        String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "jsonContent", "$.calendars");
        hql.append(SOSHibernateRegexp.getFunction(jsonFunc, ":calendarName"));

        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.SCHEDULE.intValue());
        query.setParameter("calendarName", getRegexpParameter(calendarName, "\""));
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryConfiguration> getUsedJobsByDocName(String docName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select ic from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
        hql.append("left join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
        hql.append("on ic.id=sw.inventoryConfigurationId ");
        hql.append("where ic.type=:type ");
        hql.append("and ic.deployed=sw.deployed ");
        hql.append("and ");

        String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "sw.jobs", "$.documentationNames");
        hql.append(SOSHibernateRegexp.getFunction(jsonFunc, ":docName"));

        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.WORKFLOW.intValue());
        query.setParameter("docName", getRegexpParameter(docName, "\""));
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryConfiguration> getUsedObjectsByDocName(String docName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where type != :type and ");
        hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "jsonContent", "$.documentationName")).append("=:docName");

        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.FOLDER.intValue());
        query.setParameter("docName", docName);
        return getSession().getResultList(query);
    }

    public String getPathByNameFromInvConfigurations(String name, ConfigurationType type) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select path from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
        hql.append(" where name = :name");
        hql.append(" and type = :type");
        Query<String> query = getSession().createQuery(hql.toString());
        query.setParameter("name", name);
        query.setParameter("type", type.intValue());
        return getSession().getSingleResult(query);

    }

    public String getPathByNameFromInvReleasedConfigurations(String name, ConfigurationType type) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select path from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" ");
        hql.append(" where name = :name");
        hql.append(" and type = :type");
        Query<String> query = getSession().createQuery(hql.toString());
        query.setParameter("name", name);
        query.setParameter("type", type.intValue());
        return getSession().getSingleResult(query);

    }

    public String getPathByNameFromDepHistory(String name, ConfigurationType type) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select path from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" ");
        hql.append(" where name = :name");
        hql.append(" and type = :type");
        Query<String> query = getSession().createQuery(hql.toString());
        query.setParameter("name", name);
        query.setParameter("type", type.intValue());
        return getSession().getSingleResult(query);
    }

    public String getPathByNameFromLatestActiveDepHistoryItem(String name, ConfigurationType type) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select dep.path from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as dep");
        hql.append(" where dep.id = (select max(history.id) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as history");
        hql.append(" where history.name = :name");
        hql.append(" and history.type = :type");
        hql.append(" and history.operation = 0").append(")");
        Query<String> query = getSession().createQuery(hql.toString());
        query.setParameter("name", name);
        query.setParameter("type", type.intValue());
        return getSession().getSingleResult(query);
    }

    // TODO controller?
    public String getDeployedJsonByConfigurationName(ConfigurationType type, String name) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select content from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" ");
        hql.append("where lower(name)=:name ");
        hql.append("and type=:type");

        Query<String> query = getSession().createQuery(hql.toString());
        query.setParameter("name", name.toLowerCase());
        query.setParameter("type", type.intValue());

        List<String> result = getSession().getResultList(query);
        return result == null || result.size() == 0 ? null : result.get(0);
    }

    private String getRegexpParameter(String param, String prefixSuffix) {
        return regexpParamPrefixSuffix + prefixSuffix + param + prefixSuffix + regexpParamPrefixSuffix;
    }

    private void setRegexpParamPrefixSuffix() {
        try {
            if (Dbms.MSSQL.equals(getSession().getFactory().getDbms())) {
                regexpParamPrefixSuffix = "%";
            }
        } catch (Throwable e) {
        }
    }
}
