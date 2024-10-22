
package com.sos.joc.model.inventory.changes.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * dependencies
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "searchedItem",
    "change"
})
public class GetChangeResponseItem {

    /**
     * dependencies
     * <p>
     * 
     * 
     */
    @JsonProperty("searchedItem")
    private ChangeItem searchedItem;
    /**
     * dependencies
     * <p>
     * 
     * 
     */
    @JsonProperty("change")
    private Change change;

    /**
     * dependencies
     * <p>
     * 
     * 
     */
    @JsonProperty("searchedItem")
    public ChangeItem getSearchedItem() {
        return searchedItem;
    }

    /**
     * dependencies
     * <p>
     * 
     * 
     */
    @JsonProperty("searchedItem")
    public void setSearchedItem(ChangeItem searchedItem) {
        this.searchedItem = searchedItem;
    }

    /**
     * dependencies
     * <p>
     * 
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
     * 
     */
    @JsonProperty("change")
    public void setChange(Change change) {
        this.change = change;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("searchedItem", searchedItem).append("change", change).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(searchedItem).append(change).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof GetChangeResponseItem) == false) {
            return false;
        }
        GetChangeResponseItem rhs = ((GetChangeResponseItem) other);
        return new EqualsBuilder().append(searchedItem, rhs.searchedItem).append(change, rhs.change).isEquals();
    }

}
