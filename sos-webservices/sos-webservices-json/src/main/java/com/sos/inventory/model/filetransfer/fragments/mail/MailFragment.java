
package com.sos.inventory.model.filetransfer.fragments.mail;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.fragments.AFragment;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Mail Fragment
 * <p>
 * Email notification settings.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "jobResourceNames",
    "header",
    "attachment",
    "body",
    "contentType",
    "encoding"
})
public class MailFragment
    extends AFragment
{

    /**
     * Names of Job Resources that provide mail settings.
     * (Required)
     * 
     */
    @JsonProperty("jobResourceNames")
    @JsonPropertyDescription("Names of Job Resources that provide mail settings.")
    private List<String> jobResourceNames = null;
    /**
     * Mail header settings.
     * 
     */
    @JsonProperty("header")
    @JsonPropertyDescription("Mail header settings.")
    private MailFragmentHeader header;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("attachment")
    private String attachment;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("body")
    private String body;
    /**
     * Specifies the MIME type of the mail body.
     * <p>
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * 
     */
    @JsonProperty("contentType")
    @JsonPropertyDescription("Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.")
    private String contentType = "text/plain";
    /**
     * Specifies the mail body encoding: '7bit', 'Quoted-Printable' or 'Base64'. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("encoding")
    private String encoding = "7bit";

    /**
     * No args constructor for use in serialization
     * 
     */
    public MailFragment() {
    }

    /**
     * 
     * @param attachment
     * @param jobResourceNames
     * @param header
     * @param body
     * @param encoding
     * @param contentType
     */
    public MailFragment(List<String> jobResourceNames, MailFragmentHeader header, String attachment, String body, String contentType, String encoding) {
        super();
        this.jobResourceNames = jobResourceNames;
        this.header = header;
        this.attachment = attachment;
        this.body = body;
        this.contentType = contentType;
        this.encoding = encoding;
    }

    /**
     * Names of Job Resources that provide mail settings.
     * (Required)
     * 
     */
    @JsonProperty("jobResourceNames")
    public List<String> getJobResourceNames() {
        return jobResourceNames;
    }

    /**
     * Names of Job Resources that provide mail settings.
     * (Required)
     * 
     */
    @JsonProperty("jobResourceNames")
    public void setJobResourceNames(List<String> jobResourceNames) {
        this.jobResourceNames = jobResourceNames;
    }

    /**
     * Mail header settings.
     * 
     */
    @JsonProperty("header")
    public MailFragmentHeader getHeader() {
        return header;
    }

    /**
     * Mail header settings.
     * 
     */
    @JsonProperty("header")
    public void setHeader(MailFragmentHeader header) {
        this.header = header;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("attachment")
    public String getAttachment() {
        return attachment;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("attachment")
    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("body")
    public String getBody() {
        return body;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("body")
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Specifies the MIME type of the mail body.
     * <p>
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * 
     */
    @JsonProperty("contentType")
    public String getContentType() {
        return contentType;
    }

    /**
     * Specifies the MIME type of the mail body.
     * <p>
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * 
     */
    @JsonProperty("contentType")
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Specifies the mail body encoding: '7bit', 'Quoted-Printable' or 'Base64'. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("encoding")
    public String getEncoding() {
        return encoding;
    }

    /**
     * Specifies the mail body encoding: '7bit', 'Quoted-Printable' or 'Base64'. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("encoding")
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("jobResourceNames", jobResourceNames).append("header", header).append("attachment", attachment).append("body", body).append("contentType", contentType).append("encoding", encoding).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(attachment).append(jobResourceNames).append(header).append(body).append(encoding).append(contentType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof MailFragment) == false) {
            return false;
        }
        MailFragment rhs = ((MailFragment) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(attachment, rhs.attachment).append(jobResourceNames, rhs.jobResourceNames).append(header, rhs.header).append(body, rhs.body).append(encoding, rhs.encoding).append(contentType, rhs.contentType).isEquals();
    }

}
