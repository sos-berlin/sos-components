
package com.sos.joc.model.xmleditor.read.other;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.xmleditor.common.AnswerMessage;
import com.sos.joc.model.xmleditor.common.ObjectVersionState;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * xmleditor read others configuration state answer
 * <p>
 * Describes the draft situation
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deployed",
    "versionState",
    "message"
})
public class AnswerConfigurationState {

    @JsonProperty("deployed")
    private Boolean deployed;
    /**
     * xmleditor object version state text
     * <p>
     * 
     * 
     */
    @JsonProperty("versionState")
    private ObjectVersionState versionState;
    /**
     * xmleditor answer message
     * <p>
     * 
     * 
     */
    @JsonProperty("message")
    private AnswerMessage message;

    @JsonProperty("deployed")
    public Boolean getDeployed() {
        return deployed;
    }

    @JsonProperty("deployed")
    public void setDeployed(Boolean deployed) {
        this.deployed = deployed;
    }

    /**
     * xmleditor object version state text
     * <p>
     * 
     * 
     */
    @JsonProperty("versionState")
    public ObjectVersionState getVersionState() {
        return versionState;
    }

    /**
     * xmleditor object version state text
     * <p>
     * 
     * 
     */
    @JsonProperty("versionState")
    public void setVersionState(ObjectVersionState versionState) {
        this.versionState = versionState;
    }

    /**
     * xmleditor answer message
     * <p>
     * 
     * 
     */
    @JsonProperty("message")
    public AnswerMessage getMessage() {
        return message;
    }

    /**
     * xmleditor answer message
     * <p>
     * 
     * 
     */
    @JsonProperty("message")
    public void setMessage(AnswerMessage message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deployed", deployed).append("versionState", versionState).append("message", message).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deployed).append(versionState).append(message).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AnswerConfigurationState) == false) {
            return false;
        }
        AnswerConfigurationState rhs = ((AnswerConfigurationState) other);
        return new EqualsBuilder().append(deployed, rhs.deployed).append(versionState, rhs.versionState).append(message, rhs.message).isEquals();
    }

}
