
package com.sos.joc.model.dailyplan;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Daily Plan  Order Filter
 * <p>
 * To get orders from the daily plan
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "overwrite",
    "withSubmit",
    "expandCycleOrders",
    "filter",
    "auditLog"
})
public class DailyPlanOrderFilter {

    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * overwrite parameter
     * <p>
     * controls if the order should be overwritten
     * 
     */
    @JsonProperty("overwrite")
    @JsonPropertyDescription("controls if the order should be overwritten")
    private Boolean overwrite = false;
    /**
     * withSubmit parameter
     * <p>
     * controls if the order should be submitted to the controller
     * 
     */
    @JsonProperty("withSubmit")
    @JsonPropertyDescription("controls if the order should be submitted to the controller")
    private Boolean withSubmit = true;
    /**
     * expandCycleOrders parameter
     * <p>
     * controls if the cycle order should be expanded in the answer
     * 
     */
    @JsonProperty("expandCycleOrders")
    @JsonPropertyDescription("controls if the cycle order should be expanded in the answer")
    private Boolean expandCycleOrders = false;
    /**
     * Daily Plan  Order Filter Definition
     * <p>
     * Define the filter To get orders from the daily plan
     * 
     */
    @JsonProperty("filter")
    @JsonPropertyDescription("Define the filter To get orders from the daily plan")
    private DailyPlanOrderFilterDef filter;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * overwrite parameter
     * <p>
     * controls if the order should be overwritten
     * 
     */
    @JsonProperty("overwrite")
    public Boolean getOverwrite() {
        return overwrite;
    }

    /**
     * overwrite parameter
     * <p>
     * controls if the order should be overwritten
     * 
     */
    @JsonProperty("overwrite")
    public void setOverwrite(Boolean overwrite) {
        this.overwrite = overwrite;
    }

    /**
     * withSubmit parameter
     * <p>
     * controls if the order should be submitted to the controller
     * 
     */
    @JsonProperty("withSubmit")
    public Boolean getWithSubmit() {
        return withSubmit;
    }

    /**
     * withSubmit parameter
     * <p>
     * controls if the order should be submitted to the controller
     * 
     */
    @JsonProperty("withSubmit")
    public void setWithSubmit(Boolean withSubmit) {
        this.withSubmit = withSubmit;
    }

    /**
     * expandCycleOrders parameter
     * <p>
     * controls if the cycle order should be expanded in the answer
     * 
     */
    @JsonProperty("expandCycleOrders")
    public Boolean getExpandCycleOrders() {
        return expandCycleOrders;
    }

    /**
     * expandCycleOrders parameter
     * <p>
     * controls if the cycle order should be expanded in the answer
     * 
     */
    @JsonProperty("expandCycleOrders")
    public void setExpandCycleOrders(Boolean expandCycleOrders) {
        this.expandCycleOrders = expandCycleOrders;
    }

    /**
     * Daily Plan  Order Filter Definition
     * <p>
     * Define the filter To get orders from the daily plan
     * 
     */
    @JsonProperty("filter")
    public DailyPlanOrderFilterDef getFilter() {
        return filter;
    }

    /**
     * Daily Plan  Order Filter Definition
     * <p>
     * Define the filter To get orders from the daily plan
     * 
     */
    @JsonProperty("filter")
    public void setFilter(DailyPlanOrderFilterDef filter) {
        this.filter = filter;
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("overwrite", overwrite).append("withSubmit", withSubmit).append("expandCycleOrders", expandCycleOrders).append("filter", filter).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(filter).append(controllerId).append(auditLog).append(withSubmit).append(overwrite).append(expandCycleOrders).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DailyPlanOrderFilter) == false) {
            return false;
        }
        DailyPlanOrderFilter rhs = ((DailyPlanOrderFilter) other);
        return new EqualsBuilder().append(filter, rhs.filter).append(controllerId, rhs.controllerId).append(auditLog, rhs.auditLog).append(withSubmit, rhs.withSubmit).append(overwrite, rhs.overwrite).append(expandCycleOrders, rhs.expandCycleOrders).isEquals();
    }

}
