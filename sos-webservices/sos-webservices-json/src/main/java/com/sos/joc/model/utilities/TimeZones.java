
package com.sos.joc.model.utilities;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Available time zones
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "timezones"
})
public class TimeZones {

    @JsonProperty("timezones")
    private List<String> timezones = new ArrayList<String>();

    @JsonProperty("timezones")
    public List<String> getTimezones() {
        return timezones;
    }

    @JsonProperty("timezones")
    public void setTimezones(List<String> timezones) {
        this.timezones = timezones;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("timezones", timezones).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(timezones).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TimeZones) == false) {
            return false;
        }
        TimeZones rhs = ((TimeZones) other);
        return new EqualsBuilder().append(timezones, rhs.timezones).isEquals();
    }

}
