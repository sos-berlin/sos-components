
package com.sos.joc.model.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controller"
})
public class Controller {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controller")
    private String controller;

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controller")
    public String getController() {
        return controller;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controller")
    public void setController(String controller) {
        this.controller = controller;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controller", controller).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controller).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Controller) == false) {
            return false;
        }
        Controller rhs = ((Controller) other);
        return new EqualsBuilder().append(controller, rhs.controller).isEquals();
    }

}
