
package com.sos.joc.model.inventory.search;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Filter Inventory search
 * <p>
 * returnType can only be set with starting a new search, i. e. empty token
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "returnTypes"
})
public class RequestQuickSearchFilter
    extends RequestBaseQuickSearchFilter
{

    @JsonProperty("returnTypes")
    private List<RequestSearchReturnType> returnTypes = new ArrayList<RequestSearchReturnType>();

    @JsonProperty("returnTypes")
    public List<RequestSearchReturnType> getReturnTypes() {
        return returnTypes;
    }

    @JsonProperty("returnTypes")
    public void setReturnTypes(List<RequestSearchReturnType> returnTypes) {
        this.returnTypes = returnTypes;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("returnTypes", returnTypes).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(returnTypes).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RequestQuickSearchFilter) == false) {
            return false;
        }
        RequestQuickSearchFilter rhs = ((RequestQuickSearchFilter) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(returnTypes, rhs.returnTypes).isEquals();
    }

}
