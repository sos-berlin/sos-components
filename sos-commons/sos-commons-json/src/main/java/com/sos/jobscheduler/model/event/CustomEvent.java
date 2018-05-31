
package com.sos.jobscheduler.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sos.jobscheduler.model.common.Variables;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * custom event
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "key",
    "eventId",
    "variables"
})
public class CustomEvent {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    @JacksonXmlProperty(localName = "TYPE")
    private String tYPE = "VariablesCustomEvent";
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("key")
    @JacksonXmlProperty(localName = "key")
    private String key;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("eventId")
    @JacksonXmlProperty(localName = "eventId")
    private Long eventId;
    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("variables")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    @JacksonXmlProperty(localName = "variables")
    private Variables variables;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    @JacksonXmlProperty(localName = "TYPE")
    public String getTYPE() {
        return tYPE;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    @JacksonXmlProperty(localName = "TYPE")
    public void setTYPE(String tYPE) {
        this.tYPE = tYPE;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("key")
    @JacksonXmlProperty(localName = "key")
    public String getKey() {
        return key;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("key")
    @JacksonXmlProperty(localName = "key")
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("eventId")
    @JacksonXmlProperty(localName = "eventId")
    public Long getEventId() {
        return eventId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("eventId")
    @JacksonXmlProperty(localName = "eventId")
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("variables")
    @JacksonXmlProperty(localName = "variables")
    public Variables getVariables() {
        return variables;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("variables")
    @JacksonXmlProperty(localName = "variables")
    public void setVariables(Variables variables) {
        this.variables = variables;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("key", key).append("eventId", eventId).append("variables", variables).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(eventId).append(variables).append(tYPE).append(key).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CustomEvent) == false) {
            return false;
        }
        CustomEvent rhs = ((CustomEvent) other);
        return new EqualsBuilder().append(eventId, rhs.eventId).append(variables, rhs.variables).append(tYPE, rhs.tYPE).append(key, rhs.key).isEquals();
    }

}
