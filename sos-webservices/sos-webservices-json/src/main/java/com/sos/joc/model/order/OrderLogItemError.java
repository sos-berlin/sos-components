
package com.sos.joc.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "errorStatus",
    "errorReason",
    "errorText"
})
public class OrderLogItemError {

    @JsonProperty("errorStatus")
    private String errorStatus;
    @JsonProperty("errorReason")
    private String errorReason;
    @JsonProperty("errorText")
    private String errorText;

    @JsonProperty("errorStatus")
    public String getErrorStatus() {
        return errorStatus;
    }

    @JsonProperty("errorStatus")
    public void setErrorStatus(String errorStatus) {
        this.errorStatus = errorStatus;
    }

    @JsonProperty("errorReason")
    public String getErrorReason() {
        return errorReason;
    }

    @JsonProperty("errorReason")
    public void setErrorReason(String errorReason) {
        this.errorReason = errorReason;
    }

    @JsonProperty("errorText")
    public String getErrorText() {
        return errorText;
    }

    @JsonProperty("errorText")
    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("errorStatus", errorStatus).append("errorReason", errorReason).append("errorText", errorText).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(errorStatus).append(errorText).append(errorReason).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderLogItemError) == false) {
            return false;
        }
        OrderLogItemError rhs = ((OrderLogItemError) other);
        return new EqualsBuilder().append(errorStatus, rhs.errorStatus).append(errorText, rhs.errorText).append(errorReason, rhs.errorReason).isEquals();
    }

}
