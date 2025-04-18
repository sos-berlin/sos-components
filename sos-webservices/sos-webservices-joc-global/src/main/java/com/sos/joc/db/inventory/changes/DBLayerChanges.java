package com.sos.joc.db.inventory.changes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryChange;
import com.sos.joc.db.inventory.DBItemInventoryChangesMapping;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.model.inventory.changes.ShowChangesFilter;
import com.sos.joc.model.inventory.changes.common.ChangeItem;
import com.sos.joc.model.inventory.changes.common.ChangeState;

public class DBLayerChanges extends DBLayer {

    private static final long serialVersionUID = 1L;

    private final SOSHibernateSession session;
    private static final String CREATED_FROM_DATE = "created >= :createdFrom";
    private static final String CREATED_TO_DATE = "created < :createdTo";
    private static final String MODIFIED_FROM_DATE = "modified >= :modifiedFrom";
    private static final String MODIFIED_TO_DATE = "modified < :modifiedTo";
    private static final String CLOSED_FROM_DATE = "closed >= :closedFrom";
    private static final String CLOSED_TO_DATE = "closed < :closedTo";
    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerChanges.class);
    private static final Set<ChangeState> ALL_CHANGE_STATES = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList(ChangeState.OPEN, ChangeState.PUBLISHED, ChangeState.CLOSED)));
    private static final Set<Integer> ALL_CHANGE_STATE_INT_VALUES = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList(ChangeState.OPEN.intValue(), ChangeState.PUBLISHED.intValue(), ChangeState.CLOSED.intValue())));

    public DBLayerChanges(SOSHibernateSession session) {
        this.session = session;
    }

    public SOSHibernateSession getSession() {
        return session;
    }


    public void storeChange(DBItemInventoryChange change) {
        storeChanges(Arrays.asList(new DBItemInventoryChange[] {change}));
    }
    
    public void storeChanges(Collection<DBItemInventoryChange> changes) {
        changes.stream().forEach(item -> {
            try {
                if(item.getId() != null) {
                    DBItemInventoryChange change = null;
                    try {
                        change = getChange(item.getId());
                    } catch (SOSHibernateException e) {}
                    if(change != null) {
                        getSession().update(item);
                    } else {
                        getSession().save(item);
                    }
                } else {
                    getSession().save(item);
                }
            } catch (SOSHibernateException e) {
                throw new JocSosHibernateException(e);
            }
        });
    }
    
    public DBItemInventoryChange getChange(Long id) throws SOSHibernateException {
        return getSession().get(DBItemInventoryChange.class, id);
    }

    public DBItemInventoryChange getChange(String name) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ").append(DBItemInventoryChange.class.getName());
        hql.append(" where name = :name");
        Query<DBItemInventoryChange> query = getSession().createQuery(hql.toString());
        query.setParameter("name", name);
        return getSession().getSingleResult(query);
    }

    public List<DBItemInventoryChange> getChanges(ShowChangesFilter filter) throws SOSHibernateException {
        List<String> clause = new ArrayList<>();
        if(!filter.getNames().isEmpty()) {
            if(filter.getNames().size() == 1) {
                clause.add("name = :name");
            } else {
                clause.add("name in (:names)");
            }
        }
        if(filter.getStates() != null) {
            clause.add("state in (:states)");
        }
        if(filter.getOwner() != null) {
            clause.add("owner = :owner");
        }
        if(filter.getLastPublishedBy() != null) {
            clause.add("publishedBy = :publishedBy");
        }
        if(filter.getCreated() != null) {
            clause.add(CREATED_FROM_DATE);
            if(filter.getCreated().getTo() != null) {
                clause.add(CREATED_TO_DATE);
            }
        }
        if(filter.getModified() != null) {
            clause.add(MODIFIED_FROM_DATE);
            if(filter.getModified().getTo() != null) {
                clause.add(MODIFIED_TO_DATE);
            }
        }
        if(filter.getClosed() != null) {
            clause.add(CLOSED_FROM_DATE);
            if(filter.getClosed().getTo() != null) {
                clause.add(CLOSED_TO_DATE);
            }
        }
        StringBuilder hql = new StringBuilder(" from ").append(DBItemInventoryChange.class.getName())
                .append(clause.stream().collect(Collectors.joining(" and ", " where ", "")));
        Query<DBItemInventoryChange> query = getSession().createQuery(hql.toString());
        if(!filter.getNames().isEmpty()) {
            if(filter.getNames().size() == 1) {
                query.setParameter("name", filter.getNames().iterator().next());
            } else {
                query.setParameterList("names", filter.getNames());
            }
        }
        if(filter.getStates() != null) {
            if(!filter.getStates().isEmpty()) {
                query.setParameterList("states", filter.getStates().stream().map(item -> item.intValue()).collect(Collectors.toList()));
            } else {
                // if not set use all ChangeStates
                query.setParameterList("states", ALL_CHANGE_STATE_INT_VALUES);
            }
        }
        if(filter.getOwner() != null) {
            query.setParameter("owner", filter.getOwner());
        }
        if(filter.getLastPublishedBy() != null) {
            query.setParameter("publishedBy", filter.getLastPublishedBy());
        }
        if(filter.getCreated() != null) {
            query.setParameter("createdFrom", filter.getCreated().getFrom());
            if(filter.getCreated().getTo() != null) {
                query.setParameter("createdTo", filter.getCreated().getTo());
            }
        }
        if(filter.getModified() != null) {
            query.setParameter("modifiedFrom", filter.getModified().getFrom());
            if(filter.getModified().getTo() != null) {
                query.setParameter("modifiedTo", filter.getModified().getTo());
            }
        }
        if(filter.getClosed() != null) {
            query.setParameter("closedFrom", filter.getClosed().getFrom());
            if(filter.getClosed().getTo() != null) {
                query.setParameter("closedTo", filter.getClosed().getTo());
            }
        }
        List<DBItemInventoryChange> results = getSession().getResultList(query);
        if(results == null) {
            results = Collections.emptyList();
        }
        return results;
    }

    public List<ChangeItem> getChangeItems(Long changeId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select new ").append(ChangeItem.class.getName());
        hql.append("(name, path, type, valid, deployed, released) from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where id in ");
        hql.append("(");
        hql.append("  select map.invId from ").append(DBLayer.DBITEM_INV_CHANGES_MAPPINGS).append(" as map ");
        hql.append("  where map.changeId = :changeId");
        hql.append(") ");
        Query<ChangeItem> query = getSession().createQuery(hql.toString());
        query.setParameter("changeId", changeId);
        List<ChangeItem> results = getSession().getResultList(query);
        if(results == null) {
            results = Collections.emptyList();
        }
        return results;
    }
    
    public List<DBItemInventoryChangesMapping> getMappings(Long changeId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ").append(DBLayer.DBITEM_INV_CHANGES_MAPPINGS);
        hql.append(" where changeId = :changeId");
        Query<DBItemInventoryChangesMapping> query = getSession().createQuery(hql.toString());
        query.setParameter("changeId", changeId);
        List<DBItemInventoryChangesMapping> results = getSession().getResultList(query);
        if(results == null) {
            results = Collections.emptyList();
        }
        return results;
    }
    
    public DBItemInventoryChangesMapping getMapping(Long changeId, ChangeItem changeItem) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ").append(DBLayer.DBITEM_INV_CHANGES_MAPPINGS).append(" as mapping ");
        hql.append(" where mapping.changeId = :changeId ");
        hql.append(" and mapping.invId = (select cfg.id from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" as cfg ");
        hql.append(" where cfg.name = :name and cfg.type = :type").append(")");
        Query<DBItemInventoryChangesMapping> query = getSession().createQuery(hql.toString());
        query.setParameter("changeId", changeId);
        query.setParameter("name", changeItem.getName());
        query.setParameter("type", changeItem.getObjectType().intValue());
        return getSession().getSingleResult(query);
    }
    
    public DBItemInventoryChangesMapping getMapping(Long changeId, Long inventoryId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CHANGES_MAPPINGS);
        hql.append(" where changeId = :changeId and invId = : inventoryId");
        Query<DBItemInventoryChangesMapping> query = getSession().createQuery(hql.toString());
        query.setParameter("changeId", changeId);
        query.setParameter("inventoryId", inventoryId);
        return getSession().getSingleResult(query);
    }
}
