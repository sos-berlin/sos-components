package com.sos.joc.db.audit;

import com.sos.commons.util.SOSDate;
import com.sos.joc.model.audit.AuditLogItem;
import com.sos.joc.model.audit.CategoryType;

public class AuditLogDBItem extends AuditLogItem {

    public void setCategory(Integer val) {
        try {
            super.setCategory(CategoryType.fromValue(val));
        } catch (Throwable e) {
        }
    }
    
    public void setCreated(Object created) {
        super.setCreated(SOSDate.toDate(created));
    }
}
