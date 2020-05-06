
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
    "update",
    "delete"
})
public class DeployFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("schedulers")
    private List<JobSchedulerId> schedulers = new ArrayList<JobSchedulerId>();
    @JsonProperty("update")
    private List<String> update = new ArrayList<String>();
    @JsonProperty("delete")
    private List<String> delete = new ArrayList<String>();

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

    @JsonProperty("update")
    public List<String> getUpdate() {
        return update;
    }

    @JsonProperty("update")
    public void setUpdate(List<String> update) {
        this.update = update;
    }

    @JsonProperty("delete")
    public List<String> getDelete() {
        return delete;
    }

    @JsonProperty("delete")
    public void setDelete(List<String> delete) {
        this.delete = delete;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("schedulers", schedulers).append("update", update).append("delete", delete).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(update).append(schedulers).append(delete).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployFilter) == false) {
            return false;
        }
        DeployFilter rhs = ((DeployFilter) other);
        return new EqualsBuilder().append(update, rhs.update).append(schedulers, rhs.schedulers).append(delete, rhs.delete).isEquals();
    }

}
