package com.sos.joc.db.inventory;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.items.InventoryHasNoteItem;
import com.sos.joc.db.inventory.items.InventoryNoteItem;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.note.common.HasNote;
import com.sos.joc.model.note.common.NoteIdentifier;
import com.sos.joc.model.note.common.Severity;

public class InventoryNotesDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;

    public InventoryNotesDBLayer(SOSHibernateSession session) {
        super(session);
    }
    
    public InventoryNoteItem getInvItem(NoteIdentifier note) {
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
    
    public HasNote hasNote(Long configurationId, String accountName) {
        if (configurationId == null) {
            return null;
        }
        try {
            if (accountName == null) {
                Severity color = Severity.fromValueOrNull(hasNote(configurationId));
                if (color == null) {
                    return null;
                }
                HasNote hn = new HasNote();
                hn.setSeverity(color);
                return hn;
            } else {
                StringBuilder hql = new StringBuilder("select ");
                hql.append("n.severity as color, ");
                hql.append("(case when nn.accountName=:accountName then true else false end) as notified from ");
                hql.append(DBLayer.DBITEM_INV_NOTES).append(" n left join ");
                hql.append(DBLayer.DBITEM_INV_NOTE_NOTIFICATIONS).append(" nn on n.cid=nn.cid ");
                hql.append("where n.cid=:cid group by n.severity, notified");

                Query<InventoryHasNoteItem> query = getSession().createQuery(hql.toString(), InventoryHasNoteItem.class);
                query.setParameter("cid", configurationId);
                query.setParameter("accountName", accountName);
                List<InventoryHasNoteItem> result = getSession().getResultList(query);

                return getHasNote(result);
            }
        } catch (Exception ex) {
            return null;
        }
    }
    
    private Integer hasNote(Long configurationId) {
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
    
    private static HasNote getHasNote(List<InventoryHasNoteItem> result) {
        if (result == null || result.isEmpty()) {
            return null;
        }
        return result.stream().map(HasNote.class::cast).sorted(Comparator.comparing(HasNote::getNotified).reversed()).findFirst().map(hn -> {
            hn.setNotified(hn.getNotified() ? true : null);
            return hn;
        }).get();
    }
    
//    public Map<String, Integer> hasNote(Integer type) {
//        try {
//            StringBuilder hql = new StringBuilder("select c.name, n.severity from ").append(DBLayer.DBITEM_INV_NOTES).append(" n left join ")
//                    .append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" c on n.cid=c.id where c.type=:type");
//            Query<Object[]> query = getSession().createQuery(hql.toString());
//            query.setParameter("type", type);
//            List<Object[]> result = getSession().getResultList(query);
//            if (result != null) {
//                return result.stream().collect(Collectors.toMap(i -> (String) i[0], i -> (Integer) i[1], (k1, k2) -> k1));
//            }
//            return Collections.emptyMap();
//        } catch (Exception ex) {
//            return Collections.emptyMap();
//        }
//    }
    
    public Map<String, HasNote> hasNote(Integer type, String accountName) {
        try {
            StringBuilder hql = new StringBuilder("select ");
            hql.append("c.name as objectName, ");
            hql.append("n.severity as color, ");
            hql.append("(case when nn.accountName=:accountName then true else false end) as notified from ");
            
            hql.append(DBLayer.DBITEM_INV_NOTES).append(" n left join ");
            hql.append(DBLayer.DBITEM_INV_NOTE_NOTIFICATIONS).append(" nn on n.cid=nn.cid left join ");
            hql.append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" c on n.cid=c.id ");
            hql.append("where c.type=:type group by c.name, n.severity, notified");
            
            Query<InventoryHasNoteItem> query = getSession().createQuery(hql.toString(), InventoryHasNoteItem.class);
            query.setParameter("type", type);
            query.setParameter("accountName", accountName);
            List<InventoryHasNoteItem> result = getSession().getResultList(query);
            if (result != null) {
                return result.stream().collect(Collectors.groupingBy(InventoryHasNoteItem::getObjectName, 
                        Collectors.collectingAndThen(Collectors.toList(), InventoryNotesDBLayer::getHasNote)));
            }
            return Collections.emptyMap();
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }
    
    private InventoryNoteItem getInvItem(String name, Integer type) {
        try {
            boolean isCalendar = JocInventory.isCalendar(type);
            StringBuilder hql = new StringBuilder("select id as noteId, path as path, folder as folder from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            hql.append(" where name=:name");
            if (isCalendar) {
                hql.append(" and type in (:types)");
            } else {
                hql.append(" and type=:type");
            }
            Query<InventoryNoteItem> query = getSession().createQuery(hql.toString(), InventoryNoteItem.class);
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
        if (getSession().isTransactionOpened()) {
            deleteNoteTransactional(configurationId);
        } else {
            DBItemInventoryNote note = getNote(configurationId);
            if (note != null) {
                getSession().delete(note);
            }
            List<DBItemInventoryNoteNotification> notifications = getNoteNotifications(configurationId);
            for (DBItemInventoryNoteNotification notification : notifications) {
                getSession().delete(notification);
            }
        }
    }
    
    private void deleteNoteTransactional(Long configurationId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_INV_NOTES).append(" where cid=:cid");
        Query<Integer> query = getSession().createQuery(hql.toString());
        query.setParameter("cid", configurationId);
        getSession().executeUpdate(query);
        
        deleteNoteNotificationsTransactional(configurationId);
    }
    
    public List<DBItemInventoryNoteNotification> getNoteNotifications(Long configurationId) {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_NOTE_NOTIFICATIONS).append(" where cid=:cid");
            Query<DBItemInventoryNoteNotification> query = getSession().createQuery(hql.toString());
            query.setParameter("cid", configurationId);
            List<DBItemInventoryNoteNotification> notifications = getSession().getResultList(query);
            if (notifications == null) {
                return Collections.emptyList();
            }
            return notifications;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    private void deleteNoteNotificationsTransactional(Long configurationId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_INV_NOTE_NOTIFICATIONS).append(" where cid=:cid");
        Query<Integer> query = getSession().createQuery(hql.toString());
        query.setParameter("cid", configurationId);
        getSession().executeUpdate(query);
    }
    
    public DBItemInventoryNoteNotification getNoteNotification(String accountName, Long configurationId) {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_NOTE_NOTIFICATIONS).append(
                    " where accountName=:accountName and cid=:cid");
            Query<DBItemInventoryNoteNotification> query = getSession().createQuery(hql.toString());
            query.setParameter("cid", configurationId);
            query.setParameter("accountName", accountName);
            return getSession().getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public Map<String, Long> getNumOfNoteNotificationsPerAccount(Collection<String> accountNames) {
        if (accountNames == null || accountNames.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            StringBuilder hql = new StringBuilder("select accountName, count(*) as numOf from ").append(DBLayer.DBITEM_INV_NOTE_NOTIFICATIONS).append(
                    " where accountName in (:accountNames) group by accountName");
            Query<Object[]> query = getSession().createQuery(hql.toString());
            query.setParameterList("accountNames", accountNames);
            List<Object[]> result = getSession().getResultList(query);
            if (result == null) {
                return Collections.emptyMap();
            }
            return result.stream().collect(Collectors.toMap(o -> (String) o[0], o -> (Long) o[1]));
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public Long getNumOfNoteNotifications(String accountName) {
        if (accountName == null || accountName.isEmpty()) {
            return 0L;
        }
        try {
            StringBuilder hql = new StringBuilder("select count(*) from ").append(DBLayer.DBITEM_INV_NOTE_NOTIFICATIONS).append(
                    " where accountName=:accountName");
            Query<Long> query = getSession().createQuery(hql.toString());
            query.setParameter("accountName", accountName);
            Long result = getSession().getSingleResult(query);
            if (result == null) {
                return 0L;
            }
            return result;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public List<InventoryNoteItem> getNoteNotifications(String accountName) {
        if (accountName == null || accountName.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            StringBuilder hql = new StringBuilder("select ic.path as path, ic.name as name, ic.type as type, n.id as noteId, n.severity as color from ");
            hql.append(DBLayer.DBITEM_INV_NOTE_NOTIFICATIONS).append(" nn left join ");
            hql.append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic on nn.cid=ic.id left join ");
            hql.append(DBLayer.DBITEM_INV_NOTES).append(" n on nn.cid=n.cid");
            hql.append(" order by n.modified");
            
            Query<InventoryNoteItem> query = getSession().createQuery(hql.toString(), InventoryNoteItem.class);
            List<InventoryNoteItem> result =  getSession().getResultList(query);
            
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
