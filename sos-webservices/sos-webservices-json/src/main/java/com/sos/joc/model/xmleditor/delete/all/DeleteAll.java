
package com.sos.joc.model.xmleditor.delete.all;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.xmleditor.common.ObjectType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * xmleditor delete all configurations in
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "objectTypes",
    "auditLog"
})
public class DeleteAll {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectTypes")
    private List<ObjectType> objectTypes = new ArrayList<ObjectType>();
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectTypes")
    public List<ObjectType> getObjectTypes() {
        return objectTypes;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectTypes")
    public void setObjectTypes(List<ObjectType> objectTypes) {
        this.objectTypes = objectTypes;
    }

    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    public AuditParams getAuditLog() {
        return auditLog;
    }

    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    public void setAuditLog(AuditParams auditLog) {
        this.auditLog = auditLog;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("objectTypes", objectTypes).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(objectTypes).append(auditLog).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeleteAll) == false) {
            return false;
        }
        DeleteAll rhs = ((DeleteAll) other);
        return new EqualsBuilder().append(objectTypes, rhs.objectTypes).append(auditLog, rhs.auditLog).isEquals();
    }

}
