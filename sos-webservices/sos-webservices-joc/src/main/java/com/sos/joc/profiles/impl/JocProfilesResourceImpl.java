package com.sos.joc.profiles.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.configuration.ConfigurationType;
import com.sos.joc.model.configuration.Profiles;
import com.sos.joc.model.profile.Profile;
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
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_DELETE);
            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);
            jocConfigurationDBLayer.deleteConfigurations(ConfigurationType.PROFILE, profilesFilter.getAccounts());
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

            if (identityServiceFilter.getIdentityServiceName() != null && !identityServiceFilter.getIdentityServiceName().isEmpty()) {
                DBItemIamIdentityService dbItemIamIdentityService = SecurityHelper.getIdentityService(sosHibernateSession, identityServiceFilter
                        .getIdentityServiceName());
                IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
                IamAccountFilter filter = new IamAccountFilter();
                filter.setIdentityServiceId(dbItemIamIdentityService.getId());
                List<DBItemIamAccount> listOfAccounts = iamAccountDBLayer.getIamAccountList(filter, 0);
                resultProfiles.setProfiles(profiles.stream().filter(account -> listOfAccounts.stream().anyMatch(profile -> account.getAccount()
                        .equals(profile.getAccountName()))).collect(Collectors.toList()));
            } else {
                resultProfiles.getProfiles().addAll(profiles);
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