
package com.sos.joc.model.auth.token;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * POJO for show one time token(s) response
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "tokens"
})
public class OnetimeTokensResponse {

    @JsonProperty("tokens")
    private List<OnetimeToken> tokens = new ArrayList<OnetimeToken>();

    @JsonProperty("tokens")
    public List<OnetimeToken> getTokens() {
        return tokens;
    }

    @JsonProperty("tokens")
    public void setTokens(List<OnetimeToken> tokens) {
        this.tokens = tokens;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tokens", tokens).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tokens).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OnetimeTokensResponse) == false) {
            return false;
        }
        OnetimeTokensResponse rhs = ((OnetimeTokensResponse) other);
        return new EqualsBuilder().append(tokens, rhs.tokens).isEquals();
    }

}
