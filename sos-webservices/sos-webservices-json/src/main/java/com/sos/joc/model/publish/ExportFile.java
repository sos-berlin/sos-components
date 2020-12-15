
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
    "name",
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
    @JsonProperty("name")
    private String name;
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
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
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
        return new ToStringBuilder(this).append("name", name).append("format", format).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(format).toHashCode();
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
        return new EqualsBuilder().append(name, rhs.name).append(format, rhs.format).isEquals();
    }

}
