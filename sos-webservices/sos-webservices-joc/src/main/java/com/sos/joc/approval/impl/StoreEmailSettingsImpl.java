package com.sos.joc.approval.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.approval.resource.IStoreEmailSettingsResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.configuration.ConfigurationType;
import com.sos.joc.model.security.foureyes.EmailSettings;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("approval")
public class StoreEmailSettingsImpl extends JOCResourceImpl implements IStoreEmailSettingsResource {

    private static final String API_CALL = "./approval/email_settings/store";

    @Override
    public JOCDefaultResponse storeEmailSettings(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken, CategoryType.OTHERS);
            JsonValidator.validateFailFast(filterBytes, EmailSettings.class);
            EmailSettings in = Globals.objectMapper.readValue(filterBytes, EmailSettings.class);
            JOCDefaultResponse response = initManageAccountPermissions(accessToken);
            if (response != null) {
                return response;
            }
            
            JocConfigurationFilter filter = new JocConfigurationFilter();
            filter.setConfigurationType(ConfigurationType.APPROVAL.value());
            
            DBItemJocConfiguration dbItem = new DBItemJocConfiguration();
            boolean isNew = false;
            
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(session);
            List<DBItemJocConfiguration> jocConfs = jocConfigurationDBLayer.getJocConfigurations(filter, 0);
            if (jocConfs != null && !jocConfs.isEmpty()) {
                dbItem = jocConfs.get(0);
            } else {
                isNew = true;
                dbItem.setId(null);
            }
            
            dbItem.setAccount(getJobschedulerUser().getSOSAuthCurrentAccount().getAccountname());
            dbItem.setConfigurationItem(Globals.objectMapper.writeValueAsString(in));
            dbItem.setConfigurationType(ConfigurationType.APPROVAL.value());
            dbItem.setControllerId(null);
            dbItem.setName(null);
            dbItem.setObjectType(null);
            dbItem.setShared(false);
            dbItem.setInstanceId(0L);
            
            Date now = Date.from(Instant.now());
            dbItem.setModified(now);
            
            if (isNew) {
                session.save(dbItem);
            } else {
                session.update(dbItem);
            }
            
            return responseStatusJSOk(now);
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(session);
        }
    }

}