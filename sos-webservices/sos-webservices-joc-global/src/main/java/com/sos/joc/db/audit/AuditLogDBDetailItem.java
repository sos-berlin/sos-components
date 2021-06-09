package com.sos.joc.db.audit;

import com.sos.joc.model.audit.AuditLogDetailItem;
import com.sos.joc.model.audit.ObjectType;

public class AuditLogDBDetailItem extends AuditLogDetailItem {
    
    public AuditLogDBDetailItem(String path, Integer type, String orderId) {
        if (orderId == null) {
            setPath(path);
        } else {
            setPath(path + "," + orderId);
        }
        try {
            setType(ObjectType.fromValue(type));
        } catch (Exception e) {
        }
    }
    
    public AuditLogDBDetailItem(String path, Integer type) {
        setPath(path);
        try {
            setType(ObjectType.fromValue(type));
        } catch (Exception e) {
        }
    }
    
    public AuditLogDBDetailItem(String path, String orderId) {
        if (orderId == null) {
            setPath(path);
        } else {
            setPath(path + "," + orderId);
        }
        setType(ObjectType.ORDER);
    }

}
