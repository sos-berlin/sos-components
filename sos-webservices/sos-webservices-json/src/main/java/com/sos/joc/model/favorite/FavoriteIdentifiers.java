
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
 * favorite identifiers
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "favoriteIds"
})
public class FavoriteIdentifiers {

    @JsonProperty("favoriteIds")
    private List<FavoriteIdentifier> favoriteIds = new ArrayList<FavoriteIdentifier>();

    @JsonProperty("favoriteIds")
    public List<FavoriteIdentifier> getFavoriteIds() {
        return favoriteIds;
    }

    @JsonProperty("favoriteIds")
    public void setFavoriteIds(List<FavoriteIdentifier> favoriteIds) {
        this.favoriteIds = favoriteIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("favoriteIds", favoriteIds).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(favoriteIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FavoriteIdentifiers) == false) {
            return false;
        }
        FavoriteIdentifiers rhs = ((FavoriteIdentifiers) other);
        return new EqualsBuilder().append(favoriteIds, rhs.favoriteIds).isEquals();
    }

}
