
package com.sos.sign.model.calendar;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Calendar
 * <p>
 * deploy object with fixed property 'TYPE':'Calendar'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "TYPE",
    "path",
    "timezone",
    "dateOffset",
    "orderIdToDatePattern",
    "periodDatePattern"
})
public class Calendar {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    private String tYPE = "Calendar";
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    private String path = "DailyPlan";
    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * (Required)
     * 
     */
    @JsonProperty("timezone")
    @JsonPropertyDescription("see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones")
    private String timezone = "Etc/UTC";
    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("dateOffset")
    private Long dateOffset;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderIdToDatePattern")
    private String orderIdToDatePattern = "#([^#]+)#.*";
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("periodDatePattern")
    private String periodDatePattern = "yyyy-MM-dd";

    /**
     * No args constructor for use in serialization
     * 
     */
    public Calendar() {
    }

    /**
     * 
     * @param path
     * @param timezone
     * @param dateOffset
     * @param orderIdToDatePattern
     * @param tYPE
     * @param periodDatePattern
     */
    public Calendar(String tYPE, String path, String timezone, Long dateOffset, String orderIdToDatePattern, String periodDatePattern) {
        super();
        this.tYPE = tYPE;
        this.path = path;
        this.timezone = timezone;
        this.dateOffset = dateOffset;
        this.orderIdToDatePattern = orderIdToDatePattern;
        this.periodDatePattern = periodDatePattern;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public String getTYPE() {
        return tYPE;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(String tYPE) {
        this.tYPE = tYPE;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * (Required)
     * 
     */
    @JsonProperty("timezone")
    public String getTimezone() {
        return timezone;
    }

    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * (Required)
     * 
     */
    @JsonProperty("timezone")
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("dateOffset")
    public Long getDateOffset() {
        return dateOffset;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("dateOffset")
    public void setDateOffset(Long dateOffset) {
        this.dateOffset = dateOffset;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderIdToDatePattern")
    public String getOrderIdToDatePattern() {
        return orderIdToDatePattern;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderIdToDatePattern")
    public void setOrderIdToDatePattern(String orderIdToDatePattern) {
        this.orderIdToDatePattern = orderIdToDatePattern;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("periodDatePattern")
    public String getPeriodDatePattern() {
        return periodDatePattern;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("periodDatePattern")
    public void setPeriodDatePattern(String periodDatePattern) {
        this.periodDatePattern = periodDatePattern;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("path", path).append("timezone", timezone).append("dateOffset", dateOffset).append("orderIdToDatePattern", orderIdToDatePattern).append("periodDatePattern", periodDatePattern).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(timezone).append(dateOffset).append(orderIdToDatePattern).append(tYPE).append(periodDatePattern).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Calendar) == false) {
            return false;
        }
        Calendar rhs = ((Calendar) other);
        return new EqualsBuilder().append(path, rhs.path).append(timezone, rhs.timezone).append(dateOffset, rhs.dateOffset).append(orderIdToDatePattern, rhs.orderIdToDatePattern).append(tYPE, rhs.tYPE).append(periodDatePattern, rhs.periodDatePattern).isEquals();
    }

}
