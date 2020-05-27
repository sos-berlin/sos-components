
package com.sos.joc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * version
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "version",
    "gitHash",
    "date"
})
public class Version {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    private String version;
    @JsonProperty("gitHash")
    private String gitHash;
    @JsonProperty("date")
    private String date;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("gitHash")
    public String getGitHash() {
        return gitHash;
    }

    @JsonProperty("gitHash")
    public void setGitHash(String gitHash) {
        this.gitHash = gitHash;
    }

    @JsonProperty("date")
    public String getDate() {
        return date;
    }

    @JsonProperty("date")
    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("version", version).append("gitHash", gitHash).append("date", date).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(date).append(gitHash).append(version).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Version) == false) {
            return false;
        }
        Version rhs = ((Version) other);
        return new EqualsBuilder().append(date, rhs.date).append(gitHash, rhs.gitHash).append(version, rhs.version).isEquals();
    }

}
