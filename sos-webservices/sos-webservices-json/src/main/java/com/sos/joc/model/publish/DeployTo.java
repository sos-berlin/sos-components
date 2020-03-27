
package com.sos.joc.model.publish;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.JobSchedulerId;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * deploy
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "schedulers",
    "jsObjects"
})
public class DeployTo {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("schedulers")
    private List<JobSchedulerId> schedulers = new ArrayList<JobSchedulerId>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jsObjects")
    private List<String> jsObjects = new ArrayList<String>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("schedulers")
    public List<JobSchedulerId> getSchedulers() {
        return schedulers;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("schedulers")
    public void setSchedulers(List<JobSchedulerId> schedulers) {
        this.schedulers = schedulers;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jsObjects")
    public List<String> getJsObjects() {
        return jsObjects;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jsObjects")
    public void setJsObjects(List<String> jsObjects) {
        this.jsObjects = jsObjects;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("schedulers", schedulers).append("jsObjects", jsObjects).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(schedulers).append(jsObjects).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployTo) == false) {
            return false;
        }
        DeployTo rhs = ((DeployTo) other);
        return new EqualsBuilder().append(schedulers, rhs.schedulers).append(jsObjects, rhs.jsObjects).isEquals();
    }

}
