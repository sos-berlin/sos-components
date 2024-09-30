package com.sos.joc.db.inventory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.classes.tag.GroupedTag;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.common.ATagDBLayer;
import com.sos.joc.db.inventory.items.InventoryJobTagItem;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;

public class InventoryJobTagDBLayer extends ATagDBLayer<DBItemInventoryJobTag> {

    private static final long serialVersionUID = 1L;

    public InventoryJobTagDBLayer(SOSHibernateSession session) {
        super(session);
    }

    @Override
    protected String getTagTable() {
        return DBLayer.DBITEM_INV_JOB_TAGS;
    }

    @Override
    protected String getTaggingTable() {
        return DBLayer.DBITEM_INV_JOB_TAGGINGS;
    }
    
    public Set<DBItemInventoryJobTagging> getTaggings(Long cid) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(getTaggingTable());
            sql.append(" where cid=:cid");

            Query<DBItemInventoryJobTagging> query = getSession().createQuery(sql.toString());
            query.setParameter("cid", cid);

            List<DBItemInventoryJobTagging> result = getSession().getResultList(query);
            if (result == null) {
                return Collections.emptySet();
            }

            return result.stream().collect(Collectors.toSet());

        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public boolean hasTaggings(String workflowName) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select count(id) from ").append(getTaggingTable());
            sql.append(" where workflowName=:workflowName");

            Query<Long> query = getSession().createQuery(sql.toString());
            query.setParameter("workflowName", workflowName);

            return getSession().getSingleResult(query) > 0L;

        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public int delete(String workflowName) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("delete from ").append(getTaggingTable());
            sql.append(" where workflowName=:workflowName");

            Query<String> query = getSession().createQuery(sql.toString());
            query.setParameter("workflowName", workflowName);

            return getSession().executeUpdate(query);

        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
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
                StringBuilder hql = new StringBuilder("select tg.workflowName from ").append(DBLayer.DBITEM_INV_JOB_TAGGINGS).append(" tg ");
                hql.append(" left join ").append(DBLayer.DBITEM_INV_JOB_TAGS).append(" t on t.id=tg.tagId ");
                hql.append("where t.name in (:tags) ");
                hql.append("group by tg.workflowName");

                Query<String> query = getSession().createQuery(hql);
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
    
