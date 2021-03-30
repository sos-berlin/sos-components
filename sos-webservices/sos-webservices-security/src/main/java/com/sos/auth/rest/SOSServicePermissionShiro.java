package com.sos.auth.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.Ini.Section;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.ExpiredSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionKey;
import org.ini4j.InvalidFileFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.sos.auth.client.ClientCertificateHandler;
//import com.sos.auth.rest.permission.model.SOSPermissionCommandsControllers;
//import com.sos.auth.rest.permission.model.SOSPermissionJocCockpitControllers;
import com.sos.auth.rest.permission.model.SOSPermissionRoles;
import com.sos.auth.rest.permission.model.SOSPermissionShiro;
import com.sos.auth.shiro.SOSlogin;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.audit.SecurityAudit;
import com.sos.joc.classes.security.SOSSecurityConfiguration;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocAuthenticationException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.SessionNotExistException;
import com.sos.joc.model.security.Permissions;
import com.sos.joc.model.security.SecurityConfiguration;

@Path("/authentication")
public class SOSServicePermissionShiro {

//    private static final String JOC_COCKPIT_CLIENT_ID = "JOC Cockpit";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String X_ACCESS_TOKEN = "X-Access-Token";
    private static final String UTC = "UTC";
    private static final String EMPTY_STRING = "";
    private static final String USER_IS_NULL = "user is null";
    private static final String AUTHORIZATION_HEADER_WITH_BASIC_BASED64PART_EXPECTED = "Authorization header with basic based64part expected";
    private static final String ACCESS_TOKEN_EXPECTED = "Access token header expected";
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSPermissionsCreator.class);
    private static final String SHIRO_SESSION = "SHIRO_SESSION";
    private static final String ThreadCtx = "authentication";

    private SOSShiroCurrentUser currentUser;
    private SOSlogin sosLogin;

    @Context
    UriInfo uriInfo;

    private JOCDefaultResponse getJocCockpitControllerPermissions(String accessToken, String user, String pwd) throws JocException,
            InvalidFileFormatException, SOSHibernateException, IOException {
        this.setCurrentUserfromAccessToken(accessToken, user, pwd);
        SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(currentUser);

        SOSSecurityConfiguration sosSecurityConfiguration = new SOSSecurityConfiguration();
        SecurityConfiguration entity = sosSecurityConfiguration.readConfiguration();

        Permissions sosPermissionMasters = sosPermissionsCreator.createJocCockpitPermissionControllerObjectList(accessToken,
                entity.getMasters());
        return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(sosPermissionMasters));
    }

    @GET
    @Path("/joc_cockpit_permissions")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getJocCockpitPermissions(@HeaderParam(ACCESS_TOKEN) String accessTokenFromHeader,
            @HeaderParam(X_ACCESS_TOKEN) String xAccessTokenFromHeader, @QueryParam(ACCESS_TOKEN) String accessTokenFromQuery,
            @QueryParam("user") String user, @QueryParam("pwd") String pwd) throws JocException, InvalidFileFormatException, SOSHibernateException, IOException {
        MDC.put("context", ThreadCtx);
        try {
            String accessToken = getAccessToken(accessTokenFromHeader, xAccessTokenFromHeader, accessTokenFromQuery);
            return getJocCockpitControllerPermissions(accessToken, user, pwd);
        } finally {
            MDC.remove("context");
        }
    }

    @POST
    @Path("/joc_cockpit_permissions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postJocCockpitPermissions(@HeaderParam(ACCESS_TOKEN) String accessTokenFromHeader,
            @HeaderParam(X_ACCESS_TOKEN) String xAccessTokenFromHeader) {

        //MDC.put("context", ThreadCtx);
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();
        try {
            String accessToken = getAccessToken(accessTokenFromHeader, xAccessTokenFromHeader, EMPTY_STRING);

            SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(null);
            sosPermissionsCreator.loginFromAccessToken(accessToken);

            sosWebserviceAuthenticationRecord.setAccessToken(accessToken);

            setCurrentUserfromAccessToken(sosWebserviceAuthenticationRecord.getAccessToken(), sosWebserviceAuthenticationRecord.getUser(),
                    sosWebserviceAuthenticationRecord.getPassword());

            if (currentUser == null) {
                LOGGER.debug(USER_IS_NULL + " " + AUTHORIZATION_HEADER_WITH_BASIC_BASED64PART_EXPECTED);
                return JOCDefaultResponse.responseStatusJSError(USER_IS_NULL + " " + AUTHORIZATION_HEADER_WITH_BASIC_BASED64PART_EXPECTED);
            }

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(currentUser.getSosPermissionJocCockpitControllers()));
        } catch (org.apache.shiro.session.ExpiredSessionException e) {
            LOGGER.error(e.getMessage());
            SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = createSOSShiroCurrentUserAnswer(accessTokenFromHeader,
                    sosWebserviceAuthenticationRecord.getUser(), e.getMessage());
            return JOCDefaultResponse.responseStatus440(sosShiroCurrentUserAnswer);
        } catch (Exception ee) {
            LOGGER.error(ee.getMessage());
            return JOCDefaultResponse.responseStatusJSError(ee.getMessage());
//        } finally {
//            MDC.remove("context");
        }
    }

    @POST
    @Path("/size")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getSize() {
        MDC.put("context", ThreadCtx);
        try {
            if (Globals.jocWebserviceDataContainer.getCurrentUsersList() == null) {
                return JOCDefaultResponse.responseStatus200(-1);
            } else {
                return JOCDefaultResponse.responseStatus200(Globals.jocWebserviceDataContainer.getCurrentUsersList().size());
            }
        } finally {
            MDC.remove("context");
        }
    }

    @POST
    @Path("/userbyname")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getAccessToken(String user) {
        MDC.put("context", ThreadCtx);
        try {
            if (Globals.jocWebserviceDataContainer.getCurrentUsersList() != null) {
                SOSShiroCurrentUserAnswer s = Globals.jocWebserviceDataContainer.getCurrentUsersList().getUserByName(user);
                return JOCDefaultResponse.responseStatus200(s);
            } else {
                SOSShiroCurrentUserAnswer s = new SOSShiroCurrentUserAnswer();
                s.setAccessToken("not-valid");
                s.setUser(user);
                return JOCDefaultResponse.responseStatus200(s);
            }
        } finally {
            MDC.remove("context");
        }
    }

    @POST
    @Path("/userbytoken")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getAccessToken(@HeaderParam(ACCESS_TOKEN) String accessTokenFromHeader,
            @HeaderParam(X_ACCESS_TOKEN) String xAccessTokenFromHeader) {
        MDC.put("context", ThreadCtx);
        try {
            String token = this.getAccessToken(accessTokenFromHeader, xAccessTokenFromHeader, "");
            if (Globals.jocWebserviceDataContainer.getCurrentUsersList() != null) {

                SOSShiroCurrentUserAnswer s = Globals.jocWebserviceDataContainer.getCurrentUsersList().getUserByToken(token);
                return JOCDefaultResponse.responseStatus200(s);
            } else {
                SOSShiroCurrentUserAnswer s = new SOSShiroCurrentUserAnswer();
                s.setAccessToken("not-valid");
                return JOCDefaultResponse.responseStatus200(s);
            }
        } finally {
            MDC.remove("context");
        }
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse loginPost(@Context HttpServletRequest request, @HeaderParam("Authorization") String basicAuthorization,
            @HeaderParam("X-CLIENT-ID") String loginClientId, @QueryParam("user") String user, @QueryParam("pwd") String pwd) throws JocException,
            SOSHibernateException {
        MDC.put("context", ThreadCtx);
        Globals.loginClientId = loginClientId;
        if (request != null) {
            String clientCertCN = null;
            try {
                ClientCertificateHandler clientCertHandler = new ClientCertificateHandler(request);
                clientCertCN = clientCertHandler.getClientCN();
                if(clientCertCN == null) {
                    LOGGER.info("Client Certificate CN read from Login: n/a");
                } else {
                    LOGGER.info("Client Certificate CN read from Login: " + clientCertCN);
                }
            } catch (IOException e) {
                LOGGER.debug("No Client certificate read from HttpServletRequest.");
            } 
        }
        try {
            return login(request, basicAuthorization, user, pwd);
        } finally {
            MDC.remove("context");
        }
    }

    private void removeTimeOutSessions() throws SOSHibernateException, JocConfigurationException, DBOpenSessionException {
        SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("JOC: Logout");

        try {
            sosHibernateSession.beginTransaction();
            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
            JocConfigurationFilter filter = new JocConfigurationFilter();

            filter.setAccount(".");
            filter.setConfigurationType(SHIRO_SESSION);
            List<DBItemJocConfiguration> listOfConfigurtions = jocConfigurationDBLayer.getJocConfigurationList(filter, 0);

            IniSecurityManagerFactory factory = Globals.getShiroIniSecurityManagerFactory();
            SecurityManager securityManager = factory.getInstance();
            SecurityUtils.setSecurityManager(securityManager);

            for (DBItemJocConfiguration jocConfigurationDbItem : listOfConfigurtions) {
                SessionKey s = new DefaultSessionKey(jocConfigurationDbItem.getName());
                try {
                    SecurityUtils.getSecurityManager().getSession(s);
                } catch (ExpiredSessionException e) {
                    LOGGER.debug("Session " + jocConfigurationDbItem.getName() + " removed");
                }
            }
            sosHibernateSession.commit();

        } finally {
            sosHibernateSession.rollback();
            sosHibernateSession.close();
        }
    }

    private JOCDefaultResponse logout(String accessToken) {

        if (accessToken == null || accessToken.isEmpty()) {
            return JOCDefaultResponse.responseStatusJSError(ACCESS_TOKEN_EXPECTED);
        }

        if (Globals.jocWebserviceDataContainer.getCurrentUsersList() != null) {
            currentUser = Globals.jocWebserviceDataContainer.getCurrentUsersList().getUser(accessToken);
        }
        String user = "";
        String comment = "";
        if (currentUser != null) {
            user = currentUser.getUsername();
        }
        LOGGER.debug(String.format("Method: %s, User: %s, access_token: %s", "logout", user, accessToken));
        SOSShiroSession sosShiroSession = new SOSShiroSession(currentUser);
        try {

            if (currentUser == null || currentUser.getCurrentSubject() == null) {
                try {
                    if (Globals.sosCockpitProperties == null) {
                        Globals.sosCockpitProperties = new JocCockpitProperties();
                    }
                    Globals.setProperties();
                    IniSecurityManagerFactory factory = Globals.getShiroIniSecurityManagerFactory();
                    SecurityManager securityManager = factory.getInstance();
                    SecurityUtils.setSecurityManager(securityManager);
                    SessionKey s = new DefaultSessionKey(accessToken);
                    Session session = SecurityUtils.getSecurityManager().getSession(s);
                    session.stop();
                } catch (Exception e) {
                    throw new SessionNotExistException("Session doesn't exist");
                }
                throw new SessionNotExistException("Session doesn't exist");
            }

            sosShiroSession.getTimeout();

        } catch (ExpiredSessionException ex) {
            comment = "Session time out: " + ex.getMessage();
        } catch (SessionNotExistException e) {
            comment = "Session time out: " + e.getMessage();
        } catch (UnknownSessionException u) {
            comment = "Session time out: " + u.getMessage();
        }

        JocAuditLog jocAuditLog = new JocAuditLog(user, "./logout");
        SecurityAudit s = new SecurityAudit(comment);
        jocAuditLog.logAuditMessage(s);
        try {
            if (currentUser != null && currentUser.getCurrentSubject() != null) {
                sosShiroSession.getTimeout();
                sosShiroSession.stop();
            }

        } catch (Exception e) {
        }

        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(EMPTY_STRING);
        if (currentUser != null) {
            sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(currentUser.getUsername());
        }
        sosShiroCurrentUserAnswer.setIsAuthenticated(false);
        sosShiroCurrentUserAnswer.setHasRole(false);
        sosShiroCurrentUserAnswer.setIsPermitted(false);
        sosShiroCurrentUserAnswer.setAccessToken(EMPTY_STRING);
        if (Globals.jocWebserviceDataContainer.getCurrentUsersList() != null) {
            Globals.jocWebserviceDataContainer.getCurrentUsersList().removeUser(accessToken);
        }

        try {
            this.removeTimeOutSessions();
        } catch (SOSHibernateException | JocConfigurationException | DBOpenSessionException e) {
            LOGGER.warn("Could not remove old session " + e.getMessage());
        }

        return JOCDefaultResponse.responseStatus200(sosShiroCurrentUserAnswer);
    }

    @POST
    @Path("/logout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse logoutPost(@HeaderParam(ACCESS_TOKEN) String accessTokenFromHeader,
            @HeaderParam(X_ACCESS_TOKEN) String xAccessTokenFromHeader) {
        MDC.put("context", ThreadCtx);
        try {
            String accessToken = getAccessToken(accessTokenFromHeader, xAccessTokenFromHeader, EMPTY_STRING);
            return logout(accessToken);
        } finally {
            MDC.remove("context");
        }
    }

    @POST
    @Path("/db_refresh")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse dbRefresh() {
        MDC.put("context", ThreadCtx);
        try {
            if (Globals.sosHibernateFactory != null) {
                Globals.sosHibernateFactory.close();
                Globals.sosHibernateFactory.build();

            }
            if (Globals.sosSchedulerHibernateFactories != null) {
                for (SOSHibernateFactory sosHibernateFactory : Globals.sosSchedulerHibernateFactories.values()) {
                    sosHibernateFactory.close();
                    sosHibernateFactory.build();
                }
            }

            return JOCDefaultResponse.responseStatus200("Db connections reconnected");
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e);
        } finally {
            MDC.remove("context");
        }
    }

    @GET
    @Path("/role")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSShiroCurrentUserAnswer hasRole(@HeaderParam(ACCESS_TOKEN) String accessTokenFromHeader,
            @HeaderParam(X_ACCESS_TOKEN) String xAccessTokenFromHeader, @QueryParam(ACCESS_TOKEN) String accessTokenFromQuery,
            @QueryParam("user") String user, @QueryParam("pwd") String pwd, @QueryParam("role") String role) throws SessionNotExistException {

        MDC.put("context", ThreadCtx);
        try {
            String accessToken = getAccessToken(accessTokenFromHeader, xAccessTokenFromHeader, accessTokenFromQuery);

            setCurrentUserfromAccessToken(accessToken, user, pwd);

            SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(currentUser.getUsername());
            sosShiroCurrentUserAnswer.setRole(role);
            sosShiroCurrentUserAnswer.setIsAuthenticated(currentUser.isAuthenticated());
            sosShiroCurrentUserAnswer.setHasRole(currentUser.hasRole(role));
            sosShiroCurrentUserAnswer.setAccessToken(currentUser.getAccessToken());

            return sosShiroCurrentUserAnswer;
        } finally {
            MDC.remove("context");
        }
    }

    @POST
    @Path("/permission")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSShiroCurrentUserAnswer isPermitted(@HeaderParam(ACCESS_TOKEN) String accessTokenFromHeader,
            @HeaderParam(X_ACCESS_TOKEN) String xAccessTokenFromHeader, @QueryParam(ACCESS_TOKEN) String accessTokenFromQuery,
            @QueryParam("user") String user, @QueryParam("pwd") String pwd, @QueryParam("permission") String permission)
            throws SessionNotExistException {

        MDC.put("context", ThreadCtx);
        try {
            String accessToken = getAccessToken(accessTokenFromHeader, xAccessTokenFromHeader, accessTokenFromQuery);
            setCurrentUserfromAccessToken(accessToken, user, pwd);

            SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(currentUser.getUsername());
            sosShiroCurrentUserAnswer.setPermission(permission);
            sosShiroCurrentUserAnswer.setIsAuthenticated(currentUser.isAuthenticated());
            sosShiroCurrentUserAnswer.setIsPermitted(currentUser.isPermitted(permission));

            sosShiroCurrentUserAnswer.setAccessToken(currentUser.getAccessToken());

            return sosShiroCurrentUserAnswer;
        } finally {
            MDC.remove("context");
        }
    }

    private void setCurrentUserfromAccessToken(String accessToken, String user, String pwd) throws SessionNotExistException {
        if (Globals.jocWebserviceDataContainer.getCurrentUsersList() != null && accessToken != null && accessToken.length() > 0) {
            currentUser = Globals.jocWebserviceDataContainer.getCurrentUsersList().getUser(accessToken);
            LOGGER.debug(String.format("Method: %s, access_token: %s", "setCurrentUserfromAccessToken", accessToken));
        } else {
            if (user != null && user.length() > 0 && pwd != null && pwd.length() > 0) {
                LOGGER.debug(String.format("Method: %s, User: %s, access_token: %s", "setCurrentUserfromAccessToken", user, accessToken));
                currentUser = new SOSShiroCurrentUser(user, pwd);
                try {
                    createUser();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        resetTimeOut();
    }

    @POST
    @Path("/permissions")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSPermissionShiro getPermissions(@HeaderParam(ACCESS_TOKEN) String accessTokenFromHeader,
            @HeaderParam(X_ACCESS_TOKEN) String xAccessTokenFromHeader, @QueryParam(ACCESS_TOKEN) String accessTokenFromQuery,
            @QueryParam("forUser") Boolean forUser, @QueryParam("user") String user, @QueryParam("pwd") String pwd) throws SessionNotExistException {

        MDC.put("context", ThreadCtx);
        try {
            String accessToken = this.getAccessToken(accessTokenFromHeader, xAccessTokenFromHeader, accessTokenFromQuery);
            this.setCurrentUserfromAccessToken(accessToken, user, pwd);
            SOSListOfPermissions sosListOfPermissions = new SOSListOfPermissions(currentUser, forUser);
            return sosListOfPermissions.getSosPermissionShiro();
        } finally {
            MDC.remove("context");
        }
    }

    private String getAccessToken(String accessTokenFromHeader, String xAccessTokenFromHeader, String accessTokenFromQuery) {
        if (xAccessTokenFromHeader != null && !EMPTY_STRING.equals(xAccessTokenFromHeader)) {
            accessTokenFromQuery = xAccessTokenFromHeader;
        } else {
            if (accessTokenFromHeader != null && !EMPTY_STRING.equals(accessTokenFromHeader)) {
                accessTokenFromQuery = accessTokenFromHeader;
            }
        }
        return accessTokenFromQuery;
    }

    private void createUser() throws Exception {
        if (Globals.jocWebserviceDataContainer.getCurrentUsersList() == null) {
            Globals.jocWebserviceDataContainer.setCurrentUsersList(new SOSShiroCurrentUsersList());
        }
        SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(currentUser);
        sosLogin = new SOSlogin(Globals.getShiroIniSecurityManagerFactory());
        sosLogin.login(currentUser.getUsername(), currentUser.getPassword(),currentUser.getHttpServletRequest());

        currentUser.setCurrentSubject(sosLogin.getCurrentUser());

        if (sosLogin.getCurrentUser() == null) {
            SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(currentUser.getUsername());
            sosShiroCurrentUserAnswer.setIsAuthenticated(false);
            sosShiroCurrentUserAnswer.setMessage(String.format("%s: Could not login with user: %s password:*******", sosLogin.getMsg(), currentUser
                    .getUsername()));
            throw new JocAuthenticationException(sosShiroCurrentUserAnswer);
        }

        SOSShiroSession sosShiroSession = new SOSShiroSession(currentUser);
        String accessToken = sosShiroSession.getId().toString();

        currentUser.setAccessToken(accessToken);
        Globals.jocWebserviceDataContainer.getCurrentUsersList().addUser(currentUser);

        SOSSecurityConfiguration sosSecurityConfiguration = new SOSSecurityConfiguration();
        SecurityConfiguration entity = sosSecurityConfiguration.readConfiguration();

        Permissions sosPermissionJocCockpitControllers = sosPermissionsCreator.createJocCockpitPermissionControllerObjectList(
                accessToken, entity.getMasters());
        currentUser.setSosPermissionJocCockpitControllers(sosPermissionJocCockpitControllers);
        currentUser.getCurrentSubject().getSession().setAttribute("username_joc_permissions", Globals.objectMapper.writeValueAsBytes(
                sosPermissionJocCockpitControllers));

        currentUser.initFolders();

        Section s = sosPermissionsCreator.getIni().getSection("folders");
        if (s != null) {
            for (String role : s.keySet()) {
                currentUser.addFolder(role, s.get(role));
            }
        }

        if (Globals.sosCockpitProperties == null) {
            Globals.sosCockpitProperties = new JocCockpitProperties();
        }

    }

    private SOSShiroCurrentUser getUserPwdFromHeaderOrQuery(String basicAuthorization, String user, String pwd) throws UnsupportedEncodingException,
            JocException {
        String authorization = EMPTY_STRING;

        if (basicAuthorization != null) {
            String[] authorizationParts = basicAuthorization.split(" ");
            if (authorizationParts.length > 1) {
                authorization = new String(Base64.getDecoder().decode(authorizationParts[1].getBytes("UTF-8")), "UTF-8");
            }
        } else {
            JocError error = new JocError();
            error.setMessage("The Header Authorization with the Base64 encoded authorization string is missing");
            throw new JocException(error);
        }

        int idx = authorization.indexOf(':');
        if (idx == -1) {
            if (!basicAuthorization.isEmpty()) {
                user = authorization;
            }
        } else {
            user = authorization.substring(0, idx);
            pwd = authorization.substring(idx + 1);
        }

        return new SOSShiroCurrentUser(user, pwd, authorization);
    }

    private SOSShiroCurrentUserAnswer authenticate() throws Exception {

        createUser();
        SOSShiroSession sosShiroSession = new SOSShiroSession(currentUser);

        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(currentUser.getUsername());
        sosShiroCurrentUserAnswer.setIsAuthenticated(currentUser.getCurrentSubject().isAuthenticated());
        sosShiroCurrentUserAnswer.setAccessToken(currentUser.getAccessToken());
        sosShiroCurrentUserAnswer.setUser(currentUser.getUsername());
        sosShiroCurrentUserAnswer.setSessionTimeout(sosShiroSession.getTimeout());
        sosShiroCurrentUserAnswer.setCallerHostName(currentUser.getCallerHostName());
        sosShiroCurrentUserAnswer.setCallerIpAddress(currentUser.getCallerIpAddress());

        LOGGER.info("CallerIpAddress=" + currentUser.getCallerIpAddress());

        boolean enableTouch = "true".equals(Globals.sosCockpitProperties.getProperty(WebserviceConstants.ENABLE_SESSION_TOUCH,
                WebserviceConstants.ENABLE_SESSION_TOUCH_DEFAULT));
        sosShiroCurrentUserAnswer.setEnableTouch(enableTouch);

        return sosShiroCurrentUserAnswer;

    }

    private String getRolesAsString(boolean forUser) {
        SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(currentUser);

        if (currentUser != null) {
            SOSPermissionRoles listOfRoles = sosPermissionsCreator.getRoles(forUser);
            if (listOfRoles.getSOSPermissionRole() != null) {
                return String.join(",", listOfRoles.getSOSPermissionRole());
            }
            return EMPTY_STRING;
        } else {
            return EMPTY_STRING;
        }
    }

    private JOCDefaultResponse login(HttpServletRequest request, String basicAuthorization, String user, String pwd) throws JocException,
            SOSHibernateException {
        Globals.setServletBaseUri(uriInfo);

        if (Globals.sosCockpitProperties == null) {
            Globals.sosCockpitProperties = new JocCockpitProperties();
        }
        Globals.jocTimeZone = TimeZone.getDefault();
        Globals.setProperties();
        SOSHibernateSession sosHibernateSession = null;

        if (basicAuthorization.isEmpty()) {
            String s = user + ":" + pwd;
            byte[] authEncBytes = org.apache.commons.codec.binary.Base64.encodeBase64(s.getBytes());
            String authStringEnc = new String(authEncBytes);
            basicAuthorization = "Basic " + authStringEnc;
        }

        try {

            sosHibernateSession = Globals.createSosHibernateStatelessConnection("JOC: Login");
            TimeZone.setDefault(TimeZone.getTimeZone(UTC));

            SOSShiroIniShare sosShiroIniShare = new SOSShiroIniShare(sosHibernateSession);
            sosShiroIniShare.provideIniFile();

            currentUser = getUserPwdFromHeaderOrQuery(basicAuthorization, user, pwd);

            if (currentUser == null || currentUser.getAuthorization() == null) {
                return JOCDefaultResponse.responseStatusJSError(USER_IS_NULL + " " + AUTHORIZATION_HEADER_WITH_BASIC_BASED64PART_EXPECTED);
            }

            currentUser.setAuthorization(basicAuthorization);
            currentUser.setHttpServletRequest(request);

            Globals.loginUserName = currentUser.getUsername();

            SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = authenticate();

            if (request != null) {
                sosShiroCurrentUserAnswer.setCallerIpAddress(request.getRemoteAddr());
                sosShiroCurrentUserAnswer.setCallerHostName(request.getRemoteHost());
            }

            LOGGER.debug(String.format("Method: %s, User: %s, access_token: %s", "login", currentUser.getUsername(), currentUser.getAccessToken()));

            Globals.jocWebserviceDataContainer.getCurrentUsersList().removeTimedOutUser(currentUser.getUsername());

            JocAuditLog jocAuditLog = new JocAuditLog(currentUser.getUsername(), "./login");
            SecurityAudit s = new SecurityAudit(getRolesAsString(true));
            jocAuditLog.logAuditMessage(s);

            if (!sosShiroCurrentUserAnswer.isAuthenticated()) {
                if (sosLogin != null) {
                    LOGGER.info(sosLogin.getMsg());
                }
                return JOCDefaultResponse.responseStatus401(sosShiroCurrentUserAnswer);
            } else {
                SOSShiroSession sosShiroSession = new SOSShiroSession(currentUser);
                return JOCDefaultResponse.responseStatus200WithHeaders(sosShiroCurrentUserAnswer, sosShiroCurrentUserAnswer.getAccessToken(),
                        sosShiroSession.getTimeout());
            }

        } catch (JocAuthenticationException e) {
            return JOCDefaultResponse.responseStatus401(e.getSosShiroCurrentUserAnswer());
        } catch (UnsupportedEncodingException e) {
            return JOCDefaultResponse.responseStatusJSError(AUTHORIZATION_HEADER_WITH_BASIC_BASED64PART_EXPECTED);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private void resetTimeOut() throws SessionNotExistException {

        if (currentUser != null) {
            SOSShiroSession sosShiroSession = new SOSShiroSession(currentUser);
            sosShiroSession.touch();

        } else {
            LOGGER.error(USER_IS_NULL);
        }
    }

    private SOSShiroCurrentUserAnswer createSOSShiroCurrentUserAnswer(String accessToken, String user, String message) {
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer();
        sosShiroCurrentUserAnswer.setAccessToken(accessToken);
        sosShiroCurrentUserAnswer.setUser(user);
        sosShiroCurrentUserAnswer.setHasRole(false);
        sosShiroCurrentUserAnswer.setIsAuthenticated(false);
        sosShiroCurrentUserAnswer.setIsPermitted(false);
        sosShiroCurrentUserAnswer.setMessage(message);
        return sosShiroCurrentUserAnswer;
    }

}