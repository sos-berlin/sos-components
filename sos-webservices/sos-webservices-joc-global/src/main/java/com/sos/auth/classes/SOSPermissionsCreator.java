package com.sos.auth.classes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.sos.joc.model.security.configuration.SecurityConfiguration;
import com.sos.joc.model.security.configuration.SecurityConfigurationRole;
import com.sos.joc.model.security.configuration.permissions.ControllerPermissions;
import com.sos.joc.model.security.configuration.permissions.JocPermissions;
import com.sos.joc.model.security.configuration.permissions.Permissions;
import com.sos.joc.model.security.configuration.permissions.controller.Agents;
import com.sos.joc.model.security.configuration.permissions.controller.Deployments;
import com.sos.joc.model.security.configuration.permissions.controller.Locks;
import com.sos.joc.model.security.configuration.permissions.controller.NoticeBoards;
import com.sos.joc.model.security.configuration.permissions.controller.Orders;
import com.sos.joc.model.security.configuration.permissions.controller.Workflows;
import com.sos.joc.model.security.configuration.permissions.joc.Administration;
import com.sos.joc.model.security.configuration.permissions.joc.AuditLog;
import com.sos.joc.model.security.configuration.permissions.joc.Calendars;
import com.sos.joc.model.security.configuration.permissions.joc.Cluster;
import com.sos.joc.model.security.configuration.permissions.joc.DailyPlan;
import com.sos.joc.model.security.configuration.permissions.joc.Documentations;
import com.sos.joc.model.security.configuration.permissions.joc.Encipherment;
import com.sos.joc.model.security.configuration.permissions.joc.FileTransfer;
import com.sos.joc.model.security.configuration.permissions.joc.Inventory;
import com.sos.joc.model.security.configuration.permissions.joc.Notification;
import com.sos.joc.model.security.configuration.permissions.joc.Others;
import com.sos.joc.model.security.configuration.permissions.joc.Reports;
import com.sos.joc.model.security.configuration.permissions.joc.admin.Accounts;
import com.sos.joc.model.security.configuration.permissions.joc.admin.Certificates;
import com.sos.joc.model.security.configuration.permissions.joc.admin.Controllers;
import com.sos.joc.model.security.configuration.permissions.joc.admin.Customization;
import com.sos.joc.model.security.configuration.permissions.joc.admin.Settings;

public class SOSPermissionsCreator {

    private SOSAuthCurrentAccount currentAccount;

    public SOSPermissionsCreator(SOSAuthCurrentAccount currentAccount) {
        super();
        this.currentAccount = currentAccount;
    }

    public Map<String, List<String>> getMapOfFolder() {
        Map<String, List<String>> resultMap = new HashMap<String, List<String>>();

        if (currentAccount.getCurrentSubject().getMapOfFolderPermissions() != null) {
            resultMap.putAll(currentAccount.getCurrentSubject().getMapOfFolderPermissions());
        }

        return resultMap;
    }

    public Permissions createJocCockpitPermissionControllerObjectList(SecurityConfiguration secConf) {
        Permissions permissions = new Permissions(currentAccount.getRoles(), getJocPermissions(), getControllerPermissions(""),
                new com.sos.joc.model.security.configuration.permissions.Controllers());

        Stream<Map.Entry<String, SecurityConfigurationRole>> controllersStream = secConf.getRoles().getAdditionalProperties().entrySet().stream();
        if (!permissions.getRoles().isEmpty()) {
            controllersStream = controllersStream.filter(c -> permissions.getRoles() != null && permissions.getRoles().contains(c.getKey()));
        }
        controllersStream.flatMap(e -> e.getValue().getPermissions().getControllers().getAdditionalProperties().keySet().stream()).filter(
                s -> s != null && !s.isEmpty()).forEach(controller -> permissions.getControllers().setAdditionalProperty(controller,
                        getControllerPermissions(controller)));

        return permissions;
    }

