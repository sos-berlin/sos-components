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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateFactory.Dbms;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.commons.hibernate.function.json.SOSHibernateJsonValue;
import com.sos.commons.hibernate.function.json.SOSHibernateJsonValue.ReturnType;
import com.sos.commons.hibernate.function.regex.SOSHibernateRegexp;
import com.sos.commons.util.SOSString;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
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

    public Map<Long, List<DBItemInventoryReleasedConfiguration>> getReleasedItemsByConfigurationIds(Collection<Long> configIds)
            throws SOSHibernateException {
        if (configIds != null && !configIds.isEmpty()) {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
            hql.append(" where cId in (:configIds) ");

            Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql.toString());
            query.setParameterList("configIds", configIds);
            List<DBItemInventoryReleasedConfiguration> result = getSession().getResultList(query);
            if (result != null) {
                return result.stream().collect(Collectors.groupingBy(DBItemInventoryReleasedConfiguration::getCid));
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

    public Map<DBItemInventoryConfiguration, Set<InventoryDeploymentItem>> getConfigurationsWithAllDeployments(Collection<Long> configIds)
            throws SOSHibernateException {
        if (configIds != null && !configIds.isEmpty()) {
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
            hql.append(",dh.controllerId as dhControllerId ");
            hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
            hql.append("left join ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dh ");
            hql.append("on ic.id=dh.inventoryConfigurationId ");
            hql.append("where ic.id in (:configIds) ");
            hql.append("and (dh.state = :state or dh.id is null)");

            Query<InventoryDeployablesTreeFolderItem> query = getSession().createQuery(hql.toString(), InventoryDeployablesTreeFolderItem.class);
            query.setParameterList("configIds", configIds);
            query.setParameter("state", DeploymentState.DEPLOYED.value());

            List<InventoryDeployablesTreeFolderItem> result = getSession().getResultList(query);
            if (result != null && !result.isEmpty()) {
                Comparator<InventoryDeploymentItem> comp = Comparator.nullsFirst(Comparator.comparing(InventoryDeploymentItem::getDeploymentDate)
                        .reversed());
                return result.stream().map(e -> {
                    return e.map();
                }).collect(Collectors.groupingBy(InventoryDeployablesTreeFolderItem::getConfiguration, Collectors.mapping(
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

    public List<DBItemInventoryReleasedConfiguration> getReleasedCalendarsByNames(Stream<String> namesStream) throws SOSHibernateException {
        Set<String> names = namesStream.map(String::toLowerCase).collect(Collectors.toSet());
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
        hql.append(" where type in (:types)");
        if (!names.isEmpty()) {
            hql.append(" and lower(name) in (:names)");
        }
        Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql.toString());
        if (!names.isEmpty()) {
            query.setParameterList("names", names);
        }
        query.setParameterList("types", JocInventory.getCalendarTypes());
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryReleasedConfiguration> getConfigurations(Stream<String> pathsStream, Collection<Integer> types)
            throws SOSHibernateException {
        Set<String> paths = pathsStream.map(p -> JocInventory.pathToName(p).toLowerCase()).collect(Collectors.toSet());
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
        List<String> clause = new ArrayList<>();
        if (!paths.isEmpty()) {
            clause.add("lower(name) in (:paths)");
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
        query.setParameter("jobResourceName", getRegexpParameter(jobResourceName, ""));
        query.setParameter("type", ConfigurationType.WORKFLOW.intValue());
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryConfiguration> getUsedSchedulesByWorkflowName(String workflowName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
        hql.append("where type=:type ");
        hql.append("and ");
        hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "jsonContent", "$.workflowName")).append("=:workflowName");

        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.SCHEDULE.intValue());
        query.setParameter("workflowName", workflowName);
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryReleasedConfiguration> getUsedReleasedSchedulesByWorkflowName(String workflowName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" ");
        hql.append("where type=:type ");
        hql.append("and ");
        hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "jsonContent", "$.workflowName")).append("=:workflowName");

        Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.SCHEDULE.intValue());
        query.setParameter("workflowName", workflowName);
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryConfiguration> getUsedSchedulesByWorkflowNames(List<String> workflowNames) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
        hql.append("where type=:type ");
        hql.append("and ");
        hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "jsonContent", "$.workflowName")).append(" in :workflowNames");

        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.SCHEDULE.intValue());
        query.setParameterList("workflowNames", workflowNames);
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryReleasedConfiguration> getUsedReleasedSchedulesByWorkflowNames(List<String> workflowNames)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" ");
        hql.append("where type=:type ");
        hql.append("and ");
        hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "jsonContent", "$.workflowName")).append(" in :workflowNames");

        Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.SCHEDULE.intValue());
        query.setParameterList("workflowNames", workflowNames);
        return getSession().getResultList(query);
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

    private String getRegexpParameter(String param, String prefixSuffix) {
        return regexpParamPrefixSuffix + prefixSuffix + param + prefixSuffix + regexpParamPrefixSuffix;
    }

    private void setRegexpParamPrefixSuffix() {
        try {
            if (getSession().getFactory().getDbms().equals(Dbms.MSSQL)) {
                regexpParamPrefixSuffix = "%";
            }
        } catch (Throwable e) {
        }
    }
}
