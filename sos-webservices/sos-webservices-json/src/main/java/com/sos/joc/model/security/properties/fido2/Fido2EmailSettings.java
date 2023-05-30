
package com.sos.joc.model.security.properties.fido2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Fido2 Email Settings
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "bodyRegistration",
    "subjectRegistration",
    "bodyAccess",
    "subjectAccess",
    "contentType",
    "charset",
    "encoding",
    "priority",
    "nameOfJobResource"
})
public class Fido2EmailSettings {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bodyRegistration")
    private String bodyRegistration;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("subjectRegistration")
    private String subjectRegistration;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bodyAccess")
    private String bodyAccess;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("subjectAccess")
    private String subjectAccess;
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
    @JsonProperty("priority")
    private String priority;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("nameOfJobResource")
    private String nameOfJobResource;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Fido2EmailSettings() {
    }

    /**
     * 
     * @param bodyRegistration
     * @param charset
     * @param subjectRegistration
     * @param subjectAccess
     * @param bodyAccess
     * @param nameOfJobResource
     * @param encoding
     * @param priority
     * @param contentType
     */
    public Fido2EmailSettings(String bodyRegistration, String subjectRegistration, String bodyAccess, String subjectAccess, String contentType, String charset, String encoding, String priority, String nameOfJobResource) {
        super();
        this.bodyRegistration = bodyRegistration;
        this.subjectRegistration = subjectRegistration;
        this.bodyAccess = bodyAccess;
        this.subjectAccess = subjectAccess;
        this.contentType = contentType;
        this.charset = charset;
        this.encoding = encoding;
        this.priority = priority;
        this.nameOfJobResource = nameOfJobResource;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bodyRegistration")
    public String getBodyRegistration() {
        return bodyRegistration;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bodyRegistration")
    public void setBodyRegistration(String bodyRegistration) {
        this.bodyRegistration = bodyRegistration;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("subjectRegistration")
    public String getSubjectRegistration() {
        return subjectRegistration;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("subjectRegistration")
    public void setSubjectRegistration(String subjectRegistration) {
        this.subjectRegistration = subjectRegistration;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bodyAccess")
    public String getBodyAccess() {
        return bodyAccess;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bodyAccess")
    public void setBodyAccess(String bodyAccess) {
        this.bodyAccess = bodyAccess;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("subjectAccess")
    public String getSubjectAccess() {
        return subjectAccess;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("subjectAccess")
    public void setSubjectAccess(String subjectAccess) {
        this.subjectAccess = subjectAccess;
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
    @JsonProperty("priority")
    public String getPriority() {
        return priority;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("priority")
    public void setPriority(String priority) {
        this.priority = priority;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("nameOfJobResource")
    public String getNameOfJobResource() {
        return nameOfJobResource;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("nameOfJobResource")
    public void setNameOfJobResource(String nameOfJobResource) {
        this.nameOfJobResource = nameOfJobResource;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("bodyRegistration", bodyRegistration).append("subjectRegistration", subjectRegistration).append("bodyAccess", bodyAccess).append("subjectAccess", subjectAccess).append("contentType", contentType).append("charset", charset).append("encoding", encoding).append("priority", priority).append("nameOfJobResource", nameOfJobResource).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(bodyRegistration).append(charset).append(subjectRegistration).append(subjectAccess).append(bodyAccess).append(nameOfJobResource).append(encoding).append(priority).append(contentType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Fido2EmailSettings) == false) {
            return false;
        }
        Fido2EmailSettings rhs = ((Fido2EmailSettings) other);
        return new EqualsBuilder().append(bodyRegistration, rhs.bodyRegistration).append(charset, rhs.charset).append(subjectRegistration, rhs.subjectRegistration).append(subjectAccess, rhs.subjectAccess).append(bodyAccess, rhs.bodyAccess).append(nameOfJobResource, rhs.nameOfJobResource).append(encoding, rhs.encoding).append(priority, rhs.priority).append(contentType, rhs.contentType).isEquals();
    }

}
