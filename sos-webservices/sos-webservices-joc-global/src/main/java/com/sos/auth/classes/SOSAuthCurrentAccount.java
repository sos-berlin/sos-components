package com.sos.auth.classes;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSShell;
import com.sos.joc.Globals;
import com.sos.joc.db.approval.ApprovalDBLayer;
import com.sos.joc.event.bean.approval.ApprovalUpdatedEvent;
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

import jakarta.servlet.http.HttpServletRequest;

public class SOSAuthCurrentAccount {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSAuthCurrentAccount.class);
    private ISOSAuthSubject currentSubject;
    private String accountName;
    private String id;
    private String accessToken;
    private SOSIdentityService identityService;
    private Map<String, String> identyServiceAccessToken;
    private boolean withAuthorization;
    private Set<String> roles;
    private Set<ISOSAuthSubject> currentSubjects;
    private SOSLoginParameters sosLoginParameters;
    private String kid;
    private boolean isApprover = false;
    private boolean isRequestor = false;
    private String ipAddress;

    private Permissions sosPermissionJocCockpitControllers;
    private SOSAuthFolderPermissions sosAuthFolderPermissions;
    private Permissions sos4EyesPermissions;

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
            return Optional.ofNullable(sosPermissionJocCockpitControllers.getControllers().getAdditionalProperties().get(controllerId)).orElse(
                    sosPermissionJocCockpitControllers.getControllerDefaults());

