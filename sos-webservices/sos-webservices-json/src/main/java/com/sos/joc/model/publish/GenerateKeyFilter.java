
package com.sos.joc.model.publish;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * set generate Key filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "validUntil",
    "keyAlgorithm"
})
public class GenerateKeyFilter {

    @JsonProperty("validUntil")
    private Date validUntil;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("keyAlgorithm")
    private String keyAlgorithm;

    @JsonProperty("validUntil")
    public Date getValidUntil() {
        return validUntil;
    }

    @JsonProperty("validUntil")
    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("keyAlgorithm")
    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("keyAlgorithm")
    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("validUntil", validUntil).append("keyAlgorithm", keyAlgorithm).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(validUntil).append(keyAlgorithm).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof GenerateKeyFilter) == false) {
            return false;
        }
        GenerateKeyFilter rhs = ((GenerateKeyFilter) other);
        return new EqualsBuilder().append(validUntil, rhs.validUntil).append(keyAlgorithm, rhs.keyAlgorithm).isEquals();
    }

}
