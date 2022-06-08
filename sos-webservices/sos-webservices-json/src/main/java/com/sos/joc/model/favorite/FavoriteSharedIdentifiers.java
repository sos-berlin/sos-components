
package com.sos.joc.model.favorite;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * shared favorite identifiers
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "sharedFavoriteIds"
})
public class FavoriteSharedIdentifiers {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sharedFavoriteIds")
    private List<FavoriteSharedIdentifier> sharedFavoriteIds = new ArrayList<FavoriteSharedIdentifier>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sharedFavoriteIds")
    public List<FavoriteSharedIdentifier> getSharedFavoriteIds() {
        return sharedFavoriteIds;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sharedFavoriteIds")
    public void setSharedFavoriteIds(List<FavoriteSharedIdentifier> sharedFavoriteIds) {
        this.sharedFavoriteIds = sharedFavoriteIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("sharedFavoriteIds", sharedFavoriteIds).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(sharedFavoriteIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FavoriteSharedIdentifiers) == false) {
            return false;
        }
        FavoriteSharedIdentifiers rhs = ((FavoriteSharedIdentifiers) other);
        return new EqualsBuilder().append(sharedFavoriteIds, rhs.sharedFavoriteIds).isEquals();
    }

}
