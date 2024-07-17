
package com.sos.joc.model.inventory.release;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.inventory.common.RequestFilter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * release
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "update",
    "delete",
    "addOrdersDateFrom",
    "includeLate",
    "auditLog"
})
public class ReleaseFilter {

    @JsonProperty("update")
    private List<RequestFilter> update = new ArrayList<RequestFilter>();
    @JsonProperty("delete")
    private List<RequestFilter> delete = new ArrayList<RequestFilter>();
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("addOrdersDateFrom")
    private String addOrdersDateFrom;
    @JsonProperty("includeLate")
    private Boolean includeLate = false;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    @JsonProperty("update")
    public List<RequestFilter> getUpdate() {
        return update;
    }

    @JsonProperty("update")
    public void setUpdate(List<RequestFilter> update) {
        this.update = update;
    }

    @JsonProperty("delete")
    public List<RequestFilter> getDelete() {
        return delete;
    }

    @JsonProperty("delete")
    public void setDelete(List<RequestFilter> delete) {
        this.delete = delete;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("addOrdersDateFrom")
    public String getAddOrdersDateFrom() {
        return addOrdersDateFrom;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("addOrdersDateFrom")
    public void setAddOrdersDateFrom(String addOrdersDateFrom) {
        this.addOrdersDateFrom = addOrdersDateFrom;
    }

    @JsonProperty("includeLate")
    public Boolean getIncludeLate() {
        return includeLate;
    }

    @JsonProperty("includeLate")
    public void setIncludeLate(Boolean includeLate) {
        this.includeLate = includeLate;
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
        return new ToStringBuilder(this).append("update", update).append("delete", delete).append("addOrdersDateFrom", addOrdersDateFrom).append("includeLate", includeLate).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(update).append(includeLate).append(auditLog).append(delete).append(addOrdersDateFrom).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReleaseFilter) == false) {
            return false;
        }
        ReleaseFilter rhs = ((ReleaseFilter) other);
        return new EqualsBuilder().append(update, rhs.update).append(includeLate, rhs.includeLate).append(auditLog, rhs.auditLog).append(delete, rhs.delete).append(addOrdersDateFrom, rhs.addOrdersDateFrom).isEquals();
    }

}
