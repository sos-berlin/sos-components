
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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * add order
 * <p>
 * The order template for scheduling orders to JobScheduler
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "orderTemplateName",
    "templateId",
    "workflowPath",
    "submitOrders",
    "calendars",
    "nonWorkingCalendars",
    "variables"
})
public class OrderTemplate {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderTemplateName")
    private String orderTemplateName;
    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("templateId")
    private Long templateId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    private String workflowPath;
    @JsonProperty("submitOrders")
    private Boolean submitOrders;
    /**
     * Assigned Calendars List
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
    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderTemplateName")
    public String getOrderTemplateName() {
        return orderTemplateName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderTemplateName")
    public void setOrderTemplateName(String orderTemplateName) {
        this.orderTemplateName = orderTemplateName;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("templateId")
    public Long getTemplateId() {
        return templateId;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("templateId")
    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
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

    @JsonProperty("submitOrders")
    public Boolean getSubmitOrders() {
        return submitOrders;
    }

    @JsonProperty("submitOrders")
    public void setSubmitOrders(Boolean submitOrders) {
        this.submitOrders = submitOrders;
    }

    /**
     * Assigned Calendars List
     * <p>
     * 
     * 
     */
    @JsonProperty("calendars")
    public List<AssignedCalendars> getCalendars() {
        return calendars;
    }

    /**
     * Assigned Calendars List
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
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("orderTemplateName", orderTemplateName).append("templateId", templateId).append("workflowPath", workflowPath).append("submitOrders", submitOrders).append("calendars", calendars).append("nonWorkingCalendars", nonWorkingCalendars).append("variables", variables).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(variables).append(nonWorkingCalendars).append(orderTemplateName).append(workflowPath).append(calendars).append(submitOrders).append(additionalProperties).append(jobschedulerId).append(templateId).toHashCode();
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
        return new EqualsBuilder().append(variables, rhs.variables).append(nonWorkingCalendars, rhs.nonWorkingCalendars).append(orderTemplateName, rhs.orderTemplateName).append(workflowPath, rhs.workflowPath).append(calendars, rhs.calendars).append(submitOrders, rhs.submitOrders).append(additionalProperties, rhs.additionalProperties).append(jobschedulerId, rhs.jobschedulerId).append(templateId, rhs.templateId).isEquals();
    }

}
