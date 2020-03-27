
package com.sos.joc.model.publish;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * PublishExportFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "jsObjectPaths"
})
public class PublishExportFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jsObjectPaths")
    private List<String> jsObjectPaths = new ArrayList<String>();

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
     * 
     * (Required)
     * 
     */
    @JsonProperty("jsObjectPaths")
    public List<String> getJsObjectPaths() {
        return jsObjectPaths;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jsObjectPaths")
    public void setJsObjectPaths(List<String> jsObjectPaths) {
        this.jsObjectPaths = jsObjectPaths;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("jsObjectPaths", jsObjectPaths).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobschedulerId).append(jsObjectPaths).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PublishExportFilter) == false) {
            return false;
        }
        PublishExportFilter rhs = ((PublishExportFilter) other);
        return new EqualsBuilder().append(jobschedulerId, rhs.jobschedulerId).append(jsObjectPaths, rhs.jsObjectPaths).isEquals();
    }

}
