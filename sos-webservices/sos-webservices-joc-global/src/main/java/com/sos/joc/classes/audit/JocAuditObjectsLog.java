package com.sos.joc.classes.audit;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.db.joc.DBItemJocAuditLogDetails;
import com.sos.joc.model.audit.ObjectType;

public class JocAuditObjectsLog {
    
    private static final Logger AUDIT_OBJECTS_LOGGER = LoggerFactory.getLogger(WebserviceConstants.AUDIT_OBJECTS_LOGGER);
    private Long auditLogId;
    private Set<DBItemJocAuditLogDetails> details = null;
    
    public JocAuditObjectsLog(Long auditLogId) {
        this.auditLogId = auditLogId;
    }
    
    public void addDetail(DBItemJocAuditLogDetails detail) {
        if (details == null) {
            details = new HashSet<>();
        }
        details.add(detail);
    }
    
    public void log() {
        if (details == null) {
            JocAuditObjectsLog.log(Stream.empty(), auditLogId);
        } else {
            JocAuditObjectsLog.log(details.stream(), auditLogId);
        }
    }
    
    public static void log(DBItemJocAuditLogDetails detail, Long auditlogId) {
        if (detail != null) {
            log(Collections.singleton(detail).stream(), auditlogId);
        }
    }

    public static void log(Stream<DBItemJocAuditLogDetails> details, Long auditlogId) {
        AUDIT_OBJECTS_LOGGER.info(details.map(JocAuditObjectsLog::toLogString).filter(Objects::nonNull).collect(Collectors.joining(",",
                "{\"id\"=" + auditlogId + ",\"objects\"=[", "]}")));
    }
    
    private static String toLogString(DBItemJocAuditLogDetails dbItem) {
        if (dbItem == null || dbItem.getName() == null || dbItem.getType() == null || ObjectType.FOLDER.intValue() == dbItem.getType()) {
            return null;
        }
        try {
            ObjectType oType = ObjectType.fromValue(dbItem.getType());
            switch (oType) {
            case ORDER:
                return String.format("{\"order\"=\"%s\",\"workflow\"=\"%s\"}", dbItem.getOrderId(), dbItem.getName());
            default:
                return String.format("{\"%s\"=\"%s\"}", oType.value().toLowerCase(), dbItem.getName());
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    

}
