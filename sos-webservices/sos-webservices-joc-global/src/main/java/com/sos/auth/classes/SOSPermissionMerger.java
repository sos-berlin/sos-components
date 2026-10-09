package com.sos.auth.classes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sos.auth.records.UniqueRole;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.security.SOSSecurityDBConfiguration;
import com.sos.joc.model.security.configuration.SecurityConfiguration;
import com.sos.joc.model.security.configuration.SecurityConfigurationRole;
import com.sos.joc.model.security.configuration.SecurityConfigurationRoles;
import com.sos.joc.model.security.configuration.permissions.IniPermission;

public class SOSPermissionMerger {

    private Map<Long, SecurityConfiguration> securityConfigurations;

    public SecurityConfiguration addIdentityService(SOSIdentityService sosIdentityService) throws SOSHibernateException {
        
        SecurityConfiguration securityConfiguration = SOSSecurityDBConfiguration.readConfiguration(sosIdentityService.getIdentityServiceId());
        if (securityConfigurations == null) {
            securityConfigurations = new HashMap<>();
        }
        securityConfigurations.put(sosIdentityService.getIdentityServiceId(), securityConfiguration);
        return securityConfiguration;
    }

    public SecurityConfiguration mergePermissions() {
        String approvalRequestorRole = Globals.getConfigurationGlobalsJoc().getApprovalRequestorRole().getValue();
        SecurityConfiguration securityConfigurationResult = new SecurityConfiguration();
        SecurityConfigurationRoles securityConfigurationRoles = new SecurityConfigurationRoles();
        securityConfigurationResult.setRoles(securityConfigurationRoles);

        for (Map.Entry<Long, SecurityConfiguration> securityConfiguration : securityConfigurations.entrySet()) {
            Long identityServiceId = securityConfiguration.getKey();
            SecurityConfiguration conf = securityConfiguration.getValue();
            for (Entry<String, SecurityConfigurationRole> entry : conf.getRoles().getAdditionalProperties().entrySet()) {
                String uniqueRole = new UniqueRole(entry.getKey(), identityServiceId).string();
                if (entry.getKey().equals(approvalRequestorRole)) {
                    uniqueRole = approvalRequestorRole;
                }
                if (securityConfigurationResult.getRoles().getAdditionalProperties().get(uniqueRole) == null) {
                    securityConfigurationResult.getRoles().getAdditionalProperties().put(uniqueRole, entry.getValue());
                } else {
                    securityConfigurationResult.getRoles().getAdditionalProperties().get(uniqueRole).getPermissions().getJoc().addAll(conf.getRoles()
                            .getAdditionalProperties().get(uniqueRole).getPermissions().getJoc());
                    securityConfigurationResult.getRoles().getAdditionalProperties().get(uniqueRole).getPermissions().getControllerDefaults().addAll(
                            conf.getRoles().getAdditionalProperties().get(uniqueRole).getPermissions().getControllerDefaults());
                    for (Entry<String, List<IniPermission>> controllerEntry : conf.getRoles().getAdditionalProperties().get(uniqueRole)
                            .getPermissions().getControllers().getAdditionalProperties().entrySet()) {
                        if (securityConfigurationResult.getRoles().getAdditionalProperties().get(uniqueRole).getPermissions().getControllers()
                                .getAdditionalProperties().get(controllerEntry.getKey()) == null) {
                            securityConfigurationResult.getRoles().getAdditionalProperties().get(uniqueRole).getPermissions().getControllers()
                                    .getAdditionalProperties().put(controllerEntry.getKey(), controllerEntry.getValue());
                        } else {
                            securityConfigurationResult.getRoles().getAdditionalProperties().get(uniqueRole).getPermissions().getControllers()
                                    .getAdditionalProperties().get(controllerEntry.getKey()).addAll(conf.getRoles().getAdditionalProperties().get(
                                            uniqueRole).getPermissions().getControllers().getAdditionalProperties().get(controllerEntry.getKey()));
                        }
                    }
                }
            }
        }
        return securityConfigurationResult;
    }
}
