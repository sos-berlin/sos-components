
package com.sos.joc.model.yade.xmleditor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * xmleditor yade read profiles answer
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "profiles"
})
public class ProfilesAnswer {

    @JsonProperty("profiles")
    private Object profiles;

    @JsonProperty("profiles")
    public Object getProfiles() {
        return profiles;
    }

    @JsonProperty("profiles")
    public void setProfiles(Object profiles) {
        this.profiles = profiles;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("profiles", profiles).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(profiles).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProfilesAnswer) == false) {
            return false;
        }
        ProfilesAnswer rhs = ((ProfilesAnswer) other);
        return new EqualsBuilder().append(profiles, rhs.profiles).isEquals();
    }

}
