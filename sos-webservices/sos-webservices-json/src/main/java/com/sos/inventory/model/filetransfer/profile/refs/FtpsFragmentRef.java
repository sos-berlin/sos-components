
package com.sos.inventory.model.filetransfer.profile.refs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.common.Processing;
import com.sos.inventory.model.filetransfer.profile.common.Rename;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * FTP Protocol Fragment Reference
 * <p>
 * allOf is present for validator only. jsonschema2pojo uses 'extends' for code generation. allOf ensures the validator applies the base required fields.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "processing",
    "rename"
})
public class FtpsFragmentRef
    extends FragmentRefBase
{

    /**
     * SSH processing options.
     * <p>
     * 
     * 
     */
    @JsonProperty("processing")
    private Processing processing;
    /**
     * Rename
     * <p>
     * Rename rule. Usable by any profile.
     * 
     */
    @JsonProperty("rename")
    @JsonPropertyDescription("Rename rule. Usable by any profile.")
    private Rename rename;

    /**
     * No args constructor for use in serialization
     * 
     */
    public FtpsFragmentRef() {
    }

    /**
     * 
     * @param ref
     * @param rename
     * @param processing
     */
    public FtpsFragmentRef(Processing processing, Rename rename, String ref) {
        super(ref);
        this.processing = processing;
        this.rename = rename;
    }

    /**
     * SSH processing options.
     * <p>
     * 
     * 
     */
    @JsonProperty("processing")
    public Processing getProcessing() {
        return processing;
    }

    /**
     * SSH processing options.
     * <p>
     * 
     * 
     */
    @JsonProperty("processing")
    public void setProcessing(Processing processing) {
        this.processing = processing;
    }

    /**
     * Rename
     * <p>
     * Rename rule. Usable by any profile.
     * 
     */
    @JsonProperty("rename")
    public Rename getRename() {
        return rename;
    }

    /**
     * Rename
     * <p>
     * Rename rule. Usable by any profile.
     * 
     */
    @JsonProperty("rename")
    public void setRename(Rename rename) {
        this.rename = rename;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("processing", processing).append("rename", rename).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(processing).append(rename).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FtpsFragmentRef) == false) {
            return false;
        }
        FtpsFragmentRef rhs = ((FtpsFragmentRef) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(processing, rhs.processing).append(rename, rhs.rename).isEquals();
    }

}
