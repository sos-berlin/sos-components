
package com.sos.joc.model.inventory.dependencies.get;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.ConfigurationObject;
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
    "draft",
    "item"
})
public class AffectedResponseItem {

    @JsonProperty("draft")
    private Boolean draft = false;
    /**
     * JS Object configuration
     * <p>
     * 
     * 
     */
    @JsonProperty("item")
    private ConfigurationObject item;

    @JsonProperty("draft")
    public Boolean getDraft() {
        return draft;
    }

    @JsonProperty("draft")
    public void setDraft(Boolean draft) {
        this.draft = draft;
    }

    /**
     * JS Object configuration
     * <p>
     * 
     * 
     */
    @JsonProperty("item")
    public ConfigurationObject getItem() {
        return item;
    }

    /**
     * JS Object configuration
     * <p>
     * 
     * 
     */
    @JsonProperty("item")
    public void setItem(ConfigurationObject item) {
        this.item = item;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("draft", draft).append("item", item).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(item).append(draft).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AffectedResponseItem) == false) {
            return false;
        }
        AffectedResponseItem rhs = ((AffectedResponseItem) other);
        return new EqualsBuilder().append(item, rhs.item).append(draft, rhs.draft).isEquals();
    }

}
