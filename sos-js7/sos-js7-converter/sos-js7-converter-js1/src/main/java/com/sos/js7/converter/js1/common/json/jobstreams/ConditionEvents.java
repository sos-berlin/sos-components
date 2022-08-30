
package com.sos.js7.converter.js1.common.json.jobstreams;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ConditionEvents
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "session",
    "conditionEvents",
    "conditionMissingEvents"
})
public class ConditionEvents {

    /**
     * date time
     * <p>
     * Date time. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Date time. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
    private Date deliveryDate;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("session")
    private String session;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("conditionEvents")
    private List<ConditionEvent> conditionEvents = new ArrayList<ConditionEvent>();
    @JsonProperty("conditionMissingEvents")
    private List<ConditionEvent> conditionMissingEvents = new ArrayList<ConditionEvent>();

    /**
     * date time
     * <p>
     * Date time. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * date time
     * <p>
     * Date time. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("session")
    public String getSession() {
        return session;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("session")
    public void setSession(String session) {
        this.session = session;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("conditionEvents")
    public List<ConditionEvent> getConditionEvents() {
        return conditionEvents;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("conditionEvents")
    public void setConditionEvents(List<ConditionEvent> conditionEvents) {
        this.conditionEvents = conditionEvents;
    }

    @JsonProperty("conditionMissingEvents")
    public List<ConditionEvent> getConditionMissingEvents() {
        return conditionMissingEvents;
    }

    @JsonProperty("conditionMissingEvents")
    public void setConditionMissingEvents(List<ConditionEvent> conditionMissingEvents) {
        this.conditionMissingEvents = conditionMissingEvents;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("session", session).append("conditionEvents", conditionEvents).append("conditionMissingEvents", conditionMissingEvents).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(conditionMissingEvents).append(deliveryDate).append(conditionEvents).append(session).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ConditionEvents) == false) {
            return false;
        }
        ConditionEvents rhs = ((ConditionEvents) other);
        return new EqualsBuilder().append(conditionMissingEvents, rhs.conditionMissingEvents).append(deliveryDate, rhs.deliveryDate).append(conditionEvents, rhs.conditionEvents).append(session, rhs.session).isEquals();
    }

}
