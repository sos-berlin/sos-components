
package com.sos.webservices.order.initiator.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.ConfigurationObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Order Template Edit configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "configuration"
})
public class ScheduleEdit
    extends ConfigurationObject
{

    /**
     * Order Template
     * <p>
     * The order template for scheduling orders to JobScheduler
     * 
     */
    @JsonProperty("configuration")
    @JsonPropertyDescription("The order template for scheduling orders to JobScheduler")
    private Schedule configuration;

    /**
     * Order Template
     * <p>
     * The order template for scheduling orders to JobScheduler
     * 
     */
    @JsonProperty("configuration")
    public Schedule getConfiguration() {
        return configuration;
    }

    /**
     * Order Template
     * <p>
     * The order template for scheduling orders to JobScheduler
     * 
     */
    @JsonProperty("configuration")
    public void setConfiguration(Schedule configuration) {
        this.configuration = configuration;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("configuration", configuration).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(configuration).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ScheduleEdit) == false) {
            return false;
        }
        ScheduleEdit rhs = ((ScheduleEdit) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(configuration, rhs.configuration).isEquals();
    }

}
