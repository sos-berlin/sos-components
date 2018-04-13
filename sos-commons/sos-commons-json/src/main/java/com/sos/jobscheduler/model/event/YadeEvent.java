
package com.sos.jobscheduler.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * jobscheduler custom event for YADE
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
public class YadeEvent {

    @JsonProperty("TYPE")
    @JacksonXmlProperty(localName = "TYPE")
    private String tYPE = "VariablesCustomEvent";
    /**
     * YADETransferStarted, YADETransferFinished, YADEFileStateChanged
     * 
     */
    @JsonProperty("key")
    @JsonPropertyDescription("YADETransferStarted, YADETransferFinished, YADEFileStateChanged")
    @JacksonXmlProperty(localName = "key")
    private String key;
    @JsonProperty("eventId")
    @JacksonXmlProperty(localName = "eventId")
    private String eventId;
    @JsonProperty("variables")
    @JacksonXmlProperty(localName = "variables")
    private YadeVariables variables;

    @JsonProperty("TYPE")
    @JacksonXmlProperty(localName = "TYPE")
    public String getTYPE() {
        return tYPE;
    }

    @JsonProperty("TYPE")
    @JacksonXmlProperty(localName = "TYPE")
    public void setTYPE(String tYPE) {
        this.tYPE = tYPE;
    }

    /**
     * YADETransferStarted, YADETransferFinished, YADEFileStateChanged
     * 
     */
    @JsonProperty("key")
    @JacksonXmlProperty(localName = "key")
    public String getKey() {
        return key;
    }

    /**
     * YADETransferStarted, YADETransferFinished, YADEFileStateChanged
     * 
     */
    @JsonProperty("key")
    @JacksonXmlProperty(localName = "key")
    public void setKey(String key) {
        this.key = key;
    }

    @JsonProperty("eventId")
    @JacksonXmlProperty(localName = "eventId")
    public String getEventId() {
        return eventId;
    }

    @JsonProperty("eventId")
    @JacksonXmlProperty(localName = "eventId")
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @JsonProperty("variables")
    @JacksonXmlProperty(localName = "variables")
    public YadeVariables getVariables() {
        return variables;
    }

    @JsonProperty("variables")
    @JacksonXmlProperty(localName = "variables")
    public void setVariables(YadeVariables variables) {
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
        if ((other instanceof YadeEvent) == false) {
            return false;
        }
        YadeEvent rhs = ((YadeEvent) other);
        return new EqualsBuilder().append(eventId, rhs.eventId).append(variables, rhs.variables).append(tYPE, rhs.tYPE).append(key, rhs.key).isEquals();
    }

}
