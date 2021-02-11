
package com.sos.joc.model.dailyplan;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Daily Plan  Order Selector
 * <p>
 * To generate orders for the daily plan
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "dailyPlanDate",
    "overwrite",
    "withSubmit",
    "controllerIds",
    "selector",
    "auditLog"
})
public class DailyPlanOrderSelector {

    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dailyPlanDate")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    private String dailyPlanDate;
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
    @JsonProperty("controllerIds")
    private List<String> controllerIds = null;
    /**
     * Daily Plan  Order Filter Definition
     * <p>
     * Define the selector to generate orders for the daily plan
     * 
     */
    @JsonProperty("selector")
    @JsonPropertyDescription("Define the selector to generate orders for the daily plan")
    private DailyPlanOrderSelectorDef selector;
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
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dailyPlanDate")
    public String getDailyPlanDate() {
        return dailyPlanDate;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dailyPlanDate")
    public void setDailyPlanDate(String dailyPlanDate) {
        this.dailyPlanDate = dailyPlanDate;
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

    @JsonProperty("controllerIds")
    public List<String> getControllerIds() {
        return controllerIds;
    }

    @JsonProperty("controllerIds")
    public void setControllerIds(List<String> controllerIds) {
        this.controllerIds = controllerIds;
    }

    /**
     * Daily Plan  Order Filter Definition
     * <p>
     * Define the selector to generate orders for the daily plan
     * 
     */
    @JsonProperty("selector")
    public DailyPlanOrderSelectorDef getSelector() {
        return selector;
    }

    /**
     * Daily Plan  Order Filter Definition
     * <p>
     * Define the selector to generate orders for the daily plan
     * 
     */
    @JsonProperty("selector")
    public void setSelector(DailyPlanOrderSelectorDef selector) {
        this.selector = selector;
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("dailyPlanDate", dailyPlanDate).append("overwrite", overwrite).append("withSubmit", withSubmit).append("controllerIds", controllerIds).append("selector", selector).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dailyPlanDate).append(controllerId).append(auditLog).append(controllerIds).append(withSubmit).append(selector).append(overwrite).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DailyPlanOrderSelector) == false) {
            return false;
        }
        DailyPlanOrderSelector rhs = ((DailyPlanOrderSelector) other);
        return new EqualsBuilder().append(dailyPlanDate, rhs.dailyPlanDate).append(controllerId, rhs.controllerId).append(auditLog, rhs.auditLog).append(controllerIds, rhs.controllerIds).append(withSubmit, rhs.withSubmit).append(selector, rhs.selector).append(overwrite, rhs.overwrite).isEquals();
    }

}
