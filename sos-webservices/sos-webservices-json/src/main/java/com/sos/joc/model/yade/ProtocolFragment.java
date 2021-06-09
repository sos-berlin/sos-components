
package com.sos.joc.model.yade;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * protocol, host, port, account
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "host",
    "protocol",
    "port",
    "account"
})
public class ProtocolFragment {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("host")
    private String host;
    /**
     * protocol
     * <p>
     * 
     * 
     */
    @JsonProperty("protocol")
    private Protocol protocol;
    @JsonProperty("port")
    private Integer port;
    @JsonProperty("account")
    private String account;

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("host")
    public String getHost() {
        return host;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("host")
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * protocol
     * <p>
     * 
     * 
     */
    @JsonProperty("protocol")
    public Protocol getProtocol() {
        return protocol;
    }

    /**
     * protocol
     * <p>
     * 
     * 
     */
    @JsonProperty("protocol")
    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    @JsonProperty("port")
    public Integer getPort() {
        return port;
    }

    @JsonProperty("port")
    public void setPort(Integer port) {
        this.port = port;
    }

    @JsonProperty("account")
    public String getAccount() {
        return account;
    }

    @JsonProperty("account")
    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("host", host).append("protocol", protocol).append("port", port).append("account", account).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(host).append(protocol).append(port).append(account).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProtocolFragment) == false) {
            return false;
        }
        ProtocolFragment rhs = ((ProtocolFragment) other);
        return new EqualsBuilder().append(host, rhs.host).append(protocol, rhs.protocol).append(port, rhs.port).append(account, rhs.account).isEquals();
    }

}
