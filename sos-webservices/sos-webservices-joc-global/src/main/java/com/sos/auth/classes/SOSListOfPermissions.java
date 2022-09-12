package com.sos.auth.classes;

import com.sos.joc.model.security.permissions.SOSPermission;
import com.sos.joc.model.security.permissions.SOSPermissions;

public class SOSListOfPermissions {

    private SOSPermissions sosPermissions;

    public SOSListOfPermissions() {
        super();
        this.initList();
    }

    private void initList() {

        sosPermissions = new SOSPermissions();
        sosPermissions.setSOSPermissions(new SOSPermission());

        // JocPermissions
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:get_log");

        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:adminstration:accounts:view");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:adminstration:accounts:manage");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:adminstration:certificates:view");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:adminstration:certificates:manage");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:adminstration:controllers:view");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:adminstration:controllers:manage");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:adminstration:customization:view");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:adminstration:customization:manage");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:adminstration:customization:share");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:adminstration:settings:view");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:adminstration:settings:manage");

        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:auditlog:view");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:calendars:view");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:cluster:manage");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:dailyplan:view");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:dailyplan:manage");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:documentations:view");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:documentations:manage");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:filetransfer:view");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:filetransfer:manage");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:inventory:view");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:inventory:manage");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:inventory:deploy");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:notification:view");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:notification:manage");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:others:view");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:joc:others:manage");

        // ControllerPermissions
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:controller:view");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:controller:restart");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:controller:terminate");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:controller:switch_over");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:controller:get_log");

        addPermission(sosPermissions.getSOSPermissions(), "sos:products:controller:agents:view");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:controller:deployment:view");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:controller:deployment:deploy");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:controller:locks:view");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:controller:workflows:view");

        addPermission(sosPermissions.getSOSPermissions(), "sos:products:controller:noticeboards:view");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:controller:noticeboards:post");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:controller:noticeboards:delete");

        addPermission(sosPermissions.getSOSPermissions(), "sos:products:controller:orders:view");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:controller:orders:create");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:controller:orders:cancel");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:controller:orders:modify");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:controller:orders:suspend_resume");
        addPermission(sosPermissions.getSOSPermissions(), "sos:products:controller:orders:manage_positions");

     }

    private void addPermission(SOSPermission sosPermission, String permission) {
        sosPermission.getSOSpermission().add(permission);
    }

    public SOSPermissions getSosPermissions() {
        return sosPermissions;
    }

}
