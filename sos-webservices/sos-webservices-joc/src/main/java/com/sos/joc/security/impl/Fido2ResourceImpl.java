package com.sos.joc.security.impl;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSInitialPasswordSetting;
import com.sos.auth.classes.SOSPasswordHasher;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.security.Fido2ConfirmationMail;
import com.sos.joc.classes.security.SOSBlocklist;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamFido2Registration;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;
import com.sos.joc.db.security.IamFido2DBLayer;
import com.sos.joc.db.security.IamFido2RegistrationFilter;
import com.sos.joc.db.security.IamIdentityServiceDBLayer;
import com.sos.joc.db.security.IamIdentityServiceFilter;
import com.sos.joc.exceptions.JocAuthenticationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocInfoException;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.security.accounts.AccountListFilter;
import com.sos.joc.model.security.accounts.AccountsFilter;
import com.sos.joc.model.security.blocklist.BlockedAccount;
import com.sos.joc.model.security.blocklist.BlockedAccountsDeleteFilter;
import com.sos.joc.model.security.fido2.CipherTypes;
import com.sos.joc.model.security.fido2.Fido2Registration;
import com.sos.joc.model.security.fido2.Fido2RegistrationFilter;
import com.sos.joc.model.security.fido2.Fido2RegistrationListFilter;
import com.sos.joc.model.security.fido2.Fido2Registrations;
import com.sos.joc.model.security.fido2.Fido2RegistrationsFilter;
import com.sos.joc.model.security.fido2.Fido2RequestAuthentication;
import com.sos.joc.model.security.identityservice.Fido2IdentityProvider;
import com.sos.joc.model.security.identityservice.IdentityServiceFilter;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;
import com.sos.joc.model.security.properties.fido2.Fido2Attestation;
import com.sos.joc.model.security.properties.fido2.Fido2Transports;
import com.sos.joc.model.security.properties.fido2.Fido2Userverification;
import com.sos.joc.security.classes.SOSRSAUtil;
import com.sos.joc.security.resource.IFido2Resource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("iam")
public class Fido2ResourceImpl extends JOCResourceImpl implements IFido2Resource {

    private static final Logger LOGGER = LoggerFactory.getLogger(Fido2ResourceImpl.class);
    private static final String API_CALL_FIDO2_REGISTRATIONS = "./iam/fido2registrations";
    private static final String API_CALL_FIDO2_REGISTRATION_READ = "./iam/fido2registration";
    private static final String API_CALL_FIDO2_REGISTRATION_STORE = "./iam/fido2registration/store";
    private static final String API_CALL_FIDO2_REGISTRATION_DELETE = "./iam/fido2registration/delete";
    private static final String API_CALL_APPROVE = "./iam/fido2registration/approve";
    private static final String API_CALL_REJECT = "./iam/fido2registration/reject";
    private static final String API_CALL_IDENTITY_CLIENTS = "./iam/fido2registration/identityclients";
    private static final String API_CALL_REQUEST_AUTHENTICATION = "./iam/fido2/request_authentication";

    @Override
    public JOCDefaultResponse postFido2RegistrationRead(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            initLogging(API_CALL_FIDO2_REGISTRATION_READ, body, accessToken);
            JsonValidator.validateFailFast(body, Fido2RegistrationFilter.class);
            Fido2RegistrationFilter fido2RegistrationFilter = Globals.objectMapper.readValue(body, Fido2RegistrationFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            Fido2Registration fido2Registration = new Fido2Registration();

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_FIDO2_REGISTRATION_READ);

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, fido2RegistrationFilter
                    .getIdentityServiceName());

            if (!IdentityServiceTypes.FIDO_2.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                throw new JocObjectNotExistException("Only allowed for Identity Service type FIDO2 " + "<" + dbItemIamIdentityService
                        .getIdentityServiceType() + ">");
            }

            IamFido2DBLayer iamFido2DBLayer = new IamFido2DBLayer(sosHibernateSession);
            IamFido2RegistrationFilter filter = new IamFido2RegistrationFilter();
            filter.setIdentityServiceId(dbItemIamIdentityService.getId());
            filter.setAccountName(fido2RegistrationFilter.getAccountName());

