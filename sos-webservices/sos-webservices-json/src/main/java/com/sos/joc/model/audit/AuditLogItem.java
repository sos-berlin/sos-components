
package com.sos.joc.model.audit;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * audit
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "account",
    "request",
    "created",
    "controllerId",
    "category",
    "comment",
    "parameters",
    "timeSpent",
    "ticketLink"
})
public class AuditLogItem {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("account")
    private String account;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("request")
    private String request;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("created")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date created;
    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * Tree object type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("category")
    private CategoryType category;
    @JsonProperty("comment")
    private String comment;
    /**
     * JSON object as string, parameter of request
     * 
     */
    @JsonProperty("parameters")
    @JsonPropertyDescription("JSON object as string, parameter of request")
    private String parameters;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("timeSpent")
    private Integer timeSpent;
    @JsonProperty("ticketLink")
    private String ticketLink;

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("account")
    public void setAccount(String account) {
        this.account = account;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("request")
    public String getRequest() {
        return request;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("request")
    public void setRequest(String request) {
        this.request = request;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("created")
    public Date getCreated() {
        return created;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("created")
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * Tree object type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("category")
    public CategoryType getCategory() {
        return category;
    }

    /**
     * Tree object type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("category")
    public void setCategory(CategoryType category) {
        this.category = category;
    }

    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }

    @JsonProperty("comment")
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * JSON object as string, parameter of request
     * 
     */
    @JsonProperty("parameters")
    public String getParameters() {
        return parameters;
    }

    /**
     * JSON object as string, parameter of request
     * 
     */
    @JsonProperty("parameters")
    public void setParameters(String parameters) {
        this.parameters = parameters;
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

    @JsonProperty("ticketLink")
    public String getTicketLink() {
        return ticketLink;
    }

    @JsonProperty("ticketLink")
    public void setTicketLink(String ticketLink) {
        this.ticketLink = ticketLink;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("account", account).append("request", request).append("created", created).append("controllerId", controllerId).append("category", category).append("comment", comment).append("parameters", parameters).append("timeSpent", timeSpent).append("ticketLink", ticketLink).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(request).append(controllerId).append(timeSpent).append(created).append(comment).append(category).append(parameters).append(account).append(ticketLink).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AuditLogItem) == false) {
            return false;
        }
        AuditLogItem rhs = ((AuditLogItem) other);
        return new EqualsBuilder().append(request, rhs.request).append(controllerId, rhs.controllerId).append(timeSpent, rhs.timeSpent).append(created, rhs.created).append(comment, rhs.comment).append(category, rhs.category).append(parameters, rhs.parameters).append(account, rhs.account).append(ticketLink, rhs.ticketLink).isEquals();
    }

}
