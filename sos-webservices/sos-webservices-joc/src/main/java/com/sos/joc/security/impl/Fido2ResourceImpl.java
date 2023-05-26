package com.sos.joc.security.impl;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.security.Fido2ConfirmationMail;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamFido2Devices;
import com.sos.joc.db.authentication.DBItemIamFido2Registration;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;
import com.sos.joc.db.security.IamFido2DBLayer;
import com.sos.joc.db.security.IamFido2RegistrationFilter;
import com.sos.joc.db.security.IamIdentityServiceDBLayer;
import com.sos.joc.db.security.IamIdentityServiceFilter;
import com.sos.joc.exceptions.JocAuthenticationException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocInfoException;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.security.accounts.AccountListFilter;
import com.sos.joc.model.security.fido2.Fido2AddDevice;
import com.sos.joc.model.security.fido2.Fido2ConfirmationFilter;
import com.sos.joc.model.security.fido2.Fido2Registration;
import com.sos.joc.model.security.fido2.Fido2RegistrationFilter;
import com.sos.joc.model.security.fido2.Fido2RegistrationListFilter;
import com.sos.joc.model.security.fido2.Fido2RegistrationStartResponse;
import com.sos.joc.model.security.fido2.Fido2Registrations;
import com.sos.joc.model.security.fido2.Fido2RegistrationsFilter;
import com.sos.joc.model.security.fido2.Fido2RemoveDevices;
import com.sos.joc.model.security.fido2.Fido2RequestAuthentication;
import com.sos.joc.model.security.fido2.Fido2RequestAuthenticationResponse;
import com.sos.joc.model.security.identityservice.Fido2IdentityProvider;
import com.sos.joc.model.security.identityservice.IdentityServiceFilter;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;
import com.sos.joc.model.security.properties.fido2.Fido2Attestation;
import com.sos.joc.model.security.properties.fido2.Fido2Transports;
import com.sos.joc.model.security.properties.fido2.Fido2Userverification;
import com.sos.joc.security.resource.IFido2Resource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("iam")
public class Fido2ResourceImpl extends JOCResourceImpl implements IFido2Resource {

