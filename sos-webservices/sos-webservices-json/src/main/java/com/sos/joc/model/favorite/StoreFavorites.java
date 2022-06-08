
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
 * store favorites
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "favorites"
})
public class StoreFavorites {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("favorites")
    private List<StoreFavorite> favorites = new ArrayList<StoreFavorite>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("favorites")
    public List<StoreFavorite> getFavorites() {
        return favorites;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("favorites")
    public void setFavorites(List<StoreFavorite> favorites) {
        this.favorites = favorites;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("favorites", favorites).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(favorites).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StoreFavorites) == false) {
            return false;
        }
        StoreFavorites rhs = ((StoreFavorites) other);
        return new EqualsBuilder().append(favorites, rhs.favorites).isEquals();
    }

}
