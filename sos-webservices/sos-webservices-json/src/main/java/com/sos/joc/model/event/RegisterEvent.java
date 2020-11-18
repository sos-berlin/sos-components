
package com.sos.joc.model.event;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * register event
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllers",
    "close"
})
public class RegisterEvent {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllers")
    private List<JobSchedulerObjects> controllers = new ArrayList<JobSchedulerObjects>();
    @JsonProperty("close")
    private Boolean close = false;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllers")
    public List<JobSchedulerObjects> getControllers() {
        return controllers;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllers")
    public void setControllers(List<JobSchedulerObjects> controllers) {
        this.controllers = controllers;
    }

    @JsonProperty("close")
    public Boolean getClose() {
        return close;
    }

    @JsonProperty("close")
    public void setClose(Boolean close) {
        this.close = close;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllers", controllers).append("close", close).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllers).append(close).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RegisterEvent) == false) {
            return false;
        }
        RegisterEvent rhs = ((RegisterEvent) other);
        return new EqualsBuilder().append(controllers, rhs.controllers).append(close, rhs.close).isEquals();
    }

}
