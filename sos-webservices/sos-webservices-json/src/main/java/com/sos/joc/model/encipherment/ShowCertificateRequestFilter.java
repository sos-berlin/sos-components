
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
 * show a certificate
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "certAliases"
})
public class ShowCertificateRequestFilter {

    @JsonProperty("certAliases")
    private List<String> certAliases = new ArrayList<String>();

    @JsonProperty("certAliases")
    public List<String> getCertAliases() {
        return certAliases;
    }

    @JsonProperty("certAliases")
    public void setCertAliases(List<String> certAliases) {
        this.certAliases = certAliases;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("certAliases", certAliases).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(certAliases).toHashCode();
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
        return new EqualsBuilder().append(certAliases, rhs.certAliases).isEquals();
    }

}
