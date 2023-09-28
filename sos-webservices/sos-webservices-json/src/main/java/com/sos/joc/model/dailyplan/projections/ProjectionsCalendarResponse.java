
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
 * daily plan projections response
 * <p>
 * meta only for ./dates API
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "surveyDate",
    "years",
    "meta"
})
public class ProjectionsCalendarResponse {

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
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date surveyDate;
    /**
     * daily plan projection
     * <p>
     * 
     * 
     */
    @JsonProperty("years")
    private YearsItem years;
    /**
     * daily plan projection
     * <p>
     * 
     * 
     */
    @JsonProperty("meta")
    private MetaItem meta;

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
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    public Date getSurveyDate() {
        return surveyDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("surveyDate", surveyDate).append("years", years).append("meta", meta).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(surveyDate).append(years).append(meta).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProjectionsCalendarResponse) == false) {
            return false;
        }
        ProjectionsCalendarResponse rhs = ((ProjectionsCalendarResponse) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(surveyDate, rhs.surveyDate).append(years, rhs.years).append(meta, rhs.meta).isEquals();
    }

}
