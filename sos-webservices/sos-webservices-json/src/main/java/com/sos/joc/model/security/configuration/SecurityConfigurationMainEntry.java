
package com.sos.joc.model.security.configuration;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "entryName",
    "entryValue",
    "entryComment"
})
public class SecurityConfigurationMainEntry {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("entryName")
    private String entryName;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("entryValue")
    private List<String> entryValue = new ArrayList<String>();
    @JsonProperty("entryComment")
    private List<String> entryComment = new ArrayList<String>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public SecurityConfigurationMainEntry() {
    }

    /**
     * 
     * @param entryComment
     * @param entryName
     * @param entryValue
     */
    public SecurityConfigurationMainEntry(String entryName, List<String> entryValue, List<String> entryComment) {
        super();
        this.entryName = entryName;
        this.entryValue = entryValue;
        this.entryComment = entryComment;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("entryName")
    public String getEntryName() {
        return entryName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("entryName")
    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("entryValue")
    public List<String> getEntryValue() {
        return entryValue;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("entryValue")
    public void setEntryValue(List<String> entryValue) {
        this.entryValue = entryValue;
    }

    @JsonProperty("entryComment")
    public List<String> getEntryComment() {
        return entryComment;
    }

    @JsonProperty("entryComment")
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
