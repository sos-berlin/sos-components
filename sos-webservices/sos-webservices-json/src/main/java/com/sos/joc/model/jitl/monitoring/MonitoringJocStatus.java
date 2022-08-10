
package com.sos.joc.model.jitl.monitoring;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.joc.Cockpit;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * MonitoringJocStatus
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "active",
    "passive"
})
public class MonitoringJocStatus {

    /**
     * joc cockpit
     * <p>
     * 
     * 
     */
    @JsonProperty("active")
    private Cockpit active;
    @JsonProperty("passive")
    private List<Cockpit> passive = new ArrayList<Cockpit>();

    /**
     * joc cockpit
     * <p>
     * 
     * 
     */
    @JsonProperty("active")
    public Cockpit getActive() {
        return active;
    }

    /**
     * joc cockpit
     * <p>
     * 
     * 
     */
    @JsonProperty("active")
    public void setActive(Cockpit active) {
        this.active = active;
    }

    @JsonProperty("passive")
    public List<Cockpit> getPassive() {
        return passive;
    }

    @JsonProperty("passive")
    public void setPassive(List<Cockpit> passive) {
        this.passive = passive;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("active", active).append("passive", passive).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(active).append(passive).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof MonitoringJocStatus) == false) {
            return false;
        }
        MonitoringJocStatus rhs = ((MonitoringJocStatus) other);
        return new EqualsBuilder().append(active, rhs.active).append(passive, rhs.passive).isEquals();
    }

}
