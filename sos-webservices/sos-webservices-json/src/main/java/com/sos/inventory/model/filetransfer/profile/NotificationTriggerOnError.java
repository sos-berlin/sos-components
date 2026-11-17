
package com.sos.inventory.model.filetransfer.profile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Notification on error.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "mailFragmentName"
})
public class NotificationTriggerOnError {

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("mailFragmentName")
    private String mailFragmentName;

    /**
     * No args constructor for use in serialization
     * 
     */
    public NotificationTriggerOnError() {
    }

    /**
     * 
     * @param mailFragmentName
     */
    public NotificationTriggerOnError(String mailFragmentName) {
        super();
        this.mailFragmentName = mailFragmentName;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("mailFragmentName")
    public String getMailFragmentName() {
        return mailFragmentName;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("mailFragmentName")
    public void setMailFragmentName(String mailFragmentName) {
        this.mailFragmentName = mailFragmentName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("mailFragmentName", mailFragmentName).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(mailFragmentName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof NotificationTriggerOnError) == false) {
            return false;
        }
        NotificationTriggerOnError rhs = ((NotificationTriggerOnError) other);
        return new EqualsBuilder().append(mailFragmentName, rhs.mailFragmentName).isEquals();
    }

}
