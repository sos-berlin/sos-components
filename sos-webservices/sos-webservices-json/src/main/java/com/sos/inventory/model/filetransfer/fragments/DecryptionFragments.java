
package com.sos.inventory.model.filetransfer.fragments;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.fragments.decryption.DecryptionFragment;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Collection of all Decryption fragments.
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "fragments"
})
public class DecryptionFragments {

    /**
     * List of decryption fragments.
     * 
     */
    @JsonProperty("fragments")
    @JsonPropertyDescription("List of decryption fragments.")
    private List<DecryptionFragment> fragments = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public DecryptionFragments() {
    }

    /**
     * 
     * @param fragments
     */
    public DecryptionFragments(List<DecryptionFragment> fragments) {
        super();
        this.fragments = fragments;
    }

    /**
     * List of decryption fragments.
     * 
     */
    @JsonProperty("fragments")
    public List<DecryptionFragment> getFragments() {
        return fragments;
    }

    /**
     * List of decryption fragments.
     * 
     */
    @JsonProperty("fragments")
    public void setFragments(List<DecryptionFragment> fragments) {
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
        if ((other instanceof DecryptionFragments) == false) {
            return false;
        }
        DecryptionFragments rhs = ((DecryptionFragments) other);
        return new EqualsBuilder().append(fragments, rhs.fragments).isEquals();
    }

}
