
package com.sos.joc.model.inventory.changes;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.changes.common.Change;
import com.sos.joc.model.inventory.changes.common.ChangeItem;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * changes
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "change",
    "remove"
})
public class RemoveFromChangeRequest {

    /**
     * dependencies
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("change")
    private Change change;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("remove")
    private List<ChangeItem> remove = new ArrayList<ChangeItem>();

    /**
     * dependencies
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("change")
    public Change getChange() {
        return change;
    }

    /**
     * dependencies
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("change")
    public void setChange(Change change) {
        this.change = change;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("remove")
    public List<ChangeItem> getRemove() {
        return remove;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("remove")
    public void setRemove(List<ChangeItem> remove) {
        this.remove = remove;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("change", change).append("remove", remove).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(remove).append(change).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RemoveFromChangeRequest) == false) {
            return false;
        }
        RemoveFromChangeRequest rhs = ((RemoveFromChangeRequest) other);
        return new EqualsBuilder().append(remove, rhs.remove).append(change, rhs.change).isEquals();
    }

}
