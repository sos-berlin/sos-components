package com.sos.joc.classes.security;

import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.authentication.DBItemIamBlockedAccount;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.security.blocklist.BlockedAccount;
import com.sos.joc.model.security.blocklist.BlockedAccountsDeleteFilter;

public class SOSBlocklist {

    public static void store(SOSHibernateSession sosHibernateSession, BlockedAccount blockedAccount) throws SOSHibernateException {
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
        dbItemIamBlockedAccount.setComment(blockedAccount.getComment());
        dbItemIamBlockedAccount.setSince(new Date());
        if (newBlockedAccount) {
            sosHibernateSession.save(dbItemIamBlockedAccount);
        } else {
            sosHibernateSession.update(dbItemIamBlockedAccount);
        }

    }

    public static void delete(SOSHibernateSession sosHibernateSession, BlockedAccountsDeleteFilter blockedAccountsFilter)
            throws SOSHibernateException {
        IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
        IamAccountFilter iamAccountFilter = new IamAccountFilter();

        for (String accountName : blockedAccountsFilter.getAccountNames()) {
            iamAccountFilter.setAccountName(accountName);
            int count = iamAccountDBLayer.deleteBlockedAccount(iamAccountFilter);
            if (count == 0) {
                throw new JocObjectNotExistException("Object <" + accountName + "> not found");
            }
        }
    }
}
