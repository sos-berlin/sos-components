
package com.sos.joc.model.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * events
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "events",
    "deliveryDate"
})
public class JobSchedulerEvents {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("events")
    @JacksonXmlProperty(localName = "event")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "events")
    private List<JobSchedulerEvent> events = new ArrayList<JobSchedulerEvent>();
    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
    @JacksonXmlProperty(localName = "deliveryDate")
    private Date deliveryDate;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("events")
    @JacksonXmlProperty(localName = "event")
    public List<JobSchedulerEvent> getEvents() {
        return events;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("events")
    @JacksonXmlProperty(localName = "event")
    public void setEvents(List<JobSchedulerEvent> events) {
        this.events = events;
    }

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JacksonXmlProperty(localName = "deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JacksonXmlProperty(localName = "deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("events", events).append("deliveryDate", deliveryDate).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(events).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobSchedulerEvents) == false) {
            return false;
        }
        JobSchedulerEvents rhs = ((JobSchedulerEvents) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(events, rhs.events).isEquals();
    }

}
