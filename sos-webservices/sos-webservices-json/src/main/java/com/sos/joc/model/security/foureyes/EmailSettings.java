
package com.sos.joc.model.security.foureyes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Approval Email Settings
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "body",
    "subject",
    "cc",
    "bcc",
    "contentType",
    "charset",
    "encoding",
    "priority",
    "jobResourceName"
})
public class EmailSettings {

    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("body")
    private String body;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
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
    @JsonProperty("cc")
    private String cc;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bcc")
    private String bcc;
    @JsonProperty("contentType")
    private String contentType = "text/html";
    @JsonProperty("charset")
    private String charset = "ISO-8859-1";
    @JsonProperty("encoding")
    private String encoding = "7-bit";
    @JsonProperty("priority")
    private EmailPriority priority = EmailPriority.fromValue("NORMAL");
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobResourceName")
    private String jobResourceName;

    /**
     * No args constructor for use in serialization
     * 
     */
    public EmailSettings() {
    }

    /**
     * 
     * @param cc
     * @param charset
     * @param bcc
     * @param subject
     * @param jobResourceName
     * @param body
     * @param encoding
     * @param priority
     * @param contentType
     */
    public EmailSettings(String body, String subject, String cc, String bcc, String contentType, String charset, String encoding, EmailPriority priority, String jobResourceName) {
        super();
        this.body = body;
        this.subject = subject;
        this.cc = cc;
        this.bcc = bcc;
        this.contentType = contentType;
        this.charset = charset;
        this.encoding = encoding;
        this.priority = priority;
        this.jobResourceName = jobResourceName;
    }

    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("body")
    public String getBody() {
        return body;
    }

    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("body")
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
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
     * (Required)
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
    @JsonProperty("cc")
    public String getCc() {
        return cc;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("cc")
    public void setCc(String cc) {
        this.cc = cc;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bcc")
    public String getBcc() {
        return bcc;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bcc")
    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    @JsonProperty("contentType")
    public String getContentType() {
        return contentType;
    }

    @JsonProperty("contentType")
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @JsonProperty("charset")
    public String getCharset() {
        return charset;
    }

    @JsonProperty("charset")
    public void setCharset(String charset) {
        this.charset = charset;
    }

    @JsonProperty("encoding")
    public String getEncoding() {
        return encoding;
    }

    @JsonProperty("encoding")
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @JsonProperty("priority")
    public EmailPriority getPriority() {
        return priority;
    }

    @JsonProperty("priority")
    public void setPriority(EmailPriority priority) {
        this.priority = priority;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("jobResourceName")
    public void setJobResourceName(String jobResourceName) {
        this.jobResourceName = jobResourceName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("body", body).append("subject", subject).append("cc", cc).append("bcc", bcc).append("contentType", contentType).append("charset", charset).append("encoding", encoding).append("priority", priority).append("jobResourceName", jobResourceName).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(cc).append(charset).append(bcc).append(subject).append(jobResourceName).append(body).append(encoding).append(priority).append(contentType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof EmailSettings) == false) {
            return false;
        }
        EmailSettings rhs = ((EmailSettings) other);
        return new EqualsBuilder().append(cc, rhs.cc).append(charset, rhs.charset).append(bcc, rhs.bcc).append(subject, rhs.subject).append(jobResourceName, rhs.jobResourceName).append(body, rhs.body).append(encoding, rhs.encoding).append(priority, rhs.priority).append(contentType, rhs.contentType).isEquals();
    }

}
