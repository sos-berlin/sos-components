
package com.sos.joc.model.inventory.changes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.changes.common.ChangeIdentifier;
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
    "store"
})
public class StoreChangeRequest {

    /**
     * dependencies
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("store")
    private ChangeIdentifier store;

    /**
     * dependencies
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("store")
    public ChangeIdentifier getStore() {
        return store;
    }

    /**
     * dependencies
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("store")
    public void setStore(ChangeIdentifier store) {
        this.store = store;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("store", store).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(store).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StoreChangeRequest) == false) {
            return false;
        }
        StoreChangeRequest rhs = ((StoreChangeRequest) other);
        return new EqualsBuilder().append(store, rhs.store).isEquals();
    }

}
