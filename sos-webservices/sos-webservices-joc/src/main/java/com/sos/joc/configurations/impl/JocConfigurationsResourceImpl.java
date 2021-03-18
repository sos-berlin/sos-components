package com.sos.joc.configurations.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals;
import com.sos.joc.configurations.resource.IJocConfigurationsResource;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.exceptions.JobSchedulerBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.configuration.Configuration;
import com.sos.joc.model.configuration.ConfigurationObjectType;
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
            JOCDefaultResponse jocDefaultResponse = initPermissions(configurationsFilter.getControllerId(), true);
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
                    String userName = getJobschedulerUser(accessToken).getSosShiroCurrentUser().getUsername();
                    if (configurationsFilter.getAccount() == null || configurationsFilter.getAccount().isEmpty()) {
                        configurationsFilter.setAccount(userName);
                    } else if (!configurationsFilter.getAccount().equals(userName)) {
                        throw new JobSchedulerBadRequestException("You can only read your own profile.");
                    }
                    break;
                case GLOBALS:
                    configurationsFilter.setControllerId(ConfigurationGlobals.CONTROLLER_ID);
                    configurationsFilter.setAccount(ConfigurationGlobals.ACCOUNT);
                    //configurationsFilter.setObjectType(ConfigurationGlobals.OBJECT_TYPE);
                    configurationsFilter.setObjectType(null);

                    defaultGlobalSettings = new ConfigurationGlobals().getDefaults();
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
            // cleanup wrongfully duplicated Profile entries
            listOfJocConfigurationDbItem = cleanupProfileDuplicates(listOfJocConfigurationDbItem);

            // if profile is new then try default_profile_account from joc.properties if exists
            if (configurationsFilter.getConfigurationType() == ConfigurationType.PROFILE && (listOfJocConfigurationDbItem == null
                    || listOfJocConfigurationDbItem.isEmpty() || listOfJocConfigurationDbItem.get(0).getConfigurationItem() == null
                    || listOfJocConfigurationDbItem.get(0).getConfigurationItem().isEmpty())) {
                String defaultProfileAccount = Globals.getConfigurationGlobalsJoc().getDefaultProfileAccount().getValue();
                String currentAccount = configurationsFilter.getAccount();
                if (!defaultProfileAccount.isEmpty() && !defaultProfileAccount.equals(currentAccount)) {
                    filter.setAccount(defaultProfileAccount);
                    listOfJocConfigurationDbItem = cleanupProfileDuplicates(jocConfigurationDBLayer.getJocConfigurationList(filter, 0));
                    // if default_profile_account profile exist then store it for new user
                    if (listOfJocConfigurationDbItem != null && !listOfJocConfigurationDbItem.isEmpty()) {
                        listOfJocConfigurationDbItem.get(0).setAccount(currentAccount);
                        listOfJocConfigurationDbItem.get(0).setId(null);
                        jocConfigurationDBLayer.saveOrUpdateConfiguration(listOfJocConfigurationDbItem.get(0));
                    }
                }
            }

            if (listOfJocConfigurationDbItem != null && !listOfJocConfigurationDbItem.isEmpty()) {
                boolean sharePerm = getPermissonsJocCockpit(configurationsFilter.getControllerId(), accessToken).getJOCConfigurations().getShare()
                        .getView().isStatus();
                for (DBItemJocConfiguration jocConfigurationDbItem : listOfJocConfigurationDbItem) {
                    Configuration configuration = new Configuration();
                    configuration.setAccount(jocConfigurationDbItem.getAccount());
                    configuration.setConfigurationType(ConfigurationType.fromValue(jocConfigurationDbItem.getConfigurationType()));
                    configuration.setControllerId(configurationsFilter.getControllerId());
                    configuration.setName(jocConfigurationDbItem.getName());
                    if (jocConfigurationDbItem.getObjectType() != null) {
                        //configuration.setObjectType(ConfigurationObjectType.fromValue(jocConfigurationDbItem.getObjectType()));
                        configuration.setObjectType(jocConfigurationDbItem.getObjectType());
                    }
                    configuration.setShared(jocConfigurationDbItem.getShared());
                    configuration.setId(jocConfigurationDbItem.getId());
                    if (jocConfigurationDbItem.getConfigurationItem() != null && !jocConfigurationDbItem.getConfigurationItem().isEmpty()) {
                        configuration.setConfigurationItem(jocConfigurationDbItem.getConfigurationItem());
                    }
                    if (!jocConfigurationDbItem.getShared() || sharePerm) {
                        listOfConfigurations.add(configuration);
                    }
                }
            }

            configurations.setDeliveryDate(new Date());
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
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getPermissonsJocCockpit("", accessToken).getJS7Controller()
                    .getAdministration().isEditPermissions());
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

    private List<DBItemJocConfiguration> cleanupProfileDuplicates(List<DBItemJocConfiguration> configurationDbItems) {
        if (configurationDbItems == null) {
            return null;
        }
        Comparator<DBItemJocConfiguration> itemComparator = new Comparator<DBItemJocConfiguration>() {

            @Override
            public int compare(DBItemJocConfiguration o1, DBItemJocConfiguration o2) {
                return o1.getId().compareTo(o2.getId());
            }
        };
        configurationDbItems.sort(itemComparator);
        Iterator<DBItemJocConfiguration> iterator = configurationDbItems.iterator();
        boolean found = false;
        while (iterator.hasNext()) {
            if (iterator.next().getConfigurationType().equals(ConfigurationType.PROFILE.name())) {
                if (!found) {
                    found = true;
                } else {
                    iterator.remove();
                }
            }
        }
        return configurationDbItems;
    }

}