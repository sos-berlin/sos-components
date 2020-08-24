
package com.sos.joc.model.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * login logo
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "height",
    "position"
})
public class LoginLogo {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("height")
    private String height;
    @JsonProperty("position")
    private LoginLogoPosition position;

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("height")
    public String getHeight() {
        return height;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("height")
    public void setHeight(String height) {
        this.height = height;
    }

    @JsonProperty("position")
    public LoginLogoPosition getPosition() {
        return position;
    }

    @JsonProperty("position")
    public void setPosition(LoginLogoPosition position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("height", height).append("position", position).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(position).append(height).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LoginLogo) == false) {
            return false;
        }
        LoginLogo rhs = ((LoginLogo) other);
        return new EqualsBuilder().append(name, rhs.name).append(position, rhs.position).append(height, rhs.height).isEquals();
    }

}
