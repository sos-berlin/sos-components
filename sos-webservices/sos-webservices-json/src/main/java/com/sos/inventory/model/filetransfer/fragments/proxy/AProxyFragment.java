
package com.sos.inventory.model.filetransfer.fragments.proxy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.fragments.AFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.BasicAuthentication;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.BasicConnection;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Proxy Fragment
 * <p>
 * Abstract base for all proxy fragments. Do not instantiate directly.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "connection",
    "authentication"
})
public class AProxyFragment
    extends AFragment
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("connection")
    private BasicConnection connection;
    @JsonProperty("authentication")
    private BasicAuthentication authentication;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AProxyFragment() {
    }

    /**
     * 
     * @param connection
     * @param authentication
     */
    public AProxyFragment(BasicConnection connection, BasicAuthentication authentication) {
        super();
        this.connection = connection;
        this.authentication = authentication;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("connection")
    public BasicConnection getConnection() {
        return connection;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("connection")
    public void setConnection(BasicConnection connection) {
        this.connection = connection;
    }

    @JsonProperty("authentication")
    public BasicAuthentication getAuthentication() {
        return authentication;
    }

    @JsonProperty("authentication")
    public void setAuthentication(BasicAuthentication authentication) {
        this.authentication = authentication;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("connection", connection).append("authentication", authentication).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(connection).append(authentication).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AProxyFragment) == false) {
            return false;
        }
        AProxyFragment rhs = ((AProxyFragment) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(connection, rhs.connection).append(authentication, rhs.authentication).isEquals();
    }

}
