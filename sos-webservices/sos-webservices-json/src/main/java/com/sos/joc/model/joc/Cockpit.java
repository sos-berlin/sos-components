
package com.sos.joc.model.joc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.jobscheduler.ComponentState;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "version",
    "componentState"
})
public class Cockpit {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    private String version;
    /**
     * component state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("componentState")
    private ComponentState componentState;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * component state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("componentState")
    public ComponentState getComponentState() {
        return componentState;
    }

    /**
     * component state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("componentState")
    public void setComponentState(ComponentState componentState) {
        this.componentState = componentState;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("version", version).append("componentState", componentState).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(componentState).append(version).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Cockpit) == false) {
            return false;
        }
        Cockpit rhs = ((Cockpit) other);
        return new EqualsBuilder().append(componentState, rhs.componentState).append(version, rhs.version).isEquals();
    }

}
