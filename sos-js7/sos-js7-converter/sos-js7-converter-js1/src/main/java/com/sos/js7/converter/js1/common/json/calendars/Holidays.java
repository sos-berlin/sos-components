
package com.sos.js7.converter.js1.common.json.calendars;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/** holidays
 * <p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "nationalCalendar", "dates" })
public class Holidays {

    @JsonProperty("nationalCalendar")
    private String nationalCalendar;
    @JsonProperty("dates")
    private List<String> dates = null;

    @JsonProperty("nationalCalendar")
    public String getNationalCalendar() {
        return nationalCalendar;
    }

    @JsonProperty("nationalCalendar")
    public void setNationalCalendar(String nationalCalendar) {
        this.nationalCalendar = nationalCalendar;
    }

    @JsonProperty("dates")
    public List<String> getDates() {
        return dates;
    }

    @JsonProperty("dates")
    public void setDates(List<String> dates) {
        this.dates = dates;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("nationalCalendar", nationalCalendar).append("dates", dates).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(nationalCalendar).append(dates).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Holidays) == false) {
            return false;
        }
        Holidays rhs = ((Holidays) other);
        return new EqualsBuilder().append(nationalCalendar, rhs.nationalCalendar).append(dates, rhs.dates).isEquals();
    }

}
