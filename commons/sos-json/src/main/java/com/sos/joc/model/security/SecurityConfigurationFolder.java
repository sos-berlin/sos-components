
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
    "folder",
    "recursive"
})
public class SecurityConfigurationFolder {

    @JsonProperty("folder")
    @JacksonXmlProperty(localName = "folder")
    private String folder;
    @JsonProperty("recursive")
    @JacksonXmlProperty(localName = "recursive")
    private Boolean recursive;

    @JsonProperty("folder")
    @JacksonXmlProperty(localName = "folder")
    public String getFolder() {
        return folder;
    }

    @JsonProperty("folder")
    @JacksonXmlProperty(localName = "folder")
    public void setFolder(String folder) {
        this.folder = folder;
    }

    @JsonProperty("recursive")
    @JacksonXmlProperty(localName = "recursive")
    public Boolean getRecursive() {
        return recursive;
    }

    @JsonProperty("recursive")
    @JacksonXmlProperty(localName = "recursive")
    public void setRecursive(Boolean recursive) {
        this.recursive = recursive;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("folder", folder).append("recursive", recursive).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(recursive).append(folder).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SecurityConfigurationFolder) == false) {
            return false;
        }
        SecurityConfigurationFolder rhs = ((SecurityConfigurationFolder) other);
        return new EqualsBuilder().append(recursive, rhs.recursive).append(folder, rhs.folder).isEquals();
    }

}
