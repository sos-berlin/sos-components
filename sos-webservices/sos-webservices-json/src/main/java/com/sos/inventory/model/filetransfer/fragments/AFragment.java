
package com.sos.inventory.model.filetransfer.fragments;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.AFileTransfer;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Abstract base for all fragments: CredentialStore / Decryprion / Proxy / Protocol
 * <p>
 * Abstract base for all fragments. Do not instantiate directly.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({

})
public class AFragment
    extends AFileTransfer
{


    /**
     * No args constructor for use in serialization
     * 
     */
    public AFragment() {
    }

    /**
     * 
     * @param path
     * @param documentationName
     * @param title
     * @param version
     */
    public AFragment(String version, String path, String title, String documentationName) {
        super(version, path, title, documentationName);
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
        if ((other instanceof AFragment) == false) {
            return false;
        }
        AFragment rhs = ((AFragment) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).isEquals();
    }

}
