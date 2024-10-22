
package com.sos.joc.model.inventory.changes;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
    "configurations"
})
public class GetChangesFilter {

    @JsonProperty("configurations")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ChangeItem> configurations = new LinkedHashSet<ChangeItem>();

    @JsonProperty("configurations")
    public Set<ChangeItem> getConfigurations() {
        return configurations;
    }

    @JsonProperty("configurations")
    public void setConfigurations(Set<ChangeItem> configurations) {
        this.configurations = configurations;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("configurations", configurations).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(configurations).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof GetChangesFilter) == false) {
            return false;
        }
        GetChangesFilter rhs = ((GetChangesFilter) other);
        return new EqualsBuilder().append(configurations, rhs.configurations).isEquals();
    }

}
