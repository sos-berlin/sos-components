package com.sos.joc.db.inventory;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.common.ATagDBLayer;
import com.sos.joc.event.bean.JOCEvent;
import com.sos.joc.event.bean.inventory.InventoryJobTagsEvent;
import com.sos.joc.event.bean.inventory.InventoryTagsEvent;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.inventory.search.ResponseBaseSearchItem;

public class InventoryTagGroupDBLayer extends ATagDBLayer<DBItemInventoryTagGroup> {

    private static final long serialVersionUID = 1L;

    public InventoryTagGroupDBLayer(SOSHibernateSession session) {
        super(session);
    }

    @Override
    protected String getTagTable() {
        return DBLayer.DBITEM_INV_TAG_GROUPS;
    }

    @Override
    protected String getTaggingTable() {
        return null;
    }
    
    public Integer deleteGroupIds(List<Long> groupIds, Set<String> tagNames, Set<JOCEvent> events) throws SOSHibernateException {
        if (groupIds == null || groupIds.isEmpty()) {
            return 0;
        }
        if (groupIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            int result = 0;
            for (int i = 0; i < groupIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                result += deleteGroupIds(SOSHibernate.getInClausePartition(i, groupIds), tagNames, events);
            }
            return result;
        } else {
            int i = 0;
            int i1 = deleteGroupIds(groupIds, tagNames, DBLayer.DBITEM_INV_TAGS);
            if (i1 > 0) {
                events.add(new InventoryTagsEvent());
                i = i + i1;
            }
            int i2 = deleteGroupIds(groupIds, tagNames, DBLayer.DBITEM_INV_JOB_TAGS);
            if (i2 > 0) {
                events.add(new InventoryJobTagsEvent());
                i = i + i2;
            }
            i = i + deleteGroupIds(groupIds, tagNames, DBLayer.DBITEM_INV_ORDER_TAGS);
            i = i + deleteGroupIds(groupIds, tagNames, DBLayer.DBITEM_HISTORY_ORDER_TAGS);
            return i;
        }
    }
    
    private Integer deleteGroupIds(List<Long> groupIds, Set<String> tagNames, String tablename) throws SOSHibernateException {
        if (tagNames != null && tagNames.isEmpty()) {
            return 0;
        }
        StringBuilder sql = new StringBuilder();
        sql.append("update ").append(tablename);
        sql.append(" set groupId = 0");
        if (groupIds.size() == 1) {
            sql.append(" where groupId = :groupId");
        } else {
            sql.append(" where groupId in (:groupIds)");
        }
        if (tagNames != null) {
            if (tagNames.size() == 1) {
                if (tablename.equals(DBLayer.DBITEM_HISTORY_ORDER_TAGS)) {
                    sql.append(" and tagName = :tagName");
                } else {
                    sql.append(" and name = :tagName");
                }
            } else {
                if (tablename.equals("DBItemHistoryOrderTag")) {
                    sql.append(" and tagName in (:tagNames)");
                } else {
                    sql.append(" and name in (:tagNames)");
                }
            }
        }
        Query<Integer> query = getSession().createQuery(sql.toString());
        if (groupIds.size() == 1) {
            query.setParameter("groupId", groupIds.iterator().next());
        } else {
            query.setParameterList("groupIds", groupIds);
        }
        if (tagNames != null) {
            if (tagNames.size() == 1) {
                query.setParameter("tagName", tagNames.iterator().next());
            } else {
                query.setParameterList("tagNames", tagNames);
            }
        }
        return getSession().executeUpdate(query);
    }
    
    public Set<String> getTagsByGroupId(Long groupId) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
//            sql.append("select tagName as name, ordering as ordering from ").append(DBLayer.DBITEM_HISTORY_ORDER_TAGS).append(
//                    " where groupId = :groupId");
//            sql.append(" union ");
            sql.append("select name as name, ordering as ordering from ").append(DBLayer.DBITEM_INV_ORDER_TAGS).append(" where groupId = :groupId");
            sql.append(" union ");
            sql.append("select name as name, ordering as ordering from ").append(DBLayer.DBITEM_INV_TAGS).append(" where groupId = :groupId");
            sql.append(" union ");
            sql.append("select name as name, ordering as ordering from ").append(DBLayer.DBITEM_INV_JOB_TAGS).append(" where groupId = :groupId");
            Query<ResponseBaseSearchItem> query = getSession().createQuery(sql.toString(), ResponseBaseSearchItem.class);
            query.setParameter("groupId", groupId);

            List<ResponseBaseSearchItem> result = getSession().getResultList(query);
            if (result == null) {
                return Collections.emptySet();
            }
            return result.stream().sorted(Comparator.comparingInt(ResponseBaseSearchItem::getOrdering)).map(ResponseBaseSearchItem::getName).collect(
                    Collectors.toCollection(LinkedHashSet::new));

        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
}
