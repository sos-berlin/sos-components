
package com.sos.joc.model.security.sessions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ActiveSessionsFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "accountName",
    "limit"
})
public class ActiveSessionsFilter {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("accountName")
    private String accountName;
    /**
     * restricts the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    @JsonPropertyDescription("restricts the number of responsed records; -1=unlimited")
    private Integer limit = 10000;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ActiveSessionsFilter() {
    }

    /**
     * 
     * @param accountName
     * @param limit
     */
    public ActiveSessionsFilter(String accountName, Integer limit) {
        super();
        this.accountName = accountName;
        this.limit = limit;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("accountName")
    public String getAccountName() {
        return accountName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("accountName")
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    /**
     * restricts the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    /**
     * restricts the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("accountName", accountName).append("limit", limit).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(limit).append(accountName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ActiveSessionsFilter) == false) {
            return false;
        }
        ActiveSessionsFilter rhs = ((ActiveSessionsFilter) other);
        return new EqualsBuilder().append(limit, rhs.limit).append(accountName, rhs.accountName).isEquals();
    }

}
