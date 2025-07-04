package com.sos.joc.security.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.security.SOSBlocklist;
import com.sos.joc.db.authentication.DBItemIamBlockedAccount;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.security.blocklist.BlockedAccount;
import com.sos.joc.model.security.blocklist.BlockedAccounts;
import com.sos.joc.model.security.blocklist.BlockedAccountsDeleteFilter;
import com.sos.joc.model.security.blocklist.BlockedAccountsFilter;
import com.sos.joc.security.resource.IBlocklistResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("iam")
public class BlocklistResourceImpl extends JOCResourceImpl implements IBlocklistResource {

    private static final String API_CALL_BLOCKLISTS = "./iam//blockedAccounts";
    private static final String API_CALL_BLOCKLISTS_DELETE = "./iam/blockedAccounts/delete";
    private static final String API_CALL_BLOCKLIST_STORE = "./iam/blockedAccount/store";

    @Override
    public JOCDefaultResponse postBlockedAccountStore(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            body = initLogging(API_CALL_BLOCKLIST_STORE, body, accessToken, CategoryType.IDENTITY);
            BlockedAccount blockedAccount = Globals.objectMapper.readValue(body, BlockedAccount.class);
            JsonValidator.validateFailFast(body, BlockedAccount.class);

            JOCDefaultResponse jocDefaultResponse = initManageAccountPermissions(accessToken);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_BLOCKLIST_STORE);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();

            SOSBlocklist.store(sosHibernateSession, blockedAccount);

            storeAuditLog(blockedAccount.getAuditLog());
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
    public JOCDefaultResponse postBlockedAccountsDelete(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;

        try {
            body = initLogging(API_CALL_BLOCKLISTS_DELETE, body, accessToken, CategoryType.IDENTITY);
            JsonValidator.validate(body, BlockedAccountsDeleteFilter.class);
            BlockedAccountsDeleteFilter blockedAccountsDeleteFilter = Globals.objectMapper.readValue(body, BlockedAccountsDeleteFilter.class);

            JOCDefaultResponse jocDefaultResponse = initManageAccountPermissions(accessToken);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_BLOCKLISTS_DELETE);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            SOSBlocklist.delete(sosHibernateSession, blockedAccountsDeleteFilter);

            Globals.commit(sosHibernateSession);

            storeAuditLog(blockedAccountsDeleteFilter.getAuditLog());

            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postBlockedAccounts(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            body = initLogging(API_CALL_BLOCKLISTS, body, accessToken, CategoryType.IDENTITY);
            JsonValidator.validateFailFast(body, BlockedAccountsFilter.class);
            BlockedAccountsFilter blocklistFilter = Globals.objectMapper.readValue(body, BlockedAccountsFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getBasicJocPermissions(accessToken).getAdministration().getAccounts()
                    .getView());
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
                blockedAccount.setComment(dbItemIamBlockedAccount.getComment());
                blockedAccount.setSince(dbItemIamBlockedAccount.getSince());
                blockedAccounts.getBlockedAccounts().add(blockedAccount);
            }

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(blockedAccounts));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

}