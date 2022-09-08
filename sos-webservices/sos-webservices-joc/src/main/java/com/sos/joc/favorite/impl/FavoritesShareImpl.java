package com.sos.joc.favorite.impl;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.favorite.FavoriteDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.favorite.resource.IFavoritesDelete;
import com.sos.joc.favorite.resource.IFavoritesShare;
import com.sos.joc.model.favorite.FavoriteIdentifiers;
import com.sos.schema.JsonValidator;

@Path("inventory/favorites")
public class FavoritesShareImpl extends JOCResourceImpl implements IFavoritesShare {

    private static final String API_CALL_SHARE = "./inventory/favorites/share";
    private static final String API_CALL_UNSHARE = "./inventory/favorites/make_private";
    
    @Override
    public JOCDefaultResponse shareFavorites(String accessToken, byte[] filterBytes) {
        return shareFavorites(accessToken, filterBytes, API_CALL_SHARE);
    }
    
    @Override
    public JOCDefaultResponse unshareFavorites(String accessToken, byte[] filterBytes) {
        return shareFavorites(accessToken, filterBytes, API_CALL_UNSHARE);
    }

    public JOCDefaultResponse shareFavorites(String accessToken, byte[] filterBytes, String apiCall) {

        SOSHibernateSession connection = null;
        try {
            initLogging(apiCall, filterBytes, accessToken);
            JsonValidator.validate(filterBytes, FavoriteIdentifiers.class);
            FavoriteIdentifiers favorites = Globals.objectMapper.readValue(filterBytes, FavoriteIdentifiers.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            String account = getAccount();
            connection = Globals.createSosHibernateStatelessConnection(apiCall);
            connection.setAutoCommit(false);
            connection.beginTransaction();
            FavoriteDBLayer dbLayer = new FavoriteDBLayer(connection, account);
            Date now = Date.from(Instant.now());
            if (apiCall.equals(API_CALL_SHARE)) {
                favorites.getFavoriteIds().stream().forEach(f -> dbLayer.shareFavorite(f, now));
            } else {
                favorites.getFavoriteIds().stream().forEach(f -> dbLayer.unShareFavorite(f, now));
            }
            Globals.commit(connection);
            
            return JOCDefaultResponse.responseStatusJSOk(now);
        } catch (JocException e) {
            Globals.rollback(connection);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(connection);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }

}
