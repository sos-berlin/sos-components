
package com.sos.inventory.model.filetransfer.fragments.proxy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.BasicAuthentication;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.BasicConnection;
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
public class ProxySocksFragment
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
    public ProxySocksFragment() {
    }

    /**
     * 
     * @param path
     * @param protocol
     * @param resolveHostname
     * @param credentialStoreRef
     * @param connection
     * @param documentationName
     * @param title
     * @param decryptionRef
     * @param version
     * @param authentication
     */
    public ProxySocksFragment(String resolveHostname, String credentialStoreRef, String decryptionRef, AProxyFragment.Protocol protocol, BasicConnection connection, BasicAuthentication authentication, String version, String path, String title, String documentationName) {
        super(credentialStoreRef, decryptionRef, protocol, connection, authentication, version, path, title, documentationName);
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
        if ((other instanceof ProxySocksFragment) == false) {
            return false;
        }
        ProxySocksFragment rhs = ((ProxySocksFragment) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(resolveHostname, rhs.resolveHostname).isEquals();
    }

}
