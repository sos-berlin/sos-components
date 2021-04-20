package com.sos.joc.configuration.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals;
import com.sos.joc.configuration.resource.IJocConfigurationResource;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.configuration.ConfigurationGlobalsChanged;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.configuration.Configuration;
import com.sos.joc.model.configuration.Configuration200;
import com.sos.joc.model.configuration.ConfigurationOk;
import com.sos.joc.model.configuration.ConfigurationType;
import com.sos.joc.model.configuration.globals.GlobalSettings;
import com.sos.joc.model.configuration.globals.GlobalSettingsSection;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

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
            
            String account = getJobschedulerUser().getSosShiroCurrentUser().getUsername();
            
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
            
            switch (configuration.getConfigurationType()) {
            case GLOBALS:
                if (!getJocPermissions(accessToken).getAdministration().getSettings().getManage()) {
                    return accessDeniedResponse();
                }
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
                break;
            case CUSTOMIZATION:
                if (isNew) {
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
                    List<DBItemJocConfiguration> result = jocConfigurationDBLayer.getJocConfigurations(filter, 1);
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
                        if (!getJocPermissions(accessToken).getAdministration().getCustomization().getManage()) {
                            return accessDeniedResponse();
                        }
                        boolean shareIsChanged = (dbItem.getShared() && !shouldBeShared) || (!dbItem.getShared() && shouldBeShared);
                        if (shareIsChanged && !getJocPermissions(accessToken).getAdministration().getCustomization().getShare()) {
                            return this.accessDeniedResponse();
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
                postGlobalsChangedEvent(configuration.getControllerId(), oldConfiguration, configuration.getConfigurationItem(), getJocError());
            }
            ConfigurationOk ok = new ConfigurationOk();
            ok.setId(dbItem.getId());
            ok.setDeliveryDate(now);
            return JOCDefaultResponse.responseStatus200(ok);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }

    }
    
    @Override
    public JOCDefaultResponse postReadConfiguration(String accessToken, byte[] body) {
        SOSHibernateSession connection = null;
        try {
            Configuration configuration = getConfiguration(API_CALL_READ, accessToken, body);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL_READ);
            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(connection);

            /** get item from DB with the given id */
            DBItemJocConfiguration dbItem = jocConfigurationDBLayer.getJocConfiguration(configuration.getId());
            if (dbItem == null) {
                throw new DBMissingDataException(String.format("no entry found for configuration id: %d", configuration.getId()));
            }
            
            ConfigurationType confType = ConfigurationType.fromValue(dbItem.getConfigurationType());
            switch (confType) {
            case GLOBALS:
                if (!getJocPermissions(accessToken).getAdministration().getSettings().getView()) {
                    return accessDeniedResponse();
                }
                break;
            default:
                String account = getJobschedulerUser().getSosShiroCurrentUser().getUsername();
                // owner doesn't need any permission or it is shared
                boolean owner = account.equals(dbItem.getAccount());
                if (!owner && !dbItem.getShared()) {
                    if (!getJocPermissions(accessToken).getAdministration().getCustomization().getView()) {
                        return accessDeniedResponse();
                    }
                }
            }
            
            Configuration200 entity = new Configuration200();
            entity.setDeliveryDate(Date.from(Instant.now()));
            entity.setConfiguration(setConfigurationValues(dbItem, configuration.getControllerId()));
            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
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
            DBItemJocConfiguration dbItem = jocConfigurationDBLayer.getJocConfiguration(configuration.getId());
            if (dbItem == null) {
                throw new DBMissingDataException(String.format("no entry found for configuration id: %d", configuration.getId()));
            }
            
            ConfigurationType confType = ConfigurationType.fromValue(dbItem.getConfigurationType());
            switch (confType) {
            case GLOBALS:
                if (!getJocPermissions(accessToken).getAdministration().getSettings().getManage()) {
                    return accessDeniedResponse();
                }
                break;
            default:
                String account = getJobschedulerUser().getSosShiroCurrentUser().getUsername();
                // owner doesn't need any permission
                boolean owner = account.equals(dbItem.getAccount());

                if (!owner) {
                    if (!getJocPermissions(accessToken).getAdministration().getCustomization().getManage()) {
                        return accessDeniedResponse();
                    }
                    if (!dbItem.getShared() || !getJocPermissions(accessToken).getAdministration().getCustomization().getShare()) {
                        return accessDeniedResponse();
                    }
                }
                break;
            }

            connection.delete(dbItem);

            ConfigurationOk ok = new ConfigurationOk();
            ok.setId(dbItem.getId());
            ok.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(ok);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
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
                String account = getJobschedulerUser().getSosShiroCurrentUser().getUsername();
                // owner doesn't need any permission
                boolean owner = account.equals(dbItem.getAccount());

                if (!owner) {
                    if (!getJocPermissions(accessToken).getAdministration().getCustomization().getShare()) {
                        return accessDeniedResponse();
                    }
                }
                dbItem.setShared(true);
                connection.update(dbItem);
                break;
            }
            
            ConfigurationOk ok = new ConfigurationOk();
            ok.setId(dbItem.getId());
            ok.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(ok);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
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
                String account = getJobschedulerUser().getSosShiroCurrentUser().getUsername();
                // owner doesn't need any permission
                boolean owner = account.equals(dbItem.getAccount());

                if (!owner) {
                    if (!getJocPermissions(accessToken).getAdministration().getCustomization().getShare()) {
                        return accessDeniedResponse();
                    }
                }
                dbItem.setShared(false);
                connection.update(dbItem);
                break;
            }

            ConfigurationOk ok = new ConfigurationOk();
            ok.setId(dbItem.getId());
            ok.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(ok);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    private Configuration getConfiguration(String action, String accessToken, byte[] body) throws SOSJsonSchemaException, IOException {
        initLogging(action, body, accessToken);
        JsonValidator.validateFailFast(body, Configuration.class);
        return Globals.objectMapper.readValue(body, Configuration.class);
    }
    
    private void postGlobalsChangedEvent(String controllerId, String oldSettings, String currentSettings, JocError jocError) {
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
                Globals.configurationGlobals = configurations;

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
        //if (dbItem.getObjectType() != null) {
            //config.setObjectType(ConfigurationObjectType.fromValue(dbItem.getObjectType()));
        //}
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