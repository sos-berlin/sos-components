package com.sos.joc.db.inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.common.ATagDBLayer;
import com.sos.joc.db.inventory.items.InventoryTagItem;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.search.ResponseBaseSearchItem;

public class InventoryTagDBLayer extends ATagDBLayer<DBItemInventoryTag> {

    private static final long serialVersionUID = 1L;

    public InventoryTagDBLayer(SOSHibernateSession session) {
        super(session);
    }

    @Override
    protected String getTagTable() {
        return DBLayer.DBITEM_INV_TAGS;
    }

    @Override
    protected String getTaggingTable() {
        return DBLayer.DBITEM_INV_TAGGINGS;
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

    public List<InventoryTagItem> getFoldersByTagAndTypeForReleasedObjects(Set<Integer> inventoryTypes) throws DBConnectionRefusedException,
            DBInvalidDataException {
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

    public List<DBItemInventoryTagging> getTaggingsByTagId(Long tagId) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_INV_TAGGINGS);
            sql.append(" where tagId=:tagId");

            Query<DBItemInventoryTagging> query = getSession().createQuery(sql.toString());
            query.setParameter("tagId", tagId);

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
            sql.append("select t.name from ").append(DBLayer.DBITEM_INV_TAGGINGS).append(" tg join ").append(DBLayer.DBITEM_INV_TAGS).append(
                    " t on t.id = tg.tagId");

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
            sql.append("select t.name from ").append(DBLayer.DBITEM_INV_TAGGINGS).append(" tg join ").append(DBLayer.DBITEM_INV_TAGS).append(
                    " t on t.id = tg.tagId");

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

    public List<DBItemInventoryTag> getTagDBItems(String name, Integer type) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select t from ").append(DBLayer.DBITEM_INV_TAGGINGS).append(" tg join ").append(DBLayer.DBITEM_INV_TAGS).append(
                    " t on t.id = tg.tagId");

            sql.append(" where tg.name=:name and tg.type=:type");

            Query<DBItemInventoryTag> query = getSession().createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("type", type);

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
    
    public Map<String, LinkedHashSet<String>> getWorkflowTags(Stream<String> workflowNames) {
        List<ResponseBaseSearchItem> result = getWorkflowTags(workflowNames.distinct().collect(Collectors.toList()));
        return result.stream().sorted(Comparator.comparingInt(ResponseBaseSearchItem::getOrdering)).collect(Collectors.groupingBy(
                ResponseBaseSearchItem::getPath, Collectors.mapping(ResponseBaseSearchItem::getName, Collectors.toCollection(LinkedHashSet::new))));
    }
    
    private List<ResponseBaseSearchItem> getWorkflowTags(List<String> workflowNames) {
        if (workflowNames == null || workflowNames.isEmpty()) {
            return Collections.emptyList();
        }
        if (workflowNames.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            List<ResponseBaseSearchItem> result = new ArrayList<>();
            for (int i = 0; i < workflowNames.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                result.addAll(getWorkflowTags(SOSHibernate.getInClausePartition(i, workflowNames)));
            }
            return result;
        } else {
            try {
                StringBuilder sql = new StringBuilder();
                sql.append("select ic.name as path, t.name as name, t.ordering as ordering from ");
                sql.append(DBLayer.DBITEM_INV_TAGGINGS).append(" tg left join ");
                sql.append(DBLayer.DBITEM_INV_TAGS).append(" t on t.id = tg.tagId left join ");
                sql.append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic on tg.cid = ic.id");

                sql.append(" where ic.name in (:workflowNames)");
                sql.append(" and tg.type=:type");

                Query<ResponseBaseSearchItem> query = getSession().createQuery(sql.toString(), ResponseBaseSearchItem.class);
                query.setParameterList("workflowNames", workflowNames);
                query.setParameter("type", ConfigurationType.WORKFLOW.intValue());

                List<ResponseBaseSearchItem> result = getSession().getResultList(query);
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

    public List<String> getWorkflowNamesHavingTags(List<String> tags) throws DBConnectionRefusedException, DBInvalidDataException {
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
