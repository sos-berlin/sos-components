package com.sos.joc.settings.impl;

import java.io.StringReader;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.calendar.DailyPlanCalendar;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.configuration.ConfigurationType;
import com.sos.joc.model.settings.StoreSettingsFilter;
import com.sos.joc.settings.resource.IStoreSettings;
import com.sos.schema.JsonValidator;

@Path("settings")
public class StoreSettingsImpl extends JOCResourceImpl implements IStoreSettings {

    private static final String API_CALL = "./settings/store";
    private static final String DEFAULT_EMPTY_VAL = ".";
    private static final Logger LOGGER = LoggerFactory.getLogger(StoreSettingsImpl.class);

    @Override
    public JOCDefaultResponse postStoreSettings(String xAccessToken, byte[] storeSettingsFilter) {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, storeSettingsFilter, xAccessToken);
            JsonValidator.validate(storeSettingsFilter, StoreSettingsFilter.class);
            StoreSettingsFilter filter = Globals.objectMapper.readValue(storeSettingsFilter, StoreSettingsFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getAdministration().getSettings().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            storeAuditLog(filter.getAuditLog(), CategoryType.SETTINGS);
            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(hibernateSession);
            DBItemJocConfiguration cfg = new DBItemJocConfiguration();
            cfg.setAccount(DEFAULT_EMPTY_VAL);
            cfg.setControllerId(null);
            cfg.setConfigurationType(ConfigurationType.GLOBALS.value());
            cfg.setConfigurationItem(filter.getConfigurationItem());
            cfg.setName(null);
            cfg.setObjectType(null);
            cfg.setShared(false);
            // read old Settings and call updateControllerCalendar
            DBItemJocConfiguration oldCfg = jocConfigurationDBLayer.getGlobalSettingsConfiguration();
            updateControllerCalendar(xAccessToken, cfg.getConfigurationItem(), oldCfg.getConfigurationItem());
            jocConfigurationDBLayer.saveOrUpdateGlobalSettingsConfiguration(cfg, oldCfg);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }
    
    private void updateControllerCalendar(String accessToken, String configurationItem, String oldConfigurationItem) {
        boolean updateControllerCalendar = false;
        // Calendar for controller
        try {
            Optional<JsonObject> oldObj = getOldJsonObject(oldConfigurationItem);
            updateControllerCalendar = !oldObj.isPresent() || oldObj.get().get(DefaultSections.dailyplan.name()) == null;
            if (!updateControllerCalendar) {
                JsonReader rdr = Json.createReader(new StringReader(configurationItem));
                JsonObject obj = rdr.readObject();

                JsonObject oldDailyPlan = oldObj.get().getJsonObject(DefaultSections.dailyplan.name());
                JsonObject curDailyPlan = obj.getJsonObject(DefaultSections.dailyplan.name());
                if (curDailyPlan != null) {
                    String oldTimeZone = oldDailyPlan == null || oldDailyPlan.getJsonObject("time_zone") == null ? "" : oldDailyPlan
                            .getJsonObject("time_zone").getString("value", "");
                    String oldPeriodBegin = oldDailyPlan == null || oldDailyPlan.getJsonObject("period_begin") == null ? "" : oldDailyPlan
                            .getJsonObject("period_begin").getString("value", "");
                    String curTimeZone = curDailyPlan.getJsonObject("time_zone") == null ? oldTimeZone : curDailyPlan.getJsonObject(
                            "time_zone").getString("value", oldTimeZone);
                    String curPeriodBegin = curDailyPlan.getJsonObject("period_begin") == null ? oldPeriodBegin : curDailyPlan
                            .getJsonObject("period_begin").getString("value", oldPeriodBegin);
                    if (!curTimeZone.equals(oldTimeZone) || !curPeriodBegin.equals(oldPeriodBegin)) {
                        updateControllerCalendar = true;
                        LOGGER.info("DailyPlan settings are changed. Calendar has to be updated.");
                    }
                }
            }
        } catch (Exception e) {}
        if (updateControllerCalendar) {
            // TODO: call for every know controller
            Set<String> controllerIds = Proxies.getControllerDbInstances().keySet();
            controllerIds.stream().forEach(controllerId -> 
                DailyPlanCalendar.getInstance().updateDailyPlanCalendar(controllerId, accessToken, getJocError()));
        }
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
