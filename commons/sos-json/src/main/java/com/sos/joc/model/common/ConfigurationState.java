
package com.sos.joc.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * configuration status
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "severity",
    "_text",
    "message"
})
public class ConfigurationState {

    /**
     *  4=ok; 5=replacement_is_standing_by,removing_delayed; 2=error_in_configuration_file,changed_file_not_loaded,resource_is_missing
     * (Required)
     * 
     */
    @JsonProperty("severity")
    @JsonPropertyDescription("4=ok; 5=replacement_is_standing_by,removing_delayed; 2=error_in_configuration_file,changed_file_not_loaded,resource_is_missing")
    @JacksonXmlProperty(localName = "severity")
    private Integer severity;
    /**
     * configuration status
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    @JacksonXmlProperty(localName = "_text")
    private ConfigurationStateText _text;
    /**
     * contains e.g. error message
     * 
     */
    @JsonProperty("message")
    @JsonPropertyDescription("contains e.g. error message")
    @JacksonXmlProperty(localName = "message")
    private String message;

    /**
     *  4=ok; 5=replacement_is_standing_by,removing_delayed; 2=error_in_configuration_file,changed_file_not_loaded,resource_is_missing
     * (Required)
     * 
     */
    @JsonProperty("severity")
    @JacksonXmlProperty(localName = "severity")
    public Integer getSeverity() {
        return severity;
    }

    /**
     *  4=ok; 5=replacement_is_standing_by,removing_delayed; 2=error_in_configuration_file,changed_file_not_loaded,resource_is_missing
     * (Required)
     * 
     */
    @JsonProperty("severity")
    @JacksonXmlProperty(localName = "severity")
    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    /**
     * configuration status
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    @JacksonXmlProperty(localName = "_text")
    public ConfigurationStateText get_text() {
        return _text;
    }

    /**
     * configuration status
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    @JacksonXmlProperty(localName = "_text")
    public void set_text(ConfigurationStateText _text) {
        this._text = _text;
    }

    /**
     * contains e.g. error message
     * 
     */
    @JsonProperty("message")
    @JacksonXmlProperty(localName = "message")
    public String getMessage() {
        return message;
    }

    /**
     * contains e.g. error message
     * 
     */
    @JsonProperty("message")
    @JacksonXmlProperty(localName = "message")
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("severity", severity).append("_text", _text).append("message", message).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(severity).append(_text).append(message).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ConfigurationState) == false) {
            return false;
        }
        ConfigurationState rhs = ((ConfigurationState) other);
        return new EqualsBuilder().append(severity, rhs.severity).append(_text, rhs._text).append(message, rhs.message).isEquals();
    }

}
