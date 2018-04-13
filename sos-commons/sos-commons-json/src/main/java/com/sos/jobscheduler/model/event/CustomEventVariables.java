
package com.sos.jobscheduler.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "source"
})
public class CustomEventVariables {

    @JsonProperty("source")
    @JacksonXmlProperty(localName = "source")
    private String source;

    @JsonProperty("source")
    @JacksonXmlProperty(localName = "source")
    public String getSource() {
        return source;
    }

    @JsonProperty("source")
    @JacksonXmlProperty(localName = "source")
    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("source", source).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(source).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CustomEventVariables) == false) {
            return false;
        }
        CustomEventVariables rhs = ((CustomEventVariables) other);
        return new EqualsBuilder().append(source, rhs.source).isEquals();
    }

}
