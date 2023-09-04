
package com.sos.joc.model.dailyplan.projections;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.dailyplan.projections.items.meta.MetaItem;
import com.sos.joc.model.dailyplan.projections.items.year.YearsItem;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * daily plan submissions response
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "meta",
    "years"
})
public class ProjectionsResponse {

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
     * daily plan projection
     * <p>
     * 
     * 
     */
    @JsonProperty("meta")
    private MetaItem meta;
    /**
     * daily plan projection
     * <p>
     * 
     * 
     */
    @JsonProperty("years")
    private YearsItem years;

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
     * daily plan projection
     * <p>
     * 
     * 
     */
    @JsonProperty("meta")
    public MetaItem getMeta() {
        return meta;
    }

    /**
     * daily plan projection
     * <p>
     * 
     * 
     */
    @JsonProperty("meta")
    public void setMeta(MetaItem meta) {
        this.meta = meta;
    }

    /**
     * daily plan projection
     * <p>
     * 
     * 
     */
    @JsonProperty("years")
    public YearsItem getYears() {
        return years;
    }

    /**
     * daily plan projection
     * <p>
     * 
     * 
     */
    @JsonProperty("years")
    public void setYears(YearsItem years) {
        this.years = years;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("meta", meta).append("years", years).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(meta).append(years).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProjectionsResponse) == false) {
            return false;
        }
        ProjectionsResponse rhs = ((ProjectionsResponse) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(meta, rhs.meta).append(years, rhs.years).isEquals();
    }

}
