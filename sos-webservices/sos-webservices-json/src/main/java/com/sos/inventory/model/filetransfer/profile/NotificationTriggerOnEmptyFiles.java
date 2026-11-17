
package com.sos.inventory.model.filetransfer.profile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Notification when no files are found.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "mailFragmentName"
})
public class NotificationTriggerOnEmptyFiles {

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
    public NotificationTriggerOnEmptyFiles() {
    }

    /**
     * 
     * @param mailFragmentName
     */
    public NotificationTriggerOnEmptyFiles(String mailFragmentName) {
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
        if ((other instanceof NotificationTriggerOnEmptyFiles) == false) {
            return false;
        }
        NotificationTriggerOnEmptyFiles rhs = ((NotificationTriggerOnEmptyFiles) other);
        return new EqualsBuilder().append(mailFragmentName, rhs.mailFragmentName).isEquals();
    }

}
