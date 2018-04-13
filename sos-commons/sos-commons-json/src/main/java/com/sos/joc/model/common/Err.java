
package com.sos.joc.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * error
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "code",
    "message"
})
public class Err {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("code")
    @JacksonXmlProperty(localName = "code")
    private String code;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("message")
    @JacksonXmlProperty(localName = "message")
    private String message;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("code")
    @JacksonXmlProperty(localName = "code")
    public String getCode() {
        return code;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("code")
    @JacksonXmlProperty(localName = "code")
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("message")
    @JacksonXmlProperty(localName = "message")
    public String getMessage() {
        return message;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("message")
    @JacksonXmlProperty(localName = "message")
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("code", code).append("message", message).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(message).append(code).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Err) == false) {
            return false;
        }
        Err rhs = ((Err) other);
        return new EqualsBuilder().append(message, rhs.message).append(code, rhs.code).isEquals();
    }

}
