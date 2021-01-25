
package com.sos.joc.model.controller;

import java.net.URI;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "title",
    "url",
    "clusterUrl",
    "role"
})
public class RegisterParameter {

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    private Long id;
    @JsonProperty("title")
    private String title;
    /**
     * uri
     * <p>
     * URI of a Controller
     * (Required)
     * 
     */
    @JsonProperty("url")
    @JsonPropertyDescription("URI of a Controller")
    private URI url;
    /**
     * uri
     * <p>
     * URI of a Controller
     * 
     */
    @JsonProperty("clusterUrl")
    @JsonPropertyDescription("URI of a Controller")
    private URI clusterUrl;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("role")
    private Role role;

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * uri
     * <p>
     * URI of a Controller
     * (Required)
     * 
     */
    @JsonProperty("url")
    public URI getUrl() {
        return url;
    }

    /**
     * uri
     * <p>
     * URI of a Controller
     * (Required)
     * 
     */
    @JsonProperty("url")
    public void setUrl(URI url) {
        this.url = url;
    }

    /**
     * uri
     * <p>
     * URI of a Controller
     * 
     */
    @JsonProperty("clusterUrl")
    public URI getClusterUrl() {
        return clusterUrl;
    }

    /**
     * uri
     * <p>
     * URI of a Controller
     * 
     */
    @JsonProperty("clusterUrl")
    public void setClusterUrl(URI clusterUrl) {
        this.clusterUrl = clusterUrl;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("role")
    public Role getRole() {
        return role;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("role")
    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("title", title).append("url", url).append("clusterUrl", clusterUrl).append("role", role).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(role).append(title).append(url).append(clusterUrl).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RegisterParameter) == false) {
            return false;
        }
        RegisterParameter rhs = ((RegisterParameter) other);
        return new EqualsBuilder().append(id, rhs.id).append(role, rhs.role).append(title, rhs.title).append(url, rhs.url).append(clusterUrl, rhs.clusterUrl).isEquals();
    }

}
