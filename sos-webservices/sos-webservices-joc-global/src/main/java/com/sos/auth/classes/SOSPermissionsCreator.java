package com.sos.auth.classes;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.sos.joc.Globals;
import com.sos.joc.model.security.configuration.SecurityConfiguration;
import com.sos.joc.model.security.configuration.SecurityConfigurationRole;
import com.sos.joc.model.security.configuration.permissions.ControllerPermissions;
import com.sos.joc.model.security.configuration.permissions.JocPermissions;
import com.sos.joc.model.security.configuration.permissions.Permissions;
import com.sos.joc.model.security.configuration.permissions.joc.Administration;

public class SOSPermissionsCreator {

    private SOSAuthCurrentAccount currentAccount;
    private boolean only4EyesRole = false;

    public SOSPermissionsCreator(SOSAuthCurrentAccount currentAccount) {
        super();
        this.currentAccount = currentAccount;
    }

    public Permissions createJocCockpitPermissionControllerObjectList(SecurityConfiguration secConf) {
        only4EyesRole = false;
        Permissions permissions = new Permissions(currentAccount.getRoles(), getJocPermissions(), getControllerPermissions(""),
                new com.sos.joc.model.security.configuration.permissions.Controllers());

        Set<String> uniqueRoles = currentAccount.getStringsOfUniqueRoles();
        Stream<Map.Entry<String, SecurityConfigurationRole>> controllersStream = secConf.getRoles().getAdditionalProperties().entrySet().stream();
        if (permissions.getRoles() != null && !permissions.getRoles().isEmpty()) {
            controllersStream = controllersStream.filter(c -> uniqueRoles.contains(c.getKey()));
        }
        controllersStream.flatMap(e -> e.getValue().getPermissions().getControllers().getAdditionalProperties().keySet().stream()).filter(
                s -> s != null && !s.isEmpty()).distinct().forEach(controller -> permissions.getControllers().setAdditionalProperty(controller,
                        getControllerPermissions(controller)));

        return permissions;
    }
    
    public Permissions create4EyesJocCockpitPermissionControllerObjectList(SecurityConfiguration secConf) {
        String approvalRequestorRole = Globals.getConfigurationGlobalsJoc().getApprovalRequestorRole().getValue();
        if (currentAccount.getRoles().contains(approvalRequestorRole)) {
            only4EyesRole = true;
            Permissions permissions = new Permissions(Collections.singleton(approvalRequestorRole), getJocPermissions(), getControllerPermissions(""),
                    new com.sos.joc.model.security.configuration.permissions.Controllers());

            secConf.getRoles().getAdditionalProperties().entrySet().stream().filter(c -> permissions.getRoles().contains(c.getKey())).flatMap(e -> e
                    .getValue().getPermissions().getControllers().getAdditionalProperties().keySet().stream()).filter(s -> s != null && !s.isEmpty())
                    .distinct().forEach(controller -> permissions.getControllers().setAdditionalProperty(controller, getControllerPermissions(
                            controller)));

            return permissions;
        }

        return null;
    }

    private JocPermissions getJocPermissions() {

        JocPermissions jocPermissions = new JocPermissions();

        if (currentAccount != null && currentAccount.getCurrentSubject() != null) {

            jocPermissions.setGetLog(haveRight(jocPermissions.getGetLogString()));
            Administration admin = jocPermissions.getAdministration();
            admin.getAccounts().setView(haveRight(admin.getAccounts().getViewString()));
            admin.getAccounts().setManage(haveRight(admin.getAccounts().getManageString()));
            admin.getCertificates().setView(haveRight(admin.getCertificates().getViewString()));
            admin.getCertificates().setManage(haveRight(admin.getCertificates().getManageString()));
            admin.getControllers().setView(haveRight(admin.getControllers().getViewString()));
            admin.getControllers().setManage(haveRight(admin.getControllers().getManageString()));
            admin.getSettings().setView(haveRight(admin.getSettings().getViewString()));
            admin.getSettings().setManage(haveRight(admin.getSettings().getManageString()));
            admin.getCustomization().setView(haveRight(admin.getCustomization().getViewString()));
            admin.getCustomization().setManage(haveRight(admin.getCustomization().getManageString()));
            admin.getCustomization().setShare(haveRight(admin.getCustomization().getShareString()));
            jocPermissions.setAdministration(admin);
            jocPermissions.getAuditLog().setView(haveRight(jocPermissions.getAuditLog().getViewString()));
            jocPermissions.getCalendars().setView(haveRightWithFolder(jocPermissions.getCalendars().getViewString()));
            jocPermissions.getCluster().setManage(haveRight(jocPermissions.getCluster().getManageString()));
            jocPermissions.getDailyPlan().setView(haveRightWithFolder(jocPermissions.getDailyPlan().getViewString()));
            jocPermissions.getDailyPlan().setManage(haveRightWithFolder(jocPermissions.getDailyPlan().getManageString()));
            jocPermissions.getDocumentations().setView(haveRightWithFolder(jocPermissions.getDocumentations().getViewString()));
            jocPermissions.getDocumentations().setManage(haveRightWithFolder(jocPermissions.getDocumentations().getManageString()));
            jocPermissions.getFileTransfer().setView(haveRightWithFolder(jocPermissions.getFileTransfer().getViewString()));
            jocPermissions.getFileTransfer().setManage(haveRightWithFolder(jocPermissions.getFileTransfer().getManageString()));
            jocPermissions.getInventory().setView(haveRightWithFolder(jocPermissions.getInventory().getViewString()));
            jocPermissions.getInventory().setManage(haveRightWithFolder(jocPermissions.getInventory().getManageString()));
            jocPermissions.getInventory().setDeploy(haveRightWithFolder(jocPermissions.getInventory().getDeployString()));
            jocPermissions.getNotification().setView(haveRight(jocPermissions.getNotification().getViewString()));
            jocPermissions.getNotification().setManage(haveRight(jocPermissions.getNotification().getManageString()));
            jocPermissions.getReports().setView(haveRightWithFolder(jocPermissions.getReports().getViewString()));
            jocPermissions.getReports().setManage(haveRightWithFolder(jocPermissions.getReports().getManageString()));
            jocPermissions.getOthers().setView(haveRight(jocPermissions.getOthers().getViewString()));
            jocPermissions.getOthers().setManage(haveRight(jocPermissions.getOthers().getManageString()));
            jocPermissions.getEncipherment().setEncrypt(haveRight(jocPermissions.getEncipherment().getEncryptString()));
        }

        return jocPermissions;
    }

