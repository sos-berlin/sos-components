
package com.sos.joc.model.note;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.note.common.NoteIdentifier;
import com.sos.joc.model.note.common.Severity;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * note notification
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "severity"
})
public class Notification
    extends NoteIdentifier
{

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of an object.")
    private String path;
    /**
     * note/post severity
     * <p>
     * 
     * 
     */
    @JsonProperty("severity")
    private Severity severity = Severity.fromValue("NORMAL");

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * note/post severity
     * <p>
     * 
     * 
     */
    @JsonProperty("severity")
    public Severity getSeverity() {
        return severity;
    }

    /**
     * note/post severity
     * <p>
     * 
     * 
     */
    @JsonProperty("severity")
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("path", path).append("severity", severity).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(severity).append(path).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Notification) == false) {
            return false;
        }
        Notification rhs = ((Notification) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(severity, rhs.severity).append(path, rhs.path).isEquals();
    }

}
