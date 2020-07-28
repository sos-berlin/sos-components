
package com.sos.joc.event.bean;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sos.joc.event.bean.cluster.ClusterEvent;
import com.sos.joc.event.bean.history.HistoryEvent;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "TYPE", visible = true)
@JsonSubTypes({ 
    @JsonSubTypes.Type(HistoryEvent.class),
    @JsonSubTypes.Type(ClusterEvent.class)
})


public abstract class JOCEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public JOCEvent() {
    }

    /**
     * @param key
     * @param jobschedulerId
     * @param variables
     */
    public JOCEvent(String key, String jobschedulerId, Map<String, String> variables) {
        this.key = key;
        this.jobschedulerId = jobschedulerId;
        this.variables = variables;
    }

    /**
     * eventType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    private String tYPE;
    @JsonProperty("timestamp")
    private Long timestamp;
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("key")
    private String key;
    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonIgnore
    private Map<String, String> variables = new HashMap<String, String>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public String getTYPE() {
        return tYPE;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(String tYPE) {
        this.tYPE = tYPE;
    }
    
    @JsonProperty("jobschedulerId")
    public String getJobSchedulerId() {
        return jobschedulerId;
    }

    @JsonProperty("jobSchedulerId")
    public void setJobSchedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("key")
    public String getKey() {
        return key;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("key")
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("timestamp")
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("timestamp")
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    @JsonAnyGetter
    public Map<String, String> getVariables() {
        return this.variables;
    }

    @JsonAnySetter
    public void setVariables(String name, String value) {
        this.variables.put(name, value);
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("jobschedulerId", jobschedulerId).append("key", key).append("variables", variables).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tYPE).append(jobschedulerId).append(key).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JOCEvent) == false) {
            return false;
        }
        JOCEvent rhs = ((JOCEvent) other);
        return new EqualsBuilder().append(tYPE, rhs.tYPE).append(jobschedulerId, rhs.jobschedulerId).append(key, rhs.key).isEquals();
    }

}
