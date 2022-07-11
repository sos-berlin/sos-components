package com.sos.joc.classes.profiles;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.favorite.FavoriteDBLayer;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.configuration.ConfigurationType;
import com.sos.joc.model.profile.ProfilesFilter;

public class Profiles {

    public static ProfilesDeleteResult delete(SOSHibernateSession session, ProfilesFilter filter) throws SOSHibernateException {
        ProfilesDeleteResult r = new ProfilesDeleteResult();

        JocConfigurationDbLayer dbLayer = new JocConfigurationDbLayer(session);
        r.getConfiguration().setProfile(dbLayer.deleteConfigurations(ConfigurationType.PROFILE, filter.getAccounts()));

        if (filter.getComplete()) {
            r.getConfiguration().setGit(dbLayer.deleteConfigurations(ConfigurationType.GIT, filter.getAccounts()));
            r.getConfiguration().setSetting(dbLayer.deleteConfigurations(ConfigurationType.SETTING, filter.getAccounts()));
            r.getConfiguration().setCustomization(dbLayer.deleteConfigurations(ConfigurationType.CUSTOMIZATION, filter.getAccounts()));
            r.getConfiguration().setIgnoreList(dbLayer.deleteConfigurations(ConfigurationType.IGNORELIST, filter.getAccounts()));

            FavoriteDBLayer favoriteDBLayer = new FavoriteDBLayer(session, "");
            for (String account : filter.getAccounts()) {
                r.getAccount(account).setFavorite(favoriteDBLayer.deleteByAccount(account));
            }

            if (!JocSecurityLevel.LOW.equals(Globals.getJocSecurityLevel())) {
                DBLayerKeys dbLayerKeys = new DBLayerKeys(session);
                for (String account : filter.getAccounts()) {
                    r.getAccount(account).setKey(dbLayerKeys.deleteKeyByAccount(account));
                    r.getAccount(account).setCert(dbLayerKeys.deleteCertByAccount(account));
                }
            }
        }
        return r;
    }
}
