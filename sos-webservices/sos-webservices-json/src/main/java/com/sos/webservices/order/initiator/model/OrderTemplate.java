
package com.sos.webservices.order.initiator.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.IJSObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Order Template
 * <p>
 * The order template for scheduling orders to JobScheduler
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "orderTemplatePath",
    "workflowPath",
    "submitOrderToControllerWhenPlanned",
    "planOrderAutomatically",
    "calendars",
    "nonWorkingCalendars",
    "variables"
})
public class OrderTemplate implements IJSObject
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderTemplatePath")
    private String orderTemplatePath;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    private String workflowPath;
    @JsonProperty("submitOrderToControllerWhenPlanned")
    private Boolean submitOrderToControllerWhenPlanned;
    @JsonProperty("planOrderAutomatically")
    private Boolean planOrderAutomatically;
    /**
     * Assigned Calendars
     * <p>
     * 
     * 
     */
    @JsonProperty("calendars")
    private List<AssignedCalendars> calendars = null;
    /**
     * Assigned Non Working Calendars List
     * <p>
     * 
     * 
     */
    @JsonProperty("nonWorkingCalendars")
    private List<AssignedNonWorkingCalendars> nonWorkingCalendars = null;
    /**
     * params or environment variables
     * <p>
     * 
     * 
     */
    @JsonProperty("variables")
    private List<NameValuePair> variables = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderTemplatePath")
    public String getOrderTemplatePath() {
        return orderTemplatePath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderTemplatePath")
    public void setOrderTemplatePath(String orderTemplatePath) {
        this.orderTemplatePath = orderTemplatePath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    public String getWorkflowPath() {
        return workflowPath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    public void setWorkflowPath(String workflowPath) {
        this.workflowPath = workflowPath;
    }

    @JsonProperty("submitOrderToControllerWhenPlanned")
    public Boolean getSubmitOrderToControllerWhenPlanned() {
        return submitOrderToControllerWhenPlanned;
    }

    @JsonProperty("submitOrderToControllerWhenPlanned")
    public void setSubmitOrderToControllerWhenPlanned(Boolean submitOrderToControllerWhenPlanned) {
        this.submitOrderToControllerWhenPlanned = submitOrderToControllerWhenPlanned;
    }

    @JsonProperty("planOrderAutomatically")
    public Boolean getPlanOrderAutomatically() {
        return planOrderAutomatically;
    }

    @JsonProperty("planOrderAutomatically")
    public void setPlanOrderAutomatically(Boolean planOrderAutomatically) {
        this.planOrderAutomatically = planOrderAutomatically;
    }

    /**
     * Assigned Calendars
     * <p>
     * 
     * 
     */
    @JsonProperty("calendars")
    public List<AssignedCalendars> getCalendars() {
        return calendars;
    }

    /**
     * Assigned Calendars
     * <p>
     * 
     * 
     */
    @JsonProperty("calendars")
    public void setCalendars(List<AssignedCalendars> calendars) {
        this.calendars = calendars;
    }

    /**
     * Assigned Non Working Calendars List
     * <p>
     * 
     * 
     */
    @JsonProperty("nonWorkingCalendars")
    public List<AssignedNonWorkingCalendars> getNonWorkingCalendars() {
        return nonWorkingCalendars;
    }

    /**
     * Assigned Non Working Calendars List
     * <p>
     * 
     * 
     */
    @JsonProperty("nonWorkingCalendars")
    public void setNonWorkingCalendars(List<AssignedNonWorkingCalendars> nonWorkingCalendars) {
        this.nonWorkingCalendars = nonWorkingCalendars;
    }

    /**
     * params or environment variables
     * <p>
     * 
     * 
     */
    @JsonProperty("variables")
    public List<NameValuePair> getVariables() {
        return variables;
    }

    /**
     * params or environment variables
     * <p>
     * 
     * 
     */
    @JsonProperty("variables")
    public void setVariables(List<NameValuePair> variables) {
        this.variables = variables;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("orderTemplatePath", orderTemplatePath).append("workflowPath", workflowPath).append("submitOrderToControllerWhenPlanned", submitOrderToControllerWhenPlanned).append("planOrderAutomatically", planOrderAutomatically).append("calendars", calendars).append("nonWorkingCalendars", nonWorkingCalendars).append("variables", variables).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(planOrderAutomatically).append(variables).append(nonWorkingCalendars).append(controllerId).append(orderTemplatePath).append(workflowPath).append(calendars).append(submitOrderToControllerWhenPlanned).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderTemplate) == false) {
            return false;
        }
        OrderTemplate rhs = ((OrderTemplate) other);
        return new EqualsBuilder().append(planOrderAutomatically, rhs.planOrderAutomatically).append(variables, rhs.variables).append(nonWorkingCalendars, rhs.nonWorkingCalendars).append(controllerId, rhs.controllerId).append(orderTemplatePath, rhs.orderTemplatePath).append(workflowPath, rhs.workflowPath).append(calendars, rhs.calendars).append(submitOrderToControllerWhenPlanned, rhs.submitOrderToControllerWhenPlanned).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
