
package com.sos.webservices.json.jobscheduler.history.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "errorState",
    "errorReason",
    "errorCode",
    "errorText"
})
public class Error {

    @JsonProperty("errorState")
    private String errorState;
    @JsonProperty("errorReason")
    private String errorReason;
    @JsonProperty("errorCode")
    private String errorCode;
    @JsonProperty("errorText")
    private String errorText;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Error() {
    }

    /**
     * 
     * @param errorText
     * @param errorState
     * @param errorReason
     * @param errorCode
     */
    public Error(String errorState, String errorReason, String errorCode, String errorText) {
        super();
        this.errorState = errorState;
        this.errorReason = errorReason;
        this.errorCode = errorCode;
        this.errorText = errorText;
    }

    @JsonProperty("errorState")
    public String getErrorState() {
        return errorState;
    }

    @JsonProperty("errorState")
    public void setErrorState(String errorState) {
        this.errorState = errorState;
    }

    @JsonProperty("errorReason")
    public String getErrorReason() {
        return errorReason;
    }

    @JsonProperty("errorReason")
    public void setErrorReason(String errorReason) {
        this.errorReason = errorReason;
    }

    @JsonProperty("errorCode")
    public String getErrorCode() {
        return errorCode;
    }

    @JsonProperty("errorCode")
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
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
        return new ToStringBuilder(this).append("errorState", errorState).append("errorReason", errorReason).append("errorCode", errorCode).append("errorText", errorText).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(errorCode).append(errorText).append(errorState).append(errorReason).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Error) == false) {
            return false;
        }
        Error rhs = ((Error) other);
        return new EqualsBuilder().append(errorCode, rhs.errorCode).append(errorText, rhs.errorText).append(errorState, rhs.errorState).append(errorReason, rhs.errorReason).isEquals();
    }

}
