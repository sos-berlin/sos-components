
package com.sos.joc.model.xmleditor.validate;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * xmleditor validate configuration answer
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "validated",
    "validationError"
})
public class ValidateConfigurationAnswer {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("validated")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date validated;
    /**
     * xmleditor validate configuration error answer
     * <p>
     * 
     * 
     */
    @JsonProperty("validationError")
    private ErrorMessage validationError;

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("validated")
    public Date getValidated() {
        return validated;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("validated")
    public void setValidated(Date validated) {
        this.validated = validated;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("validated", validated).append("validationError", validationError).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(validated).append(validationError).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ValidateConfigurationAnswer) == false) {
            return false;
        }
        ValidateConfigurationAnswer rhs = ((ValidateConfigurationAnswer) other);
        return new EqualsBuilder().append(validated, rhs.validated).append(validationError, rhs.validationError).isEquals();
    }

}
