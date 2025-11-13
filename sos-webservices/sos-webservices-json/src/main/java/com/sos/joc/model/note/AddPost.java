
package com.sos.joc.model.note;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.note.common.ModifyRequest;
import com.sos.joc.model.note.common.Severity;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * add post
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "content",
    "severity"
})
public class AddPost
    extends ModifyRequest
{

    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("content")
    private String content;
    /**
     * note/post severity
     * <p>
     * 
     * 
     */
    @JsonProperty("severity")
    private Severity severity = Severity.fromValue("NORMAL");

    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("content")
    public String getContent() {
        return content;
    }

    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("content")
    public void setContent(String content) {
        this.content = content;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("content", content).append("severity", severity).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(severity).append(content).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AddPost) == false) {
            return false;
        }
        AddPost rhs = ((AddPost) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(severity, rhs.severity).append(content, rhs.content).isEquals();
    }

}