    private ControllerPermissions getControllerPermissions(String controllerId) {

        ControllerPermissions controllerPermissions = new ControllerPermissions();

        if (currentAccount != null && currentAccount.getCurrentSubject() != null) {

            controllerPermissions.setView(haveRight(controllerId, controllerPermissions.getViewString()));
            controllerPermissions.setRestart(haveRight(controllerId, controllerPermissions.getRestartString()));
            controllerPermissions.setTerminate(haveRight(controllerId, controllerPermissions.getTerminateString()));
            controllerPermissions.setGetLog(haveRight(controllerId, controllerPermissions.getGetLogString()));
            controllerPermissions.setSwitchOver(haveRight(controllerId, controllerPermissions.getSwithOverString()));
            controllerPermissions.getAgents().setView(haveRight(controllerId, controllerPermissions.getAgents().getViewString()));
            controllerPermissions.getDeployments().setView(haveRight(controllerId, controllerPermissions.getDeployments().getViewString()));
            controllerPermissions.getDeployments().setDeploy(haveRightWithFolder(controllerId, controllerPermissions.getDeployments().getDeployString()));
            controllerPermissions.getNoticeBoards().setView(haveRightWithFolder(controllerId, controllerPermissions.getNoticeBoards().getViewString()));
            controllerPermissions.getNoticeBoards().setPost(haveRightWithFolder(controllerId, controllerPermissions.getNoticeBoards().getPostString()));
            controllerPermissions.getNoticeBoards().setDelete(haveRightWithFolder(controllerId, controllerPermissions.getNoticeBoards().getDeleteString()));
            controllerPermissions.getLocks().setView(haveRightWithFolder(controllerId, controllerPermissions.getLocks().getViewString()));
            controllerPermissions.getWorkflows().setView(haveRightWithFolder(controllerId, controllerPermissions.getWorkflows().getViewString()));
            controllerPermissions.getOrders().setView(haveRightWithFolder(controllerId, controllerPermissions.getOrders().getViewString()));
            controllerPermissions.getOrders().setCreate(haveRightWithFolder(controllerId, controllerPermissions.getOrders().getCreateString()));
            controllerPermissions.getOrders().setCancel(haveRightWithFolder(controllerId, controllerPermissions.getOrders().getCancelString()));
            controllerPermissions.getOrders().setModify(haveRightWithFolder(controllerId, controllerPermissions.getOrders().getModifyString()));
            controllerPermissions.getOrders().setSuspendResume(haveRightWithFolder(controllerId, controllerPermissions.getOrders().getSuspendResumeString()));
            controllerPermissions.getOrders().setResumeFailed(haveRightWithFolder(controllerId, controllerPermissions.getOrders().getResumeFailedString()));
            controllerPermissions.getOrders().setConfirm(haveRightWithFolder(controllerId, controllerPermissions.getOrders().getConfirmString()));
            controllerPermissions.getOrders().setManagePositions(haveRightWithFolder(controllerId, controllerPermissions.getOrders().getManagePositionsString()));

        }
        return controllerPermissions;
    }

    private boolean isPermitted(String controllerId, String permission, boolean considerFolders, boolean isControllerPermission) {
        return currentAccount.isAuthenticated() && currentAccount.isPermitted(controllerId, permission, considerFolders, isControllerPermission, only4EyesRole);
    }

    private boolean haveRightWithFolder(String controllerId, String permission) {
        return isPermitted(controllerId, permission, true, true);
    }
    
    private boolean haveRight(String controllerId, String permission) {
        return isPermitted(controllerId, permission, false, true);
    }
    
    private boolean haveRightWithFolder(String permission) {
        return isPermitted("", permission, true, false);
    }
    
    private boolean haveRight(String permission) {
        return isPermitted("", permission, false, false);
    }

//    private void addRole(List<String> sosRoles, String role, boolean forAccount) {
//        if (currentAccount != null && (!forAccount || currentAccount.hasRole(role)) && currentAccount.isAuthenticated()) {
//            if (!sosRoles.contains(role)) {
//                sosRoles.add(role);
//            }
//        }
//    }
}