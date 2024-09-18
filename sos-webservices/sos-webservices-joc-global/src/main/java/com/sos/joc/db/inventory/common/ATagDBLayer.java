package com.sos.joc.db.inventory.common;

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
import com.sos.joc.db.inventory.IDBItemTag;
import com.sos.joc.db.inventory.items.InventoryTagItem;
import com.sos.joc.db.inventory.items.InventoryTreeFolderItem;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;

public abstract class ATagDBLayer<T extends IDBItemTag> extends DBLayer {

    private static final long serialVersionUID = 1L;

    public ATagDBLayer(SOSHibernateSession session) {
        super(session);
    }

    protected abstract String getTagTable();

    protected abstract String getTaggingTable();
    
    public List<String> getAllTagNames() throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select name from ").append(getTagTable());
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

    public List<T> getAllTags() throws DBConnectionRefusedException, DBInvalidDataException {
        return getTags((Set<String>) null);
    }

    public List<T> getTags(Set<String> tagNames) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(getTagTable());
            if (tagNames != null && !tagNames.isEmpty()) {
                if (tagNames.size() == 1) {
                    sql.append(" where name = :name");
                } else {
                    sql.append(" where name in (:names)");
                }
            }
            sql.append(" order by ordering");
            Query<T> query = getSession().createQuery(sql.toString());
            if (tagNames != null && !tagNames.isEmpty()) {
                if (tagNames.size() == 1) {
                    query.setParameter("name", tagNames.iterator().next());
                } else {
                    query.setParameterList("names", tagNames);
                }
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

    public T getTag(String tagName) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            if (tagName == null || tagName.isEmpty()) {
                return null;
            }
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(getTagTable());
            sql.append(" where name = :name");
            sql.append(" order by ordering");
            Query<T> query = getSession().createQuery(sql.toString());
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
            sql.append("select max(ordering) from ").append(getTagTable());
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
        sql.append("delete from ").append(getTagTable());
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
        return deleteTaggingsByTagIds(getTags(tagNames).stream().map(T::getId).collect(Collectors.toList()));
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
            sql.append("delete from ").append(getTaggingTable());
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
            sql.append("delete from ").append(getTaggingTable());
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
        hql.append("left join ").append(getTaggingTable()).append(" tg ");
        hql.append("on ic.id=tg.cid ");
        hql.append("left join ").append(getTagTable()).append(" t ");
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
    
    public List<InventoryTreeFolderItem> getConfigurationsByTag(String tagName, Collection<Integer> configTypes, Boolean onlyValidObjects,
            boolean forTrash) throws SOSHibernateException {
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
            hql.append("left join ").append(getTaggingTable()).append(" tg ");
            hql.append("on (ic.name=tg.name and ic.type=tg.type) ");
        } else {
            hql.append(",ic.deleted as deleted ");
            hql.append(",ic.deployed as deployed ");
            hql.append(",ic.released as released ");
            hql.append(",count(dh.id) as countDeployed ");
            hql.append(",0 as countReleased ");
            //hql.append(",count(irc.id) as countReleased  ");
            hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
            hql.append("left join ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dh ");
            hql.append("on ic.id=dh.inventoryConfigurationId ");
            /* at the moment configTypes always Workflow
            hql.append("left join ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" irc ");
            hql.append("on ic.id=irc.cid ");
            */
            hql.append("left join ").append(getTaggingTable()).append(" tg ");
            hql.append("on ic.id=tg.cid ");
        }
        hql.append("left join ").append(getTagTable()).append(" t ");
        hql.append("on t.id=tg.tagId ");

        List<String> where = new ArrayList<>();
        where.add("t.name=:tagName");
        if (onlyValidObjects == Boolean.TRUE) {
            where.add("ic.valid = 1");
        }
        if (configTypes != null && !configTypes.isEmpty()) {
            if (configTypes.size() == 1) {
                where.add("ic.type=:configTypes");
            } else {
                where.add("ic.type in (:configTypes)"); 
            }
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
            if (configTypes.size() == 1) {
                query.setParameter("configTypes", configTypes.iterator().next());
            } else {
                query.setParameterList("configTypes", configTypes);
            }
        }
        return getSession().getResultList(query);
    }
}
