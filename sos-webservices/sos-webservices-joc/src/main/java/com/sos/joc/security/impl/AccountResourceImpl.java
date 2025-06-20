package com.sos.joc.security.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthCurrentAccount;
import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.classes.SOSInitialPasswordSetting;
import com.sos.auth.classes.SOSPasswordHasher;
import com.sos.auth.classes.SOSPermissionMerger;
import com.sos.auth.classes.SOSPermissionsCreator;
import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.auth.interfaces.ISOSLogin;
import com.sos.auth.ldap.classes.SOSLdapLogin;
import com.sos.auth.sosintern.classes.SOSInternAuthLogin;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.security.SOSBlocklist;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamAccount2RoleWithName;
import com.sos.joc.db.authentication.DBItemIamAccount2Roles;
import com.sos.joc.db.authentication.DBItemIamBlockedAccount;
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
import com.sos.joc.model.security.accounts.AccountChangePassword;
import com.sos.joc.model.security.accounts.AccountFilter;
import com.sos.joc.model.security.accounts.AccountListFilter;
import com.sos.joc.model.security.accounts.AccountNamesFilter;
import com.sos.joc.model.security.accounts.AccountRename;
import com.sos.joc.model.security.accounts.Accounts;
import com.sos.joc.model.security.accounts.AccountsFilter;
import com.sos.joc.model.security.blocklist.BlockedAccount;
import com.sos.joc.model.security.blocklist.BlockedAccountsDeleteFilter;
import com.sos.joc.model.security.configuration.SecurityConfiguration;
import com.sos.joc.model.security.configuration.permissions.Permissions;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;
import com.sos.joc.security.resource.IAccountResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("iam")
public class AccountResourceImpl extends JOCResourceImpl implements IAccountResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountResourceImpl.class);
    private static final String API_CALL_ACCOUNTS = "./iam/accounts";
    private static final String API_CALL_ACCOUNT_READ = "./iam/account";
    private static final String API_CALL_ACCOUNT_STORE = "./iam/account/store";
    private static final String API_CALL_ACCOUNT_RENAME = "./iam/account/rename";
    private static final String API_CALL_ACCOUNTS_DELETE = "./iam/accounts/delete";
    private static final String API_CALL_CHANGE_PASSWORD = "./iam/account/changepassword";
    private static final String API_CALL_ACCOUNT_PERMISSIONS = "./iam/account/permissions";
    private static final String API_CALL_FORCE_PASSWORD_CHANGE = "./iam/accounts/forcepasswordchange";
    private static final String API_CALL_RESET_PASSWORD = "./iam/accounts/resetpassword";
    private static final String API_CALL_ACCOUNTS_ENABLE = "./iam/accounts/enable";
    private static final String API_CALL_ACCOUNTS_DISABLE = "./iam/accounts/disable";

    @Override
    public JOCDefaultResponse postAccountRead(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            body = initLogging(API_CALL_ACCOUNT_READ, body, accessToken, CategoryType.IDENTITY);
            JsonValidator.validateFailFast(body, AccountFilter.class);
            AccountFilter accountFilter = Globals.objectMapper.readValue(body, AccountFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getBasicJocPermissions(accessToken).getAdministration().getAccounts()
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            Account account = new Account();

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_ACCOUNT_READ);

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, accountFilter
                    .getIdentityServiceName());

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter filter = new IamAccountFilter();
            filter.setAccountName(accountFilter.getAccountName());

            DBItemIamBlockedAccount dbItemIamBlockedAccount = iamAccountDBLayer.getBlockedAccount(filter);
            account.setBlocked(dbItemIamBlockedAccount != null);

            filter.setIdentityServiceId(dbItemIamIdentityService.getId());

            DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(filter);
            if (dbItemIamAccount != null) {
                account.setDisabled(dbItemIamAccount.getDisabled());
                account.setAccountName(accountFilter.getAccountName());
                account.setEmail(dbItemIamAccount.getEmail());
                account.setForcePasswordChange(dbItemIamAccount.getForcePasswordChange());
                account.setIdentityServiceName(accountFilter.getIdentityServiceName());
                List<DBItemIamPermissionWithName> roles = iamAccountDBLayer.getListOfRolesForAccountName(accountFilter.getAccountName(),
                        dbItemIamIdentityService.getId());
                roles.stream().map(role -> role.getRoleName()).collect(Collectors.toList());
                account.setRoles(roles.stream().map(role -> role.getRoleName()).collect(Collectors.toList()));
            } else {
                throw new JocObjectNotExistException("Couldn't find the account <" + accountFilter.getAccountName() + ">");
            }
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(account));

        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postAccountStore(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            Account accountMasked = Globals.objectMapper.readValue(body, Account.class);

            accountMasked.setPassword("********");
            initLogging(API_CALL_ACCOUNT_STORE, Globals.objectMapper.writeValueAsBytes(accountMasked), accessToken, CategoryType.IDENTITY);
            JsonValidator.validateFailFast(body, Account.class);
            Account account = Globals.objectMapper.readValue(body, Account.class);

            JOCDefaultResponse jocDefaultResponse = initManageAccountPermissions(accessToken);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_ACCOUNT_STORE);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, account
                    .getIdentityServiceName());

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);

            if (accountMasked.getBlocked() != null) {
                if (accountMasked.getBlocked()) {
                    BlockedAccount blockedAccount = new BlockedAccount();
                    blockedAccount.setAccountName(accountMasked.getAccountName());
                    SOSBlocklist.store(sosHibernateSession, blockedAccount);
                } else {
                    BlockedAccountsDeleteFilter blockedAccountsDeleteFilter = new BlockedAccountsDeleteFilter();
                    blockedAccountsDeleteFilter.getAccountNames().add(accountMasked.getAccountName());
                    SOSBlocklist.delete(sosHibernateSession, blockedAccountsDeleteFilter);
                }
            }

            SOSInitialPasswordSetting sosInitialPasswordSetting = SOSAuthHelper.getInitialPasswordSettings(sosHibernateSession);
            String initialPassword = sosInitialPasswordSetting.getInitialPassword();

            String password = "";

            IamAccountFilter iamAccountFilter = new IamAccountFilter();
            iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            iamAccountFilter.setAccountName(account.getAccountName());
            DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(iamAccountFilter);
            boolean newAccount = false;
            if (dbItemIamAccount == null) {
                dbItemIamAccount = new DBItemIamAccount();
                if (account.getPassword() == null) {
                    account.setPassword(initialPassword);
                }
                newAccount = true;
            }

            if (account.getPassword() != null) {
                password = account.getPassword();
                if (password.isEmpty()) {
                    password = initialPassword;
                }
            }

            if (SOSAuthHelper.containsPrivateUseArea(password)) {
                JocError error = new JocError();
                error.setMessage("Password is invalid");
                throw new JocInfoException(error);
            }
            dbItemIamAccount.setAccountName(account.getAccountName());
            if (dbItemIamIdentityService.getIdentityServiceType().equals(IdentityServiceTypes.JOC.value())) {

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
            if (!dbItemIamIdentityService.getIdentityServiceType().equals(IdentityServiceTypes.OIDC.value()) && !dbItemIamIdentityService
                    .getIdentityServiceType().equals(IdentityServiceTypes.OIDC_JOC.value())) {
                if (sosInitialPasswordSetting.getInitialPassword().equals(password)) {
                    dbItemIamAccount.setForcePasswordChange(true);
                } else {
                    dbItemIamAccount.setForcePasswordChange(account.getForcePasswordChange());
                }
            } else {
                dbItemIamAccount.setForcePasswordChange(false);
            }
            dbItemIamAccount.setDisabled(account.getDisabled());

            if (newAccount) {
                sosHibernateSession.save(dbItemIamAccount);
                SOSAuthHelper.storeDefaultProfile(sosHibernateSession, account.getAccountName());
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

            storeAuditLog(account.getAuditLog());
            Globals.commit(sosHibernateSession);

            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postAccountRename(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;

        try {
            body = initLogging(API_CALL_ACCOUNT_RENAME, body, accessToken, CategoryType.IDENTITY);
            JsonValidator.validate(body, AccountRename.class);
            AccountRename accountRename = Globals.objectMapper.readValue(body, AccountRename.class);

            JOCDefaultResponse jocDefaultResponse = initManageAccountPermissions(accessToken);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_ACCOUNT_RENAME);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();
            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, accountRename
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

            storeAuditLog(accountRename.getAuditLog());

            Globals.commit(sosHibernateSession);

            return responseStatusJSOk(Date.from(Instant.now()));

        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postAccountsDelete(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;

        try {
            body = initLogging(API_CALL_ACCOUNTS_DELETE, body, accessToken, CategoryType.IDENTITY);
            JsonValidator.validate(body, AccountsFilter.class);
            AccountsFilter accountsFilter = Globals.objectMapper.readValue(body, AccountsFilter.class);

            JOCDefaultResponse jocDefaultResponse = initManageAccountPermissions(accessToken);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSSecurityDBConfiguration");
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, accountsFilter
                    .getIdentityServiceName());

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter iamAccountFilter = new IamAccountFilter();
            iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            boolean onlyDisabled = Boolean.TRUE == accountsFilter.getDisabled();
            boolean onlyEnabled = Boolean.TRUE == accountsFilter.getEnabled();
            if (onlyDisabled && !onlyEnabled) {
                iamAccountFilter.setDisabled(true);
            } else if (onlyEnabled && !onlyDisabled) {
                iamAccountFilter.setDisabled(false);
            } else {
                iamAccountFilter.setDisabled(null);
            }
            for (String accountName : accountsFilter.getAccountNames()) {
                iamAccountFilter.setAccountName(accountName);
                int count = iamAccountDBLayer.deleteCascading(iamAccountFilter);
                if (count == 0) {
                    throw new JocObjectNotExistException("Couldn't find the account <" + accountName + "> disabled=" + iamAccountFilter.getDisabled()
                            + "");
                }
            }
            Globals.commit(sosHibernateSession);

            storeAuditLog(accountsFilter.getAuditLog());

            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postAccounts(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            body = initLogging(API_CALL_ACCOUNTS, body, accessToken, CategoryType.IDENTITY);
            JsonValidator.validateFailFast(body, AccountListFilter.class);
            AccountListFilter accountFilter = Globals.objectMapper.readValue(body, AccountListFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getBasicJocPermissions(accessToken).getAdministration().getAccounts()
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_ACCOUNTS);
            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, accountFilter
                    .getIdentityServiceName());

            Accounts accounts = new Accounts();
            accounts.setAccountItems(new ArrayList<Account>());

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter filter = new IamAccountFilter();

            List<DBItemIamBlockedAccount> listOfBlockedAccounts = iamAccountDBLayer.getIamBlockedAccountList(filter, 0);
            Map<String, Date> blockedAccounts = listOfBlockedAccounts.stream().collect(Collectors.toMap(DBItemIamBlockedAccount::getAccountName,
                    DBItemIamBlockedAccount::getSince));

            boolean onlyDisabled = Boolean.TRUE == accountFilter.getDisabled();
            boolean onlyEnabled = Boolean.TRUE == accountFilter.getEnabled();
            if (onlyDisabled && !onlyEnabled) {
                filter.setDisabled(true);
            } else if (onlyEnabled && !onlyDisabled) {
                filter.setDisabled(false);
            } else {
                filter.setDisabled(null);
            }
            filter.setAccountName(accountFilter.getAccountName());
            filter.setIdentityServiceId(dbItemIamIdentityService.getId());
            List<DBItemIamAccount> listOfAccounts = iamAccountDBLayer.getIamAccountList(filter, 0);
            for (DBItemIamAccount dbItemIamAccount : listOfAccounts) {
                Account account = new Account();
                account.setAccountName(dbItemIamAccount.getAccountName());
                account.setDisabled(dbItemIamAccount.getDisabled());

                account.setForcePasswordChange(dbItemIamAccount.getForcePasswordChange());
                account.setIdentityServiceName(accountFilter.getIdentityServiceName());
                account.setBlocked(blockedAccounts.get(dbItemIamAccount.getAccountName()) != null);
                account.setEmail(dbItemIamAccount.getEmail());
                List<DBItemIamAccount2RoleWithName> listOfRoles = iamAccountDBLayer.getListOfRolesWithName(dbItemIamAccount);
                account.setRoles(new ArrayList<String>());
                for (DBItemIamAccount2RoleWithName dbItemIamAccount2RoleWithName : listOfRoles) {
                    account.getRoles().add(dbItemIamAccount2RoleWithName.getRoleName());
                }
                accounts.getAccountItems().add(account);
            }

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(accounts));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postAccountPermissions(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            body = initLogging(API_CALL_ACCOUNT_PERMISSIONS, body, accessToken, CategoryType.IDENTITY);
            JsonValidator.validateFailFast(body, AccountFilter.class);
            AccountFilter accountFilter = Globals.objectMapper.readValue(body, AccountFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getBasicJocPermissions(accessToken).getAdministration().getAccounts()
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            SOSPermissionMerger sosPermissionMerger = new SOSPermissionMerger();

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_ACCOUNT_PERMISSIONS);

            Permissions permissions = new Permissions();
            //Set<String> setOfAccountPermissions = new HashSet<String>();

            SOSAuthCurrentAccount currentAccount = new SOSAuthCurrentAccount(accountFilter.getAccountName());

            IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);

            IamIdentityServiceFilter iamIdentityServiceFilter = new IamIdentityServiceFilter();
            iamIdentityServiceFilter.setDisabled(false);
            iamIdentityServiceFilter.setRequired(true);

            List<DBItemIamIdentityService> listOfIdentityServices = iamIdentityServiceDBLayer.getIdentityServiceList(iamIdentityServiceFilter, 0);

            if (listOfIdentityServices.size() == 0) {
                DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, accountFilter
                        .getIdentityServiceName());
                listOfIdentityServices.add(dbItemIamIdentityService);
            }

            ISOSLogin sosLogin = null;

            for (DBItemIamIdentityService dbItemIamIdentityServiceEntry : listOfIdentityServices) {
                SOSIdentityService sosIdentityService = new SOSIdentityService(dbItemIamIdentityServiceEntry);

                switch (sosIdentityService.getIdentyServiceType()) {
                case LDAP:
                case LDAP_JOC:
                    sosLogin = new SOSLdapLogin();
                    LOGGER.debug("Login with identity service ldap");
                    break;
                case JOC:
                    sosLogin = new SOSInternAuthLogin();
                    LOGGER.debug("Login with identity service sosintern");
                    break;
                default:
                    sosLogin = new SOSInternAuthLogin();
                    LOGGER.debug("Login with identity service sosintern");
                }

                sosLogin.setIdentityService(sosIdentityService);
                sosLogin.simulateLogin(currentAccount.getAccountname());

                ISOSAuthSubject sosAuthSubject = sosLogin.getCurrentSubject();

                currentAccount.setCurrentSubject(sosAuthSubject);
                currentAccount.setIdentityService(new SOSIdentityService(dbItemIamIdentityServiceEntry.getId(), dbItemIamIdentityServiceEntry
                        .getIdentityServiceName(), sosIdentityService.getIdentyServiceType()));

                SecurityConfiguration securityConfiguration = sosPermissionMerger.addIdentityService(sosIdentityService);

                currentAccount.setRoles(securityConfiguration);
                //setOfAccountPermissions.addAll(currentAccount.getCurrentSubject().getListOfAccountPermissions());
            }

            SecurityConfiguration securityConfiguration = sosPermissionMerger.mergePermissions();
            SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(currentAccount);
            permissions = sosPermissionsCreator.createJocCockpitPermissionControllerObjectList(securityConfiguration);

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(permissions));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private void changePassword(SOSHibernateSession sosHibernateSession, boolean withPasswordCheck, AccountChangePassword account,
            DBItemIamIdentityService dbItemIamIdentityService) throws Exception {

        IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
        IamAccountFilter iamAccountFilter = new IamAccountFilter();

        if (IdentityServiceTypes.JOC.value().equals(dbItemIamIdentityService.getIdentityServiceType())) {

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
                } else {
                    dbItemIamAccount.setForcePasswordChange(account.getForcePasswordChange());
                }

                dbItemIamAccount.setAccountPassword(SOSPasswordHasher.hash(password));
                sosHibernateSession.update(dbItemIamAccount);

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
        AccountChangePassword account = null;
        try {
            AccountChangePassword accountMasked = Globals.objectMapper.readValue(body, AccountChangePassword.class);

            accountMasked.setOldPassword("********");
            accountMasked.setPassword("********");
            accountMasked.setRepeatedPassword("********");

            initLogging(API_CALL_CHANGE_PASSWORD, Globals.objectMapper.writeValueAsBytes(accountMasked), accessToken, CategoryType.IDENTITY);

            JsonValidator.validate(body, Account.class);
            account = Globals.objectMapper.readValue(body, AccountChangePassword.class);

            // TODO why permissions == true? better adminstration:accouts:manage or not?
            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_CHANGE_PASSWORD);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, account
                    .getIdentityServiceName());

            changePassword(sosHibernateSession, true, account, dbItemIamIdentityService);
            Globals.commit(sosHibernateSession);

            storeAuditLog(account.getAuditLog());

            return responseStatusJSOk(Date.from(Instant.now()));

        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            return responseStatusJSError(e);
        } finally {
            account = null;
            Globals.disconnect(sosHibernateSession);
        }
    }

    private JOCDefaultResponse changeFlag(String accessToken, byte[] body, Boolean disable, Boolean forcePasswordChange, String apiCall) {
        SOSHibernateSession sosHibernateSession = null;
        AccountNamesFilter accountsFilter = null;
        try {

            body = initLogging(apiCall, body, accessToken, CategoryType.IDENTITY);
            JsonValidator.validate(body, AccountsFilter.class);
            accountsFilter = Globals.objectMapper.readValue(body, AccountNamesFilter.class);

            JOCDefaultResponse jocDefaultResponse = initManageAccountPermissions(accessToken);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(apiCall);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);
            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, accountsFilter
                    .getIdentityServiceName());

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter iamAccountFilter = new IamAccountFilter();

            if (IdentityServiceTypes.JOC.value().equals(dbItemIamIdentityService.getIdentityServiceType()) || IdentityServiceTypes.KEYCLOAK_JOC
                    .value().equals(dbItemIamIdentityService.getIdentityServiceType()) || IdentityServiceTypes.OIDC_JOC.value().equals(
                            dbItemIamIdentityService.getIdentityServiceType()) || IdentityServiceTypes.LDAP_JOC.value().equals(
                                    dbItemIamIdentityService.getIdentityServiceType()) || IdentityServiceTypes.FIDO.value().equals(
                                            dbItemIamIdentityService.getIdentityServiceType()) || IdentityServiceTypes.CERTIFICATE.value().equals(
                                                    dbItemIamIdentityService.getIdentityServiceType())) {

                iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
                for (String accountName : accountsFilter.getAccountNames()) {
                    iamAccountFilter.setAccountName(accountName);
                    DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(iamAccountFilter);
                    if (dbItemIamAccount != null) {
                        if (disable != null) {
                            dbItemIamAccount.setDisabled(disable);
                        }
                        if (forcePasswordChange != null) {
                            if (IdentityServiceTypes.JOC.value().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                                dbItemIamAccount.setForcePasswordChange(forcePasswordChange);
                            }
                        }
                        sosHibernateSession.update(dbItemIamAccount);
                    } else {
                        JocError error = new JocError();
                        error.setMessage("Unknown account:" + accountName);
                        throw new JocInfoException(error);
                    }
                }
            }
            storeAuditLog(accountsFilter.getAuditLog());
            Globals.commit(sosHibernateSession);

            return responseStatusJSOk(Date.from(Instant.now()));

        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            return responseStatusJSError(e);

        } finally {
            accountsFilter = null;
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse forcePasswordChange(String accessToken, byte[] body) {
        return changeFlag(accessToken, body, null, true, API_CALL_FORCE_PASSWORD_CHANGE);
    }

    @Override
    public JOCDefaultResponse enable(String accessToken, byte[] body) {
        return changeFlag(accessToken, body, false, null, API_CALL_ACCOUNTS_ENABLE);
    }

    @Override
    public JOCDefaultResponse disable(String accessToken, byte[] body) {
        return changeFlag(accessToken, body, true, null, API_CALL_ACCOUNTS_DISABLE);
    }

    @Override
    public JOCDefaultResponse resetPassword(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        AccountNamesFilter accountsFilter = null;
        try {

            body = initLogging(API_CALL_RESET_PASSWORD, body, accessToken, CategoryType.IDENTITY);
            JsonValidator.validate(body, AccountsFilter.class);
            accountsFilter = Globals.objectMapper.readValue(body, AccountNamesFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_RESET_PASSWORD);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, accountsFilter
                    .getIdentityServiceName());
            AccountChangePassword account = new AccountChangePassword();
            account.setPassword(null);
            account.setIdentityServiceName(accountsFilter.getIdentityServiceName());

            for (String accountName : accountsFilter.getAccountNames()) {
                account.setAccountName(accountName);
                changePassword(sosHibernateSession, false, account, dbItemIamIdentityService);
            }

            Globals.commit(sosHibernateSession);

            storeAuditLog(accountsFilter.getAuditLog());

            return responseStatusJSOk(Date.from(Instant.now()));

        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            return responseStatusJSError(e);
        } finally {
            accountsFilter = null;
            Globals.disconnect(sosHibernateSession);
        }
    }

}