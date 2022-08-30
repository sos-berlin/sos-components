
package com.sos.js7.converter.js1.common.json.jobstreams;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * folders2Jobstream
 * <p>
 * List of all In Conditions
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobStream",
    "folders"
})
public class Folders2Jobstream {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStream")
    private String jobStream;
    @JsonProperty("folders")
    private List<String> folders = new ArrayList<String>();

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

    @JsonProperty("folders")
    public List<String> getFolders() {
        return folders;
    }

    @JsonProperty("folders")
    public void setFolders(List<String> folders) {
        this.folders = folders;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobStream", jobStream).append("folders", folders).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobStream).append(folders).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Folders2Jobstream) == false) {
            return false;
        }
        Folders2Jobstream rhs = ((Folders2Jobstream) other);
        return new EqualsBuilder().append(jobStream, rhs.jobStream).append(folders, rhs.folders).isEquals();
    }

}
