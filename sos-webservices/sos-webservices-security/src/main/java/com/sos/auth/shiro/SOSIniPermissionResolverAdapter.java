package com.sos.auth.shiro;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleRole;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.realm.text.IniRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.classes.security.SOSSecurityConfiguration;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.IniPermissions;
import com.sos.joc.model.security.SecurityConfiguration;
import com.sos.joc.model.security.permissions.IniPermission;

public class SOSIniPermissionResolverAdapter implements RolePermissionResolver {

    private LocalIniLdapRealm realm;
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSIniPermissionResolverAdapter.class);

    @Override
    public Collection<Permission> resolvePermissionsInRole(final String roleString) {

        if (this.realm == null) {
            this.realm = new LocalIniLdapRealm();
        }
        final SimpleRole role = this.realm.getRole(roleString);
        if (role == null) {
            return Collections.<Permission> emptySet();
        } else {
            return role.getPermissions();
        }
    }

    public void setIni(final IniRealm ini) {
        this.realm = new LocalIniLdapRealm();
    }

    private static class LocalIniLdapRealm extends IniRealm {

        @Override
        protected SimpleRole getRole(final String rolename) {
            SOSSecurityConfiguration sosSecurityConfiguration = new SOSSecurityConfiguration();
            SecurityConfiguration securityConfiguration;
            SimpleRole simpleRole = new SimpleRole();
            try {
                securityConfiguration = sosSecurityConfiguration.readConfiguration();
                if (securityConfiguration.getRoles().getAdditionalProperties().get(rolename) != null) {
                    IniPermissions permissions = securityConfiguration.getRoles().getAdditionalProperties().get(rolename).getPermissions();
                    for (IniPermission permission : permissions.getJoc()) {
                        final Permission _permission;
                        if (permission.getExcluded()) {
                            _permission = new WildcardPermission("-" + permission.getPath());
                        } else {
                            _permission = new WildcardPermission(permission.getPath());
                        }
                        simpleRole.add(_permission);
                    }
                    for (IniPermission permission : permissions.getControllerDefaults()) {
                        final Permission _permission;
                        if (permission.getExcluded()) {
                            _permission = new WildcardPermission("-" + permission.getPath());
                        } else {
                            _permission = new WildcardPermission(permission.getPath());
                        }
                        simpleRole.add(_permission);

                    }
                    if (permissions.getControllers() != null && permissions.getControllers().getAdditionalProperties() != null && !permissions
                            .getControllers().getAdditionalProperties().isEmpty()) {
                        for (Map.Entry<String, List<IniPermission>> controllerPermissions : permissions.getControllers().getAdditionalProperties()
                                .entrySet()) {
                            if (controllerPermissions.getValue() != null) {
                                for (IniPermission permission : controllerPermissions.getValue()) {

                                    final Permission _permission;
                                    if (permission.getExcluded()) {
                                        _permission = new WildcardPermission("-" + permission.getPath());
                                    } else {
                                        _permission = new WildcardPermission(permission.getPath());
                                    }

                                    if (permission.getPath() != null) {
                                        if (permission.getExcluded()) {
                                            simpleRole.add(_permission);
                                        } else {
                                            simpleRole.add(_permission);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (JocException | SOSHibernateException | IOException e) {
                LOGGER.error("Error reaging roles", e);
            }
            return simpleRole;
        }

    }
}