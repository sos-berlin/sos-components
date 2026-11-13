
package com.sos.inventory.model.filetransfer.fragments.protocol.refs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.common.FragmentRefTypeForProtocolFragment;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * HTTP Alternative Fragment Reference
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({

})
public class HttpAlternativeFragmentRef
    extends AAlternativeFragmentRef
{


    /**
     * No args constructor for use in serialization
     * 
     */
    public HttpAlternativeFragmentRef() {
    }

    /**
     * 
     * @param ref
     * @param type
     */
    public HttpAlternativeFragmentRef(FragmentRefTypeForProtocolFragment type, String ref) {
        super(type, ref);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof HttpAlternativeFragmentRef) == false) {
            return false;
        }
        HttpAlternativeFragmentRef rhs = ((HttpAlternativeFragmentRef) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).isEquals();
    }

}
