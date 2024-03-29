
package com.sos.js7.converter.js1.common.json.calendars;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/** weekdays
 * <p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "from", "to", "days" })
public class WeekDays {

    /** date
     * <p>
     * ISO date YYYY-MM-DD */
    @JsonProperty("from")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    private String from;
    /** date
     * <p>
     * ISO date YYYY-MM-DD */
    @JsonProperty("to")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    private String to;
    /** (Required) */
    @JsonProperty("days")
    private List<Integer> days = null;

    /** date
     * <p>
     * ISO date YYYY-MM-DD */
    @JsonProperty("from")
    public String getFrom() {
        return from;
    }

    /** date
     * <p>
     * ISO date YYYY-MM-DD */
    @JsonProperty("from")
    public void setFrom(String from) {
        this.from = from;
    }

    /** date
     * <p>
     * ISO date YYYY-MM-DD */
    @JsonProperty("to")
    public String getTo() {
        return to;
    }

    /** date
     * <p>
     * ISO date YYYY-MM-DD */
    @JsonProperty("to")
    public void setTo(String to) {
        this.to = to;
    }

    /** (Required) */
    @JsonProperty("days")
    public List<Integer> getDays() {
        return days;
    }

    /** (Required) */
    @JsonProperty("days")
    public void setDays(List<Integer> days) {
        this.days = days;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("from", from).append("to", to).append("days", days).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(days).append(from).append(to).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WeekDays) == false) {
            return false;
        }
        WeekDays rhs = ((WeekDays) other);
        return new EqualsBuilder().append(days, rhs.days).append(from, rhs.from).append(to, rhs.to).isEquals();
    }

}
