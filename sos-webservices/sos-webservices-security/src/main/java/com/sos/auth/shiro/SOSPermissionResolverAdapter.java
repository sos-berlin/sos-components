package com.sos.auth.shiro;

import java.util.Collection;
import java.util.Collections;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleRole;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.realm.text.IniRealm;

import com.sos.joc.Globals;

public class SOSPermissionResolverAdapter implements RolePermissionResolver {

    private LocalIniRealm realm;

    @Override
    public Collection<Permission> resolvePermissionsInRole(final String roleString) {
        final SimpleRole role = this.realm.getRole(roleString);

        org.apache.shiro.config.IniSecurityManagerFactory factory = null;
        String iniFile = Globals.getShiroIniInClassPath();
        factory = new IniSecurityManagerFactory(Globals.getIniFileForShiro(iniFile));
        factory.getIni();        
        
        this.realm.setIni(factory.getIni());
        this.realm.init();

        if (role == null){
            return Collections.<Permission> emptySet();
        }else{
            return role.getPermissions();
        }
    }

    public void setIni(final IniRealm ini) {
        this.realm = new LocalIniRealm();
        this.realm.setIni(ini.getIni());
        this.realm.init();
    }

    private static class LocalIniRealm extends IniRealm {

        @Override
        protected SimpleRole getRole(final String rolename) {
            return super.getRole(rolename);
        }
    }
}