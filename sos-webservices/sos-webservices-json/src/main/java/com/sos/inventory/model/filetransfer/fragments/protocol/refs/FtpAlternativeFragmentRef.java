
package com.sos.inventory.model.filetransfer.fragments.protocol.refs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.common.FragmentRefTypeForProtocolFragment;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * FTP Alternative Fragment Reference
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({

})
public class FtpAlternativeFragmentRef
    extends AAlternativeFragmentRef
{


    /**
     * No args constructor for use in serialization
     * 
     */
    public FtpAlternativeFragmentRef() {
    }

    /**
     * 
     * @param ref
     * @param type
     */
    public FtpAlternativeFragmentRef(FragmentRefTypeForProtocolFragment type, String ref) {
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
        if ((other instanceof FtpAlternativeFragmentRef) == false) {
            return false;
        }
        FtpAlternativeFragmentRef rhs = ((FtpAlternativeFragmentRef) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).isEquals();
    }

}
