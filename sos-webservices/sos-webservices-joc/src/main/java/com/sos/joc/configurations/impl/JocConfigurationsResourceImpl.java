package com.sos.joc.configurations.impl;

import java.io.StringReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.configurations.resource.IJocConfigurationsResource;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.configuration.Configuration;
import com.sos.joc.model.configuration.ConfigurationType;
import com.sos.joc.model.configuration.Configurations;
import com.sos.joc.model.configuration.ConfigurationsDeleteFilter;
import com.sos.joc.model.configuration.ConfigurationsFilter;
import com.sos.joc.model.configuration.globals.GlobalSettings;
import com.sos.schema.JsonValidator;

@Path("configurations")
public class JocConfigurationsResourceImpl extends JOCResourceImpl implements IJocConfigurationsResource {

    private static final String API_CALL = "./configurations";
    private static final String API_CALL_DELETE = "./configurations/delete";

    @Override
    public JOCDefaultResponse postConfigurations(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ConfigurationsFilter.class);
            ConfigurationsFilter configurationsFilter = Globals.objectMapper.readValue(filterBytes, ConfigurationsFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

//            String objectType = null;
//            if (configurationsFilter.getObjectType() != null) {
//                objectType = configurationsFilter.getObjectType().value();
//            }
            String objectType = configurationsFilter.getObjectType();
            String configurationType = null;
            GlobalSettings defaultGlobalSettings = null;
            
            if (configurationsFilter.getConfigurationType() != null) {
                configurationType = configurationsFilter.getConfigurationType().value();
                switch (configurationsFilter.getConfigurationType()) {
                case PROFILE:
//                    String userName = getJobschedulerUser(accessToken).getSosShiroCurrentUser().getUsername();
//                    if (configurationsFilter.getAccount() == null || configurationsFilter.getAccount().isEmpty()) {
//                        configurationsFilter.setAccount(userName);
//                    } else if (!configurationsFilter.getAccount().equals(userName)) {
//                        throw new JocBadRequestException("You can only read your own profile.");
//                    }
                    break;
                case GLOBALS:
                    // read only user settings without permissions
//                    if (!getJocPermissions(accessToken).getAdministration().getSettings().getView()) {
//                        return accessDeniedResponse();
//                    }
                    configurationsFilter.setControllerId(ConfigurationGlobals.CONTROLLER_ID);
                    configurationsFilter.setAccount(ConfigurationGlobals.ACCOUNT);
                    //configurationsFilter.setObjectType(ConfigurationGlobals.OBJECT_TYPE);
                    configurationsFilter.setObjectType(null);

                    defaultGlobalSettings = new ConfigurationGlobals().getClonedDefaults();
                    if (!getJocPermissions(accessToken).getAdministration().getSettings().getView()) {
                        // read only user settings without permissions
                        if (defaultGlobalSettings != null) {
                            for(DefaultSections ds : EnumSet.allOf(DefaultSections.class)) {
                                if (!ds.equals(DefaultSections.user)) {
                                    defaultGlobalSettings.removeAdditionalProperty(ds.name()); 
                                }
                            }
                        }
                    }
                    break;
                default:
                    break;
                }

            }

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(connection);
            JocConfigurationFilter filter = new JocConfigurationFilter();

            filter.setObjectType(objectType);
            filter.setControllerId(configurationsFilter.getControllerId());
            filter.setConfigurationType(configurationType);
            filter.setAccount(configurationsFilter.getAccount());
            filter.setShared(configurationsFilter.getShared());

            List<DBItemJocConfiguration> listOfJocConfigurationDbItem = jocConfigurationDBLayer.getJocConfigurationList(filter, 0);
            Configurations configurations = new Configurations();
            List<Configuration> listOfConfigurations = new ArrayList<>();

            // if profile is new then try default_profile_account from settings if exists
            // only processed if controllerId is set and exists and requested account is equal current account
            String account = getJobschedulerUser().getSosShiroCurrentUser().getUsername();
            Date now = Date.from(Instant.now());
            if (configurationsFilter.getConfigurationType() == null || ConfigurationType.PROFILE.equals(configurationsFilter
                    .getConfigurationType())) {
                if (configurationsFilter.getAccount() == null || account.equals(configurationsFilter.getAccount())) {
                    if (configurationsFilter.getControllerId() != null && !configurationsFilter.getControllerId().isEmpty() && Proxies
                            .getControllerDbInstances().containsKey(configurationsFilter.getControllerId())) {
                        if (listOfJocConfigurationDbItem == null || listOfJocConfigurationDbItem.isEmpty() || !listOfJocConfigurationDbItem.stream()
                                .anyMatch(item -> ConfigurationType.PROFILE.value().equals(item.getConfigurationType()) && account.equals(item
                                        .getAccount()) && configurationsFilter.getControllerId().equals(item.getControllerId()))) {
                            // then copy from default_profile_account 
                            String defaultProfileAccount = ClusterSettings.getDefaultProfileAccount(Globals.getConfigurationGlobalsJoc());
                            JocConfigurationFilter filter2 = new JocConfigurationFilter();
                            filter2.setAccount(defaultProfileAccount);
                            filter2.setConfigurationType(ConfigurationType.PROFILE.value());
                            List<DBItemJocConfiguration> defaultProfiles = jocConfigurationDBLayer.getJocConfigurationList(filter2, 1);
                            if (defaultProfiles != null && !defaultProfiles.isEmpty()) {
                                DBItemJocConfiguration copyOfDefaultProfile = defaultProfiles.get(0);
                                copyOfDefaultProfile.setAccount(account);
                                copyOfDefaultProfile.setId(null);
                                copyOfDefaultProfile.setControllerId(configurationsFilter.getControllerId());
                                copyOfDefaultProfile.setModified(now);
                                connection.save(copyOfDefaultProfile);
                                if (listOfJocConfigurationDbItem == null) {
                                    listOfJocConfigurationDbItem = Collections.singletonList(copyOfDefaultProfile);
                                } else {
                                    listOfJocConfigurationDbItem.add(copyOfDefaultProfile);
                                }
                            }
                        }
                    }
                }
            }

            if (listOfJocConfigurationDbItem != null && !listOfJocConfigurationDbItem.isEmpty()) {
                boolean viewPerm = getJocPermissions(accessToken).getAdministration().getCustomization().getView();
                for (DBItemJocConfiguration jocConfigurationDbItem : listOfJocConfigurationDbItem) {
                    Configuration configuration = new Configuration();
                    configuration.setAccount(jocConfigurationDbItem.getAccount());
                    configuration.setConfigurationType(ConfigurationType.fromValue(jocConfigurationDbItem.getConfigurationType()));
                    configuration.setControllerId(jocConfigurationDbItem.getControllerId());
                    configuration.setName(jocConfigurationDbItem.getName());
                    if (jocConfigurationDbItem.getObjectType() != null) {
                        configuration.setObjectType(jocConfigurationDbItem.getObjectType());
                    }
                    configuration.setShared(jocConfigurationDbItem.getShared());
                    configuration.setId(jocConfigurationDbItem.getId());
                    if (jocConfigurationDbItem.getConfigurationItem() != null && !jocConfigurationDbItem.getConfigurationItem().isEmpty()) {
                        configuration.setConfigurationItem(jocConfigurationDbItem.getConfigurationItem());
                    }
                    switch (configuration.getConfigurationType()) {
                    case GLOBALS:
                        if (!getJocPermissions(accessToken).getAdministration().getSettings().getView() && configuration
                                .getConfigurationItem() != null) {
                            // read only user settings without permissions
                            try {
                                JsonReader rdr = Json.createReader(new StringReader(configuration.getConfigurationItem()));
                                JsonObject obj = rdr.readObject();
                                JsonObjectBuilder builder = Json.createObjectBuilder();
                                EnumSet.allOf(DefaultSections.class).forEach(ds -> {
                                    if (ds.equals(DefaultSections.user) && obj.get(ds.name()) != null) {
                                        builder.add(ds.name(), obj.get(ds.name()));
                                    }
                                });
                                configuration.setConfigurationItem(builder.build().toString());
                            } catch (Exception e) {
                                //
                            }
                        }
                        listOfConfigurations.add(configuration);
                        break;
                    default:
                        // if owner or shared or view permission
                        if (account.equals(configuration.getAccount()) || configuration.getShared() || viewPerm) {
                            listOfConfigurations.add(configuration);
                        }
                    }
                }
            }

            configurations.setDeliveryDate(now);
            configurations.setConfigurations(listOfConfigurations);
            configurations.setDefaultGlobals(defaultGlobalSettings);

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(configurations));
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
    public JOCDefaultResponse postConfigurationsDelete(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL_DELETE, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ConfigurationsDeleteFilter.class);
            ConfigurationsDeleteFilter configurationsFilter = Globals.objectMapper.readValue(filterBytes, ConfigurationsDeleteFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getCustomization()
                    .getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            connection = Globals.createSosHibernateStatelessConnection(API_CALL_DELETE);
            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(connection);
            connection.setAutoCommit(false);
            Globals.beginTransaction(connection);
            jocConfigurationDBLayer.deleteConfigurations(configurationsFilter.getAccounts());
            Globals.commit(connection);

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }

}