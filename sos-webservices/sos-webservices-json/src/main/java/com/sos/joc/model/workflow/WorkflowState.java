
package com.sos.joc.model.workflow;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.controller.model.common.SyncState;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * status of resuming or suspension
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "surveyDate",
    "state",
    "confirmedAgentNames",
    "notConfirmedAgentNames"
})
public class WorkflowState {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("surveyDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date surveyDate;
    /**
     * sync state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private SyncState state;
    /**
     * only specified, if state._text == RESUMING or SUSPENDING
     * 
     */
    @JsonProperty("confirmedAgentNames")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("only specified, if state._text == RESUMING or SUSPENDING")
    private Set<String> confirmedAgentNames = new LinkedHashSet<String>();
    /**
     * only specified, if state._text == RESUMING or SUSPENDING
     * 
     */
    @JsonProperty("notConfirmedAgentNames")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("only specified, if state._text == RESUMING or SUSPENDING")
    private Set<String> notConfirmedAgentNames = new LinkedHashSet<String>();

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
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
     * 
     */
    @JsonProperty("surveyDate")
    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    /**
     * sync state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public SyncState getState() {
        return state;
    }

    /**
     * sync state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public void setState(SyncState state) {
        this.state = state;
    }

    /**
     * only specified, if state._text == RESUMING or SUSPENDING
     * 
     */
    @JsonProperty("confirmedAgentNames")
    public Set<String> getConfirmedAgentNames() {
        return confirmedAgentNames;
    }

    /**
     * only specified, if state._text == RESUMING or SUSPENDING
     * 
     */
    @JsonProperty("confirmedAgentNames")
    public void setConfirmedAgentNames(Set<String> confirmedAgentNames) {
        this.confirmedAgentNames = confirmedAgentNames;
    }

    /**
     * only specified, if state._text == RESUMING or SUSPENDING
     * 
     */
    @JsonProperty("notConfirmedAgentNames")
    public Set<String> getNotConfirmedAgentNames() {
        return notConfirmedAgentNames;
    }

    /**
     * only specified, if state._text == RESUMING or SUSPENDING
     * 
     */
    @JsonProperty("notConfirmedAgentNames")
    public void setNotConfirmedAgentNames(Set<String> notConfirmedAgentNames) {
        this.notConfirmedAgentNames = notConfirmedAgentNames;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("surveyDate", surveyDate).append("state", state).append("confirmedAgentNames", confirmedAgentNames).append("notConfirmedAgentNames", notConfirmedAgentNames).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(state).append(deliveryDate).append(surveyDate).append(confirmedAgentNames).append(notConfirmedAgentNames).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WorkflowState) == false) {
            return false;
        }
        WorkflowState rhs = ((WorkflowState) other);
        return new EqualsBuilder().append(state, rhs.state).append(deliveryDate, rhs.deliveryDate).append(surveyDate, rhs.surveyDate).append(confirmedAgentNames, rhs.confirmedAgentNames).append(notConfirmedAgentNames, rhs.notConfirmedAgentNames).isEquals();
    }

}
