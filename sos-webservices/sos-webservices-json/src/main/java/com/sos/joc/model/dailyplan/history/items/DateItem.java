
package com.sos.joc.model.dailyplan.history.items;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * date object in daily plan history collection
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "date",
    "countTotal",
    "countSubmitted",
    "controllers"
})
public class DateItem {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("date")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date date;
    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("countTotal")
    private Long countTotal;
    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("countSubmitted")
    private Long countSubmitted;
    @JsonProperty("controllers")
    private List<ControllerItem> controllers = new ArrayList<ControllerItem>();

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("date")
    public Date getDate() {
        return date;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("date")
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("countTotal")
    public Long getCountTotal() {
        return countTotal;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("countTotal")
    public void setCountTotal(Long countTotal) {
        this.countTotal = countTotal;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("countSubmitted")
    public Long getCountSubmitted() {
        return countSubmitted;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("countSubmitted")
    public void setCountSubmitted(Long countSubmitted) {
        this.countSubmitted = countSubmitted;
    }

    @JsonProperty("controllers")
    public List<ControllerItem> getControllers() {
        return controllers;
    }

    @JsonProperty("controllers")
    public void setControllers(List<ControllerItem> controllers) {
        this.controllers = controllers;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("date", date).append("countTotal", countTotal).append("countSubmitted", countSubmitted).append("controllers", controllers).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(date).append(controllers).append(countTotal).append(countSubmitted).toHashCode();
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
        return new EqualsBuilder().append(date, rhs.date).append(controllers, rhs.controllers).append(countTotal, rhs.countTotal).append(countSubmitted, rhs.countSubmitted).isEquals();
    }

}
