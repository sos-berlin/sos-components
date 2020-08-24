
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
 * deploy
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllers",
    "update",
    "delete"
})
public class DeployFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllers")
    private List<Controller> controllers = new ArrayList<Controller>();
    @JsonProperty("update")
    private List<DeployUpdate> update = new ArrayList<DeployUpdate>();
    @JsonProperty("delete")
    private List<DeployDelete> delete = new ArrayList<DeployDelete>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllers")
    public List<Controller> getControllers() {
        return controllers;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllers")
    public void setControllers(List<Controller> controllers) {
        this.controllers = controllers;
    }

    @JsonProperty("update")
    public List<DeployUpdate> getUpdate() {
        return update;
    }

    @JsonProperty("update")
    public void setUpdate(List<DeployUpdate> update) {
        this.update = update;
    }

    @JsonProperty("delete")
    public List<DeployDelete> getDelete() {
        return delete;
    }

    @JsonProperty("delete")
    public void setDelete(List<DeployDelete> delete) {
        this.delete = delete;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllers", controllers).append("update", update).append("delete", delete).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllers).append(update).append(delete).toHashCode();
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
        return new EqualsBuilder().append(controllers, rhs.controllers).append(update, rhs.update).append(delete, rhs.delete).isEquals();
    }

}
