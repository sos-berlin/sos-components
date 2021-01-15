
package com.sos.webservices.order.initiator.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.common.IReleaseObject;
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
    "path",
    "workflowPath",
    "title",
    "documentationPath",
    "submitOrderToControllerWhenPlanned",
    "planOrderAutomatically",
    "calendars",
    "nonWorkingCalendars",
    "variables"
})
public class Schedule implements IConfigurationObject, IReleaseObject
{

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of an object.")
    private String path;
    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    @JsonPropertyDescription("absolute path of an object.")
    private String workflowPath;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    private String title;
    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("documentationPath")
    @JsonPropertyDescription("absolute path of an object.")
    private String documentationPath;
    @JsonProperty("submitOrderToControllerWhenPlanned")
    private Boolean submitOrderToControllerWhenPlanned;
    @JsonProperty("planOrderAutomatically")
    private Boolean planOrderAutomatically;
    /**
     * Assigned Calendars
     * <p>
     * 
     * (Required)
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

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    public String getWorkflowPath() {
        return workflowPath;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    public void setWorkflowPath(String workflowPath) {
        this.workflowPath = workflowPath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("documentationPath")
    public String getDocumentationPath() {
        return documentationPath;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("documentationPath")
    public void setDocumentationPath(String documentationPath) {
        this.documentationPath = documentationPath;
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
     * (Required)
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
     * (Required)
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("path", path).append("workflowPath", workflowPath).append("title", title).append("documentationPath", documentationPath).append("submitOrderToControllerWhenPlanned", submitOrderToControllerWhenPlanned).append("planOrderAutomatically", planOrderAutomatically).append("calendars", calendars).append("nonWorkingCalendars", nonWorkingCalendars).append("variables", variables).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(documentationPath).append(planOrderAutomatically).append(path).append(variables).append(nonWorkingCalendars).append(workflowPath).append(calendars).append(submitOrderToControllerWhenPlanned).append(title).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Schedule) == false) {
            return false;
        }
        Schedule rhs = ((Schedule) other);
        return new EqualsBuilder().append(documentationPath, rhs.documentationPath).append(planOrderAutomatically, rhs.planOrderAutomatically).append(path, rhs.path).append(variables, rhs.variables).append(nonWorkingCalendars, rhs.nonWorkingCalendars).append(workflowPath, rhs.workflowPath).append(calendars, rhs.calendars).append(submitOrderToControllerWhenPlanned, rhs.submitOrderToControllerWhenPlanned).append(title, rhs.title).isEquals();
    }

}
