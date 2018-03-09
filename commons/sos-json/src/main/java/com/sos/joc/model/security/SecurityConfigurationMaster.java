
package com.sos.joc.model.security;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "master",
    "roles"
})
public class SecurityConfigurationMaster {

    @JsonProperty("master")
    @JacksonXmlProperty(localName = "master")
    private String master;
    @JsonProperty("roles")
    @JacksonXmlProperty(localName = "role")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "roles")
    private List<SecurityConfigurationRole> roles = new ArrayList<SecurityConfigurationRole>();

    @JsonProperty("master")
    @JacksonXmlProperty(localName = "master")
    public String getMaster() {
        return master;
    }

    @JsonProperty("master")
    @JacksonXmlProperty(localName = "master")
    public void setMaster(String master) {
        this.master = master;
    }

    @JsonProperty("roles")
    @JacksonXmlProperty(localName = "role")
    public List<SecurityConfigurationRole> getRoles() {
        return roles;
    }

    @JsonProperty("roles")
    @JacksonXmlProperty(localName = "role")
    public void setRoles(List<SecurityConfigurationRole> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("master", master).append("roles", roles).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(master).append(roles).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SecurityConfigurationMaster) == false) {
            return false;
        }
        SecurityConfigurationMaster rhs = ((SecurityConfigurationMaster) other);
        return new EqualsBuilder().append(master, rhs.master).append(roles, rhs.roles).isEquals();
    }

}
