package com.sos.joc.db.inventory.dependencies;

import java.util.ArrayList;
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
    
    public void deleteDependencies (DBItemInventoryConfiguration item) {
        deleteDependencies(item.getId());
    } 
    
    public void deleteDependencies (Long id) {
        List<DBItemInventoryDependency> resultsFound;
        try {
            resultsFound = getDependencies(id);
        } catch (Exception e) {
            throw new JocSosHibernateException(e);
        }
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
        List<DBItemInventoryDependency> storedDependencies = getReferencesDependencies(item.getId());
        for(DBItemInventoryDependency storedDependency : storedDependencies) {
            if (!dependencies.contains(storedDependency)) {
                getSession().delete(storedDependency);
            }
        }
        InventoryDBLayer dbLayer = new InventoryDBLayer(getSession());
        dependencies.stream().filter(dep -> dep.getInvDependencyId().equals(item.getId()))
                .forEach(dependency -> {
                    try {
                        /*
                         * store o2:
                         * object o1 reference dby object o2 -> invEnforce = false iff o1 is released/deployed, depEnforced = false 
                         * */
                        dependency.setDepEnforce(false);
                        if(isPublished(dbLayer, dependency.getInvId(), dependency.getInvType()) 
                                || ConfigurationType.JOBTEMPLATE.equals(item.getTypeAsEnum())) {
                            dependency.setInvEnforce(false);
                        } else {
                            dependency.setInvEnforce(true);
                        }
                        if(storedDependencies.contains(dependency)) {
                            getSession().update(dependency);
                        } else {
                            getSession().save(dependency);
                        }
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
         *   object o1 referenced by object o2 -> invEnforce = true, depEnforced = true iff o2 is released/deployed
         * */
        List<DBItemInventoryDependency> result = getReferencedByDependencies(invIds);
        result.forEach(dependency -> {
            dependency.setInvEnforce(true);
            try {
                InventoryDBLayer invDbLayer = new InventoryDBLayer(getSession());
                if(isPublished(invDbLayer, dependency.getInvDependencyId(), dependency.getDependencyTypeAsEnum())) {
                    dependency.setDepEnforce(true);
                } else {
                    dependency.setDepEnforce(false);
                }
                getSession().update(dependency);
            } catch (SOSHibernateException e) {
                throw new JocSosHibernateException(e);
            }
        });
    }
    
    private static boolean isPublished(InventoryDBLayer invDbLayer, Long id, ConfigurationType type) throws SOSHibernateException {
        if(JocInventory.isDeployable(type)) {
            return invDbLayer.isDeployed(id);
        } else if (JocInventory.isReleasable(type)) {
            return invDbLayer.isReleased(id);
        }
        return false;
    }

    public Set<Long> checkPublished(List<Long> invIds) throws SOSHibernateException {
        if (invIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            Set<Long> result = new HashSet<>();
            for (int i = 0; i < invIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                result.addAll(checkPublished(SOSHibernate.getInClausePartition(i, invIds)));
            }
            return result;
        } else {
            StringBuilder sql = new StringBuilder("select inventoryConfigurationId from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS);
            sql.append(" where inventoryConfigurationId in (:ids)");
            Query<Long> query = getSession().createQuery(sql.toString());
            query.setParameter("ids", invIds);
            List<Long> deployedResults = query.getResultList();
            if (deployedResults == null) {
                deployedResults = Collections.emptyList();
            }
            sql = new StringBuilder("select cid from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
            sql.append(" where cid in (:ids)");
            query = getSession().createQuery(sql.toString());
            query.setParameter("ids", invIds);
            List<Long> releasedResults = query.getResultList();
            if (releasedResults == null) {
                releasedResults = Collections.emptyList();
            }
            Set<Long> publishedIds = new HashSet<Long>();
            publishedIds.addAll(deployedResults);
            publishedIds.addAll(releasedResults);
            return publishedIds;
        }
    }

}
