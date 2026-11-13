
package com.sos.inventory.model.filetransfer.fragments.proxy;

import com.fasterxml.jackson.annotation.JsonInclude;
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

})
public class ProxyHttpFragment
    extends AProxyFragment
{


    /**
     * No args constructor for use in serialization
     * 
     */
    public ProxyHttpFragment() {
    }

    /**
     * 
     * @param path
     * @param protocol
     * @param credentialStoreRef
     * @param connection
     * @param documentationName
     * @param title
     * @param decryptionRef
     * @param version
     * @param authentication
     */
    public ProxyHttpFragment(String credentialStoreRef, String decryptionRef, AProxyFragment.Protocol protocol, BasicConnection connection, BasicAuthentication authentication, String version, String path, String title, String documentationName) {
        super(credentialStoreRef, decryptionRef, protocol, connection, authentication, version, path, title, documentationName);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProxyHttpFragment) == false) {
            return false;
        }
        ProxyHttpFragment rhs = ((ProxyHttpFragment) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).isEquals();
    }

}
