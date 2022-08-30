
package com.sos.js7.converter.js1.common.json.jobstreams;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * job In-Condition
 * <p>
 * job In Condition
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "job",
    "haveReferenceToOtherFolders",
    "inconditions"
})
public class JobInCondition {

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    private String job;
    @JsonProperty("haveReferenceToOtherFolders")
    private Boolean haveReferenceToOtherFolders;
    @JsonProperty("inconditions")
    private List<InCondition> inconditions = new ArrayList<InCondition>();

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    public String getJob() {
        return job;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    public void setJob(String job) {
        this.job = job;
    }

    @JsonProperty("haveReferenceToOtherFolders")
    public Boolean getHaveReferenceToOtherFolders() {
        return haveReferenceToOtherFolders;
    }

    @JsonProperty("haveReferenceToOtherFolders")
    public void setHaveReferenceToOtherFolders(Boolean haveReferenceToOtherFolders) {
        this.haveReferenceToOtherFolders = haveReferenceToOtherFolders;
    }

    @JsonProperty("inconditions")
    public List<InCondition> getInconditions() {
        return inconditions;
    }

    @JsonProperty("inconditions")
    public void setInconditions(List<InCondition> inconditions) {
        this.inconditions = inconditions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("job", job).append("haveReferenceToOtherFolders", haveReferenceToOtherFolders).append("inconditions", inconditions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(inconditions).append(haveReferenceToOtherFolders).append(job).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobInCondition) == false) {
            return false;
        }
        JobInCondition rhs = ((JobInCondition) other);
        return new EqualsBuilder().append(inconditions, rhs.inconditions).append(haveReferenceToOtherFolders, rhs.haveReferenceToOtherFolders).append(job, rhs.job).isEquals();
    }

}
