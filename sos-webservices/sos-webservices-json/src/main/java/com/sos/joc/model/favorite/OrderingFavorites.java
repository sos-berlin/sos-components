
package com.sos.joc.model.favorite;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ordering favorits
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "predecessorName"
})
public class OrderingFavorites
    extends FavoriteIdentifier
{

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("predecessorName")
    private String predecessorName;

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("predecessorName")
    public String getPredecessorName() {
        return predecessorName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("predecessorName")
    public void setPredecessorName(String predecessorName) {
        this.predecessorName = predecessorName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("predecessorName", predecessorName).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(predecessorName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderingFavorites) == false) {
            return false;
        }
        OrderingFavorites rhs = ((OrderingFavorites) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(predecessorName, rhs.predecessorName).isEquals();
    }

}
