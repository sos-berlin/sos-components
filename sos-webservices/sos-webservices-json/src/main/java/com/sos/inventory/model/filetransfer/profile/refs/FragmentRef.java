
package com.sos.inventory.model.filetransfer.profile.refs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.common.FragmentRefTypeForProfile;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Fragment Reference
 * <p>
 * Reference to a local or protocol fragment, identified by type. For protocol types, 'ref' is required. For LOCAL, 'ref' is not needed and the 'local' block may carry local options.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "type",
    "ref",
    "local"
})
public class FragmentRef {

    /**
     * Profile Protocol Fragment Ref Type
     * <p>
     * Type of a protocol fragment which can be referenced in a profile. Used as discriminator in profile source / target fragment references.
     * (Required)
     * 
     */
    @JsonProperty("type")
    @JsonPropertyDescription("Type of a protocol fragment which can be referenced in a profile. Used as discriminator in profile source / target fragment references.")
    private FragmentRefTypeForProfile type;
    /**
     * Name of the referenced protocol fragment. Required for protocol types, omitted for LOCAL.
     * 
     */
    @JsonProperty("ref")
    @JsonPropertyDescription("Name of the referenced protocol fragment. Required for protocol types, omitted for LOCAL.")
    private String ref;
    /**
     * Local source/target options. Only allowed when type is LOCAL.
     * 
     */
    @JsonProperty("local")
    @JsonPropertyDescription("Local source/target options. Only allowed when type is LOCAL.")
    private Local local;

    /**
     * No args constructor for use in serialization
     * 
     */
    public FragmentRef() {
    }

    /**
     * 
     * @param ref
     * @param type
     * @param local
     */
    public FragmentRef(FragmentRefTypeForProfile type, String ref, Local local) {
        super();
        this.type = type;
        this.ref = ref;
        this.local = local;
    }

    /**
     * Profile Protocol Fragment Ref Type
     * <p>
     * Type of a protocol fragment which can be referenced in a profile. Used as discriminator in profile source / target fragment references.
     * (Required)
     * 
     */
    @JsonProperty("type")
    public FragmentRefTypeForProfile getType() {
        return type;
    }

    /**
     * Profile Protocol Fragment Ref Type
     * <p>
     * Type of a protocol fragment which can be referenced in a profile. Used as discriminator in profile source / target fragment references.
     * (Required)
     * 
     */
    @JsonProperty("type")
    public void setType(FragmentRefTypeForProfile type) {
        this.type = type;
    }

    /**
     * Name of the referenced protocol fragment. Required for protocol types, omitted for LOCAL.
     * 
     */
    @JsonProperty("ref")
    public String getRef() {
        return ref;
    }

    /**
     * Name of the referenced protocol fragment. Required for protocol types, omitted for LOCAL.
     * 
     */
    @JsonProperty("ref")
    public void setRef(String ref) {
        this.ref = ref;
    }

    /**
     * Local source/target options. Only allowed when type is LOCAL.
     * 
     */
    @JsonProperty("local")
    public Local getLocal() {
        return local;
    }

    /**
     * Local source/target options. Only allowed when type is LOCAL.
     * 
     */
    @JsonProperty("local")
    public void setLocal(Local local) {
        this.local = local;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("type", type).append("ref", ref).append("local", local).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(ref).append(type).append(local).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FragmentRef) == false) {
            return false;
        }
        FragmentRef rhs = ((FragmentRef) other);
        return new EqualsBuilder().append(ref, rhs.ref).append(type, rhs.type).append(local, rhs.local).isEquals();
    }

}
