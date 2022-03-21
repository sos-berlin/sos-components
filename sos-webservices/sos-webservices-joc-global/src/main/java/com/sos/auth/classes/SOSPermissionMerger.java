package com.sos.auth.classes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.ini4j.InvalidFileFormatException;

import com.sos.auth.interfaces.ISOSSecurityConfiguration;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.classes.security.SOSSecurityConfiguration;
import com.sos.joc.classes.security.SOSSecurityDBConfiguration;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.configuration.SecurityConfiguration;
import com.sos.joc.model.security.configuration.SecurityConfigurationRole;
import com.sos.joc.model.security.configuration.SecurityConfigurationRoles;
import com.sos.joc.model.security.configuration.permissions.IniPermission;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;

public class SOSPermissionMerger {

    private List<SOSIdentityService> identityServices;
    private List<SecurityConfiguration> listOfSecurityConfigurations;

    public void addIdentityService(String identityServiceName, IdentityServiceTypes identyServiceType) {
        if (identityServices == null) {
            identityServices = new ArrayList<SOSIdentityService>();
        }
        SOSIdentityService sosIdentityService = new SOSIdentityService(identityServiceName, identyServiceType);
        if (!identityServices.contains(sosIdentityService)) {
            identityServices.add(sosIdentityService);
        }
    }

    public SecurityConfiguration addIdentityService(SOSIdentityService sosIdentityService) throws InvalidFileFormatException, JocException,
            SOSHibernateException, IOException {
        if (identityServices == null) {
            identityServices = new ArrayList<SOSIdentityService>();
        }
        if (!identityServices.contains(sosIdentityService)) {
            identityServices.add(sosIdentityService);
        }
        ISOSSecurityConfiguration sosSecurityConfiguration;
        SecurityConfiguration securityConfiguration = new SecurityConfiguration();

        switch (sosIdentityService.getIdentyServiceType()) {
        case SHIRO:
            sosSecurityConfiguration = new SOSSecurityConfiguration();
            break;
        default:
            sosSecurityConfiguration = new SOSSecurityDBConfiguration();
        }
        securityConfiguration = sosSecurityConfiguration.readConfiguration(null, sosIdentityService.getIdentityServiceName());
        if (listOfSecurityConfigurations == null) {
            listOfSecurityConfigurations = new ArrayList<SecurityConfiguration>();
        }
        listOfSecurityConfigurations.add(securityConfiguration);
        return securityConfiguration;
    }

    public List<SecurityConfiguration> getSecurityConfigurations() {
        return listOfSecurityConfigurations;
    }

    public SecurityConfiguration mergePermissions() {
        SecurityConfiguration securityConfigurationResult = new SecurityConfiguration();
        SecurityConfigurationRoles securityConfigurationRoles = new SecurityConfigurationRoles();
        securityConfigurationResult.setRoles(securityConfigurationRoles);

        for (SecurityConfiguration securityConfiguration : listOfSecurityConfigurations) {
            for (Entry<String, SecurityConfigurationRole> entry : securityConfiguration.getRoles().getAdditionalProperties().entrySet()) {
                if (securityConfigurationResult.getRoles().getAdditionalProperties().get(entry.getKey()) == null) {
                    securityConfigurationResult.getRoles().getAdditionalProperties().put(entry.getKey(), entry.getValue());
                } else {
                    securityConfigurationResult.getRoles().getAdditionalProperties().get(entry.getKey()).getPermissions().getJoc().addAll(
                            securityConfiguration.getRoles().getAdditionalProperties().get(entry.getKey()).getPermissions().getJoc());
                    securityConfigurationResult.getRoles().getAdditionalProperties().get(entry.getKey()).getPermissions().getControllerDefaults()
                            .addAll(securityConfiguration.getRoles().getAdditionalProperties().get(entry.getKey()).getPermissions()
                                    .getControllerDefaults());
                    for (Entry<String, List<IniPermission>> controllerEntry : securityConfiguration.getRoles().getAdditionalProperties().get(entry
                            .getKey()).getPermissions().getControllers().getAdditionalProperties().entrySet()) {
                        if (securityConfigurationResult.getRoles().getAdditionalProperties().get(entry.getKey()).getPermissions().getControllers()
                                .getAdditionalProperties().get(controllerEntry.getKey()) == null) {
                            securityConfigurationResult.getRoles().getAdditionalProperties().get(entry.getKey()).getPermissions().getControllers()
                                    .getAdditionalProperties().put(controllerEntry.getKey(), controllerEntry.getValue());
                        } else {
                            securityConfigurationResult.getRoles().getAdditionalProperties().get(entry.getKey()).getPermissions().getControllers()
                                    .getAdditionalProperties().get(controllerEntry.getKey()).addAll(securityConfiguration.getRoles()
                                            .getAdditionalProperties().get(entry.getKey()).getPermissions().getControllers().getAdditionalProperties()
                                            .get(controllerEntry.getKey()));
                        }
                    }
                }
            }
        }
        return securityConfigurationResult;
    }
}
