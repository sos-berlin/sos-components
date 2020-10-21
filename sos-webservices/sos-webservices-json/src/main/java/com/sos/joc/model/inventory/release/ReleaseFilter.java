
package com.sos.joc.model.inventory.release;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * release
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerIds",
    "update",
    "delete"
})
public class ReleaseFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerIds")
    private List<String> controllerIds = new ArrayList<String>();
    @JsonProperty("update")
    private List<ReleaseUpdate> update = new ArrayList<ReleaseUpdate>();
    @JsonProperty("delete")
    private List<ReleaseDelete> delete = new ArrayList<ReleaseDelete>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerIds")
    public List<String> getControllerIds() {
        return controllerIds;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerIds")
    public void setControllerIds(List<String> controllerIds) {
        this.controllerIds = controllerIds;
    }

    @JsonProperty("update")
    public List<ReleaseUpdate> getUpdate() {
        return update;
    }

    @JsonProperty("update")
    public void setUpdate(List<ReleaseUpdate> update) {
        this.update = update;
    }

    @JsonProperty("delete")
    public List<ReleaseDelete> getDelete() {
        return delete;
    }

    @JsonProperty("delete")
    public void setDelete(List<ReleaseDelete> delete) {
        this.delete = delete;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerIds", controllerIds).append("update", update).append("delete", delete).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(update).append(delete).append(controllerIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReleaseFilter) == false) {
            return false;
        }
        ReleaseFilter rhs = ((ReleaseFilter) other);
        return new EqualsBuilder().append(update, rhs.update).append(delete, rhs.delete).append(controllerIds, rhs.controllerIds).isEquals();
    }

}
