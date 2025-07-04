package com.sos.joc.configuration.impl;

import java.io.StringReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.calendar.DailyPlanCalendar;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.configuration.resource.IJocConfigurationResource;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.configuration.ConfigurationGlobalsChanged;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.configuration.Configuration;
import com.sos.joc.model.configuration.Configuration200;
import com.sos.joc.model.configuration.ConfigurationOk;
import com.sos.joc.model.configuration.ConfigurationRead;
import com.sos.joc.model.configuration.ConfigurationType;
import com.sos.joc.model.configuration.globals.GlobalSettings;
import com.sos.joc.model.configuration.globals.GlobalSettingsSection;
import com.sos.joc.settings.impl.StoreSettingsImpl;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("configuration")
public class JocConfigurationResourceImpl extends JOCResourceImpl implements IJocConfigurationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocConfigurationResourceImpl.class);

    private static final String API_CALL_READ = "./configuration";
    private static final String API_CALL_SAVE = "./configuration/save";
    private static final String API_CALL_DELETE = "./configuration/delete";
    private static final String API_CALL_SHARE = "./configuration/share";
    private static final String API_CALL_PRIVATE = "./configuration/make_private";

    @Override
    public JOCDefaultResponse postSaveConfiguration(String accessToken, byte[] body) {
        SOSHibernateSession connection = null;
        try {
            Configuration configuration = getConfiguration(API_CALL_SAVE, accessToken, body);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            /** check save specific required parameters */
            checkRequiredParameter("configurationType", configuration.getConfigurationType());
            checkRequiredParameter("configurationItem", configuration.getConfigurationItem());

            String account = getJobschedulerUser().getSOSAuthCurrentAccount().getAccountname();

            connection = Globals.createSosHibernateStatelessConnection(API_CALL_SAVE);
            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(connection);

            /** set DBItem with values from parameters */
            DBItemJocConfiguration dbItem = new DBItemJocConfiguration();
            if (configuration.getId() == 0) {
                configuration.setId(null);
            }
            boolean isNew = true;
            if (configuration.getId() != null) {
                isNew = false;
                dbItem = jocConfigurationDBLayer.getJocConfiguration(configuration.getId());
                if (dbItem == null) {
                    throw new DBMissingDataException(String.format("no entry found for configuration id: %d", configuration.getId()));
                }
            }
            String dbControllerId = configuration.getControllerId();
            if (dbControllerId == null || dbControllerId.isEmpty()) {
                dbControllerId = ConfigurationGlobals.CONTROLLER_ID;
            }
            String oldConfiguration = null;
            boolean updateControllerCalendar = false;

            switch (configuration.getConfigurationType()) {
            case GLOBALS:
                // store only user settings without permissions
                // if (!getJocPermissions(accessToken).getAdministration().getSettings().getManage()) {
                // return accessDeniedResponse();
                // }
                if (isNew) {
                    List<DBItemJocConfiguration> result = jocConfigurationDBLayer.getJocConfigurations(ConfigurationType.GLOBALS);
                    if (result == null || result.size() == 0) {
                        configuration.setConfigurationItem(ConfigurationGlobals.DEFAULT_CONFIGURATION_ITEM);
                    } else {
                        dbItem = result.get(0);
                        configuration.setId(dbItem.getId());
                        isNew = false;
                    }
                }
                if (dbItem.getId() != null && dbItem.getId().longValue() > 0) {
                    oldConfiguration = dbItem.getConfigurationItem();
                }
                dbItem.setControllerId(ConfigurationGlobals.CONTROLLER_ID);
                dbItem.setInstanceId(ConfigurationGlobals.INSTANCE_ID);
                dbItem.setAccount(ConfigurationGlobals.ACCOUNT);
                dbItem.setShared(ConfigurationGlobals.SHARED);
                dbItem.setObjectType(ConfigurationGlobals.OBJECT_TYPE == null ? null : ConfigurationGlobals.OBJECT_TYPE.name());

                if (!getBasicJocPermissions(accessToken).getAdministration().getSettings().getManage()) {
                    if (get4EyesJocPermissions().getAdministration().getSettings().getManage()) {
                        JOCDefaultResponse response = approvalRequestResponse();
                        if (response != null) {
                            return response;
                        }
                    }
                    // store only user settings without permissions
                    configuration.setConfigurationItem(StoreSettingsImpl.updateOnlyUserSection(configuration.getConfigurationItem(), oldConfiguration,
                            getJocError()));
                } else {
                    updateControllerCalendar = StoreSettingsImpl.dailyPlanHasChanged(configuration.getConfigurationItem(), oldConfiguration);
                }
                break;
            case IAM:
                if (!getBasicJocPermissions(accessToken).getAdministration().getAccounts().getManage()) {
                    return accessDeniedResponse();
                }
                if (get4EyesJocPermissions().getAdministration().getAccounts().getManage()) {
                    return accessDeniedResponseByUnsupported4EyesPrinciple();
                }

                dbControllerId = ConfigurationGlobals.CONTROLLER_ID;
                account = ConfigurationGlobals.ACCOUNT;
                storeAuditLog(configuration.getAuditLog());

            case CUSTOMIZATION:
                if (isNew && !configuration.getConfigurationType().equals(ConfigurationType.IAM)) {
                    checkRequiredParameter("objectType", configuration.getObjectType());
                    checkRequiredParameter("name", configuration.getName());
                }
            case IGNORELIST:
            case PROFILE:
            case SETTING:
                if (isNew) {
                    JocConfigurationFilter filter = new JocConfigurationFilter();
                    filter.setAccount(account);
                    filter.setConfigurationType(configuration.getConfigurationType().value());
                    filter.setControllerId(dbControllerId);
                    filter.setName(configuration.getName());
                    filter.setObjectType(configuration.getObjectType());
                    List<DBItemJocConfiguration> result = jocConfigurationDBLayer.getJocConfigurationList(filter, 1);
                    if (result != null && !result.isEmpty()) {
                        dbItem = result.get(0);
                        configuration.setId(dbItem.getId());
                        isNew = false;
                    }
                }
                boolean shouldBeShared = configuration.getShared() == Boolean.TRUE;
                if (!isNew) {
                    // owner doesn't need any permission
                    boolean owner = account.equals(dbItem.getAccount());

                    if (!owner) {
                        if (!getBasicJocPermissions(accessToken).getAdministration().getCustomization().getManage()) {
                            return accessDeniedResponse();
                        }
                        if (get4EyesJocPermissions().getAdministration().getCustomization().getManage()) {
                            JOCDefaultResponse response = approvalRequestResponse();
                            if (response != null) {
                                return response;
                            }
                        }
                        boolean shareIsChanged = (dbItem.getShared() && !shouldBeShared) || (!dbItem.getShared() && shouldBeShared);
                        if (shareIsChanged) {
                            if (!getBasicJocPermissions(accessToken).getAdministration().getCustomization().getShare()) {
                                return accessDeniedResponse();
                            }
                            if (get4EyesJocPermissions().getAdministration().getCustomization().getShare()) {
                                JOCDefaultResponse response = approvalRequestResponse();
                                if (response != null) {
                                    return response;
                                }
                            }
                        }
                    }
                    dbItem.setInstanceId(0L);
                    if (configuration.getName() != null && !configuration.getName().isEmpty()) {
                        dbItem.setName(configuration.getName());
                    }
                    dbItem.setShared(shouldBeShared);
                } else {
                    dbItem.setId(null);
                    dbItem.setControllerId(dbControllerId);
                    dbItem.setInstanceId(0L);
                    dbItem.setName(configuration.getName());
                    dbItem.setAccount(account);
                    dbItem.setObjectType(configuration.getObjectType());
                    dbItem.setShared(shouldBeShared);
                }
                break;
            case GIT:
            case APPROVAL:
                break;
            }

            dbItem.setConfigurationType(configuration.getConfigurationType().name());
            dbItem.setConfigurationItem(configuration.getConfigurationItem());
            Date now = Date.from(Instant.now());
            dbItem.setModified(now);

            if (isNew) {
                connection.save(dbItem);
            } else {
                connection.update(dbItem);
            }

            if (oldConfiguration != null && configuration.getConfigurationType().equals(ConfigurationType.GLOBALS)) {
                postGlobalsChangedEvent(configuration.getControllerId(), oldConfiguration, configuration.getConfigurationItem(), accessToken,
                        getJocError());
            }
            if (updateControllerCalendar) {
                // TODO: call for every known controller
                DailyPlanCalendar.getInstance().updateDailyPlanCalendar(configuration.getControllerId(), accessToken, getJocError());
            }

            if (configuration.getConfigurationType() != ConfigurationType.SETTING && configuration
                    .getConfigurationType() != ConfigurationType.CUSTOMIZATION && configuration.getConfigurationType() != ConfigurationType.PROFILE) {
                storeAuditLog(configuration.getAuditLog());
            }

            ConfigurationOk ok = new ConfigurationOk();
            ok.setId(dbItem.getId());
            ok.setDeliveryDate(now);
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(ok));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(connection);
        }

    }

    @Override
    public JOCDefaultResponse postReadConfiguration(String accessToken, byte[] body) {
        SOSHibernateSession connection = null;
        try {
            ConfigurationRead configuration = getConfigurationRead(API_CALL_READ, accessToken, body);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            connection = Globals.createSosHibernateStatelessConnection(API_CALL_READ);
            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(connection);

            DBItemJocConfiguration dbItem = null;
            
            if (configuration.getId() == null || configuration.getId() == 0) {
                JocConfigurationFilter filter = new JocConfigurationFilter();
                filter.setConfigurationType(configuration.getConfigurationType().value());
                filter.setName(configuration.getName());
                filter.setObjectType(configuration.getObjectType());
                List<DBItemJocConfiguration> listOfdbItemJocConfiguration = jocConfigurationDBLayer.getJocConfigurations(filter, 0);
                if (listOfdbItemJocConfiguration.size() == 1) {
                    dbItem = listOfdbItemJocConfiguration.get(0);
                } else {
                    dbItem = new DBItemJocConfiguration();
                    dbItem.setConfigurationType(configuration.getConfigurationType().value());
                    dbItem.setName(configuration.getName());
                    dbItem.setObjectType(configuration.getObjectType());
                    dbItem.setConfigurationItem("{}");
                    dbItem.setAccount(configuration.getAccount());
                }
            } else {
                dbItem = jocConfigurationDBLayer.getJocConfiguration(configuration.getId());
                if (dbItem == null) {
                    throw new DBMissingDataException(String.format("no entry found for configuration id: %d", configuration.getId()));
                }
            }

            ConfigurationType confType = ConfigurationType.fromValue(dbItem.getConfigurationType());
            switch (confType) {
            case IAM:
            case GLOBALS:
                // if (!getJocPermissions(accessToken).getAdministration().getSettings().getView()) {
                // return accessDeniedResponse();
                // }
                break;
            default:
                String account = getJobschedulerUser().getSOSAuthCurrentAccount().getAccountname();
                // owner doesn't need any permission or it is shared
                boolean owner = account.equals(dbItem.getAccount());
                if (!owner && !dbItem.getShared()) {
                    if (!getBasicJocPermissions(accessToken).getAdministration().getCustomization().getView()) {
                        return accessDeniedResponse();
                    }
                }
            }

            Configuration200 entity = new Configuration200();
            entity.setDeliveryDate(Date.from(Instant.now()));
            Configuration conf = setConfigurationValues(dbItem, configuration.getControllerId());

            if (confType.equals(ConfigurationType.GLOBALS)) {
                // user setting from conf.getConfigurationItem() are always sent independent the settings:view permission
                String confJson = conf.getConfigurationItem();
                if (confJson != null && !getBasicJocPermissions(accessToken).getAdministration().getSettings().getView()) {
                    // delete all except user setting from conf.getConfigurationItem()
                    try {
                        JsonReader rdr = Json.createReader(new StringReader(confJson));
                        JsonObject obj = rdr.readObject();
                        JsonObjectBuilder builder = Json.createObjectBuilder();
                        EnumSet.allOf(DefaultSections.class).forEach(ds -> {
                            if (ds.equals(DefaultSections.user) && obj.get(ds.name()) != null) {
                                builder.add(ds.name(), obj.get(ds.name()));
                            }
                        });
                        conf.setConfigurationItem(builder.build().toString());
                    } catch (Exception e) {
                        //
                    }
                }
            }
            entity.setConfiguration(conf);
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(connection);
        }

    }

    @Override
    public JOCDefaultResponse postDeleteConfiguration(String accessToken, byte[] body) {
        SOSHibernateSession connection = null;
        try {
            Configuration configuration = getConfiguration(API_CALL_DELETE, accessToken, body);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            connection = Globals.createSosHibernateStatelessConnection(API_CALL_DELETE);
            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(connection);

            /** get item from DB with the given id */
            DBItemJocConfiguration dbItem = null;
            if (configuration.getId() == 0) {
                JocConfigurationFilter filter = new JocConfigurationFilter();
                filter.setConfigurationType(configuration.getConfigurationType().value());
                filter.setName(configuration.getName());
                filter.setObjectType(configuration.getObjectType());
                List<DBItemJocConfiguration> listOfdbItemJocConfiguration = jocConfigurationDBLayer.getJocConfigurations(filter, 0);
                if (listOfdbItemJocConfiguration.size() == 1) {
                    dbItem = listOfdbItemJocConfiguration.get(0);
                } else {
                    throw new DBMissingDataException(String.format("no entry found for configurationType: %s , objectType: %s, name: %s",
                            configuration.getConfigurationType(), configuration.getObjectType(), configuration.getName()));
                }
            } else {
                dbItem = jocConfigurationDBLayer.getJocConfiguration(configuration.getId());
                if (dbItem == null) {
                    throw new DBMissingDataException(String.format("no entry found for configuration id: %d", configuration.getId()));
                }
            }

            dbItem = jocConfigurationDBLayer.getJocConfiguration(configuration.getId());
            if (dbItem == null) {
                throw new DBMissingDataException(String.format("no entry found for configuration id: %d", configuration.getId()));
            }

            ConfigurationType confType = ConfigurationType.fromValue(dbItem.getConfigurationType());
            switch (confType) {
            case GLOBALS:
                JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getAdministration().getSettings()
                        .getManage()));
                if (response != null) {
                    return response;
                }
                break;
            default:
                String account = getJobschedulerUser().getSOSAuthCurrentAccount().getAccountname();
                // owner doesn't need any permission
                boolean owner = ".".equals(dbItem.getAccount()) || account.equals(dbItem.getAccount());

                if (!owner) {
//                    if (!getBasicJocPermissions(accessToken).getAdministration().getCustomization().getManage()) {
//                        return accessDeniedResponse();
//                    }
//                    if (!dbItem.getShared() || !getBasicJocPermissions(accessToken).getAdministration().getCustomization().getShare()) {
//                        return accessDeniedResponse();
//                    }
                    JOCDefaultResponse response1 = null;
                    if (!dbItem.getShared()) {
                        response1 = initPermissions(null, andPermissions(getJocPermissions(accessToken).map(p -> p.getAdministration()
                                .getCustomization().getManage()), getJocPermissions(accessToken).map(p -> p.getAdministration().getCustomization()
                                        .getShare())));
                    } else {
                        response1 = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getAdministration().getCustomization()
                                .getManage()));
                    }
                    if (response1 != null) {
                        return response1;
                    }
                }
                break;
            }

            connection.delete(dbItem);

            ConfigurationOk ok = new ConfigurationOk();
            ok.setId(dbItem.getId());
            ok.setDeliveryDate(Date.from(Instant.now()));
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(ok));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(connection);
        }
    }

    @Override
    public JOCDefaultResponse postShareConfiguration(String accessToken, byte[] body) {
        SOSHibernateSession connection = null;
        try {
            Configuration configuration = getConfiguration(API_CALL_SHARE, accessToken, body);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            connection = Globals.createSosHibernateStatelessConnection(API_CALL_SHARE);
            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(connection);

            /** get item from DB with the given id */
            DBItemJocConfiguration dbItem = jocConfigurationDBLayer.getJocConfiguration(configuration.getId().longValue());
            if (dbItem == null) {
                throw new DBMissingDataException(String.format("no entry found for configuration id: %d", configuration.getId()));
            }

            ConfigurationType confType = ConfigurationType.fromValue(dbItem.getConfigurationType());
            switch (confType) {
            case GLOBALS:
                // Nothing to do, always shared = false
                break;
            default:
                String account = getJobschedulerUser().getSOSAuthCurrentAccount().getAccountname();
                // owner doesn't need any permission
                boolean owner = account.equals(dbItem.getAccount());

                if (!owner) {
                    List<Boolean> perms = getJocPermissions(accessToken).map(p -> p.getAdministration().getCustomization().getShare()).toList();
                    if (!perms.get(0)) {
                        return accessDeniedResponse();
                    }
                    if (perms.get(1)) {
                        JOCDefaultResponse response = approvalRequestResponse();
                        if (response != null) {
                            return response;
                        }
                    }
                }
                dbItem.setShared(true);
                connection.update(dbItem);
                break;
            }

            ConfigurationOk ok = new ConfigurationOk();
            ok.setId(dbItem.getId());
            ok.setDeliveryDate(Date.from(Instant.now()));
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(ok));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(connection);
        }
    }

    @Override
    public JOCDefaultResponse postMakePrivate(String accessToken, byte[] body) {
        SOSHibernateSession connection = null;
        try {
            Configuration configuration = getConfiguration(API_CALL_PRIVATE, accessToken, body);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            connection = Globals.createSosHibernateStatelessConnection(API_CALL_PRIVATE);
            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(connection);

            /** get item from DB with the given id */
            DBItemJocConfiguration dbItem = jocConfigurationDBLayer.getJocConfiguration(configuration.getId().longValue());
            if (dbItem == null) {
                throw new DBMissingDataException(String.format("no entry found for configuration id: %d", configuration.getId()));
            }

            ConfigurationType confType = ConfigurationType.fromValue(dbItem.getConfigurationType());
            switch (confType) {
            case GLOBALS:
                // Nothing to do, always shared = false
                break;
            default:
                String account = getJobschedulerUser().getSOSAuthCurrentAccount().getAccountname();
                // owner doesn't need any permission
                boolean owner = account.equals(dbItem.getAccount());

                if (!owner) {
                    if (!getBasicJocPermissions(accessToken).getAdministration().getCustomization().getShare()) {
                        return accessDeniedResponse();
                    }
                    if (get4EyesJocPermissions().getAdministration().getCustomization().getShare()) {
                        JOCDefaultResponse response = approvalRequestResponse();
                        if (response != null) {
                            return response;
                        }
                    }
                }
                dbItem.setShared(false);
                connection.update(dbItem);
                break;
            }

            ConfigurationOk ok = new ConfigurationOk();
            ok.setId(dbItem.getId());
            ok.setDeliveryDate(Date.from(Instant.now()));
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(ok));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(connection);
        }
    }

    private ConfigurationRead getConfigurationRead(String action, String accessToken, byte[] body) throws Exception {
        body = initLogging(action, body, accessToken, CategoryType.SETTINGS);
        JsonValidator.validateFailFast(body, ConfigurationRead.class);
        return Globals.objectMapper.readValue(body, ConfigurationRead.class);
    }
    
    private Configuration getConfiguration(String action, String accessToken, byte[] body) throws Exception {
        body = initLogging(action, body, accessToken, CategoryType.SETTINGS);
        JsonValidator.validateFailFast(body, Configuration.class);
        return Globals.objectMapper.readValue(body, Configuration.class);
    }

    private void postGlobalsChangedEvent(String controllerId, String oldSettings, String currentSettings, String accessToken, JocError jocError) {
        try {
            GlobalSettings old = getSettings(oldSettings);
            GlobalSettings current = getSettings(currentSettings);
            List<String> sections = new ArrayList<String>();

            ConfigurationGlobals c = new ConfigurationGlobals();
            c.getDefaults().getAdditionalProperties().entrySet().stream().forEach(defaultSection -> {
                GlobalSettingsSection oldSection = old.getAdditionalProperties().get(defaultSection.getKey());
                GlobalSettingsSection currentSection = current.getAdditionalProperties().get(defaultSection.getKey());
                if (oldSection == null && currentSection == null) {
                } else if (oldSection == null || currentSection == null) {
                    sections.add(defaultSection.getKey());
                } else {
                    try {
                        if (!Globals.objectMapper.writeValueAsString(oldSection).equals(Globals.objectMapper.writeValueAsString(currentSection))) {
                            sections.add(defaultSection.getKey());
                        }
                    } catch (JsonProcessingException e) {
                        if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
                            LOGGER.info(jocError.printMetaInfo());
                            jocError.clearMetaInfo();
                        }
                        LOGGER.error("", e);
                    }
                }
            });
            if (sections.size() > 0) {
                ConfigurationGlobals configurations = new ConfigurationGlobals();
                configurations.setConfigurationValues(current);
                Globals.setConfigurationGlobals(configurations);

                EventBus.getInstance().post(new ConfigurationGlobalsChanged(controllerId, ConfigurationType.GLOBALS.name(), sections));
            }
        } catch (Throwable e) {
            if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
                LOGGER.info(jocError.printMetaInfo());
                jocError.clearMetaInfo();
            }
            LOGGER.error("", e);
        }
    }

    private GlobalSettings getSettings(String val) throws Exception {
        return val == null || val.equals(ConfigurationGlobals.DEFAULT_CONFIGURATION_ITEM) ? new GlobalSettings() : Globals.objectMapper.readValue(val,
                GlobalSettings.class);
    }

    private Configuration setConfigurationValues(DBItemJocConfiguration dbItem, String controllerId) {
        Configuration config = new Configuration();
        config.setId(dbItem.getId());
        config.setAccount(dbItem.getAccount());
        if (dbItem.getConfigurationType() != null) {
            config.setConfigurationType(ConfigurationType.fromValue(dbItem.getConfigurationType()));
        }
        config.setConfigurationItem(dbItem.getConfigurationItem());
        config.setObjectType(dbItem.getObjectType());
        config.setShared(dbItem.getShared());
        config.setName(dbItem.getName());
        if (dbItem.getControllerId() != null && !dbItem.getControllerId().isEmpty()) {
            config.setControllerId(dbItem.getControllerId());
        } else {
            config.setControllerId(controllerId);
        }
        return config;
    }

}