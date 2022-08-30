
package com.sos.js7.converter.js1.common.json.schedule;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "at")
@JsonPropertyOrder({
    "at"
})
public class At {

    /**
     * yyyy-mm-dd HH:MM[:SS]
     * 
     */
    @JsonProperty("at")
    @JsonPropertyDescription("yyyy-mm-dd HH:MM[:SS]")
    @JacksonXmlProperty(localName = "at", isAttribute = true)
    private String at;

    /**
     * yyyy-mm-dd HH:MM[:SS]
     * 
     */
    @JsonProperty("at")
    @JacksonXmlProperty(localName = "at", isAttribute = true)
    public String getAt() {
        return at;
    }

    /**
     * yyyy-mm-dd HH:MM[:SS]
     * 
     */
    @JsonProperty("at")
    @JacksonXmlProperty(localName = "at", isAttribute = true)
    public void setAt(String at) {
        this.at = at;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("at", at).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(at).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof At) == false) {
            return false;
        }
        At rhs = ((At) other);
        return new EqualsBuilder().append(at, rhs.at).isEquals();
    }

}