    private JocPermissions getJocPermissions() {

        JocPermissions jocPermissions = new JocPermissions(false, new Administration(new Accounts(), new Settings(), new Controllers(),
                new Certificates(), new Customization()), new Cluster(), new Inventory(), new Calendars(), new Documentations(), new AuditLog(),
                new DailyPlan(), new FileTransfer(), new Notification(), new Encipherment(), new Reports(), new Others());

        if (currentAccount != null && currentAccount.getCurrentSubject() != null) {

            jocPermissions.setGetLog(haveRight("", "sos:products:joc:get_log"));
            Administration admin = jocPermissions.getAdministration();
            admin.getAccounts().setView(haveRight("", "sos:products:joc:administration:accounts:view") || haveRight("", "sos:products:joc:adminstration:accounts:view"));
            admin.getAccounts().setManage(haveRight("", "sos:products:joc:administration:accounts:manage") || haveRight("", "sos:products:joc:adminstration:accounts:manage"));
            admin.getCertificates().setView(haveRight("", "sos:products:joc:administration:certificates:view") || haveRight("", "sos:products:joc:adminstration:certificates:view"));
            admin.getCertificates().setManage(haveRight("", "sos:products:joc:administration:certificates:manage") || haveRight("", "sos:products:joc:adminstration:certificates:manage"));
            admin.getControllers().setView(haveRight("", "sos:products:joc:administration:controllers:view") || haveRight("", "sos:products:joc:adminstration:controllers:view"));
            admin.getControllers().setManage(haveRight("", "sos:products:joc:administration:controllers:manage"));
            admin.getSettings().setView(haveRight("", "sos:products:joc:administration:settings:view") || haveRight("", "sos:products:joc:adminstration:settings:view"));
            admin.getSettings().setManage(haveRight("", "sos:products:joc:administration:settings:manage") || haveRight("", "sos:products:joc:adminstration:settings:manage"));
            admin.getCustomization().setView(haveRight("", "sos:products:joc:administration:customization:view") || haveRight("", "sos:products:joc:adminstration:customization:view"));
            admin.getCustomization().setManage(haveRight("", "sos:products:joc:administration:customization:manage") || haveRight("", "sos:products:joc:adminstration:customization:manage"));
            admin.getCustomization().setShare(haveRight("", "sos:products:joc:administration:customization:share") || haveRight("", "sos:products:joc:adminstration:customization:share"));
            jocPermissions.setAdministration(admin);
            jocPermissions.getAuditLog().setView(haveRight("", "sos:products:joc:auditlog:view"));
            jocPermissions.getCalendars().setView(haveRight("", "sos:products:joc:calendars:view"));
            jocPermissions.getCluster().setManage(haveRight("", "sos:products:joc:cluster:manage"));
            jocPermissions.getDailyPlan().setView(haveRight("", "sos:products:joc:dailyplan:view"));
            jocPermissions.getDailyPlan().setManage(haveRight("", "sos:products:joc:dailyplan:manage"));
            jocPermissions.getDocumentations().setView(haveRight("", "sos:products:joc:documentations:view"));
            jocPermissions.getDocumentations().setManage(haveRight("", "sos:products:joc:documentations:manage"));
            jocPermissions.getFileTransfer().setView(haveRight("", "sos:products:joc:filetransfer:view"));
            jocPermissions.getFileTransfer().setManage(haveRight("", "sos:products:joc:filetransfer:manage"));
            jocPermissions.getInventory().setView(haveRight("", "sos:products:joc:inventory:view"));
            jocPermissions.getInventory().setManage(haveRight("", "sos:products:joc:inventory:manage"));
            jocPermissions.getInventory().setDeploy(haveRight("", "sos:products:joc:inventory:deploy"));
            jocPermissions.getNotification().setView(haveRight("", "sos:products:joc:notification:view"));
            jocPermissions.getNotification().setManage(haveRight("", "sos:products:joc:notification:manage"));
            jocPermissions.getReports().setView(haveRight("", "sos:products:joc:reports:view"));
            jocPermissions.getReports().setManage(haveRight("", "sos:products:joc:reports:manage"));
            jocPermissions.getOthers().setView(haveRight("", "sos:products:joc:others:view"));
            jocPermissions.getOthers().setManage(haveRight("", "sos:products:joc:others:manage"));
            jocPermissions.getEncipherment().setEncrypt(haveRight("", "sos:products:joc:encipherment:encrypt"));
        }

        return jocPermissions;
    }

