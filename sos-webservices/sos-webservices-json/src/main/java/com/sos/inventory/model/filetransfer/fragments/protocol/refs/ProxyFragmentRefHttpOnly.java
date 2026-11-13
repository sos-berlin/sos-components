
package com.sos.inventory.model.filetransfer.fragments.protocol.refs;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * HTTP Proxy Reference
 * <p>
 * Reference to an HTTP proxy fragment.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "type",
    "ref"
})
public class ProxyFragmentRefHttpOnly {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    private ProxyFragmentRefHttpOnly.Type type;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ref")
    private String ref;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ProxyFragmentRefHttpOnly() {
    }

    /**
     * 
     * @param ref
     * @param type
     */
    public ProxyFragmentRefHttpOnly(ProxyFragmentRefHttpOnly.Type type, String ref) {
        super();
        this.type = type;
        this.ref = ref;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public ProxyFragmentRefHttpOnly.Type getType() {
        return type;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public void setType(ProxyFragmentRefHttpOnly.Type type) {
        this.type = type;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ref")
    public String getRef() {
        return ref;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ref")
    public void setRef(String ref) {
        this.ref = ref;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("type", type).append("ref", ref).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(type).append(ref).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProxyFragmentRefHttpOnly) == false) {
            return false;
        }
        ProxyFragmentRefHttpOnly rhs = ((ProxyFragmentRefHttpOnly) other);
        return new EqualsBuilder().append(type, rhs.type).append(ref, rhs.ref).isEquals();
    }

    public enum Type {

        HTTP("HTTP");
        private final String value;
        private final static Map<String, ProxyFragmentRefHttpOnly.Type> CONSTANTS = new HashMap<String, ProxyFragmentRefHttpOnly.Type>();

        static {
            for (ProxyFragmentRefHttpOnly.Type c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private Type(String value) {
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
        public static ProxyFragmentRefHttpOnly.Type fromValue(String value) {
            ProxyFragmentRefHttpOnly.Type constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
