
package com.sos.joc.model.inventory.search;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Filter Inventory search
 * <p>
 * returnType can only be set with starting a new search, i. e. empty token
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "search",
    "returnType",
    "returnTypes",
    "token",
    "quit"
})
public class RequestQuickSearchFilter {

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
    /**
     * Inventory search return type
     * <p>
     * 
     * 
     */
    @JsonProperty("returnType")
    private RequestSearchReturnType returnType;
    @JsonProperty("returnTypes")
    private List<RequestSearchReturnType> returnTypes = new ArrayList<RequestSearchReturnType>();
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

    /**
     * Inventory search return type
     * <p>
     * 
     * 
     */
    @JsonProperty("returnType")
    public RequestSearchReturnType getReturnType() {
        return returnType;
    }

    /**
     * Inventory search return type
     * <p>
     * 
     * 
     */
    @JsonProperty("returnType")
    public void setReturnType(RequestSearchReturnType returnType) {
        this.returnType = returnType;
    }

    @JsonProperty("returnTypes")
    public List<RequestSearchReturnType> getReturnTypes() {
        return returnTypes;
    }

    @JsonProperty("returnTypes")
    public void setReturnTypes(List<RequestSearchReturnType> returnTypes) {
        this.returnTypes = returnTypes;
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
        return new ToStringBuilder(this).append("search", search).append("returnType", returnType).append("returnTypes", returnTypes).append("token", token).append("quit", quit).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(returnTypes).append(search).append(quit).append(returnType).append(token).toHashCode();
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
        return new EqualsBuilder().append(returnTypes, rhs.returnTypes).append(search, rhs.search).append(quit, rhs.quit).append(returnType, rhs.returnType).append(token, rhs.token).isEquals();
    }

}