    private ControllerPermissions getControllerPermissions(String controllerId) {

        ControllerPermissions controllerPermissions = new ControllerPermissions(false, false, false, false, false, new Deployments(), new Orders(),
                new Agents(), new NoticeBoards(), new Locks(), new Workflows());

        if (currentAccount != null && currentAccount.getCurrentSubject() != null) {

            controllerPermissions.setView(haveRight(controllerId, "sos:products:controller:view"));
            controllerPermissions.setRestart(haveRight(controllerId, "sos:products:controller:restart"));
            controllerPermissions.setTerminate(haveRight(controllerId, "sos:products:controller:terminate"));
            controllerPermissions.setGetLog(haveRight(controllerId, "sos:products:controller:get_log"));
            controllerPermissions.setSwitchOver(haveRight(controllerId, "sos:products:controller:switch_over"));
            controllerPermissions.getAgents().setView(haveRight(controllerId, "sos:products:controller:agents:view"));
            controllerPermissions.getDeployments().setView(haveRight(controllerId, "sos:products:controller:deployment:view"));
            controllerPermissions.getDeployments().setDeploy(haveRight(controllerId, "sos:products:controller:deployment:deploy"));
            controllerPermissions.getNoticeBoards().setView(haveRight(controllerId, "sos:products:controller:noticeboards:view"));
            controllerPermissions.getNoticeBoards().setPost(haveRight(controllerId, "sos:products:controller:noticeboards:post"));
            controllerPermissions.getNoticeBoards().setDelete(haveRight(controllerId, "sos:products:controller:noticeboards:delete"));
            controllerPermissions.getLocks().setView(haveRight(controllerId, "sos:products:controller:locks:view"));
            controllerPermissions.getWorkflows().setView(haveRight(controllerId, "sos:products:controller:workflows:view"));
            controllerPermissions.getOrders().setView(haveRight(controllerId, "sos:products:controller:orders:view"));
            controllerPermissions.getOrders().setCreate(haveRight(controllerId, "sos:products:controller:orders:create"));
            controllerPermissions.getOrders().setCancel(haveRight(controllerId, "sos:products:controller:orders:cancel"));
            controllerPermissions.getOrders().setModify(haveRight(controllerId, "sos:products:controller:orders:modify"));
            controllerPermissions.getOrders().setSuspendResume(haveRight(controllerId, "sos:products:controller:orders:suspend_resume"));
            controllerPermissions.getOrders().setResumeFailed(haveRight(controllerId, "sos:products:controller:orders:resume_failed"));
            controllerPermissions.getOrders().setConfirm(haveRight(controllerId, "sos:products:controller:orders:confirm"));
            controllerPermissions.getOrders().setManagePositions(haveRight(controllerId, "sos:products:controller:orders:manage_positions"));

        }
        return controllerPermissions;
    }

    private boolean isPermitted(String controllerId, String permission) {
        return (currentAccount != null && currentAccount.isPermitted(controllerId, permission) && currentAccount.isAuthenticated());
    }

    private boolean haveRight(String controllerId, String permission) {
        return isPermitted(controllerId, permission);
    }

//    private void addRole(List<String> sosRoles, String role, boolean forAccount) {
//        if (currentAccount != null && (!forAccount || currentAccount.hasRole(role)) && currentAccount.isAuthenticated()) {
//            if (!sosRoles.contains(role)) {
//                sosRoles.add(role);
//            }
//        }
//    }
}