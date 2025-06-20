
package com.sos.joc.model.schedule.runtime;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.schedule.runtime.items.DailyPlanDates;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Schedule RunTime response
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "dates"
})
public class ScheduleRunTimeResponse {

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
     * Schedule runtime DailyPlan dates
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("dates")
    private DailyPlanDates dates;

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
     * Schedule runtime DailyPlan dates
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("dates")
    public DailyPlanDates getDates() {
        return dates;
    }

    /**
     * Schedule runtime DailyPlan dates
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("dates")
    public void setDates(DailyPlanDates dates) {
        this.dates = dates;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("dates", dates).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(dates).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ScheduleRunTimeResponse) == false) {
            return false;
        }
        ScheduleRunTimeResponse rhs = ((ScheduleRunTimeResponse) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(dates, rhs.dates).isEquals();
    }

}
