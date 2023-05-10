
package com.sos.joc.model.security.fido2;

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
    "link",
    "body",
    "to"
})
public class Fido2EmailSettings {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("link")
    private String link;
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
    @JsonProperty("to")
    private String to;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Fido2EmailSettings() {
    }

    /**
     * 
     * @param link
     * @param to
     * @param body
     */
    public Fido2EmailSettings(String link, String body, String to) {
        super();
        this.link = link;
        this.body = body;
        this.to = to;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("link")
    public String getLink() {
        return link;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("link")
    public void setLink(String link) {
        this.link = link;
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
    @JsonProperty("to")
    public String getTo() {
        return to;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("to")
    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("link", link).append("body", body).append("to", to).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(link).append(to).append(body).toHashCode();
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
        return new EqualsBuilder().append(link, rhs.link).append(to, rhs.to).append(body, rhs.body).isEquals();
    }

}
