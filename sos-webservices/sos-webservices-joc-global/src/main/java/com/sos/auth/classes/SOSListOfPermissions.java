package com.sos.auth.classes;

import java.util.List;

import com.sos.auth.classes.permission.model.ObjectFactory;
import com.sos.auth.classes.permission.model.SOSPermissionRoles;
import com.sos.auth.classes.permission.model.SOSPermissionShiro;
import com.sos.auth.classes.permission.model.SOSPermissions;

public class SOSListOfPermissions {

    private SOSAuthCurrentAccount currentAccount;
    private SOSPermissionShiro sosPermissionShiro;

    public SOSListOfPermissions(SOSAuthCurrentAccount currentAccount, Boolean forAccount) {
        super();
        this.currentAccount = currentAccount;
        this.initList(currentAccount, false);
    }

    private void initList(SOSAuthCurrentAccount currentAccount, Boolean forAccount) {
        SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(currentAccount);
        SOSPermissionRoles roles = sosPermissionsCreator.getRoles(currentAccount,forAccount);

        if (forAccount == null) {
            forAccount = false;
        }

        ObjectFactory o = new ObjectFactory();
        sosPermissionShiro = o.createSOSPermissionShiro();

        SOSPermissions sosPermissions = o.createSOSPermissions();

        // JocPermissions
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:get_log");
        
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:adminstration:accounts:view");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:adminstration:accounts:manage");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:adminstration:certificates:view");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:adminstration:certificates:manage");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:adminstration:controllers:view");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:adminstration:controllers:manage");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:adminstration:customization:view");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:adminstration:customization:manage");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:adminstration:customization:share");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:adminstration:settings:view");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:adminstration:settings:manage");
        
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:auditlog:view");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:calendars:view");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:cluster:manage");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:dailyplan:view");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:dailyplan:manage");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:documentations:view");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:documentations:manage");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:filetransfer:view");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:filetransfer:manage");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:inventory:view");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:inventory:manage");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:inventory:deploy");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:notification:view");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:notification:manage");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:others:view");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:joc:others:manage");
        
        // ControllerPermissions
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:controller:view");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:controller:restart");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:controller:terminate");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:controller:switch_over");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:controller:get_log");
        
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:controller:agents:view");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:controller:deployment:view");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:controller:deployment:deploy");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:controller:locks:view");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:controller:workflows:view");
        
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:controller:noticeboards:view");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:controller:noticeboards:post");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:controller:noticeboards:delete");
        
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:controller:orders:view");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:controller:orders:create");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:controller:orders:cancel");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:controller:orders:modify");
        addPermission(forAccount, sosPermissions.getSOSPermission(), "sos:products:controller:orders:suspend_resume");
        
        sosPermissionShiro.setSOSPermissionRoles(roles);
        sosPermissionShiro.setSOSPermissions(sosPermissions);
    }

    private boolean isPermitted(String permission) {
        return (currentAccount != null && currentAccount.isPermitted(permission) && currentAccount.isAuthenticated());
    }

    private void addPermission(Boolean forAccount, List<String> sosPermission, String permission) {
        if (!forAccount || isPermitted(permission)) {
            sosPermission.add(permission);
        }
    }

    public SOSPermissionShiro getSosPermissionShiro() {
        return sosPermissionShiro;
    }

}
