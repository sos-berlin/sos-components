
package com.sos.joc.model.jitl.monitoring;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.controller.Controller;
import com.sos.joc.model.controller.JobSchedulerP;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * MonitoringControllerStatus
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "active",
    "passive",
    "volatileStatus",
    "permanentStatus"
})
public class MonitoringControllerStatus {

    /**
     * Controller
     * <p>
     * 
     * 
     */
    @JsonProperty("active")
    private Controller active;
    /**
     * Controller
     * <p>
     * 
     * 
     */
    @JsonProperty("passive")
    private Controller passive;
    /**
     * Controller
     * <p>
     * 
     * 
     */
    @JsonProperty("volatileStatus")
    private Controller volatileStatus;
    /**
     * jobscheduler
     * <p>
     * 
     * 
     */
    @JsonProperty("permanentStatus")
    private JobSchedulerP permanentStatus;

    /**
     * Controller
     * <p>
     * 
     * 
     */
    @JsonProperty("active")
    public Controller getActive() {
        return active;
    }

    /**
     * Controller
     * <p>
     * 
     * 
     */
    @JsonProperty("active")
    public void setActive(Controller active) {
        this.active = active;
    }

    /**
     * Controller
     * <p>
     * 
     * 
     */
    @JsonProperty("passive")
    public Controller getPassive() {
        return passive;
    }

    /**
     * Controller
     * <p>
     * 
     * 
     */
    @JsonProperty("passive")
    public void setPassive(Controller passive) {
        this.passive = passive;
    }

    /**
     * Controller
     * <p>
     * 
     * 
     */
    @JsonProperty("volatileStatus")
    public Controller getVolatileStatus() {
        return volatileStatus;
    }

    /**
     * Controller
     * <p>
     * 
     * 
     */
    @JsonProperty("volatileStatus")
    public void setVolatileStatus(Controller volatileStatus) {
        this.volatileStatus = volatileStatus;
    }

    /**
     * jobscheduler
     * <p>
     * 
     * 
     */
    @JsonProperty("permanentStatus")
    public JobSchedulerP getPermanentStatus() {
        return permanentStatus;
    }

    /**
     * jobscheduler
     * <p>
     * 
     * 
     */
    @JsonProperty("permanentStatus")
    public void setPermanentStatus(JobSchedulerP permanentStatus) {
        this.permanentStatus = permanentStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("active", active).append("passive", passive).append("volatileStatus", volatileStatus).append("permanentStatus", permanentStatus).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(permanentStatus).append(active).append(volatileStatus).append(passive).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof MonitoringControllerStatus) == false) {
            return false;
        }
        MonitoringControllerStatus rhs = ((MonitoringControllerStatus) other);
        return new EqualsBuilder().append(permanentStatus, rhs.permanentStatus).append(active, rhs.active).append(volatileStatus, rhs.volatileStatus).append(passive, rhs.passive).isEquals();
    }

}
