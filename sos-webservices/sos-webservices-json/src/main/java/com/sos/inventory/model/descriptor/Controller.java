
package com.sos.inventory.model.descriptor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "primaryControllerCert",
    "secondaryControllerCert"
})
public class Controller {

    @JsonProperty("primaryControllerCert")
    private String primaryControllerCert;
    @JsonProperty("secondaryControllerCert")
    private String secondaryControllerCert;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Controller() {
    }

    /**
     * 
     * @param primaryControllerCert
     * @param secondaryControllerCert
     */
    public Controller(String primaryControllerCert, String secondaryControllerCert) {
        super();
        this.primaryControllerCert = primaryControllerCert;
        this.secondaryControllerCert = secondaryControllerCert;
    }

    @JsonProperty("primaryControllerCert")
    public String getPrimaryControllerCert() {
        return primaryControllerCert;
    }

    @JsonProperty("primaryControllerCert")
    public void setPrimaryControllerCert(String primaryControllerCert) {
        this.primaryControllerCert = primaryControllerCert;
    }

    @JsonProperty("secondaryControllerCert")
    public String getSecondaryControllerCert() {
        return secondaryControllerCert;
    }

    @JsonProperty("secondaryControllerCert")
    public void setSecondaryControllerCert(String secondaryControllerCert) {
        this.secondaryControllerCert = secondaryControllerCert;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("primaryControllerCert", primaryControllerCert).append("secondaryControllerCert", secondaryControllerCert).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(secondaryControllerCert).append(primaryControllerCert).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Controller) == false) {
            return false;
        }
        Controller rhs = ((Controller) other);
        return new EqualsBuilder().append(secondaryControllerCert, rhs.secondaryControllerCert).append(primaryControllerCert, rhs.primaryControllerCert).isEquals();
    }

}
