package com.sos.joc.favorite.impl;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.favorite.FavoriteDBLayer;
import com.sos.joc.db.inventory.DBItemInventoryFavorite;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;
import com.sos.joc.favorite.resource.IFavoritesStore;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.favorite.FavoriteType;
import com.sos.joc.model.favorite.RenameFavorite;
import com.sos.joc.model.favorite.RenameFavorites;
import com.sos.joc.model.favorite.StoreFavorite;
import com.sos.joc.model.favorite.StoreFavorites;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("inventory/favorites")
public class FavoritesStoreImpl extends JOCResourceImpl implements IFavoritesStore {

    private static final String API_CALL_STORE = "./inventory/favorites/store";
    private static final String API_CALL_RENAME = "./inventory/favorites/rename";

    @Override
    public JOCDefaultResponse storeFavorites(String accessToken, byte[] filterBytes) {

        SOSHibernateSession connection = null;
        try {
            filterBytes = initLogging(API_CALL_STORE, filterBytes, accessToken, CategoryType.SETTINGS);
            JsonValidator.validateFailFast(filterBytes, StoreFavorites.class);
            StoreFavorites favorites = Globals.objectMapper.readValue(filterBytes, StoreFavorites.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            String account = getAccount();
            connection = Globals.createSosHibernateStatelessConnection(API_CALL_STORE);
            connection.setAutoCommit(false);
            connection.beginTransaction();
            FavoriteDBLayer dbLayer = new FavoriteDBLayer(connection, account);
            
            Map<FavoriteType, Set<StoreFavorite>> favoritesMap = favorites.getFavorites().stream().collect(Collectors.groupingBy(
                    StoreFavorite::getType, Collectors.toSet()));
            Date now = Date.from(Instant.now());
            for (Map.Entry<FavoriteType, Set<StoreFavorite>> entry : favoritesMap.entrySet()) {
                int position = dbLayer.maxOrdering(entry.getKey());
                position++;
                for (StoreFavorite f : entry.getValue()) {
                    position = dbLayer.storeFavorite(f, now, position);
                }
            }
            Globals.commit(connection);
            
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            Globals.rollback(connection);
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    @Override
    public JOCDefaultResponse renameFavorites(String accessToken, byte[] filterBytes) {

        SOSHibernateSession connection = null;
        try {
            filterBytes = initLogging(API_CALL_RENAME, filterBytes, accessToken, CategoryType.INVENTORY);
            JsonValidator.validate(filterBytes, RenameFavorites.class);
            RenameFavorites favorites = Globals.objectMapper.readValue(filterBytes, RenameFavorites.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            String account = getAccount();
            connection = Globals.createSosHibernateStatelessConnection(API_CALL_RENAME);
            connection.setAutoCommit(false);
            connection.beginTransaction();
            FavoriteDBLayer dbLayer = new FavoriteDBLayer(connection, account);
            
            for (RenameFavorite favorite : favorites.getFavoriteIds()) {
                DBItemInventoryFavorite dbOldItem = dbLayer.getFavorite(favorite.getOldName(), favorite.getType());
                if (dbOldItem == null) {
                    throw new DBMissingDataException("Favorit (" + favorite.getOldName() + "/" + favorite.getType().value() + ") doesn't exist.");
                }
                DBItemInventoryFavorite dbNewItem = dbLayer.getFavorite(favorite);
                if (dbNewItem != null) {
                    throw new JocObjectAlreadyExistException("Favorit (" + favorite.getName() + "/" + favorite.getType().value()
                            + ") already exist.");
                }
                dbOldItem.setName(favorite.getName());
                connection.update(dbOldItem);
            }
            
            Globals.commit(connection);
            
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            Globals.rollback(connection);
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(connection);
        }
    }

}
