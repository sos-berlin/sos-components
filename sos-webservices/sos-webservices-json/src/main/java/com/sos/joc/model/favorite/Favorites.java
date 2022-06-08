
package com.sos.joc.model.favorite;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * favorites
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "favorites",
    "sharedFavorites"
})
public class Favorites {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    @JsonProperty("favorites")
    private List<Favorite> favorites = new ArrayList<Favorite>();
    @JsonProperty("sharedFavorites")
    private List<Favorite> sharedFavorites = new ArrayList<Favorite>();

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    @JsonProperty("favorites")
    public List<Favorite> getFavorites() {
        return favorites;
    }

    @JsonProperty("favorites")
    public void setFavorites(List<Favorite> favorites) {
        this.favorites = favorites;
    }

    @JsonProperty("sharedFavorites")
    public List<Favorite> getSharedFavorites() {
        return sharedFavorites;
    }

    @JsonProperty("sharedFavorites")
    public void setSharedFavorites(List<Favorite> sharedFavorites) {
        this.sharedFavorites = sharedFavorites;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("favorites", favorites).append("sharedFavorites", sharedFavorites).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(favorites).append(deliveryDate).append(sharedFavorites).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Favorites) == false) {
            return false;
        }
        Favorites rhs = ((Favorites) other);
        return new EqualsBuilder().append(favorites, rhs.favorites).append(deliveryDate, rhs.deliveryDate).append(sharedFavorites, rhs.sharedFavorites).isEquals();
    }

}
