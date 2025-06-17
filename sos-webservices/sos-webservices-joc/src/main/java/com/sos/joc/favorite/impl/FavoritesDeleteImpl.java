package com.sos.joc.favorite.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.favorite.FavoriteDBLayer;
import com.sos.joc.favorite.resource.IFavoritesDelete;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.favorite.FavoriteIdentifiers;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("inventory/favorites")
public class FavoritesDeleteImpl extends JOCResourceImpl implements IFavoritesDelete {

    private static final String API_CALL = "./inventory/favorites/delete";

    @Override
    public JOCDefaultResponse deleteFavorites(String accessToken, byte[] filterBytes) {

        SOSHibernateSession connection = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken, CategoryType.INVENTORY);
            JsonValidator.validateFailFast(filterBytes, FavoriteIdentifiers.class);
            FavoriteIdentifiers favorites = Globals.objectMapper.readValue(filterBytes, FavoriteIdentifiers.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            String account = getAccount();
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            connection.setAutoCommit(false);
            connection.beginTransaction();
            FavoriteDBLayer dbLayer = new FavoriteDBLayer(connection, account);
            
            favorites.getFavoriteIds().stream().forEach(dbLayer::deleteFavorite);
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
