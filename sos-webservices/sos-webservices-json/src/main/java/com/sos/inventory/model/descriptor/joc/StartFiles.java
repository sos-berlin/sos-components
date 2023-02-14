
package com.sos.inventory.model.descriptor.joc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "httpIni",
    "httpsIni",
    "sslIni"
})
public class StartFiles {

    @JsonProperty("httpIni")
    private String httpIni;
    @JsonProperty("httpsIni")
    private String httpsIni;
    @JsonProperty("sslIni")
    private String sslIni;

    /**
     * No args constructor for use in serialization
     * 
     */
    public StartFiles() {
    }

    /**
     * 
     * @param httpsIni
     * @param httpIni
     * @param sslIni
     */
    public StartFiles(String httpIni, String httpsIni, String sslIni) {
        super();
        this.httpIni = httpIni;
        this.httpsIni = httpsIni;
        this.sslIni = sslIni;
    }

    @JsonProperty("httpIni")
    public String getHttpIni() {
        return httpIni;
    }

    @JsonProperty("httpIni")
    public void setHttpIni(String httpIni) {
        this.httpIni = httpIni;
    }

    @JsonProperty("httpsIni")
    public String getHttpsIni() {
        return httpsIni;
    }

    @JsonProperty("httpsIni")
    public void setHttpsIni(String httpsIni) {
        this.httpsIni = httpsIni;
    }

    @JsonProperty("sslIni")
    public String getSslIni() {
        return sslIni;
    }

    @JsonProperty("sslIni")
    public void setSslIni(String sslIni) {
        this.sslIni = sslIni;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("httpIni", httpIni).append("httpsIni", httpsIni).append("sslIni", sslIni).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(httpIni).append(sslIni).append(httpsIni).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StartFiles) == false) {
            return false;
        }
        StartFiles rhs = ((StartFiles) other);
        return new EqualsBuilder().append(httpIni, rhs.httpIni).append(sslIni, rhs.sslIni).append(httpsIni, rhs.httpsIni).isEquals();
    }

}
