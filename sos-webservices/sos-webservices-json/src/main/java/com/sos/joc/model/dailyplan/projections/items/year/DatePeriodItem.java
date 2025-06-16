
package com.sos.joc.model.dailyplan.projections.items.year;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.calendar.Period;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * daily plan projection
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "schedule",
    "scheduleOrderName",
    "workflow",
    "period"
})
public class DatePeriodItem {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("schedule")
    private String schedule;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("scheduleOrderName")
    private String scheduleOrderName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("workflow")
    private String workflow;
    /**
     * periods
     * <p>
     * 
     * 
     */
    @JsonProperty("period")
    private Period period;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("schedule")
    public String getSchedule() {
        return schedule;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("schedule")
    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("scheduleOrderName")
    public String getScheduleOrderName() {
        return scheduleOrderName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("scheduleOrderName")
    public void setScheduleOrderName(String scheduleOrderName) {
        this.scheduleOrderName = scheduleOrderName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("workflow")
    public String getWorkflow() {
        return workflow;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("workflow")
    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    /**
     * periods
     * <p>
     * 
     * 
     */
    @JsonProperty("period")
    public Period getPeriod() {
        return period;
    }

    /**
     * periods
     * <p>
     * 
     * 
     */
    @JsonProperty("period")
    public void setPeriod(Period period) {
        this.period = period;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("schedule", schedule).append("scheduleOrderName", scheduleOrderName).append("workflow", workflow).append("period", period).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(schedule).append(period).append(additionalProperties).append(workflow).append(scheduleOrderName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DatePeriodItem) == false) {
            return false;
        }
        DatePeriodItem rhs = ((DatePeriodItem) other);
        return new EqualsBuilder().append(schedule, rhs.schedule).append(period, rhs.period).append(additionalProperties, rhs.additionalProperties).append(workflow, rhs.workflow).append(scheduleOrderName, rhs.scheduleOrderName).isEquals();
    }

}
