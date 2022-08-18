
package com.sos.joc.model.workflow;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.schedule.OrderParameterisation;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "name",
    "title",
    "orderParameterisations"
})
public class OrderParameterisations {

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of an object.")
    private String path;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    private String title;
    @JsonProperty("orderParameterisations")
    private List<OrderParameterisation> orderParameterisations = new ArrayList<OrderParameterisation>();

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

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

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("orderParameterisations")
    public List<OrderParameterisation> getOrderParameterisations() {
        return orderParameterisations;
    }

    @JsonProperty("orderParameterisations")
    public void setOrderParameterisations(List<OrderParameterisation> orderParameterisations) {
        this.orderParameterisations = orderParameterisations;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("path", path).append("name", name).append("title", title).append("orderParameterisations", orderParameterisations).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(path).append(title).append(orderParameterisations).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderParameterisations) == false) {
            return false;
        }
        OrderParameterisations rhs = ((OrderParameterisations) other);
        return new EqualsBuilder().append(name, rhs.name).append(path, rhs.path).append(title, rhs.title).append(orderParameterisations, rhs.orderParameterisations).isEquals();
    }

}
