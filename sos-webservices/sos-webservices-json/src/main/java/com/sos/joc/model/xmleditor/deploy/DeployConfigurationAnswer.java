
package com.sos.joc.model.xmleditor.deploy;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.xmleditor.common.AnswerMessage;
import com.sos.joc.model.xmleditor.validate.ErrorMessage;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * xmleditor deploy configuration answer
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deployed",
    "validationError",
    "message"
})
public class DeployConfigurationAnswer {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deployed")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deployed;
    /**
     * xmleditor validate configuration error answer
     * <p>
     * 
     * 
     */
    @JsonProperty("validationError")
    private ErrorMessage validationError;
    /**
     * xmleditor answer message
     * <p>
     * 
     * 
     */
    @JsonProperty("message")
    private AnswerMessage message;

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deployed")
    public Date getDeployed() {
        return deployed;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deployed")
    public void setDeployed(Date deployed) {
        this.deployed = deployed;
    }

    /**
     * xmleditor validate configuration error answer
     * <p>
     * 
     * 
     */
    @JsonProperty("validationError")
    public ErrorMessage getValidationError() {
        return validationError;
    }

    /**
     * xmleditor validate configuration error answer
     * <p>
     * 
     * 
     */
    @JsonProperty("validationError")
    public void setValidationError(ErrorMessage validationError) {
        this.validationError = validationError;
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
        return new ToStringBuilder(this).append("deployed", deployed).append("validationError", validationError).append("message", message).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deployed).append(message).append(validationError).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployConfigurationAnswer) == false) {
            return false;
        }
        DeployConfigurationAnswer rhs = ((DeployConfigurationAnswer) other);
        return new EqualsBuilder().append(deployed, rhs.deployed).append(message, rhs.message).append(validationError, rhs.validationError).isEquals();
    }

}
