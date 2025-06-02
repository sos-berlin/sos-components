package com.sos.joc.approval.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.approval.resource.IReadEmailSettingsResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.configuration.ConfigurationType;
import com.sos.joc.model.security.foureyes.ReadEmailSettings;

import jakarta.ws.rs.Path;

@Path("approval")
public class ReadEmailSettingsImpl extends JOCResourceImpl implements IReadEmailSettingsResource {

    private static final String API_CALL = "./approval/email_settings";

    @Override
    public JOCDefaultResponse postEmailSettings(String accessToken) {
        SOSHibernateSession session = null;
        try {
            initLogging(API_CALL, null, accessToken, CategoryType.OTHERS);
            JOCDefaultResponse response = initPermissions("", getBasicJocPermissions(accessToken).getAdministration().getAccounts().getView());
            if (response != null) {
                return response;
            }
            
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(readEmailSettings(session)));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private static ReadEmailSettings readEmailSettings(SOSHibernateSession session) throws SOSHibernateException, JsonMappingException,
            JsonProcessingException {

        JocConfigurationFilter filter = new JocConfigurationFilter();
        filter.setConfigurationType(ConfigurationType.APPROVAL.value());

        ReadEmailSettings entity = new ReadEmailSettings();

        JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(session);
        List<DBItemJocConfiguration> jocConfs = jocConfigurationDBLayer.getJocConfigurations(filter, 0);
        if (jocConfs != null && !jocConfs.isEmpty()) {
            entity = Globals.objectMapper.readValue(jocConfs.get(0).getConfigurationItem(), ReadEmailSettings.class);
        }
        entity.setDeliveryDate(Date.from(Instant.now()));
        return entity;
    }

}