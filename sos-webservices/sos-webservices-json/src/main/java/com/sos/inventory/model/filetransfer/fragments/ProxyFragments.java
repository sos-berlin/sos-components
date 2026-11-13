
package com.sos.inventory.model.filetransfer.fragments;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.fragments.proxy.AProxyFragment;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Collection of all Proxy fragments.
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "fragments"
})
public class ProxyFragments {

    /**
     * List of proxy fragments.
     * 
     */
    @JsonProperty("fragments")
    @JsonPropertyDescription("List of proxy fragments.")
    private List<AProxyFragment> fragments = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ProxyFragments() {
    }

    /**
     * 
     * @param fragments
     */
    public ProxyFragments(List<AProxyFragment> fragments) {
        super();
        this.fragments = fragments;
    }

    /**
     * List of proxy fragments.
     * 
     */
    @JsonProperty("fragments")
    public List<AProxyFragment> getFragments() {
        return fragments;
    }

    /**
     * List of proxy fragments.
     * 
     */
    @JsonProperty("fragments")
    public void setFragments(List<AProxyFragment> fragments) {
        this.fragments = fragments;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("fragments", fragments).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(fragments).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProxyFragments) == false) {
            return false;
        }
        ProxyFragments rhs = ((ProxyFragments) other);
        return new EqualsBuilder().append(fragments, rhs.fragments).isEquals();
    }

}
