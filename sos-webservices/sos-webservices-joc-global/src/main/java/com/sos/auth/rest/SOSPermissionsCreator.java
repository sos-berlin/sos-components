package com.sos.auth.rest;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Stream;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.Ini.Section;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.rest.permission.model.ObjectFactory;
import com.sos.auth.rest.permission.model.SOSPermissionRoles;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.security.SOSSecurityConfiguration;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.Permissions;
import com.sos.joc.model.security.SecurityConfiguration;
import com.sos.joc.model.security.permissions.ControllerPermissions;
import com.sos.joc.model.security.permissions.JocPermissions;
import com.sos.joc.model.security.permissions.SecurityConfigurationRole;
import com.sos.joc.model.security.permissions.controller.Agents;
import com.sos.joc.model.security.permissions.controller.Deployments;
import com.sos.joc.model.security.permissions.controller.Locks;
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
    private SOSShiroCurrentUser currentUser;
    private SOSPermissionRoles roles;
    private Ini ini;

    public SOSPermissionsCreator(SOSShiroCurrentUser currentUser) {
        super();
        this.currentUser = currentUser;
    }

    public void loginFromAccessToken(String accessToken) throws JocException, InvalidSessionException, JsonParseException, JsonMappingException,
            IOException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            if (Globals.jocWebserviceDataContainer.getCurrentUsersList() == null || Globals.jocWebserviceDataContainer.getCurrentUsersList().getUser(
                    accessToken) == null) {

                LOGGER.debug("loginFromAccessToken --> hand over session.");
                LOGGER.debug("loginFromAccessToken --> login with accessToken=" + accessToken);
                if (Globals.sosCockpitProperties == null) {
                    Globals.sosCockpitProperties = new JocCockpitProperties();
                }
                Globals.setProperties();

                sosHibernateSession = Globals.createSosHibernateStatelessConnection("JOC: loginFromAccessToken");
                LOGGER.debug("loginFromAccessToken --> hibernateSession created");
                SOSShiroIniShare sosShiroIniShare = new SOSShiroIniShare(sosHibernateSession);
                try {
                    sosShiroIniShare.provideIniFile();
                    LOGGER.debug("loginFromAccessToken --> ini file provided");
                } catch (IOException e) {
                    throw new JocException(e);
                } catch (SOSHibernateException e) {
                    throw new DBInvalidDataException(e);
                }
                Globals.disconnect(sosHibernateSession);

                @SuppressWarnings("deprecation")
                org.apache.shiro.config.IniSecurityManagerFactory factory = Globals.getShiroIniSecurityManagerFactory();
                SecurityManager securityManager = factory.getInstance();
                SecurityUtils.setSecurityManager(securityManager);
                LOGGER.debug("loginFromAccessToken --> securityManager created");

                SessionKey s = new DefaultSessionKey(accessToken);
                Session session = SecurityUtils.getSecurityManager().getSession(s);

                if (session != null) {
                    Subject subject = new Subject.Builder(securityManager).sessionId(accessToken).session(session).buildSubject();
                    LOGGER.debug("loginFromAccessToken --> subject created");
                    if (subject.isAuthenticated()) {

                        LOGGER.debug("loginFromAccessToken --> subject is authenticated");
                        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
                        currentUser = new SOSShiroCurrentUser((String) subject.getPrincipals().getPrimaryPrincipal(), "", "");

                        if (Globals.jocWebserviceDataContainer.getCurrentUsersList() == null) {
                            Globals.jocWebserviceDataContainer.setCurrentUsersList(new SOSShiroCurrentUsersList());
                        }

                        LOGGER.debug("loginFromAccessToken --> removeTimedOutUser");
                        Globals.jocWebserviceDataContainer.getCurrentUsersList().removeTimedOutUser(currentUser.getUsername());

                        currentUser.setCurrentSubject(subject);
                        currentUser.setAccessToken(accessToken);

                        Permissions sosPermissionJocCockpitControllers = null;
                        if (subject.getSession().getAttribute("username_joc_permissions") != null) {
                            sosPermissionJocCockpitControllers = Globals.objectMapper.readValue((byte[]) subject.getSession().getAttribute(
                                    "username_joc_permissions"), Permissions.class);
                            currentUser.setRoles(sosPermissionJocCockpitControllers.getRoles());
                        } else {
                            LOGGER.warn("Could not read username_joc_permissions after fail over in the session object for access token "
                                    + accessToken);
                            SOSSecurityConfiguration sosSecurityConfiguration = new SOSSecurityConfiguration();
                            SecurityConfiguration entity = sosSecurityConfiguration.readConfigurationFromFilesystem();
                            currentUser.setRoles(entity);
                            sosPermissionJocCockpitControllers = createJocCockpitPermissionControllerObjectList(accessToken, entity);
                        }

                        LOGGER.debug("loginFromAccessToken --> JocCockpitPermissionControllerObjectList created");
                        currentUser.setSosPermissionJocCockpitControllers(sosPermissionJocCockpitControllers);
                        currentUser.initFolders();
                        LOGGER.debug("loginFromAccessToken --> folders initialized");

                        Section section = getIni().getSection("folders");
                        if (section != null) {
                            for (String role : section.keySet()) {
                                currentUser.addFolder(role, section.get(role));
                            }
                        }

                        Globals.jocWebserviceDataContainer.getCurrentUsersList().addUser(currentUser);
                    }
                }
            }
        } catch (UnknownSessionException e) {
            LOGGER.debug(e.getMessage());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }
    
    public Permissions createJocCockpitPermissionControllerObjectList(String accessToken, SecurityConfiguration secConf) {
        Permissions permissions = new Permissions(currentUser.getRoles(), getJocPermissions(), getControllerPermissions(""),
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
        
        JocPermissions jocPermissions = new JocPermissions(new Administration(new Accounts(), new Settings(), new Controllers(), new Certificates(),
                new Customization()), new Cluster(), new Inventory(), new Calendars(), new Documentations(), new AuditLog(), new DailyPlan(),
                new FileTransfer(), new Notification(), new Others());

        if (currentUser != null && currentUser.getCurrentSubject() != null) {
            
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

        ControllerPermissions controllerPermissions = new ControllerPermissions(false, false, false, false, new Deployments(), new Orders(),
                new Agents(), new Locks(), new Workflows());

        if (currentUser != null && currentUser.getCurrentSubject() != null) {

            controllerPermissions.setView(haveRight(controllerId, "sos:products:controller:view"));
            controllerPermissions.setRestart(haveRight(controllerId, "sos:products:controller:restart"));
            controllerPermissions.setTerminate(haveRight(controllerId, "sos:products:controller:terminate"));
            controllerPermissions.setSwitchOver(haveRight(controllerId, "sos:products:controller:switch_over"));
            controllerPermissions.getAgents().setView(haveRight(controllerId, "sos:products:controller:agents:view"));
            controllerPermissions.getDeployments().setView(haveRight(controllerId, "sos:products:controller:deployment:view"));
            controllerPermissions.getDeployments().setDeploy(haveRight(controllerId, "sos:products:controller:deployment:deploy"));
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
        return (currentUser != null && currentUser.isPermitted(controllerId, permission) && currentUser.isAuthenticated());
    }

    private boolean haveRight(String controllerId, String permission) {
        return isPermitted(controllerId, permission);
    }

    private void addRole(List<String> sosRoles, String role, boolean forUser) {
        if (currentUser != null && (!forUser || currentUser.hasRole(role)) && currentUser.isAuthenticated()) {
            if (!sosRoles.contains(role)) {
                sosRoles.add(role);
            }
        }
    }

    public SOSPermissionRoles getRoles(boolean forUser) {

        if (roles == null || !forUser) {
            ObjectFactory o = new ObjectFactory();
            roles = o.createSOSPermissionRoles();

            ini = getIni();
            Section s = ini.getSection("roles");

            if (s != null) {
                for (String role : s.keySet()) {
                    addRole(roles.getSOSPermissionRole(), role, forUser);
                }
            }

            s = ini.getSection("folders");
            if (s != null) {
                for (String role : s.keySet()) {
                    String[] key = role.split("\\|");
                    if (key.length == 1) {
                        addRole(roles.getSOSPermissionRole(), role, forUser);
                    }
                    if (key.length == 2) {
                        addRole(roles.getSOSPermissionRole(), key[1], forUser);
                    }
                }
            }
        }
        return roles;
    }

    public Ini getIni() {

        if (ini == null) {
            return Globals.getIniFromSecurityManagerFactory();
        }
        return ini;
    }

}