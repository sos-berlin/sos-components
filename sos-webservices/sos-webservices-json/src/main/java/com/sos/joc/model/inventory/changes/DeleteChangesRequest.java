
package com.sos.joc.model.inventory.changes;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.changes.common.Change;
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
    "changes"
})
public class DeleteChangesRequest {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("changes")
    private List<Change> changes = new ArrayList<Change>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("changes")
    public List<Change> getChanges() {
        return changes;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("changes")
    public void setChanges(List<Change> changes) {
        this.changes = changes;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("changes", changes).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(changes).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeleteChangesRequest) == false) {
            return false;
        }
        DeleteChangesRequest rhs = ((DeleteChangesRequest) other);
        return new EqualsBuilder().append(changes, rhs.changes).isEquals();
    }

}
