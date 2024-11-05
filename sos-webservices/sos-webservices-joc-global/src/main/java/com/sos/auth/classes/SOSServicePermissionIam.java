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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.sos.auth.certificate.classes.SOSCertificateAuthLogin;
import com.sos.auth.client.ClientCertificateHandler;
import com.sos.auth.fido.classes.SOSFidoAuthLogin;
import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.auth.interfaces.ISOSLogin;
import com.sos.auth.keycloak.classes.SOSKeycloakLogin;
import com.sos.auth.ldap.classes.SOSLdapLogin;
import com.sos.auth.openid.SOSOpenIdHandler;
import com.sos.auth.openid.classes.SOSOpenIdLogin;
import com.sos.auth.openid.classes.SOSOpenIdWebserviceCredentials;
import com.sos.auth.sosintern.classes.SOSInternAuthLogin;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamBlockedAccount;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;
import com.sos.joc.db.security.IamHistoryDbLayer;
import com.sos.joc.db.security.IamIdentityServiceDBLayer;
import com.sos.joc.db.security.IamIdentityServiceFilter;
import com.sos.joc.exceptions.JocAuthenticationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.exceptions.JocWaitForSecondFactorException;
import com.sos.joc.exceptions.SessionNotExistException;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.security.configuration.SecurityConfiguration;
import com.sos.joc.model.security.configuration.permissions.Permissions;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;
import com.sos.joc.model.security.properties.oidc.OidcFlowTypes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

@Path("authentication")
public class SOSServicePermissionIam {

