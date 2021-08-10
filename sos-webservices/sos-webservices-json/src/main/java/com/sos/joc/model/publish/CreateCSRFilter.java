
package com.sos.joc.model.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * sets the properties to create a (C)ertificate (S)igning (R)equest filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "dn",
    "san",
    "hostname"
})
public class CreateCSRFilter {

    @JsonProperty("dn")
    private String dn;
    @JsonProperty("san")
    private String san;
    @JsonProperty("hostname")
    private String hostname;

    @JsonProperty("dn")
    public String getDn() {
        return dn;
    }

    @JsonProperty("dn")
    public void setDn(String dn) {
        this.dn = dn;
    }

    @JsonProperty("san")
    public String getSan() {
        return san;
    }

    @JsonProperty("san")
    public void setSan(String san) {
        this.san = san;
    }

    @JsonProperty("hostname")
    public String getHostname() {
        return hostname;
    }

    @JsonProperty("hostname")
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("dn", dn).append("san", san).append("hostname", hostname).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(san).append(dn).append(hostname).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CreateCSRFilter) == false) {
            return false;
        }
        CreateCSRFilter rhs = ((CreateCSRFilter) other);
        return new EqualsBuilder().append(san, rhs.san).append(dn, rhs.dn).append(hostname, rhs.hostname).isEquals();
    }

}
