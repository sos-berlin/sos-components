
package com.sos.joc.model.dailyplan.projections.items.year;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * daily plan projection
 * <p>
 * numOfOrders and numOfNonPeriods are in the ./calendar response, periods and nonPeriods are in the ./dates response
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "planned",
    "numOfOrders",
    "numOfNonPeriods",
    "periods",
    "nonPeriods"
})
public class DateItem {

    @JsonProperty("planned")
    private Boolean planned;
    @JsonProperty("numOfOrders")
    @JsonAlias({
        "numOfPeriods"
    })
    private Integer numOfOrders;
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
    private List<DatePeriodItem> nonPeriods = new ArrayList<DatePeriodItem>();

    @JsonProperty("planned")
    public Boolean getPlanned() {
        return planned;
    }

    @JsonProperty("planned")
    public void setPlanned(Boolean planned) {
        this.planned = planned;
    }

    @JsonProperty("numOfOrders")
    public Integer getNumOfOrders() {
        return numOfOrders;
    }

    @JsonProperty("numOfOrders")
    public void setNumOfOrders(Integer numOfOrders) {
        this.numOfOrders = numOfOrders;
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
        return new ToStringBuilder(this).append("planned", planned).append("numOfOrders", numOfOrders).append("numOfNonPeriods", numOfNonPeriods).append("periods", periods).append("nonPeriods", nonPeriods).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(periods).append(nonPeriods).append(planned).append(numOfNonPeriods).append(numOfOrders).toHashCode();
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
        return new EqualsBuilder().append(periods, rhs.periods).append(nonPeriods, rhs.nonPeriods).append(planned, rhs.planned).append(numOfNonPeriods, rhs.numOfNonPeriods).append(numOfOrders, rhs.numOfOrders).isEquals();
    }

}
