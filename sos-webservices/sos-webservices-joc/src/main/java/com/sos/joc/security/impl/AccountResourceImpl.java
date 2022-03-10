package com.sos.joc.security.impl;

import java.security.KeyStore;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.classes.SOSInitialPasswordSetting;
import com.sos.auth.classes.SOSPasswordHasher;
import com.sos.auth.vault.SOSVaultHandler;
import com.sos.auth.vault.classes.SOSVaultAccountCredentials;
import com.sos.auth.vault.classes.SOSVaultWebserviceCredentials;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamAccount2RoleWithName;
import com.sos.joc.db.authentication.DBItemIamAccount2Roles;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.authentication.DBItemIamPermissionWithName;
import com.sos.joc.db.authentication.DBItemIamRole;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;
import com.sos.joc.db.security.IamIdentityServiceDBLayer;
import com.sos.joc.db.security.IamIdentityServiceFilter;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocInfoException;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.security.accounts.Account;
import com.sos.joc.model.security.accounts.AccountFilter;
import com.sos.joc.model.security.accounts.AccountListFilter;
import com.sos.joc.model.security.accounts.AccountRename;
import com.sos.joc.model.security.accounts.Accounts;
import com.sos.joc.model.security.accounts.AccountsFilter;
import com.sos.joc.model.security.idendityservice.IdentityServiceTypes;
import com.sos.joc.security.resource.IAccountResource;
import com.sos.schema.JsonValidator;

