
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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
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
    "schedulerId",
    "hostName",
    "port",
    "orderKey",
    "workflowPath",
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
    @JsonProperty("schedulerId")
    @JacksonXmlProperty(localName = "schedulerId")
    private String schedulerId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("hostName")
    @JacksonXmlProperty(localName = "hostName")
    private String hostName;
    /**
     * port
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("port")
    @JacksonXmlProperty(localName = "port")
    private Integer port;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderKey")
    @JacksonXmlProperty(localName = "orderKey")
    private String orderKey;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    @JacksonXmlProperty(localName = "workflowPath")
    private String workflowPath;
    /**
     * Assigned Calendars List
     * <p>
     * 
     * 
     */
    @JsonProperty("calendars")
    @JacksonXmlProperty(localName = "calendar")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "calendars")
    private List<AssignedCalendars> calendars = null;
    /**
     * Assigned Non Working Calendars List
     * <p>
     * 
     * 
     */
    @JsonProperty("nonWorkingCalendars")
    @JacksonXmlProperty(localName = "nonWorkingCalendar")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "nonWorkingCalendars")
    private List<AssignedNonWorkingCalendars> nonWorkingCalendars = null;
    /**
     * params or environment variables
     * <p>
     * 
     * 
     */
    @JsonProperty("variables")
    @JacksonXmlProperty(localName = "variable")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "variables")
    private List<NameValuePair> variables = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("schedulerId")
    @JacksonXmlProperty(localName = "schedulerId")
    public String getSchedulerId() {
        return schedulerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("schedulerId")
    @JacksonXmlProperty(localName = "schedulerId")
    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("hostName")
    @JacksonXmlProperty(localName = "hostName")
    public String getHostName() {
        return hostName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("hostName")
    @JacksonXmlProperty(localName = "hostName")
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * port
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("port")
    @JacksonXmlProperty(localName = "port")
    public Integer getPort() {
        return port;
    }

    /**
     * port
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("port")
    @JacksonXmlProperty(localName = "port")
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderKey")
    @JacksonXmlProperty(localName = "orderKey")
    public String getOrderKey() {
        return orderKey;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderKey")
    @JacksonXmlProperty(localName = "orderKey")
    public void setOrderKey(String orderKey) {
        this.orderKey = orderKey;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    @JacksonXmlProperty(localName = "workflowPath")
    public String getWorkflowPath() {
        return workflowPath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    @JacksonXmlProperty(localName = "workflowPath")
    public void setWorkflowPath(String workflowPath) {
        this.workflowPath = workflowPath;
    }

    /**
     * Assigned Calendars List
     * <p>
     * 
     * 
     */
    @JsonProperty("calendars")
    @JacksonXmlProperty(localName = "calendar")
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
    @JacksonXmlProperty(localName = "calendar")
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
    @JacksonXmlProperty(localName = "nonWorkingCalendar")
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
    @JacksonXmlProperty(localName = "nonWorkingCalendar")
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
    @JacksonXmlProperty(localName = "variable")
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
    @JacksonXmlProperty(localName = "variable")
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
        return new ToStringBuilder(this).append("schedulerId", schedulerId).append("hostName", hostName).append("port", port).append("orderKey", orderKey).append("workflowPath", workflowPath).append("calendars", calendars).append("nonWorkingCalendars", nonWorkingCalendars).append("variables", variables).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(orderKey).append(hostName).append(variables).append(nonWorkingCalendars).append(port).append(workflowPath).append(calendars).append(schedulerId).append(additionalProperties).toHashCode();
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
        return new EqualsBuilder().append(orderKey, rhs.orderKey).append(hostName, rhs.hostName).append(variables, rhs.variables).append(nonWorkingCalendars, rhs.nonWorkingCalendars).append(port, rhs.port).append(workflowPath, rhs.workflowPath).append(calendars, rhs.calendars).append(schedulerId, rhs.schedulerId).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
