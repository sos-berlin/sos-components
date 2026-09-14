package com.sos.auth.classes;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.classes.security.SOSSecurityDBConfiguration;
import com.sos.joc.model.security.configuration.SecurityConfiguration;
import com.sos.joc.model.security.configuration.SecurityConfigurationRole;
import com.sos.joc.model.security.configuration.SecurityConfigurationRoles;
import com.sos.joc.model.security.configuration.permissions.IniPermission;

public class SOSPermissionMerger {

    private Set<SecurityConfiguration> listOfSecurityConfigurations;

    public SecurityConfiguration addIdentityService(SOSIdentityService sosIdentityService) throws SOSHibernateException {
        
        SecurityConfiguration securityConfiguration = SOSSecurityDBConfiguration.readConfiguration(sosIdentityService.getIdentityServiceId());
        if (listOfSecurityConfigurations == null) {
            listOfSecurityConfigurations = new HashSet<>();
        }
        listOfSecurityConfigurations.add(securityConfiguration);
        return securityConfiguration;
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
