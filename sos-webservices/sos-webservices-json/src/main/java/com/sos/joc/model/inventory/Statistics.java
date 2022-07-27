
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
    "numOfNoticeBoards",
    "numOfJobResources",
    "numOfFileOrderSources",
    "numOfSchedules",
    "numOfCalendars",
    "numOfIncludeScripts",
    "numOfJobTemplates"
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
    @JsonProperty("numOfNoticeBoards")
    private Long numOfNoticeBoards;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfJobResources")
    private Long numOfJobResources;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfFileOrderSources")
    private Long numOfFileOrderSources;
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
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfIncludeScripts")
    private Long numOfIncludeScripts;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfJobTemplates")
    private Long numOfJobTemplates;

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
    @JsonProperty("numOfNoticeBoards")
    public Long getNumOfNoticeBoards() {
        return numOfNoticeBoards;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfNoticeBoards")
    public void setNumOfNoticeBoards(Long numOfNoticeBoards) {
        this.numOfNoticeBoards = numOfNoticeBoards;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfJobResources")
    public Long getNumOfJobResources() {
        return numOfJobResources;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfJobResources")
    public void setNumOfJobResources(Long numOfJobResources) {
        this.numOfJobResources = numOfJobResources;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfFileOrderSources")
    public Long getNumOfFileOrderSources() {
        return numOfFileOrderSources;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfFileOrderSources")
    public void setNumOfFileOrderSources(Long numOfFileOrderSources) {
        this.numOfFileOrderSources = numOfFileOrderSources;
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

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfIncludeScripts")
    public Long getNumOfIncludeScripts() {
        return numOfIncludeScripts;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfIncludeScripts")
    public void setNumOfIncludeScripts(Long numOfIncludeScripts) {
        this.numOfIncludeScripts = numOfIncludeScripts;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfJobTemplates")
    public Long getNumOfJobTemplates() {
        return numOfJobTemplates;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfJobTemplates")
    public void setNumOfJobTemplates(Long numOfJobTemplates) {
        this.numOfJobTemplates = numOfJobTemplates;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("surveyDate", surveyDate).append("numOfJobs", numOfJobs).append("numOfWorkflows", numOfWorkflows).append("numOfLocks", numOfLocks).append("numOfNoticeBoards", numOfNoticeBoards).append("numOfJobResources", numOfJobResources).append("numOfFileOrderSources", numOfFileOrderSources).append("numOfSchedules", numOfSchedules).append("numOfCalendars", numOfCalendars).append("numOfIncludeScripts", numOfIncludeScripts).append("numOfJobTemplates", numOfJobTemplates).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(numOfCalendars).append(surveyDate).append(numOfFileOrderSources).append(numOfWorkflows).append(numOfNoticeBoards).append(numOfJobTemplates).append(numOfIncludeScripts).append(numOfJobs).append(numOfLocks).append(numOfSchedules).append(deliveryDate).append(numOfJobResources).toHashCode();
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
        return new EqualsBuilder().append(numOfCalendars, rhs.numOfCalendars).append(surveyDate, rhs.surveyDate).append(numOfFileOrderSources, rhs.numOfFileOrderSources).append(numOfWorkflows, rhs.numOfWorkflows).append(numOfNoticeBoards, rhs.numOfNoticeBoards).append(numOfJobTemplates, rhs.numOfJobTemplates).append(numOfIncludeScripts, rhs.numOfIncludeScripts).append(numOfJobs, rhs.numOfJobs).append(numOfLocks, rhs.numOfLocks).append(numOfSchedules, rhs.numOfSchedules).append(deliveryDate, rhs.deliveryDate).append(numOfJobResources, rhs.numOfJobResources).isEquals();
    }

}
