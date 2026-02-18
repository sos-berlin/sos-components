
package com.sos.joc.model.note.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * metadata
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "severity",
    "notified"
})
public class HasNote {

    /**
     * note/post severity
     * <p>
     * 
     * 
     */
    @JsonProperty("severity")
    private Severity severity = Severity.fromValue("NORMAL");
    @JsonProperty("notified")
    private Boolean notified;

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

    @JsonProperty("notified")
    public Boolean getNotified() {
        return notified;
    }

    @JsonProperty("notified")
    public void setNotified(Boolean notified) {
        this.notified = notified;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("severity", severity).append("notified", notified).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(severity).append(notified).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof HasNote) == false) {
            return false;
        }
        HasNote rhs = ((HasNote) other);
        return new EqualsBuilder().append(severity, rhs.severity).append(notified, rhs.notified).isEquals();
    }

}
