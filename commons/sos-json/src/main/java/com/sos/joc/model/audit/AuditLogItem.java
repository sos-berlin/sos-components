
package com.sos.joc.model.audit;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
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
    "jobschedulerId",
    "comment",
    "parameters",
    "job",
    "jobChain",
    "orderId",
    "calendar",
    "timeSpent",
    "ticketLink"
})
public class AuditLogItem {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("account")
    @JacksonXmlProperty(localName = "account")
    private String account;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("request")
    @JacksonXmlProperty(localName = "request")
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
    @JacksonXmlProperty(localName = "created")
    private Date created;
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("comment")
    @JacksonXmlProperty(localName = "comment")
    private String comment;
    /**
     * JSON object as string, parameter of request
     * 
     */
    @JsonProperty("parameters")
    @JsonPropertyDescription("JSON object as string, parameter of request")
    @JacksonXmlProperty(localName = "parameters")
    private String parameters;
    @JsonProperty("job")
    @JacksonXmlProperty(localName = "job")
    private String job;
    @JsonProperty("jobChain")
    @JacksonXmlProperty(localName = "jobChain")
    private String jobChain;
    @JsonProperty("orderId")
    @JacksonXmlProperty(localName = "orderId")
    private String orderId;
    @JsonProperty("calendar")
    @JacksonXmlProperty(localName = "calendar")
    private String calendar;
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

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("account")
    @JacksonXmlProperty(localName = "account")
    public String getAccount() {
        return account;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("account")
    @JacksonXmlProperty(localName = "account")
    public void setAccount(String account) {
        this.account = account;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("request")
    @JacksonXmlProperty(localName = "request")
    public String getRequest() {
        return request;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("request")
    @JacksonXmlProperty(localName = "request")
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
    @JacksonXmlProperty(localName = "created")
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
    @JacksonXmlProperty(localName = "created")
    public void setCreated(Date created) {
        this.created = created;
    }

    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

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
     * JSON object as string, parameter of request
     * 
     */
    @JsonProperty("parameters")
    @JacksonXmlProperty(localName = "parameters")
    public String getParameters() {
        return parameters;
    }

    /**
     * JSON object as string, parameter of request
     * 
     */
    @JsonProperty("parameters")
    @JacksonXmlProperty(localName = "parameters")
    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    @JsonProperty("job")
    @JacksonXmlProperty(localName = "job")
    public String getJob() {
        return job;
    }

    @JsonProperty("job")
    @JacksonXmlProperty(localName = "job")
    public void setJob(String job) {
        this.job = job;
    }

    @JsonProperty("jobChain")
    @JacksonXmlProperty(localName = "jobChain")
    public String getJobChain() {
        return jobChain;
    }

    @JsonProperty("jobChain")
    @JacksonXmlProperty(localName = "jobChain")
    public void setJobChain(String jobChain) {
        this.jobChain = jobChain;
    }

    @JsonProperty("orderId")
    @JacksonXmlProperty(localName = "orderId")
    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("orderId")
    @JacksonXmlProperty(localName = "orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @JsonProperty("calendar")
    @JacksonXmlProperty(localName = "calendar")
    public String getCalendar() {
        return calendar;
    }

    @JsonProperty("calendar")
    @JacksonXmlProperty(localName = "calendar")
    public void setCalendar(String calendar) {
        this.calendar = calendar;
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
        return new ToStringBuilder(this).append("account", account).append("request", request).append("created", created).append("jobschedulerId", jobschedulerId).append("comment", comment).append("parameters", parameters).append("job", job).append("jobChain", jobChain).append("orderId", orderId).append("calendar", calendar).append("timeSpent", timeSpent).append("ticketLink", ticketLink).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(calendar).append(request).append(orderId).append(timeSpent).append(created).append(jobChain).append(ticketLink).append(comment).append(jobschedulerId).append(job).append(parameters).append(account).toHashCode();
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
        return new EqualsBuilder().append(calendar, rhs.calendar).append(request, rhs.request).append(orderId, rhs.orderId).append(timeSpent, rhs.timeSpent).append(created, rhs.created).append(jobChain, rhs.jobChain).append(ticketLink, rhs.ticketLink).append(comment, rhs.comment).append(jobschedulerId, rhs.jobschedulerId).append(job, rhs.job).append(parameters, rhs.parameters).append(account, rhs.account).isEquals();
    }

}
