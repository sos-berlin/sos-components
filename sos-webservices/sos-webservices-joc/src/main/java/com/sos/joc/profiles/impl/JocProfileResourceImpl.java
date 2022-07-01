package com.sos.joc.profiles.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.configuration.ConfigurationType;
import com.sos.joc.model.profile.Profile;
import com.sos.joc.model.profile.ProfileFilter;
import com.sos.joc.profiles.resource.IJocProfileResource;
import com.sos.schema.JsonValidator;

@Path("profile")
public class JocProfileResourceImpl extends JOCResourceImpl implements IJocProfileResource {

    private static final String API_CALL_PROFILE = "./profile";
    private static final String API_CALL_PROFILE_STORE = "./profile/store";

    @Override
    public JOCDefaultResponse postProfileStore(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            initLogging(API_CALL_PROFILE_STORE, body, accessToken);
            JsonValidator.validateFailFast(body, Profile.class);
            Profile profile = Globals.objectMapper.readValue(body, Profile.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_PROFILE_STORE);
            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
            JocConfigurationFilter filter = new JocConfigurationFilter();
            filter.setAccount(profile.getAccount());
            filter.setControllerId(profile.getControllerId());
            filter.setConfigurationType(ConfigurationType.PROFILE.name());
            DBItemJocConfiguration dbItem = jocConfigurationDBLayer.getJocConfiguration(filter, 0);

            boolean isNew;
            if (dbItem == null) {
                dbItem = new DBItemJocConfiguration();
                isNew = true;
                dbItem.setId(null);
            } else {
                isNew = false;
            }

            dbItem.setControllerId(profile.getControllerId());
            dbItem.setInstanceId(0L);
            dbItem.setAccount(profile.getAccount());
            dbItem.setObjectType(ConfigurationType.PROFILE.name());

            dbItem.setConfigurationItem(profile.getProfileItem());
            Date now = Date.from(Instant.now());
            dbItem.setModified(now);

            if (isNew) {
                sosHibernateSession.save(dbItem);
            } else {
                sosHibernateSession.update(dbItem);
            }

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
    public JOCDefaultResponse postProfile(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            initLogging(API_CALL_PROFILE, body, accessToken);
            JsonValidator.validateFailFast(body, ProfileFilter.class);
            ProfileFilter profileFilter = Globals.objectMapper.readValue(body, ProfileFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_PROFILE);
            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
            JocConfigurationFilter filter = new JocConfigurationFilter();
            filter.setAccount(profileFilter.getAccount());
            filter.setControllerId(profileFilter.getControllerId());
            filter.setConfigurationType(ConfigurationType.PROFILE.name());
            DBItemJocConfiguration dbItem = jocConfigurationDBLayer.getJocConfiguration(filter, 0);

            if (dbItem == null) {
                dbItem = new DBItemJocConfiguration();
            }

            Profile profile = new Profile();
            profile.setAccount(dbItem.getAccount());
            profile.setControllerId(dbItem.getControllerId());
            profile.setModified(dbItem.getModified());
            profile.setProfileItem(dbItem.getConfigurationItem());

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(profile));

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