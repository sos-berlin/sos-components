package com.sos.joc.profiles.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamHistory;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.deployment.DBItemDepKeys;
import com.sos.joc.db.favorite.FavoriteDBLayer;
import com.sos.joc.db.inventory.DBItemInventoryCertificate;
import com.sos.joc.db.inventory.DBItemInventoryFavorite;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;
import com.sos.joc.db.security.IamHistoryDbLayer;
import com.sos.joc.db.security.IamHistoryFilter;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.configuration.Profiles;
import com.sos.joc.model.profile.ProfilesFilter;
import com.sos.joc.model.security.identityservice.IdentityServiceFilter;
import com.sos.joc.profiles.resource.IJocProfilesResource;
import com.sos.joc.security.classes.SecurityHelper;
import com.sos.schema.JsonValidator;

@Path("profiles")
public class JocProfilesResourceImpl extends JOCResourceImpl implements IJocProfilesResource {

    private static final String API_CALL_PROFILES = "./profiles";
    private static final String API_CALL_DELETE = "./profiles/delete";

    @Override
    public JOCDefaultResponse postProfilesDelete(String accessToken, byte[] filterBytes) {
        SOSHibernateSession sosHibernateSession = null;
        try {
            initLogging(API_CALL_DELETE, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ProfilesFilter.class);
            ProfilesFilter profilesFilter = Globals.objectMapper.readValue(filterBytes, ProfilesFilter.class);

            boolean onlyActAccount = true;
            for (String accountName : profilesFilter.getAccounts()) {
                if (!accountName.equals(this.getAccount())) {
                    onlyActAccount = false;
                    break;
                }
            }

            JOCDefaultResponse jocDefaultResponse = null;
            if (!onlyActAccount) {
                jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            } else {
                jocDefaultResponse = initPermissions("", true);
            }
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_DELETE);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            com.sos.joc.classes.profiles.Profiles.delete(sosHibernateSession, profilesFilter);

            Globals.commit(sosHibernateSession);

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postProfiles(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;

        try {
            initLogging(API_CALL_PROFILES, null, accessToken);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            JsonValidator.validateFailFast(body, IdentityServiceFilter.class);
            IdentityServiceFilter identityServiceFilter = Globals.objectMapper.readValue(body, IdentityServiceFilter.class);

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_PROFILES);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
            JocConfigurationFilter jocConfigurationFilter = new JocConfigurationFilter();
            jocConfigurationFilter.setConfigurationType("PROFILE");
            Profiles resultProfiles = new Profiles();

            List<com.sos.joc.model.configuration.Profile> profiles = jocConfigurationDBLayer.getJocConfigurationProfiles(jocConfigurationFilter);
            jocConfigurationFilter.setConfigurationType("GIT");
            List<com.sos.joc.model.configuration.Profile> profilesGit = jocConfigurationDBLayer.getJocConfigurationProfiles(jocConfigurationFilter);
            profiles.addAll(profilesGit);

            List<DBItemDepKeys> listOfDBItemDepKeys = new ArrayList<DBItemDepKeys>();
            if (!JocSecurityLevel.LOW.equals(Globals.getJocSecurityLevel())) {
                DBLayerKeys dbLayerKeys = new DBLayerKeys(sosHibernateSession);
                listOfDBItemDepKeys = dbLayerKeys.getDBItemDepKeys(Globals.getJocSecurityLevel());
                for (DBItemDepKeys dbItemDepKeys : listOfDBItemDepKeys) {
                    com.sos.joc.model.configuration.Profile profileKey = new com.sos.joc.model.configuration.Profile();
                    profileKey.setAccount(dbItemDepKeys.getAccount());
                    profiles.add(profileKey);
                }

                List<DBItemInventoryCertificate> listOfDBItemInventoryCertificate = new ArrayList<DBItemInventoryCertificate>();
                listOfDBItemInventoryCertificate = dbLayerKeys.getSigningRootCaCertificates();
                for (DBItemInventoryCertificate dbItemInventoryCertificate : listOfDBItemInventoryCertificate) {
                    com.sos.joc.model.configuration.Profile profileKey = new com.sos.joc.model.configuration.Profile();
                    profileKey.setAccount(dbItemInventoryCertificate.getAccount());
                    profiles.add(profileKey);
                }

            }

            FavoriteDBLayer dbLayer = new FavoriteDBLayer(sosHibernateSession, "");
            List<DBItemInventoryFavorite> listOfFavorites = dbLayer.getAllFavorites(0);
            for (DBItemInventoryFavorite dbItemInventoryFavorite : listOfFavorites) {
                com.sos.joc.model.configuration.Profile profileKey = new com.sos.joc.model.configuration.Profile();
                profileKey.setAccount(dbItemInventoryFavorite.getAccount());
                profiles.add(profileKey);
            }

            List<com.sos.joc.model.configuration.Profile> uniqueProfiles = new ArrayList<com.sos.joc.model.configuration.Profile>();

            for (com.sos.joc.model.configuration.Profile p : profiles) {
                p.setLastLogin(null);
                if (!uniqueProfiles.contains(p)) {
                    uniqueProfiles.add(p);
                }
            }

            // Get last login date.
            IamHistoryDbLayer iamHistoryDbLayer = new IamHistoryDbLayer(sosHibernateSession);
            IamHistoryFilter iamHistoryFilter = new IamHistoryFilter();
            iamHistoryFilter.setLoginSuccess(true);
            List<DBItemIamHistory> listOfLastLogins = iamHistoryDbLayer.getIamAccountList(iamHistoryFilter, 0);
            Map<String, Date> lastLogin = new HashMap<String, Date>();
            for (DBItemIamHistory dbItemIamHistory : listOfLastLogins) {
                lastLogin.put(dbItemIamHistory.getAccountName(), dbItemIamHistory.getLoginDate());
            }

            for (com.sos.joc.model.configuration.Profile p : uniqueProfiles) {
                Date l = lastLogin.get(p.getAccount());
                if (l != null) {
                    p.setLastLogin(l);
                }
            }

            if (identityServiceFilter.getIdentityServiceName() != null && !identityServiceFilter.getIdentityServiceName().isEmpty()) {
                DBItemIamIdentityService dbItemIamIdentityService = SecurityHelper.getIdentityService(sosHibernateSession, identityServiceFilter
                        .getIdentityServiceName());
                IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
                IamAccountFilter filter = new IamAccountFilter();
                filter.setIdentityServiceId(dbItemIamIdentityService.getId());
                List<DBItemIamAccount> listOfAccounts = iamAccountDBLayer.getIamAccountList(filter, 0);
                resultProfiles.setProfiles(uniqueProfiles.stream().filter(account -> listOfAccounts.stream().anyMatch(profile -> account.getAccount()
                        .equals(profile.getAccountName()))).collect(Collectors.toList()));
            } else {
                resultProfiles.getProfiles().addAll(uniqueProfiles);
            }

            resultProfiles.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(resultProfiles));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }
}