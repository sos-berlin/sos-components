
package com.sos.inventory.model.schedule;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.AssignedNonWorkingDayCalendars;
import com.sos.inventory.model.common.IInventoryObject;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.common.IReleaseObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Schedule
 * <p>
 * The order template for scheduling orders to Controller. workflowName is deprecated
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "version",
    "path",
    "workflowName",
    "workflowNames",
    "title",
    "documentationName",
    "submitOrderToControllerWhenPlanned",
    "planOrderAutomatically",
    "calendars",
    "nonWorkingDayCalendars",
    "orderParameterisations"
})
public class Schedule implements IInventoryObject, IConfigurationObject, IReleaseObject
{

    /**
     * inventory repository version
     * <p>
     * inventory repository version
     * 
     */
    @JsonProperty("version")
    @JsonPropertyDescription("inventory repository version")
    private String version = "1.4.0";
    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of an object.")
    private String path;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowName")
    private String workflowName;
    @JsonProperty("workflowNames")
    private List<String> workflowNames = null;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    private String title;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationName")
    private String documentationName;
    @JsonProperty("submitOrderToControllerWhenPlanned")
    private Boolean submitOrderToControllerWhenPlanned = false;
    @JsonProperty("planOrderAutomatically")
    private Boolean planOrderAutomatically = false;
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
    @JsonProperty("nonWorkingDayCalendars")
    private List<AssignedNonWorkingDayCalendars> nonWorkingDayCalendars = null;
    @JsonProperty("orderParameterisations")
    @JsonAlias({
        "variableSets"
    })
    private List<OrderParameterisation> orderParameterisations = null;

    /**
     * inventory repository version
     * <p>
     * inventory repository version
     * 
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * inventory repository version
     * <p>
     * inventory repository version
     * 
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
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
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowName")
    public String getWorkflowName() {
        return workflowName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowName")
    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    @JsonProperty("workflowNames")
    public List<String> getWorkflowNames() {
        return workflowNames;
    }

    @JsonProperty("workflowNames")
    public void setWorkflowNames(List<String> workflowNames) {
        this.workflowNames = workflowNames;
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
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationName")
    public String getDocumentationName() {
        return documentationName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationName")
    public void setDocumentationName(String documentationName) {
        this.documentationName = documentationName;
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
    @JsonProperty("nonWorkingDayCalendars")
    public List<AssignedNonWorkingDayCalendars> getNonWorkingDayCalendars() {
        return nonWorkingDayCalendars;
    }

    /**
     * Assigned Non Working Calendars List
     * <p>
     * 
     * 
     */
    @JsonProperty("nonWorkingDayCalendars")
    public void setNonWorkingDayCalendars(List<AssignedNonWorkingDayCalendars> nonWorkingDayCalendars) {
        this.nonWorkingDayCalendars = nonWorkingDayCalendars;
    }

    @JsonProperty("orderParameterisations")
    public List<OrderParameterisation> getOrderParameterisations() {
        return orderParameterisations;
    }

    @JsonProperty("orderParameterisations")
    public void setOrderParameterisations(List<OrderParameterisation> orderParameterisations) {
        this.orderParameterisations = orderParameterisations;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("version", version).append("path", path).append("workflowName", workflowName).append("workflowNames", workflowNames).append("title", title).append("documentationName", documentationName).append("submitOrderToControllerWhenPlanned", submitOrderToControllerWhenPlanned).append("planOrderAutomatically", planOrderAutomatically).append("calendars", calendars).append("nonWorkingDayCalendars", nonWorkingDayCalendars).append("orderParameterisations", orderParameterisations).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(workflowNames).append(planOrderAutomatically).append(path).append(calendars).append(submitOrderToControllerWhenPlanned).append(nonWorkingDayCalendars).append(workflowName).append(documentationName).append(title).append(orderParameterisations).append(version).toHashCode();
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
        return new EqualsBuilder().append(workflowNames, rhs.workflowNames).append(planOrderAutomatically, rhs.planOrderAutomatically).append(path, rhs.path).append(calendars, rhs.calendars).append(submitOrderToControllerWhenPlanned, rhs.submitOrderToControllerWhenPlanned).append(nonWorkingDayCalendars, rhs.nonWorkingDayCalendars).append(workflowName, rhs.workflowName).append(documentationName, rhs.documentationName).append(title, rhs.title).append(orderParameterisations, rhs.orderParameterisations).append(version, rhs.version).isEquals();
    }

}
