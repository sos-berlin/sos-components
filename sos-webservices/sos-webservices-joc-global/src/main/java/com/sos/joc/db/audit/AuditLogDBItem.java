package com.sos.joc.db.audit;

import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.model.audit.AuditLogItem;

public class AuditLogDBItem extends AuditLogItem {
    
    public AuditLogDBItem(DBItemJocAuditLog dbItem, String commitId) {
        setId(dbItem.getId());
        setControllerId(dbItem.getControllerId());
        setAccount(dbItem.getAccount());
        setRequest(dbItem.getRequest());
        setParameters(dbItem.getParameters());
        setCategory(dbItem.getTypeAsEnum());
        setComment(dbItem.getComment());
        setCreated(dbItem.getCreated());
        setTicketLink(dbItem.getTicketLink());
        setTimeSpent(dbItem.getTimeSpent());
        setCommitId(commitId);
    }

}
