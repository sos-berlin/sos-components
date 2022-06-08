package com.sos.joc.favorite.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.favorite.FavoriteDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.favorite.resource.IFavoritesTakeOver;
import com.sos.joc.model.favorite.FavoriteSharedIdentifier;
import com.sos.joc.model.favorite.FavoriteSharedIdentifiers;
import com.sos.schema.JsonValidator;

@Path("inventory/favorites")
public class FavoritesTakeOverImpl extends JOCResourceImpl implements IFavoritesTakeOver {

    private static final String API_CALL = "./inventory/favorites/take_over";

    @Override
    public JOCDefaultResponse takeOverFavorites(String accessToken, byte[] filterBytes) {

        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validate(filterBytes, FavoriteSharedIdentifiers.class);
            FavoriteSharedIdentifiers favorites = Globals.objectMapper.readValue(filterBytes, FavoriteSharedIdentifiers.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            String account = getAccount();
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            connection.setAutoCommit(false);
            connection.beginTransaction();
            FavoriteDBLayer dbLayer = new FavoriteDBLayer(connection, account);
            
            Date now = Date.from(Instant.now());
            int position = dbLayer.maxOrdering();
            for (FavoriteSharedIdentifier f : favorites.getSharedFavoriteIds()) {
                dbLayer.takeOverFavorite(f, now, ++position);
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
