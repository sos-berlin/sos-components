
package com.sos.joc.model.tag;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "addOrderTags"
})
public class AddOrdersOrderTags {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("name")
    private String name;
    @JsonProperty("addOrderTags")
    private AddOrderOrderTags addOrderTags;

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("addOrderTags")
    public AddOrderOrderTags getAddOrderTags() {
        return addOrderTags;
    }

    @JsonProperty("addOrderTags")
    public void setAddOrderTags(AddOrderOrderTags addOrderInstructions) {
        this.addOrderTags = addOrderInstructions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("addOrderTags", addOrderTags).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(addOrderTags).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AddOrdersOrderTags) == false) {
            return false;
        }
        AddOrdersOrderTags rhs = ((AddOrdersOrderTags) other);
        return new EqualsBuilder().append(name, rhs.name).append(addOrderTags, rhs.addOrderTags).isEquals();
    }

}
