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
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.commons.hibernate.function.json.SOSHibernateJsonValue;
import com.sos.commons.hibernate.function.json.SOSHibernateJsonValue.ReturnType;
import com.sos.commons.hibernate.function.regex.SOSHibernateRegexp;
import com.sos.commons.util.SOSString;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.items.InventoryDeployablesTreeFolderItem;
import com.sos.joc.db.inventory.items.InventoryDeploymentItem;
import com.sos.joc.db.inventory.items.InventoryNamePath;
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

    public InventoryDBLayer(SOSHibernateSession session) {
        super(session);
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
        hql.append("  and state = :state");
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

    // //invContent is missing in DBItemDepConfiguration
    // public DBItemDepConfiguration getLastDeployedContent(Long configId) throws SOSHibernateException {
    // StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS);
    // hql.append(" where inventoryConfigurationId := configId");
    // hql.append(" order by id desc");
    // Query<DBItemDepConfiguration> query = getSession().createQuery(hql.toString());
    // query.setParameter("configId", configId);
    // query.setMaxResults(1);
    // return getSession().getSingleResult(query);
    // }

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

    public DBItemInventoryReleasedConfiguration getReleasedItemByConfigurationId(Long configId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
        hql.append(" where cid=:configId");
        Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("configId", configId);
        return getSession().getSingleResult(query);
    }

    public Map<Long, DBItemInventoryReleasedConfiguration> getReleasedItemsByConfigurationIds(Collection<Long> configIds)
            throws SOSHibernateException {
        if (configIds != null && !configIds.isEmpty()) {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
            hql.append(" where cId in (:configIds) ");

            Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql.toString());
            query.setParameterList("configIds", configIds);
            List<DBItemInventoryReleasedConfiguration> result = getSession().getResultList(query);
            if (result != null) {
                return result.stream().collect(Collectors.toMap(DBItemInventoryReleasedConfiguration::getCid, Function.identity()));
            }
            return Collections.emptyMap();
        } else {
            return Collections.emptyMap();
        }
    }

    public Integer deleteReleasedItemsByConfigurationIds(Collection<Long> configIds) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
        hql.append(" where cid in (:configIds)");
        Query<Integer> query = getSession().createQuery(hql.toString());
        query.setParameter("configIds", configIds);
        return getSession().executeUpdate(query);
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
        hql.append("(ic, count(dh.id), count(irc.id)) from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
        hql.append("left join ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dh ");
        hql.append("on ic.id=dh.inventoryConfigurationId ");
        hql.append("left join ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" irc ");
        hql.append("on ic.id=irc.cid ");
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

    public List<InventoryDeployablesTreeFolderItem> getConfigurationsWithMaxDeployment(Collection<Long> configIds) throws SOSHibernateException {
        if (configIds != null && !configIds.isEmpty()) {
            StringBuilder hql = new StringBuilder("select new ").append(InventoryDeployablesTreeFolderItem.class.getName());
            hql.append("(");
            hql.append("ic,dh.id as deploymentId,dh.commitId,dh.version,dh.operation,dh.deploymentDate,dh.path,dh.controllerId");
            hql.append(") ");
            hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
            hql.append("left join ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dh ");
            hql.append("on ic.id=dh.inventoryConfigurationId ");
            hql.append("and dh.id=(");
            hql.append("select max(dhsub.id) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(
                    " dhsub where ic.id=dhsub.inventoryConfigurationId and state=" + DeploymentState.DEPLOYED.value());
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

    public Map<DBItemInventoryConfiguration, Set<InventoryDeploymentItem>> getConfigurationsWithAllDeployments(Collection<Long> configIds)
            throws SOSHibernateException {
        if (configIds != null && !configIds.isEmpty()) {
            StringBuilder hql = new StringBuilder("select new ").append(InventoryDeployablesTreeFolderItem.class.getName());
            hql.append("(");
            hql.append("ic");
            hql.append(",dh.id as deploymentId,dh.commitId,dh.version,dh.operation,dh.deploymentDate,dh.path,dh.controllerId");
            hql.append(") ");
            hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
            hql.append("left join ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dh ");
            hql.append("on ic.id=dh.inventoryConfigurationId ");
            hql.append("where ic.id in (:configIds) ");
            hql.append("and (dh.state = :state or ic.valid = 1)");

            Query<InventoryDeployablesTreeFolderItem> query = getSession().createQuery(hql.toString());
            query.setParameterList("configIds", configIds);
            query.setParameter("state", DeploymentState.DEPLOYED.value());
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

    public List<DBItemInventoryConfiguration> getConfigurations(Collection<Long> ids) throws SOSHibernateException {
        if (ids != null && !ids.isEmpty()) {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            hql.append(" where id in (:ids) ");

            Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
            query.setParameterList("ids", ids);
            return getSession().getResultList(query);
        } else {
            return Collections.emptyList();
        }
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

    public List<DBItemInventoryConfiguration> getConfigurationByName(String name, Integer type) throws SOSHibernateException {
        boolean isCalendar = JocInventory.isCalendar(type);
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where lower(name)=:name");
        if (isCalendar) {
            hql.append(" and type in (:types)");
        } else {
            hql.append(" and type=:type");
        }
        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("name", name.toLowerCase());
        if (isCalendar) {
            query.setParameterList("types", JocInventory.getCalendarTypes());
        } else {
            query.setParameter("type", type);
        }
        return getSession().getResultList(query);
    }
    
    public List<DBItemInventoryConfiguration> getConfigurationByNames(Stream<String> namesStream, Integer type) throws SOSHibernateException {
        boolean isCalendar = JocInventory.isCalendar(type);
        Set<String> names = namesStream.map(String::toLowerCase).collect(Collectors.toSet());
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
            query.setParameterList("names", names);
        }
        if (isCalendar) {
            query.setParameterList("types", JocInventory.getCalendarTypes());
        } else {
            query.setParameter("type", type);
        }
        return getSession().getResultList(query);
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

    // use getCalendarsByNames because Name should be unique and path could be wrong
    @Deprecated
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
    
    public List<DBItemInventoryConfiguration> getCalendarsByNames(Stream<String> namesStream) throws SOSHibernateException {
        Set<String> names = namesStream.map(String::toLowerCase).collect(Collectors.toSet());
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where type in (:types)");
        if (!names.isEmpty()) {
            hql.append(" and lower(name) in (:names)");
        }
        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        if (!names.isEmpty()) {
            query.setParameterList("names", names);
        }
        query.setParameterList("types", JocInventory.getCalendarTypes());
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryReleasedConfiguration> getConfigurations(Stream<String> pathsStream, Collection<Integer> types)
            throws SOSHibernateException {
        Set<String> paths = pathsStream.map(String::toLowerCase).collect(Collectors.toSet());
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
        List<String> clause = new ArrayList<>();
        if (!paths.isEmpty()) {
            clause.add("lower(path) in (:paths)");
        }
        if (types != null && !types.isEmpty()) {
            clause.add("type in (:types)");
        }
        hql.append(clause.stream().collect(Collectors.joining(" and ", " where ", "")));
        Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql.toString());
        if (!paths.isEmpty()) {
            query.setParameterList("paths", paths);
        }
        if (types != null && !types.isEmpty()) {
            query.setParameterList("types", types);
        }
        return getSession().getResultList(query);
    }
    
    public Map<String, String> getNamePathMapping(Collection<String> names, Integer type) throws SOSHibernateException {
        if (names == null || names.isEmpty()) {
            return Collections.emptyMap();
        }
        StringBuilder hql = new StringBuilder("select new ").append(InventoryNamePath.class.getName());
        hql.append("(name, path) from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
        hql.append(" where name in (:names)");
        if (type != null) {
            hql.append(" and type=:type");
        }
        Query<InventoryNamePath> query = getSession().createQuery(hql.toString());
        query.setParameterList("names", names);
        if (type != null) {
            query.setParameter("type", type);
        }
        
        List<InventoryNamePath> result = getSession().getResultList(query);
        if (result != null) {
            return result.stream().distinct().collect(Collectors.toMap(InventoryNamePath::getName, InventoryNamePath::getPath));
        }
        return Collections.emptyMap();
    }

    public List<DBItemInventoryReleasedConfiguration> getReleasedConfigurations(Collection<Long> ids) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
        if (ids != null && !ids.isEmpty()) {
            hql.append(" where id in (:ids)");
        }
        Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql.toString());
        if (ids != null && !ids.isEmpty()) {
            query.setParameterList("ids", ids);
        }
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryConfiguration> getFolderContent(String folder, boolean recursive, Collection<Integer> types)
            throws SOSHibernateException {
        if (folder == null) {
            folder = "/";
        }
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
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
        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
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
        return getSession().getResultList(query);
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
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS);
            hql.append(" where inventoryConfigurationId=:inventoryId");
            if (SOSString.isEmpty(hash)) {// draft
                hql.append(" and deployed=false");
            } else {
                hql.append(" and deployed=true");
                hql.append(" and contentHash=:hash");
            }
            Query<DBItemSearchWorkflow> query = getSession().createQuery(hql.toString());
            query.setParameter("inventoryId", inventoryId);
            if (!SOSString.isEmpty(hash)) {
                query.setParameter("hash", hash);
            }
            return getSession().getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public void searchWorkflow2DeploymentHistory(Long searchWorkflowId, Long inventoryId, String controllerId, List<Long> deploymentIds,
            boolean delete) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            deleteSearchWorkflow2DeploymentHistory(searchWorkflowId, inventoryId, controllerId);
            if (delete) {
                Long count = getCountSearchWorkflow2DeploymentHistory(searchWorkflowId);
                if (count == null || count.equals(0L)) {
                    deleteSearchWorkflow(searchWorkflowId, true);
                }
            } else {
                for (Long deploymentId : deploymentIds) {
                    DBItemSearchWorkflow2DeploymentHistory item = new DBItemSearchWorkflow2DeploymentHistory();
                    item.setSearchWorkflowId(searchWorkflowId);
                    item.setInventoryConfigurationId(inventoryId);
                    item.setControllerId(controllerId);
                    item.setDeploymentHistoryId(deploymentId);
                    getSession().save(item);
                }
            }

        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    private int deleteSearchWorkflow2DeploymentHistory(Long searchWorkflowId, Long inventoryId, String controllerId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS_DEPLOYMENT_HISTORY);
        hql.append(" where searchWorkflowId=:searchWorkflowId");
        hql.append(" and inventoryConfigurationId=:inventoryId");
        hql.append(" and controllerId=:controllerId");

        Query<DBItemSearchWorkflow2DeploymentHistory> query = getSession().createQuery(hql.toString());
        query.setParameter("searchWorkflowId", searchWorkflowId);
        query.setParameter("inventoryId", inventoryId);
        query.setParameter("controllerId", controllerId);

        List<DBItemSearchWorkflow2DeploymentHistory> result = getSession().getResultList(query);
        int deleted = 0;
        if (result != null) {
            for (DBItemSearchWorkflow2DeploymentHistory item : result) {
                getSession().delete(item);
                deleted++;
            }
        }
        return deleted;
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

    public int deleteSearchWorkflowByInventoryId(Long id, boolean deployed) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS);
        hql.append(" where inventoryConfigurationId=:id");
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

    private Long getCountSearchWorkflow2DeploymentHistory(Long searchWorkflowId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select count(deploymentHistoryId) from ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS_DEPLOYMENT_HISTORY);
        hql.append(" where searchWorkflowId=:searchWorkflowId");

        Query<Long> query = getSession().createQuery(hql.toString());
        query.setParameter("searchWorkflowId", searchWorkflowId);
        return getSession().getSingleValue(query);
    }

    // TODO check usage - used by DeployImpl
    public int deleteConfigurations(Set<Long> ids) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where id in (:ids)");
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

    public List<DBItemInventoryConfiguration> getUsedWorkflowsByLockId(String lockId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select ic from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
        hql.append("left join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
        hql.append("on ic.id=sw.inventoryConfigurationId ");
        hql.append("where ic.type=:type ");
        hql.append("and sw.deployed=false ");
        hql.append("and ");
        hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "sw.instructions", "$.locks." + lockId)).append(" is not null");

        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.WORKFLOW.intValue());
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryConfiguration> getUsedSchedulesByWorkflowPath(String workflowPath) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
        hql.append("where type=:type ");
        hql.append("and ");
        hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "content", "$.workflowPath")).append("=:workflowPath");

        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.SCHEDULE.intValue());
        query.setParameter("workflowPath", workflowPath);
        return getSession().getResultList(query);
    }
    
    public List<DBItemInventoryConfiguration> getUsedSchedulesByWorkflowName(String workflowName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
        hql.append("where type=:type ");
        hql.append("and ");
        hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "content", "$.workflowName")).append("=:workflowName");

        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.SCHEDULE.intValue());
        query.setParameter("workflowName", workflowName);
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryConfiguration> getUsedSchedulesByCalendarPath(String calendarPath) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
        hql.append("where type=:type ");
        hql.append("and ");
        String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "content", "$.calendars");
        hql.append(SOSHibernateRegexp.getFunction(jsonFunc, ":calendarPath"));

        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.SCHEDULE.intValue());
        query.setParameter("calendarPath", "\"" + calendarPath + "\"");
        return getSession().getResultList(query);
    }
    
    public List<DBItemInventoryConfiguration> getUsedSchedulesByCalendarName(String calendarName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
        hql.append("where type=:type ");
        hql.append("and ");
        String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "content", "$.calendars");
        hql.append(SOSHibernateRegexp.getFunction(jsonFunc, ":calendarName"));

        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.SCHEDULE.intValue());
        query.setParameter("calendarName", "\"" + calendarName + "\"");
        return getSession().getResultList(query);
    }
    
    public String getPathByNameFromInvConfigurations (String name, ConfigurationType type) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select path from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
        hql.append(" where name = :name");
        hql.append(" and type = :type");
        Query<String> query = getSession().createQuery(hql.toString());
        query.setParameter("name", name);
        query.setParameter("type", type.intValue());
        return getSession().getSingleResult(query);
        
    }

    public String getPathByNameFromInvReleasedConfigurations (String name, ConfigurationType type) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select path from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" ");
        hql.append(" where name = :name");
        hql.append(" and type = :type");
        Query<String> query = getSession().createQuery(hql.toString());
        query.setParameter("name", name);
        query.setParameter("type", type.intValue());
        return getSession().getSingleResult(query);
        
    }

    public String getPathByNameFromDepHistory (String name, ConfigurationType type) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select path from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" ");
        hql.append(" where name = :name");
        hql.append(" and type = :type");
        Query<String> query = getSession().createQuery(hql.toString());
        query.setParameter("name", name);
        query.setParameter("type", type.intValue());
        return getSession().getSingleResult(query);
    }

    public String getPathByNameFromLatestActiveDepHistoryItem (String name, ConfigurationType type) throws SOSHibernateException {
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
}
