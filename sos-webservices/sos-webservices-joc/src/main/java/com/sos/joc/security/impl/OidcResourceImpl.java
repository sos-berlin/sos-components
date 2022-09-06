package com.sos.joc.security.impl;

import java.util.List;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.db.security.IamIdentityServiceDBLayer;
import com.sos.joc.db.security.IamIdentityServiceFilter;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.identityservice.IdentityProvider;
import com.sos.joc.model.security.identityservice.IdentityProviders;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;
import com.sos.joc.security.resource.IOidcResource;

@Path("iam")
public class OidcResourceImpl extends JOCResourceImpl implements IOidcResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(OidcResourceImpl.class);

    private static final String API_CALL_IDENTITY_PROVIDERS = "./iam/identityproviders";

    private String getProperty(String value, String defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        } else {
            return value;
        }
    }

    @Override
    public JOCDefaultResponse postIdentityproviders() {
        SOSHibernateSession sosHibernateSession = null;
        try {

            initLogging(API_CALL_IDENTITY_PROVIDERS, null);

            IdentityProviders identityProviders = new IdentityProviders();

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_IDENTITY_PROVIDERS);
            IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
            IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
            filter.setIamIdentityServiceType(IdentityServiceTypes.OPENID_CONNECT);
            filter.setDisabled(false);
            List<DBItemIamIdentityService> listOfIdentityServices = iamIdentityServiceDBLayer.getIdentityServiceList(filter, 0);

            for (DBItemIamIdentityService dbItemIamIdentityService : listOfIdentityServices) {
                IdentityProvider identityProvider = new IdentityProvider();
                identityProvider.setIdentityServiceName(dbItemIamIdentityService.getIdentityServiceName());

                JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
                JocConfigurationFilter jocConfigurationFilter = new JocConfigurationFilter();
                jocConfigurationFilter.setConfigurationType(SOSAuthHelper.CONFIGURATION_TYPE_IAM);
                jocConfigurationFilter.setName(dbItemIamIdentityService.getIdentityServiceName());
                jocConfigurationFilter.setObjectType(IdentityServiceTypes.OPENID_CONNECT.value());
                List<DBItemJocConfiguration> listOfJocConfigurations = jocConfigurationDBLayer.getJocConfigurationList(jocConfigurationFilter, 0);
                if (listOfJocConfigurations.size() == 1) {
                    DBItemJocConfiguration dbItem = listOfJocConfigurations.get(0);
                    com.sos.joc.model.security.properties.Properties properties = Globals.objectMapper.readValue(dbItem.getConfigurationItem(),
                            com.sos.joc.model.security.properties.Properties.class);

                    identityProvider.setIamOidcClientId(getProperty(properties.getOidc().getIamOidcClientId(), ""));
                    identityProvider.setIamOidcClientSecret(getProperty(properties.getOidc().getIamOidcClientSecret(), ""));
                    identityProvider.setIamOidcdUrl(getProperty(properties.getOidc().getIamOidcdUrl(), ""));
                    identityProvider.setIamOidcName(getProperty(properties.getOidc().getIamOidcName(), ""));
                }
                identityProviders.getIdentityServiceItems().add(identityProvider);
            }

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(identityProviders));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

}