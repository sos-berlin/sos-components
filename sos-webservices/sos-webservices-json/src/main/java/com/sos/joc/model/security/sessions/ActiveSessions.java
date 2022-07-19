
package com.sos.joc.model.security.sessions;

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
 * ActiveSessions
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "activeSessions"
})
public class ActiveSessions {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    @JsonProperty("activeSessions")
    private List<ActiveSession> activeSessions = new ArrayList<ActiveSession>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public ActiveSessions() {
    }

    /**
     * 
     * @param activeSessions
     * @param deliveryDate
     */
    public ActiveSessions(Date deliveryDate, List<ActiveSession> activeSessions) {
        super();
        this.deliveryDate = deliveryDate;
        this.activeSessions = activeSessions;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    @JsonProperty("activeSessions")
    public List<ActiveSession> getActiveSessions() {
        return activeSessions;
    }

    @JsonProperty("activeSessions")
    public void setActiveSessions(List<ActiveSession> activeSessions) {
        this.activeSessions = activeSessions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("activeSessions", activeSessions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(activeSessions).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ActiveSessions) == false) {
            return false;
        }
        ActiveSessions rhs = ((ActiveSessions) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(activeSessions, rhs.activeSessions).isEquals();
    }

}
