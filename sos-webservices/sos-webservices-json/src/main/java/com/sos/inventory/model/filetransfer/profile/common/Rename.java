
package com.sos.inventory.model.filetransfer.profile.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Rename
 * <p>
 * Rename rule. Usable by any profile.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "replaceWhat",
    "replaceWith"
})
public class Rename {

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("replaceWhat")
    private String replaceWhat;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("replaceWith")
    private String replaceWith;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Rename() {
    }

    /**
     * 
     * @param replaceWhat
     * @param replaceWith
     */
    public Rename(String replaceWhat, String replaceWith) {
        super();
        this.replaceWhat = replaceWhat;
        this.replaceWith = replaceWith;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("replaceWhat")
    public String getReplaceWhat() {
        return replaceWhat;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("replaceWhat")
    public void setReplaceWhat(String replaceWhat) {
        this.replaceWhat = replaceWhat;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("replaceWith")
    public String getReplaceWith() {
        return replaceWith;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("replaceWith")
    public void setReplaceWith(String replaceWith) {
        this.replaceWith = replaceWith;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("replaceWhat", replaceWhat).append("replaceWith", replaceWith).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(replaceWhat).append(replaceWith).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Rename) == false) {
            return false;
        }
        Rename rhs = ((Rename) other);
        return new EqualsBuilder().append(replaceWhat, rhs.replaceWhat).append(replaceWith, rhs.replaceWith).isEquals();
    }

}
