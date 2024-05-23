
package com.sos.joc.model.security.configuration.permissions.joc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "encrypt"
})
public class Encipherment {

    /**
     * configuration tab
     * 
     */
    @JsonProperty("encrypt")
    @JsonPropertyDescription("configuration tab")
    private Boolean encrypt = false;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Encipherment() {
    }

    /**
     * 
     * @param encrypt
     */
    public Encipherment(Boolean encrypt) {
        super();
        this.encrypt = encrypt;
    }

    /**
     * configuration tab
     * 
     */
    @JsonProperty("encrypt")
    public Boolean getEncrypt() {
        return encrypt;
    }

    /**
     * configuration tab
     * 
     */
    @JsonProperty("encrypt")
    public void setEncrypt(Boolean encrypt) {
        this.encrypt = encrypt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("encrypt", encrypt).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(encrypt).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Encipherment) == false) {
            return false;
        }
        Encipherment rhs = ((Encipherment) other);
        return new EqualsBuilder().append(encrypt, rhs.encrypt).isEquals();
    }

}