            DBItemIamFido2Registration dbItemIamFido2Registration = iamFido2DBLayer.getUniqueFido2Registration(filter);
            if (dbItemIamFido2Registration != null) {
                fido2Registration.setAccountName(dbItemIamFido2Registration.getAccountName());
                fido2Registration.setIdentityServiceName(dbItemIamIdentityService.getIdentityServiceName());
                fido2Registration.setConfirmed(dbItemIamFido2Registration.getConfirmed());
                fido2Registration.setDeferred(dbItemIamFido2Registration.getDeferred());
                fido2Registration.setEmail(dbItemIamFido2Registration.getEmail());
                fido2Registration.setPublicKey(dbItemIamFido2Registration.getPublicKey());
            } else {
                throw new JocObjectNotExistException("Couldn't find the registration <" + fido2RegistrationFilter.getAccountName()
                        + " in identity service " + fido2RegistrationFilter.getIdentityServiceName() + ">");
            }
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(fido2Registration));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postFido2RequestRegistration(byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            Fido2Registration fido2Registration = Globals.objectMapper.readValue(body, Fido2Registration.class);

            initLogging(API_CALL_FIDO2_REGISTRATION_STORE, null);
            JsonValidator.validateFailFast(body, Fido2Registration.class);

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_FIDO2_REGISTRATION_STORE);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, fido2Registration
                    .getIdentityServiceName());

            if (!IdentityServiceTypes.FIDO_2.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                throw new JocObjectNotExistException("Only allowed for Identity Service type FIDO2 " + "<" + dbItemIamIdentityService
                        .getIdentityServiceType() + ">");
            }

            IamFido2DBLayer iamFido2DBLayer = new IamFido2DBLayer(sosHibernateSession);

            IamFido2RegistrationFilter iamFido2RegistrationFilter = new IamFido2RegistrationFilter();
            iamFido2RegistrationFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            iamFido2RegistrationFilter.setAccountName(fido2Registration.getAccountName());
            DBItemIamFido2Registration dbItemIamFido2Registration = iamFido2DBLayer.getUniqueFido2Registration(iamFido2RegistrationFilter);
            boolean isNew = false;
            if (dbItemIamFido2Registration == null) {
                dbItemIamFido2Registration = new DBItemIamFido2Registration();
                isNew = true;
            }

            dbItemIamFido2Registration.setConfirmed(fido2Registration.getConfirmed());
            dbItemIamFido2Registration.setDeferred(fido2Registration.getDeferred());
            dbItemIamFido2Registration.setEmail(fido2Registration.getEmail());
            dbItemIamFido2Registration.setPublicKey(fido2Registration.getPublicKey());
            dbItemIamFido2Registration.setAccountName(fido2Registration.getAccountName());
            dbItemIamFido2Registration.setIdentityServiceId(dbItemIamIdentityService.getId());
            dbItemIamFido2Registration.setToken(SOSAuthHelper.createAccessToken());
            dbItemIamFido2Registration.setCreated(new Date());

            if (isNew) {
                sosHibernateSession.save(dbItemIamFido2Registration);
            } else {
                throw new JocAuthenticationException("User " + fido2Registration.getAccountName() + " already exists");
            }

            sendConfirmationEmail(dbItemIamIdentityService.getIdentityServiceName(), fido2Registration.getEmail());

            storeAuditLog(fido2Registration.getAuditLog(), CategoryType.IDENTITY);
            Globals.commit(sosHibernateSession);

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postIdentityFido2client(byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            initLogging(API_CALL_IDENTITY_CLIENTS, body);
            IdentityServiceFilter identityServiceFilter = Globals.objectMapper.readValue(body, IdentityServiceFilter.class);
            JsonValidator.validateFailFast(body, IdentityServiceFilter.class);

            checkRequiredParameter("identityServiceName", identityServiceFilter.getIdentityServiceName());
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_IDENTITY_CLIENTS);
            IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
            IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
            filter.setIamIdentityServiceType(IdentityServiceTypes.FIDO_2);
            filter.setDisabled(false);
            filter.setIdentityServiceName(identityServiceFilter.getIdentityServiceName());
            List<DBItemIamIdentityService> listOfIdentityServices = iamIdentityServiceDBLayer.getIdentityServiceList(filter, 0);

            Fido2IdentityProvider identityProvider = new Fido2IdentityProvider();
            if (listOfIdentityServices.size() > 0) {

                identityProvider.setIdentityServiceName(listOfIdentityServices.get(0).getIdentityServiceName());
                com.sos.joc.model.security.properties.Properties properties = SOSAuthHelper.getIamProperties(listOfIdentityServices.get(0)
                        .getIdentityServiceName());

                if (properties != null) {
                    if (properties.getFido2() != null) {
                        identityProvider.setIamFido2Timeout(getProperty(properties.getFido2().getIamFido2Timeout()));
                        identityProvider.setIamFido2Transports(Fido2Transports.valueOf(getProperty(properties.getFido2().getIamFido2Transports()
                                .value(), "")));
                        identityProvider.setIamFido2UserVerification(Fido2Userverification.valueOf(getProperty(properties.getFido2()
                                .getIamFido2UserVerification().value(), "")));
                        identityProvider.setIamFido2Attestation(Fido2Attestation.valueOf(getProperty(properties.getFido2().getIamFido2Attestation()
                                .value(), "")));
                        identityProvider.setIdentityServiceName(identityServiceFilter.getIdentityServiceName());
                        if (properties.getFido2() != null) {
                            identityProvider.setIamCipherType(CipherTypes.valueOf(getProperty(properties.getFido2().getIamFido2CipherType().value(),
                                    "")));
                        }
                    }
                }
            }

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(identityProvider));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private String getChallenge(DBItemIamAccount dbItemAccount, String identityServiceName, String challengeToken) throws InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, JsonMappingException,
            JsonProcessingException, SOSHibernateException {

        com.sos.joc.model.security.properties.Properties properties = SOSAuthHelper.getIamProperties(identityServiceName);

        if (properties != null) {
            if (properties.getFido2() != null) {
                return Base64.getEncoder().encodeToString(SOSRSAUtil.encrypt(SOSAuthHelper.createAccessToken(), dbItemAccount.getPublicKey(),
                        properties.getFido2().getIamFido2CipherType()));
            }
        }
        throw new JocObjectNotExistException("No valid FIDO2 configuration for the identity service <" + identityServiceName + ">");
    }

    @Override
    public JOCDefaultResponse postFido2RequestAuthentication(byte[] body) {
        SOSHibernateSession sosHibernateSession = null;

        try {
            initLogging(API_CALL_REQUEST_AUTHENTICATION, body);
            Fido2RequestAuthentication fido2RequestAuthentication = Globals.objectMapper.readValue(body, Fido2RequestAuthentication.class);
            JsonValidator.validateFailFast(body, Fido2RequestAuthentication.class);

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_REQUEST_AUTHENTICATION);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();
            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, fido2RequestAuthentication
                    .getIdentityServiceName());

            if (!IdentityServiceTypes.FIDO_2.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                throw new JocObjectNotExistException("Only allowed for Identity Service type FIDO2 " + "<" + dbItemIamIdentityService
                        .getIdentityServiceType() + ">");
            }

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter filter = new IamAccountFilter();
            filter.setAccountName(fido2RequestAuthentication.getAccountName());
            filter.setIdentityServiceId(dbItemIamIdentityService.getId());

            DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(filter);
            if (dbItemIamAccount != null) {
                if (dbItemIamAccount.getPublicKey() == null) {
                    String challengeToken = SOSAuthHelper.createAccessToken();
                    String challenge = getChallenge(dbItemIamAccount, fido2RequestAuthentication.getIdentityServiceName(), challengeToken);
                    dbItemIamAccount.setChallenge(challengeToken);
                    fido2RequestAuthentication.setChallenge(challenge);
                    sosHibernateSession.update(dbItemIamAccount);
                } else {
                    throw new JocObjectNotExistException("Registration <" + fido2RequestAuthentication.getAccountName() + " in identity service "
                            + fido2RequestAuthentication.getIdentityServiceName() + "> is not approved");
                }

            } else {
                throw new JocObjectNotExistException("Couldn't find the account <" + fido2RequestAuthentication.getAccountName()
                        + " in identity service " + fido2RequestAuthentication.getIdentityServiceName() + ">");
            }

            Globals.commit(sosHibernateSession);

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(fido2RequestAuthentication));

        } catch (

        JocException e) {
            e.addErrorMetaInfo(getJocError());
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postFido2RegistrationDelete(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;

        try {
            initLogging(API_CALL_FIDO2_REGISTRATION_DELETE, body, accessToken);
            JsonValidator.validate(body, Fido2RegistrationsFilter.class);
            Fido2RegistrationsFilter fido2RegistrationsFilter = Globals.objectMapper.readValue(body, Fido2RegistrationsFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSSecurityDBConfiguration");
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, fido2RegistrationsFilter
                    .getIdentityServiceName());

            if (!IdentityServiceTypes.FIDO_2.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                throw new JocObjectNotExistException("Only allowed for Identity Service type FIDO2 " + "<" + dbItemIamIdentityService
                        .getIdentityServiceType() + ">");
            }

            IamFido2DBLayer iamFido2DBLayer = new IamFido2DBLayer(sosHibernateSession);
            IamFido2RegistrationFilter iamFido2RegistrationFilter = new IamFido2RegistrationFilter();
            iamFido2RegistrationFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            for (String accountName : fido2RegistrationsFilter.getAccountNames()) {
                iamFido2RegistrationFilter.setAccountName(accountName);
                int count = iamFido2DBLayer.delete(iamFido2RegistrationFilter);
                if (count == 0) {
                    throw new JocObjectNotExistException("Couldn't find the account <" + accountName + ">");
                }
            }
            Globals.commit(sosHibernateSession);

            storeAuditLog(fido2RegistrationsFilter.getAuditLog(), CategoryType.IDENTITY);

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postFido2Registrations(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            initLogging(API_CALL_FIDO2_REGISTRATIONS, body, accessToken);
            JsonValidator.validateFailFast(body, AccountListFilter.class);
            Fido2RegistrationListFilter accountFilter = Globals.objectMapper.readValue(body, Fido2RegistrationListFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_FIDO2_REGISTRATIONS);
            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, accountFilter
                    .getIdentityServiceName());

            if (!IdentityServiceTypes.FIDO_2.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                throw new JocObjectNotExistException("Only allowed for Identity Service type FIDO2 " + "<" + dbItemIamIdentityService
                        .getIdentityServiceType() + ">");
            }

            Fido2Registrations fido2Registrations = new Fido2Registrations();
            fido2Registrations.setFido2RegistrationItems(new ArrayList<Fido2Registration>());

            IamFido2DBLayer iamFido2DBLayer = new IamFido2DBLayer(sosHibernateSession);
            IamFido2RegistrationFilter iamFido2RegistrationFilter = new IamFido2RegistrationFilter();

            iamFido2RegistrationFilter.setDeferred(accountFilter.getDeferred());
            iamFido2RegistrationFilter.setConfirmed(accountFilter.getConfirmed());
            iamFido2RegistrationFilter.setAccountName(accountFilter.getAccountName());
            iamFido2RegistrationFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            List<DBItemIamFido2Registration> listOfFido2Registrations = iamFido2DBLayer.getIamRegistrationList(iamFido2RegistrationFilter, 0);
            for (DBItemIamFido2Registration dbItemIamFido2Registrations : listOfFido2Registrations) {
                Fido2Registration fido2Registration = new Fido2Registration();
                fido2Registration.setConfirmed(dbItemIamFido2Registrations.getConfirmed());
                fido2Registration.setDeferred(dbItemIamFido2Registrations.getDeferred());
                fido2Registration.setEmail(dbItemIamFido2Registrations.getEmail());
                fido2Registration.setIdentityServiceName(dbItemIamIdentityService.getIdentityServiceName());
                fido2Registration.setPublicKey(dbItemIamFido2Registrations.getPublicKey());
                fido2Registration.setAccountName(dbItemIamFido2Registrations.getAccountName());
                fido2Registrations.getFido2RegistrationItems().add(fido2Registration);
            }

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(fido2Registrations));
        } catch (

        JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private JOCDefaultResponse changeFlag(String accessToken, byte[] body, Boolean deferred, Boolean confirmed, String apiCall) {
        SOSHibernateSession sosHibernateSession = null;
        Fido2RegistrationFilter fido2RegistrationFilter = null;
        try {

            initLogging(apiCall, body, accessToken);
            JsonValidator.validate(body, Fido2RegistrationFilter.class);
            fido2RegistrationFilter = Globals.objectMapper.readValue(body, Fido2RegistrationFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(apiCall);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);
            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, fido2RegistrationFilter
                    .getIdentityServiceName());

            if (!IdentityServiceTypes.FIDO_2.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                throw new JocObjectNotExistException("Only allowed for Identity Service type FIDO2 " + "<" + dbItemIamIdentityService
                        .getIdentityServiceType() + ">");
            }

            IamFido2DBLayer iamFido2DBLayer = new IamFido2DBLayer(sosHibernateSession);
            IamFido2RegistrationFilter iamFido2RegistrationFilter = new IamFido2RegistrationFilter();

            iamFido2RegistrationFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            iamFido2RegistrationFilter.setAccountName(fido2RegistrationFilter.getAccountName());
            DBItemIamFido2Registration dbItemIamAccount = iamFido2DBLayer.getUniqueFido2Registration(iamFido2RegistrationFilter);
            if (dbItemIamAccount != null) {
                if (deferred != null) {
                    dbItemIamAccount.setDeferred(deferred);
                }
                if (confirmed != null) {
                    dbItemIamAccount.setConfirmed(confirmed);
                }
                sosHibernateSession.update(dbItemIamAccount);
            } else {
                JocError error = new JocError();
                error.setMessage("Unknown FIDO2 registration:" + fido2RegistrationFilter.getAccountName());
                throw new JocInfoException(error);
            }

            storeAuditLog(fido2RegistrationFilter.getAuditLog(), CategoryType.IDENTITY);
            Globals.commit(sosHibernateSession);

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());

        } finally {
            fido2RegistrationFilter = null;
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postFido2RegistrationApprove(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {
            Fido2RegistrationFilter fido2RegistrationFilter = null;

            JsonValidator.validate(body, Fido2RegistrationFilter.class);
            fido2RegistrationFilter = Globals.objectMapper.readValue(body, Fido2RegistrationFilter.class);
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_APPROVE);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, fido2RegistrationFilter
                    .getIdentityServiceName());

            if (!IdentityServiceTypes.FIDO_2.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                throw new JocObjectNotExistException("Only allowed for Identity Service type FIDO2 " + "<" + dbItemIamIdentityService
                        .getIdentityServiceType() + ">");
            }

            IamFido2DBLayer iamFido2DBLayer = new IamFido2DBLayer(sosHibernateSession);
            IamFido2RegistrationFilter iamFido2RegistrationFilter = new IamFido2RegistrationFilter();
            iamFido2RegistrationFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            iamFido2RegistrationFilter.setAccountName(fido2RegistrationFilter.getAccountName());
            DBItemIamFido2Registration dbItemIamFido2Registration = iamFido2DBLayer.getUniqueFido2Registration(iamFido2RegistrationFilter);
            if (dbItemIamFido2Registration == null) {
                throw new JocObjectNotExistException("Couldn't find the account <" + fido2RegistrationFilter.getAccountName() + ">");
            }
            iamFido2DBLayer.delete(iamFido2RegistrationFilter);

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);

            IamAccountFilter iamAccountFilter = new IamAccountFilter();
            iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            iamAccountFilter.setAccountName(fido2RegistrationFilter.getAccountName());
            DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(iamAccountFilter);
            if (dbItemIamAccount != null) {
                JocError jocError = new JocError();
                jocError.setMessage("Account already exists " + "<" + dbItemIamIdentityService.getIdentityServiceName() + "."
                        + fido2RegistrationFilter.getAccountName() + ">");
                throw new JocException(jocError);
            }

            dbItemIamAccount = new DBItemIamAccount();
            dbItemIamAccount.setAccountName(fido2RegistrationFilter.getAccountName());
            dbItemIamAccount.setIdentityServiceId(dbItemIamIdentityService.getId());
            dbItemIamAccount.setDisabled(false);
            dbItemIamAccount.setAccountPassword("********");
            dbItemIamAccount.setForcePasswordChange(false);
            dbItemIamAccount.setEmail(dbItemIamFido2Registration.getEmail());
            dbItemIamAccount.setPublicKey(dbItemIamFido2Registration.getPublicKey());
            sosHibernateSession.save(dbItemIamAccount);
            SOSAuthHelper.storeDefaultProfile(sosHibernateSession, fido2RegistrationFilter.getAccountName());

            if (fido2RegistrationFilter.getAuditLog() != null) {
                storeAuditLog(fido2RegistrationFilter.getAuditLog(), CategoryType.IDENTITY);
            }

            Globals.commit(sosHibernateSession);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
        return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
    }

    @Override
    public JOCDefaultResponse postFido2RegistrationDeferr(String accessToken, byte[] body) {
        return changeFlag(accessToken, body, true, null, API_CALL_REJECT);
    }

    @Override
    public JOCDefaultResponse postFido2RegistrationConfirm(String accessToken, byte[] body) {
        return changeFlag(accessToken, body, null, true, API_CALL_REJECT);
    }

    private void sendConfirmationEmail(String identityServiceName, String to) throws Exception {
        com.sos.joc.model.security.properties.Properties properties = SOSAuthHelper.getIamProperties(identityServiceName);

        Fido2ConfirmationMail fido2ConfirmationMail = new Fido2ConfirmationMail(properties.getFido2());
        fido2ConfirmationMail.sendMail(to);
    }

    private String getProperty(String value, String defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        } else {
            return value;
        }
    }

    private Integer getProperty(Integer value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}