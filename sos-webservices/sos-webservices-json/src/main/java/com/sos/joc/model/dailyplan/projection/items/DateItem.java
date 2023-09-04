
package com.sos.joc.model.dailyplan.projection.items;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * daily plan projection
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "planned",
    "periods"
})
public class DateItem {

    @JsonProperty("planned")
    private Boolean planned;
    @JsonProperty("periods")
    private List<DatePeriodItem> periods = new ArrayList<DatePeriodItem>();

    @JsonProperty("planned")
    public Boolean getPlanned() {
        return planned;
    }

    @JsonProperty("planned")
    public void setPlanned(Boolean planned) {
        this.planned = planned;
    }

    @JsonProperty("periods")
    public List<DatePeriodItem> getPeriods() {
        return periods;
    }

    @JsonProperty("periods")
    public void setPeriods(List<DatePeriodItem> periods) {
        this.periods = periods;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("planned", planned).append("periods", periods).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(periods).append(planned).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DateItem) == false) {
            return false;
        }
        DateItem rhs = ((DateItem) other);
        return new EqualsBuilder().append(periods, rhs.periods).append(planned, rhs.planned).isEquals();
    }

}