    private static final String ACCESS_TOKEN = "access_token";
    private static final String X_ACCESS_TOKEN = "X-Access-Token";
    private static final String UTC = "UTC";
    private static final String EMPTY_STRING = "";
    private static final String ACCESS_TOKEN_EXPECTED = "Access token header expected";
    private static final String ACCOUNT_IS_EMPTY = "No account is specified";
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSServicePermissionIam.class);
    private static final String AUTHORIZATION_HEADER_WITH_BASIC_BASED64PART_EXPECTED = "Authorization header with basic based64part expected";
    private static final String ThreadCtx = "authentication";

    @Context
    UriInfo uriInfo;

    @POST
    @Path("joc_cockpit_permissions")
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
    @Path("size")
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
    @Path("userbyname")
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
    @Path("userbytoken")
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
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse loginPost(@Context HttpServletRequest request, @HeaderParam("Authorization") String basicAuthorization,
            @HeaderParam("X-1ST-IDENTITY-SERVICE") String firstIdentityService, @HeaderParam("X-IDENTITY-SERVICE") String identityService,
            @HeaderParam("X-ID-TOKEN") String idToken, @HeaderParam("X-SIGNATURE") String signature,
            @HeaderParam("X-OPENID-CONFIGURATION") String openidConfiguration, @HeaderParam("X-AUTHENTICATOR-DATA") String authenticatorData,
            @HeaderParam("X-CLIENT-DATA-JSON") String clientDataJson, @HeaderParam("X-CREDENTIAL-ID") String credentialId,
            @HeaderParam("X-REQUEST-ID") String requestId, @QueryParam("account") String account, @QueryParam("pwd") String pwd) {

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
            if (((identityService == null) || (identityService.isEmpty())) && (idToken != null && !idToken.isEmpty())) {
                identityService = getOIDCClientCredentialIdentityService(idToken);
            }
            sosLoginParameters.setIdToken(idToken);
            sosLoginParameters.setBasicAuthorization(basicAuthorization);
            sosLoginParameters.setClientCertCN(clientCertCN);
            sosLoginParameters.setIdentityService(identityService);
            sosLoginParameters.setFirstIdentityService(firstIdentityService);
            sosLoginParameters.setRequest(request);
            sosLoginParameters.setAccount(account);
            sosLoginParameters.setClientDataJson(clientDataJson);
            sosLoginParameters.setOpenidConfiguration(openidConfiguration);
            sosLoginParameters.setAuthenticatorData(authenticatorData);
            sosLoginParameters.setSignature(signature);
            sosLoginParameters.setCredentialId(credentialId);
            sosLoginParameters.setRequestId(requestId);
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
    @Path("logout")
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
    @Path("db_refresh")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse dbRefresh() {
        MDC.put("context", ThreadCtx);
        try {
            if (Globals.sosHibernateFactory != null) {

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
    @Path("role")
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
    @Path("permission")
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
    @Path("permissions")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getPermissions(@HeaderParam(X_ACCESS_TOKEN) String xAccessTokenFromHeader,
            @QueryParam(ACCESS_TOKEN) String accessTokenFromQuery) throws SessionNotExistException {

        MDC.put("context", ThreadCtx);
        try {
            String accessToken = this.getAccessToken(xAccessTokenFromHeader, accessTokenFromQuery);
            this.getCurrentAccount(accessToken);
            SOSListOfPermissions sosListOfPermissions = new SOSListOfPermissions();
            return JOCDefaultResponse.responseStatus200(sosListOfPermissions.getSosPermissions());
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
        SOSIdentityService sosIdentityService = null;
        ISOSAuthSubject sosAuthSubject = null;

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
                LOGGER.debug("Login with identity service ldap");
                break;
            case KEYCLOAK:
            case KEYCLOAK_JOC:
                sosLogin = new SOSKeycloakLogin();
                LOGGER.debug("Login with identity service keycloak");
                break;
            case JOC:
                sosLogin = new SOSInternAuthLogin();
                LOGGER.debug("Login with identity service sosintern");
                break;
            case CERTIFICATE:
                sosLogin = new SOSCertificateAuthLogin();
                LOGGER.debug("Login with identity service certificate");
                break;
            case FIDO:
                sosLogin = new SOSFidoAuthLogin();
                LOGGER.debug("Login with identity service fido");
                break;
            case OIDC_JOC:
            case OIDC:
                sosLogin = new SOSOpenIdLogin();
                LOGGER.debug("Login with identity service openid_connect");
                break;
            default:
                sosLogin = new SOSInternAuthLogin();
                LOGGER.debug("Login with identity service sosintern");
            }

            sosIdentityService = new SOSIdentityService(dbItemIdentityService);

            sosLogin.setIdentityService(sosIdentityService);

            sosLogin.login(currentAccount, password);
            String msg = sosLogin.getMsg();

            sosAuthSubject = sosLogin.getCurrentSubject();
            Boolean secondFactorSuccess = null;
            try {
                secondFactorSuccess = SOSSecondFactorHandler.checkSecondFactor(currentAccount, dbItemIdentityService.getIdentityServiceName());
                if (secondFactorSuccess != null && !secondFactorSuccess) {
                    LOGGER.info("Login: second factor failed");
                    sosAuthSubject = null;
                }
            } catch (JocObjectNotExistException | JocAuthenticationException e) {
                sosAuthSubject = null;
                msg = e.getMessage();
            }

            currentAccount.setCurrentSubject(sosAuthSubject);
            currentAccount.setIdentityService(new SOSIdentityService(dbItemIdentityService.getId(), dbItemIdentityService.getIdentityServiceName(),
                    identityServiceType));

            if (sosAuthSubject == null || !sosAuthSubject.isAuthenticated()) {
                SOSAuthCurrentAccountAnswer sosAuthCurrentAccountAnswer = new SOSAuthCurrentAccountAnswer(currentAccount.getAccountname());
                sosAuthCurrentAccountAnswer.setIsAuthenticated(false);
                sosAuthCurrentAccountAnswer.setMessage(msg);
                sosAuthCurrentAccountAnswer.setIdentityService(identityServiceType.name() + ":" + identityServiceName);
                currentAccount.setCurrentSubject(null);

                throw new JocAuthenticationException(sosAuthCurrentAccountAnswer);
            }

            if (secondFactorSuccess == null && !currentAccount.getSosLoginParameters().isSecondPathOfTwoFactor() && sosIdentityService
                    .isTwoFactor()) {
                DBItemIamIdentityService dbItemSecondFactor = SOSSecondFactorHandler.getSecondFactor(dbItemIdentityService);
                SOSAuthCurrentAccountAnswer sosAuthCurrentAccountAnswer = new SOSAuthCurrentAccountAnswer(currentAccount.getAccountname());
                sosAuthCurrentAccountAnswer.setIsAuthenticated(true);
                sosAuthCurrentAccountAnswer.setAccessToken("");
                sosAuthCurrentAccountAnswer.setMessage("Second factor needed");
                sosAuthCurrentAccountAnswer.setIdentityService(identityServiceType.name() + ":" + identityServiceName);
                sosAuthCurrentAccountAnswer.setSecondFactoridentityService(dbItemSecondFactor.getIdentityServiceName());

                throw new JocWaitForSecondFactorException(sosAuthCurrentAccountAnswer);

            }
            SOSSessionHandler sosSessionHandler = new SOSSessionHandler(currentAccount);

            String accessToken = sosSessionHandler.getAccessToken().toString();
            currentAccount.setAccessToken(identityServiceName, accessToken);

            boolean authorization = true;
            if (currentAccount.getCurrentSubject() != null && currentAccount.getCurrentSubject().getListOfAccountPermissions() != null) {
                if (currentAccount.getCurrentSubject().getListOfAccountPermissions().size() == 0) {
                    if (currentAccount.getRoles().size() == 0) {
                        sosLogin.setMsg("login denied: no role assignment found");
                        currentAccount.setCurrentSubject(null);
                    } else {
                        if (currentAccount.getCurrentSubject().getListOfAccountPermissions().size() == 0) {
                            sosLogin.setMsg("login denied: no permission assignment found");
                            currentAccount.setCurrentSubject(null);
                        }
                    }
                    authorization = false;
                }
            }

            if (authorization) {
                Globals.jocWebserviceDataContainer.getCurrentAccountsList().addAccount(currentAccount);
                resetTimeOut(currentAccount);
            }

            if (Globals.sosCockpitProperties == null) {
                Globals.sosCockpitProperties = new JocCockpitProperties();
            }

            return sosLogin.getMsg();
        } else {
            return "Unknown Identity Service found: " + dbItemIdentityService.getIdentityServiceType();
        }

    }

    private SOSAuthCurrentAccount getUserFromHeaderOrQuery(String basicAuthorization, String clientCertCN, String account)
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
                account = authorization;
            }
        } else {
            account = authorization.substring(0, idx);
        }

        if (account.isEmpty() && clientCertCN != null) {
            account = clientCertCN;
        }
        return new SOSAuthCurrentAccount(account, !authorization.equals(EMPTY_STRING));
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

    private String getClientId(String idToken, com.sos.joc.model.security.properties.Properties properties) throws Exception {
        SOSOpenIdWebserviceCredentials sosOpenIdWebserviceCredentials = new SOSOpenIdWebserviceCredentials();
        sosOpenIdWebserviceCredentials.setValuesFromProperties(properties);
        sosOpenIdWebserviceCredentials.setIdToken(idToken);
        SOSOpenIdHandler sosOpenIdHandler = new SOSOpenIdHandler(sosOpenIdWebserviceCredentials);
        String clientId = sosOpenIdHandler.decodeIdToken(idToken);
        return clientId;
    }

    private String getOIDCClientCredentialIdentityService(String idToken) throws Exception {
        SOSHibernateSession sosHibernateSession = null;
        String clientId = "";

        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("Login Identity Services");
            IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
            IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
            filter.setIamIdentityServiceType(IdentityServiceTypes.OIDC);
            filter.setDisabled(false);
            List<DBItemIamIdentityService> listOfIdentityServices = iamIdentityServiceDBLayer.getIdentityServiceList(filter, 0);
            for (DBItemIamIdentityService dbItemIamIdentityService : listOfIdentityServices) {
                com.sos.joc.model.security.properties.Properties properties = SOSAuthHelper.getIamProperties(dbItemIamIdentityService
                        .getIdentityServiceName(), IdentityServiceTypes.OIDC);
                if (properties != null && properties.getOidc() != null) {
                    try {
                        clientId = getClientId(idToken, properties);

                        if (properties != null && properties.getOidc() != null && properties.getOidc().getIamOidcFlowType() != null && properties
                                .getOidc().getIamOidcFlowType().equals(OidcFlowTypes.CLIENT_CREDENTIAL) && properties.getOidc().getIamOidcClientId()
                                        .equals(clientId)) {
                            return dbItemIamIdentityService.getIdentityServiceName();
                        }
                    } finally {

                    }
                }
            }
            filter.setIamIdentityServiceType(IdentityServiceTypes.OIDC_JOC);
            listOfIdentityServices = iamIdentityServiceDBLayer.getIdentityServiceList(filter, 0);
            for (DBItemIamIdentityService dbItemIamIdentityService : listOfIdentityServices) {
                com.sos.joc.model.security.properties.Properties properties = SOSAuthHelper.getIamProperties(dbItemIamIdentityService
                        .getIdentityServiceName(), IdentityServiceTypes.OIDC_JOC);
                if (properties != null && properties.getOidc() != null && properties.getOidc().getIamOidcFlowType().equals(
                        OidcFlowTypes.CLIENT_CREDENTIAL) && properties.getOidc().getIamOidcClientId().equals(clientId)) {
                    return dbItemIamIdentityService.getIdentityServiceName();
                }
            }

        } finally {
            Globals.disconnect(sosHibernateSession);
        }
        return "";

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

                IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
                IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
                filter.setDisabled(false);
                filter.setRequired(true);
                filter.setSecondFactor(false);
                filter.setIdentityServiceName(currentAccount.getSosLoginParameters().getFirstIdentityService());

                List<DBItemIamIdentityService> listOfIdentityServices = iamIdentityServiceDBLayer.getIdentityServiceList(filter, 0);

                currentAccount.initFolders();

                if (!currentAccount.getSosLoginParameters().isOIDCLogin()) {

                    for (DBItemIamIdentityService dbItemIamIdentityService : listOfIdentityServices) {
                        if (!dbItemIamIdentityService.getIdentityServiceType().equals(IdentityServiceTypes.OIDC.value()) && !dbItemIamIdentityService
                                .getIdentityServiceType().equals(IdentityServiceTypes.OIDC_JOC.value())) {
                            msg = createAccount(currentAccount, password, dbItemIamIdentityService);
                            if (dbItemIamBlockedAccount != null) {
                                msg = "Account is blocked";
                            }
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
                                currentAccount.setCurrentSubject(null);
                                LOGGER.info("Login with required Identity Service " + dbItemIamIdentityService.getIdentityServiceName() + " failed. "
                                        + msg);
                                break;
                            }
                        }
                    }
                }

                if (msg.isEmpty()) {

                    if (currentAccount.getCurrentSubject() == null) {
                        filter.setRequired(false);
                        if (listOfIdentityServices.size() == 0) {
                            if (currentAccount.getSosLoginParameters().isOIDCLogin()) {
                                filter.setIamIdentityServiceType(IdentityServiceTypes.OIDC);
                                if (currentAccount.getSosLoginParameters().isSecondPathOfTwoFactor()) {
                                    filter.setIdentityServiceName(currentAccount.getSosLoginParameters().getFirstIdentityService());
                                } else {
                                    filter.setIdentityServiceName(currentAccount.getSosLoginParameters().getIdentityService());
                                }
                            }
                            List<DBItemIamIdentityService> listOfIdentityServicesOidc = iamIdentityServiceDBLayer.getIdentityServiceList(filter, 0);
                            filter.setIamIdentityServiceType(IdentityServiceTypes.OIDC_JOC);
                            List<DBItemIamIdentityService> listOfIdentityServicesOidcJoc = iamIdentityServiceDBLayer.getIdentityServiceList(filter,
                                    0);
                            listOfIdentityServices.addAll(listOfIdentityServicesOidc);
                            listOfIdentityServices.addAll(listOfIdentityServicesOidcJoc);

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
                                if (dbItemIamBlockedAccount != null) {
                                    msg = "Account is blocked";
                                    currentAccount.setCurrentSubject(null);
                                    throw new JocAuthenticationException(msg);
                                }
                                if (msg.isEmpty()) {
                                    String kid = "";
                                    if (currentAccount.getKid() != null && !currentAccount.getKid().isEmpty()) {
                                        kid = "[token verified using kid: " + currentAccount.getKid() + "]";
                                    }

                                    LOGGER.info("Authentication with Identity Service " + dbItemIamIdentityService.getIdentityServiceName()
                                            + " successful." + kid);
                                    addFolder(currentAccount);
                                    break;
                                }

                            } catch (JocAuthenticationException e) {
                                msg = e.getMessage();
                                LOGGER.info("Login with Identity Service " + dbItemIamIdentityService.getIdentityServiceName() + " failed:" + msg);
                                authenticationResult.put(dbItemIamIdentityService.getIdentityServiceName(), msg);
                                if (dbItemIamBlockedAccount == null) {
                                    continue;
                                }
                            } catch (JocWaitForSecondFactorException e) {
                                LOGGER.debug("First factor with Identity Service " + dbItemIamIdentityService.getIdentityServiceName()
                                        + " successful.");
                                msg = e.getMessage();
                                authenticationResult.put(dbItemIamIdentityService.getIdentityServiceName(), msg);
                                return e.getSosAuthCurrentAccountAnswer();
                            }

                        }

                    }
                }

                IamHistoryDbLayer iamHistoryDbLayer = new IamHistoryDbLayer(sosHibernateSession);

                if (currentAccount.getCurrentSubject() != null && currentAccount.getCurrentSubject().getListOfAccountPermissions() != null) {
                    iamHistoryDbLayer.addLoginAttempt(currentAccount, authenticationResult, true);
                    currentAccount.getCurrentSubject().getListOfAccountPermissions().addAll(setOfAccountPermissions);
                    SecurityConfiguration securityConfigurationEntry = sosPermissionMerger.mergePermissions();
                    SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(currentAccount);
                    Permissions sosPermissionJocCockpitControllers = sosPermissionsCreator.createJocCockpitPermissionControllerObjectList(
                            securityConfigurationEntry);
                    currentAccount.setSosPermissionJocCockpitControllers(sosPermissionJocCockpitControllers);
                } else {
                    iamHistoryDbLayer.addLoginAttempt(currentAccount, authenticationResult, false);
                }
                Globals.disconnect(sosHibernateSession);

            } catch (JocAuthenticationException e) {
                msg = e.getMessage();
                LOGGER.info(e.getSosAuthCurrentAccountAnswer().getIdentityService());
                LOGGER.info(e.getMessage());
            } catch (Exception e) {
                msg = e.getMessage();
                LOGGER.info(e.getMessage());
            } finally {
                Globals.disconnect(sosHibernateSession);
            }
            SOSAuthCurrentAccountAnswer sosAuthCurrentUserAnswer = new SOSAuthCurrentAccountAnswer(currentAccount.getAccountname());
            if (currentAccount.getCurrentSubject() == null || !currentAccount.getCurrentSubject().isAuthenticated()) {
                sosAuthCurrentUserAnswer.setIsAuthenticated(false);
                if (currentAccount.getIdentityService() != null) {
                    sosAuthCurrentUserAnswer.setIdentityService(currentAccount.getIdentityService().getIdentityServiceName());
                } else {
                    sosAuthCurrentUserAnswer.setIdentityService("");
                }
                Globals.jocWebserviceDataContainer.getSOSForceDelayHandler().addFailedLogin(currentAccount);
                Globals.jocWebserviceDataContainer.getSOSForceDelayHandler().forceDelay(currentAccount);

                sosAuthCurrentUserAnswer.setMessage(String.format("%s: Could not login", msg));
                throw new JocAuthenticationException(sosAuthCurrentUserAnswer);
            } else {
                Globals.jocWebserviceDataContainer.getSOSForceDelayHandler().resetFailedLogin(currentAccount);
            }

            SOSSessionHandler sosSessionHandler = new SOSSessionHandler(currentAccount);

            sosAuthCurrentUserAnswer.setIsAuthenticated(currentAccount.getCurrentSubject().isAuthenticated());
            sosAuthCurrentUserAnswer.setForcePasswordChange(currentAccount.getCurrentSubject().isForcePasswordChange());
            sosAuthCurrentUserAnswer.setAccessToken(currentAccount.getAccessToken());
            sosAuthCurrentUserAnswer.setAccount(currentAccount.getAccountname());
            sosAuthCurrentUserAnswer.setRole(String.join(", ", currentAccount.getRoles()));
            sosAuthCurrentUserAnswer.setHasRole(!currentAccount.getRoles().isEmpty());
            sosAuthCurrentUserAnswer.setSessionTimeout(sosSessionHandler.getTimeout());
            sosAuthCurrentUserAnswer.setCallerHostName(currentAccount.getCallerHostName());
            sosAuthCurrentUserAnswer.setCallerIpAddress(currentAccount.getCallerIpAddress());
            sosAuthCurrentUserAnswer.setIdentityService(currentAccount.getIdentityService().getIdentyServiceType() + ":" + currentAccount
                    .getIdentityService().getIdentityServiceName());

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
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("Login");

            Globals.setServletBaseUri(uriInfo);

            if (Globals.sosCockpitProperties == null) {
                Globals.sosCockpitProperties = new JocCockpitProperties();
            }


            Globals.jocTimeZone = TimeZone.getDefault();
            Globals.setProperties();
            
            Globals.setProperties();
            Globals.iamSessionTimeout = SOSAuthHelper.getSecondsFromString(Globals.getConfigurationGlobalsIdentityService().getIdleSessionTimeout()
                    .getValue());
            if (sosLoginParameters.basicAuthorizationHeaderIsEmpty()) {
                if (sosLoginParameters.getCredentialId() != null && !sosLoginParameters.getCredentialId().isEmpty()) {

                    IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
                    DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getAccountFromCredentialId(sosLoginParameters.getCredentialId());
                    if (dbItemIamAccount != null) {
                        byte[] authEncBytes = org.apache.commons.codec.binary.Base64.encodeBase64(dbItemIamAccount.getAccountName().getBytes());
                        String authStringEnc = new String(authEncBytes);
                        sosLoginParameters.setBasicAuthorization("Basic " + authStringEnc);
                    } else {
                        SOSAuthCurrentAccountAnswer sosAuthCurrentAccountAnswer = new SOSAuthCurrentAccountAnswer();
                        sosAuthCurrentAccountAnswer.setIsAuthenticated(false);
                        sosAuthCurrentAccountAnswer.setMessage("Could not find account for credential-id <" + sosLoginParameters.getCredentialId()
                                + ">");
                        sosAuthCurrentAccountAnswer.setIdentityService(sosLoginParameters.getIdentityService());
                        throw new JocAuthenticationException(sosAuthCurrentAccountAnswer);
                    }

                }
                if (sosLoginParameters.getAccount() == null) {
                    if (sosLoginParameters.isOIDCLogin()) {
                        DBItemIamIdentityService dbItemIamIdentityService;
                        if (sosLoginParameters.isFirstPathOfTwoFactor()) {
                            dbItemIamIdentityService = SOSAuthHelper.getCheckIdentityService(sosLoginParameters.getIdentityService());
                        } else {
                            dbItemIamIdentityService = SOSAuthHelper.getCheckIdentityService(sosLoginParameters.getFirstIdentityService());
                        }

                        SOSOpenIdWebserviceCredentials sosOpenIdWebserviceCredentials = new SOSOpenIdWebserviceCredentials();
                        SOSIdentityService sosIdentityService;
                        if (sosLoginParameters.isFirstPathOfTwoFactor()) {
                            sosIdentityService = new SOSIdentityService(sosLoginParameters.getIdentityService(), IdentityServiceTypes.fromValue(
                                    dbItemIamIdentityService.getIdentityServiceType()));
                        } else {
                            sosIdentityService = new SOSIdentityService(sosLoginParameters.getFirstIdentityService(), IdentityServiceTypes.fromValue(
                                    dbItemIamIdentityService.getIdentityServiceType()));
                        }
                        sosOpenIdWebserviceCredentials.setValuesFromProfile(sosIdentityService);
                        sosOpenIdWebserviceCredentials.setIdToken(sosLoginParameters.getIdToken());
                        sosOpenIdWebserviceCredentials.setOpenidConfiguration(sosLoginParameters.getOpenidConfiguration());
                        SOSOpenIdHandler sosOpenIdHandler = new SOSOpenIdHandler(sosOpenIdWebserviceCredentials);
                        String account = sosOpenIdHandler.decodeIdToken(sosLoginParameters.getIdToken());
                        sosLoginParameters.setAccount(account);
                        sosLoginParameters.setSOSOpenIdWebserviceCredentials(sosOpenIdWebserviceCredentials);

                    } else {
                        if (sosLoginParameters.getCredentialId() == null || sosLoginParameters.getCredentialId().isEmpty()) {
                            sosLoginParameters.setAccount(sosLoginParameters.getClientCertCN());
                        }
                    }
                }
                if (pwd == null) {
                    pwd = "";
                }

                if (sosLoginParameters.basicAuthorizationHeaderIsEmpty()) {
                    String s = sosLoginParameters.getAccount() + ":" + pwd;
                    byte[] authEncBytes = org.apache.commons.codec.binary.Base64.encodeBase64(s.getBytes());
                    String authStringEnc = new String(authEncBytes);
                    sosLoginParameters.setBasicAuthorization("Basic " + authStringEnc);
                }
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

                if (currentAccount.getIdentityService().getIdentyServiceType().equals(IdentityServiceTypes.KEYCLOAK) || currentAccount
                        .getIdentityService().getIdentyServiceType().equals(IdentityServiceTypes.KEYCLOAK_JOC)) {
                    if (Globals.jocWebserviceDataContainer.getSosAuthAccessTokenHandler() != null) {
                        Globals.jocWebserviceDataContainer.getSosAuthAccessTokenHandler().endExecution();
                        do {
                        } while (Globals.jocWebserviceDataContainer.getSosAuthAccessTokenHandler().isAlive());
                    }
                    Globals.jocWebserviceDataContainer.setSosAuthAccessTokenHandler(new SOSAuthAccessTokenHandler());
                    try {
                        LOGGER.debug("Starting thread to renew external access-token");
                        Globals.jocWebserviceDataContainer.getSosAuthAccessTokenHandler().start();
                    } catch (IllegalStateException e) {
                        LOGGER.debug(e.getMessage());
                    }
                }
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
        } finally {
            Globals.disconnect(sosHibernateSession);

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