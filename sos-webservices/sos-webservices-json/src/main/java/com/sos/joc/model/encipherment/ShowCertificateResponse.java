
package com.sos.joc.model.encipherment;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * shows all certificates
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "certificates"
})
public class ShowCertificateResponse {

    @JsonProperty("certificates")
    private List<Object> certificates = new ArrayList<Object>();

    @JsonProperty("certificates")
    public List<Object> getCertificates() {
        return certificates;
    }

    @JsonProperty("certificates")
    public void setCertificates(List<Object> certificates) {
        this.certificates = certificates;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("certificates", certificates).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(certificates).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ShowCertificateResponse) == false) {
            return false;
        }
        ShowCertificateResponse rhs = ((ShowCertificateResponse) other);
        return new EqualsBuilder().append(certificates, rhs.certificates).isEquals();
    }

}
