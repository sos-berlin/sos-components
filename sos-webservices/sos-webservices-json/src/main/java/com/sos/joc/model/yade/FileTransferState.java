
package com.sos.joc.model.yade;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * state for each transferred file
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "severity",
    "_text"
})
public class FileTransferState {

    /**
     *  0=transferred,compressed,remaned,success 1=skipped,ignored_due_to_zerobyte_contraint,not_overwritten, 3=undefined, 2=failed,aborted,deleted, 5=waiting,transferring,in_progress,setback,polling
     * (Required)
     * 
     */
    @JsonProperty("severity")
    @JsonPropertyDescription("0=transferred,compressed,remaned,success 1=skipped,ignored_due_to_zerobyte_contraint,not_overwritten, 3=undefined, 2=failed,aborted,deleted, 5=waiting,transferring,in_progress,setback,polling")
    private Integer severity;
    /**
     * state text for each transferred file
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    private FileTransferStateText _text;

    /**
     *  0=transferred,compressed,remaned,success 1=skipped,ignored_due_to_zerobyte_contraint,not_overwritten, 3=undefined, 2=failed,aborted,deleted, 5=waiting,transferring,in_progress,setback,polling
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public Integer getSeverity() {
        return severity;
    }

    /**
     *  0=transferred,compressed,remaned,success 1=skipped,ignored_due_to_zerobyte_contraint,not_overwritten, 3=undefined, 2=failed,aborted,deleted, 5=waiting,transferring,in_progress,setback,polling
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    /**
     * state text for each transferred file
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public FileTransferStateText get_text() {
        return _text;
    }

    /**
     * state text for each transferred file
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public void set_text(FileTransferStateText _text) {
        this._text = _text;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("severity", severity).append("_text", _text).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(severity).append(_text).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FileTransferState) == false) {
            return false;
        }
        FileTransferState rhs = ((FileTransferState) other);
        return new EqualsBuilder().append(severity, rhs.severity).append(_text, rhs._text).isEquals();
    }

}
