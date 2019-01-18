
package com.sos.jobscheduler.model.event;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "TYPE",
    "key",
    "eventId",
    "variables"
})
public class YadeEvent {

    @JsonProperty("TYPE")
    private String tYPE = "VariablesCustomEvent";
    /**
     * YADETransferStarted, YADETransferFinished, YADEFileStateChanged
     * 
     */
    @JsonProperty("key")
    private String key;
    @JsonProperty("eventId")
    private String eventId;
    @JsonProperty("variables")
    private YadeVariables variables;

    /**
     * 
     * @return
     *     The tYPE
     */
    @JsonProperty("TYPE")
    public String getTYPE() {
        return tYPE;
    }

    /**
     * 
     * @param tYPE
     *     The TYPE
     */
    @JsonProperty("TYPE")
    public void setTYPE(String tYPE) {
        this.tYPE = tYPE;
    }

    /**
     * YADETransferStarted, YADETransferFinished, YADEFileStateChanged
     * 
     * @return
     *     The key
     */
    @JsonProperty("key")
    public String getKey() {
        return key;
    }

    /**
     * YADETransferStarted, YADETransferFinished, YADEFileStateChanged
     * 
     * @param key
     *     The key
     */
    @JsonProperty("key")
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * 
     * @return
     *     The eventId
     */
    @JsonProperty("eventId")
    public String getEventId() {
        return eventId;
    }

    /**
     * 
     * @param eventId
     *     The eventId
     */
    @JsonProperty("eventId")
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * 
     * @return
     *     The variables
     */
    @JsonProperty("variables")
    public YadeVariables getVariables() {
        return variables;
    }

    /**
     * 
     * @param variables
     *     The variables
     */
    @JsonProperty("variables")
    public void setVariables(YadeVariables variables) {
        this.variables = variables;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tYPE).append(key).append(eventId).append(variables).toHashCode();
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
        return new EqualsBuilder().append(tYPE, rhs.tYPE).append(key, rhs.key).append(eventId, rhs.eventId).append(variables, rhs.variables).isEquals();
    }

}
