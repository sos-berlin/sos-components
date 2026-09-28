
package com.sos.joc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.joc.PolicyValue;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "userNotes",
    "linkInObjectTitle"
})
public class Policies {

    /**
     * Policy Values
     * <p>
     * 
     * 
     */
    @JsonProperty("userNotes")
    private PolicyValue userNotes = PolicyValue.fromValue("enabled");
    /**
     * Policy Values
     * <p>
     * 
     * 
     */
    @JsonProperty("linkInObjectTitle")
    private PolicyValue linkInObjectTitle = PolicyValue.fromValue("enabled");

    /**
     * Policy Values
     * <p>
     * 
     * 
     */
    @JsonProperty("userNotes")
    public PolicyValue getUserNotes() {
        return userNotes;
    }

    /**
     * Policy Values
     * <p>
     * 
     * 
     */
    @JsonProperty("userNotes")
    public void setUserNotes(PolicyValue userNotes) {
        this.userNotes = userNotes;
    }

    /**
     * Policy Values
     * <p>
     * 
     * 
     */
    @JsonProperty("linkInObjectTitle")
    public PolicyValue getLinkInObjectTitle() {
        return linkInObjectTitle;
    }

    /**
     * Policy Values
     * <p>
     * 
     * 
     */
    @JsonProperty("linkInObjectTitle")
    public void setLinkInObjectTitle(PolicyValue linkInObjectTitle) {
        this.linkInObjectTitle = linkInObjectTitle;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("userNotes", userNotes).append("linkInObjectTitle", linkInObjectTitle).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(userNotes).append(linkInObjectTitle).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Policies) == false) {
            return false;
        }
        Policies rhs = ((Policies) other);
        return new EqualsBuilder().append(userNotes, rhs.userNotes).append(linkInObjectTitle, rhs.linkInObjectTitle).isEquals();
    }

}
