
package com.sos.joc.model.publish;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * deploy
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerIds",
    "store",
    "delete",
    "auditLog"
})
public class DeployFilter {

    @JsonProperty("controllerIds")
    private List<String> controllerIds = new ArrayList<String>();
    /**
     * Filter for Deployable Objects
     * <p>
     * 
     * 
     */
    @JsonProperty("store")
    private DeployablesValidFilter store;
    /**
     * Filter for Deploy-delete operation
     * <p>
     * 
     * 
     */
    @JsonProperty("delete")
    private DeployDeleteFilter delete;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    @JsonProperty("controllerIds")
    public List<String> getControllerIds() {
        return controllerIds;
    }

    @JsonProperty("controllerIds")
    public void setControllerIds(List<String> controllerIds) {
        this.controllerIds = controllerIds;
    }

    /**
     * Filter for Deployable Objects
     * <p>
     * 
     * 
     */
    @JsonProperty("store")
    public DeployablesValidFilter getStore() {
        return store;
    }

    /**
     * Filter for Deployable Objects
     * <p>
     * 
     * 
     */
    @JsonProperty("store")
    public void setStore(DeployablesValidFilter store) {
        this.store = store;
    }

    /**
     * Filter for Deploy-delete operation
     * <p>
     * 
     * 
     */
    @JsonProperty("delete")
    public DeployDeleteFilter getDelete() {
        return delete;
    }

    /**
     * Filter for Deploy-delete operation
     * <p>
     * 
     * 
     */
    @JsonProperty("delete")
    public void setDelete(DeployDeleteFilter delete) {
        this.delete = delete;
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
        return new ToStringBuilder(this).append("controllerIds", controllerIds).append("store", store).append("delete", delete).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(store).append(auditLog).append(delete).append(controllerIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployFilter) == false) {
            return false;
        }
        DeployFilter rhs = ((DeployFilter) other);
        return new EqualsBuilder().append(store, rhs.store).append(auditLog, rhs.auditLog).append(delete, rhs.delete).append(controllerIds, rhs.controllerIds).isEquals();
    }

}
