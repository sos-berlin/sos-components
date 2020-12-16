
package com.sos.joc.model.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ExportFile
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "filename",
    "format"
})
public class ExportFile {

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("filename")
    private String filename;
    /**
     * Archive Format of the archive file
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("format")
    private ArchiveFormat format = ArchiveFormat.fromValue("ZIP");

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("filename")
    public String getFilename() {
        return filename;
    }

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("filename")
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Archive Format of the archive file
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("format")
    public ArchiveFormat getFormat() {
        return format;
    }

    /**
     * Archive Format of the archive file
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("format")
    public void setFormat(ArchiveFormat format) {
        this.format = format;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("filename", filename).append("format", format).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(format).append(filename).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExportFile) == false) {
            return false;
        }
        ExportFile rhs = ((ExportFile) other);
        return new EqualsBuilder().append(format, rhs.format).append(filename, rhs.filename).isEquals();
    }

}
