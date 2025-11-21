package com.sos.joc.db.inventory.dependencies;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryDependency;
import com.sos.joc.db.inventory.DBItemInventoryExtendedDependency;
import com.sos.joc.exceptions.JocSosHibernateException;

public class DBLayerDependencies extends DBLayer {

    private static final long serialVersionUID = 1L;
    private final SOSHibernateSession session;

    public DBLayerDependencies(SOSHibernateSession session) {
        this.session = session;
    }

    public SOSHibernateSession getSession() {
        return session;
    }
    
    public List<DBItemInventoryDependency> getAllDependencies () throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ").append(DBLayer.DBITEM_INV_DEPENDENCIES);
        Query<DBItemInventoryDependency> query = getSession().createQuery(hql.toString());
        List<DBItemInventoryDependency> results = query.getResultList();
        if(results != null) {
            return results;
        } else {
            return Collections.emptyList();
        }
    }
    
    public List<DBItemInventoryExtendedDependency> getAllExtendedDependencies () throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ").append(DBLayer.DBITEM_INV_EXTENDED_DEPENDENCIES);
        Query<DBItemInventoryExtendedDependency> query = getSession().createQuery(hql.toString());
        List<DBItemInventoryExtendedDependency> results = query.getResultList();
        if(results != null) {
            return results;
        } else {
            return Collections.emptyList();
        }
    }
    
    public List<DBItemInventoryDependency> getDependencies (DBItemInventoryConfiguration item) throws SOSHibernateException {
        return getDependencies(item.getId());
    }
    
    public List<DBItemInventoryDependency> getDependencies (Long id) {
        try {
            StringBuilder hql = new StringBuilder(" from ").append(DBLayer.DBITEM_INV_DEPENDENCIES);
            hql.append(" where invDependencyId = :invId or invId = :invId");
            Query<DBItemInventoryDependency> query = getSession().createQuery(hql.toString());
            query.setParameter("invId", id);
            List<DBItemInventoryDependency> results = query.getResultList();
            if(results != null) {
                return results;
            } else {
                return Collections.emptyList();
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }
    
    public List<DBItemInventoryDependency> getReferencesDependencies (Long id) {
        try {
            StringBuilder hql = new StringBuilder(" from ").append(DBLayer.DBITEM_INV_DEPENDENCIES);
            hql.append(" where invDependencyId = :invId");
            Query<DBItemInventoryDependency> query = getSession().createQuery(hql.toString());
            query.setParameter("invId", id);
            List<DBItemInventoryDependency> results = query.getResultList();
            if(results != null) {
                return results;
            } else {
                return Collections.emptyList();
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }
    
    public List<DBItemInventoryDependency> getReferencedByDependencies (Long id) {
        try {
            StringBuilder hql = new StringBuilder(" from ").append(DBLayer.DBITEM_INV_DEPENDENCIES);
            hql.append(" where invId = :invId");
            Query<DBItemInventoryDependency> query = getSession().createQuery(hql.toString());
            query.setParameter("invId", id);
            List<DBItemInventoryDependency> results = query.getResultList();
            if(results != null) {
                return results;
            } else {
                return Collections.emptyList();
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public List<DBItemInventoryDependency> getRequestedDependencies (DBItemInventoryConfiguration item) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ").append(DBLayer.DBITEM_INV_DEPENDENCIES);
        hql.append(" where invId = :invId or invDependencyId = :invId");
        Query<DBItemInventoryDependency> query = getSession().createQuery(hql.toString());
        query.setParameter("invId", item.getId());
        List<DBItemInventoryDependency> results = query.getResultList();
        if(results != null) {
            return results;
        } else {
            return Collections.emptyList();
        }
    }
    
    public Set<Long> getReferencesByIds (Long dependencyId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select invDependencyId from ").append(DBLayer.DBITEM_INV_DEPENDENCIES);
        hql.append(" where invId = :dependencyId");
        Query<Long> query = getSession().createQuery(hql.toString());
        query.setParameter("dependencyId", dependencyId);
        List<Long> results = query.getResultList();
        if(results != null) {
            return new HashSet<Long>(results);
        } else {
            return Collections.emptySet();
        }
    }
    
    public Set<Long> getReferencesIds (Long dependencyId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select invId from ").append(DBLayer.DBITEM_INV_DEPENDENCIES);
        hql.append(" where invDependencyId = :dependencyId");
        Query<Long> query = getSession().createQuery(hql.toString());
        query.setParameter("dependencyId", dependencyId);
        List<Long> results = query.getResultList();
        if(results != null) {
            return  new HashSet<Long>(results);
        } else {
            return Collections.emptySet();
        }
    }
    
    public List<DBItemInventoryConfiguration> getReferencesInventoryItem (Long dependencyId) throws SOSHibernateException {
            StringBuilder hql = new StringBuilder(" from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" as con ");
            hql.append(" where con.id in (");
            hql.append("select dep.invId from ").append(DBLayer.DBITEM_INV_DEPENDENCIES).append(" as dep");
            hql.append(" where dep.invDependencyId = :invDepId");
            hql.append(")");
            Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
            query.setParameter("invDepId", dependencyId);
            List<DBItemInventoryConfiguration> results = query.getResultList();
            if(results != null) {
                return results;
            } else {
                return Collections.emptyList();
            }
        }
    
    public void deleteDependencies (DBItemInventoryConfiguration item) throws SOSHibernateException {
        List<DBItemInventoryDependency> resultsFound = getDependencies(item);
        if(resultsFound != null) {
            resultsFound.stream().forEach(found -> {
                try {
                    getSession().delete(found);
                } catch (SOSHibernateException e) {
                    throw new JocSosHibernateException(e);
                }
            });
        }
    } 
    
    public void insertOrReplaceDependencies (DBItemInventoryConfiguration item, Set<DBItemInventoryDependency> dependencies)
            throws SOSHibernateException {
//            deleteDependencies(item);
        List<DBItemInventoryDependency> storedDependencies = getDependencies(item);
        for(DBItemInventoryDependency storedDependency : storedDependencies) {
            if (!dependencies.contains(storedDependency)) {
                getSession().delete(storedDependency);
            } else {
                // possibly check if items should be enforced
            }
        }
        dependencies.removeAll(storedDependencies);
        dependencies.stream().filter(dep -> dep.getInvDependencyId().equals(item.getId())|| dep.getInvId().equals(item.getId())).forEach(
                dependency -> {
                    try {
                        // check if relation has to be enforced 
                        // check if the invId item of this invDepId dependency is already deployed/released
//                        dependency.setEnforce(true);
                        
                        getSession().save(dependency);
                    } catch (SOSHibernateException e) {
                        throw new JocSosHibernateException(e);
                    }
                });
    }
    
    public List<DBItemInventoryConfiguration> getReferencesByDependencies(Collection<Long> referencesByIds) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where id in (:invDepId)");
        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameterList("invDepId", referencesByIds);
        List<DBItemInventoryConfiguration> results = query.getResultList();
        if(results != null) {
            return results;
        } else {
            return Collections.emptyList();
        }
    }

    public boolean checkDependenciesPresent() throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select count(*) from ").append(DBLayer.DBITEM_INV_DEPENDENCIES);
        Query<Long> query = getSession().createQuery(hql.toString());
        Long result = query.getSingleResult();
        if(result > 0) {
            return true;
        } else {
            return false;
        }
    }
    
    public int updateEnforce(Collection<Long> invIds) throws SOSHibernateException {
        if (invIds == null || invIds.isEmpty()) {
            return 0;
        }
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_INV_DEPENDENCIES).append(" set enforce=true where ");
        if (invIds.size() == 1) {
            hql.append("invId=:invId ");
        } else {
            hql.append("invId in (:invIds) ");
        }
        hql.append("and enforce=false");
        Query<?> query = getSession().createQuery(hql);
        if (invIds.size() == 1) {
            query.setParameter("invId", invIds.iterator().next());
        } else {
            query.setParameterList("invIds", invIds);
        }
        return getSession().executeUpdate(query);
    }
    
}
