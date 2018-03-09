
package com.sos.joc.model.calendar;

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
 * dates
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "dates",
    "withExcludes"
})
public class Dates {

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
    @JsonProperty("dates")
    @JacksonXmlProperty(localName = "date")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "dates")
    private List<String> dates = null;
    @JsonProperty("withExcludes")
    @JacksonXmlProperty(localName = "withExclude")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "withExcludes")
    private List<String> withExcludes = null;

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

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("dates")
    @JacksonXmlProperty(localName = "date")
    public List<String> getDates() {
        return dates;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("dates")
    @JacksonXmlProperty(localName = "date")
    public void setDates(List<String> dates) {
        this.dates = dates;
    }

    @JsonProperty("withExcludes")
    @JacksonXmlProperty(localName = "withExclude")
    public List<String> getWithExcludes() {
        return withExcludes;
    }

    @JsonProperty("withExcludes")
    @JacksonXmlProperty(localName = "withExclude")
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
