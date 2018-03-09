
package com.sos.joc.model.audit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


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

    @JsonProperty("comment")
    @JacksonXmlProperty(localName = "comment")
    private String comment;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("timeSpent")
    @JacksonXmlProperty(localName = "timeSpent")
    private Integer timeSpent;
    @JsonProperty("ticketLink")
    @JacksonXmlProperty(localName = "ticketLink")
    private String ticketLink;

    @JsonProperty("comment")
    @JacksonXmlProperty(localName = "comment")
    public String getComment() {
        return comment;
    }

    @JsonProperty("comment")
    @JacksonXmlProperty(localName = "comment")
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
    @JacksonXmlProperty(localName = "timeSpent")
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
    @JacksonXmlProperty(localName = "timeSpent")
    public void setTimeSpent(Integer timeSpent) {
        this.timeSpent = timeSpent;
    }

    @JsonProperty("ticketLink")
    @JacksonXmlProperty(localName = "ticketLink")
    public String getTicketLink() {
        return ticketLink;
    }

    @JsonProperty("ticketLink")
    @JacksonXmlProperty(localName = "ticketLink")
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
