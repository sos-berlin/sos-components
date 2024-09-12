
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
    "add"
})
public class AddToChangeRequest {

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
    @JsonProperty("add")
    private List<ChangeItem> add = new ArrayList<ChangeItem>();

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
    @JsonProperty("add")
    public List<ChangeItem> getAdd() {
        return add;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("add")
    public void setAdd(List<ChangeItem> add) {
        this.add = add;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("change", change).append("add", add).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(add).append(change).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AddToChangeRequest) == false) {
            return false;
        }
        AddToChangeRequest rhs = ((AddToChangeRequest) other);
        return new EqualsBuilder().append(add, rhs.add).append(change, rhs.change).isEquals();
    }

}
