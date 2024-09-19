package com.sos.joc.db.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.common.ATagDBLayer;
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
    
    public int get(String workflowName) {
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
    
    public List<Long> getWorkflowIdsHavingTags(List<String> tags) throws DBConnectionRefusedException, DBInvalidDataException {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        if (tags.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            List<Long> result = new ArrayList<>();
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
}
