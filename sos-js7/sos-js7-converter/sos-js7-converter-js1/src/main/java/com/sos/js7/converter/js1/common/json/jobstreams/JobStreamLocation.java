
package com.sos.js7.converter.js1.common.json.jobstreams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JobStreamLocation
 * <p>
 * Reset Workflow, unconsume expressions
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobStream",
    "folder"
})
public class JobStreamLocation {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStream")
    private String jobStream;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("folder")
    private String folder;

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStream")
    public String getJobStream() {
        return jobStream;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStream")
    public void setJobStream(String jobStream) {
        this.jobStream = jobStream;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("folder")
    public String getFolder() {
        return folder;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("folder")
    public void setFolder(String folder) {
        this.folder = folder;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobStream", jobStream).append("folder", folder).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobStream).append(folder).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobStreamLocation) == false) {
            return false;
        }
        JobStreamLocation rhs = ((JobStreamLocation) other);
        return new EqualsBuilder().append(jobStream, rhs.jobStream).append(folder, rhs.folder).isEquals();
    }

}
