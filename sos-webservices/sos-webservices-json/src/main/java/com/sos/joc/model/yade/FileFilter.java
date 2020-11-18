
package com.sos.joc.model.yade;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.LogMime;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * yade filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "fileId",
    "compact",
    "mime"
})
public class FileFilter {

    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("fileId")
    private Long fileId;
    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    @JsonPropertyDescription("controls if the object's data is compact or detailed")
    private Boolean compact = false;
    /**
     * log mime filter
     * <p>
     * The log can have a HTML representation where the HTML gets a highlighting via CSS classes.
     * 
     */
    @JsonProperty("mime")
    @JsonPropertyDescription("The log can have a HTML representation where the HTML gets a highlighting via CSS classes.")
    private LogMime mime = LogMime.fromValue("PLAIN");

    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("fileId")
    public Long getFileId() {
        return fileId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("fileId")
    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public Boolean getCompact() {
        return compact;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public void setCompact(Boolean compact) {
        this.compact = compact;
    }

    /**
     * log mime filter
     * <p>
     * The log can have a HTML representation where the HTML gets a highlighting via CSS classes.
     * 
     */
    @JsonProperty("mime")
    public LogMime getMime() {
        return mime;
    }

    /**
     * log mime filter
     * <p>
     * The log can have a HTML representation where the HTML gets a highlighting via CSS classes.
     * 
     */
    @JsonProperty("mime")
    public void setMime(LogMime mime) {
        this.mime = mime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("fileId", fileId).append("compact", compact).append("mime", mime).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(compact).append(fileId).append(mime).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FileFilter) == false) {
            return false;
        }
        FileFilter rhs = ((FileFilter) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(compact, rhs.compact).append(fileId, rhs.fileId).append(mime, rhs.mime).isEquals();
    }

}