    public Set<Long> getWorkflowIdsHavingTags(List<String> tags) throws DBConnectionRefusedException, DBInvalidDataException {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptySet();
        }
        if (tags.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            Set<Long> result = new HashSet<>();
            for (int i = 0; i < tags.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                result.addAll(getWorkflowIdsHavingTags(SOSHibernate.getInClausePartition(i, tags)));
            }
            return result;
        } else {
            try {
                StringBuilder hql = new StringBuilder("select tg.cid from ").append(DBLayer.DBITEM_INV_JOB_TAGGINGS).append(" tg ");
                hql.append(" left join ").append(DBLayer.DBITEM_INV_JOB_TAGS).append(" t on t.id=tg.tagId ");
                hql.append("where t.name in (:tags) ");
                hql.append("group by tg.cid");

                Query<Long> query = getSession().createQuery(hql);
                query.setParameterList("tags", tags);

                List<Long> result = getSession().getResultList(query);
                if (result == null) {
                    return Collections.emptySet();
                }
                return result.stream().collect(Collectors.toSet());
            } catch (SOSHibernateInvalidSessionException ex) {
                throw new DBConnectionRefusedException(ex);
            } catch (Exception ex) {
                throw new DBInvalidDataException(ex);
            }
        }
    }
    
    public Set<Long> getWorkflowIdsHavingTagIds(List<Long> tagIds) throws DBConnectionRefusedException, DBInvalidDataException {
        if (tagIds == null || tagIds.isEmpty()) {
            return Collections.emptySet();
        }
        if (tagIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            Set<Long> result = new HashSet<>();
            for (int i = 0; i < tagIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                result.addAll(getWorkflowIdsHavingTagIds(SOSHibernate.getInClausePartition(i, tagIds)));
            }
            return result;
        } else {
            try {
                StringBuilder hql = new StringBuilder("select tg.cid from ").append(DBLayer.DBITEM_INV_JOB_TAGGINGS).append(" tg ");
                hql.append(" left join ").append(DBLayer.DBITEM_INV_JOB_TAGS).append(" t on t.id=tg.tagId ");
                hql.append("where t.id in (:tagIds) ");
                hql.append("group by tg.cid");

                Query<Long> query = getSession().createQuery(hql);
                query.setParameterList("tagIds", tagIds);

                List<Long> result = getSession().getResultList(query);
                if (result == null) {
                    return Collections.emptySet();
                }
                return result.stream().collect(Collectors.toSet());
            } catch (SOSHibernateInvalidSessionException ex) {
                throw new DBConnectionRefusedException(ex);
            } catch (Exception ex) {
                throw new DBInvalidDataException(ex);
            }
        }
    }
    
    public Map<String, LinkedHashSet<String>> getTagsWithGroups(Long cid, Collection<String> jobNames) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select g.name as group, tg.jobName as jobName, t.name as tagName, t.ordering as ordering from ")
                .append(getTaggingTable()).append(" tg left join ")
                .append(getTagTable()).append(" t on t.id = tg.tagId left join ")
                .append(DBLayer.DBITEM_INV_TAG_GROUPS).append(" g on g.id = t.groupId");
            
            sql.append(" where tg.cid=:cid");
            if (jobNames != null && !jobNames.isEmpty()) {
                if (jobNames.size() == 1) {
                    sql.append(" and tg.jobName=:jobName");
                } else {
                    sql.append(" and tg.jobName in (:jobNames)"); 
                }
            }

            Query<InventoryJobTagItem> query = getSession().createQuery(sql.toString(), InventoryJobTagItem.class);
            query.setParameter("cid", cid);
            if (jobNames != null && !jobNames.isEmpty()) {
                if (jobNames.size() == 1) {
                    query.setParameter("jobName", jobNames.iterator().next());
                } else {
                    query.setParameterList("jobNames", jobNames);
                }
            }

            List<InventoryJobTagItem> result = getSession().getResultList(query);
            if (result == null) {
                return Collections.emptyMap();
            }

            return result.stream().sorted(Comparator.comparingInt(InventoryJobTagItem::getOrdering)).collect(Collectors.groupingBy(
                    InventoryJobTagItem::getJobName, Collectors.mapping(InventoryJobTagItem::getGroupedTagName, Collectors.toCollection(
                            LinkedHashSet::new))));

        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public List<String> getTagsWithGroups(Long cid) {
        try {
            StringBuilder sql = new StringBuilder("select new ").append(GroupedTag.class.getName());
            sql.append("(g.name, t.name) from ").append(DBLayer.DBITEM_INV_TAGGINGS).append(" tg left join ")
                .append(DBLayer.DBITEM_INV_TAGS).append(" t on t.id = tg.tagId left join ")
                .append(DBLayer.DBITEM_INV_TAG_GROUPS).append(" g on g.id = t.groupId");

            sql.append(" where tg.cid=:cid");
            sql.append(" order by t.ordering");

            Query<GroupedTag> query = getSession().createQuery(sql.toString());
            query.setParameter("cid", cid);

            List<GroupedTag> result = getSession().getResultList(query);
            if (result == null) {
                return Collections.emptyList();
            }

            return result.stream().map(GroupedTag::toString).collect(Collectors.toList());

        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public void renameWorkflow(Long cid, String newWorkflowName) {
        renameWorkflow(cid, newWorkflowName, Date.from(Instant.now()));
    }
    
    public void renameWorkflow(Long cid, String newWorkflowName, Date now) {
        try {
            StringBuilder hql = new StringBuilder();
            hql.append("update " + getTaggingTable() + " set workflowName=:newWorkflowName, modified=:now");
            hql.append(" where cid=:cid");
            Query<?> query = getSession().createQuery(hql);
            query.setParameter("cid", cid);
            query.setParameter("newWorkflowName", newWorkflowName);
            query.setParameter("now", now);

            getSession().executeUpdate(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public void renameJob(String workflowName, String oldJobName, String newJobName) {
        renameJob(workflowName, oldJobName, newJobName, Date.from(Instant.now()));
    }
    
    public void renameJob(String workflowName, String oldJobName, String newJobName, Date now) {
        try {
            StringBuilder hql = new StringBuilder();
            hql.append("update " + getTaggingTable() + " set jobName=:newJobName, modified=:now");
            hql.append(" where workflowName=:workflowName and jobName=:oldJobName");
            Query<?> query = getSession().createQuery(hql);
            query.setParameter("workflowName", workflowName);
            query.setParameter("oldJobName", oldJobName);
            query.setParameter("newJobName", newJobName);
            query.setParameter("now", now);

            getSession().executeUpdate(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
}
