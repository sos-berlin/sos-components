
package com.sos.inventory.model.filetransfer.profile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.profile.refs.FragmentRef;
import com.sos.inventory.model.filetransfer.profile.target.TargetFileOptions;
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
    "fragmentRef",
    "directory",
    "targetFileOptions"
})
public class Target {

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
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("directory")
    private String directory;
    /**
     * Target File Options
     * <p>
     * 
     * 
     */
    @JsonProperty("targetFileOptions")
    private TargetFileOptions targetFileOptions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Target() {
    }

    /**
     * 
     * @param fragmentRef
     * @param directory
     * @param targetFileOptions
     */
    public Target(FragmentRef fragmentRef, String directory, TargetFileOptions targetFileOptions) {
        super();
        this.fragmentRef = fragmentRef;
        this.directory = directory;
        this.targetFileOptions = targetFileOptions;
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
     * Target File Options
     * <p>
     * 
     * 
     */
    @JsonProperty("targetFileOptions")
    public TargetFileOptions getTargetFileOptions() {
        return targetFileOptions;
    }

    /**
     * Target File Options
     * <p>
     * 
     * 
     */
    @JsonProperty("targetFileOptions")
    public void setTargetFileOptions(TargetFileOptions targetFileOptions) {
        this.targetFileOptions = targetFileOptions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("fragmentRef", fragmentRef).append("directory", directory).append("targetFileOptions", targetFileOptions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(fragmentRef).append(directory).append(targetFileOptions).toHashCode();
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
        return new EqualsBuilder().append(fragmentRef, rhs.fragmentRef).append(directory, rhs.directory).append(targetFileOptions, rhs.targetFileOptions).isEquals();
    }

}
