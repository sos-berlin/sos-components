
package com.sos.joc.model.jobChain;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * fileOrderSource (volatile part)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "directory",
    "regex",
    "files",
    "repeat",
    "delayAfterError",
    "alertWhenDirectoryMissing"
})
public class FileWatchingNodeV {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("directory")
    @JacksonXmlProperty(localName = "directory")
    private String directory;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("regex")
    @JacksonXmlProperty(localName = "regex")
    private String regex;
    @JsonProperty("files")
    @JacksonXmlProperty(localName = "file")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "files")
    private List<FileWatchingNodeFile> files = new ArrayList<FileWatchingNodeFile>();
    @JsonProperty("repeat")
    @JacksonXmlProperty(localName = "repeat")
    private Integer repeat;
    @JsonProperty("delayAfterError")
    @JacksonXmlProperty(localName = "delayAfterError")
    private Integer delayAfterError;
    @JsonProperty("alertWhenDirectoryMissing")
    @JacksonXmlProperty(localName = "alertWhenDirectoryMissing")
    private Boolean alertWhenDirectoryMissing;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("directory")
    @JacksonXmlProperty(localName = "directory")
    public String getDirectory() {
        return directory;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("directory")
    @JacksonXmlProperty(localName = "directory")
    public void setDirectory(String directory) {
        this.directory = directory;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("regex")
    @JacksonXmlProperty(localName = "regex")
    public String getRegex() {
        return regex;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("regex")
    @JacksonXmlProperty(localName = "regex")
    public void setRegex(String regex) {
        this.regex = regex;
    }

    @JsonProperty("files")
    @JacksonXmlProperty(localName = "file")
    public List<FileWatchingNodeFile> getFiles() {
        return files;
    }

    @JsonProperty("files")
    @JacksonXmlProperty(localName = "file")
    public void setFiles(List<FileWatchingNodeFile> files) {
        this.files = files;
    }

    @JsonProperty("repeat")
    @JacksonXmlProperty(localName = "repeat")
    public Integer getRepeat() {
        return repeat;
    }

    @JsonProperty("repeat")
    @JacksonXmlProperty(localName = "repeat")
    public void setRepeat(Integer repeat) {
        this.repeat = repeat;
    }

    @JsonProperty("delayAfterError")
    @JacksonXmlProperty(localName = "delayAfterError")
    public Integer getDelayAfterError() {
        return delayAfterError;
    }

    @JsonProperty("delayAfterError")
    @JacksonXmlProperty(localName = "delayAfterError")
    public void setDelayAfterError(Integer delayAfterError) {
        this.delayAfterError = delayAfterError;
    }

    @JsonProperty("alertWhenDirectoryMissing")
    @JacksonXmlProperty(localName = "alertWhenDirectoryMissing")
    public Boolean getAlertWhenDirectoryMissing() {
        return alertWhenDirectoryMissing;
    }

    @JsonProperty("alertWhenDirectoryMissing")
    @JacksonXmlProperty(localName = "alertWhenDirectoryMissing")
    public void setAlertWhenDirectoryMissing(Boolean alertWhenDirectoryMissing) {
        this.alertWhenDirectoryMissing = alertWhenDirectoryMissing;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("directory", directory).append("regex", regex).append("files", files).append("repeat", repeat).append("delayAfterError", delayAfterError).append("alertWhenDirectoryMissing", alertWhenDirectoryMissing).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(regex).append(repeat).append(files).append(delayAfterError).append(alertWhenDirectoryMissing).append(directory).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FileWatchingNodeV) == false) {
            return false;
        }
        FileWatchingNodeV rhs = ((FileWatchingNodeV) other);
        return new EqualsBuilder().append(regex, rhs.regex).append(repeat, rhs.repeat).append(files, rhs.files).append(delayAfterError, rhs.delayAfterError).append(alertWhenDirectoryMissing, rhs.alertWhenDirectoryMissing).append(directory, rhs.directory).isEquals();
    }

}
