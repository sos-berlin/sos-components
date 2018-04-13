
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
    "entryName",
    "entryValue",
    "entryComment"
})
public class SecurityConfigurationMainEntry {

    @JsonProperty("entryName")
    @JacksonXmlProperty(localName = "entryName")
    private String entryName;
    @JsonProperty("entryValue")
    @JacksonXmlProperty(localName = "entryValue")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "entryValue")
    private List<String> entryValue = new ArrayList<String>();
    @JsonProperty("entryComment")
    @JacksonXmlProperty(localName = "entryComment")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "entryComment")
    private List<String> entryComment = new ArrayList<String>();

    @JsonProperty("entryName")
    @JacksonXmlProperty(localName = "entryName")
    public String getEntryName() {
        return entryName;
    }

    @JsonProperty("entryName")
    @JacksonXmlProperty(localName = "entryName")
    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }

    @JsonProperty("entryValue")
    @JacksonXmlProperty(localName = "entryValue")
    public List<String> getEntryValue() {
        return entryValue;
    }

    @JsonProperty("entryValue")
    @JacksonXmlProperty(localName = "entryValue")
    public void setEntryValue(List<String> entryValue) {
        this.entryValue = entryValue;
    }

    @JsonProperty("entryComment")
    @JacksonXmlProperty(localName = "entryComment")
    public List<String> getEntryComment() {
        return entryComment;
    }

    @JsonProperty("entryComment")
    @JacksonXmlProperty(localName = "entryComment")
    public void setEntryComment(List<String> entryComment) {
        this.entryComment = entryComment;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("entryName", entryName).append("entryValue", entryValue).append("entryComment", entryComment).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(entryValue).append(entryComment).append(entryName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SecurityConfigurationMainEntry) == false) {
            return false;
        }
        SecurityConfigurationMainEntry rhs = ((SecurityConfigurationMainEntry) other);
        return new EqualsBuilder().append(entryValue, rhs.entryValue).append(entryComment, rhs.entryComment).append(entryName, rhs.entryName).isEquals();
    }

}
