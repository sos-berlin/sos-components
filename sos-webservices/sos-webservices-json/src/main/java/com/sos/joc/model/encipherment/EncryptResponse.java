
package com.sos.joc.model.encipherment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * encrypted value
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "encryptedValue"
})
public class EncryptResponse {

    @JsonProperty("encryptedValue")
    private String encryptedValue;

    @JsonProperty("encryptedValue")
    public String getEncryptedValue() {
        return encryptedValue;
    }

    @JsonProperty("encryptedValue")
    public void setEncryptedValue(String encryptedValue) {
        this.encryptedValue = encryptedValue;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("encryptedValue", encryptedValue).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(encryptedValue).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof EncryptResponse) == false) {
            return false;
        }
        EncryptResponse rhs = ((EncryptResponse) other);
        return new EqualsBuilder().append(encryptedValue, rhs.encryptedValue).isEquals();
    }

}
