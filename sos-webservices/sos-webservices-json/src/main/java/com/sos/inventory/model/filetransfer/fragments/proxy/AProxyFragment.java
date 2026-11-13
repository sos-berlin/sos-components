
package com.sos.inventory.model.filetransfer.fragments.proxy;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
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
    "credentialStoreRef",
    "decryptionRef",
    "protocol",
    "connection",
    "authentication"
})
public class AProxyFragment
    extends AFragment
{

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("credentialStoreRef")
    private String credentialStoreRef;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("decryptionRef")
    private String decryptionRef;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("protocol")
    private AProxyFragment.Protocol protocol;
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
    public AProxyFragment(String credentialStoreRef, String decryptionRef, AProxyFragment.Protocol protocol, BasicConnection connection, BasicAuthentication authentication, String version, String path, String title, String documentationName) {
        super(version, path, title, documentationName);
        this.credentialStoreRef = credentialStoreRef;
        this.decryptionRef = decryptionRef;
        this.protocol = protocol;
        this.connection = connection;
        this.authentication = authentication;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("credentialStoreRef")
    public String getCredentialStoreRef() {
        return credentialStoreRef;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("credentialStoreRef")
    public void setCredentialStoreRef(String credentialStoreRef) {
        this.credentialStoreRef = credentialStoreRef;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("decryptionRef")
    public String getDecryptionRef() {
        return decryptionRef;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("decryptionRef")
    public void setDecryptionRef(String decryptionRef) {
        this.decryptionRef = decryptionRef;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("protocol")
    public AProxyFragment.Protocol getProtocol() {
        return protocol;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("protocol")
    public void setProtocol(AProxyFragment.Protocol protocol) {
        this.protocol = protocol;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("credentialStoreRef", credentialStoreRef).append("decryptionRef", decryptionRef).append("protocol", protocol).append("connection", connection).append("authentication", authentication).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(credentialStoreRef).append(protocol).append(connection).append(decryptionRef).append(authentication).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(credentialStoreRef, rhs.credentialStoreRef).append(protocol, rhs.protocol).append(connection, rhs.connection).append(decryptionRef, rhs.decryptionRef).append(authentication, rhs.authentication).isEquals();
    }

    public enum Protocol {

        HTTP("HTTP"),
        SOCKS("SOCKS");
        private final String value;
        private final static Map<String, AProxyFragment.Protocol> CONSTANTS = new HashMap<String, AProxyFragment.Protocol>();

        static {
            for (AProxyFragment.Protocol c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private Protocol(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static AProxyFragment.Protocol fromValue(String value) {
            AProxyFragment.Protocol constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
