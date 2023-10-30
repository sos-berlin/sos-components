
package com.sos.inventory.model.descriptor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "primaryJocCert",
    "secondaryJocCert"
})
public class Joc {

    @JsonProperty("primaryJocCert")
    private String primaryJocCert;
    @JsonProperty("secondaryJocCert")
    private String secondaryJocCert;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Joc() {
    }

    /**
     * 
     * @param secondaryJocCert
     * @param primaryJocCert
     */
    public Joc(String primaryJocCert, String secondaryJocCert) {
        super();
        this.primaryJocCert = primaryJocCert;
        this.secondaryJocCert = secondaryJocCert;
    }

    @JsonProperty("primaryJocCert")
    public String getPrimaryJocCert() {
        return primaryJocCert;
    }

    @JsonProperty("primaryJocCert")
    public void setPrimaryJocCert(String primaryJocCert) {
        this.primaryJocCert = primaryJocCert;
    }

    @JsonProperty("secondaryJocCert")
    public String getSecondaryJocCert() {
        return secondaryJocCert;
    }

    @JsonProperty("secondaryJocCert")
    public void setSecondaryJocCert(String secondaryJocCert) {
        this.secondaryJocCert = secondaryJocCert;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("primaryJocCert", primaryJocCert).append("secondaryJocCert", secondaryJocCert).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(secondaryJocCert).append(primaryJocCert).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Joc) == false) {
            return false;
        }
        Joc rhs = ((Joc) other);
        return new EqualsBuilder().append(secondaryJocCert, rhs.secondaryJocCert).append(primaryJocCert, rhs.primaryJocCert).isEquals();
    }

}
