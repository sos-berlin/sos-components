
package com.sos.inventory.model.filetransfer.profile.source;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "fileSpec",
    "directory",
    "excludedDirectories",
    "recursive"
})
public class FileSpecSelection {

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("fileSpec")
    private String fileSpec;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("directory")
    private String directory;
    @JsonProperty("excludedDirectories")
    private List<String> excludedDirectories = null;
    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("recursive")
    private String recursive;

    /**
     * No args constructor for use in serialization
     * 
     */
    public FileSpecSelection() {
    }

    /**
     * 
     * @param excludedDirectories
     * @param directory
     * @param recursive
     * @param fileSpec
     */
    public FileSpecSelection(String fileSpec, String directory, List<String> excludedDirectories, String recursive) {
        super();
        this.fileSpec = fileSpec;
        this.directory = directory;
        this.excludedDirectories = excludedDirectories;
        this.recursive = recursive;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("fileSpec")
    public String getFileSpec() {
        return fileSpec;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("fileSpec")
    public void setFileSpec(String fileSpec) {
        this.fileSpec = fileSpec;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
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
     * 
     */
    @JsonProperty("directory")
    public void setDirectory(String directory) {
        this.directory = directory;
    }

    @JsonProperty("excludedDirectories")
    public List<String> getExcludedDirectories() {
        return excludedDirectories;
    }

    @JsonProperty("excludedDirectories")
    public void setExcludedDirectories(List<String> excludedDirectories) {
        this.excludedDirectories = excludedDirectories;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("recursive")
    public String getRecursive() {
        return recursive;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("recursive")
    public void setRecursive(String recursive) {
        this.recursive = recursive;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("fileSpec", fileSpec).append("directory", directory).append("excludedDirectories", excludedDirectories).append("recursive", recursive).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(excludedDirectories).append(directory).append(recursive).append(fileSpec).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FileSpecSelection) == false) {
            return false;
        }
        FileSpecSelection rhs = ((FileSpecSelection) other);
        return new EqualsBuilder().append(excludedDirectories, rhs.excludedDirectories).append(directory, rhs.directory).append(recursive, rhs.recursive).append(fileSpec, rhs.fileSpec).isEquals();
    }

}
