
package com.sos.joc.model.inventory.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
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
    "search",
    "token",
    "quit"
})
public class RequestBaseQuickSearchFilter {

    /**
     * glob pattern
     * <p>
     * pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character
     * (Required)
     * 
     */
    @JsonProperty("search")
    @JsonPropertyDescription("pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character")
    private String search;
    @JsonProperty("token")
    private String token;
    @JsonProperty("quit")
    private Boolean quit = false;

    /**
     * glob pattern
     * <p>
     * pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character
     * (Required)
     * 
     */
    @JsonProperty("search")
    public String getSearch() {
        return search;
    }

    /**
     * glob pattern
     * <p>
     * pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character
     * (Required)
     * 
     */
    @JsonProperty("search")
    public void setSearch(String search) {
        this.search = search;
    }

    @JsonProperty("token")
    public String getToken() {
        return token;
    }

    @JsonProperty("token")
    public void setToken(String token) {
        this.token = token;
    }

    @JsonProperty("quit")
    public Boolean getQuit() {
        return quit;
    }

    @JsonProperty("quit")
    public void setQuit(Boolean quit) {
        this.quit = quit;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("search", search).append("token", token).append("quit", quit).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(search).append(quit).append(token).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RequestBaseQuickSearchFilter) == false) {
            return false;
        }
        RequestBaseQuickSearchFilter rhs = ((RequestBaseQuickSearchFilter) other);
        return new EqualsBuilder().append(search, rhs.search).append(quit, rhs.quit).append(token, rhs.token).isEquals();
    }

}
