
package com.sos.inventory.model.filetransfer.fragments.protocol.refs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.common.FragmentRefTypeForProtocolFragment;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Abstract Alternative Fragment Reference
 * <p>
 * Base for all alternative fragment references. Do not instantiate directly.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "type",
    "ref"
})
public class AAlternativeFragmentRef {

    /**
     * Protocol Fragment alternative Ref Type
     * <p>
     * Type of a fragment which can be referenced as alternative fragment in a protocol fragment.
     * (Required)
     * 
     */
    @JsonProperty("type")
    @JsonPropertyDescription("Type of a fragment which can be referenced as alternative fragment in a protocol fragment.")
    private FragmentRefTypeForProtocolFragment type;
    /**
     * Name of the referenced alternative protocol fragment.
     * (Required)
     * 
     */
    @JsonProperty("ref")
    @JsonPropertyDescription("Name of the referenced alternative protocol fragment.")
    private String ref;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AAlternativeFragmentRef() {
    }

    /**
     * 
     * @param ref
     * @param type
     */
    public AAlternativeFragmentRef(FragmentRefTypeForProtocolFragment type, String ref) {
        super();
        this.type = type;
        this.ref = ref;
    }

    /**
     * Protocol Fragment alternative Ref Type
     * <p>
     * Type of a fragment which can be referenced as alternative fragment in a protocol fragment.
     * (Required)
     * 
     */
    @JsonProperty("type")
    public FragmentRefTypeForProtocolFragment getType() {
        return type;
    }

    /**
     * Protocol Fragment alternative Ref Type
     * <p>
     * Type of a fragment which can be referenced as alternative fragment in a protocol fragment.
     * (Required)
     * 
     */
    @JsonProperty("type")
    public void setType(FragmentRefTypeForProtocolFragment type) {
        this.type = type;
    }

    /**
     * Name of the referenced alternative protocol fragment.
     * (Required)
     * 
     */
    @JsonProperty("ref")
    public String getRef() {
        return ref;
    }

    /**
     * Name of the referenced alternative protocol fragment.
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
        if ((other instanceof AAlternativeFragmentRef) == false) {
            return false;
        }
        AAlternativeFragmentRef rhs = ((AAlternativeFragmentRef) other);
        return new EqualsBuilder().append(type, rhs.type).append(ref, rhs.ref).isEquals();
    }

}
