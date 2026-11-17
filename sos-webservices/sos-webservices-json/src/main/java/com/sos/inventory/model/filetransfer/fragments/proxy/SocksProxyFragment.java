
package com.sos.inventory.model.filetransfer.fragments.proxy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * HTTP Proxy Fragment
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "resolveHostname"
})
public class SocksProxyFragment
    extends AProxyFragment
{

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("resolveHostname")
    private String resolveHostname;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SocksProxyFragment() {
    }

    /**
     * 
     * @param resolveHostname
     */
    public SocksProxyFragment(String resolveHostname) {
        super();
        this.resolveHostname = resolveHostname;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("resolveHostname")
    public String getResolveHostname() {
        return resolveHostname;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("resolveHostname")
    public void setResolveHostname(String resolveHostname) {
        this.resolveHostname = resolveHostname;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("resolveHostname", resolveHostname).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(resolveHostname).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SocksProxyFragment) == false) {
            return false;
        }
        SocksProxyFragment rhs = ((SocksProxyFragment) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(resolveHostname, rhs.resolveHostname).isEquals();
    }

}
