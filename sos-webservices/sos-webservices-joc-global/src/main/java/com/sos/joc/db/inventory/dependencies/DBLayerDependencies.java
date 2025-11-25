package com.sos.joc.db.inventory.dependencies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryDependency;
import com.sos.joc.db.inventory.DBItemInventoryExtendedDependency;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.dependencies.get.ResponseObject;

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
        return getReferencesDependencies(Collections.singletonList(id));
    }
    
    public List<DBItemInventoryDependency> getReferencesDependencies (List<Long> ids) {
        if (ids.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            List<DBItemInventoryDependency> result = new ArrayList<>();
            for (int i = 0; i < ids.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                result.addAll(getReferencesDependencies(SOSHibernate.getInClausePartition(i, ids)));
            }
            return result;
        } else {
            try {
                StringBuilder hql = new StringBuilder(" from ").append(DBLayer.DBITEM_INV_DEPENDENCIES);
                hql.append(" where invDependencyId in (:invIds)");
                Query<DBItemInventoryDependency> query = getSession().createQuery(hql.toString());
                query.setParameter("invIds", ids);
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
    }
    
    public List<DBItemInventoryDependency> getReferencedByDependencies (Long id) {
        return getReferencedByDependencies(Collections.singletonList(id));
    }

    public List<DBItemInventoryDependency> getReferencedByDependencies (List<Long> ids) {
        if (ids.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            List<DBItemInventoryDependency> result = new ArrayList<>();
            for (int i = 0; i < ids.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                result.addAll(getReferencedByDependencies(SOSHibernate.getInClausePartition(i, ids)));
            }
            return result;
        } else {
            try {
                StringBuilder hql = new StringBuilder(" from ").append(DBLayer.DBITEM_INV_DEPENDENCIES);
                hql.append(" where invId in (:invIds)");
                Query<DBItemInventoryDependency> query = getSession().createQuery(hql.toString());
                query.setParameter("invIds", ids);
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
    
    public Optional<ResponseObject> getResponseObject(String name, ConfigurationType type) {
        try {
            StringBuilder hql = new StringBuilder("select id as id, path as path, deployed as deployed, released as released, valid as valid from ")
                    .append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" where name=:name and type=:type");
            Query<ResponseObject> query = getSession().createQuery(hql.toString(), ResponseObject.class);
            query.setParameter("name", name);
            query.setParameter("type", type.intValue());
            query.setMaxResults(1);
            ResponseObject result = query.getSingleResult();
            if (result != null) {
                result.setObjectType(type);
                result.setName(name);
            }
            return Optional.ofNullable(result);
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e); 
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
    
    public void updateEnforce(List<Long> invIds) throws SOSHibernateException {
        if (invIds == null || invIds.isEmpty()) {
            return;
        }
        /*
         * rename o1:
         *   object o1 refencedby object o2 -> invEnforce = true, depEnforced = true iff o2 is released/deployed
         * */
        List<DBItemInventoryDependency> result = getReferencedByDependencies(invIds);
        result.forEach(dependency -> {
            dependency.setInvEnforce(true);
            boolean isPublished = false;
            try {
                InventoryDBLayer invDbLayer = new InventoryDBLayer(getSession());
                if(JocInventory.isDeployable(dependency.getDependencyTypeAsEnum())) {
                    isPublished = invDbLayer.isDeployed(dependency.getInvDependencyId());
                } else if (JocInventory.isReleasable(dependency.getDependencyTypeAsEnum())) {
                    isPublished = invDbLayer.isReleased(dependency.getInvDependencyId());
                }
                if(isPublished) {
                    dependency.setDepEnforce(true);
                } else {
                    dependency.setDepEnforce(false);
                }
                getSession().update(dependency);
            } catch (SOSHibernateException e) {
                throw new JocSosHibernateException(e);
            }
        });
//        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_INV_DEPENDENCIES).append(" set invEnforce=true, depEnforce=true where ");
//        if (invIds.size() == 1) {
//            hql.append("invId=:invId ");
//        } else {
//            hql.append("invId in (:invIds) ");
//        }
//        hql.append("and invEnforce=false");
//        hql.append("or depEnforce=false");
//        Query<?> query = getSession().createQuery(hql);
//        if (invIds.size() == 1) {
//            query.setParameter("invId", invIds.iterator().next());
//        } else {
//            query.setParameterList("invIds", invIds);
//        }
//        return getSession().executeUpdate(query);
    }
    
}
