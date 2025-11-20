package com.sos.joc.db.inventory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.items.InventorySearchItem;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.note.common.NoteIdentifier;

public class InventoryNotesDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;

    public InventoryNotesDBLayer(SOSHibernateSession session) {
        super(session);
    }
    
    public InventorySearchItem getInvItem(NoteIdentifier note) {
        return getInvItem(note.getName(), note.getObjectType().intValue());
    }
    
    public DBItemInventoryNote getNote(Long configurationId) {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_NOTES).append(" where cid=:cid");
            Query<DBItemInventoryNote> query = getSession().createQuery(hql.toString());
            query.setParameter("cid", configurationId);
            return getSession().getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public Integer hasNote(Long configurationId) {
        if (configurationId == null) {
            return null;
        }
        try {
            StringBuilder hql = new StringBuilder("select severity from ").append(DBLayer.DBITEM_INV_NOTES).append(" where cid=:cid");
            Query<Integer> query = getSession().createQuery(hql.toString());
            query.setParameter("cid", configurationId);
            return getSession().getSingleResult(query);
        } catch (Exception ex) {
            return null;
        }
    }
    
    public Map<String, Integer> hasNote(Integer type) {
        try {
            StringBuilder hql = new StringBuilder("select c.name, n.severity from ").append(DBLayer.DBITEM_INV_NOTES).append(" n left join ")
                    .append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" c on n.cid=c.id where c.type=:type");
            Query<Object[]> query = getSession().createQuery(hql.toString());
            query.setParameter("type", type);
            List<Object[]> result = getSession().getResultList(query);
            if (result != null) {
                return result.stream().collect(Collectors.toMap(i -> (String) i[0], i -> (Integer) i[1], (k1, k2) -> k1));
            }
            return Collections.emptyMap();
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }
    
    private InventorySearchItem getInvItem(String name, Integer type) {
        try {
            boolean isCalendar = JocInventory.isCalendar(type);
            StringBuilder hql = new StringBuilder("select id as id, path as path, folder as folder from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            hql.append(" where name=:name");
            if (isCalendar) {
                hql.append(" and type in (:types)");
            } else {
                hql.append(" and type=:type");
            }
            Query<InventorySearchItem> query = getSession().createQuery(hql.toString(), InventorySearchItem.class);
            query.setParameter("name", name);
            if (isCalendar) {
                query.setParameterList("types", JocInventory.getCalendarTypes());
                query.setMaxResults(1);
            } else {
                query.setParameter("type", type);
            }
            return getSession().getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public void deleteNote(Long configurationId) throws SOSHibernateException {
        DBItemInventoryNote note = null;
        if (getSession().isTransactionOpened()) {
            deleteNoteTransactional(configurationId);
        } else {
            note = getNote(configurationId);
            if (note != null) {
                getSession().delete(note);
            }
        }
    }
    
    private void deleteNoteTransactional(Long configurationId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_INV_NOTES).append(" where cid=:cId");
        Query<Integer> query = getSession().createQuery(hql.toString());
        query.setParameter("cId", configurationId);
        getSession().executeUpdate(query);
    }
}
