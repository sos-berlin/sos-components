
package com.sos.joc.model.configuration;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * configurationsDeleteFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "accounts"
})
public class ConfigurationsDeleteFilter {

    @JsonProperty("accounts")
    private List<String> accounts = new ArrayList<String>();

    /**
     * 
     * @return
     *     The accounts
     */
    @JsonProperty("accounts")
    public List<String> getAccounts() {
        return accounts;
    }

    /**
     * 
     * @param accounts
     *     The accounts
     */
    @JsonProperty("accounts")
    public void setAccounts(List<String> accounts) {
        this.accounts = accounts;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(accounts).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ConfigurationsDeleteFilter) == false) {
            return false;
        }
        ConfigurationsDeleteFilter rhs = ((ConfigurationsDeleteFilter) other);
        return new EqualsBuilder().append(accounts, rhs.accounts).isEquals();
    }

}
