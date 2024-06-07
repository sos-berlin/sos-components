package com.sos.joc.db.inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.items.InventoryTagItem;
import com.sos.joc.db.inventory.items.InventoryTreeFolderItem;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class InventoryTagDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;
//    private Map<Long, List<DBItemInventoryTag>> map = Collections.emptyMap();
//    private List<Long> tagIdsWithObjects = Collections.emptyList();

    public InventoryTagDBLayer(SOSHibernateSession session) {
        super(session);
    }
    
    public List<String> getAllTagNames() throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select name from ").append(DBLayer.DBITEM_INV_TAGS);
            sql.append(" order by ordering");
            Query<String> query = getSession().createQuery(sql.toString());
            List<String> result = getSession().getResultList(query);
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
    
    public List<DBItemInventoryTag> getAllTags() throws DBConnectionRefusedException, DBInvalidDataException {
        return getTags((Set<String>) null);
    }
    
    public List<DBItemInventoryTag> getTags(Set<String> tagNames) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_INV_TAGS);
            if (tagNames != null && !tagNames.isEmpty()) {
                if (tagNames.size() == 1) {
                    sql.append(" where name = :name");
                } else {
                    sql.append(" where name in (:names)");
                }
            }
            sql.append(" order by ordering");
            Query<DBItemInventoryTag> query = getSession().createQuery(sql.toString());
            if (tagNames != null && !tagNames.isEmpty()) {
                if (tagNames.size() == 1) {
                    query.setParameter("name", tagNames.iterator().next());
                } else {
                    query.setParameterList("names", tagNames);
                }
            }

            List<DBItemInventoryTag> result = getSession().getResultList(query);
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
    
    public DBItemInventoryTag getTag(String tagName) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            if (tagName == null || tagName.isEmpty()) {
                return null;
            }
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_INV_TAGS);
            sql.append(" where name = :name");
            sql.append(" order by ordering");
            Query<DBItemInventoryTag> query = getSession().createQuery(sql.toString());
            query.setParameter("name", tagName);

            return getSession().getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public Integer getMaxOrdering() throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select max(ordering) from ").append(DBLayer.DBITEM_INV_TAGS);
            Query<Integer> query = getSession().createQuery(sql.toString());
            Integer result = getSession().getSingleResult(query);
            if (result == null) {
                return 0;
            }
            return result;
            
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public Integer deleteTags(Set<String> tagNames) throws SOSHibernateException {
        if (tagNames == null || tagNames.isEmpty()) {
            return 0;
        }
        StringBuilder sql = new StringBuilder();
        sql.append("delete from ").append(DBLayer.DBITEM_INV_TAGS);
        if (tagNames.size() == 1) {
            sql.append(" where name = :name");
        } else {
            sql.append(" where name in (:names)");
        }
        Query<Integer> query = getSession().createQuery(sql.toString());
        if (tagNames.size() == 1) {
            query.setParameter("name", tagNames.iterator().next());
        } else {
            query.setParameterList("names", tagNames);
        }
        return getSession().executeUpdate(query);
    }
    
    public Integer deleteTaggings(Set<String> tagNames) throws SOSHibernateException {
        if (tagNames == null || tagNames.isEmpty()) {
            return 0;
        }
        return deleteTaggingsByTagIds(getTags(tagNames).stream().map(DBItemInventoryTag::getId).collect(Collectors.toList()));
    }
    
    private Integer deleteTaggingsByTagIds(List<Long> tagIds) throws SOSHibernateException {
        if (tagIds == null || tagIds.isEmpty()) {
            return 0;
        }
        if (tagIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            int result = 0;
            for (int i = 0; i < tagIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                result += deleteTaggingsByTagIds(SOSHibernate.getInClausePartition(i, tagIds));
            }
            return result;
        } else {
            StringBuilder sql = new StringBuilder();
            sql.append("delete from ").append(DBLayer.DBITEM_INV_TAGGINGS);
            if (tagIds.size() == 1) {
                sql.append(" where tagId = :tagId");
            } else {
                sql.append(" where tagId in (:tagIds)");
            }
            Query<Integer> query = getSession().createQuery(sql.toString());
            if (tagIds.size() == 1) {
                query.setParameter("tagId", tagIds.iterator().next());
            } else {
                query.setParameterList("tagIds", tagIds);
            }
            return getSession().executeUpdate(query);
        }
    }
    
    public Integer deleteTaggingsByIds(List<Long> taggingIds) throws SOSHibernateException {
        if (taggingIds == null || taggingIds.isEmpty()) {
            return 0;
        }
        if (taggingIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            int result = 0;
            for (int i = 0; i < taggingIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                result += deleteTaggingsByIds(SOSHibernate.getInClausePartition(i, taggingIds));
            }
            return result;
        } else {
            StringBuilder sql = new StringBuilder();
            sql.append("delete from ").append(DBLayer.DBITEM_INV_TAGGINGS);
            if (taggingIds.size() == 1) {
                sql.append(" where id = :taggingId");
            } else {
                sql.append(" where id in (:taggingIds)");
            }
            Query<Integer> query = getSession().createQuery(sql.toString());
            if (taggingIds.size() == 1) {
                query.setParameter("taggingId", taggingIds.iterator().next());
            } else {
                query.setParameterList("taggingIds", taggingIds);
            }
            return getSession().executeUpdate(query);
        }
    }
    
