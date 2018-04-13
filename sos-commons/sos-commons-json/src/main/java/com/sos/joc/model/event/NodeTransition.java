
package com.sos.joc.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * comes with event OrderStepEnded
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "type",
    "returnCode"
})
public class NodeTransition {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    @JacksonXmlProperty(localName = "type")
    private NodeTransitionType type;
    /**
     * if type=ERROR
     * 
     */
    @JsonProperty("returnCode")
    @JsonPropertyDescription("if type=ERROR")
    @JacksonXmlProperty(localName = "returnCode")
    private Integer returnCode;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    @JacksonXmlProperty(localName = "type")
    public NodeTransitionType getType() {
        return type;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    @JacksonXmlProperty(localName = "type")
    public void setType(NodeTransitionType type) {
        this.type = type;
    }

    /**
     * if type=ERROR
     * 
     */
    @JsonProperty("returnCode")
    @JacksonXmlProperty(localName = "returnCode")
    public Integer getReturnCode() {
        return returnCode;
    }

    /**
     * if type=ERROR
     * 
     */
    @JsonProperty("returnCode")
    @JacksonXmlProperty(localName = "returnCode")
    public void setReturnCode(Integer returnCode) {
        this.returnCode = returnCode;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("type", type).append("returnCode", returnCode).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(type).append(returnCode).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof NodeTransition) == false) {
            return false;
        }
        NodeTransition rhs = ((NodeTransition) other);
        return new EqualsBuilder().append(type, rhs.type).append(returnCode, rhs.returnCode).isEquals();
    }

}
