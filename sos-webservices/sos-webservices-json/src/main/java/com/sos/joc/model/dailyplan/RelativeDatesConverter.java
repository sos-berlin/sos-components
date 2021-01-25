
package com.sos.joc.model.dailyplan;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Daily Plan Relative Dates Converter
 * <p>
 * To convert a relative date like +1d to a date yyyy-mm-dd
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "timeZone",
    "relativDates",
    "absoluteDates"
})
public class RelativeDatesConverter {

    /**
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    @JsonPropertyDescription("see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones")
    private String timeZone;
    @JsonProperty("relativDates")
    private List<String> relativDates = null;
    @JsonProperty("absoluteDates")
    private List<String> absoluteDates = null;

    /**
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @JsonProperty("relativDates")
    public List<String> getRelativDates() {
        return relativDates;
    }

    @JsonProperty("relativDates")
    public void setRelativDates(List<String> relativDates) {
        this.relativDates = relativDates;
    }

    @JsonProperty("absoluteDates")
    public List<String> getAbsoluteDates() {
        return absoluteDates;
    }

    @JsonProperty("absoluteDates")
    public void setAbsoluteDates(List<String> absoluteDates) {
        this.absoluteDates = absoluteDates;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("timeZone", timeZone).append("relativDates", relativDates).append("absoluteDates", absoluteDates).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(timeZone).append(relativDates).append(absoluteDates).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RelativeDatesConverter) == false) {
            return false;
        }
        RelativeDatesConverter rhs = ((RelativeDatesConverter) other);
        return new EqualsBuilder().append(timeZone, rhs.timeZone).append(relativDates, rhs.relativDates).append(absoluteDates, rhs.absoluteDates).isEquals();
    }

}
