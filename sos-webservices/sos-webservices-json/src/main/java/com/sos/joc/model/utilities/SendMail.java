
package com.sos.joc.model.utilities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Send Mail
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobResourceName",
    "subject",
    "recipient"
})
public class SendMail {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobResourceName")
    private String jobResourceName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("subject")
    private String subject;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("recipient")
    private String recipient;

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobResourceName")
    public String getJobResourceName() {
        return jobResourceName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobResourceName")
    public void setJobResourceName(String jobResourceName) {
        this.jobResourceName = jobResourceName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("subject")
    public String getSubject() {
        return subject;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("subject")
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("recipient")
    public String getRecipient() {
        return recipient;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("recipient")
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobResourceName", jobResourceName).append("subject", subject).append("recipient", recipient).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(recipient).append(jobResourceName).append(subject).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SendMail) == false) {
            return false;
        }
        SendMail rhs = ((SendMail) other);
        return new EqualsBuilder().append(recipient, rhs.recipient).append(jobResourceName, rhs.jobResourceName).append(subject, rhs.subject).isEquals();
    }

}
