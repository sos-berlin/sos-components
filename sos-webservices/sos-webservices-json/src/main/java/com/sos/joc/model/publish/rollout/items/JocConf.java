
package com.sos.joc.model.publish.rollout.items;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JOC Conf for rollout
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "url",
    "DN",
    "jocId"
})
public class JocConf {

    @JsonProperty("url")
    private String url;
    @JsonProperty("DN")
    private String dN;
    @JsonProperty("jocId")
    private String jocId;

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("DN")
    public String getDN() {
        return dN;
    }

    @JsonProperty("DN")
    public void setDN(String dN) {
        this.dN = dN;
    }

    @JsonProperty("jocId")
    public String getJocId() {
        return jocId;
    }

    @JsonProperty("jocId")
    public void setJocId(String jocId) {
        this.jocId = jocId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("url", url).append("dN", dN).append("jocId", jocId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jocId).append(dN).append(url).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JocConf) == false) {
            return false;
        }
        JocConf rhs = ((JocConf) other);
        return new EqualsBuilder().append(jocId, rhs.jocId).append(dN, rhs.dN).append(url, rhs.url).isEquals();
    }

}
