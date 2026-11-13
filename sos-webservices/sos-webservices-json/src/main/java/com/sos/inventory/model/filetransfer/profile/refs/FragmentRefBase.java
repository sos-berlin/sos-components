
package com.sos.inventory.model.filetransfer.profile.refs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Fragment Reference Base
 * <p>
 * Base for all fragment references. Do not instantiate directly.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "ref"
})
public class FragmentRefBase {

    /**
     * string without < and >
     * <p>
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
    public FragmentRefBase() {
    }

    /**
     * 
     * @param ref
     */
    public FragmentRefBase(String ref) {
        super();
        this.ref = ref;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("ref")
    public String getRef() {
        return ref;
    }

    /**
     * string without < and >
     * <p>
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
        return new ToStringBuilder(this).append("ref", ref).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(ref).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FragmentRefBase) == false) {
            return false;
        }
        FragmentRefBase rhs = ((FragmentRefBase) other);
        return new EqualsBuilder().append(ref, rhs.ref).isEquals();
    }

}
