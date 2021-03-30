package com.sos.auth.rest;

import java.util.List;

import com.sos.auth.rest.permission.model.ObjectFactory;
import com.sos.auth.rest.permission.model.SOSPermissionListJoc;
import com.sos.auth.rest.permission.model.SOSPermissionRoles;
import com.sos.auth.rest.permission.model.SOSPermissionShiro;
import com.sos.auth.rest.permission.model.SOSPermissions;

public class SOSListOfPermissions {

    private SOSShiroCurrentUser currentUser;
    private SOSPermissionShiro sosPermissionShiro;

    public SOSListOfPermissions(SOSShiroCurrentUser currentUser, Boolean forUser) {
        super();
        this.currentUser = currentUser;
        this.initList(currentUser, false);
    }

    private void initList(SOSShiroCurrentUser currentUser, Boolean forUser) {
        SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(currentUser);
        SOSPermissionRoles roles = sosPermissionsCreator.getRoles(forUser);

        if (forUser == null) {
            forUser = false;
        }

        ObjectFactory o = new ObjectFactory();
        sosPermissionShiro = o.createSOSPermissionShiro();

        SOSPermissions sosPermissions = o.createSOSPermissions();

        SOSPermissionListJoc sosPermissionJoc = o.createSOSPermissionListJoc();
        
        // JocPermissions
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc:adminstration:accounts:view");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc:adminstration:accounts:manage");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc:adminstration:certificates:view");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc:adminstration:certificates:manage");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc:adminstration:controllers:view");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc:adminstration:controllers:manage");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc:adminstration:settings:view");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc:adminstration:settings:manage");
        
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc:auditlog:view");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc:calendars:view");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc:cluster:manage");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc:dailyplan:view");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc:dailyplan:manage");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc:documentations:view");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc:documentations:manage");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc:filetransfer:view");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc:filetransfer:manage");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc:inventory:view");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc:inventory:manage");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc:inventory:deploy");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc:notification:view");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc:notification:manage");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc:others:view");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc:others:manage");
        
        // ControllerPermissions
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:controller:view");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:controller:restart");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:controller:terminate");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:controller:switch_over");
        
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:controller:agents:view");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:controller:deployment:view");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:controller:deployment:deploy");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:controller:locks:view");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:controller:workflows:view");
        
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:controller:orders:view");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:controller:orders:create");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:controller:orders:cancel");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:controller:orders:modify");
        addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:controller:orders:suspend_resume");
        
        sosPermissions.setSOSPermissionListJoc(sosPermissionJoc);

        sosPermissionShiro.setSOSPermissionRoles(roles);
        sosPermissionShiro.setSOSPermissions(sosPermissions);
    }

    private boolean isPermitted(String permission) {
        return (currentUser != null && currentUser.isPermitted(permission) && currentUser.isAuthenticated());
    }

    private void addPermission(Boolean forUser, List<String> sosPermission, String permission) {
        if (!forUser || isPermitted(permission)) {
            sosPermission.add(permission);
        }
    }

    public SOSPermissionShiro getSosPermissionShiro() {
        return sosPermissionShiro;
    }

}
