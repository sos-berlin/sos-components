
package com.sos.joc.model.security;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "entryName",
    "entryValue",
    "entryComment"
})
public class SecurityConfigurationMainEntry {

    @JsonProperty("entryName")
    private String entryName;
    @JsonProperty("entryValue")
    private List<String> entryValue = new ArrayList<String>();
    @JsonProperty("entryComment")
    private List<String> entryComment = new ArrayList<String>();

    /**
     * 
     * @return
     *     The entryName
     */
    @JsonProperty("entryName")
    public String getEntryName() {
        return entryName;
    }

    /**
     * 
     * @param entryName
     *     The entryName
     */
    @JsonProperty("entryName")
    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }

    /**
     * 
     * @return
     *     The entryValue
     */
    @JsonProperty("entryValue")
    public List<String> getEntryValue() {
        return entryValue;
    }

    /**
     * 
     * @param entryValue
     *     The entryValue
     */
    @JsonProperty("entryValue")
    public void setEntryValue(List<String> entryValue) {
        this.entryValue = entryValue;
    }

    /**
     * 
     * @return
     *     The entryComment
     */
    @JsonProperty("entryComment")
    public List<String> getEntryComment() {
        return entryComment;
    }

    /**
     * 
     * @param entryComment
     *     The entryComment
     */
    @JsonProperty("entryComment")
    public void setEntryComment(List<String> entryComment) {
        this.entryComment = entryComment;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(entryName).append(entryValue).append(entryComment).toHashCode();
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
        return new EqualsBuilder().append(entryName, rhs.entryName).append(entryValue, rhs.entryValue).append(entryComment, rhs.entryComment).isEquals();
    }

}
