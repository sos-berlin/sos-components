
package com.sos.joc.model.calendar;

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
 * dates
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "deliveryDate",
    "dates",
    "withExcludes"
})
public class Dates {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("dates")
    private List<String> dates = null;
    @JsonProperty("withExcludes")
    private List<String> withExcludes = null;

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

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("dates")
    public List<String> getDates() {
        return dates;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("dates")
    public void setDates(List<String> dates) {
        this.dates = dates;
    }

    @JsonProperty("withExcludes")
    public List<String> getWithExcludes() {
        return withExcludes;
    }

    @JsonProperty("withExcludes")
    public void setWithExcludes(List<String> withExcludes) {
        this.withExcludes = withExcludes;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("dates", dates).append("withExcludes", withExcludes).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dates).append(deliveryDate).append(withExcludes).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Dates) == false) {
            return false;
        }
        Dates rhs = ((Dates) other);
        return new EqualsBuilder().append(dates, rhs.dates).append(deliveryDate, rhs.deliveryDate).append(withExcludes, rhs.withExcludes).isEquals();
    }

}
