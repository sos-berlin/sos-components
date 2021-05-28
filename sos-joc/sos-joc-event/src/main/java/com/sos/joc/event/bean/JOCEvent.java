
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
import com.sos.joc.event.bean.documentation.DocumentationEvent;
import com.sos.joc.event.bean.event.EventServiceEvent;
import com.sos.joc.event.bean.history.HistoryEvent;
import com.sos.joc.event.bean.inventory.InventoryEvent;
import com.sos.joc.event.bean.inventory.InventoryTrashEvent;
import com.sos.joc.event.bean.problem.ProblemEvent;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "TYPE", visible = true)
@JsonSubTypes({ 
    @JsonSubTypes.Type(HistoryEvent.class),
    @JsonSubTypes.Type(ClusterEvent.class),
    @JsonSubTypes.Type(ProblemEvent.class),
    @JsonSubTypes.Type(EventServiceEvent.class),
    @JsonSubTypes.Type(InventoryEvent.class),
    @JsonSubTypes.Type(InventoryTrashEvent.class),
    @JsonSubTypes.Type(DocumentationEvent.class)
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
     * @param controllerId
     * @param variables
     */
    public JOCEvent(String key, String controllerId, Map<String, String> variables) {
        this.key = key;
        this.controllerId = controllerId;
        if (variables != null) {
            this.variables = variables;
        }
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
    @JsonProperty("eventId")
    private Long eventId;
    @JsonProperty("controllerId")
    private String controllerId;
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
    
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    @JsonProperty("controllerId")
    public void setControllerId(String jobschedulerId) {
        this.controllerId = jobschedulerId;
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
    @JsonProperty("eventId")
    public Long getEventId() {
        return eventId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("eventId")
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
    
    @JsonAnyGetter
    public Map<String, String> getVariables() {
        return this.variables;
    }
    
    public void putVariable(String name, String value) {
        this.variables.put(name, value);
    }

    @JsonAnySetter
    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("controllerId", controllerId).append("key", key).append("variables", variables).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tYPE).append(controllerId).append(key).toHashCode();
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
        return new EqualsBuilder().append(tYPE, rhs.tYPE).append(controllerId, rhs.controllerId).append(key, rhs.key).isEquals();
    }

}
