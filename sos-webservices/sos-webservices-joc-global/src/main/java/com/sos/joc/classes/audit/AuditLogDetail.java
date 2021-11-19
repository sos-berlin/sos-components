package com.sos.joc.classes.audit;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.sos.joc.db.joc.DBItemJocAuditLogDetails;
import com.sos.joc.model.audit.ObjectType;

public class AuditLogDetail {

    private Path path;
    private Integer type;
    private String orderId;
    private String controllerId;
    
    public AuditLogDetail(Path path, Integer type) {
        this.path = path;
        this.type = type;
        this.orderId = null;
        this.controllerId = null;
    }
    
    public AuditLogDetail(String path, Integer type) {
        this.path = Paths.get(path);
        this.type = type;
        this.orderId = null;
        this.controllerId = null;
    }
    
    public AuditLogDetail(Path path, Integer type, String controllerId) {
        this.path = path;
        this.type = type;
        this.orderId = null;
        this.controllerId = controllerId;
    }
    
    public AuditLogDetail(String path, Integer type, String controllerId) {
        this.path = Paths.get(path);
        this.type = type;
        this.orderId = null;
        this.controllerId = controllerId;
    }
    
    public AuditLogDetail(String workflowPath, String orderId, String controllerId) {
        this.path = Paths.get(workflowPath);
        this.type = ObjectType.ORDER.intValue();
        this.orderId = orderId;
        this.controllerId = controllerId;
    }

    public Path getPath() {
        return path;
    }

    public Integer getConfigurationType() {
        return type;
    }
    
    public String getControllerId() {
        return controllerId;
    }
    
    public void setControllerId(String val) {
        controllerId = val;
    }
    
    public boolean hasControllerId() {
        return controllerId != null && !controllerId.isEmpty();
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String val) {
        orderId = val;
    }
    
    public DBItemJocAuditLogDetails getAuditLogDetail(Long auditlogId, Date now) {
        if (path == null || type == null || auditlogId == null || ObjectType.FOLDER.intValue() == type) {
            return null;
        }
        if (now == null) {
            now = Date.from(Instant.now());
        }
        DBItemJocAuditLogDetails dbItem = new DBItemJocAuditLogDetails();
        dbItem.setId(null);
        dbItem.setPath(path.toString().replace('\\', '/'));
        dbItem.setName(path.getFileName().toString());
        try {
            dbItem.setFolder(path.getParent().toString().replace('\\', '/'));
        } catch (Exception e) {
            dbItem.setFolder("/");
        }
        dbItem.setOrderId(orderId);
        dbItem.setType(type);
        dbItem.setAuditLogId(auditlogId);
        dbItem.setCreated(now);
        return dbItem;
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(type).append(path.toString().replace('\\', '/')).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AuditLogDetail) == false) {
            return false;
        }
        AuditLogDetail rhs = ((AuditLogDetail) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(type, rhs.type).append(path, rhs.path).isEquals();
    }

}
