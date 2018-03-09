
package com.sos.joc.model.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sos.joc.model.calendar.Calendar;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * runtime
 * <p>
 * A run_time xml is expected which is specified in the <xsd:complexType name='run_time'> element of  http://www.sos-berlin.com/schema/scheduler.xsd
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "surveyDate",
    "runTime",
    "permanentRunTime",
    "runTimeIsTemporary",
    "calendars"
})
public class RunTime {

    /**
     * survey date of the JobScheduler Master/Agent
     * <p>
     * Current date of the JobScheduler Master/Agent. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    @JsonPropertyDescription("Current date of the JobScheduler Master/Agent. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
    @JacksonXmlProperty(localName = "surveyDate")
    private Date surveyDate;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("runTime")
    @JacksonXmlProperty(localName = "runTime")
    private String runTime;
    /**
     * is required iff runTimeIsTemporary = true
     * 
     */
    @JsonProperty("permanentRunTime")
    @JsonPropertyDescription("is required iff runTimeIsTemporary = true")
    @JacksonXmlProperty(localName = "permanentRunTime")
    private String permanentRunTime;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("runTimeIsTemporary")
    @JacksonXmlProperty(localName = "runTimeIsTemporary")
    private Boolean runTimeIsTemporary = false;
    @JsonProperty("calendars")
    @JacksonXmlProperty(localName = "calendar")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "calendars")
    private List<Calendar> calendars = new ArrayList<Calendar>();

    /**
     * survey date of the JobScheduler Master/Agent
     * <p>
     * Current date of the JobScheduler Master/Agent. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    @JacksonXmlProperty(localName = "surveyDate")
    public Date getSurveyDate() {
        return surveyDate;
    }

    /**
     * survey date of the JobScheduler Master/Agent
     * <p>
     * Current date of the JobScheduler Master/Agent. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    @JacksonXmlProperty(localName = "surveyDate")
    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("runTime")
    @JacksonXmlProperty(localName = "runTime")
    public String getRunTime() {
        return runTime;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("runTime")
    @JacksonXmlProperty(localName = "runTime")
    public void setRunTime(String runTime) {
        this.runTime = runTime;
    }

    /**
     * is required iff runTimeIsTemporary = true
     * 
     */
    @JsonProperty("permanentRunTime")
    @JacksonXmlProperty(localName = "permanentRunTime")
    public String getPermanentRunTime() {
        return permanentRunTime;
    }

    /**
     * is required iff runTimeIsTemporary = true
     * 
     */
    @JsonProperty("permanentRunTime")
    @JacksonXmlProperty(localName = "permanentRunTime")
    public void setPermanentRunTime(String permanentRunTime) {
        this.permanentRunTime = permanentRunTime;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("runTimeIsTemporary")
    @JacksonXmlProperty(localName = "runTimeIsTemporary")
    public Boolean getRunTimeIsTemporary() {
        return runTimeIsTemporary;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("runTimeIsTemporary")
    @JacksonXmlProperty(localName = "runTimeIsTemporary")
    public void setRunTimeIsTemporary(Boolean runTimeIsTemporary) {
        this.runTimeIsTemporary = runTimeIsTemporary;
    }

    @JsonProperty("calendars")
    @JacksonXmlProperty(localName = "calendar")
    public List<Calendar> getCalendars() {
        return calendars;
    }

    @JsonProperty("calendars")
    @JacksonXmlProperty(localName = "calendar")
    public void setCalendars(List<Calendar> calendars) {
        this.calendars = calendars;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("surveyDate", surveyDate).append("runTime", runTime).append("permanentRunTime", permanentRunTime).append("runTimeIsTemporary", runTimeIsTemporary).append("calendars", calendars).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(runTime).append(permanentRunTime).append(runTimeIsTemporary).append(surveyDate).append(calendars).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RunTime) == false) {
            return false;
        }
        RunTime rhs = ((RunTime) other);
        return new EqualsBuilder().append(runTime, rhs.runTime).append(permanentRunTime, rhs.permanentRunTime).append(runTimeIsTemporary, rhs.runTimeIsTemporary).append(surveyDate, rhs.surveyDate).append(calendars, rhs.calendars).isEquals();
    }

}
