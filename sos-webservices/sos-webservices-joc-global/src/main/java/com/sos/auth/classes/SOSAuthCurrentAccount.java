package com.sos.auth.classes;

import java.io.File;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.joc.model.security.configuration.SecurityConfiguration;
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
import com.sos.joc.model.security.configuration.permissions.joc.FileTransfer;
import com.sos.joc.model.security.configuration.permissions.joc.Inventory;
import com.sos.joc.model.security.configuration.permissions.joc.Notification;
import com.sos.joc.model.security.configuration.permissions.joc.Others;
import com.sos.joc.model.security.configuration.permissions.joc.admin.Accounts;
import com.sos.joc.model.security.configuration.permissions.joc.admin.Certificates;
import com.sos.joc.model.security.configuration.permissions.joc.admin.Controllers;
import com.sos.joc.model.security.configuration.permissions.joc.admin.Customization;
import com.sos.joc.model.security.configuration.permissions.joc.admin.Settings;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;

public class SOSAuthCurrentAccount {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSAuthCurrentAccount.class);
    private ISOSAuthSubject currentSubject;
    private String accountName;
    private String accessToken;
    private SOSIdentityService identityServices;
    private Map<String, String> identyServiceAccessToken;
    private boolean withAuthorization;
    private Set<String> roles;
    private Boolean haveAnyIpPermission;
    private Set<ISOSAuthSubject> currentSubjects;

    private HttpServletRequest httpServletRequest;

    private Permissions sosPermissionJocCockpitControllers;
    private SOSAuthFolderPermissions sosAuthFolderPermissions;

    public SOSAuthCurrentAccount(String accountName) {
        super();
        initFolders();
        this.accountName = accountName;
    }

    public SOSAuthCurrentAccount(String accountName, boolean withAuthorization) {
        super();
        this.accountName = accountName;
        this.withAuthorization = withAuthorization;
    }

    public ControllerPermissions getControllerPermissions(String controllerId) {
        if (sosPermissionJocCockpitControllers == null) {
            sosPermissionJocCockpitControllers = initPermissions();
        }

        if (sosPermissionJocCockpitControllers.getControllers() != null) {
            if (httpServletRequest != null) {
                String ip = getCallerIpAddress();
                String ipControllerKey = "ip=" + ip + ":" + controllerId;
                String ipKey = "ip=" + ip;

                if (sosPermissionJocCockpitControllers.getControllers().getAdditionalProperties().containsKey(ipControllerKey)) {
                    return sosPermissionJocCockpitControllers.getControllers().getAdditionalProperties().get(ipControllerKey);
                }

                if (sosPermissionJocCockpitControllers.getControllers().getAdditionalProperties().containsKey(ipKey)) {
                    return sosPermissionJocCockpitControllers.getControllers().getAdditionalProperties().get(ipKey);
                }
            }

            if (sosPermissionJocCockpitControllers.getControllers().getAdditionalProperties().containsKey(controllerId)) {
                return sosPermissionJocCockpitControllers.getControllers().getAdditionalProperties().get(controllerId);
            } else {
                return sosPermissionJocCockpitControllers.getControllerDefaults();
            }
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

    public void setAccessToken(String identityServiceName, String accessToken) {
        if (identyServiceAccessToken == null) {
            identyServiceAccessToken = new HashMap<String, String>();
        }
        identyServiceAccessToken.put(identityServiceName, accessToken);
        if (this.accessToken == null) {
            this.accessToken = SOSAuthHelper.createAccessToken();
        }
    }

    public ISOSAuthSubject getCurrentSubject() {
        return currentSubject;
    }

    public String getAccountname() {
        return accountName;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public void setRoles(SecurityConfiguration securityConf) {

        if (this.roles == null) {
            this.roles = new HashSet<String>();
        }
        if (currentSubject != null) {
            this.roles.addAll(Stream.concat(securityConf.getAccounts().stream().filter(account -> account.getRoles() != null).flatMap(
                    account -> account.getRoles().stream()), securityConf.getRoles().getAdditionalProperties().keySet().stream()).filter(
                            role -> currentSubject.hasRole(role)).collect(Collectors.toSet()));

        }
    }

    public Set<String> getRoles() {
        if (roles == null) {
            return Collections.emptySet();
        }
        return roles;
    }

    public String getRolesAsString() {
        if (roles == null) {
            return "";
        }
        return String.join(",", roles);
    }

    public void setCurrentSubject(ISOSAuthSubject currentSubject) {
        if (currentSubjects == null) {
            currentSubjects = new HashSet<ISOSAuthSubject>();
        }
        currentSubjects.add(currentSubject);
        this.currentSubject = currentSubject;
    }

    public boolean hasRole(String role) {
        if (currentSubject != null) {
            return currentSubject.hasRole(role);
        } else {
            return false;
        }
    }

    private Permissions initPermissions() {
        Administration administration = new Administration(new Accounts(), new Settings(), new Controllers(), new Certificates(),
                new Customization());
        ControllerPermissions controllerDefaults = new ControllerPermissions(false, false, false, false, false, new Deployments(), new Orders(),
                new Agents(), new NoticeBoards(), new Locks(), new Workflows());
        JocPermissions joc = new JocPermissions(false, administration, new Cluster(), new Inventory(), new Calendars(), new Documentations(),
                new AuditLog(), new DailyPlan(), new FileTransfer(), new Notification(), new Others());
        return new Permissions(getRoles(), joc, controllerDefaults, new com.sos.joc.model.security.configuration.permissions.Controllers());
    }

    private Boolean getHaveAnyIpPermission() {
        if (this.haveAnyIpPermission == null) {

            if (currentSubject != null) {
                SOSListOfPermissions sosListOfPermissions = new SOSListOfPermissions(this, false);

                this.haveAnyIpPermission = false;
                String[] ipParts = this.getCallerIpAddress().split("\\.");
                for (String p : sosListOfPermissions.getSosPermissionShiro().getSOSPermissions().getSOSPermission()) {
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
        if (currentSubjects != null) {
            for (ISOSAuthSubject subject : currentSubjects) {
                Path path = Paths.get(permission.replace(':', '/'));
                int nameCount = path.getNameCount();
                for (int i = 0; i < nameCount - 1; i++) {
                    if (excluded) {
                        break;
                    }
                    String s = path.subpath(0, nameCount - i).toString().replace(File.separatorChar, ':');
                    excluded = subject.isPermitted("-" + s) || subject.isPermitted("-" + controllerId + ":" + s);
                }

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
            excluded = excluded || getExcludedController(permission, "ip=" + es) || getExcludedController(permission, "ip=" + es + ":"
                    + controllerId);
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
            LOGGER.error("", e);
        }

        String[] ipParts = ipAddress.split("\\.");

        if ((ipParts.length < 4) && !ipv6 || (ipParts.length < 8 && ipv6)) {
            LOGGER.warn("Wrong ip address found: " + ipAddress);
            return false;
        }

        return ipPermission(permission, controllerId, ipParts, 0) || ipPermission(permission, controllerId, ipParts, 1) || ipPermission(permission,
                controllerId, ipParts, 2) || ipPermission(permission, controllerId, ipParts, 3);

    }

    private boolean getPermissionFromSubject(ISOSAuthSubject subject, String controllerId, String permission) {
        if (this.getHaveAnyIpPermission()) {
            return (handleIpPermission(controllerId, permission) || subject.isPermitted(permission) || subject.isPermitted(controllerId + ":"
                    + permission)) && !getExcluded(permission, controllerId);
        } else {
            return (subject.isPermitted(permission) || subject.isPermitted(controllerId + ":" + permission)) && !getExcluded(permission,
                    controllerId);
        }
    }

    public boolean isPermitted(String controllerId, String permission) {
        boolean permitted = false;
        if (currentSubjects != null) {
            for (ISOSAuthSubject subject : currentSubjects) {
                permitted = getPermissionFromSubject(subject, controllerId, permission);
                if (permitted) {
                    return true;
                }
            }
        }
        return permitted;
    }

    public boolean isPermitted(String permission) {
        return (isPermitted("", permission));
    }

    public boolean isAuthenticated() {
        if (currentSubject != null) {
            return currentSubject.isAuthenticated();
        } else {
            return false;
        }
    }

    public void initFolders() {
        sosAuthFolderPermissions = new SOSAuthFolderPermissions();
    }

    public void addFolder(String role, String folders) {
        if (sosAuthFolderPermissions == null) {
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
            sosAuthFolderPermissions.setFolders(jobSchedulerId, folders);
        }
    }

    public boolean withAuthorization() {
        return withAuthorization;
    }

    public SOSAuthFolderPermissions getSosShiroFolderPermissions() {
        return sosAuthFolderPermissions;
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

    public String getAccessToken(String identityServiceName) {
        return identyServiceAccessToken.get(identityServiceName);
    }

    public SOSIdentityService getIdentityServices() {
        return identityServices;
    }

    public void setIdentityServices(SOSIdentityService identityServices) {
        this.identityServices = identityServices;
    }

    public boolean isShiro() {
        if (identityServices == null) {
            return false;
        }
        return identityServices.getIdentyServiceType() == IdentityServiceTypes.SHIRO;
    }
}
