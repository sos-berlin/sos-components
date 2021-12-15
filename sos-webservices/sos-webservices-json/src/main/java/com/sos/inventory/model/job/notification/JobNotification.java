
package com.sos.inventory.model.job.notification;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * job notification
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "types",
    "mail"
})
public class JobNotification {

    @JsonProperty("types")
    private List<JobNotificationType> types = null;
    /**
     * job notification mail
     * <p>
     * 
     * 
     */
    @JsonProperty("mail")
    private JobNotificationMail mail;

    /**
     * No args constructor for use in serialization
     * 
     */
    public JobNotification() {
    }

    /**
     * 
     * @param types
     * @param mail
     */
    public JobNotification(List<JobNotificationType> types, JobNotificationMail mail) {
        super();
        this.types = types;
        this.mail = mail;
    }

    @JsonProperty("types")
    public List<JobNotificationType> getTypes() {
        return types;
    }

    @JsonProperty("types")
    public void setTypes(List<JobNotificationType> types) {
        this.types = types;
    }

    /**
     * job notification mail
     * <p>
     * 
     * 
     */
    @JsonProperty("mail")
    public JobNotificationMail getMail() {
        return mail;
    }

    /**
     * job notification mail
     * <p>
     * 
     * 
     */
    @JsonProperty("mail")
    public void setMail(JobNotificationMail mail) {
        this.mail = mail;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("types", types).append("mail", mail).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(mail).append(types).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobNotification) == false) {
            return false;
        }
        JobNotification rhs = ((JobNotification) other);
        return new EqualsBuilder().append(mail, rhs.mail).append(types, rhs.types).isEquals();
    }

}
