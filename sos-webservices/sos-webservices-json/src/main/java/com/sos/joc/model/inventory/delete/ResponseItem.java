
package com.sos.joc.model.inventory.delete;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * response Delete Draft
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deleteFromTree"
})
public class ResponseItem {

    @JsonProperty("deleteFromTree")
    private Boolean deleteFromTree;

    @JsonProperty("deleteFromTree")
    public Boolean getDeleteFromTree() {
        return deleteFromTree;
    }

    @JsonProperty("deleteFromTree")
    public void setDeleteFromTree(Boolean deleteFromTree) {
        this.deleteFromTree = deleteFromTree;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deleteFromTree", deleteFromTree).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deleteFromTree).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResponseItem) == false) {
            return false;
        }
        ResponseItem rhs = ((ResponseItem) other);
        return new EqualsBuilder().append(deleteFromTree, rhs.deleteFromTree).isEquals();
    }

}
