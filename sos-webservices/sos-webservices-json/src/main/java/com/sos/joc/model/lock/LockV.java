
package com.sos.joc.model.lock;

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
    "queue"
})
public class LockV {

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
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of an object.")
    private String path;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("maxNonExclusive")
    private Integer maxNonExclusive;
    @JsonProperty("holders")
    private LockHolders holders;
    /**
     * Collection of jobs which have to wait until the lock is free
     * 
     */
    @JsonProperty("queue")
    @JsonPropertyDescription("Collection of jobs which have to wait until the lock is free")
    private List<Queue> queue = new ArrayList<Queue>();

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
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
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
    public void setMaxNonExclusive(Integer maxNonExclusive) {
        this.maxNonExclusive = maxNonExclusive;
    }

    @JsonProperty("holders")
    public LockHolders getHolders() {
        return holders;
    }

    @JsonProperty("holders")
    public void setHolders(LockHolders holders) {
        this.holders = holders;
    }

    /**
     * Collection of jobs which have to wait until the lock is free
     * 
     */
    @JsonProperty("queue")
    public List<Queue> getQueue() {
        return queue;
    }

    /**
     * Collection of jobs which have to wait until the lock is free
     * 
     */
    @JsonProperty("queue")
    public void setQueue(List<Queue> queue) {
        this.queue = queue;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("surveyDate", surveyDate).append("path", path).append("name", name).append("maxNonExclusive", maxNonExclusive).append("holders", holders).append("queue", queue).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(surveyDate).append(holders).append(maxNonExclusive).append(name).append(queue).toHashCode();
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
        return new EqualsBuilder().append(path, rhs.path).append(surveyDate, rhs.surveyDate).append(holders, rhs.holders).append(maxNonExclusive, rhs.maxNonExclusive).append(name, rhs.name).append(queue, rhs.queue).isEquals();
    }

}
