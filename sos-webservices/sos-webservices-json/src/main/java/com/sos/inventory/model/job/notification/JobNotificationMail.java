
package com.sos.inventory.model.job.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * job notification mail
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "to",
    "cc",
    "bcc"
})
public class JobNotificationMail {

    /**
     * job notification mail recipients, string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("to")
    private String to;
    /**
     * job notification mail recipients, string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("cc")
    private String cc;
    /**
     * job notification mail recipients, string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bcc")
    private String bcc;

    /**
     * No args constructor for use in serialization
     * 
     */
    public JobNotificationMail() {
    }

    /**
     * 
     * @param cc
     * @param bcc
     * @param to
     */
    public JobNotificationMail(String to, String cc, String bcc) {
        super();
        this.to = to;
        this.cc = cc;
        this.bcc = bcc;
    }

    /**
     * job notification mail recipients, string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("to")
    public String getTo() {
        return to;
    }

    /**
     * job notification mail recipients, string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("to")
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * job notification mail recipients, string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("cc")
    public String getCc() {
        return cc;
    }

    /**
     * job notification mail recipients, string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("cc")
    public void setCc(String cc) {
        this.cc = cc;
    }

    /**
     * job notification mail recipients, string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bcc")
    public String getBcc() {
        return bcc;
    }

    /**
     * job notification mail recipients, string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bcc")
    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("to", to).append("cc", cc).append("bcc", bcc).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(cc).append(to).append(bcc).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobNotificationMail) == false) {
            return false;
        }
        JobNotificationMail rhs = ((JobNotificationMail) other);
        return new EqualsBuilder().append(cc, rhs.cc).append(to, rhs.to).append(bcc, rhs.bcc).isEquals();
    }

}
