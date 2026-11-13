
package com.sos.inventory.model.filetransfer.profile.source;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Result Set
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "resultSetFile",
    "checkResultSetCount"
})
public class ResultSet {

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("resultSetFile")
    private String resultSetFile;
    @JsonProperty("checkResultSetCount")
    private CheckResultSetCount checkResultSetCount;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ResultSet() {
    }

    /**
     * 
     * @param resultSetFile
     * @param checkResultSetCount
     */
    public ResultSet(String resultSetFile, CheckResultSetCount checkResultSetCount) {
        super();
        this.resultSetFile = resultSetFile;
        this.checkResultSetCount = checkResultSetCount;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("resultSetFile")
    public String getResultSetFile() {
        return resultSetFile;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("resultSetFile")
    public void setResultSetFile(String resultSetFile) {
        this.resultSetFile = resultSetFile;
    }

    @JsonProperty("checkResultSetCount")
    public CheckResultSetCount getCheckResultSetCount() {
        return checkResultSetCount;
    }

    @JsonProperty("checkResultSetCount")
    public void setCheckResultSetCount(CheckResultSetCount checkResultSetCount) {
        this.checkResultSetCount = checkResultSetCount;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("resultSetFile", resultSetFile).append("checkResultSetCount", checkResultSetCount).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(resultSetFile).append(checkResultSetCount).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResultSet) == false) {
            return false;
        }
        ResultSet rhs = ((ResultSet) other);
        return new EqualsBuilder().append(resultSetFile, rhs.resultSetFile).append(checkResultSetCount, rhs.checkResultSetCount).isEquals();
    }

}
