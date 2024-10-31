package com.sos.joc.classes;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.classes.SOSAuthHelper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.model.configuration.ConfigurationType;
import com.sos.joc.model.configuration.globals.GlobalSettings;
import com.sos.joc.model.configuration.globals.GlobalSettingsSection;
import com.sos.joc.model.configuration.globals.GlobalSettingsSectionEntry;

public class DBMoveIamConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBMoveIamConfiguration.class);

    private static DBItemJocConfiguration getProperties(SOSHibernateSession sosHibernateSession, String configurationType, String objectType) {
        try {
            JocConfigurationFilter filter = new JocConfigurationFilter();
            filter.setConfigurationType(configurationType);
            filter.setObjectType(objectType);
            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
            List<DBItemJocConfiguration> listOfDbItemJocConfiguration = jocConfigurationDBLayer.getJocConfigurations(filter, 0);
            if (listOfDbItemJocConfiguration.size() == 1) {
                return listOfDbItemJocConfiguration.get(0);
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return null;
    }

    private static com.sos.joc.model.security.properties.Properties getGlobalIamProperties(SOSHibernateSession sosHibernateSession)
            throws JsonMappingException, JsonProcessingException {
        DBItemJocConfiguration dbItem = getProperties(sosHibernateSession, SOSAuthHelper.CONFIGURATION_TYPE_IAM,
                SOSAuthHelper.OBJECT_TYPE_IAM_GENERAL);
        com.sos.joc.model.security.properties.Properties properties = Globals.objectMapper.readValue(dbItem.getConfigurationItem(),
                com.sos.joc.model.security.properties.Properties.class);
        return properties;
    }

    private static GlobalSettings getGlobalSettings(SOSHibernateSession sosHibernateSession) throws JsonMappingException, JsonProcessingException {
        DBItemJocConfiguration dbItem = getProperties(sosHibernateSession, SOSAuthHelper.CONFIGURATION_TYPE_GLOBALS, null);

        try {
            GlobalSettings settings = Globals.objectMapper.readValue(dbItem.getConfigurationItem(), GlobalSettings.class);

            return settings;
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
        return null;
    }

    private static void moveIdentityServiceConfiguration(SOSHibernateSession sosHibernateSession) {
        try {
            com.sos.joc.model.security.properties.Properties iamProperties = getGlobalIamProperties(sosHibernateSession);
            if (iamProperties != null) {
                ConfigurationGlobals configurations = new ConfigurationGlobals();
                GlobalSettings globalSettings = getGlobalSettings(sosHibernateSession);

                GlobalSettingsSectionEntry initialPassword = new GlobalSettingsSectionEntry();
                GlobalSettingsSectionEntry idleSessionTimeout = new GlobalSettingsSectionEntry();
                GlobalSettingsSectionEntry minimumPassword_length = new GlobalSettingsSectionEntry();

                initialPassword.setOrdering(0);
                initialPassword.setValue(iamProperties.getInitialPassword());

                idleSessionTimeout.setOrdering(1);
                Integer sessionTimeout = iamProperties.getSessionTimeout();
                String unit = "s";
                if (iamProperties.getSessionTimeout() % 86400 == 0) {
                    sessionTimeout = sessionTimeout / 86400;
                    unit = "d";
                } else {
                    if (iamProperties.getSessionTimeout() % 3600 == 0) {
                        sessionTimeout = sessionTimeout / 3600;
                        unit = "h";
                    } else {
                        if (iamProperties.getSessionTimeout() % 60 == 0) {
                            sessionTimeout = sessionTimeout / 60;
                            unit = "m";
                        }
                    }
                }
                idleSessionTimeout.setValue(String.valueOf(sessionTimeout) + unit);

                minimumPassword_length.setOrdering(2);
                minimumPassword_length.setValue(String.valueOf(iamProperties.getMinPasswordLength()));

                GlobalSettingsSection globalIamSettingsSection = new GlobalSettingsSection();
                globalIamSettingsSection.setAdditionalProperty("initial_password", initialPassword);
                globalIamSettingsSection.setAdditionalProperty("idle_session_timeout", idleSessionTimeout);
                globalIamSettingsSection.setAdditionalProperty("minimum_password_length", minimumPassword_length);
                globalSettings.setAdditionalProperty(DefaultSections.identityService.name(), globalIamSettingsSection);

                configurations.setConfigurationValues(globalSettings);
                String globalConfigurationJson = Globals.objectMapper.writeValueAsString(globalSettings);

                JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
                List<DBItemJocConfiguration> result = jocConfigurationDBLayer.getJocConfigurations(ConfigurationType.GLOBALS);
                if (result != null && result.size() > 0) {
                    DBItemJocConfiguration dbItem;
                    dbItem = result.get(0);
                    dbItem.setConfigurationItem(globalConfigurationJson);
                    sosHibernateSession.beginTransaction();
                    jocConfigurationDBLayer.saveOrUpdateConfiguration(dbItem);
                    sosHibernateSession.commit();
                    JocConfigurationFilter filter = new JocConfigurationFilter();
                    filter.setConfigurationType(SOSAuthHelper.CONFIGURATION_TYPE_IAM);
                    filter.setObjectType(SOSAuthHelper.OBJECT_TYPE_IAM_GENERAL);
                    sosHibernateSession.beginTransaction();
                    jocConfigurationDBLayer.deleteConfiguration(filter);
                    sosHibernateSession.commit();
                }
            }
        } catch (Throwable e) {
            LOGGER.warn("error ocurred, update not processed." + e.getMessage());
        } 
    }

    public static void execute() {
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(DBMoveIamConfiguration.class.getSimpleName());
            moveIdentityServiceConfiguration(sosHibernateSession);
        } catch (Throwable e) {
            LOGGER.warn(e.getMessage());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

}
