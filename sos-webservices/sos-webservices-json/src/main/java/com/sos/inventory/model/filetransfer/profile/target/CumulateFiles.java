
package com.sos.inventory.model.filetransfer.profile.target;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "cumulativeFileDelete",
    "cumulativeFilename",
    "cumulativeFileSeparator"
})
public class CumulateFiles {

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("cumulativeFileDelete")
    private String cumulativeFileDelete;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("cumulativeFilename")
    private String cumulativeFilename;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("cumulativeFileSeparator")
    private String cumulativeFileSeparator;

    /**
     * No args constructor for use in serialization
     * 
     */
    public CumulateFiles() {
    }

    /**
     * 
     * @param cumulativeFilename
     * @param cumulativeFileSeparator
     * @param cumulativeFileDelete
     */
    public CumulateFiles(String cumulativeFileDelete, String cumulativeFilename, String cumulativeFileSeparator) {
        super();
        this.cumulativeFileDelete = cumulativeFileDelete;
        this.cumulativeFilename = cumulativeFilename;
        this.cumulativeFileSeparator = cumulativeFileSeparator;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("cumulativeFileDelete")
    public String getCumulativeFileDelete() {
        return cumulativeFileDelete;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("cumulativeFileDelete")
    public void setCumulativeFileDelete(String cumulativeFileDelete) {
        this.cumulativeFileDelete = cumulativeFileDelete;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("cumulativeFilename")
    public String getCumulativeFilename() {
        return cumulativeFilename;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("cumulativeFilename")
    public void setCumulativeFilename(String cumulativeFilename) {
        this.cumulativeFilename = cumulativeFilename;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("cumulativeFileSeparator")
    public String getCumulativeFileSeparator() {
        return cumulativeFileSeparator;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("cumulativeFileSeparator")
    public void setCumulativeFileSeparator(String cumulativeFileSeparator) {
        this.cumulativeFileSeparator = cumulativeFileSeparator;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("cumulativeFileDelete", cumulativeFileDelete).append("cumulativeFilename", cumulativeFilename).append("cumulativeFileSeparator", cumulativeFileSeparator).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(cumulativeFilename).append(cumulativeFileSeparator).append(cumulativeFileDelete).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CumulateFiles) == false) {
            return false;
        }
        CumulateFiles rhs = ((CumulateFiles) other);
        return new EqualsBuilder().append(cumulativeFilename, rhs.cumulativeFilename).append(cumulativeFileSeparator, rhs.cumulativeFileSeparator).append(cumulativeFileDelete, rhs.cumulativeFileDelete).isEquals();
    }

}
