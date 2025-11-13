package com.sos.joc.db.inventory;

import java.util.Optional;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.DBLayer;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.note.common.NoteIdentifier;

public class InventoryNotesDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;

    public InventoryNotesDBLayer(SOSHibernateSession session) {
        super(session);
    }
    
    public DBItemInventoryNote getNote(NoteIdentifier note) {
        return getNote(note.getName(), note.getObjectType().intValue());
    }
    
    public Long getConfigurationId(NoteIdentifier note) {
        return getConfigurationId(note.getName(), note.getObjectType().intValue());
    }
    
    public boolean hasNote(Long configurationId) {
        try {
            StringBuilder hql = new StringBuilder("select count(id) from ").append(DBLayer.DBITEM_INV_NOTES).append(" where cId=:cId");
            Query<Long> query = getSession().createQuery(hql.toString());
            query.setParameter("cId", configurationId);
            return Optional.ofNullable(getSession().getSingleResult(query)).map(count -> count > 0).orElse(false);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    private DBItemInventoryNote getNote(String name, Integer type) {
        try {
            boolean isCalendar = JocInventory.isCalendar(type);
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_NOTES).append(" in left join ");
            hql.append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic on in.cid=ic.id");
            hql.append(" where ic.name=:name");
            if (isCalendar) {
                hql.append(" and ic.type in (:types)");
            } else {
                hql.append(" and ic.type=:type");
            }
            Query<DBItemInventoryNote> query = getSession().createQuery(hql.toString());
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
    
    private Long getConfigurationId(String name, Integer type) {
        try {
            boolean isCalendar = JocInventory.isCalendar(type);
            StringBuilder hql = new StringBuilder("select id from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            hql.append(" where name=:name");
            if (isCalendar) {
                hql.append(" and type in (:types)");
            } else {
                hql.append(" and type=:type");
            }
            Query<Long> query = getSession().createQuery(hql.toString());
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

}
