package com.sos.auth.classes;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
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
import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.auth.interfaces.ISOSLogin;
import com.sos.auth.interfaces.ISOSSecurityConfiguration;
import com.sos.auth.shiro.classes.SOSShiroIniShare;
import com.sos.auth.shiro.classes.SOSShiroLogin;
import com.sos.auth.sosintern.classes.SOSInternAuthLogin;
import com.sos.auth.vault.classes.SOSVaultLogin;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.security.SOSSecurityConfiguration;
import com.sos.joc.classes.security.SOSSecurityDBConfiguration;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.db.security.IamIdentityServiceDBLayer;
import com.sos.joc.db.security.IamIdentityServiceFilter;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocAuthenticationException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.SessionNotExistException;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.security.IdentityServiceTypes;
import com.sos.joc.model.security.Permissions;
import com.sos.joc.model.security.SecurityConfiguration;

@SuppressWarnings("deprecation")
@Path("/authentication")
public class SOSServicePermissionShiro {

    private static final String CREATE_ACCOUNT = "createAccount";
    // private static final String JOC_COCKPIT_CLIENT_ID = "JOC Cockpit";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String X_ACCESS_TOKEN = "X-Access-Token";
    private static final String UTC = "UTC";
    private static final String EMPTY_STRING = "";
    private static final String ACCESS_TOKEN_EXPECTED = "Access token header expected";
    private static final String AUTHORIZATION_HEADER_WITH_BASIC_BASED64PART_EXPECTED = "Authorization header with basic based64part expected";
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSServicePermissionShiro.class);
    private static final String SHIRO_SESSION = "SHIRO_SESSION";
    private static final String ThreadCtx = "authentication";

    @Context
    UriInfo uriInfo;

    private JOCDefaultResponse getJocCockpitControllerPermissions(String accessToken) throws JocException, InvalidFileFormatException,
            SOSHibernateException, IOException {
        SOSAuthCurrentAccount currentAccount = this.getCurrentAccount(accessToken);
        SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(currentAccount);

        SOSSecurityConfiguration sosSecurityConfiguration = new SOSSecurityConfiguration();
        SecurityConfiguration entity = sosSecurityConfiguration.readConfiguration();

        currentAccount.setRoles(entity);
        Permissions sosPermissionMasters = sosPermissionsCreator.createJocCockpitPermissionControllerObjectList(entity);
        return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(sosPermissionMasters));
    }

    @GET
    @Path("/joc_cockpit_permissions")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getJocCockpitPermissions(@HeaderParam(X_ACCESS_TOKEN) String xAccessTokenFromHeader,
            @QueryParam(ACCESS_TOKEN) String accessTokenFromQuery) {
        MDC.put("context", ThreadCtx);
        try {
            String accessToken = getAccessToken(xAccessTokenFromHeader, accessTokenFromQuery);
            return getJocCockpitControllerPermissions(accessToken);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e);
        } finally {
            MDC.remove("context");
        }
    }

    @POST
    @Path("/joc_cockpit_permissions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postJocCockpitPermissions(@HeaderParam(X_ACCESS_TOKEN) String accessToken) {

        MDC.put("context", ThreadCtx);
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();
        try {
            accessToken = getAccessToken(accessToken, EMPTY_STRING);

            sosWebserviceAuthenticationRecord.setAccessToken(accessToken);

            SOSAuthCurrentAccount currentAccount = getCurrentAccount(sosWebserviceAuthenticationRecord.getAccessToken());

            if (currentAccount == null) {
                LOGGER.debug("Account is not valid");
                return JOCDefaultResponse.responseStatusJSError("Account is not valid");
            }

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(currentAccount
                    .getSosPermissionJocCockpitControllers()));
        } catch (org.apache.shiro.session.ExpiredSessionException e) {
            SOSAuthCurrentAccountAnswer sosAuthCurrentAccountAnswer = createSOSAuthCurrentAccountAnswer(accessToken, sosWebserviceAuthenticationRecord
                    .getAccount(), e.getMessage());
            return JOCDefaultResponse.responseStatus440(sosAuthCurrentAccountAnswer);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e);
        } finally {
            MDC.remove("context");
        }
    }

    @POST
    @Path("/size")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getSize() {
        MDC.put("context", ThreadCtx);
        try {
            if (Globals.jocWebserviceDataContainer.getCurrentAccountsList() == null) {
                return JOCDefaultResponse.responseStatus200(-1);
            } else {
                return JOCDefaultResponse.responseStatus200(Globals.jocWebserviceDataContainer.getCurrentAccountsList().size());
            }
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e);
        } finally {
            MDC.remove("context");
        }
    }

    @POST
    @Path("/userbyname")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getAccessToken(String account) {
        MDC.put("context", ThreadCtx);
        try {
            if (Globals.jocWebserviceDataContainer.getCurrentAccountsList() != null) {
                SOSAuthCurrentAccountAnswer s = Globals.jocWebserviceDataContainer.getCurrentAccountsList().getAccountByName(account);
                return JOCDefaultResponse.responseStatus200(s);
            } else {
                SOSAuthCurrentAccountAnswer s = new SOSAuthCurrentAccountAnswer();
                s.setAccessToken("not-valid");
                s.setAccount(account);
                return JOCDefaultResponse.responseStatus200(s);
            }
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e);
        } finally {
            MDC.remove("context");
        }
    }

    @POST
    @Path("/userbytoken")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse userByToken(@HeaderParam(X_ACCESS_TOKEN) String xAccessTokenFromHeader) {
        MDC.put("context", ThreadCtx);
        try {
            String token = this.getAccessToken(xAccessTokenFromHeader, "");
            if (Globals.jocWebserviceDataContainer.getCurrentAccountsList() != null) {

                SOSAuthCurrentAccountAnswer s = Globals.jocWebserviceDataContainer.getCurrentAccountsList().getAccountByToken(token);
                return JOCDefaultResponse.responseStatus200(s);
            } else {
                SOSAuthCurrentAccountAnswer s = new SOSAuthCurrentAccountAnswer();
                s.setAccessToken("not-valid");
                return JOCDefaultResponse.responseStatus200(s);
            }
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e);
        } finally {
            MDC.remove("context");
        }
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse loginPost(@Context HttpServletRequest request, @HeaderParam("Authorization") String basicAuthorization,
            @HeaderParam("X-CLIENT-ID") String loginClientId, @QueryParam("user") String account, @QueryParam("pwd") String pwd) {
        MDC.put("context", ThreadCtx);
        Globals.loginClientId = loginClientId;
        String clientCertCN = null;
        try {
            if (request != null) {
                try {
                    ClientCertificateHandler clientCertHandler = new ClientCertificateHandler(request);
                    clientCertCN = clientCertHandler.getClientCN();
                    if (clientCertCN == null) {
                        LOGGER.info("Client Certificate CN read from Login: n/a");
                    } else {
                        LOGGER.info("Client Certificate CN read from Login: " + clientCertCN);
                    }
                } catch (IOException e) {
                    LOGGER.debug("No Client certificate read from HttpServletRequest.");
                }
            }
            return login(request, basicAuthorization, clientCertCN, account, pwd);
        } catch (JocAuthenticationException e) {
            return JOCDefaultResponse.responseStatus401(e.getSosAuthCurrentAccountAnswer());
        } catch (UnsupportedEncodingException e) {
            return JOCDefaultResponse.responseStatusJSError(AUTHORIZATION_HEADER_WITH_BASIC_BASED64PART_EXPECTED);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e);
        } finally {
            MDC.remove("context");
        }
    }

    private void removeTimeOutShiroDBSessions() throws SOSHibernateException, JocConfigurationException, DBOpenSessionException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("JOC: Logout");
            Globals.beginTransaction(sosHibernateSession);
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
                    LOGGER.trace("Session " + jocConfigurationDbItem.getName() + " removed");
                }
            }
            Globals.commit(sosHibernateSession);
        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            throw e;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

    protected JOCDefaultResponse logout(String accessToken) {

        if (accessToken == null || accessToken.isEmpty()) {
            return JOCDefaultResponse.responseStatusJSError(ACCESS_TOKEN_EXPECTED);
        }

        SOSAuthCurrentAccount currentAccount = Globals.jocWebserviceDataContainer.getCurrentAccountsList().getAccount(accessToken);

        String account = "";
        String comment = "";
        if (currentAccount != null) {
            account = currentAccount.getAccountname();
        }
        SOSSessionHandler sosSessionHandler = new SOSSessionHandler(currentAccount);
        JocAuditLog jocAuditLog = new JocAuditLog(account, "./logout");
        AuditParams audit = new AuditParams();
        audit.setComment(comment);
        jocAuditLog.logAuditMessage(audit);
        try {
            if (currentAccount != null && currentAccount.getCurrentSubject() != null) {
                sosSessionHandler.getTimeout();
                sosSessionHandler.stop();
            }

        } catch (Exception e) {
        }

        SOSAuthCurrentAccountAnswer sosShiroCurrentUserAnswer = new SOSAuthCurrentAccountAnswer(EMPTY_STRING);
        if (currentAccount != null) {
            sosShiroCurrentUserAnswer = new SOSAuthCurrentAccountAnswer(currentAccount.getAccountname());
        }
        sosShiroCurrentUserAnswer.setIsAuthenticated(false);
        sosShiroCurrentUserAnswer.setHasRole(false);
        sosShiroCurrentUserAnswer.setIsPermitted(false);
        sosShiroCurrentUserAnswer.setAccessToken(EMPTY_STRING);
        if (Globals.jocWebserviceDataContainer.getCurrentAccountsList() != null) {
            Globals.jocWebserviceDataContainer.getCurrentAccountsList().removeAccount(accessToken);
        }

        try {
            this.removeTimeOutShiroDBSessions();
        } catch (SOSHibernateException | JocConfigurationException | DBOpenSessionException e) {
            LOGGER.warn("Could not remove old session " + e.getMessage());
        }

        return JOCDefaultResponse.responseStatus200(sosShiroCurrentUserAnswer);
    }

    @POST
    @Path("/logout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse logoutPost(@HeaderParam(X_ACCESS_TOKEN) String xAccessTokenFromHeader) {
        MDC.put("context", ThreadCtx);
        try {
            String accessToken = getAccessToken(xAccessTokenFromHeader, EMPTY_STRING);
            return logout(accessToken);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e);
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
                // if (Globals.sosHibernateFactory.dbmsIsH2()) {
                // SOSHibernateSession connection = null;
                // try {
                // connection = Globals.createSosHibernateStatelessConnection("closeH2");
                // connection.createQuery("SHUTDOWN").executeUpdate();
                // } catch (Exception e) {
                // LOGGER.warn("shutdown H2 database: " + e.toString());
                // } finally {
                // Globals.disconnect(connection);
                // }
                // }
                Globals.sosHibernateFactory.close();
                Globals.sosHibernateFactory.build();
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
    public JOCDefaultResponse hasRole(@HeaderParam(X_ACCESS_TOKEN) String xAccessTokenFromHeader,
            @QueryParam(ACCESS_TOKEN) String accessTokenFromQuery, @QueryParam("role") String role) {

        MDC.put("context", ThreadCtx);
        try {
            String accessToken = getAccessToken(xAccessTokenFromHeader, accessTokenFromQuery);
            SOSAuthCurrentAccountAnswer sosShiroCurrentUserAnswer = hasRole(accessToken, role);
            return JOCDefaultResponse.responseStatus200(sosShiroCurrentUserAnswer);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e);
        } finally {
            MDC.remove("context");
        }
    }

    protected SOSAuthCurrentAccountAnswer hasRoleTest(String accessToken, String role) {
        return hasRole(accessToken, role);
    }

    private SOSAuthCurrentAccountAnswer hasRole(String accessToken, String role) {
        SOSAuthCurrentAccount currentAccount = getCurrentAccount(accessToken);

        SOSAuthCurrentAccountAnswer sosShiroCurrentUserAnswer = new SOSAuthCurrentAccountAnswer(currentAccount.getAccountname());
        sosShiroCurrentUserAnswer.setRole(role);
        sosShiroCurrentUserAnswer.setIsAuthenticated(currentAccount.isAuthenticated());
        sosShiroCurrentUserAnswer.setHasRole(currentAccount.hasRole(role));
        sosShiroCurrentUserAnswer.setAccessToken(currentAccount.getAccessToken());
        return sosShiroCurrentUserAnswer;
    }

    @GET
    @Path("/permission")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse isPermitted(@HeaderParam(X_ACCESS_TOKEN) String xAccessTokenFromHeader,
            @QueryParam(ACCESS_TOKEN) String accessTokenFromQuery, @QueryParam("permission") String permission) throws SessionNotExistException {

        MDC.put("context", ThreadCtx);
        try {
            String accessToken = getAccessToken(xAccessTokenFromHeader, accessTokenFromQuery);
            SOSAuthCurrentAccountAnswer sosShiroCurrentUserAnswer = isPermitted(accessToken, permission);
            return JOCDefaultResponse.responseStatus200(sosShiroCurrentUserAnswer);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e);
        } finally {
            MDC.remove("context");
        }
    }

    protected SOSAuthCurrentAccountAnswer isPermittedTest(String accessToken, String permission) {
        return isPermitted(accessToken, permission);
    }

    private SOSAuthCurrentAccountAnswer isPermitted(String accessToken, String permission) {
        SOSAuthCurrentAccount currentAccount = getCurrentAccount(accessToken);

        SOSAuthCurrentAccountAnswer sosShiroCurrentUserAnswer = new SOSAuthCurrentAccountAnswer(currentAccount.getAccountname());
        sosShiroCurrentUserAnswer.setPermission(permission);
        sosShiroCurrentUserAnswer.setIsAuthenticated(currentAccount.isAuthenticated());
        sosShiroCurrentUserAnswer.setIsPermitted(currentAccount.isPermitted(permission));

        sosShiroCurrentUserAnswer.setAccessToken(currentAccount.getAccessToken());
        return sosShiroCurrentUserAnswer;
    }

    private SOSAuthCurrentAccount getCurrentAccount(String accessToken) throws SessionNotExistException {
        SOSAuthCurrentAccount currentAccount = null;
        if (Globals.jocWebserviceDataContainer.getCurrentAccountsList() != null && accessToken != null && accessToken.length() > 0) {
            currentAccount = Globals.jocWebserviceDataContainer.getCurrentAccountsList().getAccount(accessToken);
        }
        resetTimeOut(currentAccount);
        return currentAccount;
    }

    @POST
    @Path("/permissions")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getPermissions(@HeaderParam(X_ACCESS_TOKEN) String xAccessTokenFromHeader,
            @QueryParam(ACCESS_TOKEN) String accessTokenFromQuery, @QueryParam("forUser") Boolean forUser) throws SessionNotExistException {

        MDC.put("context", ThreadCtx);
        try {
            String accessToken = this.getAccessToken(xAccessTokenFromHeader, accessTokenFromQuery);
            SOSAuthCurrentAccount currentAccount = this.getCurrentAccount(accessToken);
            SOSListOfPermissions sosListOfPermissions = new SOSListOfPermissions(currentAccount, forUser);
            return JOCDefaultResponse.responseStatus200(sosListOfPermissions.getSosPermissionShiro());
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e);
        } finally {
            MDC.remove("context");
        }
    }

    private String getAccessToken(String xAccessTokenFromHeader, String accessTokenFromQuery) {
        if (xAccessTokenFromHeader != null && !xAccessTokenFromHeader.isEmpty()) {
            accessTokenFromQuery = xAccessTokenFromHeader;
        }
        return accessTokenFromQuery;
    }

    private String createAccount(SOSAuthCurrentAccount currentAccount, DBItemIamIdentityService dbItemIdentityService) throws Exception {
        if (Globals.jocWebserviceDataContainer.getCurrentAccountsList() == null) {
            Globals.jocWebserviceDataContainer.setCurrentAccountsList(new SOSAuthCurrentAccountsList());
        }

        IdentityServiceTypes identityServiceType = IdentityServiceTypes.fromValue(dbItemIdentityService.getIdentityServiceType());
        String identityServiceName = dbItemIdentityService.getIdentityServiceName();
        SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(currentAccount);

        ISOSLogin sosLogin = null;

        switch (identityServiceType) {
        case SHIRO:
            SOSHibernateSession sosHibernateSession = null;
            try {
                sosHibernateSession = Globals.createSosHibernateStatelessConnection(CREATE_ACCOUNT);

                SOSShiroIniShare sosShiroIniShare = new SOSShiroIniShare(sosHibernateSession);
                sosShiroIniShare.provideIniFile();
            } finally {
                Globals.disconnect(sosHibernateSession);
            }
            sosLogin = new SOSShiroLogin(Globals.getShiroIniSecurityManagerFactory());
            LOGGER.debug("Login with idendity service shiro");
            break;
        case VAULT:
            sosLogin = new SOSVaultLogin();
            LOGGER.debug("Login with idendity service vault");
            break;
        case JOC:
            sosLogin = new SOSInternAuthLogin();
            sosLogin.setIdentityServiceId(dbItemIdentityService.getId());
            LOGGER.debug("Login with idendity service sosintern");
            break;
        default:
            LOGGER.debug("Login with idendity service shiro");
            sosLogin = new SOSShiroLogin(Globals.getShiroIniSecurityManagerFactory());

        }

        sosLogin.login(currentAccount.getAccountname(), currentAccount.getPassword(), currentAccount.getHttpServletRequest());

        ISOSAuthSubject sosAuthSubject = sosLogin.getCurrentSubject();

        currentAccount.setCurrentSubject(sosAuthSubject);
        currentAccount.setIdentityServices(new SOSIdentityService(dbItemIdentityService.getId(), dbItemIdentityService.getIdentityServiceName(),
                identityServiceType));

        if (sosAuthSubject == null || !sosAuthSubject.isAuthenticated()) {
            SOSAuthCurrentAccountAnswer sosShiroCurrentUserAnswer = new SOSAuthCurrentAccountAnswer(currentAccount.getAccountname());
            sosShiroCurrentUserAnswer.setIsAuthenticated(false);
            sosShiroCurrentUserAnswer.setMessage(sosLogin.getMsg());
            sosShiroCurrentUserAnswer.setIdentityService(identityServiceType.name() + ":" + identityServiceName);
            currentAccount.setCurrentSubject(null);
            throw new JocAuthenticationException(sosShiroCurrentUserAnswer);
        }

        SOSSessionHandler sosSessionHandler = new SOSSessionHandler(currentAccount);
        String accessToken = sosSessionHandler.getAccessToken().toString();

        currentAccount.setAccessToken(identityServiceName, accessToken);
        Globals.jocWebserviceDataContainer.getCurrentAccountsList().addAccount(currentAccount);

        resetTimeOut(currentAccount);

        ISOSSecurityConfiguration sosSecurityConfiguration;
        switch (identityServiceType) {
        case SHIRO:
            sosSecurityConfiguration = new SOSSecurityConfiguration();
            break;
        default:
            sosSecurityConfiguration = new SOSSecurityDBConfiguration();
        }

        SecurityConfiguration securityConfiguration = sosSecurityConfiguration.readConfiguration(null, identityServiceName);
        currentAccount.setRoles(securityConfiguration);

        Permissions sosPermissionJocCockpitControllers = sosPermissionsCreator.createJocCockpitPermissionControllerObjectList(securityConfiguration);
        currentAccount.setSosPermissionJocCockpitControllers(sosPermissionJocCockpitControllers);
        currentAccount.getCurrentSubject().getSession().setAttribute("username_joc_permissions", Globals.objectMapper.writeValueAsBytes(
                sosPermissionJocCockpitControllers));

        currentAccount.initFolders();

        Map<String, List<String>> fs = sosPermissionsCreator.getMapOfFolder();
        for (String role : fs.keySet()) {
            for (String folder : fs.get(role)) {
                currentAccount.addFolder(role, folder);
            }
        }

        if (Globals.sosCockpitProperties == null) {
            Globals.sosCockpitProperties = new JocCockpitProperties();
        }

        return sosLogin.getMsg();

    }

    private SOSAuthCurrentAccount getUserPwdFromHeaderOrQuery(String basicAuthorization, String clientCertCN, String user, String pwd)
            throws UnsupportedEncodingException, JocException {
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

        if (user.isEmpty() && clientCertCN != null) {
            user = clientCertCN;
        }
        return new SOSAuthCurrentAccount(user, pwd, authorization);
    }

    private SOSAuthCurrentAccountAnswer authenticate(SOSAuthCurrentAccount currentAccount) throws Exception {

        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("Login Identity Services");
            IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
            IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
            filter.setDisabled(false);
            filter.setRequired(true);

            List<DBItemIamIdentityService> listOfIdentityServices = iamIdentityServiceDBLayer.getIdentityServiceList(filter, 0);

            String msg = "";
            try {

                for (DBItemIamIdentityService dbItemIamIdentityService : listOfIdentityServices) {
                    msg = createAccount(currentAccount, dbItemIamIdentityService);
                    if (!msg.isEmpty()) {
                        LOGGER.info("Login with required Identity Service " + dbItemIamIdentityService.getIdentityServiceName() + " failed." + msg);

                    }
                }

                if (currentAccount.getCurrentSubject() == null) {
                    filter.setRequired(false);
                    listOfIdentityServices = iamIdentityServiceDBLayer.getIdentityServiceList(filter, 0);
                    if (listOfIdentityServices.size() == 0) {
                        LOGGER.info("No Identity Service is configured. Using SHIRO as the default identity service");
                        DBItemIamIdentityService dbItemIamIdentityService = new DBItemIamIdentityService();
                        dbItemIamIdentityService.setDisabled(false);
                        dbItemIamIdentityService.setIdentityServiceName("shiro");
                        dbItemIamIdentityService.setIdentityServiceType("SHIRO");
                        dbItemIamIdentityService.setOrdering(1);
                        dbItemIamIdentityService.setRequierd(false);
                        sosHibernateSession.setAutoCommit(false);
                        sosHibernateSession.beginTransaction();
                        sosHibernateSession.save(dbItemIamIdentityService);
                        sosHibernateSession.commit();
                        listOfIdentityServices.add(dbItemIamIdentityService);
                    }

                    msg = "";
                    for (DBItemIamIdentityService dbItemIamIdentityService : listOfIdentityServices) {
                        try {
                            msg = createAccount(currentAccount, dbItemIamIdentityService);
                            if (msg.isEmpty()) {
                                LOGGER.info("Login with Identity Service " + dbItemIamIdentityService.getIdentityServiceName() + " successful.");
                                break;
                            }

                        } catch (JocAuthenticationException e) {
                            continue;
                        }
                    }
                }
            } catch (JocAuthenticationException e) {
                msg = e.getMessage();
                LOGGER.info(e.getSosAuthCurrentAccountAnswer().getIdentityService());
                LOGGER.info(e.getMessage());
            }
            SOSAuthCurrentAccountAnswer sosAuthCurrentUserAnswer = new SOSAuthCurrentAccountAnswer(currentAccount.getAccountname());
            if (currentAccount.getCurrentSubject() == null || !currentAccount.getCurrentSubject().isAuthenticated()) {
                sosAuthCurrentUserAnswer.setIsAuthenticated(false);
                sosAuthCurrentUserAnswer.setMessage(String.format("%s: Could not login with account: %s password:*******", msg, currentAccount
                        .getAccountname()));
                throw new JocAuthenticationException(sosAuthCurrentUserAnswer);
            }

            SOSSessionHandler sosSessionHandler = new SOSSessionHandler(currentAccount);

            sosAuthCurrentUserAnswer.setIsAuthenticated(currentAccount.getCurrentSubject().isAuthenticated());
            sosAuthCurrentUserAnswer.setAccessToken(currentAccount.getAccessToken());
            sosAuthCurrentUserAnswer.setAccount(currentAccount.getAccountname());
            sosAuthCurrentUserAnswer.setRole(String.join(", ", currentAccount.getRoles()));
            sosAuthCurrentUserAnswer.setHasRole(!currentAccount.getRoles().isEmpty());
            sosAuthCurrentUserAnswer.setSessionTimeout(sosSessionHandler.getTimeout());
            sosAuthCurrentUserAnswer.setCallerHostName(currentAccount.getCallerHostName());
            sosAuthCurrentUserAnswer.setCallerIpAddress(currentAccount.getCallerIpAddress());
            sosAuthCurrentUserAnswer.setIdentityService(currentAccount.getIdentityServices().getIdentyServiceType() + ":" + currentAccount
                    .getIdentityServices().getIdentityServiceName());
            sosAuthCurrentUserAnswer.setMessage(msg);

            LOGGER.debug("CallerIpAddress=" + currentAccount.getCallerIpAddress());

            boolean enableTouch = "true".equals(Globals.sosCockpitProperties.getProperty(WebserviceConstants.ENABLE_SESSION_TOUCH,
                    WebserviceConstants.ENABLE_SESSION_TOUCH_DEFAULT));
            sosAuthCurrentUserAnswer.setEnableTouch(enableTouch);

            return sosAuthCurrentUserAnswer;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            SOSAuthCurrentAccountAnswer sosAuthCurrentUserAnswer = new SOSAuthCurrentAccountAnswer();
            sosAuthCurrentUserAnswer.setMessage(e.getMessage());
            sosAuthCurrentUserAnswer.setIsAuthenticated(false);
            return sosAuthCurrentUserAnswer;

        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

    protected JOCDefaultResponse login(HttpServletRequest request, String basicAuthorization, String clientCertCN, String user, String pwd)
            throws Exception {
        Globals.setServletBaseUri(uriInfo);

        if (Globals.sosCockpitProperties == null) {
            Globals.sosCockpitProperties = new JocCockpitProperties();
        }
        Globals.jocTimeZone = TimeZone.getDefault();
        Globals.setProperties();

        if (basicAuthorization == null || basicAuthorization.isEmpty()) {
            if (user == null) {
                user = clientCertCN;
            }
            if (pwd == null) {
                pwd = "";
            }
            String s = user + ":" + pwd;
            byte[] authEncBytes = org.apache.commons.codec.binary.Base64.encodeBase64(s.getBytes());
            String authStringEnc = new String(authEncBytes);
            basicAuthorization = "Basic " + authStringEnc;
        }

        TimeZone.setDefault(TimeZone.getTimeZone(UTC));

        SOSAuthCurrentAccount currentAccount = getUserPwdFromHeaderOrQuery(basicAuthorization, clientCertCN, user, pwd);

        if (currentAccount == null || currentAccount.getAuthorization() == null) {
            return JOCDefaultResponse.responseStatusJSError(AUTHORIZATION_HEADER_WITH_BASIC_BASED64PART_EXPECTED);
        }

        currentAccount.setAuthorization(basicAuthorization);
        currentAccount.setHttpServletRequest(request);

        Globals.loginUserName = currentAccount.getAccountname();

        SOSAuthCurrentAccountAnswer sosAuthCurrentUserAnswer = authenticate(currentAccount);

        if (request != null) {
            sosAuthCurrentUserAnswer.setCallerIpAddress(request.getRemoteAddr());
            sosAuthCurrentUserAnswer.setCallerHostName(request.getRemoteHost());
        }

        LOGGER.debug(String.format("Method: %s, Account: %s", "login", currentAccount.getAccountname()));

        Globals.jocWebserviceDataContainer.getCurrentAccountsList().removeTimedOutAccount(currentAccount.getAccountname());

        JocAuditLog jocAuditLog = new JocAuditLog(currentAccount.getAccountname(), "./login");
        AuditParams audit = new AuditParams();
        audit.setComment(currentAccount.getRolesAsString());
        jocAuditLog.logAuditMessage(audit);

        if (!sosAuthCurrentUserAnswer.isAuthenticated()) {
            LOGGER.info(sosAuthCurrentUserAnswer.getMessage());
            return JOCDefaultResponse.responseStatus401(sosAuthCurrentUserAnswer);
        } else {
            SOSSessionHandler sosSessionHandler = new SOSSessionHandler(currentAccount);
            return JOCDefaultResponse.responseStatus200WithHeaders(sosAuthCurrentUserAnswer, sosAuthCurrentUserAnswer.getAccessToken(),
                    sosSessionHandler.getTimeout());
        }

    }

    private void resetTimeOut(SOSAuthCurrentAccount currentAccount) throws SessionNotExistException {

        if (currentAccount != null) {
            SOSSessionHandler sosSessionHandler = new SOSSessionHandler(currentAccount);
            sosSessionHandler.touch();

        } else {
            LOGGER.error("No valid account");
        }
    }

    private SOSAuthCurrentAccountAnswer createSOSAuthCurrentAccountAnswer(String accessToken, String user, String message) {
        SOSAuthCurrentAccountAnswer sosAuthCurrentAccountAnswer = new SOSAuthCurrentAccountAnswer();
        sosAuthCurrentAccountAnswer.setAccessToken(accessToken);
        sosAuthCurrentAccountAnswer.setAccount(user);
        sosAuthCurrentAccountAnswer.setHasRole(false);
        sosAuthCurrentAccountAnswer.setIsAuthenticated(false);
        sosAuthCurrentAccountAnswer.setIsPermitted(false);
        sosAuthCurrentAccountAnswer.setMessage(message);
        return sosAuthCurrentAccountAnswer;
    }

}