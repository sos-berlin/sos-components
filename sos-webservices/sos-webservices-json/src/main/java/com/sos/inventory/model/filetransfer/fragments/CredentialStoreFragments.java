
package com.sos.inventory.model.filetransfer.fragments;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.fragments.cs.CredentialStoreFragment;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Collection of all credential store fragments.
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "fragments"
})
public class CredentialStoreFragments {

    /**
     * List of credential store fragments.
     * 
     */
    @JsonProperty("fragments")
    @JsonPropertyDescription("List of credential store fragments.")
    private List<CredentialStoreFragment> fragments = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public CredentialStoreFragments() {
    }

    /**
     * 
     * @param fragments
     */
    public CredentialStoreFragments(List<CredentialStoreFragment> fragments) {
        super();
        this.fragments = fragments;
    }

    /**
     * List of credential store fragments.
     * 
     */
    @JsonProperty("fragments")
    public List<CredentialStoreFragment> getFragments() {
        return fragments;
    }

    /**
     * List of credential store fragments.
     * 
     */
    @JsonProperty("fragments")
    public void setFragments(List<CredentialStoreFragment> fragments) {
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
        if ((other instanceof CredentialStoreFragments) == false) {
            return false;
        }
        CredentialStoreFragments rhs = ((CredentialStoreFragments) other);
        return new EqualsBuilder().append(fragments, rhs.fragments).isEquals();
    }

}