    private static final String CHALLENGE = "challenge";
    private static final Logger LOGGER = LoggerFactory.getLogger(Fido2ResourceImpl.class);
    private static final String API_CALL_FIDO2_REGISTRATIONS = "./iam/fido2registrations";
    private static final String API_CALL_FIDO2_REMOVE_DEVICES = "./iam/fido2/remove_devices";
    private static final String API_CALL_FIDO2_ADD_DEVICE = "./iam/fido2/add_device";
    private static final String API_CALL_FIDO2_REGISTRATION_READ = "./iam/fido2registration";
    private static final String API_CALL_FIDO2_REGISTRATION_STORE = "./iam/fido2registration/store";
    private static final String API_CALL_FIDO2_REGISTRATION_DELETE = "./iam/fido2registration/delete";
    private static final String API_CALL_APPROVE = "./iam/fido2registration/approve";
    private static final String API_CALL_CONFIRM = "./iam/fido2registration/confirm";
    private static final String API_CALL_DEFERR = "./iam/fido2registration/deferr";
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

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter iamAccountFilter = new IamAccountFilter();
            iamAccountFilter.setAccountName(fido2Registration.getAccountName());
            iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());

            DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(iamAccountFilter);
            if (dbItemIamAccount != null) {
                throw new JocBadRequestException("Account is already registered for " + "<" + dbItemIamIdentityService.getIdentityServiceName()
                        + ">");
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

            dbItemIamFido2Registration.setCompleted(true);
            dbItemIamFido2Registration.setPublicKey(fido2Registration.getPublicKey());
            dbItemIamFido2Registration.setCredentialId(fido2Registration.getCredentialId());
            dbItemIamFido2Registration.setToken(SOSAuthHelper.createAccessToken());
            dbItemIamFido2Registration.setCreated(new Date());

            if (!isNew) {
                String s = fido2Registration.getClientDataJSON();
                if (s != null) {
                    String clientDataJson = new String(Base64.getUrlDecoder().decode(s.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
                    JsonReader jsonReaderClientData = Json.createReader(new StringReader(clientDataJson));
                    JsonObject jsonClientData = jsonReaderClientData.readObject();
                    String challenge = jsonClientData.getString(CHALLENGE, "");
                    byte[] challengeDecoded = Base64.getDecoder().decode(challenge);
                    String challengeDecodedString = new String(challengeDecoded, StandardCharsets.UTF_8);
                     
                    if (!challengeDecodedString.equals(dbItemIamFido2Registration.getChallenge())) {
                        iamFido2DBLayer.delete(iamFido2RegistrationFilter);
                        throw new JocBadRequestException("Challenge does not match. The FIDO2 registration cannot be completed..");
                    }
                } else {
                    throw new JocBadRequestException("Challenge does not match. The FIDO2 registration cannot be completed..");
                }

                LOGGER.info("FIDO2 registration requested completed");
                sosHibernateSession.update(dbItemIamFido2Registration);
            } else {
                throw new JocAuthenticationException("FIDO2 registration request for " + fido2Registration.getAccountName() + " not startet");
            }

            sendConfirmationEmail(dbItemIamIdentityService.getIdentityServiceName(), dbItemIamFido2Registration, fido2Registration.getEmail());

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
    public JOCDefaultResponse postFido2AddDevice(byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            Fido2AddDevice fido2AddDevice = Globals.objectMapper.readValue(body, Fido2AddDevice.class);

            initLogging(API_CALL_FIDO2_ADD_DEVICE, null);
            JsonValidator.validateFailFast(body, Fido2AddDevice.class);

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_FIDO2_REGISTRATION_STORE);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, fido2AddDevice
                    .getIdentityServiceName());

            if (!IdentityServiceTypes.FIDO_2.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                throw new JocObjectNotExistException("Only allowed for Identity Service type FIDO2 " + "<" + dbItemIamIdentityService
                        .getIdentityServiceType() + ">");
            }

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter iamAccountFilter = new IamAccountFilter();
            iamAccountFilter.setAccountName(fido2AddDevice.getAccountName());
            iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());

            DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(iamAccountFilter);
            if (dbItemIamAccount == null) {
                throw new JocBadRequestException("Account does not exist in " + "<" + dbItemIamIdentityService.getIdentityServiceName() + ">");
            }

            DBItemIamFido2Devices dbItemIamFido2Devices = new DBItemIamFido2Devices();
            dbItemIamFido2Devices.setAccountId(dbItemIamAccount.getId());
            dbItemIamFido2Devices.setPublicKey(fido2AddDevice.getPublicKey());
            dbItemIamFido2Devices.setCredentialId(fido2AddDevice.getCredentialId());
            sosHibernateSession.save(dbItemIamFido2Devices);

            storeAuditLog(fido2AddDevice.getAuditLog(), CategoryType.IDENTITY);
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
    public JOCDefaultResponse postFido2RemoveDevices(byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            Fido2RemoveDevices fido2RemoveDevices = Globals.objectMapper.readValue(body, Fido2RemoveDevices.class);

            initLogging(API_CALL_FIDO2_REMOVE_DEVICES, null);
            JsonValidator.validateFailFast(body, Fido2RemoveDevices.class);

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_FIDO2_REGISTRATION_STORE);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, fido2RemoveDevices
                    .getIdentityServiceName());

            if (!IdentityServiceTypes.FIDO_2.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                throw new JocObjectNotExistException("Only allowed for Identity Service type FIDO2 " + "<" + dbItemIamIdentityService
                        .getIdentityServiceType() + ">");
            }

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter iamAccountFilter = new IamAccountFilter();
            iamAccountFilter.setAccountName(fido2RemoveDevices.getAccountName());
            iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());

            DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(iamAccountFilter);
            if (dbItemIamAccount == null) {
                throw new JocBadRequestException("Account does not exist in " + "<" + dbItemIamIdentityService.getIdentityServiceName() + ">");
            }

            iamAccountFilter = new IamAccountFilter();
            iamAccountFilter.setId(dbItemIamAccount.getId());
            iamAccountDBLayer.deleteDevices(iamAccountFilter);

            storeAuditLog(fido2RemoveDevices.getAuditLog(), CategoryType.IDENTITY);
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
    public JOCDefaultResponse postFido2RequestRegistrationStart(byte[] body) {
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

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter iamAccountFilter = new IamAccountFilter();
            iamAccountFilter.setAccountName(fido2Registration.getAccountName());
            iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());

            DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(iamAccountFilter);
            if (dbItemIamAccount != null) {
                throw new JocBadRequestException("Account is already registered for " + "<" + dbItemIamIdentityService.getIdentityServiceName()
                        + ">");
            }
            iamAccountFilter = new IamAccountFilter();
            iamAccountFilter.setEmail(fido2Registration.getEmail());
            iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());

            List<DBItemIamAccount> listOfAccounts = iamAccountDBLayer.getIamAccountList(iamAccountFilter, 0);
            if (listOfAccounts.size() > 0) {
                throw new JocBadRequestException("Email is already registered in" + "<" + dbItemIamIdentityService.getIdentityServiceName() + ">");
            }

            Fido2RegistrationStartResponse fido2RegistrationStartResponse = new Fido2RegistrationStartResponse();

            com.sos.joc.model.security.properties.Properties properties = SOSAuthHelper.getIamProperties(fido2Registration.getIdentityServiceName());
            properties.getFido2().setIamFido2EmailSettings(null);

            fido2RegistrationStartResponse.setFido2Properties(properties.getFido2());
            fido2RegistrationStartResponse.setChallenge(SOSAuthHelper.createAccessToken());

            IamFido2DBLayer iamFido2DBLayer = new IamFido2DBLayer(sosHibernateSession);

            IamFido2RegistrationFilter iamFido2RegistrationFilter = new IamFido2RegistrationFilter();
            iamFido2RegistrationFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            iamFido2RegistrationFilter.setEmail(fido2Registration.getEmail());
            iamFido2RegistrationFilter.setCompleted(true);
            List<DBItemIamFido2Registration> listOfRegistrations = iamFido2DBLayer.getIamRegistrationList(iamFido2RegistrationFilter, 0);
            if (listOfRegistrations.size() > 0) {
                throw new JocBadRequestException("There is already a registration request for the email <" + fido2Registration.getEmail() + "> in <"
                        + dbItemIamIdentityService.getIdentityServiceName() + ">");
            }

            iamFido2RegistrationFilter = new IamFido2RegistrationFilter();
            iamFido2RegistrationFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            iamFido2RegistrationFilter.setEmail(fido2Registration.getEmail());
            iamFido2RegistrationFilter.setCompleted(false);
            iamFido2DBLayer.delete(iamFido2RegistrationFilter);

            iamFido2RegistrationFilter = new IamFido2RegistrationFilter();
            iamFido2RegistrationFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            iamFido2RegistrationFilter.setAccountName(fido2Registration.getAccountName());
            iamFido2RegistrationFilter.setCompleted(false);
            iamFido2DBLayer.delete(iamFido2RegistrationFilter);

            DBItemIamFido2Registration dbItemIamFido2Registration = iamFido2DBLayer.getUniqueFido2Registration(iamFido2RegistrationFilter);
            boolean isNew = false;
            if (dbItemIamFido2Registration == null) {
                dbItemIamFido2Registration = new DBItemIamFido2Registration();
                isNew = true;
            }

            dbItemIamFido2Registration.setConfirmed(false);
            dbItemIamFido2Registration.setCompleted(false);
            dbItemIamFido2Registration.setDeferred(false);
            dbItemIamFido2Registration.setEmail(fido2Registration.getEmail());
            dbItemIamFido2Registration.setAccountName(fido2Registration.getAccountName());
            dbItemIamFido2Registration.setIdentityServiceId(dbItemIamIdentityService.getId());
            dbItemIamFido2Registration.setChallenge(fido2RegistrationStartResponse.getChallenge());
            dbItemIamFido2Registration.setCreated(new Date());

            if (isNew) {
                LOGGER.info("FIDO2 registration request start");
                sosHibernateSession.save(dbItemIamFido2Registration);
            } else {
                throw new JocAuthenticationException("Registration request already exists");
            }

            storeAuditLog(fido2Registration.getAuditLog(), CategoryType.IDENTITY);
            Globals.commit(sosHibernateSession);

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(fido2RegistrationStartResponse));

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
                        identityProvider.setIamFido2Transports(new ArrayList<Fido2Transports>());
                        identityProvider.setIamFido2Timeout(getProperty(properties.getFido2().getIamFido2Timeout()));
                        for (Fido2Transports fido2Transport : properties.getFido2().getIamFido2Transports()) {
                            identityProvider.getIamFido2Transports().add(fido2Transport);
                        }
                        identityProvider.setIamFido2UserVerification(Fido2Userverification.valueOf(getProperty(properties.getFido2()
                                .getIamFido2UserVerification().value(), "")));
                        identityProvider.setIamFido2Attestation(Fido2Attestation.valueOf(getProperty(properties.getFido2().getIamFido2Attestation()
                                .value(), "")));
                        identityProvider.setIdentityServiceName(identityServiceFilter.getIdentityServiceName());
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

            String challengeToken = SOSAuthHelper.createAccessToken();
            
            DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(filter);
            if (dbItemIamAccount != null) {
                List<DBItemIamFido2Devices> listOfFido2Devices = iamAccountDBLayer.getListOfFido2Devices(dbItemIamAccount.getId());
                if (listOfFido2Devices.size() > 0) {
                    dbItemIamAccount.setChallenge(challengeToken);
                    fido2RequestAuthentication.setChallenge(challengeToken);
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

            List<DBItemIamFido2Devices> listOfDevices = iamAccountDBLayer.getListOfFido2Devices(dbItemIamAccount.getId());

            Fido2RequestAuthenticationResponse fido2RequestAuthenticationResponse = new Fido2RequestAuthenticationResponse();
            fido2RequestAuthenticationResponse.setCredentialIds(new ArrayList<String>());
            for (DBItemIamFido2Devices device : listOfDevices) {
                fido2RequestAuthenticationResponse.getCredentialIds().add(device.getCredentialId());
            }
            fido2RequestAuthenticationResponse.setChallenge(challengeToken);
            com.sos.joc.model.security.properties.Properties properties = SOSAuthHelper.getIamProperties(fido2RequestAuthentication
                    .getIdentityServiceName());
            properties.getFido2().setIamFido2EmailSettings(null);

            fido2RequestAuthenticationResponse.setFido2Properties(properties.getFido2());
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(fido2RequestAuthenticationResponse));

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

            iamFido2RegistrationFilter.setCompleted(true);
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

    @Override
    public JOCDefaultResponse postFido2RegistrationApprove(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {
            Fido2RegistrationsFilter fido2RegistrationsFilter = null;
            initLogging(API_CALL_APPROVE, body, accessToken);
            JsonValidator.validate(body, Fido2RegistrationsFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            fido2RegistrationsFilter = Globals.objectMapper.readValue(body, Fido2RegistrationsFilter.class);
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_APPROVE);
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
                DBItemIamFido2Registration dbItemIamFido2Registration = iamFido2DBLayer.getUniqueFido2Registration(iamFido2RegistrationFilter);
                if (dbItemIamFido2Registration == null) {
                    throw new JocObjectNotExistException("Couldn't find the account <" + accountName + ">");
                }
                iamFido2DBLayer.delete(iamFido2RegistrationFilter);

                IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);

                IamAccountFilter iamAccountFilter = new IamAccountFilter();
                iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
                iamAccountFilter.setAccountName(accountName);
                DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(iamAccountFilter);
                if (dbItemIamAccount != null) {
                    JocError jocError = new JocError();
                    jocError.setMessage("Account already exists " + "<" + dbItemIamIdentityService.getIdentityServiceName() + "." + accountName
                            + ">");
                    throw new JocException(jocError);
                }

                dbItemIamAccount = new DBItemIamAccount();
                dbItemIamAccount.setAccountName(accountName);
                dbItemIamAccount.setIdentityServiceId(dbItemIamIdentityService.getId());
                dbItemIamAccount.setDisabled(false);
                dbItemIamAccount.setAccountPassword("********");
                dbItemIamAccount.setForcePasswordChange(false);
                dbItemIamAccount.setEmail(dbItemIamFido2Registration.getEmail());
                DBItemIamFido2Devices dbItemIamFido2Devices = new DBItemIamFido2Devices();

                sosHibernateSession.save(dbItemIamAccount);
                dbItemIamFido2Devices.setAccountId(dbItemIamAccount.getId());
                dbItemIamFido2Devices.setPublicKey(dbItemIamFido2Registration.getPublicKey());
                dbItemIamFido2Devices.setCredentialId(dbItemIamFido2Registration.getCredentialId());
                sosHibernateSession.save(dbItemIamFido2Devices);

                LOGGER.info("FIDO2 registration approved");
                SOSAuthHelper.storeDefaultProfile(sosHibernateSession, accountName);

            }
            if (fido2RegistrationsFilter.getAuditLog() != null) {
                storeAuditLog(fido2RegistrationsFilter.getAuditLog(), CategoryType.IDENTITY);
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
        SOSHibernateSession sosHibernateSession = null;
        Fido2RegistrationsFilter fido2RegistrationsFilter = null;
        try {

            initLogging(API_CALL_DEFERR, body, accessToken);
            JsonValidator.validate(body, Fido2RegistrationsFilter.class);
            fido2RegistrationsFilter = Globals.objectMapper.readValue(body, Fido2RegistrationsFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_DEFERR);
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
                DBItemIamFido2Registration dbItemIamFido2Registration = iamFido2DBLayer.getUniqueFido2Registration(iamFido2RegistrationFilter);
                if (dbItemIamFido2Registration != null) {
                    LOGGER.info("FIDO2 registration deferred");
                    dbItemIamFido2Registration.setDeferred(true);
                    sosHibernateSession.update(dbItemIamFido2Registration);
                } else {
                    JocError error = new JocError();
                    error.setMessage("Unknown FIDO2 registration:" + accountName);
                    throw new JocInfoException(error);
                }
            }
            storeAuditLog(fido2RegistrationsFilter.getAuditLog(), CategoryType.IDENTITY);
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
            fido2RegistrationsFilter = null;
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postFido2RegistrationConfirm(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        Fido2ConfirmationFilter fido2ConfirmationFilter = null;
        try {

            initLogging(API_CALL_CONFIRM, body);
            fido2ConfirmationFilter = Globals.objectMapper.readValue(body, Fido2ConfirmationFilter.class);
            JsonValidator.validateFailFast(body, Fido2ConfirmationFilter.class);

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_CONFIRM);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            IamFido2DBLayer iamFido2DBLayer = new IamFido2DBLayer(sosHibernateSession);
            IamFido2RegistrationFilter iamFido2RegistrationFilter = new IamFido2RegistrationFilter();

            iamFido2RegistrationFilter.setToken(fido2ConfirmationFilter.getToken());
            DBItemIamFido2Registration dbItemIamFido2Registration = iamFido2DBLayer.getFido2Registration(iamFido2RegistrationFilter);
            if (dbItemIamFido2Registration != null) {
                LOGGER.info("FIDO2 registration confirmed");
                dbItemIamFido2Registration.setConfirmed(true);
                sosHibernateSession.update(dbItemIamFido2Registration);
            } else {
                JocError error = new JocError();
                error.setMessage("Unknown FIDO2 registration token");
                throw new JocInfoException(error);
            }

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
            fido2ConfirmationFilter = null;
            Globals.disconnect(sosHibernateSession);
        }
    }

    private void sendConfirmationEmail(String identityServiceName, DBItemIamFido2Registration dbItemIamFido2Registration, String to)
            throws Exception {
        com.sos.joc.model.security.properties.Properties properties = SOSAuthHelper.getIamProperties(identityServiceName);

        Fido2ConfirmationMail fido2ConfirmationMail = new Fido2ConfirmationMail(properties.getFido2());
        fido2ConfirmationMail.sendMail(dbItemIamFido2Registration, to, identityServiceName);
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