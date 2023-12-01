
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
    "charset",
    "encoding",
    "contentType",
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
    @JsonProperty("charset")
    private String charset;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("encoding")
    private String encoding;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("contentType")
    private String contentType;
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
    @JsonProperty("charset")
    public String getCharset() {
        return charset;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("charset")
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("encoding")
    public String getEncoding() {
        return encoding;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("encoding")
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("contentType")
    public String getContentType() {
        return contentType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("contentType")
    public void setContentType(String contentType) {
        this.contentType = contentType;
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
        return new ToStringBuilder(this).append("jobResourceName", jobResourceName).append("subject", subject).append("charset", charset).append("encoding", encoding).append("contentType", contentType).append("recipient", recipient).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(charset).append(subject).append(recipient).append(jobResourceName).append(encoding).append(contentType).toHashCode();
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
        return new EqualsBuilder().append(charset, rhs.charset).append(subject, rhs.subject).append(recipient, rhs.recipient).append(jobResourceName, rhs.jobResourceName).append(encoding, rhs.encoding).append(contentType, rhs.contentType).isEquals();
    }

}
