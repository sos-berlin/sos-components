package com.sos.joc.favorite.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.favorite.FavoriteDBLayer;
import com.sos.joc.favorite.resource.IFavoritesOrdering;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.favorite.OrderingFavorites;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("inventory/favorites")
public class FavoritesOrderingImpl extends JOCResourceImpl implements IFavoritesOrdering {

    private static final String API_CALL = "./inventory/favorites/ordering";

    @Override
    public JOCDefaultResponse ordering(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken, CategoryType.INVENTORY);
            JsonValidator.validateFailFast(filterBytes, OrderingFavorites.class);
            OrderingFavorites orderingParam = Globals.objectMapper.readValue(filterBytes, OrderingFavorites.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            String account = getAccount();
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            connection.setAutoCommit(false);
            Globals.beginTransaction(connection);
            FavoriteDBLayer dbLayer = new FavoriteDBLayer(connection, account);
            dbLayer.cleanupFavoritesOrdering(orderingParam.getType(), false);
            dbLayer.setFavoritesOrdering(orderingParam.getName(), orderingParam.getType(), orderingParam.getPredecessorName());
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
