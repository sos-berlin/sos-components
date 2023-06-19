package com.sos.joc.security.impl;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.hibernate.exception.LockAcquisitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.fido.classes.SOSFidoClientData;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.security.FidoConfirmationMail;
import com.sos.joc.classes.security.SOSSecurityUtil;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamFido2Devices;
import com.sos.joc.db.authentication.DBItemIamFido2Registration;
import com.sos.joc.db.authentication.DBItemIamFido2Requests;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;
import com.sos.joc.db.security.IamFidoDBLayer;
import com.sos.joc.db.security.IamFidoDevicesDBLayer;
import com.sos.joc.db.security.IamFidoDevicesFilter;
import com.sos.joc.db.security.IamFidoRegistrationFilter;
import com.sos.joc.db.security.IamIdentityServiceDBLayer;
import com.sos.joc.db.security.IamIdentityServiceFilter;
import com.sos.joc.exceptions.JocAuthenticationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocInfoException;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.configuration.Configuration;
import com.sos.joc.model.configuration.Configuration200;
import com.sos.joc.model.configuration.ConfigurationType;
import com.sos.joc.model.security.accounts.AccountListFilter;
import com.sos.joc.model.security.fido.FidoAddDevice;
import com.sos.joc.model.security.fido.FidoConfirmationFilter;
import com.sos.joc.model.security.fido.FidoRegistration;
import com.sos.joc.model.security.fido.FidoRegistrationAccount;
import com.sos.joc.model.security.fido.FidoRegistrationFilter;
import com.sos.joc.model.security.fido.FidoRegistrationListFilter;
import com.sos.joc.model.security.fido.FidoRegistrationStartResponse;
import com.sos.joc.model.security.fido.FidoRegistrations;
import com.sos.joc.model.security.fido.FidoRegistrationsFilter;
import com.sos.joc.model.security.fido.FidoRemoveDevices;
import com.sos.joc.model.security.fido.FidoRequestAuthentication;
import com.sos.joc.model.security.fido.FidoRequestAuthenticationResponse;
import com.sos.joc.model.security.identityservice.FidoIdentityProvider;
import com.sos.joc.model.security.identityservice.IdentityServiceFilter;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;
import com.sos.joc.model.security.properties.fido.FidoResidentKey;
import com.sos.joc.model.security.properties.fido.FidoTransports;
import com.sos.joc.model.security.properties.fido.FidoUserverification;
import com.sos.joc.security.resource.IFidoResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("iam")
public class FidoResourceImpl extends JOCResourceImpl implements IFidoResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(FidoResourceImpl.class);
    private static final String API_CALL_FIDO_CONFIGURATION = "./iam/fidoconfiguration";
    private static final String API_CALL_FIDO_REGISTRATIONS = "./iam/fidoregistrations";
    private static final String API_CALL_FIDO_REMOVE_DEVICES = "./iam/fido/remove_devices";
    private static final String API_CALL_FIDO_ADD_DEVICE = "./iam/fido/add_device";
    private static final String API_CALL_FIDO_REGISTRATION_READ = "./iam/fidoregistration";
    private static final String API_CALL_FIDO_REGISTRATION_STORE = "./iam/fidoregistration/store";
    private static final String API_CALL_FIDO_REGISTRATION_DELETE = "./iam/fidoregistration/delete";
    private static final String API_CALL_APPROVE = "./iam/fidoregistration/approve";
    private static final String API_CALL_CONFIRM = "./iam/fidoregistration/confirm";
    private static final String API_CALL_DEFERR = "./iam/fidoregistration/deferr";
    private static final String API_CALL_IDENTITY_CLIENTS = "./iam/fidoregistration/identityclients";
    private static final String API_CALL_REQUEST_AUTHENTICATION = "./iam/fido/request_authentication";

    @Override
    public JOCDefaultResponse postFidoRegistrationRead(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            initLogging(API_CALL_FIDO_REGISTRATION_READ, body, accessToken);
            JsonValidator.validateFailFast(body, FidoRegistrationFilter.class);
            FidoRegistrationFilter fidoRegistrationFilter = Globals.objectMapper.readValue(body, FidoRegistrationFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            FidoRegistration fidoRegistration = new FidoRegistration();

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_FIDO_REGISTRATION_READ);

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, fidoRegistrationFilter
                    .getIdentityServiceName());

            if (!IdentityServiceTypes.FIDO.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                throw new JocAuthenticationException("Only allowed for Identity Service type FIDO2 " + "<" + dbItemIamIdentityService
                        .getIdentityServiceType() + ">");
            }

            IamFidoDBLayer iamFidoDBLayer = new IamFidoDBLayer(sosHibernateSession);
            IamFidoRegistrationFilter filter = new IamFidoRegistrationFilter();
            filter.setIdentityServiceId(dbItemIamIdentityService.getId());
            filter.setAccountName(fidoRegistrationFilter.getAccountName());

            DBItemIamFido2Registration dbItemIamFido2Registration = iamFidoDBLayer.getUniqueFidoRegistration(filter);
            if (dbItemIamFido2Registration != null) {
                fidoRegistration.setAccountName(dbItemIamFido2Registration.getAccountName());
                fidoRegistration.setIdentityServiceName(dbItemIamIdentityService.getIdentityServiceName());
                fidoRegistration.setConfirmed(dbItemIamFido2Registration.getConfirmed());
                fidoRegistration.setDeferred(dbItemIamFido2Registration.getDeferred());
                fidoRegistration.setEmail(dbItemIamFido2Registration.getEmail());
                fidoRegistration.setPublicKey(dbItemIamFido2Registration.getPublicKey());
                fidoRegistration.setOrigin(dbItemIamFido2Registration.getOrigin());
            } else {
                throw new JocObjectNotExistException("Couldn't find the registration <" + fidoRegistrationFilter.getAccountName()
                        + " in identity service " + fidoRegistrationFilter.getIdentityServiceName() + ">");
            }
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(fidoRegistration));

        } catch (JocAuthenticationException e) {
            getJocError().setLogAsInfo(true);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
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
    public JOCDefaultResponse postFidoRequestRegistration(byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            initLogging(API_CALL_FIDO_REGISTRATION_STORE, null);
            JsonValidator.validateFailFast(body, FidoRegistration.class);
            FidoRegistration fidoRegistration = Globals.objectMapper.readValue(body, FidoRegistration.class);

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_FIDO_REGISTRATION_STORE);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, fidoRegistration
                    .getIdentityServiceName());

            if (!IdentityServiceTypes.FIDO.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                throw new JocObjectNotExistException("Only allowed for Identity Service type FIDO2 " + "<" + dbItemIamIdentityService
                        .getIdentityServiceType() + ">");
            }

            SOSFidoClientData sosFidoClientData = new SOSFidoClientData(fidoRegistration.getClientDataJSON());

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter iamAccountFilter = new IamAccountFilter();
            iamAccountFilter.setAccountName(fidoRegistration.getAccountName());
            iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());

            DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(iamAccountFilter);
            if (dbItemIamAccount != null) {

                IamFidoDevicesDBLayer iamFidoDevicesDBLayer = new IamFidoDevicesDBLayer(sosHibernateSession);
                IamFidoDevicesFilter filter = new IamFidoDevicesFilter();
                filter.setAccountId(dbItemIamAccount.getId());
                filter.setOrigin(sosFidoClientData.getOrigin());
                List<DBItemIamFido2Devices> listOfDevices = iamFidoDevicesDBLayer.getListOfFidoDevices(filter);

                if (listOfDevices.size() > 0) {
                    throw new JocAuthenticationException("Account is already registered for " + "<" + dbItemIamIdentityService
                            .getIdentityServiceName() + ">");
                }
            }

            IamFidoDBLayer iamFidoDBLayer = new IamFidoDBLayer(sosHibernateSession);

            IamFidoRegistrationFilter iamFidoRegistrationFilter = new IamFidoRegistrationFilter();
            iamFidoRegistrationFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            iamFidoRegistrationFilter.setAccountName(fidoRegistration.getAccountName());
            iamFidoRegistrationFilter.setOrigin(sosFidoClientData.getOrigin());
            DBItemIamFido2Registration dbItemIamFido2Registration = iamFidoDBLayer.getUniqueFidoRegistration(iamFidoRegistrationFilter);
            boolean isNew = false;
            if (dbItemIamFido2Registration == null) {
                dbItemIamFido2Registration = new DBItemIamFido2Registration();
                isNew = true;
            }

            dbItemIamFido2Registration.setCompleted(true);
            dbItemIamFido2Registration.setPublicKey(fidoRegistration.getPublicKey());

            dbItemIamFido2Registration.setAlgorithm(SOSSecurityUtil.getAlgFromJwk(fidoRegistration.getJwk()));
            dbItemIamFido2Registration.setCredentialId(fidoRegistration.getCredentialId());
            dbItemIamFido2Registration.setToken(SOSAuthHelper.createAccessToken());
            dbItemIamFido2Registration.setCreated(new Date());

            if (!isNew) {
                String s = fidoRegistration.getClientDataJSON();
                if (s != null) {
                    dbItemIamFido2Registration.setOrigin(sosFidoClientData.getOrigin());
                    byte[] challengeDecoded = Base64.getDecoder().decode(sosFidoClientData.getChallenge());
                    String challengeDecodedString = new String(challengeDecoded, StandardCharsets.UTF_8);

                    if (!challengeDecodedString.equals(dbItemIamFido2Registration.getChallenge())) {
                        iamFidoDBLayer.delete(iamFidoRegistrationFilter);
                        throw new JocAuthenticationException("Challenge does not match. The FIDO registration cannot be completed..");
                    }
                } else {
                    throw new JocAuthenticationException("Challenge does not match. The FIDO registration cannot be completed..");
                }

                LOGGER.info("FIDO registration requested completed");
                sosHibernateSession.update(dbItemIamFido2Registration);
            } else {
                throw new JocAuthenticationException("FIDO2 registration request for " + fidoRegistration.getAccountName() + " not startet");
            }

            sendRegistrationMail(dbItemIamIdentityService.getIdentityServiceName(), dbItemIamFido2Registration, fidoRegistration.getEmail());

            storeAuditLog(fidoRegistration.getAuditLog(), CategoryType.IDENTITY);
            Globals.commit(sosHibernateSession);

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocAuthenticationException e) {
            getJocError().setLogAsInfo(true);
            e.addErrorMetaInfo(getJocError());
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.rollback(sosHibernateSession);
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postFidoAddDevice(byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            initLogging(API_CALL_FIDO_ADD_DEVICE, null);
            JsonValidator.validateFailFast(body, FidoAddDevice.class);
            FidoAddDevice fidoAddDevice = Globals.objectMapper.readValue(body, FidoAddDevice.class);

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_FIDO_REGISTRATION_STORE);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, fidoAddDevice
                    .getIdentityServiceName());

            if (!IdentityServiceTypes.FIDO.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                throw new JocAuthenticationException("Only allowed for Identity Service type FIDO2 " + "<" + dbItemIamIdentityService
                        .getIdentityServiceType() + ">");
            }

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter iamAccountFilter = new IamAccountFilter();
            iamAccountFilter.setAccountName(fidoAddDevice.getAccountName());
            iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());

            DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(iamAccountFilter);
            if (dbItemIamAccount == null) {
                throw new JocObjectNotExistException("Account does not exist in " + "<" + dbItemIamIdentityService.getIdentityServiceName() + ">");
            }

            DBItemIamFido2Devices dbItemIamFido2Devices = new DBItemIamFido2Devices();
            dbItemIamFido2Devices.setAccountId(dbItemIamAccount.getId());
            dbItemIamFido2Devices.setPublicKey(fidoAddDevice.getPublicKey());
            dbItemIamFido2Devices.setAlgorithm(SOSSecurityUtil.getAlgFromJwk(fidoAddDevice.getJwk()));
            dbItemIamFido2Devices.setCredentialId(fidoAddDevice.getCredentialId());
            dbItemIamFido2Devices.setOrigin(fidoAddDevice.getOrigin());
            dbItemIamFido2Devices.setIdentityServiceId(dbItemIamIdentityService.getId());
            sosHibernateSession.save(dbItemIamFido2Devices);

            storeAuditLog(fidoAddDevice.getAuditLog(), CategoryType.IDENTITY);
            Globals.commit(sosHibernateSession);

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocAuthenticationException e) {
            getJocError().setLogAsInfo(true);
            e.addErrorMetaInfo(getJocError());
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e);
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
    public JOCDefaultResponse postFidoRemoveDevices(byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            initLogging(API_CALL_FIDO_REMOVE_DEVICES, null);
            JsonValidator.validateFailFast(body, FidoRemoveDevices.class);
            FidoRemoveDevices fidoRemoveDevices = Globals.objectMapper.readValue(body, FidoRemoveDevices.class);

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_FIDO_REGISTRATION_STORE);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, fidoRemoveDevices
                    .getIdentityServiceName());

            if (!IdentityServiceTypes.FIDO.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                throw new JocAuthenticationException("Only allowed for Identity Service type FIDO " + "<" + dbItemIamIdentityService
                        .getIdentityServiceType() + ">");
            }

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter iamAccountFilter = new IamAccountFilter();
            iamAccountFilter.setAccountName(fidoRemoveDevices.getAccountName());
            iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());

            DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(iamAccountFilter);
            if (dbItemIamAccount == null) {
                throw new JocObjectNotExistException("Account does not exist in " + "<" + dbItemIamIdentityService.getIdentityServiceName() + ">");
            }

            IamFidoDevicesDBLayer iamFidoDevicesDBLayer = new IamFidoDevicesDBLayer(sosHibernateSession);
            iamFidoDevicesDBLayer.delete(iamAccountFilter.getId());

            storeAuditLog(fidoRemoveDevices.getAuditLog(), CategoryType.IDENTITY);
            Globals.commit(sosHibernateSession);

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocAuthenticationException e) {
            getJocError().setLogAsInfo(true);
            e.addErrorMetaInfo(getJocError());
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e);
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
    public JOCDefaultResponse postFidoRequestRegistrationStart(byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        FidoRegistrationStartResponse fido2RegistrationStartResponse = new FidoRegistrationStartResponse();

        try {

            initLogging(API_CALL_FIDO_REGISTRATION_STORE, null);
            JsonValidator.validateFailFast(body, FidoRegistration.class);
            FidoRegistration fidoRegistration = Globals.objectMapper.readValue(body, FidoRegistration.class);

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_FIDO_REGISTRATION_STORE);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, fidoRegistration
                    .getIdentityServiceName());

            if (!IdentityServiceTypes.FIDO.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                throw new JocAuthenticationException("Only allowed for Identity Service type FIDO " + "<" + dbItemIamIdentityService
                        .getIdentityServiceType() + ">");
            }

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter iamAccountFilter = new IamAccountFilter();
            iamAccountFilter.setAccountName(fidoRegistration.getAccountName());
            iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());

            DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(iamAccountFilter);

            if (dbItemIamAccount != null) {
                IamFidoDevicesDBLayer iamFido2DevicesDBLayer = new IamFidoDevicesDBLayer(sosHibernateSession);
                IamFidoDevicesFilter filter = new IamFidoDevicesFilter();
                filter.setAccountId(dbItemIamAccount.getId());
                filter.setOrigin(fidoRegistration.getOrigin());
                List<DBItemIamFido2Devices> listOfDevices = iamFido2DevicesDBLayer.getListOfFidoDevices(filter);

                if (listOfDevices.size() > 0) {
                    throw new JocAuthenticationException("Account is already registered for " + "<" + dbItemIamIdentityService
                            .getIdentityServiceName() + "/" + fidoRegistration.getOrigin() + ">");
                }
            }
            iamAccountFilter = new IamAccountFilter();
            iamAccountFilter.setEmail(fidoRegistration.getEmail());
            iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());

            List<DBItemIamAccount> listOfAccounts = iamAccountDBLayer.getIamAccountList(iamAccountFilter, 0);
            if (listOfAccounts.size() > 0) {
                dbItemIamAccount = listOfAccounts.get(0);

                IamFidoDevicesDBLayer iamFido2DevicesDBLayer = new IamFidoDevicesDBLayer(sosHibernateSession);
                IamFidoDevicesFilter filter = new IamFidoDevicesFilter();
                filter.setAccountId(dbItemIamAccount.getId());
                filter.setOrigin(fidoRegistration.getOrigin());
                List<DBItemIamFido2Devices> listOfDevices = iamFido2DevicesDBLayer.getListOfFidoDevices(filter);

                if (listOfDevices.size() > 0) {
                    throw new JocAuthenticationException("Email is already registered in" + "<" + dbItemIamIdentityService.getIdentityServiceName()
                            + ">");
                }
            }

            com.sos.joc.model.security.properties.Properties properties = SOSAuthHelper.getIamProperties(fidoRegistration.getIdentityServiceName());
            if (properties.getFido() == null) {
                throw new JocAuthenticationException("FIDO Identity Service is not configured");
            }
            properties.getFido().setIamFidoEmailSettings(null);

            fido2RegistrationStartResponse.setFidoProperties(properties.getFido());
            fido2RegistrationStartResponse.setChallenge(SOSAuthHelper.createAccessToken());

            IamFidoDBLayer iamFidoDBLayer = new IamFidoDBLayer(sosHibernateSession);

            IamFidoRegistrationFilter iamFidoRegistrationFilter = new IamFidoRegistrationFilter();
            iamFidoRegistrationFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            iamFidoRegistrationFilter.setEmail(fidoRegistration.getEmail());
            iamFidoRegistrationFilter.setCompleted(true);
            iamFidoRegistrationFilter.setOrigin(fidoRegistration.getOrigin());

            List<DBItemIamFido2Registration> listOfRegistrations = iamFidoDBLayer.getIamRegistrationList(iamFidoRegistrationFilter, 0);
            if (listOfRegistrations.size() > 0) {
                throw new JocAuthenticationException("There is already a registration request for the email <" + fidoRegistration.getEmail()
                        + "> in <" + dbItemIamIdentityService.getIdentityServiceName() + "/" + fidoRegistration.getOrigin() + ">");
            }

            iamFidoRegistrationFilter = new IamFidoRegistrationFilter();
            iamFidoRegistrationFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            iamFidoRegistrationFilter.setCompleted(true);
            iamFidoRegistrationFilter.setAccountName(fidoRegistration.getAccountName());
            iamFidoRegistrationFilter.setOrigin(fidoRegistration.getOrigin());

            listOfRegistrations = iamFidoDBLayer.getIamRegistrationList(iamFidoRegistrationFilter, 0);
            if (listOfRegistrations.size() > 0) {
                throw new JocAuthenticationException("There is already a registration request for the account <" + fidoRegistration.getAccountName()
                        + "> in <" + dbItemIamIdentityService.getIdentityServiceName() + "/" + fidoRegistration.getOrigin() + ">");
            }

            iamFidoRegistrationFilter = new IamFidoRegistrationFilter();
            iamFidoRegistrationFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            iamFidoRegistrationFilter.setEmail(fidoRegistration.getEmail());
            iamFidoRegistrationFilter.setCompleted(false);
            iamFidoRegistrationFilter.setOrigin(fidoRegistration.getOrigin());
            try {
                iamFidoDBLayer.delete(iamFidoRegistrationFilter);
            } catch (LockAcquisitionException e) {
                Globals.rollback(sosHibernateSession);
            }

            iamFidoRegistrationFilter = new IamFidoRegistrationFilter();
            iamFidoRegistrationFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            iamFidoRegistrationFilter.setAccountName(fidoRegistration.getAccountName());
            iamFidoRegistrationFilter.setOrigin(fidoRegistration.getOrigin());
            iamFidoRegistrationFilter.setCompleted(false);
            try {
                iamFidoDBLayer.delete(iamFidoRegistrationFilter);
            } catch (LockAcquisitionException e) {
                Globals.rollback(sosHibernateSession);
            }

            DBItemIamFido2Registration dbItemIamFido2Registration = iamFidoDBLayer.getUniqueFidoRegistration(iamFidoRegistrationFilter);
            boolean isNew = false;
            if (dbItemIamFido2Registration == null) {
                dbItemIamFido2Registration = new DBItemIamFido2Registration();
                isNew = true;
            }

            if (isNew) {
                LOGGER.info("FIDO2 registration request start");
                dbItemIamFido2Registration.setConfirmed(false);
                dbItemIamFido2Registration.setCompleted(false);
                dbItemIamFido2Registration.setDeferred(false);
                dbItemIamFido2Registration.setEmail(fidoRegistration.getEmail());
                dbItemIamFido2Registration.setAccountName(fidoRegistration.getAccountName());
                dbItemIamFido2Registration.setIdentityServiceId(dbItemIamIdentityService.getId());
                dbItemIamFido2Registration.setChallenge(fido2RegistrationStartResponse.getChallenge());
                dbItemIamFido2Registration.setOrigin(fidoRegistration.getOrigin());
                dbItemIamFido2Registration.setCreated(new Date());
                sosHibernateSession.save(dbItemIamFido2Registration);
            } else {
                throw new JocAuthenticationException("Registration request already exists");
            }

            storeAuditLog(fidoRegistration.getAuditLog(), CategoryType.IDENTITY);
            Globals.commit(sosHibernateSession);

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(fido2RegistrationStartResponse));

        } catch (JocAuthenticationException e) {
            e.getError().setLogAsInfo(true);
            e.addErrorMetaInfo(getJocError());
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.rollback(sosHibernateSession);
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postIdentityFidoclient(byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            initLogging(API_CALL_IDENTITY_CLIENTS, body);
            JsonValidator.validateFailFast(body, IdentityServiceFilter.class);
            IdentityServiceFilter identityServiceFilter = Globals.objectMapper.readValue(body, IdentityServiceFilter.class);

            checkRequiredParameter("identityServiceName", identityServiceFilter.getIdentityServiceName());
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_IDENTITY_CLIENTS);
            IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
            IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
            filter.setIamIdentityServiceType(IdentityServiceTypes.FIDO);
            filter.setDisabled(false);
            filter.setIdentityServiceName(identityServiceFilter.getIdentityServiceName());
            List<DBItemIamIdentityService> listOfIdentityServices = iamIdentityServiceDBLayer.getIdentityServiceList(filter, 0);

            FidoIdentityProvider identityProvider = new FidoIdentityProvider();
            if (listOfIdentityServices.size() > 0) {

                identityProvider.setIdentityServiceName(listOfIdentityServices.get(0).getIdentityServiceName());
                com.sos.joc.model.security.properties.Properties properties = SOSAuthHelper.getIamProperties(listOfIdentityServices.get(0)
                        .getIdentityServiceName());

                if (properties != null) {
                    if (properties.getFido() != null) {
                        identityProvider.setIamFidoTransports(new ArrayList<FidoTransports>());
                        identityProvider.setIamFidoTimeout(getProperty(properties.getFido().getIamFidoTimeout()));
                        for (FidoTransports fidoTransport : properties.getFido().getIamFidoTransports()) {
                            identityProvider.getIamFidoTransports().add(fidoTransport);
                        }
                        identityProvider.setIamFidoUserVerification(FidoUserverification.valueOf(getProperty(properties.getFido()
                                .getIamFidoUserVerification().value(), "")));
                        identityProvider.setIamFidoResidentKey(FidoResidentKey.valueOf(getProperty(properties.getFido().getIamFidoResidentKey()
                                .value(), "")));
                        identityProvider.setIdentityServiceName(identityServiceFilter.getIdentityServiceName());
                        identityProvider.setIamFidoRequireAccount(properties.getFido().getIamFidoRequireAccount());
                        identityProvider.setIamFido2Attachment(properties.getFido().getIamFidoAttachment());
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
    public JOCDefaultResponse postFidoRequestAuthentication(byte[] body) {
        SOSHibernateSession sosHibernateSession = null;

        try {
            initLogging(API_CALL_REQUEST_AUTHENTICATION, body);
            JsonValidator.validateFailFast(body, FidoRequestAuthentication.class);
            FidoRequestAuthentication fidoRequestAuthentication = Globals.objectMapper.readValue(body, FidoRequestAuthentication.class);

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_REQUEST_AUTHENTICATION);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();
            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, fidoRequestAuthentication
                    .getIdentityServiceName());

            if (!IdentityServiceTypes.FIDO.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                throw new JocAuthenticationException("Only allowed for Identity Service type FIDO2 " + "<" + dbItemIamIdentityService
                        .getIdentityServiceType() + ">");
            }

            FidoRequestAuthenticationResponse fidoRequestAuthenticationResponse = new FidoRequestAuthenticationResponse();
            fidoRequestAuthenticationResponse.setChallenge(SOSAuthHelper.createAccessToken());

            IamFidoDevicesDBLayer iamFidoDevicesDBLayer = new IamFidoDevicesDBLayer(sosHibernateSession);
            IamFidoDevicesFilter iamFidoDevicesFilter = new IamFidoDevicesFilter();
            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter filter = new IamAccountFilter();
            filter.setAccountName(fidoRequestAuthentication.getAccountName());
            filter.setIdentityServiceId(dbItemIamIdentityService.getId());

            DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(filter);
            if (dbItemIamAccount == null && filter.getAccountName() != null) {
                throw new JocObjectNotExistException("Couldn't find the account <" + fidoRequestAuthentication.getAccountName()
                        + " in identity service " + fidoRequestAuthentication.getIdentityServiceName() + ">");
            }

            if (dbItemIamAccount != null) {
                iamFidoDevicesFilter.setAccountId(dbItemIamAccount.getId());
            }

            iamFidoDevicesFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            iamFidoDevicesFilter.setOrigin(fidoRequestAuthentication.getOrigin());
            List<DBItemIamFido2Devices> listOfFido2Devices = iamFidoDevicesDBLayer.getListOfFidoDevices(iamFidoDevicesFilter);
            if (listOfFido2Devices.size() == 0) {
                throw new JocAuthenticationException("Registration <" + fidoRequestAuthentication.getAccountName() + " in identity service "
                        + fidoRequestAuthentication.getIdentityServiceName() + "/" + fidoRequestAuthentication.getOrigin() + "> is not approved");
            }

            List<DBItemIamFido2Devices> listOfDevices = iamFidoDevicesDBLayer.getListOfFidoDevices(iamFidoDevicesFilter);

            fidoRequestAuthenticationResponse.setCredentialIds(new ArrayList<String>());
            for (DBItemIamFido2Devices device : listOfDevices) {
                fidoRequestAuthenticationResponse.getCredentialIds().add(device.getCredentialId());
            }

            DBItemIamFido2Requests dbItemIamFido2Requests = new DBItemIamFido2Requests();
            dbItemIamFido2Requests.setChallenge(fidoRequestAuthenticationResponse.getChallenge());
            dbItemIamFido2Requests.setCreated(new Date());
            dbItemIamFido2Requests.setIdentityServiceId(dbItemIamIdentityService.getId());
            dbItemIamFido2Requests.setRequestId(SOSAuthHelper.createAccessToken());
            sosHibernateSession.save(dbItemIamFido2Requests);
            fidoRequestAuthenticationResponse.setRequestId(dbItemIamFido2Requests.getRequestId());
            Globals.commit(sosHibernateSession);

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(fidoRequestAuthenticationResponse));

        } catch (JocAuthenticationException e) {
            getJocError().setLogAsInfo(true);
            e.addErrorMetaInfo(getJocError());
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e);
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
    public JOCDefaultResponse postFidoRegistrationDelete(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;

        try {
            initLogging(API_CALL_FIDO_REGISTRATION_DELETE, body, accessToken);
            JsonValidator.validate(body, FidoRegistrationsFilter.class);
            FidoRegistrationsFilter fidoRegistrationsFilter = Globals.objectMapper.readValue(body, FidoRegistrationsFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSSecurityDBConfiguration");
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, fidoRegistrationsFilter
                    .getIdentityServiceName());

            if (!IdentityServiceTypes.FIDO.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                throw new JocAuthenticationException("Only allowed for Identity Service type FIDO2 " + "<" + dbItemIamIdentityService
                        .getIdentityServiceType() + ">");
            }

            IamFidoDBLayer iamFido2DBLayer = new IamFidoDBLayer(sosHibernateSession);
            IamFidoRegistrationFilter iamFidoRegistrationFilter = new IamFidoRegistrationFilter();
            iamFidoRegistrationFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            for (FidoRegistrationAccount account : fidoRegistrationsFilter.getAccounts()) {
                iamFidoRegistrationFilter.setAccountName(account.getAccountName());
                iamFidoRegistrationFilter.setOrigin(account.getOrigin());

                int count = iamFido2DBLayer.delete(iamFidoRegistrationFilter);
                if (count == 0) {
                    throw new JocObjectNotExistException("Couldn't find the account <" + account.getAccountName() + "/" + account.getOrigin() + ">");
                }
            }
            Globals.commit(sosHibernateSession);

            storeAuditLog(fidoRegistrationsFilter.getAuditLog(), CategoryType.IDENTITY);

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocAuthenticationException e) {
            getJocError().setLogAsInfo(true);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.rollback(sosHibernateSession);
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postFidoRegistrations(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            initLogging(API_CALL_FIDO_REGISTRATIONS, body, accessToken);
            JsonValidator.validateFailFast(body, AccountListFilter.class);
            FidoRegistrationListFilter accountFilter = Globals.objectMapper.readValue(body, FidoRegistrationListFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_FIDO_REGISTRATIONS);
            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, accountFilter
                    .getIdentityServiceName());

            if (!IdentityServiceTypes.FIDO.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                throw new JocAuthenticationException("Only allowed for Identity Service type FIDO2 " + "<" + dbItemIamIdentityService
                        .getIdentityServiceType() + ">");
            }

            FidoRegistrations fidoRegistrations = new FidoRegistrations();
            fidoRegistrations.setFidoRegistrationItems(new ArrayList<FidoRegistration>());

            IamFidoDBLayer iamFido2DBLayer = new IamFidoDBLayer(sosHibernateSession);
            IamFidoRegistrationFilter iamFidoRegistrationFilter = new IamFidoRegistrationFilter();

            iamFidoRegistrationFilter.setCompleted(true);
            iamFidoRegistrationFilter.setDeferred(accountFilter.getDeferred());
            iamFidoRegistrationFilter.setConfirmed(accountFilter.getConfirmed());
            iamFidoRegistrationFilter.setAccountName(accountFilter.getAccountName());
            iamFidoRegistrationFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            List<DBItemIamFido2Registration> listOfFido2Registrations = iamFido2DBLayer.getIamRegistrationList(iamFidoRegistrationFilter, 0);
            for (DBItemIamFido2Registration dbItemIamFido2Registrations : listOfFido2Registrations) {
                FidoRegistration fidoRegistration = new FidoRegistration();
                fidoRegistration.setConfirmed(dbItemIamFido2Registrations.getConfirmed());
                fidoRegistration.setDeferred(dbItemIamFido2Registrations.getDeferred());
                fidoRegistration.setEmail(dbItemIamFido2Registrations.getEmail());
                fidoRegistration.setIdentityServiceName(dbItemIamIdentityService.getIdentityServiceName());
                fidoRegistration.setPublicKey(dbItemIamFido2Registrations.getPublicKey());
                fidoRegistration.setAccountName(dbItemIamFido2Registrations.getAccountName());
                fidoRegistration.setOrigin(dbItemIamFido2Registrations.getOrigin());
                fidoRegistrations.getFidoRegistrationItems().add(fidoRegistration);
            }

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(fidoRegistrations));
        } catch (JocAuthenticationException e) {
            getJocError().setLogAsInfo(true);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
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
    public JOCDefaultResponse postFidoRegistrationApprove(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {
            FidoRegistrationsFilter fidoRegistrationsFilter = null;
            initLogging(API_CALL_APPROVE, body, accessToken);
            JsonValidator.validate(body, FidoRegistrationsFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            fidoRegistrationsFilter = Globals.objectMapper.readValue(body, FidoRegistrationsFilter.class);
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_APPROVE);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, fidoRegistrationsFilter
                    .getIdentityServiceName());

            if (!IdentityServiceTypes.FIDO.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                throw new JocAuthenticationException("Only allowed for Identity Service type FIDO " + "<" + dbItemIamIdentityService
                        .getIdentityServiceType() + ">");
            }

            IamFidoDBLayer iamFidoDBLayer = new IamFidoDBLayer(sosHibernateSession);
            IamFidoRegistrationFilter iamFidoRegistrationFilter = new IamFidoRegistrationFilter();
            iamFidoRegistrationFilter.setIdentityServiceId(dbItemIamIdentityService.getId());

            for (FidoRegistrationAccount account : fidoRegistrationsFilter.getAccounts()) {

                iamFidoRegistrationFilter.setAccountName(account.getAccountName());
                iamFidoRegistrationFilter.setOrigin(account.getOrigin());
                DBItemIamFido2Registration dbItemIamFido2Registration = iamFidoDBLayer.getUniqueFidoRegistration(iamFidoRegistrationFilter);
                if (dbItemIamFido2Registration == null) {
                    throw new JocObjectNotExistException("Couldn't find the registration for <" + account.getAccountName() + "/" + account.getOrigin()
                            + ">");
                }
                iamFidoDBLayer.delete(iamFidoRegistrationFilter);

                IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);

                IamAccountFilter iamAccountFilter = new IamAccountFilter();
                iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
                iamAccountFilter.setAccountName(account.getAccountName());
                DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(iamAccountFilter);
                if (dbItemIamAccount != null) {

                    IamFidoDevicesDBLayer iamFidoDevicesDBLayer = new IamFidoDevicesDBLayer(sosHibernateSession);
                    IamFidoDevicesFilter filter = new IamFidoDevicesFilter();
                    filter.setAccountId(dbItemIamAccount.getId());
                    filter.setOrigin(account.getOrigin());
                    List<DBItemIamFido2Devices> listOfDevices = iamFidoDevicesDBLayer.getListOfFidoDevices(filter);

                    if (listOfDevices.size() > 0) {
                        JocError jocError = new JocError();
                        jocError.setMessage("Account already exists " + "<" + dbItemIamIdentityService.getIdentityServiceName() + "." + account
                                .getAccountName() + "/" + account.getOrigin() + ">");
                        throw new JocException(jocError);
                    }
                } else {

                    dbItemIamAccount = new DBItemIamAccount();
                    dbItemIamAccount.setAccountName(account.getAccountName());
                    dbItemIamAccount.setIdentityServiceId(dbItemIamIdentityService.getId());
                    dbItemIamAccount.setDisabled(false);
                    dbItemIamAccount.setAccountPassword("********");
                    dbItemIamAccount.setForcePasswordChange(false);
                    dbItemIamAccount.setEmail(dbItemIamFido2Registration.getEmail());

                    sosHibernateSession.save(dbItemIamAccount);
                }

                DBItemIamFido2Devices dbItemIamFido2Devices = new DBItemIamFido2Devices();
                dbItemIamFido2Devices.setAccountId(dbItemIamAccount.getId());
                dbItemIamFido2Devices.setPublicKey(dbItemIamFido2Registration.getPublicKey());
                dbItemIamFido2Devices.setAlgorithm(dbItemIamFido2Registration.getAlgorithm());
                dbItemIamFido2Devices.setCredentialId(dbItemIamFido2Registration.getCredentialId());
                dbItemIamFido2Devices.setIdentityServiceId(dbItemIamIdentityService.getId());
                dbItemIamFido2Devices.setOrigin(account.getOrigin());
                sosHibernateSession.save(dbItemIamFido2Devices);

                LOGGER.info("FIDO2 registration approved");

                com.sos.joc.model.security.properties.Properties properties = SOSAuthHelper.getIamProperties(dbItemIamIdentityService
                        .getIdentityServiceName());

                FidoConfirmationMail fidoConfirmationMail = new FidoConfirmationMail(properties.getFido());
                fidoConfirmationMail.sendRegistrationApprovedMail(dbItemIamFido2Registration, dbItemIamAccount.getEmail(), dbItemIamIdentityService
                        .getIdentityServiceName());

                SOSAuthHelper.storeDefaultProfile(sosHibernateSession, account.getAccountName());

            }
            if (fidoRegistrationsFilter.getAuditLog() != null) {
                storeAuditLog(fidoRegistrationsFilter.getAuditLog(), CategoryType.IDENTITY);
            }

            Globals.commit(sosHibernateSession);
        } catch (JocAuthenticationException e) {
            getJocError().setLogAsInfo(true);
            e.addErrorMetaInfo(getJocError());
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.rollback(sosHibernateSession);
            Globals.disconnect(sosHibernateSession);
        }
        return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
    }

    @Override
    public JOCDefaultResponse postFidoRegistrationDeferr(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        FidoRegistrationsFilter fidoRegistrationsFilter = null;
        try {

            initLogging(API_CALL_DEFERR, body, accessToken);
            JsonValidator.validate(body, FidoRegistrationsFilter.class);
            fidoRegistrationsFilter = Globals.objectMapper.readValue(body, FidoRegistrationsFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_DEFERR);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);
            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, fidoRegistrationsFilter
                    .getIdentityServiceName());

            if (!IdentityServiceTypes.FIDO.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                throw new JocAuthenticationException("Only allowed for Identity Service type FIDO2 " + "<" + dbItemIamIdentityService
                        .getIdentityServiceType() + ">");
            }

            IamFidoDBLayer iamFido2DBLayer = new IamFidoDBLayer(sosHibernateSession);
            IamFidoRegistrationFilter iamFidoRegistrationFilter = new IamFidoRegistrationFilter();

            iamFidoRegistrationFilter.setIdentityServiceId(dbItemIamIdentityService.getId());

            for (FidoRegistrationAccount account : fidoRegistrationsFilter.getAccounts()) {

                iamFidoRegistrationFilter.setAccountName(account.getAccountName());
                iamFidoRegistrationFilter.setOrigin(account.getOrigin());
                DBItemIamFido2Registration dbItemIamFido2Registration = iamFido2DBLayer.getUniqueFidoRegistration(iamFidoRegistrationFilter);
                if (dbItemIamFido2Registration != null) {
                    LOGGER.info("FIDO2 registration deferred");
                    dbItemIamFido2Registration.setDeferred(true);
                    sosHibernateSession.update(dbItemIamFido2Registration);
                } else {
                    JocError error = new JocError();
                    error.setMessage("Unknown FIDO2 registration:" + account.getAccountName() + "/" + account.getOrigin());
                    throw new JocInfoException(error);
                }
            }
            storeAuditLog(fidoRegistrationsFilter.getAuditLog(), CategoryType.IDENTITY);
            Globals.commit(sosHibernateSession);

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));

        } catch (JocAuthenticationException e) {
            getJocError().setLogAsInfo(true);
            e.addErrorMetaInfo(getJocError());
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());

        } finally {
            fidoRegistrationsFilter = null;
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postFidoRegistrationConfirm(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        FidoConfirmationFilter fidoConfirmationFilter = null;
        try {

            initLogging(API_CALL_CONFIRM, body);
            JsonValidator.validateFailFast(body, FidoConfirmationFilter.class);
            fidoConfirmationFilter = Globals.objectMapper.readValue(body, FidoConfirmationFilter.class);

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_CONFIRM);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            IamFidoDBLayer iamFidoDBLayer = new IamFidoDBLayer(sosHibernateSession);
            IamFidoRegistrationFilter iamFido2RegistrationFilter = new IamFidoRegistrationFilter();
            iamFido2RegistrationFilter.setToken(fidoConfirmationFilter.getToken());
            DBItemIamFido2Registration dbItemIamFido2Registration = iamFidoDBLayer.getFido2Registration(iamFido2RegistrationFilter);
            if (dbItemIamFido2Registration != null) {
                LOGGER.info("FIDO registration confirmed");
                dbItemIamFido2Registration.setConfirmed(true);
                sosHibernateSession.update(dbItemIamFido2Registration);
            } else {
                JocError error = new JocError();
                error.setMessage("Unknown FIDO registration token");
                throw new JocInfoException(error);
            }

            Globals.commit(sosHibernateSession);

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityServiceById(sosHibernateSession, dbItemIamFido2Registration
                    .getIdentityServiceId());

            sendConfirmedMail(dbItemIamIdentityService.getIdentityServiceName(), dbItemIamFido2Registration);

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());

        } finally {
            fidoConfirmationFilter = null;
            Globals.disconnect(sosHibernateSession);
        }
    }

    private Configuration setConfigurationValues(DBItemJocConfiguration dbItem, String controllerId) {
        Configuration config = new Configuration();
        config.setId(dbItem.getId());
        config.setAccount(dbItem.getAccount());
        if (dbItem.getConfigurationType() != null) {
            config.setConfigurationType(ConfigurationType.fromValue(dbItem.getConfigurationType()));
        }
        config.setConfigurationItem(dbItem.getConfigurationItem());
        config.setObjectType(dbItem.getObjectType());
        config.setShared(dbItem.getShared());
        config.setName(dbItem.getName());
        if (dbItem.getControllerId() != null && !dbItem.getControllerId().isEmpty()) {
            config.setControllerId(dbItem.getControllerId());
        } else {
            config.setControllerId(controllerId);
        }
        return config;
    }

    @Override
    public JOCDefaultResponse postReadFidoConfiguration(String accessToken, byte[] body) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL_FIDO_CONFIGURATION, body, accessToken);
            JsonValidator.validateFailFast(body, Configuration.class);
            Configuration configuration = Globals.objectMapper.readValue(body, Configuration.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            connection = Globals.createSosHibernateStatelessConnection(API_CALL_FIDO_CONFIGURATION);
            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(connection);

            DBItemJocConfiguration dbItem = null;
            JocConfigurationFilter filter = new JocConfigurationFilter();
            filter.setConfigurationType(configuration.getConfigurationType().value());
            filter.setName(configuration.getName());
            filter.setObjectType(configuration.getObjectType());
            List<DBItemJocConfiguration> listOfdbItemJocConfiguration = jocConfigurationDBLayer.getJocConfigurations(filter, 0);
            if (listOfdbItemJocConfiguration.size() == 1) {
                dbItem = listOfdbItemJocConfiguration.get(0);
            } else {
                dbItem = new DBItemJocConfiguration();
                dbItem.setConfigurationType(configuration.getConfigurationType().value());
                dbItem.setName(configuration.getName());
                dbItem.setObjectType(configuration.getObjectType());
                dbItem.setConfigurationItem("{}");
                dbItem.setAccount(configuration.getAccount());
            }
            com.sos.joc.model.security.properties.Properties properties = Globals.objectMapper.readValue(dbItem.getConfigurationItem(),
                    com.sos.joc.model.security.properties.Properties.class);

            properties = SOSAuthHelper.setDefaultFIDO2Settings(properties);

            dbItem.setConfigurationItem(Globals.objectMapper.writeValueAsString(properties));
            Configuration200 entity = new Configuration200();
            entity.setDeliveryDate(Date.from(Instant.now()));
            Configuration conf = setConfigurationValues(dbItem, configuration.getControllerId());

            entity.setConfiguration(conf);
            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }

    }

    private void sendRegistrationMail(String identityServiceName, DBItemIamFido2Registration dbItemIamFido2Registration, String to) throws Exception {
        try {
            com.sos.joc.model.security.properties.Properties properties = SOSAuthHelper.getIamProperties(identityServiceName);

            FidoConfirmationMail fidoConfirmationMail = new FidoConfirmationMail(properties.getFido());
            fidoConfirmationMail.sendRegistrationMail(dbItemIamFido2Registration, to, identityServiceName);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    private void sendConfirmedMail(String identityServiceName, DBItemIamFido2Registration dbItemIamFido2Registration) throws Exception {
        try {
            com.sos.joc.model.security.properties.Properties properties = SOSAuthHelper.getIamProperties(identityServiceName);

            FidoConfirmationMail fidoConfirmationMail = new FidoConfirmationMail(properties.getFido());
            fidoConfirmationMail.sendConfirmedMail(dbItemIamFido2Registration, identityServiceName);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
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