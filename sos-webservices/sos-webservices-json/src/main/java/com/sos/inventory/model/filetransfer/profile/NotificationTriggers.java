
package com.sos.inventory.model.filetransfer.profile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Notification triggers. At least one of onError, onSuccess, onEmptyFiles must be set.
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "onError",
    "onSuccess",
    "onEmptyFiles"
})
public class NotificationTriggers {

    /**
     * Notification on error.
     * 
     */
    @JsonProperty("onError")
    @JsonPropertyDescription("Notification on error.")
    private NotificationTriggerOnError onError;
    /**
     * Notification on success.
     * 
     */
    @JsonProperty("onSuccess")
    @JsonPropertyDescription("Notification on success.")
    private NotificationTriggerOnSuccess onSuccess;
    /**
     * Notification when no files are found.
     * 
     */
    @JsonProperty("onEmptyFiles")
    @JsonPropertyDescription("Notification when no files are found.")
    private NotificationTriggerOnEmptyFiles onEmptyFiles;

    /**
     * No args constructor for use in serialization
     * 
     */
    public NotificationTriggers() {
    }

    /**
     * 
     * @param onError
     * @param onSuccess
     * @param onEmptyFiles
     */
    public NotificationTriggers(NotificationTriggerOnError onError, NotificationTriggerOnSuccess onSuccess, NotificationTriggerOnEmptyFiles onEmptyFiles) {
        super();
        this.onError = onError;
        this.onSuccess = onSuccess;
        this.onEmptyFiles = onEmptyFiles;
    }

    /**
     * Notification on error.
     * 
     */
    @JsonProperty("onError")
    public NotificationTriggerOnError getOnError() {
        return onError;
    }

    /**
     * Notification on error.
     * 
     */
    @JsonProperty("onError")
    public void setOnError(NotificationTriggerOnError onError) {
        this.onError = onError;
    }

    /**
     * Notification on success.
     * 
     */
    @JsonProperty("onSuccess")
    public NotificationTriggerOnSuccess getOnSuccess() {
        return onSuccess;
    }

    /**
     * Notification on success.
     * 
     */
    @JsonProperty("onSuccess")
    public void setOnSuccess(NotificationTriggerOnSuccess onSuccess) {
        this.onSuccess = onSuccess;
    }

    /**
     * Notification when no files are found.
     * 
     */
    @JsonProperty("onEmptyFiles")
    public NotificationTriggerOnEmptyFiles getOnEmptyFiles() {
        return onEmptyFiles;
    }

    /**
     * Notification when no files are found.
     * 
     */
    @JsonProperty("onEmptyFiles")
    public void setOnEmptyFiles(NotificationTriggerOnEmptyFiles onEmptyFiles) {
        this.onEmptyFiles = onEmptyFiles;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("onError", onError).append("onSuccess", onSuccess).append("onEmptyFiles", onEmptyFiles).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(onError).append(onSuccess).append(onEmptyFiles).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof NotificationTriggers) == false) {
            return false;
        }
        NotificationTriggers rhs = ((NotificationTriggers) other);
        return new EqualsBuilder().append(onError, rhs.onError).append(onSuccess, rhs.onSuccess).append(onEmptyFiles, rhs.onEmptyFiles).isEquals();
    }

}
