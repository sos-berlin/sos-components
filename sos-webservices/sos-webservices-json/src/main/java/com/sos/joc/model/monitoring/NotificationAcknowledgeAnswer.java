
package com.sos.joc.model.monitoring;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * notification collection of monitors
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "acknowledgement"
})
public class NotificationAcknowledgeAnswer {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    /**
     * order object in history collection
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("acknowledgement")
    private NotificationItemAcknowledgementItem acknowledgement;

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    /**
     * order object in history collection
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("acknowledgement")
    public NotificationItemAcknowledgementItem getAcknowledgement() {
        return acknowledgement;
    }

    /**
     * order object in history collection
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("acknowledgement")
    public void setAcknowledgement(NotificationItemAcknowledgementItem acknowledgement) {
        this.acknowledgement = acknowledgement;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("acknowledgement", acknowledgement).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(acknowledgement).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof NotificationAcknowledgeAnswer) == false) {
            return false;
        }
        NotificationAcknowledgeAnswer rhs = ((NotificationAcknowledgeAnswer) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(acknowledgement, rhs.acknowledgement).isEquals();
    }

}
