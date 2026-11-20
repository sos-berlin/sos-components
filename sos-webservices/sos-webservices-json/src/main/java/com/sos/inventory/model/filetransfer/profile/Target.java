
package com.sos.inventory.model.filetransfer.profile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.profile.fragments.Fragment;
import com.sos.inventory.model.filetransfer.profile.target.TargetOptions;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Target
 * <p>
 * Target of a file transfer operation.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "fragment",
    "directory",
    "options"
})
public class Target {

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
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("directory")
    private String directory;
    /**
     * Target Options
     * <p>
     * 
     * 
     */
    @JsonProperty("options")
    private TargetOptions options;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Target() {
    }

    /**
     * 
     * @param fragment
     * @param options
     * @param directory
     */
    public Target(Fragment fragment, String directory, TargetOptions options) {
        super();
        this.fragment = fragment;
        this.directory = directory;
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
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("directory")
    public String getDirectory() {
        return directory;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("directory")
    public void setDirectory(String directory) {
        this.directory = directory;
    }

    /**
     * Target Options
     * <p>
     * 
     * 
     */
    @JsonProperty("options")
    public TargetOptions getOptions() {
        return options;
    }

    /**
     * Target Options
     * <p>
     * 
     * 
     */
    @JsonProperty("options")
    public void setOptions(TargetOptions options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("fragment", fragment).append("directory", directory).append("options", options).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(options).append(fragment).append(directory).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Target) == false) {
            return false;
        }
        Target rhs = ((Target) other);
        return new EqualsBuilder().append(options, rhs.options).append(fragment, rhs.fragment).append(directory, rhs.directory).isEquals();
    }

}
