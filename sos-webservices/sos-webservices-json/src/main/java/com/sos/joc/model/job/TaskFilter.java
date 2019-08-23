
package com.sos.joc.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.LogMime;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * task filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "taskId",
    "filename",
    "mime"
})
public class TaskFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("taskId")
    private Long taskId;
    @JsonProperty("filename")
    private String filename;
    /**
     * log mime filter
     * <p>
     * The log can have a HTML representation where the HTML gets a highlighting via CSS classes.
     * 
     */
    @JsonProperty("mime")
    @JsonPropertyDescription("The log can have a HTML representation where the HTML gets a highlighting via CSS classes.")
    private LogMime mime = LogMime.fromValue("PLAIN");

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("taskId")
    public Long getTaskId() {
        return taskId;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("taskId")
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    @JsonProperty("filename")
    public String getFilename() {
        return filename;
    }

    @JsonProperty("filename")
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * log mime filter
     * <p>
     * The log can have a HTML representation where the HTML gets a highlighting via CSS classes.
     * 
     */
    @JsonProperty("mime")
    public LogMime getMime() {
        return mime;
    }

    /**
     * log mime filter
     * <p>
     * The log can have a HTML representation where the HTML gets a highlighting via CSS classes.
     * 
     */
    @JsonProperty("mime")
    public void setMime(LogMime mime) {
        this.mime = mime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("taskId", taskId).append("filename", filename).append("mime", mime).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(filename).append(jobschedulerId).append(taskId).append(mime).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TaskFilter) == false) {
            return false;
        }
        TaskFilter rhs = ((TaskFilter) other);
        return new EqualsBuilder().append(filename, rhs.filename).append(jobschedulerId, rhs.jobschedulerId).append(taskId, rhs.taskId).append(mime, rhs.mime).isEquals();
    }

}
