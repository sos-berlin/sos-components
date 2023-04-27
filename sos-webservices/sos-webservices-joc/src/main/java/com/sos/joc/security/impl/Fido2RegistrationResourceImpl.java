package com.sos.joc.security.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamFido2Registration;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;
import com.sos.joc.db.security.IamFido2DBLayer;
import com.sos.joc.db.security.IamFido2RegistrationFilter;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocInfoException;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.security.accounts.AccountListFilter;
import com.sos.joc.model.security.accounts.AccountRename;
import com.sos.joc.model.security.accounts.AccountsFilter;
import com.sos.joc.model.security.fido2.Fido2Registration;
import com.sos.joc.model.security.fido2.Fido2RegistrationFilter;
import com.sos.joc.model.security.fido2.Fido2RegistrationListFilter;
import com.sos.joc.model.security.fido2.Fido2Registrations;
import com.sos.joc.model.security.fido2.Fido2RegistrationsFilter;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;
import com.sos.joc.security.classes.SecurityHelper;
import com.sos.joc.security.resource.IFido2RegistrationResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("iam")
public class Fido2RegistrationResourceImpl extends JOCResourceImpl implements IFido2RegistrationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(Fido2RegistrationResourceImpl.class);
    private static final String API_CALL_FIDO2_REGISTRATIONS = "./iam/fido2registrations";
    private static final String API_CALL_FIDO2_REGISTRATION_READ = "./iam/fido2registration";
    private static final String API_CALL_FIDO2_REGISTRATION_STORE = "./iam/fido2registration/store";
    private static final String API_CALL_FIDO2_REGISTRATION_DELETE = "./iam/fido2registration/delete";
    private static final String API_CALL_VERIFY_REGISTRATION = "./iam/fido2registration/verify";
    private static final String API_CALL_APPROVE = "./iam/fido2registration/approve";
    private static final String API_CALL_REJECT = "./iam/fido2registration/reject";

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

            DBItemIamIdentityService dbItemIamIdentityService = SecurityHelper.getIdentityService(sosHibernateSession, fido2RegistrationFilter
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
                fido2Registration.setRpName(dbItemIamFido2Registration.getRpName());
                fido2Registration.setIdentityServiceName(dbItemIamIdentityService.getIdentityServiceName());
                fido2Registration.setAccessToken(dbItemIamFido2Registration.getAccessToken());
                fido2Registration.setConfirmed(dbItemIamFido2Registration.getConfirmed());
                fido2Registration.setApproved(dbItemIamFido2Registration.getApproved());
                fido2Registration.setRejected(dbItemIamFido2Registration.getRejected());
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
    public JOCDefaultResponse postFido2RequestRegistration(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            Fido2Registration fido2Registration = Globals.objectMapper.readValue(body, Fido2Registration.class);

            initLogging(API_CALL_FIDO2_REGISTRATION_STORE, Globals.objectMapper.writeValueAsBytes(fido2Registration), accessToken);
            JsonValidator.validateFailFast(body, Fido2Registration.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_FIDO2_REGISTRATION_STORE);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();

            DBItemIamIdentityService dbItemIamIdentityService = SecurityHelper.getIdentityService(sosHibernateSession, fido2Registration
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

            dbItemIamFido2Registration.setAccessToken(fido2Registration.getAccessToken());
            dbItemIamFido2Registration.setConfirmed(fido2Registration.getConfirmed());
            dbItemIamFido2Registration.setApproved(fido2Registration.getApproved());
            dbItemIamFido2Registration.setEmail(fido2Registration.getEmail());
            dbItemIamFido2Registration.setPublicKey(fido2Registration.getEmail());
            dbItemIamFido2Registration.setRejected(fido2Registration.getRejected());
            dbItemIamFido2Registration.setAccountName(fido2Registration.getAccountName());
            dbItemIamFido2Registration.setRpName(fido2Registration.getRpName());
            dbItemIamFido2Registration.setIdentityServiceId(dbItemIamIdentityService.getId());

            if (isNew) {
                sosHibernateSession.save(dbItemIamFido2Registration);
            } else {
                sosHibernateSession.update(dbItemIamFido2Registration);
            }

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
    public JOCDefaultResponse postFido2VerifyRegistration(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;

        try {
            initLogging(API_CALL_VERIFY_REGISTRATION, body, accessToken);
            JsonValidator.validate(body, AccountRename.class);
            AccountRename accountRename = Globals.objectMapper.readValue(body, AccountRename.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_VERIFY_REGISTRATION);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();
            DBItemIamIdentityService dbItemIamIdentityService = SecurityHelper.getIdentityService(sosHibernateSession, accountRename
                    .getIdentityServiceName());

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);

            IamAccountFilter iamAccountFilter = new IamAccountFilter();
            iamAccountFilter.setAccountName(accountRename.getAccountNewName());
            iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(iamAccountFilter);
            if (dbItemIamAccount != null) {
                JocError error = new JocError();
                error.setMessage("Account " + accountRename.getAccountNewName() + " already exists");
                throw new JocException(error);
            }

            int count = iamAccountDBLayer.renameAccount(dbItemIamIdentityService.getId(), accountRename.getAccountOldName(), accountRename
                    .getAccountNewName());
            if (count == 0) {
                throw new JocObjectNotExistException("Couldn't find the account <" + accountRename.getAccountOldName() + ">");
            }

            storeAuditLog(accountRename.getAuditLog(), CategoryType.IDENTITY);

            Globals.commit(sosHibernateSession);

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));

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

            DBItemIamIdentityService dbItemIamIdentityService = SecurityHelper.getIdentityService(sosHibernateSession, fido2RegistrationsFilter
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
            DBItemIamIdentityService dbItemIamIdentityService = SecurityHelper.getIdentityService(sosHibernateSession, accountFilter
                    .getIdentityServiceName());

            if (!IdentityServiceTypes.FIDO_2.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                throw new JocObjectNotExistException("Only allowed for Identity Service type FIDO2 " + "<" + dbItemIamIdentityService
                        .getIdentityServiceType() + ">");
            }

            Fido2Registrations fido2Registrations = new Fido2Registrations();
            fido2Registrations.setFido2RegistrationItems(new ArrayList<Fido2Registration>());

            IamFido2DBLayer iamFido2DBLayer = new IamFido2DBLayer(sosHibernateSession);
            IamFido2RegistrationFilter iamFido2RegistrationFilter = new IamFido2RegistrationFilter();

            boolean onlyConfirmed = Boolean.TRUE == accountFilter.getConfirmed();
            boolean onlyApproved = Boolean.TRUE == accountFilter.getApproved();
            boolean onlyRejected = Boolean.TRUE == accountFilter.getRejected();
            if (onlyApproved && !onlyRejected && !onlyConfirmed) {
                iamFido2RegistrationFilter.setApproved(true);
            } else if (onlyRejected && !onlyApproved && !onlyConfirmed) {
                iamFido2RegistrationFilter.setRejected(false);
            } else if (onlyConfirmed && !onlyApproved && !onlyRejected) {
                iamFido2RegistrationFilter.setConfirmed(false);
            } else {
                iamFido2RegistrationFilter.setRejected(null);
                iamFido2RegistrationFilter.setConfirmed(null);
                iamFido2RegistrationFilter.setApproved(null);
            }
            iamFido2RegistrationFilter.setAccountName(accountFilter.getAccountName());
            iamFido2RegistrationFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            List<DBItemIamFido2Registration> listOfFido2Registrations = iamFido2DBLayer.getIamAccountList(iamFido2RegistrationFilter, 0);
            for (DBItemIamFido2Registration dbItemIamFido2Registrations : listOfFido2Registrations) {
                Fido2Registration fido2Registration = new Fido2Registration();
                fido2Registration.setAccessToken(dbItemIamFido2Registrations.getAccessToken());
                fido2Registration.setConfirmed(dbItemIamFido2Registrations.getConfirmed());
                fido2Registration.setApproved(dbItemIamFido2Registrations.getApproved());
                fido2Registration.setEmail(dbItemIamFido2Registrations.getEmail());
                fido2Registration.setIdentityServiceName(dbItemIamIdentityService.getIdentityServiceName());
                fido2Registration.setPublicKey(dbItemIamFido2Registrations.getPublicKey());
                fido2Registration.setRejected(dbItemIamFido2Registrations.getRejected());
                fido2Registration.setRpName(dbItemIamFido2Registrations.getRpName());
                fido2Registration.setAccountName(dbItemIamFido2Registrations.getAccountName());
                fido2Registrations.getFido2RegistrationItems().add(fido2Registration);
            }

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(fido2Registrations));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private JOCDefaultResponse changeFlag(String accessToken, byte[] body, Boolean approved, Boolean rejected, Boolean confirmed, String apiCall) {
        SOSHibernateSession sosHibernateSession = null;
        Fido2RegistrationsFilter fido2RegistrationsFilter = null;
        try {

            initLogging(apiCall, body, accessToken);
            JsonValidator.validate(body, AccountsFilter.class);
            fido2RegistrationsFilter = Globals.objectMapper.readValue(body, Fido2RegistrationsFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(apiCall);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);
            DBItemIamIdentityService dbItemIamIdentityService = SecurityHelper.getIdentityService(sosHibernateSession, fido2RegistrationsFilter
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
                DBItemIamFido2Registration dbItemIamAccount = iamFido2DBLayer.getUniqueFido2Registration(iamFido2RegistrationFilter);
                if (dbItemIamAccount != null) {
                    if (approved != null) {
                        dbItemIamAccount.setApproved(approved);
                    }
                    if (rejected != null) {
                        dbItemIamAccount.setRejected(rejected);
                    }
                    if (confirmed != null) {
                        dbItemIamAccount.setConfirmed(confirmed);
                    }
                    sosHibernateSession.update(dbItemIamAccount);
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
    public JOCDefaultResponse postFido2RegistrationApprove(String accessToken, byte[] body) {
        return changeFlag(accessToken, body, true, null, null, API_CALL_APPROVE);
    }

    @Override
    public JOCDefaultResponse postFido2RegistrationReject(String accessToken, byte[] body) {
        return changeFlag(accessToken, body, null, true, null, API_CALL_REJECT);
    }

    @Override
    public JOCDefaultResponse postFido2RegistrationConfirm(String accessToken, byte[] body) {
        return changeFlag(accessToken, body, null, null, true, API_CALL_REJECT);
    }

}