//    private List<Long> getTagIdsWithObjects() throws DBConnectionRefusedException, DBInvalidDataException {
//        try {
//            StringBuilder sql = new StringBuilder();
//            sql.append("select tagId from ").append(DBLayer.DBITEM_INV_TAGGINGS).append(" group by tagId");
//            Query<Long> query = getSession().createQuery(sql.toString());
//            
//            List<Long> result = getSession().getResultList(query);
//            if (result == null) {
//                return Collections.emptyList();
//            }
//            return result;
//            
//        } catch (SOSHibernateInvalidSessionException ex) {
//            throw new DBConnectionRefusedException(ex);
//        } catch (Exception ex) {
//            throw new DBInvalidDataException(ex);
//        }
//    }
    
//    public Tree getAllTagsTree(Set<Long> notPermittedTagIds) throws DBConnectionRefusedException, DBInvalidDataException {
//        map = getAllTags().stream().collect(Collectors.groupingBy(DBItemInventoryTag::getTagId));
//        if (notPermittedTagIds == null) {
//            notPermittedTagIds = Collections.emptySet();
//        }
//        Tree tree = new Tree();
//        tree.setPath("/");
//        tree.setFolders(getTreeChildren("", 0L, notPermittedTagIds));
//        return tree;
//    }
    
