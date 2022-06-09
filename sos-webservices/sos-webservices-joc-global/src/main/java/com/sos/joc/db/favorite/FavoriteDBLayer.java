package com.sos.joc.db.favorite;

import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryFavorite;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.model.favorite.FavoriteIdentifier;
import com.sos.joc.model.favorite.FavoriteSharedIdentifier;
import com.sos.joc.model.favorite.FavoriteType;
import com.sos.joc.model.favorite.StoreFavorite;

public class FavoriteDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;
    private String account;

    public FavoriteDBLayer(SOSHibernateSession session, String account) {
        super(session);
        this.account = account;
    }
    
    public DBItemInventoryFavorite getFavorite(FavoriteIdentifier favorite) {
        return getFavorite(favorite.getName(), favorite.getType());
    }
    
    public DBItemInventoryFavorite getFavorite(String name, FavoriteType type) {
        return getFavorite(name, type.intValue());
    }
    
    public DBItemInventoryFavorite getFavorite(String name, Integer type) {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_FAVORITES);
            hql.append(" where account = :account");
            hql.append(" and name = :name");
            hql.append(" and type = :type");
            Query<DBItemInventoryFavorite> query = getSession().createQuery(hql.toString());
            query.setParameter("account", account);
            query.setParameter("name", name);
            query.setParameter("type", type);
            return getSession().getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public DBItemInventoryFavorite getSharedFavorite(FavoriteSharedIdentifier sharedFavorite) {
        try {
            if (sharedFavorite.getAccount() == account) {
                return null;
            }
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_FAVORITES);
            hql.append(" where account = :account");
            hql.append(" and name = :name");
            hql.append(" and type = :type");
            hql.append(" and shared = 1");
            Query<DBItemInventoryFavorite> query = getSession().createQuery(hql.toString());
            query.setParameter("account", sharedFavorite.getAccount());
            query.setParameter("name", sharedFavorite.getName());
            query.setParameter("type", sharedFavorite.getType().intValue());
            return getSession().getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public int deleteFavorite(FavoriteIdentifier favorite) {
        try {
            DBItemInventoryFavorite item = getFavorite(favorite);
            if (item != null) {
                getSession().delete(item);
                return 1;
            }
            return 0;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public Integer takeOverFavorite(FavoriteSharedIdentifier sharedFavorite, Date now, Integer ordering) {
        try {
            DBItemInventoryFavorite sharedItem = getSharedFavorite(sharedFavorite);
            DBItemInventoryFavorite item = getFavorite(sharedFavorite);
            boolean isNew = (item == null);
            if (isNew) {
                item = new DBItemInventoryFavorite();
                item.setAccount(account);
                item.setCreated(now);
                item.setId(null);
                item.setName(sharedItem.getName());
                item.setType(sharedItem.getType());
                item.setOrdering(ordering);
                item.setShared(false);
            }
            item.setModified(now);
            item.setFavorite(sharedItem.getFavorite());
            if (isNew) {
                getSession().save(item);
                ordering++;
            } else {
                getSession().update(item);
            }
            return ordering;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public Integer storeFavorite(StoreFavorite storeFavorite, Date now, Integer ordering) {
        try {
            DBItemInventoryFavorite item = getFavorite(storeFavorite.getName(), storeFavorite.getType());
            boolean isNew = (item == null);
            if (isNew) {
                item = new DBItemInventoryFavorite();
                item.setAccount(account);
                item.setCreated(now);
                item.setId(null);
                item.setName(storeFavorite.getName());
                item.setType(storeFavorite.getType().intValue());
                item.setOrdering(ordering);
                item.setShared(storeFavorite.getShared() == Boolean.TRUE);
            } else {
                if (storeFavorite.getShared() != null) {
                    item.setShared(storeFavorite.getShared());
                }
            }
            item.setModified(now);
            if (FavoriteType.AGENT.equals(storeFavorite.getType())) {
                item.setFavorite(null);
            } else {
                item.setFavorite(storeFavorite.getContent());
            }
            if (isNew) {
                getSession().save(item);
                ordering++;
            } else {
                getSession().update(item);
            }
            return ordering;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public int shareFavorite(FavoriteIdentifier favorite, Date now) {
        try {
            DBItemInventoryFavorite item = getFavorite(favorite);
            if (item != null && !item.getShared()) {
                item.setShared(true);
                item.setModified(now);
                getSession().update(item);
                return 1;
            }
            return 0;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public int unShareFavorite(FavoriteIdentifier favorite, Date now) {
        try {
            DBItemInventoryFavorite item = getFavorite(favorite);
            if (item != null && item.getShared()) {
                item.setShared(false);
                item.setModified(now);
                getSession().update(item);
                return 1;
            }
            return 0;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemInventoryFavorite> getFavorites(Set<FavoriteType> types, int limit) {
        try {
            int numOfAllTypes = EnumSet.allOf(FavoriteType.class).size();
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_FAVORITES);
            hql.append(" where account = :account");
            if (types != null && !types.isEmpty() && types.size() < numOfAllTypes) {
                if (types.size() == 1) {
                    hql.append(" and type = :type"); 
                } else {
                    hql.append(" and type in (:types)"); 
                }
            }
            hql.append(" order by ordering");
            Query<DBItemInventoryFavorite> query = getSession().createQuery(hql.toString());
            query.setParameter("account", account);
            if (limit > -1) {
                query.setMaxResults(limit);
            }
            if (types != null && !types.isEmpty() && types.size() < numOfAllTypes) {
                if (types.size() == 1) {
                    query.setParameter("type", types.iterator().next().intValue());
                } else {
                    query.setParameter("types", types.stream().map(FavoriteType::intValue).collect(Collectors.toSet()));
                }
            }
            List<DBItemInventoryFavorite> result = getSession().getResultList(query);
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
    
    public List<DBItemInventoryFavorite> getSharedFavorites(Set<FavoriteType> types, int limit) {
        try {
            int numOfAllTypes = EnumSet.allOf(FavoriteType.class).size();
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_FAVORITES);
            hql.append(" where account != :account");
            hql.append(" and shared = 1");
            if (types != null && !types.isEmpty() && types.size() < numOfAllTypes) {
                if (types.size() == 1) {
                    hql.append(" and type = :type"); 
                } else {
                    hql.append(" and type in (:types)"); 
                }
            }
            hql.append(" order by ordering");
            Query<DBItemInventoryFavorite> query = getSession().createQuery(hql.toString());
            query.setParameter("account", account);
            if (limit > -1) {
                query.setMaxResults(limit);
            }
            if (types != null && !types.isEmpty() && types.size() < numOfAllTypes) {
                if (types.size() == 1) {
                    query.setParameter("type", types.iterator().next().intValue());
                } else {
                    query.setParameter("types", types.stream().map(FavoriteType::intValue).collect(Collectors.toSet()));
                }
            }
            List<DBItemInventoryFavorite> result = getSession().getResultList(query);
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

    public int maxOrdering(FavoriteType type) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select max(ordering) from ").append(DBLayer.DBITEM_INV_FAVORITES);
        hql.append(" where account = :account");
        hql.append(" and type = :type");
        
        Query<Integer> query = getSession().createQuery(hql.toString());
        query.setParameter("account", account);
        query.setParameter("type", type.intValue());
        Integer result = getSession().getSingleResult(query);
        if (result == null) {
            return -1;
        }
        return result;
    }
    
    public void cleanupFavoritesOrdering(FavoriteType type, boolean force) throws SOSHibernateException {
        List<DBItemInventoryFavorite> dbFavoritesByType = getFavorites(Collections.singleton(type), -1);
        if (!force) {
            // looking for duplicate orderings
            force = dbFavoritesByType.stream().collect(Collectors.groupingBy(DBItemInventoryFavorite::getOrdering, Collectors.counting())).entrySet()
                    .stream().anyMatch(e -> e.getValue() > 1L);
        }
        if (force) {
            int position = 0;
            for (DBItemInventoryFavorite dbFavoriteByType : dbFavoritesByType) {
                if (dbFavoriteByType.getOrdering() != position) {
                    dbFavoriteByType.setOrdering(position);
                    getSession().update(dbFavoriteByType);
                }
                position++;
            }
        }
    }

    public void setFavoritesOrdering(String name, FavoriteType type, String predecessorName) throws SOSHibernateException {
        // TODO better with collect by prior
        DBItemInventoryFavorite favorite = getFavorite(name, type);
        if (favorite == null) {
            throw new DBMissingDataException("Favorite with name '" + name + "' and type '" + type.value() + "' doesn't exist.");
        }
        int newPosition = -1;
        DBItemInventoryFavorite predecessorFavorite = null;
        if (predecessorName != null && !predecessorName.isEmpty()) {
            if (name.equals(predecessorName)) {
                throw new DBInvalidDataException("Favorite name '" + name + "' and predecessor favorite name '"
                        + predecessorName + "' are the same.");
            }
            predecessorFavorite = getFavorite(predecessorName, type);
            if (predecessorFavorite == null) {
                throw new DBMissingDataException("Predecessor favorite with name '" + name + "' and type '" + type.value() + "' doesn't exist.");
            }
            newPosition = predecessorFavorite.getOrdering();
        }

        int oldPosition = favorite.getOrdering();
        newPosition++;
        favorite.setOrdering(newPosition);
        if (newPosition != oldPosition) {
            StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_INV_FAVORITES);
            if (newPosition < oldPosition) {
                hql.append(" set ordering = ordering + 1").append(" where ordering >= :newPosition and ordering < :oldPosition");
            } else {
                hql.append(" set ordering = ordering - 1").append(" where ordering > :oldPosition and ordering <= :newPosition");
            }
            hql.append(" and type = :type");
            Query<?> query = getSession().createQuery(hql.toString());
            query.setParameter("newPosition", newPosition);
            query.setParameter("oldPosition", oldPosition);
            query.setParameter("type", type.intValue());

            getSession().executeUpdate(query);
            getSession().update(favorite);
        }
    }
}
