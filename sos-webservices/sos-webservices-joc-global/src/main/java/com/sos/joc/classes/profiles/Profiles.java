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

    public static void delete(SOSHibernateSession session, ProfilesFilter filter) throws SOSHibernateException {
        JocConfigurationDbLayer dbLayer = new JocConfigurationDbLayer(session);
        dbLayer.deleteConfigurations(ConfigurationType.PROFILE, filter.getAccounts());

        if (filter.getComplete()) {
            dbLayer.deleteConfigurations(ConfigurationType.GIT, filter.getAccounts());
            dbLayer.deleteConfigurations(ConfigurationType.SETTING, filter.getAccounts());
            dbLayer.deleteConfigurations(ConfigurationType.CUSTOMIZATION, filter.getAccounts());
            dbLayer.deleteConfigurations(ConfigurationType.IGNORELIST, filter.getAccounts());

            FavoriteDBLayer favoriteDBLayer = new FavoriteDBLayer(session, "");
            for (String account : filter.getAccounts()) {
                favoriteDBLayer.deleteByAccount(account);
            }

            if (!JocSecurityLevel.LOW.equals(Globals.getJocSecurityLevel())) {
                DBLayerKeys dbLayerKeys = new DBLayerKeys(session);
                for (String account : filter.getAccounts()) {
                    dbLayerKeys.deleteKeyByAccount(account);
                    dbLayerKeys.deleteCertByAccount(account);
                }
            }
        }
    }
}
