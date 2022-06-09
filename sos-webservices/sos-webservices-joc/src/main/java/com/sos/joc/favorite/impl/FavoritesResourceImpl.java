package com.sos.joc.favorite.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.favorite.FavoriteDBLayer;
import com.sos.joc.db.inventory.DBItemInventoryFavorite;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.favorite.resource.IFavoritesResource;
import com.sos.joc.model.favorite.Favorite;
import com.sos.joc.model.favorite.FavoriteIdentifier;
import com.sos.joc.model.favorite.FavoriteType;
import com.sos.joc.model.favorite.Favorites;
import com.sos.joc.model.favorite.ReadFavoritesFilter;
import com.sos.schema.JsonValidator;

@Path("inventory/favorites")
public class FavoritesResourceImpl extends JOCResourceImpl implements IFavoritesResource {

    private static final String API_CALL = "./inventory/favorites";

    @Override
    public JOCDefaultResponse postFavorites(String accessToken, byte[] filterBytes) {

        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validate(filterBytes, ReadFavoritesFilter.class);
            ReadFavoritesFilter favoritesFilter = Globals.objectMapper.readValue(filterBytes, ReadFavoritesFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            String account = getAccount();
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            FavoriteDBLayer dbLayer = new FavoriteDBLayer(connection, account);

            List<Favorite> ownFavs = new ArrayList<>();
            List<Favorite> sharedFavs = new ArrayList<>();

            if (favoritesFilter.getFavoriteIds() != null && !favoritesFilter.getFavoriteIds().isEmpty()) {
                Map<FavoriteType, Set<String>> favoriteMap = favoritesFilter.getFavoriteIds().stream().collect(Collectors.groupingBy(
                        FavoriteIdentifier::getType, Collectors.mapping(FavoriteIdentifier::getName, Collectors.toSet())));
                for (Map.Entry<FavoriteType, Set<String>> f : favoriteMap.entrySet()) {
                    if (!favoritesFilter.getOnlyShared()) {
                        int position = 0;
                        for (DBItemInventoryFavorite dbItem : dbLayer.getFavorites(Collections.singleton(f.getKey()), -1)) {
                            if (!f.getValue().contains(dbItem.getName())) {
                                continue;
                            }
                            ownFavs.add(mapDbItem(dbItem, ++position));
                        }
                    }
                    if (favoritesFilter.getWithShared()) {
                        int position = 0;
                        for (DBItemInventoryFavorite dbItem : dbLayer.getSharedFavorites(Collections.singleton(f.getKey()), -1)) {
                            if (!f.getValue().contains(dbItem.getName())) {
                                continue;
                            }
                            sharedFavs.add(mapDbItem(dbItem, ++position));
                        }
                    }
                }
            } else {
                Set<FavoriteType> types = Collections.emptySet();
                if (favoritesFilter.getTypes() != null) {
                    types = favoritesFilter.getTypes().stream().collect(Collectors.toSet());
                }
                if (!favoritesFilter.getOnlyShared()) {
                    Map<Integer, List<DBItemInventoryFavorite>> favoriteMap = dbLayer.getFavorites(types, favoritesFilter.getLimit()).stream()
                            .collect(Collectors.groupingBy(DBItemInventoryFavorite::getType));
                    for (Map.Entry<Integer, List<DBItemInventoryFavorite>> f : favoriteMap.entrySet()) {
                        int position = 0;
                        for (DBItemInventoryFavorite dbItem : f.getValue()) {
                            ownFavs.add(mapDbItem(dbItem, ++position));
                        }
                    }
                }
                if (favoritesFilter.getWithShared()) {
                    Map<Integer, List<DBItemInventoryFavorite>> favoriteMap = dbLayer.getSharedFavorites(types, favoritesFilter.getLimit()).stream()
                            .collect(Collectors.groupingBy(DBItemInventoryFavorite::getType));
                    for (Map.Entry<Integer, List<DBItemInventoryFavorite>> f : favoriteMap.entrySet()) {
                        int position = 0;
                        for (DBItemInventoryFavorite dbItem : f.getValue()) {
                            sharedFavs.add(mapDbItem(dbItem, ++position));
                        }
                    }
                }
            }

            Favorites favorites = new Favorites();
            if (!favoritesFilter.getOnlyShared()) {
                favorites.setFavorites(ownFavs);
            }
            if (favoritesFilter.getWithShared()) {
                favorites.setSharedFavorites(sharedFavs);
            }
            favorites.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(favorites);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }

    private Favorite mapDbItem(DBItemInventoryFavorite dbItem, Integer position) {
        Favorite fav = new Favorite();
        fav.setAccount(dbItem.getAccount());
        fav.setConfigurationDate(dbItem.getModified());
        fav.setContent(dbItem.getFavorite());
        fav.setName(dbItem.getName());
        fav.setOrdering(position);
        fav.setShared(dbItem.getShared());
        fav.setType(dbItem.getTypeAsEnum());
        return fav;
    }

}
