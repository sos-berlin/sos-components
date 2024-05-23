
package com.sos.joc.model.encipherment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * show a certificate
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "certAlias"
})
public class ShowCertificateRequestFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("certAlias")
    private String certAlias;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("certAlias")
    public String getCertAlias() {
        return certAlias;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("certAlias")
    public void setCertAlias(String certAlias) {
        this.certAlias = certAlias;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("certAlias", certAlias).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(certAlias).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ShowCertificateRequestFilter) == false) {
            return false;
        }
        ShowCertificateRequestFilter rhs = ((ShowCertificateRequestFilter) other);
        return new EqualsBuilder().append(certAlias, rhs.certAlias).isEquals();
    }

}
