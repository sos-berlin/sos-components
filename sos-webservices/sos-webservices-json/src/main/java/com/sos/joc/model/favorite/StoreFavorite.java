
package com.sos.joc.model.favorite;

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
    "content",
    "shared"
})
public class StoreFavorite
    extends FavoriteIdentifier
{

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("content")
    private String content;
    @JsonProperty("shared")
    private Boolean shared;

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("content")
    public String getContent() {
        return content;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("content")
    public void setContent(String content) {
        this.content = content;
    }

    @JsonProperty("shared")
    public Boolean getShared() {
        return shared;
    }

    @JsonProperty("shared")
    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("content", content).append("shared", shared).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(shared).append(content).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StoreFavorite) == false) {
            return false;
        }
        StoreFavorite rhs = ((StoreFavorite) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(shared, rhs.shared).append(content, rhs.content).isEquals();
    }

}
