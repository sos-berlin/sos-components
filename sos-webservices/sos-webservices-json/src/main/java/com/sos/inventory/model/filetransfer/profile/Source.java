
package com.sos.inventory.model.filetransfer.profile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.profile.refs.FragmentRef;
import com.sos.inventory.model.filetransfer.profile.source.SourceFileOptions;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Source
 * <p>
 * Source of a file transfer operation.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "fragmentRef",
    "sourceFileOptions"
})
public class Source {

    /**
     * Fragment Reference
     * <p>
     * Reference to a local or protocol fragment, identified by type. For protocol types, 'ref' is required. For LOCAL, 'ref' is not needed and the 'local' block may carry local options.
     * (Required)
     * 
     */
    @JsonProperty("fragmentRef")
    @JsonPropertyDescription("Reference to a local or protocol fragment, identified by type. For protocol types, 'ref' is required. For LOCAL, 'ref' is not needed and the 'local' block may carry local options.")
    private FragmentRef fragmentRef;
    /**
     * Source File Options
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("sourceFileOptions")
    private SourceFileOptions sourceFileOptions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Source() {
    }

    /**
     * 
     * @param sourceFileOptions
     * @param fragmentRef
     */
    public Source(FragmentRef fragmentRef, SourceFileOptions sourceFileOptions) {
        super();
        this.fragmentRef = fragmentRef;
        this.sourceFileOptions = sourceFileOptions;
    }

    /**
     * Fragment Reference
     * <p>
     * Reference to a local or protocol fragment, identified by type. For protocol types, 'ref' is required. For LOCAL, 'ref' is not needed and the 'local' block may carry local options.
     * (Required)
     * 
     */
    @JsonProperty("fragmentRef")
    public FragmentRef getFragmentRef() {
        return fragmentRef;
    }

    /**
     * Fragment Reference
     * <p>
     * Reference to a local or protocol fragment, identified by type. For protocol types, 'ref' is required. For LOCAL, 'ref' is not needed and the 'local' block may carry local options.
     * (Required)
     * 
     */
    @JsonProperty("fragmentRef")
    public void setFragmentRef(FragmentRef fragmentRef) {
        this.fragmentRef = fragmentRef;
    }

    /**
     * Source File Options
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("sourceFileOptions")
    public SourceFileOptions getSourceFileOptions() {
        return sourceFileOptions;
    }

    /**
     * Source File Options
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("sourceFileOptions")
    public void setSourceFileOptions(SourceFileOptions sourceFileOptions) {
        this.sourceFileOptions = sourceFileOptions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("fragmentRef", fragmentRef).append("sourceFileOptions", sourceFileOptions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(fragmentRef).append(sourceFileOptions).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Source) == false) {
            return false;
        }
        Source rhs = ((Source) other);
        return new EqualsBuilder().append(fragmentRef, rhs.fragmentRef).append(sourceFileOptions, rhs.sourceFileOptions).isEquals();
    }

}
