package com.sos.joc.settings.impl;

import java.io.StringReader;
import java.time.Instant;
import java.util.Date;
import java.util.EnumSet;
import java.util.Optional;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.configuration.Configuration;
import com.sos.joc.model.configuration.Configuration200;
import com.sos.joc.model.configuration.ConfigurationType;
import com.sos.joc.settings.resource.IReadSettings;

import jakarta.ws.rs.Path;

@Path("settings")
public class ReadSettingsImpl extends JOCResourceImpl implements IReadSettings {

    private static final String API_CALL = "./settings";
    private static final Logger LOGGER = LoggerFactory.getLogger(ReadSettingsImpl.class);

    @Override
    public JOCDefaultResponse postReadSettings(String xAccessToken) {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, "{}".getBytes(), xAccessToken, CategoryType.SETTINGS);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(hibernateSession);
            DBItemJocConfiguration dbItemGlobalSettings = jocConfigurationDBLayer.getGlobalSettingsConfiguration();
            Configuration cfg = new Configuration();
            cfg.setAccount(dbItemGlobalSettings.getAccount());
            if(!getBasicJocPermissions(xAccessToken).getAdministration().getSettings().getManage()) {
                cfg.setConfigurationItem(showUserSettingsOnly(dbItemGlobalSettings.getConfigurationItem()));
            } else {
                cfg.setConfigurationItem(dbItemGlobalSettings.getConfigurationItem());
            }
            cfg.setConfigurationType(ConfigurationType.fromValue(dbItemGlobalSettings.getConfigurationType()));
            cfg.setControllerId(dbItemGlobalSettings.getControllerId());
            cfg.setName(dbItemGlobalSettings.getName());
            cfg.setObjectType(dbItemGlobalSettings.getObjectType());
            cfg.setShared(dbItemGlobalSettings.getShared());
            Configuration200 response = new Configuration200();
            response.setConfiguration(cfg);
            response.setDeliveryDate(Date.from(Instant.now()));
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(response));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }
    
    private String showUserSettingsOnly (String cfg) {
        try {
            boolean onlyUserSection = false;
            Optional<JsonObject> oldObj = getOldJsonObject(cfg);
            JsonObjectBuilder builder = Json.createObjectBuilder();
            for (DefaultSections ds : EnumSet.allOf(DefaultSections.class)) {
                if (!ds.equals(DefaultSections.user)) {
                    if (oldObj.isPresent() && oldObj.get().get(ds.name()) != null) {
                        builder.add(ds.name(), oldObj.get().get(ds.name()));
                        onlyUserSection = true;
                    }
                } else {
                    if (oldObj.isPresent() && oldObj.get().get(ds.name()) != null) {
                        builder.add(ds.name(), oldObj.get().get(ds.name()));
                    }
                }
            }
            if (onlyUserSection) {
                if (getJocError() != null && !getJocError().getMetaInfo().isEmpty()) {
                    LOGGER.info(getJocError().printMetaInfo());
                    getJocError().clearMetaInfo();
                }
                LOGGER.info("Due to missing permissions only settings of the 'user' section were considered.");
            }
            return builder.build().toString();
        } catch (Exception e) {}
        return "";
    }
    
    private static Optional<JsonObject> getOldJsonObject(String oldConfiguration) {
        Optional<JsonObject> oldObj = Optional.empty();
        if (oldConfiguration != null) {
            JsonReader oldRdr = Json.createReader(new StringReader(oldConfiguration));
            oldObj = Optional.of(oldRdr.readObject());
        }
        return oldObj;
    }
    
}
