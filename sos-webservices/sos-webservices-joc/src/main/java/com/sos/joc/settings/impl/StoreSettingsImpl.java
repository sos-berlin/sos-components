package com.sos.joc.settings.impl;

import java.io.StringReader;
import java.time.Instant;
import java.util.Date;
import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
import com.sos.joc.classes.calendar.DailyPlanCalendar;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.configuration.ConfigurationType;
import com.sos.joc.model.settings.StoreSettingsFilter;
import com.sos.joc.settings.resource.IStoreSettings;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("settings")
public class StoreSettingsImpl extends JOCResourceImpl implements IStoreSettings {

    private static final String API_CALL = "./settings/store";
    private static final Logger LOGGER = LoggerFactory.getLogger(StoreSettingsImpl.class);

    @Override
    public JOCDefaultResponse postStoreSettings(String accessToken, byte[] storeSettingsFilter) {
        SOSHibernateSession hibernateSession = null;
        try {
            storeSettingsFilter = initLogging(API_CALL, storeSettingsFilter, accessToken, CategoryType.SETTINGS);
            JsonValidator.validate(storeSettingsFilter, StoreSettingsFilter.class);
            StoreSettingsFilter filter = Globals.objectMapper.readValue(storeSettingsFilter, StoreSettingsFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            boolean settingsPermission = getBasicJocPermissions(accessToken).getAdministration().getSettings().getManage();
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            storeAuditLog(filter.getAuditLog());
            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(hibernateSession);
            DBItemJocConfiguration cfg = new DBItemJocConfiguration();
            cfg.setAccount(ConfigurationGlobals.ACCOUNT);
            cfg.setControllerId(ConfigurationGlobals.CONTROLLER_ID);
            cfg.setConfigurationType(ConfigurationType.GLOBALS.value());
            cfg.setConfigurationItem(filter.getConfigurationItem());
            cfg.setName(null);
            cfg.setObjectType(null);
            cfg.setShared(ConfigurationGlobals.SHARED);
            // read old Settings and call updateControllerCalendar
            DBItemJocConfiguration oldCfg = jocConfigurationDBLayer.getGlobalSettingsConfiguration();
            String oldConfigurationItem = oldCfg == null ? ConfigurationGlobals.DEFAULT_CONFIGURATION_ITEM : oldCfg.getConfigurationItem();
            
            if (!settingsPermission) {
                // store only user settings without permissions
                cfg.setConfigurationItem(updateOnlyUserSection(cfg.getConfigurationItem(), oldConfigurationItem, getJocError()));
            } else if (dailyPlanHasChanged(cfg.getConfigurationItem(), oldConfigurationItem)) {
                // TODO: call for every known controller
                DailyPlanCalendar.getInstance().updateDailyPlanCalendar(null, accessToken, getJocError());
            }
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
    
    public static String updateOnlyUserSection(String configurationItem, String oldConfigurationItem, JocError jocError) {
        // store only user settings without permissions
        try {
            boolean onlyUserSection = false;
            JsonReader rdr = Json.createReader(new StringReader(configurationItem));
            JsonObject obj = rdr.readObject();
            Optional<JsonObject> oldObj = getOldJsonObject(oldConfigurationItem);
            JsonObjectBuilder builder = Json.createObjectBuilder();
            for (DefaultSections ds : EnumSet.allOf(DefaultSections.class)) {
                if (!ds.equals(DefaultSections.user)) {
                    if (oldObj.isPresent() && oldObj.get().get(ds.name()) != null) {
                        builder.add(ds.name(), oldObj.get().get(ds.name()));
                        onlyUserSection = true;
                    }
                } else {
                    if (obj.get(ds.name()) != null) {
                        builder.add(ds.name(), obj.get(ds.name()));
                    } else if (oldObj.isPresent() && oldObj.get().get(ds.name()) != null) {
                        builder.add(ds.name(), oldObj.get().get(ds.name()));
                    }
                }
            }
            if (onlyUserSection) {
                if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
                    LOGGER.info(jocError.printMetaInfo());
                    jocError.clearMetaInfo();
                }
                LOGGER.info("Due to missing permissions only settings of the 'user' section were considered.");
            }
            return builder.build().toString();
        } catch (Exception e) {
            //
        }
        return configurationItem;
    }
    
    public static boolean dailyPlanHasChanged(String configurationItem, String oldConfigurationItem) {
        boolean updateControllerCalendar = false;
        // Calendar for controller
        Optional<JsonObject> oldObj = getOldJsonObject(oldConfigurationItem);
        updateControllerCalendar = !oldObj.isPresent() || oldObj.get().get(DefaultSections.dailyplan.name()) == null;
        if (!updateControllerCalendar) {
            JsonReader rdr = Json.createReader(new StringReader(configurationItem));
            JsonObject obj = rdr.readObject();

            JsonObject oldDailyPlan = oldObj.get().getJsonObject(DefaultSections.dailyplan.name());
            JsonObject curDailyPlan = obj.getJsonObject(DefaultSections.dailyplan.name());
            if (curDailyPlan != null) {
                String oldTimeZone = oldDailyPlan == null || oldDailyPlan.getJsonObject("time_zone") == null ? "" : oldDailyPlan.getJsonObject(
                        "time_zone").getString("value", "");
                String oldPeriodBegin = oldDailyPlan == null || oldDailyPlan.getJsonObject("period_begin") == null ? "" : oldDailyPlan.getJsonObject(
                        "period_begin").getString("value", "");
                String curTimeZone = curDailyPlan.getJsonObject("time_zone") == null ? oldTimeZone : curDailyPlan.getJsonObject("time_zone")
                        .getString("value", oldTimeZone);
                String curPeriodBegin = curDailyPlan.getJsonObject("period_begin") == null ? oldPeriodBegin : curDailyPlan.getJsonObject(
                        "period_begin").getString("value", oldPeriodBegin);
                if (curPeriodBegin != null && !curPeriodBegin.isEmpty()) {
                    long periodBeginOffset = DailyPlanCalendar.convertPeriodBeginToSeconds(curPeriodBegin);
                    if (periodBeginOffset < 0 || periodBeginOffset >= TimeUnit.DAYS.toMillis(1)) {
                        throw new JocBadRequestException("Invalid 'dailyplan.period_begin': " + curPeriodBegin);
                    }
                }
                String curStartTime = curDailyPlan.getJsonObject("start_time") == null ? null : curDailyPlan.getJsonObject("start_time").getString(
                        "value");
                if (curStartTime != null && !curStartTime.isEmpty()) {
                    long curStartTimeOffset = DailyPlanCalendar.convertTimeToSeconds(curStartTime, "start_time");
                    if (curStartTimeOffset < 0 || curStartTimeOffset >= TimeUnit.DAYS.toMillis(1)) {
                        throw new JocBadRequestException("Invalid 'dailyplan.start_time': " + curStartTime);
                    }
                }
                if (!curTimeZone.equals(oldTimeZone) || !curPeriodBegin.equals(oldPeriodBegin)) {
                    updateControllerCalendar = true;
                    LOGGER.info("DailyPlan settings are changed. Calendar has to be updated.");
                }
            }
        }
        return updateControllerCalendar;
    }
    
    private static Optional<JsonObject> getOldJsonObject(String oldConfiguration) {
        Optional<JsonObject> oldObj = Optional.empty();
        if (oldConfiguration != null) {
            try {
                JsonReader oldRdr = Json.createReader(new StringReader(oldConfiguration));
                oldObj = Optional.of(oldRdr.readObject());
            } catch (Exception e) {
                //
            }
        }
        return oldObj;
    }
    
}