@Path("iam")
public class AccountResourceImpl extends JOCResourceImpl implements IAccountResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountResourceImpl.class);

    private static final String API_CALL_ACCOUNTS = "./iam/accounts";
    private static final String API_CALL_ACCOUNT_READ = "./iam/account";
    private static final String API_CALL_ACCOUNT_STORE = "./iam/account/store";
    private static final String API_CALL_ACCOUNT_RENAME = "./iam/account/rename";
    private static final String API_CALL_ACCOUNT_DELETE = "./iam/account/delete";
    private static final String API_CALL_CHANGE_PASSWORD = "./iam/account/changePassword";
    private static final String API_CALL_FORCE_PASSWORD_CHANGE = "./iam/account/forcePasswordChange";

    @Override
    public JOCDefaultResponse postAccountRead(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            AccountFilter accountFilter = Globals.objectMapper.readValue(body, AccountFilter.class);
            JsonValidator.validateFailFast(body, AccountFilter.class);

            initLogging(API_CALL_ACCOUNT_READ, body, accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            Account account = new Account();

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_ACCOUNT_READ);
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_ACCOUNT_READ);

            DBItemIamIdentityService dbItemIamIdentityService = getIdentityService(sosHibernateSession, accountFilter.getIdentityServiceName());

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter filter = new IamAccountFilter();
            filter.setAccountName(accountFilter.getAccountName());
            filter.setIdentityServiceId(dbItemIamIdentityService.getId());
            DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(filter);
            if (dbItemIamAccount != null) {
                account.setDisabled(dbItemIamAccount.getDisabled());
                account.setAccountName(accountFilter.getAccountName());
                account.setForcePasswordChange(dbItemIamAccount.getForcePasswordChange());
                account.setIdentityServiceName(accountFilter.getIdentityServiceName());
                List<DBItemIamPermissionWithName> roles = iamAccountDBLayer.getListOfRolesForAccountName(accountFilter.getAccountName(),
                        dbItemIamIdentityService.getId());
                roles.stream().map(role -> role.getRoleName()).collect(Collectors.toList());
                account.setRoles(roles.stream().map(role -> role.getRoleName()).collect(Collectors.toList()));
            } else {
                throw new JocObjectNotExistException("Object account <" + accountFilter.getAccountName() + "> not found");
            }
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(account));

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
    public JOCDefaultResponse postAccountStore(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            Account accountMasked = Globals.objectMapper.readValue(body, Account.class);
            Account account = Globals.objectMapper.readValue(body, Account.class);

            accountMasked.setOldPassword("********");
            accountMasked.setPassword("********");
            accountMasked.setRepeatedPassword("********");

            JsonValidator.validateFailFast(body, Account.class);

            initLogging(API_CALL_ACCOUNT_STORE, Globals.objectMapper.writeValueAsBytes(accountMasked), accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_ACCOUNT_STORE);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();

            DBItemIamIdentityService dbItemIamIdentityService = getIdentityService(sosHibernateSession, account.getIdentityServiceName());

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);

            SOSInitialPasswordSetting sosInitialPasswordSetting = SOSAuthHelper.getInitialPasswordSettings(sosHibernateSession);
            String initialPassword = sosInitialPasswordSetting.getInitialPassword();

            String password = "";

            if (account.getPassword() != null && !account.getPassword().isEmpty()) {
                password = account.getPassword();
            }
            if (password.isEmpty()) {
                password = initialPassword;
            }

            IamAccountFilter iamAccountFilter = new IamAccountFilter();
            iamAccountFilter.setAccountName(account.getAccountName());
            DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(iamAccountFilter);
            boolean newAccount = false;
            if (dbItemIamAccount == null) {
                dbItemIamAccount = new DBItemIamAccount();
                newAccount = true;
            }

            dbItemIamAccount.setAccountName(account.getAccountName());
            if ("JOC".equals(dbItemIamIdentityService.getIdentityServiceType()) || "VAULT-JOC-ACTIVE".equals(dbItemIamIdentityService
                    .getIdentityServiceType())) {
                if (account.getPassword() != null && !password.isEmpty()) {
                    if (!sosInitialPasswordSetting.isMininumPasswordLength(password)) {
                        JocError error = new JocError();
                        error.setMessage("Password is too short");
                        throw new JocInfoException(error);
                    }
                    dbItemIamAccount.setAccountPassword(SOSPasswordHasher.hash(password));
                }
            } else {
                dbItemIamAccount.setAccountPassword("********");
            }
            dbItemIamAccount.setIdentityServiceId(dbItemIamIdentityService.getId());
            dbItemIamAccount.setForcePasswordChange(account.getForcePasswordChange());
            if (sosInitialPasswordSetting.getInitialPassword().equals(password)) {
                dbItemIamAccount.setForcePasswordChange(true);
            }
            dbItemIamAccount.setDisabled(account.getDisabled());

            if (newAccount) {
                sosHibernateSession.save(dbItemIamAccount);
            } else {
                sosHibernateSession.update(dbItemIamAccount);
            }

            Long accountId = dbItemIamAccount.getId();
            List<DBItemIamAccount2Roles> listOfRoles = iamAccountDBLayer.getListOfRoles(accountId);
            for (DBItemIamAccount2Roles entry : listOfRoles) {
                DBItemIamRole dbItemIamRole = iamAccountDBLayer.getIamRole(entry.getRoleId());

                if (dbItemIamRole != null && !account.getRoles().contains(dbItemIamRole.getRoleName())) {
                    iamAccountFilter = new IamAccountFilter();
                    iamAccountFilter.setId(entry.getAccountId());
                    iamAccountFilter.setRoleId(entry.getRoleId());
                    iamAccountDBLayer.deleteAccount2Role(iamAccountFilter);
                }
            }
            for (String role : account.getRoles()) {
                DBItemIamRole dbItemIamRole = iamAccountDBLayer.getRoleByName(role, dbItemIamIdentityService.getId());
                if (dbItemIamRole != null) {
                    DBItemIamAccount2Roles dbItemIamAccount2Roles = iamAccountDBLayer.getRoleAssignment(dbItemIamRole.getId(), accountId);

                    if (dbItemIamAccount2Roles == null) {
                        dbItemIamAccount2Roles = new DBItemIamAccount2Roles();
                        dbItemIamAccount2Roles.setRoleId(dbItemIamRole.getId());
                        dbItemIamAccount2Roles.setAccountId(accountId);
                        sosHibernateSession.save(dbItemIamAccount2Roles);
                    }
                }
            }

            if (IdentityServiceTypes.VAULT_JOC_ACTIVE.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                SOSVaultWebserviceCredentials webserviceCredentials = new SOSVaultWebserviceCredentials();
                SOSIdentityService sosIdentityService = new SOSIdentityService(dbItemIamIdentityService);
                webserviceCredentials.setValuesFromProfile(sosIdentityService);
                storeInVault(webserviceCredentials, account, password);
            }

            storeAuditLog(account.getAuditLog(), CategoryType.IDENTITY);
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
    public JOCDefaultResponse postAccountRename(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;

        try {
            initLogging(API_CALL_ACCOUNT_RENAME, body, accessToken);
            JsonValidator.validate(body, AccountRename.class);
            AccountRename accountRename = Globals.objectMapper.readValue(body, AccountRename.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_ACCOUNT_RENAME);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();
            DBItemIamIdentityService dbItemIamIdentityService = getIdentityService(sosHibernateSession, accountRename.getIdentityServiceName());

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
                throw new JocObjectNotExistException("Object account <" + accountRename.getAccountOldName() + "> not found");
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
    public JOCDefaultResponse postAccountsDelete(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;

        try {
            JsonValidator.validate(body, AccountsFilter.class);
            AccountsFilter accountsFilter = Globals.objectMapper.readValue(body, AccountsFilter.class);
            initLogging(API_CALL_ACCOUNT_DELETE, Globals.objectMapper.writeValueAsBytes(accountsFilter), accessToken);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSSecurityDBConfiguration");
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            DBItemIamIdentityService dbItemIamIdentityService = getIdentityService(sosHibernateSession, accountsFilter.getIdentityServiceName());

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter iamAccountFilter = new IamAccountFilter();
            iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            for (String accountName : accountsFilter.getAccountNames()) {
                iamAccountFilter.setAccountName(accountName);
                int count = iamAccountDBLayer.deleteCascading(iamAccountFilter);
                if (count == 0) {
                    throw new JocObjectNotExistException("Object <" + accountName + "> not found");
                }
            }
            Globals.commit(sosHibernateSession);

            storeAuditLog(accountsFilter.getAuditLog(), CategoryType.IDENTITY);

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
    public JOCDefaultResponse postAccounts(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            AccountListFilter accountFilter = Globals.objectMapper.readValue(body, AccountListFilter.class);
            JsonValidator.validateFailFast(body, AccountListFilter.class);

            initLogging(API_CALL_ACCOUNTS, body, accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_ACCOUNTS);
            DBItemIamIdentityService dbItemIamIdentityService = getIdentityService(sosHibernateSession, accountFilter.getIdentityServiceName());

            Accounts accounts = new Accounts();
            accounts.setAccountItems(new ArrayList<Account>());

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter filter = new IamAccountFilter();
            filter.setIdentityServiceId(dbItemIamIdentityService.getId());
            List<DBItemIamAccount> listOfAccounts = iamAccountDBLayer.getIamAccountList(filter, 0);
            for (DBItemIamAccount dbItemIamAccount : listOfAccounts) {
                Account account = new Account();
                account.setAccountName(dbItemIamAccount.getAccountName());
                account.setDisabled(dbItemIamAccount.getDisabled());
                account.setForcePasswordChange(dbItemIamAccount.getForcePasswordChange());
                account.setIdentityServiceName(accountFilter.getIdentityServiceName());
                List<DBItemIamAccount2RoleWithName> listOfRoles = iamAccountDBLayer.getListOfRolesWithName(dbItemIamAccount);
                account.setRoles(new ArrayList<String>());
                for (DBItemIamAccount2RoleWithName dbItemIamAccount2RoleWithName : listOfRoles) {
                    account.getRoles().add(dbItemIamAccount2RoleWithName.getRoleName());
                }
                accounts.getAccountItems().add(account);
            }

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(accounts));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private void storeInVault(SOSVaultWebserviceCredentials webserviceCredentials, Account account, String password) throws Exception {
        KeyStore trustStore = null;

        if ((webserviceCredentials.getTruststorePath() != null) && (webserviceCredentials.getTrustStoreType() != null)) {
            trustStore = KeyStoreUtil.readTrustStore(webserviceCredentials.getTruststorePath(), webserviceCredentials.getTrustStoreType(),
                    webserviceCredentials.getTruststorePassword());

            SOSVaultHandler sosVaultHandler = new SOSVaultHandler(webserviceCredentials, trustStore);
            SOSVaultAccountCredentials sosVaultAccountCredentials = new SOSVaultAccountCredentials();
            sosVaultAccountCredentials.setUsername(account.getAccountName());

            List<String> tokenPolicies = new ArrayList<String>();
            for (String role : account.getRoles()) {
                tokenPolicies.add(role);
            }

            sosVaultAccountCredentials.setTokenPolicies(tokenPolicies);

            if (!"********".equals(password)) {
                sosVaultHandler.storeAccountPassword(sosVaultAccountCredentials, password);
            }

            sosVaultHandler.updateTokenPolicies(sosVaultAccountCredentials);
        } else {
            JocError error = new JocError();
            error.setMessage("Configuration for VAULT missing");
            throw new JocException(error);
        }

    }

    private DBItemIamIdentityService getIdentityService(SOSHibernateSession sosHibernateSession, String identityServiceName)
            throws SOSHibernateException {
        IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
        IamIdentityServiceFilter iamIdentityServiceFilter = new IamIdentityServiceFilter();
        iamIdentityServiceFilter.setIdentityServiceName(identityServiceName);
        DBItemIamIdentityService dbItemIamIdentityService = iamIdentityServiceDBLayer.getUniqueIdentityService(iamIdentityServiceFilter);
        if (dbItemIamIdentityService == null) {
            throw new JocObjectNotExistException("Object Identity Service <" + identityServiceName + "> not found");
        }
        return dbItemIamIdentityService;
    }

    private void changePassword(SOSHibernateSession sosHibernateSession, boolean withPasswordCheck, Account account,
            DBItemIamIdentityService dbItemIamIdentityService) throws Exception {

        IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
        IamAccountFilter iamAccountFilter = new IamAccountFilter();

        if (IdentityServiceTypes.VAULT_JOC_ACTIVE.toString().equals(dbItemIamIdentityService.getIdentityServiceType()) || IdentityServiceTypes.JOC
                .toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
            SOSVaultWebserviceCredentials webserviceCredentials = new SOSVaultWebserviceCredentials();
            if (IdentityServiceTypes.VAULT_JOC_ACTIVE.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                SOSIdentityService sosIdentityService = new SOSIdentityService(dbItemIamIdentityService);
                webserviceCredentials.setValuesFromProfile(sosIdentityService);
            }

            SOSInitialPasswordSetting sosInitialPasswordSetting = SOSAuthHelper.getInitialPasswordSettings(sosHibernateSession);

            if (account.getPassword() != null && !account.getPassword().equals(account.getRepeatedPassword())) {
                JocError error = new JocError();
                error.setMessage("Password does not match repeated password");
                throw new JocInfoException(error);
            }

            iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            iamAccountFilter.setAccountName(account.getAccountName());

            DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(iamAccountFilter);
            if (dbItemIamAccount != null) {

                if (withPasswordCheck) {
                    if (!SOSPasswordHasher.verify(account.getOldPassword(), dbItemIamAccount.getAccountPassword())) {
                        JocError error = new JocError();
                        error.setMessage("Unknown account or password is wrong");
                        throw new JocInfoException(error);
                    }
                }

                String password;
                if (account.getPassword() == null || account.getPassword().isEmpty()) {
                    password = sosInitialPasswordSetting.getInitialPassword();
                } else {
                    password = account.getPassword();
                }
                if (!sosInitialPasswordSetting.isMininumPasswordLength(password)) {
                    JocError error = new JocError();
                    error.setMessage("Password is too short");
                    throw new JocInfoException(error);
                }

                if (sosInitialPasswordSetting.getInitialPassword().equals(password)) {
                    dbItemIamAccount.setForcePasswordChange(true);
                }

                dbItemIamAccount.setAccountPassword(SOSPasswordHasher.hash(password));
                sosHibernateSession.update(dbItemIamAccount);

                if (IdentityServiceTypes.VAULT_JOC_ACTIVE.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                    storeInVault(webserviceCredentials, account, password);
                }
            } else {
                JocError error = new JocError();
                error.setMessage("Unknown account or password is wrong");
                throw new JocInfoException(error);
            }
        }
    }

    @Override
    public JOCDefaultResponse changePassword(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        Account account = null;
        try {
            Account accountMasked = Globals.objectMapper.readValue(body, Account.class);
            account = Globals.objectMapper.readValue(body, Account.class);

            accountMasked.setOldPassword("********");
            accountMasked.setPassword("********");
            accountMasked.setRepeatedPassword("********");

            initLogging(API_CALL_CHANGE_PASSWORD, Globals.objectMapper.writeValueAsBytes(accountMasked), accessToken);

            JsonValidator.validate(body, Account.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_CHANGE_PASSWORD);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            DBItemIamIdentityService dbItemIamIdentityService = getIdentityService(sosHibernateSession, account.getIdentityServiceName());

            changePassword(sosHibernateSession, true, account, dbItemIamIdentityService);
            Globals.commit(sosHibernateSession);

            storeAuditLog(account.getAuditLog(), CategoryType.IDENTITY);

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));

        } catch (JocException e) {
            Globals.rollback(sosHibernateSession);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            account = null;
            Globals.disconnect(sosHibernateSession);
        }
    }

    private JOCDefaultResponse changeFlage(String accessToken, byte[] body, Boolean disable, Boolean forcePasswordChange) {
        SOSHibernateSession sosHibernateSession = null;
        AccountsFilter accountsFilter = null;
        try {
            accountsFilter = Globals.objectMapper.readValue(body, AccountsFilter.class);

            initLogging(API_CALL_FORCE_PASSWORD_CHANGE, Globals.objectMapper.writeValueAsBytes(accountsFilter), accessToken);
            JsonValidator.validate(body, AccountsFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_FORCE_PASSWORD_CHANGE);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);
            DBItemIamIdentityService dbItemIamIdentityService = getIdentityService(sosHibernateSession, accountsFilter.getIdentityServiceName());

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter iamAccountFilter = new IamAccountFilter();

            if (IdentityServiceTypes.VAULT_JOC_ACTIVE.toString().equals(dbItemIamIdentityService.getIdentityServiceType()) || IdentityServiceTypes.JOC
                    .toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {

                iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
                for (String accountName : accountsFilter.getAccountNames()) {
                    iamAccountFilter.setAccountName(accountName);
                    DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(iamAccountFilter);
                    if (dbItemIamAccount != null) {
                        if (disable != null) {
                            dbItemIamAccount.setDisabled(disable);
                        }
                        if (forcePasswordChange != null) {
                            dbItemIamAccount.setForcePasswordChange(forcePasswordChange);
                        }
                        sosHibernateSession.update(dbItemIamAccount);
                    } else {
                        JocError error = new JocError();
                        error.setMessage("Unknown account:" + accountName);
                        throw new JocInfoException(error);
                    }
                }
            }
            storeAuditLog(accountsFilter.getAuditLog(), CategoryType.IDENTITY);
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
            accountsFilter = null;
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse forcePasswordChange(String accessToken, byte[] body) {
        return changeFlage(accessToken, body, null, true);
    }

    @Override
    public JOCDefaultResponse enable(String accessToken, byte[] body) {
        return changeFlage(accessToken, body, false, null);
    }

    @Override
    public JOCDefaultResponse disable(String accessToken, byte[] body) {
        return changeFlage(accessToken, body, true, null);
    }

    @Override
    public JOCDefaultResponse resetPassword(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        AccountsFilter accountsFilter = null;
        try {
            accountsFilter = Globals.objectMapper.readValue(body, AccountsFilter.class);

            initLogging(API_CALL_CHANGE_PASSWORD, Globals.objectMapper.writeValueAsBytes(accountsFilter), accessToken);

            JsonValidator.validate(body, AccountsFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_CHANGE_PASSWORD);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            DBItemIamIdentityService dbItemIamIdentityService = getIdentityService(sosHibernateSession, accountsFilter.getIdentityServiceName());
            Account account = new Account();
            account.setPassword(null);
            account.setIdentityServiceName(accountsFilter.getIdentityServiceName());

            for (String accountName : accountsFilter.getAccountNames()) {
                account.setAccountName(accountName);
                changePassword(sosHibernateSession, false, account, dbItemIamIdentityService);
            }

            Globals.commit(sosHibernateSession);

            storeAuditLog(accountsFilter.getAuditLog(), CategoryType.IDENTITY);

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));

        } catch (JocException e) {
            Globals.rollback(sosHibernateSession);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            accountsFilter = null;
            Globals.disconnect(sosHibernateSession);
        }
    }

}