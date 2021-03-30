package com.sos.auth.rest;

import java.io.File;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.model.security.Permissions;
import com.sos.joc.model.security.permissions.ControllerPermissions;
import com.sos.joc.model.security.permissions.JocPermissions;
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
import com.sos.joc.model.security.permissions.joc.admin.Settings;


public class SOSShiroCurrentUser {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSShiroCurrentUser.class);
    private Subject currentSubject;
    private String username;
    private String password;
    private String accessToken;
    private String authorization;
    private Boolean haveAnyIpPermission;
    private HttpServletRequest httpServletRequest;

    private Permissions sosPermissionJocCockpitControllers;
    private SOSShiroFolderPermissions sosShiroFolderPermissions;
    private SOSShiroFolderPermissions sosShiroCalendarFolderPermissions;

    public SOSShiroCurrentUser(String username, String password) {
        super();
        initFolders();
        this.username = username;
        this.password = password;
    }

    public SOSShiroCurrentUser(String username, String password, String authorization) {
        super();
        this.username = username;
        this.authorization = authorization;
        this.password = password;
    }

    public ControllerPermissions getControllerPermissions(String controllerId) {
        if (sosPermissionJocCockpitControllers == null) {
            sosPermissionJocCockpitControllers = initPermissions();
        }

        if (httpServletRequest != null) {
            String ip = getCallerIpAddress();
            String ipControllerKey = "ip=" + ip + ":" + controllerId;
            String ipKey = "ip=" + ip;

            if (sosPermissionJocCockpitControllers.getAdditionalProperties().containsKey(ipControllerKey)) {
                return sosPermissionJocCockpitControllers.getAdditionalProperties().get(ipControllerKey);
            }

            if (sosPermissionJocCockpitControllers.getAdditionalProperties().containsKey(ipKey)) {
                return sosPermissionJocCockpitControllers.getAdditionalProperties().get(ipKey);
            }
        }

        if (sosPermissionJocCockpitControllers.getAdditionalProperties().containsKey(controllerId)) {
            return sosPermissionJocCockpitControllers.getAdditionalProperties().get(controllerId);
        } else {
            return sosPermissionJocCockpitControllers.getControllerDefaults();
        }
    }
    
    public JocPermissions getJocPermissions() {
        if (sosPermissionJocCockpitControllers == null) {
            sosPermissionJocCockpitControllers = initPermissions();
        }
        return sosPermissionJocCockpitControllers.getJoc();
    }

    public Permissions getSosPermissionJocCockpitControllers() {
        return sosPermissionJocCockpitControllers;
    }

    public void setSosPermissionJocCockpitControllers(Permissions sosPermissionJocCockpitControllers) {
        this.sosPermissionJocCockpitControllers = sosPermissionJocCockpitControllers;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Subject getCurrentSubject() {
        return currentSubject;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setCurrentSubject(Subject currentSubject) {
        this.currentSubject = currentSubject;
    }

    public boolean hasRole(String role) {
        if (currentSubject != null) {
            return currentSubject.hasRole(role);
        } else {
            return false;
        }
    }
    
    protected static Permissions initPermissions() {
        Administration administration = new Administration(new Accounts(), new Settings(), new Controllers(), new Certificates());
        ControllerPermissions controllerDefaults = new ControllerPermissions(false, false, false, false, new Deployments(), new Orders(), new Agents(),
                new Locks(), new Workflows());
        JocPermissions joc = new JocPermissions(administration, new Cluster(), new Inventory(), new Calendars(), new Documentations(), new AuditLog(),
                new DailyPlan(), new FileTransfer(), new Notification(), new Others());
        return new Permissions(joc, controllerDefaults);
    }
    
    private Boolean getHaveAnyIpPermission() {
        if (this.haveAnyIpPermission == null) {

            if (currentSubject != null) {
                SOSListOfPermissions sosListOfPermissions = new SOSListOfPermissions(this,false);
 
                this.haveAnyIpPermission = false;
                String[] ipParts = this.getCallerIpAddress().split("\\.");
                for (String p : sosListOfPermissions.getSosPermissionShiro().getSOSPermissions().getSOSPermissionListJoc().getSOSPermission()) {
                    if (this.haveAnyIpPermission) {
                        break;
                    }
                    String es = "ip=";

                    for (int i = 0; i < ipParts.length; i++) {
                        es = es + ipParts[i];
                        this.haveAnyIpPermission = currentSubject.isPermitted(es + ":" + p) || currentSubject.isPermitted("-" + es + ":" + p);
                        if (this.haveAnyIpPermission) {
                            break;
                        }
                        es = es + ".";
                    }
                }
            }
        }
        return this.haveAnyIpPermission;
    }

    private boolean getExcludedController(String permission, String controllerId) {
        boolean excluded = false;
        if (currentSubject != null) {
            Path path = Paths.get(permission.replace(':', '/'));
            int nameCount = path.getNameCount();
            for (int i = 0; i < nameCount - 1; i++) {
                if (excluded) {
                    break;
                }
                String s = path.subpath(0, nameCount - i).toString().replace(File.separatorChar, ':');
                excluded = currentSubject.isPermitted("-" + s) || currentSubject.isPermitted("-" + controllerId + ":" + s);
            }
        }

        return excluded;
    }

    private boolean getExcludedIp(String permission, String controllerId) {
        String[] ipParts = this.getCallerIpAddress().split("\\.");
        boolean excluded = false;
        String es = "";
        for (int i = 0; i < ipParts.length; i++) {
            es = es + ipParts[i];
            excluded = excluded || getExcludedController(permission, "ip=" + es) || getExcludedController(permission, "ip=" + es + ":" + controllerId);
            if (excluded) {
                break;
            }
            es = es + ".";
        }
        return excluded;
    }

    private boolean getExcluded(String permission, String controllerId) {
        if (this.getHaveAnyIpPermission()) {
            return this.getExcludedController(permission, controllerId) || this.getExcludedIp(permission, controllerId);
        } else {
            return this.getExcludedController(permission, controllerId);
        }
    }

    public boolean testGetExcluded(String permission, String controllerId) {
        return getExcluded(permission, controllerId);
    }

    private boolean ipPermission(String permission, String controller, String[] ipParts, int parts) {
        boolean b = false;
        String s = "";

        for (int i = 0; i < parts; i++) {
            s = s + ipParts[i] + ".";
        }
        s = s + ipParts[parts];

        b = (currentSubject.isPermitted("ip=" + s + ":" + permission) || currentSubject.isPermitted("ip=" + s + ":" + controller + ":" + permission));
        return b;
    }

    private boolean handleIpPermission(String controllerId, String permission) {

        String ipAddress = this.getCallerIpAddress();
        InetAddress inetAddress;
        boolean ipv6 = false;
        try {
            inetAddress = InetAddress.getByName(ipAddress);
            ipv6 = inetAddress instanceof Inet6Address;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        String[] ipParts = ipAddress.split("\\.");

        if ((ipParts.length < 4) && !ipv6 || (ipParts.length < 8 && ipv6)) {
            LOGGER.warn("Wrong ip address found: " + ipAddress);
            return false;
        }

        return ipPermission(permission, controllerId, ipParts, 0) || ipPermission(permission, controllerId, ipParts, 1) || ipPermission(permission, controllerId,
                ipParts, 2) || ipPermission(permission, controllerId, ipParts, 3);

    }

    private boolean getPermissionFromSubject(String controllerId, String permission) {
        if (this.getHaveAnyIpPermission()) {
            return (handleIpPermission(controllerId, permission) || currentSubject.isPermitted(permission) || currentSubject.isPermitted(controllerId + ":"
                    + permission)) && !getExcluded(permission, controllerId);
        } else {
            return (currentSubject.isPermitted(permission) || currentSubject.isPermitted(controllerId + ":" + permission)) && !getExcluded(permission,
                    controllerId);
        }
    }

    public boolean isPermitted(String controllerId, String permission) {
        if (currentSubject != null) {
            return getPermissionFromSubject(controllerId, permission);
        } else {
            return false;
        }
    }

    public boolean isPermitted(String permission) {
        if (currentSubject != null) {
            return getPermissionFromSubject("", permission);
        } else {
            return false;
        }
    }

    public boolean isAuthenticated() {
        if (currentSubject != null) {
            return currentSubject.isAuthenticated();
        } else {
            return false;
        }
    }

    public void initFolders() {
        sosShiroFolderPermissions = new SOSShiroFolderPermissions();
        sosShiroCalendarFolderPermissions = new SOSShiroFolderPermissions("calendar");
    }

    public void addFolder(String role, String folders) {
        if (sosShiroFolderPermissions == null) {
            this.initFolders();
        }

        String jobSchedulerId = "";
        if (role.contains("|")) {
            String[] s = role.split("\\|");
            if (s.length > 1) {
                jobSchedulerId = s[0];
                role = s[1];
            }
        }

        if (hasRole(role)) {
            LOGGER.debug(String.format("Adding folders %s for role %s", folders, role));
            sosShiroFolderPermissions.setFolders(jobSchedulerId, folders);
            sosShiroCalendarFolderPermissions.setFolders(jobSchedulerId, folders);
        }
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public SOSShiroFolderPermissions getSosShiroFolderPermissions() {
        return sosShiroFolderPermissions;
    }

    public SOSShiroFolderPermissions getSosShiroCalendarFolderPermissions() {
        return sosShiroCalendarFolderPermissions;
    }

    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    public String getCallerIpAddress() {
        if (httpServletRequest != null) {
            String s = httpServletRequest.getRemoteAddr();
            if ("0:0:0:0:0:0:0:1".equals(s)) {
                return "127.0.0.1";
            } else {
                return s;
            }
        } else {
            return "0.0.0.0";
        }
    }

    public String getCallerHostName() {
        if (httpServletRequest != null) {
            return httpServletRequest.getRemoteHost();
        } else {
            return "";
        }
    }

    
    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

}
