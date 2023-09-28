
package com.sos.joc.model.dailyplan.projections.items.year;

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
    "numOfPeriods",
    "numOfNonPeriods",
    "periods",
    "nonPeriods"
})
public class DateItem {

    @JsonProperty("planned")
    private Boolean planned;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfPeriods")
    private Integer numOfPeriods;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfNonPeriods")
    private Integer numOfNonPeriods;
    @JsonProperty("periods")
    private List<DatePeriodItem> periods = new ArrayList<DatePeriodItem>();
    @JsonProperty("nonPeriods")
    private List<DatePeriodItem> nonPeriods;

    @JsonProperty("planned")
    public Boolean getPlanned() {
        return planned;
    }

    @JsonProperty("planned")
    public void setPlanned(Boolean planned) {
        this.planned = planned;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfPeriods")
    public Integer getNumOfPeriods() {
        return numOfPeriods;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfPeriods")
    public void setNumOfPeriods(Integer numOfPeriods) {
        this.numOfPeriods = numOfPeriods;
    }
    
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfNonPeriods")
    public Integer getNumOfNonPeriods() {
        return numOfNonPeriods;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfNonPeriods")
    public void setNumOfNonPeriods(Integer numOfNonPeriods) {
        this.numOfNonPeriods = numOfNonPeriods;
    }

    @JsonProperty("periods")
    public List<DatePeriodItem> getPeriods() {
        return periods;
    }

    @JsonProperty("periods")
    public void setPeriods(List<DatePeriodItem> periods) {
        this.periods = periods;
    }
    
    @JsonProperty("nonPeriods")
    public List<DatePeriodItem> getNonPeriods() {
        return nonPeriods;
    }

    @JsonProperty("nonPeriods")
    public void setNonPeriods(List<DatePeriodItem> nonPeriods) {
        this.nonPeriods = nonPeriods;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("planned", planned).append("numOfPeriods", numOfPeriods).append("numOfNonPeriods", numOfNonPeriods).append("periods", periods).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(periods).append(planned).append(numOfPeriods).append(numOfNonPeriods).toHashCode();
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
        return new EqualsBuilder().append(periods, rhs.periods).append(planned, rhs.planned).append(numOfPeriods, rhs.numOfPeriods).append(numOfNonPeriods, rhs.numOfNonPeriods).isEquals();
    }

}
