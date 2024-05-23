package com.sos.auth.classes;

import java.util.List;
import com.sos.joc.model.security.permissions.SOSPermissions;

public class SOSListOfPermissions {

    private SOSPermissions sosPermissions;

    public SOSListOfPermissions() {
        super();
        this.initList();
    }

    private void initList() {

        sosPermissions = new SOSPermissions();
 
        // JocPermissions
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:get_log");

        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:administration:accounts:view");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:administration:accounts:manage");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:administration:certificates:view");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:administration:certificates:manage");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:administration:controllers:view");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:administration:controllers:manage");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:administration:customization:view");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:administration:customization:manage");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:administration:customization:share");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:administration:settings:view");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:administration:settings:manage");

        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:auditlog:view");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:calendars:view");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:cluster:manage");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:dailyplan:view");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:dailyplan:manage");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:documentations:view");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:documentations:manage");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:filetransfer:view");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:filetransfer:manage");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:inventory:view");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:inventory:manage");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:inventory:deploy");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:notification:view");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:notification:manage");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:reports:view");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:reports:manage");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:others:view");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:others:manage");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:joc:encipherment:encrypt");

        // ControllerPermissions
        addPermission(sosPermissions.getSosPermissions(), "sos:products:controller:view");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:controller:restart");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:controller:terminate");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:controller:switch_over");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:controller:get_log");

        addPermission(sosPermissions.getSosPermissions(), "sos:products:controller:agents:view");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:controller:deployment:view");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:controller:deployment:deploy");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:controller:locks:view");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:controller:workflows:view");

        addPermission(sosPermissions.getSosPermissions(), "sos:products:controller:noticeboards:view");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:controller:noticeboards:post");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:controller:noticeboards:delete");

        addPermission(sosPermissions.getSosPermissions(), "sos:products:controller:orders:view");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:controller:orders:create");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:controller:orders:cancel");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:controller:orders:modify");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:controller:orders:suspend_resume");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:controller:orders:resume_failed");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:controller:orders:confirm");
        addPermission(sosPermissions.getSosPermissions(), "sos:products:controller:orders:manage_positions");

    }

    private void addPermission(List<String> sosPermission, String permission) {
        sosPermission.add(permission);
    }

    public SOSPermissions getSosPermissions() {
        return sosPermissions;
    }

}
