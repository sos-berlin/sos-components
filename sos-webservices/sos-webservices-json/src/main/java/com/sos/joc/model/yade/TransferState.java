
package com.sos.joc.model.yade;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * transfer state
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "severity",
    "_text"
})
public class TransferState {

    /**
     *  0=SUCCESSFUL, 1=INCOMPLETE, 2=FAILED
     * (Required)
     * 
     */
    @JsonProperty("severity")
    @JsonPropertyDescription("0=SUCCESSFUL, 1=INCOMPLETE, 2=FAILED")
    private Integer severity;
    /**
     * transfer state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    private TransferStateText _text;

    /**
     *  0=SUCCESSFUL, 1=INCOMPLETE, 2=FAILED
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public Integer getSeverity() {
        return severity;
    }

    /**
     *  0=SUCCESSFUL, 1=INCOMPLETE, 2=FAILED
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    /**
     * transfer state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public TransferStateText get_text() {
        return _text;
    }

    /**
     * transfer state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public void set_text(TransferStateText _text) {
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
        if ((other instanceof TransferState) == false) {
            return false;
        }
        TransferState rhs = ((TransferState) other);
        return new EqualsBuilder().append(severity, rhs.severity).append(_text, rhs._text).isEquals();
    }

}