//    private List<Tree> getTreeChildren(final String parentPath, final Long id, final Set<Long> notPermittedTagIds) {
//        final List<DBItemInventoryTag> children = map.get(id);
//        if (children == null) {
//            return Collections.emptyList();
//        }
//        return children.stream().filter(tag -> !notPermittedTagIds.contains(tag.getId())).map(tag -> {
//            Tree tree = new Tree();
//            tree.setName(tag.getName());
//            tree.setPath(parentPath + "/" + tag.getName());
//            tree.setFolders(getTreeChildren(tree.getPath(), tag.getId(), notPermittedTagIds));
//            return tree;
//        }).collect(Collectors.toList());
//    }
    
    
    public List<InventoryTreeFolderItem> getConfigurationsByTag(String tagName, Collection<Integer> configTypes,
            Boolean onlyValidObjects, boolean forTrash) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select ");
        hql.append("ic.id as id");
        hql.append(",ic.name as name");
        hql.append(",ic.title as title");
        hql.append(",ic.valid as valid");
        hql.append(",ic.type as type");
        hql.append(",ic.path as path");
        hql.append(",ic.modified as modified");
        if (forTrash) {
            hql.append(",false as deleted ");// TODO?
            hql.append(",false as deployed ");
            hql.append(",false as released ");
            hql.append(",0 as countDeployed ");
            hql.append(",0 as countReleased ");
            hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATION_TRASH).append(" ic ");
            hql.append("left join ").append(DBLayer.DBITEM_INV_TAGGINGS).append(" tg ");
            hql.append("on (ic.name=tg.name and ic.type=tg.type) ");
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
            hql.append("left join ").append(DBLayer.DBITEM_INV_TAGGINGS).append(" tg ");
            hql.append("on ic.id=tg.cid ");
        }
        hql.append("left join ").append(DBLayer.DBITEM_INV_TAGS).append(" t ");
        hql.append("on t.id=tg.tagId ");
        
        List<String> where = new ArrayList<>();
        where.add("t.name=:tagName");
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
            hql.append("group by ic.id,ic.name,ic.title,ic.valid,ic.type,ic.path,ic.modified,ic.deleted,ic.deployed,ic.released");
        }
        Query<InventoryTreeFolderItem> query = getSession().createQuery(hql.toString(), InventoryTreeFolderItem.class);
        query.setParameter("tagName", tagName);
        if (configTypes != null && !configTypes.isEmpty()) {
            query.setParameterList("configTypes", configTypes);
        }
        return getSession().getResultList(query);
    }
    
    public List<InventoryTagItem> getTagsByFolders(Collection<Folder> folders, boolean onlyObjectsThatHaveTags) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select ");
        hql.append("ic.id as cId");
        hql.append(",ic.path as path");
        hql.append(",ic.folder as folder");
        hql.append(",ic.type as type");
        hql.append(",t.name as name");
        hql.append(",t.id as tagId");
        hql.append(",tg.id as taggingId");
        hql.append(" from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
        hql.append("left join ").append(DBLayer.DBITEM_INV_TAGGINGS).append(" tg ");
        hql.append("on ic.id=tg.cid ");
        hql.append("left join ").append(DBLayer.DBITEM_INV_TAGS).append(" t ");
        hql.append("on t.id=tg.tagId ");
        
        List<String> where = new ArrayList<>();
        if (onlyObjectsThatHaveTags) {
            where.add("t.id is not null");
        }
        where.add("ic.type = :configType");
        
        if (folders != null && !folders.isEmpty()) {
            String clause = folders.stream().distinct().map(folder -> {
                if (folder.getRecursive()) {
                    return "(folder = '" + folder.getFolder() + "' or folder like '" + (folder.getFolder() + "/%").replaceAll("//+", "/") + "')";
                } else {
                    return "folder = '" + folder.getFolder() + "'";
                }
            }).collect(Collectors.joining(" or "));
            if (folders.size() > 1) {
                clause = "(" + clause + ")";
            }
            where.add(clause);
        }
        
        hql.append("where ").append(String.join(" and ", where)).append(" ");
        Query<InventoryTagItem> query = getSession().createQuery(hql.toString(), InventoryTagItem.class);
        query.setParameter("configType", ConfigurationType.WORKFLOW.intValue());
        List<InventoryTagItem> result = getSession().getResultList(query);
        if (result == null) {
           return Collections.emptyList();
        }
        return result;
    }
    
    public List<InventoryTagItem> getFoldersByTagAndTypeForInventory(Set<Integer> inventoryTypes, Boolean onlyValidObjects)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            List<String> whereClause = new ArrayList<String>();
            StringBuilder sql = new StringBuilder();
            sql.append("select t.tagId as tagId, c.folder as folder from ").append(DBLayer.DBITEM_INV_TAGGINGS).append(" t join ").append(
                    DBLayer.DBITEM_INV_CONFIGURATIONS).append(" c on t.cid = c.id");

            if (inventoryTypes != null && !inventoryTypes.isEmpty()) {
                if (inventoryTypes.size() == 1) {
                    whereClause.add("c.type = :type");
                } else {
                    whereClause.add("c.type in (:types)");
                }
            }
            if (onlyValidObjects == Boolean.TRUE) {
                whereClause.add("c.valid = 1");
            }
            if (!whereClause.isEmpty()) {
                sql.append(whereClause.stream().collect(Collectors.joining(" and ", " where ", "")));
            }
            
            Query<InventoryTagItem> query = getSession().createQuery(sql.toString(), InventoryTagItem.class);
            if (inventoryTypes != null && !inventoryTypes.isEmpty()) {
                if (inventoryTypes.size() == 1) {
                    query.setParameter("type", inventoryTypes.iterator().next());
                } else {
                    query.setParameterList("types", inventoryTypes);
                }
            }
            
            List<InventoryTagItem> result = getSession().getResultList(query);
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
    
    public List<InventoryTagItem> getFoldersByTagAndTypeForInventoryTrash(Set<Integer> inventoryTypes, Boolean onlyValidObjects)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            List<String> whereClause = new ArrayList<String>();
            StringBuilder sql = new StringBuilder();
            sql.append("select t.tagId as tagId, c.folder as folder from ").append(DBLayer.DBITEM_INV_TAGGINGS).append(" t join ").append(
                    DBLayer.DBITEM_INV_CONFIGURATION_TRASH).append(" c on (t.name = c.name and t.type = c.type)");

            if (inventoryTypes != null && !inventoryTypes.isEmpty()) {
                if (inventoryTypes.size() == 1) {
                    whereClause.add("c.type = :type");
                } else {
                    whereClause.add("c.type in (:types)");
                }
            }
            if (onlyValidObjects == Boolean.TRUE) {
                whereClause.add("c.valid = 1");
            }
            if (!whereClause.isEmpty()) {
                sql.append(whereClause.stream().collect(Collectors.joining(" and ", " where ", "")));
            }
            
            Query<InventoryTagItem> query = getSession().createQuery(sql.toString(), InventoryTagItem.class);
            if (inventoryTypes != null && !inventoryTypes.isEmpty()) {
                if (inventoryTypes.size() == 1) {
                    query.setParameter("type", inventoryTypes.iterator().next());
                } else {
                    query.setParameterList("types", inventoryTypes);
                }
            }
            
            List<InventoryTagItem> result = getSession().getResultList(query);
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
    
    public List<InventoryTagItem> getFoldersByTagAndTypeForDeployedObjects(Set<Integer> deployTypes, String controllerId)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            List<String> whereClause = new ArrayList<String>();
            StringBuilder sql = new StringBuilder();
            sql.append("select t.tagId as tagId, c.folder as folder from ").append(DBLayer.DBITEM_INV_TAGGINGS).append(" t join ").append(
                    DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" c on t.cid = c.inventoryConfigurationId");

            if (deployTypes != null && !deployTypes.isEmpty()) {
                if (deployTypes.size() == 1) {
                    whereClause.add("c.type=:type");
                } else {
                    whereClause.add("c.type in (:types)");
                }
            }
            if (controllerId != null) {
                whereClause.add("c.controllerId=:controllerId");
            }
            if (!whereClause.isEmpty()) {
                sql.append(whereClause.stream().collect(Collectors.joining(" and ", " where ", "")));
            }
            
            Query<InventoryTagItem> query = getSession().createQuery(sql.toString(), InventoryTagItem.class);
            if (deployTypes != null && !deployTypes.isEmpty()) {
                if (deployTypes.size() == 1) {
                    query.setParameter("type", deployTypes.iterator().next());
                } else {
                    query.setParameterList("types", deployTypes);
                }
            }
            if (controllerId != null) {
                query.setParameter("controllerId", controllerId);
            }
            
            List<InventoryTagItem> result = getSession().getResultList(query);
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
    
    public List<InventoryTagItem> getFoldersByTagAndTypeForReleasedObjects(Set<Integer> inventoryTypes)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            List<String> whereClause = new ArrayList<String>();
            StringBuilder sql = new StringBuilder();
            sql.append("select t.tagId as tagId, c.folder as folder from ").append(DBLayer.DBITEM_INV_TAGGINGS).append(" t join ").append(
                    DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" c on t.cid = c.cid");

            if (inventoryTypes != null && !inventoryTypes.isEmpty()) {
                if (inventoryTypes.size() == 1) {
                    whereClause.add("c.type=:type");
                } else {
                    whereClause.add("c.type in (:types)");
                }
            }
            if (!whereClause.isEmpty()) {
                sql.append(whereClause.stream().collect(Collectors.joining(" and ", " where ", "")));
            }
            
            Query<InventoryTagItem> query = getSession().createQuery(sql.toString(), InventoryTagItem.class);
            if (inventoryTypes != null && !inventoryTypes.isEmpty()) {
                if (inventoryTypes.size() == 1) {
                    query.setParameter("type", inventoryTypes.iterator().next());
                } else {
                    query.setParameterList("types", inventoryTypes);
                }
            }
            
            List<InventoryTagItem> result = getSession().getResultList(query);
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
    
    public List<DBItemInventoryTagging> getTaggings(Long cid) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_INV_TAGGINGS);
            sql.append(" where cid=:cid");

            Query<DBItemInventoryTagging> query = getSession().createQuery(sql.toString());
            query.setParameter("cid", cid);
            
            List<DBItemInventoryTagging> result = getSession().getResultList(query);
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

    public List<DBItemInventoryTagging> getTaggings(String name, Integer type) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_INV_TAGGINGS);
            sql.append(" where name=:name and type=:type");

            Query<DBItemInventoryTagging> query = getSession().createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("type", type);
            
            List<DBItemInventoryTagging> result = getSession().getResultList(query);
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
    
    public List<String> getTags(Long cid) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select t.name from ").append(DBLayer.DBITEM_INV_TAGGINGS).append(" tg join ").append(
                    DBLayer.DBITEM_INV_TAGS).append(" t on t.id = tg.tagId");
            
            sql.append(" where tg.cid=:cid");
            sql.append(" order by t.ordering");

            Query<String> query = getSession().createQuery(sql.toString());
            query.setParameter("cid", cid);
            
            List<String> result = getSession().getResultList(query);
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
    
    public List<String> getTags(String name, Integer type) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select t.name from ").append(DBLayer.DBITEM_INV_TAGGINGS).append(" tg join ").append(
                    DBLayer.DBITEM_INV_TAGS).append(" t on t.id = tg.tagId");
            
            sql.append(" where tg.name=:name and tg.type=:type");
            sql.append(" order by t.ordering");

            Query<String> query = getSession().createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("type", type);
            
            List<String> result = getSession().getResultList(query);
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
    
    public List<String> getTags(List<Long> cids) {
        if (cids == null || cids.isEmpty()) {
            return Collections.emptyList();
        }
        if (cids.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            List<String> result = new ArrayList<>();
            for (int i = 0; i < cids.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                result.addAll(getTags(SOSHibernate.getInClausePartition(i, cids)));
            }
            return result;
        } else {
            try {
                StringBuilder sql = new StringBuilder();
                sql.append("select t.name from ").append(DBLayer.DBITEM_INV_TAGGINGS).append(" tg join ").append(DBLayer.DBITEM_INV_TAGS).append(
                        " t on t.id = tg.tagId");

                sql.append(" where tg.cid in (:cids)");
                sql.append(" order by t.ordering");

                Query<String> query = getSession().createQuery(sql.toString());
                query.setParameterList("cids", cids);

                List<String> result = getSession().getResultList(query);
                if (result == null) {
                    return Collections.emptyList();
                }

                return result;

            } catch (Exception ex) {
                // this method is exclusively used for tagging events. That should not throw exception. It affects only missing events.
                return Collections.emptyList();
            }
        }
    }
    
    public boolean hasTaggings(String name, Integer type) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select count(id) from ").append(DBLayer.DBITEM_INV_TAGGINGS);
            sql.append(" where name=:name and type=:type");

            Query<Long> query = getSession().createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("type", type);
            
            return getSession().getSingleResult(query) > 0L;
            
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public Set<String> getWorkflowNamesHavingTags(Collection<String> workflowNames, Set<String> tags) throws DBConnectionRefusedException,
            DBInvalidDataException {
        if (workflowNames == null || workflowNames.isEmpty()) {
            return Collections.emptySet();
        }
        List<String> wNames = getWorkflowNamesHavingTags(tags.stream().collect(Collectors.toList()));
        wNames.retainAll(workflowNames);
        return wNames.stream().collect(Collectors.toSet());
    }
    
    public List<String> getWorkflowNamesHavingTags(List<String> tags) throws DBConnectionRefusedException,
            DBInvalidDataException {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        if (tags.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            List<String> result = new ArrayList<>();
            for (int i = 0; i < tags.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                result.addAll(getWorkflowNamesHavingTags(SOSHibernate.getInClausePartition(i, tags)));
            }
            return result;
        } else {
            try {
                StringBuilder hql = new StringBuilder("select tg.name from ").append(DBLayer.DBITEM_INV_TAGGINGS).append(" tg ");
                hql.append(" left join ").append(DBLayer.DBITEM_INV_TAGS).append(" t on t.id=tg.tagId ");
                hql.append("where tg.type=:type ");
                hql.append("and t.name in (:tags) ");
                hql.append("group by tg.name");

                Query<String> query = getSession().createQuery(hql);
                query.setParameter("type", ConfigurationType.WORKFLOW.intValue());
                query.setParameterList("tags", tags);

                List<String> result = getSession().getResultList(query);
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
    }
    
    public int delete(String name, Integer type) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("delete from ").append(DBLayer.DBITEM_INV_TAGGINGS);
            sql.append(" where name=:name and type=:type");

            Query<String> query = getSession().createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("type", type);
            
            return getSession().executeUpdate(query);
            
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public int update(String name, Integer type, Long cid) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("update ").append(DBLayer.DBITEM_INV_TAGGINGS);
            sql.append(" set cid=:cid");
            sql.append(" where name=:name and type=:type");

            Query<String> query = getSession().createQuery(sql.toString());
            query.setParameter("cid", cid);
            query.setParameter("name", name);
            query.setParameter("type", type);
            
            return getSession().executeUpdate(query);
            
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public int update(String trashName, String name, Integer type, Long cid) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("update ").append(DBLayer.DBITEM_INV_TAGGINGS);
            sql.append(" set cid=:cid");
            sql.append(" set name=:name");
            sql.append(" where name=:trashName and type=:type");

            Query<String> query = getSession().createQuery(sql.toString());
            query.setParameter("cid", cid);
            query.setParameter("name", name);
            query.setParameter("trashName", trashName);
            query.setParameter("type", type);
            
            return getSession().executeUpdate(query);
            
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
}
