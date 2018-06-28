
package com.sos.jobscheduler.model.event;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sos.jobscheduler.model.order.ChildOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * OrderForked event
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "children"
})
public class OrderForked
    extends Event
    implements IEvent
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("children")
    @JacksonXmlProperty(localName = "children")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "children")
    private List<ChildOrder> children = null;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("children")
    @JacksonXmlProperty(localName = "children")
    public List<ChildOrder> getChildren() {
        return children;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("children")
    @JacksonXmlProperty(localName = "children")
    public void setChildren(List<ChildOrder> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("children", children).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(children).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderForked) == false) {
            return false;
        }
        OrderForked rhs = ((OrderForked) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(children, rhs.children).isEquals();
    }

}
