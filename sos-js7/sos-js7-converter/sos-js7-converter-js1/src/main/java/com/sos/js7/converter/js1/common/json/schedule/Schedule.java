
package com.sos.js7.converter.js1.common.json.schedule;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.sos.js7.converter.js1.common.json.IJSObject;


/**
 * schedule
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "schedule")
@JsonPropertyOrder({
    "validFrom",
    "validTo",
    "substitute",
    "title"
})
public class Schedule
    extends AbstractSchedule
    implements IJSObject
{

    /**
     * yyyy-mm-dd HH:MM[:SS]
     * 
     */
    @JsonProperty("validFrom")
    @JsonPropertyDescription("yyyy-mm-dd HH:MM[:SS]")
    @JacksonXmlProperty(localName = "valid_from", isAttribute = true)
    private String validFrom;
    /**
     * yyyy-mm-dd HH:MM[:SS]
     * 
     */
    @JsonProperty("validTo")
    @JsonPropertyDescription("yyyy-mm-dd HH:MM[:SS]")
    @JacksonXmlProperty(localName = "valid_to", isAttribute = true)
    private String validTo;
    /**
     * path to another schedule
     * 
     */
    @JsonProperty("substitute")
    @JsonPropertyDescription("path to another schedule")
    @JacksonXmlProperty(localName = "substitute", isAttribute = true)
    private String substitute;
    @JsonProperty("title")
    @JacksonXmlProperty(localName = "title", isAttribute = true)
    private String title;

    /**
     * yyyy-mm-dd HH:MM[:SS]
     * 
     */
    @JsonProperty("validFrom")
    @JacksonXmlProperty(localName = "valid_from", isAttribute = true)
    public String getValidFrom() {
        return validFrom;
    }

    /**
     * yyyy-mm-dd HH:MM[:SS]
     * 
     */
    @JsonProperty("validFrom")
    @JacksonXmlProperty(localName = "valid_from", isAttribute = true)
    public void setValidFrom(String validFrom) {
        this.validFrom = validFrom;
    }

    /**
     * yyyy-mm-dd HH:MM[:SS]
     * 
     */
    @JsonProperty("validTo")
    @JacksonXmlProperty(localName = "valid_to", isAttribute = true)
    public String getValidTo() {
        return validTo;
    }

    /**
     * yyyy-mm-dd HH:MM[:SS]
     * 
     */
    @JsonProperty("validTo")
    @JacksonXmlProperty(localName = "valid_to", isAttribute = true)
    public void setValidTo(String validTo) {
        this.validTo = validTo;
    }

    /**
     * path to another schedule
     * 
     */
    @JsonProperty("substitute")
    @JacksonXmlProperty(localName = "substitute", isAttribute = true)
    public String getSubstitute() {
        return substitute;
    }

    /**
     * path to another schedule
     * 
     */
    @JsonProperty("substitute")
    @JacksonXmlProperty(localName = "substitute", isAttribute = true)
    public void setSubstitute(String substitute) {
        this.substitute = substitute;
    }

    @JsonProperty("title")
    @JacksonXmlProperty(localName = "title", isAttribute = true)
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    @JacksonXmlProperty(localName = "title", isAttribute = true)
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("validFrom", validFrom).append("validTo", validTo).append("substitute", substitute).append("title", title).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(validFrom).append(title).append(substitute).append(validTo).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Schedule) == false) {
            return false;
        }
        Schedule rhs = ((Schedule) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(validFrom, rhs.validFrom).append(title, rhs.title).append(substitute, rhs.substitute).append(validTo, rhs.validTo).isEquals();
    }

}
