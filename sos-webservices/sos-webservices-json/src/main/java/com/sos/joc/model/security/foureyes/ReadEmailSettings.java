
package com.sos.joc.model.security.foureyes;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Approval Email Settings
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate"
})
public class ReadEmailSettings
    extends EmailSettings
{

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
     * No args constructor for use in serialization
     * 
     */
    public ReadEmailSettings() {
    }

    /**
     * 
     * @param cc
     * @param charset
     * @param bcc
     * @param subject
     * @param jobResourceName
     * @param deliveryDate
     * @param body
     * @param encoding
     * @param priority
     * @param contentType
     */
    public ReadEmailSettings(Date deliveryDate, String body, String subject, String cc, String bcc, String contentType, String charset, String encoding, EmailPriority priority, String jobResourceName) {
        super(body, subject, cc, bcc, contentType, charset, encoding, priority, jobResourceName);
        this.deliveryDate = deliveryDate;
    }

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

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("deliveryDate", deliveryDate).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(deliveryDate).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReadEmailSettings) == false) {
            return false;
        }
        ReadEmailSettings rhs = ((ReadEmailSettings) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(deliveryDate, rhs.deliveryDate).isEquals();
    }

}
