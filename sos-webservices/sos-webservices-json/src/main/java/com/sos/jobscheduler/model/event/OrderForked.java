
package com.sos.jobscheduler.model.event;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
    "TYPE",
    "children"
})
public class OrderForked
    extends Event
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("children")
    private List<ChildOrder> children = new ArrayList<ChildOrder>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderForked() {
    }

    /**
     * 
     * @param children
     */
    public OrderForked(List<ChildOrder> children) {
        super();
        this.children = children;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("children")
    public List<ChildOrder> getChildren() {
        return children;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("children")
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
