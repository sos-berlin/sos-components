
package com.sos.joc.model.plan;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * plan state
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "severity",
    "_text",
    "since"
})
public class PlanState {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("severity")
    private Integer severity;
    /**
     * plan state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    private PlanStateText _text;
    /**
     * since only for FINISHED state
     * 
     */
    @JsonProperty("since")
    @JsonPropertyDescription("since only for FINISHED state")
    private Date since;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public Integer getSeverity() {
        return severity;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    /**
     * plan state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public PlanStateText get_text() {
        return _text;
    }

    /**
     * plan state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public void set_text(PlanStateText _text) {
        this._text = _text;
    }

    /**
     * since only for FINISHED state
     * 
     */
    @JsonProperty("since")
    public Date getSince() {
        return since;
    }

    /**
     * since only for FINISHED state
     * 
     */
    @JsonProperty("since")
    public void setSince(Date since) {
        this.since = since;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("severity", severity).append("_text", _text).append("since", since).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(severity).append(_text).append(since).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PlanState) == false) {
            return false;
        }
        PlanState rhs = ((PlanState) other);
        return new EqualsBuilder().append(severity, rhs.severity).append(_text, rhs._text).append(since, rhs.since).isEquals();
    }

}
