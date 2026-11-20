
package com.sos.inventory.model.filetransfer.profile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.profile.fragments.Fragment;
import com.sos.inventory.model.filetransfer.profile.source.Selection;
import com.sos.inventory.model.filetransfer.profile.source.SourceOptions;
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
    "selection",
    "options"
})
public class Source {

    /**
     * Fragment Reference
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("fragment")
    private Fragment fragment;
    /**
     * Selection
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("selection")
    private Selection selection;
    /**
     * Source Options
     * <p>
     * 
     * 
     */
    @JsonProperty("options")
    private SourceOptions options;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Source() {
    }

    /**
     * 
     * @param fragment
     * @param selection
     * @param options
     */
    public Source(Fragment fragment, Selection selection, SourceOptions options) {
        super();
        this.fragment = fragment;
        this.selection = selection;
        this.options = options;
    }

    /**
     * Fragment Reference
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("fragment")
    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    /**
     * Selection
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("selection")
    public Selection getSelection() {
        return selection;
    }

    /**
     * Selection
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("selection")
    public void setSelection(Selection selection) {
        this.selection = selection;
    }

    /**
     * Source Options
     * <p>
     * 
     * 
     */
    @JsonProperty("options")
    public SourceOptions getOptions() {
        return options;
    }

    /**
     * Source Options
     * <p>
     * 
     * 
     */
    @JsonProperty("options")
    public void setOptions(SourceOptions options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("fragment", fragment).append("selection", selection).append("options", options).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(options).append(fragment).append(selection).toHashCode();
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
        return new EqualsBuilder().append(options, rhs.options).append(fragment, rhs.fragment).append(selection, rhs.selection).isEquals();
    }

}
