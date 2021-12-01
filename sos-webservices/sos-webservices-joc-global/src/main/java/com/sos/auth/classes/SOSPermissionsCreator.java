package com.sos.auth.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.shiro.config.Ini;
import org.apache.shiro.config.Ini.Section;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.permission.model.ObjectFactory;
import com.sos.auth.classes.permission.model.SOSPermissionRoles;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.authentication.DBItemIamAccount2Roles;
import com.sos.joc.db.authentication.DBItemIamRole;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.model.security.Permissions;
import com.sos.joc.model.security.SecurityConfiguration;
import com.sos.joc.model.security.permissions.ControllerPermissions;
import com.sos.joc.model.security.permissions.JocPermissions;
import com.sos.joc.model.security.permissions.SecurityConfigurationRole;
import com.sos.joc.model.security.permissions.controller.Agents;
import com.sos.joc.model.security.permissions.controller.Deployments;
import com.sos.joc.model.security.permissions.controller.Locks;
import com.sos.joc.model.security.permissions.controller.NoticeBoards;
import com.sos.joc.model.security.permissions.controller.Orders;
import com.sos.joc.model.security.permissions.controller.Workflows;
import com.sos.joc.model.security.permissions.joc.Administration;
import com.sos.joc.model.security.permissions.joc.AuditLog;
import com.sos.joc.model.security.permissions.joc.Calendars;
import com.sos.joc.model.security.permissions.joc.Cluster;
import com.sos.joc.model.security.permissions.joc.DailyPlan;
import com.sos.joc.model.security.permissions.joc.Documentations;
import com.sos.joc.model.security.permissions.joc.FileTransfer;
import com.sos.joc.model.security.permissions.joc.Inventory;
import com.sos.joc.model.security.permissions.joc.Notification;
import com.sos.joc.model.security.permissions.joc.Others;
import com.sos.joc.model.security.permissions.joc.admin.Accounts;
import com.sos.joc.model.security.permissions.joc.admin.Certificates;
import com.sos.joc.model.security.permissions.joc.admin.Controllers;
import com.sos.joc.model.security.permissions.joc.admin.Customization;
import com.sos.joc.model.security.permissions.joc.admin.Settings;

public class SOSPermissionsCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSPermissionsCreator.class);
    private SOSAuthCurrentAccount currentAccount;
    private SOSPermissionRoles roles;
    private Ini ini;

    public SOSPermissionsCreator(SOSAuthCurrentAccount currentAccount) {
        super();
        this.currentAccount = currentAccount;
    }

    public Map<String, List<String>> getMapOfFolder() {
        Map<String, List<String>> resultMap = new HashMap<String, List<String>>();
        if (SOSAuthHelper.isShiro()) {
            Section section = getIni().getSection("folders");
            if (section != null) {
                for (String role : section.keySet()) {
                    if (resultMap.get(role) == null) {
                        resultMap.put(role, new ArrayList<String>());
                    }
                    resultMap.get(role).add(section.get(role));
                }
            }
        } else {
            resultMap.putAll(currentAccount.getCurrentSubject().getMapOfFolderPermissions());
        }

        return resultMap;
    }

    public Permissions createJocCockpitPermissionControllerObjectList(SecurityConfiguration secConf) {
        Permissions permissions = new Permissions(currentAccount.getRoles(), getJocPermissions(), getControllerPermissions(""),
                new com.sos.joc.model.security.permissions.Controllers());

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
                new DailyPlan(), new FileTransfer(), new Notification(), new Others());

        if (currentAccount != null && currentAccount.getCurrentSubject() != null) {

            jocPermissions.setGetLog(haveRight("", "sos:products:joc:get_log"));
            Administration admin = jocPermissions.getAdministration();
            admin.getAccounts().setView(haveRight("", "sos:products:joc:adminstration:accounts:view"));
            admin.getAccounts().setManage(haveRight("", "sos:products:joc:adminstration:accounts:manage"));
            admin.getCertificates().setView(haveRight("", "sos:products:joc:adminstration:certificates:view"));
            admin.getCertificates().setManage(haveRight("", "sos:products:joc:adminstration:certificates:manage"));
            admin.getControllers().setView(haveRight("", "sos:products:joc:adminstration:controllers:view"));
            admin.getControllers().setManage(haveRight("", "sos:products:joc:adminstration:controllers:manage"));
            admin.getSettings().setView(haveRight("", "sos:products:joc:adminstration:settings:view"));
            admin.getSettings().setManage(haveRight("", "sos:products:joc:adminstration:settings:manage"));
            admin.getCustomization().setView(haveRight("", "sos:products:joc:adminstration:customization:view"));
            admin.getCustomization().setManage(haveRight("", "sos:products:joc:adminstration:customization:manage"));
            admin.getCustomization().setShare(haveRight("", "sos:products:joc:adminstration:customization:share"));
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
            jocPermissions.getOthers().setView(haveRight("", "sos:products:joc:others:view"));
            jocPermissions.getOthers().setManage(haveRight("", "sos:products:joc:others:manage"));
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

        }
        return controllerPermissions;
    }

    private boolean isPermitted(String controllerId, String permission) {
        return (currentAccount != null && currentAccount.isPermitted(controllerId, permission) && currentAccount.isAuthenticated());
    }

    private boolean haveRight(String controllerId, String permission) {
        return isPermitted(controllerId, permission);
    }

    private void addRole(List<String> sosRoles, String role, boolean forAccount) {
        if (currentAccount != null && (!forAccount || currentAccount.hasRole(role)) && currentAccount.isAuthenticated()) {
            if (!sosRoles.contains(role)) {
                sosRoles.add(role);
            }
        }
    }

    private SOSPermissionRoles getRolesFromShiro() {
        ObjectFactory o = new ObjectFactory();
        roles = o.createSOSPermissionRoles();

        ini = getIni();
        Section s = ini.getSection("roles");

        if (s != null) {
            for (String role : s.keySet()) {
                addRole(roles.getSOSPermissionRole(), role, false);
            }
        }

        s = ini.getSection("folders");
        if (s != null) {
            for (String role : s.keySet()) {
                String[] key = role.split("\\|");
                if (key.length == 1) {
                    addRole(roles.getSOSPermissionRole(), role, false);
                }
                if (key.length == 2) {
                    addRole(roles.getSOSPermissionRole(), key[1], false);
                }
            }
        }
        return roles;

    }

    private SOSPermissionRoles getRolesFromDb() throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = null;
        ObjectFactory o = new ObjectFactory();
        roles = o.createSOSPermissionRoles();
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSPermissionCreator");
            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            List<DBItemIamRole> listOfRoles = iamAccountDBLayer.getListOfAllRoles();
            for (DBItemIamRole dbItemIamRole : listOfRoles) {
                addRole(roles.getSOSPermissionRole(), dbItemIamRole.getRoleName(), false);
            }
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
        return roles;

    }

    public SOSPermissionRoles getRoles(boolean forAccount){
        if (roles == null || !forAccount) {

            if (SOSAuthHelper.isShiro()) {
                return getRolesFromShiro();
            } else {
                try {
                    return getRolesFromDb();
                } catch (SOSHibernateException e) {
                    
                }
            }
        }

        return roles;
    }

    public Ini getIni() {
        org.apache.shiro.config.IniSecurityManagerFactory factory = null;
        String iniFile = Globals.getShiroIniInClassPath();
        factory = new IniSecurityManagerFactory(Globals.getIniFileForShiro(iniFile));
        return factory.getIni();
    }

}