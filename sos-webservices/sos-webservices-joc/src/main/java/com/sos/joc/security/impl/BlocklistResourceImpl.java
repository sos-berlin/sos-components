package com.sos.joc.security.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.authentication.DBItemIamBlockedAccount;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.security.accounts.AccountListFilter;
import com.sos.joc.model.security.blocklist.BlockedAccount;
import com.sos.joc.model.security.blocklist.BlockedAccounts;
import com.sos.joc.model.security.blocklist.BlockedAccountsDeleteFilter;
import com.sos.joc.model.security.blocklist.BlockedAccountsFilter;
import com.sos.joc.security.resource.IBlocklistResource;
import com.sos.schema.JsonValidator;

@Path("iam")
public class BlocklistResourceImpl extends JOCResourceImpl implements IBlocklistResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlocklistResourceImpl.class);

    private static final String API_CALL_BLOCKLISTS = "./iam//blockedAccounts";
    private static final String API_CALL_BLOCKLISTS_DELETE = "./iam/blockedAccounts/delete";
    private static final String API_CALL_BLOCKLIST_STORE = "./iam/blockedAccount/store";

    @Override
    public JOCDefaultResponse postBlockedAccountStore(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            BlockedAccount blockedAccount = Globals.objectMapper.readValue(body, BlockedAccount.class);

            initLogging(API_CALL_BLOCKLIST_STORE, Globals.objectMapper.writeValueAsBytes(blockedAccount), accessToken);
            JsonValidator.validateFailFast(body, BlockedAccount.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_BLOCKLIST_STORE);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);

            IamAccountFilter iamAccountFilter = new IamAccountFilter();
            iamAccountFilter.setAccountName(blockedAccount.getAccountName());
            DBItemIamBlockedAccount dbItemIamBlockedAccount = iamAccountDBLayer.getBlockedAccount(iamAccountFilter);
            boolean newBlockedAccount = false;
            if (dbItemIamBlockedAccount == null) {
                dbItemIamBlockedAccount = new DBItemIamBlockedAccount();
                newBlockedAccount = true;
            }

            dbItemIamBlockedAccount.setAccountName(blockedAccount.getAccountName());
            dbItemIamBlockedAccount.setNotice(blockedAccount.getNotice());
            dbItemIamBlockedAccount.setSince(new Date());
            if (newBlockedAccount) {
                sosHibernateSession.save(dbItemIamBlockedAccount);
            } else {
                sosHibernateSession.update(dbItemIamBlockedAccount);
            }

            storeAuditLog(blockedAccount.getAuditLog(), CategoryType.IDENTITY);
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
    public JOCDefaultResponse postBlockedAccountsDelete(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;

        try {
            initLogging(API_CALL_BLOCKLISTS_DELETE, body, accessToken);
            JsonValidator.validate(body, BlockedAccountsDeleteFilter.class);
            BlockedAccountsDeleteFilter blockedAccountsFilter = Globals.objectMapper.readValue(body, BlockedAccountsDeleteFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_BLOCKLISTS_DELETE);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter iamAccountFilter = new IamAccountFilter();

            for (String accountName : blockedAccountsFilter.getAccountNames()) {
                iamAccountFilter.setAccountName(accountName);
                int count = iamAccountDBLayer.deleteBlockedAccount(iamAccountFilter);
                if (count == 0) {
                    throw new JocObjectNotExistException("Object <" + accountName + "> not found");
                }
            }
            Globals.commit(sosHibernateSession);

            storeAuditLog(blockedAccountsFilter.getAuditLog(), CategoryType.IDENTITY);

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
    public JOCDefaultResponse postBlockedAccounts(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            initLogging(API_CALL_BLOCKLISTS, body, accessToken);
            JsonValidator.validateFailFast(body, AccountListFilter.class);
            BlockedAccountsFilter blocklistFilter = Globals.objectMapper.readValue(body, BlockedAccountsFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_BLOCKLISTS);

            BlockedAccounts blockedAccounts = new BlockedAccounts();

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            
            IamAccountFilter filter = new IamAccountFilter();
            filter.setDateFrom(blocklistFilter.getDateFrom());
            filter.setDateTo(blocklistFilter.getDateTo());
            filter.setAccountName(blocklistFilter.getAccountName());
            filter.setTimeZone(blocklistFilter.getTimeZone());
            
            List<DBItemIamBlockedAccount> listOfBlockedAccounts = iamAccountDBLayer.getIamBlockedAccountList(filter, 0);
            for (DBItemIamBlockedAccount dbItemIamBlockedAccount : listOfBlockedAccounts) {
                BlockedAccount blockedAccount = new BlockedAccount();
                blockedAccount.setAccountName(dbItemIamBlockedAccount.getAccountName());
                blockedAccount.setNotice(dbItemIamBlockedAccount.getNotice());
                blockedAccount.setSince(dbItemIamBlockedAccount.getSince());
                blockedAccounts.getBlockedAccounts().add(blockedAccount);
            }

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(blockedAccounts));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

}