
package com.sos.joc.model.inventory;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * inventory statistics
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "deliveryDate",
    "surveyDate",
    "numOfJobs",
    "numOfWorkflows",
    "numOfLocks",
    "numOfJunctions",
    "numOfFileWatches",
    "numOfSchedules",
    "numOfCalendars"
})
public class Statistics {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date surveyDate;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfJobs")
    private Long numOfJobs;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfWorkflows")
    private Long numOfWorkflows;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfLocks")
    private Long numOfLocks;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfJunctions")
    private Long numOfJunctions;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfFileWatches")
    private Long numOfFileWatches;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfSchedules")
    private Long numOfSchedules;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfCalendars")
    private Long numOfCalendars;

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    public Date getSurveyDate() {
        return surveyDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfJobs")
    public Long getNumOfJobs() {
        return numOfJobs;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfJobs")
    public void setNumOfJobs(Long numOfJobs) {
        this.numOfJobs = numOfJobs;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfWorkflows")
    public Long getNumOfWorkflows() {
        return numOfWorkflows;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfWorkflows")
    public void setNumOfWorkflows(Long numOfWorkflows) {
        this.numOfWorkflows = numOfWorkflows;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfLocks")
    public Long getNumOfLocks() {
        return numOfLocks;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfLocks")
    public void setNumOfLocks(Long numOfLocks) {
        this.numOfLocks = numOfLocks;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfJunctions")
    public Long getNumOfJunctions() {
        return numOfJunctions;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfJunctions")
    public void setNumOfJunctions(Long numOfJunctions) {
        this.numOfJunctions = numOfJunctions;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfFileWatches")
    public Long getNumOfFileWatches() {
        return numOfFileWatches;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfFileWatches")
    public void setNumOfFileWatches(Long numOfFileWatches) {
        this.numOfFileWatches = numOfFileWatches;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfSchedules")
    public Long getNumOfSchedules() {
        return numOfSchedules;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfSchedules")
    public void setNumOfSchedules(Long numOfSchedules) {
        this.numOfSchedules = numOfSchedules;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfCalendars")
    public Long getNumOfCalendars() {
        return numOfCalendars;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfCalendars")
    public void setNumOfCalendars(Long numOfCalendars) {
        this.numOfCalendars = numOfCalendars;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("surveyDate", surveyDate).append("numOfJobs", numOfJobs).append("numOfWorkflows", numOfWorkflows).append("numOfLocks", numOfLocks).append("numOfJunctions", numOfJunctions).append("numOfFileWatches", numOfFileWatches).append("numOfSchedules", numOfSchedules).append("numOfCalendars", numOfCalendars).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(numOfCalendars).append(surveyDate).append(numOfJobs).append(numOfLocks).append(numOfJunctions).append(numOfFileWatches).append(numOfSchedules).append(deliveryDate).append(numOfWorkflows).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Statistics) == false) {
            return false;
        }
        Statistics rhs = ((Statistics) other);
        return new EqualsBuilder().append(numOfCalendars, rhs.numOfCalendars).append(surveyDate, rhs.surveyDate).append(numOfJobs, rhs.numOfJobs).append(numOfLocks, rhs.numOfLocks).append(numOfJunctions, rhs.numOfJunctions).append(numOfFileWatches, rhs.numOfFileWatches).append(numOfSchedules, rhs.numOfSchedules).append(deliveryDate, rhs.deliveryDate).append(numOfWorkflows, rhs.numOfWorkflows).isEquals();
    }

}
