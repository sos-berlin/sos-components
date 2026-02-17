
package com.sos.joc.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.IEventObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * event from approval notification
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "eventId",
    "account",
    "numOfUnreadNotes"
})
public class EventNoteNotification implements IEventObject
{

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("eventId")
    private Long eventId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("account")
    private String account;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfUnreadNotes")
    private Long numOfUnreadNotes;

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

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("account")
    public String getAccount() {
        return account;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("account")
    public void setAccount(String account) {
        this.account = account;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfUnreadNotes")
    public Long getNumOfUnreadNotes() {
        return numOfUnreadNotes;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfUnreadNotes")
    public void setNumOfUnreadNotes(Long numOfUnreadNotes) {
        this.numOfUnreadNotes = numOfUnreadNotes;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("eventId", eventId).append("account", account).append("numOfUnreadNotes", numOfUnreadNotes).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(eventId).append(numOfUnreadNotes).append(account).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof EventNoteNotification) == false) {
            return false;
        }
        EventNoteNotification rhs = ((EventNoteNotification) other);
        return new EqualsBuilder().append(eventId, rhs.eventId).append(numOfUnreadNotes, rhs.numOfUnreadNotes).append(account, rhs.account).isEquals();
    }

}
