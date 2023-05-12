
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
    "url",
    "body",
    "subject",
    "nameOfJobResource"
})
public class Fido2EmailSettings {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("url")
    private String url;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("body")
    private String body;
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
     * @param subject
     * @param nameOfJobResource
     * @param body
     * @param url
     */
    public Fido2EmailSettings(String url, String body, String subject, String nameOfJobResource) {
        super();
        this.url = url;
        this.body = body;
        this.subject = subject;
        this.nameOfJobResource = nameOfJobResource;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("body")
    public String getBody() {
        return body;
    }

    /**
     * string without < and >
     * <p>
     * 
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
        return new ToStringBuilder(this).append("url", url).append("body", body).append("subject", subject).append("nameOfJobResource", nameOfJobResource).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(nameOfJobResource).append(body).append(url).append(subject).toHashCode();
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
        return new EqualsBuilder().append(nameOfJobResource, rhs.nameOfJobResource).append(body, rhs.body).append(url, rhs.url).append(subject, rhs.subject).isEquals();
    }

}