//            if (sosPermissionJocCockpitControllers.getControllers().getAdditionalProperties().containsKey(controllerId)) {
//                return sosPermissionJocCockpitControllers.getControllers().getAdditionalProperties().get(controllerId);
//            } else {
//                return sosPermissionJocCockpitControllers.getControllerDefaults();
//            }
        } else {
            return sosPermissionJocCockpitControllers.getControllerDefaults();
        }
    }

    public ControllerPermissions getControllerDefaultPermissions() {
        if (sosPermissionJocCockpitControllers == null) {
            sosPermissionJocCockpitControllers = initPermissions();
        }
        return sosPermissionJocCockpitControllers.getControllerDefaults();
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
    
    public ControllerPermissions get4EyesControllerPermissions(String controllerId) {
        if (sos4EyesPermissions == null) {
            sos4EyesPermissions = initPermissions();
        }

        if (sos4EyesPermissions.getControllers() != null) {
            return Optional.ofNullable(sos4EyesPermissions.getControllers().getAdditionalProperties().get(controllerId)).orElse(
                    sos4EyesPermissions.getControllerDefaults());
        } else {
            return sos4EyesPermissions.getControllerDefaults();
        }
    }
    
    public ControllerPermissions get4EyesControllerDefaultPermissions() {
        if (sos4EyesPermissions == null) {
            sos4EyesPermissions = initPermissions();
        }
        return sos4EyesPermissions.getControllerDefaults();
    }

    public JocPermissions get4EyesJocPermissions() {
        if (sos4EyesPermissions == null) {
            sos4EyesPermissions = initPermissions();
        }
        return sos4EyesPermissions.getJoc();
    }
    
    public void set4EyesRolePermissionJocCockpitControllers(Permissions sos4EyesRolePermissionJocCockpitControllers) {
        this.sos4EyesPermissions = sos4EyesRolePermissionJocCockpitControllers;
    }
    
    public Permissions get4EyesRolePermissionJocCockpitControllers() {
        return sos4EyesPermissions;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getId() {
        return id;
    }

    public void setAccessToken(String identityServiceName, String accessToken) {
        if (identyServiceAccessToken == null) {
            identyServiceAccessToken = new HashMap<>();
        }
        identyServiceAccessToken.put(identityServiceName, accessToken);
        if (this.id == null) {
            this.id = SOSAuthHelper.createSessionId();
        }
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
            this.roles = new HashSet<>();
        }
        if (currentSubject != null) {
            this.roles.addAll(Stream.concat(securityConf.getAccounts().stream().filter(account -> account.getRoles() != null).flatMap(
                    account -> account.getRoles().stream()), securityConf.getRoles().getAdditionalProperties().keySet().stream()).filter(
                            role -> currentSubject.hasRole(role)).collect(Collectors.toSet()));

        }
        String fourEyesRole = Globals.getConfigurationGlobalsJoc().getApprovalRequestorRole().getValue();
        if (fourEyesRole == null || fourEyesRole.isEmpty()) {
            this.isRequestor = false;
        } else {
            this.isRequestor = this.roles.contains(fourEyesRole);
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
    
    public Optional<ApprovalUpdatedEvent> createApprovalUpdatedEvent() {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("createApprovalNotification");
            return createApprovalUpdatedEvent(session);
        } finally {
            Globals.disconnect(session);
        }
    }
    
    public Optional<ApprovalUpdatedEvent> createApprovalUpdatedEvent(SOSHibernateSession session) {
        try {
            if (isApprover || isRequestor) {
                ApprovalDBLayer dbLayer = new ApprovalDBLayer(session);
                Map<String, Long> approverEvent = null;
                if (isApprover) {
                    Long numOfPA = dbLayer.getNumOfPendingApprovals(accountName);
                    if (numOfPA > 0L) {
                        approverEvent = Collections.singletonMap(accountName, numOfPA);
                    }
                }
                Map<String, Map<String, Long>> requestorEvent = null;
                if (isRequestor) {
                    Map<String, Long> numOfARR = dbLayer.getNumOfApprovedRejectedRequests(accountName);
                    if (numOfARR.values().stream().mapToLong(Long::longValue).sum() > 0L) {
                        requestorEvent = Collections.singletonMap(accountName, numOfARR);
                    }
                }
                if (approverEvent != null || requestorEvent != null) {
                    return Optional.of(new ApprovalUpdatedEvent(requestorEvent, approverEvent, true));
                }
            }
        } catch (Exception e) {
            //
        }
        return Optional.empty();
    }

    private Permissions initPermissions() {
        Administration administration = new Administration(new Accounts(), new Settings(), new Controllers(), new Certificates(),
                new Customization());
        ControllerPermissions controllerDefaults = new ControllerPermissions(false, false, false, false, false, new Deployments(), new Orders(),
                new Agents(), new NoticeBoards(), new Locks(), new Workflows());
        JocPermissions joc = new JocPermissions(false, administration, new Cluster(), new Inventory(), new Calendars(), new Documentations(),
                new AuditLog(), new DailyPlan(), new FileTransfer(), new Notification(), new Encipherment(), new Reports(), new Others());
        return new Permissions(getRoles(), joc, controllerDefaults, new com.sos.joc.model.security.configuration.permissions.Controllers());
    }

    private boolean getExcludedController(String permission, String controllerId) {
        boolean excluded = false;
        if (currentSubjects != null) {
            for (ISOSAuthSubject subject : currentSubjects) {
                if (subject != null) {
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
        }

        return excluded;
    }
    
    private boolean get4EyesExcludedController(String permission, String controllerId) {
        boolean excluded = false;
        if (currentSubjects != null) {
            for (ISOSAuthSubject subject : currentSubjects) {
                if (subject != null) {
                    Path path = Paths.get(permission.replace(':', '/'));
                    int nameCount = path.getNameCount();
                    for (int i = 0; i < nameCount - 1; i++) {
                        if (excluded) {
                            break;
                        }
                        String s = path.subpath(0, nameCount - i).toString().replace(File.separatorChar, ':');
                        excluded = subject.is4EyesPermitted("-" + s) || subject.is4EyesPermitted("-" + controllerId + ":" + s);
                    }
                }
            }
        }

        return excluded;
    }

    public boolean testGetExcluded(String permission, String controllerId) {
        return getExcludedController(permission, controllerId);
    }

    private boolean getPermissionFromSubject(ISOSAuthSubject subject, String controllerId, String permission, boolean onlyfourEyesRole) {
        if (onlyfourEyesRole) {
            if (permission.endsWith(":view")) {
                return false; //view permissions don't need 4-eyes principle
            }
            return (subject.is4EyesPermitted(permission) || subject.is4EyesPermitted(controllerId + ":" + permission)) && !get4EyesExcludedController(
                    permission, controllerId);
        } else {
            return (subject.isPermitted(permission) || subject.isPermitted(controllerId + ":" + permission)) && !getExcludedController(permission,
                    controllerId);
        }
    }

    public boolean isPermitted(String controllerId, String permission, boolean onlyfourEyesRole) {
        boolean permitted = false;
        if (currentSubjects != null) {
            for (ISOSAuthSubject subject : currentSubjects) {
                if (subject != null) {
                    permitted = getPermissionFromSubject(subject, controllerId, permission, onlyfourEyesRole);
                    if (permitted) {
                        return true;
                    }
                }
            }
        }

        return permitted;
    }

    public boolean isPermitted(String permission) {
        return (isPermitted("", permission, false));
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

    public SOSAuthFolderPermissions getSosAuthFolderPermissions() {
        return sosAuthFolderPermissions;
    }

    public String getCallerHostName() {
        if (sosLoginParameters.getRequest() != null) {
            return sosLoginParameters.getRequest().getRemoteHost();
        } else {
            return "";
        }
    }

    public String getCallerIpAddress() {
        if (ipAddress == null) {
            ipAddress = Optional.ofNullable(sosLoginParameters.getRequest()).map(HttpServletRequest::getRemoteAddr).map(ip -> {
                if ("127.0.0.1".equals(ip) || "[0:0:0:0:0:0:0:1]".equals(ip)) {
                    return SOSShell.getLocalHostNameOptional().flatMap(SOSShell::getHostAddressOptional).orElse(ip);
                }
                return ip;
            }).orElse("0.0.0.0");
        }
        return ipAddress;
        
//        if (sosLoginParameters.getRequest() != null) {
//            String s = sosLoginParameters.getRequest().getRemoteAddr();
//            if ("[0:0:0:0:0:0:0:1]".equals(s)) {
//                return "127.0.0.1";
//            } else {
//                return s;
//            }
//        } else {
//            return "0.0.0.0";
//        }
    }

    public HttpServletRequest getHttpServletRequest() {
        return sosLoginParameters.getRequest();
    }

    public String getAccessToken(String identityServiceName) {
        return identyServiceAccessToken.get(identityServiceName);
    }

    public SOSIdentityService getIdentityService() {
        return identityService;
    }

    public void setIdentityService(SOSIdentityService identityService) {
        this.identityService = identityService;
    }

    public SOSLoginParameters getSosLoginParameters() {
        return sosLoginParameters;
    }

    public void setSosLoginParameters(SOSLoginParameters sosLoginParameters) {
        this.sosLoginParameters = sosLoginParameters;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getKid() {
        return kid;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }

    public boolean isApprover() {
        return isApprover;
    }

    public void setIsApprover(boolean isApprover) {
        this.isApprover = isApprover;
    }

    public boolean isRequestor() {
        return isRequestor;
    }

}
