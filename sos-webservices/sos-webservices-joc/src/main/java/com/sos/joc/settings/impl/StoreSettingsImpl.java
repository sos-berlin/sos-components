package com.sos.joc.settings.impl;

import java.io.StringReader;
import java.time.Instant;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.calendar.DailyPlanCalendar;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.db.authentication.DBItemIamRole;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.db.security.IamRoleDBLayer;
import com.sos.joc.db.security.IamRoleFilter;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocError;
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
            
            Optional<JsonObject> oldJsonObj = getJsonObject(oldConfigurationItem);
            Optional<JsonObject> newJsonObj = getJsonObject(cfg.getConfigurationItem());
            
            if (!settingsPermission) {
                // store only user settings without permissions
                cfg.setConfigurationItem(updateOnlyUserSection(cfg.getConfigurationItem(), newJsonObj, oldJsonObj, getJocError()));
            } else {
                approvalRequestorRoleHasChanged(newJsonObj, oldJsonObj, hibernateSession);
                if (dailyPlanHasChanged(newJsonObj, oldJsonObj)) {
                    // TODO: call for every known controller
                    DailyPlanCalendar.getInstance().updateDailyPlanCalendar(null, accessToken, getJocError());
                }
            }
            jocConfigurationDBLayer.saveOrUpdateGlobalSettingsConfiguration(cfg, oldCfg);
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }
    
    public static String updateOnlyUserSection(String configurationItem, Optional<JsonObject> newJsonObj, Optional<JsonObject> oldJsonObj,
            JocError jocError) {
        // store only user settings without permissions
        try {
            boolean onlyUserSection = false;
            JsonObjectBuilder builder = Json.createObjectBuilder();
            for (DefaultSections ds : EnumSet.allOf(DefaultSections.class)) {
                if (!ds.equals(DefaultSections.user)) {
                    Optional<JsonValue> section = oldJsonObj.map(o -> o.get(ds.name()));
                    if (section.isPresent()) {
                        builder.add(ds.name(), section.get());
                        onlyUserSection = true;
                    }
                } else {
                    Optional<JsonValue> newSection = newJsonObj.map(o -> o.get(ds.name()));
                    if (newSection.isPresent()) {
                        builder.add(ds.name(), newSection.get());
                    } else {
                        Optional<JsonValue> oldSection = oldJsonObj.map(o -> o.get(ds.name()));
                        if (oldSection.isPresent()) {
                            builder.add(ds.name(), oldSection.get());
                        }
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
    
    public static boolean dailyPlanHasChanged(Optional<JsonObject> newJsonObj, Optional<JsonObject> oldJsonObj) {
        
        Optional<JsonObject> oldDailyPlan = oldJsonObj.map(o -> o.getJsonObject(DefaultSections.dailyplan.name()));
        // Calendar for controller
        boolean updateControllerCalendar = oldDailyPlan.isEmpty();
        if (!updateControllerCalendar) {

            Optional<JsonObject> curDailyPlan = newJsonObj.map(o -> o.getJsonObject(DefaultSections.dailyplan.name()));
            if (curDailyPlan.isPresent()) {
                String oldTimeZone = oldDailyPlan.map(o -> o.getJsonObject("time_zone")).map(o -> o.getString("value", "")).orElse("");
                String oldPeriodBegin = oldDailyPlan.map(o -> o.getJsonObject("period_begin")).map(o -> o.getString("value", "")).orElse("");
                String curTimeZone = curDailyPlan.map(o -> o.getJsonObject("time_zone")).map(o -> o.getString("value", "")).orElse("");
                String curPeriodBegin = curDailyPlan.map(o -> o.getJsonObject("period_begin")).map(o -> o.getString("value", "")).orElse("");
                String curStartTime = curDailyPlan.map(o -> o.getJsonObject("start_time")).map(o -> o.getString("value", "")).orElse("");
                if (!curPeriodBegin.isEmpty()) {
                    long periodBeginOffset = DailyPlanCalendar.convertPeriodBeginToSeconds(curPeriodBegin);
                    if (periodBeginOffset < 0 || periodBeginOffset >= TimeUnit.DAYS.toMillis(1)) {
                        throw new JocBadRequestException("Invalid 'dailyplan.period_begin': " + curPeriodBegin);
                    }
                }
                if (!curStartTime.isEmpty()) {
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
    
    public static void approvalRequestorRoleHasChanged(Optional<JsonObject> newJsonObj, Optional<JsonObject> oldJsonObj,
            SOSHibernateSession hibernateSession) throws SOSHibernateException {
        
        String oldApprovalRequestorRole = oldJsonObj.map(o -> o.getJsonObject(DefaultSections.joc.name())).map(o -> o.getJsonObject(
                "approval_requestor_role")).map(o -> o.getString("value", "")).orElse("");
        String newApprovalRequestorRole = newJsonObj.map(o -> o.getJsonObject(DefaultSections.joc.name())).map(o -> o.getJsonObject(
                "approval_requestor_role")).map(o -> o.getString("value", "")).orElse("");
        if (oldApprovalRequestorRole.equals(newApprovalRequestorRole)) {
            return;
        }
        if (newApprovalRequestorRole.isEmpty()) {
            return;
        }

        IamRoleDBLayer roleDbLayer = new IamRoleDBLayer(hibernateSession);
        IamRoleFilter roleFilter = new IamRoleFilter();
        roleFilter.setRoleName(newApprovalRequestorRole);
        List<DBItemIamRole> dbRoles = roleDbLayer.getIamRoleList(roleFilter, 0);

        if (dbRoles.isEmpty()) {
            throw new JocBadRequestException("Unknown role in 'joc.approval_requestor_role'");
        }

        long numOfAccountsWithOnlyApprovalRequestorRole = roleDbLayer.getAccountIDsByRoleWithOnlyOneRole(dbRoles.stream().map(DBItemIamRole::getId)
                .distinct().toList()).count();

        if (numOfAccountsWithOnlyApprovalRequestorRole > 0) {
            throw new JocBadRequestException(String.format(
                    "There are %d accounts that have the approval requestor role '%s' as their only role. The approval requestor role has to be used as an additional role, not as the only role.",
                    numOfAccountsWithOnlyApprovalRequestorRole, newApprovalRequestorRole));
        }
    }
    
    public static Optional<JsonObject> getJsonObject(String oldConfiguration) {
        Optional<JsonObject> oldObj = Optional.empty();
        if (oldConfiguration != null) {
            JsonReader oldRdr = null;
            try {
                oldRdr = Json.createReader(new StringReader(oldConfiguration));
                oldObj = Optional.of(oldRdr.readObject());
            } catch (Exception e) {
                //
            } finally {
                if (oldRdr != null) {
                    try {
                        oldRdr.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return oldObj;
    }
    
}
