
package com.sos.joc.model.inventory.changes;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.joc.model.inventory.changes.common.GetChangeResponseItem;
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
public class GetChangesResponse {

    @JsonProperty("changes")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<GetChangeResponseItem> changes = new LinkedHashSet<GetChangeResponseItem>();

    @JsonProperty("changes")
    public Set<GetChangeResponseItem> getChanges() {
        return changes;
    }

    @JsonProperty("changes")
    public void setChanges(Set<GetChangeResponseItem> changes) {
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
        if ((other instanceof GetChangesResponse) == false) {
            return false;
        }
        GetChangesResponse rhs = ((GetChangesResponse) other);
        return new EqualsBuilder().append(changes, rhs.changes).isEquals();
    }

}
