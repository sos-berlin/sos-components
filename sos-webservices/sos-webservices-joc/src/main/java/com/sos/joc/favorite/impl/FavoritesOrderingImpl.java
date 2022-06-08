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
import com.sos.joc.favorite.resource.IFavoritesOrdering;
import com.sos.joc.model.favorite.OrderingFavorites;
import com.sos.schema.JsonValidator;

@Path("inventory/favorites")
public class FavoritesOrderingImpl extends JOCResourceImpl implements IFavoritesOrdering {

    private static final String API_CALL = "./inventory/favorites/ordering";

    @Override
    public JOCDefaultResponse ordering(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validate(filterBytes, OrderingFavorites.class);
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

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
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
