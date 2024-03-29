
package com.sos.joc.model.audit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * auditParams
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "comment",
    "timeSpent",
    "ticketLink"
})
public class AuditParams {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("comment")
    private String comment;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("timeSpent")
    private Integer timeSpent;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("ticketLink")
    private String ticketLink;

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("comment")
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("timeSpent")
    public Integer getTimeSpent() {
        return timeSpent;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("timeSpent")
    public void setTimeSpent(Integer timeSpent) {
        this.timeSpent = timeSpent;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("ticketLink")
    public String getTicketLink() {
        return ticketLink;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("ticketLink")
    public void setTicketLink(String ticketLink) {
        this.ticketLink = ticketLink;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("comment", comment).append("timeSpent", timeSpent).append("ticketLink", ticketLink).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(comment).append(timeSpent).append(ticketLink).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AuditParams) == false) {
            return false;
        }
        AuditParams rhs = ((AuditParams) other);
        return new EqualsBuilder().append(comment, rhs.comment).append(timeSpent, rhs.timeSpent).append(ticketLink, rhs.ticketLink).isEquals();
    }

}
