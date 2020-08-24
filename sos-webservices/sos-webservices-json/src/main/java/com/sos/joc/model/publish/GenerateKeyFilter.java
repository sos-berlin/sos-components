
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
    "usePGP"
})
public class GenerateKeyFilter {

    @JsonProperty("validUntil")
    private Date validUntil;
    @JsonProperty("usePGP")
    private Boolean usePGP;

    @JsonProperty("validUntil")
    public Date getValidUntil() {
        return validUntil;
    }

    @JsonProperty("validUntil")
    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    @JsonProperty("usePGP")
    public Boolean getUsePGP() {
        return usePGP;
    }

    @JsonProperty("usePGP")
    public void setUsePGP(Boolean usePGP) {
        this.usePGP = usePGP;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("validUntil", validUntil).append("usePGP", usePGP).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(usePGP).append(validUntil).toHashCode();
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
        return new EqualsBuilder().append(usePGP, rhs.usePGP).append(validUntil, rhs.validUntil).isEquals();
    }

}
