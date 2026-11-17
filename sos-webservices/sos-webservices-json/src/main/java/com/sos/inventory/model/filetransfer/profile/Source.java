
package com.sos.inventory.model.filetransfer.profile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.profile.fragments.Fragment;
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
    "fragment",
    "sourceFileOptions"
})
public class Source {

    /**
     * Fragment Reference
     * <p>
     * 
     * 
     */
    @JsonProperty("fragment")
    private Fragment fragment;
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
     * @param fragment
     */
    public Source(Fragment fragment, SourceFileOptions sourceFileOptions) {
        super();
        this.fragment = fragment;
        this.sourceFileOptions = sourceFileOptions;
    }

    /**
     * Fragment Reference
     * <p>
     * 
     * 
     */
    @JsonProperty("fragment")
    public Fragment getFragment() {
        return fragment;
    }

    /**
     * Fragment Reference
     * <p>
     * 
     * 
     */
    @JsonProperty("fragment")
    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
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
        return new ToStringBuilder(this).append("fragment", fragment).append("sourceFileOptions", sourceFileOptions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(sourceFileOptions).append(fragment).toHashCode();
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
        return new EqualsBuilder().append(sourceFileOptions, rhs.sourceFileOptions).append(fragment, rhs.fragment).isEquals();
    }

}
