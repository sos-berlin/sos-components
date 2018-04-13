
package com.sos.joc.model.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "excluded"
})
public class SecurityConfigurationPermission {

    @JsonProperty("path")
    @JacksonXmlProperty(localName = "path")
    private String path;
    @JsonProperty("excluded")
    @JacksonXmlProperty(localName = "excluded")
    private Boolean excluded;

    @JsonProperty("path")
    @JacksonXmlProperty(localName = "path")
    public String getPath() {
        return path;
    }

    @JsonProperty("path")
    @JacksonXmlProperty(localName = "path")
    public void setPath(String path) {
        this.path = path;
    }

    @JsonProperty("excluded")
    @JacksonXmlProperty(localName = "excluded")
    public Boolean getExcluded() {
        return excluded;
    }

    @JsonProperty("excluded")
    @JacksonXmlProperty(localName = "excluded")
    public void setExcluded(Boolean excluded) {
        this.excluded = excluded;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("path", path).append("excluded", excluded).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(excluded).append(path).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SecurityConfigurationPermission) == false) {
            return false;
        }
        SecurityConfigurationPermission rhs = ((SecurityConfigurationPermission) other);
        return new EqualsBuilder().append(excluded, rhs.excluded).append(path, rhs.path).isEquals();
    }

}
