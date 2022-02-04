
package com.sos.joc.model.publish.repository;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.publish.Config;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Filter to Delete From Repository
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "configurations",
    "category",
    "auditLog"
})
public class DeleteFromFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("configurations")
    private List<Config> configurations = new ArrayList<Config>();
    /**
     * Repository Category, based on the local environment or environment independent/able to roll out
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("category")
    private Category category;
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
    @JsonProperty("configurations")
    public List<Config> getConfigurations() {
        return configurations;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("configurations")
    public void setConfigurations(List<Config> configurations) {
        this.configurations = configurations;
    }

    /**
     * Repository Category, based on the local environment or environment independent/able to roll out
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("category")
    public Category getCategory() {
        return category;
    }

    /**
     * Repository Category, based on the local environment or environment independent/able to roll out
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("category")
    public void setCategory(Category category) {
        this.category = category;
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
        return new ToStringBuilder(this).append("configurations", configurations).append("category", category).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(category).append(auditLog).append(configurations).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeleteFromFilter) == false) {
            return false;
        }
        DeleteFromFilter rhs = ((DeleteFromFilter) other);
        return new EqualsBuilder().append(category, rhs.category).append(auditLog, rhs.auditLog).append(configurations, rhs.configurations).isEquals();
    }

}
