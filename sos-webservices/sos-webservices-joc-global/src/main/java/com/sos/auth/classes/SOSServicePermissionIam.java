package com.sos.auth.classes;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.sos.auth.client.ClientCertificateHandler;
import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.auth.interfaces.ISOSLogin;
import com.sos.auth.keycloak.classes.SOSKeycloakLogin;
import com.sos.auth.ldap.classes.SOSLdapLogin;
import com.sos.auth.openid.classes.SOSOpenIdLogin;
import com.sos.auth.sosintern.classes.SOSInternAuthLogin;
import com.sos.auth.vault.classes.SOSVaultLogin;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.db.authentication.DBItemIamBlockedAccount;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;
import com.sos.joc.db.security.IamHistoryDbLayer;
import com.sos.joc.db.security.IamIdentityServiceDBLayer;
import com.sos.joc.db.security.IamIdentityServiceFilter;
import com.sos.joc.exceptions.JocAuthenticationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.SessionNotExistException;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.security.configuration.SecurityConfiguration;
import com.sos.joc.model.security.configuration.permissions.Permissions;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;

@Path("/authentication")
public class SOSServicePermissionIam {

    private static final String ACCESS_TOKEN = "access_token";
    private static final String X_ACCESS_TOKEN = "X-Access-Token";
    private static final String UTC = "UTC";
    private static final String EMPTY_STRING = "";
    private static final String ACCESS_TOKEN_EXPECTED = "Access token header expected";
    private static final String AUTHORIZATION_HEADER_WITH_BASIC_BASED64PART_EXPECTED = "Authorization header with basic based64part expected";
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSServicePermissionIam.class);
    private static final String ThreadCtx = "authentication";

    @Context
    UriInfo uriInfo;

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
            @HeaderParam("X-IDENTIY-SERVICE") String identityService, @HeaderParam("X-ACCESS-TOKEN") String accessToken,
            @QueryParam("account") String account, @QueryParam("pwd") String pwd) {

        if (Globals.sosCockpitProperties == null) {
            Globals.sosCockpitProperties = new JocCockpitProperties();
        } else {
            Globals.sosCockpitProperties.touchLog4JConfiguration();
        }

        MDC.put("context", ThreadCtx);
        String clientCertCN = null;
        try {
            if (request != null) {
                try {
                    ClientCertificateHandler clientCertHandler = new ClientCertificateHandler(request);
                    clientCertCN = clientCertHandler.getClientCN();
                    if (clientCertCN == null) {
                        LOGGER.info("Client Certificate CN read from Login: n/a");
                    } else {
                        LOGGER.info("Client Certificate CN read from Login");
                    }
                } catch (IOException e) {
                    LOGGER.debug("No Client certificate read from HttpServletRequest.");
                }
            }
            SOSLoginParameters sosLoginParameters = new SOSLoginParameters();
            sosLoginParameters.setAccessToken(accessToken);
            sosLoginParameters.setBasicAuthorization(basicAuthorization);
            sosLoginParameters.setClientCertCN(clientCertCN);
            sosLoginParameters.setIdentityService(identityService);
            sosLoginParameters.setRequest(request);
            sosLoginParameters.setAccount(account);

            return login(sosLoginParameters, pwd);
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

    protected JOCDefaultResponse logout(String accessToken) {

        if (accessToken == null || accessToken.isEmpty()) {
            return JOCDefaultResponse.responseStatusJSError(ACCESS_TOKEN_EXPECTED);
        }
        SOSAuthCurrentAccount currentAccount = null;
        if (Globals.jocWebserviceDataContainer != null && Globals.jocWebserviceDataContainer.getCurrentAccountsList() != null) {
            currentAccount = Globals.jocWebserviceDataContainer.getCurrentAccountsList().getAccount(accessToken);
        }

        String account = "";
        String comment = "";
        if (currentAccount != null) {
            account = currentAccount.getAccountname();

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
        }
        SOSAuthCurrentAccountAnswer sosAuthCurrentAccountAnswer = new SOSAuthCurrentAccountAnswer(EMPTY_STRING);
        if (currentAccount != null) {
            sosAuthCurrentAccountAnswer = new SOSAuthCurrentAccountAnswer(currentAccount.getAccountname());
        }
        sosAuthCurrentAccountAnswer.setIsAuthenticated(false);
        sosAuthCurrentAccountAnswer.setHasRole(false);
        sosAuthCurrentAccountAnswer.setIsPermitted(false);
        sosAuthCurrentAccountAnswer.setAccessToken(EMPTY_STRING);
        if (Globals.jocWebserviceDataContainer.getCurrentAccountsList() != null) {
            Globals.jocWebserviceDataContainer.getCurrentAccountsList().removeAccount(accessToken);
        }

        return JOCDefaultResponse.responseStatus200(sosAuthCurrentAccountAnswer);

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
            SOSAuthCurrentAccountAnswer sosAuthCurrentAccountAnswer = hasRole(accessToken, role);
            return JOCDefaultResponse.responseStatus200(sosAuthCurrentAccountAnswer);
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

        SOSAuthCurrentAccountAnswer sosAuthCurrentAccountAnswer = new SOSAuthCurrentAccountAnswer(currentAccount.getAccountname());
        sosAuthCurrentAccountAnswer.setRole(role);
        sosAuthCurrentAccountAnswer.setIsAuthenticated(currentAccount.isAuthenticated());
        sosAuthCurrentAccountAnswer.setHasRole(currentAccount.hasRole(role));
        sosAuthCurrentAccountAnswer.setAccessToken(currentAccount.getAccessToken());
        return sosAuthCurrentAccountAnswer;
    }

    @GET
    @Path("/permission")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse isPermitted(@HeaderParam(X_ACCESS_TOKEN) String xAccessTokenFromHeader,
            @QueryParam(ACCESS_TOKEN) String accessTokenFromQuery, @QueryParam("permission") String permission) throws SessionNotExistException {

        MDC.put("context", ThreadCtx);
        try {
            String accessToken = getAccessToken(xAccessTokenFromHeader, accessTokenFromQuery);
            SOSAuthCurrentAccountAnswer sosAuthCurrentAccountAnswer = isPermitted(accessToken, permission);
            return JOCDefaultResponse.responseStatus200(sosAuthCurrentAccountAnswer);
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

        SOSAuthCurrentAccountAnswer sosAuthCurrentAccountAnswer = new SOSAuthCurrentAccountAnswer(currentAccount.getAccountname());
        sosAuthCurrentAccountAnswer.setPermission(permission);
        sosAuthCurrentAccountAnswer.setIsAuthenticated(currentAccount.isAuthenticated());
        sosAuthCurrentAccountAnswer.setIsPermitted(currentAccount.isPermitted(permission));

        sosAuthCurrentAccountAnswer.setAccessToken(currentAccount.getAccessToken());
        return sosAuthCurrentAccountAnswer;
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
            @QueryParam(ACCESS_TOKEN) String accessTokenFromQuery) throws SessionNotExistException {

        MDC.put("context", ThreadCtx);
        try {
            String accessToken = this.getAccessToken(xAccessTokenFromHeader, accessTokenFromQuery);
            SOSAuthCurrentAccount currentAccount = this.getCurrentAccount(accessToken);
            SOSListOfPermissions sosListOfPermissions = new SOSListOfPermissions(currentAccount);
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

    private String createAccount(SOSAuthCurrentAccount currentAccount, String password, DBItemIamIdentityService dbItemIdentityService)
            throws Exception {
        if (Globals.jocWebserviceDataContainer.getCurrentAccountsList() == null) {
            Globals.jocWebserviceDataContainer.setCurrentAccountsList(new SOSAuthCurrentAccountsList());
        }
        IdentityServiceTypes identityServiceType = null;
        try {
            identityServiceType = IdentityServiceTypes.fromValue(dbItemIdentityService.getIdentityServiceType());
        } catch (IllegalArgumentException e) {
        }

        if (identityServiceType != null) {
            String identityServiceName = dbItemIdentityService.getIdentityServiceName();

            ISOSLogin sosLogin = null;

            switch (identityServiceType) {

            case LDAP:
            case LDAP_JOC:
                sosLogin = new SOSLdapLogin();
                LOGGER.debug("Login with idendity service ldap");
                break;
            case VAULT:
            case VAULT_JOC:
            case VAULT_JOC_ACTIVE:
                sosLogin = new SOSVaultLogin();
                LOGGER.debug("Login with idendity service vault");
                break;
            case KEYCLOAK:
            case KEYCLOAK_JOC:
                sosLogin = new SOSKeycloakLogin();
                LOGGER.debug("Login with idendity service keycloak");
                break;
            case JOC:
                sosLogin = new SOSInternAuthLogin();
                LOGGER.debug("Login with idendity service sosintern");
                break;
            case OPENID_CONNECT:
                sosLogin = new SOSOpenIdLogin();
                LOGGER.debug("Login with idendity service openid_connect");
                break;
            default:
                sosLogin = new SOSInternAuthLogin();
                LOGGER.debug("Login with idendity service sosintern");
            }

            sosLogin.setIdentityService(new SOSIdentityService(dbItemIdentityService));
            sosLogin.login(currentAccount, password);
            // sosLogin.login(currentAccount.getAccountname(), password, currentAccount.getHttpServletRequest());

            ISOSAuthSubject sosAuthSubject = sosLogin.getCurrentSubject();

            currentAccount.setCurrentSubject(sosAuthSubject);
            currentAccount.setIdentityServices(new SOSIdentityService(dbItemIdentityService.getId(), dbItemIdentityService.getIdentityServiceName(),
                    identityServiceType));

            if (sosAuthSubject == null || !sosAuthSubject.isAuthenticated()) {
                SOSAuthCurrentAccountAnswer sosAuthCurrentAccountAnswer = new SOSAuthCurrentAccountAnswer(currentAccount.getAccountname());
                sosAuthCurrentAccountAnswer.setIsAuthenticated(false);
                sosAuthCurrentAccountAnswer.setMessage(sosLogin.getMsg());
                sosAuthCurrentAccountAnswer.setIdentityService(identityServiceType.name() + ":" + identityServiceName);
                currentAccount.setCurrentSubject(null);

                throw new JocAuthenticationException(sosAuthCurrentAccountAnswer);
            }
            SOSSessionHandler sosSessionHandler = new SOSSessionHandler(currentAccount);
            String accessToken = sosSessionHandler.getAccessToken().toString();

            currentAccount.setAccessToken(identityServiceName, accessToken);
            Globals.jocWebserviceDataContainer.getCurrentAccountsList().addAccount(currentAccount);

            resetTimeOut(currentAccount);

            if (Globals.sosCockpitProperties == null) {
                Globals.sosCockpitProperties = new JocCockpitProperties();
            }
            return sosLogin.getMsg();
        } else {
            return "Unknown Identity Service found: " + dbItemIdentityService.getIdentityServiceType();
        }

    }

    private SOSAuthCurrentAccount getUserFromHeaderOrQuery(String basicAuthorization, String clientCertCN, String user)
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
        }

        if (user.isEmpty() && clientCertCN != null) {
            user = clientCertCN;
        }
        return new SOSAuthCurrentAccount(user, !authorization.equals(EMPTY_STRING));
    }

    private String getPwdFromHeaderOrQuery(String basicAuthorization, String pwd) throws UnsupportedEncodingException, JocException {
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
                pwd = null;
            }
        } else {
            pwd = authorization.substring(idx + 1);
        }

        return pwd;
    }

    private void addFolder(SOSAuthCurrentAccount currentAccount) {
        SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(currentAccount);

        Map<String, List<String>> fs = sosPermissionsCreator.getMapOfFolder();
        for (String role : fs.keySet()) {
            for (String folder : fs.get(role)) {
                currentAccount.addFolder(role, folder);
            }
        }
    }

    private SOSAuthCurrentAccountAnswer authenticate(SOSAuthCurrentAccount currentAccount, String password) throws Exception {

        try {
            SOSPermissionMerger sosPermissionMerger = new SOSPermissionMerger();
            SOSHibernateSession sosHibernateSession = null;
            String msg = "";
            try {
                sosHibernateSession = Globals.createSosHibernateStatelessConnection("Login Identity Services");

                IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
                IamAccountFilter iamAccountFilter = new IamAccountFilter();
                iamAccountFilter.setAccountName(currentAccount.getAccountname());
                DBItemIamBlockedAccount dbItemIamBlockedAccount = iamAccountDBLayer.getBlockedAccount(iamAccountFilter);

                Map<String, String> authenticationResult = new HashMap<String, String>();
                Set<String> setOfAccountPermissions = new HashSet<String>();

                if (dbItemIamBlockedAccount != null) {
                    msg = "Account is blocked";
                    authenticationResult.put("*none", msg);
                } else {

                    IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
                    IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
                    filter.setDisabled(false);
                    filter.setRequired(true);

                    List<DBItemIamIdentityService> listOfIdentityServices = iamIdentityServiceDBLayer.getIdentityServiceList(filter, 0);

                    currentAccount.initFolders();

                    for (DBItemIamIdentityService dbItemIamIdentityService : listOfIdentityServices) {
                        if (!dbItemIamIdentityService.getIdentityServiceType().equals(IdentityServiceTypes.OPENID_CONNECT.value())) {
                            msg = createAccount(currentAccount, password, dbItemIamIdentityService);
                            if (msg.isEmpty()) {
                                SecurityConfiguration securityConfiguration = sosPermissionMerger.addIdentityService(new SOSIdentityService(
                                        dbItemIamIdentityService));
                                currentAccount.setRoles(securityConfiguration);
                                if (currentAccount.getCurrentSubject().getListOfAccountPermissions() != null) {
                                    setOfAccountPermissions.addAll(currentAccount.getCurrentSubject().getListOfAccountPermissions());
                                }
                                addFolder(currentAccount);
                            } else {
                                authenticationResult.put(dbItemIamIdentityService.getIdentityServiceName(), msg);
                                LOGGER.info("Login with required Identity Service " + dbItemIamIdentityService.getIdentityServiceName() + " failed."
                                        + msg);
                            }
                        }
                    }

                    if (currentAccount.getCurrentSubject() == null) {
                        filter.setRequired(false);
                        if (listOfIdentityServices.size() == 0) {
                            if (currentAccount.getSosLoginParameters().getIdentityService() != null && currentAccount.getSosLoginParameters()
                                    .getIdentityService().equals(IdentityServiceTypes.OPENID_CONNECT.value())) {
                                filter.setIdentityServiceName(currentAccount.getSosLoginParameters().getIdentityService());
                            }
                            listOfIdentityServices = iamIdentityServiceDBLayer.getIdentityServiceList(filter, 0);
                            if (listOfIdentityServices.size() == 0) {
                                LOGGER.info("No enabled Identity Service is configured.");
                            }
                        }

                        msg = "";
                        for (DBItemIamIdentityService dbItemIamIdentityService : listOfIdentityServices) {
                            try {
                                msg = createAccount(currentAccount, password, dbItemIamIdentityService);
                                SecurityConfiguration securityConfiguration = sosPermissionMerger.addIdentityService(new SOSIdentityService(
                                        dbItemIamIdentityService));
                                currentAccount.setRoles(securityConfiguration);

                                if (msg.isEmpty()) {
                                    LOGGER.info("Login with Identity Service " + dbItemIamIdentityService.getIdentityServiceName() + " successful.");
                                    addFolder(currentAccount);
                                    break;
                                }

                            } catch (JocAuthenticationException e) {
                                LOGGER.info("Login with Identity Service " + dbItemIamIdentityService.getIdentityServiceName() + " failed.");
                                msg = e.getMessage();
                                authenticationResult.put(dbItemIamIdentityService.getIdentityServiceName(), msg);
                                continue;
                            }
                        }

                    }

                }
                IamHistoryDbLayer iamHistoryDbLayer = new IamHistoryDbLayer(sosHibernateSession);

                if (currentAccount.getCurrentSubject() != null && currentAccount.getCurrentSubject().getListOfAccountPermissions() != null) {
                    iamHistoryDbLayer.addLoginAttempt(currentAccount.getAccountname(), authenticationResult, true);
                    currentAccount.getCurrentSubject().getListOfAccountPermissions().addAll(setOfAccountPermissions);
                    SecurityConfiguration securityConfigurationEntry = sosPermissionMerger.mergePermissions();
                    SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(currentAccount);
                    Permissions sosPermissionJocCockpitControllers = sosPermissionsCreator.createJocCockpitPermissionControllerObjectList(
                            securityConfigurationEntry);
                    currentAccount.setSosPermissionJocCockpitControllers(sosPermissionJocCockpitControllers);
                } else {
                    iamHistoryDbLayer.addLoginAttempt(currentAccount.getAccountname(), authenticationResult, false);
                }
                Globals.disconnect(sosHibernateSession);

            } catch (JocAuthenticationException e) {
                msg = e.getMessage();
                LOGGER.info(e.getSosAuthCurrentAccountAnswer().getIdentityService());
                LOGGER.info(e.getMessage());
            } finally {
                Globals.disconnect(sosHibernateSession);
            }
            SOSAuthCurrentAccountAnswer sosAuthCurrentUserAnswer = new SOSAuthCurrentAccountAnswer(currentAccount.getAccountname());
            if (currentAccount.getCurrentSubject() == null || !currentAccount.getCurrentSubject().isAuthenticated()) {
                sosAuthCurrentUserAnswer.setIsAuthenticated(false);
                if (currentAccount.getIdentityServices() != null) {
                    sosAuthCurrentUserAnswer.setIdentityService(currentAccount.getIdentityServices().getIdentityServiceName());
                } else {
                    sosAuthCurrentUserAnswer.setIdentityService("");
                }

                sosAuthCurrentUserAnswer.setMessage(String.format("%s: Could not login with account/password", msg));
                throw new JocAuthenticationException(sosAuthCurrentUserAnswer);
            }

            SOSSessionHandler sosSessionHandler = new SOSSessionHandler(currentAccount);

            sosAuthCurrentUserAnswer.setIsAuthenticated(currentAccount.getCurrentSubject().isAuthenticated());
            sosAuthCurrentUserAnswer.setIsForcePasswordChange(currentAccount.getCurrentSubject().isForcePasswordChange());
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
        } catch (

        JocAuthenticationException e) {
            return e.getSosAuthCurrentAccountAnswer();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            SOSAuthCurrentAccountAnswer sosAuthCurrentUserAnswer = new SOSAuthCurrentAccountAnswer();
            sosAuthCurrentUserAnswer.setMessage(e.getMessage());
            sosAuthCurrentUserAnswer.setIsAuthenticated(false);
            return sosAuthCurrentUserAnswer;

        }
    }

    protected JOCDefaultResponse login(SOSLoginParameters sosLoginParameters, String pwd) throws Exception {
        Globals.setServletBaseUri(uriInfo);

        if (Globals.sosCockpitProperties == null) {
            Globals.sosCockpitProperties = new JocCockpitProperties();
        }
        Globals.jocTimeZone = TimeZone.getDefault();
        Globals.setProperties();

        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("login");
            DBItemJocConfiguration dbItem = null;
            JocConfigurationFilter filter = new JocConfigurationFilter();
            filter.setConfigurationType(SOSAuthHelper.CONFIGURATION_TYPE_IAM);
            filter.setObjectType(SOSAuthHelper.OBJECT_TYPE_IAM_GENERAL);
            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
            List<DBItemJocConfiguration> listOfDbItemJocConfiguration = jocConfigurationDBLayer.getJocConfigurations(filter, 0);
            if (listOfDbItemJocConfiguration.size() == 1) {
                dbItem = listOfDbItemJocConfiguration.get(0);
                com.sos.joc.model.security.properties.Properties properties = Globals.objectMapper.readValue(dbItem.getConfigurationItem(),
                        com.sos.joc.model.security.properties.Properties.class);
                Globals.iamSessionTimeout = properties.getSessionTimeout();
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

        if (sosLoginParameters.getBasicAuthorization() == null || sosLoginParameters.getBasicAuthorization().isEmpty()) {
            if (sosLoginParameters.getAccount() == null) {
                sosLoginParameters.setAccount(sosLoginParameters.getClientCertCN());
            }
            if (pwd == null) {
                pwd = "";
            }

            String s = sosLoginParameters.getAccount() + ":" + pwd;
            byte[] authEncBytes = org.apache.commons.codec.binary.Base64.encodeBase64(s.getBytes());
            String authStringEnc = new String(authEncBytes);
            sosLoginParameters.setBasicAuthorization("Basic " + authStringEnc);
        }

        TimeZone.setDefault(TimeZone.getTimeZone(UTC));

        SOSAuthCurrentAccount currentAccount = getUserFromHeaderOrQuery(sosLoginParameters.getBasicAuthorization(), sosLoginParameters
                .getClientCertCN(), sosLoginParameters.getAccount());
        String password = getPwdFromHeaderOrQuery(sosLoginParameters.getBasicAuthorization(), pwd);

        if (currentAccount == null || !currentAccount.withAuthorization()) {
            return JOCDefaultResponse.responseStatusJSError(AUTHORIZATION_HEADER_WITH_BASIC_BASED64PART_EXPECTED);
        }

        currentAccount.setSosLoginParameters(sosLoginParameters);

        SOSAuthCurrentAccountAnswer sosAuthCurrentUserAnswer = null;

        sosAuthCurrentUserAnswer = authenticate(currentAccount, password);

        if (sosLoginParameters.getRequest() != null) {
            sosAuthCurrentUserAnswer.setCallerIpAddress(sosLoginParameters.getRequest().getRemoteAddr());
            sosAuthCurrentUserAnswer.setCallerHostName(sosLoginParameters.getRequest().getRemoteHost());
        }

        LOGGER.debug(String.format("Method: %s, Account: %s", "login", currentAccount.getAccountname()));
        JocAuditLog jocAuditLog = new JocAuditLog(currentAccount.getAccountname(), "./login");
        AuditParams audit = new AuditParams();

        if (Globals.jocWebserviceDataContainer.getCurrentAccountsList() != null) {
            Globals.jocWebserviceDataContainer.getCurrentAccountsList().removeTimedOutAccount(currentAccount.getAccountname());

            if (Globals.jocWebserviceDataContainer.getSosAuthAccessTokenHandler() != null) {
                Globals.jocWebserviceDataContainer.getSosAuthAccessTokenHandler().endExecution();
                do {
                } while (Globals.jocWebserviceDataContainer.getSosAuthAccessTokenHandler().isAlive());
            }
            Globals.jocWebserviceDataContainer.setSosAuthAccessTokenHandler(new SOSAuthAccessTokenHandler());
            Globals.jocWebserviceDataContainer.getSosAuthAccessTokenHandler().start();

            audit.setComment(currentAccount.getRolesAsString());
        }
        if (!sosAuthCurrentUserAnswer.isAuthenticated()) {
            audit.setComment("===> Failed login");
            jocAuditLog.logAuditMessage(audit);
            return JOCDefaultResponse.responseStatus401(sosAuthCurrentUserAnswer);
        } else {
            jocAuditLog.logAuditMessage(audit);
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

}