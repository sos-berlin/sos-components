
package com.sos.joc.model.lock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sos.joc.model.common.ConfigurationState;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * lock object (volatile part)
 * <p>
 * The lock is free iff no holders specified
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "surveyDate",
    "path",
    "name",
    "maxNonExclusive",
    "holders",
    "queue",
    "configurationStatus"
})
public class LockV {

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
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    @JacksonXmlProperty(localName = "path")
    private String path;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    @JacksonXmlProperty(localName = "name")
    private String name;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("maxNonExclusive")
    @JacksonXmlProperty(localName = "maxNonExclusive")
    private Integer maxNonExclusive;
    @JsonProperty("holders")
    @JacksonXmlProperty(localName = "holders")
    private LockHolders holders;
    /**
     * Collection of jobs which have to wait until the lock is free
     * 
     */
    @JsonProperty("queue")
    @JsonPropertyDescription("Collection of jobs which have to wait until the lock is free")
    @JacksonXmlProperty(localName = "queue")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "queue")
    private List<Queue> queue = new ArrayList<Queue>();
    /**
     * configuration status
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationStatus")
    @JacksonXmlProperty(localName = "configurationStatus")
    private ConfigurationState configurationStatus;

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
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JacksonXmlProperty(localName = "path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JacksonXmlProperty(localName = "path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    @JacksonXmlProperty(localName = "name")
    public String getName() {
        return name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    @JacksonXmlProperty(localName = "name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("maxNonExclusive")
    @JacksonXmlProperty(localName = "maxNonExclusive")
    public Integer getMaxNonExclusive() {
        return maxNonExclusive;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("maxNonExclusive")
    @JacksonXmlProperty(localName = "maxNonExclusive")
    public void setMaxNonExclusive(Integer maxNonExclusive) {
        this.maxNonExclusive = maxNonExclusive;
    }

    @JsonProperty("holders")
    @JacksonXmlProperty(localName = "holders")
    public LockHolders getHolders() {
        return holders;
    }

    @JsonProperty("holders")
    @JacksonXmlProperty(localName = "holders")
    public void setHolders(LockHolders holders) {
        this.holders = holders;
    }

    /**
     * Collection of jobs which have to wait until the lock is free
     * 
     */
    @JsonProperty("queue")
    @JacksonXmlProperty(localName = "queue")
    public List<Queue> getQueue() {
        return queue;
    }

    /**
     * Collection of jobs which have to wait until the lock is free
     * 
     */
    @JsonProperty("queue")
    @JacksonXmlProperty(localName = "queue")
    public void setQueue(List<Queue> queue) {
        this.queue = queue;
    }

    /**
     * configuration status
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationStatus")
    @JacksonXmlProperty(localName = "configurationStatus")
    public ConfigurationState getConfigurationStatus() {
        return configurationStatus;
    }

    /**
     * configuration status
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationStatus")
    @JacksonXmlProperty(localName = "configurationStatus")
    public void setConfigurationStatus(ConfigurationState configurationStatus) {
        this.configurationStatus = configurationStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("surveyDate", surveyDate).append("path", path).append("name", name).append("maxNonExclusive", maxNonExclusive).append("holders", holders).append("queue", queue).append("configurationStatus", configurationStatus).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(surveyDate).append(configurationStatus).append(holders).append(maxNonExclusive).append(name).append(queue).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LockV) == false) {
            return false;
        }
        LockV rhs = ((LockV) other);
        return new EqualsBuilder().append(path, rhs.path).append(surveyDate, rhs.surveyDate).append(configurationStatus, rhs.configurationStatus).append(holders, rhs.holders).append(maxNonExclusive, rhs.maxNonExclusive).append(name, rhs.name).append(queue, rhs.queue).isEquals();
    }

}
