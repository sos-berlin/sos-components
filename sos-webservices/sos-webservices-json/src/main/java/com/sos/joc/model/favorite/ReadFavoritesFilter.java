
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
 * favorite object
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "types",
    "limit",
    "withShared",
    "onlyShared"
})
public class ReadFavoritesFilter
    extends FavoriteIdentifiers
{

    @JsonProperty("types")
    private List<FavoriteType> types = new ArrayList<FavoriteType>();
    @JsonProperty("limit")
    private Integer limit = -1;
    @JsonProperty("withShared")
    private Boolean withShared = false;
    @JsonProperty("onlyShared")
    private Boolean onlyShared = false;

    @JsonProperty("types")
    public List<FavoriteType> getTypes() {
        return types;
    }

    @JsonProperty("types")
    public void setTypes(List<FavoriteType> types) {
        this.types = types;
    }

    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    @JsonProperty("limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @JsonProperty("withShared")
    public Boolean getWithShared() {
        return withShared;
    }

    @JsonProperty("withShared")
    public void setWithShared(Boolean withShared) {
        this.withShared = withShared;
    }

    @JsonProperty("onlyShared")
    public Boolean getOnlyShared() {
        return onlyShared;
    }

    @JsonProperty("onlyShared")
    public void setOnlyShared(Boolean onlyShared) {
        this.onlyShared = onlyShared;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("types", types).append("limit", limit).append("withShared", withShared).append("onlyShared", onlyShared).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(limit).append(types).append(onlyShared).append(withShared).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReadFavoritesFilter) == false) {
            return false;
        }
        ReadFavoritesFilter rhs = ((ReadFavoritesFilter) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(limit, rhs.limit).append(types, rhs.types).append(onlyShared, rhs.onlyShared).append(withShared, rhs.withShared).isEquals();
    }

}
