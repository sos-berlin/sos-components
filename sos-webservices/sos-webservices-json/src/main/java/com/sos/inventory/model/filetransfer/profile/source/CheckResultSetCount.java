
package com.sos.inventory.model.filetransfer.profile.source;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "expectedResultSetCount",
    "raiseErrorIfResultSetIs"
})
public class CheckResultSetCount {

    /**
     * A non-negative integer (digits only). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("expectedResultSetCount")
    private String expectedResultSetCount;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("raiseErrorIfResultSetIs")
    private String raiseErrorIfResultSetIs;

    /**
     * No args constructor for use in serialization
     * 
     */
    public CheckResultSetCount() {
    }

    /**
     * 
     * @param expectedResultSetCount
     * @param raiseErrorIfResultSetIs
     */
    public CheckResultSetCount(String expectedResultSetCount, String raiseErrorIfResultSetIs) {
        super();
        this.expectedResultSetCount = expectedResultSetCount;
        this.raiseErrorIfResultSetIs = raiseErrorIfResultSetIs;
    }

    /**
     * A non-negative integer (digits only). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("expectedResultSetCount")
    public String getExpectedResultSetCount() {
        return expectedResultSetCount;
    }

    /**
     * A non-negative integer (digits only). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("expectedResultSetCount")
    public void setExpectedResultSetCount(String expectedResultSetCount) {
        this.expectedResultSetCount = expectedResultSetCount;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("raiseErrorIfResultSetIs")
    public String getRaiseErrorIfResultSetIs() {
        return raiseErrorIfResultSetIs;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("raiseErrorIfResultSetIs")
    public void setRaiseErrorIfResultSetIs(String raiseErrorIfResultSetIs) {
        this.raiseErrorIfResultSetIs = raiseErrorIfResultSetIs;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("expectedResultSetCount", expectedResultSetCount).append("raiseErrorIfResultSetIs", raiseErrorIfResultSetIs).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(expectedResultSetCount).append(raiseErrorIfResultSetIs).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CheckResultSetCount) == false) {
            return false;
        }
        CheckResultSetCount rhs = ((CheckResultSetCount) other);
        return new EqualsBuilder().append(expectedResultSetCount, rhs.expectedResultSetCount).append(raiseErrorIfResultSetIs, rhs.raiseErrorIfResultSetIs).isEquals();
    }

}
