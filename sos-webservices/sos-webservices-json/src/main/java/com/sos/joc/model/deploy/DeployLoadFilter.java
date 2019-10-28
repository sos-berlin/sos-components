
package com.sos.joc.model.deploy;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Deploy load filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "objects"
})
public class DeployLoadFilter {

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
    @JsonProperty("objects")
    private List<LoadableObject> objects = new ArrayList<LoadableObject>();

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
    @JsonProperty("objects")
    public List<LoadableObject> getObjects() {
        return objects;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("objects")
    public void setObjects(List<LoadableObject> objects) {
        this.objects = objects;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("objects", objects).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobschedulerId).append(objects).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployLoadFilter) == false) {
            return false;
        }
        DeployLoadFilter rhs = ((DeployLoadFilter) other);
        return new EqualsBuilder().append(jobschedulerId, rhs.jobschedulerId).append(objects, rhs.objects).isEquals();
    }

}
