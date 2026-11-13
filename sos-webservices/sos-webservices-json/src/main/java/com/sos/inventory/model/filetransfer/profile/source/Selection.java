
package com.sos.inventory.model.filetransfer.profile.source;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Selection
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "filePath",
    "fileSpec",
    "fileList"
})
public class Selection {

    @JsonProperty("filePath")
    private FilePathSelection filePath;
    @JsonProperty("fileSpec")
    private FileSpecSelection fileSpec;
    @JsonProperty("fileList")
    private FileListSelection fileList;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Selection() {
    }

    /**
     * 
     * @param filePath
     * @param fileList
     * @param fileSpec
     */
    public Selection(FilePathSelection filePath, FileSpecSelection fileSpec, FileListSelection fileList) {
        super();
        this.filePath = filePath;
        this.fileSpec = fileSpec;
        this.fileList = fileList;
    }

    @JsonProperty("filePath")
    public FilePathSelection getFilePath() {
        return filePath;
    }

    @JsonProperty("filePath")
    public void setFilePath(FilePathSelection filePath) {
        this.filePath = filePath;
    }

    @JsonProperty("fileSpec")
    public FileSpecSelection getFileSpec() {
        return fileSpec;
    }

    @JsonProperty("fileSpec")
    public void setFileSpec(FileSpecSelection fileSpec) {
        this.fileSpec = fileSpec;
    }

    @JsonProperty("fileList")
    public FileListSelection getFileList() {
        return fileList;
    }

    @JsonProperty("fileList")
    public void setFileList(FileListSelection fileList) {
        this.fileList = fileList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("filePath", filePath).append("fileSpec", fileSpec).append("fileList", fileList).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(fileList).append(filePath).append(fileSpec).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Selection) == false) {
            return false;
        }
        Selection rhs = ((Selection) other);
        return new EqualsBuilder().append(fileList, rhs.fileList).append(filePath, rhs.filePath).append(fileSpec, rhs.fileSpec).isEquals();
    }

}
