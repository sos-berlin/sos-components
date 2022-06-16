package com.sos.auth.classes;

import java.util.List;

import com.sos.auth.classes.permission.model.ObjectFactory;
import com.sos.auth.classes.permission.model.SOSPermissionRoles;
import com.sos.auth.classes.permission.model.SOSPermissionShiro;
import com.sos.auth.classes.permission.model.SOSPermissions;

public class SOSListOfPermissions {

    private SOSAuthCurrentAccount currentAccount;
    private SOSPermissionShiro sosPermissionShiro;

    public SOSListOfPermissions(SOSAuthCurrentAccount currentAccount) {
        super();
        this.currentAccount = currentAccount;
        this.initList(currentAccount);
    }

    private void initList(SOSAuthCurrentAccount currentAccount) {
        SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(currentAccount);

        ObjectFactory o = new ObjectFactory();
        sosPermissionShiro = o.createSOSPermissionShiro();

        SOSPermissions sosPermissions = o.createSOSPermissions();

        // JocPermissions
        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:get_log");

        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:adminstration:accounts:view");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:adminstration:accounts:manage");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:adminstration:certificates:view");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:adminstration:certificates:manage");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:adminstration:controllers:view");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:adminstration:controllers:manage");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:adminstration:customization:view");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:adminstration:customization:manage");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:adminstration:customization:share");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:adminstration:settings:view");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:adminstration:settings:manage");

        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:auditlog:view");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:calendars:view");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:cluster:manage");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:dailyplan:view");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:dailyplan:manage");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:documentations:view");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:documentations:manage");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:filetransfer:view");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:filetransfer:manage");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:inventory:view");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:inventory:manage");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:inventory:deploy");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:notification:view");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:notification:manage");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:others:view");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:joc:others:manage");

        // ControllerPermissions
        addPermission(sosPermissions.getSOSPermission(), "sos:products:controller:view");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:controller:restart");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:controller:terminate");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:controller:switch_over");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:controller:get_log");

        addPermission(sosPermissions.getSOSPermission(), "sos:products:controller:agents:view");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:controller:deployment:view");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:controller:deployment:deploy");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:controller:locks:view");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:controller:workflows:view");

        addPermission(sosPermissions.getSOSPermission(), "sos:products:controller:noticeboards:view");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:controller:noticeboards:post");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:controller:noticeboards:delete");

        addPermission(sosPermissions.getSOSPermission(), "sos:products:controller:orders:view");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:controller:orders:create");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:controller:orders:cancel");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:controller:orders:modify");
        addPermission(sosPermissions.getSOSPermission(), "sos:products:controller:orders:suspend_resume");

        sosPermissionShiro.setSOSPermissions(sosPermissions);
    }

    private void addPermission(List<String> sosPermission, String permission) {
        sosPermission.add(permission);
    }

    public SOSPermissionShiro getSosPermissionShiro() {
        return sosPermissionShiro;
    }

}
